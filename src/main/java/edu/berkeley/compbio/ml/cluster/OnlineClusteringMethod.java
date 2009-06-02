package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: OnlineClusteringMethod.java 398 2009-05-12 05:11:40Z soergel $
 */
public interface OnlineClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Create some initial clusters using the first few training samples.
	 */
	// void initializeWithRealData(Iterator<T> trainingIterator, GenericFactory<T> prototypeFactory) throws GenericFactoryException;
	}
