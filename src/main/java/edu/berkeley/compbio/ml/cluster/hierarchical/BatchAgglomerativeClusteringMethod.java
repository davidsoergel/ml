package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.IndexedSymmetric2dBiMapWithDefault;
import com.davidsoergel.dsutils.collections.OrderedPair;
import com.davidsoergel.dsutils.collections.UnorderedPair;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.PointClusterFilter;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class BatchAgglomerativeClusteringMethod<T extends Clusterable<T>> extends BatchHierarchicalClusteringMethod<T>
	{
	private static final Logger logger = Logger.getLogger(BatchAgglomerativeClusteringMethod.class);

	public static final Float LONG_DISTANCE = Float.MAX_VALUE;

	// set in the constructor
	protected final Agglomerator<T> agglomerator;

	//state

	protected IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix;
	//=	new Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double>();


	// the result ends up here, but it may temporarily contain intermediate values
	protected HierarchicalCentroidCluster<T> theRoot;

//	private final Map<T, HierarchicalCentroidCluster<T>> sampleToLeafClusterMap =
//			new HashMap<T, HierarchicalCentroidCluster<T>>();

	protected final AtomicInteger nextId = new AtomicInteger(0);
	private Float threshold;

	public void setThreshold(final float threshold)
		{
		this.threshold = threshold;
		}

//	HierarchicalCentroidCluster<T> saveNode;

	public void setDistanceMatrix(
			final IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix)
		{
		this.theActiveNodeDistanceMatrix = theActiveNodeDistanceMatrix;
		nextId.set(theActiveNodeDistanceMatrix.getNextId());
		}


	public BatchAgglomerativeClusteringMethod(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                          final Map<String, Set<String>> predictLabelSets,
	                                          final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels,
	                                          Agglomerator agg)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		this.agglomerator = agg;
		theActiveNodeDistanceMatrix =
				new IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float>(LONG_DISTANCE);
		nextId.set(theActiveNodeDistanceMatrix.getNextId());
		}

	public BatchAgglomerativeClusteringMethod(final DissimilarityMeasure<T> dm, final Set<String> potentialTrainingBins,
	                                          final Map<String, Set<String>> predictLabelSets,
	                                          final ProhibitionModel<T> prohibitionModel, final Set<String> testLabels,
	                                          final ArrayList<HierarchicalCentroidCluster<T>> theClusters,
	                                          final Map<String, HierarchicalCentroidCluster<T>> assignments,
	                                          final int n, Agglomerator agg,
	                                          IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float> theActiveNodeDistanceMatrix)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels, theClusters, assignments, n);
		this.agglomerator = agg;
		this.theActiveNodeDistanceMatrix = theActiveNodeDistanceMatrix;
		nextId.set(theActiveNodeDistanceMatrix.getNextId());
		}


	public IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Float> getDistanceMatrix()
		{
		return theActiveNodeDistanceMatrix;
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

		// ** sanity checks
/*		for (Map.Entry<UnorderedPair<HierarchicalCentroidCluster<T>>, Float> entry : theActiveNodeDistanceMatrix
				.entrySet())
			{
			UnorderedPair<HierarchicalCentroidCluster<T>> pair = entry.getKey();
			assert pair.getKey1().getParent() == null;
			assert pair.getKey2().getParent() == null;
			}*/

		while (!theActiveNodeDistanceMatrix.isEmpty())
			{

//			OrderedPair<UnorderedPair<HierarchicalCentroidCluster<T>>, Double> keyValuePair =
//					theActiveNodeDistanceMatrix.getKeyPairAndSmallestValue();

			// find shortest distance

			//	UnorderedPair<HierarchicalCentroidCluster<T>> pair = //keyValuePair.getKey1();
			//			theActiveNodeDistanceMatrix.getKeyPairWithSmallestValue();
			OrderedPair<UnorderedPair<HierarchicalCentroidCluster<T>>, Float> smallest =
					theActiveNodeDistanceMatrix.getKeyPairAndSmallestValue();
			UnorderedPair<HierarchicalCentroidCluster<T>> pair = smallest.getKey1();
			if (smallest.getKey2() > threshold)
				{
				break;
				}
			final HierarchicalCentroidCluster<T> a = pair.getKey1();
			final HierarchicalCentroidCluster<T> b = pair.getKey2();

			assert a.getParent() == null;
			assert b.getParent() == null;

			//assert theActiveNodeDistanceMatrix.getActiveKeys().contains(a);
			//assert theActiveNodeDistanceMatrix.getActiveKeys().contains(b);

			assert a.getWeight() != null;
			assert b.getWeight() != null;

			final HierarchicalCentroidCluster<T> composite =
					agglomerator.joinNodes(nextId.getAndIncrement(), a, b, theActiveNodeDistanceMatrix);

//			agglomerator.removeJoinedNodes(a, b, theActiveNodeDistanceMatrix);

			theActiveNodeDistanceMatrix.remove(a);
			theActiveNodeDistanceMatrix.remove(b);

			//assert !theActiveNodeDistanceMatrix.getActiveKeys().contains(a);
			//assert !theActiveNodeDistanceMatrix.getActiveKeys().contains(b);

			addCluster(composite);
			theRoot = composite;  // this may actually be true on the last iteration

			assert composite.getParent() == null;
			assert !composite.getChildren().isEmpty();
			assert composite.getWeight() != null;


			/*		int numKeys = theActiveNodeDistanceMatrix.getActiveKeys().size();
		   if (numKeys % 100 == 0)
			   {
			   //if (numPairs % 10000 == 0)
			   //	{
			   int numPairs = theActiveNodeDistanceMatrix.numPairs();
			   logger.info("Batch agglomerative clustering: " + numKeys + " active nodes, " + numPairs
						   + " pair distances");
			   }*/
			}


		// if we had set a distance cutoff and ended up with multiple disconnected clusters,
		// connect them to a single root via a star phylogeny with long branches

		Collection<HierarchicalCentroidCluster<T>> remainingKeys = theActiveNodeDistanceMatrix.getKeys();
		if (remainingKeys.size() > 1)
			{
			theRoot = new HierarchicalCentroidCluster<T>(nextId.getAndIncrement(), null);
			for (HierarchicalCentroidCluster<T> remainingKey : remainingKeys)
				{
				remainingKey.setParent(theRoot);
				remainingKey.setLength(Double.MAX_VALUE);
				theRoot.addAll(remainingKey);
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
