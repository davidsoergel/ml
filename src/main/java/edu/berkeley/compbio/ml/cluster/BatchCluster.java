package edu.berkeley.compbio.ml.cluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BatchCluster<T extends Clusterable<T>> extends AbstractCluster<T>
	{
	/**
	 * The set of samples contained in this cluster.
	 */
	private Set<T> thePoints = new HashSet<T>();


	public Set<T> getPoints()
		{
		return thePoints;
		}


	public BatchCluster(int id)
		{
		super(id);
		}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(T point)
		{
		if (thePoints.add(point))
			{
			super.add(point);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Cluster<T> otherCluster)
		{
		if (thePoints.addAll(((BatchCluster<T>) otherCluster).getPoints()))
			{
			super.addAll(otherCluster);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(T point)
		{
		if (thePoints.remove(point))
			{
			super.remove(point);
			return true;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Cluster<T> otherCluster)
		{
		if (thePoints.removeAll(((BatchCluster<T>) otherCluster).getPoints()))
			{
			super.removeAll(otherCluster);
			return true;
			}
		return false;
		}

	/**
	 * drop the references to the training examples, but don't forget the label distribution
	 */
	public void forgetExamples()
		{
		thePoints = new HashSet<T>();
		}
	}
