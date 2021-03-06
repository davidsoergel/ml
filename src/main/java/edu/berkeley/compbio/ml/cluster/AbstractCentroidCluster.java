/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Formatter;


/**
 * A cluster, i.e. a grouping of samples, generally learned during a clustering process.  Stores a centroid, an object
 * of the same type as the samples representing the location of the cluster.  Depending on the clustering algorithm, the
 * centroid may or may not be sufficient to describe the cluster (see AdditiveCluster); in the limit, a Cluster subclass
 * may simply store all the samples in the cluster.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractCentroidCluster<T extends Clusterable<T>> extends AbstractCluster<T>
		implements CentroidCluster<T>, Serializable
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractCentroidCluster.class);


	/**
	 * Field centroid
	 */
	protected  T centroid;
/*	public void setCentroid( final T centroid )
		{
		this.centroid = centroid;
		}*/
	/**
	 * The sum of the squared distances from samples in this cluster to the centroid
	 */
	protected double sumOfSquareDistances = 0;


// --------------------------- CONSTRUCTORS ---------------------------


	/**
	 * Constructs a new Cluster with the given id and centroid.  Note the centroid may be modified in the course of running
	 * a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e., it's probably best to
	 * clone it first).
	 *
	 * @param id       an integer uniquely identifying this cluster
	 * @param centroid the T
	 */
	public AbstractCentroidCluster(final int id, final T centroid)//DistanceMeasure<T> dm
		{
		super(id);
		this.centroid = centroid;//.clone();
		if (centroid != null)
			{
			centroid.doneLabelling();
			mutableWeightedLabels.addAll(centroid.getImmutableWeightedLabels());
			}
		//n++;
		//add(centroid);
		logger.debug("Created cluster with centroid: " + centroid);
		//theDistanceMeasure = dm;
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * {@inheritDoc}
	 */
	//@NotNull
	public T getCentroid()
		{
		return centroid;
		}

	/**
	 * {@inheritDoc}
	 */
/*	public void setCentroid(@NotNull final T centroid)
		{
		this.centroid = centroid;
		}
*/

	/**
	 * {@inheritDoc}
	 */
	public void setSumOfSquareDistances(final double v)
		{
		sumOfSquareDistances = v;
		}

// ------------------------ CANONICAL METHODS ------------------------


/*	@Override
	public boolean equals(final Object other)
		{
		// don't worry about matching the generic type; centroid.equals will take care of that
		if (other instanceof AbstractCentroidCluster)
			{
			//	if (logger.isDebugEnabled())
			//   {
			//   logger.debug("" + this + " equals " + other + ": " + result);
			 //  }
			T otherCentroid = ((AbstractCentroidCluster<T>) other).getCentroid();

			if ((centroid == null && otherCentroid != null) || centroid != null && otherCentroid == null)
				{
				return false;
				}

			// now either both centroids are null, or neither are

			return ((centroid == null && otherCentroid == null) || centroid.equals(otherCentroid))
				   // && theDistanceMeasure.equals(other.getTheDistanceMeasure())
				   && super.equals(other);
			}
		return false;
		}

	@Override
	public int hashCode()
		{
		int result = super.hashCode();
		result = 31 * result + (centroid != null ? centroid.hashCode() : 0);
		return result;
		}
*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
		{
		final Formatter f = new Formatter();
		f.format("[Cluster %d] n=%d sd=%.2f", id, getN(), getStdDev());

		return f.out().toString();
		}

	/**
	 * {@inheritDoc}
	 */
	public double getStdDev()
		{
		return Math.sqrt(sumOfSquareDistances / getN());
		}

// ------------------------ INTERFACE METHODS ------------------------


	/**
	 * {@inheritDoc}
	 */
	public void addToSumOfSquareDistances(final double v)
		{
		sumOfSquareDistances += v;
		}
	}
