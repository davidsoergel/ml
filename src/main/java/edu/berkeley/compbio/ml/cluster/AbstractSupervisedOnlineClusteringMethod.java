package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractSupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractOnlineClusteringMethod<T, C> implements SupervisedClusteringMethod<T>
	{
	private static final Logger logger = Logger.getLogger(AbstractSupervisedOnlineClusteringMethod.class);

// --------------------------- CONSTRUCTORS ---------------------------

	protected AbstractSupervisedOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                                   Map<String, Set<String>> predictLabelSets,
	                                                   Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels);
		}


/*	public void train(
			CollectionIteratorFactory<T> trainingCollectionIteratorFactory) //, List<Double> secondBestDistances
			throws ClusterException
		{
		Iterator<T> trainingIterator = trainingCollectionIteratorFactory.next();
		train(trainingIterator);
		removeEmptyClusters();
		normalizeClusterLabelProbabilities();
		}*/

	public void train(ClusterableIterator<T> trainingIterator) throws ClusterException
		{
		trainWithKnownTrainingLabels(trainingIterator);
		removeEmptyClusters();
		normalizeClusterLabelProbabilities();
		preparePriors();
		}

	protected abstract void trainWithKnownTrainingLabels(final ClusterableIterator<T> trainingIterator);


	protected Map<Cluster<T>, Double> clusterPriors;

	/**
	 * for now we make a uniform prior
	 */
	protected void preparePriors()
		{
		try
			{
			final Multinomial<Cluster<T>> priorsMult = new Multinomial<Cluster<T>>();

			for (Cluster<T> cluster : theClusters)
				{
				priorsMult.put(cluster, 1);
				}
			priorsMult.normalize();
			clusterPriors = priorsMult.getValueMap();
			}
		catch (DistributionException e)
			{
			logger.error("Error", e);
			throw new ClusterRuntimeException(e);
			}
		}


	/*
		{
		while (trainingIterator.hasNext())
			{
			T point = trainingIterator.next();

			try
				{

				if (add())
					{
					changed++;
					}
				}
			catch (NoGoodClusterException e)
				{
				// too bad, just ignore this unclassifiable point.
				// it may be classifiable in a future iteration.
				// if no other points get changed, then this one will stay unclassified.
				}

			c++;

			}
		if (logger.isDebugEnabled())
			{
			logger.debug("\n" + clusteringStats());
			}

		}*/
	}
