package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
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
	                                                           final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix);

	private void addCompositeToDistanceMatrix(final HierarchicalCentroidCluster<T> a,
	                                          final HierarchicalCentroidCluster<T> b,
	                                          final HierarchicalCentroidCluster<T> composite,
	                                          final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix)
		{
		// there was a mysterious concurrent-modification sort of problem; does this fix it?
		final HashSet<HierarchicalCentroidCluster<T>> activeKeys =
				new HashSet<HierarchicalCentroidCluster<T>>(theActiveNodeDistanceMatrix.getActiveKeys());

		assert !theActiveNodeDistanceMatrix.getActiveKeys().contains(composite);
		theActiveNodeDistanceMatrix.addKey(composite);

		//PERF serial test
		for (HierarchicalCentroidCluster<T> node : activeKeys)
			{
			addCompositeVsNodeToDistanceMatrix(a, b, composite, node, theActiveNodeDistanceMatrix);
			}

		assert theActiveNodeDistanceMatrix.getActiveKeys().contains(composite);

		/*	Parallel.forEach(activeKeys,
								 new Function<HierarchicalCentroidCluster<T>, Void>()
								 {
								 public Void apply(final HierarchicalCentroidCluster<T> node)
									 {
									 addCompositeVsNodeToDistanceMatrix(a, b, composite, node, theActiveNodeDistanceMatrix);
									 return null;
									 }
								 });*/
		}

	public HierarchicalCentroidCluster<T> joinNodes(final int id, final HierarchicalCentroidCluster<T> a,
	                                                final HierarchicalCentroidCluster<T> b,
	                                                final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix)
		{
		// set the branch lengths

		Float dist = theActiveNodeDistanceMatrix.get(a, b);
		/*	if (dist == null)
		   {
		   dist = theActiveNodeDistanceMatrix.get(a, b);
		   }*/
		Float distance = dist / 2f;
		a.setLength(distance.doubleValue());
		b.setLength(distance.doubleValue());

		// create a composite node

		final HierarchicalCentroidCluster<T> composite = new HierarchicalCentroidCluster<T>(id,
		                                                                                    null);  // don't bother storing explicit centroids for composite nodes


		a.setParent(composite);
		b.setParent(composite);

		composite.addAll(a);
		composite.addAll(b);

		// weight and weightedLabels.getItemCount() are maybe redundant; too bad
		composite.setWeight(a.getWeight() + b.getWeight());
		assert composite.getWeight() != null;

		composite.doneLabelling();

		assert composite.getWeight() != null;

		int numActive = theActiveNodeDistanceMatrix.numKeys();
//		int numPairs = theActiveNodeDistanceMatrix.numPairs();

		// PERF unnecessary...
		// add the branch to the distance table for consistency
		//	theActiveNodeDistanceMatrix.put(a, composite, distance);
		//	theActiveNodeDistanceMatrix.put(b, composite, distance);
		assert composite.getWeight() != null;

		addCompositeToDistanceMatrix(a, b, composite, theActiveNodeDistanceMatrix);
		assert composite.getWeight() != null;

		//** these assertions are likely true only for single-threaded clusterers,
		// and we don't want to synchronize a whole block on theActiveNodeDistancesMatrix here just for the sake of the assertions
		if (numActive > 2)
			{
			assert theActiveNodeDistanceMatrix.numKeys() == numActive + 1;
//			assert theActiveNodeDistanceMatrix.numPairs() == numPairs + numActive;
			}


		// add the composite node to the active list
		// no longer needed; automatic
		// theActiveNodeDistanceMatrix.add(composite);
		assert composite.getChildren().size() > 0;

		return composite;
		}

/*	public void removeJoinedNodes(final HierarchicalCentroidCluster<T> a, final HierarchicalCentroidCluster<T> b,
	                              final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix)
		{
		//	int numActive = theActiveNodeDistanceMatrix.numKeys();
		//	int numPairs = theActiveNodeDistanceMatrix.numPairs();

		// remove the two merged clusters from consideration

		//	int removedA =
		theActiveNodeDistanceMatrix.remove(a);
		//	int removedB =
		theActiveNodeDistanceMatrix.remove(b);
*/
/*		assert removedA == numActive - 1;
		assert removedB == numActive - 2;

		if (numActive > 3)
			{
			assert theActiveNodeDistanceMatrix.numKeys() == numActive - 2;
			assert theActiveNodeDistanceMatrix.numPairs() == numPairs - (numActive - 1) - (numActive - 2);
			}*/
//		}
	}