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
	protected static Logger logger = Logger.getLogger(DataCollector.class);
	private DoubleTimecourse[] timecourses;
	private int lastStep;
	// protected DataCollector chainTo;


	private FileWriter trajectoryWriter;
	private FileWriter ensembleWriter;


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
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

		}

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
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}
