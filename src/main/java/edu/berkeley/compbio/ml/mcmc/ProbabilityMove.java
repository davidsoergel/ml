package edu.berkeley.compbio.ml.mcmc;

/**
 * @author lorax
 * @version 1.0
 */
public interface ProbabilityMove
	{
	public abstract MonteCarloState doMove();
	}
