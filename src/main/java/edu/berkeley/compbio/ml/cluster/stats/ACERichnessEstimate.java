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
public class ACERichnessEstimate implements Statistic<ClusterList<? extends Clusterable>>
	{
	public double measure(final ClusterList<? extends Clusterable> a)
		{
		List<? extends Cluster<? extends Clusterable>> l = a.getClusters();
		int Nrare = 0;
		int Srare = 0;
		int Sabund = 0;
		int[] F = new int[11];

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

		double Cace = 1 - F[1] / Nrare;

		int sum = 0;
		for (int i = 1; i <= 10; i++)
			{
			sum += i * (i - 1) * F[i];
			}

		double gamma2ace = Srare * sum / (Cace * Nrare * (Nrare - 1));
		gamma2ace = Math.max(gamma2ace, 0);

		return Sabund + (Srare / Cace) + (F[1] / Cace) * gamma2ace;
		}
	}
