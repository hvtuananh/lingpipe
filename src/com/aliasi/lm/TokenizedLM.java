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

import com.aliasi.symbol.SymbolTable;
import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.stats.BinomialDistribution;
import com.aliasi.stats.Statistics;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
// import com.aliasi.util.Arrays;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Exceptions;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * A <code>TokenizedLM</code> provides a dynamic sequence language
 * model which models token sequences with an n-gram model, and
 * whitespace and unknown tokens with their own sequence language
 * models.
 *
 * <P>A tokenized language model factors the probability assigned to a
 * character sequence as follows:
 *
 * <blockquote><code>
 * P(cs)
 * = P<sub><sub>tok</sub></sub>(toks(cs))
 * <big><big><big>&Pi;</big></big></big><sub><sub>t in unknownToks(cs)</sub></sub>
 *  P<sub><sub>unk</sub></sub>(t)
 * <big><big><big>&Pi;</big></big></big><sub><sub>w in whitespaces(cs)</sub></sub>
 *   P<sub><sub>whsp</sub></sub>(w)
 * </code></blockquote>
 *
 * where
 *
 * <UL>

 * <LI> <code>P<sub><sub>tok</sub></sub></code> is the token model
 * esimate and where <code>toks(cs)</code> replaces known tokens with
 * their integer identifiers, unknown tokens with <code>-1</code> and
 * adds boundary symbols <code>-2</code> front and back, the same
 * adjustment is used to remove the initial boundary estimate as in
 * {@link NGramBoundaryLM};
 *
 * <LI> <code>P<sub><sub>unk</sub></sub></code> is the unknown token
 * sequence language model and <code>unknownToks(cs)</code> is the
 * list of unknown tokens in the input (with duplicates); and
 *
 * <LI> <code>P<sub><sub>whsp</sub></sub></code> is the whitespace sequence
 * language model and <code>whitespaces(cs)</code> is the list of
 * whitespaces in the character sequence (with duplicates).
 *
 * </UL>
 *
 * <P>The token n-gram model itself uses the same method of counting
 * and smoothing as described in the class documentation for {@link
 * NGramProcessLM}.  Like {@link NGramBoundaryLM}, boundary tokens are
 * inserted before and after other tokens.  And like the n-gram
 * character boundary model, the initial boundary estimate is subtracted
 * from the overall estimate for normalization purposes.
 *
 * <P>Tokens are all converted to integer identifiers using an
 * internal dynamic symbol table.  All symbols in symbol tables get
 * non-negative identifiers; the negative value <code>-1</code> is
 * used for the unknown token in models, just as in symbol tables.
 * The value <code>-2</code> is used for the boundary marker in the
 * counters.
 *
 * <P>In order for all estimates to be non-zero, the integer
 * sequence counter used to back the token model is initialized
 * with a count of 1 for the end-of-stream identifier (-2).  The
 * unknown token count for any context is taken to be the number
 * of outcomes in that context.  Because unknowns are estimated
 * directly in this manner, there is no need to interpolate the
 * unigram model with a uniform model for unknown outcome.  Instead,
 * the occurrence of an unknown is modeled directly and its
 * identity is modeled by the unknown token language model.
 *
 * <P>In order to produce a properly normalized sequence model, the
 * concatenation of tokens and whitespaces returned by the tokenizer
 * should concatenate together to produce the original input.  Note
 * that this condition is <i>not</i> checked at runtime.  But,
 * sequences may be normalized before being trained and evaluated for
 * a language model.  For instance, all alphabetic characters might be
 * reduced to lower case and all punctuation characters removed and
 * all non-empty sequences of whitespace reduced to a single space
 * character.  A langauge model may then be defined over this
 * normalized space of input, not the original space (and may thus use
 * a reduced number of characters for its uniform estimates).
 * Although this normalization may be carried out by a tokenizer in
 * practice, for instance for use in a tokenized classifier, an
 * normalization is consistent the interface specification for {@link
 * LanguageModel.Sequence} or {@link LanguageModel.Dynamic} only if
 * done on the outside.
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 */
public class TokenizedLM
    implements LanguageModel.Dynamic,
               LanguageModel.Sequence,
               LanguageModel.Tokenized,
               ObjectHandler<CharSequence> {

    private final TokenizerFactory mTokenizerFactory;
    private final MapSymbolTable mSymbolTable;
    private final TrieIntSeqCounter mCounter;
    private final LanguageModel.Sequence mUnknownTokenModel;
    private final LanguageModel.Sequence mWhitespaceModel;
    private final double mLambdaFactor;

    private final LanguageModel.Dynamic mDynamicUnknownTokenModel;
    private final LanguageModel.Dynamic mDynamicWhitespaceModel;

    private final int mNGramOrder;

    /**
     * Constructs a tokenized language model with the specified
     * tokenization factory and n-gram order (see warnings below on
     * where this simple constructor may be used).  
     *
     * <p>The unknown token and whitespace models are both uniform
     * sequence language models with default parameters as described
     * in the documentation for the constructor {@link
     * UniformBoundaryLM#UniformBoundaryLM()}.  The default
     * interpolation hyperparameter is equal to the n-gram Order.
     *
     * <p><b>Warning:</b> This construction method is probably only
     * going to be useful if you are only using the tokenized LM to
     * store character n-grams.  Because it uses fat constant uniform
     * language models for smoothing tokens and whitespaces, it will
     * provide very high entropy estimates for unseen text.  The other
     * constructors allow smoothing LMs to be supplied (which will take
     * up more space to estimate, but will provide more reasonable
     * estimates).
     *
     * @param factory Tokenizer factory for the model.
     * @param nGramOrder N-gram Order.
     * @throws IllegalArgumentException If the n-gram order is less
     * than 0.
     */
    public TokenizedLM(TokenizerFactory factory,
                       int nGramOrder) {
        this(factory,
             nGramOrder,
             new UniformBoundaryLM(),
             new UniformBoundaryLM(),
             nGramOrder);
    }

    /**
     * Construct a tokenized language model with the specified
     * tokenization factory and n-gram order, sequence models for
     * unknown tokens and whitespace, and an interpolation
     * hyperparameter.
     *
     * <P>In order for this model to be serializable, the unknown
     * token and whitespace models should be serializable.  If they do
     * not, a runtime exception will be thrown when attempting to
     * serialize this model.  If these models implement {@link
     * LanguageModel.Dynamic}, they will be trained by calls to the
     * training method.
     *
     * @param tokenizerFactory Tokenizer factory for the model.
     * @param nGramOrder Length of maximum n-gram for model.
     * @param unknownTokenModel Sequence model for unknown tokens.
     * @param whitespaceModel Sequence model for all whitespace.
     * @param lambdaFactor Value of the interpolation hyperparameter.
     * @throws IllegalArgumentException If the n-gram order is less
     * than 1 or the interpolation is not a non-negative number.
     */
    public TokenizedLM(TokenizerFactory tokenizerFactory,
                       int nGramOrder,
                       LanguageModel.Sequence unknownTokenModel,
                       LanguageModel.Sequence whitespaceModel,
                       double lambdaFactor) {
        this(tokenizerFactory,nGramOrder,
             unknownTokenModel,whitespaceModel,lambdaFactor,
             true);
    }

    /**
     * Construct a tokenized language model with the specified
     * tokenization factory and n-gram order, sequence models for
     * unknown tokens and whitespace, and an interpolation
     * hyperparameter, as well as a flag indicating whether to
     * automatically increment a null input to avoid numerical
     * problems with zero counts.
     *
     * <P>In order for this model to be serializable, the unknown
     * token and whitespace models should be serializable.  If they do
     * not, a runtime exception will be thrown when attempting to
     * serialize this model.  If these models implement {@link
     * LanguageModel.Dynamic}, they will be trained by calls to the
     * training method.
     *
     * @param tokenizerFactory Tokenizer factory for the model.
     * @param nGramOrder Length of maximum n-gram for model.
     * @param unknownTokenModel Sequence model for unknown tokens.
     * @param whitespaceModel Sequence model for all whitespace.
     * @param lambdaFactor Value of the interpolation hyperparameter.
     * @param initialIncrementBoundary Flag indicating whether or not
     * to increment the subsequence <code>{&nbsp;BOUNDARY_TOKEN&nbsp;}</code>
     * automatically after construction to avoid {@code NaN} error
     * states.
     * @throws IllegalArgumentException If the n-gram order is less
     * than 1 or the interpolation is not a non-negative number.
     */
    public TokenizedLM(TokenizerFactory tokenizerFactory,
                       int nGramOrder,
                       LanguageModel.Sequence unknownTokenModel,
                       LanguageModel.Sequence whitespaceModel,
                       double lambdaFactor,
                       boolean initialIncrementBoundary) {
        NGramProcessLM.checkMaxNGram(nGramOrder);
        NGramProcessLM.checkLambdaFactor(lambdaFactor);
        mSymbolTable = new MapSymbolTable();
        mNGramOrder = nGramOrder;
        mTokenizerFactory = tokenizerFactory;
        mUnknownTokenModel = unknownTokenModel;
        mWhitespaceModel = whitespaceModel;
        mDynamicUnknownTokenModel
            = (mUnknownTokenModel instanceof LanguageModel.Dynamic)
            ? (LanguageModel.Dynamic) mUnknownTokenModel
            : null;
        mDynamicWhitespaceModel
            = (mWhitespaceModel instanceof LanguageModel.Dynamic)
            ? (LanguageModel.Dynamic) mWhitespaceModel
            : null;
        mCounter = new TrieIntSeqCounter(nGramOrder);
        mLambdaFactor = lambdaFactor;
        // following is so it starts without NaN problems
        // decrement this if necessary when not needed
        if (initialIncrementBoundary)
            mCounter.incrementSubsequences(new int[] { BOUNDARY_TOKEN },0,1);
    }

    /**
     * Returns the interpolation ratio, or lambda factor,
     * for interpolating in this tokenized language model.
     * See the class documentation above for more details.
     *
     * @return The interpolation ratio for this LM.
     */
    public double lambdaFactor() {
        return mLambdaFactor;
    }


    /**
     * Returns the integer sequence counter underlying this model.
     * Symbols are mapped to integers using the symbol table returned
     * by {@link #symbolTable()}.  Changes to this counter affect this
     * tokenized language model.
     *
     * @return The sequence counter underlying this model.
     */
    public TrieIntSeqCounter sequenceCounter() {
        return mCounter;
    }

    /**
     * Returns the symbol table underlying this tokenized language
     * model's token n-gram model.  Changes to the symbol table affect
     * this tokenized language model.
     *
     * @return The symbol table underlying this language model.
     */
    public SymbolTable symbolTable() {
        return mSymbolTable;
    }

    /**
     * Returns the order of the token n-gram model underlying this
     * tokenized language model.
     *
     * @return The order of the token n-gram model underlying this
     * tokenized language model.
     */
    public int nGramOrder() {
        return mNGramOrder;
    }

    /**
     * Returns the tokenizer factory for this tokenized language
     * model.
     *
     * @return The tokenizer factory for this tokenized language
     * model.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns the unknown token seqeunce language model for this
     * tokenized language model.  Changes to the returned language
     * model affect this tokenized language model.
     *
     * @return The unknown token language model.
     */
    public LanguageModel.Sequence unknownTokenLM() {
        return mUnknownTokenModel;
    }

    /**
     * Returns the whitespace language model for this tokenized
     * language model.  Changes to the returned language model affect
     * this tokenized language model.
     *
     * @return The whitespace language model.
     */
    public LanguageModel.Sequence whitespaceLM() {
        return mWhitespaceModel;
    }

    /**
     * Writes a compiled version of this tokenized language model to
     * the specified object output.  When the model is read back in
     * it will be an instance of {@link CompiledTokenizedLM}.
     *
     * @param objOut Object output to which a compiled version of this
     * model is written.
     * @throws IOException If there is an I/O error writing the
     * output.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    /**
     * Visits the n-grams of the specified length with at least the specified
     * minimum count stored in the underlying counter of this
     * tokenized language model and passes them to the specified handler.
     *
     * @param nGramLength Length of n-grams visited.
     * @param minCount Minimum count of a visited n-gram.
     * @param handler Handler whose handle method is called for each
     * visited n-gram.
     */
    public void handleNGrams(int nGramLength, int minCount,
                             ObjectHandler<String[]> handler) {
        StringArrayAdapter adapter = new StringArrayAdapter(handler);
        mCounter.handleNGrams(nGramLength,minCount,adapter);
    }





    double lambda(int[] tokIds) {
        double numExtensionsD = mCounter.numExtensions(tokIds,0,tokIds.length);
        double extCountD = mCounter.extensionCount(tokIds,0,tokIds.length);
        return extCountD / (extCountD + mLambdaFactor * numExtensionsD);
    }


    /**
     * Trains the token sequence model, whitespace model (if dynamic) and
     * unknown token model (if dynamic).
     *
     * @param cSeq Character sequence to train.
     */
    public void train(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        train(cs,0,cs.length);
    }

    /**
     * Trains the token sequence model, whitespace model (if dynamic) and
     * unknown token model (if dynamic) with the specified count number
     * of instances.  Calling <code>train(cs,n)</code> is equivalent to
     * calling <code>train(cs)</code> a total of <code>n</code> times.
     *
     * @param cSeq Character sequence to train.
     * @param count Number of instances to train.
     * @throws IllegalArgumentException If the count is not positive.
     */
    public void train(CharSequence cSeq, int count) {
        if (count < 0) {
            String msg = "Counts must be non-negative."
                + " Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        if (count == 0) return;
        char[] cs = Strings.toCharArray(cSeq);
        train(cs,0,cs.length,count);
    }

    /**
     * Trains the token sequence model, whitespace model (if dynamic) and
     * unknown token model (if dynamic).
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one plus last character in slice.
     * @throws IndexOutOfBoundsException If the indices are out of
     * range for the character array.
     */
    public void train(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        Tokenizer tokenizer =  mTokenizerFactory.tokenizer(cs,start,end-start);
        List<String> tokenList = new ArrayList<String>();
        while (true) {
            if (mDynamicWhitespaceModel != null) {
                String whitespace = tokenizer.nextWhitespace();
                mDynamicWhitespaceModel.train(whitespace);
            } // this'll pick up the last whitespace after last token
            String token = tokenizer.nextToken();
            if (token == null) break;
            tokenList.add(token);
        }
        int[] tokIds = new int[tokenList.size()+2];
        tokIds[0] = BOUNDARY_TOKEN;
        tokIds[tokIds.length-1] = BOUNDARY_TOKEN;
        Iterator<String> it = tokenList.iterator();
        for (int i = 1; it.hasNext(); ++i) {
            String token = it.next();
            // train underlying token model just once per token
            if (mDynamicUnknownTokenModel != null
                && mSymbolTable.symbolToID(token) < 0) {
                mDynamicUnknownTokenModel.train(token);
            }
            tokIds[i] = mSymbolTable.getOrAddSymbol(token);
        }
        mCounter.incrementSubsequences(tokIds,0,tokIds.length);
        mCounter.decrementUnigram(BOUNDARY_TOKEN);
    }


    /**
     * Trains the language model on the specified character sequence.
     *
     * <p>This method delegates to the {@link
     * #train(CharSequence,int)} method.
     * 
     * <p>This method implements the <code>ObjectHandler&lt;CharSequence&gt;</code>
     * interface.
     */
    public void handle(CharSequence cs) {
        train(cs,1);
    }

    /**
     * Trains the token sequence model, whitespace model (if dynamic) and
     * unknown token model (if dynamic).
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one plus last character in slice.
     * @param count Number of instances of sequence to train.
     * @throws IndexOutOfBoundsException If the indices are out of range for the
     * character array.
     * @throws IllegalArgumentException If the count is negative.
     */
    public void train(char[] cs, int start, int end, int count) {
        Strings.checkArgsStartEnd(cs,start,end);
        if (count < 0) {
            String msg = "Counts must be non-negative."
                + " Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        if (count == 0) return;
        Tokenizer tokenizer =  mTokenizerFactory.tokenizer(cs,start,end-start);
        List<String> tokenList = new ArrayList<String>();
        while (true) {
            if (mDynamicWhitespaceModel != null) {
                String whitespace = tokenizer.nextWhitespace();
                mDynamicWhitespaceModel.train(whitespace,count);
            } // this'll pick up the last whitespace after last token
            String token = tokenizer.nextToken();
            if (token == null) break;
            tokenList.add(token);
        }
        int[] tokIds = new int[tokenList.size()+2];
        tokIds[0] = BOUNDARY_TOKEN;
        tokIds[tokIds.length-1] = BOUNDARY_TOKEN;
        Iterator<String> it = tokenList.iterator();
        for (int i = 1; it.hasNext(); ++i) {
            String token = it.next();
            // train underlying token model just once per token
            if (mDynamicUnknownTokenModel != null
                && mSymbolTable.symbolToID(token) < 0) {
                mDynamicUnknownTokenModel.train(token,count);
            }
            tokIds[i] = mSymbolTable.getOrAddSymbol(token);
        }
        mCounter.incrementSubsequences(tokIds,0,tokIds.length,count);
        mCounter.decrementUnigram(BOUNDARY_TOKEN,count);
    }


    /**
     * This method trains the last token in the sequence given the
     * previous tokens.  See {@link #trainSequence(CharSequence, int)}
     * for more information.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one plus last character in slice.
     * @throws IndexOutOfBoundsException If the indices are out of
     * range for the character array.
     * @throws IllegalArgumentException If the count is negative.
     */
    void trainSequence(char[] cs, int start, int end, int count) {
        Strings.checkArgsStartEnd(cs,start,end);
        if (count < 0) {
            String msg = "Count must be non-negative.  Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        String[] tokens = tokenizer.tokenize();
        int len = Math.min(tokens.length,nGramOrder());
        int offset = tokens.length - len;
        int[] tokIds = new int[len];
        for (int i = 0; i < len; ++i)
            tokIds[i] = mSymbolTable.getOrAddSymbol(tokens[i+offset]);
        mCounter.incrementSequence(tokIds,0,len,count);
    }

    /**
     * This method increments the count of the entire sequence
     * specified.  Note that this method does not increment any of the
     * token subsequences and does not increment the whitespace or
     * token smoothing models.
     *
     * <p>This method may be used to train a tokenized language model
     * from individual character sequence counts.  Because the token
     * smoothing models are not implemented for this method, a pure
     * token model may be constructed by calling
     * <code>train(CharSequence,int)</code> for character sequences
     * corresponding to unigrams rather than this method in order to
     * train token smoothing with character subseuqneces.
     *
     * <p>For instance, with
     * <code>com.aliasi.tokenizer.IndoEuropeanTokenizerFactory</code>,
     * the sequence calling <code>trainSequence(&quot;the fast
     * computer&quot;,5)</code> would extract three tokens,
     * <code>the</code>, <code>fast</code> and <code>computer</code>,
     * and would increment the count of the three-token sequence, but
     * not any of its subsequences.
     *
     * <p>If the number of tokens is longer than the maximum n-gram
     * length, only the final tokens are trained. For instance, with
     * an n-gram length of 2, and the Indo-European tokenizer factory,
     * calling <code>trainSequence(&quot;a slightly faster
     * computer&quot;,93)</code> is equivalent to calling
     * <code>trainSequence(&quot;faster computer&quot;,93)</code>.
     *
     * <p>All tokens trained are added to the symbol table.  This
     * does not include any initial tokens that are not used because
     * the maximum n-gram length is too short.
     *
     * @param cSeq Character sequence to train.
     * @param count Number of instances to train.
     * @throws IllegalArgumentException If the count is negative.
     */
    public void trainSequence(CharSequence cSeq, int count) {
        char[] cs = Strings.toCharArray(cSeq);
        trainSequence(cs,0,cs.length,count);
    }

    public double log2Estimate(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return log2Estimate(cs,0,cs.length);
    }

    public double log2Estimate(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        double logEstimate = 0.0;

        // collect tokens, estimate whitespaces
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        List<String> tokenList = new ArrayList<String>();
        while (true) {
            String whitespace = tokenizer.nextWhitespace();
            logEstimate += mWhitespaceModel.log2Estimate(whitespace);
            String token = tokenizer.nextToken();
            if (token == null) break;
            tokenList.add(token);
        }

        // collect token ids, estimate unknown tokens
        int[] tokIds = new int[tokenList.size()+2];
        tokIds[0] = BOUNDARY_TOKEN;
        tokIds[tokIds.length-1] = BOUNDARY_TOKEN;
        Iterator<String> it = tokenList.iterator();
        for (int i = 1; it.hasNext(); ++i) {
            String token = it.next();
            tokIds[i] = mSymbolTable.symbolToID(token);
            if (tokIds[i] < 0) {
                logEstimate += mUnknownTokenModel.log2Estimate(token);
            }
        }

        // estimate token ids excluding start, inlcuding end
        for (int i = 2; i <= tokIds.length; ++i) {
            logEstimate += conditionalLog2TokenEstimate(tokIds,0,i);
        }
        return logEstimate;
    }

    class StringArrayAdapter implements ObjectHandler<int[]> {
        ObjectHandler<String[]> mHandler;
        public StringArrayAdapter(ObjectHandler<String[]> handler) {
            mHandler = handler;
        }
        public void handle(int[] nGram) {
            mHandler.handle(simpleNGramToTokens(nGram));
        }
        String[] simpleNGramToTokens(int[] nGram) {
            String[] tokens = new String[nGram.length];
            for (int i = 0; i < tokens.length; ++i)
                tokens[i]
                    = nGram[i] >= 0
                    ? mSymbolTable.idToSymbol(nGram[i])
                    : null;
            return tokens;
        }
    }

    abstract class Collector implements ObjectHandler<int[]> {
        final BoundedPriorityQueue<ScoredObject<String[]>> mBPQ;
        Collector(int maxReturned, boolean reverse) {
            Comparator<ScoredObject<String[]>> comparator = null;
            if (reverse)
                comparator = ScoredObject.reverseComparator();
            else
                comparator = ScoredObject.comparator();
            mBPQ = new BoundedPriorityQueue<ScoredObject<String[]>>(comparator,
                                                                    maxReturned);
        }
        SortedSet<ScoredObject<String[]>> nGramSet() {
            return mBPQ;
        }
        ScoredObject<String[]>[] nGrams() {
            // necessary for array
            return mBPQ.<ScoredObject<String[]>>toArray(EMPTY_SCORED_OBJECT_STRING_ARRAY_ARRAY);
        }
        public void handle(int[] nGram) {
            for (int i = 0; i < nGram.length; ++i)
                if (nGram[i] < 0) return;  // don't include boundaries
            mBPQ.offer(new ScoredObject<String[]>(nGramToTokens(nGram),
                                                  scoreNGram(nGram)));

        }
        abstract double scoreNGram(int[] nGram);
    }


    class FreqTermCollector extends Collector {
        FreqTermCollector(int maxReturned, boolean reverse) {
            super(maxReturned,reverse);
        }
        @Override
        double scoreNGram(int[] nGram) {
            return mCounter.count(nGram,0,nGram.length);
        }
    }

    class CollocationCollector extends Collector {
        CollocationCollector(int maxReturned) {
            super(maxReturned,false);
        }
        @Override
        double scoreNGram(int[] nGram) {
            return chiSquaredIndependence(nGram);
        }
    }

    class SigTermCollector extends Collector {
        final LanguageModel.Tokenized mBGModel;
        SigTermCollector(int maxReturned, LanguageModel.Tokenized bgModel,
                         boolean reverse) {
            super(maxReturned,reverse);
            mBGModel = bgModel;
        }
        @Override
        double scoreNGram(int[] nGram) {
            String[] tokens = nGramToTokens(nGram);
            int totalSampleCount = mCounter.count(nGram,0,0);
            int sampleCount = mCounter.count(nGram,0,nGram.length);
            double bgProb
                = mBGModel.tokenProbability(tokens,0,tokens.length);
            double score = BinomialDistribution.z(bgProb,
                                                  sampleCount,
                                                  totalSampleCount);
            return score;
        }
    }

    String[] nGramToTokens(int[] nGram) {
        String[] toks = new String[nGram.length];
        for (int i = 0; i < nGram.length; ++i) {
            toks[i] = nGram[i] >= 0
                ? mSymbolTable.idToSymbol(nGram[i])
                : (i == 0) ? "*BEGIN*" : "*END*";
        }
        return toks;
    }

    public double tokenProbability(String[] tokens, int start, int end) {
        return java.lang.Math.pow(2.0,tokenLog2Probability(tokens,start,end));
    }


    public double tokenLog2Probability(String[] tokens, int start, int end) {
        // check args!!!
        double log2Estimate = 0.0;
        int[] tokIds = new int[tokens.length];
        for (int i = start; i < end; ++i) {
            tokIds[i] = mSymbolTable.symbolToID(tokens[i]);
            double conditionalLog2TokenEstimate
                = conditionalLog2TokenEstimate(tokIds,0,i+1);
            if (Double.isInfinite(conditionalLog2TokenEstimate)) {
                double extCountD = mCounter.extensionCount(new int[0], 0, 0);
                double numTokensD = mSymbolTable.numSymbols();
                log2Estimate
                    +=  com.aliasi.util.Math.log2(extCountD
                                                  / (extCountD + numTokensD));
                log2Estimate += mUnknownTokenModel.log2Estimate(tokens[i]);
            } else {
                log2Estimate += conditionalLog2TokenEstimate;
            }
            if (Double.isInfinite(log2Estimate)) {
                System.out.println("tokens[" + i + "]=" + tokens[i]
                                   + "\n     id=" + tokIds[i]);
            }
        }
        return log2Estimate;
    }

    /**
     * Returns the probability of the specified tokens in the
     * underlying token n-gram distribution.  This includes the
     * estimation of the actual token for unknown tokens.
     *
     * @param tokens Tokens whose probability is returned.
     * @return The probability of the tokens.
     */
    public double processLog2Probability(String[] tokens) {
        return tokenLog2Probability(tokens,0,tokens.length);
    }


    /**
     * Returns an array of collocations in order of confidence that
     * their token sequences are not independent.  The object
     * contained in the returned scored objects will be an instance of
     * <code>String[]</code> containing tokens.  The length of n-gram,
     * minimum count for a result and the maximum number of results
     * returned are all specified.  The confidence ordering is based
     * on the result of Pearson's C<sub><sub>2</sub></sub>
     * independence statistic as computed by {@link
     * #chiSquaredIndependence(int[])}.
     *
     * @param nGram Length of n-grams to search for collocations.
     * @param minCount Minimum count for a returned n-gram.
     * @param maxReturned Maximum number of results returned.
     * @return Array of collocations in confidence order.
     */
    public SortedSet<ScoredObject<String[]>> collocationSet(int nGram,
                                                        int minCount,
                                                        int maxReturned) {
        CollocationCollector collector = new CollocationCollector(maxReturned);
        mCounter.handleNGrams(nGram,minCount,collector);
        return collector.nGramSet();
    }


    /**
     * Returns a list of scored n-grams ordered by the significance
     * of the degree to which their counts in this model exceed their
     * expected counts in a specified background model.  The returned
     * scored object array contains {@link ScoredObject} instances
     * whose objects are terms represented as string arrays and whose
     * scores are the collocation score for the term.  For instance,
     * the new terms may be printed in order of significance by:
     *
     * <code><pre>
     * ScoredObject[] terms = new Terms(3,5,100,bgLM);
     * for (int i = 0; i < terms.length; ++i) {
     *     String[] term = (String[]) terms[i].getObject();
     *     double score = terms[i].score();
     *     ...
     * }
     * </pre></code>
     *
     * <P>The exact scoring used is the z-score as defined in {@link
     * BinomialDistribution#z(double,int,int)} with the success
     * probability defined by the n-grams probability estimate in the
     * background model, the number of successes being the count of
     * the n-gram in this model and the number of trials being the
     * total count in this model.
     *
     * <p>See {@link #oldTermSet(int,int,int,LanguageModel.Tokenized)}
     * for a method that returns the least significant terms in
     * this model relative to a background model.
     * @param nGram Length of n-grams to search for significant new terms.
     * @param minCount Minimum count for a returned n-gram.
     * @param maxReturned Maximum number of results returned.
     * @param backgroundLM Background language model against which
     * significance is measured.
     * @return New terms ordered by significance.
     */
    public SortedSet<ScoredObject<String[]>> newTermSet(int nGram, int minCount,
                                                    int maxReturned,
                                                    LanguageModel.Tokenized backgroundLM) {
        return sigTermSet(nGram,minCount,maxReturned,backgroundLM,false);
    }


    /**
     * Returns a list of scored n-grams ordered in reverse order
     * of significance with respect to the background model.  In
     * other words, these are ones that occur less often in this
     * model than they would have been expected to given the
     * background model.
     *
     * <p>Note that only terms that exist in the foreground model are
     * considered.  By contrast, reversing the roles of the models in
     * the sister method {@link
     * #newTermSet(int,int,int,LanguageModel.Tokenized)} considers
     * every n-gram in the background model and may return slightly
     * different results.
     *
     * @param nGram Length of n-grams to search for significant old terms.
     * @param minCount Minimum count in background model for a returned n-gram.
     * @param maxReturned Maximum number of results returned.
     * @param backgroundLM Background language model from which counts are
     * derived.
     * @return Old terms ordered by significance.
     */
    public SortedSet<ScoredObject<String[]>> oldTermSet(int nGram, int minCount,
                                                    int maxReturned,
                                                    LanguageModel.Tokenized backgroundLM) {
        return sigTermSet(nGram,minCount,maxReturned,backgroundLM,true);
    }

    private ScoredObject<String[]>[] sigTerms(int nGram, int minCount,
                                              int maxReturned,
                                              LanguageModel.Tokenized backgroundLM,
                                              boolean reverse) {
        SigTermCollector collector
            = new SigTermCollector(maxReturned,backgroundLM,reverse);
        mCounter.handleNGrams(nGram,minCount,collector);
        return collector.nGrams();
    }

    private SortedSet<ScoredObject<String[]>> sigTermSet(int nGram, int minCount,
                                                     int maxReturned,
                                                     LanguageModel.Tokenized backgroundLM,
                                                     boolean reverse) {
        SigTermCollector collector
            = new SigTermCollector(maxReturned,backgroundLM,reverse);
        mCounter.handleNGrams(nGram,minCount,collector);
        return collector.nGramSet();
    }



    /**
     * Returns the most frequent n-gram terms in the training data up
     * to the specified maximum number.  The terms are ordered by raw
     * counts and returned in order.  The scored objects in the return
     * array have objects that are the terms themselves and
     * scores based on count.
     *
     * <p>See {@link #infrequentTermSet(int,int)} to retrieve the most
     * infrequent terms.
     *
     * @param nGram Length of n-grams to search.
     * @param maxReturned Maximum number of results returned.
     */
    public SortedSet<ScoredObject<String[]>> frequentTermSet(int nGram, int maxReturned) {
        return freqTermSet(nGram,maxReturned,false);
    }

    private ScoredObject<String[]>[] freqTerms(int nGram, int maxReturned,
                                               boolean reverse) {
        FreqTermCollector collector
            = new FreqTermCollector(maxReturned,reverse);
        mCounter.handleNGrams(nGram,1,collector);
        return collector.nGrams();
    }

    private SortedSet<ScoredObject<String[]>> freqTermSet(int nGram, int maxReturned,
                                               boolean reverse) {
        FreqTermCollector collector
            = new FreqTermCollector(maxReturned,reverse);
        mCounter.handleNGrams(nGram,1,collector);
        return collector.nGramSet();
    }


    /**
     * Returns the least frequent n-gram terms in the training data up
     * to the specified maximum number.  The terms are ordered by raw
     * counts and returned in reverse order.  The scored objects in
     * the return array have objects that are the terms themselves and
     * scores based on count.
     *
     * <p>See {@link #frequentTermSet(int,int)} to retrieve the most
     * frequent terms.
     *
     * @param nGram Length of n-grams to search.
     * @param maxReturned Maximum number of results returned.
     */
    public SortedSet<ScoredObject<String[]>> infrequentTermSet(int nGram, int maxReturned) {
        return freqTermSet(nGram,maxReturned,true);
    }

    /**
     * Returns the maximum value of Pearson's C<sub><sub>2</sub></sub>
     * independence test statistic resulting from splitting the
     * specified n-gram in half to derive a contingency matrix.
     * Higher return values indicate more dependence among the terms
     * in the n-gram.
     *
     * <P>The input n-gram is split into two halves,
     * <code>Term<sub><sub>1</sub></sub></code> and
     * <code>Term<sub><sub>2</sub></sub></code>, each of which is a
     * non-empty sequence of integers.
     * <code>Term<sub><sub>1</sub></sub></code> consists of the tokens
     * indexed <code>0</code> to <code>mid-1</code> and
     * <code>Term<sub><sub>2</sub></sub></code> from <code>mid</code>
     * to <code>end-1</code>.
     *
     * <P>The contingency matrix for computing the independence
     * statistic is:
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td>&nbsp;</td><td>+Term<sub><sub>2</sub></sub></td><td>-Term<sub><sub>2</sub></sub></td></tr>
     * <tr><td>+Term<sub><sub>1</sub></sub></td><td>Term(+,+)</td><td>Term(+,-)</td></tr>
     * <tr><td>-Term<sub><sub>1</sub></sub></td><td>Term(-,+)</td><td>Term(-,-)</td></tr>
     * </table>
     * </blockquote>
     *
     * where values for a specified integer sequence
     * <code>nGram</code> and midpoint <code>0 < mid < end</code> is:
     *
     * <blockquote><code>
     *  Term(+,+) = count(nGram,0,end)
     *  <br>
     *  Term(+,-) = count(nGram,0,mid) - count(nGram,0,end)
     *  <br>
     *  Term(-,+) = count(nGram,mid,end) - count(nGram,0,end)
     *  <br>
     *  Term(-,-) = totalCount - Term(+,+) - Term(+,-) - Term(-,+)
     * </code></blockquote>
     *
     * Note that using the overall total count provides a slight
     * overapproximation of the count of appropriate-length n-grams.
     *
     * <P>For further information on the independence test, see the
     * documentation for {@link
     * Statistics#chiSquaredIndependence(double,double,double,double)}.
     *
     * @param nGram Array of integers whose independence
     * statistic is returned.
     * @return Minimum independence test statistic score for splits of
     * the n-gram.
     * @throws IllegalArgumentException If the specified n-gram is not at
     * least two elements long.
     */
    public double chiSquaredIndependence(int[] nGram) {
        if (nGram.length < 2) {
            String msg = "Require n-gram >= 2 for chi square independence."
                + " Found nGram length=" + nGram.length;
            throw new IllegalArgumentException(msg);
        }
        if (nGram.length == 2) {
            return chiSquaredSplit(nGram,1);
        }
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int mid = 1; mid+1 < nGram.length; ++mid)
            bestScore = Math.max(bestScore,
                                 chiSquaredSplit(nGram,mid));
        return bestScore;
    }

    /**
     * Returns the z-score of the specified n-gram with the specified
     * count out of a total sample count, as measured against the
     * expectation of this tokenized language model.  Negative
     * z-scores mean the sample n-gram count is lower than expected
     * and positive z-scores mean the sample n-gram count is higher
     * than expected.  Z-scores close to zero indicate the sample
     * count is in line with expectations according to this language
     * model.
     *
     * <P>Formulas for z-scores and an explanation of their scaling by
     * deviation is described in the documentation for the static
     * method {@link BinomialDistribution#z(double,int,int)}.
     *
     * @param nGram The n-gram to test.
     * @param nGramSampleCount The number of observations of the
     * n-gram in the sample.
     * @param totalSampleCount The total number of samples.
     * @return The z-score for the specified sample counts against the
     * expections of this language model.
     */
    public double z(int[] nGram, int nGramSampleCount, int totalSampleCount) {
        double totalCount = mCounter.count(nGram,0,0);
        double nGramCount = mCounter.count(nGram,0,nGram.length);
        double successProbability = nGramCount / totalCount;
        return BinomialDistribution.z(successProbability,
                                      nGramSampleCount,
                                      totalSampleCount);
    }

    /**
     * Returns a string-based representation of the token
     * counts for this language model.
     *
     * @return A string-based representation of this model.
     */
    @Override
    public String toString() {
        return mCounter.mRootNode.toString(mSymbolTable);
    }

    private double conditionalLog2TokenEstimate(int[] tokIds,
                                                int start, int end) {
        if (end < 1) return 0.0; // this can't get hit from current calls; end >= 1
        int maxLength = mCounter.maxLength();
        int contextEnd = end-1;

        double estimate = tokIds[end-1] == UNKNOWN_TOKEN ? 1.0 : 0.0;
        for (int contextStart = end-1;
             (contextStart >= start
              && (end-contextStart) <= maxLength);
             --contextStart) {
            int numExtensions
                = mCounter.numExtensions(tokIds,contextStart,contextEnd);
            if (numExtensions == 0) break;
            double extCountD
                = mCounter.extensionCount(tokIds,contextStart,contextEnd);
            double lambda
                = extCountD
                / (extCountD + mLambdaFactor * (double) numExtensions);
            estimate = estimate * (1.0 - lambda);
            if (tokIds[end-1] == UNKNOWN_TOKEN) continue;
            int count = mCounter.count(tokIds,contextStart,end);
            if (count > 0)
                estimate += (lambda * ((double) count))/extCountD;
        }
        return com.aliasi.util.Math.log2(estimate);
    }

    private double chiSquaredSplit(int[] pair, int mid) {
        // contingency table & probabilities
        //         _2    _y
        //    1_   12    1y
        //    x_   x2    xy
        long count12 = mCounter.count(pair,0,pair.length);
        long count1_ = mCounter.count(pair,0,mid);
        long count_2 = mCounter.count(pair,mid,pair.length);
        long n = mCounter.extensionCount(pair,0,0);
        long countxy = n - count1_ - count_2 + count12;
        long countx2 = count_2 - count12;
        long count1y = count1_ - count12;
        return Statistics.chiSquaredIndependence(count12,count1y,countx2,countxy);
    }

    private int lastInternalNodeIndex() {
        int last = 1;
        LinkedList<IntNode> queue = new LinkedList<IntNode>();
        queue.add(mCounter.mRootNode);
        for (int i = 1; !queue.isEmpty(); ++i) {
            IntNode node = queue.removeFirst();
            if (node.numExtensions() > 0)
                last = i;
            node.addDaughters(queue);
        }
        return last-1;
    }

    /**
     * The symbol used for unknown symbol IDs.
     */
    public static final int UNKNOWN_TOKEN =
        SymbolTable.UNKNOWN_SYMBOL_ID;

    /**
     * The symbol used for boundaries in the counter, -2.
     */
    public static final int BOUNDARY_TOKEN = -2;

    private static int[] concatenate(int[] is, int i) {
        int[] result = new int[is.length+1];
        System.arraycopy(is,0,result,0,is.length);
        result[is.length] = i;
        return result;
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 6135272620545804504L;
        final TokenizedLM mLM;
        public Externalizer() {
            this(null);
        }
        public Externalizer(TokenizedLM lm) {
            mLM = lm;
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            try {
                return new CompiledTokenizedLM(in);
            } catch (ClassNotFoundException e) {
                throw Exceptions.toIO("TokenizedLM.Externalizer.read()",e);
            }
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            if (mLM.mTokenizerFactory instanceof Serializable) {
                objOut.writeUTF("");
                objOut.writeObject(mLM.mTokenizerFactory);
            } else {
                objOut.writeUTF(mLM.mTokenizerFactory.getClass().getName());
            }
            objOut.writeObject(mLM.mSymbolTable);
            ((LanguageModel.Dynamic) mLM.mUnknownTokenModel).compileTo(objOut);
            ((LanguageModel.Dynamic) mLM.mWhitespaceModel).compileTo(objOut);
            objOut.writeInt(mLM.mNGramOrder);

            int numNodes = mLM.mCounter.mRootNode.trieSize();
            objOut.writeInt(numNodes);

            int lastInternalNodeIndex = mLM.lastInternalNodeIndex();
            objOut.writeInt(lastInternalNodeIndex);

            // write root node (-int,-logP,-log(1-L),firstDtr)
            objOut.writeInt(Integer.MIN_VALUE);  // root symbol unknown
            objOut.writeFloat(Float.NaN); // no estimate
            objOut.writeFloat((float)
                              com.aliasi.util.Math
                              .log2(1.0-mLM.lambda(com.aliasi.util.Arrays
                                                   .EMPTY_INT_ARRAY)));
            objOut.writeInt(1);           // first dtr = 1

            LinkedList<int[]> queue = new LinkedList<int[]>();
            int[] outcomes
                = mLM.mCounter.mRootNode
                .integersFollowing(com.aliasi.util.Arrays.EMPTY_INT_ARRAY,0,0);
            for (int i = 0; i < outcomes.length; ++i)
                queue.add(new int[] { outcomes[i] });
            for (int i = 1; !queue.isEmpty(); ++i) {
                int[] is = queue.removeFirst();
                objOut.writeInt(is[is.length-1]);
                objOut.writeFloat((float)
                                  mLM.conditionalLog2TokenEstimate(is,0,is.length));
                if (i <= lastInternalNodeIndex) {
                    objOut.writeFloat((float)
                                      com.aliasi.util.Math.log2(1.0-mLM.lambda(is)));
                    objOut.writeInt(i+queue.size()+1);
                }
                int[] followers
                    = mLM.mCounter.mRootNode.integersFollowing(is,0,is.length);
                for (int j = 0; j < followers.length; ++j)
                    queue.add(concatenate(is,followers[j]));
            }
        }
    }


    @SuppressWarnings("rawtypes")
    static final ScoredObject[] EMPTY_SCORED_OBJECT_ARRAY
        = new ScoredObject[0];

    static final ScoredObject<String[]>[] EMPTY_SCORED_OBJECT_STRING_ARRAY_ARRAY
        = emptyScoredObjectArray();

    static ScoredObject<String[]>[] emptyScoredObjectArray() {
        @SuppressWarnings("unchecked")
        ScoredObject<String[]>[] result
            = (ScoredObject<String[]>[]) EMPTY_SCORED_OBJECT_ARRAY;
        return result;
    }


}

