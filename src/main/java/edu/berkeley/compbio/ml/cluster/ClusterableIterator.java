/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.conja.NextOnlyIterator;

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
public interface ClusterableIterator<T extends Clusterable<T>> extends NextOnlyIterator<T>
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
