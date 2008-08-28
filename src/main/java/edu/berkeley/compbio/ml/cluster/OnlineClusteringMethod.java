/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
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
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;


/**
 * A clustering method that is able to update clusters continuously as samples are added one at a time.
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class OnlineClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(OnlineClusteringMethod.class);
	//private Iterator<T> theDataPointProvider;


	// --------------------- GETTER / SETTER METHODS ---------------------

	// -------------------------- OTHER METHODS --------------------------

	//public static <T extends AdditiveClusterable<T>> Cluster<T> bestCluster(List<Cluster<T>> theClusters, T p)


	/*
	   */


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

	public abstract void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                            GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException;

	// -------------------------- INNER CLASSES --------------------------
	}
