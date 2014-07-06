/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.stats.DissimilarityMeasure;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractUnsupervisedOnlineClusteringMethod<T extends Clusterable<T>, C extends Cluster<T>>
		extends AbstractClusteringMethod<T, C> implements OnlineClusteringMethod<T>,
//		extends AbstractOnlineClusteringMethod<T, C> implements
                                                          UnsupervisedClusteringMethod<T>
	{


//	protected final DissimilarityMeasure<T> measure;

// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractUnsupervisedOnlineClusteringMethod.class);


// --------------------------- CONSTRUCTORS ---------------------------

	protected AbstractUnsupervisedOnlineClusteringMethod(final DissimilarityMeasure<T> dm,
	                                                     final Set<String> potentialTrainingBins,
	                                                     final Map<String, Set<String>> predictLabelSets,
	                                                     final ProhibitionModel<T> prohibitionModel,
	                                                     final Set<String> testLabels)
		{
		super(dm, potentialTrainingBins, predictLabelSets, prohibitionModel, testLabels);
		//	measure = dm;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnlineClusteringMethod ---------------------

	/**
	 * adjust the centroids by considering each of the incoming data points exactly once per iteration.
	 */
	public void train(final ClusterableIteratorFactory<T> trainingCollectionIteratorFactory, final int trainingEpochs)
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
			final ClusterableIteratorFactory<T> trainingCollectionIteratorFactory) //, List<Double> secondBestDistances
			throws ClusterException
		{
		int changed = 0;
		final ClusterableIterator<T> trainingIterator = trainingCollectionIteratorFactory.next();
		//normalizeClusters();
		int c = 0;
//		final Date starttime = new Date();
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

					final T p = trainingIterator.next();
					if (add(p))
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
		final int changedProportion = changed == 0 ? 0 : (int) (100.0 * changed / c);
		logger.debug("Changed cluster assignment of " + changed + " points (" + changedProportion + "%)\n");
		// computeClusterStdDevs(theDataPointProvider);  // PERF cluster stddev is slow, should be optional.  Also, only works for sequential DPP
		if (logger.isDebugEnabled())
			{
			logger.debug("\n" + clusteringStats());
			}

		return changed == 0;
		}
	}
