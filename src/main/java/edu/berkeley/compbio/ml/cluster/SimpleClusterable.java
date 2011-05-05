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

	public SimpleClusterable(final T id)
		{
		this.id = id;
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
		return id.toString();
		}

	public int compareTo(final SimpleClusterable<T> o)
		{
		return id.compareTo(o.getId());
		}

	@Override
	public String toString()
		{
		return "SimpleClusterable{" + "id=" + id + '}';
		}
	}
