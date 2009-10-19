package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.cluster.stats.ACERichnessEstimate;
import edu.berkeley.compbio.ml.cluster.stats.AbundanceModel;
import edu.berkeley.compbio.ml.cluster.stats.Chao1RichnessEstimate;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusteringStats
	{
	private static Chao1RichnessEstimate chao1 = new Chao1RichnessEstimate();
	private static ACERichnessEstimate ace = new ACERichnessEstimate();

	//final ClusterList theClusterList;
	final AbundanceModel a;

	public ClusteringStats(final ClusterList theClusterList)
		{
		//	this.theClusterList = theClusterList;
		a = new AbundanceModel(theClusterList);
		}

	public double ace()
		{
		return ace.measure(a);
		}

	public double chao1()
		{
		return chao1.measure(a);
		}

	public int rawRichness()
		{
		return a.observed;
		}

	public int singletons()
		{
		return a.F[1];
		}

	public int doubletons()
		{
		return a.F[2];
		}
	}
