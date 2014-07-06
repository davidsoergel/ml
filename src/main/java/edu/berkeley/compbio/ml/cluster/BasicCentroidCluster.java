/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import org.apache.commons.lang.NotImplementedException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BasicCentroidCluster<T extends Clusterable<T>> extends AbstractCentroidCluster<T>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new Cluster with the given id and centroid.  Note the centroid may be modified in the course of running
	 * a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e., it's probably best to
	 * clone it first).
	 *
	 * @param id       an int uniquely identifying this cluster
	 * @param centroid the T
	 */
	public BasicCentroidCluster(final int id, final T centroid)
		{
		super(id, centroid);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------

	// we can't support adding and removing points because we don't know how to modify the centroid if it's not additive

	/**
	 * {@inheritDoc}
	 */
	public boolean add(final T point)
		{
		throw new NotImplementedException();
		//return false;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(final T point)
		{
		throw new NotImplementedException();
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(final CentroidCluster<T> point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAll(final CentroidCluster<T> point)
		{
		throw new NotImplementedException();
		}
	}
