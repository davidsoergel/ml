package edu.berkeley.compbio.ml.cluster.kohonen;

import edu.berkeley.compbio.ml.cluster.AdditiveClusterable;
import edu.berkeley.compbio.ml.cluster.CentroidCluster;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public interface DiffusableLabelClusteringMethod<T extends AdditiveClusterable<T>, C extends CentroidCluster<T>>
	{
// -------------------------- OTHER METHODS --------------------------

	List<? extends C> getClusters();

	/**
	 * Gets an iterator over cells in the neighborhood of the given cell.  The definition of "neighborhood" is
	 * implementation-specific, and may have parameters (such as a radius).
	 *
	 * @param cell the cell whose neighborhood to iterate over.
	 * @return an iterator over cells in the neighborhood, in no particular order.
	 */
	Iterator<Set<C>> getNeighborhoodShellIterator(C cell);

	//Iterator<Set<KohonenSOMCell<T>>> getNeighborhoodShellIterator(KohonenSOMCell<T> cell);
	}
