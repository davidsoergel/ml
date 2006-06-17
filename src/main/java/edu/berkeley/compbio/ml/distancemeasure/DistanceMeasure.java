package edu.berkeley.compbio.ml.distancemeasure;

/**
 * @author lorax
 * @version 1.0
 */
public interface DistanceMeasure<T>
	{
	//public String getName();
	public double distanceBetween(T a, T b);
	}
