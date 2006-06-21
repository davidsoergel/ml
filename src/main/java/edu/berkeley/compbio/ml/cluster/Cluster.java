package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
public class Cluster<T extends Clusterable<T>>
	{
	private static Logger logger = Logger.getLogger(Cluster.class);
	protected DistanceMeasure<T> theDistanceMeasure;

	protected T centroid;
	protected int n = 0;

	public int getId()
		{
		return id;
		}

	public void setId(int id)
		{
		this.id = id;
		}

	private int id;

	public Cluster(DistanceMeasure<T> dm, T centroid) throws CloneNotSupportedException
		{
		this.centroid = centroid.clone();
		n++;
		//add(centroid);
		logger.debug("Created cluster with centroid: " + centroid);
		theDistanceMeasure = dm;
		}


	public double distanceToCentroid(T p)
		{
		return theDistanceMeasure.distanceBetween(centroid, p);
		}

	public boolean recenterByAdding(T point)
		{

		n++;
		logger.debug("Cluster added " + point);
		centroid.incrementBy(point);  // works because Kcounts are "nonscaling additive", but it's not generic
		//times((double)n/n+1).plus(point.times(1/((double)n+1)));
		return true;

		}


	public boolean recenterByRemoving(T point)
		{

		n--;
		logger.debug("Cluster removed " + point);
		centroid.decrementBy(point);  // works because Kcounts are "nonscaling additive", but it's not generic
		//times((double)n/n+1).plus(point.times(1/((double)n+1)));
		return true;

		}

	public DistanceMeasure<T> getTheDistanceMeasure()
		{
		return theDistanceMeasure;
		}

	public void setTheDistanceMeasure(DistanceMeasure<T> theDistanceMeasure)
		{
		this.theDistanceMeasure = theDistanceMeasure;
		}

	public T getCentroid()
		{
		return centroid;
		}

	public void setCentroid(T centroid)
		{
		this.centroid = centroid;
		}

	public int getN()
		{
		return n;
		}
/*
	public Cluster<T> clone()
		{
		Cluster<T> result = new Cluster<T>(theDistanceMeasure, centroid);
		result.addAll(this);
		return result;
		}
*/

	public String toString()
		{
		StringBuffer sb = new StringBuffer("\nCluster:");
		sb.append(" ").append(centroid).append("\n");

		return sb.toString();
		}


	public boolean equals(Cluster<T> other)
		{
		boolean result = centroid.equals(other.getCentroid())
				&& theDistanceMeasure.equals(other.getTheDistanceMeasure()) && super.equals(other);
		logger.debug("" + this + " equals " + other + ": " + result);
		return result;
		}
	}
