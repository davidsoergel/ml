package edu.berkeley.compbio.ml.cluster.bayesian;

import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.AbstractSupervisedOnlineClusteringMethod;
import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringMethod;
import edu.berkeley.compbio.ml.cluster.CentroidClusteringUtils;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class NeighborClustering<T extends AdditiveClusterable<T>>
		extends AbstractSupervisedOnlineClusteringMethod<T, CentroidCluster<T>> implements CentroidClusteringMethod<T>
//		extends AbstractOnlineClusteringMethod<T, CentroidCluster<T>>
//		implements SupervisedClusteringMethod<T, CentroidCluster<T>>
	{
	protected double unknownDistanceThreshold;

	protected Map<CentroidCluster, Double> priors;

	public NeighborClustering(DissimilarityMeasure<T> dm, double unknownDistanceThreshold,
	                          Set<String> potentialTrainingBins, Set<String> predictLabels,
	                          Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		this.unknownDistanceThreshold = unknownDistanceThreshold;
		}

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

	public void writeClusteringStatsToStream(OutputStream outf)
		{
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, outf);
		}

	@Override
	public String clusteringStats()
		{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		CentroidClusteringUtils.writeClusteringStatsToStream(theClusters, measure, b);
		return b.toString();
		}

	@Override
	public String shortClusteringStats()
		{
		return CentroidClusteringUtils.shortClusteringStats(theClusters, measure);
		}
	}
