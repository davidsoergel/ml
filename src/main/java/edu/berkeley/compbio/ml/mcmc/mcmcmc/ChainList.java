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

import edu.berkeley.compbio.ml.mcmc.DataCollector;
import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.MonteCarloState;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * @version 1.0
 */
public class ChainList extends ArrayList<MonteCarlo> implements MonteCarloState
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(ChainList.class);


// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MonteCarloState ---------------------

	/**
	 * {@inheritDoc}
	 */
	public double unnormalizedLogLikelihood()
		{
		return 1;// never used, irrelevant
		}

	/**
	 * {@inheritDoc}
	 */
	public void writeToDataCollector(final int step, final DataCollector dc)
		{
		// ignore, this never happens
		}

// -------------------------- OTHER METHODS --------------------------

	public void init()
		{
		//To change body of implemented methods use File | Settings | File Templates.
		}
	}
