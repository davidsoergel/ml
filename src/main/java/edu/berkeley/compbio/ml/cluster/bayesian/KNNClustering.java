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
 * k-Nearest Neighbor classifier.  This makes sense only when multiple clusters have the same label.  In that case we
 * can look at the nearest k clusters, and vote among the labels (or even report the whole distribution).  Since this
 * corner of the code doesn't know about labels, we'll just return the top k clusters and let the label-voting happen
 * elsewhere.
 *
 * @author David Soergel
 * @version $Id$
 */
public class KNNClustering<T extends AdditiveClusterable<T>>
		extends MultiNeighborClustering<T> //OnlineClusteringMethod<T, CentroidCluster<T>>
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
	public KNNClustering(Set<String> potentialTrainingBins, DissimilarityMeasure<T> dm, double unknownDistanceThreshold,
	                     Set<String> leaveOneOutLabels, int maxNeighbors, double voteProportionThreshold,
	                     double voteTieThresholdRatio,
	                     double distanceTieThresholdRatio) //, double decompositionDistanceThreshold)
		{
		super(potentialTrainingBins, dm, unknownDistanceThreshold, leaveOneOutLabels);
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
	 * Evaluates the classification accuracy of this clustering using an iterator of test samples.  These samples should
	 * not have been used in learning the cluster positions.  Determines what proportions of the test samples are
	 * classified correctly, incorrectly, or not at all.
	 *
	 * @param theTestIterator     an Iterator of test samples. //@param mutuallyExclusiveLabels a Set of labels that we're
	 *                            trying to classify
	 * @param intraLabelDistances a measure of how different the labels are from each other.  For simply determining
	 *                            whether the classification is correct or wrong, use a delta function (i.e. equals).
	 *                            Sometimes, however, one label may be more wrong than another; this allows us to track
	 *                            that.
	 * @return a TestResults object encapsulating the proportions of test samples classified correctly, incorrectly, or not
	 *         at all.
	 * @throws edu.berkeley.compbio.ml.cluster.NoGoodClusterException
	 *                          when a test sample cannot be assigned to any cluster
	 * @throws com.davidsoergel.stats.DistributionException
	 *                          when something goes wrong in computing the label probabilities
	 * @throws ClusterException when something goes wrong in the bowels of the clustering implementation
	 */
	public TestResults test(Iterator<T> theTestIterator,// Set<String> mutuallyExclusiveLabels,
	                        DissimilarityMeasure<String> intraLabelDistances) throws // NoGoodClusterException,
			DistributionException, ClusterException
		{
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
			final String label = theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(this.trainingLabels);
			trainingLabels.add(label);
			tr.totalTrainingMass += theCluster.getWeightedLabels().getWeightSum();
			}


		// classify the test samples
		int i = 0;
		while (theTestIterator.hasNext())
			{
			T frag = theTestIterator.next();
			//String fragDominantLabel = frag.getWeightedLabels().getDominantKeyInSet(this.trainingLabels);


			double voteProportion = 0;
			double secondToBestVoteRatio;
			double secondToBestDistanceRatio;
			double wrongness;
			double bestWeightedDistance;
			try
				{
				TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(frag);

				// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
				final VotingResults votingResults = addUpNeighborVotes(moves);

				// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

				// now pick the best one
				String bestLabel = votingResults.getBestLabel();
				bestWeightedDistance = votingResults.computeWeightedDistance(bestLabel);

				voteProportion = votingResults.getProb(bestLabel);

				// check that there's not a (near) tie
				if (votingResults.hasSecondBestLabel())
					{
					String secondBestLabel = votingResults.getSecondBestLabel();

					double bestVotes = votingResults.getVotes(bestLabel);
					double secondBestVotes = votingResults.getVotes(secondBestLabel);
					assert secondBestVotes <= bestVotes;

					double secondBestWeightedDistance = votingResults.computeWeightedDistance(secondBestLabel);

					// if the top two votes are too similar...
					secondToBestVoteRatio = secondBestVotes / bestVotes;
					secondToBestDistanceRatio = secondBestWeightedDistance / bestWeightedDistance;

					if (secondToBestVoteRatio >= voteTieThresholdRatio)
						{
						//... try to break the tie using the distances

						// ** is this right?  Why consider the inverse ratio too?
						double minRatio = distanceTieThresholdRatio;
						double maxRatio = 1. / distanceTieThresholdRatio;

						if (secondToBestDistanceRatio < minRatio || secondToBestDistanceRatio > maxRatio)
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
				else
					{
					secondToBestVoteRatio = Double.MAX_VALUE;  // infinity really, but that causes problems
					secondToBestDistanceRatio = Double.MAX_VALUE;  // infinity really, but that causes problems
					}
				//	String dominantExclusiveLabel = labelVotes.getDominantKeyInSet(mutuallyExclusiveLabels);


				//** note we usually want this not to kick in so we can plot vs. the threshold in Jandy
				if (voteProportion < voteProportionThreshold)
					{
					throw new NoGoodClusterException();
					}
				else
					{

					// the fragment's best label does not match any training label, it should be unknown
					/*		if (!trainingLabels.contains(fragDominantLabel))
							 {
							 tr.shouldHaveBeenUnknown++;
							 }
	 */

					// if the fragment's best label from the same exclusive set is the same one, that's a match.
					// instead of binary classification, measure how bad the miss is (0 for perfect match)

					wrongness = intraLabelDistances
							.distanceFromTo(frag.getWeightedLabels().getDominantKeyInSet(this.trainingLabels),
							                bestLabel);
					if (Double.isNaN(wrongness))
						{
						logger.error("Wrongness NaN");
						}

					if (Double.isInfinite(wrongness))
						{
						logger.error("Infinite Wrongness");
						}


					logger.debug("Label distance wrongness = " + wrongness);

					if (bestWeightedDistance < Double.MAX_VALUE)
						{
						computedDistancesInteresting = true;
						}
					if (voteProportion != 1)
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
				}
			catch (NoGoodClusterException e)
				{
				wrongness = UNKNOWN_DISTANCE;
				bestWeightedDistance = UNKNOWN_DISTANCE;
				secondToBestDistanceRatio = UNKNOWN_DISTANCE;
				secondToBestVoteRatio = UNKNOWN_DISTANCE;

				tr.unknown++;

				// the fragment's best label does match a training label, it should not be unknown
				/*	if (trainingLabels.contains(fragDominantLabel))
					 {
					 tr.shouldNotHaveBeenUnknown++;
					 }
			 */
				}

			tr.labelDistances.add(wrongness);
			tr.computedDistances.add(bestWeightedDistance);
			tr.secondToBestDistanceRatios.add(secondToBestDistanceRatio);
			tr.secondToBestVoteRatios.add(secondToBestVoteRatio);
			tr.labelWithinClusterProbabilities.add(voteProportion);

			if (i % 100 == 0)
				{
				logger.debug("Tested " + i + " samples.");
				}
			i++;
			}
		if (!clusterProbabilitiesInteresting)
			{
			tr.labelWithinClusterProbabilities = null;
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

	private VotingResults addUpNeighborVotes(TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves)
		{
		VotingResults result = new VotingResults();

		int neighborsCounted = 0;
		for (ClusterMove<T, CentroidCluster<T>> cm : moves.values()) // these should be in order of increasing distance
			{
			if (neighborsCounted >= maxNeighbors)
				{
				break;
				}

			WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();

			result.addVotes(labelsOnThisCluster);

			for (Map.Entry<String, Double> entry : labelsOnThisCluster.getItemNormalizedMap().entrySet())
				{
				final String label = entry.getKey();
				final Double labelProbability = entry.getValue();

				result.addContribution(cm, label, labelProbability);
				}

			neighborsCounted++;
			}
		result.finish();
		return result;
		}
	}