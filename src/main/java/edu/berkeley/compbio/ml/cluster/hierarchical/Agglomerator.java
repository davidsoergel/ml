/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import edu.berkeley.compbio.ml.cluster.Clusterable;

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
		// final Collection<HierarchicalCentroidCluster<T>> activeKeys =
		//		new HashSet<HierarchicalCentroidCluster<T>>(theActiveNodeDistanceMatrix.getKeys());

		//assert !theActiveNodeDistanceMatrix.getActiveKeys().contains(composite);
		theActiveNodeDistanceMatrix.addKey(composite);

		//PERF serial test
		for (HierarchicalCentroidCluster<T> node : theActiveNodeDistanceMatrix.getKeys())
			{
			addCompositeVsNodeToDistanceMatrix(a, b, composite, node, theActiveNodeDistanceMatrix);
			}


		//assert theActiveNodeDistanceMatrix.getActiveKeys().contains(composite);

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

		T centroid = null;  // don't bother storing explicit centroids for composite nodes

		//a.getCentroid().

		final HierarchicalCentroidCluster<T> composite = createComposite(id, centroid);
		a.setParent(composite);
		b.setParent(composite);

		composite.addAll(a);
		composite.addAll(b);

		// weight and weightedLabels.getItemCount() are maybe redundant; too bad
		composite.setWeight(a.getWeight() + b.getWeight());
		assert composite.getWeight() != null;

		composite.doneLabelling();

		int numActive = theActiveNodeDistanceMatrix.numKeys();
//		int numPairs = theActiveNodeDistanceMatrix.numPairs();

		// PERF unnecessary...
		// add the branch to the distance table for consistency
		//	theActiveNodeDistanceMatrix.put(a, composite, distance);
		//	theActiveNodeDistanceMatrix.put(b, composite, distance);


		addCompositeToDistanceMatrix(a, b, composite, theActiveNodeDistanceMatrix);

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
	protected HierarchicalCentroidCluster<T> createComposite( final int id, final T centroid )
		{
		return new HierarchicalCentroidCluster<T>(id,
		                                   centroid);
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
