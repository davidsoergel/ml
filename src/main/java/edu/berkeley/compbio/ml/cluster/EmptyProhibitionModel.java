package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class EmptyProhibitionModel<T extends Clusterable<T>> implements ProhibitionModel<T>
	{
	public PointClusterFilter<T> getFilter(final T p)
		{
		return new EmptyPointClusterFilter<T>();
		}
	}
