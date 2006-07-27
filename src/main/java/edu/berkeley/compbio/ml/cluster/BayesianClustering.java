package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;

import java.util.List;

/**
 * User: David
 * Date: Jul 11, 2006
 * Time: 4:14:05 PM
 */
public class BayesianClustering<T extends Clusterable<T>> extends OnlineClusteringMethod<T>
	{
	private static Logger logger = Logger.getLogger(BayesianClustering.class);

	private T[] centroids;
	private DistanceMeasure<T> measure;
	private double[] priors;

	public BayesianClustering(T[] theCentroids, double[] thePriors, DistanceMeasure<T> dm)
		{
		centroids = theCentroids;
		measure = dm;
		priors = thePriors;

		for (int i = 0; i < centroids.length; i++)
			{
			Cluster<T> c = new Cluster<T>(dm, theCentroids[i]);
			c.setId(i);

			theClusters.add(c);
			}
		logger.debug("initialized " + centroids.length + " clusters");
		}

	public boolean add(T p, List<Double> secondBestDistances)
		{
		theClusters.get(getBestCluster(p, secondBestDistances)).recenterByAdding(p);
		return true;
		}

	public OnlineClusteringMethod<T>.ClusterMove bestClusterMove(T p)
		{
		return null;
		}

	public int getBestCluster(T p, List<Double> secondBestDistances)
		{
		int i;
		double secondbestdistance = Double.MAX_VALUE;
		double bestdistance = Double.MAX_VALUE;
		double temp;
		int j = -1;
		for(i = 0; i < theClusters.size(); i++)
			{
			if((temp = measure.distanceBetween(centroids[i], p) * priors[i]) <= bestdistance)
				{
				secondbestdistance = bestdistance;
				bestdistance = temp;
				j = i;
				}
			else if(temp <= secondbestdistance)
				{
				secondbestdistance = temp;
				}
			}
		secondBestDistances.add(secondbestdistance);
		return j;
		}
	}
