/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kmeans;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;

import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class GrowableKmeansClustering<T extends AdditiveClusterable<T>> extends KmeansClustering<T>
	{
// --------------------------- CONSTRUCTORS ---------------------------

	public GrowableKmeansClustering(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                final Map<String, Set<String>> predictLabelSets,
	                                final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		}

// -------------------------- OTHER METHODS --------------------------
/*
	public void addCluster(final CentroidCluster<T> cluster)
		{
		theClusters.add(cluster);
		}*/
	}
