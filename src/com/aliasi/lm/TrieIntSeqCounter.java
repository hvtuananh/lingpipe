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

package com.aliasi.lm;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.ObjectToCounterMap;

/**
 * An <code>TrieIntSeqCounter</code> implements an integer sequence
 * counter with a trie structure of counts.
 *
 * <P><i>Implementation Note:</i> This trie-based integer sequence
 * counter is not as tight in memory as the character tries, but is
 * much more efficient for nodes with many daughters.  It unfolds
 * 1-daughter and 2-daughter nodes, and beyond that uses 
 * balanced binary trees (via <code>java.util.TreeMap</code>) 
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe2.0
 */
public class TrieIntSeqCounter implements IntSeqCounter {

    private final int mMaxLength;
    final IntNode mRootNode;


    /**
     * Construct an integer sequence counter for subsequences
     * up to the specified maximum length.
     *
     * @param maxLength Maximum length of subsequences counted.
     * @throws IllegalArgumentException If the maximum length is
     * less than zero.
     */
    public TrieIntSeqCounter(int maxLength) {
        if (maxLength < 0) {
            String msg = "Max length must be >= 0."
                + " Found maxLength=" + maxLength;
            throw new IllegalArgumentException(msg);
        }
        mMaxLength = maxLength;
        mRootNode = new IntNode();
    }

    /**
     * Removes all counts for sequences that are less than the minimum
     * count.  This operation is safe in that it will never remove the
     * root node.  Pruning is idempotent in that pruning twice with
     * the same count has no effect.
     *
     * @param minCount Minimum count to maintain a node.
     */
    public void prune(int minCount) {
        mRootNode.prune(minCount);
    }

    /**
     * Rescales all counts by multiplying them by the specified
     * factor.  Counts are rounded down by casting back to
     * <code>int</code> after being multipled by the scaling factor:
     * 
     * <blockquote><code>
     * count=(int)(count*countMultiplier)
     * </code></blockquote>
     *
     * Unlike pruning, scaling has a cumulative effect and is not
     * idempotent.  For instance, a count of four scaled by half once
     * will be two, and scaled by half twice will be one.  Because of
     * rounding, it's not even guaranteed that rescaling twice,
     * <code>rescale(0.5);&nbsp;rescale(0.5);</code>, returns the same
     * result as rescaling with the combined factor,
     * <code>rescale(0.25);</code>. 
     * 
     * <P>Also unlike pruning, scaling, because of the integer
     * rounding, may change the ratios between surviving counts.
     * For instance, under scaling by 0.5, both 3 and 2 rescale
     * to 1.
     *
     * @param countMultiplier Amount by which counts are scaled.
     */
    public void rescale(double countMultiplier) {
        mRootNode.rescale(countMultiplier);
    }

    /**
     * Returns the maximum length of subsequence of integers being
     * counted.
     *
     * @return The maximum length of subsequence of integers being
     * counted.
     */
    public int maxLength() {
        return mMaxLength;
    }

    /**
     * Increments the count for all subsequences of the specified
     * integer sequence up to the specified maximum length.  For
     * instance, calling
     * <code>incrementSubsequences({1,3,17,8,122},1,4)</code> with a
     * maximum length of <code>2</code> increments the bigram sequence
     * counts <code>{3,17}</code>, <code>{17,8}</code> and the unigram
     * sequence counts <code>{3}</code>, <code>{17}</code> and
     * <code>{8}</code>.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     */
    public void incrementSubsequences(int[] is, int start, int end) {
        checkBoundaries(is,start,end);
        for (int i = start; i < end; ++i)
            mRootNode.increment(is,i,Math.min(i+maxLength(),end));
    }


    /**
     * Increments the count for all subsequences of the specified
     * integer sequence up to the specified maximum length with the
     * specified count.  Calling
     * <code>incrementSubsequences(is,start,end,n)</code> is
     * equivalent to calling
     * <code>incrementSubsequences(is,start,end)</code> a total of
     * <code>n</code> times.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @param count
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     * @throws IllegalArgumentException If the count is less than zero.
     */
    public void incrementSubsequences(int[] is, int start, int end, 
                                      int count) {
        checkBoundaries(is,start,end);
        checkCount(count);
        if (count == 0) return;
        for (int i = start; i < end; ++i)
            mRootNode.increment(is,i,Math.min(i+maxLength(),end),count);
    }

    static void checkCount(int count) {
        if (count >= 0) return;
        String msg = "Counts must be non-negative."
            + " Found count=" + count;
        throw new IllegalArgumentException(msg);
    }



    /**
     * Increments the count for the specified slice by the specified
     * amount. For instance, calling
     * <code>incrementSequence({1,2,3,4},1,3,15)</code> results in the
     * sequence <code>2,3</code> having its count incremented by 15.
     *
     * <p>If the sequence provided is longer than the maximum sequence
     * counted, only its final counts are used.  For example, if the
     * maximum length is 3, then calling
     * <code>incrementSequence({1,2,3,4,5},0,5,12)</code> is
     * equivalent to calling
     * <code>incrementSequence({3,4,5},0,3,12)</code>.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @param count
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     * @throws IllegalArgumentException If the count is less than zero.
     */
    public void incrementSequence(int[] is, int start, int end, 
                                  int count) {
        checkBoundaries(is,start,end);
        checkCount(count);
        if (count == 0) return;
        mRootNode.incrementSequence(is,
                                    Math.max(start,end-maxLength()), end,
                                    count);
    }

    


    /**
     * Returns a histogram of counts for n-grams of integers of
     * the specified length, with a count of at least the specified
     * minimum count.  The resulting counter will be empty under
     * if there are no n-grams in this counter of the specified length
     * above the specified threshold.  Note that one case of this is
     * if the specified n-gram is greater than the maximum n-gram
     * length for this counter.
     *
     * @param nGram Length of n-gram whose histrogram is returned.
     * @param minCount Minimum count of element in histogram.
     * @return Histogram of counts of n-grams of the specified length with
     * counts above the specified minimum.
     * @throws IllegalArgumentException If the n-gram length is less
     * than 1.
     */
    public ObjectToCounterMap<int[]> nGramCounts(int nGram, int minCount) {
        if (nGram < 1) {
            String msg = "Ngrams must be positive."
                + " Found n-gram=" + nGram;
            throw new IllegalArgumentException(msg);
        }
        ObjectToCounterMap<int[]> result 
            = new ObjectToCounterMap<int[]>();
        int[] nGramBuffer = new int[nGram];
        addNGramCounts(minCount,0,nGram,nGramBuffer,result);
        return result;
    }

    /**
     * Returns the size of this graph, measured in number of nodes
     * in the trie structure.  This is equal to the number of
     * sequences of integers for which this counter stores counts.
     *
     * @return The size of this counter.
     */
    public int trieSize() {
        return mRootNode.trieSize();
    }

    /**
     * Supplies each n-gram of the specified length and with greater
     * than or equal to the specified minimum count to the specified
     * handler.
     *
     * @param nGram Length of n-grams to visit.
     * @param minCount Minimum count of visited n-gram.
     * @param handler Handler for visited n-grams.
     */
    public void handleNGrams(int nGram, int minCount, 
                             ObjectHandler<int[]> handler) {
        if (nGram < 1) {
            String msg = "Ngrams must be positive."
                + " Found n-gram=" + nGram;
            throw new IllegalArgumentException(msg);
        }
        int[] nGramBuffer = new int[nGram];
        handleNGrams(minCount,0,nGram,nGramBuffer,handler);
    }

    public int count(int[] is, int start, int end) {
        checkBoundaries(is,start,end);
        IntNode dtr = mRootNode.getDtr(is,start,end);
        return dtr == null ? 0 : dtr.count();
    }

    public long extensionCount(int[] is, int start, int end) {
        checkBoundaries(is,start,end);
        IntNode dtr = mRootNode.getDtr(is,start,end);
        return dtr == null ? 0l : dtr.extensionCount();
    }

    public int numExtensions(int[] is, int start, int end) {
        checkBoundaries(is,start,end);
        IntNode dtr = mRootNode.getDtr(is,start,end);
        return dtr == null ? 0 : dtr.numExtensions();
    }

    public int[] observedIntegers() {
        return mRootNode.observedIntegers();
    }

    public int[] integersFollowing(int[] is, int start, int end) {
        return mRootNode.integersFollowing(is,start,end);
    }
    
    /**
     * Return a string-based representation of this integer sequence
     * counter.
     *
     * @return A string-based representation of this integer sequence
     * counter.
     */
    @Override
    public String toString() {
        return mRootNode.toString(null);
    }

    void decrementUnigram(int symbol) {
        mRootNode.decrement(symbol);
    }

    void decrementUnigram(int symbol, int count) {
        mRootNode.decrement(symbol,count);
    }

    void handleNGrams(int minCount, int pos, int nGram, int[] buf,
                      ObjectHandler<int[]> handler) {
        int[] integersFollowing = integersFollowing(buf,0,pos);
        if (pos == nGram) {
            int count = count(buf,0,nGram);
            if (count < minCount) return;
            handler.handle(buf);
            return;
        }
        for (int i = 0; i < integersFollowing.length; ++i) {
            buf[pos] = integersFollowing[i];
            handleNGrams(minCount,pos+1,nGram,buf,handler);
        }
    }    

    void addNGramCounts(int minCount, int pos, int nGram, int[] buf, 
                        ObjectToCounterMap<int[]> counter) {
        int[] integersFollowing = integersFollowing(buf,0,pos);
        if (pos == nGram) {
            int count = count(buf,0,nGram);
            if (count < minCount) return;
            counter.set(buf.clone(),count);
            return;
        }
        for (int i = 0; i < integersFollowing.length; ++i) {
            buf[pos] = integersFollowing[i];
            addNGramCounts(minCount,pos+1,nGram,buf,counter);
        }
    }

    static void checkBoundaries(int[] is, int start, int end) {
        if (start < 0) {
            String msg = "Start must be in array range."
                + " Found start=" + start;
            throw new IndexOutOfBoundsException(msg);
        }
        if (end > is.length) {
            String msg = "End must be in array range."
                + " Found end=" + end
                + " Length=" + is.length;
            throw new IndexOutOfBoundsException(msg);
        }
        if (end < start) {
            String msg = "End must be at or after start."
                + " Found start=" + start
                + " Found end=" + end;
            throw new IndexOutOfBoundsException(msg);
        }

    }

}
