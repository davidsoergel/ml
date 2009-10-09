package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.collections.Symmetric2dBiMap;
import com.davidsoergel.stats.DissimilarityMeasure;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ProhibitionModel;
import edu.berkeley.compbio.phyloutils.PhylogenyNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class RandomOrderedBatchAgglomerativeClustering<T extends Clusterable<T>> extends BatchAgglomerativeClustering<T>
	{
	public RandomOrderedBatchAgglomerativeClustering(final DissimilarityMeasure<T> dm,
	                                                 final Set<String> potentialTrainingBins,
	                                                 final Map<String, Set<String>> predictLabelSets,
	                                                 final ProhibitionModel<T> tProhibitionModel,
	                                                 final Set<String> testLabels, Agglomerator agg)
		{
		super(dm, potentialTrainingBins, predictLabelSets, tProhibitionModel, testLabels, agg);
		}

	public RandomOrderedBatchAgglomerativeClustering(final DissimilarityMeasure<T> dm,
	                                                 final Set<String> potentialTrainingBins,
	                                                 final Map<String, Set<String>> predictLabelSets,
	                                                 final ProhibitionModel<T> tProhibitionModel,
	                                                 final Set<String> testLabels,
	                                                 final ArrayList<HierarchicalCentroidCluster<T>> theClusters,
	                                                 final Map<String, HierarchicalCentroidCluster<T>> assignments,
	                                                 final int n, Agglomerator agg,
	                                                 Symmetric2dBiMap<HierarchicalCentroidCluster<T>, Double> theActiveNodeDistanceMatrix)
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

		while (theActiveNodeDistanceMatrix.numKeys() > 1)

			{
			HierarchicalCentroidCluster<T> node =
					DSCollectionUtils.chooseRandom(theActiveNodeDistanceMatrix.getActiveKeys());


			// find the nearest cluster to this node, including other leaves and composite clusters
			HierarchicalCentroidCluster<T> bestCluster = null;
			double bestDistance = Double.MAX_VALUE;

			// PERF the matrix is already sorted, but it's not obvious how to take advantage of that
			//theActiveNodeDistanceMatrix.getMatchingPairWithSmallestValue(node);

			for (HierarchicalCentroidCluster<T> testCluster : theActiveNodeDistanceMatrix.getActiveKeys())
				{
				double dist = theActiveNodeDistanceMatrix.get(node, testCluster);
				if (dist < bestDistance)
					{
					bestDistance = dist;
					bestCluster = testCluster;
					}
				}

			// invalidate the portions of the tree that are sensitive to the cluster: its ancestors disappear entirely, and all of their immediate children lose their parent link

			for (PhylogenyNode<CentroidCluster<T>> ancestor : bestCluster.getAncestorPath())
				{
				theClusters.remove(ancestor);
				theActiveNodeDistanceMatrix.remove((HierarchicalCentroidCluster<T>) ancestor);
				for (PhylogenyNode<CentroidCluster<T>> ancestorChild : ancestor.getChildren())
					{
					ancestorChild.setLength(null);
					ancestorChild.setParent(null);
					}
				}

			// join the leaf with that cluster

			final HierarchicalCentroidCluster<T> composite =
					agglomerator.joinNodes(idCount.getAndIncrement(), node, bestCluster, theActiveNodeDistanceMatrix);

			// note we _don't_ remove the joined nodes!

			addCluster(composite);
			theRoot = composite;  // this will actually be true on the last iteration
			}

		normalizeClusterLabelProbabilities();
		}
	}
