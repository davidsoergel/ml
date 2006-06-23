package edu.berkeley.compbio.ml.cluster;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.io.IOException;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class ClusterableIterator<T extends Clusterable<T>> implements Iterator<T>//, Comparable<ClusterableIterator<T>>
	{
	private static Logger logger = Logger.getLogger(ClusterableIterator.class);

	//public abstract ClusterableIterator<T> clone() throws CloneNotSupportedException;

	public abstract void reset() throws IOException;
	}
