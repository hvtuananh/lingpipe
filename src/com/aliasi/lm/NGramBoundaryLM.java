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
import com.aliasi.util.Compilable;
import com.aliasi.util.Strings;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * An <code>NGramBoundaryLM</code> provides a dynamic sequence
 * language model for which training, estimation and pruning may be
 * interleaved.  A sequence language model normalizes probabilities
 * over all sequences.
 *
 * <P>This class wraps an n-gram process language model by supplying a
 * special boundary character <code>boundaryChar</code> at
 * construction time which will be added to the total number of
 * characters in defining the estimator.  For each training event, the
 * boundary character is inserted both before and after the character
 * sequence provided.  The actual unigram count of this boundary must
 * then be decremented so that the initial character isn't counted in
 * estimates.  During estimation, the initial boundary character is
 * used as context and the final one is used to estimate the
 * end-of-stream likelihood.  Thus if <code>P<sub><sub>pr</sub></sub></code>
 * is the underlying process model then the boundary model defines
 * estimates by:
 *
 * <blockquote><code>
 *  P<sub><sub>b</sub></sub>(c<sub><sub>1</sub></sub>,...,c<sub><sub>N</sub></sub>)
 * <br>&nbsp;
 * = P<sub><sub>pr</sub></sub>(boundaryChar|boundaryChar,c<sub><sub>1</sub></sub>,...,c<sub><sub>N</sub></sub>)
 * <br>&nbsp;&nbsp;&nbsp;
 *   * <big><big><big>&Sigma;</big></big></big><sub><sub>1<=i<=N</sub></sub>
 *           P<sub><sub>pr</sub></sub>(c<sub><sub>i</sub></sub>|boundaryChar,c<sub><sub>1</sub></sub>,...,c<sub><sub>i-1</sub></sub>)
 *
 * <br>&nbsp;
 *  = P<sub><sub>pr</sub></sub>(boundaryChar,c<sub><sub>1</sub></sub>,...,c<sub><sub>N</sub></sub>,boundaryChar)
 *    - P<sub><sub>pr</sub></sub>(boundaryChar)
 * </code></blockquote>
 *
 * The result of serializing and deserializing an n-gram boundary
 * language model is a compiled implementation of a conditional
 * sequence language model.  The serialization format is the boundary character
 * followed by the serialization of the contained writable process
 * language model.
 *
 *<p>Models may be pruned by pruning the substring counter returned
 * by {@link #substringCounter()}.  See the documentation for the
 * class of the return object, {@link TrieCharSeqCounter}, for more
 * information.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * N-gram boundary language models are both serializable and compilable,
 * implementing Java's {@link Serializable} interface and
 * LingPipe's {@link Compilable} interface.  
 *
 * <p>Serialization and deserialization returns a copy of the
 * serialized object, which again implements this class, {@code
 * NGramBoundaryLM}.  Compilation and deserialization returns an
 * instance of {@link CompiledNGramBoundaryLM}.  The compiled version
 * is much faster and may also be more compact in memory.
 *
 * @author Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe2.0
 */
public class NGramBoundaryLM
    implements LanguageModel.Sequence,
               LanguageModel.Conditional,
               LanguageModel.Dynamic,
               Model<CharSequence>,
               Compilable, 
               Serializable {

    static final long serialVersionUID = 2917786830470130748L;

    private final NGramProcessLM mProcessLM;
    private final char mBoundaryChar;

    private final char[] mBoundaryArray;

    /**
     * Constructs a dynamic n-gram sequence language model with the
     * specified maximum n-gram and default values for other
     * parameters.
     *
     * <P>The default number of characters is {@link
     * Character#MAX_VALUE}<code>-1</code>, the default interpolation
     * parameter ratio is equal to the n-gram length, and the boundary
     * character is the byte-order marker <code>U+FFFF</code>
     *
     * @param maxNGram Maximum n-gram length in model.
     */
    public NGramBoundaryLM(int maxNGram) {
        this(maxNGram,Character.MAX_VALUE-1);
    }

    /**
     * Constructs a dynamic n-gram sequence language model with the
     * specified maximum n-gram, specified maximum number of observed
     * characters, and default values for other parameters.
     *
     * <P>The default interpolation
     * parameter ratio is equal to the n-gram length, and the boundary
     * character is the byte-order marker <code>U+FFFF</code>
     *
     * @param maxNGram Maximum n-gram length in model.
     * @param numChars Maximum number of character seen in training
     * and test sets.
     */
    public NGramBoundaryLM(int maxNGram, int numChars) {
        this(maxNGram,numChars,maxNGram,'\uFFFF');
    }

    /**
     * Construct a dynamic n-gram sequence language model with the
     * specified maximum n-gram length, number of characters,
     * interpolation ratio hyperparameter and boundary character.
     * Note that the boundary character must not occur as a regular
     * character in the input.  Unicode provides several options for
     * marker characters; for instance the byte order markers
     * <code>U+FFFF</code> or <code>U+FEFF</code> may be used
     * internally by applications but may not be part of valid unicode
     * character streams and thus make ideal choices for boundary
     * characters.  See:
     *
     * <a href="http://www.unicode.org/versions/Unicode4.0.0/ch15.pdf">Unicode Standard, Chapter 15.8:  NonCharacters</a>
     *
     * @param maxNGram Maximum n-gram length in model.
     * @param numChars Maximum number of character seen in training
     * and test sets.
     * @param lambdaFactor Interpolation ratio hyperparameter.
     * @param boundaryChar Boundary character.
     */
    public NGramBoundaryLM(int maxNGram,
                           int numChars,
                           double lambdaFactor,
                           char boundaryChar) {
        this(new NGramProcessLM(maxNGram,numChars+1,lambdaFactor),
             boundaryChar);
    }

    /**
     * Construct an n-gram boundary language model with the specified
     * boundary character and underlying process language model.
     *
     * <p>This constructor may be used to reconstitute a serialized
     * model.  By writing the trie character sequence counter for the
     * underlying process language model, it may be read back in.
     * This may be used to construct a process language model, which
     * may be used to reconstruct a boundary language model using
     * this constructor.
     *
     * @param processLm Underlying process language model.
     * @param boundaryChar Character used to encode boundaries.
     */
    public NGramBoundaryLM(NGramProcessLM processLm,
                           char boundaryChar) {

        mBoundaryChar = boundaryChar;
        mBoundaryArray = new char[] { boundaryChar };
        mProcessLM = processLm;
    }

    /**
     * Writes this language model to the specified output stream.
     *
     * <p>A bit output is wrapped around the output stream for
     * writing.  The format begins with a delta-encoding of
     * the boundary character plus 1, and is followed by the
     * bit output of the underlying process language model.
     *
     * @param out Output stream from which to read the language model.
     * @throws IOException If there is an underlying I/O error.
     */
    public void writeTo(OutputStream out) throws IOException {
        BitOutput bitOut = new BitOutput(out);
        bitOut.writeDelta((long)(mBoundaryChar+1));
        mProcessLM.writeTo(bitOut);
        bitOut.flush();
    }

    /**
     * Read a process language model from the specified input
     * stream.
     *
     * <p>See {@link #writeTo(OutputStream)} for a description
     * of the binary format.
     *
     * @param in Input stream from which to read the model.
     * @return Process language model read from stream.
     * @throws IOException If there is an underlying I/O error.
     */
    public static NGramBoundaryLM readFrom(InputStream in)
        throws IOException {

        BitInput bitIn = new BitInput(in);
        char boundaryChar = (char) (bitIn.readDelta()-1L);
        NGramProcessLM processLM = NGramProcessLM.readFrom(bitIn);
        return new NGramBoundaryLM(processLM,boundaryChar);
    }

    /**
     * Returns the underlying n-gram process language model
     * for this boundary language model.  Changes to the returned
     * model affect this language model.
     *
     * @return The underlying process language model.
     */
    public NGramProcessLM getProcessLM() {
        return mProcessLM;
    }

    /**
     * Returns the characters that have been observed for this
     * language model, including the special boundary character.
     *
     * @return The observed characters for this langauge model.
     */
    public char[] observedCharacters() {
        return mProcessLM.observedCharacters();
    }

    /**
     * Returns the underlying substring counter for this language
     * model.  This model may be pruned by pruning the counter
     * returned by this method.
     *
     * @return The underlying substring counter for this language model.
     */
    public TrieCharSeqCounter substringCounter() {
        return mProcessLM.substringCounter();
    }


    /**
     * Writes a compiled version of this boundary language model to
     * the specified object output.  The result may be read back in
     * by casting the result of {@link ObjectInput#readObject()} to
     * {@link CompiledNGramBoundaryLM}.
     *
     * @param objOut Object output to which this model is compiled.
     * @throws IOException If there is an I/O exception during the
     * write.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    /**
     * Train the language model on the specified character sequence.
     * This method just delegates to {@link #train(CharSequence)}.
     *
     * @param cSeq Character sequence on which to train.
     */
    public void handle(CharSequence cSeq) {
        train(cSeq);
    }

    public void train(CharSequence cs, int count) {
        char[] csBounded = addBoundaries(cs,mBoundaryChar);
        mProcessLM.train(csBounded,0,csBounded.length,count);
        // don't count initial boundary
        mProcessLM.decrementUnigram(mBoundaryChar,count);
    }

    public void train(CharSequence cs) {
        train(cs,1);
    }

    public void train(char[] cs, int start, int end) {
        train(cs,start,end,1);
    }

    public void train(char[] cs, int start, int end, int count) {
        char[] csBounded = addBoundaries(cs,start,end,mBoundaryChar);
        mProcessLM.train(csBounded,0,csBounded.length,count);
        mProcessLM.decrementUnigram(mBoundaryChar,count);
    }

    public double log2ConditionalEstimate(CharSequence cs) {
        if (cs.length() < 1) {
            String msg = "Conditional estimate must be at least one character.";
            throw new IllegalArgumentException(msg);
        }
        char[] csBounded = addBoundaries(cs,mBoundaryChar);
        return mProcessLM.log2ConditionalEstimate(csBounded,0,csBounded.length-1);
    }

    public double log2ConditionalEstimate(char[] cs, int start, int end) {
        if (end <= start) {
            String msg = "Conditional estimate must be at least one character.";
            throw new IllegalArgumentException(msg);
        }
        char[] csBounded = addBoundaries(cs,start,end,mBoundaryChar);
        return mProcessLM.log2ConditionalEstimate(csBounded,0,csBounded.length-1);
    }

    public double log2Estimate(CharSequence cs) {
        char[] csBounded = addBoundaries(cs,mBoundaryChar);
        return mProcessLM.log2Estimate(csBounded,0,csBounded.length)
            - mProcessLM.log2Estimate(mBoundaryArray,0,1);

    }

    public double log2Estimate(char[] cs, int start, int end) {
        char[] csBounded = addBoundaries(cs,start,end,mBoundaryChar);
        return mProcessLM.log2Estimate(csBounded,0,csBounded.length)
            - mProcessLM.log2Estimate(mBoundaryArray,0,1);
    }

    /**
     * This method is a convenience impelementation of the {@link
     * Model} interface which delegates the call to {@link
     * #log2Estimate(CharSequence)}.
     *
     * @param cSeq Character sequence whose probability is returned.
     * @return The log (base 2) probability of the specified character sequence.
     */
    public double log2Prob(CharSequence cSeq) {
        return log2Estimate(cSeq);
    }

    /**
     * This method is a convenience implementation of the {@link Model}
     * interface which returns the result of raising 2.0 to the
     * power of the result of a call to {@link #log2Estimate(CharSequence)}.
     *
     * @param cSeq Character sequence whose probability is returned.
     * @return The log probability of the specified character sequence.
     */
    public double prob(CharSequence cSeq) {
        return java.lang.Math.pow(2.0,log2Estimate(cSeq));
    }

    /**
     * Returns a string-based representation of this language model.
     * It displays the boundary character and the contained
     * process language model.
     *
     * @return A string-based representation of this language model.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Boundary char=" + ((int)mBoundaryChar));
        sb.append('\n');
        mProcessLM.toStringBuilder(sb);
        return sb.toString();
    }

    static char[] addBoundaries(CharSequence cs, char boundaryChar) {
        char[] cs2 = new char[cs.length() + 2];
        for (int i = 0; i < cs.length(); ++i) {
            char c = cs.charAt(i);
            if (c == boundaryChar) {
                String msg = "Estimated string cannot contain boundary char."
                    + " Found boundary char=" + c
                    + " at index=" + i;
                throw new IllegalArgumentException(msg);
            }
            cs2[i+1] = cs.charAt(i);
        }
        addBoundaryChars(cs2,boundaryChar);
        return cs2;
    }

    static char[] addBoundaries(char[] cs, int start, int end, char boundaryChar) {
        char[] cs2 = new char[cs.length+1];
        int len = end-start;
        for (int i = 0; i < len; ++i) {
            char c = cs[i+start];
            if (c == boundaryChar) {
                // ugly cut and paste from above
                String msg = "Estimated string cannot contain boundary char."
                    + " Found boundary char=" + c
                    + " at index=" + (i+start);
                throw new IllegalArgumentException(msg);
            }
            cs2[i+1] = c;
        }
        addBoundaryChars(cs2,boundaryChar);
        return cs2;
    }

    static void addBoundaryChars(char[] cs, char boundaryChar) {
        cs[0] = boundaryChar;
        cs[cs.length-1] = boundaryChar;
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = -7945082563035787530L;
        final NGramBoundaryLM mLM;
        public Externalizer() {
            this(null);
        }
        public Externalizer(NGramBoundaryLM lm) {
            mLM = lm;
        }
        @Override
        public Object read(ObjectInput objIn) throws IOException {
            return new CompiledNGramBoundaryLM(objIn);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeChar(mLM.mBoundaryChar);
            mLM.mProcessLM.compileTo(objOut);
        }
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = -251292379784295407L;
        final NGramBoundaryLM mLM;
        public Serializer() {
            this(null);
        }
        public Serializer(NGramBoundaryLM lm) {
            mLM = lm;
        }
        @Override 
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeChar(mLM.mBoundaryChar);
            objOut.writeObject(mLM.mProcessLM);
        }
        public Object read(ObjectInput objIn) throws IOException, ClassNotFoundException {
            char boundaryChar = objIn.readChar();
            @SuppressWarnings("unchecked")
            NGramProcessLM lm = (NGramProcessLM) objIn.readObject();
            return new NGramBoundaryLM(lm,boundaryChar);
        }
    }

}
