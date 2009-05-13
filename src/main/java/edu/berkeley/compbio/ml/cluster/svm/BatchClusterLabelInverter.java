package edu.berkeley.compbio.ml.cluster.svm;

import edu.berkeley.compbio.jlibsvm.labelinverter.LabelInverter;
import edu.berkeley.compbio.ml.cluster.BatchCluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BatchClusterLabelInverter<T extends Clusterable<T>> implements LabelInverter<BatchCluster<T>>
	{
// ------------------------------ FIELDS ------------------------------

	// cache the inversions so as not to always create new ones
	Map<BatchCluster<T>, BatchCluster<T>> inversions = new HashMap<BatchCluster<T>, BatchCluster<T>>();


// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface LabelInverter ---------------------

	public BatchCluster<T> invert(BatchCluster<T> label)
		{
		BatchCluster<T> result = inversions.get(label);
		if (result == null)
			{
			result = new BatchCluster<T>(-label.getId());
			inversions.put(label, result);
			inversions.put(result, label);
			}
		return result;
		}
	}
