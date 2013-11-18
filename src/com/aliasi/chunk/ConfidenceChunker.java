/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.chunk;

import java.util.Iterator;

/**
 * The <code>ConfidenceChunker</code> interface specifies a method
 * for returning an iterator over chunks in order of confidence.
 * Each chunk will be returned at most once, and
 * scores are assumed to be conditional probabilities of the chunk
 * given the input character slice.
 *
 * <p>This interface does not extend <code>Chunker</code> because
 * there are many ways a system might return confidence over
 * chunks without being able to return a proper <code>Chunking</code>,
 * which requires a single analysis for an input.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.3
 */
public interface ConfidenceChunker {

    /**
     * Returns the n-best chunks in decreasing order of probability
     * estimates.  The return results implement the {@link Chunk}
     * interface, and their scores are conditional probability
     * estimates of the chunk given the input character slice.
     *
     * @param cs Underlying character array.
     * @param start Index of first character to analyze.
     * @param end Index of one past the last character to analyze.
     * @param maxNBest The maximum number of chunks to return.
     */
    public Iterator<Chunk> nBestChunks(char[] cs, int start, int end, 
                                       int maxNBest);
    

}
