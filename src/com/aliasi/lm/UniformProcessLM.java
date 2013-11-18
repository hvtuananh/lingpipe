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

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Exceptions;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>UniformLM.Sequence</code> implements a uniform sequence
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
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe2.0
 */
public class UniformProcessLM
    implements LanguageModel.Dynamic,
               LanguageModel.Process {

    // temp object for externalizable
    private final int mNumOutcomes;
    private final double mLog2EstimatePerChar;

    /**
     * Construct a uniform process language model with a
     * number of outcomes equal to the total number of
     * characters.
     */
    public UniformProcessLM() {
        this(Character.MAX_VALUE);
    }

    /**
     * Construct a uniform process language model with the specified
     * number of outcomes.  The per-character conditional estimate is
     * <code>1/numOutcomes</code>. 
     *
     * @param numOutcomes The number of outcomes for this language
     * model.
     */
    public UniformProcessLM(int numOutcomes) {
        validateNumOutcomes(numOutcomes);
    mNumOutcomes = numOutcomes;
        mLog2EstimatePerChar = -com.aliasi.util.Math.log2(numOutcomes);
    }

    /**
     * Construct a uniform process language model with the specified
     * character cross-entropy rate.  Recall that cross-entropy is
     * the negative character average log probability:
     *
     * <blockquote><code>
     * log<sub><sub>2</sub></sub> P(cs)
     * = - crossEntropyRate * cs.length()
     * </code></blockquote>
     *
     * The number of outcomes is set by rounding down the exponent
     * of the cross-entropy:
     * 
     * <blockquote><code>
     * numOutcomes = (int) 2.0<sup><sup>crossEntropyRate</sup></sup>
     * </code></blockquote>
     *
     * @param crossEntropyRate Character cross-entropy rate of the
     * uniform model.
     */
    public UniformProcessLM(double crossEntropyRate) {
        Exceptions.finiteNonNegative("Cross-entropy rate",
                                     crossEntropyRate);
    mLog2EstimatePerChar = -crossEntropyRate;
    mNumOutcomes = (int) java.lang.Math.pow(2.0,crossEntropyRate);
    }

    private UniformProcessLM(int numOutcomes, double log2EstimatePerChar) {
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
     * Writes a compiled version of this model to the specified object
     * output.  The object read back in will also be an instance
     * of {@link UniformProcessLM}.
     *
     * @param objOut Object output to which this model is written.
     * @throws IOException If there is an I/O error during the write.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
    objOut.writeObject(new Externalizer(this));
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
     * Ignores the training data.
     *
     * @param cs Ignored.
     * @param start Ignored.
     * @param end Ignored.
     */
    public void train(char[] cs, int start, int end) {
        // ignored
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
        // ignored
    }

    /**
     * Ignores the training data.
     *
     * @param cSeq Ignored.
     */
    public void train(CharSequence cSeq) { 
        // ignored
    }

    /**
     * Ignores the training data.
     *
     * @param cSeq Ignored.
     * @param count Ignored.
     */
    public void train(CharSequence cSeq, int count) { 
        // ignored
    }

    public double log2Estimate(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        return log2Estimate(end-start);
    }

    public double log2Estimate(CharSequence cSeq) {
        return log2Estimate(cSeq.length());
    }

    private double log2Estimate(int length) {
        return mLog2EstimatePerChar * (double) length;
    }

    static void validateNumOutcomes(int numOutcomes) {
        if (numOutcomes <= 0) {
            String msg = "Number of outcomes must be > 0. Found="
                + numOutcomes;
            throw new IllegalArgumentException(msg);
        }
        if (numOutcomes > Character.MAX_VALUE) {
            String msg = "Num outcomes must be <="
                + ((int)(Character.MAX_VALUE))
                + " Found value=" + numOutcomes;
            throw new IllegalArgumentException(msg);
        }
    }

    private static UniformProcessLM 
        createUniformProcessLM(int numOutcomes, double log2EstimatePerChar) {

        return new UniformProcessLM(numOutcomes,log2EstimatePerChar);
    }

    private static class Externalizer extends AbstractExternalizable {
    private static final long serialVersionUID = 8496069837136242338L;
    private final UniformProcessLM mLM;
    public Externalizer() { 
        mLM = null; 
    }
    public Externalizer(UniformProcessLM lm) {
        mLM = lm;
    }
    @Override
    public Object read(ObjectInput objIn) throws IOException {
        int numOutcomes = objIn.readInt();
        double log2EstimatePerChar = objIn.readDouble();
        return createUniformProcessLM(numOutcomes,log2EstimatePerChar);
    }
    @Override
    public void writeExternal(ObjectOutput objOut) throws IOException {
        objOut.writeInt(mLM.numOutcomes());
        objOut.writeDouble(mLM.mLog2EstimatePerChar);
    }
    }


}
