package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import com.davidsoergel.stats.ProbabilisticDissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractSupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.PrototypeBasedCentroidClusteringMethod;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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
	protected double unknownDistanceThreshold;

	protected Map<CentroidCluster<T>, Double> priors;


// --------------------------- CONSTRUCTORS ---------------------------

	public NearestNeighborClustering(DissimilarityMeasure<T> dm, double unknownDistanceThreshold,
	                                 Set<String> potentialTrainingBins, Map<String, Set<String>> predictLabelSets,
	                                 Set<String> leaveOneOutLabels, Set<String> testLabels, int testThreads)
		{
		super(dm, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels, testThreads);
		this.unknownDistanceThreshold = unknownDistanceThreshold;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface CentroidClusteringMethod ---------------------


	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(theClusters, measure);
		}

	/*public void addAll(Iterable<T> samples)
		{
		for (Clusterable<T> sample : samples)
			{
			add(sample);
			}
		}
*/
	public void computeClusterStdDevs(ClusterableIterator<T> theDataPointProvider) throws IOException
		{
		CentroidClusteringUtils.computeClusterStdDevs(theClusters, measure, assignments, theDataPointProvider);
		}

	@Override
	public String clusteringStats()
		{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, b);
		return b.toString();
		}

	public void writeClusteringStatsToStream(OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, outf);
		}

// --------------------- Interface OnlineClusteringMethod ---------------------


	/**
	 * {@inheritDoc}
	 */
	public boolean add(T p) throws ClusterException, NoGoodClusterException //, List<Double> secondBestDistances
		{
		ClusterMove best = bestClusterMove(p);
		//secondBestDistances.add(best.secondBestDistance);
		best.bestCluster.add(p);
		return true;
		}

	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int trainingEpochs)
			throws ClusterException
		{
		super.train(trainingCollectionIteratorFactory, trainingEpochs);
		limitToPopulatedClusters();
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, CentroidCluster<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		ClusterMove result = new ClusterMove();

		result.secondBestDistance = Double.POSITIVE_INFINITY;
		result.bestDistance = Double.POSITIVE_INFINITY;

		String disallowedLabel = null;
		if (leaveOneOutLabels != null)
			{
			try
				{
				disallowedLabel = p.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels);
				}
			catch (NoSuchElementException e)
				{
				// OK, leave disallowedLabel==null then
				}
			}

		for (CentroidCluster<T> cluster : theClusters)
			{
			double distance;
			if (disallowedLabel != null && disallowedLabel
					.equals(cluster.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels)))
				{
				// ignore this cluster
				}
			else
				{
				// Note that different distance measures may need to deal with the priors differently:
				// if it's probability, multiply; if log probability, add; for other distance types, who knows?
				// so, just pass the priors in and let the distance measure decide what to do with them
				if (measure instanceof ProbabilisticDissimilarityMeasure)
					{
					distance = ((ProbabilisticDissimilarityMeasure) measure)
							.distanceFromTo(p, cluster.getCentroid(), priors.get(cluster));
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
			}

		if (result.bestCluster == null)
			{
			throw new ClusterRuntimeException(
					"None of the " + theClusters.size() + " clusters matched: " + p); // + ", last distance = " + temp);
			}
		if (result.bestDistance > unknownDistanceThreshold)
			{
			throw new NoGoodClusterException(
					"Best distance " + result.bestDistance + " > threshold " + unknownDistanceThreshold);
			}
		return result;
		}

	private void limitToPopulatedClusters()
		{
		try
			{
			// remove any clusters for which there were no training samples, to avoid any later confusion

			// while we're at it, make a uniform prior for the populated bins
			final Multinomial<CentroidCluster<T>> priorsMult = new Multinomial<CentroidCluster<T>>();

			for (Iterator<CentroidCluster<T>> i = theClusters.iterator(); i.hasNext();)
				{
				CentroidCluster<T> c = i.next();
				if (c.getN() == 0)
					{
					priors.remove(c);
					i.remove();
					}
				else
					{
					priorsMult.put(c, 1);
					}
				}
			priorsMult.normalize();
			priors = priorsMult.getValueMap();
			}
		catch (DistributionException e)
			{
			logger.error("Error", e);
			throw new ClusterRuntimeException(e);
			}
		}
	}
