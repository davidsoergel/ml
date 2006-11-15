package edu.berkeley.compbio.ml.mcmc;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class Move
	{
	public abstract void propose();

	private static ThreadLocal type_tl;

	public int getType()
		{
		return ((Integer) type_tl.get()).intValue();
		}

	public static void setType(int t)
		{
		type_tl = new ThreadLocal();
		type_tl.set(new Integer(t));
		}
	}
