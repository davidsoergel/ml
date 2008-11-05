package edu.berkeley.compbio.ml.cluster.svm;

import edu.berkeley.compbio.ml.cluster.Clusterable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface Kernel<T extends Clusterable<T>>
	{
	double evaluate(T a, T b);
	}
