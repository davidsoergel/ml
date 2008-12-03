package edu.berkeley.compbio.ml.cluster.svm;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.dsutils.collections.HashWeightedSet;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassModel;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassProblem;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassProblemImpl;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassificationSVM;
import edu.berkeley.compbio.ml.cluster.BatchCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.SupervisedOnlineClusteringMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MultiClassificationSVMAdapter<T extends Clusterable<T>>
		extends SupervisedOnlineClusteringMethod<T, BatchCluster<T>>
	{
	private MultiClassificationSVM<BatchCluster<T>, T> svm;
	private MultiClassModel<BatchCluster<T>, T> model;


	private BinaryClassificationSVM<BatchCluster<T>, T> binarySvm;

	public void setBinarySvm(BinaryClassificationSVM<BatchCluster<T>, T> binarySvm)
		{
		this.binarySvm = binarySvm;
		}

	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException
		{
		Iterator<T> trainingIterator = trainingCollectionIteratorFactory.next();

		// cache the training set into an example map		// (too bad the svm training requires all examples in memory)

		//Multimap<String, T> examples = new HashMultimap<String, T>();
		Map<T, BatchCluster<T>> examples = new HashMap<T, BatchCluster<T>>();

		while (trainingIterator.hasNext())
			{
			T sample = trainingIterator.next();
			String label = sample.getWeightedLabels().getDominantKeyInSet(mutuallyExclusiveLabels);

			BatchCluster<T> cluster = theClusterMap.get(label);
			cluster.add(sample);

			examples.put(sample, cluster);
			}

		svm = new MultiClassificationSVM<BatchCluster<T>, T>(binarySvm, BatchCluster.class);
		MultiClassProblem<BatchCluster<T>, T> problem =
				new MultiClassProblemImpl(BatchCluster.class, new BatchClusterLabelInverter(), examples);
		svm.setupQMatrix(problem);
		model = svm.train(problem);
		}


	Map<String, BatchCluster<T>> theClusterMap;

	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException
		{		// do nothing with the iterator or any of that
		assert initSamples == 0;

		// by analogy with BayesianClustering, take this opportunity to initialize the clusters

		theClusterMap = new HashMap<String, BatchCluster<T>>(mutuallyExclusiveLabels.size());
		int i = 0;
		for (String label : mutuallyExclusiveLabels)
			{
			BatchCluster<T> cluster = theClusterMap.get(label);

			if (cluster == null)
				{
				cluster = new BatchCluster<T>(i++);
				theClusterMap.put(label, cluster);

				// ** consider how best to store the test labels
				HashWeightedSet<String> derivedLabelProbabilities = new HashWeightedSet<String>();
				derivedLabelProbabilities.add(label, 1);
				cluster.setDerivedLabelProbabilities(derivedLabelProbabilities);
				}
			}
		theClusters = theClusterMap.values();
		}


	public ClusterMove<T, BatchCluster<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		ClusterMove<T, BatchCluster<T>> result = new ClusterMove<T, BatchCluster<T>>();
		result.bestCluster = model.predictLabel(p);

		if (result.bestCluster == null)
			{
			throw new NoGoodClusterException();
			}

		// no other fields of ClusterMove are populated :(
		return result;
		}
	}
