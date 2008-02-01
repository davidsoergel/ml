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

import com.davidsoergel.dsutils.AbstractGenericFactoryAware;
import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.dsutils.MathUtils;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

/* $Id$ */

/**
 * A node of a Probabilistic Suffix Tree.  Much of this code is very similar to that found in MarkovTreeNode, but I
 * thought it best to separate them to avoid confusion (i.e., because a PST grows to the left whereas a markov tree
 * grows to the right, and such).
 *
 * @Author David Soergel
 * @Version 1.0
 */
public class RonPSTNode extends AbstractGenericFactoryAware
		//implements SequenceSpectrum<RonPSTNode>, MutableDistribution
		// a node by itself doesn't represent a spectrum like a MarkovTreeNode does; for that you need the root.
	{
	private static final Logger logger = Logger.getLogger(RonPSTNode.class);

	private byte[] id;
	private byte[] alphabet;
	private double[] logprobs;
	private Multinomial<Byte> probs = new Multinomial<Byte>();


	public RonPSTNode()
		{
		}

	// includes only the children, with nulls where the tree does not continue
	private RonPSTNode[] upstreamNodes;

	public RonPSTNode[] getUpstreamNodes()
		{
		return upstreamNodes;
		}

	public Multinomial<Byte> getProbs()
		{
		return probs;
		}

	public void setId(byte[] id)
		{
		this.id = id;
		}

	/**
	 * Constructs a new MarkovTreeNode with the given identifier
	 *
	 * @param id the sequence of symbols leading to this node
	 */
	public RonPSTNode(byte[] id, byte[] alphabet)
		{
		this.id = id;
		setAlphabet(alphabet);
		}

	public void setAlphabet(byte[] alphabet)
		{
		this.alphabet = alphabet;
		logprobs = new double[alphabet.length];
		upstreamNodes = new RonPSTNode[alphabet.length];
		}


	private void setProb(byte b, double prob) throws DistributionException
		{
		probs.put(b, prob);
		}


	public void updateLogProbsRecursive()
		{
		updateLogProbs();
		for (RonPSTNode upstream : upstreamNodes)//.values())
			{
			if (upstream != null)
				{
				upstream.updateLogProbsRecursive();
				}
			}
		}

	public void updateLogProbs()
		{
		try
			{
			for (int i = 0; i < alphabet.length; i++)
				{
				logprobs[i] = MathUtils.approximateLog(probs.get(alphabet[i]));
				}
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		}


	/**
	 * Gets the child node associated with the given sequence, creating it (and nodes along the way) as needed
	 *
	 * @param suffix the sequence of symbols to follow from this node
	 * @return the node at the end of the chain of transitions
	 */
	public RonPSTNode add(byte[] suffix) throws SequenceSpectrumException
		{
		if (suffix.length == 0)
			{
			// this should probably never occur
			return this;
			}
		else if (suffix.length == 1)
			{
			return addUpstreamNode(suffix[0]);
			}
		else if (suffix.length >= 1)
			{
			return addUpstreamNode(suffix[suffix.length - 1]).add(ArrayUtils.prefix(suffix, 1));
			}
		throw new Error("Impossible");
		}

	/**
	 * Gets the child node associated with the given symbol, creating it first if needed.
	 *
	 * @param sigma the transition to follow from this node
	 * @return the node at the other end of the transition
	 */
	public RonPSTNode addUpstreamNode(byte sigma) throws SequenceSpectrumException
		{
		//leaf = false;
		int index = ArrayUtils.indexOf(alphabet, sigma);
		RonPSTNode result = upstreamNodes[index];
		if (result == null)
			{
			result = new RonPSTNode(ArrayUtils.append(id, sigma), alphabet);
			upstreamNodes[index] = result;
			}
		return result;
		}
	}
