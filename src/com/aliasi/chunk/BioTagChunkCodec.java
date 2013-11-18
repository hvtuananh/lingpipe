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

import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Iterators;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * The {@code BioTagChunkCodec} implements a chunk to tag
 * coder/decoder based on the BIO encoding scheme and a
 * specified tokenizer factory.
 *
 *
 * <h3>BIO Encoding</h3>
 *
 * The basis of the BIO encoding of a chunking is to break the
 * chunking down into tokens.  The first token in a chunk of type
 * <i>X</i> is labeled <code>B_<i>X</i></code>, and subsequent tokens
 * in a chunk of type <i>X</i> are labeled <code>I_<i>X</i></code>.
 * All other chunks are labeled with the tag <code>O</code>.
 *
 * <p>For instance, consider the following input string:
 *
 * <blockquote><pre>
 * John Jones Mary and Mr. J. J. Jones ran to Washington.
 * 012345678901234567890123456789012345678901234567890123
 * 0         1         2         3         4         5</pre></blockquote>
 *
 * and chunking consisting of the string and chunks:
 *
 * <blockquote><pre>
 * (0,10):PER, (11,15):PER, (24,35):PER, (43,53):LOC</pre></blockquote>
 *
 * Recall that indexing is of the first character and one past the
 * last character.  Note that the two person names "John Jones" and
 * "Mary", are separate chunks of type PER (for persons), and
 * the location chunk for "Washington" ends before the period.
 *
 * <p>If we have a tokenizer that breaks on whitespace
 * and punctuation, we have tokens starting at + and
 * continuing through the - signs.
 *
 * <blockquote><pre>
 * John Jones Mary and Mr. J. J. Jones ran to Washington.
 * +--- +---- +--- +-- +-+ ++ ++ +---- +-- +- +---------+</pre></blockquote>
 *
 * In particular, note that the the four periods form their own
 * tokens, even though they are adjacent to characters in other
 * tokens.  Writing the tokens out in a column, we show the tags used
 * by the default BIO encoding to the right:
 *
 * <blockquote>
 * <table border="1" cellpadding="5">
 * <tr><th>Token</th><th>Tag</th></tr>
 * <tr><td>John</td><td><code>B_PER</code></td></tr>
 * <tr><td>Jones</td><td><code>I_PER</code></td></tr>
 * <tr><td>Mary</td><td><code>B_PER</code></td></tr>
 * <tr><td>and</td><td><code>O</code></td></tr>
 * <tr><td>Mr</td><td><code>O</code></td></tr>
 * <tr><td>.</td><td><code>O</code></td></tr>
 * <tr><td>J</td><td><code>B_PER</code></td></tr>
 * <tr><td>.</td><td><code>I_PER</code></td></tr>
 * <tr><td>J</td><td><code>I_PER</code></td></tr>
 * <tr><td>.</td><td><code>I_PER</code></td></tr>
 * <tr><td>Jones</td><td><code>I_PER</code></td></tr>
 * <tr><td>ran</td><td><code>O</code></td></tr>
 * <tr><td>to</td><td><code>O</code></td></tr>
 * <tr><td>Washington</td><td><code>B_LOC</code></td></tr>
 * <tr><td>.</td><td><code>O</code></td></tr>
 * </table>
 * </blockquote>
 *
 * Note that chunks may be any number of tokens long.
 *
 * <h3>Non-Default Prefixes and Suffixes</h3>
 *
 * The default prefix for begin tags is {@code B_} and the default prefix
 * for in tags is {@code I_} and the default out tag is {@code O}.  Different
 * begin and in prefixes and out tags may be specified using the constructor
 * {@link #BioTagChunkCodec(TokenizerFactory,boolean,String,String,String)}.
 * 
 * <h3>Set of Tags</h3>
 *
 * There is a single tag <code>O</code>, as well as tags
 * <code>B_<i>X</i></code> and <code>I_<i>X</i></code> for
 * all chunk types <code><i>X</i></code>.
 *
 * <h3>Legal Tag Sequences</h3>
 *
 * The legal tag sequences are described in the following table,
 * where the first column lists tags schematically and the
 * second column shows the tags that may follow them, with
 * the variables ranging over all chunk types:
 *
 * <blockquote>
 * <table border="1" cellpadding="5">
 * <tr><th>Tag</th><th>Legal Following Tags</th></tr>
 * <tr><td><code>O</code></td><td><code>O, B_<i>X</i></code></td></tr>
 * <tr><td><code>B_<i>X</i></code></td><td><code>O, I_<i>X</i>, B_<i>Y</i></code></td></tr>
 * <tr><td><code>I_<i>X</i></code></td><td><code>O, I_<i>X</i>, B_<i>Y</i></code></td></tr>
 * </table>
 * </blockquote>
 *
 * Note that the begin and in tags have the same legal followers.
 *
 * <p>Attempts to encode taggings with illegal tag sequences
 * will result in exceptions.
 *
 * <h3>Enforcing Tokenization Consistency</h3>
 *
 * <p>If the consistency flag is set on the constructor, attempts
 * to encode chunkings or decode taggings that are inconsistent with
 * the tokenizer will throw illegal argument exceptions.
 *
 * <p>In order for a tokenizer to be consistent with a chunking,
 * the tokenization of the characterer sequence for the chunking
 * must be such that every chunk start and end occurs at
 * a token start or end.  The same rule applies for tagging, in
 * that the chunking produced has to obey the same rules.
 *
 * <p>For example, if a regular-expression based tokenizer that breaks
 * on whitespace were used for the above example, the character
 * sequence "Washington." is a token, including the final period.
 * This conflicts with the location-type entity, which ends with the
 * last character before the period.
 *
 * <h3>Serialization</h3>
 *
 * Instances of this class are serializable if their underlying
 * tokenizer factories are serializable.  Reading them back in
 * produces an instance of the same class with the same behavior.
 *
 *
 * <h3>Thread Safety</h3>
 *
 * After constructino, a BIO tag-chunk codec is thread safe.
 * 
 * @author  Bob Carpenter
 * @version 3.9.2
 * @since   LingPipe3.9.1
 */
public class BioTagChunkCodec
    extends AbstractTagChunkCodec
    implements Serializable {

    static final long serialVersionUID = -4597052413756614276L;

    private final String mBeginTagPrefix;
    private final String mInTagPrefix;
    private final String mOutTag;

    /**
     * Construct a BIO-encoded tag/chunk decoder with a null tokenizer
     * that does not enforce consistency and uses the default begin,
     * in, and out tags.  A codec constructed with
     * this method only supports the conversion of a string tagging to
     * a chunking, not vice-versa.
     */
    public BioTagChunkCodec() {
        this(null,false);
    }

    /**
     * Construct a BIO-encoding based tag-chunk coder/decoder
     * based on the specified tokenizer factory, enforcing
     * consistency of chunkings and tagging coded if the specified
     * flag is set, and using the default being, in, and out tags.
     *
     * @param tokenizerFactory Tokenizer factory for generating tokens.
     * @param enforceConsistency Set to {@code true} to ensure all
     * coded chunkings and decoded taggings are consistent for
     * round trips.
     */
    public BioTagChunkCodec(TokenizerFactory tokenizerFactory,
                            boolean enforceConsistency) {
        this(tokenizerFactory,enforceConsistency,
             BEGIN_TAG_PREFIX,IN_TAG_PREFIX,OUT_TAG);
    }

    /**
     * Construct a BIO-encoding based tag-chunk coder/decoder
     * based on the specified tokenizer factory, enforcing
     * consistency of chunkings and tagging coded if the specified
     * flag is set.
     *
     * @param tokenizerFactory Tokenizer factory for generating tokens.
     * @param enforceConsistency Set to {@code true} to ensure all
     * coded chunkings and decoded taggings are consistent for
     * round trips.
     
     */
    public BioTagChunkCodec(TokenizerFactory tokenizerFactory,
                            boolean enforceConsistency,
                            String beginTagPrefix,
                            String inTagPrefix,
                            String outTag) {
        super(tokenizerFactory,enforceConsistency);
        mOutTag = outTag;
        mBeginTagPrefix = beginTagPrefix;
        mInTagPrefix = inTagPrefix;
    }


    /**
     * Returns {@code true} if this codec enforces consistency
     * of the chunkings relative to the tokenizer factory.  Consistency
     * requires each chunk to start on the first character of a token
     * and requires each chunk to end on the last character of
     * a token (as usual, ends are one past the last character).
     *
     * @return {@code true} if this codec enforces consistency of
     * chunkings relative to tokenization.
     */
    public boolean enforceConsistency() {
        return mEnforceConsistency;
    }

    /**
     * @inheritDoc
     */
    public Set<String> tagSet(Set<String> chunkTypes) {
        Set<String> tagSet = new HashSet<String>();
        tagSet.add(mOutTag);
        for (String chunkType : chunkTypes) {
            tagSet.add(mBeginTagPrefix + chunkType);
            tagSet.add(mInTagPrefix + chunkType);
        }
        return tagSet;
    }

    public boolean legalTagSubSequence(String... tags) {
        if (tags.length == 0)
            return true;
        if (tags.length == 1)
            return legalTagSingle(tags[0]);
        for (int i = 1; i < tags.length; ++i)
            if (!legalTagPair(tags[i-1],tags[i]))
                return false;
        return true;
    }

    public boolean legalTags(String... tags) {
        return legalTagSubSequence(tags)
            && (tags.length == 0 || !tags[0].startsWith(mInTagPrefix));
    }

    public Chunking toChunking(StringTagging tagging) {
        enforceConsistency(tagging);
        ChunkingImpl chunking = new ChunkingImpl(tagging.characters());
        for (int n = 0; n < tagging.size(); ++n) {
            String tag = tagging.tag(n);
            if (mOutTag.equals(tag)) continue;
            if (!tag.startsWith(mBeginTagPrefix)) {
                if (n == 0) {
                    String msg = "First tag must be out or begin."
                        + " Found tagging.tag(0)=" + tagging.tag(0);
                    throw new IllegalArgumentException(msg);
                }
                String msg = "Illegal tag sequence."
                    + " tagging.tag(" + (n-1) + ")=" + tagging.tag(n-1)
                    + " tagging.tag(" + n + ")=" + tagging.tag(n);
                throw new IllegalArgumentException(msg);
            }
            String type = tag.substring(2);
            int start = tagging.tokenStart(n);
            String inTag = mInTagPrefix + type;
            while ((n+1) < tagging.size() && inTag.equals(tagging.tag(n+1)))
                ++n;
            int end = tagging.tokenEnd(n);
            Chunk chunk = ChunkFactory.createChunk(start,end,type);
            chunking.add(chunk);
        }
        return chunking;
    }

    /**
     * @inheritDoc
     *
     * @throws UnsupportedOperationException If the tokenizer factory is null.
     */
    public StringTagging toStringTagging(Chunking chunking) {
        if (mTokenizerFactory == null) {
            String msg = "Tokenizer factory must be non-null to convert chunking to tagging.";
            throw new UnsupportedOperationException(msg);
        }
        enforceConsistency(chunking);
        List<String> tokenList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        List<Integer> tokenStartList = new ArrayList<Integer>();
        List<Integer> tokenEndList = new ArrayList<Integer>();
        toTagging(chunking,tokenList,tagList,
                  tokenStartList,tokenEndList);
        StringTagging tagging = new StringTagging(tokenList,
                                                  tagList,
                                                  chunking.charSequence(),
                                                  tokenStartList,
                                                  tokenEndList);
        return tagging;
    }

    /**
     * @inheritDoc
     *
     * @throws UnsupportedOperationException If the tokenizer factory is null.
     */
    public Tagging<String> toTagging(Chunking chunking) {
        if (mTokenizerFactory == null) {
            String msg = "Tokenizer factory must be non-null to convert chunking to tagging.";
            throw new UnsupportedOperationException(msg);
        }
        enforceConsistency(chunking);
        List<String> tokens = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        toTagging(chunking,tokens,tags,null,null);
        return new Tagging<String>(tokens,tags);
    }

    public Iterator<Chunk> nBestChunks(TagLattice<String> lattice,
                                       int[] tokenStarts,
                                       int[] tokenEnds,
                                       int maxResults) {
        if (maxResults < 0) {
            String msg = "Require non-negative number of results.";
            throw new IllegalArgumentException(msg);
        }
        if (tokenStarts.length != lattice.numTokens()) {
            String msg = "Token starts must line up with num tokens."
                + " Found tokenStarts.length=" + tokenStarts.length
                + " lattice.numTokens()=" + lattice.numTokens();
            throw new IllegalArgumentException(msg);
        }
        if (tokenEnds.length != lattice.numTokens()) {
            String msg = "Token ends must line up with num tokens."
                + " Found tokenEnds.length=" + tokenEnds.length
                + " lattice.numTokens()=" + lattice.numTokens();
            throw new IllegalArgumentException(msg);
        }
        for (int i = 1; i < tokenStarts.length; ++i) {
            if (tokenStarts[i-1] > tokenStarts[i]) {
                String msg = "Token starts must be in order."
                    + " Found tokenStarts[" + (i-1) + "]=" + tokenStarts[i-1]
                    + " tokenStarts[" + i + "]=" + tokenStarts[i];
                throw new IllegalArgumentException(msg);
            }
            if (tokenEnds[i-1] > tokenEnds[i]) {
                String msg = "Token ends must be in order."
                    + " Found tokenEnds[" + (i-1) + "]=" + tokenEnds[i-1]
                    + " tokenEnds[" + i + "]=" + tokenEnds[i];
                throw new IllegalArgumentException(msg);
            }
        }
        if (lattice.numTags() == 0) {
            return Iterators.<Chunk>empty();
        }
        for (int i = 0; i < tokenStarts.length; ++i) {
            if (tokenStarts[i] > tokenEnds[i]) {
                String msg = "Token ends must not precede starts."
                    + " Found tokenStarts[" + i + "]=" + tokenStarts[i]
                    + " tokenEnds[" + i + "]=" + tokenEnds[i];
                throw new IllegalArgumentException(msg);
            }
        }
        return new NBestIterator(lattice,tokenStarts,tokenEnds,maxResults,
                                 mBeginTagPrefix,mInTagPrefix,mOutTag);
    }

    /**
     * Return a string-based representation of this codec.
     *
     * @return A string-based representation of this codec.
     */
    public String toString() {
        return "BioTagChunkCodec";
    }

    static class NBestIterator extends Iterators.Buffered<Chunk> {
        private final String mBeginTagPrefix;
        private final String mInTagPrefix;
        private final String mOutTag;
        private final TagLattice<String> mLattice;
        private final int[] mTokenStarts;
        private final int[] mTokenEnds;
        private final BoundedPriorityQueue<Chunk> mChunkQueue;
        private final BoundedPriorityQueue<NBestState> mStateQueue;
        private final int mMaxResults;
        private final String[] mChunkTypes;
        private final int[] mBeginTagIds;
        private final int[] mInTagIds;
        private final int mOutTagId;
        private int mNumResults = 0;
        public NBestIterator(TagLattice<String> lattice,
                             int[] tokenStarts,
                             int[] tokenEnds,
                             int maxResults,
                             String beginTagPrefix,
                             String inTagPrefix,
                             String outTag) {
            mBeginTagPrefix = beginTagPrefix;
            mInTagPrefix = inTagPrefix;
            mOutTag = outTag;

            mLattice = lattice;
            mTokenStarts = tokenStarts;
            mTokenEnds = tokenEnds;
            mMaxResults = maxResults;
            Set<String> chunkTypeSet = new HashSet<String>();
            SymbolTable tagSymbolTable = lattice.tagSymbolTable();
            for (int k = 0; k < lattice.numTags(); ++k)
                if (lattice.tag(k).startsWith(mInTagPrefix))
                    chunkTypeSet.add(lattice.tag(k).substring(mInTagPrefix.length()));
            mChunkTypes = chunkTypeSet.toArray(Strings.EMPTY_STRING_ARRAY);
            mBeginTagIds = new int[mChunkTypes.length];
            mInTagIds = new int[mChunkTypes.length];
            for (int j = 0; j < mChunkTypes.length; ++j) {
                mBeginTagIds[j] = tagSymbolTable.symbolToID(mBeginTagPrefix + mChunkTypes[j]);
                mInTagIds[j] = tagSymbolTable.symbolToID(mInTagPrefix + mChunkTypes[j]);
            }
            mOutTagId = tagSymbolTable.symbolToID(mOutTag);

            mStateQueue = new BoundedPriorityQueue<NBestState>(ScoredObject.comparator(),
                                                               maxResults);
            mChunkQueue = new BoundedPriorityQueue<Chunk>(ScoredObject.comparator(),
                                                          maxResults);

            double[] nonContBuf = new double[lattice.numTags()-1];
            for (int j = 0; j < mChunkTypes.length; ++j) {
                int lastN = lattice.numTokens() - 1;
                if (lastN < 0) continue;
                String chunkType = mChunkTypes[j];
                int inTagId = mInTagIds[j];
                int beginTagId = mBeginTagIds[j];
                // System.out.println("beginTagId=" + beginTagId + " lastN=" + lastN);
                mChunkQueue.offer(ChunkFactory.createChunk(mTokenStarts[lastN],
                                                           mTokenEnds[lastN],
                                                           chunkType,
                                                           lattice.logProbability(lastN,beginTagId)));
                if (lastN > 0) {
                    mStateQueue.offer(new NBestState(lattice.logBackward(lastN,inTagId),
                                                     lastN,lastN,j));
                }
                for (int n = 0; n < lastN; ++n) {
                    double nonCont = nonContLogSumExp(j,n,beginTagId,lattice,nonContBuf);
                    mChunkQueue.offer(ChunkFactory.createChunk(mTokenStarts[n],
                                                               mTokenEnds[n],
                                                               chunkType,
                                                               nonCont + lattice.logForward(n,beginTagId)
                                                               - lattice.logZ()));
                }
                for (int n = 1; n < lastN; ++n) {
                    double nonCont = nonContLogSumExp(j,n,inTagId,lattice,nonContBuf);
                    mStateQueue.offer(new NBestState(nonCont,n,n,j));
                }
            }
        }
        double nonContLogSumExp(int j, int n, int fromTagId, TagLattice<String> lattice, double[] nonContBuf) {
            nonContBuf[0] = lattice.logBackward(n+1,mOutTagId)
                + lattice.logTransition(n,fromTagId,mOutTagId);
            int bufPos = 1;
            for (int j2 = 0; j2 < mBeginTagIds.length; ++j2) {
                nonContBuf[bufPos++] = lattice.logBackward(n+1,mBeginTagIds[j2])
                    + lattice.logTransition(n,fromTagId,mBeginTagIds[j2]);
                if (j == j2) continue;
                nonContBuf[bufPos++] = lattice.logBackward(n+1,mInTagIds[j2])
                    + lattice.logTransition(n,fromTagId,mInTagIds[j2]);
            }
            double result = com.aliasi.util.Math.logSumOfExponentials(nonContBuf);
            // System.out.print("nonCont j=" + j + " n=" + n + " fromId=" + fromTagId + " result=" + result + "  ");
            // for (int i = 0; i < nonContBuf.length; ++i) System.out.print(nonContBuf[i] + ", ");
            // System.out.println();
            return result;
        }

        public Chunk bufferNext() {
            if (mNumResults >= mMaxResults)
                return null;
            search();
            Chunk chunk = mChunkQueue.poll();
            if (chunk == null)
                return null;
            ++mNumResults;
            return chunk;
        }
        void search() {
            while ((!mStateQueue.isEmpty())
                   && (mChunkQueue.isEmpty()
                       || mChunkQueue.peek().score() < mStateQueue.peek().score())) {
                NBestState state = mStateQueue.poll();
                extend(state);
            }
        }
        void extend(NBestState state) {
            // System.out.println("\nextend " + state + "\n");
            int beginTagId = mBeginTagIds[state.mChunkId];
            int inTagId = mInTagIds[state.mChunkId];
            mChunkQueue.offer(ChunkFactory.createChunk(mTokenStarts[state.mPos-1],
                                                       mTokenEnds[state.mEndPos],
                                                       mChunkTypes[state.mChunkId],
                                                       state.score()
                                                       + mLattice.logForward(state.mPos-1,beginTagId)
                                                       + mLattice.logTransition(state.mPos-1,beginTagId,inTagId)
                                                       - mLattice.logZ()));
            if (state.mPos > 1)
                mStateQueue.offer(new NBestState(state.score()
                                                 + mLattice.logTransition(state.mPos-1,inTagId,inTagId),
                                                 state.mPos - 1,
                                                 state.mEndPos,
                                                 state.mChunkId));
        }
        static class NBestState implements Scored {
            private final double mScore;
            private final int mPos;
            private final int mEndPos;
            private int mChunkId;
            public NBestState(double score, int pos, int endPos,
                              int chunkId) {
                mScore = score;
                mPos = pos;
                mEndPos = endPos;
                mChunkId = chunkId;
            }
            public double score() {
                return mScore;
            }
            @Override
            public String toString() {
                return "score=" + mScore + " pos=" + mPos + " end=" + mEndPos + " id=" + mChunkId;
            }
        }
    }



    void enforceConsistency(StringTagging tagging) {
        if (!mEnforceConsistency) return;
        StringBuilder sb = new StringBuilder();
        if (isDecodable(tagging,sb)) return;
        throw new IllegalArgumentException(sb.toString());
    }

    void enforceConsistency(Chunking chunking) {
        if (!mEnforceConsistency) return;
        StringBuilder sb = new StringBuilder();
        if (isEncodable(chunking,sb)) return;
        throw new IllegalArgumentException(sb.toString());
    }

    boolean legalTagSingle(String tag) {
        return mOutTag.equals(tag)
            || tag.startsWith(mBeginTagPrefix)
            || tag.startsWith(mInTagPrefix);
    }
    boolean legalTagPair(String tag1, String tag2) {
        // B_X, I_X -> I_X, B_Y, O
        // O -> B_Y, O
        if (!legalTagSingle(tag1)) 
            return false;
        if (!legalTagSingle(tag2)) 
            return false;
        if (tag2.startsWith(mInTagPrefix))
            return tag1.endsWith(tag2.substring(mInTagPrefix.length()));
        return true;
    }

    void toTagging(Chunking chunking,
                   List<String> tokenList,
                   List<String> tagList,
                   List<Integer> tokenStartList,
                   List<Integer> tokenEndList) {
        char[] cs = Strings.toCharArray(chunking.charSequence());
        Set<Chunk> chunkSet = chunking.chunkSet();
        Chunk[] chunks = chunkSet.toArray(new Chunk[chunkSet.size()]);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
        int pos = 0;
        for (Chunk chunk : chunks) {
            String type = chunk.type();
            int start = chunk.start();
            int end = chunk.end();
            outBioTag(cs,pos,start,tokenList,tagList,tokenStartList,tokenEndList);
            chunkBioTag(cs,type,start,end,tokenList,tagList,tokenStartList,tokenEndList);
            pos = end;
        }
        outBioTag(cs,pos,cs.length,tokenList,tagList,tokenStartList,tokenEndList);
    }

    void outBioTag(char[] cs, int start, int end,
                   List<String> tokenList, List<String> tagList,
                   List<Integer> tokenStartList, List<Integer> tokenEndList) {
        int length = end - start;
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,length);
        String token;
        while ((token = tokenizer.nextToken()) != null) {
            tokenList.add(token);
            addOffsets(tokenizer,start,tokenStartList,tokenEndList);
            tagList.add(mOutTag);
        }
    }

    void chunkBioTag(char[] cs, String type, int start, int end,
                     List<String> tokenList, List<String> tagList,
                     List<Integer> tokenStartList, List<Integer> tokenEndList) {
        int length = end - start;
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,length);
        String firstToken = tokenizer.nextToken();
        if (firstToken == null) {
            String msg = "Chunks must contain at least one token."
                + " Found chunk with yield=|" + new String(cs,start,length) + "|";
            throw new IllegalArgumentException(msg);
        }
        tokenList.add(firstToken);
        addOffsets(tokenizer,start,tokenStartList,tokenEndList);
        String beginTag = mBeginTagPrefix + type;
        tagList.add(beginTag);
        String inTag = mInTagPrefix + type;
        String token;
        while ((token = tokenizer.nextToken()) != null) {
            tokenList.add(token);
            addOffsets(tokenizer,start,tokenStartList,tokenEndList);
            tagList.add(inTag);
        }
    }

    void addOffsets(Tokenizer tokenizer,
                    int offset,
                    List<Integer> tokenStartList, List<Integer> tokenEndList) {
        if (tokenStartList == null) return;
        int start = tokenizer.lastTokenStartPosition() + offset;
        int end = tokenizer.lastTokenEndPosition() + offset;
        tokenStartList.add(start);
        tokenEndList.add(end);
    }

    Object writeReplace() {
        return new Serializer(this);
    }



    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = -2473387657606045149L;
        private final BioTagChunkCodec mCodec;
        public Serializer() {
            this(null);
        }
        public Serializer(BioTagChunkCodec codec) {
            mCodec = codec;
        }
        public void writeExternal(ObjectOutput out)
            throws IOException {

            out.writeBoolean(mCodec.mEnforceConsistency);
            out.writeObject(Boolean.TRUE); // signals 3.9.2 encoding for bw compatibility
            out.writeObject(mCodec.mTokenizerFactory != null
                            ? mCodec.mTokenizerFactory
                            : Boolean.FALSE); // false just a dummy object
            out.writeUTF(mCodec.mBeginTagPrefix);
            out.writeUTF(mCodec.mInTagPrefix);
            out.writeUTF(mCodec.mOutTag);
        }
        public Object read(ObjectInput in)
            throws IOException, ClassNotFoundException {

            boolean enforceConsistency = in.readBoolean();
            Object obj = in.readObject();
            if (Boolean.TRUE.equals(obj)) {
                // 3.9.2 serialization
                Object obj2 = in.readObject();
                @SuppressWarnings("unchecked")
                TokenizerFactory tf
                    = Boolean.FALSE.equals(obj2)
                    ? null 
                    : (TokenizerFactory) obj2;
                String beginTagPrefix = in.readUTF();
                String inTagPrefix = in.readUTF();
                String outTag = in.readUTF();
                return new BioTagChunkCodec(tf,enforceConsistency,
                                            beginTagPrefix,inTagPrefix,outTag);
                
            }
            // pre 3.9.2 serialization for backward compatibility
            @SuppressWarnings("unchecked")
            TokenizerFactory tf
                = Boolean.FALSE.equals(obj)
                ? null 
                : (TokenizerFactory) obj;
            return new BioTagChunkCodec(tf,enforceConsistency);
        }
    }

    /**
     * Default name of out tag, {@code "O"}.
     */
    public static final String OUT_TAG = "O";

    /**
     * Default prefix for begin tags, {@code "B_"}.
     */
    public static final String BEGIN_TAG_PREFIX = "B_";

    /**
     * Default prefix for continuation tags, {@code "I_"}.
     */
    public static final String IN_TAG_PREFIX = "I_";

    static final int PREFIX_LENGTH = 2;

}