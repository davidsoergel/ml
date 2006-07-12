package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

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

	public KmeansClustering(Iterator<T> dpp, int k, DistanceMeasure<T> dm) throws CloneNotSupportedException
		{
		//super(dpp);
		//this.k = k;

		for (int i = 0; i < k; i++)
			{
			// initialize the clusters with the first k points

			Cluster<T> c = new Cluster<T>(dm, dpp.next());
			c.setId(i);

			theClusters.add(c);
			}
		logger.debug("initialized " + k + " clusters");
		}

	public boolean add(T p, List<Double> secondBestDistances)
		{
		assert p != null;
		//n++;
		String id = p.getId();
		ClusterMove cm = bestClusterMove(p);
		secondBestDistances.add(cm.secondBestDistance);
		if (cm.changed())
			{
			try
				{
				cm.oldCluster.recenterByRemoving(p); //, cm.oldDistance);
				}
			catch (NullPointerException e)
				{ // probably just the first round
				}
			cm.bestCluster
					.recenterByAdding(
							p); //, cm.bestDistance);  // this will automatically recalculate the centroid, etc.
			assignments.put(id, cm.bestCluster);
			return true;
			}
		return false;
		}


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

/*	public void addAndRecenter(T p)
		{
		assert p != null;
		bestCluster(theClusters, p).addAndRecenter(p);  // this will automatically recalculate the centroid, etc.
		}*/


	}
