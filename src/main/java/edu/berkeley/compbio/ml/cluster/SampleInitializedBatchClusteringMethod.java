/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * Some online clustering methods (e.g. Kohonen SOM, K-means) need their clusters initialized with some real data before
 * the training proper begins
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SampleInitializedBatchClusteringMethod<T extends Clusterable<T>>
		extends BatchClusteringMethod<T> //, UnsupervisedClusteringMethod<T>
	{
// -------------------------- OTHER METHODS --------------------------

//	void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, final GenericFactory<T> prototypeFactory,
//	           int trainingEpochs) throws IOException, ClusterException;

	/**
	 * if the implementation is also a PrototypeBasedControidClusteringMethod, then createClusters must be called first to
	 * provide the prototype factory
	 *
	 * @param sequenceFragmentIterator
	 * @param initSamples
	 */
	void initializeWithSamples(ClusterableIterator<T> sequenceFragmentIterator);
	}
