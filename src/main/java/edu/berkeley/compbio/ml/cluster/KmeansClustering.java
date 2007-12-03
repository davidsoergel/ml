/* $Id$ */

/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * @author lorax
 * @version 1.0
 */
public class KmeansClustering<T extends AdditiveClusterable<T>> extends OnlineClusteringMethod<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(KmeansClustering.class);


	// --------------------------- CONSTRUCTORS ---------------------------

	//private int k;

	public KmeansClustering(Iterator<T> dpp, int k, DistanceMeasure<T> dm) throws CloneNotSupportedException
		{
		//super(dpp);
		//this.k = k;

		for (int i = 0; i < k; i++)
			{
			// initialize the clusters with the first k points

			Cluster<T> c = new AdditiveCluster<T>(dm, dpp.next());
			c.setId(i);

			theClusters.add(c);
			}
		logger.debug("initialized " + k + " clusters");
		}

	// -------------------------- OTHER METHODS --------------------------

	public int getBestCluster(T p, List<Double> secondBestDistances)
		{
		ClusterMove cm = bestClusterMove(p);
		return theClusters.indexOf(cm.bestCluster);
		}

	public boolean add(T p, List<Double> secondBestDistances)
		{
		assert p != null;
		//n++;
		String id = p.getId();
		ClusterMove cm = bestClusterMove(p);
		secondBestDistances.add(cm.secondBestDistance);
		if (cm.changed())
			{
			try
				{
				cm.oldCluster.recenterByRemoving(p);//, cm.oldDistance);
				}
			catch (NullPointerException e)
				{// probably just the first round
				}
			cm.bestCluster
					.recenterByAdding(
							p);//, cm.bestDistance);  // this will automatically recalculate the centroid, etc.
			assignments.put(id, cm.bestCluster);
			return true;
			}
		return false;
		}

	public ClusterMove bestClusterMove(T p)
		{
		ClusterMove result = new ClusterMove();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		String id = p.getId();
		result.oldCluster = assignments.get(id);

		if (logger.isDebugEnabled())
			{
			logger.debug("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (Cluster<T> c : theClusters)
			{
			double d = c.distanceToCentroid(p);
			if (logger.isDebugEnabled())
				{
				logger.debug("Trying " + c + "; distance = " + d + "; best so far = " + result.bestDistance);
				}
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
		if (logger.isDebugEnabled())
			{
			logger.debug("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			logger.warn("Can't classify: " + p);
			assert false;
			}
		return result;
		}

	/*	public void addAndRecenter(T p)
	   {
	   assert p != null;
	   bestCluster(theClusters, p).addAndRecenter(p);  // this will automatically recalculate the centroid, etc.
	   }*/
	}
