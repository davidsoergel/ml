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


package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * k-Nearest Neighbor classifier.  This makes sense only when multiple clusters have the same label.  In that case we
 * can look at the nearest k clusters, and vote among the labels (or even report the whole distribution).  Since this
 * corner of the code doesn't know about labels, we'll just return the top k clusters and let the label-voting happen
 * elsewhere.
 *
 * @author David Soergel
 * @version $Id$
 */
public class KNNClustering<T extends AdditiveClusterable<T>>
		extends BayesianClustering<T> //OnlineClusteringMethod<T, CentroidCluster<T>>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KNNClustering.class);


	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public KNNClustering(DissimilarityMeasure<T> dm, double unknownDistanceThreshold, int neighbors)
		{
		super(dm, unknownDistanceThreshold);
		}


	public SortedMap<Double, ClusterMove> scoredClusterMoves(T p)
		{

		SortedMap<Double, ClusterMove> result = new TreeMap<Double, ClusterMove>();
		// collect moves for all clusters, sorted by distance

		for (CentroidCluster<T> cluster : theClusters)
			{

			try
				{
				// ** careful: how to deal with priors depends on the distance measure.
				// if it's probability, multiply; if log probability, add; for other distance types, who knows?

				double distance = measure.distanceFromTo(p, cluster.getCentroid());

				double weightedDistance = distance * priors.get(cluster);
				ClusterMove cm = new ClusterMove();
				cm.bestCluster = cluster;
				cm.bestDistance = weightedDistance;

				// ignore the secondBestDistance, we don't need it here

				result.put(weightedDistance, cm);
				}
			catch (DistributionException e)
				{
				throw new ClusterRuntimeException(e);
				}
			}

		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove bestClusterMove(T p) throws NoGoodClusterException
		{
		ClusterMove result = new ClusterMove();
		//int i;
		result.secondBestDistance = Double.MAX_VALUE;
		result.bestDistance = Double.MAX_VALUE;
		//Cluster<T> best = null;
		double temp = -1;
		//int j = -1;
		for (CentroidCluster<T> cluster : theClusters)
			{

			try
				{
				// ** careful: how to deal with priors depends on the distance measure.
				// if it's probability, multiply; if log probability, add; for other distance types, who knows?

				if ((temp = measure.distanceFromTo(p, cluster.getCentroid()) * priors.get(cluster))
						<= result.bestDistance)
					{
					result.secondBestDistance = result.bestDistance;
					result.bestDistance = temp;
					result.bestCluster = cluster;
					//j = i;
					}
				else if (temp <= result.secondBestDistance)
					{
					result.secondBestDistance = temp;
					}
				}
			catch (DistributionException e)
				{
				throw new ClusterRuntimeException(e);
				}
			}

		if (result.bestCluster == null)
			{
			throw new ClusterRuntimeException(
					"None of the " + theClusters.size() + " clusters matched: " + p + ", last distance = " + temp);
			}
		if (result.bestDistance > unknownDistanceThreshold)
			{
			throw new NoGoodClusterException(
					"Best distance " + result.bestDistance + " > threshold " + unknownDistanceThreshold);
			}
		return result;
		}
	}