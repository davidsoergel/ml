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
	private List<Double> predictionDistances = new ArrayList<Double>();
	private List<Double> predictionDistancesWithPrecisionCost = new ArrayList<Double>();
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


	public void putResults(HierarchicalTypedPropertyNode<String, Object> resultsNode, String labelDistancesName)
		{

		resultsNode.addChild("numPopulatedRealLabels", numPopulatedRealLabels());
		resultsNode.addChild("numPredictedLabels", numPredictedLabels());

		resultsNode.addChild("labelWithinClusterProbabilities", getLabelWithinClusterProbabilitiesArray());

		resultsNode.addChild("accuracy", new Double(accuracy()));
		resultsNode.addChild("accuracyGivenClassified", accuracyGivenClassified());
		resultsNode.addChild("sensitivity", classNormalizedSensitivity());
		resultsNode.addChild("specificity", classNormalizedSpecificity());
		resultsNode.addChild("precision", classNormalizedPrecision());
		resultsNode.addChild("unknownLabel", unknown());

		storeLabelDistances(labelDistancesName, getPredictionDistances(), resultsNode);
		storeLabelDistances(labelDistancesName + "ToSample", getPredictionDistancesWithPrecisionCost(), resultsNode);
		}


	public static void storeLabelDistances(String labelDistanceName, List<Double> labelDistances,
	                                       HierarchicalTypedPropertyNode<String, Object> resultsNode)
		{
		resultsNode.addChild(labelDistanceName, labelDistances.toArray(DSArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));

		if (!labelDistances.isEmpty())
			{
			// we used "-0.1" to indicate a strain-perfect match, better than a match at distance 0.
			// but for the mean and stddev, we should just consider those to be 0.
			List<Double> d = new ArrayList<Double>(labelDistances.size());
			for (Double labelDistance : labelDistances)
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

			Histogram1D h = null;

			h = new EqualWeightHistogram1D(100,
			                               DSArrayUtils.toPrimitive((Double[]) labelDistances.toArray(new Double[]{})));


			try
				{
				resultsNode.addChild(labelDistanceName + "90", h.topOfBin(89));
				resultsNode.addChild(labelDistanceName + "95", h.topOfBin(94));
				resultsNode.addChild(labelDistanceName + "99", h.topOfBin(98));
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
				resultsNode.addChild(labelDistanceName + "Mean", mean);
				resultsNode.addChild(labelDistanceName + "StdDev", DSArrayUtils.stddev(labelDistances, mean));
				}
			}
		}
	}
