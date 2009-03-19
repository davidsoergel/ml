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
import java.util.HashSet;
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

	protected static final Double UNKNOWN_DISTANCE = Double.MAX_VALUE;

	protected DissimilarityMeasure<T> measure;
	protected Collection<C> theClusters = new ArrayList<C>();
	protected Map<String, C> assignments = new HashMap<String, C>();// see whether anything changed
	protected int n = 0;

	protected Set<String> trainingLabels;
	protected Set<String> testLabels;

	public ClusteringMethod(DissimilarityMeasure<T> dm)
		{
		measure = dm;
		}

	/**
	 * Sets a list of labels to be used for classification.  For a supervised method, this must be called before training.
	 *
	 * @param trainingLabels a set of mutually-exclusive labels that we want to predict.  Note multiple bins may predict
	 *                       the same label; defining the clusters is a separate issue.
	 */
	public void setTrainingLabels(Set<String> trainingLabels)
		{
		this.trainingLabels = trainingLabels;
		}

	/**
	 * Sets a list of labels that the test samples will have, to which to compare our predictions.  Typically these will be
	 * the same as the training labels, but they need not be, as long as the wrongness measure can compare across the two
	 * sets.
	 *
	 * @param testLabels a set of mutually-exclusive labels that we want to predict.  Note multiple bins may predict the
	 *                   same label; defining the clusters is a separate issue.
	 */
	public void setTestLabels(Set<String> testLabels)
		{
		this.testLabels = testLabels;
		}

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
	 * @param theTestIterator     an Iterator of test samples. // @param mutuallyExclusiveLabels a Set of labels that we're
	 *                            trying to classify
	 * @param intraLabelDistances a measure of how different the labels are from each other.  For simply determining
	 *                            whether the classification is correct or wrong, use a delta function (i.e. equals).
	 *                            Sometimes, however, one label may be more wrong than another; this allows us to track
	 *                            that.
	 * @return a TestResults object encapsulating the proportions of test samples classified correctly, incorrectly, or not
	 *         at all.
	 * @throws edu.berkeley.compbio.ml.cluster.NoGoodClusterException
	 *          when a test sample cannot be assigned to any cluster
	 * @throws com.davidsoergel.stats.DistributionException
	 *          when something goes wrong in computing the label probabilities
	 * @throwz ClusterException when something goes wrong in the bowels of the clustering implementation
	 */
	public TestResults test(Iterator<T> theTestIterator, //Set<String> mutuallyExclusiveLabels,
	                        DissimilarityMeasure<String> intraLabelDistances) throws // NoGoodClusterException,
			DistributionException, ClusterException
		{		// evaluate labeling correctness using the test samples

		//	List<Double> secondBestDistances = new ArrayList<Double>();
		TestResults tr = new TestResults();

		tr.numClusters = theClusters.size();

		boolean computedDistancesInteresting = false;
		boolean clusterProbabilitiesInteresting = false;

		// Figure out which of the potential training labels were actually populated (some got tossed to provide for unknown test samples)
		// while we're at it, sum up the cluster masses

		Set<String> populatedTrainingLabels = new HashSet<String>();
		for (C theCluster : theClusters)
			{
			populatedTrainingLabels.add(theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(trainingLabels));
			tr.totalTrainingMass += theCluster.getWeightedLabels().getWeightSum();
			}

		// classify the test samples
		int i = 0;
		while (theTestIterator.hasNext())
			{
			T frag = theTestIterator.next();

			double clusterProb = 0;
			double secondToBestDistanceRatio;
			double wrongness;
			double bestDistance;

			String actualLabel = frag.getWeightedLabels().getDominantKeyInSet(testLabels);

			try
				{
				// make the prediction
				ClusterMove<T, C> cm = bestClusterMove(frag);   // throws NoGoodClusterException
				bestDistance = cm.bestDistance;
				secondToBestDistanceRatio = cm.secondBestDistance / cm.bestDistance;

				// keep track of whether any good predictions are ever made
				if (cm.bestDistance < Double.MAX_VALUE)
					{
					computedDistancesInteresting = true;
					}

				// get the predicted label and its cluster-conditional probability
				WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();
				String predictedLabel = labelsOnThisCluster.getDominantKeyInSet(trainingLabels);
				clusterProb = labelsOnThisCluster.getNormalized(predictedLabel);

				// keep track of whether any good predictions are ever made
				if (clusterProb != 1)
					{
					clusterProbabilitiesInteresting = true;
					}

				// the fragment's real label does not match any populated training label (to which it might possibly have been classified), it should be unknown
				if (!populatedTrainingLabels.contains(actualLabel))
					{
					tr.shouldHaveBeenUnknown++;
					}

				// compute a measure of how badly the prediction missed the truth
				wrongness = intraLabelDistances.distanceFromTo(actualLabel, predictedLabel);
				logger.debug("Label distance wrongness = " + wrongness);

				if (Double.isNaN(wrongness))
					{
					logger.error("Wrongness NaN");
					}

				if (Double.isInfinite(wrongness))
					{
					logger.error("Infinite Wrongness");
					}

				// don't bother with this: the predictions and real labels may be entirely disjoint; we're just interested in the distances
/*
				if (predictedLabel.equals(actualLabel))
					{
					tr.perfect++;
					}
*/


				}
			catch (NoGoodClusterException e)
				{
				wrongness = UNKNOWN_DISTANCE;
				bestDistance = UNKNOWN_DISTANCE;
				secondToBestDistanceRatio = 1.0;
				//secondToBestVoteRatio = 1.0;
				clusterProb = 0;

				tr.unknown++;

				// the fragment's best label does match a training label, it should not be unknown
				if (populatedTrainingLabels.contains(actualLabel))
					{
					tr.shouldNotHaveBeenUnknown++;
					}
				}

			tr.labelDistances.add(wrongness);
			tr.computedDistances.add(bestDistance);
			tr.secondToBestDistanceRatios.add(secondToBestDistanceRatio);
			//	tr.voteRatios.add(bestToSecondVoteRatio);
			tr.labelWithinClusterProbabilities.add(clusterProb);

			if (i % 100 == 0)
				{
				logger.debug("Tested " + i + " samples.");
				}
			i++;
			}
		if (!clusterProbabilitiesInteresting)
			{
			tr.labelWithinClusterProbabilities = null;
			}
		if (!computedDistancesInteresting)
			{
			tr.computedDistances = null;
			}
		tr.testSamples = i;
		tr.finish();
		logger.debug("Tested " + i + " samples.");		//	return i;
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

		/**
		 * The real distance between the predicted label and the real label ("wrongness") according to a label distance
		 * measure
		 */
		public List<Double> labelDistances = new ArrayList<Double>();

		/**
		 * The computed distance between the sample and the predicted bin
		 */
		public List<Double> computedDistances = new ArrayList<Double>();

		/**
		 * The second-best distance as a proportion of the best distance; 1.0 = tie
		 */
		public List<Double> secondToBestDistanceRatios = new ArrayList<Double>();

		/**
		 * For voting-based classifiers, the second-best number of votes as a proportion of the best votes; 1.0 = tie
		 */
		public List<Double> secondToBestVoteRatios = new ArrayList<Double>();

		/**
		 * Probability of the best label, given the best cluster.  Used for unsupervised clustering where each cluster may
		 * contain samples with multiple labels.
		 */
		public List<Double> labelWithinClusterProbabilities = new ArrayList<Double>();

		//public double correct = 0;
		//public double wrong = 0;
		public int unknown = 0;
		public int numClusters = 0;
		public int shouldHaveBeenUnknown = 0;
		public int shouldNotHaveBeenUnknown = 0;
		public int testSamples;
		public double trainingSeconds;
		public double testingSeconds;
		public double totalTrainingMass = 0;
		public int perfect = 0;

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
				logger.error(e);
				throw new Error(e);
				}

			correctDistanceHistogram = cHist.getCumulativeFractions();


			FixedWidthHistogram1D wHist =
					new FixedWidthHistogram1D(minDistance, maxDistance, binwidth, wrongDistancesPrimitive);
			wHist.setTotalcounts(total);
			wrongDistanceHistogram = wHist.getCumulativeFractions();
			*/
			}

		public double getAccuracy()
			{
			return (double) perfect / (double) labelDistances.size();
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
