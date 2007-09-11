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

import com.davidsoergel.stats.DistributionProcessorException;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceReader;

import java.io.IOException;

/**
 * Provides an interface for classes that can scan sequences to count the occurrences of substrings
 *
 * @author David Soergel
 * @version $Id
 */
public interface KcountScanner
	{
	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Scan a sequence to count pattern frequencies.
	 *
	 * @param sequenceReader the SequenceReader providing the sequence to be scanned
	 * @param desiredLength  the number of symbols to read before returning
	 * @return a Kcount containing the counts of all patterns being scanned for
	 * @throws IOException                when an input/output error occurs on the reader
	 * @throws FilterException            when the scanner is filtering the sequence while reading it, but the filter
	 *                                    throws an exception
	 * @throws NotEnoughSequenceException when the reader cannot supply the desired amound of sequence (some scanners may
	 *                                    not throw this exception, but instead simply return a Kcount based on the short
	 *                                    sequence)
	 */
	Kcount scanSequence(SequenceReader sequenceReader, int desiredLength)//, SequenceFragment fragment)
			throws IOException, FilterException, NotEnoughSequenceException, DistributionProcessorException;

	/*	Kcount scanSequence(SequenceReader in, int desiredlength, List<byte[]> firstWords) //, int firstWordLength)
				throws IOException, FilterException, NotEnoughSequenceException;*/
	}
