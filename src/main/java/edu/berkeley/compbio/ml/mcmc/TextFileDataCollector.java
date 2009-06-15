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
