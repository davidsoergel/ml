/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc;

/**
 * Represents a move from one state to another in a Monte Carlo simulation.  Such a move is proposed, but may or may not
 * actually occur, depending on the probabilities of the two states.
 * <p/>
 * A move based purely on relative probabilities would not respond to "temperature", but we do allow moves to take
 * temperature into account by performing some transformation on the probabilities.  At a temperature of 1, the
 * probability ratio is used directly; higher temperatures cause moves to be accepted that otherwise would not be.  This
 * makes ProbabilityMoves behave much like EnergyMoves. However, some ProbabilityMoves do not actually respond to
 * temperature; the argument to toMove() is just ignored.  This situation that is confusing and may need some
 * rethinking.
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface ProbabilityMove<T extends MonteCarloState>
	{
// -------------------------- OTHER METHODS --------------------------

	//abstract T doMove();

	/**
	 * Decide whether or not to accept this move, and if so, perform it.
	 *
	 * @param temperature the temperature of the chain; higher temperatures increase the move acceptance rate
	 * @return the MonteCarloState arrived at after this move; if the move was rejected, this is the same as as the state
	 *         before the move.
	 */
	abstract T doMove(double temperature);
	}
