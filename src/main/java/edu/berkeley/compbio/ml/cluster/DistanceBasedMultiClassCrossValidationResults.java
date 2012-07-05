package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.stats.EqualWeightHistogram1D;
import com.davidsoergel.stats.Histogram1D;
import com.davidsoergel.stats.StatsException;
import com.davidsoergel.trees.htpn.HierarchicalTypedPropertyNode;
import edu.berkeley.compbio.ml.MultiClassCrossValidationResults;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class DistanceBasedMultiClassCrossValidationResults<L extends Comparable>
		extends MultiClassCrossValidationResults<L>
	{
	private static final Logger logger = Logger.getLogger(DistanceBasedMultiClassCrossValidationResults.class);

	/**
	 * The real distance between the predicted label and the real label ("wrongness") according to a label distance
	 * measure
	 */
	private final ArrayList<Double> predictionDistances = new ArrayList<Double>();
	private final ArrayList<Double> predictionDistancesWithPrecisionCost = new ArrayList<Double>();
	/**
	 * Probability of the best label, given the best cluster.  Used for unsupervised clustering where each cluster may
	 * contain samples with multiple labels.
	 */
	private List<Double> labelWithinClusterProbabilities = new ArrayList<Double>();

	private int shouldHaveBeenUnknown = 0;
	private int shouldNotHaveBeenUnknown = 0;
	private int other = 0;
	private int shouldNotHaveBeenOther = 0;
	private int ignoredSamples;  // samples that were not considered because they violated some condition

	public void addSample(final L realLabel, final L predictedLabel, final double clusterProb,
	                      final double broadWrongness, final double detailedWrongness)
		{
		super.addSample(realLabel, predictedLabel);

		predictionDistances.add(broadWrongness);
		predictionDistancesWithPrecisionCost.add(detailedWrongness);
		labelWithinClusterProbabilities.add(clusterProb);
		}


	public void addIgnoredSample()
		{
		ignoredSamples++;
		}

	public ArrayList<Double> getPredictionDistances()
		{
		return predictionDistances;
		}

	public ArrayList<Double> getPredictionDistancesWithPrecisionCost()
		{
		return predictionDistancesWithPrecisionCost;
		}

	public void finish()
		{
		if (DSCollectionUtils.allElementsEqual(labelWithinClusterProbabilities, 1.0))
			{
			labelWithinClusterProbabilities = null;
			}
		}

	public Double[] getLabelWithinClusterProbabilitiesArray()
		{
		return labelWithinClusterProbabilities == null ? null :
		       labelWithinClusterProbabilities.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY);
		}
/*
	Map<L, String> friendlyLabelMap;

	public void setFriendlyLabelMap(final Map<L, String> friendlyLabelMap)
		{
		this.friendlyLabelMap = friendlyLabelMap;
		}*/

	public void putResults(final HierarchicalTypedPropertyNode<String, Serializable, ?> resultsNode,
	                       //final List<String> keyPath,
	                       final String labelDistancesName, final Map<L, String> friendlyLabelMap)
		{

		resultsNode.addChild("numPopulatedRealLabels", numPopulatedRealLabels());
		resultsNode.addChild("numPredictedLabels", numPredictedLabels());

		resultsNode.addChild("labelWithinClusterProbabilities", getLabelWithinClusterProbabilitiesArray());

		resultsNode.addChild("accuracy", new Double(accuracy()));
		resultsNode.addChild("accuracyGivenClassified", accuracyGivenClassified());
		resultsNode.addChild("classNormalizedSensitivity", classNormalizedSensitivity());
		resultsNode.addChild("classNormalizedSpecificity", classNormalizedSpecificity());
		resultsNode.addChild("classNormalizedPrecision", classNormalizedPrecision());
		resultsNode.addChild("unknownLabel", unknown());
		resultsNode.addChild("shouldHaveBeenUnknown", shouldHaveBeenUnknown);
		resultsNode.addChild("shouldNotHaveBeenUnknown", shouldNotHaveBeenUnknown);
		resultsNode.addChild("other", other);
		resultsNode.addChild("shouldNotHaveBeenOther", shouldNotHaveBeenOther);
		resultsNode.addChild("ignoredSamples", ignoredSamples);
		if (ignoredSamples != 0)
			{
			logger.error("Ignored " + ignoredSamples
			             + " samples that produced errors (e.g., due to insufficient class labels); " + numExamples
			             + " samples remained");
			}

		storeLabelDistances(labelDistancesName, getPredictionDistances(), resultsNode);
		storeLabelDistances(labelDistancesName + "ToSample", getPredictionDistancesWithPrecisionCost(), resultsNode);

		resultsNode.addChild("classLabels", getLabels().toArray(DSArrayUtils.EMPTY_STRING_ARRAY));
		resultsNode.addChild("friendlyLabels", getFriendlyLabels(friendlyLabelMap));
		resultsNode.addChild("sensitivity", DSArrayUtils.castToDouble(getSensitivities()));
		resultsNode.addChild("specificity", DSArrayUtils.castToDouble(getSpecificities()));
		resultsNode.addChild("precision", DSArrayUtils.castToDouble(getPrecisions()));
		resultsNode.addChild("predictedCounts", DSArrayUtils.castToDouble(getPredictedCounts()));
		resultsNode.addChild("actualCounts", DSArrayUtils.castToDouble(getActualCounts()));

		List<Integer> flattenedConfusionMatrix = new ArrayList<Integer>();
		SortedSet<L> labels = getLabels();
		for (L actualLabel : labels)
			{
			for (L predictedLabel : labels)
				{
				flattenedConfusionMatrix.add(getCount(actualLabel, predictedLabel));
				}
			}

		resultsNode.addChild("confusionMatrix", DSArrayUtils.toPrimitiveIntArray(flattenedConfusionMatrix));
		/*	for(L label : getLabels())
		   {
		   asdf
		   }*/
		}


	public static void storeLabelDistances(final String labelDistanceName, final List<Double> labelDistances,
	                                       final HierarchicalTypedPropertyNode<String, Serializable, ?> resultsNode)
		{
		resultsNode.addChild(labelDistanceName, labelDistances.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));

		if (!labelDistances.isEmpty())
			{
			// we used "-0.1" to indicate a strain-perfect match, better than a match at distance 0.
			// but for the mean and stddev, we should just consider those to be 0.
			final List<Double> d = new ArrayList<Double>(labelDistances.size());
			for (final Double labelDistance : labelDistances)
				{
				if (labelDistance < 0)
					{
					d.add(0.);
					}
				else if (labelDistance > 1e100)
					{
					// drop the UNKNOWN_DISTANCE cases; compute the percentiles and averages below based on only the classified cases
					}
				else
					{
					d.add(labelDistance);
					}
				}

			if (d.isEmpty())
				{
				logger.warn("All distances were enormous (e.g., UNKNOWN_DISTANCE); no samples were predicted at all");
				}
			else
				{
				try
					{
					final Histogram1D h =
							new EqualWeightHistogram1D(100, DSArrayUtils.toPrimitive(d.toArray(new Double[d.size()])));

					resultsNode.addChild(labelDistanceName + "90", h.topOfBin(89));
					resultsNode.addChild(labelDistanceName + "95", h.topOfBin(94));
					resultsNode.addChild(labelDistanceName + "99", h.topOfBin(98));
					// note the highest bin is #99, so the top of that bin is the 100th %ile
					}
				catch (StatsException e)
					{
					throw new ClusterRuntimeException(e);
					}

				final double mean = DSArrayUtils.mean(d);
				if (Double.isInfinite(mean))
					{
					logger.warn("labelDistance mean is Infinity");
					}
				else
					{
					resultsNode.addChild(labelDistanceName + "MeanGivenClassified", mean);
					resultsNode.addChild(labelDistanceName + "StdDevGivenClassified", DSArrayUtils.stddev(d, mean));
					}
				}
			}
		}

	public void incrementShouldHaveBeenUnknown()
		{
		shouldHaveBeenUnknown++;
		}

	public void incrementShouldNotHaveBeenUnknown()
		{
		shouldNotHaveBeenUnknown++;
		}

	public void incrementOther()
		{
		other++;
		}

	public void incrementShouldNotHaveBeenOther()
		{
		shouldNotHaveBeenOther++;
		}
	}
