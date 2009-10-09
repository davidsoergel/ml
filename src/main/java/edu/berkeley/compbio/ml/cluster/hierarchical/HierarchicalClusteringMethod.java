package edu.berkeley.compbio.ml.cluster.hierarchical;

import edu.berkeley.compbio.ml.cluster.Clusterable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface HierarchicalClusteringMethod<T extends Clusterable<T>>
	{// -------------------------- OTHER METHODS --------------------------

	/**
	 * Returns a LengthWeightHierarchyNode representing the root of the computed clustering tree.  Only valid after
	 * performClustering() has been run.
	 *
	 * @return a LengthWeightHierarchyNode representing the root of the computed clustering tree, or null if the clustering
	 *         procedure has not been performed yet.
	 */
	//public abstract LengthWeightHierarchyNode<CentroidCluster<T>, ? extends LengthWeightHierarchyNode>
	HierarchicalCentroidCluster<T> getTree();
	}
