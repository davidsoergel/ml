package edu.berkeley.compbio.ml.cluster.biojavasvm;

import com.davidsoergel.dsutils.math.MathUtils;
import edu.berkeley.compbio.ml.cluster.Clusterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Heavily modified from from BioJava 1.6  org.biojava.stats.svm.SMOTrainer which was mostly translated from the Platt
 * 1998 pseudocode anyway
 * <p/>
 * Train a support vector machine using the Sequential Minimal Optimization algorithm.  See Kernel Methods book.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class ClassificationSVM<T extends Clusterable<T>>//, C extends Cluster<T>> //extends SupervisedOnlineClusteringMethod<T, BatchCluster<T>>
	{
	Kernel<T> kernel;
	double threshold;
	private Collection<SVMExample> examples;

	// I had wanted C and epsilon to be parameters only of the SMOTrainer; but the error terms embedded in the SVMExamples depend on at least C
	private double C = 1000;
	private double epsilon = 0.000001;


	public double classify(T item)
		{
		double delta = 0;
		for (SVMExample entry : examples)//Iterator i = itemAlphas().iterator(); i.hasNext();)
			{
			if (entry.alpha != 0)
				{
				delta += entry.alpha * kernel.evaluate(entry.example, item);
				}
			}
		return delta - threshold;
		}


	public ClassificationSVM(Map<T, Double> examples, Kernel<T> kernel)
		{
		this.kernel = kernel;
		SMOTrainer smot = new SMOTrainer();
		smot.trainModel(examples);
		}


	private class SMOTrainer
		{
		//	private double C = 1000;
		//	private double epsilon = 0.000001;


		private void setupExamples(Map<T, Double> target)
			{
			examples = new HashSet<SVMExample>();
			for (Map.Entry<T, Double> entry : target.entrySet())
				{
				examples.add(new SVMExample(entry.getKey(), entry.getValue()));
				}
			}


		public void trainModel(Map<T, Double> target)//TrainingListener l)
			{
			//	SMOTrainingContext trainingContext = new SMOTrainingContext(target, kernel, l);
			setupExamples(target);


			int numChanged = 0;
			boolean examineAll = true;

			while (numChanged > 0 || examineAll)
				{
				numChanged = 0;
				if (examineAll)
					{
					//System.out.println("Running full iteration");
					for (SVMExample example : examples)//int i = 0; i < trainingContext.size(); i++)
						{
						//System.out.println("Item " + i);
						numChanged += examineExample(example);
						//numChanged += examineExample(trainingContext, i);
						}
					}
				else
					{
					//System.out.println("Running non-bounds iteration");
					for (SVMExample example : examples)
						//				for (int i = 0; i < trainingContext.size(); i++)
						{
						if (!isBound(example))
							{
							//numChanged += examineExample(trainingContext, i);
							numChanged += examineExample(example);
							}
						}
					}
				if (examineAll)
					{
					examineAll = false;
					}
				else
					{
					examineAll = (numChanged == 0);
					}

				//	trainingCycleCompleted();
				}
			trainingCompleted();

			//			return trainingContext.getModel();
			}

		/*	public void trainingCycleCompleted()
			  {
			  cycle++;
			  if (listener != null)
				  {
				  listener.trainingCycleComplete(ourEvent);
				  }
			  }
  */
		public void trainingCompleted()
			{
			// drop zero-valued alpha mappings; keep only the support vectors

			Iterator<SVMExample> iter = examples.iterator();
			while (iter.hasNext())
				{
				SVMExample example = iter.next();
				if (MathUtils.equalWithinFPError(example.alpha, 0))
					{
					iter.remove();
					}
				}

			/*	if (listener != null)
			   {
			   listener.trainingComplete(ourEvent);
			   }*/
			}

		private int examineExample(SVMExample example)//SMOTrainingContext trainingContext, int i2)
			{
			//double y2 = example.target;//targets.get(example);  //trainingContext.getTarget(i2);
			//double alpha2 = example.alpha;//alphas.get(example);
			//double E2 = example.getError();
			double r2 = example.getError() * example.target;

			if ((r2 < -epsilon && example.alpha < C) || (r2 > epsilon && example.alpha > 0))
				{
				SVMExample secondChoice = null;
				double step = 0.0;
				//System.out.println("First choice heuristic");
				//for (int l = 0; l < trainingContext.size(); ++l)
				for (SVMExample example2 : examples)
					{
					if (!isBound(example2))
						{
						double thisStep = Math.abs(example2.getError() - example.getError());
						if (thisStep > step)
							{
							step = thisStep;
							secondChoice = example2;
							}
						}
					}

				if (secondChoice != null)
					{
					if (takeStep(secondChoice, example))
						{
						return 1;
						}
					}

				// PERF

				List<SVMExample> examplesInRandomOrder = new ArrayList<SVMExample>(examples);
				Collections.shuffle(examplesInRandomOrder);
				//System.out.println("Unbound");

				for (SVMExample example2 : examplesInRandomOrder)
					{
					if (!isBound(example2))
						{
						//	examples.remove(example2);  // worth the cost?  we'd have to use a LinkedList
						if (takeStep(example2, example))
							{
							return 1;
							}
						}
					}
				// The second pass should look at ALL alphas, but
				// we've already checked the non-bound ones.
				//System.out.println("Bound");
				for (SVMExample example2 : examplesInRandomOrder)
					{
					if (isBound(example2))// if we'd removed the unbound ones above we wouldn't have to test again
						{
						if (takeStep(example2, example))
							{
							return 1;
							}
						}
					}
				}
			else
				{
				//System.out.print("Nothing to optimize");
				}
			return 0;
			}


		private boolean takeStep(SVMExample example1, SVMExample example2)
			{
			// //System.out.print("+");

			if (example1 == example2)
				{
				return false;
				}

			double y1 = example1.target;
			double y2 = example2.target;
			double alpha1 = example1.alpha;
			double alpha2 = example2.alpha;
			double E1 = example1.getError();
			double E2 = example2.getError();
			double s = y1 * y2;

			double L, H;
			if (y2 != y1)/* preferred (s<0) */
				{
				// targets in opposite directions
				L = Math.max(0, alpha2 - alpha1);
				H = Math.min(C, C + alpha2 - alpha1);
				}
			else
				{
				// Equal targets.
				L = Math.max(0, alpha1 + alpha2 - C);
				H = Math.min(C, alpha1 + alpha2);
				}
			if (L == H)
				{
				////System.out.print("h");
				return false;
				}

			double k11 = kernel.evaluate(example1.example, example1.example);
			double k12 = kernel.evaluate(example1.example, example2.example);
			double k22 = kernel.evaluate(example2.example, example2.example);
			double eta = 2 * k12 - k11 - k22;

			double a1 = 0, a2 = 0;
			if (eta > 0 && eta < epsilon)
				{
				eta = 0.0;
				}

			if (eta < 0)
				{
				a2 = alpha2 - y2 * (E1 - E2) / eta;
				if (a2 < L)
					{
					a2 = L;
					}
				else if (a2 > H)
					{
					a2 = H;
					}
				}
			else
				{
				//System.out.println("Positive eta!");

				/*

								  double gamma = alpha1 + s*alpha2;
								  double v1 = model.classify(model.getVector(i1)) + model.getThreshold() - y1*alpha1*k11 - y2*alpha2*k12;
								  double v2 = model.classify(model.getVector(i2)) + model.getThreshold() - y1*alpha1*k12 - y2*alpha2*k22;

								  double Lobj = gamma - s * L + L - 0.5*k11*Math.pow(gamma - s*L,2) - 0.5*k22*Math.pow(L,2) - s*k12*(gamma-s*L)*L-y1*(gamma-s*L) - y1*(gamma - s*L)*v1 - y2*L*v2;
								  double Hobj = gamma - s * H + H - 0.5*k11*Math.pow(gamma - s*H,2) - 0.5*k22*Math.pow(H,2) - s*k12*(gamma-s*H)*H-y1*(gamma-s*H) - y1*(gamma - s*H)*v1 - y2*H*v2;
								  if (Lobj > Hobj+epsilon) {
									a2 = L;
								  } else if (Lobj < Hobj-epsilon) {
									a2 = H;
								  } else {
									a2 = alpha2;
								  }
								  */
				////System.out.print("+");
				return false;
				}

			a1 = alpha1 + s * (alpha2 - a2);
			if (Math.abs(a1 - alpha1) < epsilon * (a1 + alpha1 + 1 + epsilon))
				{
				//    //System.out.print("s");
				return false;
				}

			// Calculate new threshold

			double oldThreshold = threshold;//trainingContext.getThreshold();

			if (0 < a1 && a1 < C)
				{
				// use "b1 formula"
				// //System.out.println("b1");
				threshold = E1 + y1 * (a1 - alpha1) * k11 + y2 * (a2 - alpha2) * k12 + oldThreshold;
				}
			else if (0 < a2 && a2 < C)
				{
				// use "b2 formula"
				threshold = E2 + y1 * (a1 - alpha1) * k12 + y2 * (a2 - alpha2) * k22 + oldThreshold;
				// //System.out.println("b2");
				}
			else
				{
				// Both are at bounds -- use `half way' method.
				double b1, b2;
				b1 = E1 + y1 * (a1 - alpha1) * k11 + y2 * (a2 - alpha2) * k12 + oldThreshold;
				b2 = E2 + y1 * (a1 - alpha1) * k12 + y2 * (a2 - alpha2) * k22 + oldThreshold;
				// //System.out.println("hybrid");
				threshold = (b1 + b2) / 2.0;
				}

			example1.alpha = a1;
			example2.alpha = a2;

			// Update error cache

			example1.recomputeError();
			example2.recomputeError();

			for (SVMExample example3 : examples)
				{

				if (example3 == example1 || example3 == example2)
					{
					continue;
					}
				if (!isBound(example3))
					{
					example3.incrementError(y1 * (a1 - alpha1) * kernel.evaluate(example1.example, example3.example)
							+ y2 * (a2 - alpha2) * kernel.evaluate(example2.example, example3.example) + oldThreshold
							- threshold);
					}
				}

			return true;
			}
		}

	// BAD check weighting by example set size (libsvm?)

	private class SVMExample
		{
		T example;
		double target;
		double alpha;
		private double error;

		SVMExample(T key, Double value)
			{
			example = key;
			target = value;
			alpha = 0;
			error = 0;
			}

		void recomputeError()
			{
			error = classify(example) - target;
			}

		void incrementError(double delta)
			{
			error += delta;
			}

		double getError()
			{
			if (isBound(this))
				{
				recomputeError();
				}
			return error;
			}
		}

	private boolean isBound(SVMExample example)
		{
		return (example.alpha <= 0 || example.alpha >= C);
		}
	}

