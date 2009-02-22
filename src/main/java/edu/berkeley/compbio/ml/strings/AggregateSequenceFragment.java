package edu.berkeley.compbio.ml.strings;

import edu.berkeley.compbio.sequtils.NotEnoughSequenceException;
import edu.berkeley.compbio.sequtils.SequenceException;
import edu.berkeley.compbio.sequtils.SequenceFragmentMetadata;
import edu.berkeley.compbio.sequtils.SequenceReader;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AggregateSequenceFragment extends SequenceFragment
	{
	private static final Logger logger = Logger.getLogger(AggregateSequenceFragment.class);

	private Collection<SequenceFragment> theSFs = new HashSet<SequenceFragment>();

	//	private AggregateReader theAggregateReader;
	/**
	 * Constructs a new SequenceFragment by specifying its coordinates with respect to a containing parent sequence.
	 *
	 * @param parent       the SequenceFragmentMetadata representing a larger sequence in which this one is contained
	 * @param sequenceName a String identifier for this sequence
	 */
	public AggregateSequenceFragment(SequenceFragmentMetadata parent, String sequenceName,
	                                 Collection<SequenceFragment> sequenceFragments)
		{
		super(parent, sequenceName, 0, UNKNOWN_LENGTH);
		theSFs.addAll(sequenceFragments);
		theScanner = theSFs.iterator().next().getScanner();
		//	theAggregateReader = new AggregateReader();
		}

	public AggregateSequenceFragment(SequenceFragmentMetadata parent, String sequenceName, SequenceFragment initialSF)
		{
		super(parent, sequenceName, 0, UNKNOWN_LENGTH);
		theSFs.add(initialSF);
		theScanner = initialSF.getScanner();
		//	theAggregateReader = new AggregateReader();
		}

	public void add(SequenceFragment sf)
		{
		theSFs.add(sf);
		}

	protected void rescan()
		{
		SequenceSpectrum baseSpectrum = getBaseSpectrum();
		if (baseSpectrum != null && baseSpectrum.getOriginalSequenceLength() != UNKNOWN_LENGTH)
			{
			return;
			}
		baseSpectrum = theScanner.getEmpty();
		length = 0;

		for (SequenceFragment sf : theSFs)
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

	public SequenceReader getResetReader() throws NotEnoughSequenceException
		{
		throw new NotImplementedException("Can't get a reader on an aggregate");
		//	theAggregateReader.reset();
		//	return theAggregateReader;
		}

	/*
   public class AggregateReader implements SequenceReader
	   {

	   }*/


	public boolean overlaps(SequenceFragmentMetadata other) throws SequenceException
		{
		throw new NotImplementedException();
		}

	public Collection<SequenceFragment> getSequenceFragments()
		{
		return theSFs;
		}
	}
