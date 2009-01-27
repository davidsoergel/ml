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

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 */
@PropertyConsumer
public abstract class MonteCarlo//<T extends MonteCarloState>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MonteCarlo.class);


	@Property(helpmessage = "", defaultvalue = "0")
	public int burnIn;

	@Property(helpmessage = "", defaultvalue = "100000")
	public int numSteps;

	@Property(helpmessage = "Write status to the console every n samples", defaultvalue = "1000")
	public int writeToConsoleInterval;

	@Property(helpmessage = "Collect data to disk every n samples", defaultvalue = "100")
	public int collectDataToDiskInterval;

	@Property(defaultvalue = "edu.berkeley.compbio.ml.mcmc.MoveTypeSet")
	public MoveTypeSet movetypes;

	//@Property(inherited = true)
	protected DataCollector dataCollector;


	protected int acceptedCount;

	@Property(helpmessage = "= kT.  Must be >= 1.  1 is cold chain", defaultvalue = "1")
	public double heatFactor = 1;

	protected String id;
	//protected int[] accepted;
	//protected int[] proposed;
	protected Map<Class<Move>, Integer> accepted = new HashMap<Class<Move>, Integer>();
	protected Map<Class<Move>, Integer> proposed = new HashMap<Class<Move>, Integer>();

	protected boolean isColdest = true;

	//@Property(isNullable = true)
	//public T currentState;

	//protected T newState;
	private int proposedCount;

	private int step = 0;

	// -------------------------- STATIC METHODS --------------------------
	public void run() throws IOException, GenericFactoryException//MonteCarloFactory mcf, int burnIn, int numSteps)
		{
		burnIn();
		runNoBurnIn();
		}

	public void burnIn() throws IOException, GenericFactoryException
		{
		//MonteCarlo mc = mcf.newChain(1);
		for (int i = 0; i < burnIn; i++)
			{
			doStep();
			//logger.warn("Burnin step: " + i);
			}
		resetCounts();
		step = 0;
		}

	public void runNoBurnIn()
			throws IOException, GenericFactoryException//MonteCarloFactory mcf, int burnIn, int numSteps)
		{

		for (int i = 0; i < numSteps; i++)
			{
			/*
		   if (i % writeToConsoleInterval == 0)
			   {
			   }*/
			doStep();
			//logger.warn("Step: " + i);
			}
		}

	public void resetCounts()
		{

		proposedCount = 0;
		acceptedCount = 0;//writeToConsoleInterval;
		//Arrays.fill(proposed, 0);
		//Arrays.fill(accepted, 0);
		//	proposed.clear();
		//	accepted.clear();
		for (Class c : movetypes.pluginMap.getAvailablePlugins())
			{
			proposed.put(c, 0);
			accepted.put(c, 0);
			}
		}

	public void doStep() throws IOException, GenericFactoryException
		{
		step++;// make the modulos and outputs appear 1-based
		//logger.debug(String.format("[ %s ] Doing step %d: %d, %d", getId(), step, writeToConsoleInterval, collectDataToDiskInterval));
		boolean writeToConsole = writeToConsoleInterval != 0 && ((step % writeToConsoleInterval) == 0);
		boolean collectDataToDisk = collectDataToDiskInterval != 0 && ((step % collectDataToDiskInterval) == 0);


		MonteCarloState currentState = getCurrentState();
		MonteCarloState newState;

		Move m = movetypes.newMove(currentState);
		if (m instanceof EnergyMove)
			{
			//throw new Error("EnergyMoves are currently prohibited, pending refactoring");
			newState = ((EnergyMove) m).doMove(heatFactor);
			}
		else
			{
			//newState = ((ProbabilityMove) m).doMove();
			newState = ((ProbabilityMove) m).doMove(heatFactor);
			}

		Class movetype = m.getClass();

		proposedCount++;
		//proposed[movetype]++;
		proposed.put(movetype, proposed.get(movetype) + 1);

		if (currentState != newState)
			{
			acceptedCount++;
			//accepted[movetype]++;
			accepted.put(movetype, accepted.get(movetype) + 1);
			}

		setCurrentState(newState);
		if (collectDataToDisk)// && isColdest)
			{
			if (isColdest())
				{
				currentState.writeToDataCollector(step, dataCollector);
				}

			for (GenericFactory<Move> f : movetypes.getFactories())
				{
				Class c = f.getCreatesClass();
				dataCollector.setTimecourseValue(id + "." + c.getSimpleName() + ".proposed", proposed.get(c));
				dataCollector.setTimecourseValue(id + "." + c.getSimpleName() + ".accepted", accepted.get(c));
				}
			}
		if (writeToConsole && logger.isInfoEnabled())
			{
			//System.out.print("\033c");

			logger.debug("Step " + step);
			logger.debug(
					"[ " + id + " ] Accepted " + acceptedCount + " out of " + proposedCount + " proposed total moves.");

			for (GenericFactory<Move> f : movetypes.getFactories())
				{
				Class c = f.getCreatesClass();
				logger.debug("[ " + id + " ] Accepted " + accepted.get(c) + " out of " + proposed.get(c) + " proposed "
						+ c + " moves.");
				}
			//System.out.println("\n\n");
			//acceptedCount = writeToConsoleInterval;
			resetCounts();

			System.out.println(currentState);
			if (dataCollector != null)
				{
				System.out.println(dataCollector.toString());
				}
			}
		}

	public abstract void setCurrentState(MonteCarloState newState);

	public abstract MonteCarloState getCurrentState();

	// --------------------------- CONSTRUCTORS ---------------------------

	public MonteCarlo()//String injectorId)
		{
		//ResultsCollectingProgramRun.getProps().injectProperties(injectorId, this);
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	/*
	 public T getCurrentState()
		 {
		 return currentState;
		 }

	 public void setCurrentState(T currentState)
		 {
		 this.currentState = currentState;
		 }
 */

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

	/*
	 public T getNewState()
		 {
		 return newState;
		 }

	 public void setNewState(T newState)
		 {
		 this.newState = newState;
		 }
 */
	// -------------------------- OTHER METHODS --------------------------

	public DataCollector getDataCollector()
		{
		return dataCollector;
		}

	/*
   public void init()
	   {
	   accepted = new HashMap<Class<Move>, Integer>();//int[movetypes.size()];
	   proposed = new HashMap<Class<Move>, Integer>();//int[movetypes.size()];

	   //writeToConsoleInterval = (writeToConsoleInterval));
	   //collectDataToDiskInterval = (new Integer(Run.getProps().getProperty("collectDataToDiskInterval")));
	   //acceptedCount = writeToConsoleInterval;
	   resetCounts();
	   //currentState = new MonteCarloState(); //.init();
	   }*/

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
		this.dataCollector = dc;
		}

	public double unnormalizedLogLikelihood(MonteCarloState mcs)
		{
		//return Math.pow(mcs.unnormalizedLikelihood(), (1./heatFactor));
		logger.debug(String.format("unnormalizedLogLikelihood: %f, heatFactor = %f, product = %f",
		                           mcs.unnormalizedLogLikelihood(), heatFactor,
		                           mcs.unnormalizedLogLikelihood() * (1. / heatFactor)));
		return mcs.unnormalizedLogLikelihood() * (1. / heatFactor);
		}

	/*
   public void setChainSharedState(Object chainSharedState)
	   {
	   }

   public Object getChainSharedState()
	   {
	   return null;
	   }*/

	public void init()
		{
		resetCounts();
		}
	}
