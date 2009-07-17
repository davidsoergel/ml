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
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import com.google.common.collect.ImmutableMap;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.ml.cluster.kmeans.GrowableKmeansClustering;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Performs a simple unsupervised clustering on all the samples with a given label in an attempt to decompose the
 * label-level cluster into several smaller clusters.  This is an unusual case in that it both requires a prototype
 * factory and is sample-initialized.
 *
 * @author David Soergel
 * @version $Id$
 */
public class LabelDecomposingBayesianClustering<T extends AdditiveClusterable<T>> extends NearestNeighborClustering<T>
		//	implements SampleInitializedOnlineClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(LabelDecomposingBayesianClustering.class);


	GenericFactory<T> prototypeFactory;


// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public LabelDecomposingBayesianClustering(final DissimilarityMeasure<T> dm, final double unknownDistanceThreshold,
	                                          final Set<String> potentialTrainingBins,
	                                          final Map<String, Set<String>> predictLabelSets,
	                                          final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels)
		{
		super(dm, unknownDistanceThreshold, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PrototypeBasedCentroidClusteringMethod ---------------------

	public void setPrototypeFactory(final GenericFactory<T> prototypeFactory) throws GenericFactoryException
		{
		this.prototypeFactory = prototypeFactory;
		}

// --------------------- Interface SampleInitializedOnlineClusteringMethod ---------------------

	protected void trainWithKnownTrainingLabels(final ClusterableIterator<T> trainingIterator)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public void initializeWithSamples(final ClusterableIterator<T> trainingIterator,
	                                  final int initSamples) //GenericFactory<T> prototypeFactory)
		//	throws ClusterException
		{
		final Map<String, GrowableKmeansClustering<T>> theSubclusteringMap =
				new HashMap<String, GrowableKmeansClustering<T>>();

		if (predictLabelSets.size() > 1)
			{
			throw new ClusterRuntimeException(
					"LabelDecomposingBayesianClustering can't yet handle more than one exclusive label set at a time: "
					+ predictLabelSets.keySet());
			}

		final Set<String> predictLabels = predictLabelSets.values().iterator().next();
		try
			{
			// BAD consume the entire iterator, ignoring initsamples
			final Multinomial<Cluster<T>> priorsMult = new Multinomial<Cluster<T>>();
			try
				{
				int i = 0;
				while (true)
					{

					final T point = trainingIterator.next();

					final String bestLabel = point.getWeightedLabels().getDominantKeyInSet(predictLabels);
//Cluster<T> cluster = theClusterMap.get(bestLabel);


					GrowableKmeansClustering<T> theIntraLabelClustering = theSubclusteringMap.get(bestLabel);

					if (theIntraLabelClustering == null)
						{
						theIntraLabelClustering =
								new GrowableKmeansClustering<T>(measure, potentialTrainingBins, predictLabelSets,
								                                prohibitionModel, testLabels);
						theSubclusteringMap.put(bestLabel, theIntraLabelClustering);
						}

					// naive online agglomerative clustering:
					// add points to clusters in the order they arrive, one pass only, create new clusters as needed

					// the resulting clustering may suck, but it should still more or less span the space of the inputs,
					// so it may work well enough for this purpose.

					// doing proper k-means would be nicer, but then we'd have to store all the training points, or re-iterate them somehow.

					final ClusterMove<T, CentroidCluster<T>> cm = theIntraLabelClustering.bestClusterMove(point);

					CentroidCluster<T> cluster = cm.bestCluster;

					if (cm.bestDistance > unknownDistanceThreshold)
						{
						logger.debug("Creating new subcluster (" + cm.bestDistance + " > " + unknownDistanceThreshold
						             + ") for " + bestLabel);
						cluster = new AdditiveCentroidCluster<T>(i++, prototypeFactory.create());
//cluster.setId(i++);

// add the new cluster to the local per-label clustering...
						theIntraLabelClustering.addCluster(cluster);

// ... and also to the overall clustering
						addCluster(cluster);

// REVIEW for now we make a uniform prior
						priorsMult.put(cluster, 1);
						}
					cluster.add(point);
/*		if(cluster.getLabelCounts().uniqueSet().size() != 1)
			{
			throw new Error();
			}*/

					}
				}
			catch (NoSuchElementException e)
				{
				// iterator exhausted
				}
			priorsMult.normalize();


//			clusterPriors = priorsMult.getValueMap();

			final ImmutableMap.Builder<Cluster<T>, Double> builder = ImmutableMap.builder();
			clusterPriors = builder.putAll(priorsMult.getValueMap()).build();


//theClusters = theSubclusteringMap.values();

			for (final Map.Entry<String, GrowableKmeansClustering<T>> entry : theSubclusteringMap.entrySet())
				{
				final String label = entry.getKey();
				final GrowableKmeansClustering<T> theIntraLabelClustering = entry.getValue();
				if (logger.isInfoEnabled())
					{
					logger.info("Created " + theIntraLabelClustering.getClusters().size() + " clusters from "
					            + theIntraLabelClustering.getN() + " points for " + label);
					}
				}
			}
		catch (DistributionException e)
			{
			throw new ClusterRuntimeException(e);
			}
		catch (GenericFactoryException e)
			{
			throw new ClusterRuntimeException(e);
			}
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
/*	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException
		{
		//super.train(trainingCollectionIteratorFactory);

		//limitToPopulatedClusters();

		// after that, normalize the label probabilities

		removeEmptyClusters();
		normalizeClusterLabelProbabilities();
		}*/
	}
