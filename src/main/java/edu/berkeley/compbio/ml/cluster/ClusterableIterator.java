/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.conja.ThreadSafeNextOnlyIterator;

import java.io.IOException;

/**
 * Interface for resettable iterators over Clusterable objects.  This is useful because many clustering algorithms need
 * to consider the entire set of samples repeatedly, but don't want to store the samples explicitly.  In that case we
 * can just reset the iterator and run through it again.
 * <p/>
 * We don't extend Iterator in order to avoid thread synchronization issues resulting from the use of hasNext().  It's
 * better to just call next() and rely on NoSuchElementException.  All classes implementing this should have a
 * synchronized next().
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface ClusterableIterator<T extends Clusterable<T>> extends ThreadSafeNextOnlyIterator<T>
		//	extends Iterator<T>, Iterable<T>//, Comparable<ClusterableIterator<T>>
	{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Iterator ---------------------


// -------------------------- OTHER METHODS --------------------------

	//public abstract ClusterableIterator<T> clone() throws CloneNotSupportedException;

	/**
	 * Resets the iterator to the beginning, so that the next call to next() will return the first element.
	 *
	 * @throws IOException when something goes wrong
	 */
//	void reset(); //throws IOException;
	public T nextFullyLabelled();
	}
