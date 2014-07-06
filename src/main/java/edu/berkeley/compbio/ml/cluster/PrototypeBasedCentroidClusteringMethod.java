/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;

/**
 * A clustering method which represents each cluster as a point (the "centroid"), which must be generated from a
 * prototype and which has various stats associated with it
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface PrototypeBasedCentroidClusteringMethod<T extends Clusterable<T>> extends CentroidClusteringMethod<T>
	{
// -------------------------- OTHER METHODS --------------------------

	void setPrototypeFactory(final GenericFactory<T> prototypeFactory) throws GenericFactoryException;
	}
