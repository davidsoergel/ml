/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface UnsupervisedClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
	/**
	 * consider each of the incoming data points exactly once per iteration.  Note iterations > 1 only makes sense for
	 * unsupervised clustering.
	 */
	public void train(ClusterableIteratorFactory<T> trainingCollectionIteratorFactory, int iterations)
			throws ClusterException;
	/*	{
		if (iterations > 1)
			{
			throw new ClusterException(
					"Multiple-iteration training probably doesn't make sense for supervised clustering");
			}

		if (predictLabels == null)
			{
			throw new ClusterException("Must assign a set of labels before training supervised clustering");
			}

		train(trainingCollectionIteratorFactory);
		}*/


	/**
	 * Adds a point to the best cluster.  Generally it's not a good idea to store the point itself in the cluster for
	 * memory reasons; so this method is primarily useful for updating the position of the centroid.
	 *
	 * @param p
	 * @return
	 * @throws ClusterException
	 * @throws NoGoodClusterException
	 */
	boolean add(T p) throws NoGoodClusterException;  //, List<Double> secondBestDistances
	}
