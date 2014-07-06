/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import com.davidsoergel.dsutils.collections.ConcurrentHashWeightedSet;
import com.davidsoergel.dsutils.collections.ImmutableHashWeightedSet;
import com.davidsoergel.dsutils.collections.MutableWeightedSet;
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
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(NeighborhoodCollectingLabelDiffuser.class);
	final int requiredLabels;


// --------------------------- CONSTRUCTORS ---------------------------

	public NeighborhoodCollectingLabelDiffuser(final int requiredLabels) //, double labelRetainThreshold)
		{
		this.requiredLabels = requiredLabels;
//		this.labelRetainThreshold = labelRetainThreshold;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface LabelDiffuser ---------------------

	/**
	 * {@inheritDoc}
	 */
	public void propagateLabels(final DiffusableLabelClusteringMethod<T, C> theMap)
		{
		int i = 0;
		for (final C cell : theMap.getClusters())
			{
			final MutableWeightedSet<String> weightedLabels = new ConcurrentHashWeightedSet<String>();
			final Iterator<Set<C>> shells = theMap.getNeighborhoodShellIterator(cell);

			while (weightedLabels.getItemCount() < requiredLabels)
				{
				for (final CentroidCluster<T> shellMember : shells.next())
					{
					weightedLabels.addAll(shellMember.getMutableWeightedLabels());
					}
				}

			//try
			//	{
			// PERF maybe we don't want to copy the weights so much?  The alternative is to let the derived weights be mutable
			cell.setDerivedLabelProbabilities(new ImmutableHashWeightedSet<String>(weightedLabels));
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
