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

package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.DistributionProcessor;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

@PropertyConsumer
public class KneserNeyPSASmoother implements DistributionProcessor<RonPSA>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KneserNeyPSASmoother.class);

	@Property(helpmessage = "Smoothing factor", defaultvalue = "0.1")
	public Double smoothFactor;
	private double smoothFactorTimesFour;// = smoothFactor * 4;


	// --------------------------- CONSTRUCTORS ---------------------------

	public KneserNeyPSASmoother()//String injectorId)//double smoothFactor)
		{
		//this.smoothFactor = smoothFactor;
		//ResultsCollectingProgramRun.getProps().injectProperties(injectorId, this);
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface DistributionProcessor ---------------------

	public void process(RonPSA ronPSA)
		{
		// mix the root with the uniform distribution
		Multinomial<Byte> uniform = new Multinomial<Byte>();
		try
			{
			for (byte b : ronPSA.getAlphabet())
				{
				uniform.put(b, 1);
				}
			uniform.normalize();

			ronPSA.getProbs().mixIn(uniform, smoothFactorTimesFour);

			// do the rest of the tree, breadth first

			List<RonPSANode> nodesRemaining = new LinkedList<RonPSANode>();
			for (MarkovTreeNode n : ronPSA.getChildren())
				{
				if (n != null)
					{
					nodesRemaining.add((RonPSANode) n);
					}
				}

			while (!nodesRemaining.isEmpty())
				{
				RonPSANode node = nodesRemaining.remove(0);
				smooth(node);//, ronPST);
				//	nodesRemaining.addAll(node.getChildren());//.values());
				for (MarkovTreeNode n : node.getChildren())
					{
					if (n != null)
						{
						nodesRemaining.add((RonPSANode) n);
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

	public void init()
		{
		smoothFactorTimesFour = smoothFactor * 4;
		}

	private void smooth(RonPSANode node) throws DistributionException
		{
		node.getProbs().mixIn(node.getBackoffPrior().getProbs(), smoothFactorTimesFour);

		//node.getProbs().mixIn(ronPST.getBackoffPrior(node.getIdBytes()).getProbs(), smoothFactorTimesFour);
		}
	}
