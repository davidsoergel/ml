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
import com.davidsoergel.runutils.OverridingPropertiesAggregator;
import com.davidsoergel.runutils.RunUnsuccessfulException;
import com.davidsoergel.runutils.ThreadLocalRun;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.DistributionProcessorException;
import com.davidsoergel.stats.Multinomial;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RonPSTTest
	{
	private static Logger logger = Logger.getLogger(RonPSTTest.class);

	@BeforeMethod
	public void setUp() throws Exception
		{
		ThreadLocalRun.removeInstance();
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
		};
		Map<String, Object> props = new HashMap<String, Object>();

		props.put("edu.berkeley.compbio.ml.strings.KneserNeyPSTSmoother.smoothFactor", "0.1");

		OverridingPropertiesAggregator opa = new OverridingPropertiesAggregator();
		opa.addSource(props);
		ThreadLocalRun.getInstance().setProps(opa);
		}

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
		RonPST pst = new RonPST(0.0001, 0, 1.1, 0.01, 4, ss);
		//pst.completeAndCopyProbsFrom(ss);
		return pst;
		}

	private boolean allNodesAreCompleteOrEmpty(MarkovTreeNode node, int maxWidth)
		{
		if (!nodeIsCompleteOrEmpty(node, maxWidth))
			{
			return false;
			}
		for (MarkovTreeNode child : node.getChildren().values())
			{
			if (!allNodesAreCompleteOrEmpty(child, maxWidth))
				{
				return false;
				}
			}
		return true;
		}

	private boolean nodeIsCompleteOrEmpty(MarkovTreeNode node, int maxWidth)
		{
		int width = node.getChildren().size();
		if (width != 0 && width != maxWidth)
			{
			return false;
			}
		return true;
		}


	@Test
	public void maxDepthIsCalculated() throws SequenceSpectrumException
		{
		RonPST pst = createSimplePST();
		assert pst.getMaxDepth() == 2;
		}


	@Test
	public void conditionalProbabilitiesAreGivenBasedOnLongestAvailableSuffix() throws SequenceSpectrumException
		{
		RonPST pst = createSimplePST();

		assert pst.conditionalProbability((byte) 'a', new byte[]{'d', 'a', 'b', 'a', 'b'}) == pst
				.conditionalProbability((byte) 'a', new byte[]{'a', 'b'});
		assert pst.conditionalProbability((byte) 'a', new byte[]{'a', 'b'}) != pst
				.conditionalProbability((byte) 'a', new byte[]{'b'});
		assert pst.conditionalProbability((byte) 'a', new byte[]{'a', 'b'}) != pst
				.conditionalProbability((byte) 'a', new byte[]{'a'});
		}

	@Test
	public void highRatioThresholdProducesShallowTree() throws SequenceSpectrumException
		{
		SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		RonPST pst = new RonPST(0.0001, 0, 500, 0.01, 4, ss);
		//	pst.completeAndCopyProbsFrom(ss);

		assert pst.getMaxDepth() == 1;
		}


	@Test
	public void lowRatioThresholdProducesDeepTree() throws SequenceSpectrumException
		{
		// todo improve by making a deeper tree to test

		SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		RonPST pst = new RonPST(0.0001, 0, 1, 0.01, 4, ss);
		//		pst.completeAndCopyProbsFrom(ss);

		assert pst.getMaxDepth() == 2;
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

	public static class StubSequenceSpectrum implements SequenceSpectrum
		{
		Map<Byte, Double> counts = new HashMap<Byte, Double>();
		Map<Byte, Map<Byte, Double>> counts2 = new HashMap<Byte, Map<Byte, Double>>();


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

		public SequenceSpectrum clone()
			{
			throw new NotImplementedException();
			}

		public boolean equalValue(Clusterable other)
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
			throw new SequenceSpectrumException("depth oops");
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
			return new byte[]{'a', 'b', 'c', 'd'};
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


		public void decrementBy(AdditiveClusterable object)
			{
			throw new NotImplementedException();
			}

		public void incrementBy(AdditiveClusterable object)
			{
			throw new NotImplementedException();
			}

		public AdditiveClusterable minus(AdditiveClusterable object)
			{
			throw new NotImplementedException();
			}

		public AdditiveClusterable plus(AdditiveClusterable object)
			{
			throw new NotImplementedException();
			}

		public List<byte[]> getFirstWords(int k)
			{
			throw new NotImplementedException();
			}

		public void setIgnoreEdges(boolean ignoreEdges)
			{
			// not relevant here...
			}
		}
	}
