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

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: UPGMA.java 488 2009-07-29 01:05:07Z soergel $
 */

public class AverageLinkageAgglomerator<T extends Clusterable<T>> extends Agglomerator<T>
	{
	private static final Logger logger = Logger.getLogger(AverageLinkageAgglomerator.class);


	protected void addCompositeVsNodeToDistanceMatrix(final HierarchicalCentroidCluster<T> origA,
	                                                  final HierarchicalCentroidCluster<T> origB,
	                                                  final HierarchicalCentroidCluster<T> composite,
	                                                  final HierarchicalCentroidCluster<T> otherNode,
	                                                  final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix)
		{
		if (otherNode == origA || otherNode == origB || otherNode == composite)
			{
			// ignore
			}
		else
			{
			double d = (origA.getWeight() / composite.getWeight()) * theActiveNodeDistanceMatrix.get(origA, otherNode)
			           + (origB.getWeight() / composite.getWeight()) * theActiveNodeDistanceMatrix
					.get(origB, otherNode);
			theActiveNodeDistanceMatrix.put(otherNode, composite, d);

			/*	int numKeys = theActiveNodeDistanceMatrix.getActiveKeys().size();

		   if (numKeys % 1000 == 0)
			   {
			   int numPairs = theActiveNodeDistanceMatrix.numPairs();
			   logger.info("Single-linkage training " + numKeys + " active nodes, " + numPairs + " pair distances");
			   }*/
			}
		}
	}
