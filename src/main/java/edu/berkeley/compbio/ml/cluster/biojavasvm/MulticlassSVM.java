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

package edu.berkeley.compbio.ml.cluster.biojavasvm;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.binary.C_SVC;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassificationSVM;
import edu.berkeley.compbio.ml.cluster.BatchCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.SupervisedOnlineClusteringMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MulticlassSVM<T extends Clusterable<T>> extends SupervisedOnlineClusteringMethod<T, BatchCluster<T>>
	{
	Symmetric2dBiMap<BatchCluster<T>, BinarySVM<T, BatchCluster<T>>> allVsAllClassifiers;
	Map<BatchCluster<T>, BinarySVM<T, BatchCluster<T>>> oneVsAllClassifiers;

	MultiClassificationSVM multiSvm;

	Map<String, BatchCluster<T>> theClusterMap;

	private Kernel<T> kernel;

	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException
		{
		// do nothing with the iterator or any of that
		assert initSamples == 0;

		// by analogy with BayesianClustering, take this opportunity to initialize the clusters

		theClusterMap = new HashMap<String, BatchCluster<T>>(trainingLabels.size());
		int i = 0;
		for (String label : trainingLabels)
			{
			BatchCluster<T> cluster = theClusterMap.get(label);

			if (cluster == null)
				{
				cluster = new BatchCluster<T>(i++);
				theClusterMap.put(label, cluster);
				}
			}
		theClusters = theClusterMap.values();
		}

	public MulticlassSVM(Kernel<T> kernel)
		{
		super(null);
		//ImmutableSvmParameterPoint.Builder<T, BatchCluster<T>> builder = new ImmutableSvmParameterPoint.Builder<T, BatchCluster<T>>();
		//BinaryClassificationSVM<BatchCluster<T>, T> binarySvm =
		//		new C_SVC<BatchCluster<T>, T>(kernel, new NoopScalingModelLearner<T>(), builder.build());
		BinaryClassificationSVM<BatchCluster<T>, T> binarySvm = new C_SVC<BatchCluster<T>, T>();
		this.multiSvm = new MultiClassificationSVM<BatchCluster<T>, T>(binarySvm);
		this.kernel = kernel;
		}

	/**
	 * Return a ClusterMove object describing the best way to reassign the given point to a new cluster.
	 *
	 * @param p
	 * @return
	 */
	public ClusterMove bestClusterMove(T p) throws NoGoodClusterException
		{
		// phase 1: classify by all vs. all voting

		final Multiset<BatchCluster<T>> votes = new HashMultiset<BatchCluster<T>>();

		for (BinarySVM<T, BatchCluster<T>> svm : allVsAllClassifiers.values())
			{
			votes.add(svm.classify(p));
			}

		BatchCluster<T> winner;// = votes.getDominantKey();

		// PERF

		List<BatchCluster<T>> clustersInVoteOrder = new ArrayList<BatchCluster<T>>(votes.elementSet());

		Collections.sort(clustersInVoteOrder, new Comparator<Cluster<T>>()
		{
		public int compare(Cluster<T> o1, Cluster<T> o2)
			{
			int v1 = votes.count(o1);
			int v2 = votes.count(o2);
			return v1 < v2 ? -1 : v1 == v2 ? 0 : 1;
			}
		});

		winner = clustersInVoteOrder.get(0);


		// phase 2: reject classification by one vs. all

		if (!oneVsAllClassifiers.get(winner).classify(p).equals(winner))
			{
			throw new NoGoodClusterException("Winning bin rejected by one-vs-all filter");
			}

		// if the top hit is rejected, should we try the second hit, etc.?
		// that's why we bothered sorting tho whore list above

		ClusterMove<T, BatchCluster<T>> result = new ClusterMove<T, BatchCluster<T>>();
		result.bestCluster = winner;
		return result;
		}

	/**
	 * consider each of the incoming data points exactly once.
	 */
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException
		{
		// throw out any existing classifiers

		allVsAllClassifiers = new Symmetric2dBiMap<BatchCluster<T>, BinarySVM<T, BatchCluster<T>>>();
		oneVsAllClassifiers = new HashMap<BatchCluster<T>, BinarySVM<T, BatchCluster<T>>>(trainingLabels.size());

		Iterator<T> trainingIterator = trainingCollectionIteratorFactory.next();

		// separate the training set into label-specific sets, caching all the while
		// (too bad the svm training requires all examples in memory)

		// Multimap<String, T> examples = new HashMultimap<String, T>();

		while (trainingIterator.hasNext())
			{
			T sample = trainingIterator.next();
			String label = sample.getWeightedLabels().getDominantKeyInSet(trainingLabels);

			theClusterMap.get(label).add(sample);
			//examples.put(label, sample);
			}

		// create and train all vs all classifiers

		for (BatchCluster<T> cluster1 : theClusters)
			{
			for (BatchCluster<T> cluster2 : theClusters)
				{
				if (cluster2.getId() > cluster1.getId())// avoid redundant pairs
					{
					BinarySVM<T, BatchCluster<T>> svm = new BinarySVM<T, BatchCluster<T>>(cluster1, cluster2, kernel);
					allVsAllClassifiers.put(cluster1, cluster2, svm);
					svm.train(cluster1.getPoints(), cluster2.getPoints());
					}
				}
			}

		// create and train one vs all classifiers

		BatchCluster<T> notCluster = new BatchCluster<T>(-1);
		for (Cluster<T> cluster1 : theClusters)
			{
			notCluster.addAll(cluster1);
			}

		for (BatchCluster<T> cluster1 : theClusters)
			{
			notCluster.removeAll(cluster1);

			BinarySVM<T, BatchCluster<T>> svm = new BinarySVM<T, BatchCluster<T>>(cluster1, notCluster, kernel);
			oneVsAllClassifiers.put(cluster1, svm);
			svm.train(cluster1.getPoints(), notCluster.getPoints());

			notCluster.addAll(cluster1);
			}

		// we can throw out the examples now
		for (BatchCluster<T> cluster1 : theClusters)
			{
			cluster1.forgetExamples();
			}
		}
	}
