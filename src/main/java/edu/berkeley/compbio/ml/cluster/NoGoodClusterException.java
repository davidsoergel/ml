/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.ChainedException;
import org.apache.log4j.Logger;

/**
 * An exception thrown when no satisfactory cluster is found for a sample.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class NoGoodClusterException extends ChainedException
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(ClusterException.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public NoGoodClusterException()
		{
		super();
		}

	public NoGoodClusterException(final String s)
		{
		super(s);
		}

	public NoGoodClusterException(final Exception e)
		{
		super(e);
		}

	public NoGoodClusterException(final Exception e, final String s)
		{
		super(e, s);
		}
	}
