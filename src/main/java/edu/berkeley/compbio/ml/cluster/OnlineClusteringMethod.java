package edu.berkeley.compbio.ml.cluster;

import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface OnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends BatchClusteringMethod<T, C>
	{
	/**
	 * Adds a point to the best cluster.  Generally it's not a good idea to store the point itself in the cluster for
	 * memory reasons; so this method is primarily useful for updating the position of the centroid.
	 *
	 * @param p
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 * @return
	 * @throws ClusterException
	 * @throws NoGoodClusterException
	 */
	boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException;
	}
