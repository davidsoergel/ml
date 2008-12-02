package edu.berkeley.compbio.ml.cluster.svm;

import edu.berkeley.compbio.jlibsvm.labelinverter.LabelInverter;
import edu.berkeley.compbio.ml.cluster.BatchCluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BatchClusterLabelInverter<T extends Clusterable<T>> implements LabelInverter<BatchCluster<T>>
	{
	public BatchCluster<T> invert(BatchCluster<T> label)
		{
		return new BatchCluster<T>(-label.getId());
		}
	}
