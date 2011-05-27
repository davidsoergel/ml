package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.ProbabilisticDissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractSupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.PointClusterFilter;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.ml.cluster.PrototypeBasedCentroidClusteringMethod;
import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasureRuntimeException;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class NearestNeighborClustering<T extends AdditiveClusterable<T>>
		extends AbstractSupervisedOnlineClusteringMethod<T, CentroidCluster<T>>
		implements PrototypeBasedCentroidClusteringMethod<T>
//		extends AbstractOnlineClusteringMethod<T, CentroidCluster<T>>
//		implements SupervisedClusteringMethod<T, CentroidCluster<T>>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(NearestNeighborClustering.class);
	protected final double unknownDistanceThreshold;


// --------------------------- CONSTRUCTORS ---------------------------

	public NearestNeighborClustering(final DissimilarityMeasure<T> dm, final double unknownDistanceThreshold,
	                                 final Set<String> potentialTrainingBins,
	                                 final Map<String, Set<String>> predictLabelSets,
	                                 final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		this.unknownDistanceThreshold = unknownDistanceThreshold;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidClusteringMethod ---------------------


	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(getClusters(), measure);
		}

	/*public void addAll(Iterable<T> samples)
		{
		for (Clusterable<T> sample : samples)
			{
			add(sample);
			}
		}
*/
	public void computeClusterStdDevs(final ClusterableIterator<T> theDataPointProvider)
		{
		CentroidClusteringUtils.computeClusterStdDevs(getClusters(), measure, getAssignments(), theDataPointProvider);
		}

	@Override
	public String clusteringStats()
		{
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(final OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(getClusters(), measure, outf);
		}

// --------------------- Interface OnlineClusteringMethod ---------------------


	/**
	 * {@inheritDoc}
	 */
/*	public boolean add(T p) throws ClusterException, NoGoodClusterException //, List<Double> secondBestDistances
		{
		ClusterMove best = bestClusterMove(p);
		//secondBestDistances.add(best.secondBestDistance);
		best.bestCluster.add(p);
		return true;
		}*/
/*
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory) //, int trainingEpochs)
			throws ClusterException
		{
		super.train(trainingCollectionIteratorFactory);
		//removeEmptyClusters();  // already done
		preparePriors();
		}*/

// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(final T p) throws NoGoodClusterException
		{
		final ClusterMove result = new ClusterMove();

		result.secondBestDistance = Double.POSITIVE_INFINITY;
		result.bestDistance = Double.POSITIVE_INFINITY;

		/*	Set<String> disallowedLabels = null;
	   if (leaveOneOutLabels != null)
		   {
		   try
			   {
			   disallowedLabels = prohibitionModel.getProhibited(p.getWeightedLabels());
	   //		disallowedLabel = p.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels);
			   }
		   catch (NoSuchElementException e)
			   {
			   // OK, leave disallowedLabel==null then
			   }
		   }*/


		PointClusterFilter<T> clusterFilter = prohibitionModel == null ? null : prohibitionModel.getFilter(p);

		for (final CentroidCluster<T> cluster : getClusters())
			{
			if (clusterFilter != null && clusterFilter.isProhibited(cluster))
				//if (disallowedLabels != null && disallowedLabels
				//		.containsAny(cluster.getWeightedLabels())) //.getDominantKeyInSet(leaveOneOutLabels)))
				{
				// ignore this cluster
				}
			else
				{
				try
					{
					// Note that different distance measures may need to deal with the priors differently:
					// if it's probability, multiply; if log probability, add; for other distance types, who knows?
					// so, just pass the priors in and let the distance measure decide what to do with them
					final double distance;
					if (measure instanceof ProbabilisticDissimilarityMeasure)
						{
						distance = ((ProbabilisticDissimilarityMeasure) measure)
								.distanceFromTo(p, cluster.getCentroid(), clusterPriors.get(cluster));
						}
					else
						{
						distance = measure.distanceFromTo(p, cluster.getCentroid());
						}

					if (distance <= result.bestDistance)
						{
						result.secondBestDistance = result.bestDistance;
						result.bestDistance = distance;
						result.bestCluster = cluster;
						}
					else if (distance <= result.secondBestDistance)
						{
						result.secondBestDistance = distance;
						}
					}
				catch (DistanceMeasureRuntimeException e)
					{
					// unable to compute a distance between the point and this cluster, for some reason.
					// Too bad, just ignore this cluster and see if we can assign to another one instead.
					// If the fault lies with the point, we'll end up with bestCluster == null
					logger.warn("Ignoring cluster " + cluster.getId() + "; couldn't compute distance");
					}
				}
			}

		if (result.bestCluster == null)
			{
			throw new ClusterRuntimeException(
					"None of the " + getNumClusters() + " clusters matched: " + p); // + ", last distance = " + temp);
			}
		if (result.bestDistance > unknownDistanceThreshold)
			{
			throw new NoGoodClusterException(
					"Best distance " + result.bestDistance + " > threshold " + unknownDistanceThreshold);
			}
		return result;
		}
	}
