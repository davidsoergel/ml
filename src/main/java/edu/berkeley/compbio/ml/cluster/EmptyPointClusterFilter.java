package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class EmptyPointClusterFilter<T extends Clusterable<T>> implements PointClusterFilter<T>
	{
	public boolean isProhibited(final Cluster<T> tCluster)
		{
		return false;
		}
	}
