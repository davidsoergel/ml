/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.mcmc;

import com.davidsoergel.dsutils.ChainedException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MoveNotPossibleException extends ChainedException
	{
// --------------------------- CONSTRUCTORS ---------------------------

	public MoveNotPossibleException(final String s)
		{
		super(s);
		}

	public MoveNotPossibleException(final Throwable e)
		{
		super(e);
		}

	public MoveNotPossibleException(final Throwable e, final String s)
		{
		super(e, s);
		}
	}
