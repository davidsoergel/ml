/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.distancemeasure;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.ClusterableDoubleArray;
import org.apache.log4j.Logger;

/**
 * @version 1.0
 */
public class EuclideanDistance implements DissimilarityMeasure<ClusterableDoubleArray>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(EuclideanDistance.class);

	private static final EuclideanDistance _instance = new EuclideanDistance();


// -------------------------- STATIC METHODS --------------------------

	public static EuclideanDistance getInstance()
		{
		return _instance;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface DissimilarityMeasure ---------------------

	/**
	 * {@inheritDoc}
	 */
	public double distanceFromTo(final ClusterableDoubleArray a, final ClusterableDoubleArray b)
		{
		double sum = 0;
		final int l = a.length();
		for (int i = 0; i < l; i++)
			{
			double x = a.get(i);
			double y = b.get(i);
			sum += (x - y) * (x - y);
			}
		return Math.sqrt(sum);
		}
	}
