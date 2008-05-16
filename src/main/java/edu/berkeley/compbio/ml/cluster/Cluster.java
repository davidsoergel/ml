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

import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;

/**
 * A cluster, i.e. a grouping of samples, generally learned during a clustering process.  Stores a centroid, an object
 * of the same type as the samples representing the location of the cluster.  Depending on the clustering algorithm, the
 * centroid may or may not be sufficient to describe the cluster (see AdditiveCluster); in the limit, a Cluster subclass
 * may simply store all the samples in the cluster.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public interface Cluster<T extends Clusterable<T>>
	{
	T getCentroid();

	void setCentroid(T centroid);

	int getId();

	void setId(int id);

	int getN();

	//	boolean equals(Cluster<T> other);

	/**
	 * Add the given sample to this cluster.  Does not automatically remove the sample from other clusters of which it is
	 * already a member.
	 *
	 * @param point the sample to add
	 * @return true if the point was successfully added; false otherwise
	 */
	boolean recenterByAdding(T point);

	/**
	 * Remove the given sample from this cluster.
	 *
	 * @param point the sample to remove
	 * @return true if the point was successfully removed; false otherwise (in particular, if the point is not a member of
	 *         this cluster in teh first place)
	 */
	boolean recenterByRemoving(T point);

	void updateLabelProbabilitiesFromCounts();

	void setLabelProbabilities(Multinomial<String> labelProbabilities);

	Multinomial<String> getLabelProbabilities() throws DistributionException;

	double getDominantProbability() throws DistributionException;

	String getDominantLabel();

	void addLabel(T point);

	void removeLabel(T point);
	}
