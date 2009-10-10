package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import com.davidsoergel.dsutils.collections.UnorderedPair;
import com.davidsoergel.dsutils.concurrent.Parallel;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.google.common.base.Function;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.ClusterRuntimeException;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.PointClusterFilter;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BatchAgglomerativeClustering<T extends Clusterable<T>> extends BatchHierarchicalClusteringMethod<T>
	{
	private static final Logger logger = Logger.getLogger(BatchAgglomerativeClustering.class);

	// set in the constructor
	protected final Agglomerator<T> agglomerator;

	//state

	protected final Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix;
	//=	new Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double>();


	// the result ends up here, but it may temporarily contain intermediate values
	protected HierarchicalCentroidCluster<T> theRoot;

//	private final Map<T, HierarchicalCentroidCluster<T>> sampleToLeafClusterMap =
//			new HashMap<T, HierarchicalCentroidCluster<T>>();

	protected final AtomicInteger idCount = new AtomicInteger(0);


//	HierarchicalCentroidCluster<T> saveNode;


	public BatchAgglomerativeClustering(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                    final Map<String, Set<String>> predictLabelSets,
	                                    final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels,
	                                    Agglomerator agg)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		this.agglomerator = agg;
		theActiveNodeDistanceMatrix = new Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double>();
		}

	public BatchAgglomerativeClustering(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                    final Map<String, Set<String>> predictLabelSets,
	                                    final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels,
	                                    final ArrayList<HierarchicalCentroidCluster<T>> theClusters,
	                                    final Map<String, HierarchicalCentroidCluster<T>> assignments, final int n,
	                                    Agglomerator agg,
	                                    Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels, theClusters, assignments, n);
		this.agglomerator = agg;
		this.theActiveNodeDistanceMatrix = theActiveNodeDistanceMatrix;
		}


	/**
	 * {@inheritDoc}
	 */
	public synchronized void addAll(final ClusterableIterator<T> samples)
		{
		if (theRoot != null)
			{
			throw new ClusterRuntimeException("Can't add samples to a batch clustering that has already been trained");
			}


		// first create all the new clusters
		final Set<HierarchicalCentroidCluster<T>> newClusters = createNewClusters(samples);


		// then compute distances between all new clusters and all clusters (including the old and new ones)
		computeDistances(newClusters);


		//});

		// this method doesn't parallelize well, because the apply() phase is too fast relative to the next() phase

		/*
		UnorderedPairIterator<HierarchicalCentroidCluster<T>> pairs =
				new UnorderedPairIterator<HierarchicalCentroidCluster<T>>(newClusters, getClusters());


		Parallel.forEach(pairs, new Function<UnorderedPair<HierarchicalCentroidCluster<T>>, Void>()
		{
		public Void apply(final UnorderedPair<HierarchicalCentroidCluster<T>> pair)
			{
			HierarchicalCentroidCluster<T> a = pair.getKey1();
			HierarchicalCentroidCluster<T> b = pair.getKey2();

			final Double d = measure.distanceFromTo(a.getValue().getCentroid(), b.getValue().getCentroid());
			theActiveNodeDistanceMatrix.put(a, b, d);
			int numPairs = theActiveNodeDistanceMatrix.numPairs();
			int numKeys = theActiveNodeDistanceMatrix.getActiveKeys().size();

			if (numPairs % 10000 == 0)
				{
				logger.info("UPGMA preparing " + numKeys + " active nodes, " + numPairs + " pair distances");
				}
			return null;
			}
		});
		*/
		}

	private void computeDistances(final Set<HierarchicalCentroidCluster<T>> newClusters)
		{
		final List<HierarchicalCentroidCluster<T>> allClusters = getClusters();

		final AtomicInteger clusterCounter = new AtomicInteger(0);

		//PERF Gah concurrency problems
		//** just use lazy compute?  No point, theActiveNodeDistanceMatrix needs the values immediately because it sorts on them.

		//for (HierarchicalCentroidCluster<T> a : newClusters)
		Parallel.forEach(newClusters, new Function<HierarchicalCentroidCluster<T>, Void>()
		{
		public Void apply(final HierarchicalCentroidCluster<T> a)
			{
			final int ahc = a.hashCode();
			// store up the results in this thread before copying them to the synchronized store at the end
			final Map<UnorderedPair<HierarchicalCentroidCluster<T>>, Double> result =
					new HashMap<UnorderedPair<HierarchicalCentroidCluster<T>>, Double>(getClusters().size());

			for (HierarchicalCentroidCluster<T> b : allClusters)
				{
				if (ahc <= b.hashCode() && !a.equals(b))
					{
					final double d = measure.distanceFromTo(a.getPayload().getCentroid(), b.getPayload().getCentroid());

					// concurrency bottleneck
					//theActiveNodeDistanceMatrix.put(a, b, d);

					result.put(new UnorderedPair<HierarchicalCentroidCluster<T>>(a, b), d);
					}
				}

			// this adds distances between all the new clusters and all those that were present as of the getClusters() call above.
			// if more clusters have been added in the meantime by another thread, then that thread's getClusters() call will include
			// the new clusters from this thread, because getClusters is synchronized; so the distances will be computed there.
			// if clusters were _removed_ in the meantime, that would be a problem because we'd hereby reactivate them.
			// But the whole point of this batch method is that we addAll first and train later, which is why both methods are synchronized.
			theActiveNodeDistanceMatrix.putAll(result);

			final int clusterCount = clusterCounter.incrementAndGet();
			if (clusterCount % 100 == 0)
				{
				int numKeys = theActiveNodeDistanceMatrix.getActiveKeys().size();

				//if (numPairs % 10000 == 0)
				//	{
				int numPairs = theActiveNodeDistanceMatrix.numPairs();
				logger.info("Batch agglomerative clustering preparing " + clusterCount + " of " + numKeys
				            + " active nodes, " + numPairs + " pair distances");
				}

			return null;
			}
		});
		theActiveNodeDistanceMatrix.matrixCompleteSanityCheck();
		}

	private Set<HierarchicalCentroidCluster<T>> createNewClusters(final ClusterableIterator<T> samples)
		{
		final Set<HierarchicalCentroidCluster<T>> newClusters =
				new ConcurrentSkipListSet<HierarchicalCentroidCluster<T>>();

		Parallel.forEach(samples, new Function<T, Void>()
		{
		public Void apply(final T sample)
			{
			final HierarchicalCentroidCluster<T> c =
					new HierarchicalCentroidCluster<T>(idCount.getAndIncrement(), sample);
			c.doneLabelling();
			newClusters.add(c);
			addCluster(c);
			return null;
			}
		});
		return newClusters;
		}

	/*
	private synchronized void add(final T sample)
		{
		final HierarchicalCentroidCluster c = new HierarchicalCentroidCluster(idCount.getAndIncrement(), sample);
		c.doneLabelling();
		//c.setN(1);
		addAndComputeDistances(c);
		}

		*/

	public void createClusters()
		{
		// do nothing
		}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void train()  // aka performClustering
		{
		setN(theActiveNodeDistanceMatrix.numKeys());
		while (theActiveNodeDistanceMatrix.numKeys() > 1)
			{
			// find shortest distance

			UnorderedPair<HierarchicalCentroidCluster<T>> pair =
					theActiveNodeDistanceMatrix.getKeyPairWithSmallestValue();
			final HierarchicalCentroidCluster<T> a = pair.getKey1();
			final HierarchicalCentroidCluster<T> b = pair.getKey2();
			final HierarchicalCentroidCluster<T> composite =
					agglomerator.joinNodes(idCount.getAndIncrement(), a, b, theActiveNodeDistanceMatrix);
			agglomerator.removeJoinedNodes(a, b, theActiveNodeDistanceMatrix);
			addCluster(composite);
			theRoot = composite;  // this will actually be true on the last iteration

			int numKeys = theActiveNodeDistanceMatrix.getActiveKeys().size();
			if (numKeys % 100 == 0)
				{
				//if (numPairs % 10000 == 0)
				//	{
				int numPairs = theActiveNodeDistanceMatrix.numPairs();
				logger.info("Batch agglomerative clustering: " + numKeys + " active nodes, " + numPairs
				            + " pair distances");
				}
			}

		normalizeClusterLabelProbabilities();
		}


	/**
	 * We can't add a single node when the matrix is empty, since it won't make any pairs and thus won't retain the node at
	 * all.  Hence the addInitialPair business above.
	 *
	 * @param node
	 */
/*	private synchronized void addAndComputeDistances(final HierarchicalCentroidCluster<T> node)
		{
		//PERF better synchronization
		//synchronized (theClusters)
		//	{
		addCluster(node);
		final int s = getNumClusters();
		final Set<HierarchicalCentroidCluster<T>> activeNodes =
				new HashSet(theActiveNodeDistanceMatrix.getActiveKeys());// avoid ConcurrentModificationException
		//	}

		if (s == 1)
			{
			saveNode = node;
			}
		if (s == 2)
			{
			final Double d =
					measure.distanceFromTo(saveNode.getPayload().getCentroid(), node.getPayload().getCentroid());
			theActiveNodeDistanceMatrix.put(saveNode, node, d);
			saveNode = null;
			}
		else
			{
			for (final HierarchicalCentroidCluster<T> theActiveNode : activeNodes)
				{
				final Double d = measure.distanceFromTo(node.getPayload().getCentroid(),
				                                        theActiveNode.getPayload().getCentroid());
				theActiveNodeDistanceMatrix.put(node, theActiveNode, d);
				}
			}
		}
*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClusterMove<T, HierarchicalCentroidCluster<T>> bestClusterMove(final T p) throws NoGoodClusterException
		{
		final ClusterMove result = new ClusterMove();
		result.bestDistance = Double.POSITIVE_INFINITY;

		// since we're not using sampleToLeafClusterMap explicitly, it's possible for one of the training samples to be mapped to another cluster at distance 0
		/*
		final HierarchicalCentroidCluster<T> c = sampleToLeafClusterMap.get(p);
		if (c != null)
			{
			// this sample was part of the initial clustering, so of course the best cluster is the one representing just the sample itself
			result.bestCluster = c;
			result.bestDistance = 0;
			return result;
			}
			*/

		PointClusterFilter<T> clusterFilter = prohibitionModel == null ? null : prohibitionModel.getFilter(p);

		for (final CentroidCluster<T> theCluster : getClusters())
			{
			if (clusterFilter != null && clusterFilter.isProhibited(theCluster))
				{
				// ignore this cluster
				}
			else
				{
				final double distance = measure.distanceFromTo(p, theCluster.getCentroid());
				if (distance < result.bestDistance)
					{
					result.bestCluster = theCluster;
					result.bestDistance = distance;
					}
				}
			}
		if (result.bestCluster == null)
			{
			throw new NoGoodClusterException("No cluster found for point: " + p);
			}
		return result;
		}

	/**
	 * {@inheritDoc}
	 */
	public HierarchicalCentroidCluster<T> getTree()
		{
		return theRoot;
		}
	}
