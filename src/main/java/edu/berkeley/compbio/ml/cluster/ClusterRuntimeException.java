/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.ChainedRuntimeException;
import org.apache.log4j.Logger;

/**
 * A runtime exception having to do with clustering.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusterRuntimeException extends ChainedRuntimeException
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(ClusterException.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public ClusterRuntimeException(final String s)
		{
		super(s);
		}

	public ClusterRuntimeException(final Exception e)
		{
		super(e);
		}

	public ClusterRuntimeException(final Exception e, final String s)
		{
		super(e, s);
		}
	}
