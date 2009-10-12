package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.collections.Symmetric2dBiMapWithDefault;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.phyloutils.PhylogenyNode;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a sort of hierarchical k-means
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class RandomOrderedBatchAgglomerativeClusteringMethod<T extends Clusterable<T>>
		extends BatchAgglomerativeClusteringMethod<T>
	{
	private static final Logger logger = Logger.getLogger(RandomOrderedBatchAgglomerativeClusteringMethod.class);

	public RandomOrderedBatchAgglomerativeClusteringMethod(final DissimilarityMeasure<T> dm,
	                                                       final Set<String> potentialTrainingBins,
	                                                       final Map<String, Set<String>> predictLabelSets,
	                                                       final ProhibitionModel<T> tProhibitionModel,
	                                                       final Set<String> testLabels, Agglomerator agg)
		{
		super(dm, potentialTrainingBins, predictLabelSets, tProhibitionModel, testLabels, agg);
		}

	public RandomOrderedBatchAgglomerativeClusteringMethod(final DissimilarityMeasure<T> dm,
	                                                       final Set<String> potentialTrainingBins,
	                                                       final Map<String, Set<String>> predictLabelSets,
	                                                       final ProhibitionModel<T> tProhibitionModel,
	                                                       final Set<String> testLabels,
	                                                       final ArrayList<HierarchicalCentroidCluster<T>> theClusters,
	                                                       final Map<String, HierarchicalCentroidCluster<T>> assignments,
	                                                       final int n, Agglomerator agg,
	                                                       Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix)
		{
		super(dm, potentialTrainingBins, predictLabelSets, tProhibitionModel, testLabels, theClusters, assignments, n,
		      agg, theActiveNodeDistanceMatrix);
		}

	public synchronized void train()  // aka performClustering
		{
		setN(theActiveNodeDistanceMatrix.numKeys());

		//	List<HierarchicalCentroidCluster<T>> theLeaves = new ArrayList(theActiveNodeDistanceMatrix.getActiveKeys());

		//	Collections.shuffle(theLeaves);

		//	for (HierarchicalCentroidCluster<T> leaf : theLeaves)

		// because we're going to leave all nodes in the distance matrix, we have to keep track separately of which nodes need aggregation
		final Set<HierarchicalCentroidCluster<T>> parentlessNodes =
				new HashSet<HierarchicalCentroidCluster<T>>(theActiveNodeDistanceMatrix.getActiveKeys());

		while (parentlessNodes.size() > 1)

			{
			HierarchicalCentroidCluster<T> node = DSCollectionUtils.chooseRandom(parentlessNodes);

			// find the nearest cluster to this node, including other leaves and composite clusters
			HierarchicalCentroidCluster<T> bestCluster = null;
			double bestDistance = Double.MAX_VALUE;

			// PERF the matrix is already sorted, but it's not obvious how to take advantage of that
			//theActiveNodeDistanceMatrix.getMatchingPairWithSmallestValue(node);

			for (HierarchicalCentroidCluster<T> testCluster : theActiveNodeDistanceMatrix.getActiveKeys())
				{
				if (!testCluster.equals(node))
					{
					double dist = theActiveNodeDistanceMatrix.get(node, testCluster);
					if (dist < bestDistance)
						{
						bestDistance = dist;
						bestCluster = testCluster;
						}
					}
				}

			// invalidate the portions of the tree that are sensitive to the cluster: its ancestors disappear entirely, and all of their immediate children lose their parent link

			final List<PhylogenyNode<CentroidCluster<T>>> ancestors = bestCluster.getAncestorPath(false);

			if (!ancestors.isEmpty())
				{

				final Set<PhylogenyNode<CentroidCluster<T>>> ancestorChildren =
						new HashSet<PhylogenyNode<CentroidCluster<T>>>();

				for (PhylogenyNode<CentroidCluster<T>> ancestor : ancestors)
					{
					ancestorChildren.addAll(ancestor.getChildren());
					theActiveNodeDistanceMatrix.remove((HierarchicalCentroidCluster<T>) ancestor);
					}

				assert ancestorChildren.contains(bestCluster);

				ancestorChildren.removeAll(ancestors);
				theClusters.removeAll(ancestors);
				parentlessNodes.removeAll(ancestors);
				//theActiveNodeDistanceMatrix.removeAll(ancestors);


				for (PhylogenyNode<CentroidCluster<T>> ancestorChild : ancestorChildren)
					{
					ancestorChild.setLength(null);
					ancestorChild.setParent(null);
					parentlessNodes.add((HierarchicalCentroidCluster<T>) ancestorChild);
					}
				//parentlessNodes.addAll(ancestorChildren);

				}

			// join the leaf with that cluster

			final HierarchicalCentroidCluster<T> composite =
					agglomerator.joinNodes(idCount.getAndIncrement(), node, bestCluster, theActiveNodeDistanceMatrix);

			parentlessNodes.remove(node);
			parentlessNodes.remove(bestCluster);
			parentlessNodes.add(composite);

			// note we _don't_ remove the joined nodes!

			addCluster(composite);
			theRoot = composite;  // this will actually be true on the last iteration


			int numKeys = parentlessNodes.size();
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
	}
