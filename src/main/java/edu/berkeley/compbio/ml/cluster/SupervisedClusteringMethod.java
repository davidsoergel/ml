/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.io.IOException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SupervisedClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
	/**
	 * Note the prototypeFactory and the trainingEpochs may be ignored, depending on the implementation; this is just a
	 * hacky way to get a consistent API
	 *
	 * @param trainingCollectionIteratorFactory
	 *
	 * @param prototypeFactory
	 * @param trainingEpochs
	 * @throws IOException
	 * @throws ClusterException
	 */
//	void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, final GenericFactory<T> prototypeFactory,
//	           int trainingEpochs) throws IOException, ClusterException;

	//public DissimilarityMeasure<T> getDissimilarityMeasure();

	String getDistanceSpec();
	}
