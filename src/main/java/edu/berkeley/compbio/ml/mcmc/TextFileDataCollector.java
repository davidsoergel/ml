/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


package edu.berkeley.compbio.ml.mcmc;

import com.davidsoergel.runutils.Property;
import com.davidsoergel.runutils.PropertyConsumer;
import com.davidsoergel.stats.DoubleTimecourse;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

/**
 * @version 1.0
 */
@PropertyConsumer
public class TextFileDataCollector implements DataCollector
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(TextFileDataCollector.class);
	// protected DataCollector chainTo;

	@Property(inherited = true)
	public String outputDirectoryName;

	@Property(defaultvalue = "trajectory")
	public String trajectoryFilename;

	@Property(defaultvalue = "ensemble")
	public String ensembleFilename;
	private final HashMap<String, DoubleTimecourse> timecourses = new HashMap<String, DoubleTimecourse>();

	private int lastStep;

	//@ComputedProperty("runId")
	//public String runId;

	private FileWriter trajectoryWriter;
	private FileWriter ensembleWriter;


// ------------------------ CANONICAL METHODS ------------------------

	@Override
	public String toString()
		{
		final StringBuffer sb = new StringBuffer();
		final Formatter formatter = new Formatter(sb, Locale.US);

		//sb.append(getStep()).append("\n");
		for (final DoubleTimecourse t : timecourses.values())
			{
			formatter.format("%1$20s = %2$10s, %3$10s\n", t.name(), t.last(), t.runningaverage());
			}

		return sb.toString();
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface DataCollector ---------------------

	public void close()
		{
		try
			{
			trajectoryWriter.flush();
			trajectoryWriter.close();
			ensembleWriter.flush();
			ensembleWriter.close();
			trajectoryWriter = null;
			ensembleWriter = null;
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		}

	public void init()
		{
		final File outputDirectory = new File(outputDirectoryName);
		logger.debug("Writing outputs to " + outputDirectoryName);
		logger.debug("Found directory: " + outputDirectory);

		try
			{
			final String trajectoryFilename = outputDirectory.getCanonicalPath() + File.separator + "trajectory";
			ensembleFilename = outputDirectory.getCanonicalPath() + File.separator + "sampleEnsemble";

			trajectoryWriter = new FileWriter(trajectoryFilename);
			ensembleWriter = new FileWriter(ensembleFilename);
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		}

	public DataCollector newSubCollector(final String name)
		{
		throw new NotImplementedException();
		}

	public void setTimecourseValue(final String name, final double val)
		{
		final DoubleTimecourse t = timecourses.get(name);
		t.set(val);

		//		if (chainTo != null)
		//			{
		//			chainTo.setTimecourse(name, val);
		//			}
		}

	public void writeSample(final String s)
		{
		try
			{
			ensembleWriter.write(s);
			ensembleWriter.flush();
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		}

// -------------------------- OTHER METHODS --------------------------

	/*
   public void setChainTo(DataCollector dc)
	   {
	   chainTo = dc;
	   }*/

	public int getStep()
		{
		return lastStep;
		}

	public void setStep(final int lastStep)
		{
		this.lastStep = lastStep;
		}

	/**
	 * Store the current trajectory values.  This is distinct from setTimecourse because we need to set all the timecourse
	 * values first and only then write them all out on one line.
	 */
	public void writeLatestTrajectoryValues()
		{
		final StringBuffer sb = new StringBuffer();

		sb.append(getStep()).append(", ");
		for (final DoubleTimecourse t : timecourses.values())
			{
			sb.append(t.last());
			sb.append(", ");
			sb.append(t.runningaverage());
			sb.append(", ");
			}

		sb.append("\n");

		try
			{
			trajectoryWriter.write(sb.toString());
			trajectoryWriter.flush();
			}
		catch (IOException e)
			{
			logger.error("Error", e);
			}
		}
	}
