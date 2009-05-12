package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SupervisedClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends ClusteringMethod<T, C>
	{
	}
