package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.CollectionIteratorFactory;
import com.davidsoergel.stats.DissimilarityMeasure;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractUnsupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractOnlineClusteringMethod<T, C> implements UnsupervisedClusteringMethod<T>
	{
	private static final Logger logger = Logger.getLogger(AbstractUnsupervisedOnlineClusteringMethod.class);

	protected AbstractUnsupervisedOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                                     Set<String> predictLabels, Set<String> leaveOneOutLabels,
	                                                     Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabels, leaveOneOutLabels, testLabels);
		}


	/**
	 * consider each of the incoming data points exactly once per iteration.  Note iterations > 1 only makes sense for
	 * unsupervised clustering.
	 */
	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory, int iterations)
			throws IOException, ClusterException//, int maxpoints) throws IOException
		{
		// if initializeWithRealData is required, override this and then call super.train() as appropriate

		//Date totalstarttime = new Date();
		//List<Double> secondBestDistances = new ArrayList<Double>();
		for (int i = 0; i < iterations; i++)
			{
			if (trainOneIteration(trainingCollectionIteratorFactory)) //, secondBestDistances))
				{
				logger.debug("Steady state, done after " + (i + 1) + " iterations!");
				break;
				}
			}
		}
	}
