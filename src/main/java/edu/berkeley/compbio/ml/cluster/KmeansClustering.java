package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author lorax
 * @version 1.0
 */
public class KmeansClustering<T extends Clusterable<T>> extends OnlineClusteringMethod<T>
	{
	private static Logger logger = Logger.getLogger(KmeansClustering.class);

	//private int k;

	public KmeansClustering(Iterator<T> dpp, int k, DistanceMeasure<T> dm) throws CloneNotSupportedException
		{
		//super(dpp);
		//this.k = k;

		for (int i = 0; i < k; i++)
			{
			// initialize the clusters with the first k points

			Cluster<T> c = new Cluster<T>(dm, dpp.next());

			theClusters.add(c);
			}
		logger.debug("initialized " + k + " clusters");
		}


	public void add(T p)
		{
		n++;
		bestCluster(p).recenterByAdding(p);  // this will automatically recalculate the centroid, etc.
		}


/*	public void addAndRecenter(T p)
		{
		assert p != null;
		bestCluster(theClusters, p).addAndRecenter(p);  // this will automatically recalculate the centroid, etc.
		}*/


	}
