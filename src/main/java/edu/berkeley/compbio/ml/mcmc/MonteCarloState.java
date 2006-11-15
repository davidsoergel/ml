package edu.berkeley.compbio.ml.mcmc;

/**
 * @author lorax
 * @version 1.0
 */
public interface MonteCarloState
	{
	public abstract void init();

	public abstract void writeToDataCollector(int step, DataCollector dc);

	double unnormalizedLogLikelihood();
	}
