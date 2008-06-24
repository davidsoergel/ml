package edu.berkeley.compbio.ml.mcmc;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Rev$
 */
public interface DataCollector
	{
	void init();

	void close();

	void setTimecourse(Enum name, double val);

	void writeSample(String s);
	}
