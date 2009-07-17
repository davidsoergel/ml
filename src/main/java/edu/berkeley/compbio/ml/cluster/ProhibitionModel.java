package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface ProhibitionModel<T extends Clusterable<T>>
	{
	public boolean isProhibited(final T p, final Cluster<T> cluster);
	}
