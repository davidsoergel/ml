/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;


/**
 * Once a Kohonen SOM is learned, propagate the sample labels around in some way to give label confidences for every
 * cell.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface LabelDiffuser<T extends AdditiveClusterable<T>, C extends CentroidCluster<T>>
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Once a Kohonen SOM is learned, propagate the sample labels around in some way to give label confidences for every
	 * cell.
	 *
	 * @param theMap the KohonenSOM<T>
	 */
	void propagateLabels(DiffusableLabelClusteringMethod<T, C> theMap);
	}
