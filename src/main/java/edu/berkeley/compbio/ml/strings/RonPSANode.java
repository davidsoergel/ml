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

import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.stats.DistributionException;

import java.util.Formatter;
import java.util.Queue;


/**
 * Extends a MarkovTreeNode with backlinks, for making make a Probabilistic Suffix Automaton (PSA).
 *
 * @Author David Soergel
 * @Version 1.0
 */
public class RonPSANode extends MarkovTreeNode
	{

	private RonPSANode backoffPrior;
	// includes both the real children and the backlinks together
	private RonPSANode[] nextNodes;

	public RonPSANode()
		{
		}

	/**
	 * Constructs a new MarkovTreeNode with the given identifier
	 *
	 * @param id the sequence of symbols leading to this node
	 */
	public RonPSANode(byte[] id, byte[] alphabet)
		{
		super(id, alphabet);
		}

	public void setAlphabet(byte[] alphabet)
		{
		super.setAlphabet(alphabet);
		nextNodes = new RonPSANode[alphabet.length];
		}

	public RonPSANode getBackoffPrior()
		{
		return backoffPrior;
		}


	public void setBacklinksUsingRoot(RonPSANode rootNode, Queue<RonPSANode> breadthFirstQueue)
		{
		for (MarkovTreeNode child : children)//.values())
			{
			if (child != null)
				{
				breadthFirstQueue.add((RonPSANode) child);//.setBacklinksUsingRoot(rootNode);
				}
			}

		//	if (children.isEmpty())
		//		{
		if (id.length > 0)
			{
			try
				{
				setBackoffPrior(rootNode.getLongestSuffix(DSArrayUtils.suffix(id, 1)));
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


	public RonPSANode[] nextNodes()
		{
		return nextNodes;
		}

	/**
	 * This method assumes that it has not been run before, and that all nodes at higher levels in the tree have already
	 * been proceesed.  So it must be run in breadth-first order exactly once.
	 *
	 * @param backoffPrior
	 */
	public void setBackoffPrior(RonPSANode backoffPrior)
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

	/**
	 * Gets the child node associated with the given sequence, if it exists
	 *
	 * @param sigma the symbol to follow from this node
	 * @return the node pointed to, or null if that leaf does not exist
	 */
	public RonPSANode nextNode(byte sigma) throws SequenceSpectrumException
		{
		return nextNodes[DSArrayUtils.indexOf(alphabet, sigma)];
		}

	public RonPSANode nextNodeByAlphabetIndex(int c)
		{
		return nextNodes[c];
		}


	private void addChild(byte b, RonPSANode child) throws SequenceSpectrumException
		{
		leaf = false;
		int childIndex = DSArrayUtils.indexOf(alphabet, b);
		children[childIndex] = child;
		nextNodes[childIndex] = child;
		//children.put(b, child);
		}

	/**
	 * Gets the child node associated with the given symbol, creating it first if needed.
	 *
	 * @param sigma the transition to follow from this node
	 * @return the node at the other end of the transition
	 */
	public RonPSANode addChild(byte sigma)//throws SequenceSpectrumException
		{
		leaf = false;
		int index = DSArrayUtils.indexOf(alphabet, sigma);
		RonPSANode result = (RonPSANode) children[index];
		if (result == null)
			{
			result = new RonPSANode(DSArrayUtils.append(id, sigma), alphabet);
			nextNodes[index] = result;
			children[index] = result;
			}
		return result;
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

		MarkovTreeNode nextChild = nextNodes[DSArrayUtils.indexOf(alphabet, seq[0])];

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
			return nextChild.get(DSArrayUtils.suffix(seq, 1));
			}
		throw new Error("Impossible");
		}


	/**
	 * gets the node associated with the longest available suffix of the given sequence.
	 *
	 * @param suffix the sequence to walk
	 * @return the MarkovTreeNode
	 */
	public RonPSANode getLongestSuffix(byte[] suffix) throws SequenceSpectrumException
		{
		// simply walk the sequence to the end
		RonPSANode currentNode = this;
		for (byte b : suffix)
			{
			currentNode = currentNode.nextNode(b);
			}
		return currentNode;
		}

	/*
	 public RonPSANode[] getChildren()
		 {
		 return (RonPSANode[]) children;
		 }
 */
	public RonPSANode getChild(byte sigma)//throws SequenceSpectrumException
		{
		return (RonPSANode) (super.getChild(sigma));
		}

	public RonPSANode getDescendant(byte[] descendantId)//throws SequenceSpectrumException
		{
		return (RonPSANode) super.getDescendant(descendantId);
		}
	}

