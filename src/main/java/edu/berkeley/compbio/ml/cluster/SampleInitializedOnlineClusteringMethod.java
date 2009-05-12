package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.GenericFactory;

import java.util.Iterator;

/**
 * Some online clustering methods (e.g. Kohonen SOM, K-means) need their clusters initialized with some real data before
 * the training proper begins
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SampleInitializedOnlineClusteringMethod<T extends Clusterable<T>> extends OnlineClusteringMethod<T>
	{

//	void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, final GenericFactory<T> prototypeFactory,
//	           int trainingEpochs) throws IOException, ClusterException;

	void initializeWithSamples(Iterator<T> sequenceFragmentIterator, int initSamples,
	                           GenericFactory<T> prototypeFactory);
	}
