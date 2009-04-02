package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import com.davidsoergel.stats.ProbabilisticDissimilarityMeasure;
import com.google.common.collect.TreeMultimap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.BasicCentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
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
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class MultiNeighborClustering<T extends AdditiveClusterable<T>> extends NeighborClustering<T>
	{
	private static final Logger logger = Logger.getLogger(MultiNeighborClustering.class);


	public MultiNeighborClustering(Set<String> potentialTrainingBins, DissimilarityMeasure<T> dm,
	                               double unknownDistanceThreshold, Set<String> leaveOneOutLabels)
		{
		super(potentialTrainingBins, dm, unknownDistanceThreshold, leaveOneOutLabels);
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		throw new ClusterRuntimeException(
				"It doesn't make sense to get the best clustermove with a multi-neighbor clustering; look for the best label instead using scoredClusterMoves");
		}


	/**
	 * Unlike situations where we make a cluster per label, here we make a whole "cluster" per test sample
	 */
	@Override
	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException
		{
		theClusters = new HashSet<CentroidCluster<T>>();

		Multinomial<CentroidCluster> priorsMult = new Multinomial<CentroidCluster>();
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

				// generate one "cluster" per test sample.
				CentroidCluster<T> cluster = new BasicCentroidCluster<T>(i++, point);//measure

				//** for now we make a uniform prior
				priorsMult.put(cluster, 1);
				theClusters.add(cluster);
				}

			logger.info("Done processing " + sampleCount + " training samples.");

			priorsMult.normalize();
			priors = priorsMult.getValueMap();
			}
		catch (DistributionException e)
			{
			throw new Error(e);
			}
		}

	protected class VotingResults
		{
		// use WeightedSet instead of MultiSet so we can aggregate label probabilities

		// keep track of clusters per label, for the sake of tracking computed distances to the clusters contributing to each label
		private final Map<String, WeightedSet<ClusterMove<T, CentroidCluster<T>>>> labelContributions =
				new HashMap<String, WeightedSet<ClusterMove<T, CentroidCluster<T>>>>();

		private String bestLabel;
		private String secondBestLabel;

		private WeightedSet<String> labelVotes = new HashWeightedSet<String>();

		public String getBestLabel()
			{
			return bestLabel;
			}

		public String getSecondBestLabel()
			{
			return secondBestLabel;
			}

		public void finish(Set<String> predictionLabels)
			{
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

			labelVotes.retainKeys(predictionLabels);
			Iterator<String> vi = labelVotes.keysInDecreasingWeightOrder(weightedDistanceSort).iterator();

			bestLabel = vi.next();
			try
				{
				secondBestLabel = vi.next();
				}
			catch (NoSuchElementException e)
				{
				//no problem
				}
			}

		/*public WeightedSet<ClusterMove<T, CentroidCluster<T>>> getContributions(String label)
			{
			return labelContributions.get(label);
			}*/

		public double computeWeightedDistance(String label)
			{
			return computeWeightedDistance(labelContributions.get(label));
			}

		/**
		 * compute weighted average computed distance to clusters contributing to this label
		 */
		private double computeWeightedDistance(
				WeightedSet<ClusterMove<T, CentroidCluster<T>>> dominantLabelContributions)
			{
			double weightedComputedDistance = 0;

			for (Map.Entry<ClusterMove<T, CentroidCluster<T>>, Double> entry : dominantLabelContributions
					.getItemNormalizedMap().entrySet())
				{
				ClusterMove<T, CentroidCluster<T>> contributingCm = entry.getKey();
				Double contributionWeight = entry.getValue();
				weightedComputedDistance += contributionWeight * contributingCm.bestDistance;
				}
			return weightedComputedDistance;
			}

		public double getProb(String bestLabel)
			{
			return labelVotes.getNormalized(bestLabel);
			}

		public boolean hasSecondBestLabel()
			{
			return secondBestLabel != null;
			}

		public double getVotes(String label)
			{
			return labelVotes.get(label);
			}

		public void addVotes(WeightedSet<String> labelsOnThisCluster)
			{
			labelVotes.addAll(labelsOnThisCluster);
			}

		public void addVotes(WeightedSet<String> labelsOnThisCluster, double multiplier)
			{
			labelVotes.addAll(labelsOnThisCluster, multiplier);
			}

		public void addContribution(ClusterMove<T, CentroidCluster<T>> cm, String label, Double labelProbability)
			{
			WeightedSet<ClusterMove<T, CentroidCluster<T>>> contributionSet = labelContributions.get(label);
			if (contributionSet == null)
				{
				contributionSet = new HashWeightedSet<ClusterMove<T, CentroidCluster<T>>>();
				labelContributions.put(label, contributionSet);
				}
			contributionSet.add(cm, labelProbability);
			contributionSet.incrementItems();
			}
		}


	/**
	 * Returns a map from distance to cluster, sorted by distance; includes only those clusters with distances better than
	 * the unknown threshold.
	 *
	 * @param p
	 * @return
	 */
	protected TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> scoredClusterMoves(T p)
			throws NoGoodClusterException
		{
		TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>> result =
				new TreeMultimap<Double, ClusterMove<T, CentroidCluster<T>>>();

		String disallowedLabel = null;
		if (leaveOneOutLabels != null)
			{
			disallowedLabel = p.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels);
			}

		for (CentroidCluster<T> cluster : theClusters)
			{
			if (disallowedLabel != null && disallowedLabel
					.equals(cluster.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels)))
				{
				// ignore this cluster
				}
			else
				{
				// Note that different distance measures may need to deal with the priors differently:
				// if it's probability, multiply; if log probability, add; for other distance types, who knows?
				// so, just pass the priors in and let the distance measure decide what to do with them

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

				//** note we usually want this not to kick in so we can plot vs. the threshold in Jandy
				if (distance < unknownDistanceThreshold)
					{
					result.put(distance, cm);
					}
				}
			}

		//result = result.headMap(unknownDistanceThreshold);

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
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int iterations)
		{
		// do nothing

		// after that, normalize the label probabilities
		for (Cluster c : theClusters)
			{
			c.updateDerivedWeightedLabelsFromLocal();
			}
		}
	}
