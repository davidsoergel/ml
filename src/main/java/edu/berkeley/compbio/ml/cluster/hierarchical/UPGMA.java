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

import com.davidsoergel.dsutils.HashMap2D;
import com.davidsoergel.dsutils.LengthWeightHierarchyNode;
import edu.berkeley.compbio.ml.cluster.BatchTreeClusteringMethod;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.commons.lang.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;


/**
 * @Author David Soergel
 * @Version 1.0
 */
public class UPGMA<T extends Clusterable<T>> extends BatchTreeClusteringMethod<T>
	{

	private DistanceMeasure<T> distanceMeasure;

	private SortedSet<ClusterPair<T>> theClusterPairs;

	public UPGMA(DistanceMeasure<T> distanceMeasure)
		{
		this.distanceMeasure = distanceMeasure;

		theClusterPairs = new TreeMap<ClusterPair<T>, Double>();
		}

	public LengthWeightHierarchyNode<T> getTree()
		{
		return null;
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
		// find shortest distance

		pair = theClusterPairs.remove(0);  // the map is sorted by distance

		// remove the two merged clusters from consideration

		// compute the distance from the composite node to each remaining cluster

		for()
			{
			distance = theClusterPairs.get(a, i) + theClusterPairs.get(b, i);
			theClusterPairs.add()
			}
		}

	public void addAll(Collection<Clusterable<T>> samples)
		{
		//theClusters.addAll(samples);
		for (Clusterable<T> sample : samples)
			{
			theActiveNodes.add(new LengthWeightHierarchyNode())
			}
		}

	private class SymmetricPairwiseDistanceMatrix<T>
		{
		HashMap2D theDistanceMatrix = new HashMap2D();
		}

	private class ClusterPair<T extends Clusterable<T>>
		{
		Cluster<T> cluster1;
		Cluster<T> cluster2;
		}
	}
