package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusteringTestResults;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class TacoaClustering<T extends AdditiveClusterable<T>> extends MultiNeighborClustering<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(TacoaClustering.class);

	private double bestScoreRatioThreshold;


// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * @param dm The distance measure to use
	 */


	public TacoaClustering(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins, Set<String> predictLabels,
	                       Set<String> leaveOneOutLabels, Set<String> testLabels, int maxNeighbors,
	                       double bestScoreRatioThreshold, int testThreads)
		{
		super(dm, Double.POSITIVE_INFINITY, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels,
		      maxNeighbors, testThreads);
		this.bestScoreRatioThreshold = bestScoreRatioThreshold;
		}

// -------------------------- OTHER METHODS --------------------------

	protected Set<String> findPopulatedTrainingLabels(ClusteringTestResults tr) throws DistributionException
		{
		Multiset<String> populatedTrainingLabels = new HashMultiset<String>();
		for (CentroidCluster<T> theCluster : theClusters)
			{
			final String label = theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(this.predictLabels);
			populatedTrainingLabels.add(label);
			tr.incrementTotalTrainingMass(theCluster.getWeightedLabels().getWeightSum());
			}

		// we're going to hack the prior probabilities using the number of clusters per label
		// TacoaDistanceMeasure takes the prior to be per label, not per cluster

		priors = new HashMap<CentroidCluster<T>, Double>();
		Multinomial<String> labelPriors = new Multinomial<String>(populatedTrainingLabels);
		for (CentroidCluster<T> theCluster : theClusters)
			{
			final String label =
					theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(this.predictLabels); // PERF redundant
			priors.put(theCluster, labelPriors.get(label));
			}
		return populatedTrainingLabels.elementSet();
		}

	/**
	 * allow an overriding clustering method to tweak the distances, set vote weights, etc.
	 *
	 * @param cluster
	 * @param distance
	 * @return
	 */
	protected ClusterMove<T, CentroidCluster<T>> makeClusterMove(CentroidCluster<T> cluster, double distance)
		{
		ClusterMove<T, CentroidCluster<T>> cm = new ClusterMove<T, CentroidCluster<T>>();
		cm.bestCluster = cluster;
		cm.voteWeight = distance;

		// ** hack: monotonic positive inversion to a distance-like metric (smaller better)
		cm.bestDistance = 1.0 / distance;
		return cm;
		}

	protected void testOneSample(DissimilarityMeasure<String> intraLabelDistances, ClusteringTestResults tr,
	                             Set<String> populatedTrainingLabels, T frag)
		{
		double voteProportion = 0;
		double bestVotes;
		double secondToBestVoteRatio;
		double broadWrongness;
		double detailedWrongness;

		// note the labels on the test set may be different from the training labels, as long as we can calculate wrongness.
		// This supports a hierarchical classification scenario, where the "detailed" label is a leaf, and the "broad" label is a higher aggregate node.
		// we want to measure wrongness _both_ at the broad level, matching where the prediction is made (so a perfect match is possible),
		// _and_ at the detailed level, where even a perfect broad prediction incurs a cost due to lack of precision.

		String broadActualLabel = frag.getWeightedLabels().getDominantKeyInSet(predictLabels);
		String detailedActualLabel = frag.getWeightedLabels().getDominantKeyInSet(testLabels);

		try
			{
			TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(frag);

			// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
			final VotingResults votingResults = addUpNeighborVotes(moves, populatedTrainingLabels);

			// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

			// now pick the best one
			String predictedLabel = votingResults.getBestLabel();
			bestVotes = votingResults.getVotes(predictedLabel);

			voteProportion = votingResults.getProb(predictedLabel);

			// In TACOA, distance == votes, so we don't deal with them separately

			// check that there's not a (near) tie
			if (votingResults.hasSecondBestLabel())
				{
				String secondBestLabel = votingResults.getSecondBestLabel();

				double secondBestVotes = votingResults.getVotes(secondBestLabel);
				assert secondBestVotes <= bestVotes;

				// if the top two scores are too similar...
				secondToBestVoteRatio = secondBestVotes / bestVotes;
				if (secondToBestVoteRatio > bestScoreRatioThreshold)
					{
					throw new NoGoodClusterException();
					}
				}
			else
				{
				secondToBestVoteRatio =
						1e308; // Double.MAX_VALUE; triggers MySQL bug # 21497  // infinity really, but that causes jdbc problems
				}


			// the fragment's real label does not match any populated training label (to which it might possibly have been classified), it should be unknown
			if (!populatedTrainingLabels.contains(broadActualLabel))
				{
				tr.incrementShouldHaveBeenUnknown();
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
		catch (NoGoodClusterException e)
			{
			broadWrongness = UNKNOWN_DISTANCE;
			detailedWrongness = UNKNOWN_DISTANCE;
			bestVotes = UNKNOWN_DISTANCE;
			secondToBestVoteRatio = UNKNOWN_DISTANCE;

			tr.incrementUnknown();

			// the fragment's best label does match a training label, it should not be unknown
			if (populatedTrainingLabels.contains(broadActualLabel))
				{
				tr.incrementShouldNotHaveBeenUnknown();
				}
			}

		// In TACOA, distance == inverse of votes, so we don't really need to record them separately
		// ** hack: monotonic positive inversion to a distance-like metric (smaller better)
		double bestDistance = 1.0 / bestVotes;
		double secondToBestDistanceRatio = 1.0 / secondToBestVoteRatio;

		tr.addResult(broadWrongness, detailedWrongness, bestDistance, secondToBestDistanceRatio, 0, voteProportion,
		             secondToBestVoteRatio);
		}

/*
	private VotingResults addUpNeighborVotes(TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves, Set<String> populatedTrainingLabels)
		{
		VotingResults result = new VotingResults();

		for (ClusterMove<T, CentroidCluster<T>> cm : moves.values())
			{
			WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();

			// in the usual case, labelsOnThisCluster contains a single label with weight 1.
			// but it might have weights for all the ancestors too

			// we actually want the vote to count in proportion to the computed "distance", which is really a score (bigger better):
			result.addVotes(labelsOnThisCluster, cm.bestDistance);

			//** dunno if this makes any sense here... OK, it allows computing weighted distances per label later
			for (Map.Entry<String, Double> entry : labelsOnThisCluster.getItemNormalizedMap().entrySet())
				{
				final String label = entry.getKey();
				final Double labelProbability = entry.getValue();

				result.addContribution(cm, label, labelProbability);
				}
			}
		result.finish(populatedTrainingLabels);
		return result;
		}*/
	}
