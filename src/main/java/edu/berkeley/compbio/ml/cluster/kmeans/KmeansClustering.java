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


package edu.berkeley.compbio.ml.cluster.kmeans;

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.OnlineClusteringMethod;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @version 1.0
 */
public class KmeansClustering<T extends AdditiveClusterable<T>> extends OnlineClusteringMethod<T, CentroidCluster<T>>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KmeansClustering.class);
	//private DistanceMeasure<T> distanceMeasure;


	// --------------------------- CONSTRUCTORS ---------------------------

	//private int k;


	public KmeansClustering(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins, Set<String> predictLabels,
	                        Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeWithRealData(Iterator<T> trainingIterator, int k, GenericFactory<T> prototypeFactory)
			throws GenericFactoryException
		{

		for (int i = 0; i < k; i++)
			{
			// initialize the clusters with the first k points

			AbstractCentroidCluster<T> c = new AdditiveCentroidCluster<T>(i, trainingIterator.next());
			//c.setId(i);

			theClusters.add(c);
			}
		logger.debug("initialized " + k + " clusters");
		}

	// -------------------------- OTHER METHODS --------------------------


	/**
	 * Adds a point to the best cluster.  Generally it's not a good idea to store the point itself in the cluster for
	 * memory reasons; so this method is primarily useful for updating the position of the centroid.
	 *
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 */
	@Override
	public boolean add(T p, List<Double> secondBestDistances)
		{
		assert p != null;
		//n++;
		String id = p.getId();
		ClusterMove<T, CentroidCluster<T>> cm = bestClusterMove(p);
		secondBestDistances.add(cm.secondBestDistance);
		if (cm.isChanged())
			{
			try
				{
				cm.oldCluster.remove(p);//, cm.oldDistance);
				}
			catch (NullPointerException e)
				{// probably just the first round
				}
			cm.bestCluster.add(p);//, cm.bestDistance);  // this will automatically recalculate the centroid, etc.
			assignments.put(id, cm.bestCluster);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	/*	@Override
	 public Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
		 {
		 ClusterMove cm = bestClusterMove(p);
		 return cm.bestCluster;
		 }
 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(T p)
		{
		ClusterMove<T, CentroidCluster<T>> result = new ClusterMove<T, CentroidCluster<T>>();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		String id = p.getId();
		result.oldCluster = assignments.get(id);

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (CentroidCluster<T> c : theClusters)
			{
			double d = measure.distanceFromTo(c.getCentroid(), p);//c.distanceToCentroid(p);
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
		if (logger.isTraceEnabled())
			{
			logger.trace("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			logger.warn("Can't classify: " + p);
			// probably this is a GrowingKmeansClustering that has no clusters yet
			//assert false;
			}
		return result;
		}

	/*	public void addAndRecenter(T p)
	   {
	   assert p != null;
	   bestCluster(theClusters, p).addAndRecenter(p);  // this will automatically recalculate the centroid, etc.
	   }*/
	}
