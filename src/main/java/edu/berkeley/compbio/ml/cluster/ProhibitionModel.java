/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface ProhibitionModel<T extends Clusterable<T>>
	{
	//public boolean isProhibited(final T p, final Cluster<T> cluster);

	PointClusterFilter<T> getFilter(T p);
	}
