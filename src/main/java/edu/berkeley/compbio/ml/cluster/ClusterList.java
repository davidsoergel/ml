/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.util.List;


/**
 * Interface for objects that contain a list of Clusters.  These are maintained as a List rather than a Set for ease of
 * identification by index.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface ClusterList<T extends Clusterable<T>>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Returns the contained Collection of Clusters.
	 *
	 * @return the contained Collection of Clusters.
	 */
	List<? extends Cluster<? extends T>> getClusters();
	}
