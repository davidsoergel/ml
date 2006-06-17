package edu.berkeley.compbio.bugbags.cluster;

import com.davidsoergel.dsutils.Props;
import edu.berkeley.compbio.bugbags.BugbagsException;
import edu.berkeley.compbio.bugbags.BugbagsRun;
import edu.berkeley.compbio.bugbags.Kcount;
import edu.berkeley.compbio.bugbags.KcountFromSequenceProvider;
import edu.berkeley.compbio.bugbags.SequenceProvider.SequenceProvider;
import edu.berkeley.compbio.bugbags.distancemeasure.JDivergenceHierarchical;
import edu.berkeley.compbio.bugbags.sequenceprovider.MockSequenceProvider;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author lorax
 * @version 1.0
 */
public class KmeansClusteringTest
	{
	private static Logger logger = Logger.getLogger(KmeansClusteringTest.class);

	@Test
	public void testSimilarSequencesClusterTogether()
		{
		try
			{
			Props props = new Props();
			new BugbagsRun("test", props);
			BugbagsRun.getProps().setProperty("jkmern.ktrie.smoothfactor", "0.6");

			SequenceProvider sp;

			sp = new MockSequenceProvider().init();

			KcountFromSequenceProvider kp = new KcountFromSequenceProvider(sp, 50, 2);
			KmeansClustering<Kcount> oc = new KmeansClustering<Kcount>(kp, 4, JDivergenceHierarchical.getInstance());

			oc.run(200); // more steps than sequences

			//	batchUpdateAndPrint(oc);
			//	batchUpdateAndPrint(oc);

			List<Cluster<Kcount>> theClusters = oc.getClusters();

			for (Cluster<Kcount> c : theClusters)
				{
				logger.debug(c);

				}

			assert true; // this test doesn't assert anything,but looks good
			}
		catch (BugbagsException e)
			{
			logger.debug(e);
			assert false;
			}
		}

	private void batchUpdateAndPrint(KmeansClustering<Kcount> oc) throws BugbagsException
		{
		logger.debug("Batch update: ");
		if (oc.batchUpdate())
			{
			logger.debug("...Changed!");
			}
		else
			{
			logger.debug("...Not changed!");
			}


		List<Cluster<Kcount>> theClusters = oc.getClusters();

		for (Cluster<Kcount> c : theClusters)
			{
			logger.debug(c);

			}
		}


	}
