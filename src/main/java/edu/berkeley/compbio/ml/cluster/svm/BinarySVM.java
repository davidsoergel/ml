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

package edu.berkeley.compbio.ml.cluster.svm;

import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps libsvm
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BinarySVM<T extends Clusterable<T>, C extends Cluster<T>>
		implements Comparable// extends RegressionSVM<T, C>
	{
	C cluster1;
	C cluster2;

	RegressionSVM<T> rsvm;
	private Kernel<T> kernel;

	public BinarySVM(C cluster1, C cluster2, Kernel<T> k)
		{
		this.cluster1 = cluster1;
		this.cluster2 = cluster2;
		this.kernel = k;
		}

	public C classify(T p)
		{
		double d = rsvm.classify(p);
		if (d >= 0)
			{
			return cluster1;
			}
		else
			{
			return cluster2;
			}
		}

	public void train(Collection<T> posExamples, Collection<T> negExamples)
		{
		Map<T, Double> examples = new HashMap<T, Double>();

		// PERF
		for (T example : posExamples)
			{
			examples.put(example, 1.);
			}
		for (T example : negExamples)
			{
			examples.put(example, 1.);
			}
		rsvm = new RegressionSVM<T>(examples, kernel);
		}


	public int compareTo(Object o)
		{
		// random but deterministic ordering
		int h = hashCode();
		int oh = o.hashCode();
		return h < oh ? -1 : h == oh ? 0 : 1;
		}

	public boolean equals(Object o)
		{
		if (this == o)
			{
			return true;
			}
		if (o == null || getClass() != o.getClass())
			{
			return false;
			}

		BinarySVM binarySVM = (BinarySVM) o;

		if (cluster1 != null ? !cluster1.equals(binarySVM.cluster1) : binarySVM.cluster1 != null)
			{
			return false;
			}
		if (cluster2 != null ? !cluster2.equals(binarySVM.cluster2) : binarySVM.cluster2 != null)
			{
			return false;
			}

		return true;
		}

	public int hashCode()
		{
		int result;
		result = (cluster1 != null ? cluster1.hashCode() : 0);
		result = 31 * result + (cluster2 != null ? cluster2.hashCode() : 0);
		return result;
		}
	}

