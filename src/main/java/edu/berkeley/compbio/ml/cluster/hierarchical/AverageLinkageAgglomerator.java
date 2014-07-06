/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: UPGMA.java 488 2009-07-29 01:05:07Z soergel $
 */

public class AverageLinkageAgglomerator<T extends AdditiveClusterable<T>> extends ExplicitAgglomerator<T>
	{
	private static final Logger logger = Logger.getLogger(AverageLinkageAgglomerator.class);


	protected void addCompositeVsNodeToDistanceMatrix( final HierarchicalCentroidCluster<T> origA, final HierarchicalCentroidCluster<T> origB, final HierarchicalCentroidCluster<T> composite,
	                                                   final HierarchicalCentroidCluster<T> otherNode, final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix )
		{
		if (otherNode == origA || otherNode == origB || otherNode == composite)
			{
			// ignore
			}
		else
			{
			float d =
					(float) ((origA.getWeight() / composite.getWeight()) * theActiveNodeDistanceMatrix.get(origA, otherNode) + (origB.getWeight() / composite.getWeight()) *
					                                                                                                           theActiveNodeDistanceMatrix
							.get(origB, otherNode));
			theActiveNodeDistanceMatrix.put(otherNode, composite, d);

			/*	int numKeys = theActiveNodeDistanceMatrix.getActiveKeys().size();

		   if (numKeys % 1000 == 0)
			   {
			   int numPairs = theActiveNodeDistanceMatrix.numPairs();
			   logger.info("Single-linkage training " + numKeys + " active nodes, " + numPairs + " pair distances");
			   }*/
			}
		}
/*
	@Override protected T makeCompositeCentroid( final HierarchicalCentroidCluster<T> origA, final HierarchicalCentroidCluster<T> origB ) {
	int an = origA.getN();
	int bn = origB.getN();
	//return origA.getCentroid().times(an).plus(origB.getCentroid().times(bn)).times((1. / (an + bn)));
	return origA.getCentroid().weightedMixture(origB.getCentroid(), an, bn);
	}*/
	}
