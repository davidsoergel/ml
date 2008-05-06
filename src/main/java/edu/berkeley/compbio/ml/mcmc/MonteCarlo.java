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

import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * @author lorax
 * @version 1.0
 */
@PropertyConsumer
public abstract class MonteCarlo
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(MonteCarlo.class);


	@Property(helpmessage = "Write status to the console every n samples", defaultvalue = "1000")
	public int writeToConsoleInterval;

	@Property(helpmessage = "Collect data to disk every n samples", defaultvalue = "100")
	public int collectDataToDiskInterval;
	protected MoveTypeSet movetypes;
	protected int acceptedCount;

	protected double heatFactor = 1;//  = beta = kT.  Must be >= 1.  1 is cold chain
	protected String id;
	protected int[] accepted;
	protected int[] proposed;
	protected DataCollector dc;

	protected boolean isColdest = true;

	protected MonteCarloState currentState;
	protected MonteCarloState newState;
	private int proposedCount;


	// -------------------------- STATIC METHODS --------------------------

	public static void run(MonteCarloFactory mcf, int burnIn, int numSteps)
		{
		MonteCarlo mc = mcf.newChain(1);
		for (int i = 0; i < burnIn; i++)
			{
			mc.doStep(0);
			//System.err.println("Burnin step: " + i);
			}

		mc.resetCounts();
		for (int i = 0; i < numSteps; i++)
			{
			/*
		   if (i % writeToConsoleInterval == 0)
			   {
			   }*/
			mc.doStep(i);
			//System.err.println("Step: " + i);
			}
		}

	public void resetCounts()
		{
		proposedCount = 0;
		acceptedCount = 0;//writeToConsoleInterval;
		Arrays.fill(proposed, 0);
		Arrays.fill(accepted, 0);
		}

	public void doStep(int step)
		{
		//logger.debug(String.format("[ %s ] Doing step %d: %d, %d", getId(), step, writeToConsoleInterval, collectDataToDiskInterval));
		boolean writeToConsole = ((step % writeToConsoleInterval) == 0);
		boolean collectDataToDisk = ((step % collectDataToDiskInterval) == 0);

		Move m = movetypes.newMove(currentState);
		if (m instanceof EnergyMove)
			{
			throw new Error("EnergyMoves are currently prohibited, pending refactoring");
			//newState = ((EnergyMove) m).doMove(heatFactor);
			}
		else
			{
			newState = ((ProbabilityMove) m).doMove();
			}

		int movetype = m.getType();

		proposedCount++;
		proposed[movetype]++;

		if (currentState != newState)
			{
			acceptedCount++;
			accepted[movetype]++;
			}

		currentState = newState;
		if (collectDataToDisk && isColdest)
			{
			currentState.writeToDataCollector(step, dc);
			}
		if (writeToConsole && logger.isInfoEnabled())
			{
			//System.out.print("\033c");

			logger.info("Step " + step);
			logger.info(
					"[ " + id + " ] Accepted " + acceptedCount + " out of " + proposedCount + " proposed total moves.");

			for (int i = 0; i < movetypes.size(); i++)
				{
				logger.info("[ " + id + " ] Accepted " + accepted[i] + " out of " + proposed[i] + " proposed "
						+ movetypes.getName(i) + " moves.");
				}
			//System.out.println("\n\n");
			//acceptedCount = writeToConsoleInterval;
			resetCounts();

			System.out.println(currentState);
			if (dc != null)
				{
				System.out.println(dc.toString());
				}
			}
		}

	// --------------------------- CONSTRUCTORS ---------------------------

	public MonteCarlo()//String injectorId)
		{
		//ResultsCollectingProgramRun.getProps().injectProperties(injectorId, this);
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	public MonteCarloState getCurrentState()
		{
		return currentState;
		}

	public void setCurrentState(MonteCarloState currentState)
		{
		this.currentState = currentState;
		}

	public double getHeatFactor()
		{
		return heatFactor;
		}

	public void setHeatFactor(double t)
		{
		heatFactor = t;
		}

	public String getId()
		{
		return id;
		}

	public void setId(String id)
		{
		this.id = id;
		}

	public MoveTypeSet getMovetypes()
		{
		return movetypes;
		}

	public void setMovetypes(MoveTypeSet movetypes)
		{
		this.movetypes = movetypes;
		}

	public MonteCarloState getNewState()
		{
		return newState;
		}

	public void setNewState(MonteCarloState newState)
		{
		this.newState = newState;
		}

	// -------------------------- OTHER METHODS --------------------------

	public DataCollector getDataCollector()
		{
		return dc;
		}

	public void init()
		{
		accepted = new int[movetypes.size()];
		proposed = new int[movetypes.size()];

		//writeToConsoleInterval = (writeToConsoleInterval));
		//collectDataToDiskInterval = (new Integer(Run.getProps().getProperty("collectDataToDiskInterval")));
		//acceptedCount = writeToConsoleInterval;
		resetCounts();
		currentState.init();
		}

	public boolean isColdest()
		{
		return isColdest;
		}

	public void setColdest(boolean coldest)
		{
		isColdest = coldest;
		}

	public void setDataCollector(DataCollector dc)
		{
		this.dc = dc;
		}

	public double unnormalizedLogLikelihood(MonteCarloState mcs)

		{
		//return Math.pow(mcs.unnormalizedLikelihood(), (1./heatFactor));
		logger.debug(String.format("unnormalizedLogLikelihood: %f, heatFactor = %f, product = %f",
		                           mcs.unnormalizedLogLikelihood(), heatFactor,
		                           mcs.unnormalizedLogLikelihood() * (1. / heatFactor)));
		return mcs.unnormalizedLogLikelihood() * (1. / heatFactor);
		}
	}
