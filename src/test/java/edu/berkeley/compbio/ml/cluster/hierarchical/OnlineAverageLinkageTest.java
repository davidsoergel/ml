/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.math.MathUtils;
import com.davidsoergel.trees.TreePrinter;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;
import edu.berkeley.compbio.ml.cluster.Cluster;
import edu.berkeley.compbio.ml.cluster.ClusterableDoubleArray;
import edu.berkeley.compbio.ml.cluster.ClusterableIterator;
import edu.berkeley.compbio.ml.cluster.ClusterableIteratorFactory;
import edu.berkeley.compbio.ml.distancemeasure.EuclideanDistance;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class OnlineAverageLinkageTest
	{
// ------------------------------ FIELDS ------------------------------

	private static final Logger logger = Logger.getLogger(OnlineAverageLinkageTest.class);

	final List<ClusterableDoubleArray> points = new ArrayList<ClusterableDoubleArray>();


// -------------------------- OTHER METHODS --------------------------

	@BeforeSuite
	public void setUp()
		{
		MathUtils.initApproximateLog(-12, +12, 3, 100000);
		points.add(new ClusterableDoubleArray("1", new double[]{1, 2, 3, 4, 5}));
		points.add(new ClusterableDoubleArray("2", new double[]{1, 2, 3, 4, 6}));
		points.add(new ClusterableDoubleArray("3", new double[]{10, 20, 3, 4, 5}));
		points.add(new ClusterableDoubleArray("4", new double[]{10, 20, 30, 4, 5}));
		points.add(new ClusterableDoubleArray("5", new double[]{10, 20, 30, 40, 5}));
		points.add(new ClusterableDoubleArray("6", new double[]{10, 20, 30, 40, 50}));
		for (ClusterableDoubleArray point : points)
			{
			point.doneLabelling();
			}
		}

	@Test
	public void testSimilarPointsClusterTogether() throws CloneNotSupportedException, IOException
		{
		ClusterableIterator ci;

		//ci = new MockClusterableIterator().init();

		//	final Object distanceMatrix;
		final OnlineAgglomerativeClustering<ClusterableDoubleArray> oc =
				new OnlineAgglomerativeClustering<ClusterableDoubleArray>(EuclideanDistance.getInstance(), null,
				                                                               null, null, null, 0.001,
				                                                               new AverageLinkageAgglomerator());

		ClusterableIterator<ClusterableDoubleArray> clusterableIterator = new ClusterableIteratorFactory<ClusterableDoubleArray>(points).next();
		//oc.addAll(clusterableIterator);

//oc.getDistanceMatrix();
		oc.train(clusterableIterator );

		final Collection<? extends CentroidCluster<ClusterableDoubleArray>> theClusters = oc.getClusters();


		for (final Cluster<ClusterableDoubleArray> c : theClusters)
			{
			logger.debug(c);
			}

		oc.writeAssignmentsAsTextToStream(System.err);

		//assert oc.getTree().getValue().getN() == 6;
		final HierarchicalCentroidCluster<ClusterableDoubleArray> root = oc.getTree();
		logger.info("\n" + TreePrinter.prettyPrint(root));

		final double largestLengthSpan = oc.getTree().getLargestLengthSpan();
		//logger.info("found n = " + oc.getN());
		//logger.info("found num clusters = " + theClusters.size());
		logger.info("found largestLengthSpan = " + largestLengthSpan);
		//assert oc.getN() == 6;
		//assert theClusters.size() == 11;
		assert MathUtils.equalWithinFPError(largestLengthSpan, 108.56383323669434);
		//assert MathUtils.equalWithinFPError(largestLengthSpan, 89.15612888336182);
		}
	}
