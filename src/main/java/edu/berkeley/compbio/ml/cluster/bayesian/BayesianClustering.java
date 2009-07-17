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


package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.dsutils.concurrent.Parallel;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.google.common.base.Function;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performs cluster classification with a naive bayesian classifier
 *
 * @author David Tulga
 * @author David Soergel
 * @version $Id$
 */
public class BayesianClustering<T extends AdditiveClusterable<T>> extends NearestNeighborClustering<T>
		//	implements SampleInitializedOnlineClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(BayesianClustering.class);


// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public BayesianClustering(final DissimilarityMeasure<T> dm, final double unknownDistanceThreshold,
	                          final Set<String> potentialTrainingBins, final Map<String, Set<String>> predictLabelSets,
	                          final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels)
		{
		super(dm, unknownDistanceThreshold, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PrototypeBasedCentroidClusteringMethod ---------------------


	private GenericFactory<T> prototypeFactory;

	public void setPrototypeFactory(final GenericFactory<T> prototypeFactory)
		{
		assert getNumClusters() == 0;

		// ** just store the factory for now so we can use it during training

		this.prototypeFactory = prototypeFactory;

/*		int i = 0;
		for (String potentialTrainingBin : potentialTrainingBins)
			{
			try
				{
				final T centroid = prototypeFactory.create(potentialTrainingBin);
				final int clusterId = i++;
				CentroidCluster<T> cluster = new AdditiveCentroidCluster<T>(clusterId, centroid);
				theClusters.add(cluster);

				theClusterMap.put(potentialTrainingBin, cluster);
				}
			catch (GenericFactoryException e)
				{
				//logger.error("Error", e);
				//throw new ClusterRuntimeException(e);

				// ** there may be legitimate reasons why a cluster can't be created, e.g. it doesn't match a leave-one-out label
				// just ignore it
				}
			}*/
		}

// -------------------------- OTHER METHODS --------------------------

	protected synchronized void trainWithKnownTrainingLabels(final ClusterableIterator<T> trainingIterator)
		{

		final Map<String, CentroidCluster<T>> theClusterMap = new ConcurrentHashMap<String, CentroidCluster<T>>();

		//		ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();

		// the execService approach caches all the points.  In that the reason for the memory problem?


		final AtomicInteger i = new AtomicInteger(0);

		Parallel.forEach(trainingIterator, new Function<T, Void>()
		{

		public Void apply(@Nullable final T point)
			{
			// generate one cluster per exclusive training bin (regardless of the labels we want to predict).
			// the training samples must already be labelled with a bin ID.

			final String clusterBinId = point.getWeightedLabels().getDominantKeyInSet(potentialTrainingBins);

			// nearly defeats the purpose of the foreach, except that trainingIterator.next() may be expensive
			synchronized (theClusterMap)
				{
				CentroidCluster<T> cluster = theClusterMap.get(clusterBinId);

				if (cluster == null)
					{
					try
						{
						final T centroid = prototypeFactory.create(clusterBinId);
						final int clusterId = i.incrementAndGet();
						cluster = new AdditiveCentroidCluster<T>(clusterId, centroid);
						addCluster(cluster);

						theClusterMap.put(clusterBinId, cluster);
						}
					catch (GenericFactoryException e)
						{
						logger.error("Error", e);
						throw new ClusterRuntimeException(e);

						// ** there may be legitimate reasons why a cluster can't be created, e.g. it doesn't match a leave-one-out label

						}
					//throw new ClusterRuntimeException("The clusters were not all created prior to training");
					}

				// note this updates the cluster labels as well.
				// In particular, the point should already be labelled with a Training Label (not just a bin ID),
				// so that the cluster will know what labels it predicts.
				cluster.add(point);
				}
			return null;
			}
		});


		/*

	   try
		   {
		   while (true)
			   {
			   final T point = trainingIterator.next();

			   // generate one cluster per exclusive training bin (regardless of the labels we want to predict).
			   // the training samples must already be labelled with a bin ID.

			   String clusterBinId = point.getWeightedLabels().getDominantKeyInSet(potentialTrainingBins);
			   CentroidCluster<T> cluster = theClusterMap.get(clusterBinId);

			   if (cluster == null)
				   {
				   try
					   {
					   final T centroid = prototypeFactory.create(clusterBinId);
					   final int clusterId = i++;
					   cluster = new AdditiveCentroidCluster<T>(clusterId, centroid);
					   theClusters.add(cluster);

					   theClusterMap.put(clusterBinId, cluster);
					   }
				   catch (GenericFactoryException e)
					   {
					   logger.error("Error", e);
					   throw new ClusterRuntimeException(e);

					   // ** there may be legitimate reasons why a cluster can't be created, e.g. it doesn't match a leave-one-out label

					   }
				   //throw new ClusterRuntimeException("The clusters were not all created prior to training");
				   }

			   // note this updates the cluster labels as well.
			   // In particular, the point should already be labelled with a Training Label (not just a bin ID),
			   // so that the cluster will know what labels it predicts.
			   cluster.add(point);
			   }
		   }
	   catch (NoSuchElementException e)
		   {
		   // iterator exhausted
		   }*/

		//	theClusters = theClusterMap.values();
		}
	}
