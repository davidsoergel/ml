/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc.mcmcmc;

import com.davidsoergel.dsutils.math.MersenneTwisterFast;
import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.Move;
import edu.berkeley.compbio.ml.mcmc.ProbabilityMove;
import org.apache.log4j.Logger;

/**
 * @version 1.0
 */
public class MetropolisCoupledSwapMove extends Move implements ProbabilityMove
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MetropolisCoupledSwapMove.class);

	//private static MersenneTwisterFast mtf = new MersenneTwisterFast();

	private ChainList chains;
	private int swap1, swap2;


// --------------------------- CONSTRUCTORS ---------------------------

	public MetropolisCoupledSwapMove(final ChainList cl)
		{
		chains = cl;

		propose();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void propose()
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

	/**
	 * {@inheritDoc}
	 */
	public ChainList doMove(final double temperature)
		{
		if (isAccepted())
			{
			final MonteCarlo mc1 = chains.get(swap1);
			final MonteCarlo mc2 = chains.get(swap2);

			logger.debug(
					"SWAPPING CHAINS " + swap1 + " (" + mc1.getHeatFactor() + ") " + swap2 + " (" + mc2.getHeatFactor()
					+ ") ");
			final double temp = mc1.getHeatFactor();
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
			final ChainList newChains = new ChainList();
			newChains.addAll(chains);
			chains = newChains;
			}
		return chains;
		}

// -------------------------- OTHER METHODS --------------------------

	private boolean isAccepted()
		{
		final MonteCarlo mc1 = chains.get(swap1);
		final MonteCarlo mc2 = chains.get(swap2);

		//		double stateProbabilityRatio = (mc1.unnormalizedLikelihood(mc2.getCurrentState()) * mc2.unnormalizedLikelihood(mc1.getCurrentState()))
		//				/ (mc1.unnormalizedLikelihood(mc1.getCurrentState()) * mc2.unnormalizedLikelihood(mc2.getCurrentState()));

		//double totalProbability = stateProbabilityRatio * proposalProbabilityRatio;
		// all proposals are equally likely; no need for the Hastings term

		final double swapLogLikelihoodRatio = (mc1.unnormalizedLogLikelihood(mc2.getCurrentState()) + mc2
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
