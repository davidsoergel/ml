package edu.berkeley.compbio.ml.cluster;

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class BatchKmeansClustering<T extends Clusterable<T>> extends BatchClustering<T>
	{
/*	private static Logger logger = Logger.getLogger(BatchKmeansClustering.class);

	private int k;

	public BatchKmeansClustering(Set<T> dataPointSet)
		{
		super(dataPointSet);
		}

	public BatchKmeansClustering(Set<T> dataPointSet, int k, DistanceMeasure<T> dm)
		{
		super(dataPointSet);
		this.k = k;

		for (int i = 0; i < k; i++)
			{
			Cluster<T> c = new Cluster<T>(dm, da.next()); // initialize the clusters with the first k points
			theClusters.add(c);
			}
		}


	public boolean batchUpdate() throws ClusterException
		{
		List<Cluster<T>> oldClusters = theClusters;
		theClusters = new ArrayList<Cluster<T>>();

		// what if a cluster ends up empty??

		for (Cluster<T> c : oldClusters)
			{
			Cluster<T> n = new Cluster<T>(c.getTheDistanceMeasure(), c.getCentroid());
			logger.debug(theClusters.add(n));  // this just fails when theClusters is a HashSet. ???
			assert theClusters.contains(n);
			//c.clear();
			//new Cluster<T>(c.getTheDistanceMeasure(), c.getCentroid()));
			}
		for (Cluster<T> o : oldClusters)
			{
			for (T point : o)
				{
				bestCluster(theClusters, point).add(point);
				}
			}
		boolean changed = false;
		for (Cluster<T> o : theClusters)
			{
			changed = changed || o.recalculateCentroid();
			}
		return changed;
		}
*/
	}
