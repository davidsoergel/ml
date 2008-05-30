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

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.MonteCarloState;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: lorax Date: Apr 29, 2004 Time: 4:46:42 PM To change this template use File | Settings
 * | File Templates.
 */
@PropertyConsumer
public class MetropolisCoupledMonteCarlo extends MonteCarlo
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(MetropolisCoupledMonteCarlo.class);

	//@Property(helpmessage = "", defaultvalue = "edu.berkeley.compbio.ml.mcmc.MonteCarloFactory")
	//public MonteCarloFactory mcf;
	@Property(helpmessage = "", defaultvalue = "edu.berkeley.compbio.ml.mcmc.MonteCarlo")
	public GenericFactory<MonteCarlo> chainFactory;

	@Property(helpmessage = "", defaultvalue = "1,2,4,8,16,32")
	public double[] heatFactors;

	//override to avoid presenting in GUI
	@Property(ignore = true, isNullable = true)
	public int writeToConsoleInterval;

	//override to avoid presenting in GUI
	@Property(ignore = true, isNullable = true)
	public int collectDataToDiskInterval;

	@Property(defaultvalue = "edu.berkeley.compbio.ml.mcmc.mcmcmc.ChainList")
	public ChainList currentState;

	public ChainList getCurrentState()
		{
		return currentState;
		}

	public void setCurrentState(MonteCarloState currentState)
		{
		this.currentState = (ChainList) currentState;
		}

	//	@Property(helpmessage = "", defaultvalue = "10")
	//	public int swapInterval;

	// -------------------------- STATIC METHODS --------------------------

	public void run() throws IOException, GenericFactoryException
		{
		assert heatFactors[0] == 1;
		ChainList chains = new ChainList();
		for (double hf : heatFactors)
			{
			MonteCarlo subChain = chainFactory.create();
			subChain.setHeatFactor(hf);
			chains.add(subChain);//mcf.newChain(hf));
			}

		MonteCarlo mc = chains.get(0);
		mc.setColdest(true);

		//MetropolisCoupledMonteCarlo couplingChain = new MetropolisCoupledMonteCarlo();
		setCurrentChainList(chains);
		setColdest(true);// suppress any output
		setId("COUPLING");
		init();

		logger.info("Initialized MCMCMC: " + heatFactors);

		// burn in
		for (int i = 0; i < burnIn; i++)
			{
			doBurnIn();
			}

		// do the real run
		for (int i = 0; i < numSteps; i++)
			{
			doStep(i);
			}
		}

	public void setCurrentChainList(ChainList currentChainList)
		{
		this.currentState = currentChainList;
		}

	public void doBurnIn() throws IOException, GenericFactoryException
		{
		// run each chain independently for a while
		// ** parallelizable
		for (MonteCarlo chain : getCurrentChainList())
			{
			chain.burnIn();
			/*	int maxStep = step + swapInterval;
		   for (int i = step; i < maxStep; i++)
			   {
			   chain.doStep(i);
			   }*/
			}

		// do the temperature swap attempt
		super.doStep(0);
		resetCounts();
		}

	public void doStep(int step) throws IOException, GenericFactoryException
		{
		// run each chain independently for a while
		// ** parallelizable
		for (MonteCarlo chain : getCurrentChainList())
			{
			chain.run();
			/*	int maxStep = step + swapInterval;
		   for (int i = step; i < maxStep; i++)
			   {
			   chain.doStep(i);
			   }*/
			}

		// do the temperature swap attempt
		super.doStep(step);
		}

	public ChainList getCurrentChainList()
		{
		return (ChainList) currentState;
		}

	// --------------------------- CONSTRUCTORS ---------------------------

	public MetropolisCoupledMonteCarlo() throws IOException
		{
		super();
		}
	}
