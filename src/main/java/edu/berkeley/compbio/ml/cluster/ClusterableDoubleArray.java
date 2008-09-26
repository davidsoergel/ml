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

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.stats.DoubleArrayContainer;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class ClusterableDoubleArray implements AdditiveClusterable<ClusterableDoubleArray>, DoubleArrayContainer
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(ClusterableDoubleArray.class);

	protected String id;
	protected double[] data;

	private String label;

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
		{
		return label;
		}

	public void setLabel(String label)
		{
		this.label = label;
		}

	// --------------------------- CONSTRUCTORS ---------------------------

	public ClusterableDoubleArray()
		{
		}

	public ClusterableDoubleArray(String id, double[] data)
		{
		this.id = id;
		this.data = data;
		}

	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterableDoubleArray clone()
		{
		return new ClusterableDoubleArray(id,
		                                  data.clone());//To change body of overridden methods use File | Settings | File Templates.
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface AdditiveClusterable ---------------------

	/**
	 * {@inheritDoc}
	 */
	public void decrementBy(ClusterableDoubleArray object)
		{
		//data = ArrayUtils.minus(data, object.data);
		DSArrayUtils.decrementBy(data, object.data);
		}

	/**
	 * {@inheritDoc}
	 */
	public void incrementBy(ClusterableDoubleArray object)
		{
		//data = ArrayUtils.plus(data, object.data);
		DSArrayUtils.incrementBy(data, object.data);
		}


	/**
	 * {@inheritDoc}
	 */
	public void decrementByWeighted(ClusterableDoubleArray object, double weight)
		{
		//data = ArrayUtils.minus(data, object.data);
		DSArrayUtils.decrementByWeighted(data, object.data, weight);
		}

	/**
	 * {@inheritDoc}
	 */
	public void incrementByWeighted(ClusterableDoubleArray object, double weight)
		{
		//data = ArrayUtils.plus(data, object.data);
		DSArrayUtils.incrementByWeighted(data, object.data, weight);
		}

	/**
	 * {@inheritDoc}
	 */
	public void multiplyBy(double scalar)
		{
		//data = ArrayUtils.times(data, scalar);
		DSArrayUtils.multiplyBy(data, scalar);
		}

	/**
	 * {@inheritDoc}
	 */
	public ClusterableDoubleArray minus(ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), DSArrayUtils.minus(data, object.data));
		}

	/**
	 * {@inheritDoc}
	 */
	public ClusterableDoubleArray plus(ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), DSArrayUtils.plus(data, object.data));
		}

	/**
	 * {@inheritDoc}
	 */
	public ClusterableDoubleArray times(double scalar)
		{
		return new ClusterableDoubleArray(id + "*" + scalar, DSArrayUtils.times(data, scalar));
		}

	// --------------------- Interface Clusterable ---------------------

	/**
	 * {@inheritDoc}
	 */
	public boolean equalValue(ClusterableDoubleArray object)
		{
		return id.equals(object.id) && DSArrayUtils.equalWithinFPError(data, object.data);
		}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
		{
		return null;
		}

	// --------------------- Interface DoubleArrayContainer ---------------------


	/**
	 * {@inheritDoc}
	 */
	public double[] getArray()
		{
		return data;
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

	private WeightedSet<String> weightedLabels = new HashWeightedSet<String>();

	public WeightedSet<String> getWeightedLabels()
		{
		return weightedLabels;
		}
	}
