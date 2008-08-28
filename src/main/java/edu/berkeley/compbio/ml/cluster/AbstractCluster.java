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

import com.davidsoergel.stats.DistributionException;
import com.davidsoergel.stats.Multinomial;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.log4j.Logger;

import java.util.Formatter;


/**
 * A cluster, i.e. a grouping of samples, generally learned during a clustering process.  Stores a centroid, an object
 * of the same type as the samples representing the location of the cluster.  Depending on the clustering algorithm, the
 * centroid may or may not be sufficient to describe the cluster (see AdditiveCluster); in the limit, a Cluster subclass
 * may simply store all the samples in the cluster.
 *
 * @author <a href="mailto:dev.davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public abstract class AbstractCluster<T extends Clusterable<T>> implements Cluster<T>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(AbstractCluster.class);

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
	protected int n = 0;

	/**
	 * The sum of the squared distances from samples in this cluster to the centroid
	 */
	protected double sumOfSquareDistances = 0;

	/**
	 * The unique integer identifier of this cluster
	 */
	private int id;


	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructs a new Cluster with the given DistanceMeasure and centroid.  Note the centroid may be modified in the
	 * course of running a clustering algorithm, so it may not be a good idea to provide a real data point here (i.e., it's
	 * probably best to clone it first).
	 *
	 * @param dm       the DistanceMeasure<T>
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
	 * Returns the centroid.
	 *
	 * @return
	 */
	public T getCentroid()
		{
		return centroid;
		}

	/**
	 * Sets the centroid.
	 *
	 * @param centroid the centroid
	 */
	public void setCentroid(T centroid)
		{
		this.centroid = centroid;
		}


	/**
	 * Returns the integer id for this cluster
	 *
	 * @return
	 */
	public int getId()
		{
		return id;
		}

	/**
	 * Sets the integer id for this cluster
	 *
	 * @param id the id
	 */
	public void setId(int id)
		{
		this.id = id;
		}

	/**
	 * Returns the number of samples in this cluster
	 *
	 * @return
	 */
	public int getN()
		{
		return n;
		}

	public void setN(int n)
		{
		this.n = n;
		}

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
	 * Sets the sum of squared distances from the samples in this cluster to its centroid.  Computed externally in {@see
	 * ClusteringMethod.computeClusterStdDevs}.
	 *
	 * @param v the sumOfSquareDistances
	 */
	public void setSumOfSquareDistances(double v)
		{
		sumOfSquareDistances = v;
		}

	// ------------------------ CANONICAL METHODS ------------------------

	/*
	 public Cluster<T> clone()
		 {
		 Cluster<T> result = new Cluster<T>(theDistanceMeasure, centroid);
		 result.addAll(this);
		 return result;
		 }
 */

	/**
	 * Returns a string representation of the object. In general, the <code>toString</code> method returns a string that
	 * "textually represents" this object. The result should be a concise but informative representation that is easy for a
	 * person to read. It is recommended that all subclasses override this method.
	 * <p/>
	 * The <code>toString</code> method for class <code>Object</code> returns a string consisting of the name of the class
	 * of which the object is an instance, the at-sign character `<code>@</code>', and the unsigned hexadecimal
	 * representation of the hash code of the object. In other words, this method returns a string equal to the value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	public String toString()
		{
		Formatter f = new Formatter();
		f.format("[Cluster %d] n=%d sd=%.2f", id, n, getStdDev());

		return f.out().toString();
		}

	/**
	 * Returns the standard deviation of the distances from each sample to the centroid, if this has already been computed.
	 * Can be used as a (crude?) measure of clusteredness, in combination with the distances between the cluster centroids
	 * themselves.  May return an obsolete value if the cluster centroid or members have been altered since the standard
	 * deviation was computed.
	 *
	 * @return the standard deviation of the distances from each sample to the centroid
	 */
	public double getStdDev()
		{
		return Math.sqrt(sumOfSquareDistances / n);
		}

	// -------------------------- OTHER METHODS --------------------------


	/**
	 * Increments the sum of square distances.  Note there can be no online method of updating the sum of squares, because
	 * the centroid keeps moving
	 */
	public void addToSumOfSquareDistances(double v)
		{
		sumOfSquareDistances += v;
		}

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
	public boolean equals(AbstractCluster<T> other)
		{
		boolean result = centroid.equals(other.getCentroid())
				// && theDistanceMeasure.equals(other.getTheDistanceMeasure())
				&& super.equals(other);
		logger.debug("" + this + " equals " + other + ": " + result);
		return result;
		}

	private Multiset<String> labelCounts = new HashMultiset<String>();

	// we let the label probabilities be completely distinct from the counts, so that the probabilities
	// can be set based on outside information (e.g., in the case of the Kohonen map, neighboring cells
	// may exert an influence)

	Multinomial<String> labelProbabilities = new Multinomial<String>();

	public Multiset<String> getLabelCounts()
		{
		return labelCounts;
		}

	public void updateLabelProbabilitiesFromCounts()//throws DistributionException
		{
		labelProbabilities = new Multinomial<String>();
		for (Multiset.Entry<String> o : labelCounts.entrySet())// too bad Bag isn't generic
			{
			try
				{
				labelProbabilities.put(o.getElement(), o.getCount());
				}
			catch (DistributionException e)
				{
				logger.debug(e);
				e.printStackTrace();
				throw new ClusterRuntimeException(e);
				}
			}
		//		labelProbabilities.normalize();  // don't bother, it'll be done on request anyway
		}


	public void setLabelProbabilities(Multinomial<String> labelProbabilities)
		{
		this.labelProbabilities = labelProbabilities;
		}

	public Multinomial<String> getLabelProbabilities() throws DistributionException
		{
		labelProbabilities.normalize();
		return labelProbabilities;
		}

	public double getDominantProbability() throws DistributionException
		{
		labelProbabilities.normalize();
		return labelProbabilities.getDominantProbability();
		}

	public String getDominantLabel()
		{
		return labelProbabilities.getDominantKey();
		}

	private int totalLabels = 0;

	public int getTotalLabels()
		{
		return totalLabels;
		}

	public void addLabel(T point)
		{
		totalLabels++;
		labelCounts.add(point.getLabel());
		}

	public void removeLabel(T point)
		{
		totalLabels--;
		// we don't sanity check that the label was present to begin with
		labelCounts.remove(point.getLabel());
		}
	}
