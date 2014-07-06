/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.LabellableImpl;
import com.davidsoergel.dsutils.collections.ImmutableHashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;

import java.io.Serializable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AbstractCluster<T extends Clusterable<T>> extends LabellableImpl<String>
		implements Cluster<T>, Serializable
	{
// ------------------------------ FIELDS ------------------------------

	/**
	 * The unique integer identifier of this cluster
	 */
	protected final int id;
//	protected final MutableWeightedSet<String> weightedLabels = new ConcurrentHashWeightedSet<String>();


	/**
	 * we let the label probabilities be completely distinct from the local weights themselves, so that the probabilities
	 * can be set based on outside information (e.g., in the case of the Kohonen map, neighboring cells may exert an
	 * influence)
	 */
	private WeightedSet<String> derivedLabelProbabilities = null; //new ImmutableHashWeightedSet<String>();


// --------------------------- CONSTRUCTORS ---------------------------

	public AbstractCluster(final int id)
		{
		this.id = id;
		getMutableWeightedLabels(); // initialize it
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * {@inheritDoc}
	 */
	public WeightedSet<String> getDerivedLabelProbabilities()//throws DistributionException
		{
		return derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setDerivedLabelProbabilities(final ImmutableHashWeightedSet<String> derivedLabelProbabilities)
		{
		this.derivedLabelProbabilities = derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public int getId()
		{
		return id;
		}

	/**
	 * {@inheritDoc}
	 */
/*	public void setId(final int id)
		{
		this.id = id;
		}
*/

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------

	/**
	 * {@inheritDoc}
	 */
	public int getN()
		{
        //Since we are carrying a WeightedSet around for the labels anyway, just reuse its itemCount as the cluster weight
		return getItemCount(); //getMutableWeightedLabels().getItemCount();
		}

	/**
	 * {@inheritDoc}
	 */
	public void updateDerivedWeightedLabelsFromLocal()//throws DistributionException
		{
		//assert !weightedLabels.isEmpty();
		derivedLabelProbabilities =
				new ImmutableHashWeightedSet<String>(getImmutableWeightedLabels()); //mutableWeightedLabels);
		//derivedLabelProbabilities.addAll(weightedLabels);
		//derivedLabelProbabilities.normalize();  // don't bother, it'll be done on request anyway
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(final T point)
		{
		mutableWeightedLabels.addAll(point.getImmutableWeightedLabels());
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(final Cluster<T> otherCluster)
		{
		mutableWeightedLabels.addAll(otherCluster.getImmutableWeightedLabels());
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(final T point)
		{
		mutableWeightedLabels.removeAll(point.getImmutableWeightedLabels());
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAll(final Cluster<T> otherCluster)
		{
		mutableWeightedLabels.removeAll(otherCluster.getImmutableWeightedLabels());
		return true;
		}

// -------------------------- OTHER METHODS --------------------------

	public int compareTo(final Cluster<T> o)
		{
		return id - o.getId();
		}
	}
