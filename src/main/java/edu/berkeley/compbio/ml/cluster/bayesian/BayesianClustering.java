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

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.dsutils.IteratorProvider;
import com.davidsoergel.stats.DistanceMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import edu.berkeley.compbio.ml.cluster.AdditiveCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.OnlineClusteringMethod;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	//private T[] centroids;
	//protected DistanceMeasure<T> measure;
	//private double[] priors;

	protected double unknownDistanceThreshold;


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
	/*	public BayesianClustering(T[] theCentroids, double[] thePriors, DistanceMeasure<T> dm, double unknownThreshold)
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
	   }*/
	public BayesianClustering(DistanceMeasure<T> dm, double unknownDistanceThreshold)
		{
		measure = dm;
		this.unknownDistanceThreshold = unknownDistanceThreshold;
		}

	protected Multinomial<Cluster> priors = new Multinomial<Cluster>();

	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException
		{
		Map<String, Cluster<T>> theClusterMap = new HashMap<String, Cluster<T>>();

		try
			{
			// consume the entire iterator, ignoring initsamples
			int i = 0;
			while (trainingIterator.hasNext())
				{
				T point = trainingIterator.next();
				Cluster<T> cluster = theClusterMap.get(point.getLabel());

				if (cluster == null)
					{
					cluster = new AdditiveCluster<T>(i++, prototypeFactory.create());//measure
					//cluster.setId(i++);
					theClusterMap.put(point.getLabel(), cluster);

					//** for now we make a uniform prior
					priors.put(cluster, 1);
					}
				cluster.recenterByAdding(point);
				/*		if(cluster.getLabelCounts().uniqueSet().size() != 1)
				{
				throw new Error();
				}*/
				}
			priors.normalize();
			}
		catch (DistributionException e)
			{
			throw new Error(e);
			}
		theClusters = theClusterMap.values();
		}


	public void train(IteratorProvider<T> trainingIteratorProvider, int iterations)
		{
		// do nothing

		// after that, normalize the label probabilities
		for (Cluster c : theClusters)
			{
			c.updateLabelProbabilitiesFromCounts();
			}
		}

	// -------------------------- OTHER METHODS --------------------------

	public boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException
		{
		getBestCluster(p, secondBestDistances).recenterByAdding(p);
		return true;
		}

	public ClusterMove bestClusterMove(T p)
		{
		return null;
		}

	double bestdistance = Double.MAX_VALUE;

	public Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
			throws ClusterException, NoGoodClusterException
		{
		//int i;
		double secondbestdistance = Double.MAX_VALUE;
		bestdistance = Double.MAX_VALUE;
		Cluster<T> best = null;
		double temp;
		//int j = -1;
		for (Cluster<T> cluster : theClusters)
			{

			try
				{
				//** careful: how to deal with priors depends on the distance measure.
				// if it's probability, multiply; if log probability, add; for other distance types, who knows?

				if ((temp = measure.distanceFromTo(p, cluster.getCentroid()) * priors.get(cluster)) <= bestdistance)
					{
					secondbestdistance = bestdistance;
					bestdistance = temp;
					best = cluster;
					//j = i;
					}
				else if (temp <= secondbestdistance)
					{
					secondbestdistance = temp;
					}
				}
			catch (DistributionException e)
				{
				throw new ClusterException(e);
				}
			}


		secondBestDistances.add(secondbestdistance);
		if (best == null)
			{
			throw new ClusterException("Found no cluster at all, that's impossible");
			}
		if (bestdistance > unknownDistanceThreshold)
			{
			throw new NoGoodClusterException(
					"Best distance " + bestdistance + " > threshold " + unknownDistanceThreshold);
			}
		return best;
		}

	public double getBestdistance()
		{
		return bestdistance;
		}
	}
