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

import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.SimpleFunction;
import com.google.common.collect.TreeMultimap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusteringTestResults;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.Map;
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

	private final double voteProportionThreshold;
	private final double distanceTieThresholdRatio;
	private final double voteTieThresholdRatio;
	private SimpleFunction function = null;


// --------------------------- CONSTRUCTORS ---------------------------

	//	private double decompositionDistanceThreshold;

	/**
	 * @param dm                       The distance measure to use
	 * @param unknownDistanceThreshold the minimum probability to accept when adding a point to a cluster
	 */
	public KNNClustering(final DissimilarityMeasure<T> dm, final double unknownDistanceThreshold,
	                     final Set<String> potentialTrainingBins, final Map<String, Set<String>> predictLabelSets,
	                     final Set<String> leaveOneOutLabels, final Set<String> testLabels, final int maxNeighbors,
	                     final double voteProportionThreshold, final double voteTieThresholdRatio,
	                     final double distanceTieThresholdRatio,
	                     final SimpleFunction function) //, double decompositionDistanceThreshold)
		{
		//	super(potentialTrainingBins, dm, unknownDistanceThreshold, leaveOneOutLabels, maxNeighbors);
		super(dm, unknownDistanceThreshold, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels,
		      maxNeighbors);

		//		this.decompositionDistanceThreshold = decompositionDistanceThreshold;
		this.voteProportionThreshold = voteProportionThreshold;
		this.voteTieThresholdRatio = voteTieThresholdRatio;
		this.distanceTieThresholdRatio = distanceTieThresholdRatio;
		this.function = function;
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * allow an overriding clustering method to tweak the distances, set vote weights, etc.
	 *
	 * @param cluster
	 * @param distance
	 * @return
	 */
	protected ClusterMove<T, CentroidCluster<T>> makeClusterMove(final CentroidCluster<T> cluster,
	                                                             final double distance)
		{
		final ClusterMove<T, CentroidCluster<T>> cm = new ClusterMove<T, CentroidCluster<T>>();
		cm.bestCluster = cluster;
		cm.bestDistance = distance;
		if (function != null)
			{
			cm.voteWeight = function.f(distance);
			}
		return cm;
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

/*	protected void testOneSample(DissimilarityMeasure<String> intraLabelDistances, ClusteringTestResults tr,
							   final Map<String, Set<String>> populatedPredictLabelSets, T frag)
	  {
	  WeightedSet<String> predictedLabelWeights = predictLabelWeights(tr, frag);
	  testAgainstPredictionLabels(intraLabelDistances, tr, populatedPredictLabelSets, frag, predictedLabelWeights);
	  }*/
	protected WeightedSet<String> predictLabelWeights(final ClusteringTestResults tr, final T frag)
		//                    Set<String> populatedTrainingLabels)
		{
		//double secondToBestDistanceRatio = 0;

		//double bestDistance;
		//double bestVoteProportion;
		double secondToBestVoteRatio = 0;
		double secondToBestDistanceRatio;

		double voteProportion = 0;
		double bestWeightedDistance;
		//	double bestVotes = 0;

		WeightedSet<String> labelWeights = null;
		//VotingResults votingResults = null;

		//	boolean unknown = false;
		try
			{
			// make the prediction
			final TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(frag);

			// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
			final VotingResults votingResults = addUpNeighborVotes(moves); //, populatedTrainingLabels);
			labelWeights = votingResults.getLabelVotes();

			final BestLabelPair votingWinners = votingResults.getSubResults(potentialTrainingBins);

			// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

			// now pick the best one
			String predictedLabel = votingWinners.getBestLabel();
			bestWeightedDistance = votingResults.computeWeightedDistance(predictedLabel);

			// check that there's not a (near) tie
			if (votingWinners.hasSecondBestLabel())
				{
				final String secondBestLabel = votingWinners.getSecondBestLabel();

				final double bestVotes = labelWeights.get(predictedLabel);
				final double secondBestVotes = labelWeights.get(secondBestLabel);
				assert secondBestVotes <= bestVotes;

				final double secondBestWeightedDistance = votingResults.computeWeightedDistance(secondBestLabel);

				// if the top two votes are too similar...
				secondToBestVoteRatio = secondBestVotes / bestVotes;
				secondToBestDistanceRatio = secondBestWeightedDistance / bestWeightedDistance;

				if (secondToBestVoteRatio >= voteTieThresholdRatio)
					{
					//... try to break the tie using the distances.
					// we don't know whether the "second-best" or the "best" distance is actually better,
					// so we first check for a tie using both the threshold and its inverse.

					final double minRatio = distanceTieThresholdRatio;
					final double maxRatio = 1. / distanceTieThresholdRatio;

					if (!(secondToBestDistanceRatio < minRatio || secondToBestDistanceRatio > maxRatio))
						{
						// indistinguishable tie, call it unknown
						// ** Would it better to just pick one?
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
				secondToBestDistanceRatio =
						1e308; // Double.MAX_VALUE; triggers MySQL bug # 21497  // infinity really, but that causes jdbc problems
				}


			voteProportion = labelWeights.getNormalized(predictedLabel);

			//** note we usually want this not to kick in so we can plot vs. the threshold in Jandy; set voteProportionThreshold high
			if (voteProportion < voteProportionThreshold)
				{
				throw new NoGoodClusterException();
				}
			}
		catch (NoGoodClusterException e)
			{
			bestWeightedDistance = UNKNOWN_DISTANCE;
			secondToBestDistanceRatio = 1.0;
			voteProportion = 0;
			secondToBestVoteRatio = 1.0;

			tr.incrementUnknown();
			}


		tr.addClusterResult(bestWeightedDistance, secondToBestDistanceRatio, voteProportion, secondToBestVoteRatio);

		return labelWeights;
		}
	}
