/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.cluster.kmeans;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AbstractUnsupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveCentroidCluster;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.ml.cluster.SampleInitializedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.SemisupervisedClusteringMethod;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * @version 1.0
 */
public class KmeansClustering<T extends AdditiveClusterable<T>>
		extends AbstractUnsupervisedOnlineClusteringMethod<T, CentroidCluster<T>>
		implements SemisupervisedClusteringMethod<T>, CentroidClusteringMethod<T>,
		           SampleInitializedOnlineClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(KmeansClustering.class);


// --------------------------- CONSTRUCTORS ---------------------------

	//private DistanceMeasure<T> distanceMeasure;
	//private int k;

	public KmeansClustering(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                        final Map<String, Set<String>> predictLabelSets, final ProhibitionModel<T> prohibitionModel,
	                        final Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidClusteringMethod ---------------------


	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(getClusters(), measure);
		}

	/*	public void addAndRecenter(T p)
	   {
	   assert p != null;
	   bestCluster(theClusters, p).addAndRecenter(p);  // this will automatically recalculate the centroid, etc.
	   }*/

	public void computeClusterStdDevs(final ClusterableIterator<T> theDataPointProvider)
		{
		CentroidClusteringUtils.computeClusterStdDevs(getClusters(), measure, getAssignments(), theDataPointProvider);
		}

	@Override
	public String clusteringStats()
		{
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(final OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, outf);
		}

// --------------------- Interface OnlineClusteringMethod ---------------------

	public boolean add(final T p)
		{
		// ** this is not synchronized!  I think it's OK, but be careful...
		// that should really only cause trouble if the same point gets added twice and simultaneously, and gets assiged to different clusters.  That seems highly unlikely.

		assert p != null;
		//n++;
		final String id = p.getId();
		final ClusterMove<T, CentroidCluster<T>> cm = bestClusterMove(p);
		//secondBestDistances.add(cm.secondBestDistance);
		if (cm.isChanged())
			{
			try
				{
				cm.oldCluster.remove(p);//, cm.oldDistance);
				}
			catch (NullPointerException e)
				{// probably just the first round
				}
			cm.bestCluster.add(p);//, cm.bestDistance);  // this will automatically recalculate the centroid, etc.
			putAssignment(id, cm.bestCluster);
			return true;
			}
		return false;
		}

// --------------------- Interface SampleInitializedOnlineClusteringMethod ---------------------

	/**
	 * {@inheritDoc}
	 */
	public void initializeWithSamples(final ClusterableIterator<T> trainingIterator,
	                                  final int initsamples) //, GenericFactory<T> prototypeFactory)
		//	throws GenericFactoryException
		{
		for (int i = 0; i < initsamples; i++)
			{
			// initialize the clusters with the first k points

			final AbstractCentroidCluster<T> c =
					new AdditiveCentroidCluster<T>(i, trainingIterator.nextFullyLabelled());
			//c.setId(i);

			addCluster(c);
			}
		logger.debug("initialized " + initsamples + " clusters");
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
	/*	@Override
	 public Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
		 {
		 ClusterMove cm = bestClusterMove(p);
		 return cm.bestCluster;
		 }
 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(final T p)
		{
		final ClusterMove<T, CentroidCluster<T>> result = new ClusterMove<T, CentroidCluster<T>>();
		//double bestDistance = Double.MAX_VALUE;
		//Cluster<T> bestCluster = null;

		final String id = p.getId();
		result.oldCluster = getAssignment(id);

		if (logger.isTraceEnabled())
			{
			logger.trace("Choosing best cluster for " + p + " (previous = " + result.oldCluster + ")");
			}
		for (final CentroidCluster<T> c : getClusters())
			{
			final double d = measure.distanceFromTo(p, c.getCentroid());//c.distanceToCentroid(p);
			if (logger.isTraceEnabled())
				{
				logger.trace("Trying " + c + "; distance = " + d + "; best so far = " + result.bestDistance);
				}
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
		if (logger.isTraceEnabled())
			{
			logger.trace("Chose " + result.bestCluster);
			}
		if (result.bestCluster == null)
			{
			logger.warn("Can't classify: " + p);
			// probably this is a GrowingKmeansClustering that has no clusters yet
			//assert false;
			}
		return result;
		}
	}
