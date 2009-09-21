package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.LabellableImpl;
import com.davidsoergel.dsutils.collections.ConcurrentHashWeightedSet;
import com.davidsoergel.dsutils.collections.ImmutableHashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class AbstractCluster<T extends Clusterable<T>> extends LabellableImpl<String> implements Cluster<T>
	{
// ------------------------------ FIELDS ------------------------------

	/**
	 * The unique integer identifier of this cluster
	 */
	protected final int id;
//	protected final MutableWeightedSet<String> weightedLabels = new ConcurrentHashWeightedSet<String>();


	/**
	 * we let the label probabilities be completely distinct from the local weights themselves, so that the probabilities
	 * can be set based on outside information (e.g., in the case of the Kohonen map, neighboring cells may exert an
	 * influence)
	 */
	private WeightedSet<String> derivedLabelProbabilities = new ConcurrentHashWeightedSet<String>();


// --------------------------- CONSTRUCTORS ---------------------------

	public AbstractCluster(final int id)
		{
		this.id = id;
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * {@inheritDoc}
	 */
	public WeightedSet<String> getDerivedLabelProbabilities()//throws DistributionException
		{
		return derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setDerivedLabelProbabilities(final WeightedSet<String> derivedLabelProbabilities)
		{
		this.derivedLabelProbabilities = derivedLabelProbabilities;
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
/*	public void setId(final int id)
		{
		this.id = id;
		}
*/

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Cluster ---------------------

	/**
	 * {@inheritDoc}
	 */
	public int getN()
		{
		return getMutableWeightedLabels().getItemCount();
		}

	/**
	 * {@inheritDoc}
	 */
	public void updateDerivedWeightedLabelsFromLocal()//throws DistributionException
		{
		//assert !weightedLabels.isEmpty();
		derivedLabelProbabilities = new ImmutableHashWeightedSet<String>(mutableWeightedLabels);
		//derivedLabelProbabilities.addAll(weightedLabels);
		//derivedLabelProbabilities.normalize();  // don't bother, it'll be done on request anyway
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(final T point)
		{
		mutableWeightedLabels.addAll(point.getMutableWeightedLabels());
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAll(final Cluster<T> otherCluster)
		{
		mutableWeightedLabels.addAll(otherCluster.getMutableWeightedLabels());
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(final T point)
		{
		mutableWeightedLabels.removeAll(point.getMutableWeightedLabels());
		return true;
		}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAll(final Cluster<T> otherCluster)
		{
		mutableWeightedLabels.removeAll(otherCluster.getMutableWeightedLabels());
		return true;
		}

// -------------------------- OTHER METHODS --------------------------

	public int compareTo(final Cluster<T> o)
		{
		return id - o.getId();
		}
	}
