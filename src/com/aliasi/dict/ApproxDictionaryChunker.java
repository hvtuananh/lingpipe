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

package com.aliasi.dict;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.Chunker;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Scored;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An <code>ApproxDictionaryChunker</code> implements a chunker that
 * produces chunks based on weighted edit distance of strings from
 * dictionary entries.  This is an approximate or &quot;fuzzy&quot;
 * dictionary matching strategy.
 *
 * <P>The underlying dictionary is required to be an instance of
 * {@link TrieDictionary} in order to support efficient search for
 * matches.  Other dictionaries can be easily converted to
 * trie dictionaries by adding their entries to a fresh trie
 * dictionary.
 *
 * <P>Entries are matched by weighted edit distance, as supplied by an
 * implementation of {@link WeightedEditDistance}.  All substrings
 * within the maximum distance specified at construction time are
 * returned as part of the chunking.  Keep in mind that weights for
 * weighted edit distance are specified as proximities, that is, as
 * negative distances.
 *
 * <h4>No Transposition</h4>
 *
 * <p>Transposition is not implemented in the approximate dictionary
 * chunker, so no matches are possible through
 * transposition. Specifically, the transpose weight method is never
 * called on the underlying weighted edit distance.
 *
 * <h4>Token Sensitivity</h4>
 *
 * <P>The tokenizer factory supplied at construction time is
 * only used to constrain search by enforcing boundary conditions.
 * Chunks are only returned if they start on the first character
 * of a token and end on the last character of a token.
 *
 * <p>Using an instance of {@link
 * com.aliasi.tokenizer.CharacterTokenizerFactory} effectively removes
 * token sensitivity by treating every non-whitespace character as a
 * token and thus rendering every non-whitespace position a possible
 * chunk boundary.
 *
 * <h4>Serialization</h4>
 *
 * An approximate dictionary is serializable if its tokenizer factory
 * and edit distance are serializable.  The reconstituted object will
 * be an instance of this class, {@code ApproxDictionaryChunker}.
 * 
 * <h4>References</h4>
 *
 * <P>The approach implemented here is very similar to that described
 * in the following paper:
 *
 * <ul>
 *
 * <li> Yoshimasa Tsuruoka and Jun'ichi Tsujii.  2003. <a
 * href="http://www-tsujii.is.s.u-tokyo.ac.jp/~tsuruoka/papers/acl03bio.pdf"
 * >Boosting precision and recall of dictionary-based protein name
 * recognition</a> In <i>Proceedings of the 2003 ACL workshop on NLP
 * in Biomedicine</i>.
 * </li>
 * </ul>
 *
 * The best general reference for approximate string matching
 * is:
 *
 * <ul>
 * <li>
 * Gusfield, Dan.  1997.  <i>Algorithms on Strings, Trees and Sequences</i>.
 * Cambridge University Press.
 * </li>
 * </ul>
 *
 * @author Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.1
 */
public class ApproxDictionaryChunker 
    implements Chunker,
               Serializable {

    static final long serialVersionUID = 5364907367744655793L;

    private final TrieDictionary<String> mDictionary;
    private final TokenizerFactory mTokenizerFactory;
    private final WeightedEditDistance mEditDistance;
    private double mDistanceThreshold;

    /**
     * Construct an approximate dictionary chunker from the specified
     * dictionary, tokenizer factory, weighted edit distance and
     * distance bound.  The dictionary is used for the candidate
     * matches.  The tokenizer factory is used for determining
     * possible boundaries of matches, which must start on the first
     * character of a token and end on the last character of a token.
     * The edit distance is used for measuring substrings against
     * dictionary entries.  The distance threshold specifies the
     * maximum distance at which matches are returned.
     *
     * @param dictionary Dictionary to use for matching.
     * @param tokenizerFactory Tokenizer factory for boundary
     * determination.
     * @param editDistance Matching distance measure.
     * @param distanceThreshold Distance threshold for matching.
     */
    public ApproxDictionaryChunker(TrieDictionary<String> dictionary,
                                   TokenizerFactory tokenizerFactory,
                                   WeightedEditDistance editDistance,
                                   double distanceThreshold) {
        mDictionary = dictionary;
        mTokenizerFactory = tokenizerFactory;
        mEditDistance = editDistance;
        mDistanceThreshold = distanceThreshold;
    }


    /**
     * Returns the trie dictionary underlying this chunker.
     * This is the actual dictionary used by the chunker, so changes
     * to it will affect this chunker.
     *
     * @return The trie dictionary underlying this chunker.
     */
    public TrieDictionary<String> dictionary() {
        return mDictionary;
    }

    /**
     * Returns the weighted edit distance for matching with
     * this chunker.  This is the actual edit distance used by
     * the chunker, so changes to it will affect this chunker.
     *
     * @return The weighted edit distance for this chunker.
     */
    public WeightedEditDistance editDistance() {
        return mEditDistance;
    }

    /**
     * Returns the tokenizer factory for matching with this
     * chunker.  This is the actual tokenizer factory used
     * by this chunker, so changes to it will affect the
     * behavior of this class.
     *
     * @return The tokenizer factory for this chunker.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns the maximum edit distance a string can be from a
     * dictionary entry in order to be returned by this chunker.  This
     * value is set using {@link #setMaxDistance(double)}.
     *
     * @return The maximum edit distance for this chunker.
     */
    public double distanceThreshold() {
        return mDistanceThreshold;
    }


    /**
     * Set the max distance a string can be from a dictionary entry
     * in order to be returned as a chunk by this chunker.
     */
    public void setMaxDistance(double distanceThreshold) {
        mDistanceThreshold = distanceThreshold;
    }

    /**
     * Return the approximate dictionary-based chunking for
     * the specified character sequence.
     *
     * @param cSeq Character sequence to chunk.
     * @return Chunking of the specified character sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return chunk(cs,0,cs.length);
    }

    /**
     * Return the approximate dictionary-based chunking for the
     * specified character sequence.
     *
     * @param cs Underlying characters.
     * @param start Index of first character in the array.
     * @param end Index of one past the last character in the array.
     * @return Chunking of the specified character sequence.
     * @throws IllegalArgumentException If the indices are out of
     * bounds in the character sequence.
     */
    public Chunking chunk(char[] cs, int start, int end) {
        int length = end-start;

        // token start/ends setup; throws exception if args wrong
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,length);
        boolean[] startTokens = new boolean[length];
        boolean[] endTokens = new boolean[length+1];
        Arrays.fill(startTokens,false);
        Arrays.fill(endTokens,false);
        String token;
        while ((token = tokenizer.nextToken()) != null) {
            int lastStart = tokenizer.lastTokenStartPosition();
            startTokens[lastStart] = true;
            endTokens[lastStart + token.length()] = true;
        }

        Map<Dp,Chunk> dpToChunk = new HashMap<Dp,Chunk>();
        Map<SearchState,SearchState> queue = new HashMap<SearchState,SearchState>();
        for (int i = 0; i < length; ++i) {
            int startPlusI = start + i;
            char c = cs[startPlusI];
            if (startTokens[i]) {
                add(queue,mDictionary.mRootNode,startPlusI,
                    0.0,
                    false,dpToChunk,cs,startPlusI);
            }
            Map<SearchState,SearchState> nextQueue = new HashMap<SearchState,SearchState>();
            double deleteCost = -mEditDistance.deleteWeight(c);
            for (SearchState state : queue.values()) {
                
                // delete
                add(nextQueue,state.mNode,state.mStartIndex,
                    state.mScore + deleteCost,
                    endTokens[i+1],dpToChunk,cs,startPlusI);

                // match or subst
                char[] dtrChars = state.mNode.mDtrChars;
                Node<String>[] dtrNodes = state.mNode.mDtrNodes;
                for (int j = 0; j < dtrChars.length; ++j) {
                    add(nextQueue,dtrNodes[j],state.mStartIndex,
                        state.mScore
                        - (dtrChars[j] == c
                           ?  mEditDistance.matchWeight(dtrChars[j])
                           : mEditDistance.substituteWeight(dtrChars[j],c)),
                        endTokens[i+1],dpToChunk,cs,startPlusI);
                }

            }
            queue = nextQueue;
        }
        ChunkingImpl result = new ChunkingImpl(cs,start,end);
        for (Chunk chunk : dpToChunk.values())
            result.add(chunk);
        return result;
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    void add(Map<SearchState,SearchState> nextQueue, Node<String> node, int startIndex,
             double chunkScore,
             boolean isTokenEnd,
             Map<Dp,Chunk> chunking, char[] cs, int end) {

        if (chunkScore > mDistanceThreshold)
            return;

        SearchState state2
            = new SearchState(node,startIndex,chunkScore);

        SearchState exState = nextQueue.get(state2);

        if (exState != null && exState.mScore < chunkScore)
            return;

        nextQueue.put(state2,state2);

        // finish match at token end by adding each cat (may be 0)
        if (isTokenEnd) {
            for (int i = 0; i < node.mEntries.length; ++i) {
                Chunk newChunk
                    = ChunkFactory
                    .createChunk(startIndex,end+1,
                                 node.mEntries[i].category().toString(),
                                 chunkScore);
                Dp dpNewChunk = new Dp(newChunk);
                Chunk oldChunk = chunking.get(dpNewChunk);
                if (oldChunk != null && oldChunk.score() <= chunkScore)
                    continue;
                chunking.remove(dpNewChunk);
                chunking.put(dpNewChunk,newChunk);
            }
        }

        // insert
        for (int i = 0; i < node.mDtrChars.length; ++i)
            add(nextQueue,node.mDtrNodes[i],startIndex,
                chunkScore - mEditDistance.insertWeight(node.mDtrChars[i]),
                isTokenEnd,chunking,cs,end);
    }

    // chunk's data less score for efficient dynamic programming key
    static final class Dp {
        final int mStart;
        final int mEnd;
        final String mType;
        int mHashCode;
        Dp(Chunk chunk) {
            mStart = chunk.start();
            mEnd = chunk.end();
            mType = chunk.type();
            mHashCode = mStart + 31 * (mEnd + 31 * mType.hashCode());
        }
        @Override
        public int hashCode() {
            return mHashCode;
        }
        @Override
        public boolean equals(Object that) {
            Dp thatDp = (Dp) that;
            return mStart == thatDp.mStart
                && mEnd == thatDp.mEnd
                && mType.equals(thatDp.mType);
        }
    }

    static final class SearchState implements Scored {
        private final double mScore;
        private final Node<String> mNode;
        private final int mStartIndex; // absolute in cs
        SearchState(Node<String> node, int startIndex) {
            this(node,startIndex,0.0);
        }
        SearchState(Node<String> node, int startIndex, double score) {
            mNode = node;
            mStartIndex = startIndex;
            mScore = score;
        }
        public double score() {
            return mScore;
        }
        @Override
        public boolean equals(Object that) {
            SearchState thatState = (SearchState) that;
            return mStartIndex == thatState.mStartIndex
                && mNode == thatState.mNode;
        }
        @Override
        public int hashCode() {
            return mStartIndex; //  + 31 * mNode.hashCode();
        }
        @Override
        public String toString() {
            return "SearchState(" + mNode
                + ", " + mStartIndex
                + ", " + mScore + ")";
        }
    }

    /**
     * This is a weighted edit distance defined by Tsuruoka and Tsujii
     * for matching protein names in biomedical texts.  Reproducing
     * table 1 from their paper provides the weighting function
     * (converting slightly to our terminology and scale):
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><b><i>Operation</i></b></td>
     *     <td><b><i>Character</i></b></td>
     *     <td><b><i>Cost</i></b></td></tr>
     *
     * <tr><td rowspan='2'><i>Insertion</i></td>
     *     <td>space or hyphen</td>
     *     <td>-10</td></tr>
     * <tr><td>other characters</td>
     *     <td>-100</td></tr>
     *
     * <tr><td rowspan='2'><i>Deletion</i></td>
     *     <td>space or hyphen</td>
     *     <td>-10</td></tr>
     * <tr><td>other characters</td>
     *     <td>-100</td></tr>
     *
     * <tr><td rowspan='4'><i>Substitution</i></td>
     *     <td>space for hyphen</td>
     *     <td>-10</td></tr>
     * <tr><td>digit for other digit</td>
     *     <td>-10</td></tr>
     * <tr><td>capital for lowercase</td>
     *     <td>-10</td></tr>
     * <tr><td>other characters</td>
     *     <td>-50</td></tr>
     *
     * <tr><td><i>Match</i></td>
     *     <td>any character</td>
     *     <td>0</td></tr>
     *
     * <tr><td><i>Transposition</i></td>
     *     <td>any characters</td>
     *     <td>Double.NEGATIVE_INFINITY</td></tr>
     *
     * <tr><td colspan='3'><b>Tsuruoka and Tsujii's Weighted Edit Distance</b></td></tr>
     * </table>
     * </table>
     * </blockquote>
     *
     * Tsuruoka and Tsujii's paper is available online:
     *
     * <blockquote> Yoshimasa Tsuruoka and Jun'ichi Tsujii.  2003. <a
     * href="http://www-tsujii.is.s.u-tokyo.ac.jp/~tsuruoka/papers/acl03bio.pdf"
     * >Boosting precision and recall of dictionary-based protein name
     * recognition</a> In <i>Proceedings of the 2003 ACL workshop on
     * NLP in Biomedicine</i>.
     */
    public static final WeightedEditDistance TT_DISTANCE = new TTDistance();

    static final class TTDistance extends WeightedEditDistance {
        @Override
        public double deleteWeight(char cDeleted) {
            return (cDeleted == ' ' || cDeleted == '-')
                ? -10.0
                : -100.0;
        }
        @Override
        public double insertWeight(char cInserted) {
            return deleteWeight(cInserted);
        }
        @Override
        public double matchWeight(char cMatched) {
            return 0.0;
        }
        @Override
        public double substituteWeight(char cDeleted, char cInserted) {
            if (cDeleted == ' ' && cInserted == '-')
                return -10.0;
            if (cDeleted == '-' && cInserted == ' ')
                return -10.0;
            if (Character.isDigit(cDeleted) && Character.isDigit(cInserted))
                return -10.0;
            if (Character.toLowerCase(cDeleted)
                == Character.toLowerCase(cInserted))
                return -10.0;
            return -50.0;
        }
        @Override
        public double transposeWeight(char c1, char c2) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 3935654738558540166L;
        private final ApproxDictionaryChunker mChunker;
        public Serializer() {
            this(null);
        }
        public Serializer(ApproxDictionaryChunker chunker) {
            mChunker = chunker;
        }
        @Override
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            TrieDictionary<String> dictionary
                = (TrieDictionary<String>) in.readObject();
            @SuppressWarnings("unchecked")
            TokenizerFactory tokenizerFactory
                = (TokenizerFactory) in.readObject();
            @SuppressWarnings("unchecked")
            WeightedEditDistance editDistance
                = (WeightedEditDistance) in.readObject();
            double distanceThreshold = in.readDouble();
            return new ApproxDictionaryChunker(dictionary,
                                               tokenizerFactory,
                                               editDistance,
                                               distanceThreshold);
            
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mChunker.mDictionary);
            out.writeObject(mChunker.mTokenizerFactory);
            out.writeObject(mChunker.mEditDistance);
            out.writeDouble(mChunker.mDistanceThreshold);
        }
    }

}
