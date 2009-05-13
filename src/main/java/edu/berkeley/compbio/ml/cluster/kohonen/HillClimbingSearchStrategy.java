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

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class HillClimbingSearchStrategy<T extends AdditiveClusterable<T>> extends KohonenSOM2DSearchStrategy<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(HillClimbingSearchStrategy.class);

	/*	private void setSearchRadius(int i)
		 {
		 searchRadius = i;
		 }
 */ KohonenSOM2DSearchStrategy<T> fallbackStrategy = new CoarseGridSearchStrategy<T>();


// -------------------------- OTHER METHODS --------------------------

	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	@Override
	public ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		ClusterMove<T, KohonenSOMCell<T>> result = new ClusterMove<T, KohonenSOMCell<T>>();

		String id = p.getId();
		result.oldCluster = som.getAssignment(id);

		if (result.oldCluster == null)
			{
			return fallbackStrategy.bestClusterMove(p);
			}

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		KohonenSOM2D<T>.WeightedMask mask = som.getWeightedMask((int) getSearchRadius());


		Set<CentroidCluster<T>> alreadyTested = new HashSet<CentroidCluster<T>>(10);

		result.bestCluster = result.oldCluster;
		result.bestDistance = measure.distanceFromTo(p, result.bestCluster.getCentroid());
		alreadyTested.add(result.bestCluster);
		boolean changed = true;

		while (changed)
			{
			changed = false;
			for (Iterator<KohonenSOM2D<T>.WeightedCell> i = mask.iterator((KohonenSOMCell<T>) result.bestCluster);
			     i.hasNext();)
				{
				KohonenSOMCell<T> c = i.next().theCell;
				if (!alreadyTested.contains(c))
					{
					alreadyTested.add(c);
					double d = measure.distanceFromTo(p, c.getCentroid());//c.distanceToCentroid(p);
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

		if (logger.isTraceEnabled())
			{
			logger.trace("Chose " + result.bestCluster);
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
		return 3;
		}

	@Override
	public void setDistanceMeasure(DissimilarityMeasure<T> dissimilarityMeasure)
		{
		super.setDistanceMeasure(dissimilarityMeasure);
		fallbackStrategy.setDistanceMeasure(dissimilarityMeasure);
		}

	//** @Property
	//private final int gridSpacing = 4;


	//private int searchRadius;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSOM(KohonenSOM2D<T> som)
		{
		super.setSOM(som);

		fallbackStrategy.setSOM(som);
		//** @Property
		//setSearchRadius(8);
		}
	}
