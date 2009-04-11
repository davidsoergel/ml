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

import com.davidsoergel.stats.DissimilarityMeasure;
import com.google.common.collect.TreeMultimap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusteringTestResults;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * k-Nearest Neighbor classifier.  This makes sense only when multiple clusters have the same label.  In that case we
 * can look at the nearest k clusters, and vote among the labels (or even report the whole distribution).
 *
 * @author David Soergel
 * @version $Id$
 */
public class KNNClustering<T extends AdditiveClusterable<T>>
		extends MultiNeighborClustering<T> //OnlineClusteringMethod<T, CentroidCluster<T>>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KNNClustering.class);

	private double voteProportionThreshold;
	private double distanceTieThresholdRatio;
	private double voteTieThresholdRatio;
	//	private double decompositionDistanceThreshold;

	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public KNNClustering(Set<String> potentialTrainingBins, DissimilarityMeasure<T> dm, double unknownDistanceThreshold,
	                     Set<String> leaveOneOutLabels, int maxNeighbors, double voteProportionThreshold,
	                     double voteTieThresholdRatio,
	                     double distanceTieThresholdRatio) //, double decompositionDistanceThreshold)
		{
		super(potentialTrainingBins, dm, unknownDistanceThreshold, leaveOneOutLabels, maxNeighbors);
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
	protected void testOneSample(DissimilarityMeasure<String> intraLabelDistances, ClusteringTestResults tr,
	                             Set<String> populatedTrainingLabels, T frag)
		{
		double voteProportion = 0;
		double secondToBestVoteRatio;
		double secondToBestDistanceRatio;
		double broadWrongness;
		double detailedWrongness;
		double bestWeightedDistance;

		// note the labels on the test set may be different from the training labels, as long as we can calculate wrongness.
		// This supports a hierarchical classification scenario, where the "detailed" label is a leaf, and the "broad" label is a higher aggregate node.
		// we want to measure wrongness _both_ at the broad level, matching where the prediction is made (so a perfect match is possible),
		// _and_ at the detailed level, where even a perfect broad prediction incurs a cost due to lack of precision.

		String broadActualLabel = frag.getWeightedLabels().getDominantKeyInSet(trainingLabels);
		String detailedActualLabel = frag.getWeightedLabels().getDominantKeyInSet(testLabels);


		try
			{
			TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(frag);

			// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
			final VotingResults votingResults = addUpNeighborVotes(moves, populatedTrainingLabels);

			// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

			// now pick the best one
			String predictedLabel = votingResults.getBestLabel();
			bestWeightedDistance = votingResults.computeWeightedDistance(predictedLabel);

			voteProportion = votingResults.getProb(predictedLabel);

			// check that there's not a (near) tie
			if (votingResults.hasSecondBestLabel())
				{
				String secondBestLabel = votingResults.getSecondBestLabel();

				double bestVotes = votingResults.getVotes(predictedLabel);
				double secondBestVotes = votingResults.getVotes(secondBestLabel);
				assert secondBestVotes <= bestVotes;

				double secondBestWeightedDistance = votingResults.computeWeightedDistance(secondBestLabel);

				// if the top two votes are too similar...
				secondToBestVoteRatio = secondBestVotes / bestVotes;
				secondToBestDistanceRatio = secondBestWeightedDistance / bestWeightedDistance;

				if (secondToBestVoteRatio >= voteTieThresholdRatio)
					{
					//... try to break the tie using the distances.
					// we don't know whether the "second-best" or the "best" distance is actually better,
					// so we first check for a tie using both the threshold and its inverse.

					double minRatio = distanceTieThresholdRatio;
					double maxRatio = 1. / distanceTieThresholdRatio;

					if (!(secondToBestDistanceRatio < minRatio || secondToBestDistanceRatio > maxRatio))
						{
						// indistinguishable tie, call it unknown
						throw new NoGoodClusterException();
						}

					// OK, it's not a distance tie, so pick the closer one

					if (bestWeightedDistance < secondBestWeightedDistance)
						{
						// OK, leave the current bestLabel intact then
						}
					else
						{
						predictedLabel = secondBestLabel;
						bestWeightedDistance = secondBestWeightedDistance;
						}
					}
				}
			else
				{
				secondToBestVoteRatio = 0;
				secondToBestDistanceRatio = Double.MAX_VALUE;  // infinity really, but that causes jdbc problems
				}


			//** note we usually want this not to kick in so we can plot vs. the threshold in Jandy; set voteProportionThreshold high
			if (voteProportion < voteProportionThreshold)
				{
				throw new NoGoodClusterException();
				}
			else
				{

				// the fragment's real label does not match any populated training label (to which it might possibly have been classified), it should be unknown
				if (!populatedTrainingLabels.contains(broadActualLabel))
					{
					tr.shouldHaveBeenUnknown++;
					}

				// compute a measure of how badly the prediction missed the truth, at the broad level
				broadWrongness = intraLabelDistances.distanceFromTo(broadActualLabel, predictedLabel);
				logger.debug("Label distance broad wrongness = " + broadWrongness);

				if (Double.isNaN(broadWrongness) || Double.isInfinite(broadWrongness))
					{
					logger.error("Broad Wrongness = " + broadWrongness);
					}

				// compute a measure of how badly the prediction missed the truth, at the broad level
				detailedWrongness = intraLabelDistances.distanceFromTo(detailedActualLabel, predictedLabel);
				logger.debug("Label distance detailed wrongness = " + detailedWrongness);

				if (Double.isNaN(detailedWrongness) || Double.isInfinite(detailedWrongness))
					{
					logger.error("Detailed Wrongness = " + detailedWrongness);
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
			}
		catch (NoGoodClusterException e)
			{
			broadWrongness = UNKNOWN_DISTANCE;
			detailedWrongness = UNKNOWN_DISTANCE;
			bestWeightedDistance = UNKNOWN_DISTANCE;
			secondToBestDistanceRatio = 1.0;
			secondToBestVoteRatio = 1.0;

			tr.unknown++;

			// the fragment's best label does match a training label, it should not be unknown
			if (populatedTrainingLabels.contains(broadActualLabel))
				{
				tr.shouldNotHaveBeenUnknown++;
				}
			}

		tr.addResult(broadWrongness, detailedWrongness, bestWeightedDistance, secondToBestDistanceRatio, voteProportion,
		             secondToBestVoteRatio);
		}
	}