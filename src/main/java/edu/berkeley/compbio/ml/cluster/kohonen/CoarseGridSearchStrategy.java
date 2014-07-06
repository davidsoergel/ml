/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class CoarseGridSearchStrategy<T extends AdditiveClusterable<T>> extends KohonenSOM2DSearchStrategy<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(CoarseGridSearchStrategy.class);

	//** @Property
	//private final int gridSpacing = 4;


	private int gridSpacing;
	private Set<? extends KohonenSOMCell<T>> sparseGrid;


// --------------------------- CONSTRUCTORS ---------------------------

	public CoarseGridSearchStrategy()
		{
		super();
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Copied from KmeansClustering
	 *
	 * @param p
	 * @return
	 */
	@Override
	public ClusterMove<T, KohonenSOMCell<T>> bestClusterMove(final T p) throws NoGoodClusterException
		{
		final ClusterMove<T, KohonenSOMCell<T>> result = new ClusterMove<T, KohonenSOMCell<T>>();

		final String id = p.getId();
		result.oldCluster = som.getAssignment(id);

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (final KohonenSOMCell<T> c : sparseGrid)
			{
			final double d = measure.distanceFromTo(p, c.getCentroid());//c.distanceToCentroid(p);
			if (d < result.bestDistance)
				{
				result.secondBestDistance = result.bestDistance;
				result.bestDistance = d;
				result.bestCluster = c;
				}
			else if (d < result.secondBestDistance)
				{
				result.secondBestDistance = d;
				}
			}
		//Set<Cluster<T>> neighborhood = som.getNeighborhood(result.bestCluster(), gridSpacing * 2);

		for (Iterator<KohonenSOM2D<T>.WeightedCell> i =
				som.getWeightedMask(gridSpacing * 2).iterator(result.bestCluster); i.hasNext();)
			{
			final KohonenSOMCell<T> c = i.next().theCell;
			final double d = measure.distanceFromTo(p, c.getCentroid());//c.distanceToCentroid(p);
			if (d < result.bestDistance)
				{
				result.secondBestDistance = result.bestDistance;
				result.bestDistance = d;
				result.bestCluster = c;
				}
			else if (d < result.secondBestDistance)
				{
				result.secondBestDistance = d;
				}
			}

		if (logger.isDebugEnabled())
			{
			logger.debug("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			//logger.error("Can't classify: " + p);
			throw new NoGoodClusterException("No cluster found for " + p + ": " + result);
			}
		return result;
		}

	@Override
	public void setSOM(final KohonenSOM2D<T> som)
		{
		super.setSOM(som);
		setGridSpacing(4);
		}

	public void setGridSpacing(final int gridSpacing)
		{
		this.gridSpacing = gridSpacing;
		sparseGrid = getSparseGridClusters();
		}

	public Set<? extends KohonenSOMCell<T>> getSparseGridClusters()
		{
		final Set<KohonenSOMCell<T>> result = new HashSet<KohonenSOMCell<T>>();
		final int width = som.cellsPerDimension[0];
		final int height = som.cellsPerDimension[1];
		for (int x = 0; x < width; x += gridSpacing)
			{
			for (int y = 0; y < height; y += gridSpacing)
				{
				if (x != width && y != height)
					{
					result.add(som.clusterAt(x, y));
					}
				}
			}
		return result;
		}
	}
