package edu.berkeley.compbio.ml;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multiset;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MultiClassCrossValidationResults<L extends Comparable> extends CrossValidationResults
	{
	private static final Logger logger = Logger.getLogger(MultiClassCrossValidationResults.class);

	private int numExamples;
	private final Map<L, Multiset<L>> confusionMatrix;

	// BAD currently we don't include these in the computations...
	private final Multiset<L> confusionRowNull = new HashMultiset<L>();

	public MultiClassCrossValidationResults()
		{
		confusionMatrix = new MapMaker().makeComputingMap(new Function<L, Multiset<L>>()
		{
		public Multiset<L> apply(L key)
			{
			return new HashMultiset<L>();
			}
		});
		}

	public void sanityCheck()
		{
		int predictionCount = 0;
		for (Multiset<L> ls : confusionMatrix.values())
			{
			predictionCount += ls.size();
			}
		assert predictionCount == numExamples;  // every example got a prediction (perhaps null)
		}


	public void addSample(final L realValue, final L predictedValue)
		{
		Multiset<L> confusionRow = realValue == null ? confusionRowNull : confusionMatrix.get(realValue);
		confusionRow.add(predictedValue);
		numExamples++;
		}

	public float accuracy()
		{
		int correct = 0;
		for (Map.Entry<L, Multiset<L>> entry : confusionMatrix.entrySet())
			{
			correct += entry.getValue().count(entry.getKey());
			}
		return (float) correct / (float) numExamples;
		}

	public float unknown()
		{
		int unknown = 0;
		for (Map.Entry<L, Multiset<L>> entry : confusionMatrix.entrySet())
			{
			unknown += entry.getValue().count(null);
			}
		return (float) unknown / (float) numExamples;
		}

	public float accuracyGivenClassified()
		{
		int correct = 0;
		int unknown = 0;
		for (Map.Entry<L, Multiset<L>> entry : confusionMatrix.entrySet())
			{
			correct += entry.getValue().count(entry.getKey());
			unknown += entry.getValue().count(null);
			}
		return (float) correct / ((float) numExamples - (float) unknown);
		}

	float sensitivity(L label)
		{
		Multiset<L> predictionsForLabel = confusionMatrix.get(label);
		int totalWithRealLabel = predictionsForLabel.size();
		int truePositives = predictionsForLabel.count(label);
		float result = (float) truePositives / (float) totalWithRealLabel;
		return result;
		}

	float precision(L label)
		{
		Multiset<L> predictionsForLabel = confusionMatrix.get(label);

		int truePositives = predictionsForLabel.count(label);
		float result = (float) truePositives / (float) getTotalPredicted(label);
		return result;
		}

	float specificity(L label)
		{
		// == sensitivity( not label )
		// note "unknown" counts as a negative

		Multiset<L> predictionsForLabel = confusionMatrix.get(label);

		int hasLabel = predictionsForLabel.size();
		int hasLabelRight = predictionsForLabel.count(label);  // true positives


		int notLabelWrong = getTotalPredicted(label) - hasLabelRight;  // false negatives
		int notLabel = numExamples - hasLabel;
		int notLabelRight = notLabel - notLabelWrong;   // true negatives

		if (notLabel == 0)
			{
			return 1.0f;
			}

		float result = (float) notLabelRight / (float) notLabel;
		return result;
		}

	int getTotalPredicted(L label)

		{
		int totalWithPredictedLabel = 0;

		// PERF if we want precisions for all the labels, it's inefficient to iterate this n times; in practice it doesn't matter though since there are few enough labels
		for (Map.Entry<L, Multiset<L>> entry : confusionMatrix.entrySet())
			{
			totalWithPredictedLabel += entry.getValue().count(label);
			}
		return totalWithPredictedLabel;
		}

	public float classNormalizedSpecificity()
		{
		float sum = 0;
		for (L label : confusionMatrix.keySet())
			{
			sum += specificity(label);
			}
		return sum / (float) confusionMatrix.size();
		}

	public float classNormalizedSensitivity()
		{
		float sum = 0;
		for (L label : confusionMatrix.keySet())
			{
			sum += sensitivity(label);
			}
		return sum / (float) confusionMatrix.size();
		}

	public float classNormalizedPrecision()
		{
		float sum = 0;
		for (L label : confusionMatrix.keySet())
			{
			float v = precision(label);
			if (!Double.isNaN(v))
				{
				sum += v;
				}
			else
				{
				logger.warn("Label " + label + " did not contribute to precision; " + getTotalPredicted(label)
				            + " predictions");
				}
			}
		return sum / (float) confusionMatrix.size();
		}

	public int numPopulatedRealLabels()
		{
		return confusionMatrix.size();
		}

	public int numPredictedLabels()
		{
		Set<L> predictedLabels = new HashSet<L>();
		for (Multiset<L> ls : confusionMatrix.values())
			{
			predictedLabels.addAll(ls.elementSet());
			}
		return predictedLabels.size();
		}
	}
