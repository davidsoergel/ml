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

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: lorax Date: Jun 24, 2007 Time: 7:20:33 PM To change this template use File | Settings
 * | File Templates.
 */
public class AdditiveCluster<T extends AdditiveClusterable<T>> extends Cluster<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(AdditiveCluster.class);


	// --------------------------- CONSTRUCTORS ---------------------------

	public AdditiveCluster(DistanceMeasure<T> dm, T centroid)
		{
		super(dm, centroid);
		}

	// -------------------------- OTHER METHODS --------------------------

	public boolean recenterByAdding(T point)//, double distance)
		{
		n++;
		//sumSquareDistances += (distance * distance);  // WRONG
		logger.debug("Cluster added " + point);
		centroid.incrementBy(point);// works because Kcounts are "nonscaling additive", but it's not generic
		//times((double)n/n+1).plus(point.times(1/((double)n+1)));
		return true;
		}

	public boolean recenterByRemoving(T point)//, double distance)
		{
		n--;
		//sumSquareDistances -= (distance * distance);  // WRONG
		logger.debug("Cluster removed " + point);
		centroid.decrementBy(point);// works because Kcounts are "nonscaling additive", but it's not generic
		//times((double)n/n+1).plus(point.times(1/((double)n+1)));
		return true;
		}
	}
