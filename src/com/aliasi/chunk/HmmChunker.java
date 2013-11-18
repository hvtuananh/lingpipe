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

import com.aliasi.hmm.HmmDecoder;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Iterators;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;
import static com.aliasi.util.Math.naturalLogToBase2Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/**
 * An <code>HmmChunker</code> uses a hidden Markov model to perform
 * chunking over tokenized character sequences.  Instances contain a
 * hidden Markov model, decoder for the model and tokenizer factory.
 *
 * <p>Chunking results are available through three related methods.
 * The method {@link #chunk(CharSequence)} and its sister method
 * {@link #chunk(char[],int,int)} implement the {@link Chunking}
 * interface by returning the first-best chunking for the argument
 * character sequence or slice.  The method {@link
 * #nBest(char[],int,int,int)} return an iterator over complete
 * chunkings and their joint probability estimates in descending order
 * of probability, with the last argument supplying an upper bound on
 * the number of chunkings returned.  Finally, the method {@link
 * #nBestChunks(char[],int,int,int)} returns an iterator over the
 * chunks themselves, this time in descending order of the chunk's
 * conditional probability given the input (i.e. descending
 * confidence), with the final argument providing an upper bound on
 * the number of such chunks returned.
 *
 * <h4>Internal Token/Tag Encoding</h4>
 *
 * <P>The chunker requires a hidden Markov model whose states
 * conform to a token-by-token encoding of a chunking.  This
 * class assumes the following encoding:
 *
 * <blockquote><table border="1" cellpadding="5">
 * <tr><th align="left">Tag</th>
 *     <th align="left">Description of Tokens to which it is Assigned</th>
 *     <th align="left">May Follow</th>
 *     <th align="left">May Precede</th>
 * </tr>
 * <tr><td><code>B_<i>X</i></code></td>
 *     <td>Initial (begin) token of chunk of
 *         type <code><i>X</i></code></td>
 *     <td><code>E_<i>Y</i>, W_<i>Y</i>, EE_O_<i>X</i>, WW_O_<i>X</i></code></td>
 *     <td><code>M_<i>X</i>, W_<i>X</i></code></td></tr>
 * <tr><td><code>M_<i>X</i></code></td>
 *     <td>Interior (middle) token of chunk of
 *         type <code><i>X</i></code></td>
 *     <td><code>B_<i>X</i>, M_<i>X</i></code></td>
 *     <td><code>M_<i>X</i>, W_<i>X</i></code></td></tr>
 * <tr><td><code>E_<i>X</i></code></td>
 *     <td>Final (end) token of chunk of
 *         type <code><i>X</i></code></td>
 *     <td><code>B_<i>X</i>, M_<i>X</i></code></td>
 *     <td><code>B_<i>Y</i>, W_<i>Y</i>, BB_O_<i>X</i>, WW_O_<i>Y</i></code></td></tr>
 * <tr><td><code>W_<i>X</i></code></td>
 *     <td>Token by itself comprising a (whole) chunk of
 *         type <code><i>X</i></code></td>
 *     <td><code>E_<i>Y</i>, W_<i>Y</i>, EE_O_<i>X</i>, WW_O_<i>X</i></code></td>
 *     <td><code>B_<i>Y</i>, W_<i>Y</i>, BB_O_<i>X</i>, WW_O_<i>Y</i></code></td></tr>
 * <tr><td><code>BB_O_<i>X</i></code></td>
 *     <td>Token not in chunk, previous token ending chunk of
 *         type <code><i>X</i></code></td>
 *     <td><code>E_<i>X</i>, W_<i>X</i></code></td>
 *     <td><code>MM_O, EE_O_<i>Y</i></code></td></tr>
 * <tr><td><code>MM_O</code></td>
 *     <td>Token, previous token and following token not in chunk</td>
 *     <td><code>BB_O_<i>Y</i>, MM_O</code></td>
 *     <td><code>MM_O, EE_O_<i>Y</i></code></td></tr>
 * <tr><td><code>EE_O_<i>X</i></code></td>
 *     <td>Token and previous token not in a chunk, following
 *         token begins chunk of type <code><i>X</i></code></td>
 *     <td><code>BB_O_<i>Y</i>, MM_O</code></td>
 *     <td><code>B_<i>X</i>, W_<i>X</i></code></td></tr>
 * <tr><td><code>WW_O_<i>X</i></code></td>
 *     <td>Token not in chunk, previous token ended a chunk,
 *         following token begins chunk of
 *         type <code><i>X</i></code></td>
 *     <td><code>E_<i>X</i>, W_<i>X</i></code></td>
 *     <td><code>B_<i>Y</i>, W_<i>Y</i></code></td></tr>
 * </table></blockquote>
 *
 * The intention here is that the <code><i>X</i></code> tags in the
 * last two columns (legal followers and preceders) match the tag
 * in the first column, whereas the <code><i>Y</i></code> tags
 * vary freely.
 *
 * Note that this produces the following number of states:
 *
 * <blockquote><pre>
 * numTags = (7 * numTypes) + 1<pre></blockquote>
 *
 * Not all transitions between states are legal; the ones ruled out
 * in the table above must receive zero probability estimates.  The
 * number of legal transitions is given by:
 *
 * <blockquote><pre>
 * numTransitions = 5*numTypes<sup><sup>2</sup></sup> + 13*numTypes + 1<pre></blockquote>

 * <p> By including an indication of the position in a chunk, an HMM
 * is able to model tokens that start and end chunks, as well as those
 * that fall in the middle of chunks or make up chunks on their own.
 * In addition, it also models tokens that precede or follow chunks of
 * a given type.  For instance, consider the following tokenization
 * and tagging, with an implicit tag <code>W_OOS</code> for the
 * out-of-sentence tag:
 *
 * <blockquote><pre>
 *                 (W_OOS)
 * Yestereday      BB_O_OOS
 * afternoon       MM_O
 * ,               EE_O_PER
 * John            B_PER
 * J               M_PER
 * .               M_PER
 * Smith           E_PER
 * traveled        BB_O_PER
 * to              EE_O_LOC
 * Washington      W_LOC
 * .               WW_O_OOS
 *                 (W_OOS)</pre></blockquote>
 *
 * First note that the person chunk <code>John J. Smith</code> consists
 * of three tokens: <code>John</code> with a begin-person tag,
 * <code>J</code> and <code>.</code> with in-person tokens, and
 * <code>Smith</code> an end-person token.  In contrast, the token
 * <code>Washington</code> makes up a location chunk all by itself.
 *
 * <p> There are several flavors of tags assigned to tokens that are
 * not part of chunks based on the status of the surrounding tokens.
 * First, <code>BB_O_OOS</code> is the tag assigned to
 * <code>Yesterday</code>, because it is an out token that follows (an
 * implicit) <code>OOS</code> tag.  That is, it's the first out token
 * following out-of-sentence.  This allows the tag to capture the
 * capitalization pattern of sentence-initial tokens that are not part
 * of chunks.  The interior token <code>afternoon</code> is simply
 * assigned <code>MM_O</code>; its context does not allow it to see
 * any surrounding chunks.  At the other end of the sentence, the
 * final period token is assigned the tag
 * <code>WW_O_OOS</code>, because it preceds the (implicit) <code>OOS</code>
 * (out of sentence) chunk.  This allows some discrimination between
 * sentence-final punctuation and other punctuation.
 *
 * <p> Next note that the token <code>traveled</code> is assigned to
 * the category of first tokens following person, whereas
 * <code>to</code> is assigned to the category of a final token
 * preceding a location.  Finally note the tag <code>MM_O</code>
 * assigned to the token <code>afternoon</code> which appears between
 * two other tokens that are not part of chunks.
 *
 * <p>If taggings of this sort are required rather than chunkings, the
 * HMM decoder may be retrieved via {@link #getDecoder()} and used
 * along with the tokenizer factory retrieved through {@link
 * #getTokenizerFactory()} to produce taggings.
 *
 * <h4>Training</h4>
 *
 * <p>The class {@link CharLmHmmChunker} may be used to train a
 * chunker using an HMM estimator such as {@link
 * com.aliasi.hmm.HmmCharLmEstimator} to estimate the HMM.  This
 * estimator uses bounded character language models to estimate
 * emission probabilities.
 *
 *
 * <h4>Caching</h4>
 *
 * The efficiency of the chunker may be improved (at the expense of
 * increased memory usage) if caching is turned on for the HMM
 * decoder.  The first-best and n-best results only use log
 * probabilities, and hence only require caching of the log
 * probabilities.  The confidence-based estimator uses only the linear
 * estimates, and hence only require caching of linear probabilities.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.2
 */
public class HmmChunker implements NBestChunker, ConfidenceChunker {

    private final TokenizerFactory mTokenizerFactory;
    private final HmmDecoder mDecoder;


    /**
     * Construct a chunker from the specified tokenizer factory
     * and hidden Markov model decoder.  The decoder should operate
     * over a tag set as specified in the class documentation above.
     * Once constructed, the tokenizer factory and decoder may not
     * be changed.
     *
     * <p>See the note in the class documentation concerning caching
     * in the decoder.  A typical application will configure the cache
     * of the decoder before creating an HMM chunker.  See the class
     * documentation for {@link HmmDecoder}, as well as the method
     * documentation for {@link HmmDecoder#setEmissionCache(Map)} and
     * {@link HmmDecoder#setEmissionLog2Cache(Map)} for more
     * information.
     *
     * @param tokenizerFactory Tokenizer factory for tokenization.
     * @param decoder Hidden Markov model decoder.
     */
    public HmmChunker(TokenizerFactory tokenizerFactory,
                      HmmDecoder decoder) {
        mTokenizerFactory = tokenizerFactory;
        mDecoder = decoder;
    }

    /**
     * Returns the underlying hidden Markov model decoder for this
     * chunker.  This is the actual decoder used by this chunker, so
     * any changes to it will affect this chunker.
     *
     * <p>The decoder provides access to the underlying hidden Markov
     * model for this chunker.
     *
     * @return The decoder for this chunker.
     */
    public HmmDecoder getDecoder() {
        return mDecoder;
    }

    /**
     * Returns the underlying tokenizer factory for this chunker.
     *
     * @return The tokenizer factory for this chunker.
     */
    public TokenizerFactory getTokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns a chunking of the specified character slice.  This is
     * the chunking determined by the first-best hidden Markov model
     * tags given the tokenization performed by the underlying factory.
     * More information about the underlying HMM is provided in the
     * class documentation above.
     *
     * @param cs Array of characters.
     * @param start Index of first character.
     * @param end Index of one past last character.
     * @return First-best chunking of the specified character slice.
     * @throws IndexOutOfBoundsException If the specified indices are out
     * of bounds of the specified character array.
     */
    public Chunking chunk(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        List<String> tokList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        tokenizer.tokenize(tokList,whiteList);
        String[] toks = toStringArray(tokList);
        String[] whites = toStringArray(whiteList);
        String[] tags = mDecoder.tag(tokList).tags().toArray(Strings.EMPTY_STRING_ARRAY);
        decodeNormalize(tags);
        return ChunkTagHandlerAdapter2.toChunkingBIO(toks,whites,tags);
    }

    /**
     * Returns a chunking of the specified character sequence.  This is
     * the chunking determined by the first-best hidden Markov model
     * tags given the tokenization performed by the underlying factory.
     * More information about the underlying HMM is provided in the
     * class documentation above.
     *
     * @param cSeq Character sequence to chunk.
     * @return First-best chunking of the specified character
     * sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return chunk(cs,0,cs.length);
    }


    /**
     * Returns a size-bounded iterator over scored objects with joint
     * probability estimates of tags and tokens as scores and
     * chunkings as objects.  The maximum number of chunkings returned
     * is specified by the last argument position.  This limit should
     * be set as low as possible to reduce the memory requirements of
     * n-best search.
     *
     * @param cs Array of characters.
     * @param start Index of first character.
     * @param end Index of one past last character.
     * @param maxNBest Maximum number of results to return.
     * @return Iterator over scored objects containing chunkings and
     * joint probability estimates.
     * @throws IndexOutOfBoundsException If the specified indices are out
     * of bounds of the specified character array.
     * @throws IllegalArgumentException If the maximum n-best value is
     * not greater than zero.
     */
    public Iterator<ScoredObject<Chunking>> nBest(char[] cs, int start, int end, int maxNBest) {
        Strings.checkArgsStartEnd(cs,start,end);
        if (maxNBest < 1) {
            String msg = "Maximum n-best value must be greater than zero."
                + " Found maxNBest=" + maxNBest;
            throw new IllegalArgumentException(msg);
        }
        String[][] toksWhites = getToksWhites(cs,start,end);
        Iterator<ScoredTagging<String>> it = mDecoder.tagNBest(Arrays.asList(toksWhites[0]),maxNBest);
        return new NBestIt(it,toksWhites);
    }


    /**
     * Returns a size-bounded iterator over scored objects with
     * conditional probability estimates of tags and tokens as scores
     * and chunkings as objects.  The maximum number of chunkings
     * returned is specified by the last argument position.  This
     * limit should be set as low as possible to reduce the memory
     * requirements of n-best search.
     *
     * @param cs Array of characters.
     * @param start Index of first character.
     * @param end Index of one past last character.
     * @param maxNBest Maximum number of results to return.
     * @return Iterator over scored objects containing chunkings and
     * conditional probability estimates.
     * @throws IndexOutOfBoundsException If the specified indices are out
     * of bounds of the specified character array.
     * @throws IllegalArgumentException If the maximum n-best value is
     * not greater than zero.
     */
    public Iterator<ScoredObject<Chunking>> nBestConditional(char[] cs, int start, int end, int maxNBest) {
        Strings.checkArgsStartEnd(cs,start,end);
        if (maxNBest < 1) {
            String msg = "Maximum n-best value must be greater than zero."
                + " Found maxNBest=" + maxNBest;
            throw new IllegalArgumentException(msg);
        }
        String[][] toksWhites = getToksWhites(cs,start,end);
        Iterator<ScoredTagging<String>> it = mDecoder.tagNBestConditional(Arrays.asList(toksWhites[0]),maxNBest);
        return new NBestIt(it,toksWhites);
    }



    /**
     * Returns an iterator over scored objects with conditional
     * probability estimates for scores and chunks as objects.  This
     * returns the possible chunks in confidence-ranked order, with
     * scores being the conditional probability of the chunk given the
     * input.  If the results are exhausted, or after the specified
     * number of n-best results have been returned, the iterator will
     * return <code>false</code> when its <code>hasNext()</code>
     * method is called.
     *
     * @param cs Array of characters.
     * @param start Index of first character.
     * @param end Index of one past last character.
     * @param maxNBest Maximum number of chunks returned.
     * @return Iterator over scored objects containing chunkings and
     * joint probability estimates.
     * @throws IndexOutOfBoundsException If the specified indices are out
     * of bounds of the specified character array.
     */
    public Iterator<Chunk> nBestChunks(char[] cs, int start, int end, int maxNBest) {
        String[][] toksWhites = getToksWhites(cs,start,end);
        @SuppressWarnings("deprecation")
        TagLattice<String> lattice = mDecoder.tagMarginal(Arrays.asList(toksWhites[0]));
        return new NBestChunkIt(lattice,toksWhites[1],maxNBest);
    }

    String[][] getToksWhites(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        List<String> tokList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        tokenizer.tokenize(tokList,whiteList);
        String[] toks = toStringArray(tokList);
        String[] whites = toStringArray(whiteList);
        return new String[][] { toks, whites };
    }


    private static class NBestChunkIt extends Iterators.Buffered<Chunk> {
        final TagLattice<String> mLattice;
        final String[] mWhites;
        final int mMaxNBest;
        final int[] mTokenStartIndexes;
        final int[] mTokenEndIndexes;

        String[] mBeginTags;
        int[] mBeginTagIds;
        int[] mMidTagIds;
        int[] mEndTagIds;

        String[] mWholeTags;
        int[] mWholeTagIds;

        final BoundedPriorityQueue<Scored> mQueue;

        final int mNumToks;

        final double mTotal;

        int mCount = 0;

        NBestChunkIt(TagLattice<String> lattice, String[] whites, int maxNBest) {
            mTotal = com.aliasi.util.Math.naturalLogToBase2Log(lattice.logZ());
            mLattice = lattice;
            mWhites = whites;
            String[] toks = lattice.tokenList().toArray(Strings.EMPTY_STRING_ARRAY);
            mNumToks = toks.length;
            mTokenStartIndexes = new int[mNumToks];
            mTokenEndIndexes = new int[mNumToks];
            int pos = 0;
            for (int i = 0; i < mNumToks; ++i) {
                pos += whites[i].length();
                mTokenStartIndexes[i] = pos;
                pos += toks[i].length();
                mTokenEndIndexes[i] = pos;
            }

            mMaxNBest = maxNBest;
            mQueue = new BoundedPriorityQueue<Scored>(ScoredObject.comparator(),
                                                      maxNBest);
            initializeTags();
            initializeQueue();
        }

        void initializeTags() {
            SymbolTable tagTable = mLattice.tagSymbolTable();
            List<String> beginTagList = new ArrayList<String>();
            List<Integer> beginTagIdList = new ArrayList<Integer>();
            List<Integer> midTagIdList = new ArrayList<Integer>();
            List<Integer> endTagIdList = new ArrayList<Integer>();
            List<String> wholeTagList = new ArrayList<String>();
            List<Integer> wholeTagIdList = new ArrayList<Integer>();
            int numTags = tagTable.numSymbols();
            for (int i = 0; i < numTags; ++i) {
                String tag = tagTable.idToSymbol(i);
                if (tag.startsWith("B_")) {
                    String baseTag = tag.substring(2);
                    beginTagList.add(baseTag);
                    beginTagIdList.add(Integer.valueOf(i));

                    String midTag = "M_" + baseTag;
                    int midTagId = tagTable.symbolToID(midTag);
                    midTagIdList.add(Integer.valueOf(midTagId));

                    String endTag = "E_" + baseTag;
                    int endTagId = tagTable.symbolToID(endTag);
                    endTagIdList.add(Integer.valueOf(endTagId));
                }
                else if (tag.startsWith("W_")) {
                    String baseTag = tag.substring(2);
                    wholeTagList.add(baseTag);
                    wholeTagIdList.add(Integer.valueOf(i));
                }
            }
            mBeginTags = toStringArray(beginTagList);
            mBeginTagIds = toIntArray(beginTagIdList);
            mMidTagIds = toIntArray(midTagIdList);
            mEndTagIds = toIntArray(endTagIdList);

            mWholeTags = toStringArray(wholeTagList);
            mWholeTagIds = toIntArray(wholeTagIdList);
        }
        void initializeQueue() {
            int len = mWhites.length-1;
            for (int i = 0; i < len; ++i) {
                for (int j = 0; j < mBeginTagIds.length; ++j)
                    initializeBeginTag(i,j);
                for (int j = 0; j < mWholeTagIds.length; ++j)
                    initializeWholeTag(i,j);
            }
        }
        void initializeBeginTag(int tokPos, int j) {
            int startCharPos = mTokenStartIndexes[tokPos];

            String tag = mBeginTags[j];
            int beginTagId = mBeginTagIds[j];
            int midTagId = mMidTagIds[j];
            int endTagId = mEndTagIds[j];

            double forward = naturalLogToBase2Log(mLattice.logForward(tokPos,beginTagId));
            double backward = naturalLogToBase2Log(mLattice.logBackward(tokPos,beginTagId));

            ChunkItState state
                = new ChunkItState(startCharPos,tokPos,
                                   tag,beginTagId,midTagId,endTagId,
                                   forward,backward);
            mQueue.offer(state);
        }
        void initializeWholeTag(int tokPos, int j) {
            int start = mTokenStartIndexes[tokPos];
            int end = mTokenEndIndexes[tokPos];
            String tag = mWholeTags[j];
            double log2Score = naturalLogToBase2Log(mLattice.logProbability(tokPos,mWholeTagIds[j]));
            Chunk chunk = ChunkFactory.createChunk(start,end,tag,log2Score);
            mQueue.offer(chunk);
        }
        @Override
        public Chunk bufferNext() {
            if (mCount > mMaxNBest) return null;
            while (!mQueue.isEmpty()) {
                Object next = mQueue.poll();
                if (next instanceof Chunk) {
                    ++mCount;
                    Chunk result = (Chunk) next;
                    return ChunkFactory.createChunk(result.start(),
                                                    result.end(),
                                                    result.type(),
                                                    result.score()-mTotal);
                }
                ChunkItState state = (ChunkItState) next;
                addNextMidState(state);
                addNextEndState(state);
            }
            return null;
        }
        void addNextMidState(ChunkItState state) {
            int nextTokPos = state.mTokPos + 1;
            if (nextTokPos + 1 >= mNumToks)
                return; // don't add if can't extend
            int midTagId = state.mMidTagId;
            double transition
                = naturalLogToBase2Log(mLattice.logTransition(nextTokPos-1,
                                                              state.mCurrentTagId,
                                                              midTagId));
            double forward = state.mForward + transition;
            double backward = naturalLogToBase2Log(mLattice.logBackward(nextTokPos,midTagId));
            ChunkItState nextState
                = new ChunkItState(state.mStartCharPos,nextTokPos,
                                   state.mTag, midTagId,
                                   state.mMidTagId, state.mEndTagId,
                                   forward,backward);
            mQueue.offer(nextState);
        }
        void addNextEndState(ChunkItState state) {
            int nextTokPos = state.mTokPos + 1;
            if (nextTokPos >= mNumToks) return;
            int endTagId = state.mEndTagId;
            double transition
                = naturalLogToBase2Log(mLattice.logTransition(nextTokPos-1,
                                                              state.mCurrentTagId,
                                                              endTagId));
            double forward = state.mForward + transition;
            double backward = naturalLogToBase2Log(mLattice.logBackward(nextTokPos,endTagId));
            double log2Prob = forward + backward; //  - mTotal;
            Chunk chunk
                = ChunkFactory.createChunk(state.mStartCharPos,
                                           mTokenEndIndexes[nextTokPos],
                                           state.mTag,
                                           log2Prob);
            mQueue.offer(chunk);
        }
    }

    private static class ChunkItState implements Scored {
        final int mStartCharPos;
        final int mTokPos;

        final String mTag;
        final double mForward;
        final double mBack;
        final double mScore;

        final int mCurrentTagId;
        final int mMidTagId;
        final int mEndTagId;
        ChunkItState(int startCharPos, int tokPos,
                     String tag, int currentTagId, int midTagId, int endTagId,
                     double forward, double back) {
            mStartCharPos = startCharPos;
            mTokPos = tokPos;

            mTag = tag;
            mCurrentTagId = currentTagId;
            mMidTagId = midTagId;
            mEndTagId = endTagId;

            mForward = forward;
            mBack = back;
            mScore = forward + back;
        }
        public double score() {
            return mScore;
        }
    }






    private static class NBestIt
        implements Iterator<ScoredObject<Chunking>> {

        final Iterator<ScoredTagging<String>> mIt;
        final String[] mWhites;
        final String[] mToks;
        NBestIt(Iterator<ScoredTagging<String>> it, String[][] toksWhites) {
            mIt = it;
            mToks = toksWhites[0];
            mWhites = toksWhites[1];
        }
        public boolean hasNext() {
            return mIt.hasNext();
        }
        public ScoredObject<Chunking> next() {
            ScoredTagging<String> so = mIt.next();
            double score = so.score();
            String[] tags = so.tags().toArray(Strings.EMPTY_STRING_ARRAY);
            decodeNormalize(tags);
            Chunking chunking
                = ChunkTagHandlerAdapter2.toChunkingBIO(mToks,mWhites,tags);
            return new ScoredObject<Chunking>(chunking,score);
        }
        public void remove() {
            mIt.remove();
        }
    }


    private static String[] toStringArray(Collection<String> c) {
        return c.<String>toArray(Strings.EMPTY_STRING_ARRAY);
    }

    private static int[] toIntArray(Collection<Integer> c) {
        int[] result = new int[c.size()];
        Iterator<Integer> it = c.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Integer nextVal = it.next();
            result[i] = nextVal.intValue();
        }
        return result;
    }


    static String baseTag(String tag) {
        if (ChunkTagHandlerAdapter2.isOutTag(tag)) return tag;
        return tag.substring(2);
    }



    static String[] trainNormalize(String[] tags) {
        if (tags.length == 0) return tags;
        String[] normalTags = new String[tags.length];
        for (int i = 0; i < normalTags.length; ++i) {
            String prevTag = (i-1 >= 0) ? tags[i-1] : "W_BOS"; // "W_BOS";
            String nextTag = (i+1 < tags.length) ? tags[i+1] : "W_BOS"; // "W_EOS";
            normalTags[i] = trainNormalize(prevTag,tags[i],nextTag);
        }
        return normalTags;
    }


    private static void decodeNormalize(String[] tags) {
        for (int i = 0; i < tags.length; ++i)
            tags[i] = decodeNormalize(tags[i]);
    }


    static String trainNormalize(String prevTag,
                                 String tag,
                                 String nextTag) {
        if (ChunkTagHandlerAdapter2.isOutTag(tag)) {
            if (ChunkTagHandlerAdapter2.isOutTag(prevTag)) {
                if (ChunkTagHandlerAdapter2.isOutTag(nextTag)) {
                    return "MM_O";
                } else {
                    return "EE_O_" + baseTag(nextTag);
                }
            } else if (ChunkTagHandlerAdapter2.isOutTag(nextTag)) {
                return "BB_O_" + baseTag(prevTag);
            } else {
                return "WW_O_" + baseTag(nextTag); // WW_O
            }
        }
        if (ChunkTagHandlerAdapter2.isBeginTag(tag)) {
            if (ChunkTagHandlerAdapter2.isInTag(nextTag))
                return "B_" + baseTag(tag);
            else
                return "W_" + baseTag(tag);
        }
        if (ChunkTagHandlerAdapter2.isInTag(tag)) {
            if (ChunkTagHandlerAdapter2.isInTag(nextTag))
                return "M_" + baseTag(tag);
            else
                return "E_" + baseTag(tag);
        }
        String msg = "Unknown tag triple."
            + " prevTag=" + prevTag
            + " tag=" + tag
            + " nextTag=" + nextTag;
        throw new IllegalArgumentException(msg);
    }

    private static String decodeNormalize(String tag) {
        if (tag.startsWith("B_") || tag.startsWith("W_")) {
            String baseTag = tag.substring(2);
            return ChunkTagHandlerAdapter2.toBeginTag(baseTag);
        }
        if (tag.startsWith("M_") || tag.startsWith("E_")) {
            String baseTag = tag.substring(2);
            return ChunkTagHandlerAdapter2.toInTag(baseTag);
        }
        return ChunkTagHandlerAdapter2.OUT_TAG;
    }

}
