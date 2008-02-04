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

import com.davidsoergel.dsutils.MathUtils;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

public class SimplexVectorTargetAndProportion<T>// implements SequenceSpectrum<RelativeMarkovTreeNode>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(SimplexVectorTargetAndProportion.class);
	Multinomial<T> target = new Multinomial<T>();
	double mixingProportion;
	//	protected MarkovTreeNode backoffParent;


	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new RelativeMarkovTreeNode by comparing one MarkovTreeNode with another and representing the difference
	 * as a vector on the simplex.  A PSA can be constructed from these where each node is represented the difference
	 * between the multinomial at the node and its (backoff) prior expectation.
	 * <p/>
	 * //@param currentNode   a MarkovTreeNode containing a multinomial //@param backoffParent the MarkovTreeNode
	 * containing a multinomial to which the currentNode should be compared; //                    typically the backoff
	 * prior in our case.
	 */
	public SimplexVectorTargetAndProportion(Multinomial<T> fromDist,
	                                        Multinomial<T> toDist)//MarkovTreeNode currentNode, MarkovTreeNode backoffParent)
		{
		//	this.backoffParent = backoffParent;
		//	Multinomial<Byte> childProbs = currentNode.getProbs();
		//	Multinomial<Byte> parentProbs = backoffParent.getProbs();

		//byte zeroSymbol = 0;
		mixingProportion = 0;
		//double maxSymbol = 0;
		try
			{
			// see which symbol probability would hit zero first if we keep going in the same direction
			for (T b : toDist.getElements())
				{
				double alpha = 1 - (toDist.get(b) / fromDist.get(b));

				if (mixingProportion < alpha && alpha <= 1)
					{
					mixingProportion = alpha;
					//	zeroSymbol = b;
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
				for (T b : toDist.getElements())
					{
					double targetVal;
					/*	if (b == zeroSymbol)
					   {

					   targetVal = 0;
					   }
				   else
					   {*/
					targetVal = (1 / mixingProportion) * toDist.get(b) + (1 - (1 / mixingProportion)) * fromDist
							.get(b);
					//	}
					// avoid infinitesimal negative values due to numerical imprecision
					if (MathUtils.equalWithinFPError(targetVal, 0))
						{
						targetVal = 0;
						}
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

	// --------------------- GETTER / SETTER METHODS ---------------------

	public double getMixingProportion()
		{
		return mixingProportion;
		}

	public Multinomial<T> getTarget()
		{
		return target;
		}
	}
