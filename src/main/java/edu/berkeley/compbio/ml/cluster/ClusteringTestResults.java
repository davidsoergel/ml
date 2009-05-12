package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import org.apache.commons.collections15.functors.EqualPredicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the probability histograms of test samples classified correctly, incorrectly, or not at all.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClusteringTestResults
	{		//	public Histogram1D correctProbabilities = new FixedWidthHistogram1D(0., 1., .01);		//	public Histogram1D wrongProbabilities = new FixedWidthHistogram1D(0., 1., .01);

	//public List<Double> correctDistances = new ArrayList<Double>();		//public List<Double> wrongDistances = new ArrayList<Double>();

	/**
	 * The real distance between the predicted label and the real label ("wrongness") according to a label distance
	 * measure
	 */
	private List<Double> predictionDistances = new ArrayList<Double>();
	private List<Double> predictionDistancesWithPrecisionCost = new ArrayList<Double>();

	public List<Double> getPredictionDistances()
		{
		return predictionDistances;
		}

	public List<Double> getPredictionDistancesWithPrecisionCost()
		{
		return predictionDistancesWithPrecisionCost;
		}

	/**
	 * The computed distance between the sample and the predicted bin
	 */
	private List<Double> computedDistances = new ArrayList<Double>();

	public Double[] getComputedDistancesArray()
		{
		return computedDistances == null ? null : computedDistances.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
		}

	/**
	 * The second-best distance as a proportion of the best distance; 1.0 = tie
	 */
	private List<Double> secondToBestDistanceRatios = new ArrayList<Double>();

	public Double[] getSecondToBestDistanceRatiosArray()
		{
		return secondToBestDistanceRatios == null ? null :
				secondToBestDistanceRatios.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
		}

	/**
	 * For voting-based classifiers, the proportion of votes that the best label got
	 */
	private List<Double> bestVoteProportions = new ArrayList<Double>();

	public Double[] getBestVoteProportionsArray()
		{
		return bestVoteProportions == null ? null : bestVoteProportions.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
		}

	/**
	 * For voting-based classifiers, the second-best number of votes as a proportion of the best votes; 1.0 = tie
	 */
	private List<Double> secondToBestVoteRatios = new ArrayList<Double>();

	public Double[] getSecondToBestVoteRatiosArray()
		{
		return secondToBestVoteRatios == null ? null :
				secondToBestVoteRatios.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
		}

	/**
	 * Probability of the best label, given the best cluster.  Used for unsupervised clustering where each cluster may
	 * contain samples with multiple labels.
	 */
	private List<Double> labelWithinClusterProbabilities = new ArrayList<Double>();

	public Double[] getLabelWithinClusterProbabilitiesArray()
		{
		return labelWithinClusterProbabilities == null ? null :
				labelWithinClusterProbabilities.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
		}


	//public double correct = 0;
	//public double wrong = 0;
	private int unknown = 0;
	private int numClusters = 0;
	private int shouldHaveBeenUnknown = 0;
	private int shouldNotHaveBeenUnknown = 0;
	private int testSamples;
	private double trainingSeconds;
	private double testingSeconds;
	private double totalTrainingMass = 0;
	//public int perfect = 0;

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
		{
		// keep track of whether any good predictions are ever made
		if (DSCollectionUtils.allElementsEqual(computedDistances, AbstractBatchClusteringMethod.UNKNOWN_DISTANCE))
			{
			computedDistances = null;
			}
		if (DSCollectionUtils.allElementsEqual(computedDistances, 1.0))
			{
			labelWithinClusterProbabilities = null;
			}
		if (DSCollectionUtils.allElementsEqual(secondToBestDistanceRatios, 0.0))
			{
			secondToBestDistanceRatios = null;
			}
		if (DSCollectionUtils.allElementsEqual(secondToBestVoteRatios, 0.0))
			{
			secondToBestVoteRatios = null;
			}
		if (DSCollectionUtils.allElementsEqual(labelWithinClusterProbabilities, 1.0))
			{
			labelWithinClusterProbabilities = null;
			}
		/*		int[] correctCounts = correctProbabilities.getCounts();
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
		if (predictionDistances != null && predictionDistances.size() != 0)
			{
			return (double) DSCollectionUtils.countMatches(predictionDistances, new EqualPredicate(0.0))
					/ (double) predictionDistances.size();
			}
		else
			{
			return 0;
			}
		}

	public synchronized void addResult(double broadWrongness, double detailedWrongness, double bestDistance,
	                                   double secondToBestDistanceRatio, double clusterProb, double bestVoteProportion,
	                                   double secondToBestVoteRatio)
		{
		assert !(Double.isNaN(broadWrongness) || Double.isInfinite(broadWrongness));
		assert !(Double.isNaN(detailedWrongness) || Double.isInfinite(detailedWrongness));
		assert !(Double.isNaN(bestDistance) || Double.isInfinite(bestDistance));
		assert !(Double.isNaN(secondToBestDistanceRatio) || Double.isInfinite(secondToBestDistanceRatio));
		assert !(Double.isNaN(secondToBestVoteRatio) || Double.isInfinite(secondToBestVoteRatio));
		assert !(Double.isNaN(clusterProb) || Double.isInfinite(clusterProb));

		predictionDistances.add(broadWrongness);
		predictionDistancesWithPrecisionCost.add(detailedWrongness);
		computedDistances.add(bestDistance);
		secondToBestDistanceRatios.add(secondToBestDistanceRatio);
		bestVoteProportions.add(bestVoteProportion);
		secondToBestVoteRatios.add(secondToBestVoteRatio);
		labelWithinClusterProbabilities.add(clusterProb);
		}

	public synchronized void setNumClusters(int numClusters)
		{
		this.numClusters = numClusters;
		}

	public synchronized void setTestSamples(int i)
		{
		testSamples = i;
		}

	public synchronized void incrementTotalTrainingMass(double weightSum)
		{
		totalTrainingMass += weightSum;
		}

	public synchronized void incrementShouldHaveBeenUnknown()
		{
		shouldHaveBeenUnknown++;
		}

	public synchronized void incrementShouldNotHaveBeenUnknown()
		{
		shouldNotHaveBeenUnknown++;
		}

	public synchronized void incrementUnknown()
		{
		unknown++;
		}

	public void setTrainingSeconds(double trainingSeconds)
		{
		this.trainingSeconds = trainingSeconds;
		}

	public void setTestingSeconds(double testingSeconds)
		{
		this.testingSeconds = testingSeconds;
		}

	public int getNumClusters()
		{
		return numClusters;
		}

	public int getUnknown()
		{
		return unknown;
		}

	public int getShouldHaveBeenUnknown()
		{
		return shouldHaveBeenUnknown;
		}

	public int getShouldNotHaveBeenUnknown()
		{
		return shouldNotHaveBeenUnknown;
		}

	public int getTestSamples()
		{
		return testSamples;
		}

	public double getTrainingSeconds()
		{
		return trainingSeconds;
		}

	public double getTestingSeconds()
		{
		return testingSeconds;
		}

	public double getTotalTrainingMass()
		{
		return totalTrainingMass;
		}
	}
