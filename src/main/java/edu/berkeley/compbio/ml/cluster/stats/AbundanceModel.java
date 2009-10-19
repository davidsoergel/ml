package edu.berkeley.compbio.ml.cluster.stats;

import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterList;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AbundanceModel
	{
	int Nrare = 0;
	int Srare = 0;
	int Sabund = 0;
	public int[] F = new int[11];
	public int observed;

	public AbundanceModel(final ClusterList<? extends Clusterable> a)
		{
		List<? extends Cluster<? extends Clusterable>> l = a.getClusters();

		for (Cluster<? extends Clusterable> cluster : l)
			{
			final int n = cluster.getN();
			if (n == 0)
				{
				F[0]++;
				}
			else if (n <= 10)
				{
				Nrare += n;
				Srare++;
				F[n]++;
				}
			else
				{
				Sabund++;
				}
			}
		observed = l.size() - F[0];
		}
	}
