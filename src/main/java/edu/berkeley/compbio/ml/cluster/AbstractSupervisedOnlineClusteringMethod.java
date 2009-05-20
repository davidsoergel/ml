package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;

import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractSupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractOnlineClusteringMethod<T, C> implements SupervisedClusteringMethod<T>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	protected AbstractSupervisedOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                                   Set<String> predictLabels, Set<String> leaveOneOutLabels,
	                                                   Set<String> testLabels, int testThreads)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels, testThreads);
		}
	}
