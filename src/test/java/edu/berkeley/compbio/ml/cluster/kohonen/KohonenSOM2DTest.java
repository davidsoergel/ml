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

package edu.berkeley.compbio.ml.cluster.kohonen;

import com.davidsoergel.dsutils.GenericFactory;
import com.davidsoergel.dsutils.GenericFactoryException;
import com.davidsoergel.stats.DissimilarityMeasure;
import com.davidsoergel.stats.SimpleFunction;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.ClusterException;
import edu.berkeley.compbio.ml.cluster.ClusterableDoubleArray;
import edu.berkeley.compbio.ml.cluster.NoGoodClusterException;
import edu.berkeley.compbio.ml.distancemeasure.EuclideanDistance;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public class KohonenSOM2DTest
	{
	private BruteForceSearchStrategy<ClusterableDoubleArray> bruteForceStrategy =
			new BruteForceSearchStrategy<ClusterableDoubleArray>();

	SimpleFunction radiusFunction = new SimpleFunction()
	{
	public double f(double x)
		{
		return 15 - x;
		}
	};

	SimpleFunction moveFactorFunction = new SimpleFunction()
	{
	public double f(double x)
		{
		return 1;
		}
	};

	SimpleFunction weightFunction = new SimpleFunction()
	{
	public double f(double x)
		{
		return 1.1 - x;
		}
	};

	DissimilarityMeasure<ClusterableDoubleArray> dm = new EuclideanDistance();

	@Test
	public void initialTrainingSampleAltersAllCells()
			throws ClusterException, NoGoodClusterException, GenericFactoryException
		{
		ClusterableDoubleArray prototype = new ClusterableDoubleArray("test1", new double[]{
				0,
				0,
				0,
				0,
				0
		});
		KohonenSOM2D<ClusterableDoubleArray> som = new KohonenSOM2D<ClusterableDoubleArray>(new Integer[]{
				10,
				10
		}, dm, moveFactorFunction, radiusFunction, weightFunction, false, true, 1, bruteForceStrategy);

		som.initializeWithRealData(null, 0, prototypeFactory);
		som.add(new ClusterableDoubleArray("test1", new double[]{
				1,
				2,
				3,
				4,
				5
		}), null);

		for (CentroidCluster<ClusterableDoubleArray> cell : som.getClusters())
			{
			assert cell.getCentroid() != null;
			assert !cell.getCentroid().equalValue(prototype);
			}
		}

	@Test
	public void secondTrainingSampleMatchesAppropriateCell()
			throws ClusterException, NoGoodClusterException, GenericFactoryException
		{

		KohonenSOM2D<ClusterableDoubleArray> som = new KohonenSOM2D<ClusterableDoubleArray>(new Integer[]{
				10,
				10
		}, dm, moveFactorFunction, radiusFunction, weightFunction, false, true, 1, bruteForceStrategy);

		som.initializeWithRealData(null, 0, prototypeFactory);
		som.add(new ClusterableDoubleArray("test1", new double[]{
				1,
				2,
				3,
				4,
				5
		}), null);

		assert som.bestClusterMove(new ClusterableDoubleArray("test1", new double[]{
				0,
				0,
				0,
				0,
				0
		})).bestCluster != som.getClusters().iterator().next();// the first cell in the list, at (0,0)
		}

	@Test
	public void WeightedMaskMakesRadiusZeroCircle()
		{
		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				10,
				10
		}, null, null, null, null, false, false, 0, bruteForceStrategy);

		KohonenSOM2D.WeightedMask mask = som.getWeightedMask(0);

		assert mask.numCells == 1;
		}

	@Test
	public void WeightedMaskMakesRadiusOneCircle()
		{

		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				10,
				10
		}, null, null, null, null, false, false, 0, bruteForceStrategy);

		KohonenSOM2D.WeightedMask mask = som.getWeightedMask(1);

		assert mask.numCells == 5;
		}


	@Test
	public void WeightedMaskMakesRadiusTwoCircle()
		{

		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				10,
				10
		}, null, null, null, null, false, false, 0, bruteForceStrategy);

		KohonenSOM2D.WeightedMask mask = som.getWeightedMask(2);

		assert mask.numCells == 21;
		}

	@Test
	public void WeightedMaskMakesRadiusTwentyCircle()
		{

		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				100,
				100
		}, null, null, null, null, false, false, 0, bruteForceStrategy);

		KohonenSOM2D.WeightedMask mask = som.getWeightedMask(20);

		assert mask.numCells == 1303;
		}

	@Test
	public void WeightedMaskMakesRadiusTenCircle()
		{

		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				100,
				100
		}, null, null, null, null, false, false, 0, bruteForceStrategy);

		KohonenSOM2D.WeightedMask mask = som.getWeightedMask(10);

		assert mask.numCells == 345;
		}

	@Test
	public void WeightedMaskCellsAreUnique()
		{

		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				100,
				100
		}, null, null, null, null, false, false, 0, bruteForceStrategy);

		KohonenSOM2D.WeightedMask mask = som.getWeightedMask(20);

		Set<XYPair> pairSet = new HashSet<XYPair>();
		for (int i = 0; i < mask.numCells; i++)
			{
			XYPair pair = new XYPair(mask.deltaX[i], mask.deltaY[i]);
			assert !pairSet.contains(pair);
			pairSet.add(pair);
			}
		}

	GenericFactory<ClusterableDoubleArray> prototypeFactory = new GenericFactory<ClusterableDoubleArray>()
	{
	public ClusterableDoubleArray create(Object... constructorArguments) throws GenericFactoryException
		{
		return new ClusterableDoubleArray("test1", new double[]{
				0,
				0,
				0,
				0,
				0
		});
		}

	public Class getCreatesClass()
		{
		return ClusterableDoubleArray.class;
		}
	};

	@Test
	public void averageDistanceToNeighboringCellsIsComputedCorrectly() throws GenericFactoryException
		{
		KohonenSOM2D som = new KohonenSOM2D(new Integer[]{
				5,
				5
		}, dm, null, null, null, false, false, 0, bruteForceStrategy);


		/*ClusterableDoubleArray zeroArray = new ClusterableDoubleArray("test1", new double[]{
						0,
						0,
						0,
						0
				});

				for (int x = 0; x < 5; x++)
				   {
				   for (int y = 0; y < 5; y++)
					   {
					   som.clusterAt(x, y).setCentroid(zeroArray);
					   }
				   }*/
		som.initializeWithRealData(null, 0, prototypeFactory);

		som.clusterAt(2, 2).setCentroid(new ClusterableDoubleArray("test1", new double[]{
				1,
				1,
				1,
				1,
				1
		}));

		double[] avgDist = som.computeCellAverageNeighborDistances();

		assert Arrays.equals(avgDist, new double[]{
				//
				0,
				0,
				0,
				0,
				0,
				//
				0,
				0,
				Math.sqrt(5) / 4.,
				0,
				0,
				//
				0,
				Math.sqrt(5) / 4.,
				Math.sqrt(5),
				Math.sqrt(5) / 4.,
				0,
				//
				0,
				0,
				Math.sqrt(5) / 4.,
				0,
				0,
				//
				0,
				0,
				0,
				0,
				0
		});

		// ** test edges
		}

	private class XYPair
		{
		int x, y;

		private XYPair(int x, int y)
			{
			this.x = x;
			this.y = y;
			}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o)
			{
			if (this == o)
				{
				return true;
				}
			if (!(o instanceof XYPair))
				{
				return false;
				}

			XYPair xyPair = (XYPair) o;

			if (x != xyPair.x)
				{
				return false;
				}
			if (y != xyPair.y)
				{
				return false;
				}

			return true;
			}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
			{
			int result;
			result = x;
			result = 31 * result + y;
			return result;
			}
		}
	}
