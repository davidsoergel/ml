package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import com.davidsoergel.dsutils.concurrent.Parallel;
import com.google.common.base.Function;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.HashSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class Agglomerator<T extends Clusterable<T>>
	{
	protected abstract void addCompositeVsNodeToDistanceMatrix(final HierarchicalCentroidCluster<T> origA,
	                                                           final HierarchicalCentroidCluster<T> origB,
	                                                           final HierarchicalCentroidCluster<T> composite,
	                                                           final HierarchicalCentroidCluster<T> otherNode,
	                                                           final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix);

	private void addCompositeToDistanceMatrix(final HierarchicalCentroidCluster<T> a,
	                                          final HierarchicalCentroidCluster<T> b,
	                                          final HierarchicalCentroidCluster<T> composite,
	                                          final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix)
		{
		// there was a mysterious concurrent-modification sort of problem; does this fix it?
		Parallel.forEach(new HashSet<HierarchicalCentroidCluster<T>>(theActiveNodeDistanceMatrix.getActiveKeys()),
		                 new Function<HierarchicalCentroidCluster<T>, Void>()
		                 {
		                 public Void apply(final HierarchicalCentroidCluster<T> node)
			                 {
			                 addCompositeVsNodeToDistanceMatrix(a, b, composite, node, theActiveNodeDistanceMatrix);
			                 return null;
			                 }
		                 });
		}

	public HierarchicalCentroidCluster<T> joinNodes(final int id, final HierarchicalCentroidCluster<T> a,
	                                                final HierarchicalCentroidCluster<T> b,
	                                                final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix)
		{
		// set the branch lengths

		Double distance = theActiveNodeDistanceMatrix.get(a, b) / 2.;
		a.setLength(distance);
		b.setLength(distance);

		// create a composite node

		final HierarchicalCentroidCluster<T> composite = new HierarchicalCentroidCluster<T>(id,
		                                                                                    null);  // don't bother storing explicit centroids for composite nodes


		a.setParent(composite);
		b.setParent(composite);

		composite.addAll(a);
		composite.addAll(b);

		// weight and weightedLabels.getItemCount() are maybe redundant; too bad
		composite.setWeight(a.getWeight() + b.getWeight());


		addCompositeToDistanceMatrix(a, b, composite, theActiveNodeDistanceMatrix);

		// remove the two merged clusters from consideration

		theActiveNodeDistanceMatrix.remove(a);
		theActiveNodeDistanceMatrix.remove(b);

		// add the composite node to the active list
		// no longer needed; automatic
		// theActiveNodeDistanceMatrix.add(composite);
		return composite;
		}
	}
