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
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.ProbabilisticDissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.BasicCentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * k-Nearest Neighbor classifier.  This makes sense only when multiple clusters have the same label.  In that case we
 * can look at the nearest k clusters, and vote among the labels (or even report the whole distribution).  Since this
 * corner of the code doesn't know about labels, we'll just return the top k clusters and let the label-voting happen
 * elsewhere.
 *
 * @author David Soergel
 * @version $Id$
 */
public class KNNClustering<T extends AdditiveClusterable<T>>
		extends BayesianClustering<T> //OnlineClusteringMethod<T, CentroidCluster<T>>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KNNClustering.class);

	private final int maxNeighbors;
	private double voteProportionThreshold;
	private double distanceTieThresholdRatio;
	private double voteTieThresholdRatio;
	//	private double decompositionDistanceThreshold;

	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public KNNClustering(DissimilarityMeasure<T> dm, double unknownDistanceThreshold, int maxNeighbors,
	                     double voteProportionThreshold, double voteTieThresholdRatio,
	                     double distanceTieThresholdRatio) //, double decompositionDistanceThreshold)
		{
		super(dm, unknownDistanceThreshold);
		this.maxNeighbors = maxNeighbors;
		//		this.decompositionDistanceThreshold = decompositionDistanceThreshold;
		this.voteProportionThreshold = voteProportionThreshold;
		this.voteTieThresholdRatio = voteTieThresholdRatio;
		this.distanceTieThresholdRatio = distanceTieThresholdRatio;
		}

	//** clean up code redundancy etc.
	/**
	 * COPIED FROM LabelDecomposingBayesianClustering
	 */
	/*	@Override
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

				 // doing proper k-means would be nicer, but then we'd have to store all the training points, or re-iterate them somehow.

				 ClusterMove<T, CentroidCluster<T>> cm = theIntraLabelClustering.bestClusterMove(point);

				 CentroidCluster<T> cluster = cm.bestCluster;

				 if (cm.bestDistance > decompositionDistanceThreshold)
					 {
					 logger.info(
							 "Creating new subcluster (" + cm.bestDistance + " > " + decompositionDistanceThreshold + ") for "
									 + bestLabel);
					 cluster = new AdditiveCentroidCluster<T>(i++, prototypeFactory.create());
					 //cluster.setId(i++);

					 // add the new cluster to the local per-label clustering...
					 theIntraLabelClustering.addCluster(cluster);

					 // ... and also to the overall clustering
					 theClusters.add(cluster);

					 // REVIEW for now we make a uniform prior
					 // REVIEW NO, weight the test prior according to the training distribution, see below
					 //priors.put(cluster, 1);
					 }
				 priors.increment(cluster, 1);
				 cluster.add(point);
				 //		if(cluster.getLabelCounts().uniqueSet().size() != 1)
				 //{
				 //throw new Error();
				 //}
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
 */

	/**
	 * Unlike situations where we make a cluster per label, here we make a whole "cluster" per test sample
	 */
	@Override
	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException
		{
		theClusters = new HashSet<CentroidCluster<T>>();
		//	Map<String, CentroidCluster<T>> theClusterMap = new HashMap<String, CentroidCluster<T>>();

		/*		if(decompositionDistanceThreshold != 0)
			  {
			  throw new NotImplementedException("Sub-clustering of k-NN training samples is not supported yet, use decompositionDistanceThreshold = 0 ");
			  }
  */
		try
			{
			// consume the entire iterator, ignoring initsamples
			int i = 0;
			int sampleCount = 0;
			while (trainingIterator.hasNext())
				{
				if (sampleCount % 1000 == 0)
					{
					logger.info("Processed " + sampleCount + " training samples.");
					}
				sampleCount++;

				T point = trainingIterator.next();

				// generate one cluster per exclusive label.


				CentroidCluster<T> cluster = new BasicCentroidCluster<T>(i++, point);//measure

				//** for now we make a uniform prior
				priors.put(cluster, 1);
				theClusters.add(cluster);
				}


			logger.info("Done processing " + sampleCount + " training samples.");

			priors.normalize();
			}
		catch (DistributionException e)
			{
			throw new Error(e);
			}
		//	theClusters = theClusterMap.values();
		}


	/**
	 * Returns a map from distance to cluster, sorted by distance; includes only those clusters with distances better than
	 * the unknown threshold.
	 *
	 * @param p
	 * @return
	 */
	public SortedMap<Double, ClusterMove<T, CentroidCluster<T>>> scoredClusterMoves(T p) throws NoGoodClusterException
		{

		SortedMap<Double, ClusterMove<T, CentroidCluster<T>>> result =
				new TreeMap<Double, ClusterMove<T, CentroidCluster<T>>>();

		// collect moves for all clusters, sorted by distance

		for (CentroidCluster<T> cluster : theClusters)
			{

			try
				{
				// ** careful: how to deal with priors depends on the distance measure.
				// if it's probability, multiply; if log probability, add the log; for other distance types, who knows?

				double distance;

				if (measure instanceof ProbabilisticDissimilarityMeasure)
					{
					distance = ((ProbabilisticDissimilarityMeasure) measure)
							.distanceFromTo(p, cluster.getCentroid(), priors.get(cluster));
					}
				else
					{
					distance = measure.distanceFromTo(p, cluster.getCentroid());
					}


				ClusterMove<T, CentroidCluster<T>> cm = new ClusterMove<T, CentroidCluster<T>>();
				cm.bestCluster = cluster;
				cm.bestDistance = distance;

				// ignore the secondBestDistance, we don't need it here

				result.put(distance, cm);
				}
			catch (DistributionException e)
				{
				throw new ClusterRuntimeException(e);
				}
			}

		result = result.headMap(unknownDistanceThreshold);

		if (result.isEmpty())
			{
			throw new NoGoodClusterException("No clusters passed the unknown threshold");
			}

		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		throw new ClusterRuntimeException(
				"It doesn't make sense to get the best clustermove with k-NN; look for the best label instead");
		}


	/**
	 * Evaluates the classification accuracy of this clustering using an iterator of test samples.  These samples should
	 * not have been used in learning the cluster positions.  Determines what proportions of the test samples are
	 * classified correctly, incorrectly, or not at all.
	 *
	 * @param theTestIterator         an Iterator of test samples.
	 * @param mutuallyExclusiveLabels a Set of labels that we're trying to classify
	 * @param intraLabelDistances     a measure of how different the labels are from each other.  For simply determining
	 *                                whether the classification is correct or wrong, use a delta function (i.e. equals).
	 *                                Sometimes, however, one label may be more wrong than another; this allows us to track
	 *                                that.
	 * @return a TestResults object encapsulating the proportions of test samples classified correctly, incorrectly, or not
	 *         at all.
	 * @throws edu.berkeley.compbio.ml.cluster.NoGoodClusterException
	 *          when a test sample cannot be assigned to any cluster
	 * @throws com.davidsoergel.stats.DistributionException
	 *          when something goes wrong in computing the label probabilities
	 * @throwz ClusterException when something goes wrong in the bowels of the clustering implementation
	 */
	public TestResults test(Iterator<T> theTestIterator, Set<String> mutuallyExclusiveLabels,
	                        DissimilarityMeasure<String> intraLabelDistances) throws // NoGoodClusterException,
			DistributionException, ClusterException
		{		// evaluate labeling correctness using the test samples

		//	List<Double> secondBestDistances = new ArrayList<Double>();
		TestResults tr = new TestResults();

		tr.numClusters = theClusters.size();

		boolean computedDistancesInteresting = false;
		boolean clusterProbabilitiesInteresting = false;

		// Figure out which of the mutually exclusive labels actually had training bins (some got tossed to provide for unknown test samples)
		Set<String> trainingLabels = new HashSet<String>();
		for (CentroidCluster<T> theCluster : theClusters)
			{
			trainingLabels.add(theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(mutuallyExclusiveLabels));
			}


		// classify the test samples
		int i = 0;
		while (theTestIterator.hasNext())
			{
			T frag = theTestIterator.next();
			String fragDominantLabel = frag.getWeightedLabels().getDominantKeyInSet(mutuallyExclusiveLabels);

			try
				{
				SortedMap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(frag);

				WeightedSet<String> labelVotes = new HashWeightedSet<String>()
						;  // use WeightedSet instead of MultiSet so we can aggregate label probabilities

				// keep track of clusters per label, for the sake of
				// tracking computed distances to the clusters contributing to each label
				final Map<String, WeightedSet<ClusterMove<T, CentroidCluster<T>>>> labelContributions =
						new HashMap<String, WeightedSet<ClusterMove<T, CentroidCluster<T>>>>();

				// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
				int neighborsCounted = 0;
				for (ClusterMove<T, CentroidCluster<T>> cm : moves
						.values()) // these should be in order of increasing distance
					{
					if (neighborsCounted >= maxNeighbors)
						{
						break;
						}

					WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();

					labelVotes.addAll(labelsOnThisCluster);

					for (Map.Entry<String, Double> entry : labelsOnThisCluster.getNormalizedMap().entrySet())
						{
						final String label = entry.getKey();
						final Double labelProbability = entry.getValue();

						WeightedSet<ClusterMove<T, CentroidCluster<T>>> contributionSet = labelContributions.get(label);
						if (contributionSet == null)
							{
							contributionSet = new HashWeightedSet<ClusterMove<T, CentroidCluster<T>>>();
							labelContributions.put(label, contributionSet);
							}
						contributionSet.add(cm, labelProbability);
						}

					neighborsCounted++;
					}


				// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

				// now pick the best one

				// primary sort the labels by votes, secondary by weighted distance
				// even if there is a unique label with the most votes, the second place one may still matter depending on the unknown thresholds

				Comparator weightedDistanceSort = new Comparator()
				{
				Map<String, Double> cache = new HashMap<String, Double>();

				private Double getWeightedDistance(String label)
					{
					Double result = cache.get(label);
					if (result == null)
						{
						result = computeWeightedDistance(labelContributions.get(label));
						cache.put(label, result);
						}
					return result;
					}

				public int compare(Object o1, Object o2)
					{
					return Double.compare(getWeightedDistance((String) o1), getWeightedDistance((String) o2));
					}
				};

				Iterator<String> vi = labelVotes.keysInDecreasingWeightOrder(weightedDistanceSort).iterator();

				String bestLabel = vi.next();
				double bestWeightedDistance = computeWeightedDistance(labelContributions.get(bestLabel));
				// too bad we can't esily ccess the cache.  Needs more thorough refactoring.
				// weightedDistanceSort.getWeightedDistance(bestLabel);


				// check that there's not a (near) tie
				if (labelVotes.keySet().size() > 1)
					{
					String secondBestLabel = vi.next();

					double bestVotes = labelVotes.get(bestLabel);
					double secondBestVotes = labelVotes.get(secondBestLabel);
					assert secondBestVotes <= bestVotes;

					double secondBestWeightedDistance = computeWeightedDistance(labelContributions.get(secondBestLabel))
							;

					// if the top two votes are too similar...
					if (secondBestVotes / bestVotes >= voteTieThresholdRatio)
						{
						//... try to break the tie using the distances

						double minRatio = distanceTieThresholdRatio;
						double maxRatio = 1. / distanceTieThresholdRatio;

						final double distanceRatio = secondBestWeightedDistance / bestWeightedDistance;
						if (distanceRatio < minRatio || distanceRatio > maxRatio)
							{
							// indistinguishable tie, call it unknown
							throw new NoGoodClusterException();
							}


						if (bestWeightedDistance > secondBestWeightedDistance)
							{
							// OK, leave the current bestLabel intact then
							}
						else
							{
							bestLabel = secondBestLabel;
							bestWeightedDistance = secondBestWeightedDistance;
							}
						}
					}
				//	String dominantExclusiveLabel = labelVotes.getDominantKeyInSet(mutuallyExclusiveLabels);

				double labelProb = labelVotes.getNormalized(bestLabel);

				if (labelProb < voteProportionThreshold)
					{
					throw new NoGoodClusterException();
					}


				// the fragment's best label does not match any training label, it should be unknown
				if (!trainingLabels.contains(fragDominantLabel))
					{
					tr.shouldHaveBeenUnknown++;
					}


				// if the fragment's best label from the same exclusive set is the same one, that's a match.
				// instead of binary classification, measure how bad the miss is (0 for perfect match)
				double wrongness = intraLabelDistances.distanceFromTo(fragDominantLabel, bestLabel);

				if (Double.isNaN(wrongness))
					{
					logger.error("Wrongness NaN");
					}
				if (Double.isInfinite(wrongness))
					{
					logger.error("Infinite Wrongness");
					}


				tr.computedDistances.add(bestWeightedDistance);
				tr.clusterProbabilities.add(labelProb);
				tr.labelDistances.add(wrongness);
				logger.debug("Label distance wrongness = " + wrongness);

				if (bestWeightedDistance < Double.MAX_VALUE)
					{
					computedDistancesInteresting = true;
					}
				if (labelProb != 1)
					{
					clusterProbabilitiesInteresting = true;
					}

				/*		if (fragDominantLabel.equals(dominantExclusiveLabel))
				   {
				   tr.correctProbabilities.add(clusterProb);
				   tr.correctDistances.add(cm.bestDistance);
				   }
			   else
				   {
				   tr.wrongProbabilities.add(clusterProb);
				   tr.wrongDistances.add(cm.bestDistance);
				   }*/
				}
			catch (NoGoodClusterException e)
				{
				tr.unknown++;

				// the fragment's best label does match a training label, it should not be unknown
				if (trainingLabels.contains(fragDominantLabel))
					{
					tr.shouldNotHaveBeenUnknown++;
					}
				}
			if (i % 100 == 0)
				{
				logger.info("Tested " + i + " samples.");
				}
			i++;
			}
		if (!clusterProbabilitiesInteresting)
			{
			tr.clusterProbabilities = null;
			}
		if (!computedDistancesInteresting)
			{
			tr.computedDistances = null;
			}
		tr.testSamples = i;
		tr.finish();
		logger.info("Tested " + i + " samples.");		//	return i;
		return tr;
		}


	private double computeWeightedDistance(WeightedSet<ClusterMove<T, CentroidCluster<T>>> dominantLabelContributions)
		{

		// compute weighted average computed distance to clusters contributing to this label
		double weightedComputedDistance = 0;


		for (Map.Entry<ClusterMove<T, CentroidCluster<T>>, Double> entry : dominantLabelContributions.getNormalizedMap()
				.entrySet())
			{
			ClusterMove<T, CentroidCluster<T>> contributingCm = entry.getKey();
			Double contributionWeight = entry.getValue();
			weightedComputedDistance += contributionWeight * contributingCm.bestDistance;
			}
		return weightedComputedDistance;
		}
	}