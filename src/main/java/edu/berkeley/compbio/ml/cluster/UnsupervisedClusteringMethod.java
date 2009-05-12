package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface UnsupervisedClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
	/**
	 * consider each of the incoming data points exactly once per iteration.  Note iterations > 1 only makes sense for
	 * unsupervised clustering.
	 */
	//public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int iterations)
	//		throws IOException, ClusterException;
	/*	{
		if (iterations > 1)
			{
			throw new ClusterException(
					"Multiple-iteration training probably doesn't make sense for supervised clustering");
			}

		if (predictLabels == null)
			{
			throw new ClusterException("Must assign a set of labels before training supervised clustering");
			}

		train(trainingCollectionIteratorFactory);
		}*/
	}
