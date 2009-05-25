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

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A clustering method that is able to update clusters continuously as samples are added one at a time.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractClusteringMethod<T, C> implements OnlineClusteringMethod<T> //, CentroidClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractOnlineClusteringMethod.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public AbstractOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                      Map<String, Set<String>> predictLabelSets, Set<String> leaveOneOutLabels,
	                                      Set<String> testLabels, int testThreads)
		{
		super(dm, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels, testThreads);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnlineClusteringMethod ---------------------

	//private Iterator<T> theDataPointProvider;
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
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int trainingEpochs)
			throws ClusterException
		{
		for (int i = 0; i < trainingEpochs; i++)
			{
			trainOneIteration(trainingCollectionIteratorFactory);
			}
		normalizeClusterLabelProbabilities();
		}

// -------------------------- OTHER METHODS --------------------------

	protected boolean trainOneIteration(
			CollectionIteratorFactory<T> trainingCollectionIteratorFactory) //, List<Double> secondBestDistances
			throws ClusterException
		{
		int changed = 0;
		Iterator<T> trainingIterator = trainingCollectionIteratorFactory.next();
		//normalizeClusters();
		int c = 0;
		Date starttime = new Date();
		//secondBestDistances.clear();
		while (trainingIterator.hasNext())
			{
			try
				{
				if (add(trainingIterator.next()))
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
			/*	if (logger.isDebugEnabled() && c % 1000 == 0)
			   {
			   Date endtime = new Date();
			   double realtime = (endtime.getTime() - starttime.getTime()) / (double) 1000;

			   logger.debug(
					   new Formatter().format("%d p/%d sec = %d p/sec; specificity = %.3f; %s", c, (int) realtime,
											  (int) (c / realtime),
											  (DSCollectionUtils.sum(secondBestDistances) / (double) c),
											  shortClusteringStats()));
			   //					logger.info("" + c + " p/" + (int) realtime + " sec = " + (int) (c / realtime)
			   //							+ " p/sec; specificity = " + (ArrayUtils.sum(secondBestDistances) / (double) c) + " " + shortClusteringStats());
			   }*/
			/*	if (c >= maxpoints)
			   {
			   break;
			   }*/
			}
		int changedProportion = changed == 0 ? 0 : (int) (100.0 * changed / c);
		logger.debug("Changed cluster assignment of " + changed + " points (" + changedProportion + "%)\n");
		// computeClusterStdDevs(theDataPointProvider);  // PERF cluster stddev is slow, should be optional.  Also, only works for sequential DPP
		if (logger.isDebugEnabled())
			{
			logger.debug("\n" + clusteringStats());
			}

		return changed == 0;
		}

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

	/**
	 * Create some initial clusters using the first few training samples.  This is not the same as the training itself!
	 *
	 * Okay, but we don't want to separate it from the training phase; let the implementation decide
	 *
	 * @param trainingIterator
	 * @param initSamples
	 * @throws GenericFactoryException
	 * @throws ClusterException
	 */
//	public abstract void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
//	                                            GenericFactory<T> prototypeFactory)
//			throws GenericFactoryException, ClusterException;
	//public abstract void createClusters(T sequenceFragment);
	}
