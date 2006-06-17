package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.util.Set;

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public abstract class BatchClustering<T extends Clusterable<T>>
	{
	private static Logger logger = Logger.getLogger(BatchClustering.class);

	private Set<T> theDataPoints;

	Set<Cluster<T>> theClusters;

	public BatchClustering(Set<T> dataPointSet)
		{
		theDataPoints = dataPointSet;
		}

	public BatchClustering(Set<Cluster<T>> preexistingClusters, Set<T> newDataPointsToAdd)
		{
		theClusters = preexistingClusters;
		// TODO
		}

	//public abstract void run(int iterations);

	public Set<Cluster<T>> getClusters()
		{
		return theClusters;
		}


	}
