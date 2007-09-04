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

import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.stats.DoubleArrayContainer;
import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
public class ClusterableDoubleArray implements AdditiveClusterable<ClusterableDoubleArray>, DoubleArrayContainer
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(ClusterableDoubleArray.class);

	protected String id;
	protected double[] data;


	// --------------------------- CONSTRUCTORS ---------------------------

	public ClusterableDoubleArray(String id, double[] data)
		{
		this.id = id;
		this.data = data;
		}

	public ClusterableDoubleArray()
		{
		}

	// ------------------------ CANONICAL METHODS ------------------------

	public ClusterableDoubleArray clone()
		{
		return new ClusterableDoubleArray(id,
		                                  data.clone());//To change body of overridden methods use File | Settings | File Templates.
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface AdditiveClusterable ---------------------

	public void decrementBy(ClusterableDoubleArray object)
		{
		data = ArrayUtils.minus(data, object.data);
		}

	public void incrementBy(ClusterableDoubleArray object)
		{
		data = ArrayUtils.plus(data, object.data);
		}

	public ClusterableDoubleArray minus(ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), ArrayUtils.minus(data, object.data));
		}

	public ClusterableDoubleArray plus(ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), ArrayUtils.plus(data, object.data));
		}

	// --------------------- Interface Clusterable ---------------------

	public boolean equalValue(ClusterableDoubleArray object)
		{
		return id.equals(object.id) && ArrayUtils.equalWithinFPError(data, object.data);
		}

	public String getId()
		{
		return null;
		}

	// -------------------------- OTHER METHODS --------------------------

	public double get(int i)
		{
		return data[i];
		}

	public int length()
		{
		return data.length;
		}

	public double[] getArray()
		{
		return data;
		}
	}
