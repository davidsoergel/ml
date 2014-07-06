/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

/**
 * Interface for classes that can be clustered, and that have the property that the centroid of a set of objects is simply the sum of the objects.  Making objects have this property obviates the need
 * for special centroid-calculation methods and thus makes it easier to write abstract clustering algorithms.  In particular, this makes it possible to store only the centroids of each cluster rather
 * than the list of all members.  That is, additivity (for some concept of "addition") is required for it to be possible to construct clusters in an online manner.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface AdditiveClusterable<T extends AdditiveClusterable> extends Clusterable<T>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * updates this object by subtracting another one from it.
	 *
	 * @param point the object to subtract from this one
	 */
	void decrementBy( T point );

	/**
	 * Updates this object by subtracting another one from it in a weighted manner.  Usually equivalent to decrementBy(point.times(motionFactor)).
	 *
	 * @param point        the object to subtract from this one
	 * @param motionFactor the strength of the desired effect in arbitrary units (in many implementations, this will likely be between 0 and 1)
	 */
	void decrementByWeighted( T point, double motionFactor );

	/**
	 * updates this object by adding another one to it.
	 *
	 * @param point the object to add to this one
	 */
	void incrementBy( T point );

	/**
	 * Updates this object by adding another one to it in a weighted manner.  Usually equivalent to incrementBy(point.times(motionFactor)).
	 *
	 * @param point        the object to add to this one
	 * @param motionFactor the strength of the desired effect in arbitrary units (in many implementations, this will likely be between 0 and 1)
	 */
	void incrementByWeighted( T point, double motionFactor );

	/**
	 * Returns a new object representing the difference between this one and the given argument.
	 *
	 * @param object the object to be subtracted from this one
	 * @return the difference between this object and the argument
	 */
	T minus( T point );

	/**
	 * Multiply this object by the given scalar in place.
	 *
	 * @param v the scalar multiplier
	 */
	void multiplyBy( double v );

	/**
	 * Returns a new object representing the sum of this one and the given argument.
	 *
	 * @param point the object to be added to this one
	 * @return the sum of this object and the argument
	 */
	T plus( T point );

	/**
	 * Returns a new object representing the product of this one and the given scalar.
	 *
	 * @param v the scalar multiplier
	 * @return the product of this object and the argument
	 */
	T times( double v );


	//public T weightedAverage(T object, double weight);

	//void normalize(int n);
	//T weightedMixture( T object, double thisWeight, double thatWeight );
	}
