/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
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
