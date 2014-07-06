/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.LabellableImpl;
import com.davidsoergel.stats.DoubleArrayContainer;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class ClusterableDoubleArray extends LabellableImpl<String>
		implements AdditiveClusterable<ClusterableDoubleArray>, DoubleArrayContainer
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(ClusterableDoubleArray.class);

	protected String id;
	protected double[] data;
	protected Double dataSum;

	private String label;

	//private final MutableWeightedSet<String> weightedLabels = new ConcurrentHashWeightedSet<String>();


// --------------------------- CONSTRUCTORS ---------------------------

	public ClusterableDoubleArray()
		{
		}

	public ClusterableDoubleArray(final String id, final double[] data)
		{
		this.id = id;
		this.data = data;
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	public void setLabel(final String label)
		{
		this.label = label;
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
	public void decrementBy(final ClusterableDoubleArray object)
		{
		//data = ArrayUtils.minus(data, object.data);
		DSArrayUtils.decrementBy(data, object.data);
		dataSum = null;
		}

	/**
	 * {@inheritDoc}
	 */
	public void decrementByWeighted(final ClusterableDoubleArray object, final double weight)
		{
		//data = ArrayUtils.minus(data, object.data);
		DSArrayUtils.decrementByWeighted(data, object.data, weight);
		dataSum = null;
		}

	/**
	 * {@inheritDoc}
	 */
	public void incrementBy(final ClusterableDoubleArray object)
		{
		//data = ArrayUtils.plus(data, object.data);
		DSArrayUtils.incrementBy(data, object.data);
		dataSum = null;
		}

	/**
	 * {@inheritDoc}
	 */
	public void incrementByWeighted(final ClusterableDoubleArray object, final double weight)
		{
		//data = ArrayUtils.plus(data, object.data);
		DSArrayUtils.incrementByWeighted(data, object.data, weight);
		dataSum = null;
		}

	/**
	 * {@inheritDoc}
	 */
	public ClusterableDoubleArray minus(final ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), DSArrayUtils.minus(data, object.data));
		}

	/**
	 * {@inheritDoc}
	 */
	public void multiplyBy(final double scalar)
		{
		//data = ArrayUtils.times(data, scalar);
		DSArrayUtils.multiplyBy(data, scalar);
		dataSum = null;  // we could multiply it by that might be less numerically precise...??
		}

	/**
	 * {@inheritDoc}
	 */
	public ClusterableDoubleArray plus(final ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), DSArrayUtils.plus(data, object.data));
		}

	/**
	 * {@inheritDoc}
	 */
	public ClusterableDoubleArray times(final double scalar)
		{
		return new ClusterableDoubleArray(id + "*" + scalar, DSArrayUtils.times(data, scalar));
		}

// --------------------- Interface Clusterable ---------------------

	/**
	 * {@inheritDoc}
	 */
	public boolean equalValue(final ClusterableDoubleArray object)
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

	public double getArraySum()
		{
		if (dataSum == null)
			{
			dataSum = DSArrayUtils.sum(data);
			}
		return dataSum;
		}

// -------------------------- OTHER METHODS --------------------------

	public double get(final int i)
		{
		return data[i];
		}

	/**
	 * {@inheritDoc}
	 */
	public String getExclusiveLabel()
		{
		return label;
		}

	public String getSourceId()
		{
		throw new NotImplementedException();
		}

	public int length()
		{
		return data.length;
		}
	}
