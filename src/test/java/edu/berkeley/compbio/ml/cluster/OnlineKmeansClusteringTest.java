package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.MathUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author lorax
 * @version 1.0
 */
public class OnlineKmeansClusteringTest
	{
	private static Logger logger = Logger.getLogger(OnlineKmeansClusteringTest.class);

	@Configuration(beforeSuite = true)
	public void setUp()
		{
		MathUtils.initApproximateLog(1000000, 10000);
		}

	@Test
	public void testSimilarPointsClusterTogether() throws CloneNotSupportedException, IOException
		{
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
