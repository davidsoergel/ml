/*
 * Copyright (c) 2008 Regents of the University of California
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

package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class HillClimbingSearchStrategy<T extends AdditiveClusterable<T>> implements KohonenSOM2DSearchStrategy<T>
	{
	private static final Logger logger = Logger.getLogger(HillClimbingSearchStrategy.class);

	//** @Property
	//private final int gridSpacing = 4;


	//private int searchRadius;
	protected KohonenSOM2D<T> som;

	public void setSOM(KohonenSOM2D<T> som)
		{
		this.som = som;

		fallbackStrategy.setSOM(som);
		//** @Property
		//setSearchRadius(8);
		}

	/*	private void setSearchRadius(int i)
		 {
		 searchRadius = i;
		 }
 */ KohonenSOM2DSearchStrategy<T> fallbackStrategy = new CoarseGridSearchStrategy<T>();


	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	public ClusterMove<T> bestClusterMove(T p) throws NoGoodClusterException
		{
		ClusterMove<T> result = new ClusterMove<T>();

		String id = p.getId();
		result.oldCluster = som.getAssignment(id);

		if (result.oldCluster == null)
			{
			return fallbackStrategy.bestClusterMove(p);
			}

		if (logger.isDebugEnabled())
			{
			logger.debug("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		KohonenSOM2D<T>.WeightedMask mask = som.getWeightedMask((int) getSearchRadius());


		Set<Cluster<T>> alreadyTested = new HashSet<Cluster<T>>(10);

		result.bestCluster = result.oldCluster;
		result.bestDistance = result.bestCluster.distanceToCentroid(p);
		alreadyTested.add(result.bestCluster);
		boolean changed = true;

		while (changed)
			{
			changed = false;
			for (Iterator<KohonenSOM2D<T>.WeightedCell> i = mask.iterator((KohonenSOMCell<T>) result.bestCluster);
			     i.hasNext();)
				{
				KohonenSOMCell c = i.next().theCell;
				if (!alreadyTested.contains(c))
					{
					alreadyTested.add(c);
					double d = c.distanceToCentroid(p);
					if (d < result.bestDistance)
						{
						result.secondBestDistance = result.bestDistance;
						result.bestDistance = d;
						result.bestCluster = c;
						changed = true;
						}
					else if (d < result.secondBestDistance)
						{
						result.secondBestDistance = d;
						}
					}
				}
			}

		if (logger.isDebugEnabled())
			{
			logger.debug("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			//logger.error("Can't classify: " + p);
			throw new NoGoodClusterException("No cluster found for " + p + ": " + result);
			}
		return result;
		}

	public double getSearchRadius()
		{
		return 2;
		}
	}
