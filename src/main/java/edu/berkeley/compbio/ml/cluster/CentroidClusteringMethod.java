package edu.berkeley.compbio.ml.cluster;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface CentroidClusteringMethod<T extends Clusterable<T>> extends ClusteringMethod<T>
	{
	void computeClusterStdDevs(ClusterableIterator<T> theDataPointProvider) throws IOException;

//	List<T> getCentroids();

	String shortClusteringStats();

	String clusteringStats();

	void writeClusteringStatsToStream(OutputStream outf);
	}
