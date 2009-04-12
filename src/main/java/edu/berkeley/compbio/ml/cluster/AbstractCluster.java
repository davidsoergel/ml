package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AbstractCluster<T extends Clusterable<T>> implements Cluster<T>
	{
	/**
	 * The unique integer identifier of this cluster
	 */
	protected int id;
	protected WeightedSet<String> weightedLabels = new HashWeightedSet<String>();
	private WeightedSet<String> derivedLabelProbabilities = new HashWeightedSet<String>();

	public AbstractCluster(int id)
		{
		this.id = id;
		}

	public int compareTo(Cluster<T> o)
		{
		return id - o.getId();
		}

	/**
	 * {@inheritDoc}
	 */
	public int getId()
		{
		return id;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setId(int id)
		{
		this.id = id;
		}

	/**
	 * {@inheritDoc}
	 */
	public WeightedSet<String> getDerivedLabelProbabilities()//throws DistributionException
		{
		//derivedWeightedLabels.normalize();
		return derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setDerivedLabelProbabilities(WeightedSet<String> derivedLabelProbabilities)
		{
		this.derivedLabelProbabilities = derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public int getN()
		{
		return weightedLabels.getItemCount();
		}

	/**
	 * {@inheritDoc}
	 */
	public void updateDerivedWeightedLabelsFromLocal()//throws DistributionException
		{
		assert !weightedLabels.isEmpty();
		derivedLabelProbabilities = new HashWeightedSet<String>(weightedLabels.getItemCount());
		derivedLabelProbabilities.addAll(weightedLabels);
		/*for (Multiset.Entry<String> o : exclusiveLabelCounts.entrySet())// too bad Bag isn't generic
			{
			try
				{
				exclusiveLabelProbabilities.put(o.getElement(), o.getCount());
				}
			catch (DistributionException e)
				{
				logger.error(e);
				throw new ClusterRuntimeException(e);
				}
			}*/
		//derivedLabelProbabilities.normalize();  // don't bother, it'll be done on request anyway
		}

	public WeightedSet<String> getWeightedLabels()
		{
		return weightedLabels;
		}

	/**
	 * Add the given sample to this cluster.  Does not automatically remove the sample from other clusters of which it is
	 * already a member.
	 *
	 * @param point the sample to add
	 * @return true if the point was successfully added; false otherwise
	 */
	public boolean add(T point)
		{
		weightedLabels.addAll(point.getWeightedLabels());
		return true;
		}

	/**
	 * Add all the samples in the given cluster to this cluster.  Does not automatically remove the samples from other
	 * clusters of which they are already members.
	 *
	 * @param otherCluster the cluster containing samples to add
	 * @return true if the point was successfully added; false otherwise
	 */
	public boolean addAll(Cluster<T> otherCluster)
		{
		weightedLabels.addAll(otherCluster.getWeightedLabels());
		return true;
		}

	/**
	 * Remove the given sample from this cluster.
	 *
	 * @param point the sample to remove
	 * @return true if the point was successfully removed; false otherwise (in particular, if the point is not a member of
	 *         this cluster in the first place)
	 */
	public boolean remove(T point)
		{
		weightedLabels.removeAll(point.getWeightedLabels());
		return true;
		}

	/**
	 * Remove all the samples in the given cluster from this cluster.
	 *
	 * @param otherCluster the cluster containing samples to remove
	 * @return true if the point was successfully added; false otherwise
	 */
	public boolean removeAll(Cluster<T> otherCluster)
		{
		weightedLabels.removeAll(otherCluster.getWeightedLabels());
		return true;
		}
	}
