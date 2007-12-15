/* $Id$ */

/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
import com.davidsoergel.stats.DistributionProcessor;
import com.davidsoergel.stats.DistributionProcessorException;
import com.davidsoergel.stats.Multinomial;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceReader;
import edu.berkeley.compbio.sequtils.TranslationException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
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
public class RonPST extends MarkovTreeNode//implements SequenceSpectrumTranslator
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(RonPST.class);
	//private double eta0, eta1, eta2, gammaMin, L;

	//private double pMin, alpha, pRatioMinMax, gammaMin;
	//private int l_max;

	// --------------------------- CONSTRUCTORS ---------------------------

	/*
	 Pmin = (1.-eta1) * eta0
	 alpha = eta2
	 gammaMin
	 pRatioMinMax = p_ratio = (1 + (3 * eta2))
 */

	@Property(helpmessage = "A distribution processor to run on this NucleotideKtrie",
	          defaultvalue = "", isNullable = true)
	//isPlugin = true,
	public DistributionProcessor<RonPST> completionProcessor;

	public RonPST()//String injectorId)
		{
		//ThreadLocalRun.getProps().injectProperties(injectorId, this);
		}

	/**
	 * Constructs a new Probabilistic Suffix Tree according to the Learn-PST algorithm (Ron et al 1996 p. 13), based on an
	 * existing sequence spectrum (typically, the set of all word counts; or naively just the sequence itself, if we don't
	 * mind re-scanning it a whole bunch of times as they seem to suggest).
	 *
	 * @param pMin         the minimum total probability of a string that should be taken seriously.  Branches with
	 *                     probabilities less than this will be pruned.
	 * @param alpha        the "headroom" over the smoothing factor gammaMin that determines whether a branch is to be
	 *                     pruned or not.  a branch must have an empirical probability of at least (1 + alpha) * gammaMin
	 *                     in order to be retained.
	 * @param pRatioMinMax The ratio threshold for considering the probability of a symbol to have changed compared to the
	 *                     back-off prior.  That is, either the symbol probability must be a factor of pRatioMinMax greater
	 *                     than its back-off prior value, or vice versa, in order for the probability to be taken
	 *                     seriously.  Note that values less than 1 would cause all symbol probabilities to pass the test,
	 *                     so those aren't very useful.
	 * @param gammaMin     the minimum conditional probability of a symbol that we are interested in.  In the Ron /
	 *                     Bejerano formulation, this is used as a smoothing factor; no conditional probability may be less
	 *                     than this parameter after smoothing.  Even in the absence of smoothing, this parameter
	 *                     contributes to the determination of whether a branch is to be pruned or not, since if the
	 *                     empirical probability of a branch before smoothing is less than the smoothing factor, it's
	 *                     considered to be noise and should be pruned.
	 * @param l_max        the maximum depth of the tree (that is, the maximum memory length)
	 * @param prob         the SequenceSpectrum providing the symbol conditional probabilities from which the PST will be
	 *                     learned
	 * @throws SequenceSpectrumException if something goes wrong with the given spectrum
	 */
	public RonPST(double pMin, double alpha, double pRatioMinMax, double gammaMin, int l_max, SequenceSpectrum prob)
		//throws SequenceSpectrumException//DistributionException,
		{
		this();
		learn(pMin, alpha, pRatioMinMax, gammaMin, l_max, prob);
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
		}

	private List<MarkovTreeNode> setBacklinks()
		{
		List<MarkovTreeNode> result = new LinkedList();
		Queue<MarkovTreeNode> breathFirstQueue = new LinkedList<MarkovTreeNode>();
		breathFirstQueue.add(this);
		while (!breathFirstQueue.isEmpty())
			{
			MarkovTreeNode next = breathFirstQueue.remove();
			next.setBacklinksUsingRoot(this, breathFirstQueue);
			result.add(next);
			}
		return result;
		}


	public void learn(double pMin, double alpha, double pRatioMinMax, double gammaMin, int l_max, SequenceSpectrum prob)
		{
		setId(new byte[]{});
		setAlphabet(prob.getAlphabet());

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

		for (byte c : prob.getAlphabet())
			{
			byte[] s = new byte[]{c};
			try
				{
				if (prob.totalProbability(s) >= pMin)
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
			for (byte sigma : prob.getAlphabet())
				{
				try
					{
					double conditional = prob.conditionalProbability(sigma, s);
					double suffixConditional = prob.conditionalProbability(sigma, ArrayUtils.suffix(s, 1));

					double probRatio = conditional / suffixConditional;
					logger.debug("" + conditional + " / " + suffixConditional + " = " + probRatio);
					if ((conditional >= (1. + alpha) * gammaMin) &&
							// for some reason Ron et al only want to test this one way, but Bejerano, Kermorvant, etc.
							// do it both ways, and that makes more sense anyway
							((probRatio >= pRatioMinMax) || (probRatio <= (1. / pRatioMinMax))))
						{
						logger.debug("" + conditional + " / " + suffixConditional + " = " + probRatio);
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
				for (byte sigma2 : prob.getAlphabet())
					{
					byte[] s2 = ArrayUtils.prepend(sigma2, s);
					try
						{
						if (prob.totalProbability(s2) >= pMin)
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
		completeAndCopyProbsFrom(prob);
		logger.debug("2");


		// now we have the right tree structure, with the empirical probabilities assigned.

		// Ron et al split the steps up differently, in that they build the tree structure first, and
		// then simultaneously assign the probabilities and smooth them.  We prefer to do the
		// smoothing as a distinct step.


		// Step 3 (smoothing)

		// ** Smoothing framework
		// in this framework, smoothing is accomplished simply by constructing a new
		// SequenceSpectrum of the appropriate type, i.e. KneserNeySmoothedSpectrum

		//new KneserNeyPSTSmoother().smooth(this);
		/*	for (MarkovTreeNode n : thePST)
		   {
		   n.setGamma();
		   }*/

		//return root;
		List<MarkovTreeNode> breadthFirstList = setBacklinks();

		int total = 0, leaves = 0, maxdepth = 0;
		double avgdepth = 0;
		for (MarkovTreeNode node : breadthFirstList)
			{
			total++;
			if (node.isLeaf())
				{
				leaves++;
				int depth = node.getId().length();
				avgdepth += depth;
				maxdepth = Math.max(maxdepth, depth);
				}
			}
		avgdepth /= leaves;
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
			add(s);
			s = ArrayUtils.suffix(s, 1);
			}
		}

	/**
	 * Computes the conditional probability of generating a symbol given a prefix under the model, backing off to shorter
	 * prefixes as needed if the given prefix is not explicitly represented.
	 *
	 * @param sigma  a byte specifying the symbol whose probability is to be computed
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the conditional probability, a double value between 0 and 1, inclusive
	 * @see #fragmentLogProbability(edu.berkeley.compbio.ml.strings.SequenceFragment)
	 */
	public double conditionalProbability(byte sigma, byte[] prefix) throws SequenceSpectrumException
		{
		//return getLongestSuffix(ArrayUtils.append(prefix, sigma)).conditionalProbability(sigma);
		return getLongestSuffix(prefix).conditionalProbability(sigma);
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
		// simply follow the MarkovTreeNode as a state machine, using backlinks
		SequenceReader in = sequenceFragment.getResetReader();
		in.setTranslationAlphabet(getAlphabet());
		double logprob = 0;
		MarkovTreeNode currentNode = this;
		int count = 0;
		int desiredLength = sequenceFragment.getDesiredLength();
		while (count < desiredLength)
			{
			try
				{
				int c = in.readTranslated();
				logprob += currentNode.logConditionalProbabilityByAlphabetIndex(c);
				currentNode = currentNode.nextNodeByAlphabetIndex(c);
				}
			catch (NotEnoughSequenceException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e);
				}
			catch (TranslationException e)
				{
				// probably a bad input character
				logger.debug(" at " + in, e);

				// ignore it, but reset the state machine
				currentNode = this;
				}
			catch (IOException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e);
				}
			catch (FilterException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e);
				}
			count++;
			}
		return logprob;
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
		return getLongestSuffix(prefix).conditionalsFrom(new byte[]{});
		}

	public String toLongString()
		{
		StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(sb, Locale.US);
		appendString(formatter, "");
		return sb.toString();
		}

	public MarkovTreeNode getBackoffPrior(byte[] id) throws SequenceSpectrumException
		{
		return getLongestSuffix(ArrayUtils.suffix(id, 1));
		}

	}
