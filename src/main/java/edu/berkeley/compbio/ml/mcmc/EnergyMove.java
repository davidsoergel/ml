package edu.berkeley.compbio.ml.mcmc;

/**
 * @author lorax
 * @version 1.0
 */
public interface EnergyMove
	{
	public abstract MonteCarloState doMove(double temperature);
	}
