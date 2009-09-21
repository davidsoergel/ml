package edu.berkeley.compbio.ml.cluster;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This will be threadsafe iff the underlying collection and its iterators are
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class CollectionClusterableIterator<T extends Clusterable<T>> implements ClusterableIterator<T>
	{
	Iterator<T> it;
	final Collection<T> underlyingCollection;

	public CollectionClusterableIterator(final Collection<T> coll)
		{
		underlyingCollection = coll;
		it = underlyingCollection.iterator();
		}

	@NotNull
	public T next() throws NoSuchElementException
		{
		return it.next();
		}

	public T nextFullyLabelled()
		{
		T s = next();
		s.doneLabelling();
		return s;
		}

	/*
   public void reset()
	   {
	   it = underlyingCollection.iterator();
	   }*/
	}
