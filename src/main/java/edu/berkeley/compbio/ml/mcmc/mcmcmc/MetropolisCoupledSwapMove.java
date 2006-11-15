package edu.berkeley.compbio.ml.mcmc.mcmcmc;

import com.davidsoergel.dsutils.MersenneTwisterFast;
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
	private static Logger logger = Logger.getLogger(MetropolisCoupledSwapMove.class);

	private static MersenneTwisterFast mtf = new MersenneTwisterFast();

	private ChainList chains;
	private int swap1, swap2;


	public MetropolisCoupledSwapMove(ChainList cl)
		{
		chains = cl;

		propose();

		}

	public void propose()
		{

		// ** is it OK to swap only adjacent temperatures?
		// Yes!
		swap1 = mtf.nextInt(chains.size() - 1);
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

	public ChainList doMove()
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
			}
		return chains;
		}

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


		logger.debug(String.format("Swap log likelihood components: (%f * %f) / (%f * %f)",
		                           mc1.unnormalizedLogLikelihood(mc2.getCurrentState()),
		                           mc2.unnormalizedLogLikelihood(mc1.getCurrentState()),
		                           mc1.unnormalizedLogLikelihood(mc1.getCurrentState()),
		                           mc2.unnormalizedLogLikelihood(mc2.getCurrentState())));

		logger.debug("swapLogLikelihoodRatio = " + swapLogLikelihoodRatio);

		double swapProbability = Math.exp(swapLogLikelihoodRatio);
		logger.debug("Swap probability = " + swapProbability);

		return mtf.nextDouble() < swapProbability;

		}
	}


