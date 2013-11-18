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

import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * A <code>TrainTokenShapeChunker</code> is used to train a token and
 * shape-based chunker.
 *
 * <p> Estimation is based on a joint model of tags
 * <code>T1,...,TN</code> and tokens <code>W1,...,WN</code>, which is
 * approximated with a limited history and smoothed using linear
 * interpolation.
 * <br/><br/>
 * <table cellpadding="10" border="1"><tr><td width="100%">
 *  By the chain rule:
 * <pre>
 *   P(W1,...,WN,T1,...TN)
 *       = P(W1,T1) * P(W2,T2|W1,T1) * P(W3,T3|W1,W2,T1,T2)
 *         * ... * P(WN,TN|W1,...,WN-1,T1,...,TN-1)
 * </pre>
 * The longer contexts are approximated with the two previous
 * tokens and one previous tag.
 * <pre>
 *   P(WN,TN|W1,...,WN-1,T1,...,TN-1)
 *       ~ P(WN,TN|WN-2,WN-1,TN-1)
 * </pre>
 * The shorter contexts are padded with tags and tokens for the
 * beginning of a stream, and an addition end-of-stream symbol is
 * trained after the last symbol in the input.
 * The joint model is further decomposed into a conditional tag model
 * and a conditional token model by the chain rule:
 * <pre>
 *    P(WN,TN|WN-2,WN-1,TN-1)
 *        = P(TN|WN-2,WN-1,TN-1)
 *          * P(WN|WN-2,WN-1,TN-1,TN)
 * </pre>
 * The token model is further approximated as:
 * <pre>
 *   P(WN|WN-2,WN-1,TN-1,TN)
 *       ~ P(WN|WN-1,interior(TN-1),TN)
 * </pre>
 * where <code>interior(TN-1)</code> is the interior
 * version of a tag; for instance:
 * <pre>
 *   interior("ST_PERSON").equals("PERSON")
 *   interior("PERSON").equals("PERSON")
 * </pre>
 * This performs what is known as "model tying", and it
 * amounts to sharing the models for the two contexts.
 * The tag model is also approximated by tying start
 * and interior tag histories:
 * <pre>
 *   P(TN|WN-2,WN-1,TN-1)
 *       ~ P(TN|WN-2,WN-1,interior(TN-1))
 * </pre>
 * The tag and token models are themselves simple
 * linear interpolation models, with smoothing parameters defined
 * by the Witten-Bell method.  The order
 * of contexts for the token model is:
 * <pre>
 *   P(WN|TN,interior(TN-1),WN-1)
 *   ~ lambda(TN,interior(TN-1),WN-1) * P_ml(WN|TN,interior(TN-1),WN-1)
 *     + (1-lambda(")) * P(WN|TN,interior(TN-1))
 *
 *   P(WN|TN,interior(TN-1))
 *   ~ lambda(TN,interior(TN-1)) * P_ml(WN|TN,interior(TN-1))
 *     + (1-lambda(")) * P(WN|TN)
 *
 *   P(WN|TN)  ~  lambda(TN) * P_ml(WN|TN)
 *                + 1-lambda(") * UNIFORM_ESTIMATE
 * </pre>
 *
 * The last step is degenerate in that <code>SUM_W P(W|T) =
 * INFINITY</code>, because there are infinitely many possible tokens,
 * and each is assigned the uniform estimate.  To fix this, a model
 * would be needed of character sequences that ensured <code>SUM_W
 * P(W|T) = 1.0</code>.  (The steps to do the final uniform estimate
 * are handled by the compiled estimator.)
 * <br/><br/>
 * The tag estimator is smoothed by:
 * <pre>
 *   P(TN|interior(TN-1),WN-1,WN-2)
 *       ~ lambda(interior(TN-1),WN-1,WN-2) * P_ml(TN|interior(TN-1),WN-1,WN-2)
 *       + (1-lambda(")) * P(TN|interior(TN-1),WN-1)
 *
 *  P(TN|interior(TN-1),WN-1)
 *      ~ lambda(interior(TN-1),WN-1) * P_ml(TN|interior(TN-1),WN-1)
 *      + (1-lambda("))               * P_ml(TN|interior(TN-1))
 * </pre>
 *
 * Note that the smoothing stops at estimating a tag in terms
 * of the previous tags.  This guarantees that only bigram tag
 * sequences seen in the training data get non-zero probability
 * under the estimator.
 * </p>
 *
 * </td></tr></table>
 *
 * <p> Sequences of training pairs are added via {@link
 * #handle(Chunking)} method.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe1.0
 */
public class TrainTokenShapeChunker
    implements ObjectHandler<Chunking>, 
               Compilable {

    private final boolean mValidateTokenizer = false;

    private final int mKnownMinTokenCount;
    private final int mMinTokenCount;
    private final int mMinTagCount;
    private final TokenCategorizer mTokenCategorizer;
    private final TokenizerFactory mTokenizerFactory;

    private final TrainableEstimator mTrainableEstimator;
    private final List<String> mTokenList = new ArrayList<String>();
    private final List<String> mTagList = new ArrayList<String>();

    /**
     * Construct a trainer for a token/shape chunker based on
     * the specified token categorizer and tokenizer factory.
     * The other parameters receive default vaules.  The
     * interpolation ratio is set to <code>4.0</code>, the
     * number of tokens to <code>3,000,000</code>, the
     * known minimum token count to 8, and the min tag and
     * token count for pruning to 1.
     *
     * @param categorizer Token categorizer for unknown tokens.
     * @param factory Tokenizer factory for creating tokenizers.
     */
    public TrainTokenShapeChunker(TokenCategorizer categorizer,
                                  TokenizerFactory factory) {
        this(categorizer,factory,
             8, 1, 1);
    }

    /**
     * Construct a trainer for a token/shape chunker based on
     * the specified token categorizer, tokenizer factory and
     * numerical parameters.  The parameters are described in
     * detail in the class documentation above.
     *
     * @param categorizer Token categorizer for unknown tokens.
     * @param factory Tokenizer factory for tokenizing data.
     * @param knownMinTokenCount Number of instances required for
     * a token to count as known for unknown training.
     * @param minTokenCount Minimum token count for token contexts to
     * survive after pruning.
     * @param minTagCount Minimum count for tag contexts to survive
     * after pruning.
     */
    public TrainTokenShapeChunker(TokenCategorizer categorizer,
                                  TokenizerFactory factory,
                                  int knownMinTokenCount,
                                  int minTokenCount,
                                  int minTagCount) {
        mTokenCategorizer = categorizer;
        mTokenizerFactory = factory;
        mKnownMinTokenCount = knownMinTokenCount;
        mMinTokenCount = minTokenCount;
        mMinTagCount = minTagCount;
        mTrainableEstimator = new TrainableEstimator(categorizer);
    }

    /**
     * Trains the underlying estimator on the specified BIO-encoded
     * chunk tagging.
     *
     * @param tokens Sequence of tokens to train.
     * @param whitespaces Sequence of whitespaces (ignored).
     * @param tags Sequence of tags to train.
     * @throws IllegalArgumentException If the tags and tokens are
     * different lengths.
     * @throws NullPointerException If any of the tags or tokens are null.
     */
    void handle(String[] tokens, String[] whitespaces, String[] tags) {

        if (tokens.length != tags.length) {
            String msg = "Tokens and tags must be same length."
                + " Found tokens.length=" + tokens.length
                + " tags.length=" + tags.length;
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < tokens.length; ++i) {
            if (tokens[i] == null || tags[i] == null) {
                String msg = "Tags and tokens must not be null."
                    + " Found tokens[" + i + "]=" + tokens[i]
                    + " tags[" + i + "]=" + tags[i];
                throw new NullPointerException(msg);
            }
            mTokenList.add(tokens[i]);
            mTagList.add(tags[i]);
        }
    }

    // cut and paste from old adapter; another copy in CharLmHmmChunker

    /**
     * Add the specified chunking as a training event.
     *
     * @param chunking Chunking for training.
     */
    public void handle(Chunking chunking) {
        CharSequence cSeq = chunking.charSequence();
        char[] cs = Strings.toCharArray(cSeq);

        Set<Chunk> chunkSet = chunking.chunkSet();
        Chunk[] chunks = chunkSet.<Chunk>toArray(EMPTY_CHUNK_ARRAY);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);

        List<String> tokenList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        int pos = 0;
        for (Chunk nextChunk : chunks) {
            String type = nextChunk.type();
            int start = nextChunk.start();
            int end = nextChunk.end();
            outTag(cs,pos,start,tokenList,whiteList,tagList,mTokenizerFactory);
            chunkTag(cs,start,end,type,tokenList,whiteList,tagList,mTokenizerFactory);
            pos = end;
        }
        outTag(cs,pos,cSeq.length(),tokenList,whiteList,tagList,mTokenizerFactory);
        String[] toks = tokenList.<String>toArray(Strings.EMPTY_STRING_ARRAY);
        String[] whites = whiteList.toArray(Strings.EMPTY_STRING_ARRAY);
        String[] tags = tagList.toArray(Strings.EMPTY_STRING_ARRAY);
        if (mValidateTokenizer
            && !consistentTokens(toks,whites,mTokenizerFactory)) {
            String msg = "Tokens not consistent with tokenizer factory."
                + " Tokens=" + Arrays.asList(toks)
                + " Tokenization=" + tokenization(toks,whites)
                + " Factory class=" + mTokenizerFactory.getClass();
            throw new IllegalArgumentException(msg);
        }
        handle(toks,whites,tags);
    }

    /**
     * Compiles a chunker based on the training data received by
     * this trainer to the specified object output.
     *
     * @param objOut Object output to which the chunker is written.
     * @throws IOException If there is an underlying I/O error.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 142720610674437597L;
        final TrainTokenShapeChunker mChunker;
        public Externalizer() {
            this(null);
        }
        public Externalizer(TrainTokenShapeChunker chunker) {
            mChunker = chunker;
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            TokenizerFactory factory = (TokenizerFactory) in.readObject();
            TokenCategorizer categorizer = (TokenCategorizer) in.readObject();

            // System.out.println("factory.class=" + factory.getClass());
            // System.out.println("categorizer.class=" + categorizer.getClass());

            CompiledEstimator estimator = (CompiledEstimator) in.readObject();
            // System.out.println("estimator.class=" + estimator.getClass());
            TokenShapeDecoder decoder
                = new TokenShapeDecoder(estimator,categorizer,1000.0);

            return new TokenShapeChunker(factory,decoder);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            int len = mChunker.mTagList.size();
            String[] tokens
                = mChunker.mTokenList.<String>toArray(Strings.EMPTY_STRING_ARRAY);
            String[] tags
                = mChunker.mTagList.<String>toArray(Strings.EMPTY_STRING_ARRAY);

            // train once with straight vals
            mChunker.mTrainableEstimator.handle(tokens,tags);

            // train again with unknown tokens replaced with categories
            mChunker.replaceUnknownsWithCategories(tokens);
            mChunker.mTrainableEstimator.handle(tokens,tags);
            mChunker.mTrainableEstimator.prune(mChunker.mMinTagCount,
                                               mChunker.mMinTokenCount);
            // smoothe after prune for persistence
            mChunker.mTrainableEstimator.smoothTags(1);

            // write: tokfact, tokcat, estimator
            AbstractExternalizable.compileOrSerialize(mChunker.mTokenizerFactory,objOut);
            AbstractExternalizable.compileOrSerialize(mChunker.mTokenCategorizer,objOut);
            mChunker.mTrainableEstimator.compileTo(objOut);
        }
    }

    // copied from old adapter; another copy in CharLmHmmChunker

    void replaceUnknownsWithCategories(String[] tokens) {
        ObjectToCounterMap<String> counter = new ObjectToCounterMap<String>();
        for (int i = 0; i < tokens.length; ++i)
            counter.increment(tokens[i]);
        for (int i = 0; i < tokens.length; ++i)
            if (counter.getCount(tokens[i]) < mKnownMinTokenCount)
                tokens[i] = mTokenCategorizer.categorize(tokens[i]);
    }

    static final Chunk[] EMPTY_CHUNK_ARRAY = new Chunk[0];

    static void outTag(char[] cs, int start, int end,
                       List<String> tokenList, List<String> whiteList, List<String> tagList,
                       TokenizerFactory factory) {
        Tokenizer tokenizer = factory.tokenizer(cs,start,end-start);
        whiteList.add(tokenizer.nextWhitespace());
        String nextToken;
        while ((nextToken = tokenizer.nextToken()) != null) {
            tokenList.add(nextToken);
            tagList.add(ChunkTagHandlerAdapter2.OUT_TAG);
            whiteList.add(tokenizer.nextWhitespace());
        }

    }

    static void chunkTag(char[] cs, int start, int end, String type,
                         List<String> tokenList, List<String> whiteList, List<String> tagList,
                         TokenizerFactory factory) {
        Tokenizer tokenizer = factory.tokenizer(cs,start,end-start);
        String firstToken = tokenizer.nextToken();
        tokenList.add(firstToken);
        tagList.add(ChunkTagHandlerAdapter2.BEGIN_TAG_PREFIX + type);
        while (true) {
            String nextWhitespace = tokenizer.nextWhitespace();
            String nextToken = tokenizer.nextToken();
            if (nextToken == null) break;
            tokenList.add(nextToken);
            whiteList.add(nextWhitespace);
            tagList.add(ChunkTagHandlerAdapter2.IN_TAG_PREFIX + type);
        }
    }

    static boolean consistentTokens(String[] toks,
                                    String[] whitespaces,
                                    TokenizerFactory tokenizerFactory) {
        if (toks.length+1 != whitespaces.length) return false;
        char[] cs = getChars(toks,whitespaces);
        Tokenizer tokenizer = tokenizerFactory.tokenizer(cs,0,cs.length);
        String nextWhitespace = tokenizer.nextWhitespace();
        if (!whitespaces[0].equals(nextWhitespace)) {
            return false;
        }
        for (int i = 0; i < toks.length; ++i) {
            String token = tokenizer.nextToken();
            if (token == null) {
                return false;
            }
            if (!toks[i].equals(token)) {
                return false;
            }
            nextWhitespace = tokenizer.nextWhitespace();
            if (!whitespaces[i+1].equals(nextWhitespace)) {
                return false;
            }
        }
        return true;
    }

    List<String> tokenization(String[] toks, String[] whitespaces) {
        List<String> tokList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        char[] cs = getChars(toks,whitespaces);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        tokenizer.tokenize(tokList,whiteList);
        return tokList;
    }

    static char[] getChars(String[] toks, String[] whitespaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toks.length; ++i) {
            sb.append(whitespaces[i]);
            sb.append(toks[i]);
        }
        sb.append(whitespaces[whitespaces.length-1]);
        return Strings.toCharArray(sb);
    }

}




