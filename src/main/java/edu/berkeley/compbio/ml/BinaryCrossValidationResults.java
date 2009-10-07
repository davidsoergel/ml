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

	float trueTrueRate()
		{
		return (float) tt / (float) numExamples;
		}

	float falseTrueRate()
		{
		return (float) ft / (float) numExamples;
		}

	float trueFalseRate()
		{
		return (float) tf / (float) numExamples;
		}

	float falseFalseRate()
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

	public int getNumExamples()
		{
		return numExamples;
		}

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
	}
