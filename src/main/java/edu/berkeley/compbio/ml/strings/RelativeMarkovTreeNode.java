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

import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

public class RelativeMarkovTreeNode// implements SequenceSpectrum<RelativeMarkovTreeNode>
	{
	private static Logger logger = Logger.getLogger(RelativeMarkovTreeNode.class);
	protected MarkovTreeNode backoffParent;
	Multinomial<Byte> target = new Multinomial<Byte>();
	double mixingProportion;


	/**
	 * Constructs a new RelativeMarkovTreeNode by comparing one MarkovTreeNode with another and representing the difference
	 * as a vector on the simplex.  A PST can be constructed from these where each node is represented the difference
	 * between the multinomial at the node and its (backoff) prior expectation.
	 *
	 * @param currentNode   a MarkovTreeNode containing a multinomial
	 * @param backoffParent the MarkovTreeNode containing a multinomial to which the currentNode should be compared;
	 *                      typically the backoff prior in our case.
	 */
	public RelativeMarkovTreeNode(MarkovTreeNode currentNode, MarkovTreeNode backoffParent)
		{
		this.backoffParent = backoffParent;
		Multinomial<Byte> childProbs = currentNode.getProbs();
		Multinomial<Byte> parentProbs = backoffParent.getProbs();


		mixingProportion = 0;
		//double maxSymbol = 0;
		try
			{
			// see which symbol probability would hit zero first if we keep going in the same direction
			for (byte b : childProbs.getElements())
				{
				double alpha = 1 - (childProbs.get(b) / parentProbs.get(b));

				if (mixingProportion < alpha && alpha <= 1)
					{
					mixingProportion = alpha;
					//maxSymbol = b;
					}
				}

			// then find the target probabilities
			if (mixingProportion == 0)
				{
				// distributions are identical
				target = null;
				}
			else
				{
				for (byte b : childProbs.getElements())
					{
					double targetVal =
							(1 / mixingProportion) * childProbs.get(b) + (1 - (1 / mixingProportion)) * parentProbs
									.get(b);

					target.put(b, targetVal);
					}
				if (!target.isAlreadyNormalized())
					{
					throw new DistributionException("Failed to compute conditional bias target distribution correctly");
					}
				}
			}
		catch (DistributionException e)
			{
			logger.error(e);
			e.printStackTrace();
			//	throw new Error(e);
			}
		}

	public Multinomial<Byte> getTarget()
		{
		return target;
		}

	public double getMixingProportion()
		{
		return mixingProportion;
		}
	}
