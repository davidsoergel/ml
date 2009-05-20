/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.SimpleFunction;
import edu.berkeley.compbio.ml.cluster.AbstractUnsupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Kohonen Self Organizing Map implementation for a rectangular grid of two dimensions.
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
public class KohonenSOM2D<T extends AdditiveClusterable<T>>
		extends AbstractUnsupervisedOnlineClusteringMethod<T, KohonenSOMCell<T>> implements KohonenSOM<T>
	{
// ------------------------------ FIELDS ------------------------------

	// we jump through some hoops to avoid actually storing the cells in an array,
	// since we don't know a priori how many dimensions it should have, and it would be redundant with
	// OnlineClusteringMethod.theClusters
	// aha: since theClusters is a list, we can map our array into it.  see listIndexFor(int[] cellposition)
	private static final Logger logger = Logger.getLogger(KohonenSOM2D.class);

	// how many cells wide is the grid along each axis
	int[] cellsPerDimension;
	double maxRadius;
	double minRadius;

	// the product of the first i dimensions, precomputed for convenience
	int[] blockSize;

	//	Map<Vector<Integer>, T> centroidsByPosition;

	int time = 0;

	// how many point assignments have changed in this epoch
	int changed = 0;

	//private DistanceMeasure<T> measure;
	private int dimensions;
	private boolean edgesWrap;

	private boolean decrementLosingNeighborhood;

	// how strong the motion should be vs. time
	private SimpleFunction moveFactorFunction;

	// what radius should be considered vs. time
	private SimpleFunction radiusFunction;

	// how strong the motion should be vs. fraction of the radius
	private SimpleFunction weightFunction;
	private Map<Integer, WeightedMask> weightedMasks = new HashMap<Integer, WeightedMask>();
	private Map<Integer, WeightedMask> shellMasks = new HashMap<Integer, WeightedMask>();

	private KohonenSOM2DSearchStrategy<T> searchStrategy;

	private LabelDiffuser<T, KohonenSOMCell<T>> labeler;


// --------------------------- CONSTRUCTORS ---------------------------

	public KohonenSOM2D(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins, Set<String> predictLabels,
	                    Set<String> leaveOneOutLabels, Set<String> testLabels, @NotNull Integer[] cellsPerDimension,
	                    SimpleFunction moveFactorFunction, SimpleFunction radiusFunction, SimpleFunction weightFunction,
	                    boolean decrementLosingNeighborhood, boolean edgesWrap, double minRadius,
	                    KohonenSOM2DSearchStrategy<T> searchStrategy, int testThreads)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels, testThreads);

		this.cellsPerDimension = DSArrayUtils.toPrimitive(cellsPerDimension);

		this.dimensions = cellsPerDimension.length;
		this.moveFactorFunction = moveFactorFunction;
		this.radiusFunction = radiusFunction;
		this.weightFunction = weightFunction;
		this.decrementLosingNeighborhood = decrementLosingNeighborhood;
		this.edgesWrap = edgesWrap;
		this.minRadius = minRadius;
		this.searchStrategy = searchStrategy;


		if (dimensions != 2)
			{
			throw new ClusterRuntimeException("KohonenSOM2D accepts only two-dimensional grid.");
			}

		// precompute stuff for listIndexFor
		blockSize = new int[dimensions];
		blockSize[1] = 1;
		blockSize[0] = cellsPerDimension[1];

		int totalCells = cellsPerDimension[0] * cellsPerDimension[1];

		// this overwrites the original list of unknown capacity
		theClusters = new ArrayList<KohonenSOMCell<T>>(totalCells);

		int[] zeroCell = new int[dimensions];
		Arrays.fill(zeroCell, 0);
		//createClusters(zeroCell, -1, prototype);
		//createClusters(totalCells, prototype);
		//List<Interval<Double>> axisRanges;
		//	initializeClusters(axisRanges);

		maxRadius = DSArrayUtils.norm(this.cellsPerDimension) / 2.;//Math.ceil();

		searchStrategy.setDistanceMeasure(measure);
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	public int getChanged()
		{
		return changed;
		}

	public void setLabeler(LabelDiffuser<T, KohonenSOMCell<T>> labeler)
		{
		this.labeler = labeler;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidClusteringMethod ---------------------


	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(theClusters, measure);
		}

	public void computeClusterStdDevs(ClusterableIterator<T> theDataPointProvider) throws IOException
		{
		CentroidClusteringUtils.computeClusterStdDevs(theClusters, measure, assignments, theDataPointProvider);
		}

	@Override
	public String clusteringStats()
		{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, outf);
		}

// --------------------- Interface DiffusableLabelClusteringMethod ---------------------


	/**
	 * {@inheritDoc}
	 */
	public Iterator<Set<KohonenSOMCell<T>>> getNeighborhoodShellIterator(KohonenSOMCell<T> cell)
		{
		return new NeighborhoodShellIterator(cell);
		}

// --------------------- Interface OnlineClusteringMethod ---------------------

	/**
	 * @param p
	 * @return
	 * @throws ClusterException
	 * @throws NoGoodClusterException
	 */
	public boolean add(T p) throws ClusterException, NoGoodClusterException
		{
		ClusterMove<T, KohonenSOMCell<T>> cm = bestClusterMove(p);

		if (cm.isChanged())
			{
			changed++;
			assignments.put(p.getId(), cm.bestCluster);
			}

		// do the moves whether or not the assignment changed

		KohonenSOMCell<T> loser = cm.oldCluster;
		KohonenSOMCell<T> winner = cm.bestCluster;

		double moveFactor = moveFactorFunction.f(time);

		moveFactor = Math.min(moveFactor, 1);
		moveFactor = Math.max(moveFactor, 0);
		double radius = getCurrentRadius();

		logger.trace("Adding point with neighborhood radius " + radius + ", moveFactor " + moveFactor);

		// REVIEW decrementLosingNeighborhood has issues
		// yeah a couple things:
		// 1. it produces negative counts, which makes no sense, and
		// 2. it leaves the average count number very low
		// 3. in many of our runs we have an infinite supply of new samples, and never reclassify old samples

		if (decrementLosingNeighborhood && loser != null)
			{
			winner.getWeightedLabels().removeAll(p.getWeightedLabels());
			for (Iterator<WeightedCell> i = getWeightedMask((int) radius).iterator(loser); i.hasNext();)
				{
				WeightedCell v = i.next();
				KohonenSOMCell<T> neighbor = v.theCell;
				/*T motion = p.minus(neighbor.getCentroid());
								 motion.multiplyBy(-moveFactor);
								 if (v.weight != 1)
									 {
									 motion.multiplyBy(v.weight);
									 }
								 neighbor.recenterByAdding(motion);*/

				double motionFactor = moveFactor * v.weight;
				neighbor.recenterByRemovingWeighted(p, motionFactor);
				}
			}

		winner.getWeightedLabels().addAll(p.getWeightedLabels());
		for (Iterator<WeightedCell> i = getWeightedMask((int) radius).iterator(winner); i.hasNext();)
			{
			WeightedCell v = i.next();
			KohonenSOMCell<T> neighbor = v.theCell;

			// REVIEW Rearrange to avoid subtraction
			/*
						 T motion = p.minus(neighbor.getCentroid());

						 motion.multiplyBy(moveFactor);
						 if (v.weight != 1)
							 {
							 motion.multiplyBy(v.weight);
							 }
						 neighbor.recenterByAdding(motion);
						 */

			double motionFactor = moveFactor * v.weight;

			//neighbor = (1-motionFactor) * neighbor + motionFactor * p;


			// REVIEW does neighbor recentering work right?
			neighbor.recenterByAddingWeighted(p, motionFactor);
			}

		time++;
		return true;
		}

	/**
	 * Create a rectangular grid of cells using the given dimensionality and size, assigning a null vector to each
	 *
	 * @param cellPosition
	 * @param changingDimension // * @param prototype
	 */
	/*	private void createClusters(int[] cellPosition, int changingDimension, T prototype)
		 {
		 changingDimension++;
		 if (changingDimension == dimensions)
			 {
			 KohonenSOMCell<T> c = new KohonenSOMCell<T>(measure, prototype.clone());
			 theClusters.set(listIndexFor(cellPosition), c);
			 }
		 else
			 {
			 for (int i = 0; i < cellsPerDimension[changingDimension]; i++)
				 {
				 cellPosition[changingDimension] = i;
				 createClusters(cellPosition, changingDimension, prototype);
				 }
			 }
 */
	/*  for (int i = 0; i < k; i++) {
				  // initialize the clusters with the first k points

				  Cluster<T> c = new AdditiveCluster<T>(measure);
				  c.setId(i);

				  theClusters.add(c);
			  }
			  logger.debug("initialized " + k + " clusters");*/
	//		}
	@Override
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int iterations)
			throws ClusterException
		{
		super.train(trainingCollectionIteratorFactory, iterations);
		labeler.propagateLabels(this);
		}

// --------------------- Interface PrototypeBasedCentroidClusteringMethod ---------------------

	/*
	 private void initializeClusters(List<Interval<Double>> axisRanges)
		 {

		 }
 */

	public void createClusters(GenericFactory<T> prototypeFactory) throws GenericFactoryException
		{
		int totalCells = cellsPerDimension[0] * cellsPerDimension[1];
		createClusters(totalCells, prototypeFactory);

		searchStrategy.setSOM(this);
		}

// --------------------- Interface SampleInitializedOnlineClusteringMethod ---------------------


	/**
	 * {@inheritDoc}
	 */
	public void initializeWithSamples(Iterator<T> initIterator, int initSamples)
		{
		//createClusters(prototypeFactory);

		for (int i = 0; i < initSamples; i++)
			//int i = 0;
			//while(initIterator.hasNext())
			{
			addToRandomCell(initIterator.next());
			if (i % 100 == 0)
				{
				logger.debug("Initialized with " + i + " samples.");
				}
			//	i++;
			}
		}

// -------------------------- OTHER METHODS --------------------------

	public void addToRandomCell(T p)
		{
		KohonenSOMCell<T> winner = (KohonenSOMCell<T>) chooseRandomCluster();

		double moveFactor = .5;
		double radius = maxRadius;

		logger.trace("Adding point with neighborhood radius " + radius + ", moveFactor " + moveFactor);

		// winner.addLabel(p);  // no, this is just for random initialization

		for (Iterator<WeightedCell> i = getWeightedMask((int) radius).iterator(winner); i.hasNext();)
			{
			WeightedCell v = i.next();
			KohonenSOMCell<T> neighbor = v.theCell;

			double motionFactor = moveFactor * v.weight;
			neighbor.recenterByAddingWeighted(p, motionFactor);
			}
		//time++;  // no!
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
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		return searchStrategy.bestClusterMove(p);
		}

	/**
	 * assumes inputs are entirely positive and within the bounds given by cellsPerDimension
	 *
	 * @return
	 */
	/*	private int listIndexFor(int[] cellposition)
		 {
		 int result = 0;
		 assert cellposition.length == cellsPerDimension.length;
		 for (int i = 0; i < dimensions; i++)
			 {
			 result += cellposition[i] * blockSize[i];
			 }
		 return result;
		 }
 */
	private int[] cellPositionFor(int listIndex)
		{
		int[] result = new int[dimensions];
		for (int i = 0; i < dimensions; i++)
			{
			result[i] = listIndex / blockSize[i];
			listIndex = listIndex % blockSize[i];
			}
		return result;
		}

	public double[] computeCellAverageNeighborDistances()
		{
		double[] result = new double[theClusters.size()];

		// assume the distances are symmetric, so we only calculate them once per pair of cells

		// we just average the four straight-line distances per cell (no diagonals)

		int width = cellsPerDimension[0];
		int height = cellsPerDimension[1];
		for (int x = 0; x < width - 1; x++)
			{
			for (int y = 0; y < height - 1; y++)
				{
				//if (x != width && y != height)
				//	{
				CentroidCluster<T> here = clusterAt(x, y);

				CentroidCluster<T> right = clusterAt(x + 1, y);
				double d = measure.distanceFromTo(here.getCentroid(),
				                                  right.getCentroid());//here.distanceToCentroid(right.getCentroid());

				result[listIndexFor(x, y)] += d;
				result[listIndexFor(x + 1, y)] += d;


				CentroidCluster<T> down = clusterAt(x, y + 1);
				d = measure.distanceFromTo(here.getCentroid(),
				                           down.getCentroid());//here.distanceToCentroid(down.getCentroid());

				result[listIndexFor(x, y)] += d;
				result[listIndexFor(x, y + 1)] += d;
				//	}
				}
			}

		for (int i = 0; i < result.length; i++)
			{
			// if the edges don't wrap, then the edge cells should be divided by 3, not 4.  Oh well.

			result[i] /= 4;
			}

		return result;//Arrays.asList(ArrayUtils.toObject(result));
		}

	public KohonenSOMCell<T> clusterAt(int x, int y)
		{
		return ((List<KohonenSOMCell<T>>) theClusters).get(listIndexFor(x, y));
		}

	/**
	 * assumes inputs are entirely positive and within the bounds given by cellsPerDimension
	 *
	 * @return
	 */
	private int listIndexFor(int x, int y)//int[] cellposition)
		{
		if (edgesWrap)
			{
			x %= cellsPerDimension[0];
			y %= cellsPerDimension[1];
			}

		return x * blockSize[0] + y;
		}

	private void createClusters(int totalCells, GenericFactory<T> prototypeFactory) throws GenericFactoryException
		{
		for (int i = 0; i < totalCells; i++)
			{
			KohonenSOMCell<T> c =
					new KohonenSOMCell<T>(i, prototypeFactory == null ? null : prototypeFactory.create("" + i));
			c.setId(i);
			theClusters.add(c);
			}
		}

	public double getCurrentRadius()
		{
		double radius = radiusFunction.f(time);

		radius = Math.min(radius, maxRadius);
		radius = Math.max(radius, minRadius);

		return radius;
		}

	WeightedMask getShellMask(int radius)
		{
		WeightedMask result = shellMasks.get(radius);
		if (result == null)
			{
			if (radius < 1)
				{
				result = getWeightedMask(0);
				}
			else
				{
				WeightedMask outerMask = getWeightedMask(radius);
				WeightedMask innerMask = getWeightedMask(radius - 1);
				List<Integer> xList = new ArrayList<Integer>();
				List<Integer> yList = new ArrayList<Integer>();
				for (int i = 0; i < outerMask.deltaX.length; i++)
					{
					int x = outerMask.deltaX[i];
					int y = outerMask.deltaY[i];
					if (!innerMask.containsPoint(x, y))
						{
						xList.add(x);
						yList.add(y);
						}
					}

				result = new WeightedMask();
				result.deltaX = DSArrayUtils.toPrimitive(xList.toArray(new Integer[]{}));
				result.deltaY = DSArrayUtils.toPrimitive(yList.toArray(new Integer[]{}));
				result.weight = new double[result.deltaX.length];
				Arrays.fill(result.weight, 1);
				result.numCells = result.deltaX.length;
				}
			shellMasks.put(radius, result);
			}
		return result;
		}

	WeightedMask getWeightedMask(int radius)
		{
		WeightedMask result = weightedMasks.get(radius);
		if (result == null)
			{
			result = new WeightedMask(radius);
			weightedMasks.put(radius, result);
			}
		return result;
		}

	/*
	 public Set<Cluster<T>> watershedClustering(double threshold)
		 {
		 double[] uMatrix = computeCellAverageNeighborDistances();

		 int width = cellsPerDimension[0];
		 int height = cellsPerDimension[1];
		 for (int x = 0; x < width; x++)
			 {
			 for (int y = 0; y < height; y++)
				 {
				 if (uMatrix[listIndexFor(x, y)] < threshold)
					 {

					 }
				 }
			 }
		 }
 */
	public void resetChanged()
		{
		changed = 0;
		}

	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory,
	                  GenericFactory<T> prototypeFactory, int trainingEpochs) throws IOException, ClusterException
		{
		train(trainingCollectionIteratorFactory, trainingEpochs);
		}

// -------------------------- INNER CLASSES --------------------------

	/**
	 * Iterates over all the cells within a given radius of a center cell, using a fast algorithm from
	 * http://homepage.smc.edu/kennedy_john/BCIRCLE.PDF
	 * <p/>
	 * We can probably speed this up further by caching the results (circle masks, basically).
	 * <p/>
	 * Note we can't just return the cell, because if the edges wrap, then the same cell may be returned up to four times,
	 * but with different distances; so we need to return the distance too.
	 */
	class WeightedMask
		{
// ------------------------------ FIELDS ------------------------------

		// store a list of x,y pairs representing all the vectors from the center
		int[] deltaX;
		int[] deltaY;

		// cache the weights associated with each x, y pair very inefficiently for easy & fast access
		double[] weight;


		// we don't know exactly how many pixels will be in the circle in advance, so we'll allocate somewhat more
		// memory to the above arrays than we need.  Then we need to keep track of the highest index that is valid,
		// i.e. the logical end of the array as opposed to the physical end.
		int numCells;


// --------------------------- CONSTRUCTORS ---------------------------

		private WeightedMask()
			{
			}

		private WeightedMask(int radius)
			{
			if (radius == 0)
				{
				deltaX = new int[1];
				deltaY = new int[1];
				weight = new double[1];

				deltaX[0] = 0;
				deltaY[0] = 0;
				weight[0] = weightFunction == null ? 1 : weightFunction.f(0);
				numCells = 1;
				}
			else
				{
				int x = radius;
				int y = 0;
				int xChange = 1 - 2 * radius;
				int yChange = 1;
				int radiusError = 0;

				int i = 0;

				// we'll see if 3.2 is enough, given rounding, to work for small r.
				// If it's not then we'll get an ArrayIndexOutOfBoundsException below.
				// 4 should be absolutely safe (i.e., the whole square) and the memory cost is likely no issue anyway.
				int overestimateNumCells = (int) (3.2 * ((radius + 1) * (radius + 1)));

				deltaX = new int[overestimateNumCells];
				deltaY = new int[overestimateNumCells];
				weight = new double[overestimateNumCells];

				// always add the center (only once)
				deltaX[i] = 0;
				deltaY[i] = 0;
				weight[i] = weightFunction == null ? 1 : weightFunction.f(0);
				assert weight[i] > 0;
				i++;

				while (x >= y)
					{
					i = plot8CirclePoints(i, x, y, radius);

					y++;
					radiusError += yChange;
					yChange += 2;
					if (2 * radiusError + xChange > 0)
						{
						x--;
						radiusError += xChange;
						xChange += 2;
						}
					}

				// for some reason the above algorithm always leaves out the points (0,radius) and (0,-radius).
				// I don't bother to understand why, I just fix it.

				double theWeight = weightFunction == null ? 1 : weightFunction.f(1);
				assert theWeight > 0;

				deltaX[i] = 0;
				deltaY[i] = radius;
				weight[i] = theWeight;
				i++;

				deltaX[i] = 0;
				deltaY[i] = -radius;
				weight[i] = theWeight;
				i++;


				numCells = i;
				}
			}

		private int plot8CirclePoints(int i, int x, int y, int radius)
			{
			// here we're given x,y pairs around the circumference of the circle; but actually we need to fill it in.

			// also the given x,y pairs are only in one eighth of the circle, so we have to be careful how we fill in the rest
			// to avoid hitting the same internal cells multiple times.

			// note that the circle is always centered around a cell, so the width and height will always be odd.
			// that is, there will always be cells along the axes (x=0 and y=0) which should not be double-counted.

			// we could add pixels to a non-redundant Set of some sort, but that would be slow; we want to do everything with int arrays,
			// so we have to be careful to avoid redundancy up front.

			if (x != 0 && y != 0)
				{
				// don't use x >= y, because then we'd double-count the diagonals and the center
				for (; x > y; x--)
					{
					double dist = Math.sqrt(x * x + y * y);
					double theWeight = weightFunction == null ? 1 : weightFunction.f(dist / (double) radius);
					/*		if (logger.isDebugEnabled())
					   {
					   logger.debug("Plotting circle point " + x + ", " + y + " distance " + dist + " radius " + radius
							   + " weight " + theWeight);
					   }*/
					assert theWeight > 0;

					deltaX[i] = x;
					deltaY[i] = y;
					weight[i] = theWeight;
					i++;

					deltaX[i] = x;
					deltaY[i] = -y;
					weight[i] = theWeight;
					i++;

					deltaX[i] = -x;
					deltaY[i] = y;
					weight[i] = theWeight;
					i++;

					deltaX[i] = -x;
					deltaY[i] = -y;
					weight[i] = theWeight;
					i++;

					deltaX[i] = y;
					deltaY[i] = x;
					weight[i] = theWeight;
					i++;

					deltaX[i] = y;
					deltaY[i] = -x;
					weight[i] = theWeight;
					i++;

					deltaX[i] = -y;
					deltaY[i] = x;
					weight[i] = theWeight;
					i++;

					deltaX[i] = -y;
					deltaY[i] = -x;
					weight[i] = theWeight;
					i++;
					}


				// count the four diagonals (x = y) only once.  Note y != 0 so we're not worried about the center.
				// we know that y is incremented exactly once for each call of plot8CirclePoints.

				double dist = Math.sqrt(y * y + y * y);
				double theWeight = weightFunction == null ? 1 : weightFunction.f(dist / (double) radius);
				assert theWeight > 0;

				deltaX[i] = y;
				deltaY[i] = y;
				weight[i] = theWeight;
				i++;
				deltaX[i] = y;
				deltaY[i] = -y;
				weight[i] = theWeight;
				i++;
				deltaX[i] = -y;
				deltaY[i] = y;
				weight[i] = theWeight;
				i++;
				deltaX[i] = -y;
				deltaY[i] = -y;
				weight[i] = theWeight;
				i++;

				// count the vertical center line only once.
				// again we know this will be called once for each y (where y != 0 due to the conditional block we're in)
				theWeight = weightFunction == null ? 1 : weightFunction.f((double) y / (double) radius);
				assert theWeight > 0;

				deltaX[i] = 0;
				deltaY[i] = y;
				weight[i] = weightFunction == null ? 1 : weightFunction.f(theWeight);
				i++;
				deltaX[i] = 0;
				deltaY[i] = -y;
				weight[i] = weightFunction == null ? 1 : weightFunction.f(theWeight);
				i++;
				}
			else if (y == 0 && x != 0)
				{
				// count the horizontal center line only once

				// don't use x >= y, because then we'd double-count the center
				for (; x > 0; x--)
					{
					double theWeight = weightFunction == null ? 1 : weightFunction.f((double) x / (double) radius);
					assert theWeight > 0;

					deltaX[i] = x;
					deltaY[i] = 0;
					weight[i] = theWeight;
					i++;

					deltaX[i] = -x;
					deltaY[i] = 0;
					weight[i] = theWeight;
					i++;
					}
				}
			else
				// (x == 0 && y != 0)
				{
				// ignore this situation, since the vertical line and the center are already accounted for by our construction above
				}
			return i;
			}

// -------------------------- OTHER METHODS --------------------------

		public boolean containsPoint(int x, int y)
			{
			//brute force search
			for (int i = 0; i < deltaX.length; i++)
				{
				if (deltaX[i] == x && deltaY[i] == y)
					{
					return true;
					}
				}
			return false;
			}

		public Iterator<WeightedCell> iterator(KohonenSOMCell<T> center)
			{
			return new MaskIterator(center);
			}

// -------------------------- INNER CLASSES --------------------------

		private class MaskIterator implements Iterator<WeightedCell>
			{
// ------------------------------ FIELDS ------------------------------

			WeightedCell currentCell, nextCell;
			int xCenter, yCenter;

			// the current index in the mask list.  Points to the next cell to be returned.
			int trav = -1;


// --------------------------- CONSTRUCTORS ---------------------------

			public MaskIterator(KohonenSOMCell<T> center)
				{
				//this.center = center;
				int[] c = cellPositionFor(((List<KohonenSOMCell<T>>) theClusters).indexOf(center));
				xCenter = c[0];
				yCenter = c[1];
				nextCell = findNextCell();
				}

			@Nullable
			private WeightedCell findNextCell()
				{
				trav++;

				boolean foundCell = false;

				int realX = -1, realY =
						-1;// the loop below can't complete without setting these; if there's a bug we'll get ArrayIndexOutOfBoundsException

				// iterate rather than recurse to avoid huge stacks
				while (!foundCell)
					{
					if (trav >= numCells)
						{
						return null;
						}
					else
						{
						realX = xCenter + deltaX[trav];
						realY = yCenter + deltaY[trav];

						if (!edgesWrap && (realX < 0 || realX >= cellsPerDimension[0] || realY < 0
						                   || realY >= cellsPerDimension[1]))
							{
							// foundCell still false, try again
							trav++;
							}
						else
							{
							realX %= cellsPerDimension[0];
							// avoid negatives too
							if (realX < 0)
								{
								realX += cellsPerDimension[0];
								}

							realY %= cellsPerDimension[1];
							// avoid negatives too
							if (realY < 0)
								{
								realY += cellsPerDimension[1];
								}

							foundCell = true;
							}
						}
					}


				return new WeightedCell(
						(KohonenSOMCell<T>) ((List<KohonenSOMCell<T>>) theClusters).get(listIndexFor(realX, realY)),
						weight[trav]);
				}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Iterator ---------------------

			/**
			 * {@inheritDoc}
			 */
			public boolean hasNext()
				{
				return nextCell != null;
				}

			/**
			 * {@inheritDoc}
			 */
			public WeightedCell next()
				{
				currentCell = nextCell;

				nextCell = findNextCell();


				return currentCell;
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

	class WeightedCell
		{
// ------------------------------ FIELDS ------------------------------

		KohonenSOMCell<T> theCell;
		double weight;


// --------------------------- CONSTRUCTORS ---------------------------

		private WeightedCell(KohonenSOMCell<T> theCell, double weight)
			{
			this.theCell = theCell;
			this.weight = weight;
			}
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

	private class NeighborhoodShellIterator implements Iterator<Set<KohonenSOMCell<T>>>
		{
// ------------------------------ FIELDS ------------------------------

		//	WeightedMask oldMask, currentMask;
		int radius = 0;
		private KohonenSOMCell<T> center;


// --------------------------- CONSTRUCTORS ---------------------------

		public NeighborhoodShellIterator(KohonenSOMCell<T> center)
			{
			this.center = center;
			}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Iterator ---------------------

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext()
			{
			return true;
			}

		/**
		 * {@inheritDoc}
		 */
		// PERF This is horribly inefficient but we don't do it often
		public Set<KohonenSOMCell<T>> next()
			{
			WeightedMask mask = getShellMask(radius);
			//	oldMask = currentMask;
			//	currentMask = getWeightedMask(radius);

			Set<KohonenSOMCell<T>> result = new HashSet<KohonenSOMCell<T>>();
			for (Iterator<WeightedCell> i = mask.iterator(center); i.hasNext();)
				{
				result.add(i.next().theCell);
				}
			/*	for (Iterator<WeightedCell> i = currentMask.iterator(center); i.hasNext();)
			   {
			   result.add(i.next().theCell);
			   }
		   if (oldMask != null)
			   {
			   for (Iterator<WeightedCell> i = oldMask.iterator(center); i.hasNext();)
				   {
				   result.remove(i.next().theCell);
				   }
			   }*/
			radius++;
			return result;
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
