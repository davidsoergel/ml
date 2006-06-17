package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.bugbags.BugbagsException;
import edu.berkeley.compbio.bugbags.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author lorax
 * @version 1.0
 */
public class KmeansClustering<T extends Clusterable<T>> extends OnlineClusteringMethod<T>
	{
	private static Logger logger = Logger.getLogger(KmeansClustering.class);

	//private int k;

	public KmeansClustering(Iterator<T> dpp, int k, DistanceMeasure<T> dm)
		{
		//super(dpp);
		//this.k = k;

		for (int i = 0; i < k; i++)
			{
			// initialize the clusters with the first k points
			T t = dpp.next();
			Cluster<T> c = new Cluster<T>(dm, t);
			c.add(t);
			theClusters.add(c);
			}
		}


	public void add(T p)
		{
		bestCluster(theClusters, p).add(p);  // this will automatically recalculate the centroid, etc.
		}


	public void addAndRecenter(T p)
		{
		bestCluster(theClusters, p).addAndRecenter(p);  // this will automatically recalculate the centroid, etc.
		}


	public boolean batchUpdate() throws BugbagsException
		{
		List<Cluster<T>> oldClusters = theClusters;
		theClusters = new ArrayList<Cluster<T>>();

		// what if a cluster ends up empty??

		for (Cluster<T> c : oldClusters)
			{
			Cluster<T> n = new Cluster<T>(c.getTheDistanceMeasure(), c.getCentroid());
			logger.debug(theClusters.add(n));  // this just fails when theClusters is a HashSet. ???
			assert theClusters.contains(n);
			//c.clear();
			//new Cluster<T>(c.getTheDistanceMeasure(), c.getCentroid()));
			}
		for (Cluster<T> o : oldClusters)
			{
			for (T point : o)
				{
				bestCluster(theClusters, point).add(point);
				}
			}
		boolean changed = false;
		for (Cluster<T> o : theClusters)
			{
			changed = changed || o.recalculateCentroid();
			}
		return changed;
		}

	public static <T extends Clusterable<T>> Cluster<T> bestCluster(List<Cluster<T>> theClusters, T p)
		{
		double bestDistance = Double.MAX_VALUE;
		Cluster<T> bestCluster = null;

		for (Cluster<T> c : theClusters)
			{
			double d = c.distanceToCentroid(p);
			if (d < bestDistance)
				{
				bestDistance = d;
				bestCluster = c;
				}
			}
		return bestCluster;
		}

	}
