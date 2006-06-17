package edu.berkeley.compbio.ml.cluster;

/**
 * @author lorax
 * @version 1.0
 */
public interface Clusterable<T extends Clusterable>
	{
	public T plus(T object);

	public T minus(T object);

	public boolean equalValue(T object);
	//public T times(double d);

	//public T weightedAverage(T object, double weight);
	}
