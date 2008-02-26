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

package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.DistributionProcessor;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

@PropertyConsumer
public class RonPSASmoother implements DistributionProcessor<RonPSA>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(RonPSASmoother.class);

	@Property(helpmessage = "Smoothing factor (aka gammaMin)", defaultvalue = "0.01")
	public Double smoothFactor;

	// --------------------------- CONSTRUCTORS ---------------------------

	public RonPSASmoother()//String injectorId)//double smoothFactor)
		{
		//this.smoothFactor = smoothFactor;
		//ThreadLocalRun.getProps().injectProperties(injectorId, this);
		}

	// ------------------------ INTERFACE METHODS ------------------------

	// --------------------- Interface DistributionProcessor ---------------------

	public void process(RonPSA ronPSA)
		{
		try
			{
			// breadth first  (for no reason, just symmetry with the KneserNeyPSTSmoother where it is important)

			List<MarkovTreeNode> nodesRemaining = new LinkedList<MarkovTreeNode>();
			nodesRemaining.add(ronPSA);

			while (!nodesRemaining.isEmpty())
				{
				MarkovTreeNode node = nodesRemaining.remove(0);
				node.getProbs().redistributeWithMinimum(smoothFactor);

				for (MarkovTreeNode n : node.getChildren())
					{
					if (n != null)
						{
						nodesRemaining.add(n);
						}
					}
				}
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			throw new Error(e);
			}
		}

	// -------------------------- OTHER METHODS --------------------------
	}