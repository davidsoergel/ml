package edu.berkeley.compbio.ml.strings;

import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceException;
import edu.berkeley.compbio.sequtils.SequenceFragmentMetadata;
import edu.berkeley.compbio.sequtils.SequenceReader;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AggregateSequenceFragment extends SequenceFragment
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AggregateSequenceFragment.class);

	private final Collection<SequenceFragment> theSFs = new HashSet<SequenceFragment>();


// --------------------------- CONSTRUCTORS ---------------------------

	//	private AggregateReader theAggregateReader;

	/**
	 * Constructs a new SequenceFragment by specifying its coordinates with respect to a containing parent sequence.
	 *
	 * @param parent       the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName a String identifier for this sequence
	 */
	public AggregateSequenceFragment(final SequenceFragmentMetadata parent, final String sequenceName,
	                                 final Collection<SequenceFragment> sequenceFragments)
		{
		super(parent, sequenceName, 0, UNKNOWN_LENGTH, sequenceFragments.iterator().next().getScanner());
		theSFs.addAll(sequenceFragments);
		//	theScanner = theSFs.iterator().next().getScanner();
		//	theAggregateReader = new AggregateReader();
		}

	public AggregateSequenceFragment(final SequenceFragmentMetadata parent, final String sequenceName,
	                                 final SequenceFragment initialSF)
		{
		super(parent, sequenceName, 0, UNKNOWN_LENGTH, initialSF.getScanner());
		theSFs.add(initialSF);
		//theScanner = initialSF.getScanner();
		//	theAggregateReader = new AggregateReader();
		}

// -------------------------- OTHER METHODS --------------------------

	public void add(final SequenceFragment sf)
		{
		theSFs.add(sf);
		}

	@NotNull
	public SequenceReader getReaderForSynchronizing()
		{
		throw new NotImplementedException("Can't get a reader on an aggregate");
		}

	public synchronized SequenceReader getResetReader() throws NotEnoughSequenceException
		{
		throw new NotImplementedException("Can't get a reader on an aggregate");
		//	theAggregateReader.reset();
		//	return theAggregateReader;
		}

	public Collection<SequenceFragment> getSequenceFragments()
		{
		return theSFs;
		}

	/*
   public class AggregateReader implements SequenceReader
	   {

	   }*/


	public boolean overlaps(final SequenceFragmentMetadata other) throws SequenceException
		{
		throw new NotImplementedException();
		}

	protected synchronized void rescan()
		{
/*		SequenceSpectrum baseSpectrum = getBaseSpectrum();
		if (baseSpectrum != null && baseSpectrum.getOriginalSequenceLength() != UNKNOWN_LENGTH)
			{
			return;
			}*/
		final SequenceSpectrum baseSpectrum = theScanner.getEmpty();
		length = 0;

		// PERF sort the fragments?
		for (final SequenceFragment sf : theSFs)
			{
			try
				{
				length += sf.getLength();
				baseSpectrum.incrementBy(sf.getSpectrum(baseSpectrum.getClass(), baseSpectrum.getFactory()));
				}
			catch (SequenceSpectrumException e)
				{
				logger.error("Error", e);
				throw new Error(e);
				}
			}
		fireUpdated(baseSpectrum);
		}

/*	public void setScanner(@NotNull final SequenceSpectrumScanner scanner)
		{
		theScanner = scanner;
		}*/
	}
