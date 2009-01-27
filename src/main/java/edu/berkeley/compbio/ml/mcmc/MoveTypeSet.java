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
	@Property(defaultvalue = "edu.berkeley.compbio.ml.mcmc.Move {edu.berkeley.compbio.ml.mcmc.mcmcmc}")
	public PluginMap<Double> pluginMap;

	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MoveTypeSet.class);

	//	private static ThreadLocal<HashMap<Object, MoveTypeSet>> _instance_tl =
	//			new ThreadLocal<HashMap<Object, MoveTypeSet>>();
	//	private static HashMap<Object, MoveTypeSet> _instances = new HashMap<Object, MoveTypeSet>();

	//	int numTypes = 0;

	//@Property
	//String packageName;

	//	@PluginMap
	//	Map<Class<Move>, Double> moveProbabilities;// = new PluginMap;

	private Multinomial<GenericFactory<Move>> types = new Multinomial<GenericFactory<Move>>();

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
			for (Class movetype : pluginMap.getAvailablePlugins())//SubclassFinder.find(packageName, Move.class))
				{
				/*	if (Modifier.isAbstract(movetype.getModifiers()))
								{
								continue;
								}
							logger.debug("Found move type class: " + movetype.getName());*/
				String shortname = movetype.getName();
				shortname = shortname.substring(shortname.lastIndexOf(".") + 1);

				Double prob = pluginMap.getValue(movetype);

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
			logger.error(e);
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
		   logger.error(e);
		   throw new Error(e);
		   }
	   }*/

	// -------------------------- OTHER METHODS --------------------------

	/*	public String getName(int i)
	   {
	   return (theNames.get(i));
	   }*/

	public Move newMove(MonteCarloState currentMonteCarloState)
		{
		//	Class[] argTypes = {currentMonteCarloState.getClass()};
		try
			{
			GenericFactory<Move> c = types.sample();
			Object[] args = {currentMonteCarloState};
			//logger.debug("New Move: " + c.getName());
			Move theMove = c.create(args);//getConstructor(argTypes)
			//logger.debug("...Move created.");
			return theMove;
			}
		catch (Exception e)
			{
			logger.error(e);
			throw new Error(e);
			}
		}

	/*	public int size()
	   {
	   return numTypes;//types.size();
	   }*/

	public Collection<GenericFactory<Move>> getFactories()
		{
		return types.getElements();
		}
	}
