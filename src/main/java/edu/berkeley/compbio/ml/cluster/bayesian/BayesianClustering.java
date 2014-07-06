/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
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

			final String clusterBinId = point.getImmutableWeightedLabels().getDominantKeyInSet(potentialTrainingBins);

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

		doneLabellingClusters();
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
