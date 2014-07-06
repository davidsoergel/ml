/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc;

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.runutils.PluginMap;
import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

import java.util.Collection;


/**
 * Hold a set of move types, and act as a factory for creating new moves.
 *
 * @version 1.0
 */
@PropertyConsumer
public class MoveTypeSet//<T extends MonteCarloState>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MoveTypeSet.class);
	@Property(defaultvalue = "edu.berkeley.compbio.ml.mcmc.Move {edu.berkeley.compbio.ml.mcmc.mcmcmc}")
	public PluginMap<Double> pluginMap;

	//	private static ThreadLocal<HashMap<Object, MoveTypeSet>> _instance_tl =
	//			new ThreadLocal<HashMap<Object, MoveTypeSet>>();
	//	private static HashMap<Object, MoveTypeSet> _instances = new HashMap<Object, MoveTypeSet>();

	//	int numTypes = 0;

	//@Property
	//String packageName;

	//	@PluginMap
	//	Map<Class<Move>, Double> moveProbabilities;// = new PluginMap;

	private final Multinomial<GenericFactory<Move>> types = new Multinomial<GenericFactory<Move>>();


// -------------------------- OTHER METHODS --------------------------

	/*	public int size()
	   {
	   return numTypes;//types.size();
	   }*/

	public Collection<GenericFactory<Move>> getFactories()
		{
		return types.getElements();
		}

	//	private Class[] setTypeArgTypes = {int.class};

	//	private List theTypes = new ArrayList();
	//	private List<String> theNames = new ArrayList<String>();


	// -------------------------- STATIC METHODS --------------------------

	/*	public static MoveTypeSet getInstance(String movePackage) throws IOException
	   {
	   //HashMap<Object, MoveTypeSet> instances = _instance_tl.get();
	   //	if (_instances == null)
	   //		{
	   //		_instances = new HashMap<Object, MoveTypeSet>();
	   //_instance_tl.set(instances);
	   //		}
	   MoveTypeSet result = _instances.get(movePackage);
	   if (result == null)
		   {
		   result = new MoveTypeSet(movePackage);
		   _instances.put(movePackage, result);
		   }
	   return result;
	   }*/

	// --------------------------- CONSTRUCTORS ---------------------------

	//	@Property(helpmessage = "Map of move type names to move probabilities", defaultvalue = "")
	//	public HashMap<Class<Move>, Double> moveProbabilities;

	public void init()
		{
		try
			{
			//PluginManager.registerPackage(packageName, Move.class);

			//		ResultsCollectingProgramRun.getProps().injectProperties(injectorId, this);

			//Double prob;
			//for(Class<Move> movetype : PluginManager.getPlugins(Move.class))
			for (final Class movetype : pluginMap.getAvailablePlugins())//SubclassFinder.find(packageName, Move.class))
				{
				/*	if (Modifier.isAbstract(movetype.getModifiers()))
								{
								continue;
								}
							logger.debug("Found move type class: " + movetype.getName());*/
				String shortname = movetype.getName();
				shortname = shortname.substring(shortname.lastIndexOf(".") + 1);

				final Double prob = pluginMap.getValue(movetype);

				if (prob == null || Double.isNaN(prob))
					{
					logger.warn("No move probability found for " + shortname + "; assigning probability zero.");
					}
				else if (prob != 0)
					{
					types.put(pluginMap.getFactory(movetype), prob);
					//registerType(movetype, prob, shortname);
					}
				}
			types.normalize();
			/*			try
								   {
								   typeProbabilities.normalize();
								   }
							   catch (DistributionException e)
								   {
								   throw new Error(e);
								   }*/
			}
		catch (DistributionException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		}

	/*	private void registerType(GenericFactory<Move> c, double probability, String shortname)
	   {
	   logger.debug("Registering move type " + c.getCreatesClass() + " (" + shortname + ") with probability " + probability);
	   try
		   {
		   //		theTypes.add(c);
		   //		typeProbabilities.add(probability);
		   //Object[] setTypeArgs = {new Integer(numTypes)};
		   //c.put("type", numTypes); //getMethod("setType", setTypeArgTypes).invoke(null, setTypeArgs);
		   //theNames.add(c.getMethod("getName", null).invoke(null, null));
		   theNames.add(shortname);
		   numTypes++;
		   }
	   catch (Exception e)
		   {
		   logger.error("Error", e);
		   throw new Error(e);
		   }
	   }*/

	// -------------------------- OTHER METHODS --------------------------

	/*	public String getName(int i)
	   {
	   return (theNames.get(i));
	   }*/

	public Move newMove(final MonteCarloState currentMonteCarloState)
		{
		//	Class[] argTypes = {currentMonteCarloState.getClass()};
		try
			{
			final GenericFactory<Move> c = types.sample();
			final Object[] args = {currentMonteCarloState};
			//logger.debug("New Move: " + c.getName());
			//logger.debug("...Move created.");
			return c.create(args);
			}
		catch (Exception e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		}
	}
