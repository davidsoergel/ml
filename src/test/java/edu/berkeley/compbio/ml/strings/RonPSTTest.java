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
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RonPSTTest
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(RonPSTTest.class);


	// -------------------------- OTHER METHODS --------------------------

	@Test
	public void afterCompleteEveryNodeHasTransitionsForEverySymbolOrNone()
			throws SequenceSpectrumException, DistributionException
		{
		RonPST pst = createSimplePST();

		byte[] alphabet = pst.getAlphabet();
		assert allNodesAreCompleteOrEmpty(pst, alphabet.length);
		}

	private RonPST createSimplePST() throws SequenceSpectrumException
		{
		SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		RonPST pst = new RonPST(0.0001, 0.01, 1.1, 4, ss);
		//pst.copyProbsFrom(ss);
		return pst;
		}

	public static SequenceSpectrum createStubSimpleSequenceSpectrum() throws SequenceSpectrumException
		{
		return new StubSequenceSpectrum();
		/*SequenceSpectrum ss = createMock(SequenceSpectrum.class);

		expect(ss.getAlphabet()).andReturn(new byte[]{'a', 'b', 'c', 'd'}).anyTimes();

		expect(ss.totalProbability(aryEq(new byte[]{'a'}))).andReturn(.1);
		expect(ss.totalProbability(aryEq(new byte[]{'b'}))).andReturn(.2);
		expect(ss.totalProbability(aryEq(new byte[]{'c'}))).andReturn(.3);
		expect(ss.totalProbability(aryEq(new byte[]{'d'}))).andReturn(.4);


		expect(ss.totalProbability(aryEq(new byte[]{'a', 'a'}))).andReturn(.1);
		expect(ss.totalProbability(aryEq(new byte[]{'a', 'b'}))).andReturn(.2);
		expect(ss.totalProbability(aryEq(new byte[]{'a', 'c'}))).andReturn(.3);
		expect(ss.totalProbability(aryEq(new byte[]{'a', 'd'}))).andReturn(.4);

		expect(ss.totalProbability(aryEq(new byte[]{'b', 'a'}))).andReturn(.4);
		expect(ss.totalProbability(aryEq(new byte[]{'b', 'b'}))).andReturn(.3);
		expect(ss.totalProbability(aryEq(new byte[]{'b', 'c'}))).andReturn(.2);
		expect(ss.totalProbability(aryEq(new byte[]{'b', 'd'}))).andReturn(.1);

		expect(ss.totalProbability(aryEq(new byte[]{'c', 'a'}))).andReturn(.11);
		expect(ss.totalProbability(aryEq(new byte[]{'c', 'b'}))).andReturn(.21);
		expect(ss.totalProbability(aryEq(new byte[]{'c', 'c'}))).andReturn(.31);
		expect(ss.totalProbability(aryEq(new byte[]{'c', 'd'}))).andReturn(.37);

		expect(ss.totalProbability(aryEq(new byte[]{'d', 'a'}))).andReturn(.1);
		expect(ss.totalProbability(aryEq(new byte[]{'d', 'b'}))).andReturn(.2);
		expect(ss.totalProbability(aryEq(new byte[]{'d', 'c'}))).andReturn(.3);
		expect(ss.totalProbability(aryEq(new byte[]{'d', 'd'}))).andReturn(.4);

		replay(ss);
		return ss;*/
		}

	private boolean allNodesAreCompleteOrEmpty(RonPSTNode node, int maxWidth)
		{
		if (!nodeIsCompleteOrEmpty(node, maxWidth))
			{
			return false;
			}
		for (RonPSTNode child : node.getUpstreamNodes())//.values())
			{
			if (child != null && !allNodesAreCompleteOrEmpty(child, maxWidth))
				{
				return false;
				}
			}
		return true;
		}

	private boolean nodeIsCompleteOrEmpty(RonPSTNode node, int maxWidth)
		{
		int width = node.countUpstreamNodes();//.size();
		if (width != 0 && width != maxWidth)
			{
			return false;
			}
		return true;
		}

	@Test
	public void conditionalProbabilitiesAreGivenBasedOnLongestAvailableSuffix() throws SequenceSpectrumException
		{
		RonPST pst = createSimplePST();

		// really should use a 3-level pst for this

		assert pst.conditionalProbability((byte) 'a', new byte[]{
				'd',
				'a',
				'b',
				'a',
				'b'
		}) == pst
				.conditionalProbability((byte) 'a', new byte[]{
						'a',
						'b'
				});
		assert pst.conditionalProbability((byte) 'a', new byte[]{
				'a',
				'b'
		}) == pst
				.conditionalProbability((byte) 'a', new byte[]{'b'});
		assert pst.conditionalProbability((byte) 'a', new byte[]{'b'}) != pst
				.conditionalProbability((byte) 'a', new byte[]{});
		assert pst.conditionalProbability((byte) 'a', new byte[]{
				'a',
				'b'
		}) != pst
				.conditionalProbability((byte) 'a', new byte[]{'a'});
		}

	@Test
	public void highRatioThresholdProducesShallowTree() throws SequenceSpectrumException
		{
		SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		RonPST pst = new RonPST(0.0001, 0.01, 500, 4, ss);
		//	pst.copyProbsFrom(ss);

		assert pst.getMaxDepth() == 1;
		}

	@Test
	public void lowRatioThresholdProducesDeepTree() throws SequenceSpectrumException
		{
		// todo improve by making a deeper tree to test

		SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		RonPST pst = new RonPST(0.0001, 0.01, 1, 4, ss);
		//		pst.copyProbsFrom(ss);

		// note the stub spectrum uses a backoff 1-mer prior for the 3rd level
		assert pst.getMaxDepth() == 3;
		}

	@Test
	public void maxDepthIsCalculated() throws SequenceSpectrumException
		{
		RonPST pst = createSimplePST();
		assert pst.getMaxDepth() == 2;
		}

	@BeforeMethod
	public void setUp() throws Exception
		{
		MathUtils.initApproximateLog(-12, 12, 3, 100000);
		/*	ThreadLocalRun.removeInstance();
		new ThreadLocalRun()
		{
		public String getVersion()
			{
			return null;//To change body of implemented methods use File | Settings | File Templates.
			}

		public void run() throws RunUnsuccessfulException
			{
			//To change body of implemented methods use File | Settings | File Templates.
			}
		};*/
		//	Map<String, Object> props = new HashMap<String, Object>();

		////props.put("edu.berkeley.compbio.ml.strings.KneserNeyPSTSmoother.smoothFactor", "0.1");

		//	HierarchicalTypedPropertyNode n = PropertyConsumerClassParser.parseRootContextClass(StubSequenceFragmentIterator.class);
		//	MapToHierarchicalTypedPropertyNodeAdapter.mergeInto(n, props);
		//	stubSequenceFragmentIteratorFactory = new PropertyConsumerFactory<StubSequenceFragmentIterator>(n);
		}

	// -------------------------- INNER CLASSES --------------------------

	public static class StubSequenceSpectrum extends AbstractGenericFactoryAware
			implements SequenceSpectrum<StubSequenceSpectrum>
		{
		Map<Byte, Double> counts = new HashMap<Byte, Double>();
		Map<Byte, Map<Byte, Double>> counts2 = new HashMap<Byte, Map<Byte, Double>>();

		public void multiplyBy(double v)
			{
			throw new NotImplementedException();
			}

		/**
		 * updates this object by subtracting another one from it.
		 *
		 * @param object the object to subtract from this one
		 */
		public void decrementBy(StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * updates this object by adding another one to it.
		 *
		 * @param object the object to add to this one
		 */
		public void incrementBy(StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * Returns a new object representing the difference between this one and the given argument.
		 *
		 * @param object the object to be subtracted from this one
		 * @return the difference between this object and the argument
		 */
		public StubSequenceSpectrum minus(StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * Returns a new object representing the sum of this one and the given argument.
		 *
		 * @param object the object to be added to this one
		 * @return the sum of this object and the argument
		 */
		public StubSequenceSpectrum plus(StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		public StubSequenceSpectrum times(double v)
			{
			throw new NotImplementedException();
			}

		public int getOriginalSequenceLength()
			{
			throw new NotImplementedException();
			}

		public void addPseudocounts()
			{
			throw new NotImplementedException();
			}

		public StubSequenceSpectrum()
			{
			Map<Byte, Double> aCounts = new HashMap<Byte, Double>();
			aCounts.put((byte) 'a', 1.);
			aCounts.put((byte) 'b', 2.);
			aCounts.put((byte) 'c', 3.);
			aCounts.put((byte) 'd', 4.);
			counts2.put((byte) 'a', aCounts);
			counts.put((byte) 'a', ArrayUtils.sum(aCounts.values()));

			Map<Byte, Double> bCounts = new HashMap<Byte, Double>();
			bCounts.put((byte) 'a', 3.);
			bCounts.put((byte) 'b', 3.);
			bCounts.put((byte) 'c', 3.);
			bCounts.put((byte) 'd', 1.);
			counts2.put((byte) 'b', bCounts);
			counts.put((byte) 'b', ArrayUtils.sum(bCounts.values()));

			Map<Byte, Double> cCounts = new HashMap<Byte, Double>();
			cCounts.put((byte) 'a', 2.);
			cCounts.put((byte) 'b', 4.);
			cCounts.put((byte) 'c', 6.);
			cCounts.put((byte) 'd', 8.);
			counts2.put((byte) 'c', cCounts);
			counts.put((byte) 'c', ArrayUtils.sum(cCounts.values()));

			Map<Byte, Double> dCounts = new HashMap<Byte, Double>();
			dCounts.put((byte) 'a', 4.);
			dCounts.put((byte) 'b', 3.);
			dCounts.put((byte) 'c', 2.);
			dCounts.put((byte) 'd', 1.);
			counts2.put((byte) 'd', dCounts);
			counts.put((byte) 'd', ArrayUtils.sum(dCounts.values()));
			}

		public StubSequenceSpectrum clone()
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
		public boolean equalValue(StubSequenceSpectrum other)
			{
			throw new NotImplementedException();
			}


		public String getId()
			{
			return "Test Spectrum";
			}

		public double conditionalProbability(byte sigma, byte[] prefix) throws SequenceSpectrumException
			{
			if (prefix.length == 0)
				{
				return counts.get(sigma) / getNumberOfSamples();
				}
			if (prefix.length == 1)
				{
				return counts2.get(prefix[0]).get(sigma) / counts.get(prefix[0]);
				}
			//backoff to 1-mer composition
			return conditionalProbability(sigma, ArrayUtils.suffix(prefix, 1));
			//throw new SequenceSpectrumException("depth oops");
			}

		public Multinomial<Byte> conditionalsFrom(byte[] prefix) throws SequenceSpectrumException
			{
			try
				{
				if (prefix.length == 0)
					{
					return new Multinomial<Byte>(ArrayUtils.toObject(getAlphabet()), counts);
					}
				if (prefix.length == 1)
					{
					return new Multinomial<Byte>(ArrayUtils.toObject(getAlphabet()), counts2.get(prefix[0]));
					}
				}
			catch (DistributionException e)
				{
				throw new SequenceSpectrumRuntimeException(e);
				}
			throw new SequenceSpectrumException("depth oops");
			}

		public double fragmentLogProbability(SequenceFragment sequenceFragment)
			{
			throw new NotImplementedException();
			}

		public byte[] getAlphabet()
			{
			return new byte[]{
					'a',
					'b',
					'c',
					'd'
			};
			}

		public int getMaxDepth()
			{
			return 2;
			}

		public int getNumberOfSamples()
			{
			return 50;
			}

		public byte sample(byte[] prefix) throws SequenceSpectrumException
			{
			try
				{
				if (prefix.length == 0)
					{
					return new Multinomial<Byte>(ArrayUtils.toObject(getAlphabet()), counts).sample();
					}
				if (prefix.length == 1)
					{
					return new Multinomial<Byte>(ArrayUtils.toObject(getAlphabet()), counts2.get(prefix[0])).sample();
					}
				throw new SequenceSpectrumException("depth oops");
				}
			catch (DistributionException e)
				{
				logger.debug(e);
				throw new SequenceSpectrumException(e);
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
			}

		public boolean spectrumEquals(SequenceSpectrum spectrum)
			{
			return spectrum == this;
			}

		public double totalProbability(byte[] s) throws SequenceSpectrumException
			{
			if (s.length == 0)
				{
				return 1;
				}
			if (s.length == 1)
				{
				return conditionalProbability(s[0], new byte[0]);
				}
			if (s.length == 2)
				{
				return conditionalProbability(s[0], new byte[0]) * conditionalProbability(s[1], new byte[]{s[0]});
				}
			throw new SequenceSpectrumException("depth oops");
			}

		public void runBeginTrainingProcessor() throws DistributionProcessorException
			{
			// do nothing
			}

		public void runFinishTrainingProcessor() throws DistributionProcessorException
			{
			// do nothing
			}


		public List<byte[]> getFirstWords(int k)
			{
			throw new NotImplementedException();
			}

		public void setIgnoreEdges(boolean ignoreEdges)
			{
			// not relevant here...
			}

		public void setImmutable()
			{
			// not relevant here
			}
		}
	}
