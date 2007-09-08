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

package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import com.davidsoergel.runutils.ThreadLocalRun;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.DistributionProcessor;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

@PropertyConsumer
public class KneserNeyPSTSmoother implements DistributionProcessor<RonPST>
	{
	private static Logger logger = Logger.getLogger(KneserNeyPSTSmoother.class);

	@Property(helpmessage = "Smoothing factor", defaultvalue = "0.1")
	public Double smoothFactor;
	private double smoothFactorTimesFour;// = smoothFactor * 4;

	public KneserNeyPSTSmoother()//double smoothFactor)
		{
		//this.smoothFactor = smoothFactor;
		ThreadLocalRun.getProps().injectProperties(this);
		smoothFactorTimesFour = smoothFactor * 4;
		}

	public void process(RonPST ronPST)
		{
		// mix the root with the uniform distribution
		Multinomial<Byte> uniform = new Multinomial<Byte>();
		try
			{
			for (byte b : ronPST.getAlphabet())
				{
				uniform.put(b, 1);
				}
			uniform.normalize();

			ronPST.getProbs().mixIn(uniform, smoothFactorTimesFour);

			// do the rest of the tree, breadth first

			List<MarkovTreeNode> nodesRemaining = new LinkedList<MarkovTreeNode>();
			nodesRemaining.addAll(ronPST.getChildren().values());

			while (!nodesRemaining.isEmpty())
				{
				MarkovTreeNode node = nodesRemaining.remove(0);
				smooth(node, ronPST);
				nodesRemaining.addAll(node.getChildren().values());
				}

			}
		catch (DistributionException e)
			{
			logger.debug(e);
			throw new Error(e);
			}
		}

	private void smooth(MarkovTreeNode node, RonPST ronPST) throws DistributionException
		{
		node.getProbs().mixIn(ronPST.getBackoffPrior(node.getIdBytes()).getProbs(), smoothFactorTimesFour);
		}
	}
