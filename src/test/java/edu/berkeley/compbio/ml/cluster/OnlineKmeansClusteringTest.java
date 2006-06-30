package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.MathUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Configuration;

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

/*	@Test
	public void testSimilarPointsClusterTogether()
		{

			ClusterableIterator ci;

			ci = new MockClusterableIterator().init();

			KmeansClustering<MockClusterable> oc = new KmeansClustering<MockClusterable>(ci, 5, JDivergenceHierarchical.getInstance());

			oc.run(kc, 7);

			//	batchUpdateAndPrint(oc);
			//	batchUpdateAndPrint(oc);

			List<Cluster<Kcount>> theClusters = oc.getClusters();

			for (Cluster<Kcount> c : theClusters)
				{
				logger.debug(c);

				}

			oc.writeAssignmentsAsTextToStream(System.err);

			assert true; // this test doesn't assert anything,but looks good

		}*/
	}
