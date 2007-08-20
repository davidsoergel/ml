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

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceFragmentMetadata;
import edu.berkeley.compbio.sequtils.SequenceReader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages information about a sequence fragment, including its metadata and any statistics that have been calculated
 * from the sequence, in the form of SequenceSpectra.  It is assumed that any SequenceSpectrum objects stored here are
 * all derived from a single "base spectrum" (which could in the worst case be a SequenceSpectrum implementation that
 * simply stores the entire input sequence).
 */
public class SequenceFragment extends SequenceFragmentMetadata implements AdditiveClusterable<SequenceFragment>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(SequenceFragment.class);

	protected Map<Class, SequenceSpectrum> theSpectra = new HashMap<Class, SequenceSpectrum>();

	private SequenceSpectrum baseSpectrum;
	private FirstWordProvider firstWordProvider;

	//private List<byte[]> firstWords;//prefix;
	//private final int FIRSTWORD_LENGTH = 10;
	//private int prefixValid = 0;

	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new SequenceFragment by specifying its start position with respect to a containing parent sequence, but
	 * with an unknown length.  (The length will presumably be determined and set later, as needed.)
	 *
	 * @param parent        the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName  a String identifier for this sequence
	 * @param startPosition the index in the parent sequence of the first symbol in this sequence
	 */
	public SequenceFragment(SequenceFragmentMetadata parent, String sequenceName, int startPosition)
		{
		super(parent, sequenceName, startPosition);
		}

	/**
	 * Constructs a new SequenceFragment by specifying its coordinates with respect to a containing parent sequence.
	 *
	 * @param parent        the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName  a String identifier for this sequence
	 * @param startPosition the index in the parent sequence of the first symbol in this sequence
	 * @param length        the length of this sequence
	 */
	public SequenceFragment(SequenceFragmentMetadata parent, String sequenceName, int startPosition, int length)
		{
		super(parent, sequenceName, startPosition, length);
		}

	/**
	 * Convenience constructor, creates a new SequenceFragment by applying a KcountScanner to an input stream, and using
	 * the resulting Kcount as the base spectrum.
	 *
	 * @param parent        the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName  a String identifier for this sequence
	 * @param startPosition the index in the parent sequence of the first symbol in this sequence
	 * @param in            the SequenceReader providing the input sequence
	 * @param desiredlength the number of symbols to attempt to read from the input stream
	 * @param scanner       the KcountScanner to use
	 * @throws IOException                when an input/output error occurs on the reader
	 * @throws FilterException            when the scanner is filtering the sequence while reading it, but the filter
	 *                                    throws an exception
	 * @throws NotEnoughSequenceException when the reader cannot supply the desired amound of sequence (some scanners may
	 *                                    not throw this exception, but instead simply return a Kcount based on the short
	 *                                    sequence)
	 */
	public SequenceFragment(SequenceFragmentMetadata parent, String sequenceName, int startPosition, SequenceReader in,
	                        int desiredlength, KcountScanner scanner)
			throws NotEnoughSequenceException, IOException, FilterException
		{
		this(parent, sequenceName, startPosition, 0);// length = 0 because nothing is scanned so far

		//prefix = new byte[PREFIX_LENGTH];
		Kcount s = scanner.scanSequence(in, desiredlength);//, firstWords, FIRSTWORD_LENGTH);
		//prefixValid = Math.min(PREFIX_LENGTH, s.getNumberOfSamples() + s.getK() - 1);
		length = s.getLength();// how much sequence was actually read
		setBaseSpectrum(s);
		}

	/*byte[] getPrefix(int length) throws NotEnoughSequenceException
		{
		if (length > prefixValid)
			{
			throw new NotEnoughSequenceException(
					"Only " + prefixValid + " symbols available; " + length + " requested");
			}
		return ArrayUtils.prefix(prefix, length);
		}*/

	/**
	 * Sets the base spectrum from which all other spectra are derived
	 *
	 * @param spectrum the new base spectrum describing statistics of this sequence
	 */
	public void setBaseSpectrum(SequenceSpectrum spectrum)
		{
		theSpectra.clear();
		baseSpectrum = spectrum;
		if (!(spectrum instanceof FirstWordProvider))
			{
			throw new SequenceSpectrumRuntimeException("Base spectrum must implement FirstWordProvider");
			}
		firstWordProvider = (FirstWordProvider) spectrum;
		theSpectra.put(baseSpectrum.getClass(), baseSpectrum);
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Gets the base spectrum from which all other spectra are derived
	 *
	 * @return the base spectrum of this sequence
	 */
	public SequenceSpectrum getBaseSpectrum()
		{
		return baseSpectrum;
		}

	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Clone this object.  Should behave like {@link Object#clone()} except that it returns an appropriate type and so
	 * requires no cast.  Also, we insist that is method be implemented in inheriting classes, so it does not throw
	 * CloneNotSupportedException.
	 *
	 * @return a clone of this instance.
	 * @see Object#clone
	 * @see java.lang.Cloneable
	 */
	public SequenceFragment clone()
		{
		SequenceFragment result = new SequenceFragment(parentMetadata, sequenceName, startPosition, length);
		result.setBaseSpectrum(getBaseSpectrum().clone());
		return result;
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface AdditiveClusterable ---------------------

	/* these methods are here so that a SequenceFragment object can represent the centroid of a cluster.  Obviously its metadata will be meaningless, though. */

	public void decrementBy(SequenceFragment object)
		{
		try
			{
			length -= object.getLength();
			baseSpectrum.decrementBy(object.getSpectrum(baseSpectrum.getClass()));
			}
		catch (SequenceSpectrumException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}

	public void incrementBy(SequenceFragment object)
		{
		try
			{
			length += object.getLength();
			baseSpectrum.incrementBy(object.getSpectrum(baseSpectrum.getClass()));
			}
		catch (SequenceSpectrumException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}

	public SequenceFragment minus(SequenceFragment k2)
		{
		SequenceFragment result = clone();
		result.decrementBy(k2);
		return result;
		}

	public SequenceFragment plus(SequenceFragment k2)
		{
		SequenceFragment result = clone();
		result.incrementBy(k2);
		return result;
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
	public boolean equalValue(SequenceFragment other)
		{
		try
			{
			return baseSpectrum.spectrumEquals(other.getSpectrum(baseSpectrum.getClass()));
			}
		catch (SequenceSpectrumException e)
			{
			// the object being compared doesn't even have a base spectrum of the proper type, so it's definitely not equal
			return false;
			}
		}

	/**
	 * Returns a String identifying this object.  Ideally each sequence fragment being analyzed should have a unique
	 * identifier.
	 *
	 * @return a unique identifier for this object
	 */
	public String getId()
		{
		return getSequenceName();
		}

	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Indicates that the sequence statistics have changed in some way, such that any derived statistics must be
	 * recomputed. Since the provided SequenceSpectrum has changed, it must become the new base spectrum, since otherwise
	 * the existing base would be inconsistent.
	 *
	 * @param source the SequenceSpectrum representing the updated statistics
	 */
	public void fireUpdated(SequenceSpectrum source)
		{
		setBaseSpectrum(source);
		}

	/**
	 * Returns the number of samples on which the base spectrum is based.
	 *
	 * @return The number of samples
	 */
	public int getNumberOfSamples()
		{
		return getBaseSpectrum().getNumberOfSamples();//theKcount.getNumberOfSamples();
		}

	/**
	 * Provides a SequenceSpectrum of the requested type.  If the spectrum has already been computed, it is returned.  If
	 * not, a new SequenceSpectrum of the requested type is created with this SequenceFragment as the constructor argument.
	 * Thus, the specific SequenceSpectrum constructor can call this method again to request a SequenceSpectrum of a
	 * different type, from which the requested one is then derived.  This mechanism assumes that a spectrum of a given
	 * class always has the same value-- that is, that it doesn't make sense to store multiple spectra of the same class
	 * associated with the same SequenceFragment.  If in fact the spectra have someparameters associated with their
	 * computation, then this isn't really true, and can create confusion.
	 *
	 * @param c the Class of a SequenceSpectrum implementation that is requested
	 * @return the SequenceSpectrum of the requested type describing statistics of this sequence
	 * @throws SequenceSpectrumException when a spectrum of the requested type cannot be found or generated
	 */
	public SequenceSpectrum getSpectrum(Class<? extends SequenceSpectrum> c) throws SequenceSpectrumException
		{
		SequenceSpectrum s = theSpectra.get(c);
		if (s == null)
			{
			for (Class sc : theSpectra.keySet())
				{
				if (c.isAssignableFrom(sc))
					{
					return theSpectra.get(sc);
					}
				}

			try
				{
				s = c.getConstructor(SequenceFragment.class).newInstance(this);
				theSpectra.put(c, s);
				}
			catch (NoSuchMethodException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			catch (IllegalAccessException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			catch (InvocationTargetException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			catch (InstantiationException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			}
		return s;
		}

	/**
	 * Test whether this sequence has statistics matching the given spectrum.  A spectrum of this sequence is generated
	 * that is of the same type as the given spectrum such that they may be compared.  The statistical equivalence reported
	 * naturally depends on the particular statistics being compared (that is, on the specific SequenceSpectrum
	 * implementation).
	 *
	 * @param spectrum the SequenceSpectrum to compare
	 * @return True if the spectra are equivalent, false otherwise
	 * @see SequenceSpectrum#spectrumEquals(SequenceSpectrum)
	 */
	public boolean spectrumEquals(SequenceSpectrum spectrum)
		{
		try
			{
			return getSpectrum(spectrum.getClass()).spectrumEquals(spectrum);
			}
		catch (SequenceSpectrumException e)
			{
			// the object being compared doesn't even have a base spectrum of the proper type, so it's definitely not equal
			return false;
			}
		}

	/*
   public Iterable<? extends byte[]> getFirstWords(int k)
	   {
	   getBaseSpectrum().getK();
	   if (k > FIRSTWORD_LENGTH)
		   {
		   throw new SequenceSpectrumRuntimeException(
				   "First words of length " + k + "were not stored during scanning.");
		   }
	   if (k == FIRSTWORD_LENGTH)
		   {
		   return firstWords;
		   }

	   List<byte[]> result = new ArrayList<byte[]>();
	   for (byte[] longFirstWord : firstWords)
		   {
		   result.add(ArrayUtils.prefix(longFirstWord, k));
		   }
	   return result;
	   }*/

	public List<byte[]> getFirstWords(int k)
		{
		return getFirstWordProvider().getFirstWords(k);
		}


	public FirstWordProvider getFirstWordProvider()
		{
		return firstWordProvider;
		}

	public void addPseudocounts()
		{
		baseSpectrum.addPseudocounts();
		}
	}
