package edu.berkeley.compbio.ml.distancemeasure;

import org.apache.log4j.Logger;
import edu.berkeley.compbio.ml.cluster.ClusterableDoubleArray;

/**
 * @author lorax
 * @version 1.0
 */
public class EuclideanDistance implements DistanceMeasure<ClusterableDoubleArray>
	{
	private static Logger logger = Logger.getLogger(EuclideanDistance.class);

	private static EuclideanDistance _instance = new EuclideanDistance();

	public static EuclideanDistance getInstance()
		{
		return _instance;
		}

	public double distanceBetween(ClusterableDoubleArray a, ClusterableDoubleArray b)
		{
		double sum = 0;
		double x,y;
		int l = a.length();
		for(int i = 0; i<l; i++)
			{
			x = a.get(i);
			y = b.get(i);
			sum += (x-y)*(x-y);
			}
		return Math.sqrt(sum);
		}
	}
