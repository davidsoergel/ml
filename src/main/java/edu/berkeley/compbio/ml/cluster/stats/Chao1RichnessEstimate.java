package edu.berkeley.compbio.ml.cluster.stats;

import com.davidsoergel.stats.Statistic;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterList;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class Chao1RichnessEstimate implements Statistic<ClusterList<? extends Clusterable>>
	{
	public double measure(final ClusterList<? extends Clusterable> a)
		{
		List<? extends Cluster<? extends Clusterable>> l = a.getClusters();
		int empty = 0;
		int singletons = 0;
		int doubletons = 0;
		for (Cluster<? extends Clusterable> cluster : l)
			{
			final int n = cluster.getN();
			if (n == 0)
				{
				empty++;
				}
			else if (n == 1)
				{
				singletons++;
				}
			else if (n == 2)
					{
					doubletons++;
					}
			}
		int observed = l.size() - empty;
		return observed + (singletons * singletons / (2. * doubletons));
		}
	}
