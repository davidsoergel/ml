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
