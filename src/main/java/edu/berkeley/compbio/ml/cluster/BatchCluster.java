/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.util.SortedSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface BatchCluster<T extends Clusterable<T>, H extends BatchCluster<T, H>> extends Cluster<T>, Comparable<H>
	{
	void forgetExamples();

	SortedSet<T> getPoints();
	}
