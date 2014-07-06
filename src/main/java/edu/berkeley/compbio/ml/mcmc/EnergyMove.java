/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc;

/**
 * Represents a move from one state to another in a Monte Carlo simulation.  Such a move is proposed, but may or may not
 * actually occur, depending on the energies of the two states and the current temperature.
 * <p/>
 * Typically the probability of a move will be exp(-deltaE / temperature).
 * <p/>
 * Note there is some confusion and redundancy between this and ProbabilityMove.
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @see ProbabilityMove
 */
public interface EnergyMove
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Decide whether or not to accept this move, and if so, perform it.
	 *
	 * @param temperature the temperature of the chain; higher temperatures increase the move acceptance rate
	 * @return the MonteCarloState arrived at after this move; if the move was rejected, this is the same as as the state
	 *         before the move.
	 */
	abstract MonteCarloState doMove(double temperature);
	}
