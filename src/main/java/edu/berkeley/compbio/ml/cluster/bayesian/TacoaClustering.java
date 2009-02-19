package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.google.common.collect.TreeMultimap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class TacoaClustering<T extends AdditiveClusterable<T>> extends MultiNeighborClustering<T>
	{
	private static final Logger logger = Logger.getLogger(TacoaClustering.class);

	private double bestScoreRatioThreshold;

	/**
	 * @param dm The distance measure to use
	 */
	public TacoaClustering(DissimilarityMeasure<T> dm, double bestScoreRatioThreshold)
		{
		// ** should make common superclass
		super(dm, Double.MAX_VALUE);
		this.bestScoreRatioThreshold = bestScoreRatioThreshold;
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
	 *                          when a test sample cannot be assigned to any cluster
	 * @throws com.davidsoergel.stats.DistributionException
	 *                          when something goes wrong in computing the label probabilities
	 * @throws ClusterException when something goes wrong in the bowels of the clustering implementation
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
		// while we're at it, sum up the cluster masses

		Set<String> trainingLabels = new HashSet<String>();
		for (CentroidCluster<T> theCluster : theClusters)
			{
			trainingLabels.add(theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(mutuallyExclusiveLabels));
			tr.totalTrainingMass += theCluster.getWeightedLabels().getWeightSum();
			}


		// classify the test samples
		int i = 0;
		while (theTestIterator.hasNext())
			{
			T frag = theTestIterator.next();
			String fragDominantLabel = frag.getWeightedLabels().getDominantKeyInSet(mutuallyExclusiveLabels);

			try
				{
				TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(frag);

				// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
				final VotingResults v = addUpNeighborVotes(moves);

				// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

				// now pick the best one
				String bestLabel = v.getBestLabel();
				double bestVotes = v.getVotes(bestLabel);

				// check that there's not a (near) tie
				if (v.hasSecondBestLabel())
					{
					String secondBestLabel = v.getSecondBestLabel();

					double secondBestVotes = v.getVotes(secondBestLabel);
					assert secondBestVotes <= bestVotes;

					// if the top two scores are too similar...
					if (bestVotes / secondBestVotes < bestScoreRatioThreshold)
						{
						throw new NoGoodClusterException();
						}
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


				tr.computedDistances.add(bestVotes);
				tr.labelDistances.add(wrongness);
				logger.debug("Label distance wrongness = " + wrongness);


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
				logger.debug("Tested " + i + " samples.");
				}
			i++;
			}
		tr.clusterProbabilities = null;

		tr.testSamples = i;
		tr.finish();
		logger.info("Tested " + i + " samples.");		//	return i;
		return tr;
		}


	private VotingResults addUpNeighborVotes(TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves)
		{
		VotingResults result = new VotingResults();

		for (ClusterMove<T, CentroidCluster<T>> cm : moves.values()) // these should be in order of increasing distance
			{

			WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();

			// in the usual case, labelsOnThisCluster contains a single label with weight 1.

			// we actually want the vote to count in proportion to the computed distance:
			labelsOnThisCluster.multiplyBy(cm.bestDistance);

			result.addVotes(labelsOnThisCluster);

			//** dunno if this makes any sense here
			for (Map.Entry<String, Double> entry : labelsOnThisCluster.getItemNormalizedMap().entrySet())
				{
				final String label = entry.getKey();
				final Double labelProbability = entry.getValue();

				result.addContribution(cm, label, labelProbability);
				}
			}
		result.finish();
		return result;
		}
	}
