/* $Id$ */

/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class BatchKmeansClustering<T extends AdditiveClusterable<T>> extends BatchClustering<T>
	{
	/*	private static Logger logger = Logger.getLogger(BatchKmeansClustering.class);

	 private int k;

	 public BatchKmeansClustering(Set<T> dataPointSet)
		 {
		 super(dataPointSet);
		 }

	 public BatchKmeansClustering(Set<T> dataPointSet, int k, DistanceMeasure<T> dm)
		 {
		 super(dataPointSet);
		 this.k = k;

		 for (int i = 0; i < k; i++)
			 {
			 Cluster<T> c = new Cluster<T>(dm, da.next()); // initialize the clusters with the first k points
			 theClusters.add(c);
			 }
		 }


	 public boolean batchUpdate() throws ClusterException
		 {
		 List<Cluster<T>> oldClusters = theClusters;
		 theClusters = new ArrayList<Cluster<T>>();

		 // what if a cluster ends up empty??

		 for (Cluster<T> c : oldClusters)
			 {
			 Cluster<T> n = new Cluster<T>(c.getTheDistanceMeasure(), c.getCentroid());
			 logger.debug(theClusters.add(n));  // this just fails when theClusters is a HashSet. ???
			 assert theClusters.contains(n);
			 //c.clear();
			 //new Cluster<T>(c.getTheDistanceMeasure(), c.getCentroid()));
			 }
		 for (Cluster<T> o : oldClusters)
			 {
			 for (T point : o)
				 {
				 bestCluster(theClusters, point).add(point);
				 }
			 }
		 boolean changed = false;
		 for (Cluster<T> o : theClusters)
			 {
			 changed = changed || o.recalculateCentroid();
			 }
		 return changed;
		 }
 */
	}
