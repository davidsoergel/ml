/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Explicitly stores all the points in a cluster.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: BatchCluster.java 312 2008-11-04 01:40:15Z soergel $
 */
public class BasicBatchCluster<T extends Clusterable<T>> extends AbstractCluster<T>
		implements //Comparable<BasicBatchCluster<T>>,
		BatchCluster<T, BasicBatchCluster<T>>
	{
// ------------------------------ FIELDS ------------------------------

	/**
	 * The set of samples contained in this cluster.
	 */
	private SortedSet<T> thePoints = new TreeSet<T>();


// --------------------------- CONSTRUCTORS ---------------------------

	public BasicBatchCluster(final int id)
		{
		super(id);
		}

// ------------------------ CANONICAL METHODS ------------------------

	public synchronized String toString()
		{
		return "BatchCluster containing " + thePoints.size() + " points";
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------


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
	public synchronized boolean addAll(final Cluster<T> otherCluster)
		{
		if (thePoints.addAll(((BatchCluster<T, BasicBatchCluster<T>>) otherCluster).getPoints()))
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
	public synchronized boolean remove(final T point)
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
	public synchronized boolean removeAll(final Cluster<T> otherCluster)
		{
		if (thePoints.removeAll(((BatchCluster<T, BasicBatchCluster<T>>) otherCluster).getPoints()))
			{
			super.removeAll(otherCluster);
			return true;
			}
		return false;
		}

// --------------------- Interface Comparable ---------------------

	public int compareTo(final BasicBatchCluster<T> o)
		{
		return id - o.getId();
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * drop the references to the training examples, but don't forget the label distribution
	 */
	public synchronized void forgetExamples()
		{
		thePoints = new TreeSet<T>();
		}

	public synchronized SortedSet<T> getPoints()
		{
		return thePoints;
		}
	}
