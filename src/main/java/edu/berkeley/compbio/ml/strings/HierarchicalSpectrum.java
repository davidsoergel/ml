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

package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.dsutils.AbstractGenericFactoryAware;
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public abstract class HierarchicalSpectrum<T extends HierarchicalSpectrum> extends AbstractGenericFactoryAware
		implements SequenceSpectrum<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(HierarchicalSpectrum.class);

	protected T parent = null;


	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Returns the parent of this Kcount.
	 *
	 * @return the parent of this Kcount.
	 */
	@Nullable
	public T getParent()//throws SequenceSpectrumException// aggregateUp()
		{
		if (!hasParent())
			{
			return null;
			}
		if (parent == null)
			{
			try
				{
				newParent();
				}
			catch (SequenceSpectrumException e)
				{
				throw new SequenceSpectrumRuntimeException(e);
				}
			}
		return parent;
		}

	public abstract boolean hasParent();

	/**
	 * Generates a new Kcount based on this one and stores it as the parent
	 */
	protected abstract void newParent() throws SequenceSpectrumException;//throws SequenceSpectrumException;


	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Clone this object.  Should behave like {@link Object#clone()} except that it returns an appropriate type and so
	 * requires no cast.  Also, we insist that is method be implemented in inheriting classes, so it does not throw
	 * CloneNotSupportedException.
	 *
	 * @return a clone of this instance.
	 * @see Object#clone
	 * @see Cloneable
	 */
	@Override
	public abstract T clone();


	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Recursively generalize thisKcount, creating a chain of "parents" until no further generalization is possible
	 */
	protected void ensureAllParentsExist() throws SequenceSpectrumException
		{
		T kc = getParent();
		if (kc != null)
			{
			kc.ensureAllParentsExist();
			}
		}

	public List<HierarchicalSpectrum<T>> getAncestryList()
		{
		List<HierarchicalSpectrum<T>> result;
		if (parent == null)
			{
			result = new ArrayList<HierarchicalSpectrum<T>>();
			}
		else
			{
			result = parent.getAncestryList();
			}
		result.add(this);
		return result;
		}


	private WeightedSet<String> weightedLabels = new HashWeightedSet<String>();

	public WeightedSet<String> getWeightedLabels()
		{
		return weightedLabels;
		}

	protected String label;

	public String getExclusiveLabel()
		{
		return label;
		}

	public void setLabel(String label)
		{
		this.label = label;
		}
	}
