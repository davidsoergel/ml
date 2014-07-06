/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AbstractCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Cluster;
import org.apache.commons.lang.NotImplementedException;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class KohonenSOMCell<T extends AdditiveClusterable<T>> extends AbstractCentroidCluster<T>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	public KohonenSOMCell(final int id, final T centroid)//DistanceMeasure<T> dm,
		{
		super(id, centroid);//dm
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(final T point)
		{
		// we don't increment n here, because moving the centroid and actually assigning a sample to this cell are two different things
		centroid.incrementBy(point);
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(final Cluster<T> otherCluster)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(final T point)
		{
		centroid.decrementBy(point);


		return true;
		//throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(final Cluster<T> otherCluster)
		{
		throw new NotImplementedException();
		}

// -------------------------- OTHER METHODS --------------------------

	public void recenterByAddingWeighted(final T point, final double motionFactor)
		{
		// REVIEW Note assumption of an additive statistical model for the centroids
		/*		if (!additiveModel)
		   {
		   centroid.multiplyBy(1 - motionFactor);
		   }*/

		// this is slow because point.times() requires an array copy, since we don't want to modify the original
		//centroid.incrementBy(point.times(motionFactor));

		centroid.incrementByWeighted(point, motionFactor);
		}

	public void recenterByRemovingWeighted(final T point, final double motionFactor)
		{
		// REVIEW Note assumption of an additive statistical model for the centroids
		/*		if (!additiveModel)
		   {
		   centroid.multiplyBy(1 - motionFactor);
		   }*/
		centroid.decrementByWeighted(point, motionFactor);
		}
	}
