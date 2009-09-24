package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.dsutils.concurrent.Parallel;
import com.davidsoergel.dsutils.concurrent.ProgressReportingThreadPoolExecutor;
import com.davidsoergel.dsutils.math.MersenneTwisterFast;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.RequiresPreparationDistanceMetric;
import com.google.common.base.Function;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		implements ClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractClusteringMethod.class);


	protected final DissimilarityMeasure<T> measure;
	private final ArrayList<C> theClusters = new ArrayList<C>(); //Collections.synchronizedList(new ArrayList<C>());
	private final Map<String, C> assignments = new HashMap<String, C>();// see whether anything changed
	private int n = 0;

	protected final Set<String> potentialTrainingBins;
	//protected final Set<String> predictLabels;
	protected final Map<String, Set<String>> predictLabelSets;
	protected final ProhibitionModel<T> prohibitionModel; //Set<String> leaveOneOutLabels;
	protected final Set<String> testLabels;

//	protected final int testThreads;

	//private final Map<String, String> friendlyLabelMap;
// --------------------------- CONSTRUCTORS ---------------------------


	public void setN(final int n)
		{
		this.n = n;
		}

	public AbstractClusteringMethod(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                final Map<String, Set<String>> predictLabelSets,
	                                final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels)
		{
		measure = dm;
		this.potentialTrainingBins = potentialTrainingBins;
		this.prohibitionModel = prohibitionModel;
		this.predictLabelSets = predictLabelSets;
		this.testLabels = testLabels;
		//this.friendlyLabelMap =friendlyLabelMap;
//		this.testThreads = testThreads;
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

	public int getNumClusters()
		{
		synchronized (theClusters)
			{
			return theClusters.size();
			}
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ClusterSet ---------------------

	/**
	 * {@inheritDoc}
	 */
	public List<C> getClusters() //? extends C>
		{
		synchronized (theClusters)
			{
			return Collections.unmodifiableList(theClusters);
			}
		}

	public void setNumClusters(final int numClusters)
		{
		synchronized (theClusters)
			{
			theClusters.ensureCapacity(numClusters);
			}
		}

	public void addCluster(final C c)
		{
		synchronized (theClusters)
			{
			theClusters.add(c);
			}
		}

	public void setCluster(final int index, final C c)
		{
		synchronized (theClusters)
			{
			theClusters.set(index, c);
			}
		}

	public int getClusterIndexOf(final C c)
		{
		synchronized (theClusters)
			{
			return theClusters.indexOf(c);
			}
		}

	public C getCluster(final int index)
		{
		synchronized (theClusters)
			{
			return theClusters.get(index);
			}
		}

	public Map<String, C> getAssignments()
		{
		synchronized (assignments)
			{
			return Collections.unmodifiableMap(assignments);
			}
		}

	public void putAssignment(final String pointId, final C cluster)
		{
		synchronized (assignments)
			{
			assignments.put(pointId, cluster);
			}
		}

	protected void removeEmptyClusters()
		{
		synchronized (theClusters)
			{
			final Iterator<C> iter = theClusters.iterator();
			while (iter.hasNext())
				{
				final C c = iter.next();
				if (c.getN() == 0)
					{
					iter.remove();
					}
				}
			}
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
	 *                          when a test sample cannot be assigned to any cluster
	 * @throws com.davidsoergel.stats.DistributionException
	 *                          when something goes wrong in computing the label probabilities
	 * @throws ClusterException when something goes wrong in the bowels of the clustering implementation
	 */
	public synchronized ClusteringTestResults test(final ClusterableIterator<T> theTestIterator,
	                                               final DissimilarityMeasure<String> intraLabelDistances)
			throws DistributionException, ClusterException
		{
		final ClusteringTestResults tr = new ClusteringTestResults();

		tr.setNumClusters(getNumClusters());

		computeTrainingMass(tr);

		// BAD failure to distinguish a DissimilarityMeasure from what is really a DissimilarityMeasureFactory
		// the reason for this is that we instantiate/inject it long before we have the test labels available, with which it may need to be "prepared"

		//final DissimilarityMeasure<String> intraLabelDistances;

		// prepare a tree for all prediction labels, whether they're populated or not

		if (intraLabelDistances instanceof RequiresPreparationDistanceMetric
		    && ((RequiresPreparationDistanceMetric) intraLabelDistances).reallyRequiresPreparation())
			{
			final Set<String> allLabels = new HashSet<String>();
			allLabels.addAll(testLabels);
			for (final Set<String> predictLabels : predictLabelSets.values())
				{
				allLabels.addAll(predictLabels);
				}

			//	intraLabelDistances =
			((RequiresPreparationDistanceMetric<String>) intraLabelDistances).prepare(allLabels);
			}
		else
			{
			//	intraLabelDistances = intraLabelDistancesMaybeFactory;
			}

		// these are used for checking whether a sample should have been unknown or not
		final Map<String, Set<String>> populatedPredictLabelSets = findPopulatedPredictLabelSets();

		// classify the test samples

		final AtomicInteger i = new AtomicInteger(0);

		Parallel.forEach(theTestIterator, new Function<T, Void>()
		{
		public Void apply(@Nullable final T frag)
			{
			// the forEach uses next(), not nextFullyLabelled
			frag.doneLabelling();  // just in case, though it may have already been called
			i.incrementAndGet();
			testOneSample(intraLabelDistances, tr, populatedPredictLabelSets, frag);
			return null;
			}
		});

		logger.info("Tested " + i + " samples.");
		tr.setTestSamples(i.intValue());

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
	public String bestLabel(final T sample, final Set<String> predictLabels) throws NoGoodClusterException
		{
		final Cluster<T> c = bestClusterMove(sample).bestCluster;
		return c.getMutableWeightedLabels().getDominantKeyInSet(predictLabels);
//		c.updateDerivedWeightedLabelsFromLocal();
//		WeightedSet<String> probs = c.getDerivedLabelProbabilities();
//		String label = probs.getDominantKey();
		}


// -------------------------- OTHER METHODS --------------------------

	/**
	 * Returns a randomly selected cluster.
	 *
	 * @return a randomly selected cluster.
	 */
	protected Cluster<T> chooseRandomCluster()
		{
/*		// PERF slow, but rarely used
		final int index = MersenneTwisterFast.randomInt(theClusters.size());


		// we have to iterate since we don't know the underlying Collection type.
		final Iterator<? extends Cluster<T>> iter = theClusters.iterator();
		Cluster<T> result = iter.next();
		for (int i = 0; i < index; result = iter.next())
			{
			i++;
			}
		return result;*/

		return getCluster(MersenneTwisterFast.randomInt(getNumClusters()));
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
	 * Figure out which of the potential prediction labels were actually populated (some got tossed to provide for unknown
	 * test samples)
	 */
	protected Map<String, Set<String>> findPopulatedPredictLabelSets()
		{
		final Map<String, Set<String>> result = new HashMap<String, Set<String>>();

		for (final Map.Entry<String, Set<String>> entry : predictLabelSets.entrySet())
			{
			final String predictionSetName = entry.getKey();
			final Set<String> predictLabels = entry.getValue();

			final Set<String> populatedPredictLabels = new HashSet<String>();
			int clustersWithPredictionLabel = 0;
			for (final C theCluster : getClusters())
				{
				try
					{
					// note this also insures that every cluster has a prediction label, otherwise it throws NoSuchElementException
					final String label = theCluster.getDerivedLabelProbabilities().getDominantKeyInSet(predictLabels);
					populatedPredictLabels.add(label);
					clustersWithPredictionLabel++;
					}
				catch (NoSuchElementException e)
					{
					logger.debug("Cluster has no prediction label: " + theCluster);
					}
				}
			result.put(predictionSetName, populatedPredictLabels);
			logger.info(predictionSetName + ": " + clustersWithPredictionLabel + " of " + getNumClusters()
			            + " clusters have a prediction label; " + populatedPredictLabels.size()
			            + " labels can be predicted");
			}
		return result;
		}

	public void computeTrainingMass(final ClusteringTestResults tr)
		{
		for (final C theCluster : getClusters())
			{
			tr.incrementTotalTrainingMass(theCluster.getMutableWeightedLabels().getItemCount());
			}
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
	public C getAssignment(final String id)
		{
		synchronized (assignments)
			{
			return assignments.get(id);
			}
		}

	protected void normalizeClusterLabelProbabilities()
		{
		final ProgressReportingThreadPoolExecutor execService = new ProgressReportingThreadPoolExecutor();
		for (final Cluster<T> c : getClusters())
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

	protected void testOneSample(final DissimilarityMeasure<String> intraLabelDistances, final ClusteringTestResults tr,
	                             final Map<String, Set<String>> populatedPredictLabelSets, final T frag)
		{
		final WeightedSet<String> predictedLabelWeights = predictLabelWeights(tr, frag);
		testAgainstPredictionLabels(intraLabelDistances, tr, populatedPredictLabelSets, frag, predictedLabelWeights);
		}

	protected void testAgainstPredictionLabels(final DissimilarityMeasure<String> intraLabelDistances,
	                                           final ClusteringTestResults tr,
	                                           final Map<String, Set<String>> populatedPredictLabelSets, final T frag,
	                                           final WeightedSet<String> predictedLabelWeights)
		{

		final boolean unknown = predictedLabelWeights == null;

		// note the labels on the test set may be different from the training labels, as long as we can calculate wrongness.
		// This supports a hierarchical classification scenario, where the "detailed" label is a leaf, and the "broad" label is a higher aggregate node.
		// we want to measure wrongness _both_ at the broad level, matching where the prediction is made (so a perfect match is possible),
		// _and_ at the detailed level, where even a perfect broad prediction incurs a cost due to lack of precision.

		final WeightedSet<String> fragmentActualLabels = frag.getImmutableWeightedLabels();
		final String detailedActualLabel = fragmentActualLabels.getDominantKeyInSet(testLabels);

		for (final Map.Entry<String, Set<String>> entry : predictLabelSets.entrySet())
			{
			final String predictionSetName = entry.getKey();
			final Set<String> predictLabels = entry.getValue();

			//	MultiClassCrossValidationResults cvResults = getCvResults(predictionSetName);

			String broadActualLabel = null;
			try
				{
				broadActualLabel = fragmentActualLabels.getDominantKeyInSet(predictLabels);
				}
			catch (NoSuchElementException e)
				{
				// the fragment has none of the requested classifications; leave broadActualLabel = null then.
				// this should produce MAXDISTANCE and ShouldHaveBeenUnknown
				}

			String predictedLabel;

			double broadWrongness;
			double detailedWrongness;
			double clusterProb;
			if (unknown)
				{
				predictedLabel = null;
				clusterProb = 0;

				// the fragment's best label does match a training label, it should not be unknown
				if (populatedPredictLabelSets.get(predictionSetName).contains(broadActualLabel))
					{
					tr.incrementShouldNotHaveBeenUnknown(predictionSetName);
					}

				broadWrongness = DissimilarityMeasure.UNKNOWN_DISTANCE;
				detailedWrongness = DissimilarityMeasure.UNKNOWN_DISTANCE;
				}
			else
				{
				// get the predicted label and its cluster-conditional probability

				try
					{
					predictedLabel = predictedLabelWeights.getDominantKeyInSet(predictLabels);
					clusterProb = predictedLabelWeights.getNormalized(predictedLabel);

					// the fragment's real label does not match any populated training label (to which it might possibly have been classified), it should be unknown
					if (!populatedPredictLabelSets.get(predictionSetName).contains(broadActualLabel))
						{
						tr.incrementShouldHaveBeenUnknown(predictionSetName);
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
				catch (NoSuchElementException e)
					{
					// a cluster was found, but it has no prediction label.
					// BAD treat this as "unknown" for now
					// Note it's not "unknown" but "other".
					predictedLabel = null;
					clusterProb = 0;


					tr.incrementOther(predictionSetName);

					// the fragment's best label does match a training label, it should not be unknown
					if (populatedPredictLabelSets.get(predictionSetName).contains(broadActualLabel))
						{
						tr.incrementShouldNotHaveBeenOther(predictionSetName);
						}

					broadWrongness = DissimilarityMeasure.UNKNOWN_DISTANCE;
					detailedWrongness = DissimilarityMeasure.UNKNOWN_DISTANCE;
					}
				}

			//	cvResults.addSample(broadActualLabel, predictedLabel);

			tr.addPredictionResult(predictionSetName, broadActualLabel, predictedLabel, 1.0 - clusterProb,
			                       broadWrongness, detailedWrongness);
			}
		}

	protected WeightedSet<String> predictLabelWeights(final ClusteringTestResults tr,
	                                                  final T frag) //, Set<String> populatedTrainingLabels)
		{
		double secondToBestDistanceRatio = 0;

		double bestDistance;
		double bestVoteProportion;
		double secondToBestVoteRatio = 0;


		WeightedSet<String> labelWeights = null;

		try
			{
			// make the prediction
			final ClusterMove<T, C> cm = bestClusterMove(frag);   // throws NoGoodClusterException
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

			labelWeights = cm.bestCluster.getDerivedLabelProbabilities();
			}
		catch (NoGoodClusterException e)
			{
			bestDistance = DissimilarityMeasure.UNKNOWN_DISTANCE;
			secondToBestDistanceRatio = 1.0;
			bestVoteProportion = 0;
			secondToBestVoteRatio = 1.0;

			tr.incrementUnknown();
			}

		tr.addClusterResult(bestDistance, secondToBestDistanceRatio, bestVoteProportion, secondToBestVoteRatio);
		return labelWeights;
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
	public void writeAssignmentsAsTextToStream(final OutputStream outf)
		{
		final int c = 0;
		final PrintWriter p = new PrintWriter(outf);
		synchronized (assignments)
			{
			for (final Map.Entry<String, C> stringCEntry : assignments.entrySet())
				{
				p.println(stringCEntry.getKey() + " " + stringCEntry.getValue().getId());
				}
			}
		p.flush();
		}
	}
