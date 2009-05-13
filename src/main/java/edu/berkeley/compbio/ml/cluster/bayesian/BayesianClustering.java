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

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;

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


	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public BayesianClustering(DissimilarityMeasure<T> dm, double unknownDistanceThreshold,
	                          Set<String> potentialTrainingBins, Set<String> predictLabels,
	                          Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		super(dm, unknownDistanceThreshold, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		}

	public void createClusters(final GenericFactory<T> prototypeFactory)
		{
		assert theClusters.isEmpty();

		try
			{
			final Multinomial<CentroidCluster<T>> priorsMult = new Multinomial<CentroidCluster<T>>();
			int i = 0;
			for (String potentialTrainingBin : potentialTrainingBins)
				{
				final T centroid = prototypeFactory.create(potentialTrainingBin);

				final int clusterId = i++;
				CentroidCluster<T> cluster = new AdditiveCentroidCluster<T>(clusterId, centroid);
				theClusters.add(cluster);

				//** for now we make a uniform prior
				priorsMult.put(cluster, 1);
				}
			priorsMult.normalize();
			priors = priorsMult.getValueMap();
			}
		catch (DistributionException e)
			{
			logger.error("Error", e);
			throw new ClusterRuntimeException(e);
			}
		catch (GenericFactoryException e)
			{
			logger.error("Error", e);
			throw new ClusterRuntimeException(e);
			}
		}

	//BAD just initialize all clusters, then delete the empty ones
/*	public void initializeWithSamples(Iterator<T> trainingIterator, int initSamples,
	                                  final GenericFactory<T> prototypeFactory)
		//	throws ClusterException
		//	throws GenericFactoryException, ClusterException
		{
		final Map<String, CentroidCluster<T>> theClusterMap = new HashMap<String, CentroidCluster<T>>();

		// The reason this stuff is here, rather than in train(), is that train() expects that the clusters are already defined.
		// but because of the way labelling works now, we have to consume the entire training iterator in order to know what the clusters should be.
		// we are provided with the list of potential training bins, but some of those may not actually have training samples.

		final Multinomial<CentroidCluster> priorsMult = new Multinomial<CentroidCluster>();
		try
			{
			// consume the entire iterator, ignoring initsamples
			int i = 0;

			//		ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();

			// the execService approach caches all the points.  In that the reason for the memory problem?

			while (trainingIterator.hasNext())
				{
				final T point = trainingIterator.next();
				final int clusterId = i++;
				//		execService.submit(new Runnable()
				//		{
				//		public void run()
				//			{
				try
					{
					// generate one cluster per exclusive training bin (regardless of the labels we want to predict).
					// the training samples must already be labelled with a bin ID.

					String clusterBinId = point.getWeightedLabels().getDominantKeyInSet(potentialTrainingBins);
					CentroidCluster<T> cluster = theClusterMap.get(clusterBinId);

					if (cluster == null)
						{
						final T centroid = prototypeFactory.create(point.getId());

						cluster = new AdditiveCentroidCluster<T>(clusterId, centroid);

						theClusterMap.put(clusterBinId, cluster);

						//** for now we make a uniform prior
						priorsMult.put(cluster, 1);
						}

					// note this updates the cluster labels as well.
					// In particular, the point should already be labelled with a Training Label (not just a bin ID),
					// so that the cluster will know what label it predicts.
					cluster.add(point);
					}
				catch (DistributionException e)
					{
					logger.error("Error", e);
					throw new ClusterRuntimeException(e);
					}
				catch (GenericFactoryException e)
					{
					logger.error("Error", e);
					throw new ClusterRuntimeException(e);
					}
				}
			//	});
			//	}

			//execService.finish("Processed %d training samples", 30);

			priorsMult.normalize();
			priors = priorsMult.getValueMap();
			}
		catch (DistributionException e)
			{
			throw new Error(e);
			}

		theClusters = theClusterMap.values();
		}
*/

	/**
	 * {@inheritDoc}
	 */
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException
		{
		super.train(trainingCollectionIteratorFactory, 1);

		//limitToPopulatedClusters();

		// after that, normalize the label probabilities

		normalizeClusterLabelProbabilities();
		}


	// -------------------------- OTHER METHODS --------------------------
	}
