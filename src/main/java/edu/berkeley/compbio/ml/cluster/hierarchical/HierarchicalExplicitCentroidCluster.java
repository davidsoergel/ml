/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.trees.BasicPhylogenyNode;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.BasicCentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;

import java.util.Iterator;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HierarchicalExplicitCentroidCluster<T extends AdditiveClusterable<T>> extends HierarchicalCentroidCluster<T>
	{
	public HierarchicalExplicitCentroidCluster( final int id, final T sample )
		{
		super(new AdditiveCentroidCluster<T>(id, sample));
		}

	/**
	 * {@inheritDoc}
	 */
	public T getCentroid()
		{
		T result = getPayload().getCentroid();
		if (result == null)
			{
			Iterator<BasicPhylogenyNode<CentroidCluster<T>>> i = children.iterator();
			BasicPhylogenyNode<CentroidCluster<T>> w = i.next();
			AdditiveCentroidCluster<T> cc = new AdditiveCentroidCluster<T>(getPayload().getId(), w.getPayload().getCentroid());
			while (i.hasNext())
				{
				w = i.next();
				cc.addAll(w.getPayload());
				}
			//getPayload().setCentroid(result);
			setPayload(cc);
			}
		return result;
		}


	}
