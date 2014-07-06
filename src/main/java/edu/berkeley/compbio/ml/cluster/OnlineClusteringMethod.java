/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: OnlineClusteringMethod.java 398 2009-05-12 05:11:40Z soergel $
 */
public interface OnlineClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Create some initial clusters using the first few training samples.
	 */
	// void initializeWithRealData(Iterator<T> trainingIterator, GenericFactory<T> prototypeFactory) throws GenericFactoryException;
	}
