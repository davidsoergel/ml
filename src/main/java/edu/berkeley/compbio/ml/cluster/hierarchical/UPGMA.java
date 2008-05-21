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
import edu.berkeley.compbio.ml.cluster.BatchTreeClusteringMethod;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.HierarchicalCluster;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import edu.berkeley.compbio.phyloutils.LengthWeightHierarchyNode;
import org.apache.commons.lang.NotImplementedException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @Author David Soergel
 * @Version 1.0
 */
public class UPGMA<T extends Clusterable<T>> extends BatchTreeClusteringMethod<T>
	{

	private DistanceMeasure<T> distanceMeasure;
	//private SymmetricPairwiseDistanceMatrix theActiveNodeDistanceMatrix = new SymmetricPairwiseDistanceMatrix();
	private Symmetric2dBiMap<HierarchicalCluster<T>, Double> theActiveNodeDistanceMatrix =
			new Symmetric2dBiMap<HierarchicalCluster<T>, Double>();
	private HierarchicalCluster<T> theRoot;

	//	private SortedSet<ClusterPair<T>> theClusterPairs;

	public UPGMA(DistanceMeasure<T> distanceMeasure)
		{
		this.distanceMeasure = distanceMeasure;

		//		theClusterPairs = new TreeMap<ClusterPair<T>, Double>();
		}

	public LengthWeightHierarchyNode<Cluster<T>> getTree()
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

		return best;
		}

	/**
	 * Return a ClusterMove object describing the best way to reassign the given point to a new cluster.
	 *
	 * @param p
	 * @return
	 */
	public ClusterMove bestClusterMove(T p) throws NoGoodClusterException
		{
		throw new NotImplementedException("");
		}

	public void performClustering()
		{
		n = theActiveNodeDistanceMatrix.numKeys();
		HierarchicalCluster<T> composite = null;
		while (theActiveNodeDistanceMatrix.numKeys() > 1)
			{
			// find shortest distance

			//	NodePair<T> pair = theActiveNodeDistanceMatrix.getClosestPair();// the map is sorted by distance

			Double distance = theActiveNodeDistanceMatrix.getSmallestValue() / 2;
			HierarchicalCluster<T> a = theActiveNodeDistanceMatrix.getKey1WithSmallestValue();
			HierarchicalCluster<T> b = theActiveNodeDistanceMatrix.getKey2WithSmallestValue();


			// set the branch lengths

			a.setLength(distance);
			b.setLength(distance);


			// create a composite node

			composite = new HierarchicalCluster<T>(idCount++, null);
			composite.addChild(a);
			composite.addChild(b);
			composite.setWeight(a.getWeight() + b.getWeight());
			composite.setN(a.getN() + b.getN());

			theClusters.add(composite);

			// compute the distance from the composite node to each remaining cluster
			for (HierarchicalCluster<T> node : new HashSet<HierarchicalCluster<T>>(
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

	public void addAll(Collection<Clusterable<T>> samples)
		{
		//theClusters.addAll(samples);
		Iterator<Clusterable<T>> i = samples.iterator();

		if (theActiveNodeDistanceMatrix.numKeys() == 0)
			{
			HierarchicalCluster a = new HierarchicalCluster(idCount++, i.next());
			HierarchicalCluster b = new HierarchicalCluster(idCount++, i.next());
			a.setN(1);
			b.setN(1);

			addInitialPair(a, b);
			}

		while (i.hasNext())
			{
			Clusterable<T> sample = i.next();
			HierarchicalCluster c = new HierarchicalCluster(idCount++, sample);
			c.setN(1);
			addAndComputeDistances(c);
			}
		}


	void addInitialPair(HierarchicalCluster<T> node1, HierarchicalCluster<T> node2)
		{
		theClusters.add(node1);
		theClusters.add(node2);

		Double d = distanceMeasure
				.distanceFromTo(node1.getValue().getCentroid(), node2.getValue().getCentroid());
		theActiveNodeDistanceMatrix.put(node1, node2, d);
		}

	/**
	 * We can't add a single node when the matrix is empty, since it won't make any pairs ard thus won't retain the node at
	 * all.  Hence the addInitialPair business above.
	 *
	 * @param node
	 */
	void addAndComputeDistances(HierarchicalCluster<T> node)
		{
		theClusters.add(node);
		Set<HierarchicalCluster<T>> activeNodes =
				new HashSet(theActiveNodeDistanceMatrix.getActiveKeys());// avoid ConcurrentModificationException

		/*	Double d = distanceMeasure.distanceFromTo(node.getValue().getCentroid(),
																 node.getValue().getCentroid());// probably 0, but you never know
					   NodePair<T> pair = getOrCreateNodePair(node, node);

					   distanceToPair.put(d, pair);
					   pairToDistance.put(pair, d);
		   */
		for (HierarchicalCluster<T> theActiveNode : activeNodes)
			{
			Double d = distanceMeasure
					.distanceFromTo(node.getValue().getCentroid(), theActiveNode.getValue().getCentroid());
			theActiveNodeDistanceMatrix.put(node, theActiveNode, d);
			}
		}
	}