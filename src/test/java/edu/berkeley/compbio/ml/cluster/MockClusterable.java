package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
public class MockClusterable implements Clusterable
	{
	private static Logger logger = Logger.getLogger(MockClusterable.class);

	public String getId()
		{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

	public Clusterable plus(Clusterable object)
		{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

	public Clusterable minus(Clusterable object)
		{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

	public void incrementBy(Clusterable object)
		{
		//To change body of implemented methods use File | Settings | File Templates.
		}

	public void decrementBy(Clusterable object)
		{
		//To change body of implemented methods use File | Settings | File Templates.
		}

	public boolean equalValue(Clusterable object)
		{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
		}

	public Clusterable clone()
		{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}
	}
