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

package edu.berkeley.compbio.ml.strings;

import java.util.List;


/**
 * Interface for objects that are able to provide a list of the "first words" associated with a sequence spectrum.  The
 * purpose of this is that we may compute a chain of spectra, e.g., computing conditional k-mer probabilities from
 * absolute probabilities, which are in turn based on counts.  A spectrum based on conditional probabilities does not
 * fully describe the distribution unless the starting points are also specified.  A spectrum may be based on multiple
 * independent sequences (either becaue there are multiple inputs, or because there are unknown symbols in the
 * sequence).  Thus, a complete specification of the spectrum requires a list containing the initial word of each
 * segment.  This list can be propagated around, or not, depending on whether it is relevant, independent of the rest of
 * the model (e.g., the conditional probabilities).
 *
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */

public interface FirstWordProvider
	{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * Returns a list of all the initial words of length k that are associated with this object.  A single sequence has
	 * only one initial word, of course, but this object may represent a set of sequences, or a sequence with internal
	 * interruptions such as unknown symbols, and so may contain a number of initial words.
	 *
	 * @param k the width of the words to provide.
	 * @return the List<byte[]>
	 */
	List<byte[]> getFirstWords(int k);
	}
