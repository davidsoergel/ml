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

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DistributionProcessorException;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.sequtils.FilterException;
import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceException;
import edu.berkeley.compbio.sequtils.SequenceFragmentMetadata;
import edu.berkeley.compbio.sequtils.SequenceReader;
import edu.berkeley.compbio.sequtils.TranslationException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Manages information about a sequence fragment, including its metadata and any statistics that have been calculated
 * from the sequence, in the form of SequenceSpectra.  It is assumed that any SequenceSpectrum objects stored here are
 * all derived from a single "base spectrum" (which could in the worst case be a SequenceSpectrum implementation that
 * simply stores the entire input sequence).
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class SequenceFragment extends SequenceFragmentMetadata implements AdditiveClusterable<SequenceFragment>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(SequenceFragment.class);

	protected final Map<Class, SequenceSpectrum> theDerivedSpectra = new WeakHashMap<Class, SequenceSpectrum>();

	// ** weak references cause problems for additive clusters that can't be rescanned
	//protected WeakReference<SequenceSpectrum> _baseSpectrum;

	private SequenceSpectrum baseSpectrum;
	protected final SequenceSpectrumScanner theScanner;

	private FirstWordProvider firstWordProvider;
	private final SequenceReader theReader;
	private final int desiredlength;
	private boolean ignoreEdges;


// --------------------------- CONSTRUCTORS ---------------------------

	//private List<byte[]> firstWords;//prefix;
	//private final int FIRSTWORD_LENGTH = 10;
	//private int prefixValid = 0;
	/**
	 * Constructs a new SequenceFragment by specifying its start position with respect to a containing parent sequence, but
	 * with an unknown length.  (The length will presumably be determined and set later, as needed.)
	 *
	 * @param parent        the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName  a String identifier for this sequence
	 * @param startPosition the index in the parent sequence of the first symbol in this sequence
	 */
	/*	public SequenceFragment(SequenceFragmentMetadata parent, String sequenceName, Integer taxid, int startPosition)
		 {
		 super(parent, sequenceName, taxid, startPosition);
		 }
 */
	/**
	 * Constructs a new SequenceFragment by specifying its coordinates with respect to a containing parent sequence.
	 *
	 * @param parent        the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName  a String identifier for this sequence
	 * @param startPosition the index in the parent sequence of the first symbol in this sequence
	 * @param length        the length of this sequence
	 */
	public SequenceFragment(final SequenceFragmentMetadata parent, final String sequenceName, final int startPosition,
	                        final int length, final SequenceSpectrumScanner scanner)
		{
		this(parent, sequenceName, startPosition, null, 0, scanner, length);

		/*
				super(parent, sequenceName, null, startPosition, length);
				theReader = null;
				theScanner = null;
				this.desiredlength = 0;*/
		}

	public SequenceFragment(final SequenceFragmentMetadata parent, final String sequenceName, final int startPosition,
	                        final SequenceReader in, final int desiredlength, final SequenceSpectrumScanner scanner)
		{
		this(parent, sequenceName, startPosition, in, desiredlength, scanner, UNKNOWN_LENGTH);
		}

	/**
	 * Convenience constructor, creates a new SequenceFragment by applying a SequenceSpectrumScanner to an input stream,
	 * and using the resulting SequenceSpectrum as the base spectrum.
	 *
	 * @param parent        the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName  a String identifier for this sequence
	 * @param startPosition the index in the parent sequence of the first symbol in this sequence
	 * @param in            the SequenceReader providing the input sequence
	 * @param desiredlength the number of symbols to attempt to read from the input stream
	 * @param scanner       the SequenceSpectrumScanner to use
	 * @throws IOException                when an input/output error occurs on the reader
	 * @throws FilterException            when the scanner is filtering the sequence while reading it, but the filter
	 *                                    throws an exception
	 * @throws NotEnoughSequenceException when the reader cannot supply the desired amound of sequence (some scanners may
	 *                                    not throw this exception, but instead simply return a Kcount based on the short
	 *                                    sequence)
	 */
	public SequenceFragment(final SequenceFragmentMetadata parent, final String sequenceName, final int startPosition,
	                        final SequenceReader in, final int desiredlength, final SequenceSpectrumScanner scanner,
	                        final int length)
		{
		super(parent, sequenceName, null, startPosition, length);

//		this(parent, sequenceName, startPosition, UNKNOWN_LENGTH);// length = 0 because nothing is scanned so far
		theReader = in;
		theScanner = scanner;
		this.desiredlength = desiredlength;

		// if the sequence name is null but there is a parent, we'll let getSequenceName() build an appropriate string on demand
		if (sequenceName == null && parent == null && theReader != null)
			{
			this.sequenceName = theReader.getName();
			}
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Gets the base spectrum from which all other spectra are derived
	 *
	 * @return the base spectrum of this sequence
	 */
	public final synchronized SequenceSpectrum getBaseSpectrum()
		{
		// ** weak references cause problems for additive clusters that can't be rescanned
		//	SequenceSpectrum baseSpectrum = _baseSpectrum == null ? null : _baseSpectrum.get();
		if (baseSpectrum == null)
			{
			try
				{
				rescan();
				}
			catch (NotEnoughSequenceException e)
				{
				throw new SequenceSpectrumRuntimeException(e);
				}
			//baseSpectrum = _baseSpectrum.get();
			baseSpectrum.setIgnoreEdges(ignoreEdges);
			}
		return baseSpectrum;
		}

	public synchronized FirstWordProvider getFirstWordProvider() throws SequenceSpectrumException
		{
		if (ignoreEdges)
			{
			throw new SequenceSpectrumException("We're ignoring edges");
			}
		getBaseSpectrum(); //scanIfNeeded();
		return firstWordProvider;
		}

	/**
	 * Returns the length of this sequence
	 *
	 * @return the length of this sequence
	 */
	public synchronized int getLength()
		{
		if (length == UNKNOWN_LENGTH)
			{
			try
				{
				rescan();
				}
			/*	catch (IOException e)
			   {
			   logger.error(e);
			   }*/
			catch (NotEnoughSequenceException e)
				{
				//logger.error(e);
				}
			}
		return length;
		}

	// ** should be weak
	byte[] translatedSequence = null;

	protected synchronized void rescanTranslatedSequence()
		{
		try
			{
			if (theReader != null)
				{
				translatedSequence = new byte[desiredlength];

				theReader.seek(parentMetadata, startPosition);
				for (int i = 0; i < desiredlength; i++)
					{
					translatedSequence[i] = (byte) theReader.readTranslated();
					}
				}
			}
		catch (NotEnoughSequenceException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (TranslationException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (FilterException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		}


	protected synchronized void rescan() throws NotEnoughSequenceException
		{
		/*	if (_baseSpectrum.get() != null)
			 {
			 return;
			 }
	 */
		try
			{
			final SequenceSpectrum s;
			if (theReader == null)
				{
				s = theScanner.getEmpty();
				}
			else
				{
				theReader.seek(parentMetadata, startPosition);
				//prefix = new byte[PREFIX_LENGTH];
				s = theScanner.scanSequence(this);//theReader, desiredlength);//, firstWords, FIRSTWORD_LENGTH);
				}
			//prefixValid = Math.min(PREFIX_LENGTH, s.getNumberOfSamples() + s.getK() - 1);
			setBaseSpectrum(s);

			// so far the weights were set per character, because we didn't necessarily know the length...??
			// getWeightedLabels().multiplyBy(length);
			}
		catch (GenericFactoryException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (FilterException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		/*catch (NotEnoughSequenceException e)
			{
			//logger.error(e);
			throw new SequenceSpectrumRuntimeException(e);
			}*/
		catch (DistributionProcessorException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
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
	public synchronized void setBaseSpectrum(@NotNull final SequenceSpectrum spectrum)
		{
		//	SequenceSpectrum baseSpectrum = _baseSpectrum == null ? null : _baseSpectrum.get();
		/*	if (baseSpectrum == spectrum && theSpectra.size() == 1)
		   {
		   // nothing has changed
		   return;
		   }*/
		theDerivedSpectra.clear();
		baseSpectrum = spectrum;
		length = baseSpectrum.getOriginalSequenceLength();// how much sequence was actually read
		if (!ignoreEdges)
			{
			if (!(spectrum instanceof FirstWordProvider))
				{
				//throw new SequenceSpectrumRuntimeException("Base spectrum must implement FirstWordProvider");
				}
			else
				{
				firstWordProvider = (FirstWordProvider) spectrum;
				}
			}
		//_baseSpectrum = new WeakReference<SequenceSpectrum>(baseSpectrum);
		//theSpectra.put(baseSpectrum.getClass(), baseSpectrum);
		}

	public synchronized boolean isIgnoreEdges()
		{
		return ignoreEdges;
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
	@Override
	public synchronized SequenceFragment clone()
		{
		final SequenceFragment result =
				new SequenceFragment(parentMetadata, sequenceName, startPosition, length, theScanner);
		result.setBaseSpectrum(getBaseSpectrum().clone());
		return result;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AdditiveClusterable ---------------------

	/* these methods are here so that a SequenceFragment object can represent the centroid of a cluster.  Obviously its metadata will be meaningless, though. */

	public synchronized void decrementBy(final SequenceFragment object)
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();
		try
			{
			length -= object.getLength();
			baseSpectrum.decrementBy(object.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()));
			}
		catch (SequenceSpectrumException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}

	public synchronized void decrementByWeighted(final SequenceFragment object, final double weight)
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();
		try
			{
			length -= object.getLength();
			baseSpectrum.decrementByWeighted(object.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()),
			                                 weight);
			}
		catch (SequenceSpectrumException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}

	public synchronized void incrementBy(final SequenceFragment object)
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();

		if (sequenceName == null)
			{
			this.sequenceName = object.getSequenceName();
			}

		try
			{
			length += object.getLength();
			baseSpectrum.incrementBy(object.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()));
			}
		catch (SequenceSpectrumException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}

	public synchronized void incrementByWeighted(final SequenceFragment object, final double weight)
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();
		if (sequenceName == null)
			{
			this.sequenceName += object.getSequenceName();
			}
		try
			{
			length += object.getLength();
			baseSpectrum.incrementByWeighted(object.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()),
			                                 weight);
			}
		catch (SequenceSpectrumException e)
			{
			logger.error("Error", e);
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}

	public synchronized SequenceFragment minus(final SequenceFragment k2)
		{
		final SequenceFragment result = clone();
		result.decrementBy(k2);
		return result;
		}

	public synchronized void multiplyBy(final double v)
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();
		baseSpectrum.multiplyBy(v);
		fireUpdated(baseSpectrum);// ensure that any derived spectra are cleared
		}

	public synchronized SequenceFragment plus(final SequenceFragment k2)
		{
		final SequenceFragment result = clone();
		result.incrementBy(k2);
		return result;
		}

	public synchronized SequenceFragment times(final double v)
		{
		final SequenceFragment result = clone();
		result.multiplyBy(v);
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
	public synchronized boolean equalValue(final SequenceFragment other)
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();
		try
			{
			return baseSpectrum.spectrumEquals(other.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()));
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

/*	@NotNull
	public String getSourceId()
		{
		return getRootSequenceName();
		}*/
// -------------------------- OTHER METHODS --------------------------

	/*	public void runCompletionProcessor() throws DistributionProcessorException
	   {
	   baseSpectrum.runCompletionProcessor();

	   }*/

	// the only way to do this is to scan the sequence.  It sucks but we have no choice

	public synchronized void checkAvailable() throws NotEnoughSequenceException
		{
		if (desiredlength == UNKNOWN_LENGTH)
			{
			return;
			}

		// if we're reading the file anyway, we may as well remember the spectrum while we're at it.
		rescan();

		/*		try
		   {
		   if (_baseSpectrum.get() == null)
			   {



			   //theReader.seek(parentMetadata, startPosition); // ** shouldn't be necessary
			   //prefix = new byte[PREFIX_LENGTH];
			   theScanner.checkSequenceAvailable(this);// throws NotEnoughSequenceException

			   if (theReader.getTotalSequence() < desiredlength)
				   {
				   throw new NotEnoughSequenceException(
						   "Not enough sequence: " + desiredlength + " requested, " + theReader.getTotalSequence()
								   + " available.");
				   }
			   }
		   }
	   //	catch (IOException e)
	   //   {
	   //   logger.error(e);
		//  throw new SequenceSpectrumRuntimeException(e);
		//  }
	   catch (FilterException e)
		   {
		   logger.error(e);
		   throw new SequenceSpectrumRuntimeException(e);
		   }*/
		}

	public boolean desiredLengthUnknown()
		{
		return desiredlength == UNKNOWN_LENGTH;
		}

	/**
	 * Indicates that the sequence statistics have changed in some way, such that any derived statistics must be
	 * recomputed. Since the provided SequenceSpectrum has changed, it must become the new base spectrum, since otherwise
	 * the existing base would be inconsistent.
	 *
	 * @param source the SequenceSpectrum representing the updated statistics
	 */
	public void fireUpdated(final SequenceSpectrum source)
		{
		setBaseSpectrum(source);
		}

	/*	public SequenceSpectrum scan(byte[] prefix)
	   {
	   if (prefix == null || prefix.length == 0)
		   {
		   scanIfNeeded();
		   return getBaseSpectrum();
		   }

	   // now we're guaranteed that length has been set

	   try
		   {
		   return theScanner.scanSequence(getResetReader(), length, prefix);
		   }
	   catch (IOException e)
		   {
		   logger.error(e);
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (FilterException e)
		   {
		   logger.error(e);
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (NotEnoughSequenceException e)
		   {
		   logger.error(e);
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (DistributionProcessorException e)
		   {
		   logger.error(e);
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (GenericFactoryException e)
		   {
		   logger.error(e);
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   }*/

	public int getDesiredLength()
		{
		return desiredlength;
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

	public synchronized List<byte[]> getFirstWords(final int k) throws SequenceSpectrumException
		{
		return getFirstWordProvider().getFirstWords(k);
		}

	@NotNull
	public SequenceReader getReaderForSynchronizing()
		{
		return theReader;
		}

	/**
	 * Returns the number of samples on which the base spectrum is based.
	 *
	 * @return The number of samples
	 */
	/*	public int getNumberOfSamples()
		 {
		 return getBaseSpectrum().getNumberOfSamples();//theKcount.getNumberOfSamples();
		 }
 */
	public synchronized SequenceReader getResetReader() throws NotEnoughSequenceException
		{
		try
			{
			theReader.seek(parentMetadata, startPosition);
			return theReader;
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (NullPointerException e)
			{
			logger.error("Error", e);
			throw new NotEnoughSequenceException("This sequence fragment is not based on a reader.");
			}
		}

	public SequenceSpectrumScanner getScanner()
		{
		return theScanner;
		}

	/**
	 * Provides a SequenceSpectrum of the requested type.  If the spectrum has already been computed, it is returned.  If
	 * not, a new SequenceSpectrum of the requested type is created using the given factory with this SequenceFragment as
	 * the constructor argument. Thus, the specific SequenceSpectrum constructor can call this method again to request a
	 * SequenceSpectrum of a different type, from which the requested one is then derived.  This mechanism assumes that a
	 * spectrum of a given class always has the same value-- that is, that it doesn't make sense to store multiple spectra
	 * of the same class associated with the same SequenceFragment.  If in fact the spectra have some parameters associated
	 * with their computation, then this isn't really true, and can create confusion.
	 *
	 * @param c the Class of a SequenceSpectrum implementation that is requested
	 * @return the SequenceSpectrum of the requested type describing statistics of this sequence
	 * @throws SequenceSpectrumException when a spectrum of the requested type cannot be found or generated
	 */
	public synchronized <X extends SequenceSpectrum> SequenceSpectrum getSpectrum(final Class<X> c,
	                                                                              final GenericFactory<X> factory)
			throws SequenceSpectrumException
		{
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();  // scan if needed
		SequenceSpectrum s = theDerivedSpectra.get(c);
		if (s == null)
			{
			if (c.isAssignableFrom(baseSpectrum.getClass()))
				{
				return baseSpectrum;
				}

			for (final Map.Entry<Class, SequenceSpectrum> classSequenceSpectrumEntry : theDerivedSpectra.entrySet())
				{
				if (c.isAssignableFrom(classSequenceSpectrumEntry.getKey()))
					{
					//logger.debug(c + " is assignable from " + sc + ".");
					return classSequenceSpectrumEntry.getValue();
					}
				}


			try
				{
				if (factory == null)
					{
					throw new SequenceSpectrumException("Need to create new spectrum, but no factory was provided");
					}
				s = factory.create(this);
				//s = c.getConstructor(SequenceFragment.class).newInstance(this);
				theDerivedSpectra.put(c, s);
				}
			catch (GenericFactoryException e)
				{
				logger.error("Error", e);
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			catch (NullPointerException e)
				{
				logger.error("Error", e);
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			/*	catch (NoSuchMethodException e)
			   {
			   logger.error(e);
			   throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
			   }
		   catch (IllegalAccessException e)
			   {
			   logger.error(e);
			   throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
			   }
		   catch (InvocationTargetException e)
			   {
			   logger.error(e);
			   throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
			   }
		   catch (InstantiationException e)
			   {
			   logger.error(e);
			   throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
			   }*/
			}
		return s;
		}


	/*
   public void releaseCachedResources()

	   {
	   theReader.releaseCachedResources();

	   for (SequenceSpectrum sequenceSpectrum : theSpectra.values())
		   {
		   sequenceSpectrum.releaseCachedResources();
		   }
	   }*/

	/**
	 * Careful: this does not propagate labels to the subsidiary fragments, because they may or may not apply.  These
	 * fragments should be labelled after being split, not before.
	 *
	 * @param remove
	 * @return
	 * @throws SequenceException
	 */
	public synchronized Set<SequenceFragment> removeOverlaps(final Collection<SequenceFragment> remove)
			throws SequenceException
		{
		final Set<SequenceFragment> result = new HashSet<SequenceFragment>();

		final List<SequenceFragment> conflicts = new ArrayList<SequenceFragment>();

		final SequenceFragmentMetadata root =
				getRootMetadata(); // do all arithmetic with respect to the root to avoid confusion

		for (final SequenceFragment sf : remove)
			{
			if (overlaps(sf))  //sf.getRootMetadata().equalValue(root)) //
				{
				conflicts.add(sf);
				}
			}
		Collections.sort(conflicts);

		int trav = getStartPositionFromRoot();
		for (final SequenceFragment conflict : conflicts)
			{
			final int trav2 = conflict.getStartPositionFromRoot();
			final int len = trav2 - trav;
			if (len > 0)
				{
				assert theReader != null;
				assert theScanner != null;
				final SequenceFragment sf = new SequenceFragment(root, null, trav, theReader, len, theScanner);
				result.add(sf);
				}
			trav = trav2 + conflict.length;
			}

		// add the last fragment after the last conflict

		final int len = getStartPositionFromRoot() + length - trav;
		if (len > 0)
			{
			assert theReader != null;
			assert theScanner != null;
			final SequenceFragment sf = new SequenceFragment(root, null, trav, theReader, len, theScanner);
			result.add(sf);
			}

		return result;
		}

	/*
	   public void addPseudocounts()
		   {
		   baseSpectrum.addPseudocounts();
		   }*/

	/*	public void runBeginTrainingProcessor() throws DistributionProcessorException
		 {
		 SequenceSpectrum baseSpectrum = getBaseSpectrum();
		 baseSpectrum.runBeginTrainingProcessor();
		 }
 */
	// REVIEW compute a base spectrum early to manage memory?

	/*	@Property(helpmessage = "A type of spectrum to compute early to manage memory",
			   defaultvalue = "null", isNullable = true)
	 //, isPlugin = true)
	 public GenericFactory<SequenceSpectrum> precomputeSpectrumTypeFactory;

	 public void runFinishTrainingProcessor() throws DistributionProcessorException
		 {
		 scanIfNeeded();
		 if (precomputeSpectrumTypeFactory != null)
			 {
			 try
				 {
				 getSpectrum(precomputeSpectrumTypeFactory.getCreatesClass(), precomputeSpectrumTypeFactory);
				 }
			 catch (SequenceSpectrumException e)
				 {
				 logger.error(e);
				 throw new DistributionProcessorException(e);
				 }
			 }
		 baseSpectrum.runFinishTrainingProcessor();

		 }
 */

	/*	public void runFinishTrainingProcessor() throws DistributionProcessorException
		 {
		 SequenceSpectrum baseSpectrum = getBaseSpectrum();
		 baseSpectrum.runFinishTrainingProcessor();
		 }
 */

	public synchronized void setIgnoreEdges(final boolean b)
		{
		ignoreEdges = b;
		final SequenceSpectrum baseSpectrum = getBaseSpectrum();
		baseSpectrum.setIgnoreEdges(b);
		if (ignoreEdges)
			{
			firstWordProvider = null;
			}
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
	public synchronized boolean spectrumEquals(final SequenceSpectrum spectrum)
		{
		try
			{
			return getSpectrum(spectrum.getClass(), spectrum.getFactory()).spectrumEquals(spectrum);
			}
		catch (SequenceSpectrumException e)
			{
			// the object being compared doesn't even have a base spectrum of the proper type, so it's definitely not equal
			return false;
			}
		}

	public void setTranslationAlphabet(final byte[] alphabet)
		{
		// perf
		if (theReader.setTranslationAlphabet(alphabet))
			{
			translatedSequence = null;
			}
		}

	public byte[] getTranslatedSequence(final byte[] alphabet)
		{
		if (translatedSequence == null)
			{
			rescanTranslatedSequence();
			}
		return translatedSequence;
		}
	}
