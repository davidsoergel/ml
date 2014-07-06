/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SemisupervisedClusteringMethod<T extends Clusterable<T>>
		extends UnsupervisedClusteringMethod<T>, SupervisedClusteringMethod<T>
	{
//	void performClustering();

	//(Iterator<T> combinedIterator);

//	ClusteringTestResults test(Iterator<T> testIterator, //Set<String> mutuallyExclusiveLabels,
//	                           DissimilarityMeasure<String> intraLabelDistances)
//			throws DistributionException, ClusterException;


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
