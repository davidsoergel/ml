package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.MutableWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface Cluster<T extends Clusterable<T>>// extends Comparable<? extends Cluster<T>>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Add the given sample to this cluster.  Does not automatically remove the sample from other clusters of which it is
	 * already a member.
	 *
	 * @param point the sample to add
	 * @return true if the point was successfully added; false otherwise
	 */
	boolean add(T point);

	/**
	 * Add all the samples in the given cluster to this cluster.  Does not automatically remove the samples from other
	 * clusters of which they are already members.
	 *
	 * @param point the sample to add
	 * @return true if the point was successfully added; false otherwise
	 */
	boolean addAll(Cluster<T> point);

	/**
	 * Recomputes the probabilities of labels, based on the actual labels observed in the contained samples.  This must be
	 * called explicitly to avoid unnecessary recomputation on every sample addition.
	 */
	//	void updateExclusiveLabelProbabilitiesFromCounts();

	/**
	 * Gets the probabilities of String labels.  The labels need not be mutually exclusive, so the weights need not sum to
	 * one.
	 */
	WeightedSet<String> getDerivedLabelProbabilities();

	/**
	 * Returns the id
	 *
	 * @return the id
	 */
	int getId();

	/**
	 * Returns the number of samples in thie cluster
	 *
	 * @return the number of samples in thie cluster
	 */
	int getN();

	/**
	 * Gets the probabilities of mutually exclusive String labels.
	 *
	 * @return a Multinomial giving the probabilities of mutually exclusive String labels.
	 */
	MutableWeightedSet<String> getMutableWeightedLabels();// throws DistributionException;

	WeightedSet<String> getImmutableWeightedLabels();

	/**
	 * Remove the given sample from this cluster.
	 *
	 * @param point the sample to remove
	 * @return true if the point was successfully removed; false otherwise (in particular, if the point is not a member of
	 *         this cluster in the first place)
	 */
	boolean remove(T point);

	/**
	 * Remove all the samples in the given cluster from this cluster.
	 *
	 * @param point the sample to add
	 * @return true if the point was successfully added; false otherwise
	 */
	boolean removeAll(Cluster<T> point);

	/**
	 * Sets the probabilities of String labels.  The labels need not be mutually exclusive, so the weights need not sum to
	 * one.
	 *
	 * @param derivedLabelProbabilities a WeightedSet giving the probabilities of String labels.
	 */
	void setDerivedLabelProbabilities(WeightedSet<String> derivedLabelProbabilities);

	/**
	 * Sets the integer id of this cluster.  These ids should be unique within a given clustering or classification
	 * process.
	 *
	 * @param id the integer id of this cluster.
	 */
//	void setId(int id);

	/**
	 * Copy the local label weights into the derived label weights.
	 */
	void updateDerivedWeightedLabelsFromLocal();
	}
