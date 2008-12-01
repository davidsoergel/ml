package edu.berkeley.compbio.ml.cluster.biojavasvm;

import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.berkeley.compbio.ml.cluster.Clusterable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface Kernel<T extends Clusterable<T>> extends KernelFunction<T>
	{
	//double evaluate(T a, T b);
	}
