/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

	/**
	 * for each cluster, compute the standard deviation of the distance of each point to the centroid.  This does not
	 * reassign points or move the centroids.  Logically it would be nice for this to be a method of Cluster, but we can't
	 * do that since the Cluster may not keep track of the samples it contains.
	 *
	 * @param theDataPointProvider
	 */
	public static <T extends Clusterable<T>> void computeClusterStdDevs(
			Collection<? extends CentroidCluster<T>> theClusters, DissimilarityMeasure<T> measure,
			Map<String, ? extends CentroidCluster<T>> assignments,
			ClusterableIterator<T> theDataPointProvider) //throws IOException
		{
		theDataPointProvider.reset();
		for (CentroidCluster<T> c : theClusters)
			{
			c.setSumOfSquareDistances(0);
			}
		while (theDataPointProvider.hasNext())
			{
			T p = theDataPointProvider.next();
			CentroidCluster<T> c = assignments.get(p.getId());
			double dist = measure.distanceFromTo(c.getCentroid(), p);// c.distanceToCentroid(p);
			c.addToSumOfSquareDistances(dist * dist);
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
			Collection<? extends CentroidCluster<T>> theClusters, DissimilarityMeasure<T> measure)
		{
		List<Double> distances = new ArrayList<Double>();
		int numDistances = 0;
		for (CentroidCluster<T> c : theClusters)
			{
			for (CentroidCluster<T> d : theClusters)
				{
				double distance = measure.distanceFromTo(c.getCentroid(),
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
		double avg = DSCollectionUtils.sum(distances) / (double) distances.size();
		double sd = 0;
		for (double d : distances)
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
			Collection<? extends CentroidCluster<T>> theClusters, DissimilarityMeasure<T> measure, OutputStream outf)
		{
		PrintWriter p = new PrintWriter(outf);
		for (CentroidCluster<T> c : theClusters)
			{
			p.println(c);
			double stddev1 = c.getStdDev();
			for (CentroidCluster<T> d : theClusters)
				{
				double distance = measure.distanceFromTo(c.getCentroid(),
				                                         d.getCentroid());// c.distanceToCentroid(d.getCentroid());
				if (c == d && !MathUtils.equalWithinFPError(distance, 0))
					{
					logger.warn("Floating point trouble: self distance = " + distance + " " + c);
					assert false;
					}
				double stddev2 = d.getStdDev();
				double margin1 = distance - (stddev1 + stddev2);
				double margin2 = distance - 2 * (stddev1 + stddev2);

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
