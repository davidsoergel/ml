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

import com.davidsoergel.dsutils.MathUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/* $Id$ */

/**
 * @Author David Soergel
 * @Version 1.0
 */
public class RonPSTNodeTest
	{
	private static final Logger logger = Logger.getLogger(RonPSTNodeTest.class);
	RonPSTNode theNode;

	@BeforeMethod
	public void setUp()
		{
		//MathUtils.initApproximateLog(-12, 12, 3, 100000);
		try
			{
			buildSimplePST();
			}
		catch (SequenceSpectrumException e)
			{
			logger.debug(e);
			e.printStackTrace();
			}
		}

	@Test
	public void updateLogProbsWorks()
		{
		theNode.copyProbsFrom(new RonPSTTest.StubSequenceSpectrum());
		theNode.updateLogProbs();
		assert MathUtils.equalWithinFPError(theNode.getLogProbs()[0], -1.6094379124341003);
		}


	@Test
	public void updateLogProbsRecursiveWorks() throws SequenceSpectrumException
		{
		theNode.copyProbsFrom(new RonPSTTest.StubSequenceSpectrum());
		theNode.updateLogProbsRecursive();
		assert MathUtils.equalWithinFPError(theNode.getUpstreamNode((byte) 'a').getLogProbs()[0], -2.3025850929940455);
		}


	@Test
	public void addDirectlyUpstreamNodeWorks() throws SequenceSpectrumException
		{
		theNode = new RonPSTNode(new byte[0], new byte[]{
				'a',
				'b',
				'c',
				'd'
		});
		assert theNode.getUpstreamNode((byte) 'a') == null;
		theNode.addUpstreamNode((byte) 'a');
		assert theNode.getUpstreamNode((byte) 'a') != null;
		}


	@Test
	public void addUpstreamNodeChainWorks() throws SequenceSpectrumException
		{
		theNode = new RonPSTNode(new byte[0], new byte[]{
				'a',
				'b',
				'c',
				'd'
		});
		assert theNode.getUpstreamNode((byte) 'd') == null;
		theNode.addUpstreamNode(new byte[]{
				'a',
				'b',
				'c',
				'd'
		});
		assert theNode.getUpstreamNode((byte) 'd').getUpstreamNode((byte) 'c').getUpstreamNode((byte) 'b')
				.getUpstreamNode((byte) 'a') != null;
		}


	@Test
	public void getAllUpstreamNodesWorks()
		{
		assert theNode.getAllUpstreamNodes().size() == 11;
		}


	@Test
	public void countUpstreamNodesWorks()
		{
		assert theNode.countUpstreamNodes() == 3;
		}


	@Test
	public void copyProbsFromWorks()
		{
		try
			{
			theNode.copyProbsFrom(new RonPSTTest.StubSequenceSpectrum());
			for (RonPSTNode trav : theNode.getAllUpstreamNodes())
				{
				assert trav.getProbs().size() == 0 || (trav.getProbs().size() == 4 && trav.getProbs()
						.isAlreadyNormalized());
				}
			}
		catch (Exception e)
			{
			logger.debug(e);
			e.printStackTrace();
			assert false;
			}
		}

	private void buildSimplePST() throws SequenceSpectrumException
		{
		theNode = new RonPSTNode(new byte[0], new byte[]{
				'a',
				'b',
				'c',
				'd'
		});
		theNode.addUpstreamNode((byte) 'a');
		theNode.addUpstreamNode((byte) 'b');
		theNode.addUpstreamNode((byte) 'c');

		RonPSTNode trav = theNode.getUpstreamNode((byte) 'a');
		trav.addUpstreamNode((byte) 'b');
		trav.addUpstreamNode((byte) 'c');

		trav = theNode.getUpstreamNode((byte) 'b');
		trav.addUpstreamNode((byte) 'c');
		trav.addUpstreamNode((byte) 'd');

		trav = theNode.getUpstreamNode((byte) 'c');
		trav.addUpstreamNode((byte) 'b');
		trav.addUpstreamNode((byte) 'c');
		trav.addUpstreamNode((byte) 'd');
		}
	}
