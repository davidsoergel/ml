package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.SubclassFinder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @author lorax
 * @version 1.0
 */
public class OnlineClusteringMethodMap extends HashMap<String, OnlineClusteringMethod>
	{
	private static Logger logger = Logger.getLogger(OnlineClusteringMethodMap.class);

	private static OnlineClusteringMethodMap _instance = new OnlineClusteringMethodMap();

	public static OnlineClusteringMethodMap getInstance()
		{
		return _instance;
		}


	public OnlineClusteringMethodMap()
		{
		for (Class dm : SubclassFinder.find("edu.berkeley.compbio.bugbags.cluster", OnlineClusteringMethod.class))
			{
			//put((String)dm.getMethod("getName").invoke(), dm);
			try
				{
				put(dm.getSimpleName(), (OnlineClusteringMethod) dm.newInstance());
				}
			catch (InstantiationException e)
				{
				logger.debug(e);
				}
			catch (IllegalAccessException e)
				{
				logger.debug(e);
				}
			}
		}

	public static OnlineClusteringMethod findByName(String s)
		{
		OnlineClusteringMethod result = getInstance().get(s);
		if (result == null)
			{
			logger.warn("Can't find command " + s + ".  Available commands: \n" + StringUtils
					.join(getInstance().keySet().iterator(), "\n"));
			}
		return result;
		}
	}


