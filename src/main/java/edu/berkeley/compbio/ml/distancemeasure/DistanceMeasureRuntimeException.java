/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.distancemeasure;

import com.davidsoergel.dsutils.ChainedRuntimeException;
import org.apache.log4j.Logger;


/**
 * Thrown when something involving a SequenceSpectrum goes wrong, such as when a requested spectrum cannot be computed.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class DistanceMeasureRuntimeException extends ChainedRuntimeException
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(DistanceMeasureRuntimeException.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public DistanceMeasureRuntimeException(final String s)
		{
		super(s);
		}

	public DistanceMeasureRuntimeException(final Exception e)
		{
		super(e);
		}

	public DistanceMeasureRuntimeException(final Exception e, final String s)
		{
		super(e, s);
		}
	}
