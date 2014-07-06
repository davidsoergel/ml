/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc;

/**
 * @version 1.0
 */
public abstract class Move//<T extends MonteCarloState>
	{
// ------------------------------ FIELDS ------------------------------

	private static ThreadLocal type_tl;


// -------------------------- STATIC METHODS --------------------------

	public static void setType(final int t)
		{
		type_tl = new ThreadLocal();
		type_tl.set(Integer.valueOf(t));
		}

// -------------------------- OTHER METHODS --------------------------

	public int getType()
		{
		return ((Integer) type_tl.get()).intValue();
		}

	public abstract void propose();
	}
