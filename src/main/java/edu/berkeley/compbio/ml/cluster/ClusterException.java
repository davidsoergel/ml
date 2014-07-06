/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.ChainedException;
import org.apache.log4j.Logger;


/**
 * An exception having to do with clustering.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusterException extends ChainedException
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(ClusterException.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public ClusterException(final String s)
		{
		super(s);
		}

	public ClusterException(final Exception e)
		{
		super(e);
		}

	public ClusterException(final Exception e, final String s)
		{
		super(e, s);
		}
	}
