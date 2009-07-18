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

import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DistributionProcessorException;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SubstitutionFilter;

import java.io.IOException;

/**
 * Provides an interface for classes that can scan sequences to count the occurrences of substrings
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface SequenceSpectrumScanner
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Returns an empty SequenceSpectrum of the appropriate type.  This spectrum is "empty" in the sense that it is not
	 * based on any sequence, but it may nonetheless represent some prior, e.g. uniform.
	 *
	 * @return an empty SequenceSpectrum of the appropriate type.
	 */
	SequenceSpectrum getEmpty();

	SubstitutionFilter getNucleotideFilter();
	/*	Kcount scanSequence(SequenceReader in, int desiredlength, List<byte[]> firstWords) //, int firstWordLength)
				throws IOException, FilterException, NotEnoughSequenceException;*/

	/**
	 * Check whether the reader associated with this scanner is able to provide the sequence specified by the provided
	 * SequenceFragment.  Essentially, this seeks the reader to the start of the fragment and attempts to read symbols up
	 * to the length of the fragment.  If the method returns without throwing a NotEnoughSequenceException, then the
	 * requested fragment is available.
	 *
	 * @param fragment the SequenceFragment
	 * @throws IOException                when an input/output error occurs on the reader
	 * @throws FilterException            when the scanner is filtering the sequence while reading it, but the filter
	 *                                    throws an exception
	 * @throws NotEnoughSequenceException when the reader cannot supply the desired amound of sequence
	 */
//	void checkSequenceAvailable(SequenceFragment fragment)//SequenceReader theReader, int desiredlength)
//			throws IOException, FilterException, NotEnoughSequenceException;

	/**
	 * Scans a sequence to count pattern frequencies.
	 *
	 * @param fragment the SequenceFragment providing the sequence to be scanned
	 * @return a SequenceSpectrum containing the counts of all patterns being scanned for
	 * @throws java.io.IOException when an input/output error occurs on the reader
	 * @throws edu.berkeley.compbio.sequtils.FilterException
	 *                             when the scanner is filtering the sequence while reading it, but the filter throws an
	 *                             exception
	 * @throws edu.berkeley.compbio.sequtils.NotEnoughSequenceException
	 *                             when the reader cannot supply the desired amound of sequence (some scanners may not
	 *                             throw this exception, but instead simply return a Kcount based on the short sequence)
	 */
	SequenceSpectrum scanSequence(SequenceFragment fragment)//SequenceReader sequenceReader, int desiredLength)//
			throws IOException, FilterException, NotEnoughSequenceException, DistributionProcessorException,
			       GenericFactoryException;

	/**
	 * Scans a sequence to count pattern frequencies, considering only words that follow the given prefix.  Primarily used
	 * for multi-pass scanning, to achieve a large effective word length within reasonable memory.
	 *
	 * @param fragment the SequenceFragment providing the sequence to be scanned
	 * @param prefix   the word that must precede each pattern in order for it to be counted
	 * @return a SequenceSpectrum based on words immediately following the prefix
	 * @throws IOException
	 * @throws edu.berkeley.compbio.sequtils.FilterException
	 *
	 * @throws NotEnoughSequenceException
	 */
	SequenceSpectrum scanSequence(SequenceFragment fragment, //SequenceReader resetReader, int desiredLength,
	                              byte[] prefix)
			throws IOException, FilterException, NotEnoughSequenceException, DistributionProcessorException,
			       GenericFactoryException;
	}
