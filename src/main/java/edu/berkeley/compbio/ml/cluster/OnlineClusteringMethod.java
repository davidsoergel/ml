package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: OnlineClusteringMethod.java 398 2009-05-12 05:11:40Z soergel $
 */
public interface OnlineClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
	/**
	 * Create some initial clusters using the first few training samples.
	 */
	// void initializeWithRealData(Iterator<T> trainingIterator, GenericFactory<T> prototypeFactory) throws GenericFactoryException;

	/**
	 * Adds a point to the best cluster.  Generally it's not a good idea to store the point itself in the cluster for
	 * memory reasons; so this method is primarily useful for updating the position of the centroid.
	 *
	 * @param p
	 * @return
	 * @throws ClusterException
	 * @throws NoGoodClusterException
	 */
	boolean add(T p) throws ClusterException, NoGoodClusterException;  //, List<Double> secondBestDistances
	}
