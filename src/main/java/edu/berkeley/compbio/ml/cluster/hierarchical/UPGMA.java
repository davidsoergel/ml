/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.BatchTreeClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.HierarchicalCentroidCluster;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.phyloutils.LengthWeightHierarchyNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class UPGMA<T extends Clusterable<T>> extends BatchTreeClusteringMethod<T>
	{

	private DissimilarityMeasure<T> dissimilarityMeasure;
	//private SymmetricPairwiseDistanceMatrix theActiveNodeDistanceMatrix = new SymmetricPairwiseDistanceMatrix();
	private Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix =
			new Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double>();
	private HierarchicalCentroidCluster<T> theRoot;

	//	private SortedSet<ClusterPair<T>> theClusterPairs;

	public UPGMA(DissimilarityMeasure<T> dissimilarityMeasure)
		{
		this.dissimilarityMeasure = dissimilarityMeasure;

		//		theClusterPairs = new TreeMap<ClusterPair<T>, Double>();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LengthWeightHierarchyNode<CentroidCluster<T>, ? extends LengthWeightHierarchyNode> getTree()
		{
		return theRoot;
		}

	/**
	 * Returns the best cluster without adding the point.  Since in this case every sample is a cluster, just returns the
	 * closest sample.
	 *
	 * @param p                   Point to find the best cluster of
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 */
	/*	@Nullable
   @Override
   public Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
		   throws ClusterException, NoGoodClusterException
	   {
	   Cluster<T> best = null;
	   double bestDistance = Double.MAX_VALUE;

	   for (Cluster<T> theCluster : theClusters)
		   {
		   double distance = distanceMeasure.distanceFromTo(p, theCluster.getCentroid());
		   if (distance < bestDistance)
			   {
			   best = theCluster;
			   bestDistance = distance;
			   }
		   }
	   if (best == null)
		   {
		   throw new NoGoodClusterException("No cluster found for point: " + p);
		   }
	   return best;
	   }*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove bestClusterMove(T p) throws NoGoodClusterException
		{
		ClusterMove result = new ClusterMove();
		result.bestDistance = Double.MAX_VALUE;

		for (CentroidCluster<T> theCluster : theClusters)
			{
			double distance = dissimilarityMeasure.distanceFromTo(p, theCluster.getCentroid());
			if (distance < result.bestDistance)
				{
				result.bestCluster = theCluster;
				result.bestDistance = distance;
				}
			}
		if (result.bestCluster == null)
			{
			throw new NoGoodClusterException("No cluster found for point: " + p);
			}
		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performClustering()
		{
		n = theActiveNodeDistanceMatrix.numKeys();
		HierarchicalCentroidCluster<T> composite = null;
		while (theActiveNodeDistanceMatrix.numKeys() > 1)
			{
			// find shortest distance

			//	NodePair<T> pair = theActiveNodeDistanceMatrix.getClosestPair();// the map is sorted by distance

			Double distance = theActiveNodeDistanceMatrix.getSmallestValue() / 2;
			HierarchicalCentroidCluster<T> a = theActiveNodeDistanceMatrix.getKey1WithSmallestValue();
			HierarchicalCentroidCluster<T> b = theActiveNodeDistanceMatrix.getKey2WithSmallestValue();


			// set the branch lengths

			a.setLength(distance);
			b.setLength(distance);


			// create a composite node

			composite = new HierarchicalCentroidCluster<T>(idCount++, null);
			a.setParent(composite);
			b.setParent(composite);

			composite.addAll(a);
			composite.addAll(b);

			// weight and weightedLabels.getItemCount() are maybe redundant; too bad
			composite.setWeight(a.getWeight() + b.getWeight());
			//composite.setN(a.getN() + b.getN());

			theClusters.add(composite);

			// compute the distance from the composite node to each remaining cluster
			for (HierarchicalCentroidCluster<T> node : new HashSet<HierarchicalCentroidCluster<T>>(
					theActiveNodeDistanceMatrix.getActiveKeys()))
				{
				if (node == a || node == b)
					{
					continue;
					}
				distance = (a.getWeight() / composite.getWeight()) * theActiveNodeDistanceMatrix.get(a, node)
						+ (b.getWeight() / composite.getWeight()) * theActiveNodeDistanceMatrix.get(b, node);
				theActiveNodeDistanceMatrix.put(node, composite, distance);
				}

			// remove the two merged clusters from consideration

			theActiveNodeDistanceMatrix.remove(a);
			theActiveNodeDistanceMatrix.remove(b);

			// add the composite node to the active list
			// no longer needed; automatic
			// theActiveNodeDistanceMatrix.add(composite);
			}

		theRoot = composite;//theActiveNodeDistanceMatrix.getActiveKeys().iterator().next();
		}

	private int idCount = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAll(Collection<? extends Clusterable<T>> samples)
		{
		//theClusters.addAll(samples);
		Iterator<? extends Clusterable<T>> i = samples.iterator();

		if (theActiveNodeDistanceMatrix.numKeys() == 0)
			{
			HierarchicalCentroidCluster a = new HierarchicalCentroidCluster(idCount++, i.next());
			HierarchicalCentroidCluster b = new HierarchicalCentroidCluster(idCount++, i.next());
			//a.setN(1);
			//b.setN(1);

			addInitialPair(a, b);
			}

		while (i.hasNext())
			{
			Clusterable<T> sample = i.next();
			HierarchicalCentroidCluster c = new HierarchicalCentroidCluster(idCount++, sample);
			//c.setN(1);
			addAndComputeDistances(c);
			}
		}


	void addInitialPair(HierarchicalCentroidCluster<T> node1, HierarchicalCentroidCluster<T> node2)
		{
		theClusters.add(node1);
		theClusters.add(node2);

		Double d = dissimilarityMeasure
				.distanceFromTo(node1.getValue().getCentroid(), node2.getValue().getCentroid());
		theActiveNodeDistanceMatrix.put(node1, node2, d);
		}

	/**
	 * We can't add a single node when the matrix is empty, since it won't make any pairs ard thus won't retain the node at
	 * all.  Hence the addInitialPair business above.
	 *
	 * @param node
	 */
	void addAndComputeDistances(HierarchicalCentroidCluster<T> node)
		{
		theClusters.add(node);
		Set<HierarchicalCentroidCluster<T>> activeNodes =
				new HashSet(theActiveNodeDistanceMatrix.getActiveKeys());// avoid ConcurrentModificationException

		/*	Double d = distanceMeasure.distanceFromTo(node.getValue().getCentroid(),
																 node.getValue().getCentroid());// probably 0, but you never know
					   NodePair<T> pair = getOrCreateNodePair(node, node);

					   distanceToPair.put(d, pair);
					   pairToDistance.put(pair, d);
		   */
		for (HierarchicalCentroidCluster<T> theActiveNode : activeNodes)
			{
			Double d = dissimilarityMeasure
					.distanceFromTo(node.getValue().getCentroid(), theActiveNode.getValue().getCentroid());
			theActiveNodeDistanceMatrix.put(node, theActiveNode, d);
			}
		}
	}
