/*
 * Copyright (c) 2008 Regents of the University of California
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
import com.davidsoergel.stats.DistributionProcessor;
import com.davidsoergel.stats.DistributionProcessorException;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceReader;
import edu.berkeley.compbio.sequtils.TranslationException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class RonPSA extends RonPSANode
	{
	private static final Logger logger = Logger.getLogger(RonPSA.class);

	public RonPSA()
		{

		}

	/*	public RonPSA(byte[] alphabet)
	   {
	   super(new byte[0], alphabet);
	   }*/

	/**
	 * Convert a PST into a PSA as described in Ron et al Appendix B.  Assumes we're starting from an empty node.
	 *
	 * @param pst
	 */
	private void convertFrom(RonPST pst)
		{
		setAlphabet(pst.getAlphabet());
		setId(new byte[0]);
		copyProbsFromSpectrumRecursively(pst);
		buildPSARecursivelyFromPSTNode(pst, pst);
		updateLogProbsRecursive();
		// at this point we have a pure tree with a full set of probabilities at every node

		setBacklinks();
		}

	private void buildPSARecursivelyFromPSTNode(RonPSTNode pstNode, SequenceSpectrum spectrum)
		{
		getOrAddNodeAndIntermediates(pstNode.getIdBytes(), spectrum);

		for (RonPSTNode pstUpstream : pstNode.getUpstreamNodes())
			{
			if (pstUpstream != null)
				{
				buildPSARecursivelyFromPSTNode(pstUpstream, spectrum);
				}
			}
		}

	private RonPSANode getOrAddNodeAndIntermediates(byte[] id, SequenceSpectrum spectrum)
		{
		//RonPSANode psaNode = new RonPSANode(id, alphabet);
		RonPSANode result = getDescendant(id);
		if (result == null)
			{
			RonPSANode parent = getOrAddNodeAndIntermediates(ArrayUtils.prefix(id, id.length - 1), spectrum);
			result = parent.addChild(id[id.length - 1]);
			result.copyProbsFromSpectrumRecursively(spectrum);
			}
		return result;
		}


	public void learn(double branchAbsoluteMin, double branchConditionalMin, double pRatioMinMax, int l_max,
	                  SequenceSpectrum fromSpectrum, DistributionProcessor<RonPSA> completionProcessor)
			throws DistributionProcessorException
		{
		RonPST pst = new RonPST(branchAbsoluteMin, branchConditionalMin, pRatioMinMax, l_max, fromSpectrum);
		convertFrom(pst);
		completionProcessor.process(this);
		}

	private List<RonPSANode> setBacklinks()
		{
		List<RonPSANode> result = new LinkedList();
		Queue<RonPSANode> breadthFirstQueue = new LinkedList<RonPSANode>();
		breadthFirstQueue.add(this);
		while (!breadthFirstQueue.isEmpty())
			{
			RonPSANode next = breadthFirstQueue.remove();
			next.setBacklinksUsingRoot(this, breadthFirstQueue);
			result.add(next);
			}
		return result;
		}

	//	@NotNull
	//	public MarkovTreeNode nextNode(byte sigma)
	//		{
	//		return children.get(sigma);
	/*		MarkovTreeNode result = children.get(sigma);
					if (result == null)
						{
						result = backlinkChildren.get(sigma);
						}
					return result;*/
	//		}


	/**
	 * Computes the total log probability of generating the given sequence fragment under the model.  This differs from
	 * {@link #totalProbability(byte[])} in that the sequence fragment is not given explicitly but only as metadata.  Thus
	 * its probability may be computed from summary statistics that are already available in the given SequenceFragment
	 * rather than from the raw sequence.  Also, because these probabilities are typically very small, the result is
	 * returned in log space (indeed implementations will likely compute them in log space).
	 * <p/>
	 * Note this computes the probability only in the forward direction (i.e., it does not take a reverse complement
	 * also).
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
		RonPSANode currentNode = this;
		int count = 0;
		int desiredLength = sequenceFragment.getDesiredLength();
		while (count < desiredLength)
			{
			try
				{
				int c = in.readTranslated();
				double logConditionalProbability = currentNode.logConditionalProbabilityByAlphabetIndex(c);

				logger.debug("Conditional at " + new String(currentNode.getIdBytes()) + " " + (char) getAlphabet()[c]
						+ " = " + currentNode
						.conditionalProbabilityByAlphabetIndex(c));
				logprob += logConditionalProbability;
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
	}
