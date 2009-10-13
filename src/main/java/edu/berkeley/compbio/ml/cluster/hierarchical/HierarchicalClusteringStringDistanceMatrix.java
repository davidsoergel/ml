package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.OrderedPair;
import com.davidsoergel.dsutils.collections.Symmetric2dBiMapWithDefault;
import com.davidsoergel.dsutils.collections.UnorderedPair;
import edu.berkeley.compbio.ml.cluster.SimpleClusterable;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HierarchicalClusteringStringDistanceMatrix
		extends Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<SimpleClusterable<String>>, Float>
		implements Serializable
	{
	private static final long serialVersionUID = 1L;

	public HierarchicalClusteringStringDistanceMatrix()  // for custom deserialization
		{
		super(null);
		}

	public HierarchicalClusteringStringDistanceMatrix(final Float defaultValue)
		{
		super(defaultValue);
		}
/*
	public static Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<SimpleClusterable<String>>, Double> fromString(
			final Symmetric2dBiMapWithDefault<String, Double> stringDistanceMatrix)
		{
		Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<SimpleClusterable<String>>, Double> result =
				new Symmetric2dBiMapWithDefault<HierarchicalCentroidCluster<SimpleClusterable<String>>, Double>(
						stringDistanceMatrix.getDefaultValue());

		final AtomicInteger idCount = new AtomicInteger();

		Map<String, HierarchicalCentroidCluster<SimpleClusterable<String>>> clusters =
				createNewClusters(stringDistanceMatrix.getActiveKeys(), idCount);

		result.setMaxId(idCount.get());

		// insure that the empty lists get copied too
		for (Map.Entry<String, HierarchicalCentroidCluster<SimpleClusterable<String>>> entry : clusters.entrySet())
			{
			result.addKey(entry.getValue());
			}

		for (Map.Entry<UnorderedPair<String>, Double> entry : stringDistanceMatrix.entrySet())
			{
			final UnorderedPair<String> pair = entry.getKey();
			final String string1 = pair.getKey1();
			final String string2 = pair.getKey2();

			final HierarchicalCentroidCluster<SimpleClusterable<String>> cluster1 = clusters.get(string1);
			final HierarchicalCentroidCluster<SimpleClusterable<String>> cluster2 = clusters.get(string2);

			assert cluster1 != null;
			assert cluster2 != null;

			result.put(cluster1, cluster2, entry.getValue());
			}

		return result;
		}
	*/

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
		{
		defaultValue = (Float) (stream.readObject());
		List<String> keys = (List<String>) stream.readObject();

		final AtomicInteger idCount = new AtomicInteger();
		Map<String, HierarchicalCentroidCluster<SimpleClusterable<String>>> clusters = createNewClusters(keys, idCount);

		setMaxId(idCount.get());

		try
			{
			while (true)
				{
				int id1 = stream.read();
				int id2 = stream.read();
				float value = stream.readFloat();
				String string1 = keys.get(id1);
				String string2 = keys.get(id2);

				final HierarchicalCentroidCluster<SimpleClusterable<String>> cluster1 = clusters.get(string1);
				final HierarchicalCentroidCluster<SimpleClusterable<String>> cluster2 = clusters.get(string2);

				assert cluster1 != null;
				assert cluster2 != null;

				put(cluster1, cluster2, value);
				}
			}
		catch (EOFException e)
			{
			}
		}

	private static Map<String, HierarchicalCentroidCluster<SimpleClusterable<String>>> createNewClusters(
			final Collection<String> samples, final AtomicInteger idCount)
		{
		final Map<String, HierarchicalCentroidCluster<SimpleClusterable<String>>> newClusters =
				new ConcurrentHashMap<String, HierarchicalCentroidCluster<SimpleClusterable<String>>>();

		for (String sample : samples)
			{

			//assert new HashSet<String>(samples).size() == samples.size();

//		Parallel.forEach(samples, new Function<String, Void>()
//		{
//		public Void apply(final String sample)
//			{

			final HierarchicalCentroidCluster<SimpleClusterable<String>> c =
					new HierarchicalCentroidCluster<SimpleClusterable<String>>(idCount.getAndIncrement(),
					                                                           new SimpleClusterable<String>(sample));
			c.doneLabelling();
			assert newClusters.get(sample) == null;
			newClusters.put(sample, c);
			//theActiveNodeDistanceMatrix.addKey(c);
			//addCluster(c);
//			return null;
			}
//		});
		assert newClusters.size() == samples.size();
		return newClusters;
		}

	private void writeObject(ObjectOutputStream stream) throws IOException
		{
		stream.writeObject(defaultValue);

		// establish a canonical order for the keys

		List<String> keys = new ArrayList<String>();
		for (HierarchicalCentroidCluster<SimpleClusterable<String>> cluster : getActiveKeys())
			{
			keys.add(cluster.getCentroid().getId());
			}
		Collections.sort(keys);
		stream.writeObject(keys);

		// store the pairs using integer indexes
		for (OrderedPair<UnorderedPair<HierarchicalCentroidCluster<SimpleClusterable<String>>>, Float> entry : keyPairToValueSorted
				.getSortedPairs())
			{
			UnorderedPair<HierarchicalCentroidCluster<SimpleClusterable<String>>> idPair = entry.getKey1();
			HierarchicalCentroidCluster<SimpleClusterable<String>> cluster1 = idPair.getKey1();
			HierarchicalCentroidCluster<SimpleClusterable<String>> cluster2 = idPair.getKey2();
			String key1 = cluster1.getCentroid().getId();
			String key2 = cluster2.getCentroid().getId();
			int id1 = keys.indexOf(key1);
			int id2 = keys.indexOf(key2);
			float value = entry.getKey2();
			stream.write(id1);
			stream.write(id2);
			stream.writeFloat(value);
			}
		}
	}
