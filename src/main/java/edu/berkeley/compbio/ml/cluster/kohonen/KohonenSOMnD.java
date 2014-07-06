/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.SimpleFunction;
import edu.berkeley.compbio.ml.cluster.AbstractUnsupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * Kohonen Self Organizing Map implementation for a rectangular grid of arbitrary dimensions.
 * <p/>
 * The standard algorithm moves the cells towards the winner cell; in our implementation we also have the option to
 * remove the point from the cell where it was previously assigned, moving the neighbors away from the moved node a
 * bit.
 * <p/>
 * Note that because this is an "online" method, we can't do PCA or whatever to initialize the grid.  That's OK; we'll
 * just initialize the grid with a uniform prototype; after placing the first incoming point with a neighborhood
 * encompassing the whole grid, all cells will be differentiated.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public class KohonenSOMnD<T extends AdditiveClusterable<T>>
		extends AbstractUnsupervisedOnlineClusteringMethod<T, KohonenSOMCell<T>> implements KohonenSOM<T>
	{
// ------------------------------ FIELDS ------------------------------

	// we jump through some hoops to avoid actually storing the cells in an array,
	// since we don't know a priori how many dimensions it should have, and it would be redundant with
	// OnlineClusteringMethod.theClusters

	// aha: since theClusters is a list, we can map our array into it.  see listIndexFor(int[] cellposition)
	private static final Logger logger = Logger.getLogger(KohonenSOMnD.class);

	// how many cells wide is the grid along each axis
	final int[] cellsPerDimension;

	// the product of the first i dimensions, precomputed for convenience
	final int[] blockSize;

	//Map<Vector<Integer>, T> centroidsByPosition;

	int time = 0;

	final KohonenSOMCell<T>[] immediateNeighbors;

	//private DissimilarityMeasure<T> measure;
	private final int dimensions;
	private final boolean edgesWrap;

	private final boolean decrementLosingNeighborhood;

	private final SimpleFunction moveFactorFunction;
	private final SimpleFunction radiusFunction;

	/*
	 private void initializeClusters(List<Interval<Double>> axisRanges)
		 {

		 }
 */
	private int idCount = 0;

	private final double defaultMaxRadius;


// --------------------------- CONSTRUCTORS ---------------------------

	public KohonenSOMnD(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                    final Map<String, Set<String>> predictLabelSets, final ProhibitionModel<T> prohibitionModel,
	                    final Set<String> testLabels, final int[] cellsPerDimension,
	                    final SimpleFunction moveFactorFunction, final SimpleFunction radiusFunction,
	                    final boolean decrementLosingNeighborhood, final boolean edgesWrap)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);

		this.cellsPerDimension = cellsPerDimension;

		this.dimensions = cellsPerDimension.length;
		this.moveFactorFunction = moveFactorFunction;
		this.radiusFunction = radiusFunction;
		this.decrementLosingNeighborhood = decrementLosingNeighborhood;
		this.edgesWrap = edgesWrap;

		immediateNeighbors = new KohonenSOMCell[2 * dimensions];

		// precompute stuff for listIndexFor
		blockSize = new int[dimensions];
		blockSize[dimensions - 1] = 1;
		for (int i = dimensions - 2; i > 0; i--)
			{
			blockSize[i] = blockSize[i + 1] * cellsPerDimension[i];
			}


		//List<Interval<Double>> axisRanges;
		//	initializeClusters(axisRanges);

		defaultMaxRadius = DSArrayUtils.norm(cellsPerDimension);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidClusteringMethod ---------------------


	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(getClusters(), measure);
		}

	/*
   private class OldBogusNeighborhoodIterator implements Iterator<KohonenSOMCell<T>>
	   {
	   double radius;

	   KohonenSOMCell<T> center;
	   // this will likely need optimizing later
	   Set<KohonenSOMCell<T>> todo = new HashSet<KohonenSOMCell<T>>();
	   Set<KohonenSOMCell<T>> done = new HashSet<KohonenSOMCell<T>>();


	   private NeighborhoodIterator(KohonenSOMCell<T> center, int time)
		   {
		   this.center = center;
		   radius = radiusFunction.f(time);
		   todo.add(center);
		   }

	   public boolean hasNext()
		   {
		   return !todo.isEmpty();
		   }

	   public KohonenSOMCell<T> next()
		   {
		   KohonenSOMCell<T> trav = todo.iterator().next();
		   done.add(trav);

		   computeImmediateNeighbors(trav);
		   for (KohonenSOMCell<T> neighbor : immediateNeighbors)
			   {
			   // careful not to repeat cells when the radius is large
			   // no problem, the done list deals with that

			   // optimizations possible here, i.e. test squares inscribed in circle first before doing sqrt
			   if (neighbor != null && euclideanDistance(neighbor, center) <= radius && !done.contains(neighbor))
				   {
				   todo.add(neighbor);
				   }
			   }
		   return trav;
		   }

	   private double euclideanDistance(KohonenSOMCell<T> neighbor, KohonenSOMCell<T> center)
		   {
		   int[] a = cellPositionFor(theClusters.indexOf(neighbor));
		   int[] b = cellPositionFor(theClusters.indexOf(center));

		   int sum = 0;
		   for (int i = 0; i < dimensions; i++)
			   {
			   int dist = a[i] - b[i];
			   if (edgesWrap)
				   {
				   dist = Math.min(dist, b[i] - a[i]);
				   }
			   sum += dist * dist;
			   }
		   return Math.sqrt(sum);
		   }


	   public void remove()
		   {
		   throw new NotImplementedException();
		   }
	   }*/


	public void computeClusterStdDevs(final ClusterableIterator<T> theDataPointProvider)
		{
		CentroidClusteringUtils.computeClusterStdDevs(getClusters(), measure, getAssignments(), theDataPointProvider);
		}

	@Override
	public String clusteringStats()
		{
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(final OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, outf);
		}

// --------------------- Interface DiffusableLabelClusteringMethod ---------------------


	/**
	 * {@inheritDoc}
	 */
	public Iterator<Set<KohonenSOMCell<T>>> getNeighborhoodShellIterator(final KohonenSOMCell<T> cell)
		{
		return new NeighborhoodShellIterator(cell, defaultMaxRadius);
		}

// --------------------- Interface OnlineClusteringMethod ---------------------

	public boolean add(final T p) throws NoGoodClusterException
		{
		final ClusterMove cm = bestClusterMove(p);
		final KohonenSOMCell<T> loser = (KohonenSOMCell<T>) cm.oldCluster;
		final KohonenSOMCell<T> winner = (KohonenSOMCell<T>) cm.bestCluster;

		// REVIEW decrementLosingNeighborhood has issues, see 2d version
		if (decrementLosingNeighborhood)
			{
			for (Iterator<KohonenSOMCell<T>> i = new NeighborhoodIterator(loser, time); i.hasNext();)
				{
				final KohonenSOMCell<T> neighbor = i.next();
				final T motion = p.minus(neighbor.getCentroid());
				motion.multiplyBy(-moveFactor(time));
				neighbor.add(motion);
				}
			}

		for (Iterator<KohonenSOMCell<T>> i = new NeighborhoodIterator(winner, time); i.hasNext();)
			{
			final KohonenSOMCell<T> neighbor = i.next();
			final T motion = p.minus(neighbor.getCentroid());
			motion.multiplyBy(moveFactor(time));
			neighbor.add(motion);
			}
		time++;
		return true;
		}

// --------------------- Interface PrototypeBasedCentroidClusteringMethod ---------------------

	public void setPrototypeFactory(final GenericFactory<T> prototypeFactory) throws GenericFactoryException
		{
		final int[] zeroCell = new int[dimensions];
		Arrays.fill(zeroCell, 0);
		createClusters(zeroCell, -1, prototypeFactory);
		}

// --------------------- Interface SampleInitializedOnlineClusteringMethod ---------------------


	public void initializeWithSamples(final ClusterableIterator<T> initIterator, final int initSamples
	                                  // ,GenericFactory<T> prototypeFactory
	) //throws GenericFactoryException
		//	, GenericFactory<T> prototypeFactory) throws GenericFactoryException
		{
		for (int i = 0; i < initSamples; i++)
			//int i = 0;
			//while(initIterator.hasNext())
			{
			addToRandomCell(initIterator.nextFullyLabelled());
			if (i % 100 == 0)
				{
				logger.debug("Initialized with " + i + " samples.");
				}
			//i++;
			}
		}

// -------------------------- OTHER METHODS --------------------------

	public void addToRandomCell(final T p)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	/*	@Override
	 public Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
			 throws ClusterException, NoGoodClusterException
		 {
		 ClusterMove cm = bestClusterMove(p);
		 return cm.bestCluster;
		 }
 */
	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	@Override
	public ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(final T p)
		{
		final ClusterMove result = new ClusterMove();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		final String id = p.getId();
		result.oldCluster = getAssignment(id);

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}

		// PERF brute force search, and re-creating the immutable cluster list on every call (swamped by the distance measure anyway, probably)
		for (final CentroidCluster<T> c : getClusters())
			{
			// grid already initialized with prototype, never mind all this stuff

			/*
			// while initializing the grid, cell centroids are null.  In that case, just assign the present point.
			// no, this won't work right at all
			// why not?? PCA would be better, but this should work, just slowly.
			// aha: if there are more grid points than samples
			if (c.getCentroid() == null)
				{
				c.setCentroid(p.clone());
				result.bestDistance = 0;
				result.bestCluster = c;
				return result;
				}
*/
			// otherwise find the nearest cluster
			final double d = measure.distanceFromTo(p, c.getCentroid());//c.distanceToCentroid(p);
			if (logger.isTraceEnabled())
				{
				logger.trace("Trying " + c + "; distance = " + d + "; best so far = " + result.bestDistance);
				}
			if (d < result.bestDistance)
				{
				result.secondBestDistance = result.bestDistance;
				result.bestDistance = d;
				result.bestCluster = c;
				}
			else if (d < result.secondBestDistance)
				{
				result.secondBestDistance = d;
				}
			}
		if (logger.isDebugEnabled())
			{
			logger.trace("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			logger.warn("Can't classify: " + p);
			assert false;
			}
		return result;
		}

	private int[] cellPositionFor(int listIndex)
		{
		final int[] result = new int[dimensions];
		for (int i = 0; i < dimensions; i++)
			{
			result[i] = listIndex / blockSize[i];
			listIndex = listIndex % blockSize[i];
			}
		return result;
		}

	/**
	 * Create a rectangular grid of cells using the given dimensionality and size, assigning a null vector to each
	 *
	 * @param cellPosition
	 * @param changingDimension // * @param prototype
	 */
	private void createClusters(final int[] cellPosition, int changingDimension,
	                            final GenericFactory<T> prototypeFactory) throws GenericFactoryException
		{
		changingDimension++;
		if (changingDimension == dimensions)
			{
			final KohonenSOMCell<T> c = new KohonenSOMCell<T>(idCount++, prototypeFactory.create());//measure,
			setCluster(listIndexFor(cellPosition), c);
			}
		else
			{
			for (int i = 0; i < cellsPerDimension[changingDimension]; i++)
				{
				cellPosition[changingDimension] = i;
				createClusters(cellPosition, changingDimension, prototypeFactory);
				}
			}

		/*  for (int i = 0; i < k; i++) {
			  // initialize the clusters with the first k points

			  Cluster<T> c = new AdditiveCluster<T>(measure);
			  c.setId(i);

			  theClusters.add(c);
		  }
		  logger.debug("initialized " + k + " clusters");*/
		}


	/**
	 * assumes inputs are entirely positive and within the bounds given by cellsPerDimension
	 *
	 * @param cellposition
	 * @return
	 */
	private int listIndexFor(final int[] cellposition)
		{
		assert cellposition.length == cellsPerDimension.length;
		int result = 0;
		for (int i = 0; i < dimensions; i++)
			{
			result += cellposition[i] * blockSize[i];
			}
		return result;
		}

	private double moveFactor(final int time)
		{
		return moveFactorFunction.f(time);
		//throw new NotImplementedException();
		}

// -------------------------- INNER CLASSES --------------------------

	private class NeighborhoodShellIterator implements Iterator<Set<KohonenSOMCell<T>>>
		{
// ------------------------------ FIELDS ------------------------------

		int radius = 0;
		final int[] centerPos;


		Set<KohonenSOMCell<T>> prevShell = new HashSet<KohonenSOMCell<T>>();
		Set<KohonenSOMCell<T>> prevPrevShell = new HashSet<KohonenSOMCell<T>>();
		private int radiusSquared;

		private final double maxRadius;

// --------------------------- CONSTRUCTORS ---------------------------

		public NeighborhoodShellIterator(final KohonenSOMCell<T> cell, double maxRadius)
			{
			// PERF
			centerPos = cellPositionFor(getClusters().indexOf(cell));
			prevShell.add(cell);
			this.maxRadius = maxRadius;
			}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Iterator ---------------------

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext()
			{
			return (radius + 1) <= maxRadius;
			}


		/**
		 * {@inheritDoc}
		 */
		public Set<KohonenSOMCell<T>> next()
			{
			final Set<KohonenSOMCell<T>> shell = new HashSet<KohonenSOMCell<T>>();
			for (final KohonenSOMCell<T> cell : prevShell)
				{
				// ** problem with edges when not wrapping; need to include edges in the shell?
				computeImmediateNeighbors(cell);
				for (final KohonenSOMCell<T> neighbor : immediateNeighbors)
					{
					if (neighbor != null && !prevShell.contains(neighbor) && !prevPrevShell.contains(neighbor)
					    && isWithinCurrentRadius(neighbor))
						{
						shell.add(neighbor);
						}
					}
				}

			prevPrevShell = prevShell;
			prevShell = shell;

			radius++;
			radiusSquared = radius * radius;

			return shell;
			}

		/**
		 * {@inheritDoc}
		 */
		public void remove()
			{
			throw new NotImplementedException();
			}

// -------------------------- OTHER METHODS --------------------------

		//  populates the immediateNeighbors array (which is allocated only once for efficiency). straight-line neighbors of
		//  this node (not including diagonals)

		//  @param trav
		//  @return

		private void computeImmediateNeighbors(final KohonenSOMCell<T> trav)
			{
			//	theClusters.get(target).getNeighbors(radius);

			// no need to reallocate every time; see immediateNeighbors array
			//List<KohonenSOMCell<T>> result = new ArrayList<KohonenSOMCell<T>>(2 * dimensions);

			final List<? extends KohonenSOMCell<T>> clusterList = getClusters();
			final int[] pos = cellPositionFor(clusterList.indexOf(trav));
			for (int i = 0; i < dimensions; i++)
				{
				// the -1 neighbor
				pos[i]--;
				if (pos[i] == -1)
					{
					if (edgesWrap)
						{
						pos[i] = cellsPerDimension[i] - 1;
						immediateNeighbors[2 * i] = clusterList.get(listIndexFor(pos));
						pos[i] = -1;
						}
					else
						{
						immediateNeighbors[2 * i] = null;
						}
					}
				else
					{
					immediateNeighbors[2 * i] = clusterList.get(listIndexFor(pos));
					}

				// the +1 neighbor
				pos[i] += 2;
				if (pos[i] == cellsPerDimension[i] - 1)
					{
					if (edgesWrap)
						{
						pos[i] = 0;
						immediateNeighbors[2 * i + 1] = clusterList.get(listIndexFor(pos));
						pos[i] = cellsPerDimension[i] - 1;
						}
					else
						{
						immediateNeighbors[2 * i + 1] = null;
						}
					}
				else
					{
					immediateNeighbors[2 * i + 1] = clusterList.get(listIndexFor(pos));
					}

				// return to the original position
				pos[i]--;
				}
			}

		private double getNextRadius()
			{
			return radius + 1;
			}

		private boolean isWithinCurrentRadius(final KohonenSOMCell<T> neighbor)
			{
			// PERF
			final int[] pos = cellPositionFor(getClusters().indexOf(neighbor));

			int sum = 0;
			for (int i = 0; i < dimensions; i++)
				{
				int dist = Math.abs(pos[i] - centerPos[i]);
				if (edgesWrap)
					{
					dist = Math.min(dist, cellsPerDimension[i] - dist);
					}
				sum += dist * dist;
				}

			return sum <= radiusSquared;
			}
		}

	/**
	 * Iterates over all the cells within a given radius of a center cell, using a fast algorithm from
	 * http://homepage.smc.edu/kennedy_john/BCIRCLE.PDF
	 * <p/>
	 * We can probably speed this up further by caching the results (circle masks, basically)
	 */
	private class NeighborhoodIterator implements Iterator<KohonenSOMCell<T>>
		{
// ------------------------------ FIELDS ------------------------------

		//	KohonenSOMCell<T> center;
		private final NeighborhoodShellIterator shells;
		private Set<KohonenSOMCell<T>> currentShell;
		private Iterator<KohonenSOMCell<T>> currentShellIterator;


		private final double maxRadius;
// --------------------------- CONSTRUCTORS ---------------------------

		private NeighborhoodIterator(final KohonenSOMCell<T> center, final int time)
			{
			maxRadius = radiusFunction.f(time);
			shells = new NeighborhoodShellIterator(center, maxRadius);
			}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Iterator ---------------------

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext()
			{
			return currentShellIterator.hasNext() || (shells.hasNext() && shells.getNextRadius() <= maxRadius);
			}

		/**
		 * {@inheritDoc}
		 */
		public KohonenSOMCell<T> next()
			{
			try
				{
				return currentShellIterator.next();
				}
			catch (NoSuchElementException e)
				{
				if (shells.getNextRadius() > maxRadius)
					{
					throw new NoSuchElementException();
					}
				currentShell = shells.next();
				currentShellIterator = currentShell.iterator();

				return currentShellIterator.next();
				}
			}

		/**
		 * {@inheritDoc}
		 */
		public void remove()
			{
			throw new NotImplementedException();
			}
		}
	}
