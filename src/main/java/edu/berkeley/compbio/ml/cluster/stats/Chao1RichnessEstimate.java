package edu.berkeley.compbio.ml.cluster.stats;

import com.davidsoergel.stats.Statistic;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class Chao1RichnessEstimate implements Statistic<AbundanceModel>
	{
	public double measure(final AbundanceModel a)
		{
		return a.observed + (a.F[1] * a.F[1] / (2. * a.F[2]));
		}
	}
