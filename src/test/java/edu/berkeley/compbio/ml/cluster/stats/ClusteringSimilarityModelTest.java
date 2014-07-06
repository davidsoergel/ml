/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.stats;

import edu.berkeley.compbio.ml.cluster.BasicBatchCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.SimpleClusterList;
import edu.berkeley.compbio.ml.cluster.SimpleClusterable;
import junit.framework.TestCase;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusteringSimilarityModelTest extends TestCase
	{

	@Test
	public void clusteringSimilarityModelWorks()
		{


		final Set<Cluster<SimpleClusterable<String>>> clustersA = new HashSet<Cluster<SimpleClusterable<String>>>();
		final Set<Cluster<SimpleClusterable<String>>> clustersB = new HashSet<Cluster<SimpleClusterable<String>>>();


		clustersA.add(createCluster("a", "b", "c"));
		clustersA.add(createCluster("d", "e"));
		clustersA.add(createCluster("f", "g", "h", "i"));
		clustersA.add(createCluster("j", "k"));

		clustersB.add(createCluster("a", "b"));
		clustersB.add(createCluster("d", "e"));
		clustersB.add(createCluster("c", "f", "g", "h", "i"));
		clustersB.add(createCluster("j", "k"));


		final int totalSamples = 11;


		/*final Comparator<String> comparator = new Comparator<String>()
		{
		public int compare(final String o1, final String o2)
			{
			return o1.compareTo(o2);
			}
		};*/

		ClusteringSimilarityModel result =
				new ClusteringSimilarityModel(new SimpleClusterList(clustersA), new SimpleClusterList(clustersB),
				                              totalSamples); //, comparator);

		assert result.pairwiseSameClusterSensitivity > 0.5;
		assert result.pairwiseSameClusterSpecificity > 0.5;

		assert result.proportionOfPredictedClustersIdentical == 0.5;
		assert result.proportionOfReferenceClustersIdentical == 0.5;

		assert result.proportionOfPredictedClustersMappable == 1.0;
		assert result.proportionOfReferenceClustersMappable == 1.0;

		assert result.proportionOfSamplesInIdenticalClusters > 0;
		assert result.proportionOfTruePositiveSamplePairsInIdenticalClusters > 0;

		assert result.proportionOfSamplesInMappableReferenceClusters == 1.0;
		assert result.proportionOfSamplePairsInMappableReferenceClusters == 1.0;

		assert result.proportionOfSamplesInMappablePredictedClusters == 1.0;
		assert result.proportionOfSamplePairsInMappablePredictedClusters == 1.0;
		}

	private static int id = 0;

	private BasicBatchCluster<SimpleClusterable<String>> createCluster(String... points)
		{
		BasicBatchCluster<SimpleClusterable<String>> result = new BasicBatchCluster<SimpleClusterable<String>>(id++);

		for (String point : points)
			{
			SimpleClusterable<String> simpleClusterable = new SimpleClusterable<String>(point);
			simpleClusterable.getMutableWeightedLabels().add("" + id, 1.0, 1);
			simpleClusterable.doneLabelling();
			result.add(simpleClusterable);
			}
		result.doneLabelling();


		return result;
		}
	}
