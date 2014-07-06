/*
 * Copyright (c) 2006-2013  David Soergel  <dev@davidsoergel.com>
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.berkeley.compbio.ml.cluster.hierarchical;

import com.davidsoergel.dsutils.collections.DSCollectionUtils;
import com.davidsoergel.dsutils.collections.IndexedSymmetric2dBiMapWithDefault;
import com.davidsoergel.dsutils.collections.InsertionTrackingSet;
import com.davidsoergel.dsutils.collections.OrderedPair;
import com.davidsoergel.dsutils.collections.SortedSymmetric2dBiMap;
import com.davidsoergel.dsutils.collections.SortedSymmetric2dBiMapWithDefault;
import com.davidsoergel.dsutils.collections.UnorderedPair;
import com.davidsoergel.dsutils.collections.UnorderedPairIterator;
import edu.berkeley.compbio.ml.cluster.SimpleClusterable;
import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class HierarchicalClusteringStringDistanceMatrix
		extends IndexedSymmetric2dBiMapWithDefault<HierarchicalCentroidCluster<SimpleClusterable<String>>, Float>
		implements Serializable, SortedSymmetric2dBiMap<HierarchicalCentroidCluster<SimpleClusterable<String>>, Float>
	{
	private static final Logger logger = Logger.getLogger(HierarchicalClusteringStringDistanceMatrix.class);

	private static final long serialVersionUID = 6L;

	public HierarchicalClusteringStringDistanceMatrix()  // for custom deserialization
	{
	super();
	//super(null);
	}

	public HierarchicalClusteringStringDistanceMatrix(final Float defaultValue)
		{
		super(defaultValue);
		}

	public HierarchicalClusteringStringDistanceMatrix(final Float defaultValue,
	                                                  final InsertionTrackingSet<HierarchicalCentroidCluster<SimpleClusterable<String>>> keys)
		{
		super(defaultValue, keys);
		}

	public HierarchicalClusteringStringDistanceMatrix copyDefaultAndKeys()
		{
		return new HierarchicalClusteringStringDistanceMatrix(getDefaultValue(), keys);
		}

	public HierarchicalClusteringStringDistanceMatrix(HierarchicalClusteringStringDistanceMatrix cloneFrom,
	                                                  boolean deepCopy)
		{
		setDefaultValue(cloneFrom.getDefaultValue());

		// map the old clusters to new clusters, maintaining the indexes

		keys = new InsertionTrackingSet<HierarchicalCentroidCluster<SimpleClusterable<String>>>();
		final Collection<HierarchicalCentroidCluster<SimpleClusterable<String>>> oldClusters = cloneFrom.getKeys();
		for (HierarchicalCentroidCluster<SimpleClusterable<String>> oldCluster : oldClusters)
			{
			String name = oldCluster.getCentroid().getId();
			final Integer id = cloneFrom.keys.indexOf(oldCluster);

			SimpleClusterable<String> simpleClusterable = new SimpleClusterable<String>(name);
			simpleClusterable.getMutableWeightedLabels().add(name, 1.0, 1);
			simpleClusterable.doneLabelling();

			final HierarchicalCentroidCluster<SimpleClusterable<String>> c =
					new HierarchicalCentroidCluster<SimpleClusterable<String>>(id, simpleClusterable);
			c.doneLabelling();

			keys.put(c, id);
			}

		if (deepCopy)
			{
			// clone the value table
			underlyingIntMap = new SortedSymmetric2dBiMapWithDefault<Integer, Float>(cloneFrom.underlyingIntMap);
			sanityCheck();  // keeping these separate makes the stack trace reveal which branch we're in when an error occurs
			}
		else
			{
			// this is a performance improvement for cases where we don't care about modifying the original.
			// It's dangerous though, use with caution
			underlyingIntMap = cloneFrom.underlyingIntMap;
			sanityCheck();
			}
		}


	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
		{
		setDefaultValue((Float) (stream.readObject()));
		InsertionTrackingSet<String> keyStrings = (InsertionTrackingSet<String>) stream.readObject();

		// map the strings to new clusters, maintaining the indexes

		keys = new InsertionTrackingSet<HierarchicalCentroidCluster<SimpleClusterable<String>>>();
		for (String name : keyStrings)
			{
			final Integer id = keyStrings.indexOf(name);
			SimpleClusterable<String> simpleClusterable = new SimpleClusterable<String>(name);
			simpleClusterable.getMutableWeightedLabels().add(name, 1.0, 1);
			simpleClusterable.doneLabelling();
			final HierarchicalCentroidCluster<SimpleClusterable<String>> c =
					new HierarchicalCentroidCluster<SimpleClusterable<String>>(id, simpleClusterable);
			//	c.getMutableWeightedLabels().add(name, 1.0, 1);
			c.doneLabelling();
			keys.put(c, id);
			}

		int count = 0;

		try
			{
			while (true)
				{
				int id1 = stream.readInt();
				int id2 = stream.readInt();
				float value = stream.readFloat();
				underlyingIntMap.put(id1, id2, value);
				count++;
				/*	if (count % 1000 == 0)
				   {
				   logger.info("Loading distance matrix from cache: " + count + " pairs between " + keys.size()
							   + " keys.");
				   }*/
				}
			}
		catch (EOFException e)
			{
			}
		underlyingIntMap.addKeys(keys.getIndexes());
		logger.info("Loaded distance matrix from cache: " + keys.size() + " keys, " + count + " pairs");
		}

	private void writeObject(ObjectOutputStream stream) throws IOException
		{
		stream.writeObject(getDefaultValue());

		// map the clusters to their strings, maintaining the indexes

		InsertionTrackingSet<String> keyStrings = new InsertionTrackingSet<String>();
		for (HierarchicalCentroidCluster<SimpleClusterable<String>> cluster : keys)
			{
			keyStrings.put(cluster.getCentroid().getId(), keys.indexOf(cluster));
			}
		stream.writeObject(keyStrings);

		// store the pairs using integer indexes
		for (OrderedPair<UnorderedPair<Integer>, Float> entry : underlyingIntMap.keyPairToValueSorted.getSortedPairs())
			{
			UnorderedPair<Integer> idPair = entry.getKey1();
			int id1 = idPair.getKey1();
			int id2 = idPair.getKey2();
			float value = entry.getKey2();
			stream.writeInt(id1);
			stream.writeInt(id2);
			stream.writeFloat(value);
			}
		}

	public HierarchicalClusteringStringDistanceMatrix sample(final int sampleSize)
		{
		if (sampleSize == 0 || sampleSize >= numKeys())
			{
			return this;
			}
		final Set<HierarchicalCentroidCluster<SimpleClusterable<String>>> clusters =
				new HashSet<HierarchicalCentroidCluster<SimpleClusterable<String>>>(getKeys());
		DSCollectionUtils.retainRandom(clusters, sampleSize);

		HierarchicalClusteringStringDistanceMatrix result =
				new HierarchicalClusteringStringDistanceMatrix(getDefaultValue());
		UnorderedPairIterator<HierarchicalCentroidCluster<SimpleClusterable<String>>> pi =
				new UnorderedPairIterator<HierarchicalCentroidCluster<SimpleClusterable<String>>>(clusters, clusters);
		while (pi.hasNext())
			{
			UnorderedPair<HierarchicalCentroidCluster<SimpleClusterable<String>>> p = pi.next();
			result.put(p, get(p));
			}
		return result;
		}
	}
