package edu.berkeley.compbio.ml.mcmc.mcmcmc;

import edu.berkeley.compbio.ml.mcmc.DataCollector;
import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.MonteCarloState;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * @author lorax
 * @version 1.0
 */
public class ChainList extends ArrayList<MonteCarlo> implements MonteCarloState
	{
	private static Logger logger = Logger.getLogger(ChainList.class);

	public void init()
		{
		//To change body of implemented methods use File | Settings | File Templates.
		}

	public void writeToDataCollector(int step, DataCollector dc)
		{
		// ignore, this never happens
		}

	public double unnormalizedLogLikelihood()
		{
		return 1;  // never used, irrelevant
		}
	}
