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

import com.davidsoergel.dsutils.MathUtils;
import com.davidsoergel.dsutils.TestInstanceFactory;
import com.davidsoergel.stats.DistributionException;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class MarkovTreeNodeTest implements TestInstanceFactory
	{
	private byte[] alphabet = new byte[]{'a', 'b', 'c', 'd'};

	@Test
	public void emptyClonesHaveEqualValue() throws SequenceSpectrumException
		{
		MarkovTreeNode n = createComplexMarkovTree();
		assert n.clone().equalValue(n);
		}

	@Test
	public void populatedClonesHaveEqualValue() throws SequenceSpectrumException, DistributionException
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();

		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);

		assert n.clone().equalValue(n);
		}

	private MarkovTreeNode createComplexMarkovTree() throws SequenceSpectrumException
		{
		MarkovTreeNode node = new MarkovTreeNode(new byte[0], alphabet);
		node.add(new byte[]{'b', 'c', 'b'});
		node.add(new byte[]{'b', 'a', 'b'});
		node.add(new byte[]{'a', 'a', 'b'});
		return node;
		}

	private MarkovTreeNode createSimpleMarkovTree() throws SequenceSpectrumException
		{
		MarkovTreeNode node = new MarkovTreeNode(new byte[0], alphabet);
		node.add(new byte[]{'b', 'c'});
		node.add(new byte[]{'b', 'a'});
		node.add(new byte[]{'a'});
		return node;
		}

	@Test
	public void maxDepthWorks() throws SequenceSpectrumException
		{
		MarkovTreeNode n = createComplexMarkovTree();
		assert n.getChild((byte) 'd') == null;
		n.addChild((byte) 'd');
		assert n.getChild((byte) 'd') != null;
		assert n.getChild((byte) 'd').getMaxDepth() == 0;
		}

	@Test
	public void addOneChildWorks() throws SequenceSpectrumException
		{
		MarkovTreeNode n = createComplexMarkovTree();
		assert n.getChild((byte) 'd') == null;
		n.addChild((byte) 'd');
		assert n.getChild((byte) 'd') != null;
		assert n.getChild((byte) 'd').getMaxDepth() == 0;
		}

	@Test
	public void addChildSequenceWorks() throws SequenceSpectrumException
		{
		MarkovTreeNode n = createComplexMarkovTree();
		assert n.getChild((byte) 'd') == null;
		n.add(new byte[]{'d', 'a', 'a', 'b'});
		assert n.getChild((byte) 'd') != null;
		assert n.getChild((byte) 'd').getChild((byte) 'a') != null;
		assert n.getChild((byte) 'd').getChild((byte) 'a').getChild((byte) 'a') != null;
		assert n.getChild((byte) 'd').getChild((byte) 'a').getChild((byte) 'a').getChild((byte) 'b') != null;
		assert n.getChild((byte) 'd').getChild((byte) 'a').getChild((byte) 'a').getChild((byte) 'b').getMaxDepth() == 0;
		assert n.getChild((byte) 'd').getMaxDepth() == 3;
		}


	@Test
	public void completeAndCopyProbsFromWorks() throws SequenceSpectrumException, DistributionException
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();

		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);

		assert n.getChild((byte) 'd') == null;// node d has no children, so it has no reason to exist
		assert n.get(new byte[]{'d'}) == null;
		assert n.conditionalProbability((byte) 'd') == 0.4;// but there is still a transition probability
		assert n.get(new byte[]{'b'}) != null;
		assert n.conditionalProbability((byte) 'd', new byte[]{'b'}) == 0.36;
		assert n.conditionalProbability((byte) 'd', new byte[]{'a'})
				== 0.0;// this one was not specified, but shouldn't throw an exception-- that's the "complete" part
		}


	@Test
	public void variousProbabilitiesAreCorrectAndConsistent() throws SequenceSpectrumException, DistributionException
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();

		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);

		assert n.conditionalProbability((byte) 'a') == 0.1;
		assert n.conditionalProbability((byte) 'b') == 0.2;
		assert n.conditionalProbability((byte) 'c') == 0.3;
		assert n.conditionalProbability((byte) 'd') == 0.4;

		assert n.conditionalProbability((byte) 'a', new byte[]{'b'}) == 0.11;
		assert n.conditionalProbability((byte) 'b', new byte[]{'b'}) == 0.22;
		assert n.conditionalProbability((byte) 'c', new byte[]{'b'}) == 0.31;
		assert n.conditionalProbability((byte) 'd', new byte[]{'b'}) == 0.36;

		assert n.getChild((byte) 'b').conditionalProbability((byte) 'a') == 0.11;
		assert n.getChild((byte) 'b').conditionalProbability((byte) 'b') == 0.22;
		assert n.getChild((byte) 'b').conditionalProbability((byte) 'c') == 0.31;
		assert n.getChild((byte) 'b').conditionalProbability((byte) 'd') == 0.36;

		assert n.conditionalsFrom(new byte[]{'b'}).get((byte) 'a') == 0.11;
		assert n.conditionalsFrom(new byte[]{'b'}).get((byte) 'b') == 0.22;
		assert n.conditionalsFrom(new byte[]{'b'}).get((byte) 'c') == 0.31;
		assert n.conditionalsFrom(new byte[]{'b'}).get((byte) 'd') == 0.36;
		}

	@Test
	public void totalProbabilitiesAreCorrect() throws SequenceSpectrumException, DistributionException
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();
		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);

		assert MathUtils.equalWithinFPError(n.totalProbability(new byte[]{'b'}), 0.2);
		assert MathUtils.equalWithinFPError(n.totalProbability(new byte[]{'b', 'a'}), 0.022);
		assert MathUtils.equalWithinFPError(n.totalProbability(new byte[]{'b', 'a'}), 0.022);
		assert MathUtils.equalWithinFPError(n.totalProbability(new byte[]{'b', 'c'}), 0.062);
		}

	@Test(expectedExceptions = {SequenceSpectrumException.class})
	public void totalProbabilityThrowsExceptionOnOverlySpecificProbabilityRequest()
			throws SequenceSpectrumException, DistributionException
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();

		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);

		n.totalProbability(new byte[]{'b', 'a', 'd', 'd'});
		}

	@Test(expectedExceptions = {SequenceSpectrumException.class})
	public void conditionalProbabilityThrowsExceptionOnOverlySpecificProbabilityRequest()
			throws SequenceSpectrumException, DistributionException
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();

		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);

		n.conditionalProbability((byte) 'd', new byte[]{'b', 'a', 'd'});
		}

	public static SequenceSpectrum createMockSimpleSpectrum() throws SequenceSpectrumException
		{
		SequenceSpectrum ss = createMock(SequenceSpectrum.class);

		expect(ss.conditionalProbability(eq((byte) 'a'), aryEq(new byte[0]))).andReturn(.1);
		expect(ss.conditionalProbability(eq((byte) 'b'), aryEq(new byte[0]))).andReturn(.2);
		expect(ss.conditionalProbability(eq((byte) 'c'), aryEq(new byte[0]))).andReturn(.3);
		expect(ss.conditionalProbability(eq((byte) 'd'), aryEq(new byte[0]))).andReturn(.4);

		expect(ss.conditionalProbability(eq((byte) 'a'), aryEq(new byte[]{'a'}))).andReturn(.1);
		expect(ss.conditionalProbability(eq((byte) 'b'), aryEq(new byte[]{'a'}))).andReturn(.2);
		expect(ss.conditionalProbability(eq((byte) 'c'), aryEq(new byte[]{'a'}))).andReturn(.3);
		//expect(ss.conditionalProbability(eq((byte) 'd'), aryEq(new byte[]{'a'}))).andReturn(.4);
		expect(ss.conditionalProbability(eq((byte) 'd'), aryEq(new byte[]{'a'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();

		expect(ss.conditionalProbability(eq((byte) 'a'), aryEq(new byte[]{'b'}))).andReturn(.11);
		expect(ss.conditionalProbability(eq((byte) 'b'), aryEq(new byte[]{'b'}))).andReturn(.22);
		expect(ss.conditionalProbability(eq((byte) 'c'), aryEq(new byte[]{'b'}))).andReturn(.31);
		expect(ss.conditionalProbability(eq((byte) 'd'), aryEq(new byte[]{'b'}))).andReturn(.36);

		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'a', 'a'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'a', 'b'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'a', 'c'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'a', 'd'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();

		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'b', 'a'})))
				.andReturn(.25).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'b', 'b'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'b', 'c'})))
				.andReturn(.25).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'b', 'd'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();

		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'c', 'a'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'c', 'b'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'c', 'c'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'c', 'd'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();

		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'d', 'a'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'d', 'b'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'d', 'c'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();
		expect(ss.conditionalProbability(anyByte(), aryEq(new byte[]{'d', 'd'})))
				.andThrow(new SequenceSpectrumException("Unknown probability")).anyTimes();


		/*
		  expect(ss.conditionalProbability(eq((byte) 'a'), aryEq(new byte[]{
						  'b',
						  'a'
				  }))).andReturn(.1);
				  expect(ss.conditionalProbability(eq((byte) 'b'), aryEq(new byte[]{
						  'b',
						  'a'
				  }))).andReturn(.2);
				  expect(ss.conditionalProbability(eq((byte) 'c'), aryEq(new byte[]{
						  'b',
						  'a'
				  }))).andReturn(.3);
				  expect(ss.conditionalProbability(eq((byte) 'd'), aryEq(new byte[]{
						  'b',
						  'a'
				  }))).andReturn(.4);


				  expect(ss.conditionalProbability(eq((byte) 'a'), aryEq(new byte[]{
						  'b',
						  'c'
				  }))).andReturn(.1);
				  expect(ss.conditionalProbability(eq((byte) 'b'), aryEq(new byte[]{
						  'b',
						  'c'
				  }))).andReturn(.2);
				  expect(ss.conditionalProbability(eq((byte) 'c'), aryEq(new byte[]{
						  'b',
						  'c'
				  }))).andReturn(.3);
				  expect(ss.conditionalProbability(eq((byte) 'd'), aryEq(new byte[]{
						  'b',
						  'c'
				  }))).andReturn(.4);
		  */

		replay(ss);
		return ss;
		}

	public MarkovTreeNode createInstance() throws Exception
		{
		SequenceSpectrum ss = createMockSimpleSpectrum();
		MarkovTreeNode n = createSimpleMarkovTree();
		n.completeAndCopyProbsFrom(ss);
		return n;
		}

	@Factory
	public Object[] testInterfaces()
		{
		TestInstanceFactory t = this;
		Object[] result = new Object[1];
		result[0] = new SequenceSpectrumAbstractTest(t)
		{
		};
		return result;
		}
	}
