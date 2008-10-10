/*
 * Copyright (c) 2001-2008 David Soergel
 * 418 Richmond St., El Cerrito, CA  94530
 * dev@davidsoergel.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of any contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package edu.berkeley.compbio.ml.cluster;

import com.davidsoergel.dsutils.collections.HashWeightedSet;
import com.davidsoergel.dsutils.collections.WeightedSet;
import org.apache.log4j.Logger;

import java.util.Formatter;


/**
 * A cluster, i.e. a grouping of samples, generally learned during a clustering process.  Stores a centroid, an object
 * of the same type as the samples representing the location of the cluster.  Depending on the clustering algorithm, the
 * centroid may or may not be sufficient to describe the cluster (see AdditiveCluster); in the limit, a Cluster subclass
 * may simply store all the samples in the cluster.
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractCluster<T extends Clusterable<T>> implements Cluster<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(AbstractCluster.class);

	/**
	 * The distance measure to use for computing distances from samples to the centroid of this cluster
	 */
	//	protected DistanceMeasure<T> theDistanceMeasure;

	/**
	 * Field centroid
	 */
	protected T centroid;

	/**
	 * The number of samples in this cluster
	 */
	//protected int n = 0;

	/**
	 * The sum of the squared distances from samples in this cluster to the centroid
	 */
	protected double sumOfSquareDistances = 0;

	/**
	 * The unique integer identifier of this cluster
	 */
	private int id;

	//private Multiset<String> exclusiveLabelCounts = new HashMultiset<String>();


	protected WeightedSet<String> weightedLabels = new HashWeightedSet<String>();

	// we let the label probabilities be completely distinct from the local weights themselves, so that the probabilities
	// can be set based on outside information (e.g., in the case of the Kohonen map, neighboring cells
	// may exert an influence)

	private WeightedSet<String> derivedLabelProbabilities = new HashWeightedSet<String>();
	//	new Multinomial<String>();

	//private int totalLabels = 0;


	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new Cluster with the given id and centroid.  Note the centroid may be modified in the course of running
	 * a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e., it's probably best to
	 * clone it first).
	 *
	 * @param id       an integer uniquely identifying this cluster
	 * @param centroid the T
	 */
	public AbstractCluster(int id, T centroid)//DistanceMeasure<T> dm
		{
		this.centroid = centroid;//.clone();
		this.id = id;
		//n++;
		//add(centroid);
		logger.debug("Created cluster with centroid: " + centroid);
		//theDistanceMeasure = dm;
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * {@inheritDoc}
	 */
	public T getCentroid()
		{
		return centroid;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setCentroid(T centroid)
		{
		this.centroid = centroid;
		}

	/**
	 * {@inheritDoc}
	 */
	public int getId()
		{
		return id;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setId(int id)
		{
		this.id = id;
		}

	/*	public Multiset<String> getExclusiveLabelCounts()
	   {
	   return exclusiveLabelCounts;
	   }*/

	/**
	 * {@inheritDoc}
	 */
	public WeightedSet<String> getDerivedLabelProbabilities()//throws DistributionException
		{
		//derivedWeightedLabels.normalize();
		return derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public void setDerivedLabelProbabilities(WeightedSet<String> derivedLabelProbabilities)
		{
		this.derivedLabelProbabilities = derivedLabelProbabilities;
		}

	/**
	 * {@inheritDoc}
	 */
	public int getN()
		{
		return weightedLabels.getItemCount();
		}

	/**
	 * {@inheritDoc}
	 */
	/*	public void setN(int n)
	   {
	   this.n = n;
	   }*/

	/*	public int getTotalLabels()
	   {
	   return totalLabels;
	   }*/

	/*	public void normalize()
		   {
		   centroid.normalize(n);
		   }*/

	/**
	 * Returns the DistanceMeasure used for computing distances from samples to this cluster
	 *
	 * @return
	 */
	/*	public DistanceMeasure<T> getTheDistanceMeasure()
	   {
	   return theDistanceMeasure;
	   }*/

	/**
	 * Sets the theDistanceMeasure used for computing distances from samples to this cluster
	 *
	 * @param theDistanceMeasure the theDistanceMeasure
	 */
	/*	public void setTheDistanceMeasure(DistanceMeasure<T> theDistanceMeasure)
	   {
	   this.theDistanceMeasure = theDistanceMeasure;
	   }*/

	/**
	 * {@inheritDoc}
	 */
	public void setSumOfSquareDistances(double v)
		{
		sumOfSquareDistances = v;
		}

	// ------------------------ CANONICAL METHODS ------------------------

	/**
	 * Computes the distance from the given point to the centroid of this cluster, using the distance measure previously
	 * assigned to this cluster.
	 *
	 * @param p the point
	 * @return the distance
	 */
	/*	public double distanceToCentroid(T p)
	   {
	   return theDistanceMeasure.distanceFromTo(p, centroid);
	   }*/

	// premature optimization
	/*	public double distanceToCentroid(T p, double distanceToBeat)
	   {
	   return theDistanceMeasure.distanceFromTo(p, centroid, distanceToBeat);
	   }*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other)
		{
		// don't worry about matching the generic type; centroid.equals will take care of that
		if (other instanceof AbstractCluster)
			{
			boolean result = centroid.equals(((AbstractCluster<T>) other).getCentroid())
					// && theDistanceMeasure.equals(other.getTheDistanceMeasure())
					&& super.equals(other);
			logger.debug("" + this + " equals " + other + ": " + result);
			return result;
			}
		return false;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
		{
		int result = super.hashCode();
		result = 31 * result + (centroid != null ? centroid.hashCode() : 0);
		return result;
		}

	/*
	 public Cluster<T> clone()
		 {
		 Cluster<T> result = new Cluster<T>(theDistanceMeasure, centroid);
		 result.addAll(this);
		 return result;
		 }
 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
		{
		Formatter f = new Formatter();
		f.format("[Cluster %d] n=%d sd=%.2f", id, getN(), getStdDev());

		return f.out().toString();
		}

	/**
	 * {@inheritDoc}
	 */
	public double getStdDev()
		{
		return Math.sqrt(sumOfSquareDistances / getN());
		}

	// ------------------------ INTERFACE METHODS ------------------------


	// --------------------- Interface Cluster ---------------------


	/**
	 * {@inheritDoc}
	 */
	public void updateDerivedWeightedLabelsFromLocal()//throws DistributionException
		{
		derivedLabelProbabilities = new HashWeightedSet<String>();
		derivedLabelProbabilities.addAll(weightedLabels);
		/*for (Multiset.Entry<String> o : exclusiveLabelCounts.entrySet())// too bad Bag isn't generic
			{
			try
				{
				exclusiveLabelProbabilities.put(o.getElement(), o.getCount());
				}
			catch (DistributionException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new ClusterRuntimeException(e);
				}
			}*/
		//derivedLabelProbabilities.normalize();  // don't bother, it'll be done on request anyway
		}

	/**
	 * {@inheritDoc}
	 */
	/*	public double getDominantProbability() throws DistributionException
	   {
	   exclusiveLabelProbabilities.normalize();
	   return exclusiveLabelProbabilities.getDominantProbability();
	   }*/

	/**
	 * {@inheritDoc}
	 */
	/*	public String getDominantExclusiveLabel()
	   {
	   return exclusiveLabelProbabilities.getDominantKey();
	   }*/

	/**
	 * {@inheritDoc}
	 */
	/*	public void addExclusiveLabel(T point)
	   {
	   totalLabels++;
	   exclusiveLabelCounts.add(point.getExclusiveLabel());
	   weightedLabels.addAll(point.getWeightedLabels());
	   }*/

	/**
	 * {@inheritDoc}
	 */
	/*	public void removeExclusiveLabel(T point)
	   {
	   totalLabels--;
	   // we don't sanity check that the label was present to begin with
	   exclusiveLabelCounts.remove(point.getExclusiveLabel());
	   weightedLabels.removeAll(point.getWeightedLabels());
	   }*/

	/**
	 * {@inheritDoc}
	 */
	public void addToSumOfSquareDistances(double v)
		{
		sumOfSquareDistances += v;
		}

	public WeightedSet<String> getWeightedLabels()
		{
		return weightedLabels;
		}
	}
