/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.PrototypeBasedCentroidClusteringMethod;
import edu.berkeley.compbio.ml.cluster.SampleInitializedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.SemisupervisedClusteringMethod;


/**
 * A Kohonen Self-Organizing Map.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public interface KohonenSOM<T extends AdditiveClusterable<T>>
		extends SemisupervisedClusteringMethod<T>, PrototypeBasedCentroidClusteringMethod<T>,
		        SampleInitializedOnlineClusteringMethod<T>, DiffusableLabelClusteringMethod<T, KohonenSOMCell<T>>
	{
	//boolean add(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException;


	//int getBestCluster(T p, List<Double> secondBestDistances) throws ClusterException, NoGoodClusterException;

	//OnlineClusteringMethod.ClusterMove bestClusterMove(T p);
	}
