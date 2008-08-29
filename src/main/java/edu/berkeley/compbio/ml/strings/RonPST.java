/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.DistributionProcessor;
import com.davidsoergel.stats.DistributionProcessorException;
import com.davidsoergel.stats.Multinomial;
import com.davidsoergel.stats.MutableDistribution;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A probabilistic suffix tree, as described in
 * <p/>
 * D. Ron, Y. Singer and N. Tishby. The power of amnesia: learning probabilistic automata with variable memory length.
 * Machine Learning, 25:117-149, 1996.  http://citeseer.ist.psu.edu/article/ron96power.html
 *
 * @Author David Soergel (soergel@compbio.berkeley.edu)
 */
@PropertyConsumer
public class RonPST extends RonPSTNode
		implements SequenceSpectrum<RonPST>, MutableDistribution//implements SequenceSpectrumTranslator
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(RonPST.class);
	//private double eta0, eta1, eta2, gammaMin, L;

	//private double pMin, alpha, pRatioMinMax, gammaMin;
	//private int l_max;
	/*
	 Pmin = (1.-eta1) * eta0
	 alpha = eta2
	 gammaMin
	 pRatioMinMax = p_ratio = (1 + (3 * eta2))
 */

	private String label;

	public String getLabel()
		{
		return label;
		}

	public void setLabel(String label)
		{
		this.label = label;
		}

	@Property(helpmessage = "A distribution processor to run on this PST, typically used for smoothing",
	          defaultvalue = "edu.berkeley.compbio.ml.strings.RonPSTSmoother", isNullable = true)
	//isPlugin = true,
	public DistributionProcessor<RonPST> completionProcessor;


	// --------------------------- CONSTRUCTORS ---------------------------

	public RonPST()//String injectorId)
		{
		//ResultsCollectingProgramRun.getProps().injectProperties(injectorId, this);
		}

	/*
	     * @param alpha                the "headroom" over the smoothing factor gammaMin that determines whether a branch is to
		 *                             be pruned or not.  a branch must have a conditional probability of at least (1 + alpha)
		 *                             * gammaMin in order to be retained.
		 *  * @param gammaMin             the minimum conditional probability of a symbol that we are interested in.  In the Ron /
		 *                             Bejerano formulation, this is used as a smoothing factor; no conditional probability may
		 *                             be less than this parameter after smoothing.  Even in the absence of smoothing, this
		 *                             parameter contributes to the determination of whether a branch is to be pruned or not,
		 *                             since if the empirical probability of a branch before smoothing is less than the
		 *                             smoothing factor, it's considered to be noise and should be pruned.

	 */
	/**
	 * Constructs a new Probabilistic Suffix Tree according to the Learn-PST algorithm (Ron et al 1996 p. 13), based on an
	 * existing sequence spectrum (typically, the set of all word counts; or naively just the sequence itself, if we don't
	 * mind re-scanning it a whole bunch of times as they seem to suggest).
	 *
	 * @param branchAbsoluteMin    the minimum total probability of a string that should be taken seriously.  Branches with
	 *                             probabilities less than this will be pruned.  Ron et al. call this pMin.
	 * @param branchConditionalMin the minimum conditional probability of a symbol that we are interested in.  Ron et al.
	 *                             express this as (1 + alpha) * gammaMin.  Here we just provide it directly since the
	 *                             smoothing process (and hence gammaMin) are broken out.
	 * @param pRatioMinMax         The ratio threshold for considering the probability of a symbol to have changed compared
	 *                             to the back-off prior.  That is, either the symbol probability must be a factor of
	 *                             pRatioMinMax greater than its back-off prior value, or vice versa, in order for the
	 *                             probability to be taken seriously.  Note that values less than 1 would cause all symbol
	 *                             probabilities to pass the test, so those aren't very useful.
	 * @param l_max                the maximum depth of the tree (that is, the maximum memory length)
	 * @param prob                 the SequenceSpectrum providing the symbol conditional probabilities from which the PST
	 *                             will be learned
	 * @throws SequenceSpectrumException if something goes wrong with the given spectrum
	 */


	public RonPST(double branchAbsoluteMin, double branchConditionalMin, double pRatioMinMax, int l_max,
	              SequenceSpectrum prob)
		//throws SequenceSpectrumException//DistributionException,
		{
		this();
		learn(branchAbsoluteMin, branchConditionalMin, pRatioMinMax, l_max, prob);
		}

	/*	public RonPST(double pMin, double alpha, double pRatioMinMax, double gammaMin, int l_max, SequenceSpectrum prob)
	   //throws SequenceSpectrumException//DistributionException,
	   {
	   this();
	   learn(pMin, alpha, pRatioMinMax, gammaMin, l_max, prob);
	   }*/

	public void learn(double branchAbsoluteMin, double branchConditionalMin, double pRatioMinMax, int l_max,
	                  SequenceSpectrum fromSpectrum)
		{
		setId(new byte[]{});
		setAlphabet(fromSpectrum.getAlphabet());

		/*
		  this.pMin = pMin;
		  this.alpha = alpha;
		  this.pRatioMinMax = pRatioMinMax;
		  this.gammaMin = gammaMin;
		  this.l_max = l_max;
  */


		// First Phase

		// Step 1

		Set<byte[]> remainingSequences = new HashSet<byte[]>();

		for (byte c : fromSpectrum.getAlphabet())
			{
			byte[] s = new byte[]{c};
			try
				{
				if (fromSpectrum.totalProbability(s) >= branchAbsoluteMin)//pMin)
					{
					remainingSequences.add(s);
					}
				}
			catch (SequenceSpectrumException e)
				{
				logger.warn("Unknown probability: " + new String(s));
				logger.info(e);
				// too bad, the requested probability is not known
				}
			}

		// Step 2

		while (!remainingSequences.isEmpty())
			{
			byte[] s = remainingSequences.iterator().next();

			// A)
			remainingSequences.remove(s);

			// B)
			for (byte sigma : fromSpectrum.getAlphabet())
				{
				try
					{
					double conditional = fromSpectrum.conditionalProbability(sigma, s);
					double suffixConditional = fromSpectrum.conditionalProbability(sigma, ArrayUtils.suffix(s, 1));

					double probRatio = conditional / suffixConditional;
					//	logger.debug("" + conditional + " / " + suffixConditional + " = " + probRatio);
					if ((conditional >= branchConditionalMin)// (1. + alpha) * gammaMin)
							&&
							// for some reason Ron et al only want to test this one way, but Bejerano, Kermorvant, etc.
							// do it both ways, and that makes more sense anyway
							((probRatio >= pRatioMinMax) || (probRatio <= (1. / pRatioMinMax))))
						{
						//	logger.debug("" + conditional + " / " + suffixConditional + " = " + probRatio);
						addAllSuffixes(s);
						break;
						}
					}
				catch (SequenceSpectrumException e)
					{
					logger.warn("Unknown probability: " + new String(s));
					// too bad, the requested probability is not known
					}
				}
			// C)
			if (s.length < l_max)
				{
				for (byte sigma2 : fromSpectrum.getAlphabet())
					{
					byte[] s2 = ArrayUtils.prepend(sigma2, s);
					try
						{
						if (fromSpectrum.totalProbability(s2) >= branchAbsoluteMin)//pMin)
							{
							remainingSequences.add(s2);
							}
						}
					catch (SequenceSpectrumException e)
						{
						// too bad, the requested probability is not known
						}
					}
				}
			}


		// Second Phase

		// Step 1 (unnecessary)
		// Step 2: add missing nodes to the tree

		//complete();
		logger.debug("1");
		copyProbsFrom(fromSpectrum);
		logger.debug("2");


		// now we have the right tree structure, with the empirical probabilities assigned.

		// Ron et al split the steps up differently, in that they build the tree structure first, and
		// then simultaneously assign the probabilities and smooth them.  We prefer to do the
		// smoothing as a distinct step.


		// Step 3 (smoothing)

		// REVIEW Smoothing framework
		// in this framework, smoothing is accomplished simply by constructing a new
		// SequenceSpectrum of the appropriate type, i.e. KneserNeySmoothedSpectrum

		//new KneserNeyPSTSmoother().smooth(this);
		/*	for (MarkovTreeNode n : thePST)
		   {
		   n.setGamma();
		   }*/

		//return root;
		//List<MarkovTreeNode> breadthFirstList = setBacklinks();


		if (completionProcessor != null)
			{
			try
				{
				completionProcessor.process(this);
				}
			catch (DistributionProcessorException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumRuntimeException(e);
				}
			}

		updateLogProbsRecursive();

		// diagnostics
		int total = 0, leaves = 0, maxdepth = 0;
		double avgdepth = 0;
		for (RonPSTNode node : getAllUpstreamNodes())
			{
			total++;
			if (node.isLeaf())
				{
				leaves++;
				int depth = node.getIdBytes().length;//length();
				avgdepth += depth;
				maxdepth = Math.max(maxdepth, depth);
				}
			}
		maxdepth += 1;
		avgdepth /= leaves;
		avgdepth += 1;
		logger.info("Learned Ron PST with " + total + " nodes, " + leaves + " leaves, avg depth " + avgdepth
				+ ", max depth " + maxdepth);
		if (logger.isDebugEnabled())
			{
			logger.debug("\n" + toLongString());
			}
		}

	/**
	 * Adds each suffix of the given string to the tree.  Note that each suffix is started fresh from the root.  (A call to
	 * the regular add() makes all suffixes present further down the tree anyway, which is to say, conditional on the
	 * prefix up to that point.  Here, we want nodes for each suffix without such conditioning.)
	 *
	 * @param s the byte[]
	 */
	private void addAllSuffixes(byte[] s) throws SequenceSpectrumException
		{
		while (s.length > 0)
			{
			addUpstreamNode(s);
			s = ArrayUtils.suffix(s, 1);
			}
		}

	public String toLongString()
		{
		StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(sb, Locale.US);
		appendString(formatter, "");
		return sb.toString();
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface SequenceSpectrum ---------------------

	/**
	 * Clone this object.  Should behave like {@link Object#clone()} except that it returns an appropriate type and so
	 * requires no cast.  Also, we insist that this method be implemented in inheriting classes, so it does not throw
	 * CloneNotSupportedException.
	 *
	 * @return a clone of this instance.
	 * @see Object#clone
	 * @see Cloneable
	 */
	public RonPST clone()
		{
		throw new NotImplementedException();
		}

	/**
	 * Test whether the given object is the same as this one.  Differs from equals() in that implementations of this
	 * interface may contain additional state which make them not strictly equal; here we're only interested in whether
	 * they're equal as far as this interface is concerned, i.e., for purposes of clustering.
	 *
	 * @param other The clusterable object to compare against
	 * @return True if they are equivalent, false otherwise
	 */
	public boolean equalValue(RonPST other)
		{
		throw new NotImplementedException();
		}

	/**
	 * Computes the conditional probability of generating a symbol given a prefix under the model, backing off to shorter
	 * prefixes as needed if the given prefix is not explicitly represented.
	 *
	 * @param sigma  a byte specifying the symbol whose probability is to be computed
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the conditional probability, a double value between 0 and 1, inclusive
	 * @see #fragmentLogProbability(SequenceFragment)
	 */
	public double conditionalProbability(byte sigma, byte[] prefix) throws SequenceSpectrumException
		{
		//return getLongestSuffix(ArrayUtils.append(prefix, sigma)).conditionalProbability(sigma);
		try
			{
			return getLongestSuffix(prefix).getProbs().get(sigma);
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumException(e);
			}
		}

	/**
	 * Computes the conditional probability distribution of symbols given a prefix under the model, backing off to shorter
	 * prefixes as needed if the given prefix is not explicitly represented.
	 *
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the Multinomial conditional distribution of symbols following the given prefix
	 */
	public Multinomial<Byte> conditionalsFrom(byte[] prefix) throws SequenceSpectrumException
		{
		return getLongestSuffix(prefix).getProbs();
		}

	/**
	 * Computes the total log probability of generating the given sequence fragment under the model.  This differs from
	 * {@link #totalProbability(byte[])} in that the sequence fragment is not given explicitly but only as metadata.  Thus
	 * its probability may be computed from summary statistics that are already available in the given SequenceFragment
	 * rather than from the raw sequence.  Also, because these probabilities are typically very small, the result is
	 * returned in log space (indeed implementations will likely compute them in log space).
	 *
	 * @param sequenceFragment the SequenceFragment whose probability is to be computed
	 * @return the natural logarithm of the conditional probability (a double value between 0 and 1, inclusive)
	 */
	public double fragmentLogProbability(SequenceFragment sequenceFragment) throws SequenceSpectrumException
		{
		throw new NotImplementedException();
		}

	/**
	 * Returns the alphabet of this SequenceSpectrum object.
	 *
	 * @return the alphabet (type byte[]) of this SequenceSpectrum object.
	 */
	public byte[] getAlphabet()
		{
		return alphabet;
		}

	/**
	 * Returns the length of the sequence that was scanned to produce this spectrum.  This number may be greater than that
	 * given by {@link #getNumberOfSamples()} because every symbol is not necessarily counted as a sample, depending on the
	 * implementation.
	 *
	 * @return the length (type int) of this Kcount object.
	 */
	public int getOriginalSequenceLength()
		{
		throw new NotImplementedException();
		}

	/**
	 * Returns the maximum length of substrings considered in computing this statistical model of the sequence.  Our
	 * implicit assumption is that the sequences being modeled have some correlation length, and thus that statistical
	 * models of them can be built from substrings up to that length.  Thus, this method tells the maximum correlation
	 * length provided by the model.  A manifestation of this is that conditional probabilities of symbols given a prefix
	 * will cease to change as the prefix is lengthened (to the left) past this length.
	 *
	 * @return the maximum correlation length considered in the model.
	 */
	public int getMaxDepth()
		{
		return super.getMaxDepth();
		}

	/**
	 * Returns the number of samples on which this spectrum is based.
	 *
	 * @return The number of samples
	 */
	public int getNumberOfSamples()
		{
		throw new NotImplementedException();
		}

	public void runBeginTrainingProcessor() throws DistributionProcessorException
		{
		}

	public void runFinishTrainingProcessor() throws DistributionProcessorException
		{
		}

	/**
	 * Chooses a random symbol according to the conditional probabilities of symbols following the given prefix.  Shortcut
	 * equivalent to conditionalsFrom(prefix).sample().byteValue()
	 *
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the chosen symbol
	 */
	public byte sample(byte[] prefix) throws SequenceSpectrumException
		{
		throw new NotImplementedException();
		}

	/**
	 * Chooses a random string according to the conditional probabilities of symbols.
	 *
	 * @param length the length of the desired random string
	 * @return a byte[] of the desired length sampled from this distribution
	 */
	public byte[] sample(int length) throws SequenceSpectrumException
		{
		throw new NotImplementedException();
		}

	public void setIgnoreEdges(boolean b)
		{
		throw new NotImplementedException();
		}

	public void setImmutable()
		{
		throw new NotImplementedException();
		}

	/**
	 * Test whether the given sequence statistics are equivalent to this one.  Differs from equals() in that
	 * implementations of this interface may contain additional state which make them not strictly equal; here we're only
	 * interested in whether they're equal as far as this interface is concerned.
	 * <p/>
	 * Naive implementations will simply test for exact equality; more sophisticated implementations ought to use a more
	 * rigorous idea of "statistically equivalent", though in that case we'll probabably need to provide more parameters,
	 * such as a p-value threshold to use.  Note that the spectra know the number of samples used to generate them, so at
	 * least that's covered.
	 *
	 * @param spectrum the SequenceSpectrum to compare
	 * @return True if the spectra are equivalent, false otherwise
	 */
	public boolean spectrumEquals(SequenceSpectrum spectrum)
		{
		throw new NotImplementedException();
		}

	/**
	 * Computes the probability of generating the given sequence under the model.
	 *
	 * @param s a byte array
	 * @return the probability, a double value between 0 and 1, inclusive
	 */
	public double totalProbability(byte[] s) throws SequenceSpectrumException
		{
		throw new NotImplementedException();
		}

	// -------------------------- OTHER METHODS --------------------------

	/*
	 public RonPSTNode getBackoffPrior(byte[] id) throws SequenceSpectrumException
		 {
		 return getLongestSuffix(ArrayUtils.suffix(id, 1));
		 }
 */

	private RonPSTNode getLongestSuffix(byte[] bytes) throws SequenceSpectrumException
		{
		RonPSTNode currentNode = this;
		for (int i = bytes.length - 1; i >= 0; i--)
			{
			RonPSTNode nextNode = currentNode.getUpstreamNode(bytes[i]);
			if (nextNode == null)
				{
				return currentNode;
				}
			currentNode = nextNode;
			}
		return currentNode;
		}


	/**
	 * updates this object by subtracting another one from it.
	 *
	 * @param object the object to subtract from this one
	 */
	public void decrementBy(RonPST object)
		{
		throw new NotImplementedException();
		}

	/**
	 * updates this object by adding another one to it.
	 *
	 * @param object the object to add to this one
	 */
	public void incrementBy(RonPST object)
		{
		throw new NotImplementedException();
		}


	public void decrementByWeighted(RonPST object, double weight)
		{
		throw new NotImplementedException();
		}

	public void incrementByWeighted(RonPST object, double weight)
		{
		throw new NotImplementedException();
		}


	/**
	 * Returns a new object representing the difference between this one and the given argument.
	 *
	 * @param object the object to be subtracted from this one
	 * @return the difference between this object and the argument
	 */
	public RonPST minus(RonPST object)
		{
		throw new NotImplementedException();
		}

	/**
	 * Returns a new object representing the sum of this one and the given argument.
	 *
	 * @param object the object to be added to this one
	 * @return the sum of this object and the argument
	 */
	public RonPST plus(RonPST object)
		{
		throw new NotImplementedException();
		}

	public RonPST times(double v)
		{
		throw new NotImplementedException();
		}

	public void multiplyBy(double v)
		{
		throw new NotImplementedException();
		}
	}
