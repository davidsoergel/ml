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


package edu.berkeley.compbio.ml.mcmc.mcmcmc;

import com.davidsoergel.dsutils.math.MersenneTwisterFast;
import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.Move;
import edu.berkeley.compbio.ml.mcmc.ProbabilityMove;
import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
public class MetropolisCoupledSwapMove extends Move implements ProbabilityMove
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(MetropolisCoupledSwapMove.class);

	//private static MersenneTwisterFast mtf = new MersenneTwisterFast();

	private ChainList chains;
	private int swap1, swap2;


	// --------------------------- CONSTRUCTORS ---------------------------

	public MetropolisCoupledSwapMove(ChainList cl)
		{
		chains = cl;

		propose();
		}

	public void propose()
		{
		// REVIEW is it OK to swap only adjacent temperatures?
		// Yes!
		swap1 = MersenneTwisterFast.randomInt(chains.size() - 1);
		swap2 = swap1 + 1;

		// This proposal swaps everything around at random, which may produce a low acceptance rate.
		/*
				swap1 = mtf.nextInt(chains.size());
				do
					{
					swap2 = mtf.nextInt(chains.size());
					}
				while (swap2 == swap1);*/
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface ProbabilityMove ---------------------

	public ChainList doMove(double temperature)
		{
		if (isAccepted())
			{
			MonteCarlo mc1 = chains.get(swap1);
			MonteCarlo mc2 = chains.get(swap2);

			logger.debug("SWAPPING CHAINS " + swap1 + " (" + mc1.getHeatFactor() + ") " + swap2 + " ("
					+ mc2.getHeatFactor() + ") ");
			double temp = mc1.getHeatFactor();
			mc1.setHeatFactor(mc2.getHeatFactor());
			mc2.setHeatFactor(temp);

			if (mc1.isColdest())
				{
				mc1.setColdest(false);
				mc2.setColdest(true);
				}
			else if (mc2.isColdest())
				{
				mc2.setColdest(false);
				mc1.setColdest(true);
				}

			// hack to make the mcmc realize that the move was accepted
			ChainList newChains = new ChainList();
			newChains.addAll(chains);
			chains = newChains;
			}
		return chains;
		}

	// -------------------------- OTHER METHODS --------------------------

	private boolean isAccepted()
		{
		MonteCarlo mc1 = chains.get(swap1);
		MonteCarlo mc2 = chains.get(swap2);

		//		double stateProbabilityRatio = (mc1.unnormalizedLikelihood(mc2.getCurrentState()) * mc2.unnormalizedLikelihood(mc1.getCurrentState()))
		//				/ (mc1.unnormalizedLikelihood(mc1.getCurrentState()) * mc2.unnormalizedLikelihood(mc2.getCurrentState()));

		//double totalProbability = stateProbabilityRatio * proposalProbabilityRatio;
		// all proposals are equally likely; no need for the Hastings term

		double swapLogLikelihoodRatio = (mc1.unnormalizedLogLikelihood(mc2.getCurrentState()) + mc2
				.unnormalizedLogLikelihood(mc1.getCurrentState())) - (
				mc1.unnormalizedLogLikelihood(mc1.getCurrentState()) + mc2
						.unnormalizedLogLikelihood(mc2.getCurrentState()));


		if (logger.isDebugEnabled())
			{
			logger.debug(String.format("Swap log likelihood components: (%f * %f) / (%f * %f)",
			                           mc1.unnormalizedLogLikelihood(mc2.getCurrentState()),
			                           mc2.unnormalizedLogLikelihood(mc1.getCurrentState()),
			                           mc1.unnormalizedLogLikelihood(mc1.getCurrentState()),
			                           mc2.unnormalizedLogLikelihood(mc2.getCurrentState())));

			logger.debug("swapLogLikelihoodRatio = " + swapLogLikelihoodRatio);
			}

		//double swapProbability = Math.exp(swapLogLikelihoodRatio);
		//logger.debug("Swap probability = " + swapProbability);

		//return mtf.nextDouble() < swapProbability;
		return Math.log(MersenneTwisterFast.random()) < swapLogLikelihoodRatio;
		}
	}


