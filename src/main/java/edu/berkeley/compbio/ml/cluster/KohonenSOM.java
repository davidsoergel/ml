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

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/* $Id$ */

/**
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

	int[] cellsPerDimension;
	int[] blockSize;

	Map<Vector<Integer>, T> centroidsByPosition;

	int time = 0;

	private DistanceMeasure<T> measure;
	private int dimensions;
	private boolean edgesWrap;

	// --------------------------- CONSTRUCTORS ---------------------------

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


	public KohonenSOM(int[] cellsPerDimension, List<Vector> initialVectors, DistanceMeasure<T> dm)
		{
		this.cellsPerDimension = cellsPerDimension;
		this.measure = dm;
		this.dimensions = cellsPerDimension.length;

		// precompute stuff for listIndexFor
		blockSize = new int[dimensions];
		blockSize[dimensions - 1] = 1;
		for (int i = dimensions - 2; i > 0; i--)
			{
			blockSize[i] = blockSize[i + 1] * cellsPerDimension[i];
			}

		createClusters(initialVectors, new int[dimensions], 0);
		}

	private void createClusters(List<Vector> initialVectors, int[] cellPosition, int changingDimension)
		{
		if (changingDimension == dimensions)
			{
			//**	KohonenSOMCell<T> c = new KohonenSOMCell<T>(measure, prototype.clone());
			//**	theClusters.set(listIndexFor(cellPosition),c);
			}


		{
		for (int i = 0; i < cellsPerDimension[changingDimension]; i++)
			{
			cellPosition[changingDimension] = i;
			createClusters(initialVectors, cellPosition, changingDimension + 1);
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
		int target = getBestCluster(p, secondBestDistances);

		for (Iterator<KohonenSOMCell<T>> i = neighborhoodOf(target, time); i.hasNext();)
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
		throw new NotImplementedException();
		}

	private Iterator<KohonenSOMCell<T>> neighborhoodOf(int target, int time)
		{
		//**	theClusters.get(target).getNeighbors(roadius);
		return null;
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
