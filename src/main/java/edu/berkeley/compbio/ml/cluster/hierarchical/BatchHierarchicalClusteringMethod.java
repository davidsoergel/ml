/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.DistanceMatrixBatchClusteringMethod;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.ml.cluster.SupervisedClusteringMethod;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Represents a hierarchical clustering algorithm.  In general an algorithm will group Clusterable samples into a tree
 * of LengthWeightHierarchyNodes, with a corresponding Cluster for each.  Note that this produces overlapping Clusters,
 * as opposed to the usual assumption in the ClusterMethod superclass that the Clusters are discrete.
 * <p/>
 * This interface is agnostic about whether the implementation is supervised or unsupervised, and whether it is online
 * or batch.  Oops that's no longer true; rethink...
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public abstract class BatchHierarchicalClusteringMethod<T extends Clusterable<T>>
		extends AbstractClusteringMethod<T, HierarchicalCentroidCluster<T>>
		implements DistanceMatrixBatchClusteringMethod<T>,
		//extends AbstractBatchClusteringMethod<T, HierarchicalCentroidCluster<T>>
		           SupervisedClusteringMethod<T>, CentroidClusteringMethod<T>, HierarchicalClusteringMethod<T>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	protected BatchHierarchicalClusteringMethod(final DissimilarityMeasure<T> dm,
	                                            final Set<String> potentialTrainingBins,
	                                            final Map<String, Set<String>> predictLabelSets,
	                                            final ProhibitionModel<T> prohibitionModel,
	                                            final Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		}

	public BatchHierarchicalClusteringMethod(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                         final Map<String, Set<String>> predictLabelSets,
	                                         final ProhibitionModel<T> tProhibitionModel, final Set<String> testLabels,
	                                         final ArrayList<HierarchicalCentroidCluster<T>> theClusters,
	                                         final Map<String, HierarchicalCentroidCluster<T>> assignments, final int n)
		{
		super(dm, potentialTrainingBins, predictLabelSets, tProhibitionModel, testLabels, theClusters, assignments, n);
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
	}
