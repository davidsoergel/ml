/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.stats.DissimilarityMeasure;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * Represents a clustering algorithm.  In general an algorithm will group Clusterable samples into Clusters; this
 * interface is agnostic about whether the implementation is supervised or unsupervised, and whether it is online or
 * batch.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id: CentroidClusteringMethod.java 393 2009-05-08 00:49:01Z soergel $
 */
public final class CentroidClusteringUtils //<T extends Clusterable<T>>
//		extends AbstractBatchClusteringMethod<T, CentroidCluster<T>> implements CentroidClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	//protected boolean leaveOneOut;
	/*	protected AbstractBatchCentroidClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
													 Set<String> predictLabels, Set<String> leaveOneOutLabels,
													 Set<String> testLabels)
		 {
		 super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		 //	this.leaveOneOut = leaveOneOut;
		 }
 */
	private static final Logger logger = Logger.getLogger(CentroidClusteringUtils.class);


// -------------------------- STATIC METHODS --------------------------

	/**
	 * for each cluster, compute the standard deviation of the distance of each point to the centroid.  This does not
	 * reassign points or move the centroids.  Logically it would be nice for this to be a method of Cluster, but we can't
	 * do that since the Cluster may not keep track of the samples it contains.
	 *
	 * @param theDataPointProvider
	 */
	public static <T extends Clusterable<T>> void computeClusterStdDevs(
			final Collection<? extends CentroidCluster<T>> theClusters, final DissimilarityMeasure<T> measure,
			final Map<String, ? extends CentroidCluster<T>> assignments,
			final ClusterableIterator<T> theDataPointProvider) //throws IOException
		{
		//theDataPointProvider.reset();
		for (final CentroidCluster<T> c : theClusters)
			{
			c.setSumOfSquareDistances(0);
			}

		try
			{
			while (true) //(theDataPointProvider.hasNext())
				{
				final T p = theDataPointProvider.nextFullyLabelled();
				final CentroidCluster<T> c = assignments.get(p.getId());
				final double dist = measure.distanceFromTo(p, c.getCentroid());// c.distanceToCentroid(p);
				c.addToSumOfSquareDistances(dist * dist);
				}
			}
		catch (NoSuchElementException e)
			{
			// iterator exhausted
			}
		}

	/**
	 * Returns the best cluster without adding the point
	 *
	 * @param p                   Point to find the best cluster of
	 * @param secondBestDistances List of second-best distances to add to (just for reporting purposes)
	 */
	/*	@NotNull
		public abstract Cluster<T> getBestCluster(T p, List<Double> secondBestDistances)
				throws NoGoodClusterException, ClusterException;

		public abstract double getBestDistance();*/

	/**
	 * Returns a List of the current Cluster centroids
	 *
	 * @return
	 */
	/*
	 public List<T> getCentroids()
		 {
		 List<T> result = new ArrayList<T>();
		 for (CentroidCluster<T> c : theClusters)
			 {
			 result.add(c.getCentroid());
			 }
		 return result;
		 }
 */
	/**
	 * {@inheritDoc}
	 */

	public static <T extends Clusterable<T>> String shortClusteringStats(
			final Collection<? extends CentroidCluster<T>> theClusters, final DissimilarityMeasure<T> measure)
		{
		final List<Double> distances = new ArrayList<Double>();
		final int numDistances = 0;
		for (final CentroidCluster<T> c : theClusters)
			{
			for (final CentroidCluster<T> d : theClusters)
				{
				final double distance = measure.distanceFromTo(c.getCentroid(),
				                                               d.getCentroid());// c.distanceToCentroid(d.getCentroid());
				if (c == d && !MathUtils.equalWithinFPError(distance, 0))
					{
					logger.warn("Floating point trouble: self distance = " + distance + " " + c);
					assert false;
					}
				if (c != d)
					{
					logger.debug("Distance between clusters = " + d);
					distances.add(distance);
					}
				}
			}
		final double avg = DSCollectionUtils.sum(distances) / (double) distances.size();
		double sd = 0;
		for (final double d : distances)
			{
			sd += d * d;
			}
		sd = Math.sqrt(sd / (double) distances.size());

		return new Formatter().format("Separation: %.3f (%.3f)", avg, sd).toString();
		}

	/**
	 * {@inheritDoc}
	 */
	/*
	 @Override

	 public String clusteringStats()
		 {
		 ByteArrayOutputStream b = new ByteArrayOutputStream();
		 writeClusteringStatsToStream(b);
		 return b.toString();
		 }
 */
	/**
	 * Writes a long String describing statistics about the clustering, such as the complete cluster distance matrix, to
	 * the given output stream.
	 *
	 * @param outf an OutputStream to which to write the string as it's built
	 * @return a long String describing statistics about the clustering.
	 */
	public static <T extends Clusterable<T>> void writeClusteringStatsToStream(
			final Collection<? extends CentroidCluster<T>> theClusters, final DissimilarityMeasure<T> measure,
			final OutputStream outf)
		{
		final PrintWriter p = new PrintWriter(outf);
		for (final CentroidCluster<T> c : theClusters)
			{
			p.println(c);
			final double stddev1 = c.getStdDev();
			for (final CentroidCluster<T> d : theClusters)
				{
				final double distance = measure.distanceFromTo(c.getCentroid(),
				                                               d.getCentroid());// c.distanceToCentroid(d.getCentroid());
				if (c == d && !MathUtils.equalWithinFPError(distance, 0))
					{
					logger.warn("Floating point trouble: self distance = " + distance + " " + c);
					assert false;
					}
				final double stddev2 = d.getStdDev();
				final double margin1 = distance - (stddev1 + stddev2);
				final double margin2 = distance - 2 * (stddev1 + stddev2);

				p.printf("\t%.2f (%.2f)", distance, margin1);//,  margin2);
				}
			p.println();
			}
		p.flush();
		}

	/* * @param unknownLabelProbabilityThreshold
		 *                        The smallest label probability to accept as a classification, as opposed to considering the
		 *                        point unclassifiable (this occurs when a sample matches a cluster which contains a diversity
		 *                        of labels, none of them dominant).*/
	}
