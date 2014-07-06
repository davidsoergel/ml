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
public class EmptyPointClusterFilter<T extends Clusterable<T>> implements PointClusterFilter<T>
	{
	public boolean isProhibited(final Cluster<T> tCluster)
		{
		return false;
		}
	}
