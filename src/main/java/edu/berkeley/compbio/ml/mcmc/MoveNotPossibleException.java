package edu.berkeley.compbio.ml.mcmc;

import com.davidsoergel.dsutils.ChainedException;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MoveNotPossibleException extends ChainedException
	{
// --------------------------- CONSTRUCTORS ---------------------------

	public MoveNotPossibleException(String s)
		{
		super(s);
		}

	public MoveNotPossibleException(Throwable e)
		{
		super(e);
		}

	public MoveNotPossibleException(Throwable e, String s)
		{
		super(e, s);
		}
	}
