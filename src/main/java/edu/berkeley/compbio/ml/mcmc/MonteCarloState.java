/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc;

/**
 * Interface for objects representing the current state of a Monte Carlo simulation.
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface MonteCarloState
	{
// -------------------------- OTHER METHODS --------------------------

	//	abstract void init();

	/**
	 * Returns the log likelihood of this state.  May be unnormalized; the only requirement for proper operation of the
	 * MCMC is that ratios of the values returned are log likelihood differences (i.e., log probability ratios).
	 *
	 * @return
	 */
	double unnormalizedLogLikelihood();

	/**
	 * store the current state (or some summary of it) in the provided data collector
	 *
	 * @param step the current step number of the simulation (i.e., time)
	 * @param dc   the DataCollector into which to record the present state
	 */
	abstract void writeToDataCollector(int step, DataCollector dc);
	}
