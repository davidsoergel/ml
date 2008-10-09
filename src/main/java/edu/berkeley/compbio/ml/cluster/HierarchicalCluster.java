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

import com.davidsoergel.dsutils.collections.WeightedSet;
import edu.berkeley.compbio.phyloutils.BasicPhylogenyNode;
import org.apache.commons.lang.NotImplementedException;

import java.util.Formatter;


/**
 * A tree node representing a hierarchical cluster.  Fakes multiple inheritance by providing a facade to the contained
 * Cluster.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HierarchicalCluster<T extends Clusterable<T>> extends BasicPhylogenyNode<Cluster<T>> implements Cluster<T>
	{
	public HierarchicalCluster(int id, Clusterable<T> sample)
		{
		super(new BasicCluster(id, sample));
		setWeight(1.);
		}

	/**
	 * {@inheritDoc}
	 */
	public T getCentroid()
		{
		return getValue().getCentroid();
		}

	/**
	 * {@inheritDoc}
	 */
	public void setCentroid(T centroid)
		{
		getValue().setCentroid(centroid);
		}

	/**
	 * {@inheritDoc}
	 */
	public int getId()
		{
		return getValue().getId();
		}

	/**
	 * {@inheritDoc}
	 */
	public void setId(int id)
		{
		getValue().setId(id);
		}

	/**
	 * {@inheritDoc}
	 */
	public int getN()
		{
		return getValue().getN();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean recenterByAdding(T point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean recenterByRemoving(T point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean recenterByAddingAll(Cluster<T> point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean recenterByRemovingAll(Cluster<T> point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public void setSumOfSquareDistances(double i)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public void addToSumOfSquareDistances(double v)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public double getStdDev()
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	/*	public void setN(int i)
	   {
	   getValue().setN(i);
	   }*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
		{
		Formatter f = new Formatter();
		f.format("l=%.2f w=%.2f %s", length, weight, value);//%[Cluster %d] n=%d sd=%.2f", id, n, getStdDev());

		return f.out().toString();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HierarchicalCluster<T> clone()
		{
		return (HierarchicalCluster<T>) super.clone();
		}

	/**
	 * Recomputes the probabilities of labels, based on the actual labels observed in the contained samples.  This must be
	 * called explicitly to avoid unnecessary recomputation on every sample addition.
	 */
	public WeightedSet<String> getDerivedLabelProbabilities()
		{
		throw new NotImplementedException();
		}

	/**
	 * Sets the probabilities of String labels.  The labels need not be mututally exclusive, so the weights need not sum to
	 * one.
	 *
	 * @param derivedLabelProbabilities a WeightedSet giving the probabilities of mutually exclusive String labels.
	 */
	public void setDerivedLabelProbabilities(WeightedSet<String> derivedLabelProbabilities)
		{
		throw new NotImplementedException();
		}

	/**
	 * Copy the local label weights into the derived label weights.
	 */
	public void updateDerivedWeightedLabelsFromLocal()
		{
		throw new NotImplementedException();
		}

	/**
	 * Gets the probabilities of mutually exclusive String labels.
	 *
	 * @return a Multinomial giving the probabilities of mutually exclusive String labels.
	 */
	public WeightedSet<String> getWeightedLabels()
		{
		throw new NotImplementedException();
		}

	/**
	 * Gets the probabilities of mutually exclusive String labels.
	 *
	 * @return a Multinomial giving the probabilities of mutually exclusive String labels.
	 */
	/*public WeightedSet<String> addWeightedLabels(WeightedSet<String> l)
		{
		throw new NotImplementedException();
		}*/
	}
