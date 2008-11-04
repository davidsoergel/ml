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

import org.apache.commons.lang.NotImplementedException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BasicCentroidCluster<T extends Clusterable<T>> extends AbstractCentroidCluster<T>
	{
	/**
	 * Constructs a new Cluster with the given id and centroid.  Note the centroid may be modified in the course of running
	 * a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e., it's probably best to
	 * clone it first).
	 *
	 * @param id       an int uniquely identifying this cluster
	 * @param centroid the T
	 */
	public BasicCentroidCluster(int id, T centroid)
		{
		super(id, centroid);
		}

	// we can't support adding and removing points because we don't know how to modify the centroid if it's not additive

	/**
	 * {@inheritDoc}
	 */
	public boolean add(T point)
		{
		throw new NotImplementedException();
		//return false;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(CentroidCluster<T> point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(T point)
		{
		throw new NotImplementedException();
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAll(CentroidCluster<T> point)
		{
		throw new NotImplementedException();
		}
	}
