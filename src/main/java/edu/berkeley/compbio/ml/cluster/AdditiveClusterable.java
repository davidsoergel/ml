/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package edu.berkeley.compbio.ml.cluster;

/**
 * Interface for classes that can be clustered, and that have the property that the centroid of a set of objects is
 * simply the sum of the objects.  Making objects have this property obviates the need for special centroid-calculation
 * methods and thus makes it easier to write abstract clustering algorithms.  In particular, this makes it possible to
 * store only the centroids of each cluster rather than the list of all members.  That is, additivity (for some concept
 * of "addition") is required for it to be possible to construct clusters in an online manner.
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public interface AdditiveClusterable<T extends AdditiveClusterable> extends Clusterable<T>
	{
	// -------------------------- OTHER METHODS --------------------------

	/**
	 * updates this object by subtracting another one from it.
	 *
	 * @param point the object to subtract from this one
	 */
	void decrementBy(T point);

	/**
	 * updates this object by adding another one to it.
	 *
	 * @param point the object to add to this one
	 */
	void incrementBy(T point);

	/**
	 * Returns a new object representing the difference between this one and the given argument.
	 *
	 * @param object the object to be subtracted from this one
	 * @return the difference between this object and the argument
	 */
	T minus(T point);

	/**
	 * Returns a new object representing the sum of this one and the given argument.
	 *
	 * @param point the object to be added to this one
	 * @return the sum of this object and the argument
	 */
	T plus(T point);

	/**
	 * Returns a new object representing the product of this one and the given scalar.
	 *
	 * @param v the scalar multiplier
	 * @return the product of this object and the argument
	 */
	T times(double v);

	/**
	 * Multiply this object by the given scalar in place.
	 *
	 * @param v the scalar multiplier
	 */
	void multiplyBy(double v);

	/**
	 * Updates this object by adding another one to it in a weighted manner.  Usually equivalent to incrementBy(point.times(motionFactor)).
	 *
	 * @param point the object to add to this one
	 * @param motionFactor the strength of the desired effect in arbitrary units (in many implementations, this will likely be between 0 and 1)
	 */
	void incrementByWeighted(T point, double motionFactor);

	/**
	 * Updates this object by subtracting another one from it in a weighted manner.  Usually equivalent to decrementBy(point.times(motionFactor)).
	 *
	 * @param point the object to subtract from this one
	 * @param motionFactor the strength of the desired effect in arbitrary units (in many implementations, this will likely be between 0 and 1)
	 */
	void decrementByWeighted(T point, double motionFactor);


	//public T weightedAverage(T object, double weight);

	//void normalize(int n);
	}
