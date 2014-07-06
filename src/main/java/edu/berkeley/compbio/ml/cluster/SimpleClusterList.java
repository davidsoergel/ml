/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class SimpleClusterList<T extends Clusterable<T>> implements ClusterList<T>
	{
	private List<? extends Cluster<? extends T>> theList;

	public SimpleClusterList(final Collection<? extends Cluster<? extends T>> clusters)
		{
		this.theList = new ArrayList<Cluster<? extends T>>(clusters);
		}

	public List<? extends Cluster<? extends T>> getClusters()
		{
		return theList;
		}
	}
