package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractBatchClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractClusteringMethod<T, C> implements BatchClusteringMethod<T>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	public AbstractBatchClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                     Map<String, Set<String>> predictLabelSets, Set<String> leaveOneOutLabels,
	                                     Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels);
		}
	}
