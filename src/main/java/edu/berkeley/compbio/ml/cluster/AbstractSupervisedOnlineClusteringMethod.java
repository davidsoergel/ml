package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.stats.DissimilarityMeasure;

import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractSupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractOnlineClusteringMethod<T, C> implements SupervisedClusteringMethod<T>
	{
	protected AbstractSupervisedOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                                   Set<String> predictLabels, Set<String> leaveOneOutLabels,
	                                                   Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		}


	/**
	 * consider each of the incoming data points exactly once.
	 */
	protected void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
			throws IOException, ClusterException//, int maxpoints) throws IOException
		{
		// if initializeWithRealData is required, override this and then call super.train() as appropriate

		//Date totalstarttime = new Date();
		//List<Double> secondBestDistances = new ArrayList<Double>();
		trainOneIteration(trainingCollectionIteratorFactory); //, secondBestDistances);
		}


	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory,
	                  GenericFactory<T> prototypeFactory, int trainingEpochs) throws IOException, ClusterException
		{
		train(trainingCollectionIteratorFactory);
		}
	}
