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
import com.davidsoergel.stats.Multinomial;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;

/**
 * Represents a statistical model of a sequence of bytes.  The generic type tells what other kinds of SequenceSpecturm
 * classes this one is compatible with under addition; frequently this is just the implementation class itself, but it
 * may also be a superclass or interface that is compatible under addition via {@link AdditiveClusterable}.  The fact
 * that this class extends AdditiveClusterable expresses the requirement that there be a sensible (i.e., commutative and
 * associative) "addition" operation on these models, given by {@link #plus(edu.berkeley.compbio.ml.cluster.AdditiveClusterable)}.
 *
 * @author David Soergel
 * @version $Id
 */
public interface SequenceSpectrum<T extends SequenceSpectrum> extends AdditiveClusterable<T>, Cloneable
	{
	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Clone this object.  Should behave like {@link Object#clone()} except that it returns an appropriate type and so
	 * requires no cast.  Also, we insist that this method be implemented in inheriting classes, so it does not throw
	 * CloneNotSupportedException.
	 *
	 * @return a clone of this instance.
	 * @see Object#clone
	 * @see java.lang.Cloneable
	 */
	public T clone();


	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Computes the conditional probability of generating a symbol given a prefix under the model.
	 *
	 * @param sigma  a byte specifying the symbol whose probability is to be computed
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the conditional probability, a double value between 0 and 1, inclusive
	 * @throws SequenceSpectrumException when anything goes wrong
	 * @see #fragmentLogProbability(SequenceFragment)
	 */
	double conditionalProbability(byte sigma, byte[] prefix)
			throws SequenceSpectrumException;// throws SequenceSpectrumException;


	/**
	 * Computes the conditional probability distribution of symbols given a prefix under the model.
	 *
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the Multinomial conditional distribution of symbols following the given prefix
	 * @throws SequenceSpectrumException when anything goes wrong
	 */
	Multinomial<Byte> conditionalsFrom(byte[] prefix)
			throws SequenceSpectrumException;// throws SequenceSpectrumException;

	/**
	 * Computes the total log probability of generating the given sequence fragment under the model.  This differs from
	 * {@link #totalProbability(byte[])} in that the sequence fragment is not given explicitly but only as metadata.  Thus
	 * its probability may be computed from summary statistics that are already available in the given SequenceFragment
	 * rather than from the raw sequence.  Also, because these probabilities are typically very small, the result is
	 * returned in log space (indeed implementations will likely compute them in log space).
	 *
	 * @param sequenceFragment the SequenceFragment whose probability is to be computed
	 * @return the natural logarithm of the conditional probability (a double value between 0 and 1, inclusive)
	 */
	double fragmentLogProbability(SequenceFragment sequenceFragment) throws SequenceSpectrumException;

	/**
	 * Returns the alphabet of this SequenceSpectrum object.
	 *
	 * @return the alphabet (type byte[]) of this SequenceSpectrum object.
	 */
	byte[] getAlphabet();

	/**
	 * Returns the maximum length of substrings considered in computing this statistical model of the sequence.  Our
	 * implicit assumption is that the sequences being modeled have some correlation length, and thus that statistical
	 * models of them can be built from substrings up to that length.  Thus, this method tells the maximum correlation
	 * length provided by the model.  A manifestation of this is that conditional probabilities of symbols given a prefix
	 * will cease to change as the prefix is lengthened (to the left) past this length.
	 *
	 * @return the maximum correlation length considered in the model.
	 */
	public int getMaxDepth();

	/**
	 * Returns the number of samples on which this spectrum is based.
	 *
	 * @return The number of samples
	 */
	public int getNumberOfSamples();

	/**
	 * Chooses a random symbol according to the conditional probabilities of symbols following the given prefix.  Shortcut
	 * equivalent to conditionalsFrom(prefix).sample().byteValue()
	 *
	 * @param prefix a byte array providing the conditioning prefix
	 * @return the chosen symbol
	 */
	public byte sample(byte[] prefix) throws SequenceSpectrumException;

	/**
	 * Chooses a random string according to the conditional probabilities of symbols.
	 *
	 * @param length the length of the desired random string
	 * @return a byte[] of the desired length sampled from this distribution
	 */
	public byte[] sample(int length) throws SequenceSpectrumException;

	/**
	 * Test whether the given sequence statistics are equivalent to this one.  Differs from equals() in that
	 * implementations of this interface may contain additional state which make them not strictly equal; here we're only
	 * interested in whether they're equal as far as this interface is concerned.
	 * <p/>
	 * Naive implementations will simply test for exact equality; more sophisticated implementations ought to use a more
	 * rigorous idea of "statistically equivalent", though in that case we'll probabably need to provide more parameters,
	 * such as a p-value threshold to use.  Note that the spectra know the number of samples used to generate them, so at
	 * least that's covered.
	 *
	 * @param spectrum the SequenceSpectrum to compare
	 * @return True if the spectra are equivalent, false otherwise
	 */
	boolean spectrumEquals(SequenceSpectrum spectrum);

	/**
	 * Computes the probability of generating the given sequence under the model.
	 *
	 * @param s a byte array
	 * @return the probability, a double value between 0 and 1, inclusive
	 */
	double totalProbability(byte[] s) throws SequenceSpectrumException;// throws SequenceSpectrumException;

	//void addPseudocounts();


	// ** really the lifecycle of a SequenceSpectrum should be managed more carefully, i.e. as a Builder

	void runBeginTrainingProcessor() throws DistributionProcessorException;

	void runFinishTrainingProcessor() throws DistributionProcessorException;

	//void runCompletionProcessor() throws DistributionProcessorException;
	}
