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

import com.davidsoergel.dsutils.AbstractGenericFactoryAware;
import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;


/**
 * A node of a Probabilistic Suffix Tree, where each symbol is a byte.  Much of this code is very similar to that found
 * in MarkovTreeNode, but I thought it best to separate them to avoid confusion (i.e., because a PST grows to the left
 * whereas a Markov tree grows to the right, and such).
 * <p/>
 * A PST node by itself doesn't represent a spectrum like a MarkovTreeNode does; for that you need the root.  So, we
 * don't implement SequenceSpectrum or MutableDistribution here.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 * @Author David Soergel
 */
public class RonPSTNode extends AbstractGenericFactoryAware
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(RonPSTNode.class);
	protected byte[] alphabet;

	private byte[] id;
	private double[] logProbs;
	private Multinomial<Byte> probs = new Multinomial<Byte>();

	private boolean leaf = true;

	/**
	 * The upstream node for each symbol, indexed by the alphabet index.  Contains nulls for symbols where no upstream node
	 * exists.
	 */
	private RonPSTNode[] upstreamNodes;


// --------------------------- CONSTRUCTORS ---------------------------

	public RonPSTNode()
		{
		}

	/**
	 * Constructs a new RonPSTNode with the given identifier
	 *
	 * @param id       the suffix associated with this node
	 * @param alphabet the array of valid symbols
	 */
	public RonPSTNode(byte[] id, byte[] alphabet)
		{
		this.id = id;
		setAlphabet(alphabet);
		}

	/**
	 * Sets the array of valid symbols.  The order is important since various other arrays (e.g. upstreamNodes) are of the
	 * same size and use the same indexes.
	 *
	 * @param alphabet
	 */
	public void setAlphabet(byte[] alphabet)
		{
		this.alphabet = alphabet;
		logProbs = new double[alphabet.length];
		upstreamNodes = new RonPSTNode[alphabet.length];
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	public double[] getLogProbs()
		{
		return logProbs;
		}

	/**
	 * Gets the probability distribution over symbols following the suffix specified by this node's id.
	 *
	 * @return a Multinomial<Byte> distribution over symbols.
	 */
	public Multinomial<Byte> getProbs()
		{
		return probs;
		}

	/**
	 * Returns an array of upstream nodes for each symbol, indexed by the alphabet index.  Contains nulls for symbols where
	 * no upstream node exists.
	 */
	public RonPSTNode[] getUpstreamNodes()
		{
		return upstreamNodes;
		}

	/**
	 * Tells whether this node is a leaf or not.
	 *
	 * @return true if this node has no upstream nodes, and false otherwise
	 */
	public boolean isLeaf()
		{
		return leaf;
		}

	/**
	 * Sets the id of this node, which is just the suffix associated with this node represented as a byte[].
	 *
	 * @param id
	 */
	public void setId(byte[] id)
		{
		this.id = id;
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Gets the upstream node associated with the given suffix starting from this node, creating it (and nodes along the
	 * way) as needed
	 *
	 * @param suffix the sequence of symbols to follow from this node
	 * @return the node at the end of the chain of transitions
	 */
	public RonPSTNode addUpstreamNode(byte[] suffix) throws SequenceSpectrumException
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
				return addUpstreamNode(suffix[suffix.length - 1])
						.addUpstreamNode(DSArrayUtils.prefix(suffix, suffix.length - 1));
				}
		throw new Error("Impossible");
		}

	/**
	 * Gets the upstream node associated with the given symbol starting from this node, creating it first if needed.
	 *
	 * @param sigma the transition to follow from this node
	 * @return the node at the other end of the transition
	 */
	public RonPSTNode addUpstreamNode(byte sigma) throws SequenceSpectrumException
		{
		leaf = false;
		int index = DSArrayUtils.indexOf(alphabet, sigma);
		RonPSTNode result = upstreamNodes[index];
		if (result == null)
			{
			result = new RonPSTNode(DSArrayUtils.prepend(sigma, id), alphabet);
			upstreamNodes[index] = result;
			}
		return result;
		}

	/**
	 * Appends a string representation of this node to the given formatter, indenting as needed based on the hierarchy
	 * level.
	 *
	 * @param formatter a Formatter, mostly as a convenience for rendering doubles with limited precision
	 * @param indent    a String to use to indent this node and its children.  This will get longer as the recursion
	 *                  proceeds towards the leaves of the tree.
	 */
	public void appendString(Formatter formatter, String indent)
		{
		formatter.format("[");
		for (int i = 0; i < alphabet.length; i++)
			{
			byte b = alphabet[i];
			try
				{
				formatter.format("%3.3g ", probs.get(b));
				}
			catch (DistributionException e)
				{
				//sb.append(indent + "ERROR ->" + b);
				formatter.format("%s ", "ERROR");
				}
			}
		formatter.format("]");

		for (int i = 0; i < alphabet.length; i++)
			{
			byte b = alphabet[i];

			formatter.format("\n%s %c ", indent, b);
			//append(indent + probs.get(b) + " -> " + (char)b.byteValue() + "\n");
			RonPSTNode child = upstreamNodes[i];
			if (child != null && child.getIdBytes().length > getIdBytes().length)
				{
				child.appendString(formatter, indent + "     | ");
				}
			}
		}

	/**
	 * Gets the id of this node, which is just the suffix associated with this node represented as a byte[].
	 */
	public byte[] getIdBytes()
		{
		return id;
		//	return new String(id);
		}

	/**
	 * Recursively fills out the tree so that each node has either a complete set of probabilities or none at all, and
	 * assigns conditional probabilities according to the given spectrum.  That is: if this node has any children, ensures
	 * that there is a probability for each symbol in the alphabet.  There may or may not be a child node for each symbol,
	 * though.
	 */
	public void copyProbsFrom(SequenceSpectrum spectrum)
		//throws SequenceSpectrumException//DistributionException,
		{
		for (byte sigma : alphabet)
			{
			// copy the probabilities regardless
			double prob = 0;
			try
				{
				prob = spectrum.conditionalProbability(sigma, id);
				}
			catch (SequenceSpectrumException e)
				{
				//logger.error(e);
				// no worries, just let prob=0 then
				}
			try
				{
				setProb(sigma, prob);
				}
			catch (DistributionException e)
				{
				throw new SequenceSpectrumRuntimeException(e);
				}

			// Nooo!  We already made sure that we have probability entries for each possible child.
			// But we only want a full-fledged node if the child itself has children.
			/*

								// if there are any children, make sure there are all children
							   if (children != null && children.size() != 0)
								   {
								   add(sigma).copyProbsFrom(spectrum);
								   }

								   */
			}
		try
			{
			probs.normalize();
			}
		catch (DistributionException e)
			{
			// there should be no node with zero probability weight!
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}

		// okay, but we still need to recurse to the children that do exist
		for (RonPSTNode upstream : upstreamNodes)//.values())
			{
			if (upstream != null)
				{
				upstream.copyProbsFrom(spectrum);
				}
			}
		}

	/**
	 * Sets the probability of the given symbol to the given value.  This may cause the probability distribution to become
	 * unnormalized.
	 *
	 * @param b    the symbol
	 * @param prob the probability
	 * @throws DistributionException if something bad happens, like a negative probability
	 */
	private void setProb(byte b, double prob) throws DistributionException
		{
		probs.put(b, prob);
		}

	/**
	 * Counts the immediately upstream nodes that exist.
	 *
	 * @returns an int between 0 and the size of the alphabet (inclusive) giving the number of symbols that lead to more
	 * specific nodes.
	 */
	public int countUpstreamNodes()
		{
		int result = 0;
		for (RonPSTNode n : upstreamNodes)
			{
			if (n != null)
				{
				result++;
				}
			}
		return result;
		}

	/**
	 * Gets a List<RonPSTNode> of all nodes in the subtree starting from this node, including this node.
	 *
	 * @return a List of all nodes in the subtree starting from this node, including this node.
	 */
	public List<RonPSTNode> getAllUpstreamNodes()
		{
		List<RonPSTNode> result = new ArrayList<RonPSTNode>();

		collectUpstreamNodes(result);
		return result;
		}

	/**
	 * Recursively append this node and all its children (upstream nodes) to the provided List<RonPSTNode>
	 *
	 * @param nodeList a List to which to add this node and its children
	 */
	private void collectUpstreamNodes(List<RonPSTNode> nodeList)
		{
		nodeList.add(this);
		for (RonPSTNode n : upstreamNodes)
			{
			if (n != null)
				{
				n.collectUpstreamNodes(nodeList);
				}
			}
		}

	/**
	 * Gets the id of this node, which is just the suffix associated with this node represented as a byte[], here converted
	 * to String for the sake of the Clusterable interface.
	 */
	public String getId()
		{
		return new String(id);
		}

	public int getMaxDepth()
		{
		int result = 1;
		for (RonPSTNode n : upstreamNodes)
			{
			if (n != null)
				{
				int i = n.getMaxDepth() + 1;
				if (i > result)
					{
					result = i;
					}
				}
			}

		return result;
		}

	public String getSourceId()
		{
		throw new NotImplementedException();
		}

	/**
	 * Get the upstream node for the specified symbol.  Returns null if the node does not exist.
	 *
	 * @param b the symbol specifying the upstream transition to follow
	 * @return the node found by following the requested transition, or null if the node does not exist.
	 * @throws SequenceSpectrumException if the requested symbol is not part of the alphabet
	 */
	public RonPSTNode getUpstreamNode(byte b) throws SequenceSpectrumException
		{
		try
			{
			return upstreamNodes[DSArrayUtils.indexOf(alphabet, b)];
			}
		catch (Exception e)
			{
			throw new SequenceSpectrumException(e);
			}
		}

	/**
	 * Normalize the underlying probability distribution
	 *
	 * @throws DistributionException
	 */
	public void normalize() throws DistributionException
		{
		probs.normalize();
		}

	/**
	 * Takes the logs of all the probabilities at this node and caches them; then follows the upstream transitions and
	 * repeats recursively.
	 */
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

	/**
	 * Takes the logs of all the probabilities at this node and caches them.
	 */
	public void updateLogProbs()
		{
		try
			{
			for (int i = 0; i < alphabet.length; i++)
				{
				logProbs[i] = MathUtils.approximateLog(probs.get(alphabet[i]));
				}
			}
		catch (DistributionException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		}
	}
