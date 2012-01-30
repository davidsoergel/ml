package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Clusterable;
/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class ExplicitAgglomerator<T extends AdditiveClusterable<T>> extends Agglomerator<T>
	{
	@Override protected HierarchicalCentroidCluster<T> createComposite( final int id, final T centroid ) {
	return new HierarchicalExplicitCentroidCluster<T>(id, centroid);
	}
	}
