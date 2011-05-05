package edu.berkeley.compbio.ml.cluster;

import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface BatchCluster<T extends Clusterable<T>, H extends BatchCluster<T, H>> extends Cluster<T>, Comparable<H>
	{
	void forgetExamples();

	Set<T> getPoints();
	}
