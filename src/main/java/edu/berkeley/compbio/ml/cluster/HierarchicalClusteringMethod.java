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

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.phyloutils.LengthWeightHierarchyNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * Represents a hierarchical clustering algorithm.  In general an algorithm will group Clusterable samples into a tree
 * of LengthWeightHierarchyNodes, with a corresponding Cluster for each.  Note that this produces overlapping Clusters,
 * as opposed to the usual assumption in the ClusterMethod superclass that the Clusters are discrete.
 * <p/>
 * This interface is agnostic about whether the implementation is supervised or unsupervised, and whether it is online
 * or batch.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public abstract class HierarchicalClusteringMethod<T extends Clusterable<T>>
		extends AbstractBatchClusteringMethod<T, CentroidCluster<T>>
		implements CentroidClusteringMethod<T>, SemisupervisedClusteringMethod<T>
	{


	protected HierarchicalClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                       Set<String> predictLabels, Set<String> leaveOneOutLabels,
	                                       Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		}

	/**
	 * Returns a LengthWeightHierarchyNode representing the root of the computed clustering tree.  Only valid after
	 * performClustering() has been run.
	 *
	 * @return a LengthWeightHierarchyNode representing the root of the computed clustering tree, or null if the clustering
	 *         procedure has not been performed yet.
	 */
	public abstract LengthWeightHierarchyNode<CentroidCluster<T>, ? extends LengthWeightHierarchyNode> getTree();

	public void computeClusterStdDevs(ClusterableIterator<T> theDataPointProvider) throws IOException
		{
		CentroidClusteringUtils.computeClusterStdDevs(theClusters, measure, assignments, theDataPointProvider);
		}

	public void writeClusteringStatsToStream(OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, outf);
		}

	@Override
	public String clusteringStats()
		{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, b);
		return b.toString();
		}

	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(theClusters, measure);
		}
	}
