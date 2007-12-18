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

import com.davidsoergel.dsutils.AbstractGenericFactoryAware;
import com.davidsoergel.dsutils.ArrayUtils;
import com.davidsoergel.dsutils.MathUtils;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.DistributionProcessorException;
import com.davidsoergel.stats.Multinomial;
import com.davidsoergel.stats.MutableDistribution;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceReader;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Queue;

/**
 * A node in a tree where each transition is labeled by a byte and has a local probability (that is, conditional on the
 * parent node).  If the tree is completely filled out with respect to its alphabet up to a depth k, then it represents
 * a k-order Markov model.  If it is incompletely filled out, it may represent a Probabilistic Suffix Tree.  There is
 * some confusion about whether PSTs should be drawn "to the left" or "to the right", but it turns out to be equivalent
 * if you think about it properly.  The only remaining difficulty is how to add strings recursively; in the case of
 * PSTs, the recursion should walk all suffixes, not prefixes.
 */
public class MarkovTreeNode extends AbstractGenericFactoryAware
		implements SequenceSpectrum<MarkovTreeNode>, MutableDistribution
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(MarkovTreeNode.class);
	private byte[] id;
	private byte[] alphabet;
	private double[] logprobs;

	// use the alphabet to get indexes into the child array, instead of the HashM'p, for efficiency (no autoboxing, etc)

	//	private Map<Byte, MarkovTreeNode> children = new HashMap<Byte, MarkovTreeNode>();

	// includes only the children, with nulls where the tree does not continue
	private MarkovTreeNode[] children;

	// includes both the real children and the backlinks together
	private MarkovTreeNode[] nextNodes;

	//private Map<Byte, MarkovTreeNode> backlinkChildren = new HashMap<Byte, MarkovTreeNode>();
	private Multinomial<Byte> probs = new Multinomial<Byte>();
	private MarkovTreeNode backoffPrior;

	private boolean leaf = true;


	// --------------------------- CONSTRUCTORS ---------------------------

	public MarkovTreeNode()
		{
		}

	/**
	 * Constructs a new MarkovTreeNode with the given identifier
	 *
	 * @param id the sequence of symbols leading to this node
	 */
	public MarkovTreeNode(byte[] id, byte[] alphabet)
		{
		this.id = id;
		setAlphabet(alphabet);
		}

	public void setAlphabet(byte[] alphabet)
		{
		this.alphabet = alphabet;
		logprobs = new double[alphabet.length];
		children = new MarkovTreeNode[alphabet.length];
		nextNodes = new MarkovTreeNode[alphabet.length];
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Returns the alphabet of this SequenceSpectrum object.
	 *
	 * @return the alphabet (type byte[]) of this SequenceSpectrum object.
	 */
	public byte[] getAlphabet()
		{
		return alphabet;
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

	public MarkovTreeNode getBackoffPrior()
		{
		return backoffPrior;
		}

	public MarkovTreeNode[] getChildren()
		{
		return children;
		}

	/**
	 * Returns a mapping of symbols to child nodes, describing the possible transitions from this node to its children.
	 * Does not provide any information about the probabilities associated with those transitions.
	 *
	 * @return a Map<Byte, MarkovTreeNode> describing the possible transitions from this node to its children.
	 */
	/*	public Map<Byte, MarkovTreeNode> getChildren()
	   {
	   return children;
	   }*/
	public Multinomial<Byte> getProbs()
		{
		return probs;
		}

	public boolean isLeaf()
		{
		return leaf;
		}

	public void setId(byte[] id)
		{
		this.id = id;
		}

	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Performs a deep copy of this node.  Does not populate nextNode, since it can't know the identities of the
	 * appropriate backlink nodes
	 *
	 * @return a copy of this node, including a deep copy of its children and their probabilities
	 */
	public MarkovTreeNode clone()
		{
		MarkovTreeNode result = new MarkovTreeNode(id, alphabet);
		result.probs = probs.clone();
		try
			{
			for (byte b : alphabet)//children.keySet())
				{
				MarkovTreeNode child = getChild(b);
				if (child != null)
					{
					result.addChild(b, child.clone());
					}
				}
			}
		catch (SequenceSpectrumException e)
			{
			throw new Error("Impossible");
			}
		/*		try
					   {
					   result.setProb(b, probs.get(b));
					   }
				   catch (DistributionException e)
					   {
					   // no problem, just leave it empty then
					   }*/

		/*	try
				   {
				   result.normalize();
				   }
			   catch (DistributionException e)
				   {
				   throw new SequenceSpectrumRuntimeException(e);
				   }*/
		return result;
		}

	/**
	 * Gets the child node associated with the given sequence, if it exists
	 *
	 * @param sigma the symbol to follow from this node
	 * @return the node pointed to, or null if that leaf does not exist
	 */
	public MarkovTreeNode getChild(byte sigma) throws SequenceSpectrumException
		{
		return children[alphabetIndexForSymbol(sigma)];
		}

	private void addChild(byte b, MarkovTreeNode child) throws SequenceSpectrumException
		{
		leaf = false;
		int childIndex = alphabetIndexForSymbol(b);
		children[childIndex] = child;
		nextNodes[childIndex] = child;
		//children.put(b, child);
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface AdditiveClusterable ---------------------

	public void decrementBy(MarkovTreeNode object)
		{
		throw new NotImplementedException();
		}

	public void incrementBy(MarkovTreeNode object)
		{
		throw new NotImplementedException();
		}

	public MarkovTreeNode minus(MarkovTreeNode object)
		{
		throw new NotImplementedException();
		}

	public MarkovTreeNode plus(MarkovTreeNode object)
		{
		throw new NotImplementedException();
		}

	public void multiplyBy(double v)
		{
		throw new NotImplementedException();
		}

	public MarkovTreeNode times(double v)
		{
		throw new NotImplementedException();
		}

	// --------------------- Interface Clusterable ---------------------

	/**
	 * Recursively tests the deep equality of two PSTNodes.  Requires that the tree structure and the transition
	 * probabilities are identical.  Ignores the id arrays, though those should match anyway.
	 *
	 * @param other the MarkovTreeNode to compare
	 * @return true if the trees have the same structure and transition probabilities, false otherwise
	 */
	public boolean equalValue(MarkovTreeNode other)
		{
		if (!id.equals(other.id))
			{
			return false;
			}
		if (!alphabet.equals(other.alphabet))
			{
			return false;
			}
		/*	if (children.size() != other.children.size())
		   {
		   return false;
		   }*/
		for (byte b : alphabet)//children.keySet())
			{
			double v = 0, v1 = 0;
			int unknown = 0;
			try
				{
				v = probs.get(b);
				}
			catch (DistributionException e)
				{
				unknown++;
				}
			try
				{
				v1 = other.probs.get(b);
				}
			catch (DistributionException e1)
				{
				unknown++;
				}
			if (unknown == 1)
				{
				return false;
				}
			if (unknown == 0 && !MathUtils.equalWithinFPError(v, v1))
				{
				return false;
				}
			MarkovTreeNode node = null;
			MarkovTreeNode node1 = null;
			try
				{
				node = getChild(b);
				node1 = other.getChild(b);
				}
			catch (SequenceSpectrumException e)
				{
				throw new Error("Impossible");
				}
			if (node != null && node1 == null)
				{
				return false;
				}
			if (node == null && node1 != null)
				{
				return false;
				}
			if (node != null && node1 != null && !node.equalValue(node1))
				{
				return false;
				}
			}
		return true;
		}

	/**
	 * Returns a String representing the sequence of symbols leading to this node in the tree. The id is internally a
	 * byte[]; we just make a String out of it for the sake of the interface.
	 *
	 * @return a unique identifier for this object
	 */
	public String getId()
		{
		return new String(id);
		//throw new NotImplementedException();
		}

	// --------------------- Interface SequenceSpectrum ---------------------


	/**
	 * Computes the conditional probability distribution of symbols given a prefix under the model.
	 *
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the Multinomial conditional distribution of symbols following the given prefix
	 * @throws edu.berkeley.compbio.ml.strings.SequenceSpectrumException
	 *          when anything goes wrong
	 */
	public Multinomial<Byte> conditionalsFrom(byte[] prefix) throws SequenceSpectrumException
		{
		MarkovTreeNode node = get(prefix);
		if (node == null || node.probs == null)
			{
			throw new SequenceSpectrumException("Unknown probabilities at " + prefix);
			}
		return node.probs;
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
		// the RonPST implementation uses backlinks and so is vastly more efficient.
		// We can't use backlinks here because they might point to nodes outside of this subtree

		SequenceReader in = sequenceFragment.getResetReader();
		int requiredPrefixLength = getMaxDepth();
		double logprob = 0;
		CircularFifoBuffer prefix = new CircularFifoBuffer(requiredPrefixLength);

		while (true)
			{
			try
				{
				byte c = in.read();

				try
					{
					//** this is terribly inefficient
					byte[] prefixAsBytes = ArrayUtils.toPrimitive((Byte[]) prefix.toArray(new Byte[]{}));

					// these log probabilities could be cached, e.g. logConditionalProbability(c, prefix)
					logprob += MathUtils.approximateLog(conditionalProbability(c, prefixAsBytes));
					prefix.add(c);
					}
				catch (SequenceSpectrumException e)
					{
					// probably just an invalid character
					logger.debug("Invalid character " + (char) c);
					// ignore this character as far as the probability is concerned
					prefix.clear();
					}
				}
			catch (NotEnoughSequenceException e)
				{
				break;
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
			}
		return logprob;
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
		// inefficient; could be cached

		int result = 0;
		if (probs.size() > 0)
			{
			result = 1;
			}
		for (MarkovTreeNode child : children)//.values())
			{
			if (child != null)
				{
				result = Math.max(result, child.getMaxDepth() + 1);
				}
			}
		return result;
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

	public int getLength()
		{
		throw new NotImplementedException();
		}

	/**
	 * Chooses a random symbol according to the conditional probabilities of symbols following the given prefix.  Shortcut
	 * equivalent to conditionalsFrom(prefix).sample().byteValue()
	 *
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the chosen symbol
	 */
	public byte sample(byte[] prefix) throws SequenceSpectrumRuntimeException
		{
		try
			{
			return conditionalsFrom(prefix).sample();
			//return probs.sample();
			}
		catch (SequenceSpectrumException e)
			{
			return sample(ArrayUtils.suffix(prefix, 1));
			/*
						logger.debug(e);
						e.printStackTrace();
						throw new SequenceSpectrumRuntimeException(e);*/
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
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
		/*
			 byte[] result = new byte[length];
			 for (int i = 0; i < length; i++)
				 {

				 }*/
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

	public void runBeginTrainingProcessor() throws DistributionProcessorException
		{
		// do nothing
		}

	public void runFinishTrainingProcessor() throws DistributionProcessorException
		{
		// do nothing
		}

	public void setIgnoreEdges(boolean ignoreEdges)
		{
		// not relevant here...
		}

	// note that the probabilities should exist even if there are no corresponding child nodes!
	// There is no point in having a node with no associated probabilities, except temporarily
	// during the learning process before the probabilities have been filled in.

	// avoid holding a parent link; we don't need it so far


	public void setImmutable()
		{
		// not relevant here
		}

	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Gets the child node associated with the given sequence, creating it (and nodes along the way) as needed
	 *
	 * @param prefix the sequence of symbols to follow from this node
	 * @return the node at the end of the chain of transitions
	 */
	public MarkovTreeNode add(byte[] prefix) throws SequenceSpectrumException
		{
		if (prefix.length == 0)
			{
			// this should probably never occur
			return this;
			}
		else if (prefix.length == 1)
			{
			return addChild(prefix[0]);
			}
		else if (prefix.length >= 1)
			{
			return addChild(prefix[0]).add(ArrayUtils.suffix(prefix, 1));
			}
		throw new Error("Impossible");
		}

	/**
	 * Gets the child node associated with the given symbol, creating it first if needed.
	 *
	 * @param sigma the transition to follow from this node
	 * @return the node at the other end of the transition
	 */
	public MarkovTreeNode addChild(byte sigma) throws SequenceSpectrumException
		{
		leaf = false;
		int index = alphabetIndexForSymbol(sigma);
		MarkovTreeNode result = children[index];
		if (result == null)
			{
			result = new MarkovTreeNode(ArrayUtils.append(id, sigma), alphabet);
			nextNodes[index] = result;
			children[index] = result;
			}
		return result;
		}

	public void addPseudocounts()
		{
		throw new NotImplementedException();
		}

	public void appendString(Formatter formatter, String indent)
		{
		for (int i = 0; i < alphabet.length; i++)
			{
			byte b = alphabet[i];
			try
				{
				formatter.format("%s %3.3g -> %c\n", indent, probs.get(b), b);
				//append(indent + probs.get(b) + " -> " + (char)b.byteValue() + "\n");
				MarkovTreeNode child = nextNodes[i];
				if (child != null && child.getId().length() > getId().length())
					{
					child.appendString(formatter, indent + "     | ");
					}
				}
			catch (DistributionException e)
				{
				//sb.append(indent + "ERROR ->" + b);
				formatter.format("%s %s -> %c\n", indent, "ERROR", b);
				}
			}
		}

	/**
	 * Recursively fills out the tree so that each node has a complete set of children, or none at all.  That is: if this
	 * node has any children at all, ensures that there is a child for each symbol in the alphabet.
	 */
	/*	public void complete()
		   {
		   if (children == null || children.size() == 0)
			   {
			   return;
			   }

		   // we only get here if there is at least one child.  In that case, the get() calls will create all the other children, as needed.
		   for (byte sigma : getAlphabet())
			   {
			   add(sigma).complete();
			   }
		   }*/

	/**
	 * Recursively fills out the tree so that each node has either a complete set of children or none at all, and assigns
	 * conditional probabilities according to the given spectrum.  That is: if this node has any children, ensures that
	 * there is a child for each symbol in the alphabet.
	 */
	public void completeAndCopyProbsFrom(SequenceSpectrum spectrum)
		//throws SequenceSpectrumException//DistributionException,
		{
		for (byte sigma : getAlphabet())
			{
			// copy the probabilities regardless
			double prob = 0;
			try
				{
				prob = spectrum.conditionalProbability(sigma, id);
				}
			catch (SequenceSpectrumException e)
				{
				//e.printStackTrace();
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
			// But we only want a full-fledged node if tha child itself has children.
			/*

								// if there are any children, make sure there are all children
							   if (children != null && children.size() != 0)
								   {
								   add(sigma).completeAndCopyProbsFrom(spectrum);
								   }

								   */
			}
		try
			{
			probs.normalize();
			}
		catch (DistributionException e)
			{
			throw new SequenceSpectrumRuntimeException(e);
			}

		// okay, but we still need to recurse to the children that do exist
		for (MarkovTreeNode child : children)//.values())
			{
			if (child != null)
				{
				child.completeAndCopyProbsFrom(spectrum);
				}
			}
		}

	private void setProb(byte b, double prob) throws DistributionException
		{
		probs.put(b, prob);
		for (int i = 0; i < alphabet.length; i++)
			{
			if (b == alphabet[i])
				{
				logprobs[i] = MathUtils.approximateLog(prob);
				}
			}
		}

	public double conditionalProbability(byte sigma) throws SequenceSpectrumException
		{
		try
			{
			return probs.get(sigma);
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			throw new SequenceSpectrumException(e);
			}
		}

	public double conditionalProbability(byte[] suffix, byte[] prefix) throws SequenceSpectrumException
		{
		if (suffix.length == 0)
			{
			return 1;
			}
		byte sigma = suffix[0];
		if (suffix.length == 1)
			{
			return conditionalProbability(sigma, prefix);
			}
		return conditionalProbability(sigma, prefix) * conditionalProbability(ArrayUtils.suffix(suffix, 1),
		                                                                      ArrayUtils.append(prefix, sigma));
		}

	public int countChildren()
		{
		int result = 0;
		for (MarkovTreeNode n : children)
			{
			if (n != null)
				{
				result++;
				}
			}
		return result;
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
	public double fragmentLogProbabilityFromIntKcount(SequenceFragment sequenceFragment)
			throws SequenceSpectrumException
		{
		IntKcount kc = (IntKcount) sequenceFragment.getSpectrum(IntKcount.class, null);
		int k = kc.getK();

		double result = 0;


		for (byte[] firstWord : sequenceFragment.getFirstWords(kc.getK()))
			{
			result += MathUtils.approximateLog(totalProbability(firstWord));
			}

		/*	try
		   {
		   byte[] prefix = sequenceFragment.getPrefix(kc.getK());
		   result += MathUtils.approximateLog(totalProbability(prefix));
		   }
	   catch (NotEnoughSequenceException e)
		   {
		   logger.debug(e);
		   throw new SequenceSpectrumException(e);
		   }*/


		int[] counts = kc.getArray();

		for (int id = 0; id < counts.length; id++)
			{
			if (counts[id] > 0)
				{
				byte[] prefix = kc.prefixForId(id);
				byte sigma = kc.lastSymbolForId(id);

				result += counts[id] * MathUtils.approximateLog(conditionalProbability(sigma, prefix));
				}
			}

		return result;
		}

	/**
	 * Computes the probability of generating the given sequence under the model.
	 *
	 * @param s a byte array
	 * @return the probability, a double value between 0 and 1, inclusive
	 */
	public double totalProbability(byte[] s) throws SequenceSpectrumException
		{
		if (s.length == 0)
			{
			return 1;
			}
		return conditionalProbability(s, new byte[0]);
		}

	public double conditionalProbability(byte sigma, byte[] prefix) throws SequenceSpectrumException
		{
		MarkovTreeNode node = get(prefix);
		if (node == null || node.probs == null)
			{
			throw new SequenceSpectrumException("Unknown probability: " + prefix + " -> " + sigma);
			}
		try
			{
			return node.probs.get(sigma);
			}
		catch (DistributionException e)
			{
			logger.debug(e);
			throw new SequenceSpectrumException(e);
			}
		}

	/**
	 * Gets the child node associated with the given sequence, if it exists
	 *
	 * @param seq the sequence of symbols to follow from this node
	 * @return the node at the end of the chain of transitions, or null if that leaf does not exist
	 */
	public MarkovTreeNode get(byte[] seq) throws SequenceSpectrumException
		{
		if (seq.length == 0)
			{
			// this should probably never occur
			return this;
			}

		MarkovTreeNode nextChild = nextNodes[alphabetIndexForSymbol(seq[0])];

		if (nextChild == null)
			{
			return null;
			}
		if (seq.length == 1)
			{
			return nextChild;
			}
		else if (seq.length >= 1)
			{
			return nextChild.get(ArrayUtils.suffix(seq, 1));
			}
		throw new Error("Impossible");
		}

	public List<byte[]> getFirstWords(int k)
		{
		throw new NotImplementedException();
		}

	public byte[] getIdBytes()
		{
		return id;
		}

	public int getSubtreeNodes()
		{
		int result = 1;// this
		for (MarkovTreeNode child : children)//.values())
			{
			if (child != null)
				{
				result += child.getSubtreeNodes();
				}
			}
		return result;
		}

	public double logConditionalProbability(byte sigma) throws SequenceSpectrumException
		{
		return logprobs[alphabetIndexForSymbol(sigma)];
		}

	private int alphabetIndexForSymbol(byte sigma) throws SequenceSpectrumException
		{
		for (int i = 0; i < alphabet.length; i++)
			{
			if (sigma == alphabet[i])
				{
				return i;
				}
			}
		throw new SequenceSpectrumException("Symbol " + (char) sigma + " not in alphabet");
		}

	public double logConditionalProbabilityByAlphabetIndex(int c)
		{
		return logprobs[c];
		}

	/**
	 * Gets the child node associated with the given sequence, if it exists
	 *
	 * @param sigma the symbol to follow from this node
	 * @return the node pointed to, or null if that leaf does not exist
	 */
	public MarkovTreeNode nextNode(byte sigma) throws SequenceSpectrumException
		{
		return nextNodes[alphabetIndexForSymbol(sigma)];
		}

	public MarkovTreeNode nextNodeByAlphabetIndex(int c)
		{
		return nextNodes[c];
		}

	/*
   public Set<MarkovTreeNode> getAllNodes()
	   {
	   Set<MarkovTreeNode> result = new HashSet<MarkovTreeNode>();
	   result.add(this);
	   for (MarkovTreeNode child : children.values())
		   {
		   child.addAllNodes(result);
		   }
	   return result;
	   }

   public void addAllNodes(Set<MarkovTreeNode> result)
	   {
	   result.add(this);
	   for (MarkovTreeNode child : children.values())
		   {
		   child.addAllNodes(result);
		   }
	   }*/

	public MarkovTreeNode[] nextNodes()
		{
		return nextNodes;
		}

	private void normalize() throws DistributionException
		{
		probs.normalize();
		}

	public void setBacklinksUsingRoot(MarkovTreeNode rootNode, Queue<MarkovTreeNode> breadthFirstList)
		{
		// must do this first, before we fill out the children hash
		for (MarkovTreeNode child : children)//.values())
			{
			if (child != null)
				{
				breadthFirstList.add(child);//.setBacklinksUsingRoot(rootNode);
				}
			}
		//	if (children.isEmpty())
		//		{
		if (id.length > 0)
			{
			try
				{
				setBackoffPrior(rootNode.getLongestSuffix(ArrayUtils.suffix(id, 1)));
				}
			catch (SequenceSpectrumException e)
				{
				throw new Error("Impossible");
				}
			}
		//		}
		//else
		//	{

		//	}
		}

	/**
	 * This method assumes that it has not been run before, and that all nodes at higher levels in the tree hve already
	 * been proceesed.  So it must be run in breadth-first order exactly once.
	 *
	 * @param backoffPrior
	 */
	public void setBackoffPrior(MarkovTreeNode backoffPrior)
		{
		this.backoffPrior = backoffPrior;
		/*	if (children.isEmpty())
		   {
		   leaf = true;
		   }*/
		for (int i = 0; i < alphabet.length; i++)
			{
			if (nextNodes[i] == null)
				{
				nextNodes[i] = getBackoffPrior().nextNodes[i];//get(sigma));
				}
			}
		}

	public MarkovTreeNode getLongestSuffix(byte[] suffix) throws SequenceSpectrumException
		{
		byte[] prefix = ArrayUtils.clone(suffix);
		ArrayUtils.reverse(prefix);
		return getLongestPrefix(prefix);
		}

	/**
	 * gets the node associated with the longest available prefix of the given sequence.
	 *
	 * @param prefix the sequence to walk
	 * @return the MarkovTreeNode
	 */
	public MarkovTreeNode getLongestPrefix(byte[] prefix) throws SequenceSpectrumException
		{
		MarkovTreeNode result = this;
		MarkovTreeNode next;

		// this could also have been recursive, but that would have involved making each suffix byte[] explicitly
		for (byte b : prefix)
			{
			next = result.getChild(b);
			if (next == null)
				{
				return result;
				}
			else
				{
				result = next;
				}
			}

		return result;
		}
	}
