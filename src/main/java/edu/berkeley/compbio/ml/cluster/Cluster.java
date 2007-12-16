/* $Id$ */

/*
 * Copyright (c) 2007 Regents of the University of California
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
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import edu.berkeley.compbio.ml.distancemeasure.DistanceMeasure;
import org.apache.log4j.Logger;

import java.util.Formatter;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class Cluster<T extends Clusterable<T>>
	{
	// ------------------------------ FIELDS ------------------------------

	private static Logger logger = Logger.getLogger(Cluster.class);
	protected DistanceMeasure<T> theDistanceMeasure;

	protected T centroid;
	protected int n = 0;
	protected double sumOfSquareDistances = 0;

	private int id;


	// --------------------------- CONSTRUCTORS ---------------------------

	public Cluster(DistanceMeasure<T> dm, T centroid)
		{
		this.centroid = centroid;//.clone();
		n++;
		//add(centroid);
		logger.debug("Created cluster with centroid: " + centroid);
		theDistanceMeasure = dm;
		}

	// --------------------- GETTER / SETTER METHODS ---------------------

	public T getCentroid()
		{
		return centroid;
		}

	public void setCentroid(T centroid)
		{
		this.centroid = centroid;
		}


	public int getId()
		{
		return id;
		}

	public void setId(int id)
		{
		this.id = id;
		}

	public int getN()
		{
		return n;
		}

	/*	public void normalize()
	   {
	   centroid.normalize(n);
	   }*/

	public DistanceMeasure<T> getTheDistanceMeasure()
		{
		return theDistanceMeasure;
		}

	public void setTheDistanceMeasure(DistanceMeasure<T> theDistanceMeasure)
		{
		this.theDistanceMeasure = theDistanceMeasure;
		}

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

	public String toString()
		{
		Formatter f = new Formatter();
		f.format("\n[Cluster %d] n=%d sd=%.2f", id, n, getStdDev());

		return f.out().toString();
		}

	public double getStdDev()
		{
		return Math.sqrt(sumOfSquareDistances / n);
		}

	// -------------------------- OTHER METHODS --------------------------


	/**
	 * Note there can be no online method of updating the sum of squares, because the centroid keeps moving
	 */
	public void addToSumOfSquareDistances(double v)
		{
		sumOfSquareDistances += v;
		}

	public double distanceToCentroid(T p)
		{
		return theDistanceMeasure.distanceFromTo(centroid, p);
		}

	public boolean equals(Cluster<T> other)
		{
		boolean result = centroid.equals(other.getCentroid())
				&& theDistanceMeasure.equals(other.getTheDistanceMeasure()) && super.equals(other);
		logger.debug("" + this + " equals " + other + ": " + result);
		return result;
		}

	public abstract boolean recenterByAdding(T point);

	public abstract boolean recenterByRemoving(T point);
	}
