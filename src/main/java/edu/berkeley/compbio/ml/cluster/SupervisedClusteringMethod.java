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
	}
