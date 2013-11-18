package com.aliasi.lm;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Exceptions;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>UniformBoundaryLM</code> implements a uniform sequence
 * language model with a specified number of outcomes and the same
 * probability assigned to the end-of-stream marker.  The formula
 * for computing sequence likelihood estimates is:
 *
 * <blockquote><code>
 *   log2Estimate(cSeq) =
 *   = log<sub><sub>2</sub></sub> ( (cSeq.length()+1) / (numOutcomes+1) )
 * </code></blockquote>
 *
 * Adding one to the number of outcomes makes the end-of-sequence
 * just as likely as any other character.  Adding one to the
 * sequence length adds the log likelihood of the end-of-sequence
 * marker itself.
 *
 * <p>This model is defined as dynamic for convenience.  Calls
 * to the training methods have no effect.
 *
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe2.0
 */
public class UniformBoundaryLM
    implements LanguageModel.Dynamic,
               LanguageModel.Sequence {

    private final double mLog2EstimatePerChar;
    private final int mNumOutcomes;

    /**
     * Construct uniform boundary language model with the full set
     * of characters.
     */
    public UniformBoundaryLM() {
        this(Character.MAX_VALUE-1);
    }

    /**
     * Construct a uniform boundary language model with the specified
     * number of outcomes.  The estimate will include the
     * end-of-stream boundary output and thus the per-character
     * estimate will be <code>1/(numOutcomes+1)</code>.
     *
     * @param numOutcomes Number of outcomes.
     */
    public UniformBoundaryLM(int numOutcomes) {
        UniformProcessLM.validateNumOutcomes(numOutcomes+1);
        mNumOutcomes = numOutcomes;
        mLog2EstimatePerChar 
            = -com.aliasi.util.Math.log2(1.0 + (double)numOutcomes);
    }

    /**
     * Create a constant uniform boundary LM with the specified
     * character cross-entropy rate.  Recall that cross-entropy is the
     * negative character average log probability.  Thus the log
     * estimate returned for a boundary model will include the final
     * terminator, and yield:
     *
     * <blockquote><code>
     * log<sub><sub>2</sub></sub> P(cs) 
     * = - crossEntropyRate * (cs.length() + 1)
     * </code></blockquote>
     *
     * The number of outcomes is set by rounding down the exponent of
     * the cross-entropy and subtracting one for the boundary
     * character:
     * 
     * <blockquote><code>
     * numOutcomes = (int) 2.0<sup><sup>crossEntropyRate</sup></sup> - 1
     * </code></blockquote>
     * 
     * Even if the above expression evaluates to less than zero, the
     * number of outcomes will then be rounded up to zero.
     *
     * @param crossEntropyRate The cross-entropy rate of the model.
     * @throws IllegalArgumentException If the cross-entropy rate is
     * not finite and non-negative.
     */
    public UniformBoundaryLM(double crossEntropyRate) {
        Exceptions.finiteNonNegative("Cross-entropy rate", 
                                     crossEntropyRate);
        mLog2EstimatePerChar = -crossEntropyRate;
        mNumOutcomes = Math.max(0,
                                (int) (java.lang.Math.pow(2.0,crossEntropyRate) - 1.0));
    }
    
    private UniformBoundaryLM(int numOutcomes,
                              double log2EstimatePerChar) {
        mNumOutcomes = numOutcomes;
        mLog2EstimatePerChar = log2EstimatePerChar;
    }

    /**
     * Returns the number of outcomes for this uniform model.
     *
     * @return The number of outcomes for this uniform model.
     */
    public int numOutcomes() {
        return mNumOutcomes;
    }

    /**
     * This method for training a character sequence is supplied
     * for compatibility with the dynamic language model interface,
     * but is implemented to do nothing.
     *
     * @param cs Ignored.
     */
    public void handle(CharSequence cs) {
        /* no op */
    }

    /**
     * Writes a compiled version of this model to the specified object
     * output.  The object read back in will also be an instance
     * of {@link UniformBoundaryLM}.
     *
     * @param objOut Object output to which this model is written.
     * @throws IOException If there is an I/O error during the write.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    /**
     * Ignores the training data.
     *
     * @param cs Ignored.
     * @param start Ignored.
     * @param end Ignored.
     */
    public void train(char[] cs, int start, int end) {
        // ignore
    }

    /**
     * Ignores the training data.
     *
     * @param cs Ignored.
     * @param start Ignored.
     * @param end Ignored.
     * @param count Ignored.
     */
    public void train(char[] cs, int start, int end, int count) {
        // ignore
    }

    /**
     * Ignores the training data.
     *
     * @param cSeq Ignored.
     */
    public void train(CharSequence cSeq) { 
        // ignore
    }


    /**
     * Ignores the training data.
     *
     * @param cSeq Ignored.
     * @param count Ignored.
     */
    public void train(CharSequence cSeq, int count) { 
        // ignore
    }


    public double log2Estimate(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        return log2Estimate(end-start);
    }

    public double log2Estimate(CharSequence cSeq) {
        return log2Estimate(cSeq.length());
    }

    private double log2Estimate(int length) {
        return mLog2EstimatePerChar * (1.0 + (double) length);
    }

    private static UniformBoundaryLM 
        createUniformBoundaryLM(int numOutcomes,
                                double log2EstimatePerChar) {
        return new UniformBoundaryLM(numOutcomes,log2EstimatePerChar);
    }

    /**
     * A constant uniform boundary language model returning
     * zero log estimates.  This is done by setting the number
     * of characters to zero.  
     *
     * <P>This constant is particularly useful for removing the
     * contribution of whitespace characters to token n-gram language
     * models.
     */
    public static final UniformBoundaryLM ZERO_LM
        = new UniformBoundaryLM(0);

    private static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -5389627995529538230L;
        private final UniformBoundaryLM mLM;
        public Externalizer() { 
            mLM = null; 
        }
        public Externalizer(UniformBoundaryLM lm) {
            mLM = lm;
        }
        @Override
        public Object read(ObjectInput objIn) throws IOException {
            int numOutcomes = objIn.readInt();
            double log2EstimatePerChar = objIn.readDouble();
            return createUniformBoundaryLM(numOutcomes,log2EstimatePerChar);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mLM.numOutcomes());
            objOut.writeDouble(mLM.mLog2EstimatePerChar);
        }
    }
}
