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

/* $Id$ */

package edu.berkeley.compbio.ml.mcmc;

import com.davidsoergel.stats.DoubleTimecourse;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author lorax
 * @version 1.0
 */
public class DataCollector
	{
	// ------------------------------ FIELDS ------------------------------

	protected static Logger logger = Logger.getLogger(DataCollector.class);
	private DoubleTimecourse[] timecourses;
	private int lastStep;
	// protected DataCollector chainTo;


	private FileWriter trajectoryWriter;
	private FileWriter ensembleWriter;


	// --------------------------- CONSTRUCTORS ---------------------------

	public DataCollector(String trajectoryFilename, String ensembleFilename, Enum[] tcnames)
		{
		timecourses = new DoubleTimecourse[tcnames.length];
		for (int i = 0; i < tcnames.length; i++)
			{
			timecourses[i] = new DoubleTimecourse(tcnames[i].name());
			}

		try
			{
			trajectoryWriter = new FileWriter(trajectoryFilename);
			ensembleWriter = new FileWriter(ensembleFilename);
			}
		catch (IOException e)
			{
			logger.error(e);
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			}
		}

	// ------------------------ CANONICAL METHODS ------------------------

	public String toString()
		{
		StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(sb, Locale.US);

		//sb.append(getStep()).append("\n");
		for (DoubleTimecourse t : timecourses)
			{
			formatter.format("%1$20s = %2$10s, %3$10s\n", t.name(), t.last(), t.runningaverage());
			}

		return sb.toString();
		}

	// -------------------------- OTHER METHODS --------------------------

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
			logger.error(e);
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			}
		}

	/*
   public void setChainTo(DataCollector dc)
	   {
	   chainTo = dc;
	   }*/

	public int getStep()
		{
		return lastStep;
		}

	public void setStep(int lastStep)
		{
		this.lastStep = lastStep;
		}

	public void setTimecourse(Enum name, double val)
		{
		DoubleTimecourse t = timecourses[name.ordinal()];
		t.set(val);

		//		if (chainTo != null)
		//			{
		//			chainTo.setTimecourse(name, val);
		//			}
		}

	public void writeSample(String s)
		{
		try
			{
			ensembleWriter.write(s);
			ensembleWriter.flush();
			}
		catch (IOException e)
			{
			logger.error(e);
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			}
		}

	public void writeTrajectory()
		{
		StringBuffer sb = new StringBuffer();

		sb.append(getStep()).append(", ");
		for (DoubleTimecourse t : timecourses)
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
			logger.error(e);
			e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
			}
		}
	}
