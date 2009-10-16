package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.cluster.stats.ACERichnessEstimate;
import edu.berkeley.compbio.ml.cluster.stats.Chao1RichnessEstimate;
import edu.berkeley.compbio.ml.cluster.stats.NonemptyClusterCount;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusteringStats
	{
	private static Chao1RichnessEstimate chao1 = new Chao1RichnessEstimate();
	private static ACERichnessEstimate ace = new ACERichnessEstimate();
	private static NonemptyClusterCount rawRichness = new NonemptyClusterCount();

	final ClusterList theClusterList;

	public ClusteringStats(final ClusterList theClusterList)
		{
		this.theClusterList = theClusterList;
		}

	public double ace()
		{
		return ace.measure(theClusterList);
		}

	public double chao1()
		{
		return chao1.measure(theClusterList);
		}

	public int rawRichness()
		{
		return (int) rawRichness.measure(theClusterList);
		}
	}
