/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * A Cluster that explicitly stores the set of samples it contains.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BatchAdditiveCentroidCluster<T extends AdditiveClusterable<T>> extends AdditiveCentroidCluster<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(BatchAdditiveCentroidCluster.class);

	/**
	 * The set of samples contained in this cluster.
	 */
	private final Set<T> thePoints = new HashSet<T>();


// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new Cluster with the given DistanceMeasure and centroid.  Note the centroid may be modified in the
	 * course of running a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e., it's
	 * probably best to clone it first).
	 *
	 * @param centroid the T
	 */
	public BatchAdditiveCentroidCluster(final int id, final T centroid) throws CloneNotSupportedException
		{
		super(id, centroid);
		}

// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
		{
		final StringBuffer sb = new StringBuffer("\nCluster:");
		sb.append(" ").append(centroid).append("\n");
		for (final T t : thePoints)
			{
			sb.append(" ").append(t).append("\n");
			}
		return sb.toString();
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------

	/*	public boolean recalculateCentroid() throws ClusterException
		 {
		 // REVIEW is recalculateCentroid done somewhere else now??
		 // works because Kcounts are "nonscaling additive", but it's not generic

		 assert thePoints.size() > 0;
		 Iterator<T> i = thePoints.iterator();
		 T sum = i.next();
		 while (i.hasNext())
			 {
			 sum = sum.plus(i.next());
			 }
		 if (centroid.equalValue(sum))
			 {
			 return false;
			 }
		 centroid = sum;
		 return true;
		 }
*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean add(final T point)
		{
		if (thePoints.add(point))
			{
			super.add(point);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(final Cluster<T> otherCluster)
		{
		if (thePoints.addAll(((BatchAdditiveCentroidCluster<T>) otherCluster).getPoints()))
			{
			super.addAll(otherCluster);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(final T point)
		{
		if (thePoints.remove(point))
			{
			super.remove(point);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(final Cluster<T> otherCluster)
		{
		if (thePoints.removeAll(((BatchAdditiveCentroidCluster<T>) otherCluster).getPoints()))
			{
			super.removeAll(otherCluster);
			return true;
			}
		return false;
		}

// -------------------------- OTHER METHODS --------------------------

	public Set<T> getPoints()
		{
		return thePoints;
		}
	}
