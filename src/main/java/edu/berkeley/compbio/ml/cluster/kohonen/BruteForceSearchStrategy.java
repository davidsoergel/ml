/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
	private static final Logger logger = Logger.getLogger(BruteForceSearchStrategy.class);


	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	@Override
	public ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(T p)
		{
		ClusterMove<T, KohonenSOMCell<T>> result = new ClusterMove<T, KohonenSOMCell<T>>();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		String id = p.getId();
		result.oldCluster = som.getAssignment(id);

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (KohonenSOMCell<T> c : som.getClusters())
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
			double d = measure.distanceFromTo(c.getCentroid(), p);//c.distanceToCentroid(p);
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
