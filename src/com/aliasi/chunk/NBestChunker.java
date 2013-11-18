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

import com.aliasi.util.ScoredObject;

import java.util.Iterator;

/**
 * An <code>NBestChunker</code> is a chunker that is able to return
 * results iterating over scored chunkings or scored chunks in order
 * of decreasing likelihood.  
 *
 * <p>For <i>n</i>-best chunkings, the method {@link
 * #nBest(char[],int,int,int)} returns the <i>n</i>-best chunkings as
 * an iterator.  In list form, is often called an <i>n-best list</i>,
 * and represents the top <i>n</i> analyses.  Scores are assumed to be
 * joint log probabilities of the chunking (i.e. the input string plus
 * chunks).
 *
 * <p>N-best chunkers may be used in an application directly, or the
 * n-best list may be rescored using the {@link RescoringChunker}.
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.3
 */
public interface NBestChunker extends Chunker {
    
    /**
     * Return the scored chunkings of the specified character sequence
     * in order as an iterator in order of score.  The return result
     * is an iterator over scored objects consisting of chunkings and
     * scores.  The maximum number of returned chunkings is also
     * specified; for many n-best chunkers, a smaller maximum n-best
     * size leads to faster results.
     *
     * @param cs Underlying character array.
     * @param start Index of first character to analyze.
     * @param end Index of one past the last character to analyze.
     * @param maxNBest The maximum number of results to return.n
     */
    public Iterator<ScoredObject<Chunking>>
        nBest(char[] cs, int start, int end, int maxNBest);


}
