package edu.berkeley.compbio.ml.cluster;

/**
 * @author lorax
 * @version 1.0
 */
public interface Clusterable<T extends Clusterable>
	{
	public String getId();

	public T plus(T object);

	public T minus(T object);

	public void incrementBy(T object);

	public void decrementBy(T object);

	public boolean equalValue(T object);

	public T clone();
	//public T times(double d);

	//public T weightedAverage(T object, double weight);
	}
