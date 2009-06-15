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
import com.davidsoergel.dsutils.ContractTest;
import com.davidsoergel.dsutils.ContractTestAware;
import com.davidsoergel.dsutils.DSArrayUtils;
import com.davidsoergel.dsutils.TestInstanceFactory;
import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class RonPSTTest extends ContractTestAware<RonPSTTest> implements TestInstanceFactory<SequenceSpectrum>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(RonPSTTest.class);


// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addContractTestsToQueue(final Queue<ContractTest> theContractTests)
		{
		theContractTests.add(new SequenceSpectrumInterfaceTest(this));
		}

	@Test
	public void afterCompleteEveryNodeHasTransitionsForEverySymbolOrNone()
			throws SequenceSpectrumException, DistributionException
		{
		final RonPST pst = createInstance();

		final byte[] alphabet = pst.getAlphabet();
		assert allNodesAreCompleteOrEmpty(pst, alphabet.length);
		}

	/**
	 * {@inheritDoc}
	 */
	public RonPST createInstance() throws SequenceSpectrumException
		{
		final SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		//pst.copyProbsFrom(ss);
		return new RonPST(0.0001, 0.01, 1.1, 4, ss);
		}

	private boolean allNodesAreCompleteOrEmpty(final RonPSTNode node, final int maxWidth)
		{
		if (!nodeIsCompleteOrEmpty(node, maxWidth))
			{
			return false;
			}
		for (final RonPSTNode child : node.getUpstreamNodes())//.values())
			{
			if (child != null && !allNodesAreCompleteOrEmpty(child, maxWidth))
				{
				return false;
				}
			}
		return true;
		}

	private boolean nodeIsCompleteOrEmpty(final RonPSTNode node, final int maxWidth)
		{
		final int width = node.countUpstreamNodes();//.size();
		if (width != 0 && width != maxWidth)
			{
			return false;
			}
		return true;
		}

	@Test
	public void conditionalProbabilitiesAreGivenBasedOnLongestAvailableSuffix() throws SequenceSpectrumException
		{
		final RonPST pst = createInstance();

		// really should use a 3-level pst for this

		assert pst.conditionalProbability((byte) 'a', new byte[]{'d', 'a', 'b', 'a', 'b'}) == pst
				.conditionalProbability((byte) 'a', new byte[]{'a', 'b'});
		assert pst.conditionalProbability((byte) 'a', new byte[]{'a', 'b'}) == pst
				.conditionalProbability((byte) 'a', new byte[]{'b'});
		assert pst.conditionalProbability((byte) 'a', new byte[]{'b'}) != pst
				.conditionalProbability((byte) 'a', new byte[]{});
		assert pst.conditionalProbability((byte) 'a', new byte[]{'a', 'b'}) != pst
				.conditionalProbability((byte) 'a', new byte[]{'a'});
		}

	@Test
	public void highRatioThresholdProducesShallowTree() throws SequenceSpectrumException
		{
		final SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		final RonPST pst = new RonPST(0.0001, 0.01, 500, 4, ss);
		//	pst.copyProbsFrom(ss);

		assert pst.getMaxDepth() == 1;
		}

	public static SequenceSpectrum createStubSimpleSequenceSpectrum()
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

	@Factory
	public Object[] instantiateAllContractTests()
		{
		return super.instantiateAllContractTestsWithName(RonPST.class.getCanonicalName());
		}

	@Test
	public void lowRatioThresholdProducesDeepTree() throws SequenceSpectrumException
		{
		// ** improve by making a deeper tree to test

		final SequenceSpectrum ss = createStubSimpleSequenceSpectrum();
		final RonPST pst = new RonPST(0.0001, 0.01, 1, 4, ss);
		//		pst.copyProbsFrom(ss);

		// note the stub spectrum uses a backoff 1-mer prior for the 3rd level
		assert pst.getMaxDepth() == 3;
		}

	@Test
	public void maxDepthIsCalculated() throws SequenceSpectrumException
		{
		final RonPST pst = createInstance();
		assert pst.getMaxDepth() == 2;
		}

	@BeforeMethod
	public void setUp() throws Exception
		{
		MathUtils.initApproximateLog(-12, 12, 3, 100000);
		/*	ResultsCollectingProgramRun.removeInstance();
		new ResultsCollectingProgramRun()
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
// ------------------------------ FIELDS ------------------------------

		final Map<Byte, Double> counts = new HashMap<Byte, Double>();
		final Map<Byte, Map<Byte, Double>> counts2 = new HashMap<Byte, Map<Byte, Double>>();

		private final WeightedSet<String> weightedLabels = new HashWeightedSet<String>();


// --------------------------- CONSTRUCTORS ---------------------------

		public StubSequenceSpectrum()
			{
			final Map<Byte, Double> aCounts = new HashMap<Byte, Double>();
			aCounts.put((byte) 'a', 1.);
			aCounts.put((byte) 'b', 2.);
			aCounts.put((byte) 'c', 3.);
			aCounts.put((byte) 'd', 4.);
			counts2.put((byte) 'a', aCounts);
			counts.put((byte) 'a', DSCollectionUtils.sum(aCounts.values()));

			final Map<Byte, Double> bCounts = new HashMap<Byte, Double>();
			bCounts.put((byte) 'a', 3.);
			bCounts.put((byte) 'b', 3.);
			bCounts.put((byte) 'c', 3.);
			bCounts.put((byte) 'd', 1.);
			counts2.put((byte) 'b', bCounts);
			counts.put((byte) 'b', DSCollectionUtils.sum(bCounts.values()));

			final Map<Byte, Double> cCounts = new HashMap<Byte, Double>();
			cCounts.put((byte) 'a', 2.);
			cCounts.put((byte) 'b', 4.);
			cCounts.put((byte) 'c', 6.);
			cCounts.put((byte) 'd', 8.);
			counts2.put((byte) 'c', cCounts);
			counts.put((byte) 'c', DSCollectionUtils.sum(cCounts.values()));

			final Map<Byte, Double> dCounts = new HashMap<Byte, Double>();
			dCounts.put((byte) 'a', 4.);
			dCounts.put((byte) 'b', 3.);
			dCounts.put((byte) 'c', 2.);
			dCounts.put((byte) 'd', 1.);
			counts2.put((byte) 'd', dCounts);
			counts.put((byte) 'd', DSCollectionUtils.sum(dCounts.values()));
			}

// --------------------- GETTER / SETTER METHODS ---------------------

		@NotNull
		public WeightedSet<String> getWeightedLabels()
			{
			return weightedLabels;
			}

// ------------------------ CANONICAL METHODS ------------------------

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StubSequenceSpectrum clone()
			{
			throw new NotImplementedException();
			}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AdditiveClusterable ---------------------


		/**
		 * updates this object by subtracting another one from it.
		 *
		 * @param object the object to subtract from this one
		 */
		public void decrementBy(final StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public void decrementByWeighted(final StubSequenceSpectrum object, final double weight)
			{
			throw new NotImplementedException();
			}

		/**
		 * updates this object by adding another one to it.
		 *
		 * @param object the object to add to this one
		 */
		public void incrementBy(final StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public void incrementByWeighted(final StubSequenceSpectrum object, final double weight)
			{
			throw new NotImplementedException();
			}

		/**
		 * Returns a new object representing the difference between this one and the given argument.
		 *
		 * @param object the object to be subtracted from this one
		 * @return the difference between this object and the argument
		 */
		public StubSequenceSpectrum minus(final StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public void multiplyBy(final double v)
			{
			throw new NotImplementedException();
			}

		/**
		 * Returns a new object representing the sum of this one and the given argument.
		 *
		 * @param object the object to be added to this one
		 * @return the sum of this object and the argument
		 */
		public StubSequenceSpectrum plus(final StubSequenceSpectrum object)
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public StubSequenceSpectrum times(final double v)
			{
			throw new NotImplementedException();
			}

// --------------------- Interface Clusterable ---------------------


		/**
		 * Test whether the given object is the same as this one.  Differs from equals() in that implementations of this
		 * interface may contain additional state which make them not strictly equal; here we're only interested in whether
		 * they're equal as far as this interface is concerned, i.e., for purposes of clustering.
		 *
		 * @param other The clusterable object to compare against
		 * @return True if they are equivalent, false otherwise
		 */
		public boolean equalValue(final StubSequenceSpectrum other)
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public String getId()
			{
			return "Test Spectrum";
			}

// --------------------- Interface SequenceSpectrum ---------------------


		/**
		 * {@inheritDoc}
		 */
		public double conditionalProbability(final byte sigma, final byte[] prefix) throws SequenceSpectrumException
			{
			if (prefix.length == 0)
				{
				return counts.get(sigma) / getSumOfCounts();//NumberOfSamples();  // should really be getSumOfCounts
				}
			if (prefix.length == 1)
				{
				return counts2.get(prefix[0]).get(sigma) / counts.get(prefix[0]);
				}
			//backoff to 1-mer composition
			return conditionalProbability(sigma, DSArrayUtils.suffix(prefix, 1));
			//throw new SequenceSpectrumException("depth oops");
			}

		/**
		 * {@inheritDoc}
		 */
		public Multinomial<Byte> conditionalsFrom(final byte[] prefix) throws SequenceSpectrumException
			{
			try
				{
				if (prefix.length == 0)
					{
					return new Multinomial<Byte>(DSArrayUtils.toObject(getAlphabet()), counts);
					}
				if (prefix.length == 1)
					{
					return new Multinomial<Byte>(DSArrayUtils.toObject(getAlphabet()), counts2.get(prefix[0]));
					}
				}
			catch (DistributionException e)
				{
				throw new SequenceSpectrumRuntimeException(e);
				}
			throw new SequenceSpectrumException("depth oops");
			}

		/**
		 * {@inheritDoc}
		 */
		public double fragmentLogProbability(final SequenceFragment sequenceFragment, final boolean perSample)
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public byte[] getAlphabet()
			{
			return new byte[]{'a', 'b', 'c', 'd'};
			}

		/**
		 * {@inheritDoc}
		 */
		public int getMaxDepth()
			{
			return 2;
			}

		/**
		 * {@inheritDoc}
		 */
		public int getOriginalSequenceLength()
			{
			return 50;
			//throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		/*		public int getNumberOfSamples()
			  {
			  throw new NotImplementedException();
			  //return 50;
			  }
  */
		/**
		 * {@inheritDoc}
		 */
		public byte sample(final byte[] prefix) throws SequenceSpectrumException
			{
			try
				{
				if (prefix.length == 0)
					{
					return new Multinomial<Byte>(DSArrayUtils.toObject(getAlphabet()), counts).sample();
					}
				if (prefix.length == 1)
					{
					return new Multinomial<Byte>(DSArrayUtils.toObject(getAlphabet()), counts2.get(prefix[0])).sample();
					}
				throw new SequenceSpectrumException("depth oops");
				}
			catch (DistributionException e)
				{
				logger.error("Error", e);
				throw new SequenceSpectrumException(e);
				}
			}

		/**
		 * {@inheritDoc}
		 */
		public byte[] sample(final int length) throws SequenceSpectrumException
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public void setIgnoreEdges(final boolean ignoreEdges)
			{
			// not relevant here...
			}

		/**
		 * {@inheritDoc}
		 */
		public void setImmutable()
			{
			// not relevant here
			}

		/**
		 * {@inheritDoc}
		 */
		public boolean spectrumEquals(final SequenceSpectrum spectrum)
			{
			return spectrum == this;
			}

		/**
		 * {@inheritDoc}
		 */
		public double totalProbability(final byte[] s) throws SequenceSpectrumException
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

// -------------------------- OTHER METHODS --------------------------

		public void addPseudocounts()
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public String getExclusiveLabel()
			{
			return "Stub";
			}

		/*
		public void runBeginTrainingProcessor() throws DistributionProcessorException
			{
			// do nothing
			}

		public void runFinishTrainingProcessor() throws DistributionProcessorException
			{
			// do nothing
			}
*/

		public List<byte[]> getFirstWords(final int k)
			{
			throw new NotImplementedException();
			}

		public String getSourceId()
			{
			throw new NotImplementedException();
			}

		/**
		 * {@inheritDoc}
		 */
		public int getSumOfCounts()
			{
			return 50;
			}
		}
	}
