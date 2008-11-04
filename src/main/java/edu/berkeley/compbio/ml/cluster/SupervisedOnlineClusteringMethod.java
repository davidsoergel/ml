package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class SupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends OnlineClusteringMethod<T, C>
	{
	/**
	 * Adds a point to the best cluster.  Generally it's not a good idea to store the point itself in the cluster for
	 * memory reasons; so this method is primarily useful for updating the position of the centroid.
	 *
	 * @param p
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 * @return
	 * @throws edu.berkeley.compbio.ml.cluster.ClusterException
	 *
	 * @throws edu.berkeley.compbio.ml.cluster.NoGoodClusterException
	 *
	 */
	public boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException
		{
		throw new NotImplementedException("Can't add a point to a supervised clustering");
		}


	/**
	 * consider each of the incoming data points exactly once per iteration.  Note iterations > 1 only makes sense for
	 * unsupervised clustering.
	 */
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int iterations)
			throws IOException, ClusterException
		{
		if (iterations != 1)
			{
			throw new ClusterException(
					"Multiple-iteration training probably doesn't make sense for supervised clustering");
			}

		if (mutuallyExclusiveLabels == null)
			{
			throw new ClusterException("Must assign a set of labels before training supervised clustering");
			}

		train(trainingCollectionIteratorFactory);
		}


	/**
	 * consider each of the incoming data points exactly once.
	 */
	public abstract void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException;
	}
