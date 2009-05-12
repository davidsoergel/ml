package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.stats.DissimilarityMeasure;

import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class SupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractOnlineClusteringMethod<T, C> implements SupervisedClusteringMethod<T, C>
	{
	protected SupervisedOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                           Set<String> predictLabels, Set<String> leaveOneOutLabels,
	                                           Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		}


	/**
	 * consider each of the incoming data points exactly once.
	 */
	public abstract void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException;
	}
