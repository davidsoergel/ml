/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DistributionException;

import java.io.Serializable;

/**
 * A cluster, i.e. a grouping of samples, generally learned during a clustering process.  Stores a centroid, an object
 * of the same type as the samples representing the location of the cluster.  Depending on the clustering algorithm, the
 * centroid may or may not be sufficient to describe the cluster (see AdditiveCluster); in the limit, a Cluster subclass
 * may simply store all the samples in the cluster.
 * <p/>
 * A Cluster keeps track of the probabilities of labels on the samples within it, assuming that each sample has a single
 * label and these are mutually exclusive.
 * <p/>
 * We let the label probabilities be completely distinct from the actual counts of labels observed on the samples, so
 * that the probabilities can be set based on outside information (e.g., in the case of the Kohonen map, neighboring
 * cells may exert an influence)These probabilities are stored separately from the samples themselves.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface CentroidCluster<T extends Clusterable<T>>
		extends Cluster<T>, Serializable //BAD this is not really Serializable, is it?
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Increments the sum of square distances.  Note there can be no online method of updating the sum of squares, because
	 * the centroid keeps moving
	 */
	void addToSumOfSquareDistances(double v);

	/**
	 * Returns the centroid
	 *
	 * @return the centroid
	 */
	T getCentroid();

//	void setCentroid(T newCentroid);

	/**
	 * Returns the standard deviation of the distances from each sample to the centroid, if this has already been computed.
	 * Can be used as a (crude?) measure of clusteredness, in combination with the distances between the cluster centroids
	 * themselves.  May return an obsolete value if the cluster centroid or members have been altered since the standard
	 * deviation was computed.
	 *
	 * @return the standard deviation of the distances from each sample to the centroid
	 */
	double getStdDev();

	/**
	 * Sets the centroid
	 *
	 * @param centroid the centroid
	 */
//	void setCentroid(T centroid);

	/**
	 * Sets the number of samples in thie cluster
	 *
	 * @param i the n umber of samples in thie cluster
	 */
	//void setN(int i);

	//	boolean equals(Cluster<T> other);

	/**
	 * Sets the weights of String labels.  Generally should be probabilities.  No guarantee of exclusivity.
	 *
	 * @param labelWeights a WeightedSet giving the probabilities of mutually exclusive String labels.
	 */
	//void setDerivedLabelWeights(WeightedSet<String> labelWeights);

	// WeightedSet<String> addWeightedLabels(WeightedSet<String> l);

	/**
	 * Returns the probability of the most probable label
	 *
	 * @return the probability of the most probable label
	 * @throws DistributionException when something goes wrong
	 */
	//	double getDominantProbability() throws DistributionException;

	/**
	 * Returns the most probable label
	 *
	 * @return the most probable label
	 * @throws DistributionException when something goes wrong
	 */
	//	String getDominantExclusiveLabel();


	/**
	 * Add the label on the given sample to the counts.
	 *
	 * @param point the sample whose label to count.
	 */
	//	void addExclusiveLabel(T point);

	/**
	 * Remove the label on the given sample from the counts.
	 *
	 * @param point the sample whose label to remove.
	 */
	//	void removeExclusiveLabel(T point);

	/**
	 * Sets the sum of squared distances from the samples in this cluster to its centroid.  Computed externally in {@see
	 * ClusteringMethod.computeClusterStdDevs}.
	 *
	 * @param i the sumOfSquareDistances
	 */
	void setSumOfSquareDistances(double i);

	//	String getDominantLabelInSet(Set<String> mutuallyExclusiveLabels);
	}
