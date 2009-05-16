package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.dsutils.concurrent.ProgressReportingThreadPoolExecutor;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		implements ClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractClusteringMethod.class);

	protected static final Double UNKNOWN_DISTANCE = 1e308; // Double.MAX_VALUE; triggers MySQL bug # 21497

	protected DissimilarityMeasure<T> measure;
	protected Collection<C> theClusters = new ArrayList<C>();
	protected Map<String, C> assignments = new HashMap<String, C>();// see whether anything changed
	protected int n = 0;

	protected final Set<String> potentialTrainingBins;
	protected final Set<String> predictLabels;
	protected final Set<String> leaveOneOutLabels;
	protected final Set<String> testLabels;


// --------------------------- CONSTRUCTORS ---------------------------

	public AbstractClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                Set<String> predictLabels, Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		measure = dm;
		this.potentialTrainingBins = potentialTrainingBins;
		this.leaveOneOutLabels = leaveOneOutLabels;
		this.predictLabels = predictLabels;
		this.testLabels = testLabels;
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Returns the number of samples clustered so far
	 *
	 * @return the number of samples clustered so far
	 */
	public int getN()
		{
		return n;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ClusterSet ---------------------

	/**
	 * {@inheritDoc}
	 */
	public Collection<? extends C> getClusters()
		{
		return theClusters;
		}

// --------------------- Interface ClusteringMethod ---------------------


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
	public ClusteringTestResults test(Iterator<T> theTestIterator,
	                                  final DissimilarityMeasure<String> intraLabelDistances)
			throws DistributionException, ClusterException
		{
		final ClusteringTestResults tr = new ClusteringTestResults();

		tr.setNumClusters(theClusters.size());

		// these are used for checking whether a sample should have been unknown or not
		final Set<String> populatedTrainingLabels = findPopulatedTrainingLabels(tr);

		ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();

		// classify the test samples
		int i = 0;
		while (theTestIterator.hasNext())
			{
			final T frag = theTestIterator.next();

			Future fut = execService.submit(new Runnable()
			{
			public void run()
				{
				testOneSample(intraLabelDistances, tr, populatedTrainingLabels, frag);
				}
			});

			/*	if (i % 100 == 0)
			   {
			   logger.debug("Enqueued " + i + " samples.");
			   }*/
			i++;
			}
		logger.debug("Enqueued " + i + " samples.");
		tr.setTestSamples(i);

		execService.finish("Tested %d samples.", 30);

		tr.finish();
		return tr;
		}

	/**
	 * Choose the best label for the given sample from the set of permissible labels
	 *
	 * @param sample
	 * @param predictLabels
	 * @return
	 * @throws NoGoodClusterException
	 */
	public String bestLabel(T sample, Set<String> predictLabels) throws NoGoodClusterException
		{
		Cluster<T> c = bestClusterMove(sample).bestCluster;
		return c.getWeightedLabels().getDominantKeyInSet(predictLabels);
/*		c.updateDerivedWeightedLabelsFromLocal();
		WeightedSet<String> probs = c.getDerivedLabelProbabilities();
		String label = probs.getDominantKey();*/
		}

// -------------------------- OTHER METHODS --------------------------

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
	 * Returns a long String describing statistics about the clustering, such as the complete cluster distance matrix.
	 *
	 * @return a long String describing statistics about the clustering.
	 */
	public String clusteringStats()
		{
		return "No clustering stats available";
		}

	/**
	 * Figure out which of the potential training labels were actually populated (some got tossed to provide for unknown
	 * test samples) while we're at it, sum up the cluster masses
	 */
	protected Set<String> findPopulatedTrainingLabels(ClusteringTestResults tr) throws DistributionException
		{
		Set<String> populatedTrainingLabels = new HashSet<String>();
		for (C theCluster : theClusters)
			{
			// note this also insures that every cluster has a training label, otherwise it throws NoSuchElementException
			String label = theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(potentialTrainingBins);
			populatedTrainingLabels.add(label);
			tr.incrementTotalTrainingMass(theCluster.getWeightedLabels().getWeightSum());
			}
		return populatedTrainingLabels;
		}

	/**
	 * Sets a list of labels to be used for classification.  For a supervised method, this must be called before training.
	 *
	 * @param predictLabels a set of mutually-exclusive labels that we want to predict.  Note multiple bins may predict
	 *                       the same label; defining the clusters is a separate issue.
	 */
//	public void setPredictLabels(Set<String> predictLabels)
//		{
//		this.predictLabels = predictLabels;
//		}

	/**
	 * Sets a list of labels that the test samples will have, to which to compare our predictions.  Typically these will be
	 * the same as the training labels, but they need not be, as long as the wrongness measure can compare across the two
	 * sets.
	 *
	 * @param testLabels a set of mutually-exclusive labels that we want to predict.  Note multiple bins may predict the
	 *                   same label; defining the clusters is a separate issue.
	 */
//	public void setTestLabels(Set<String> testLabels)
//		{
//		this.testLabels = testLabels;
//		}

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

	protected void normalizeClusterLabelProbabilities()
		{
		ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();
		for (final Cluster<T> c : theClusters)
			{
			execService.submit(new Runnable()
			{
			public void run()
				{
				c.updateDerivedWeightedLabelsFromLocal();
				}
			});
			}
		execService.finish("Normalized %d training probabilities", 30);
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

	protected void testOneSample(DissimilarityMeasure<String> intraLabelDistances, ClusteringTestResults tr,
	                             Set<String> populatedTrainingLabels, T frag)
		{
		double clusterProb = 0;
		double secondToBestDistanceRatio = 0;
		double broadWrongness;
		double detailedWrongness;
		double bestDistance;
		double bestVoteProportion;
		double secondToBestVoteRatio = 0;

		// note the labels on the test set may be different from the training labels, as long as we can calculate wrongness.
		// This supports a hierarchical classification scenario, where the "detailed" label is a leaf, and the "broad" label is a higher aggregate node.
		// we want to measure wrongness _both_ at the broad level, matching where the prediction is made (so a perfect match is possible),
		// _and_ at the detailed level, where even a perfect broad prediction incurs a cost due to lack of precision.

		String broadActualLabel = frag.getWeightedLabels().getDominantKeyInSet(predictLabels);
		String detailedActualLabel = frag.getWeightedLabels().getDominantKeyInSet(testLabels);

		try
			{
			// make the prediction
			ClusterMove<T, C> cm = bestClusterMove(frag);   // throws NoGoodClusterException
			bestDistance = cm.bestDistance;
			if (cm.bestDistance != 0)
				{
				secondToBestDistanceRatio = cm.secondBestDistance / cm.bestDistance;
				}
			bestVoteProportion = cm.voteProportion;
			if (cm.voteProportion != 0)
				{
				secondToBestVoteRatio = cm.secondBestVoteProportion / cm.voteProportion;
				}

			// get the predicted label and its cluster-conditional probability
			WeightedSet<String> labelsOnThisCluster = cm.bestCluster.getDerivedLabelProbabilities();
			String predictedLabel = labelsOnThisCluster.getDominantKeyInSet(predictLabels);
			clusterProb = labelsOnThisCluster.getNormalized(predictedLabel);

			// the fragment's real label does not match any populated training label (to which it might possibly have been classified), it should be unknown
			if (!populatedTrainingLabels.contains(broadActualLabel))
				{
				tr.incrementShouldHaveBeenUnknown();
				}

			// compute a measure of how badly the prediction missed the truth, at the broad level
			broadWrongness = intraLabelDistances.distanceFromTo(broadActualLabel, predictedLabel);
			logger.debug("Label distance broad wrongness = " + broadWrongness);

			if (Double.isNaN(broadWrongness) || Double.isInfinite(broadWrongness))
				{
				logger.error("Broad Wrongness = " + broadWrongness);
				}

			// compute a measure of how badly the prediction missed the truth, at the detailed level
			detailedWrongness = intraLabelDistances.distanceFromTo(detailedActualLabel, predictedLabel);
			logger.debug("Label distance detailed wrongness = " + detailedWrongness);

			if (Double.isNaN(detailedWrongness) || Double.isInfinite(detailedWrongness))
				{
				logger.error("Detailed Wrongness = " + detailedWrongness);
				}
			}
		catch (NoGoodClusterException e)
			{
			broadWrongness = UNKNOWN_DISTANCE;
			detailedWrongness = UNKNOWN_DISTANCE;
			bestDistance = UNKNOWN_DISTANCE;
			secondToBestDistanceRatio = 1.0;
			bestVoteProportion = 0;
			secondToBestVoteRatio = 1.0;
			clusterProb = 0;

			tr.incrementUnknown();

			// the fragment's best label does match a training label, it should not be unknown
			if (populatedTrainingLabels.contains(broadActualLabel))
				{
				tr.incrementShouldNotHaveBeenUnknown();
				}
			}

		tr.addResult(broadWrongness, detailedWrongness, bestDistance, secondToBestDistanceRatio, clusterProb,
		             bestVoteProportion, secondToBestVoteRatio);
		}

	/**
	 * Return a ClusterMove object describing the best way to reassign the given point to a new cluster.
	 *
	 * @param p
	 * @return
	 */
	public abstract ClusterMove<T, C> bestClusterMove(T p) throws NoGoodClusterException;

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
	}
