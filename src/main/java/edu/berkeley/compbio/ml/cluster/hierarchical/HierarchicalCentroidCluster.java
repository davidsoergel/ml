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

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.Labellable;
import com.davidsoergel.dsutils.collections.ImmutableHashWeightedSet;
import com.davidsoergel.dsutils.collections.MutableWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.trees.BasicPhylogenyNode;
import edu.berkeley.compbio.ml.cluster.BasicCentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.io.Writer;
import java.util.Formatter;
import java.util.Iterator;


/**
 * A tree node representing a hierarchical cluster.  Fakes multiple inheritance by providing a facade to the contained
 * Cluster.  I.e., this is mostly a PhylogenyNode containing a CentroidCluster value, but it can also act like a
 * CentroidCluster directly.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HierarchicalCentroidCluster<T extends Clusterable<T>> extends BasicPhylogenyNode<CentroidCluster<T>>
		implements CentroidCluster<T>, Comparable<HierarchicalCentroidCluster<T>>
		//,    HierarchyNode<CentroidCluster<T>,HierarchicalCentroidCluster<T>>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	public HierarchicalCentroidCluster(final int id, final T sample)
		{
		super(new BasicCentroidCluster<T>(id, sample));
		setWeight(1.);
		}

// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HierarchicalCentroidCluster<T> clone()
		{
		return (HierarchicalCentroidCluster<T>) super.clone();
		}

	/**
	 * {@inheritDoc}
	 *
	 * This iterator should return HierarchicalCentroidClusters
	 */
	/*public DepthFirstTreeIterator<CentroidCluster<T>, PhylogenyNode<CentroidCluster<T>>> depthFirstIterator()
		{
		return new DepthFirstTreeIteratorImpl<CentroidCluster<T>, PhylogenyNode<CentroidCluster<T>>>(this);
		}
		*/


	/**
	 * {@inheritDoc}
	 * <p/>
	 * This iterator should return HierarchicalCentroidClusters
	 */
/*	public DepthFirstTreeIterator<CentroidCluster<T>, CentroidCluster<T>>> depthFirstIterator()
		{
		return new DepthFirstTreeIteratorImpl<CentroidCluster<T>, CentroidCluster<T>>>(this);
		}
*/
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
		final Formatter f = new Formatter();
		f.format("l=%.2f w=%.2f %s", length, weight, value);//%[Cluster %d] n=%d sd=%.2f", id, n, getStdDev());

		return f.out().toString();
		}


// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidCluster ---------------------


	/**
	 * {@inheritDoc}
	 */
	public void addToSumOfSquareDistances(final double v)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public T getCentroid()
		{
		return getPayload().getCentroid();
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
	/*	public void setCentroid(final T centroid)
		 {
		 getValue().setCentroid(centroid);
		 }
 */

	/**
	 * {@inheritDoc}
	 */
	public void setSumOfSquareDistances(final double i)
		{
		throw new NotImplementedException();
		}

// --------------------- Interface Cluster ---------------------


	/**
	 * {@inheritDoc}
	 */
	public boolean add(final T point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(final Cluster<T> point)
		{
		return getPayload().addAll(point);
		//throw new NotImplementedException();
		}

	/**
	 * Recomputes the probabilities of labels, based on the actual labels observed in the contained samples.  This must be
	 * called explicitly to avoid unnecessary recomputation on every sample addition.
	 */
	public WeightedSet<String> getDerivedLabelProbabilities()
		{
		return getPayload().getDerivedLabelProbabilities();
//		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public int getId()
		{
		return getPayload().getId();
		}

	/**
	 * {@inheritDoc}
	 */
	public int getN()
		{
		return getPayload().getN();
		}

	/**
	 * Gets the probabilities of mutually exclusive String labels.
	 *
	 * @return a Multinomial giving the probabilities of mutually exclusive String labels.
	 */
	public WeightedSet<String> getImmutableWeightedLabels()
		{
		return getPayload().getImmutableWeightedLabels();
		//	throw new NotImplementedException();
		}

	public void doneLabelling()
		{
		((Labellable<String>) getPayload()).doneLabelling();
		}

	/**
	 * Gets the probabilities of mutually exclusive String labels.
	 *
	 * @return a Multinomial giving the probabilities of mutually exclusive String labels.
	 */
	public MutableWeightedSet<String> getMutableWeightedLabels()
		{
		return ((Labellable<String>) getPayload()).getMutableWeightedLabels();
		//	throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(final T point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAll(final Cluster<T> point)
		{
		throw new NotImplementedException();
		}

	/**
	 * Sets the probabilities of String labels.  The labels need not be mututally exclusive, so the weights need not sum to
	 * one.
	 *
	 * @param derivedLabelProbabilities a WeightedSet giving the probabilities of mutually exclusive String labels.
	 */
	public void setDerivedLabelProbabilities(final ImmutableHashWeightedSet<String> derivedLabelProbabilities)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
/*	public void setId(final int id)
		{
		getValue().setId(id);
		}*/

	/**
	 * Copy the local label weights into the derived label weights.
	 */
	public void updateDerivedWeightedLabelsFromLocal()
		{
		getPayload().updateDerivedWeightedLabelsFromLocal();
		}

// --------------------- Interface LengthWeightHierarchyNode ---------------------


	/**
	 * Gets the probabilities of mutually exclusive String labels.
	 *
	 * @return a Multinomial giving the probabilities of mutually exclusive String labels.
	 */
	/*public WeightedSet<String> addWeightedLabels(WeightedSet<String> l)
		{
		throw new NotImplementedException();
		}*/
	public void toNewick(final Writer out, String prefix, final String tab, final int minClusterSize,
	                     final double minLabelProb) throws IOException
		{
		// (children)name:length

		if (prefix != null)
			{
			out.write(prefix);
			}

		if (!children.isEmpty())
			{
			prefix = prefix == null ? null : prefix + tab;
			out.write("(");
			final Iterator<BasicPhylogenyNode<CentroidCluster<T>>> i = children.iterator();
			while (i.hasNext())
				{
				final BasicPhylogenyNode<CentroidCluster<T>> child = i.next();
				if (child.getPayload().getN() >= minClusterSize)
					{
					child.toNewick(out, prefix, tab, minClusterSize, minLabelProb);
					if (i.hasNext())
						{
						out.write(",");
						}
					}
				}
			out.write(")");
			}

		out.write(getN());

		final WeightedSet<String> labels = getDerivedLabelProbabilities();

		for (final String label : labels.keysInDecreasingWeightOrder())
			{
			final double labelProb = labels.getNormalized(label);
			if (labelProb < minLabelProb)
				{
				break;
				}
			out.write("_");
			out.write(label);
			out.write("=");
			out.write(String.format("%.2f", labelProb));
			}

		if (bootstrap != 0)
			{
			out.write(":");
			out.write(bootstrap.toString());
			}
		}

	public int compareTo(final HierarchicalCentroidCluster<T> o)
		{
		final int id = getId();
		final int oid = o.getId();

		return (id < oid) ? -1 : (id > oid) ? 1 : 0;
		}

	@Override
	public boolean equals(final Object o)
		{
		if (this == o)
			{
			return true;
			}
		if (!(o instanceof HierarchicalCentroidCluster))
			{
			return false;
			}
		if (getId() != ((HierarchicalCentroidCluster) o).getId())
			{
			return false;
			}
		return true;
		}

	@Override
	public int hashCode()
		{
		return getId();
		}
/*
	public void setParent(HierarchicalCentroidCluster<T> c)
		{
		super.setParent(c);
		}


	public void registerChild(HierarchicalCentroidCluster<T> c)
		{
		super.registerChild(c);
		}

	public void unregisterChild(HierarchicalCentroidCluster<T> c)
		{
		super.unregisterChild(c);
		}

	public List<HierarchicalCentroidCluster<T>> getAncestorPath()
		{

		}*/

	public int getItemCount()
		{
		return getPayload().getItemCount();
		}
	}
