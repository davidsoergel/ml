/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * A clustering method that needs the complete set of samples on hand in order to operate.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
//@Deprecated

public interface BatchClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
//public abstract class BatchClusteringMethod<T extends Clusterable<T>> extends CentroidClusteringMethod<T>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Add the given samples to the set to be clustered.
	 *
	 * @param samples a Collection of Clusterable objects.
	 */
	//void addAll(ClusterableIterator<T> samples);  //? extends Clusterable<T>

	//void add(T sample);

	/**
	 * Add the given samples to the set to be clustered, and remember the mapping from sample to cluster
	 *
	 * @param testIterator
	 */
//	void addAllAndRemember(Iterator<T> testIterator);

	void createClusters();

/*
	protected BatchClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                Set<String> predictLabels, Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		//	this.leaveOneOut = leaveOneOut;
		}
	// different implementations may prefer different data structures
	//Collection<Clusterable<T>> samples;
*/
	/**
	 * Recompute a set of clusters from the stored samples.
	 */
//	public abstract void performClustering();

	/**
	 * Add the given samples to the set to be clustered.
	 *
	 * @param samples a Collection of Clusterable objects.
	 */
//	public abstract void addAll(Collection<Clusterable<T>> samples);


	/**
	 * Recompute a set of clusters from the stored samples.
	 */
	void train();
	}
