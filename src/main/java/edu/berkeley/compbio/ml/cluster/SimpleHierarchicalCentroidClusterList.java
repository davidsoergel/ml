package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.cluster.hierarchical.HierarchicalCentroidCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// ** this should be handled by generics on SimpleClusterList
/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class SimpleHierarchicalCentroidClusterList<T extends Clusterable<T>> implements ClusterList<T>
	{
	private List<? extends HierarchicalCentroidCluster<? extends T>> theList;

	public SimpleHierarchicalCentroidClusterList( final Collection<? extends HierarchicalCentroidCluster<? extends T>> clusters )
		{
		this.theList = new ArrayList<HierarchicalCentroidCluster<? extends T>>(clusters);
		}

	public List<? extends HierarchicalCentroidCluster<? extends T>> getClusters()
		{
		return theList;
		}
	}
