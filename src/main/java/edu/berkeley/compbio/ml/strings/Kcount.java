/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.stats.DiscreteDistribution1D;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;

public abstract class Kcount<T extends Kcount>
		implements AdditiveClusterable<T>, SequenceSpectrum<T>, DiscreteDistribution1D
	{
	protected int k;
	protected int numberOfBins;
	protected int length;
	protected T parent = null;

	/**
	 * Returns the pattern length K that this Kcount handles (i.e. the number of symbols per word being counted)
	 *
	 * @return the pattern length
	 */
	public int getK()
		{
		return k;
		}

	/**
	 * Returns the length of the sequence that was scanned to produce this Kcount.  This number may be greater than that
	 * given by {@link #getNumberOfSamples()} because every symbol is not necessarily counted as a sample, depending on the
	 * implementation.
	 *
	 * @return the length (type int) of this Kcount object.
	 * @see #addUnknown()
	 */
	public int getLength()
		{
		return length;
		}

	/**
	 * Returns the number of bins, which is the number of possible patterns in this Kcount.  Typically this will the
	 * alphabet size to the power of K.  Assuming a straightforward implementation, this will equal getCounts().size().
	 *
	 * @return The number of bins
	 */
	public int getNumberOfBins()
		{
		return numberOfBins;
		}

	/**
	 * Returns the parent of this Kcount.
	 *
	 * @return the parent of this Kcount.
	 */
	public T getParent()// aggregateUp()
		{
		if (k == 0)
			{
			return null;
			}
		if (parent == null)
			{
			newParent();
			}
		return parent;
		}

	/**
	 * Generates a new Kcount based on this one and stores it as the parent
	 */
	protected abstract void newParent();

	/**
	 * Returns the number of samples, which equals the sum of the counts.
	 *
	 * @return The number of samples
	 */
	public abstract int getNumberOfSamples();

	/**
	 * Adds an "unknown" sample to this kcount, indicating that a character was consumed without incrementing any counter.
	 */
	public void addUnknown()
		{
		//metadata.length++;//incrementLength();
		length++;
		}

	/**
	 * Recursively generalize thisKcount, creating a chain of "parents" until no further generalization is possible
	 */
	protected void ensureAllParentsExist()
		{
		T kc = getParent();
		if (kc != null)
			{
			kc.ensureAllParentsExist();
			}
		}

	public abstract int idForSequence(byte[] seq);

	public abstract byte[] sequenceForId(int i);

	public abstract byte[] prefixForId(int id);

	public abstract int prefixId(int id);

	public abstract int suffixId(int id);

	public abstract byte lastSymbolForId(int id);


	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Clone this object.  Should behave like {@link Object#clone()} except that it returns an appropriate type and so
	 * requires no cast.  Also, we insist that is method be implemented in inheriting classes, so it does not throw
	 * CloneNotSupportedException.
	 *
	 * @return a clone of this instance.
	 * @see Object#clone
	 * @see java.lang.Cloneable
	 */
	public abstract T clone();

	// --------------------- Interface AdditiveClusterable ---------------------

	/**
	 * updates this object by subtracting another one from it.
	 *
	 * @param object the object to subtract from this one
	 */
	public abstract void decrementBy(T object);

	/**
	 * updates this object by adding another one to it.
	 *
	 * @param object the object to add to this one
	 */
	public abstract void incrementBy(T object);

	/**
	 * Returns a new object representing the difference between this one and the given argument.
	 *
	 * @param object the object to be subtracted from this one
	 * @return the difference between this object and the argument
	 */
	public T minus(T object)
		{
		T result = clone();
		result.decrementBy(object);
		//result.getMetadata().setSequenceName("minus result");
		return result;
		}

	/**
	 * Returns a new object representing the sum of this one and the given argument.
	 *
	 * @param object the object to be added to this one
	 * @return the sum of this object and the argument
	 */
	public T plus(T object)
		{
		T result = clone();
		result.incrementBy(object);
		//result.getMetadata().setSequenceName("plus result");
		return result;
		}
	}
