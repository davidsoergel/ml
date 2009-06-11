package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractUnsupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractOnlineClusteringMethod<T, C> implements UnsupervisedClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractUnsupervisedOnlineClusteringMethod.class);


// --------------------------- CONSTRUCTORS ---------------------------

	protected AbstractUnsupervisedOnlineClusteringMethod(DissimilarityMeasure<T> dm, Set<String> potentialTrainingBins,
	                                                     Map<String, Set<String>> predictLabelSets,
	                                                     Set<String> leaveOneOutLabels, Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels);
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnlineClusteringMethod ---------------------

	/**
	 * adjust the centroids by considering each of the incoming data points exactly once per iteration.
	 */
	public void train(ClusterableIteratorFactory<T> trainingCollectionIteratorFactory, int trainingEpochs)
			throws ClusterException//, int maxpoints) throws IOException
		{
		// if initializeWithRealData is required, override this and then call super.train() as appropriate

		//Date totalstarttime = new Date();
		//List<Double> secondBestDistances = new ArrayList<Double>();
		for (int i = 0; i < trainingEpochs; i++)
			{
			if (trainOneIteration(trainingCollectionIteratorFactory)) //, secondBestDistances))
				{
				logger.debug("Steady state, done after " + (i + 1) + " iterations!");
				break;
				}
			}
		removeEmptyClusters();
		normalizeClusterLabelProbabilities();
		}


	//public abstract void addAndRecenter(T v);
	//public abstract void reassign(T v);


// -------------------------- OTHER METHODS --------------------------

	protected boolean trainOneIteration(
			ClusterableIteratorFactory<T> trainingCollectionIteratorFactory) //, List<Double> secondBestDistances
			throws ClusterException
		{
		int changed = 0;
		ClusterableIterator<T> trainingIterator = trainingCollectionIteratorFactory.next();
		//normalizeClusters();
		int c = 0;
		Date starttime = new Date();
		//secondBestDistances.clear();
		try
			{
			while (true)
				{

				try
					{
					// why on earth are we 'choosing best clusters" in training ???
					// okay, that's assuming an unsupervised clustering method, where things just get clustered by proximity and we look at the labels later.
					// but for a supervised method, the clusters are defined by the labels in the first place.

					if (add(trainingIterator.next()))
						{
						changed++;
						}
					}
				catch (NoGoodClusterException e)
					{
					// too bad, just ignore this unclassifiable point.
					// it may be classifiable in a future iteration.
					// if no other points get changed, then this one will stay unclassified.
					}

				c++;
				/*	if (logger.isDebugEnabled() && c % 1000 == 0)
												   {
												   Date endtime = new Date();
												   double realtime = (endtime.getTime() - starttime.getTime()) / (double) 1000;

												   logger.debug(
														   new Formatter().format("%d p/%d sec = %d p/sec; specificity = %.3f; %s", c, (int) realtime,
																				  (int) (c / realtime),
																				  (DSCollectionUtils.sum(secondBestDistances) / (double) c),
																				  shortClusteringStats()));
												   //					logger.info("" + c + " p/" + (int) realtime + " sec = " + (int) (c / realtime)
												   //							+ " p/sec; specificity = " + (ArrayUtils.sum(secondBestDistances) / (double) c) + " " + shortClusteringStats());
												   }*/
				/*	if (c >= maxpoints)
												   {
												   break;
												   }*/
				}
			}
		catch (NoSuchElementException e)
			{
			// iterator exhausted
			}
		int changedProportion = changed == 0 ? 0 : (int) (100.0 * changed / c);
		logger.debug("Changed cluster assignment of " + changed + " points (" + changedProportion + "%)\n");
		// computeClusterStdDevs(theDataPointProvider);  // PERF cluster stddev is slow, should be optional.  Also, only works for sequential DPP
		if (logger.isDebugEnabled())
			{
			logger.debug("\n" + clusteringStats());
			}

		return changed == 0;
		}
	}
