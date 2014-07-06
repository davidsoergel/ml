/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import java.util.Collections;
import java.util.List;


/**
 * A Factory for new Iterators based on a List, where each new Iterator provides the contents in a random order.  The
 * shuffling is done in place on the underlying collection.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: PermutingCollectionIteratorFactory.java 283 2008-10-13 20:51:20Z soergel $
 */

public class PermutingClusterableIteratorFactory<T extends Clusterable<T>> extends ClusterableIteratorFactory<T>
	{

	public PermutingClusterableIteratorFactory(final List<T> underlyingList)
		{
		super(underlyingList);
		}


	public ClusterableIterator<T> next()
		{
		Collections.shuffle((List<T>) underlyingCollection);
		return super.next();
		}
	}
