package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class OnlineClusteringMethod<T extends Clusterable<T>>
	{
	private static Logger logger = Logger.getLogger(OnlineClusteringMethod.class);
	//private Iterator<T> theDataPointProvider;

	List<Cluster<T>> theClusters = new ArrayList<Cluster<T>>();

	private Map<String, Cluster<T>> assignments = new HashMap<String, Cluster<T>>();  // see whether anything changed

	protected int n = 0;

	/*public OnlineClustering(Iterator<T> vp)
		{
		theDataPointProvider = vp;
		}
*/

	//public abstract void add(T v);


	public boolean add(T p)
		{
		assert p != null;
		//n++;
		String id = p.getId();
		ClusterMove cm = bestClusterMove(p);
		if (cm.changed())
			{
			try
				{
				cm.oldCluster.recenterByRemoving(p, cm.oldDistance);
				}
			catch (NullPointerException e)
				{ // probably just the first round
				}
			cm.bestCluster
					.recenterByAdding(p, cm.bestDistance);  // this will automatically recalculate the centroid, etc.
			assignments.put(id, cm.bestCluster);
			return true;
			}
		return false;
		}
	//public abstract void addAndRecenter(T v);
	//public abstract void reassign(T v);

	/**
	 * adjust the centroids by considering each of the incoming data points exactly once per iteration
	 */
	public void run(ClusterableIterator<T> theDataPointProvider, int iterations) throws IOException
		{
		//Date totalstarttime = new Date();
		for (int i = 0; i < iterations; i++)
			{
			int changed = 0;
			theDataPointProvider.reset();
			//normalizeClusters();
			int c = 0;
			Date starttime = new Date();
			while (theDataPointProvider.hasNext())
				{
				if (add(theDataPointProvider.next()))
					{
					changed++;
					}
				c++;
				if (c % 1000 == 0)
					{
					Date endtime = new Date();
					double realtime = (endtime.getTime() - starttime.getTime()) / (double) 1000;

					logger.info("Processed " + c + " data points in " + realtime + " seconds; average " + (c / realtime)
							+ " points/sec");
					}
				}
			logger.info("Changed cluster assignment of " + changed + " points (" + (int) (100 * changed / c) + "%)\n");
			logger.info("\n" + clusteringStats());
			if (changed == 0)
				{
				logger.info("Steady state, done after " + (i+1) + " iterations!");
				break;
				}
			}
		}

	/**
	 * choose the best cluster for each incoming data point and report it
	 */
	public void writeAssignmentsAsTextToStream(OutputStream outf)
		{
		int c = 0;
		PrintWriter p = new PrintWriter(outf);
		for (String s : assignments.keySet())
			{
			p.println(s + " " + assignments.get(s).getId());
			}
		p.flush();
		}

/*
   */

	public List<Cluster<T>> getClusters()
		{
		return theClusters;
		}

	//public static <T extends Clusterable<T>> Cluster<T> bestCluster(List<Cluster<T>> theClusters, T p)

	public ClusterMove bestClusterMove(T p)
		{
		ClusterMove result = new ClusterMove();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		String id = p.getId();
		result.oldCluster = assignments.get(id);

		if (logger.isDebugEnabled())
			{
			logger.debug("Choosing best cluster for " + p);
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
				result.bestDistance = d;
				result.bestCluster = c;
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

	private class ClusterMove
		{
		Cluster<T> bestCluster;
		double bestDistance = Double.MAX_VALUE;
		;
		Cluster<T> oldCluster;
		double oldDistance;
//
//		public clusterMove(Cluster<T> newC, double newDistance, double oldDistance)
//			{
//			this.newC = newC;
//			this.newDistance = newDistance;
//			this.oldDistance = oldDistance;
//			}

		public boolean changed()
			{
			return (oldCluster == null || bestCluster.equals(oldCluster));
			}
		}

	public int getN()
		{
		return n;
		}

	public String clusteringStats()
		{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		writeClusteringStatsToStream(b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(OutputStream outf)
		{
		PrintWriter p = new PrintWriter(outf);
		for (Cluster<T> c : theClusters)
			{
			p.println(c);
			double stddev1 = c.getStdDev();
			for (Cluster<T> d : theClusters)
				{
				double distance = c.distanceToCentroid(d.getCentroid());
				double stddev2 = d.getStdDev();
				double margin1 = distance - (stddev1 + stddev2);
				double margin2 = distance - 2 * (stddev1 + stddev2);

				p.print("     " + distance + "(" + margin1 + ", " + margin2 + ")");
				}
			p.println();
			}
		p.flush();
		}
	}
