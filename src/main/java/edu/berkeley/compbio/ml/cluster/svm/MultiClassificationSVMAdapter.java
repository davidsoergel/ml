package edu.berkeley.compbio.ml.cluster.svm;

import com.davidsoergel.dsutils.concurrent.DepthFirstThreadPoolExecutor;
import com.davidsoergel.dsutils.concurrent.ThreadPoolPerformanceStats;
import com.davidsoergel.runutils.HierarchicalTypedPropertyNode;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.DistributionException;
import com.google.common.base.Function;
import com.google.common.base.Nullable;
import com.google.common.collect.MapMaker;
import edu.berkeley.compbio.jlibsvm.ImmutableSvmParameter;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassModel;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassProblem;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassProblemImpl;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassificationSVM;
import edu.berkeley.compbio.jlibsvm.multi.VotingResult;
import edu.berkeley.compbio.jlibsvm.scaler.NoopScalingModel;
import edu.berkeley.compbio.ml.cluster.AbstractBatchClusteringMethod;
import edu.berkeley.compbio.ml.cluster.BatchCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterMove;
import edu.berkeley.compbio.ml.cluster.Clusterable;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ClusteringTestResults;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.cluster.SupervisedClusteringMethod;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class MultiClassificationSVMAdapter<T extends Clusterable<T>>
		extends AbstractBatchClusteringMethod<T, BatchCluster<T>> implements SupervisedClusteringMethod<T>
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(MultiClassificationSVMAdapter.class);


	ImmutableSvmParameter<BatchCluster<T>, T> param;

	Map<T, BatchCluster<T>> examples = new HashMap<T, BatchCluster<T>>();
	Map<T, Integer> exampleIds = new HashMap<T, Integer>();


	Map<String, BatchCluster<T>> theClusterMap;

	Map<String, MultiClassModel<BatchCluster<T>, T>> leaveOneOutModels;

	private MultiClassificationSVM<BatchCluster<T>, T> svm;
	private MultiClassModel<BatchCluster<T>, T> model;


	private BinaryClassificationSVM<BatchCluster<T>, T> binarySvm;

	private int nrThreads;

// --------------------------- CONSTRUCTORS ---------------------------

	//public MultiClassificationSVMAdapter(@NotNull ImmutableSvmParameter<BatchCluster<T>, T> param)
	//	{
	//	super(null);
	//	this.param = param;
	//	}

	public MultiClassificationSVMAdapter(Set<String> potentialTrainingBins, Map<String, Set<String>> predictLabelSets,
	                                     Set<String> leaveOneOutLabels, Set<String> testLabels,
	                                     @NotNull ImmutableSvmParameter<BatchCluster<T>, T> param, int trainingThreads,
	                                     int testThreads)
		{
		super(null, potentialTrainingBins, predictLabelSets, leaveOneOutLabels, testLabels, testThreads);
		this.param = param;
		this.nrThreads = trainingThreads;
		}

// --------------------- GETTER / SETTER METHODS ---------------------

	public void setBinarySvm(BinaryClassificationSVM<BatchCluster<T>, T> binarySvm)
		{
		this.binarySvm = binarySvm;
		}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BatchClusteringMethod ---------------------

	Integer c = 0;

	public void addAll(
			final ClusterableIterator<T> trainingIterator) //CollectionIteratorFactory<T> trainingCollectionIteratorFactory)
		{
		//	Iterator<T> trainingIterator = trainingCollectionIteratorFactory.next();


		// cache the training set into an example map
		//  (too bad the svm training requires all examples in memory)

		//Multimap<String, T> examples = new HashMultimap<String, T>();

		DepthFirstThreadPoolExecutor execService = new DepthFirstThreadPoolExecutor(nrThreads, nrThreads * 2);

		execService.submitAndWaitForAll(new Iterator<Runnable>()
		{
		boolean hasnext = true;

		public boolean hasNext()
			{
			return hasnext;
			}

		public Runnable next()
			{
			return new Runnable()
			{
			public void run()
				{
				try
					{
					T sample = trainingIterator.next();
					add(sample);
					}
				catch (NoSuchElementException e)
					{
					// iterator exhausted
					hasnext = false;
					}
				}
			};
			}

		public void remove()
			{
			}
		});

		execService.shutdown();

		logger.info("Prepared " + c + " training samples");
		}

	public void add(final T sample)
		{
		String label = sample.getWeightedLabels().getDominantKeyInSet(potentialTrainingBins);

		BatchCluster<T> cluster = theClusterMap.get(label);
		cluster.add(sample);

		synchronized (c)
			{
			examples.put(sample, cluster);
			exampleIds.put(sample, c);
			c++;

			if (c % 1000 == 0)
				{
				logger.debug("Prepared " + c + " training samples");
				}
			}
		}

//	public void initializeWithRealData(Iterator<T> trainingIterator, int initSamples,
//	                                   GenericFactory<T> prototypeFactory)
//			throws GenericFactoryException, ClusterException
//		{
	// do nothing with the iterator or any of that
	// assert initSamples == 0;
//		}

	public void createClusters()
		{

		theClusterMap = new HashMap<String, BatchCluster<T>>(potentialTrainingBins.size());
		int i = 0;
		for (String label : potentialTrainingBins)
			{
			BatchCluster<T> cluster = theClusterMap.get(label);

			if (cluster == null)
				{
				cluster = new BatchCluster<T>(i++);
				theClusterMap.put(label, cluster);

				// ** consider how best to store the test labels

				// derive the label probabilities from the training weights later, as usual
				/*	HashWeightedSet<String> derivedLabelProbabilities = new HashWeightedSet<String>();
				derivedLabelProbabilities.add(label, 1.);
				derivedLabelProbabilities.incrementItems();
				cluster.setDerivedLabelProbabilities(derivedLabelProbabilities);
				*/
				}
			}
		theClusters = theClusterMap.values();
		}

/*	public void train(CollectionIteratorFactory<T> trainingCollectionIteratorFactory,
	                  int trainingEpochs) throws IOException, ClusterException
		{
		addAll(trainingCollectionIteratorFactory.next());
		train();
		}*/

	public ThreadPoolPerformanceStats trainingStats;

	public void train()
		{
		svm = new MultiClassificationSVM<BatchCluster<T>, T>(binarySvm);
		MultiClassProblem<BatchCluster<T>, T> problem =
				new MultiClassProblemImpl<BatchCluster<T>, T>(BatchCluster.class, new BatchClusterLabelInverter<T>(),
				                                              examples, exampleIds, new NoopScalingModel<T>());
		//svm.setupQMatrix(problem);
		logger.debug("Performing multiclass training");
		DepthFirstThreadPoolExecutor execService = new DepthFirstThreadPoolExecutor(nrThreads, nrThreads * 2);
		model = svm.train(problem, param, execService);

		trainingStats = execService.shutdown();

		if (leaveOneOutLabels != null)
			{
			leaveOneOutModels =
					new MapMaker().makeComputingMap(new Function<String, MultiClassModel<BatchCluster<T>, T>>()
					{
					public MultiClassModel<BatchCluster<T>, T> apply(@Nullable String disallowedLabel)
						{
						Set<BatchCluster<T>> disallowedClusters = new HashSet<BatchCluster<T>>();

						for (BatchCluster<T> cluster : model.getLabels())
							{
							if (cluster.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels)
									.equals(disallowedLabel))
								{
								disallowedClusters.add(cluster);
								}
							}
						return new MultiClassModel<BatchCluster<T>, T>(model, disallowedClusters);
						}
					});
			}

		removeEmptyClusters();
		normalizeClusterLabelProbabilities();
		}


	public void putResults(final HierarchicalTypedPropertyNode<String, Object> resultsNode)
		{
		resultsNode.addChild("trainingCpuSeconds", trainingStats.getCpuSeconds());
		resultsNode.addChild("trainingUserSeconds", trainingStats.getUserSeconds());
		resultsNode.addChild("trainingSystemSeconds", trainingStats.getSystemSeconds());
		resultsNode.addChild("trainingBlockedSeconds", trainingStats.getBlockedSeconds());
		resultsNode.addChild("trainingWaitedSeconds", trainingStats.getWaitedSeconds());
		}

	// -------------------------- OTHER METHODS --------------------------
	@Override
	public ClusteringTestResults test(ClusterableIterator<T> theTestIterator,
	                                  DissimilarityMeasure<String> intraLabelDistances)
			throws DistributionException, ClusterException
		{
		ClusteringTestResults result = super.test(theTestIterator, intraLabelDistances);
		result.setInfo(model.getInfo());
		//result.setCrossValidationResults(model.getCrossValidationResults());
		return result;
		}

	public ClusterMove<T, BatchCluster<T>> bestClusterMove(T p) throws NoGoodClusterException
		{
		MultiClassModel<BatchCluster<T>, T> leaveOneOutModel = model;
		if (leaveOneOutLabels != null)
			{
			try
				{
				//BAD LOO doesn't work??
				String disallowedLabel = p.getWeightedLabels().getDominantKeyInSet(leaveOneOutLabels);
				leaveOneOutModel = leaveOneOutModels.get(disallowedLabel);
				}
			catch (NoSuchElementException e)
				{
				// OK, just use the full model then
				//leaveOneOutModel = model;
				}
			}


		VotingResult<BatchCluster<T>> r = leaveOneOutModel.predictLabelWithQuality(p);
		ClusterMove<T, BatchCluster<T>> result = new ClusterMove<T, BatchCluster<T>>();
		result.bestCluster = r.getBestLabel();

		result.voteProportion = r.getBestVoteProportion();
		result.secondBestVoteProportion = r.getSecondBestVoteProportion();

		result.bestDistance = r.getBestOneVsAllProbability();
		result.secondBestDistance = r.getSecondBestOneVsAllProbability();


		//**  just drop these for now
		/*
		r.getBestOneClassProbability();
		r.getSecondBestOneClassProbability();
		*/


		if (result.bestCluster == null)
			{
			throw new NoGoodClusterException();
			}

		// no other fields of ClusterMove are populated :(
		return result;
		}
	}
