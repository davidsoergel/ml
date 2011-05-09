package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.LabellableImpl;
import com.davidsoergel.dsutils.collections.MutableWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class SimpleClusterable<T extends Serializable & Comparable>
		implements Clusterable<SimpleClusterable<T>>, Serializable, Comparable<SimpleClusterable<T>>
	{
	transient private LabellableImpl<String> labels = new LabellableImpl<String>();

	final T id;
	final String idString;

	public SimpleClusterable(final T id)
		{
		this.id = id;
		this.idString = id.toString();
		}

	public void doneLabelling()
		{
		labels.doneLabelling();
		}

	@NotNull
	public WeightedSet<String> getImmutableWeightedLabels()
		{
		return labels.getImmutableWeightedLabels();
		}

	@NotNull
	public MutableWeightedSet<String> getMutableWeightedLabels()
		{
		return labels.getMutableWeightedLabels();
		}

	public int getItemCount()
		{
		return labels.getItemCount();
		}

	public SimpleClusterable<T> clone()
		{
		return new SimpleClusterable<T>(id);
		}

	public boolean equalValue(final SimpleClusterable other)
		{
		return id.equals(other.id);
		}

	public String getId()
		{
		return idString;
		}

	// we can't safely implement equals and hashCode() because the labels are mutable; but we can provide a stable sort anyway

	public int compareTo(final SimpleClusterable<T> o)
		{
		return idString.compareTo(o.getId());
		}

	@Override
	public String toString()
		{
		return "SimpleClusterable{" + "id=" + id + '}';
		}
	}
