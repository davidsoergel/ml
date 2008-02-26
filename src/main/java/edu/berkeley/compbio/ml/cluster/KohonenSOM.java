/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.SimpleFunction;
import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/* $Id$ */

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
 * @Author David Soergel
 * @Version 1.0
 */
public class KohonenSOM<T extends AdditiveClusterable<T>> extends OnlineClusteringMethod<T>
	{

	// we jump through some hoops to avoid actually storing the cells in an array,
	// since we don't know a priori how many dimensions it should have, and it would be redundant with
	// OnlineClusteringMethod.theClusters

	// aha: since theClusters is a list, we can map our array into it.  see listIndexFor(int[] cellposition)

	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KohonenSOM.class);

	// how many cells wide is the grid along each axis
	int[] cellsPerDimension;

	// the product of the first i dimensions, precomputed for convenience
	int[] blockSize;

	Map<Vector<Integer>, T> centroidsByPosition;

	int time = 0;

	private DistanceMeasure<T> measure;
	private int dimensions;
	private boolean edgesWrap;

	private boolean decrementLosingNeighborhood;

	private SimpleFunction moveFactorFunction;
	private SimpleFunction radiusFunction;

	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * assumes inputs are entirely positive and within the bounds given by cellsPerDimension
	 *
	 * @param cellposition
	 * @return
	 */
	private int listIndexFor(int[] cellposition)
		{
		int result = 0;
		assert cellposition.length == cellsPerDimension.length;
		for (int i = 0; i < dimensions; i++)
			{
			result += cellposition[i] * blockSize[i];
			}
		return result;
		}

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


	public KohonenSOM(int[] cellsPerDimension, DistanceMeasure<T> dm, T prototype, SimpleFunction moveFactorFunction,
	                  SimpleFunction radiusFunction, boolean decrementLosingNeighborhood, boolean edgesWrap)
		{
		this.cellsPerDimension = cellsPerDimension;
		this.measure = dm;
		this.dimensions = cellsPerDimension.length;
		this.moveFactorFunction = moveFactorFunction;
		this.radiusFunction = radiusFunction;
		this.decrementLosingNeighborhood = decrementLosingNeighborhood;
		this.edgesWrap = edgesWrap;

		// precompute stuff for listIndexFor
		blockSize = new int[dimensions];
		blockSize[dimensions - 1] = 1;
		for (int i = dimensions - 2; i > 0; i--)
			{
			blockSize[i] = blockSize[i + 1] * cellsPerDimension[i];
			}

		int[] zeroCell = new int[dimensions];
		Arrays.fill(zeroCell, 0);
		createClusters(zeroCell, -1, prototype);
		//List<Interval<Double>> axisRanges;
		//	initializeClusters(axisRanges);

		}

	/*
	 private void initializeClusters(List<Interval<Double>> axisRanges)
		 {

		 }
 */

	/**
	 * Create a rectangular grid of cells using the given dimensionality and size, assigning a null vector to each
	 *
	 * @param cellPosition
	 * @param changingDimension // * @param prototype
	 */
	private void createClusters(int[] cellPosition, int changingDimension, T prototype)
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

		/*  for (int i = 0; i < k; i++) {
			  // initialize the clusters with the first k points

			  Cluster<T> c = new AdditiveCluster<T>(measure);
			  c.setId(i);

			  theClusters.add(c);
		  }
		  logger.debug("initialized " + k + " clusters");*/
		}

	// -------------------------- OTHER METHODS --------------------------

	public boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException
		{
		ClusterMove cm = bestClusterMove(p);
		KohonenSOMCell<T> loser = (KohonenSOMCell<T>) cm.oldCluster;
		KohonenSOMCell<T> winner = (KohonenSOMCell<T>) cm.bestCluster;

		// ** I had a problem with this before??
		if (decrementLosingNeighborhood)
			{
			for (Iterator<KohonenSOMCell<T>> i = new NeighborhoodIterator(loser, time); i.hasNext();)
				{
				KohonenSOMCell<T> neighbor = i.next();
				T motion = p.minus(neighbor.getCentroid());
				motion.multiplyBy(-moveFactor(time));
				neighbor.recenterByAdding(motion);
				}
			}

		for (Iterator<KohonenSOMCell<T>> i = new NeighborhoodIterator(winner, time); i.hasNext();)
			{
			KohonenSOMCell<T> neighbor = i.next();
			T motion = p.minus(neighbor.getCentroid());
			motion.multiplyBy(moveFactor(time));
			neighbor.recenterByAdding(motion);
			}
		time++;
		return true;
		}

	private double moveFactor(int time)
		{
		return moveFactorFunction.f(time);
		//throw new NotImplementedException();
		}

	KohonenSOMCell<T>[] immediateNeighbors = new KohonenSOMCell[2 * dimensions];

	//private Iterator<KohonenSOMCell<T>> neighborhoodOf(int target, int time)
	private class NeighborhoodIterator implements Iterator<KohonenSOMCell<T>>
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

		/**
		 * populates the immediateNeighbors array (which is allocated only once for efficiency). straight-line neighbors of
		 * this node (not including diagonals)
		 *
		 * @param trav
		 * @return
		 */
		private void computeImmediateNeighbors(KohonenSOMCell<T> trav)
			{
			//	theClusters.get(target).getNeighbors(radius);

			// no need to reallocate every time; see immediateNeighbors array
			//List<KohonenSOMCell<T>> result = new ArrayList<KohonenSOMCell<T>>(2 * dimensions);

			int[] pos = cellPositionFor(theClusters.indexOf(trav));
			for (int i = 0; i < dimensions; i++)
				{
				// the -1 neighbor
				pos[i]--;
				if (pos[i] == -1)
					{
					if (edgesWrap)
						{
						pos[i] = cellsPerDimension[i] - 1;
						immediateNeighbors[2 * i] = (KohonenSOMCell<T>) theClusters.get(listIndexFor(pos));
						pos[i] = -1;
						}
					else
						{
						immediateNeighbors[2 * i] = null;
						}
					}
				else
					{
					immediateNeighbors[2 * i] = (KohonenSOMCell<T>) theClusters.get(listIndexFor(pos));
					}

				// the +1 neighbor
				pos[i] += 2;
				if (pos[i] == cellsPerDimension[i] - 1)
					{
					if (edgesWrap)
						{
						pos[i] = 0;
						immediateNeighbors[2 * i + 1] = (KohonenSOMCell<T>) theClusters.get(listIndexFor(pos));
						pos[i] = cellsPerDimension[i] - 1;
						}
					else
						{
						immediateNeighbors[2 * i + 1] = null;
						}
					}
				else
					{
					immediateNeighbors[2 * i + 1] = (KohonenSOMCell<T>) theClusters.get(listIndexFor(pos));
					}

				// return to the original position
				pos[i]--;
				}
			}

		public void remove()
			{
			throw new NotImplementedException();
			}
		}

	/**
	 * Returns the best cluster without adding the point
	 *
	 * @param p                   Point to find the best cluster of
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 */
	public int getBestCluster(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException
		{
		ClusterMove cm = bestClusterMove(p);
		return theClusters.indexOf(cm.bestCluster);
		}

	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	public ClusterMove bestClusterMove(T p)
		{
		ClusterMove result = new ClusterMove();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		String id = p.getId();
		result.oldCluster = assignments.get(id);

		if (logger.isDebugEnabled())
			{
			logger.debug("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (Cluster<T> c : theClusters)
			{
			// grid already initialized with prototype, never mind all this stuff

			/*
			// while initializing the grid, cell centroids are null.  In that case, just assign the present point.
			// ** no this won't work right at all
			// ** why not?? PCA would be better, but this should work, just slowly.
			// ** aha: if there are more grid points than samples
			if (c.getCentroid() == null)
				{
				c.setCentroid(p.clone());
				result.bestDistance = 0;
				result.bestCluster = c;
				return result;
				}
*/
			// otherwise find the nearest cluster
			double d = c.distanceToCentroid(p);
			if (logger.isDebugEnabled())
				{
				logger.debug("Trying " + c + "; distance = " + d + "; best so far = " + result.bestDistance);
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
			logger.debug("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			logger.warn("Can't classify: " + p);
			assert false;
			}
		return result;
		}
	}
