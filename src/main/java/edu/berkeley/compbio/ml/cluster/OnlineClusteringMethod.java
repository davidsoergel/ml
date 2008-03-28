/* $Id$ */

/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.dsutils.IteratorProvider;
import com.davidsoergel.dsutils.MathUtils;
import com.davidsoergel.dsutils.MersenneTwisterFast;
import com.davidsoergel.stats.DistributionException;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class OnlineClusteringMethod<T extends Clusterable<T>> implements ClusterSet<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(OnlineClusteringMethod.class);
	//private Iterator<T> theDataPointProvider;

	protected Collection<Cluster<T>> theClusters = new ArrayList<Cluster<T>>();

	protected Map<String, Cluster<T>> assignments = new HashMap<String, Cluster<T>>();// see whether anything changed

	protected int n = 0;


	public Cluster<T> getAssignment(String id)
		{
		return assignments.get(id);
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	public int getN()
		{
		return n;
		}

	// -------------------------- OTHER METHODS --------------------------

	//public static <T extends AdditiveClusterable<T>> Cluster<T> bestCluster(List<Cluster<T>> theClusters, T p)

	/**
	 * Return a ClusterMove object describing the best way to reassign the given point to a new cluster.
	 *
	 * @param p
	 * @return
	 */
	public abstract ClusterMove bestClusterMove(T p) throws NoGoodClusterException;


	/**
	 * for each cluster, compute the standard deviation of the distance of each point to the centroid. This does not
	 * reassign points or move the centroids.
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

	/**
	 * Returns the best cluster without adding the point
	 *
	 * @param p                   Point to find the best cluster of
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 */
	public abstract Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
			throws ClusterException, NoGoodClusterException;


	public List<T> getCentroids()
		{
		List<T> result = new ArrayList<T>();
		for (Cluster<T> c : theClusters)
			{
			result.add(c.getCentroid());
			}
		return result;
		}

	/*
	   */

	public Collection<? extends Cluster<T>> getClusters()
		{
		return theClusters;
		}


	protected Cluster<T> chooseRandomCluster()
		{
		//** slow, but rarely used

		int index = MersenneTwisterFast.randomInt(theClusters.size());
		Iterator<? extends Cluster<T>> iter = theClusters.iterator();
		Cluster<T> result = iter.next();
		for (int i = 0; i < index; result = iter.next())
			{
			;
			}
		return result;

		//return theClusters.get(MersenneTwisterFast.randomInt(theClusters.size()));
		}


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
	 * consider each of the incoming data points exactly once per iteration.  Note iterations > 1 only makes sense for
	 * unsupervised clustering.
	 */
	public void train(IteratorProvider<T> trainingIteratorProvider, int iterations)
			throws IOException, ClusterException//, int maxpoints) throws IOException
		{
		//Date totalstarttime = new Date();
		List<Double> secondBestDistances = new ArrayList<Double>();
		for (int i = 0; i < iterations; i++)
			{
			int changed = 0;
			Iterator<T> trainingIterator = trainingIteratorProvider.next();
			//normalizeClusters();
			int c = 0;
			Date starttime = new Date();
			secondBestDistances.clear();
			while (trainingIterator.hasNext())
				{
				try
					{
					if (add(trainingIterator.next(), secondBestDistances))
						{
						changed++;
						}
					}
				catch (NoGoodClusterException e)
					{
					// too bad, just ignore this unclassifiable point.
					// it may be classifiable in a future iteration.
					// if no other points get changed, then this one will stay unclassified.
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

	/**
	 * Adds a point to the best cluster.  Generally it's not a good idea to store the point itself in the cluster for
	 * memory reasons; so this method is primarily useful for updating the position of the centroid.
	 *
	 * @param p
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 * @return
	 * @throws ClusterException
	 * @throws NoGoodClusterException
	 */
	public abstract boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException;

	/*public OnlineClustering(Iterator<T> vp)
		{
		theDataPointProvider = vp;
		}
*/

	//public abstract void add(T v);


	public String shortClusteringStats()
		{
		List<Double> distances = new ArrayList<Double>();
		int numDistances = 0;
		for (Cluster<T> c : theClusters)
			{
			for (Cluster<T> d : theClusters)
				{
				double distance = c.distanceToCentroid(d.getCentroid());
				if (c == d && !MathUtils.equalWithinFPError(distance, 0))
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
				if (c == d && !MathUtils.equalWithinFPError(distance, 0))
					{
					logger.warn("Floating point trouble: self distance = " + distance + " " + c);
					assert false;
					}
				double stddev2 = d.getStdDev();
				double margin1 = distance - (stddev1 + stddev2);
				double margin2 = distance - 2 * (stddev1 + stddev2);

				p.printf("\t%.2f (%.2f)", distance, margin1);//,  margin2);
				}
			p.println();
			}
		p.flush();
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

	public abstract void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                            GenericFactory<T> prototypeFactory) throws GenericFactoryException;

	// -------------------------- INNER CLASSES --------------------------

	public class TestResults
		{
		public double correct = 0;
		public double wrong = 0;
		public double unknown = 0;

		public void normalize()
			{
			double total = correct + wrong + unknown;
			correct /= total;
			wrong /= total;
			unknown /= total;
			}
		}

	public TestResults test(Iterator<T> theTestIterator, double unknownThreshold)
			throws ClusterException, NoGoodClusterException, DistributionException
		{
		// evaluate labeling correctness using the test samples

		List<Double> secondBestDistances = new ArrayList<Double>();
		TestResults tr = new TestResults();

		int i = 0;
		while (theTestIterator.hasNext())
			{
			T frag = theTestIterator.next();
			Cluster<T> best = getBestCluster(frag, secondBestDistances);
			double prob = best.getDominantProbability();
			if (prob <= unknownThreshold)
				{
				tr.unknown++;
				}
			else
				{
				String dominantLabel = best.getDominantLabel();
				if (frag.getLabel().equals(dominantLabel))
					{
					tr.correct++;
					}
				else
					{
					tr.wrong++;
					}
				}
			if (i % 100 == 0)
				{
				logger.info("Tested " + i + " samples.");
				}
			i++;
			}
		tr.normalize();
		logger.info("Tested " + i + " samples.");
		//	return i;
		return tr;
		}
	}
