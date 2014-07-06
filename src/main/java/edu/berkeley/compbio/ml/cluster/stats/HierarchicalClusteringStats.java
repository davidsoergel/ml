/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.stats;

import com.davidsoergel.trees.BasicPhylogenyNode;
import com.davidsoergel.trees.DepthFirstTreeIterator;
import com.davidsoergel.trees.LengthWeightHierarchyNode;
import com.davidsoergel.trees.PhylogenyNode;
import com.davidsoergel.trees.TreeException;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterList;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ClusteringStats;
import edu.berkeley.compbio.ml.cluster.SimpleHierarchicalCentroidClusterList;
import edu.berkeley.compbio.ml.cluster.hierarchical.HierarchicalCentroidCluster;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HierarchicalClusteringStats<T extends Clusterable<T>>
	{
	final HierarchicalCentroidCluster<T> theClustering;
	final HierarchicalCentroidCluster<T> referenceTree;

	public HierarchicalClusteringStats(final HierarchicalCentroidCluster<T> theClustering,
	                                   final HierarchicalCentroidCluster<T> referenceTree)
		{
		this.theClustering = theClustering;
		this.referenceTree = referenceTree;
		}

	public Map<Double, ClusteringStats> statsByLevel(Collection<Double> thresholds) throws TreeException
		{
		final Map<Double, ClusteringStats> result = new HashMap<Double, ClusteringStats>();

		Map<Double, SimpleHierarchicalCentroidClusterList<T>> clusterSets = selectOTUs(theClustering, thresholds);
		Map<Double, SimpleHierarchicalCentroidClusterList<T>> referenceSets = selectOTUs(referenceTree, thresholds);

		for (Map.Entry<Double, SimpleHierarchicalCentroidClusterList<T>> entry : clusterSets.entrySet())
			{
			Double threshold = entry.getKey();
			ClusterList clusters = entry.getValue();

			ClusterList referenceClusters = referenceSets.get(threshold);

			ClusteringStats stats = new ClusteringStats(clusters, referenceClusters); //, comparator);
			result.put(threshold, stats);
			}
		return result;
		}

	public static <R extends Clusterable<R>> Map<Double, SimpleHierarchicalCentroidClusterList<R>> selectOTUs(final BasicPhylogenyNode tree, Collection<Double> thresholds)
			throws TreeException
		{

		final Map<Double, SimpleHierarchicalCentroidClusterList<R>> results = new HashMap<Double, SimpleHierarchicalCentroidClusterList<R>>();

		int i = 0;
		for (final Double threshold : thresholds)
			{
			final DepthFirstTreeIterator<CentroidCluster<R>, PhylogenyNode<CentroidCluster<R>>> it =
					tree.depthFirstIterator();

			Set<HierarchicalCentroidCluster<R>> result = new HashSet<HierarchicalCentroidCluster<R>>();
			final double halfThreshold = threshold / 2.0;
			//int otuCount = 0;
			while (it.hasNext())
				{
				final HierarchicalCentroidCluster<R> node = (HierarchicalCentroidCluster<R>) it.next();
				final Collection<? extends LengthWeightHierarchyNode> children = node.getChildren();
				//assert children.isEmpty() || children.size() == 2;

				// take advantage of the fact that we know that this is a binary tree, and the two branch lengths below each node are equal
				// the exception is the root node, which may have many children at great distance, but that will fail the skip test here anyway

				if (children.isEmpty() || children.iterator().next().getLength() < halfThreshold)
					{
					//otuCount++;
					//final CentroidCluster payload = node.getPayload();
					//final Clusterable cluster = node.getCentroid();
					result.add(node);
					it.skipAllDescendants(node);
					}
				}
			results.put(threshold, new SimpleHierarchicalCentroidClusterList<R>(result));
			i++;
			}
		return results;
		}
	}
