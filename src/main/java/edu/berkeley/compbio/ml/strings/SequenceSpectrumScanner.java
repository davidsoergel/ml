package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.dsutils.GenericFactoryException;
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
public interface SequenceSpectrumScanner
	{
	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Scan a sequence to count pattern frequencies.
	 *
	 * @param sequenceReader the SequenceReader providing the sequence to be scanned
	 * @param desiredLength  the number of symbols to read before returning
	 * @return a Kcount containing the counts of all patterns being scanned for
	 * @throws java.io.IOException when an input/output error occurs on the reader
	 * @throws edu.berkeley.compbio.sequtils.FilterException
	 *                             when the scanner is filtering the sequence while reading it, but the filter throws an
	 *                             exception
	 * @throws edu.berkeley.compbio.sequtils.NotEnoughSequenceException
	 *                             when the reader cannot supply the desired amound of sequence (some scanners may not
	 *                             throw this exception, but instead simply return a Kcount based on the short sequence)
	 */
	SequenceSpectrum scanSequence(SequenceReader sequenceReader, int desiredLength)//, SequenceFragment fragment)
			throws IOException, FilterException, NotEnoughSequenceException, DistributionProcessorException,
			GenericFactoryException;

	/*	Kcount scanSequence(SequenceReader in, int desiredlength, List<byte[]> firstWords) //, int firstWordLength)
				throws IOException, FilterException, NotEnoughSequenceException;*/

	void checkSequenceAvailable(SequenceReader theReader, int desiredlength)
			throws IOException, FilterException, NotEnoughSequenceException;

	SequenceSpectrum scanSequence(SequenceReader resetReader, int desiredLength, byte[] prefix) throws IOException,
			FilterException, NotEnoughSequenceException, DistributionProcessorException, GenericFactoryException;
	}
