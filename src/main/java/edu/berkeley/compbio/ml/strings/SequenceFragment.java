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
import edu.berkeley.compbio.sequtils.SequenceFragmentMetadata;
import edu.berkeley.compbio.sequtils.SequenceReader;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	protected Map<Class, SequenceSpectrum> theSpectra = new HashMap<Class, SequenceSpectrum>();

	protected SequenceSpectrum baseSpectrum;
	private FirstWordProvider firstWordProvider;
	private SequenceReader theReader;
	protected SequenceSpectrumScanner theScanner;
	private int desiredlength;
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
	public SequenceFragment(SequenceFragmentMetadata parent, String sequenceName, int startPosition, int length)
		{
		super(parent, sequenceName, null, startPosition, length);
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
	public SequenceFragment(SequenceFragmentMetadata parent, String sequenceName, int startPosition, SequenceReader in,
	                        int desiredlength, @NotNull SequenceSpectrumScanner scanner)
		{
		this(parent, sequenceName, startPosition, 0);// length = 0 because nothing is scanned so far
		theReader = in;
		theScanner = scanner;
		this.desiredlength = desiredlength;

		// if the sequence name is null but there is a parent, we'll let getSequenceName() build an appropriate string on demand
		if (sequenceName == null && parent == null && theReader != null)
			{
			this.sequenceName = theReader.getName();
			}
		}

	public SequenceSpectrumScanner getScanner()
		{
		return theScanner;
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Gets the base spectrum from which all other spectra are derived
	 *
	 * @return the base spectrum of this sequence
	 */
	public SequenceSpectrum getBaseSpectrum()
		{
		scanIfNeeded();
		return baseSpectrum;
		}

	public void scanIfNeeded()
		{
		if (baseSpectrum != null)
			{
			return;
			}
		try
			{
			SequenceSpectrum s;
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
			}
		catch (GenericFactoryException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (IOException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (FilterException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (NotEnoughSequenceException e)
			{
			//logger.debug(e);
			//e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (DistributionProcessorException e)
			{
			logger.debug(e);
			e.printStackTrace();
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
	public void setBaseSpectrum(SequenceSpectrum spectrum)
		{
		if (baseSpectrum == spectrum && theSpectra.size() == 1)
			{
			// nothing has changed
			return;
			}
		theSpectra.clear();
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
		theSpectra.put(baseSpectrum.getClass(), baseSpectrum);
		}

	public FirstWordProvider getFirstWordProvider() throws SequenceSpectrumException
		{
		if (ignoreEdges)
			{
			throw new SequenceSpectrumException("We're ignoring edges");
			}
		scanIfNeeded();
		return firstWordProvider;
		}

	public boolean isIgnoreEdges()
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
	public SequenceFragment clone()
		{
		scanIfNeeded();
		SequenceFragment result = new SequenceFragment(parentMetadata, sequenceName, startPosition, length);
		result.setBaseSpectrum(getBaseSpectrum().clone());
		return result;
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface AdditiveClusterable ---------------------

	/* these methods are here so that a SequenceFragment object can represent the centroid of a cluster.  Obviously its metadata will be meaningless, though. */

	public void decrementBy(SequenceFragment object)
		{
		scanIfNeeded();
		try
			{
			length -= object.getLength();
			baseSpectrum.decrementBy(object.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()));
			}
		catch (SequenceSpectrumException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}


	public void decrementByWeighted(SequenceFragment object, double weight)
		{
		scanIfNeeded();
		try
			{
			length -= object.getLength();
			baseSpectrum.decrementByWeighted(object.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()),
			                                 weight);
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
		scanIfNeeded();

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
			logger.debug(e);
			e.printStackTrace();
			throw new Error(e);
			}
		fireUpdated(baseSpectrum);
		}


	public void incrementByWeighted(SequenceFragment object, double weight)
		{
		scanIfNeeded();
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

	public void multiplyBy(double v)
		{
		baseSpectrum.multiplyBy(v);
		setBaseSpectrum(baseSpectrum);// ensure that any derived spectra are cleared
		}

	public SequenceFragment times(double v)
		{
		SequenceFragment result = clone();
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
	public boolean equalValue(SequenceFragment other)
		{
		scanIfNeeded();
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

	/*	public void runCompletionProcessor() throws DistributionProcessorException
	   {
	   baseSpectrum.runCompletionProcessor();

	   }*/

	// the only way to do this is to scan the sequence.  It sucks but we have no choice

	public void checkAvailable() throws NotEnoughSequenceException, IOException
		{
		if (desiredlength == UNKNOWN_LENGTH)
			{
			return;
			}

		try
			{
			if (baseSpectrum == null)
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
		/*	catch (IOException e)
		   {
		   logger.debug(e);
		   e.printStackTrace();
		   throw new SequenceSpectrumRuntimeException(e);
		   }*/
		catch (FilterException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
		}

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
		   logger.debug(e);
		   e.printStackTrace();
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (FilterException e)
		   {
		   logger.debug(e);
		   e.printStackTrace();
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (NotEnoughSequenceException e)
		   {
		   logger.debug(e);
		   e.printStackTrace();
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (DistributionProcessorException e)
		   {
		   logger.debug(e);
		   e.printStackTrace();
		   throw new SequenceSpectrumRuntimeException(e);
		   }
	   catch (GenericFactoryException e)
		   {
		   logger.debug(e);
		   e.printStackTrace();
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

	public List<byte[]> getFirstWords(int k) throws SequenceSpectrumException
		{
		return getFirstWordProvider().getFirstWords(k);
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
	public SequenceReader getResetReader() throws NotEnoughSequenceException
		{
		try
			{
			theReader.seek(parentMetadata, startPosition);
			return theReader;
			}
		catch (IOException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new SequenceSpectrumRuntimeException(e);
			}
		catch (NullPointerException e)
			{
			logger.debug(e);
			e.printStackTrace();
			throw new NotEnoughSequenceException("This sequence fragment is not based on a reader.");
			}
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
	public <X extends SequenceSpectrum> SequenceSpectrum getSpectrum(Class<X> c, GenericFactory<X> factory)
			throws SequenceSpectrumException
		{
		scanIfNeeded();
		SequenceSpectrum s = theSpectra.get(c);
		if (s == null)
			{
			for (Class sc : theSpectra.keySet())
				{
				if (c.isAssignableFrom(sc))
					{
					//logger.debug(c + " is assignable from " + sc + ".");
					return theSpectra.get(sc);
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
				theSpectra.put(c, s);
				}
			catch (GenericFactoryException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			catch (NullPointerException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new SequenceSpectrumException(e, "Requested spectrum unavailable");
				}
			/*	catch (NoSuchMethodException e)
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
			   }*/
			}
		return s;
		}

	/*
	   public void addPseudocounts()
		   {
		   baseSpectrum.addPseudocounts();
		   }*/

	public void runBeginTrainingProcessor() throws DistributionProcessorException
		{
		scanIfNeeded();
		baseSpectrum.runBeginTrainingProcessor();
		}

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
				 logger.debug(e);
				 e.printStackTrace();
				 throw new DistributionProcessorException(e);
				 }
			 }
		 baseSpectrum.runFinishTrainingProcessor();

		 }
 */

	public void runFinishTrainingProcessor() throws DistributionProcessorException
		{
		scanIfNeeded();
		baseSpectrum.runFinishTrainingProcessor();
		}

	public void setIgnoreEdges(boolean b)
		{
		ignoreEdges = b;
		scanIfNeeded();
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
	public boolean spectrumEquals(SequenceSpectrum spectrum)
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

	public boolean desiredLengthUnknown()
		{
		return desiredlength == UNKNOWN_LENGTH;
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
	}
