/* $Id$ */

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

package edu.berkeley.compbio.ml.cluster.bayesian;

import edu.berkeley.compbio.ml.cluster.AdditiveCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.OnlineClusteringMethod;
import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Performs cluster classification with a naive bayesian classifier
 *
 * @author David Tulga
 * @author David Soergel
 */
public class BayesianClustering<T extends AdditiveClusterable<T>> extends OnlineClusteringMethod<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(BayesianClustering.class);

	private T[] centroids;
	private DistanceMeasure<T> measure;
	private double[] priors;

	private double unknownThreshold;


	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Creates a new BayesianClustering with the following parameters
	 *
	 * @param theCentroids     Centroids of the clusters.  Note these will be used as is and modified; clone them first if
	 *                         you need to
	 * @param thePriors        Prior expectations for the clusters
	 * @param dm               The distance measure to use
	 * @param unknownThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public BayesianClustering(T[] theCentroids, double[] thePriors, DistanceMeasure<T> dm, double unknownThreshold)
		{
		centroids = theCentroids;
		measure = dm;
		priors = thePriors;
		this.unknownThreshold = unknownThreshold;

		for (int i = 0; i < centroids.length; i++)
			{
			Cluster<T> c = new AdditiveCluster<T>(dm, theCentroids[i]);
			c.setId(i);

			theClusters.add(c);
			}
		logger.debug("initialized " + centroids.length + " clusters");
		}

	// -------------------------- OTHER METHODS --------------------------

	public boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException
		{
		theClusters.get(getBestCluster(p, secondBestDistances)).recenterByAdding(p);
		return true;
		}

	public OnlineClusteringMethod<T>.ClusterMove bestClusterMove(T p)
		{
		return null;
		}

	double bestdistance = Double.MAX_VALUE;

	public int getBestCluster(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException
		{
		int i;
		double secondbestdistance = Double.MAX_VALUE;
		bestdistance = Double.MAX_VALUE;
		double temp;
		int j = -1;
		for (i = 0; i < theClusters.size(); i++)
			{
			if ((temp = measure.distanceFromTo(p, centroids[i]) * priors[i]) <= bestdistance)
				{
				secondbestdistance = bestdistance;
				bestdistance = temp;
				j = i;
				}
			else if (temp <= secondbestdistance)
				{
				secondbestdistance = temp;
				}
			}
		secondBestDistances.add(secondbestdistance);
		if (j == -1)
			{
			throw new ClusterException("Found no cluster at all, that's impossible");
			}
		if (bestdistance > unknownThreshold)
			{
			throw new NoGoodClusterException("Best distance " + bestdistance + " > threshold " + unknownThreshold);
			}
		return j;
		}

	public double getBestdistance()
		{
		return bestdistance;
		}
	}
