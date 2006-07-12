package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * @author lorax
 * @version 1.0
 */
public class ClusterableDoubleArray implements Clusterable<ClusterableDoubleArray>
	{
	private static Logger logger = Logger.getLogger(ClusterableDoubleArray.class);

	private String id;
	private double[] data;

	public String getId()
		{
		return null;
		}

	public ClusterableDoubleArray(String id, double[] data)
		{
		this.id = id;
		this.data = data;
		}

	public ClusterableDoubleArray plus(ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), ArrayUtils.plus(data, object.data));
		}

	public ClusterableDoubleArray minus(ClusterableDoubleArray object)
		{
		return new ClusterableDoubleArray(id + "+" + object.getId(), ArrayUtils.minus(data, object.data));
		}

	public void incrementBy(ClusterableDoubleArray object)
		{
		data = ArrayUtils.plus(data, object.data);
		}

	public void decrementBy(ClusterableDoubleArray object)
		{
		data = ArrayUtils.minus(data, object.data);
		}

	public boolean equalValue(ClusterableDoubleArray object)
		{
		return id.equals(object.id) && ArrayUtils.equalWithinFPError(data, object.data);
		}

	public ClusterableDoubleArray clone()
		{
		return new ClusterableDoubleArray(id, data.clone());    //To change body of overridden methods use File | Settings | File Templates.
		}

	public int length() { return data.length; }
	public double get(int i) { return data[i]; }
	}
