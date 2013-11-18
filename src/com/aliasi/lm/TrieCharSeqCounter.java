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

// import com.aliasi.util.Arrays;

import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>TrieCharSeqCounter</code> stores counts for substrings of
 * strings.  When the counter is constructed, a maximum length is
 * specified, and counts are only stored for strings up to that
 * length.  For instance, an n-gram language model needs only counts
 * for strings up to length n.
 *
 * <P> Strings may be added to the counter using {@link
 * #incrementSubstrings(char[],int,int)}, which increments the counts
 * for all substrings of the specified character slice up to the
 * specified maximum length substring. The method {@link
 * #incrementPrefixes(char[],int,int)} increments only the prefixes of
 * the specified string.  All substrings are incremented by
 * incrementing prefixes for each suffix.  A substring counter may be
 * pruned using {@link #prune(int)}, which removes all substrings with
 * count below the specified threshold.
 *
 * <P>There are a wide range of reporting methods for trie-based
 * counters.
 *
 * <P><i>Implementation Note:</i> The trie counters are a heavily
 * unfolded implementation of a character-based Patricia (PAT) trie.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public class TrieCharSeqCounter implements CharSeqCounter {

    Node mRootNode =  NodeFactory.createNode(0);
    final int mMaxLength;

    /**
     * Construct a substring counter that stores substrings
     * up to the specified maximum length.
     *
     * @param maxLength Maximum length of substrings stored by this
     * counter.
     * @throws IllegalArgumentException If the maximum length is
     * negative.
     */
    public TrieCharSeqCounter(int maxLength) {
        if (maxLength < 0) {
            String msg = "Max length must be >= 0."
                + " Found length=" + maxLength;
            throw new IllegalArgumentException(msg);
        }
        mMaxLength = maxLength;
    }

    // following is CharSeqCounter interface w. inherited comments

    public long count(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        return mRootNode.count(cs,start,end);
    }

    public long extensionCount(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        return mRootNode.contextCount(cs,start,end);
    }

    public char[] observedCharacters() {
        return com.aliasi.util.Arrays.copy(mRootNode.outcomes(new char[] { },0,0));
    }

    public char[] charactersFollowing(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        return com.aliasi.util.Arrays.copy(mRootNode.outcomes(cs,start,end));
    }

    public int numCharactersFollowing(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        return mRootNode.numOutcomes(cs,start,end);
    }

    /**
     * Returns the sum of counts for all non-empty character
     * sequences.
     *
     * @return The sum of counts for all non-empty character
     * sequences.
     */
    public long totalSequenceCount() {
        long sum = 0l;
        long[][] uniqueTotals = uniqueTotalNGramCount();
        for (int i = 0; i < uniqueTotals.length; ++i)
            sum += uniqueTotals[i][1];
        return sum;
    }

    /**
     * Returns the sum of the counts of all character sequences of
     * the specified length.
     *
     * @return The sum of the counts of all character sequences of
     * the specified length.
     */
    public long totalSequenceCount(int length) {
        return mRootNode.totalNGramCount(length);
    }

    /**
     * Returns the number of character sequences with non-zero counts,
     * including the empty (zero length) character sequence.
     *
     * @return Number of character sequences with non-zero counts.
     */
    public long uniqueSequenceCount() {
        return mRootNode.size();
    }

    /**
     * Returns the number of character sequences of the specified length
     * with non-zero counts.
     *
     * @return The number of character sequences of the specified
     * length with non-zero counts.
     */
    public long uniqueSequenceCount(int nGramOrder) {
        return mRootNode.uniqueNGramCount(nGramOrder);
    }

    /**
     * Removes strings with counts below the specified minimum.
     * Counts for remaining strings are not affected.  Pruning may be
     * interleaved with updating counts in any order.
     *
     * @param minCount Minimum count required to retain a substring
     * count.
     * @throws IllegalArgumentException If the count is less than
     * <code>1</code>.
     */
    public void prune(int minCount) {
        if (minCount < 1) {
            String msg = "Prune minimum count must be more than 1."
                + " Found minCount=" + minCount;
            throw new IllegalArgumentException(msg);
        }
        mRootNode = mRootNode.prune(minCount);
        if (mRootNode == null)
            mRootNode = NodeFactory.createNode(0);
    }

    /**
     * Returns an array of frequency counts for n-grams of the
     * specified n-gram order sorted in descending frequency order.
     * This form of result is sometimes called a Zipf plot because
     * of the sorting.
     *
     * @param nGramOrder Order of n-gram counted.
     * @return Array of frequency counts, sorted in decreasing order
     * of rank.
     */
    public int[] nGramFrequencies(int nGramOrder) {
        List<Long> counts = countsList(nGramOrder);
        int[] result = new int[counts.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = counts.get(i).intValue();
        java.util.Arrays.sort(result);
        for (int i = result.length/2; i >= 0; --i) {
            int iOpp = result.length-i-1;
            int tmp = result[i];
            result[i] = result[iOpp];
            result[iOpp] = tmp;
        }
        return result;
    }

    /**
     * Returns the array of unique and total n-gram counts for each
     * n-gram length.  The return array is indexed in the first
     * position by n-gram length, and in the second position by
     * <code>0</code> for unique counts and <code>1</code> for total
     * counts.  Thus for <code>0&lt;=n&lt;=maxLength()</code>:
     *
     * <blockquote><code>
     * uniqueTotalNGramCount()[n][0] == uniqueNGramCount(n)
     * </code></blockquote>
     *
     * and
     *
     * <blockquote><code>
     * uniqueTotalNGramCount()[n][1] == totalNGramCount(n)
     * </code></blockquote>
     *
     * If unique and total counts are required for several
     * n-gram depths, this method is much more efficient than
     * calling all of the individual methods separately.
     *
     * @return The array of unique and total n-gram counts for
     * each n-gram length.
     */
    public long[][] uniqueTotalNGramCount() {
        long[][] result = new long[mMaxLength+1][2];
        mRootNode.addNGramCounts(result,0);
        return result;
    }

    /**
     * Returns a counter of occurrences of the highest frequency
     * n-grams of a specified n-gram order.  The actual n-grams are
     * represented as strings in the result; recall that strings
     * are instances of {@link CharSequence}.
     * 
     * <p>The maximum number of results returned must be specified,
     * because the entire set of n-grams is usually too large to
     * return as a counter.
     *
     * @param nGramOrder Order of n-gram to count.
     * @param maxReturn Maximum number of objects returned.
     */
    public ObjectToCounterMap<String> topNGrams(int nGramOrder, int maxReturn) {
        NBestCounter counter = new NBestCounter(maxReturn,true);
        mRootNode.topNGrams(counter,new char[nGramOrder],0,nGramOrder);
        return counter.toObjectToCounter();
    }

    /**
     * Returns the count in the training corpus for the specified
     * sequence of characters.  The count returned may have been
     * reduced from the raw counts in training cases by pruning.
     *
     * @param cSeq Character sequence.
     * @return Count of character sequence in model.
     */
    public long count(CharSequence cSeq) {
        return count(com.aliasi.util.Arrays.toArray(cSeq),0,cSeq.length());
    }


    /**
     * Returns the sum of the counts of all character sequences one
     * character longer than the specified character sequence.
     *
     * @param cSeq Character sequence.
     * @return The sum of the counts of all character sequences one
     * character longer than the specified character sequence.
     */
    public long extensionCount(CharSequence cSeq) {
        return mRootNode.contextCount(com.aliasi.util.Arrays.toArray(cSeq),0,cSeq.length());
    }

    /**
     * Increments the count of all substrings of the specified
     * character array slice up to the maximum length specified in the
     * constructor.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @throws IndexOutOfBoundsException If the specified start and one plus
     * end point are not in the bounds of character sequence.
     */
    public void incrementSubstrings(char[] cs, int start, int end) {
        incrementSubstrings(cs,start,end,1);
    }

    /**
     * Increments by the specified count all substrings of the
     * specified character array slice up to the maximum length
     * specified in the constructor.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @param count Amount to increment.
     * @throws IndexOutOfBoundsException If the specified start and one plus
     * end point are not in the bounds of character sequence.
     */
    public void incrementSubstrings(char[] cs, int start, int end,
                                    int count) {
        Strings.checkArgsStartEnd(cs,start,end);
        // increment maximal strings and prefixes
        for (int i = start; i+mMaxLength <= end; ++i)
            incrementPrefixes(cs,i,i+mMaxLength,count);
        // increment short final strings and prefixes
        for (int i = Math.max(start,end-mMaxLength+1); i < end; ++i)
            incrementPrefixes(cs,i,end,count);
    }

    /**
     * Increments the count of all substrings of the specified
     * character sequence up to the maximum length specified in the
     * constructor.
     *
     * @param cSeq Character sequence.
     */
    public void incrementSubstrings(CharSequence cSeq) {
        incrementSubstrings(cSeq,1);
    }

    /**
     * Increments by the specified count all substrings of the
     * specified character sequence up to the maximum length specified
     * in the constructor.
     *
     * @param cSeq Character sequence.
     * @param count Amount to increment.
     */
    public void incrementSubstrings(CharSequence cSeq, int count) {
        incrementSubstrings(com.aliasi.util.Arrays.toArray(cSeq),
                            0,cSeq.length(),count);
    }

    /**
     * Increments the count of all prefixes of the specified
     * character sequence up to the maximum length specified in the
     * constructor.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @throws IndexOutOfBoundsException If the specified start and one plus
     * end point are not in the bounds of character sequence.
     */
    public void incrementPrefixes(char[] cs, int start, int end) {
        incrementPrefixes(cs,start,end,1);
    }


    /**
     * Increments the count of all prefixes of the specified
     * character sequence up to the maximum length specified in the
     * constructor.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @param count Amount to increment.
     * @throws IndexOutOfBoundsException If the specified start and one plus
     * end point are not in the bounds of character sequence.
     */
    public void incrementPrefixes(char[] cs, int start, int end,
                                  int count) {
        Strings.checkArgsStartEnd(cs,start,end);
        mRootNode = mRootNode.increment(cs,start,end,count);
    }


    /**
     * Decrements all of the substrings of the specified character
     * slice by one.  This method may be used in conjunction with
     * {@link #incrementSubstrings(char[],int,int)} to implement
     * counts for conditional probability estimates without affecting
     * underlying estimates.  For example, the following code:
     * 
     * <blockquote><pre>
     * char[] cs = &quot;abcdefghi&quot;.toCharArray();
     * counter.incrementSubstrings(cs,3,7);
     * counter.decrementSubstrings(cs,3,5);
     * </pre></blockquote>
     *
     * will increment the substrings of <code>&quot;defg&quot;</code>
     * and then decrement the substrings of <code>&quot;de&quot;</code>,
     * causing the net effect of incrementing the counts of substrings
     * <code>&quot;defg&quot;</code>,
     * <code>&quot;efg&quot;</code>,
     * <code>&quot;fg&quot;</code>,
     * <code>&quot;g&quot;</code>,
     * <code>&quot;def&quot;</code>,
     * <code>&quot;ef&quot;</code>, and
     * <code>&quot;f&quot;</code>.  This has the effect of increasing
     * the estimate of <code>g</code> given <code>def</code>, without
     * increasing the estimate of <code>d</code> in an empty context.
     *
     * @param cs Underlying array of characters in slice.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @throws IllegalArgumentException If the array slice is valid.
     */
    public void decrementSubstrings(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        for (int i = start; i < end; ++i)
            for (int j = i; j <= end; ++j) 
                mRootNode = mRootNode.decrement(cs,i,j);
    }

    /**
     * Returns a string representation of the trie structure of counts
     * underlying this counter.
     *
     * <P><b>Warning:</b> The resulting string will be very large if
     * the number of substrings is large.  To avoid blowing out
     * memory, do not call this method for large counters.
     *
     * @return String representation of this counter.
     */
    @Override
    public String toString() {
        return mRootNode.toString();
    }

    void toStringBuilder(StringBuilder sb) {
        mRootNode.toString(sb,0);
    }

    /**
     * Decrements the unigram count for the specified character.  This
     * method is useful for training conditional probabilities, even
     * though it is not powerful enough to do it in full generality.
     *
     * @param c Decrement the unigram count for the specified
     * character.
     */
    public void decrementUnigram(char c) {
        decrementUnigram(c,1);
    }

    /**
     * Decrements the unigram count by the specified amount for the
     * specified character.  This
     * method is useful for training conditional probabilities, even
     * though it is not powerful enough to do it in full generality.
     *
     * @param c Decrement the unigram count for the specified
     * character.
     * @param count Amount to decrement.
     */
    public void decrementUnigram(char c, int count) {
        mRootNode = mRootNode.decrement(new char[] { c }, 0, 1, count);
    }

    private List<Long> countsList(int nGramOrder) {
        List<Long> accum = new ArrayList<Long>();
        mRootNode.addCounts(accum,nGramOrder);
        return accum;
    }


    /**
     * Writes an encoding of this counter to the specified output
     * stream.  It may be read back in using {@link
     * #readFrom(InputStream)}.
     *
     * <p>The output is produced using a {@link BitTrieWriter} wrapped
     * around a {@link BitOutput} wrapped around the specified
     * underlying output stream.  First, the bit output is used to
     * delta-code the maximum n-gram plus 1.  Then, the trie is
     * encoded as described in {@link BitTrieWriter}.  Finally, the
     * bit output is flushed.  The underlying output stream is neither
     * flushed nor closed, allowing them to be used for other pruposes
     * after this counter is written.
     *
     * <p>If necessary for efficiency, streams should be buffered
     * before being passed to this method.
     *
     * @param out Underlying output stream for writing.
     * @throws IOException If there is an underlying I/O error.
     */
    public void writeTo(OutputStream out) throws IOException {
        BitOutput bitOut = new BitOutput(out);
        bitOut.writeDelta(mMaxLength+1L);
        TrieWriter writer = new BitTrieWriter(bitOut);
        writeCounter(this,writer,mMaxLength);
        bitOut.flush();
    }

    /**
     * Writes the specified sequence counter to the specified trie
     * writer, restricting output to n-grams not longer than the
     * specified maximum.
     *
     * @param counter Counter to write.
     * @param writer Trie writer to which counter is written.
     * @param maxNGram Maximum length n-gram written.
     * @throws IOException If there is an underlying I/O error.
     */
    public static void writeCounter(CharSeqCounter counter,
                                    TrieWriter writer,
                                    int maxNGram) 
        throws IOException {

        writeCounter(new char[maxNGram],0,counter,writer);
    }


    /**
     * Reads a trie character sequence counter from the specified
     * input stream.  
     *
     * <p>The expected encoding is described in {@link
     * #writeTo(OutputStream)}.
     *
     * <p>If necessary for efficiency, streams should be buffered
     * before being passed to this method.
     *
     * @param in Underlying input stream for reading.
     * @throws IOException If there is an underlying I/O error.
     */
    public static TrieCharSeqCounter readFrom(InputStream in) 
        throws IOException {

        BitInput bitIn = new BitInput(in);
        int maxNGram = (int) (bitIn.readDelta() - 1L);
        BitTrieReader reader = new BitTrieReader(bitIn);
        return readCounter(reader,maxNGram);
    }

    /**
     * Reads a trie character sequence counter from the specified
     * trie reader, restricting the result to the specified maximum
     * n-gram.
     *
     * @param reader Reader from which to read the trie.
     * @param maxNGram Maximum length n-gram to read.
     * @return The counter read from the reader.
     * @throws IOException If there is an underlying I/O error.
     */
    public static TrieCharSeqCounter readCounter(TrieReader reader,
                                                 int maxNGram) 
        throws IOException {

        TrieCharSeqCounter counter = new TrieCharSeqCounter(maxNGram);
        counter.mRootNode = readNode(reader,0,maxNGram);
        return counter;
    }

    static void writeCounter(char[] cs, int pos, 
                             CharSeqCounter counter,
                             TrieWriter writer)
        throws IOException {

        long count = counter.count(cs,0,pos);
        writer.writeCount(count);
        if (pos < cs.length) { // daughters within n-gram bound
            char[] csNext = counter.charactersFollowing(cs,0,pos);
            for (int i = 0; i < csNext.length; ++i) {
                writer.writeSymbol(csNext[i]);
                cs[pos] = csNext[i];
                writeCounter(cs,pos+1,counter,writer);
            }
        }
        writer.writeSymbol(-1L);  // end of daughters
    }



    private static void skipNode(TrieReader reader) 
        throws IOException {

        reader.readCount();
        while (reader.readSymbol() != -1)
            skipNode(reader);
    }

    private static Node readNode(TrieReader reader, int depth, int maxDepth)
        throws IOException {
    
        if (depth > maxDepth) {
            skipNode(reader);
            return null;
        }

        long count = reader.readCount();

        int depthPlus1 = depth + 1;

        long sym1 = reader.readSymbol();

        // 0+ daughters
        if (sym1 == -1L)
            return NodeFactory.createNode(count);

        // 1+ daughters
        Node node1 = readNode(reader,depthPlus1,maxDepth);
        long sym2 = reader.readSymbol();
        if (sym2 == -1L)
            return NodeFactory.createNodeFold((char)sym1,node1,
                                              count);

        Node node2 = readNode(reader,depthPlus1,maxDepth);
        long sym3 = reader.readSymbol();
        if (sym3 == -1L)
            return NodeFactory.createNode((char)sym1,node1,
                                          (char)sym2,node2,
                                          count);

        Node node3 = readNode(reader,depthPlus1,maxDepth);
        long sym4 = reader.readSymbol();
        if (sym4 == -1L) 
            return NodeFactory.createNode((char)sym1,node1,
                                          (char)sym2,node2,
                                          (char)sym3,node3,
                                          count);
        Node node4 = readNode(reader,depthPlus1,maxDepth);
    
        // 4+ daughters
        StringBuilder cBuf = new StringBuilder();
        cBuf.append((char)sym1);
        cBuf.append((char)sym2);
        cBuf.append((char)sym3);
        cBuf.append((char)sym4);
    
        List<Node> nodeList = new ArrayList<Node>();
        nodeList.add(node1);
        nodeList.add(node2);
        nodeList.add(node3);
        nodeList.add(node4);

        long sym;

        while ((sym = reader.readSymbol()) != -1L) {
            cBuf.append((char)sym);
            nodeList.add(readNode(reader,depthPlus1,maxDepth));
        }
        Node[] nodes = nodeList.toArray(EMPTY_NODE_ARRAY);
        char[] cs = Strings.toCharArray(cBuf);
        return NodeFactory.createNode(cs,nodes,count);  // > 3 daughters
    }

    static final Node[] EMPTY_NODE_ARRAY
        = new Node[0];

}
    
