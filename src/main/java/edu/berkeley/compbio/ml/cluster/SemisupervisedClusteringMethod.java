package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;

import java.util.Iterator;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SemisupervisedClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends UnsupervisedClusteringMethod<T, C>, SupervisedClusteringMethod<T, C>
	{

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

	ClusteringTestResults test(Iterator<T> testIterator, //Set<String> mutuallyExclusiveLabels,
	                           DissimilarityMeasure<String> intraLabelDistances)
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
//	public abstract void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
//	                                            GenericFactory<T> prototypeFactory)
//			throws GenericFactoryException, ClusterException;


	/**
	 * Sets a list of labels to be used for classification.  For a supervised method, this must be called before training.
	 *
	 * @param trainingLabels a set of mutually-exclusive labels that we want to predict.  Note multiple bins may predict
	 *                       the same label; defining the clusters is a separate issue.
	 */
//	public void setTrainingLabels(Set<String> trainingLabels);

	/**
	 * Sets a list of labels that the test samples will have, to which to compare our predictions.  Typically these will be
	 * the same as the training labels, but they need not be, as long as the wrongness measure can compare across the two
	 * sets.
	 *
	 * @param testLabels a set of mutually-exclusive labels that we want to predict.  Note multiple bins may predict the
	 *                   same label; defining the clusters is a separate issue.
	 */
//	public void setTestLabels(Set<String> testLabels);
	}
