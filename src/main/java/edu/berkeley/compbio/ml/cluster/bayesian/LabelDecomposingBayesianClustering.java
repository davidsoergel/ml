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
import com.davidsoergel.stats.DistanceMeasure;
import com.davidsoergel.stats.DistributionException;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.kmeans.GrowableKmeansClustering;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Performs cluster classification with a naive bayesian classifier
 *
 * @author David Tulga
 * @author David Soergel
 * @version $Id$
 */
public class LabelDecomposingBayesianClustering<T extends AdditiveClusterable<T>> extends BayesianClustering<T>
	{
	private static final Logger logger = Logger.getLogger(LabelDecomposingBayesianClustering.class);

	public LabelDecomposingBayesianClustering(DistanceMeasure<T> dm, double unknownDistanceThreshold)
		{
		super(dm, unknownDistanceThreshold);
		}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory) throws ClusterException
		{
		Map<String, GrowableKmeansClustering<T>> theSubclusteringMap =
				new HashMap<String, GrowableKmeansClustering<T>>();

		try
			{
			// consume the entire iterator, ignoring initsamples
			int i = 0;
			while (trainingIterator.hasNext())
				{
				T point = trainingIterator.next();

				String bestLabel = point.getWeightedLabels().getDominantKeyInSet(mutuallyExclusiveLabels);
				//Cluster<T> cluster = theClusterMap.get(bestLabel);


				GrowableKmeansClustering<T> theIntraLabelClustering = theSubclusteringMap.get(bestLabel);

				if (theIntraLabelClustering == null)
					{
					theIntraLabelClustering = new GrowableKmeansClustering<T>(measure);
					theSubclusteringMap.put(bestLabel, theIntraLabelClustering);
					}

				// naive online agglomerative clustering:
				// add points to clusters in the order they arrive, one pass only, create new clusters as needed

				// the resulting clustering may suck, but it should still more or less span the space of the inputs,
				// so it may work well enough for this purpose.

				// doing proper k-means would be nicer, but then we'd have to store all the  training points, or re-iterate them somehow.

				ClusterMove<T, CentroidCluster<T>> cm = theIntraLabelClustering.bestClusterMove(point);

				CentroidCluster<T> cluster = cm.bestCluster;

				if (cm.bestDistance > unknownDistanceThreshold)
					{
					logger.info("Creating new subcluster (" + cm.bestDistance + " > " + unknownDistanceThreshold
							+ ") for " + bestLabel);
					cluster = new AdditiveCentroidCluster<T>(i++, prototypeFactory.create());
					//cluster.setId(i++);

					// add the new cluster to the local per-label clustering...
					theIntraLabelClustering.addCluster(cluster);

					// ... and also to the overall clustering
					theClusters.add(cluster);

					// REVIEW for now we make a uniform prior
					priors.put(cluster, 1);
					}
				cluster.add(point);
				/*		if(cluster.getLabelCounts().uniqueSet().size() != 1)
				{
				throw new Error();
				}*/
				}
			priors.normalize();
			//theClusters = theSubclusteringMap.values();

			for (Map.Entry<String, GrowableKmeansClustering<T>> entry : theSubclusteringMap.entrySet())
				{
				String label = entry.getKey();
				GrowableKmeansClustering<T> theIntraLabelClustering = entry.getValue();
				if (logger.isInfoEnabled())
					{
					logger.info("Created " + theIntraLabelClustering.getClusters().size() + " clusters from "
							+ theIntraLabelClustering.getN() + " points for " + label);
					}
				}
			}
		catch (DistributionException e)
			{
			throw new ClusterException(e);
			}
		catch (GenericFactoryException e)
			{
			throw new ClusterException(e);
			}
		}
	}