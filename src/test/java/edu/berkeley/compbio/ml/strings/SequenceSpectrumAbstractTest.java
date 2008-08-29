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
import com.davidsoergel.dsutils.TestInstanceFactory;
import com.davidsoergel.dsutils.math.MathUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;


public abstract class SequenceSpectrumAbstractTest
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(SequenceSpectrumAbstractTest.class);
	//public abstract SequenceSpectrum createInstance() throws Exception;

	private TestInstanceFactory<SequenceSpectrum> tif;


	// --------------------------- CONSTRUCTORS ---------------------------

	public SequenceSpectrumAbstractTest(TestInstanceFactory<SequenceSpectrum> tif)
		{
		this.tif = tif;
		}

	// -------------------------- OTHER METHODS --------------------------

	@Test
	public void getRandomReturnsAlphabetSymbols() throws Exception
		{
		SequenceSpectrum ss = tif.createInstance();
		byte[] alphabet = ss.getAlphabet();
		for (int count = 0; count < 100; count++)
			{
			byte b = ss.sample(new byte[0]);
			assert DSArrayUtils.contains(alphabet, b);
			}
		}

	@Test
	public void variousProbabilitiesAreConsistent() throws Exception
		{
		SequenceSpectrum ss = tif.createInstance();
		int multipliedConditionals = 0;
		for (int count = 0; count < 100; count++)
			{
			byte b = ss.sample(new byte[0]);
			byte c = ss.sample(new byte[]{b});
			assert ss.conditionalProbability(b, new byte[0]) == ss.conditionalsFrom(new byte[0]).get(b);

			double d1, d2;

			try
				{
				d1 = ss.conditionalProbability(c, new byte[]{b});
				}
			catch (SequenceSpectrumException e)
				{
				d1 = -1;// just a marker
				}
			catch (SequenceSpectrumRuntimeException e)
				{
				d1 = -1;// just a marker
				}
			try
				{
				d2 = ss.conditionalsFrom(new byte[]{b}).get(c);
				}
			catch (SequenceSpectrumException e)
				{
				d2 = -1;// just a marker
				}
			catch (SequenceSpectrumRuntimeException e)
				{
				d2 = -1;// just a marker
				}
			assert d1 == d2;

			try
				{
				double total = ss.totalProbability(new byte[]{
						b,
						c
				});
				double cond1 = ss.conditionalProbability(b, new byte[0]);
				double cond2 = ss
						.conditionalProbability(c, new byte[]{b});
				double mult = cond1 * cond2;
				if (!MathUtils.equalWithinFPError(total, mult))
					{
					logger.error("" + cond1 + "  *  " + cond2);
					logger.error("Total prob = " + total + "; Multiplied conditionals = " + mult);
					}
				assert MathUtils.equalWithinFPError(total, mult);
				multipliedConditionals++;
				}
			catch (SequenceSpectrumException e)
				{
				// no point in doing this test if the given spectrum doesn't have the required resolution
				// but hey, that's why we do it 100 times.
				}
			}
		assert multipliedConditionals > 5;// still we want to be sure it worked at all
		}
	}
