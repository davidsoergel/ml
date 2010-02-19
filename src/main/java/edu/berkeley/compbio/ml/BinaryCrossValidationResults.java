package edu.berkeley.compbio.ml;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BinaryCrossValidationResults extends CrossValidationResults
	{
	protected int numExamples;
	protected int tt;
	protected int tf;
	protected int ft;
	protected int ff;

	public float trueTrueRate()
		{
		return (float) tt / (float) numExamples;
		}

	public float falseTrueRate()
		{
		return (float) ft / (float) numExamples;
		}

	public float trueFalseRate()
		{
		return (float) tf / (float) numExamples;
		}

	public float falseFalseRate()
		{
		return (float) ff / (float) numExamples;
		}

	public float accuracy()
		{
		return (float) (tt + ff) / (float) numExamples;
		}

	public float accuracyGivenClassified()
		{
		// ** for now everything was classified
		return accuracy();
		}

	public float unknown()
		{
		// ** for now everything was classified
		return 0F;
		}

	public float classNormalizedSensitivity()
		{
		return ((float) tt / (float) (tt + tf) + (float) ff / (float) (ff + ft)) / 2f;
		}

	public float sensitivityA()
		{
		return (float) tt / (float) (tt + tf);
		}

	public float sensitivityB()
		{
		return (float) ff / (float) (ff + ft);
		}

	public float precisionA()
		{
		return (float) tt / (float) (tt + ft);
		}

	public float precisionB()
		{
		return (float) ff / (float) (ff + tf);
		}

	public int getNumExamples()
		{
		return numExamples;
		}

	/**
	 * A measure of the tradeoff between TF and FT.  When this is 0, there are equally many false positives and false
	 * negatives (in absolute numbers).  When it's greater than 0, there are more false negatives (TF), and when it's less
	 * than 0, there are more false positives (FT).
	 *
	 * @return
	 */
	public float falseBalance()
		{
		int denom = ft + tf;
		if (denom == 0)
			{
			return 0;
			}
		return 2f * ((float) tf / (float) denom) - 1f;
		}


	public float absFalseBalance()
		{
		return Math.abs(falseBalance());
		}

	public String toString()
		{
		final StringBuffer sb = new StringBuffer();
		sb.append(String.format("Cross Validation Classified = %.2f%%\n", 100.0 * (1.0 - unknown())));
		sb.append(String.format("True->True: %.2f%%, False->False: %.2f%%, True->False: %.2f%%, False->True: %.2f%%\n",
		                        100.0 * trueTrueRate(), 100.0 * falseFalseRate(), 100.0 * trueFalseRate(),
		                        100.0 * falseTrueRate()));

		sb.append(String.format("Cross Validation Accuracy (of those classified) = %.2f%%\n",
		                        100.0 * accuracyGivenClassified()));
		sb.append(String.format("Cross Validation Accuracy (of total) = %.2f%%\n", 100.0 * accuracy()));

		sb.append(String.format("Sensitivity(true): %.2f%%, Sensitivity(false): %.2f%%\n", 100.0 * sensitivityA(),
		                        100.0 * sensitivityB()));

		sb.append(String.format("Class-normalized sensitivity: %.2f%%\n", 100.0 * classNormalizedSensitivity()));

		sb.append(String.format("Precision(true): %.2f%%, Precision(false): %.2f%%\n", 100.0 * precisionA(),
		                        100.0 * precisionB()));

		sb.append(String.format("False balance (>0 => TF, <0 => FT): %.2f\n", falseBalance()));

		return sb.toString();
		}
	}
