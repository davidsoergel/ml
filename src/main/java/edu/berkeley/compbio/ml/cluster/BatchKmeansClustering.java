package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.util.Set;

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class BatchKmeansClustering<T extends Clusterable<T>> extends BatchClustering<T>
	{
	private static Logger logger = Logger.getLogger(BatchKmeansClustering.class);

	private int k;

	public BatchKmeansClustering(Set<T> dataPointSet)
		{
		super(dataPointSet);
		}
/*
	public BatchKmeansClustering(Set<T> dataPointSet, int k, DistanceMeasure<T> dm)
		{
		super(dataPointSet);
		this.k = k;

		for (int i = 0; i < k; i++)
			{
			Cluster<T> c = new Cluster<T>(dm, da.next()); // initialize the clusters with the first k points
			theClusters.add(c);
			}
		}*/

	}
