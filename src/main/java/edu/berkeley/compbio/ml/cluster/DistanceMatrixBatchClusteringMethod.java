package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.IndexedSymmetric2dBiMapWithDefault;
import edu.berkeley.compbio.ml.cluster.hierarchical.HierarchicalCentroidCluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface DistanceMatrixBatchClusteringMethod<T extends Clusterable<T>> extends BatchClusteringMethod<T>
	{
	IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float> getDistanceMatrix();

	void setDistanceMatrix(IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float> distanceMatrix);

	void setThreshold(float threshold);
	}
