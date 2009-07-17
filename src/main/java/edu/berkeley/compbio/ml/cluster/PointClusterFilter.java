package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface PointClusterFilter<T extends Clusterable<T>>
	{
	public boolean isProhibited(final Cluster<T> cluster);
	}
