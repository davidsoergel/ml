package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class OnlineClusteringMethod<T extends Clusterable<T>>
	{
	private static Logger logger = Logger.getLogger(OnlineClusteringMethod.class);
	//private Iterator<T> theDataPointProvider;

	List<Cluster<T>> theClusters = new ArrayList<Cluster<T>>();

	protected int n = 0;

	/*public OnlineClustering(Iterator<T> vp)
		{
		theDataPointProvider = vp;
		}
*/

	public abstract void add(T v);
	//public abstract void addAndRecenter(T v);
	//public abstract void reassign(T v);

	/** adjust the centroids by considering each of the incoming data points exactly once */
	public void runOnce(Iterator<T> theDataPointProvider)
		{
		int c = 0;
		Date starttime = new Date();
		while (theDataPointProvider.hasNext())
			{
			add(theDataPointProvider.next());
			if(c++ % 10 == 0)
				{

				Date endtime = new Date();
				double realtime = (endtime.getTime() - starttime.getTime()) / (double) 1000;

				logger.info("Processed " + c + " data points in " + realtime + " seconds; average " + (c / realtime) + " points/sec");
				}
			}
		}

	/** choose the best cluster for each incoming data point and report it */
	public void writeAssignmentsAsTextToStream(Iterator<T> theDataPointProvider, OutputStream outf)
		{
		int c = 0;
		Date starttime = new Date();
		PrintWriter p = new PrintWriter(outf);

		while (theDataPointProvider.hasNext())
			{
			T point = theDataPointProvider.next();
			Cluster<T> best = bestCluster(point);
			p.println(point.getId() + " " + best.getId());

			if(c++ % 10 == 0)
				{

				Date endtime = new Date();
				double realtime = (endtime.getTime() - starttime.getTime()) / (double) 1000;

				logger.info("Assigned " + c + " data points in " + realtime + " seconds; average " + (c / realtime) + " points/sec");
				}
			}
		}


/*
	*/

	public List<Cluster<T>> getClusters()
		{
		return theClusters;
		}

	//public static <T extends Clusterable<T>> Cluster<T> bestCluster(List<Cluster<T>> theClusters, T p)

	public Cluster<T> bestCluster(T p)
		{
		double bestDistance = Double.MAX_VALUE;
		Cluster<T> bestCluster = null;

		logger.debug("Chosing best cluster for " + p);
		for (Cluster<T> c : theClusters)
			{
			double d = c.distanceToCentroid(p);
			logger.debug("Trying " + c + "; distance = " + d + "; best so far = " + bestDistance);
			if (d < bestDistance)
				{
				bestDistance = d;
				bestCluster = c;
				}
			}
		logger.debug("Chose " + bestCluster);
		return bestCluster;
		}

	public int getN()
		{
		return n;
		}
	}
