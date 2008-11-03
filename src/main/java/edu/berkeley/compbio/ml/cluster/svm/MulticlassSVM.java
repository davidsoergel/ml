package edu.berkeley.compbio.ml.cluster.svm;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.SupervisedOnlineClusteringMethod;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MulticlassSVM<T extends Clusterable<T>> extends SupervisedOnlineClusteringMethod<T>
	{


	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
	                                   GenericFactory<T> prototypeFactory)
			throws GenericFactoryException, ClusterException
		{
		// do nothing
		}


	/**
	 * Return a ClusterMove object describing the best way to reassign the given point to a new cluster.
	 *
	 * @param p
	 * @return
	 */
	public ClusterMove bestClusterMove(T p) throws NoGoodClusterException
		{
		// phase 1: classify by all vs. all voting

		// phase 2: reject classification by one vs. all

		// if the top hit is rejected, should we try the second hit, etc.?
		}

	/**
	 * consider each of the incoming data points exactly once.
	 */
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException
		{
		Iterator<T> trainingIterator = trainingCollectionIteratorFactory.next();

		// separate the training set into label-specific sets, caching all the while

		for (T sample : trainingIterator)
			{
			sample.getWeightedLabels()
			}
		}


	/**
	 * Sets a list of labels wo be used for classification.  For a supervised method, this must be called before training.
	 *
	 * @param mutuallyExclusiveLabels
	 */
	public void setLabels(Set<String> mutuallyExclusiveLabels)
		{
		super.setLabels(mutuallyExclusiveLabels);

		setupAllVsAllClassifiers();
		setupOneVsAllClassifiers();
		}
	}
