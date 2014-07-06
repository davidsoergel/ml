/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.cluster.stats.ACERichnessEstimate;
import edu.berkeley.compbio.ml.cluster.stats.AbundanceModel;
import edu.berkeley.compbio.ml.cluster.stats.Chao1RichnessEstimate;
import edu.berkeley.compbio.ml.cluster.stats.ClusteringSimilarityModel;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusteringStats<T>
	{
	private static Chao1RichnessEstimate chao1 = new Chao1RichnessEstimate();
	private static ACERichnessEstimate ace = new ACERichnessEstimate();

	//final ClusterList theClusterList;
	final AbundanceModel a;
	final ClusteringSimilarityModel b;

	public ClusteringStats(final ClusterList theClusterList)
		{
		this(theClusterList, null);
		}

	public ClusteringStats(final ClusterList theClusterList, final ClusterList referenceClusterList)
		// ,final Comparator<T> comparator)
	{
	//	this.theClusterList = theClusterList;
	a = new AbundanceModel(theClusterList);
	if (referenceClusterList != null)
		{
		b = new ClusteringSimilarityModel(theClusterList, referenceClusterList, a.totalSamples); //, comparator);
		}
	else
		{
		b = null;
		}
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

	public ClusteringSimilarityModel getClusteringSimilarityModel()
		{
		return b;
		}
	}
