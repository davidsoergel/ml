package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;

import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface ClusteringMethod<T extends Clusterable<T>> extends ClusterSet<T>
	{
// -------------------------- OTHER METHODS --------------------------

	String bestLabel(T sample, Set<String> predictLabels) throws NoGoodClusterException;

	ClusteringTestResults test(Iterator<T> theTestIterator, final DissimilarityMeasure<String> intraLabelDistances)
			throws DistributionException, ClusterException;
	}
