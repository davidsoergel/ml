package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;

import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SemisupervisedClusteringMethod<T extends Clusterable<T>>
	{
	void setLabels(Set<String> mutuallyExclusiveLabels);

//	void performClustering();

	//(Iterator<T> combinedIterator);

	/**
	 * Recompute a set of clusters from the stored samples.
	 */
	public abstract void performClustering();

	/**
	 * Add the given samples to the set to be clustered.
	 *
	 * @param samples a Collection of Clusterable objects.
	 */
	void addAll(Iterator<? extends Clusterable<T>> samples);

	ClusteringMethod.TestResults test(Iterator<T> testIterator, //Set<String> mutuallyExclusiveLabels,
	                                  DissimilarityMeasure<String> intraLabelDistancesA,
	                                  DissimilarityMeasure<String> intraLabelDistancesB)
			throws DistributionException, ClusterException;

	void addAllAndRemember(Iterator<T> testIterator);

	/**
	 * Create some initial clusters using the first few training samples.  This is not the same as the training itself!
	 *
	 * @param trainingIterator
	 * @param initSamples
	 * @param prototypeFactory
	 * @throws com.davidsoergel.dsutils.GenericFactoryException
	 *
	 * @throws ClusterException
	 */
	public abstract void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                            GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException;
	}
