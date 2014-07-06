/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class BruteForceSearchStrategy<T extends AdditiveClusterable<T>> extends KohonenSOM2DSearchStrategy<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(BruteForceSearchStrategy.class);


// -------------------------- OTHER METHODS --------------------------

	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	@Override
	public ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(final T p)
		{
		final ClusterMove<T, KohonenSOMCell<T>> result = new ClusterMove<T, KohonenSOMCell<T>>();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		final String id = p.getId();
		result.oldCluster = som.getAssignment(id);

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (final KohonenSOMCell<T> c : som.getClusters())
			{
			// grid already initialized with prototype, never mind all this stuff

			/*
				  // while initializing the grid, cell centroids are null.  In that case, just assign the present point.
				  // no, this won't work right at all
				  // why not?? PCA would be better, but this should work, just slowly.
				  //  aha: if there are more grid points than samples
				  if (c.getCentroid() == null)
					  {
					  c.setCentroid(p.clone());
					  result.bestDistance = 0;
					  result.bestCluster = c;
					  return result;
					  }
	  */
			// otherwise find the nearest cluster
			final double d = measure.distanceFromTo(p, c.getCentroid());//c.distanceToCentroid(p);
			/*	if (logger.isDebugEnabled())
			   {
			   logger.debug("Trying " + c + "; distance = " + d + "; best so far = " + result.bestDistance);
			   }*/
			if (d < result.bestDistance)
				{
				result.secondBestDistance = result.bestDistance;
				result.bestDistance = d;
				result.bestCluster = c;
				}
			else if (d < result.secondBestDistance)
				{
				result.secondBestDistance = d;
				}
			}
		if (logger.isTraceEnabled())
			{
			logger.trace("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			logger.warn("Can't classify: " + p);
			assert false;
			}
		return result;
		}
	}
