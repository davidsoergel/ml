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

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractBatchClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.ml.cluster.SupervisedClusteringMethod;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
		extends AbstractBatchClusteringMethod<T, HierarchicalCentroidCluster<T>>
		implements SupervisedClusteringMethod<T>, CentroidClusteringMethod<T>, HierarchicalClusteringMethod<T>
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
