/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;
import com.davidsoergel.dsutils.collections.ConcurrentHashWeightedSet;
import com.davidsoergel.dsutils.collections.MutableWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.ProbabilisticDissimilarityMeasure;
import com.google.common.collect.TreeMultimap;
import edu.berkeley.compbio.ml.cluster.AbstractSupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.BasicCentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.PointClusterFilter;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Note we use CentroidClusters internally, but th e centroids are in fact just the training samples, not new
 * prototype-based centroids
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class MultiNeighborClustering<T extends AdditiveClusterable<T>>
		extends AbstractSupervisedOnlineClusteringMethod<T, CentroidCluster<T>>
		//implements SampleInitializedOnlineClusteringMethod<T>
		//, CentroidClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MultiNeighborClustering.class);
	protected final int maxNeighbors;

	protected final double unknownDistanceThreshold;


// --------------------------- CONSTRUCTORS ---------------------------

	public MultiNeighborClustering(final DissimilarityMeasure<T> dm, final double unknownDistanceThreshold,
	                               final Set<String> potentialTrainingBins,
	                               final Map<String, Set<String>> predictLabelSets,
	                               final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels,
	                               final int maxNeighbors)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		this.maxNeighbors = maxNeighbors;
		this.unknownDistanceThreshold = unknownDistanceThreshold;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ClusteringMethod ---------------------

	public String bestLabel(final T sample, final Set<String> predictLabels) throws NoGoodClusterException
		{
		final TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves = scoredClusterMoves(sample);

		// consider up to maxNeighbors neighbors.  If fewer neighbors than that passed the unknown threshold, so be it.
		final VotingResults votingResults = addUpNeighborVotes(moves);

		// note the "votes" from each cluster may be fractional (probabilities) but we just summed them all up.

		// now pick the best one

		return votingResults.getSubResults(predictLabels).getBestLabel();
		}

// --------------------- Interface OnlineClusteringMethod ---------------------


	/**
	 * {@inheritDoc}
	 */
/*	public boolean add(T p) throws ClusterException, NoGoodClusterException //, List<Double> secondBestDistances
		{
		ClusterMove best = bestClusterMove(p);
		//secondBestDistances.add(best.secondBestDistance);
		best.bestCluster.add(p);
		return true;
		}
*/

// --------------------- Interface SampleInitializedOnlineClusteringMethod ---------------------


	/**
	 * Unlike situations where we make a cluster per label, here we make a whole "cluster" per training sample
	 */
	//@Override
	public void trainWithKnownTrainingLabels(final ClusterableIterator<T> trainingIterator)
		//,                             GenericFactory<T> prototypeFactory)
//			throws GenericFactoryException, ClusterException
		{
		//theClusters = new HashSet<CentroidCluster<T>>();

		//	ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();

		final AtomicInteger i = new AtomicInteger(0);

		Parallel.forEach(trainingIterator, new Function<T, Void>()
		{
		public Void apply(@Nullable final T point)
			{
			final int clusterId = i.incrementAndGet();
			//		execService.submit(new Runnable()
			//		{
			//		public void run()
			//			{

			// generate one "cluster" per training sample.
			final CentroidCluster<T> cluster = new BasicCentroidCluster<T>(clusterId, point);//measure
			addCluster(cluster);

			if (clusterId % 1000 == 0)
				{
				logger.info("Trained " + clusterId + " samples");
				}

			return null;
			}
		});
		doneLabellingClusters();
		logger.info("Done training " + getNumClusters() + " samples");
		}


// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
/*	@Override
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
		{
		//initializeWithRealData(trainingCollectionIteratorFactory.next());

		// after that, normalize the label probabilities

		ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();
		for (final Cluster c : theClusters)
			{
			execService.submit(new Runnable()
			{
			public void run()
				{
				c.updateDerivedWeightedLabelsFromLocal();
				}
			});
			}
		execService.finish("Normalized %d training probabilities", 30);
		}*/
	protected VotingResults addUpNeighborVotes(
			final TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> moves)//,  Set<String> populatedTrainingLabels)
		{
		final VotingResults result = new VotingResults();

		int neighborsCounted = 0;
		double lastDistance = 0.0;
		for (final ClusterMove<T, CentroidCluster<T>> cm : moves.values())
			{
			if (neighborsCounted >= maxNeighbors)
				{
				break;
				}

			// the moves must be sorted in order of increasing distance
			assert cm.bestDistance >= lastDistance;
			lastDistance = cm.bestDistance;

			final WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();

			result.addVotes(labelsOnThisCluster, cm.voteWeight);

			for (final Map.Entry<String, Double> entry : labelsOnThisCluster.getItemNormalizedMap().entrySet())
				{
				final String label = entry.getKey();
				final Double labelProbability = entry.getValue();

				result.addContribution(cm, label, labelProbability);
				}

			neighborsCounted++;
			}
		//result.finish(populatedTrainingLabels);
		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(final T p) throws NoGoodClusterException
		{
		throw new ClusterRuntimeException(
				//logger.warn(
				"It doesn't make sense to get the best clustermove with a multi-neighbor clustering; look for the best label instead using scoredClusterMoves");
		//return scoredClusterMoves(p).values().iterator().next();
		}

	/**
	 * Returns a map from distance to cluster, sorted by distance; includes only those clusters with distances better than
	 * the unknown threshold.
	 *
	 * @param p
	 * @return
	 */
	protected TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> scoredClusterMoves(final T p)
			throws NoGoodClusterException
		{
		final TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> result =
				TreeMultimap.create(); //<Double, ClusterMove<T, CentroidCluster<T>>>();


		//ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();

		PointClusterFilter<T> clusterFilter = prohibitionModel == null ? null : prohibitionModel.getFilter(p);
		for (final CentroidCluster<T> cluster : getClusters())
			{
			if (clusterFilter != null && clusterFilter.isProhibited(cluster))
				{
				// ignore this cluster
				}
			else
				{
				//	execService.submit(new Runnable()
				//	{
				//	public void run()
				//		{
				// Note that different distance measures may need to deal with the priors differently:
				// if it's probability, multiply; if log probability, add; for other distance types, who knows?
				// so, just pass the priors in and let the distance measure decide what to do with them

				final double distance;

				if (measure instanceof ProbabilisticDissimilarityMeasure)
					{
					distance = ((ProbabilisticDissimilarityMeasure) measure)
							.distanceFromTo(p, cluster.getCentroid(), clusterPriors.get(cluster));
					}
				else
					{
					distance = measure.distanceFromTo(p, cluster.getCentroid());
					}

				final ClusterMove<T, CentroidCluster<T>> cm = makeClusterMove(cluster, distance);

				// ignore the secondBestDistance, we don't need it here

				//** note we usually want this not to kick in so we can plot vs. the threshold in Jandy
				if (cm.bestDistance < unknownDistanceThreshold)
					{
					result.put(cm.bestDistance, cm);
					}
				//	}
				//	});
				}
			}

		//	execService.finish("Tested sample against %d clusters", 30);

		//result = result.headMap(unknownDistanceThreshold);

		if (result.isEmpty())
			{
			throw new NoGoodClusterException("No clusters passed the unknown threshold");
			}

		return result;
		}

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
		return cm;
		}

// -------------------------- INNER CLASSES --------------------------

	protected class VotingResults
		{
// ------------------------------ FIELDS ------------------------------

		// use WeightedSet instead of MultiSet so we can aggregate label probabilities
		// keep track of clusters per label, for the sake of tracking computed distances to the clusters contributing to each label
		private final Map<String, MutableWeightedSet<ClusterMove<T, CentroidCluster<T>>>> labelContributions =
				new HashMap<String, MutableWeightedSet<ClusterMove<T, CentroidCluster<T>>>>();

		//	private String bestLabel;
		//	private String secondBestLabel;

		private final MutableWeightedSet<String> labelVotes = new ConcurrentHashWeightedSet<String>();


// --------------------- GETTER / SETTER METHODS ---------------------

		/*	public String getBestLabel()
			  {
			  return bestLabel;
			  }

		  public String getSecondBestLabel()
			  {
			  return secondBestLabel;
			  }
  */
// -------------------------- OTHER METHODS --------------------------

		public void addContribution(final ClusterMove<T, CentroidCluster<T>> cm, final String label,
		                            final Double labelProbability)
			{
			MutableWeightedSet<ClusterMove<T, CentroidCluster<T>>> contributionSet = labelContributions.get(label);
			if (contributionSet == null)
				{
				contributionSet = new ConcurrentHashWeightedSet<ClusterMove<T, CentroidCluster<T>>>();
				labelContributions.put(label, contributionSet);
				}
			contributionSet.add(cm, labelProbability, 1);
			//contributionSet.incrementItems();
			}

		public void addVotes(final WeightedSet<String> labelsOnThisCluster)
			{
			labelVotes.addAll(labelsOnThisCluster);
			}

		public void addVotes(final WeightedSet<String> labelsOnThisCluster, final double multiplier)
			{
			labelVotes.addAll(labelsOnThisCluster, multiplier);
			}

		/*public WeightedSet<ClusterMove<T, CentroidCluster<T>>> getContributions(String label)
			{
			return labelContributions.get(label);
			}*/

		public double computeWeightedDistance(final String label)
			{
			return computeWeightedDistance(labelContributions.get(label));
			}

		public BestLabelPair getSubResults(final Set<String> populatedTrainingLabels) throws NoGoodClusterException
			{
			// primary sort the labels by votes, secondary by weighted distance
			// even if there is a unique label with the most votes, the second place one may still matter depending on the unknown thresholds

			final Comparator weightedDistanceSort = new Comparator()
			{
			final Map<String, Double> cache = new HashMap<String, Double>();

			private Double getWeightedDistance(final String label)
				{
				Double result = cache.get(label);
				if (result == null)
					{
					result = computeWeightedDistance(labelContributions.get(label));
					cache.put(label, result);
					}
				return result;
				}

			public int compare(final Object o1, final Object o2)
				{
				return Double.compare(getWeightedDistance((String) o1), getWeightedDistance((String) o2));
				}
			};

			final WeightedSet<String> subVotes = labelVotes.extractWithKeys(populatedTrainingLabels);

			//subVotes.retainKeys(populatedTrainingLabels);
			final Iterator<String> vi = subVotes.keysInDecreasingWeightOrder(weightedDistanceSort).iterator();
			final String bestLabel;

			try
				{
				bestLabel = vi.next();
				}
			catch (NoSuchElementException e)
				{
				// this can happen with Gaussian KNN, for example, when all vote weights are zero
				throw new NoGoodClusterException();
				}

			String secondBestLabel = null;
			try
				{
				secondBestLabel = vi.next();
				}
			catch (NoSuchElementException e)
				{
				//no problem
				}

			return new BestLabelPair(bestLabel, secondBestLabel);
			}

		/**
		 * compute weighted average computed distance to clusters contributing to this label
		 */
		private double computeWeightedDistance(
				final WeightedSet<ClusterMove<T, CentroidCluster<T>>> dominantLabelContributions)
			{
			double weightedComputedDistance = 0;

			for (final Map.Entry<ClusterMove<T, CentroidCluster<T>>, Double> entry : dominantLabelContributions
					.getItemNormalizedMap().entrySet())
				{
				final ClusterMove<T, CentroidCluster<T>> contributingCm = entry.getKey();
				final Double contributionWeight = entry.getValue();
				weightedComputedDistance += contributionWeight * contributingCm.bestDistance;
				}
			return weightedComputedDistance;
			}

		/*
		  public double getProb(String bestLabel)
			  {
			  return labelVotes.getNormalized(bestLabel);
			  }

		  public double getVotes(String label)
			  {
			  return labelVotes.get(label);
			  }
  */
/*		public boolean hasSecondBestLabel()
			{
			return secondBestLabel != null;
			}
			*/
/*
		public String getDominantKeyInSet(final Set<String> predictLabels)
			{
			return labelVotes.getDominantKeyInSet(predictLabels);
			}
			*/

		public WeightedSet<String> getLabelVotes()
			{
			return labelVotes;
			}
		}

	public static class BestLabelPair
		{
		final String bestLabel;
		final String secondBestLabel;

		private BestLabelPair(final String bestLabel, final String secondBestLabel)
			{
			this.bestLabel = bestLabel;
			this.secondBestLabel = secondBestLabel;
			}

		public String getBestLabel()
			{
			return bestLabel;
			}

		public String getSecondBestLabel()
			{
			return secondBestLabel;
			}

		public boolean hasSecondBestLabel()
			{
			return secondBestLabel != null;
			}
		}
	}
