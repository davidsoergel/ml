package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.runutils.HierarchicalTypedPropertyNode;
import com.davidsoergel.stats.EqualWeightHistogram1D;
import com.davidsoergel.stats.Histogram1D;
import com.davidsoergel.stats.StatsException;
import edu.berkeley.compbio.ml.MultiClassCrossValidationResults;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private final List<Double> predictionDistances = new ArrayList<Double>();
	private final List<Double> predictionDistancesWithPrecisionCost = new ArrayList<Double>();
	/**
	 * Probability of the best label, given the best cluster.  Used for unsupervised clustering where each cluster may
	 * contain samples with multiple labels.
	 */
	private List<Double> labelWithinClusterProbabilities = new ArrayList<Double>();


	public void addSample(final L realLabel, final L predictedLabel, final double clusterProb,
	                      final double broadWrongness, final double detailedWrongness)
		{
		super.addSample(realLabel, predictedLabel);

		predictionDistances.add(broadWrongness);
		predictionDistancesWithPrecisionCost.add(detailedWrongness);
		labelWithinClusterProbabilities.add(clusterProb);
		}


	public List<Double> getPredictionDistances()
		{
		return predictionDistances;
		}

	public List<Double> getPredictionDistancesWithPrecisionCost()
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

	public void putResults(final HierarchicalTypedPropertyNode<String, Object> resultsNode, final List<String> keyPath,
	                       final String labelDistancesName, final Map<L, String> friendlyLabelMap)
		{

		resultsNode.addChild(keyPath, "numPopulatedRealLabels", numPopulatedRealLabels());
		resultsNode.addChild(keyPath, "numPredictedLabels", numPredictedLabels());

		resultsNode.addChild(keyPath, "labelWithinClusterProbabilities", getLabelWithinClusterProbabilitiesArray());

		resultsNode.addChild(keyPath, "accuracy", new Double(accuracy()));
		resultsNode.addChild(keyPath, "accuracyGivenClassified", accuracyGivenClassified());
		resultsNode.addChild(keyPath, "classNormalizedSensitivity", classNormalizedSensitivity());
		resultsNode.addChild(keyPath, "classNormalizedSpecificity", classNormalizedSpecificity());
		resultsNode.addChild(keyPath, "classNormalizedPrecision", classNormalizedPrecision());
		resultsNode.addChild(keyPath, "unknownLabel", unknown());

		storeLabelDistances(labelDistancesName, getPredictionDistances(), resultsNode, keyPath);
		storeLabelDistances(labelDistancesName + "ToSample", getPredictionDistancesWithPrecisionCost(), resultsNode,
		                    keyPath);

		resultsNode.addChild(keyPath, "classLabels", getLabels().toArray(DSArrayUtils.EMPTY_STRING_ARRAY));
		resultsNode.addChild(keyPath, "friendlyLabels",
		                     getFriendlyLabels(friendlyLabelMap).toArray(DSArrayUtils.EMPTY_STRING_ARRAY));
		resultsNode.addChild(keyPath, "sensitivity", DSArrayUtils.castToDouble(getSensitivities()));
		resultsNode.addChild(keyPath, "specificity", DSArrayUtils.castToDouble(getSpecificities()));
		resultsNode.addChild(keyPath, "precision", DSArrayUtils.castToDouble(getPrecisions()));
		resultsNode.addChild(keyPath, "predictedCounts", DSArrayUtils.castToDouble(getPredictedCounts()));
		resultsNode.addChild(keyPath, "actualCounts", DSArrayUtils.castToDouble(getActualCounts()));

		/*	for(L label : getLabels())
		   {
		   asdf
		   }*/
		}


	public static void storeLabelDistances(final String labelDistanceName, final List<Double> labelDistances,
	                                       final HierarchicalTypedPropertyNode<String, Object> resultsNode,
	                                       final List<String> keyPath)
		{
		resultsNode
				.addChild(keyPath, labelDistanceName, labelDistances.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));

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
				else
					{
					d.add(labelDistance);
					}
				}

			final Histogram1D h = new EqualWeightHistogram1D(100, DSArrayUtils.toPrimitive(
					labelDistances.toArray(new Double[labelDistances.size()])));


			try
				{
				resultsNode.addChild(keyPath, labelDistanceName + "90", h.topOfBin(89));
				resultsNode.addChild(keyPath, labelDistanceName + "95", h.topOfBin(94));
				resultsNode.addChild(keyPath, labelDistanceName + "99", h.topOfBin(98));
				// note the highest bin is #99, so the top of that bin is the 100th %ile
				}
			catch (StatsException e)
				{
				throw new ClusterRuntimeException(e);
				}

			final double mean = DSArrayUtils.mean(labelDistances);
			if (Double.isInfinite(mean))
				{
				logger.warn("labelDistance mean is Infinity");
				}
			else
				{
				resultsNode.addChild(keyPath, labelDistanceName + "Mean", mean);
				resultsNode.addChild(keyPath, labelDistanceName + "StdDev", DSArrayUtils.stddev(labelDistances, mean));
				}
			}
		}
	}
