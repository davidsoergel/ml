/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.Labellable;
import org.jetbrains.annotations.Nullable;

/**
 * Primarily a marker interface for classes that can be clustered by various algorithms.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface Clusterable<T extends Clusterable> extends Cloneable, Labellable<String>
	{
// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Clone this object.  Should behave like {@link Object#clone()} except that it returns an appropriate type and so
	 * requires no cast.  Also, we insist that is method be implemented in inheriting classes, so it does not throw
	 * CloneNotSupportedException.
	 *
	 * @return a clone of this instance.
	 * @see Object#clone
	 * @see java.lang.Cloneable
	 */
	//protected T clone();


// -------------------------- OTHER METHODS --------------------------

	/**
	 * Test whether the given object is the same as this one.  Differs from equals() in that implementations of this
	 * interface may contain additional state which make them not strictly equal; here we're only interested in whether
	 * they're equal as far as this interface is concerned, i.e., for purposes of clustering.
	 *
	 * @param other The clusterable object to compare against
	 * @return True if they are equivalent, false otherwise
	 */
	boolean equalValue(T other);

	/**
	 * Returns a String identifying this object.  Ideally each clusterable object being analyzed should have a unique
	 * identifier.
	 *
	 * @return a unique identifier for this object
	 */
	@Nullable
	String getId();

	/**
	 * Returns a String identifying the source of this object (i.e., what class it was sampled from, or such).  Keeping
	 * track of this facilitates leave-one-out evaluation, where a test sample is not allowed to be classified to the same
	 * bin it came from.
	 *
	 * @return a unique identifier for this object
	 */
//	@NotNull
//	String getSourceId();

	/**
	 * Get a set of classification labels, if available, with associated weights between 0 and 1. (optional operation)
	 *
	 * @return a set of Strings describing this object
	 */
	/*@NotNull
	MutableWeightedSet<String> getMutableWeightedLabels();

	@NotNull
	WeightedSet<String> getImmutableWeightedLabels();*/
	//throws PhyloUtilsException;

	/**
	 * Get the primary classification label, if available (optional operation)
	 *
	 * @return a Strings describing this object
	 */
	//	String getExclusiveLabel();
	}
