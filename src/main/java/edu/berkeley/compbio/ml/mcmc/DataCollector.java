package edu.berkeley.compbio.ml.mcmc;

/**
 * Interface for objects that can collect timecourse samples from a Monte Carlo simulation.  The collected timecourses
 * are essentially arrays, whose indexes should all correspond; there is no explicit sense of the time scale except for
 * the array index.  Of course it is possible to add a timecourse which records the time (or step number, etc.) at which
 * each sample is taken.
 * <p/>
 * In addition to the timecourses (which typically record some summary statistics about the chain state), the
 * DataCollector allows storing String representations of the complete state itself.
 * <p/>
 * Finally, DataCollectors may have hierarchical structure; for instance, a DataCollector for a metropolis-coupled monte
 * carlo simulation can contiain a child DataCollector for each chain.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface DataCollector
	{
	/**
	 * Initialize this DataCollector.  May be automatically called by Spring, even if there are no explicit references to
	 * it in the code.
	 */
	void init();

	/**
	 * Perform any summary computations and clean up resources, e.g. closing output streams and database connections.
	 */
	void close();

	/**
	 * Append the given value to the named timecourse.  Note it's important to keep all timecourses in sync by calling this
	 * once for each timecourse when a sample is recorded.
	 *
	 * @param name the String name of the timecourse
	 * @param val  the double value to append
	 */
	void setTimecourseValue(String name, double val);

	//void writeLatestTrajectoryValues();

	/**
	 * Record a String, which should represent the complete state of the chain.  Should be used exclusively by
	 * implementations of {@see MonteCarloState.writeToDataCollector()}.
	 *
	 * @param s the String to record
	 */
	void writeSample(String s);

	/**
	 * Create and return a DataCollector that is a child of this one.
	 *
	 * @param name a String describing the child DataCollector that distinguishes it from other children
	 * @return the newly created DataCollector
	 */
	DataCollector newSubCollector(String name);
	}
