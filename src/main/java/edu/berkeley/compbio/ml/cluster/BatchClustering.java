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

/**
 * @author lorax
 * @version 1.0
 */
@Deprecated
public abstract class BatchClustering<T extends AdditiveClusterable<T>>
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
