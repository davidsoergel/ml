/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

/**
 * A cluster whose centroid can be moved by adding or removing individual samples.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AdditiveCentroidCluster<T extends AdditiveClusterable<T>> extends AbstractCentroidCluster<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AdditiveCentroidCluster.class);


// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new AdditiveCluster with the given DistanceMeasure and centroid.  Note the centroid may be modified in
	 * the course of running a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e.,
	 * it's probably best to clone it first).
	 *
	 * @param centroid the T
	 */
	public AdditiveCentroidCluster(final int id, final T centroid)//DistanceMeasure<T> dm,
		{
		super(id, centroid);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean add(final T point)
		{
		super.add(point);
		centroid.incrementBy(point);
		return true;
		}

	@Override
	public boolean addAll(final Cluster<T> otherCluster)
		{
		super.addAll(otherCluster);
		/*
		for(T point : otherCluster.getPoints())
			{
			recenterByAdding(point;)
			}
			*/

		final int otherN = otherCluster.getN();

		T otherCentroid = ((CentroidCluster<T>) otherCluster).getCentroid();
		if(centroid == null)
			{
			centroid = otherCentroid;
			}
		else {
			centroid.incrementByWeighted(otherCentroid, otherN / (otherN + getN()));
		}
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(final T point)
		{
		super.remove(point);
		centroid.decrementBy(point);
		return true;
		}

	@Override
	public boolean removeAll(final Cluster<T> otherCluster)
		{
		super.removeAll(otherCluster);
		/*
		for(T point : otherCluster.getPoints())
			{
			recenterByAdding(point;)
			}
			*/

		final int otherN = otherCluster.getN();
		centroid.decrementByWeighted(((CentroidCluster<T>) otherCluster).getCentroid(), otherN / (otherN + getN()));
		logger.debug("Cluster removed " + otherCluster);
		return true;
		}
	}
