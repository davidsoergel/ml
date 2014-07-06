/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
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

	@Property(helpmessage = "= kT.  Must be >= 1.  1 is cold chain", defaultvalue = "1")
	public double heatFactor = 1;

	//@Property(inherited = true)
	protected DataCollector dataCollector;


	protected int acceptedCount;

	protected String id;
	//protected int[] accepted;
	//protected int[] proposed;
	protected final Map<Class<Move>, Integer> accepted = new HashMap<Class<Move>, Integer>();
	protected final Map<Class<Move>, Integer> proposed = new HashMap<Class<Move>, Integer>();

	protected boolean isColdest = true;

	//@Property(isNullable = true)
	//public T currentState;

	//protected T newState;
	private int proposedCount;

	private int step = 0;


// --------------------------- CONSTRUCTORS ---------------------------

	public MonteCarlo()//String injectorId)
		{
		//ResultsCollectingProgramRun.getProps().injectProperties(injectorId, this);
		}

// --------------------- GETTER / SETTER METHODS ---------------------

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

	public void setDataCollector(final DataCollector dc)
		{
		this.dataCollector = dc;
		}

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

	public void setHeatFactor(final double t)
		{
		heatFactor = t;
		}

	public String getId()
		{
		return id;
		}

	public void setId(final String id)
		{
		this.id = id;
		}

	public MoveTypeSet getMovetypes()
		{
		return movetypes;
		}

	public void setMovetypes(final MoveTypeSet movetypes)
		{
		this.movetypes = movetypes;
		}

// -------------------------- OTHER METHODS --------------------------

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

	public void doStep() throws IOException, GenericFactoryException
		{
		step++;// make the modulos and outputs appear 1-based
		//logger.debug(String.format("[ %s ] Doing step %d: %d, %d", getId(), step, writeToConsoleInterval, collectDataToDiskInterval));
		final boolean writeToConsole = writeToConsoleInterval != 0 && ((step % writeToConsoleInterval) == 0);
		final boolean collectDataToDisk = collectDataToDiskInterval != 0 && ((step % collectDataToDiskInterval) == 0);


		final MonteCarloState currentState = getCurrentState();
		final MonteCarloState newState;

		final Move m = movetypes.newMove(currentState);
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

		final Class movetype = m.getClass();

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

			for (final GenericFactory<Move> f : movetypes.getFactories())
				{
				final Class c = f.getCreatesClass();
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

			for (final GenericFactory<Move> f : movetypes.getFactories())
				{
				final Class c = f.getCreatesClass();
				logger.debug(
						"[ " + id + " ] Accepted " + accepted.get(c) + " out of " + proposed.get(c) + " proposed " + c
						+ " moves.");
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

	public abstract MonteCarloState getCurrentState();

	public abstract void setCurrentState(MonteCarloState newState);

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

	public void resetCounts()
		{
		proposedCount = 0;
		acceptedCount = 0;//writeToConsoleInterval;
		//Arrays.fill(proposed, 0);
		//Arrays.fill(accepted, 0);
		//	proposed.clear();
		//	accepted.clear();
		for (final Class c : movetypes.pluginMap.getAvailablePlugins())
			{
			proposed.put(c, 0);
			accepted.put(c, 0);
			}
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

	public void setColdest(final boolean coldest)
		{
		isColdest = coldest;
		}

	public double unnormalizedLogLikelihood(final MonteCarloState mcs)
		{
		//return Math.pow(mcs.unnormalizedLikelihood(), (1./heatFactor));
		logger.debug(String.format("unnormalizedLogLikelihood: %f, heatFactor = %f, product = %f",
		                           mcs.unnormalizedLogLikelihood(), heatFactor,
		                           mcs.unnormalizedLogLikelihood() * (1. / heatFactor)));
		return mcs.unnormalizedLogLikelihood() * (1. / heatFactor);
		}
	}
