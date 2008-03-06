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

package edu.berkeley.compbio.ml.cluster.kohonen;

import com.davidsoergel.stats.Multinomial;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class KohonenSOMCell<T extends AdditiveClusterable<T>> extends Cluster<T>
	{
	public KohonenSOMCell(DistanceMeasure<T> dm, T centroid)
		{
		super(dm, centroid);
		}

	public boolean recenterByAdding(T point)
		{
		centroid.incrementBy(point);
		labelCounts.add(point.getLabel());
		return true;
		}

	public boolean recenterByRemoving(T point)
		{
		centroid.decrementBy(point);

		// we don't sanity check that the label was present to begin with
		labelCounts.remove(point.getLabel());

		return true;
		//throw new NotImplementedException();
		}

	Bag labelCounts = new HashBag();

	Multinomial<String> labelProbabilities = new Multinomial<String>();

	public Bag getLabelCounts()
		{
		return labelCounts;
		}

	public void setLabelProbabilities(Multinomial<String> labelProbabilities)
		{
		this.labelProbabilities = labelProbabilities;
		}

	public Multinomial<String> getLabelProbabilities()
		{
		return labelProbabilities;
		}

	public double getDominantProbability()
		{
		return labelProbabilities.getDominantProbability();
		}

	public String getDominantLabel()
		{
		return labelProbabilities.getDominantKey();
		}
	}