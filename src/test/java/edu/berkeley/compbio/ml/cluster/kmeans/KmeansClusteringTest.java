/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster.kmeans;

import com.davidsoergel.dsutils.math.MathUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @version 1.0
 */
public class KmeansClusteringTest
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KmeansClusteringTest.class);


// -------------------------- OTHER METHODS --------------------------

	@BeforeSuite
	public void setUp()
		{
		MathUtils.initApproximateLog(-12, +12, 3, 100000);
		}

	@Test
	public void testSimilarPointsClusterTogether() throws CloneNotSupportedException, IOException
		{
		// BAD Test is commented out!
		/*
			  ClusterableIterator ci;

			  ci = new MockClusterableIterator().init();

			  KmeansClustering<ClusterableDoubleArray> oc = new KmeansClustering<ClusterableDoubleArray>(ci, 5, EuclideanDistance.getInstance());

			  oc.run(ci, 7);

			  //	batchUpdateAndPrint(oc);
			  //	batchUpdateAndPrint(oc);

			  List<Cluster<ClusterableDoubleArray>> theClusters = oc.getClusters();

			  for (Cluster<ClusterableDoubleArray> c : theClusters)
				  {
				  logger.debug(c);

				  }

			  oc.writeAssignmentsAsTextToStream(System.err);

			  assert true; // this test doesn't assert anything,but looks good
  */
		}
	}
