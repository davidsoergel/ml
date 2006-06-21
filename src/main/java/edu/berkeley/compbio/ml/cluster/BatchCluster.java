package edu.berkeley.compbio.ml.cluster;

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lorax
 * @version 1.0
 */
public class BatchCluster<T extends Clusterable<T>> extends Cluster<T>
	{
	private static Logger logger = Logger.getLogger(BatchCluster.class);

	Set<T> thePoints = new HashSet<T>();

	public BatchCluster(DistanceMeasure<T> dm, T centroid) throws CloneNotSupportedException
		{
		super(dm, centroid);
		}

/*	public boolean recalculateCentroid() throws ClusterException
		{
		// TODO
		// works because Kcounts are "nonscaling additive", but it's not generic

		assert thePoints.size() > 0;
		Iterator<T> i = thePoints.iterator();
		T sum = i.next();
		while (i.hasNext())
			{
			sum = sum.plus(i.next());
			}
		if (centroid.equalValue(sum))
			{
			return false;
			}
		centroid = sum;
		return true;
		}


	public boolean addAndRecenter(T point)
		{
		if (thePoints.add(point))
			{
			recenterByAdding(point);
			return true;
			}
		return false;
		}


	public boolean removeAndRecenter(T point)
		{
		if (thePoints.remove(point))
			{
			recenterByRemoving(point);
			return true;
			}
		return false;
		}

	public String toString()
		{
		StringBuffer sb = new StringBuffer("\nCluster:");
		sb.append(" ").append(centroid).append("\n");
		for (T t : thePoints)
			{
			sb.append(" ").append(t).append("\n");
			}
		return sb.toString();
		}

*/
	}
