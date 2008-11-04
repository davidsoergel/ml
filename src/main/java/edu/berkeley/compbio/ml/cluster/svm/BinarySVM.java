package edu.berkeley.compbio.ml.cluster.svm;

import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import org.apache.commons.lang.NotImplementedException;

import java.util.Collection;

/**
 * Wraps libsvm
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BinarySVM<T extends Clusterable<T>, C extends Cluster<T>> implements Comparable
	{
	Cluster<T> cluster1;
	Cluster<T> cluster2;

	public BinarySVM(Cluster<T> cluster1, Cluster<T> cluster2)
		{
		this.cluster1 = cluster1;
		this.cluster2 = cluster2;
		}

	public C classify(T p)
		{
		throw new NotImplementedException();
		}

	public void train(Collection<T> points1, Collection<T> points2)
		{
		throw new NotImplementedException();
		}

	public int compareTo(Object o)
		{
		// random but deterministic ordering
		int h = hashCode();
		int oh = o.hashCode();
		return h < oh ? -1 : h == oh ? 0 : 1;
		}

	public boolean equals(Object o)
		{
		if (this == o)
			{
			return true;
			}
		if (o == null || getClass() != o.getClass())
			{
			return false;
			}

		BinarySVM binarySVM = (BinarySVM) o;

		if (cluster1 != null ? !cluster1.equals(binarySVM.cluster1) : binarySVM.cluster1 != null)
			{
			return false;
			}
		if (cluster2 != null ? !cluster2.equals(binarySVM.cluster2) : binarySVM.cluster2 != null)
			{
			return false;
			}

		return true;
		}

	public int hashCode()
		{
		int result;
		result = (cluster1 != null ? cluster1.hashCode() : 0);
		result = 31 * result + (cluster2 != null ? cluster2.hashCode() : 0);
		return result;
		}
	}
