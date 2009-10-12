package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMapWithDefault;
import edu.berkeley.compbio.ml.cluster.hierarchical.HierarchicalCentroidCluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface DistanceMatrixBatchClusteringMethod<T extends Clusterable<T>> extends BatchClusteringMethod<T>
	{
	Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Double> getDistanceMatrix();

	void setDistanceMatrix(Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Double> distanceMatrix);
	}
