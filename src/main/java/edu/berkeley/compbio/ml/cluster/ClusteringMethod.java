package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.dsutils.math.MersenneTwisterFast;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class ClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>> implements ClusterSet<T>
	{
	private static final Logger logger = Logger.getLogger(ClusteringMethod.class);

	protected DissimilarityMeasure<T> measure;
	protected Collection<C> theClusters = new ArrayList<C>();
	protected Map<String, C> assignments = new HashMap<String, C>();// see whether anything changed
	protected int n = 0;

	/**
	 * Returns the cluster to which the sample identified by the given String is assigned.
	 *
	 * @param id the unique String identifier of the sample
	 * @return the Cluster to which the sample belongs
	 */
	public C getAssignment(String id)
		{
		return assignments.get(id);
		}

	/**
	 * Returns the number of samples clustered so far
	 *
	 * @return the number of samples clustered so far
	 */
	public int getN()
		{
		return n;
		}

	/**
	 * Return a ClusterMove object describing the best way to reassign the given point to a new cluster.
	 *
	 * @param p
	 * @return
	 */
	public abstract ClusterMove<T, C> bestClusterMove(T p) throws NoGoodClusterException;

	/**
	 * {@inheritDoc}
	 */
	public Collection<? extends C> getClusters()
		{
		return theClusters;
		}

	/**
	 * Returns a randomly selected cluster.
	 *
	 * @return a randomly selected cluster.
	 */
	protected Cluster<T> chooseRandomCluster()
		{		// PERF slow, but rarely used		// we have to iterate since we don't know the underlying Collection type.

		int index = MersenneTwisterFast.randomInt(theClusters.size());
		Iterator<? extends Cluster<T>> iter = theClusters.iterator();
		Cluster<T> result = iter.next();
		for (int i = 0; i < index; result = iter.next())
			{
			i++;
			}
		return result;

		//return theClusters.get(MersenneTwisterFast.randomInt(theClusters.size()));
		}

	/**
	 * choose the best cluster for each incoming data point and report it
	 */
	public void writeAssignmentsAsTextToStream(OutputStream outf)
		{
		int c = 0;
		PrintWriter p = new PrintWriter(outf);
		for (String s : assignments.keySet())
			{
			p.println(s + " " + assignments.get(s).getId());
			}
		p.flush();
		}

	/**
	 * Evaluates the classification accuracy of this clustering using an iterator of test samples.  These samples should
	 * not have been used in learning the cluster positions.  Determines what proportions of the test samples are
	 * classified correctly, incorrectly, or not at all.
	 *
	 * @param theTestIterator         an Iterator of test samples.
	 * @param mutuallyExclusiveLabels a Set of labels that we're trying to classify
	 * @param intraLabelDistances     a measure of how different the labels are from each other.  For simply determining
	 *                                whether the classification is correct or wrong, use a delta function (i.e. equals).
	 *                                Sometimes, however, one label may be more wrong than another; this allows us to track
	 *                                that.
	 * @return a TestResults object encapsulating the proportions of test samples classified correctly, incorrectly, or not
	 *         at all.
	 * @throws edu.berkeley.compbio.ml.cluster.NoGoodClusterException
	 *          when a test sample cannot be assigned to any cluster
	 * @throws com.davidsoergel.stats.DistributionException
	 *          when something goes wrong in computing the label probabilities
	 * @throwz ClusterException when something goes wrong in the bowels of the clustering implementation
	 */
	public TestResults test(Iterator<T> theTestIterator, Set<String> mutuallyExclusiveLabels,
	                        DissimilarityMeasure<String> intraLabelDistances) throws // NoGoodClusterException,
			DistributionException, ClusterException
		{		// evaluate labeling correctness using the test samples

		//	List<Double> secondBestDistances = new ArrayList<Double>();
		TestResults tr = new TestResults();

		tr.numClusters = theClusters.size();

		int i = 0;
		while (theTestIterator.hasNext())
			{
			T frag = theTestIterator.next();
			try
				{
				ClusterMove<T, C> cm = bestClusterMove(frag);

				//			secondBestDistances.add(cm.secondBestDistance);				//Cluster<T> best = getBestCluster(frag, secondBestDistances);				//double bestDistance = getBestDistance();

				// there are two ways to get classified "unknown":				// if no bin is within the max distance from the sample, then NoGoodClusterException is thrown				// if we got a bin, but no label is sufficiently certain in the bin, that's "unknown" too

				// to be classified correct, the dominant label of the fragment must match the dominant label of the cluster

				//Map.Entry<String, Double> dominant =				//		best.getDerivedLabelProbabilities().getDominantEntryInSet(mutuallyExclusiveLabels);


				// ** consider how best to store the test labels
				WeightedSet<String> clusterLabels = cm.bestCluster.getDerivedLabelProbabilities();

				// the "dominant" label is the one assigned by this clustering process.
				String dominantExclusiveLabel = clusterLabels.getDominantKeyInSet(mutuallyExclusiveLabels);

				// if the fragment's best label from the same exclusive set is the same one, that's a match.
				String fragDominantLabel = frag.getWeightedLabels().getDominantKeyInSet(mutuallyExclusiveLabels);

				double clusterProb = clusterLabels.getNormalized(dominantExclusiveLabel);

				double wrongness = intraLabelDistances.distanceFromTo(fragDominantLabel, dominantExclusiveLabel);

				tr.computedDistances.add(cm.bestDistance);
				tr.clusterProbabilities.add(clusterProb);
				tr.labelDistances.add(wrongness);

				/*		if (fragDominantLabel.equals(dominantExclusiveLabel))
				   {
				   tr.correctProbabilities.add(clusterProb);
				   tr.correctDistances.add(cm.bestDistance);
				   }
			   else
				   {
				   tr.wrongProbabilities.add(clusterProb);
				   tr.wrongDistances.add(cm.bestDistance);
				   }*/
				}
			catch (NoGoodClusterException e)
				{
				tr.unknown++;
				}
			if (i % 100 == 0)
				{
				logger.info("Tested " + i + " samples.");
				}
			i++;
			}
		tr.finish();
		logger.info("Tested " + i + " samples.");		//	return i;
		return tr;
		}

	/**
	 * Encapsulates the probability histograms of test samples classified correctly, incorrectly, or not at all.
	 *
	 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
	 * @version $Id$
	 */
	public class TestResults
		{		//	public Histogram1D correctProbabilities = new FixedWidthHistogram1D(0., 1., .01);		//	public Histogram1D wrongProbabilities = new FixedWidthHistogram1D(0., 1., .01);

		//public List<Double> correctDistances = new ArrayList<Double>();		//public List<Double> wrongDistances = new ArrayList<Double>();


		public List<Double> computedDistances = new ArrayList<Double>();
		public List<Double> clusterProbabilities = new ArrayList<Double>();
		public List<Double> labelDistances = new ArrayList<Double>();

		//public double correct = 0;		//public double wrong = 0;
		public int unknown = 0;
		public int numClusters = 0;

		/**
		 * Normalize the proportions to 1.  Useful for instance if the proportion fields are initially set to raw counts.
		 */		/*public void normalize()
			{
			double total = correct + wrong + unknown;
			correct /= total;
			wrong /= total;
			unknown /= total;
			}*/

		//	public double[] correctPercentages;		//	public double[] wrongPercentages;

		//	public double[] correctDistanceHistogram;		//	public double[] wrongDistanceHistogram;		//	public double[] distanceBinCenters;
		public void finish()
			{			/*		int[] correctCounts = correctProbabilities.getCounts();
			int[] wrongCounts = wrongProbabilities.getCounts();

			int correctTotal = DSArrayUtils.sum(correctCounts);
			int wrongTotal = DSArrayUtils.sum(wrongCounts);

			int total = correctTotal + wrongTotal;

			correctPercentages = DSArrayUtils.castToDouble(correctCounts);
			wrongPercentages = DSArrayUtils.castToDouble(wrongCounts);

			DSArrayUtils.multiplyBy(correctPercentages, 1. / total);
			DSArrayUtils.multiplyBy(wrongPercentages, 1. / total);

			double[] correctDistancesPrimitive =
					DSArrayUtils.toPrimitive((Double[]) correctDistances.toArray(new Double[0]));
			double[] wrongDistancesPrimitive =
					DSArrayUtils.toPrimitive((Double[]) wrongDistances.toArray(new Double[0]));

			double minDistance =
					Math.min(DSArrayUtils.min(correctDistancesPrimitive), DSArrayUtils.min(wrongDistancesPrimitive));
			double maxDistance =
					Math.max(DSArrayUtils.max(correctDistancesPrimitive), DSArrayUtils.max(wrongDistancesPrimitive));


			double binwidth = (maxDistance - minDistance) / 1000.;

			Histogram1D cHist =
					new FixedWidthHistogram1D(minDistance, maxDistance, binwidth, correctDistancesPrimitive);
			cHist.setTotalcounts(total);
			try
				{
				distanceBinCenters = cHist.getBinCenters();
				}
			catch (StatsException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new Error(e);
				}

			correctDistanceHistogram = cHist.getCumulativeFractions();


			FixedWidthHistogram1D wHist =
					new FixedWidthHistogram1D(minDistance, maxDistance, binwidth, wrongDistancesPrimitive);
			wHist.setTotalcounts(total);
			wrongDistanceHistogram = wHist.getCumulativeFractions();
			*/
			}
		}

	/**
	 * Returns a short String describing statistics about the clustering, such as the mean and stddev of the distances
	 * between clusters.
	 *
	 * @return a short String describing statistics about the clustering.
	 */
	public String shortClusteringStats()
		{
		return "No clustering stats available";
		}

	/**
	 * Returns a long String describing statistics about the clustering, such as the complete cluster distance matrix.
	 *
	 * @return a long String describing statistics about the clustering.
	 */
	public String clusteringStats()
		{
		return "No clustering stats available";
		}
	}
