/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public abstract class KohonenSOM2DSearchStrategy<T extends AdditiveClusterable<T>>
	{
// ------------------------------ FIELDS ------------------------------

	//void setSOM(KohonenSOM2D<T> som);
	protected KohonenSOM2D<T> som;
	protected DissimilarityMeasure<T> measure;


// -------------------------- OTHER METHODS --------------------------

	abstract ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(T p) throws NoGoodClusterException;

	public void setDistanceMeasure(final DissimilarityMeasure<T> measure)
		{
		this.measure = measure;
		}

	public void setSOM(final KohonenSOM2D<T> som)
		{
		this.som = som;
		}
	}
