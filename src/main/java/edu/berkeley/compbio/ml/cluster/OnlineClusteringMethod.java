package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.dsutils.MathUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
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

	protected Map<String, Cluster<T>> assignments = new HashMap<String, Cluster<T>>();  // see whether anything changed

	protected int n = 0;

	/*public OnlineClustering(Iterator<T> vp)
		{
		theDataPointProvider = vp;
		}
*/

	//public abstract void add(T v);

	public abstract boolean add(T p, List<Double> secondBestDistances);

	//public abstract void addAndRecenter(T v);
	//public abstract void reassign(T v);

	/**
	 * adjust the centroids by considering each of the incoming data points exactly once per iteration
	 */
/*	public void run(ClusterableIterator<T> theDataPointProvider, int iterations) throws IOException
		{
		run(theDataPointProvider, iterations, Integer.MAX_VALUE);
		}*/

	/**
	 * consider each of the incoming data points exactly once per iteration.  Note iterations > 1 only makes sense for unsupervised clustering.
	 */
	public void run(ClusterableIterator<T> theDataPointProvider, int iterations)
			throws IOException //, int maxpoints) throws IOException
		{
		//Date totalstarttime = new Date();
		List<Double> secondBestDistances = new ArrayList<Double>();
		for (int i = 0; i < iterations; i++)
			{
			int changed = 0;
			theDataPointProvider.reset();
			//normalizeClusters();
			int c = 0;
			Date starttime = new Date();
			secondBestDistances.clear();
			while (theDataPointProvider.hasNext())
				{
				if (add(theDataPointProvider.next(), secondBestDistances))
					{
					changed++;
					}

				c++;
				if (c % 1000 == 0)
					{
					Date endtime = new Date();
					double realtime = (endtime.getTime() - starttime.getTime()) / (double) 1000;

					logger.info(new Formatter().format("%d p/%d sec = %d p/sec; specificity = %.3f; %s", c,
					                                   (int) realtime, (int) (c / realtime),
					                                   (ArrayUtils.sum(secondBestDistances) / (double) c),
					                                   shortClusteringStats()));
//					logger.info("" + c + " p/" + (int) realtime + " sec = " + (int) (c / realtime)
//							+ " p/sec; specificity = " + (ArrayUtils.sum(secondBestDistances) / (double) c) + " " + shortClusteringStats());
					}
				/*	if (c >= maxpoints)
				   {
				   break;
				   }*/
				}
			logger.info("Changed cluster assignment of " + changed + " points (" + (int) (100 * changed / c) + "%)\n");
			// computeClusterStdDevs(theDataPointProvider);  // ** Slow, should be optional.  Also, only works for sequential DPP
			logger.info("\n" + clusteringStats());
			if (changed == 0)
				{
				logger.info("Steady state, done after " + (i + 1) + " iterations!");
				break;
				}
			}
		}

	/*private void normalizeClusters()
		{
		// This is a little tricky because we want to set the sample size of each cluster back to 0
		// while leaving its position intact; but its position _is_ just the sum of the Kcounts of
		// its members.  OK, wer, the problem just passes through to Kcount.normalize()

		for(Cluster c : theClusters)
			{
			c.normalize();
			}

		}*/

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

	/**
	 * for each cluster, compute the standard deviation of the distance of each point to the centroid.
	 * This does not reassign points or move the centroids.
	 *
	 * @param theDataPointProvider
	 */
	public void computeClusterStdDevs(ClusterableIterator<T> theDataPointProvider) throws IOException
		{
		theDataPointProvider.reset();
		for (Cluster<T> c : theClusters)
			{
			c.setSumOfSquareDistances(0);
			}
		while (theDataPointProvider.hasNext())
			{
			T p = theDataPointProvider.next();
			Cluster<T> c = assignments.get(p.getId());
			double dist = c.distanceToCentroid(p);
			c.addToSumOfSquareDistances(dist * dist);
			}
		}

/*
   */

	public List<Cluster<T>> getClusters()
		{
		return theClusters;
		}

	//public static <T extends Clusterable<T>> Cluster<T> bestCluster(List<Cluster<T>> theClusters, T p)


	public abstract ClusterMove bestClusterMove(T p);


	protected class ClusterMove
		{
		Cluster<T> bestCluster;
		double bestDistance = Double.MAX_VALUE;
		Cluster<T> oldCluster;
		double oldDistance;
		double secondBestDistance = 0;
//
//		public clusterMove(Cluster<T> newC, double newDistance, double oldDistance)
//			{
//			this.newC = newC;
//			this.newDistance = newDistance;
//			this.oldDistance = oldDistance;
//			}

		public boolean changed()
			{
			return (oldCluster == null || (! bestCluster.equals(oldCluster)));
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
				if (c == d && ! MathUtils.equalWithinFPError(distance, 0))
					{
					logger.warn("Floating point trouble: self distance = " + distance + " " + c);
					assert false;
					}
				double stddev2 = d.getStdDev();
				double margin1 = distance - (stddev1 + stddev2);
				double margin2 = distance - 2 * (stddev1 + stddev2);

				p.printf("\t%.2f (%.2f)", distance, margin1); //,  margin2);
				}
			p.println();
			}
		p.flush();
		}


	public String shortClusteringStats()
		{
		List<Double> distances = new ArrayList<Double>();
		int numDistances = 0;
		for (Cluster<T> c : theClusters)
			{
			for (Cluster<T> d : theClusters)
				{
				double distance = c.distanceToCentroid(d.getCentroid());
				if (c == d && ! MathUtils.equalWithinFPError(distance, 0))
					{
					logger.warn("Floating point trouble: self distance = " + distance + " " + c);
					assert false;
					}
				if (c != d)
					{
					logger.debug("Distance between clusters = " + d);
					distances.add(distance);
					}
				}
			}
		double avg = ArrayUtils.sum(distances) / (double) distances.size();
		double sd = 0;
		for (double d : distances)
			{
			sd += d * d;
			}
		sd = Math.sqrt(sd / (double) distances.size());

		return new Formatter().format("Separation: %.3f (%.3f)", avg, sd).toString();

		}
	}
