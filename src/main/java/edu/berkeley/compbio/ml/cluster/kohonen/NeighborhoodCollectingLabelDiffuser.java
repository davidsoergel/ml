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

import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Set;


/**
 * Label each cell in a Kohonen SOM with label proportions according to the label counts that are present in the cell,
 * or in the smallest neighborhood around the cell including a given number of real data points.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 * @Version 1.0
 */
public class NeighborhoodCollectingLabelDiffuser<T extends AdditiveClusterable<T>, C extends CentroidCluster<T>>
		implements LabelDiffuser<T, C>
	{
	private static final Logger logger = Logger.getLogger(NeighborhoodCollectingLabelDiffuser.class);
	int requiredLabels;

	public NeighborhoodCollectingLabelDiffuser(int requiredLabels) //, double labelRetainThreshold)
		{
		this.requiredLabels = requiredLabels;
//		this.labelRetainThreshold = labelRetainThreshold;
		}

	/**
	 * {@inheritDoc}
	 */
	public void propagateLabels(DiffusableLabelClusteringMethod<T, C> theMap)
		{
		int i = 0;
		for (C cell : theMap.getClusters())
			{
			WeightedSet<String> weightedLabels = new HashWeightedSet<String>();
			Iterator<Set<C>> shells = theMap.getNeighborhoodShellIterator(cell);

			while (weightedLabels.getWeightSum() < requiredLabels)
				{
				for (CentroidCluster<T> shellMember : shells.next())
					{
					weightedLabels.addAll(shellMember.getWeightedLabels());
					}
				}

			//try
			//	{
			cell.setDerivedLabelProbabilities(weightedLabels);
			/*	}
			catch (DistributionException e)
				{
				logger.warn("Empty bag?", e);
				cell.setDerivedLabelProbabilities(null);
				}*/
			if (i % 1000 == 0)
				{
				logger.debug("Relabeled " + i + " nodes.");
				}
			i++;
			}
		}
	}
