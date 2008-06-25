package edu.berkeley.compbio.ml.mcmc;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public interface DataCollector
	{
	void init();

	void close();

	void setTimecourseValue(String name, double val);

	void writeLatestTrajectoryValues();

	void writeSample(String s);

	DataCollector newSubCollector(String name);
	}
