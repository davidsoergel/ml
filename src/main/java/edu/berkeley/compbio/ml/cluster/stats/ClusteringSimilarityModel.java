/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.stats;

import com.davidsoergel.dsutils.EquivalenceDefinition;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.collections.UnorderedPair;
import edu.berkeley.compbio.ml.cluster.BatchCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterList;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusteringSimilarityModel<T extends Clusterable<T> & Comparable<T>>
	{
	public final double proportionOfReferenceClustersIdentical;
	public final double proportionOfPredictedClustersIdentical;
	public final double proportionOfSamplesInIdenticalClusters;
	public final double proportionOfTruePositiveSamplePairsInIdenticalClusters;

	public final double proportionOfReferenceClustersMappable;
	public final double proportionOfPredictedClustersMappable;
	public final double proportionOfSamplesInMappableReferenceClusters;
	public final double proportionOfSamplePairsInMappableReferenceClusters;
	public final double proportionOfSamplesInMappablePredictedClusters;
	public final double proportionOfSamplePairsInMappablePredictedClusters;

	public final double pairwiseSameClusterSensitivity;
	public final double pairwiseSameClusterSpecificity;

	public ClusteringSimilarityModel(final ClusterList<T> theClusterList, final ClusterList<T> referenceClusterList,
	                                 int totalSamples)
		{

		int totalSamplePairs = (totalSamples * (totalSamples - 1)) / 2;

		// Sensitivity/specificity of detecting clusters perfectly between scrambled and reference

		List<? extends Cluster<? extends T>> referenceClusters = referenceClusterList.getClusters();
		List<? extends Cluster<? extends T>> clusters = theClusterList.getClusters();

		Set<BatchCluster<T, ?>> castReferenceClusters = new HashSet<BatchCluster<T, ?>>();
		for (Cluster<? extends T> referenceCluster : referenceClusters)
			{
			castReferenceClusters.add((BatchCluster<T, ?>) referenceCluster);
			}

		Set<BatchCluster<T, ?>> castClusters = new HashSet<BatchCluster<T, ?>>();
		for (Cluster<? extends T> cluster : clusters)
			{
			castClusters.add((BatchCluster<T, ?>) cluster);
			}

		Set<BatchCluster<T, ?>> identicalClusters = DSCollectionUtils
				.intersectionFast(castReferenceClusters, castClusters, new IdenticalClusterComparator());


		proportionOfReferenceClustersIdentical =
				(double) identicalClusters.size() / (double) castReferenceClusters.size();
		proportionOfPredictedClustersIdentical = (double) identicalClusters.size() / (double) castClusters.size();


		// look for clusters that can be unambiguously mapped to one another

		Set<BatchCluster<T, ?>> mappableReferenceClusters = DSCollectionUtils
				.intersectionExhaustive(castReferenceClusters, castClusters, new MappableClusterComparator(0.5));
		Set<BatchCluster<T, ?>> mappablePredictedClusters = DSCollectionUtils
				.intersectionExhaustive(castClusters, castReferenceClusters, new MappableClusterComparator(0.5));

		// obviously the number of mappable clusters must be the same, since the mapping is 1-1, though the actual clusters are different
		// oops this is no longer true since the "mappable" thing is now asymmetric
		// assert mappablePredictedClusters.size() == mappableReferenceClusters.size();

		proportionOfReferenceClustersMappable =
				(double) mappableReferenceClusters.size() / (double) castReferenceClusters.size();
		proportionOfPredictedClustersMappable =
				(double) mappableReferenceClusters.size() / (double) castClusters.size();


		// Sensitivity/specificity of pairwise taxon OTU co-occurrence.  I.e., being in the same cluster in the reference clustering is the goal; how well is that predicted?

		SortedSet<UnorderedPair<T>> referenceSameClusterSamplePairs = new TreeSet<UnorderedPair<T>>();
		for (BatchCluster<T, ?> cluster : castReferenceClusters)
			{
			SortedSet<T> points = cluster.getPoints(); //new TreeSet<T>(cluster.getPoints());
			for (T point : points)
				{
				for (T t : points.headSet(point))
					{
					referenceSameClusterSamplePairs.add(new UnorderedPair<T>(point, t));
					}
				}
			}

		SortedSet<UnorderedPair<T>> predictedSameClusterSamplePairs = new TreeSet<UnorderedPair<T>>();
		for (BatchCluster<T, ?> cluster : castClusters)
			{
			SortedSet<T> points = cluster.getPoints();  //new TreeSet<T>(cluster.getPoints());
			for (T point : points)
				{
				for (T t : points.headSet(point))
					{
					predictedSameClusterSamplePairs.add(new UnorderedPair<T>(point, t));
					}
				}
			}

		// careful: hashCode/equals/compareTo of Clusterable objects may not work right, since the labelling stuff can't be final
		/*
			   int truePositives =
					   DSCollectionUtils.intersection(referenceSameClusterSamplePairs, predictedSameClusterSamplePairs).size();

			   int falsePositives =
					   DSCollectionUtils.subtract(predictedSameClusterSamplePairs, referenceSameClusterSamplePairs).size();
			   int falseNegatives =
					   DSCollectionUtils.subtract(referenceSameClusterSamplePairs, predictedSameClusterSamplePairs).size();
			   */

		Set<UnorderedPair<T>> truePositives = DSCollectionUtils
				.intersectionFastUsingCompare(referenceSameClusterSamplePairs, predictedSameClusterSamplePairs);
		Set<UnorderedPair<T>> falsePositives = DSCollectionUtils
				.subtractFastUsingCompare(predictedSameClusterSamplePairs, referenceSameClusterSamplePairs);
		Set<UnorderedPair<T>> falseNegatives = DSCollectionUtils
				.subtractFastUsingCompare(referenceSameClusterSamplePairs, predictedSameClusterSamplePairs);

		int trueNegatives = totalSamplePairs - truePositives.size() - falsePositives.size() - falseNegatives.size();

		pairwiseSameClusterSensitivity =
				(double) truePositives.size() / (double) (truePositives.size() + falseNegatives.size());
		pairwiseSameClusterSpecificity = (double) trueNegatives / (double) (trueNegatives + falsePositives.size());


		// % of cluster pairs in identical clusters, out of all the true positive pairs

		int samplesInIdenticalClusters = 0;
		int samplePairsInIdenticalClusters = 0;
		for (BatchCluster<T, ?> identicalCluster : identicalClusters)
			{
			int n = identicalCluster.getN();
			assert n > 0;
			samplesInIdenticalClusters += n;

			if (n > 1)
				{
				int pairs = (n * (n - 1)) / 2;
				samplePairsInIdenticalClusters += pairs;
				}
			}
		proportionOfSamplesInIdenticalClusters = (double) samplesInIdenticalClusters / (double) totalSamples;
		proportionOfTruePositiveSamplePairsInIdenticalClusters =
				(double) samplePairsInIdenticalClusters / (double) truePositives.size();


		// % of cluster pairs in mappable clusters, out of all the positive pairs

		int samplesInMappableReferenceClusters = 0;
		int samplePairsInMappableReferenceClusters = 0;
		for (BatchCluster<T, ?> mappableCluster : mappableReferenceClusters)
			{
			int n = mappableCluster.getN();
			assert n > 0;
			samplesInMappableReferenceClusters += n;

			if (n > 1)
				{
				int pairs = (n * (n - 1)) / 2;
				samplePairsInMappableReferenceClusters += pairs;
				}
			}
		proportionOfSamplesInMappableReferenceClusters =
				(double) samplesInMappableReferenceClusters / (double) totalSamples;
		proportionOfSamplePairsInMappableReferenceClusters =
				(double) samplePairsInMappableReferenceClusters / (double) referenceSameClusterSamplePairs.size();

		int samplesInMappablePredictedClusters = 0;
		int samplePairsInMappablePredictedClusters = 0;
		for (BatchCluster<T, ?> mappableCluster : mappablePredictedClusters)
			{
			int n = mappableCluster.getN();
			assert n > 0;
			samplesInMappablePredictedClusters += n;

			if (n > 1)
				{
				int pairs = (n * (n - 1)) / 2;
				samplePairsInMappablePredictedClusters += pairs;
				}
			}
		proportionOfSamplesInMappablePredictedClusters =
				(double) samplesInMappablePredictedClusters / (double) totalSamples;
		proportionOfSamplePairsInMappablePredictedClusters =
				(double) samplePairsInMappablePredictedClusters / (double) predictedSameClusterSamplePairs.size();
		}


/*	@Override
	protected ClusteringSimilarityModel<T> clone() throws CloneNotSupportedException
		{
		throw new CloneNotSupportedException();
		//return super.clone();
		}*/


	private class IdenticalClusterComparator implements Comparator<BatchCluster<T, ?>>
		{

		/**
		 * Two clusters are considered equal if this proportion of the points are the same (exclusive except in the case of
		 * 1.0)
		 *
		 * @param minMatchingSamplesRequiredForEquality
		 *
		 */
		private IdenticalClusterComparator()
			{
			}

		public int compare(final BatchCluster<T, ?> o1, final BatchCluster<T, ?> o2)
			{

			// recall n may be different from the number of samples, so we don't trust it.
			//	int so1 = o1.getN();
			//	int so2 = o2.getN();

			SortedSet<T> samplesA = o1.getPoints();
			SortedSet<T> samplesB = o2.getPoints();

			// don't rely on hashcodes & equality of the samples
			// this is to support SimpleClusterable, which has a stable sort comparing the ID strings,
			// but where we can't use equals() and hashCode() because the labels are mutable
			Collection<T> union = DSCollectionUtils.unionFastUsingCompare(samplesA, samplesB);
			Collection<T> intersection = DSCollectionUtils.intersectionFastUsingCompare(samplesA, samplesB);

			if (intersection.size() == union.size())
				{
				return 0;
				}

			// still need a stable sort


			int so1 = samplesA.size();
			int so2 = samplesB.size();

			if (so1 < so2)
				{
				return -1;
				}
			if (so1 > so2)
				{
				return 1;
				}


			// rely on the natural sort of the samples
			TreeSet<T> sortedSamplesA = new TreeSet<T>(samplesA);
			TreeSet<T> sortedSamplesB = new TreeSet<T>(samplesB);

			Iterator<T> itA = sortedSamplesA.iterator();
			Iterator<T> itB = sortedSamplesB.iterator();

			while (itA.hasNext())
				{
				T a = itA.next();
				T b = itB.next();

				int c = a.compareTo(b);
				if (c == 0)
					{
					continue;
					}
				else
					{
					return c;
					}
				}

			throw new ClusterRuntimeException("Impossible");
			}
		}

	private class MappableClusterComparator implements EquivalenceDefinition<BatchCluster<T, ?>>
		{
		private double minMatchingSamplesRequiredForEquality;

		/**
		 * Two clusters are considered equal if this proportion of the points are the same (exclusive except in the case of
		 * 1.0)
		 *
		 * @param minMatchingSamplesRequiredForEquality
		 *
		 */
		private MappableClusterComparator(double minMatchingSamplesRequiredForEquality)
			{
			// because the bound is exclusive, requiring 1.0 always fails
			this.minMatchingSamplesRequiredForEquality = Math.min(minMatchingSamplesRequiredForEquality, 0.99999);
			}

		//** could rejigger to sort by overall match quality: large clusters with lots of overlap first
		//** note this sort is overall unstable: "equal" elements may not be contiguous
		public boolean areEquivalent(final BatchCluster<T, ?> o1, final BatchCluster<T, ?> o2)
			{

			// recall n may be different from the number of samples, so we don't trust it.
			//	int so1 = o1.getN();
			//	int so2 = o2.getN();

			Set<T> samplesA = o1.getPoints();
			Set<T> samplesB = o2.getPoints();

			// rely on hashcodes & equality of the samples
			Collection<T> union = DSCollectionUtils.unionUsingCompare(samplesA, samplesB);
			Collection<T> intersection = DSCollectionUtils.intersectionUsingCompare(samplesA, samplesB);

			/*
			if (intersection.size() > union.size() * minMatchingSamplesRequiredForEquality)
				{
				return 0;
				}
			*/

			// since this is asymmetric anyway, may as well do the relaxed variant
			return (intersection.size() > samplesA.size() * minMatchingSamplesRequiredForEquality);
			/*	{
				return true;
				}

			// still need a stable sort


			int so1 = samplesA.size();
			int so2 = samplesB.size();

			if (so1 < so2)
				{
				return -1;
				}
			if (so1 > so2)
				{
				return 1;
				}


			// rely on the natural sort of the samples
			TreeSet<T> sortedSamplesA = new TreeSet<T>(samplesA);
			TreeSet<T> sortedSamplesB = new TreeSet<T>(samplesB);

			Iterator<T> itA = sortedSamplesA.iterator();
			Iterator<T> itB = sortedSamplesB.iterator();

			while (itA.hasNext())
				{
				T a = itA.next();
				T b = itB.next();

				int c = a.compareTo(b);
				if (c == 0)
					{
					continue;
					}
				else
					{
					return c;
					}
				}

			throw new ClusterRuntimeException("Impossible");*/
			}
		}
	}
