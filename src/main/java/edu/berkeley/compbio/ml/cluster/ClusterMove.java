/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * Represents a potential move of a sample from one cluster to another.  Doesn't actually refer to the sample itself,
 * strangely; apparently we haven't needed that yet.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusterMove<T extends Clusterable<T>, C extends Cluster<T>> implements Comparable<ClusterMove<T, C>>
	{

//	final static AtomicLong idCounter = new AtomicLong();

	// globally unique id for hashCode efficiency ?? no, default Object.hashcode() should be fine

//	private final long id;

	public ClusterMove()
		{
		//	id = idCounter.incrementAndGet();
		}

/*	@Override
	public boolean equals(final Object o)
		{
		if (this == o)
			{
			return true;
			}
		if (o == null || getClass() != o.getClass())
			{
			return false;
			}

		final ClusterMove that = (ClusterMove) o;

		if (id != that.id)
			{
			return false;
			}

		return true;
		}

	@Override
	public int hashCode()
		{
		int result;
		result = (int) (id ^ (id >>> 32));
		return result;
		}*/
// ------------------------------ FIELDS ------------------------------

	/**
	 * The destination cluster
	 */
	public C bestCluster;

	/**
	 * The distance of the point under consideration to the destination cluster centroid
	 */
	public double bestDistance = Double.POSITIVE_INFINITY;

	/**
	 * The source cluster
	 */
	public C oldCluster;

	/**
	 * Sometimes it's interesting to know how much better the best distance is than the second-best one, so we allow
	 * recording it here.
	 */
	public double secondBestDistance = 0;

	/**
	 * For voting-based classifiers, different moves may count for different numbers of votes, perhaps as a function of
	 * distance. Defaults to 1.0.
	 */
	public double voteWeight = 1.0;

	/**
	 * for an SVM, this move may have been arrived at by a voting procedure
	 */
	public double voteProportion;
	/**
	 * for an SVM, this move may have been arrived at by a voting procedure
	 */
	public double secondBestVoteProportion;

	/**
	 * The distance of the point under consideration to the source cluster centroid
	 */
	public double oldDistance;


// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Returns a string representation of the object. In general, the <code>toString</code> method returns a string that
	 * "textually represents" this object. The result should be a concise but informative representation that is easy for a
	 * person to read. It is recommended that all subclasses override this method.
	 * <p/>
	 * The <code>toString</code> method for class <code>Object</code> returns a string consisting of the name of the class
	 * of which the object is an instance, the at-sign character `<code>@</code>', and the unsigned hexadecimal
	 * representation of the hash code of the object. In other words, this method returns a string equal to the value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
		{
		return "bestDistance = " + bestDistance + ", bestCluster = " + bestCluster + ", oldCluster = " + oldCluster
		       + ", oldDistance = " + oldDistance;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Comparable ---------------------

	public int compareTo(final ClusterMove<T, C> o)
		{
		return bestCluster.getId() - o.bestCluster.getId();
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Tells whether this move has any effect.
	 *
	 * @return true if the new cluster differs from the old one; false otherwise.
	 */
	public boolean isChanged()
		{
		return (oldCluster == null || (!bestCluster.equals(oldCluster)));
		}
	}
