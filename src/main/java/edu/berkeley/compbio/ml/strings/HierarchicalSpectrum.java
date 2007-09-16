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

import java.util.ArrayList;
import java.util.List;

public abstract class HierarchicalSpectrum<T extends HierarchicalSpectrum> implements SequenceSpectrum<T>
	{
	protected T parent = null;

	/**
	 * Returns the parent of this Kcount.
	 *
	 * @return the parent of this Kcount.
	 */
	public T getParent()//throws SequenceSpectrumException// aggregateUp()
		{
		if (!hasParent())
			{
			return null;
			}
		if (parent == null)
			{
			newParent();
			}
		return parent;
		}

	public List<HierarchicalSpectrum> getAncestryList()
		{
		List<HierarchicalSpectrum> result;
		if (parent == null)
			{
			result = new ArrayList<HierarchicalSpectrum>();
			}
		else
			{
			result = parent.getAncestryList();
			}
		result.add(this);
		return result;
		}

	/**
	 * Generates a new Kcount based on this one and stores it as the parent
	 */
	protected abstract void newParent();//throws SequenceSpectrumException;

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

	public abstract boolean hasParent();

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

	}
