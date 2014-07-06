/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.io.OutputStream;

/**
 * A clustering method which represents each cluster as a point (the "centroid"), which must be generated from a
 * prototype and which has various stats associated with it
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface CentroidClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
// -------------------------- OTHER METHODS --------------------------

	String clusteringStats();

	void computeClusterStdDevs(ClusterableIterator<T> theDataPointProvider);

//	List<T> getCentroids();

	String shortClusteringStats();

	void writeClusteringStatsToStream(OutputStream outf);
	}
