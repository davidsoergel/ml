/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.stats;

import com.davidsoergel.stats.Statistic;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ACERichnessEstimate implements Statistic<AbundanceModel>
	{
	public double measure(final AbundanceModel a)
		{
		try
			{
			double Cace = 1 - a.F[1] / a.Nrare;

			int sum = 0;
			for (int i = 1; i <= 10; i++)
				{
				sum += i * (i - 1) * a.F[i];
				}

			double gamma2ace = a.Srare * sum / (Cace * a.Nrare * (a.Nrare - 1));
			gamma2ace = Math.max(gamma2ace, 0);

			return a.Sabund + (a.Srare / Cace) + (a.F[1] / Cace) * gamma2ace;
			}
		catch (ArithmeticException e)
			{
			return a.observed;
			}
		}
	}
