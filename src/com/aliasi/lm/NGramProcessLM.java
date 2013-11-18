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

import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import com.aliasi.stats.Model;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.Externalizable;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Serializable;

import java.util.LinkedList;

/**
 * An <code>NGramProcessLM</code> provides a dynamic conditional
 * process language model process for which training, estimation, and
 * pruning may be interleaved.  A process language model normalizes
 * probablities for a given length of input.
 *
 * <P>The model may be compiled to an object output stream; the
 * compiled model read back in will be an instance of {@link
 * CompiledNGramProcessLM}.
 *
 * <P>This class implements a generative language model based on the
 * chain rule, as specified by {@link LanguageModel.Conditional}.
 * The maximum likelihood estimator (see {@link CharSeqCounter}),
 * is smoothed by linear interpolation with the next lower-order context
 * model:
 *
 * <blockquote><code>
 *   P'(c<sub><sub>k</sub></sub>|c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>)
 *   <br>
 *   = lambda(c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>)
 *     * P<sub><sub>ML</sub></sub>(c<sub><sub>k</sub></sub>|c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>)
 *   <br> &nbsp; &nbsp;
 *   + (1-lambda(c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>))
 *     * P'(c<sub><sub>k</sub></sub>|c<sub><sub>j+1</sub></sub>,...,c<sub><sub>k-1</sub></sub>)
 * </code></blockquote>
 *
 * <p>The <code>P<sub><sub>ML</sub></sub></code> terms in the above definition
 * are maximum likelihood estimates based on frequency:
 *
 * <blockquote><pre>
 * P<sub><sub>ML</sub></sub>(c<sub><sub>k</sub></sub>|c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>)
 * = count(c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>, c<sub><sub>k</sub></sub>)
 * / extCount(c<sub><sub>j</sub></sub>,...,c<sub><sub>k-1</sub></sub>)</pre></blockquote>
 *
 * The <code>count</code> is just the number of times a given string
 * has been seein the data, whereas <code>extCount</code> is the number
 * of times an extension to the string has been seen in the data:
 *
 * <blockquote><pre>
 * extCount(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub></code>)
 * = <big><big>&Sigma;</big></big><sub><sub>d</sub></sub> count(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub></code>,d)</pre></blockquote>
 *
 *
 * <p>In the parametric Witten-Bell method, the interpolation ratio
 * <code>lambda</code> is defined based on extensions of the context
 * of estimation to be:
 *
 * <blockquote><code>
 *   lambda(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>)
 *   <br> = extCount(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>)
 *   <br> &nbsp; &nbsp; / (extCount(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>)
 *                         + L * numExtensions(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>))
 * </code></blockquote>
 *
 * where
 * <code>c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub></code>
 * is the conditioning context for estimation, <code>extCount</code>
 * is as defined above, where <code>numExtensions</code> is the
 * number of extensions of a context:
 *
 * <blockquote><pre>
 * numExtensions(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>)</code>
 * = cardinality( { d | count(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>,d) &gt; 0 } )</pre></blockquote>
 *
 * and where <code>L</code> is a hyperparameter of the distribution
 * (described below).
 *
 * <p>As a base case, <code>P(c<sub><sub>k</sub></sub>)</code> is
 * interpolated with the uniform distribution
 * <code>P<sub><sub>U</sub></sub></code>, with interpolation defined
 * as usual with the argument to <code>lambda</code> being the
 * empty (i.e. zero length) sequence:
 *
 * <blockquote><pre>
 * P(d) = lambda() * P<sub><sub>ML</sub></sub>(d)
 *      + (1-lambda()) * P<sub><sub>U</sub></sub>(d)</pre></blockquote>
 *
 * The uniform distribution <code>P<sub><sub>U</sub></sub></code> only
 * depends on the number of possible characters used in training and
 * tests:
 *
 * <blockquote><pre>
 * P<sub><sub>U</sub></sub>(c) = 1/alphabetSize</pre></blockquote>
 *
 * where <code>alphabetSize</code> is the maximum number of distinct
 * characters in this model.
 *
 * <P>The free hyperparameter <code>L</code> in the smoothing equation
 * determines the balance between higher-order and lower-order models.
 * A higher value for <code>L</code> gives more of the weight to
 * lower-order contexts.  As the amount of data grows against a fixed
 * alphabet of characters, the impact of <code>L</code> is reduced.
 * In Witten and Bell's original paper, the hyperparameter
 * <code>L</code> was set to 1.0, which is not a particularly good
 * choice for most text sources.  A value of the lambda factor that is
 * roughly the length of the longest n-gram seems to be a good rule of
 * thumb.
 *
 * <P>Methods are provided for computing a sample cross-entropy rate
 * for a character sequence.  The sample cross-entropy
 * <code>H(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>;P<sub><sub>M</sub></sub>)</code> for
 * sequence <code>c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub></code> in
 * probability model <code>P<sub><sub>M</sub></sub></code> is defined to be the
 * average log (base 2) probability of the characters in the sequence
 * according to the model. In symbols:
 *
 * <blockquote><code>
 * H(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>;P<sub><sub>M</sub></sub>)
 =  (-log<sub><sub>2</sub></sub> P<sub><sub>M</sub></sub>(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>))/n
 * </code></blockquote>
 *
 * The cross-entropy rate of distribution <code>P'</code>
 * with respect to a distribution <code>P</code> is defined by:
 *
 * <blockquote><code>
 *   H(P',P)
 *   = <big><big><big>&Sigma;</big></big></big><sub><sub>x</sub></sub>
 *     P(x) * log<sub><sub>2</sub></sub> P'(x)
 * </code></blockquote>
 *
 * The Shannon-McMillan-Breiman theorem shows that as the length of
 * the sample drawn from the true distribution <code>P</code> grows,
 * the sample cross-entropy rate approaches the actual cross-entropy
 * rate.  In symbols:
 *
 * <blockquote>
 * <code>
 *  H(P,P<sub><sub>M</sub></sub>)
 *  = lim<sub><sub>n->infinity</sub></sub>
 *    H(c<sub><sub>1</sub></sub>,...,c<sub><sub>n</sub></sub>;P<sub><sub>M</sub></sub>)/n
 * </code>
 * </blockquote>
 *
 * The entropy of a distribution <code>P</code> is defined by its
 * cross-entropy against itself, <code>H(P,P)</code>.  A
 * distribution's entropy is a lower bound on its cross-entropy; in
 * symbols, <code>H(P',P) > H(P,P)</code> for all distributions
 * <code>P'</code>.
 *
 * <h3>Pruning</h3>
 *
 * <P>Models may be pruned by pruning the underlying substring
 * counter for the language model.  This counter is returned by
 * the method {@link #substringCounter()}.  See the class documentat
 * for the return result {@link TrieCharSeqCounter} for more information.
 *
 * <h3>Serialization</h3>
 *
 * <p>Models may be serialized in the usual way by creating an object
 * output object and writing the object:
 *
 * <blockquote><pre>
 * NGramProcessLM lm = ...;
 * ObjectOutput out = ...;
 * out.writeObject(lm);</pre></blockquote>
 *
 * Reading just reverses the process:
 *
 * <blockquote><pre>
 * ObjectInput in = ...;
 * NGramProcessLM lm = (NGramProcessLM) in.readObject();</pre></blockquote>
 *
 * Serialization is based on the methods {@link #writeTo(OutputStream)}
 * and {@link #readFrom(InputStream)}.  These write compressed forms of
 * the model to streams in binary format.
 *
 * <p><b>Warning:</b> The object input and output used for
 * serialization must extend {@link InputStream} and {@link
 * OutputStream}.  The only implementations of {@link ObjectInput} and
 * {@link ObjectOutput} as of the 1.6 JDK do extend the streams, so
 * this will only be a problem with customized object input or output
 * objects.  If you need this method to work with custom input and
 * output objects that do not extend the corresponding streams, drop
 * us a line and we can perhaps refactor the output methods to remove
 * this restriction.
 *
 * <h3>References</h3>
 *
 * <P>For information on the Witten-Bell interpolation method, see:
 * <ul>
 * <li>
 * Witten, Ian H. and Timothy C. Bell.  1991. The zero-frequency
 * problem: estimating the probabilities of novel events in adaptive
 * text compression. <i>IEEE Transactions on Information Theory</i>
 * <b>37</b>(4).
 * </ul>
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 */
public class NGramProcessLM
    implements Model<CharSequence>,
               LanguageModel.Process,
               LanguageModel.Conditional,
               LanguageModel.Dynamic,
               ObjectHandler<CharSequence>,
               Serializable {

    static final long serialVersionUID = -2865886217715962249L;

    private final TrieCharSeqCounter mTrieCharSeqCounter;

    private final int mMaxNGram;
    private double mLambdaFactor;
    private int mNumChars;
    private double mUniformEstimate;
    private double mLog2UniformEstimate;

    /**
     * Constructs an n-gram process language model with the specified
     * maximum n-gram length.  The number of characters is set to the
     * default of {@link Character#MAX_VALUE} and the interpolation
     * parameter is set to the default of being equal to the n-gram
     * length.
     *
     * @param maxNGram Maximum length n-gram for which counts are
     * stored.
     */
    public NGramProcessLM(int maxNGram) {
        this(maxNGram,Character.MAX_VALUE);
    }

    /**
     * Construct an n-gram language process with the specified maximum
     * n-gram length and maximum number of characters.  The interpolation
     * hyperparameter will be set to the same value as the maximum n-gram
     * length.
     *
     * @param maxNGram Maximum length n-gram for which counts are
     * stored.
     * @param numChars Maximum number of characters in training data.
     * @throws IllegalArgumentException If the maximum n-gram is
     * less than 1 or if the number of characters is not between 1 and
     * the maximum number of characters.
     */
    public NGramProcessLM(int maxNGram,
                          int numChars) {
        this(maxNGram,numChars,maxNGram);
    }

    /**
     * Construct an n-gram language process with the specified maximum
     * n-gram length, number of characters, and interpolation ratio
     * hyperparameter.
     *
     * @param maxNGram Maximum length n-gram for which counts are
     * stored.
     * @param numChars Maximum number of characters in training data.
     * @param lambdaFactor Central value of interpolation
     * hyperparameter explored.
     * @throws IllegalArgumentException If the maximum n-gram is
     * less than 1, the number of characters is not between 1 and
     * the maximum number of characters, of if the lambda factor
     * is not greater than or equal to 0.
     */
    public NGramProcessLM(int maxNGram,
                          int numChars,
                          double lambdaFactor) {
        this(numChars,lambdaFactor,new TrieCharSeqCounter(maxNGram));
    }


    /**
     * Construct an n-gram process language model with the specified
     * number of characters, interpolation parameter
     * and character sequence counter.  The maximum n-gram is determined
     * by the sequence counter.
     *
     * <p>The counter argument allows serialized counters to be
     * read back in and used to create an n-gram process LM.
     *
     * @param numChars Maximum number of characters in training and
     * test data.
     * @param lambdaFactor Interpolation parameter (see class doc).
     * @param counter Character sequence counter to use.
     * @throws IllegalArgumentException If the number of characters is
     * not between 1 and the maximum number of characters, of if the
     * lambda factor is not greater than or equal to 0.
     */
    public NGramProcessLM(int numChars,
                          double lambdaFactor,
                          TrieCharSeqCounter counter) {
        mMaxNGram = counter.mMaxLength;
        setLambdaFactor(lambdaFactor);  // checks range
        setNumChars(numChars);
        mTrieCharSeqCounter = counter;
    }

    /**
     * Writes this language model to the specified output stream.
     *
     * <p>A language model is written using a {@link BitOutput}
     * wrapped around the specified output stream.  This bit output is
     * used to delta encode the maximum n-gram, number of characters,
     * lambda factor times 1,000,000, and then the underlying sequence
     * counter using {@link
     * TrieCharSeqCounter#writeCounter(CharSeqCounter,TrieWriter,int)}.
     * The bit output is flushed, but the output stream is not closed.
     *
     * <p>A language model can be read and written using the following
     * code, given a file <code>f</code>:
     *
     * <blockquote><pre>
     * NGramProcessLM lm = ...;
     * File f = ...;
     * OutputStream out = new FileOutputStream(f);
     * BufferedOutputStream bufOut = new BufferedOutputStream(out);
     * lm.writeTo(bufOut);
     * bufOut.close();
     *
     * ...
     * InputStream in = new FileInputStream(f);
     * BufferedInputStream bufIn = new BufferedInputStream(in);
     * NGramProcessLM lm2 = NGramProcessLM.readFrom(bufIn);
     * bufIn.close();</pre></blockquote>
     *
     * @param out Output stream to which to write language model.
     * @throws IOException If there is an underlying I/O error.
     */
    public void writeTo(OutputStream out) throws IOException {
        BitOutput bitOut = new BitOutput(out);
        writeTo(bitOut);
        bitOut.flush();
    }

    void writeTo(BitOutput bitOut) throws IOException {
        bitOut.writeDelta(mMaxNGram);
        bitOut.writeDelta(mNumChars);
        bitOut.writeDelta((int) (mLambdaFactor * 1000000));
        BitTrieWriter trieWriter = new BitTrieWriter(bitOut);
        TrieCharSeqCounter.writeCounter(mTrieCharSeqCounter,trieWriter,
                                        mMaxNGram);
    }

    /**
     * Reads a language model from the specified input stream.
     *
     * <p>See {@link #writeTo(OutputStream)} for information on the
     * binary I/O format.
     *
     * @param in Input stream from which to read a language model.
     * @return The language model read from the stream.
     * @throws IOException If there is an underlying I/O error.
     */
    public static NGramProcessLM readFrom(InputStream in) throws IOException {
        BitInput bitIn = new BitInput(in);
        return readFrom(bitIn);
    }

    static NGramProcessLM readFrom(BitInput bitIn) throws IOException {
        int maxNGram = (int) bitIn.readDelta();
        int numChars = (int) bitIn.readDelta();
        double lambdaFactor = bitIn.readDelta() / 1000000.0;
        BitTrieReader trieReader = new BitTrieReader(bitIn);
        TrieCharSeqCounter counter
            = TrieCharSeqCounter.readCounter(trieReader,maxNGram);
        return new NGramProcessLM(numChars,lambdaFactor,counter);
    }

    public double log2Prob(CharSequence cSeq) {
        return log2Estimate(cSeq);
    }

    public double prob(CharSequence cSeq) {
        return java.lang.Math.pow(2.0,log2Estimate(cSeq));
    }

    public final double log2Estimate(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return log2Estimate(cs,0,cs.length);
    }

    public final double log2Estimate(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        double sum = 0.0;
        for (int i = start+1; i <= end; ++i)
            sum += log2ConditionalEstimate(cs,start,i);
        return sum;
    }

    public void train(CharSequence cSeq) {
        train(cSeq,1);
    }

    public void train(CharSequence cSeq, int incr) {
        char[] cs = Strings.toCharArray(cSeq);
        train(cs,0,cs.length,incr);
    }

    public void train(char[] cs, int start, int end) {
        train(cs,start,end,1);
    }
    public void train(char[] cs, int start, int end, int incr) {
        Strings.checkArgsStartEnd(cs,start,end);
        mTrieCharSeqCounter.incrementSubstrings(cs,start,end,incr);
    }

    /**
     * Implements the object handler interface over character
     * sequences for training.  The implementation delegates to {@link
     * #train(CharSequence)}.
     * 
     * @param cSeq Character sequence on which to train.
     */
    public void handle(CharSequence cSeq) {
        train(cSeq);
    }

    /**
     * Trains the specified conditional outcome(s) of the specified
     * character slice given the background slice.

     * <P>This method just shorthand for incrementing the counts of
     * all substrings of <code>cs</code> from position
     * <code>start</code> to <code>end-1</code> inclusive, then
     * decrementing all of the counts of substrings from position
     * <code>start</code> to <code>condEnd-1</code>.  For instance, if
     * <code>cs</code> is
     * <code>&quot;abcde&quot;.toCharArray()</code>, then calling
     * <code>trainConditional(cs,0,5,2)</code> will increment the
     * counts of <code>cde</code> given <code>ab</code>, but will not
     * increment the counts of <code>ab</code> directly.  This increases
     * the following probabilities:
     *
     * <blockquote><code>
     * P('e'|&quot;abcd&quot;) &nbsp;
     * P('e'|&quot;bcd&quot;) &nbsp;
     * P('e'|&quot;cd&quot;) &nbsp;
     * P('e'|&quot;d&quot;) &nbsp;
     * P('e'|&quot;&quot;)
     * <br>
     * P('d'|&quot;abc&quot;) &nbsp;&nbsp;
     * P('d'|&quot;bc&quot;) &nbsp;&nbsp;
     * P('d'|&quot;c&quot;) &nbsp;&nbsp;
     * P('d'|&quot;&quot;)
     * <br>
     * P('c'|&quot;ab&quot;) &nbsp;&nbsp;&nbsp;
     * P('c'|&quot;b&quot;) &nbsp;&nbsp;&nbsp;
     * P('c'|&quot;&quot;)
     * </code></blockquote>
     *
     * but does not increase the following probabilities:
     *
     * <blockquote><code>
     * P('b'|&quot;a&quot;) &nbsp;
     * P('b'|&quot;&quot;)
     * <br>
     * P('a'|&quot;&quot;)</code></blockquote>
     *
     * @param cs Array of characters.
     * @param start Start position for slice.
     * @param end One past end position for slice.
     * @param condEnd One past the end of the conditional portion of
     * the slice.
     */
    public void trainConditional(char[] cs, int start, int end,
                                 int condEnd) {
        Strings.checkArgsStartEnd(cs,start,end);
        Strings.checkArgsStartEnd(cs,start,condEnd);
        if (condEnd > end) {
            String msg = "Conditional end must be < end."

                + " Found condEnd=" + condEnd
                + " end=" + end;
            throw new IllegalArgumentException(msg);
        }
        if (condEnd == end) return;
        mTrieCharSeqCounter.incrementSubstrings(cs,start,end);
        mTrieCharSeqCounter.decrementSubstrings(cs,start,condEnd);

    }

    public char[] observedCharacters() {
        return mTrieCharSeqCounter.observedCharacters();
    }

    /**
     * Writes a compiled version of this process language model to the
     * specified object output.
     *
     * <P>The object written will be an instance of {@link
     * CompiledNGramProcessLM}.  It may be read in by casting the
     * result of {@link ObjectInput#readObject()}.
     *
     * <P>Compilation is time consuming, because it must traverse the
     * entire trie structure, and for each node, estimate its log
     * probability and if it is internal, its log interpolation value.
     * Given that time taken is proportional to the size of the trie,
     * pruning first may greatly speed up this operation and reduce
     * the size of the compiled object that is written.
     *
     * @param objOut Object output to which a compiled version of this
     * langauge model will be written.
     * @throws IOException If there is an I/O exception writing the
     * compiled object.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    public double log2ConditionalEstimate(CharSequence cSeq) {
        return log2ConditionalEstimate(cSeq,mMaxNGram,mLambdaFactor);
    }

    public double log2ConditionalEstimate(char[] cs, int start, int end) {
        return log2ConditionalEstimate(cs,start,end,mMaxNGram,mLambdaFactor);
    }

    /**
     * Returns the substring counter for this language model.
     * Modifying the counts in the returned counter, such as by
     * pruning, will change the estimates in this language model.
     *
     * @return Substring counter for this language model.
     */
    public TrieCharSeqCounter substringCounter() {
        return mTrieCharSeqCounter;
    }

    /**
     * Returns the maximum n-gram length for this model.
     *
     * @return The maximum n-gram length for this model.
     */
    public int maxNGram() {
        return mMaxNGram;
    }

    /**
     * Returns the log (base 2) conditional estimate of the last
     * character in the specified character sequence given the
     * previous characters based only on counts of n-grams up to the
     * specified maximum n-gram.  If the maximum n-gram argument is
     * greater than or equal to the one supplied at construction time,
     * the results wil lbe the same as the ordinary conditional
     * estimate.
     *
     * @param cSeq Character sequence to estimate.
     * @param maxNGram Maximum length of n-gram count to use for
     * estimate.
     * @param lambdaFactor Value of interpolation hyperparameter for
     * this estimate.
     * @return Log (base 2) conditional estimate.
     * @throws IllegalArgumentException If the character sequence is not at
     * least one character long.
     */
    public double log2ConditionalEstimate(CharSequence cSeq, int maxNGram,
                                          double lambdaFactor) {
        char[] cs = Strings.toCharArray(cSeq);
        return log2ConditionalEstimate(cs,0,cs.length,maxNGram,lambdaFactor);
    }

    /**
     * Returns the log (base 2) conditional estimate for a specified
     * character slice with a specified maximum n-gram and specified
     * hyperparameter.
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @param maxNGram Maximum length of n-gram to use in estimates.
     * @param lambdaFactor Value of interpolation hyperparameter.
     * @return Log (base 2) conditional estimate of the last character
     * in the slice given the previous characters.
     * @throws IndexOutOfBoundsException If the start index and end
     * index minus one are out of range of the character array or if the
     * character slice is less than one character long.
     */
    public double log2ConditionalEstimate(char[] cs, int start, int end,
                                          int maxNGram, double lambdaFactor) {
        if (end <= start) {
            String msg = "Conditional estimates require at least one character.";
            throw new IllegalArgumentException(msg);
        }
        Strings.checkArgsStartEnd(cs,start,end);
        checkMaxNGram(maxNGram);
        checkLambdaFactor(lambdaFactor);
        int maxUsableNGram = Math.min(maxNGram,mMaxNGram);
        if (start == end) return 0.0;
        double currentEstimate = mUniformEstimate;
        int contextEnd = end-1;
        int longestContextStart = Math.max(start,end-maxUsableNGram);
        for (int currentContextStart = contextEnd;
             currentContextStart >= longestContextStart;
             --currentContextStart) {
            long contextCount
                = mTrieCharSeqCounter.extensionCount(cs,currentContextStart,contextEnd);
            if (contextCount == 0) break;
            long outcomeCount = mTrieCharSeqCounter.count(cs,currentContextStart,end);
            double lambda = lambda(cs,currentContextStart,contextEnd,lambdaFactor);
            currentEstimate
                = lambda * (((double)outcomeCount) / (double)contextCount)
                + (1.0 - lambda) * currentEstimate;
        }
        return com.aliasi.util.Math.log2(currentEstimate);
    }

    /**
     * Returns the interpolation ratio for the specified character
     * slice interpreted as a context.  The hyperparameter used is
     * that returned by {@link #getLambdaFactor()}.  The definition of
     * <code>lambda()</code> is provided in the class documentation
     * above.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @throws IndexOutOfBoundsException If the start index and end
     * index minus one are out of range of the character array.
     */
    double lambda(char[] cs, int start, int end) {
        return lambda(cs,start,end,getLambdaFactor());
    }

    /**
     * Returns the interpolation ratio for the specified character
     * slice interpreted as a context with the specified
     * hyperparameter.  The definition of <code>lambda()</code> is
     * provided in the class documentation above.  *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @param lambdaFactor Value for interpolation ratio hyperparameter.
     * @throws IndexOutOfBoundsException If the start index and end
     * index minus one are out of range of the character array.
     */
    double lambda(char[] cs, int start, int end, double lambdaFactor) {
        checkLambdaFactor(lambdaFactor);
        Strings.checkArgsStartEnd(cs,start,end);
        double count = mTrieCharSeqCounter.extensionCount(cs,start,end);
        if (count <= 0.0) return 0.0;
        double numOutcomes = mTrieCharSeqCounter.numCharactersFollowing(cs,start,end);
        return lambda(count,numOutcomes,lambdaFactor);
    }

    /**
     * Returns the current setting of the interpolation ratio
     * hyperparameter.  See the class documentation above for
     * information on how the interpolation ratio is used in
     * estimates.
     *
     * @return The current setting of the interpolation ratio
     * hyperparameter.
     */
    public double getLambdaFactor() {
        return mLambdaFactor;
    }

    /**
     * Sets the value of the interpolation ratio hyperparameter
     * to the specified value.  See the class documentation above
     * for information on how the interpolation ratio is used in estimates.
     *
     * @param lambdaFactor New value for interpolation ratio
     * hyperparameter.
     * @throws IllegalArgumentException If the value is not greater
     * than or equal to zero.
     */
    public final void setLambdaFactor(double lambdaFactor) {
        checkLambdaFactor(lambdaFactor);
        mLambdaFactor = lambdaFactor;
    }

    /**
     * Sets the number of characters for this language model.  All
     * subsequent estimates will be based on this number.  See the
     * class definition above for information on how the number of
     * character is used to determine the base case uniform
     * distribution.
     *
     * @param numChars New number of characters for this language model.
     * @throws IllegalArgumentException If the number of characters is
     * less than <code>0</code> or more than
     * <code>Character.MAX_VALUE</code>.
     */
    public final void setNumChars(int numChars) {
        checkNumChars(numChars);
        mNumChars = numChars;
        mUniformEstimate = 1.0 / (double)mNumChars;
        mLog2UniformEstimate
            = com.aliasi.util.Math.log2(mUniformEstimate);
    }

    /**
     * Returns a string-based representation of this language model.
     *
     * @return A string-based representation of this language model.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringBuilder(sb);
        return sb.toString();
    }

    void toStringBuilder(StringBuilder sb) {
        sb.append("Max NGram=" + mMaxNGram + " ");
        sb.append("Num characters=" + mNumChars + "\n");
        sb.append("Trie of counts=\n");
        mTrieCharSeqCounter.toStringBuilder(sb);
    }

    // need this for the process model to get boundaries right
    void decrementUnigram(char c) {
        decrementUnigram(c,1);
    }

    void decrementUnigram(char c, int count) {
        mTrieCharSeqCounter.decrementUnigram(c,count);
    }

    private double lambda(double count, double numOutcomes,
                          double lambdaFactor) {
        return count
            / (count + lambdaFactor * numOutcomes);
    }

    private double lambda(Node node) {
        double count = node.contextCount(Strings.EMPTY_CHAR_ARRAY,0,0);
        double numOutcomes = node.numOutcomes(Strings.EMPTY_CHAR_ARRAY,0,0);
        return lambda(count,numOutcomes,mLambdaFactor);
    }

    private int lastInternalNodeIndex() {
        int last = 1;
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(mTrieCharSeqCounter.mRootNode);
        for (int i = 1; !queue.isEmpty(); ++i) {
            Node node = queue.removeFirst();
            if (node.numOutcomes(Strings.EMPTY_CHAR_ARRAY,
                                 0,0) > 0)
                last = i;
            node.addDaughters(queue);
        }
        return last-1;
    }


    private Object writeReplace() {
        return new Serializer(this);
    }


    // unfortunately, this depends on serialization happening with streams
    static class Serializer implements Externalizable {
        static final long serialVersionUID = -7101238964823109652L;
        NGramProcessLM mLM;
        public Serializer() { /* empty no-op constructor */
        }
        public Serializer(NGramProcessLM lm) {
            mLM = lm;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            mLM.writeTo((OutputStream) out);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            mLM = NGramProcessLM.readFrom((InputStream) in);
        }
        public Object readResolve() {
            return mLM;
        }
    }

    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -3623859317152451545L;
        final NGramProcessLM mLM;
        public Externalizer() {
            this(null);
        }
        public Externalizer(NGramProcessLM lm) {
            mLM = lm;
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            return new CompiledNGramProcessLM(in);
        }
        @Override
        public void writeExternal(ObjectOutput dataOut) throws IOException {
            dataOut.writeInt(mLM.mMaxNGram);

            dataOut.writeFloat((float) mLM.mLog2UniformEstimate);

            long numNodes = mLM.mTrieCharSeqCounter.uniqueSequenceCount();
            if (numNodes > Integer.MAX_VALUE) {
                String msg = "Maximum number of compiled nodes is"
                    + " Integer.MAX_VALUE = " + Integer.MAX_VALUE
                    + " Found number of nodes=" + numNodes;
                throw new IllegalArgumentException(msg);
            }
            dataOut.writeInt((int)numNodes);

            int lastInternalNodeIndex = mLM.lastInternalNodeIndex();
            dataOut.writeInt(lastInternalNodeIndex);

            // write root node (char,logP,log(1-L),firstDtr)
            dataOut.writeChar('\uFFFF');
            dataOut.writeFloat((float) mLM.mLog2UniformEstimate);
            double oneMinusLambda
                = 1.0 - mLM.lambda(mLM.mTrieCharSeqCounter.mRootNode);
            float log2OneMinusLambda = Double.isNaN(oneMinusLambda)
                ? 0f : (float) com.aliasi.util.Math.log2(oneMinusLambda);
            dataOut.writeFloat(log2OneMinusLambda);
            dataOut.writeInt(1);  // firstDtr
            char[] cs = mLM.mTrieCharSeqCounter.observedCharacters();

            LinkedList<char[]> queue = new LinkedList<char[]>();
            for (int i = 0; i < cs.length; ++i)
                queue.add(new char[] { cs[i] });
            for (int index = 1; !queue.isEmpty(); ++index) {
                char[] nGram = queue.removeFirst();
                char c = nGram[nGram.length-1];
                dataOut.writeChar(c);

                float logConditionalEstimate
                    = (float) mLM.log2ConditionalEstimate(nGram,0,nGram.length);
                dataOut.writeFloat(logConditionalEstimate);

                if (index <= lastInternalNodeIndex) {
                    double oneMinusLambda2
                        = 1.0 - mLM.lambda(nGram,0,nGram.length);
                    float log2OneMinusLambda2
                        = (float) com.aliasi.util.Math.log2(oneMinusLambda2);
                    dataOut.writeFloat(log2OneMinusLambda2);
                    int firstChildIndex = index + queue.size() + 1;
                    dataOut.writeInt(firstChildIndex);
                }
                char[] cs2
                    = mLM.mTrieCharSeqCounter
                    .charactersFollowing(nGram,0,nGram.length);
                for (int i = 0; i < cs2.length; ++i)
                    queue.add(com.aliasi.util.Arrays.concatenate(nGram,cs2[i]));
            }
        }
    }

    static void checkLambdaFactor(double lambdaFactor) {
        if (lambdaFactor < 0.0
            || Double.isInfinite(lambdaFactor)
            || Double.isNaN(lambdaFactor)) {
            String msg = "Lambda factor must be ordinary non-negative double."
                + " Found lambdaFactor=" + lambdaFactor;
            throw new IllegalArgumentException(msg);
        }
    }

    static void checkMaxNGram(int maxNGram) {
        if (maxNGram < 1) {
            String msg = "Maximum n-gram must be greater than zero."
                + " Found max n-gram=" + maxNGram;
            throw new IllegalArgumentException(msg);
        }
    }

    private static void checkNumChars(int numChars) {
        if (numChars < 0 || numChars > Character.MAX_VALUE) {
            String msg = "Number of characters must be > 0 and "
                + " must be less than Character.MAX_VALUE"
                + " Found numChars=" + numChars;
            throw new IllegalArgumentException(msg);

        }
    }

}

