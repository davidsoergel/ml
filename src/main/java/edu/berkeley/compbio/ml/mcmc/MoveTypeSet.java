package edu.berkeley.compbio.ml.mcmc;

import com.davidsoergel.dsutils.SubclassFinder;
import com.davidsoergel.runutils.ThreadLocalRun;
import com.davidsoergel.stats.MultinomialDistribution;
import org.apache.log4j.Logger;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Hold a set of move types, and act as a factory for creating new moves.  Singleton.
 *
 * @author lorax
 * @version 1.0
 */
//@PropertyConsumer
public class MoveTypeSet
	{
	private static Logger logger = Logger.getLogger(MoveTypeSet.class);

	private static ThreadLocal<HashMap<Object, MoveTypeSet>> _instance_tl =
			new ThreadLocal<HashMap<Object, MoveTypeSet>>();

	//	@Property(helpmessage = "Map of move type names to move probabilities", defaultvalue = "")
	//	public HashMap<String, Double> moveProbabilities;


	public MoveTypeSet(String movePackage)
		{
		//		ThreadLocalRun.getProps().injectProperties(this);

		Double prob;
		for (Class movetype : SubclassFinder.find(movePackage, Move.class))
			{
			if (Modifier.isAbstract(movetype.getModifiers()))
				{
				continue;
				}
			logger.debug("Found move type class: " + movetype.getName());
			String shortname = movetype.getName();
			shortname = shortname.substring(shortname.lastIndexOf(".") + 1);

			// ** eliminate dependency (huh?  looks OK)
			//prob = moveProbabilities.get(shortname);
			// ** hack because we can't inject into a Map
			prob = ThreadLocalRun.getProps()
					.getDouble("edu.berkeley.compbio.ml.mcmc.MoveTypeSet.moveProbabilities." + shortname);
			if (prob == null || Double.isNaN(prob))
				{
				logger.warn("No move probability found for " + shortname + "; assigning probability zero.");
				}
			else if (prob != 0)
				{
				registerType(movetype, prob, shortname);
				}
			}
		typeProbabilities.normalize();

		}

	public static MoveTypeSet getInstance(String movePackage)
		{
		HashMap<Object, MoveTypeSet> instances = _instance_tl.get();
		if (instances == null)
			{
			instances = new HashMap<Object, MoveTypeSet>();
			_instance_tl.set(instances);
			}
		MoveTypeSet result = instances.get(movePackage);
		if (result == null)
			{
			result = new MoveTypeSet(movePackage);
			instances.put(movePackage, result);
			}
		return result;
		}

	int numTypes = 0;

	private List theTypes = new ArrayList();
	private List<String> theNames = new ArrayList<String>();

	MultinomialDistribution typeProbabilities = new MultinomialDistribution();


	public Move newMove(MonteCarloState currentMonteCarloState)
		{
		Class[] argTypes = {currentMonteCarloState.getClass()};
		try
			{
			Class c = (Class) (theTypes.get(typeProbabilities.sample()));
			Object[] args = {currentMonteCarloState};
			//logger.debug("New Move: " + c.getName());
			Move theMove = (Move) (c.getConstructor(argTypes).newInstance(args));
			//logger.debug("...Move created.");
			return theMove;
			}
		catch (Exception e)
			{
			logger.debug(e);
			throw new Error(e);
			}
		}

	public int size()
		{
		return numTypes;
		}

	Class[] setTypeArgTypes = {int.class};

	private void registerType(Class c, double probability, String shortname)
		{
		logger.debug("Registering move type " + c + " (" + shortname + ") with probability " + probability);
		try
			{
			theTypes.add(c);
			typeProbabilities.add(probability);
			Object[] setTypeArgs = {new Integer(numTypes)};
			c.getMethod("setType", setTypeArgTypes).invoke(null, setTypeArgs);
			//theNames.add(c.getMethod("getName", null).invoke(null, null));
			theNames.add(shortname);
			numTypes++;
			}
		catch (Exception e)
			{
			//e.printStackTrace();
			logger.debug(e);
			throw new Error(e);
			}
		}

	public String getName(int i)
		{
		return (theNames.get(i));
		}
	}
