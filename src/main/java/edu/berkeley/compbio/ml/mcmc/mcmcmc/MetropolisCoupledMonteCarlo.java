/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc.mcmcmc;

import com.davidsoergel.dsutils.DSStringUtils;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.MonteCarloState;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

@PropertyConsumer
public class MetropolisCoupledMonteCarlo extends MonteCarlo
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MetropolisCoupledMonteCarlo.class);

	//@Property(helpmessage = "", defaultvalue = "edu.berkeley.compbio.ml.mcmc.MonteCarloFactory")
	//public MonteCarloFactory mcf;
	@Property(helpmessage = "", defaultvalue = "edu.berkeley.compbio.ml.mcmc.MonteCarlo")
	public GenericFactory<MonteCarlo> chainFactory;

	@Property(helpmessage = "heat factors for each subchain.", defaultvalue = "1,2,4,8,16,32")
	public Double[] heatFactors;

	//override to avoid presenting in GUI
	//	@Property(ignore = true, isNullable = true)
	//	public int writeToConsoleInterval;

	//override to avoid presenting in GUI
	//	@Property(ignore = true, isNullable = true)
	//	public int collectDataToDiskInterval;


	//@Property(defaultvalue = "edu.berkeley.compbio.ml.mcmc.mcmcmc.ChainList")
	public ChainList currentState = new ChainList();


// --------------------------- CONSTRUCTORS ---------------------------

	public MetropolisCoupledMonteCarlo()
		{
		super();
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChainList getCurrentState()
		{
		return currentState;
		}

// -------------------------- OTHER METHODS --------------------------

	//	@Property(helpmessage = "", defaultvalue = "10")
	//	public int swapInterval;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init()
		{
		super.init();
		//	super.setDataCollector(dataCollector);
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() throws IOException, GenericFactoryException
		{
		assert heatFactors[0] == 1;
		final ChainList chains = new ChainList();
		//	Object subChainSharedState = null;
		for (final double hf : heatFactors)
			{
			final MonteCarlo subChain = chainFactory.create();
			subChain.setHeatFactor(hf);
			subChain.setId(String.valueOf(hf));
			subChain.setDataCollector(dataCollector.newSubCollector(String.valueOf(hf)));
			subChain.setColdest(false);
			//	subChain.setChainSharedState(subChainSharedState);
			//	subChainSharedState = subChain.getChainSharedState();  // should be unchanged after the first
			chains.add(subChain);//mcf.newChain(hf));
			}

		final MonteCarlo mc = chains.get(0);
		mc.setColdest(true);

		//MetropolisCoupledMonteCarlo couplingChain = new MetropolisCoupledMonteCarlo();
		setCurrentChainList(chains);
		setColdest(true);// we do want output from the coupling chain
		setId("COUPLING");

		logger.debug("Initialized MCMCMC: " + DSStringUtils.join(heatFactors, ", "));
		//ArrayUtils.toObject(heatFactors)

		// burn in
		for (int i = 0; i < burnIn; i++)
			{
			doBurnInStep();
			}

		// do the real run
		for (int i = 0; i < numSteps; i++)
			{
			doStep();
			}
		}

	public void setCurrentChainList(final ChainList currentChainList)
		{
		this.currentState = currentChainList;
		}

	public void doBurnInStep() throws IOException, GenericFactoryException
		{
		// run each chain independently for a while
		// PERF parallelizable
		for (final MonteCarlo chain : getCurrentChainList())
			{
			chain.burnIn();
			/*	int maxStep = step + swapInterval;
		   for (int i = step; i < maxStep; i++)
			   {
			   chain.doStep(i);
			   }*/
			}

		// do the temperature swap attempt
		super.doStep();
		resetCounts();
		}

	public ChainList getCurrentChainList()
		{
		return currentState;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStep() throws IOException, GenericFactoryException
		{
		// run each chain independently for a while
		// PERF parallelizable
		for (final MonteCarlo chain : getCurrentChainList())
			{
			chain.runNoBurnIn();
			/*	int maxStep = step + swapInterval;
		   for (int i = step; i < maxStep; i++)
			   {
			   chain.doStep(i);
			   }*/
			}

		// do the temperature swap attempt
		super.doStep();
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentState(final MonteCarloState currentState)
		{
		this.currentState = (ChainList) currentState;
		}
	}
