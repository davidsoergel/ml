/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractSupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * This is a supervised method because after the tree is built, new unknown samples can be placed on it and labels
 * inferred. The unsupervised part, building the tree in the first place, is considered the training phase
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class OnlineHierarchicalClusteringMethod<T extends Clusterable<T>>
		extends AbstractSupervisedOnlineClusteringMethod<T, HierarchicalCentroidCluster<T>>
		implements CentroidClusteringMethod<T>, HierarchicalClusteringMethod<T>
	{// --------------------------- CONSTRUCTORS ---------------------------

	protected OnlineHierarchicalClusteringMethod(final DissimilarityMeasure<T> dm,
	                                             final Set<String> potentialTrainingBins,
	                                             final Map<String, Set<String>> predictLabelSets,
	                                             final ProhibitionModel<T> prohibitionModel,
	                                             final Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidClusteringMethod ---------------------


	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(getClusters(), measure);
		}

	public void computeClusterStdDevs(final ClusterableIterator<T> theDataPointProvider)
		{
		CentroidClusteringUtils.computeClusterStdDevs(getClusters(), measure, getAssignments(), theDataPointProvider);
		}

	@Override
	public String clusteringStats()
		{
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(final OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, outf);
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Returns a LengthWeightHierarchyNode representing the root of the computed clustering tree.  Only valid after
	 * performClustering() has been run.
	 *
	 * @return a LengthWeightHierarchyNode representing the root of the computed clustering tree, or null if the clustering
	 *         procedure has not been performed yet.
	 */
	//public abstract LengthWeightHierarchyNode<CentroidCluster<T>, ? extends LengthWeightHierarchyNode> getTree();
	}
