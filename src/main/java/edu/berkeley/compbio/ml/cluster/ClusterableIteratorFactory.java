/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import org.apache.commons.lang.NotImplementedException;

import java.util.Collection;
import java.util.Iterator;


/**
 * A Factory for new Iterators based on a Collection.  Each provided Iterator is a new, independent object, iterating in
 * whatever order the underlying Collection provides (which may or may not be defined).
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: CollectionIteratorFactory.java 313 2009-02-23 03:36:52Z soergel $
 */

public class ClusterableIteratorFactory<T extends Clusterable<T>> implements Iterator<ClusterableIterator<T>>
	{
	protected final Collection<T> underlyingCollection;

	public ClusterableIteratorFactory(final Collection<? extends T> underlyingCollection)
		{
		this.underlyingCollection = (Collection<T>) underlyingCollection;
		}

	public boolean hasNext()
		{
		return true;
		}

	public ClusterableIterator<T> next()
		{
		return new CollectionClusterableIterator(underlyingCollection);
		}

	public void remove()
		{
		throw new NotImplementedException();
		}
	}
