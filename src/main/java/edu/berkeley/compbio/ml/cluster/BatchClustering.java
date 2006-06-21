package edu.berkeley.compbio.ml.cluster;

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public abstract class BatchClustering<T extends Clusterable<T>>
	{
/*	private static Logger logger = Logger.getLogger(BatchClustering.class);

	private Set<T> theDataPoints;

	Set<Cluster<T>> theClusters;

	public BatchClustering(Set<T> dataPointSet)
		{
		theDataPoints = dataPointSet;
		}

	public BatchClustering(Set<Cluster<T>> preexistingClusters, Set<T> newDataPointsToAdd)
		{
		theClusters = preexistingClusters;
		// TODO
		}

	//public abstract void run(int iterations);

	public Set<Cluster<T>> getClusters()
		{
		return theClusters;
		}

private void rerunExisting(int steps)
		{
		// going through these in cluster order sucks...
		while (steps > 0)
			{
			for (Cluster<T> c : theClusters)
				{
				for (T t : new HashSet<T>(c))
					{
					if (c.size() == 1)
						{
						steps--;
						continue;
						}
					c.removeAndRecenter(t);
					addAndRecenter(t);
					if (--steps < 0)
						{
						return;
						}
					}
				}
			}
		}

		public void reassignAll()
		{
			for (Cluster<T> c : theClusters)
				{
				for (T t : new HashSet<T>(c))
					{
					c.remove(t);
					add(t);
					}
				}

		}


	public void writeTextToStream(OutputStream out)
		{

		PrintWriter p = new PrintWriter(out);

		for (Cluster<T> c : theClusters)
			{
			p.println("<cluster id=\"" + c.getId() + "\" centroid=\"" + c.getCentroid() + "\">");
			for (T t : c)
				{
				p.println("\t" + t);
				}
			p.println("</cluster>");
			}
		p.flush();
		}


	public void run(Iterator<T> theDataPointProvider, int steps)
		{
		for (int i = 0; i < steps; i++)
			{
			if (!theDataPointProvider.hasNext())
				{
				//rerunExisting(steps - i);
				return;
				}
			addAndRecenter(theDataPointProvider.next());
			}
		}*/
	}
