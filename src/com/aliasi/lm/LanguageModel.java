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

import com.aliasi.util.Compilable;


/**
 * A <code>LanguageModel</code> provides an estimate of the probability of a
 * sequence of characters.  Sequences of characters may be specified
 * via an array slice or with a Java {@link CharSequence}, which is an
 * interface implemented by {@link String}, {@link StringBuilder} and
 * the new I/O buffer class {@link java.nio.CharBuffer}.
 *
 * <P>There are several subinterfaces of language model.  The primary
 * distinction is between {@link LanguageModel.Sequence}
 * and {@link LanguageModel.Process}, which place different normalization
 * requirements on their estimates.  Sequence models require the sum
 * of the estimates to be 1.0 over all character sequences, whereas a
 * process requires for each length that the sum of estimates to be
 * 1.0 over all sequences of that length.  Every language model should
 * be marked by one of these two sub-interfaces.
 *
 * <P>The {@link Conditional} interface provides additional methods
 * for conditional estimates.  The {@link Dynamic} interface provides
 * a method for training the model with sample character sequence
 * data.  Finally, several of the language model implementations are
 * serializable to an object output stream.
 *
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe2.0
 */
public interface LanguageModel {

    /**
     * Returns an estimate of the log (base 2) probability of the
     * specified character slice.
     *
     * @param cs Underlying array of characters.
     * @param start Index of first character in slice.
     * @param end One plus index of last character in slice.
     * @return Log estimate of likelihood of specified character
     * sequence.
     * @throws IndexOutOfBoundsException If the start and end minus
     * one points are outside of the bounds of the character array.
     */
    public double log2Estimate(char[] cs, int start, int end);

    /**
     * Returns an estimate of the log (base 2) probability of the
     * specified character sequence.
     *
     * @param cs Character sequence to estimate.
     * @return Log estimate of likelihood of specified character
     * sequence.
     */
    public double log2Estimate(CharSequence cs);


    /**
     * A <code>LanguageModel.Conditional</code> is a language model
     * that implements conditional estimates of characters given
     * previous characters.  A conditional model should also be marked
     * as either a {@link com.aliasi.lm.LanguageModel.Process} or {@link
     * com.aliasi.lm.LanguageModel.Sequence} model.
     *
     * <P>A conditional language model should have conditional
     * estimates that are appropriate for the joint estimates.  For
     * a process language model:
     *
     * <blockquote><code>
     *   log2Estimate(cs,start,end)
     *   <br>
     *   &nbsp; =
     *      <big><big>&Sigma;</big></big><sub><sub>start < i <= end</sub></sub>
     *       log2ConditionalEstimate(cs,start,i)
     * </code></blockquote>
     *
     * For a sequence language model, the situation is more complex.
     * The joint estimate includes an estimate of the end-of-stream or
     * a length estimate in addition to the per-character conditional
     * log estimate.
     *
     * @author  Bob Carpenter
     * @version 2.0
     * @since   LingPipe2.0
     */
    public interface Conditional extends LanguageModel {

        /**
         * Returns the log (base 2) of the probability estimate for the
         * conditional probability of the last character in the specified
         * slice given the previous characters.
         *
         * @param cs Underlying array of characters.
         * @param start Index of first character in slice.
         * @param end One plus the index of the last character in the slice.
         * @return The log conditional probability estimate.
         * @throws IndexOutOfBoundsException If the start and end
         * minus one points are outside of the bounds of the character
         * array.
         */
        public double log2ConditionalEstimate(char[] cs, int start, int end);

        /**
         * Returns the log (base 2) of the probabilty estimate for the
         * conditional probability of the last character in the specified
         * character sequence given the previous characters.
         *
         * @param cSeq Character sequence to estimate.
         * @return The log conditional probability estimate.
     * @throws IndexOutOfBoundsException If the character sequence is
     * length zero.
         */
        public double log2ConditionalEstimate(CharSequence cSeq);

    /**
     * Returns the array of characters that have been observed
     * for this model.  The character array will be sorted into
     * ascending unicode order.
     *
     * @return The array of observed characters for this model.
     */
    public char[] observedCharacters();

    }


    /**
     * A <code>LanguageModel.Sequence</code> is normalized over all
     * character sequences. A sequence language model is required to
     * assign a probability of 1.0 to the sum of the probability of
     * all character sequences, regardless of length:
     *
     * <blockquote><code>
     *   <big><big>&Sigma</big></big><sub><sub>n >= 0</sub></sub>
     *   <big><big>&Sigma</big></big><sub><sub>cs.length()=n</sub></sub>
     *    2<sup><sup>log2Prob(cs)</sup></sup>
     *    = 1.0
     * </code></blockquote>
     *
     * Note that this interface is a marker interface and does not
     * specify any additional method signatures.
     *
     * @author  Bob Carpenter
     * @version 2.0
     * @since   LingPipe2.0
     */
    public interface Sequence extends LanguageModel {  /* empty interface */ }


    /**
     * A <code>LanguageModel.Process</code> is normalized by length.
     * A process language model is required to assign a probability of
     * 1.0 to the sum of the probability of all character sequences of
     * a specified length.  Specifically, this interface requires for
     * all non-negative <code>n</code> that:
     *
     * <blockquote><code>
     *   <big><big>&Sigma</big></big><sub><sub>cs.length()=n</sub></sub>
     *    2<sup><sup>log2Prob(cs)</sup></sup>
     *    = 1.0
     * </code></blockquote>
     *
     * Note that this interface is a marker interface and does not
     * specify any additional method signatures.
     *
     * @author  Bob Carpenter
     * @version 2.0
     * @since   LingPipe2.0
     */
    public interface Process extends LanguageModel { /* empty marker interface */}


    /**
     * A <code>LanguageModel.Dynamic</code> accepts training events in
     * the form of character slices or sequences.  A dynamic language
     * model should also implement either the {@link
     * LanguageModel.Process} interface or the {@link
     * LanguageModel.Sequence} interface.  
     *
     * <P>Optionally, a dynamic language model will implement the
     * {@link #compileTo(ObjectOutput)} method to write a compiled
     * version of the dynamic language model to an object output stream.
     *
     * @author  Bob Carpenter
     * @version 2.4
     * @since   LingPipe2.0
     */
    public interface Dynamic 
        extends Compilable, LanguageModel, ObjectHandler<CharSequence> {

        /**
         * Update the model with the training data provided by the
         * specified character sequence with a count of one.
         *
         * @param cs The character sequence to use as training data.
         */
        public void train(CharSequence cs);

        /**
         * Update the model with the training data provided by the
         * specified character sequence with the specified count.
         * Calling this method, <code>train(cs,n)</code> is equivalent
         * to calling <code>train(cs)</code> a total of <code>n</code>
         * times.
         *
         * @param cs The character sequence to use as training data.
         * @param count Number of instances to train.
         */
        public void train(CharSequence cs, int count);

        /**
         * Update the model with the training data provided by
         * the specified character slice.
         *
         * @param cs The underlying character array for the slice.
         * @param start Index of first character in the slice.
         * @param end Index of one plus the last character in the
         * training slice.
         * @throws IndexOutOfBoundsException If the end index minus
         * one and the start index are not in the range of the
         * character slice.
         */
        public void train(char[] cs, int start, int end);

        /**
         * Update the model with the training data provided by the
         * specified character sequence with the specifiedc count. 
         * Calling this method, <code>train(cs,n)</code> is equivalent
         * to calling <code>train(cs)</code> a total of
         * <code>n</code> times.
         *
         * Update the model with the training data provided by
         * the specified character slice.
         *
         * @param cs The underlying character array for the slice.
         * @param start Index of first character in the slice.
         * @param end Index of one plus the last character in the
         * training slice.
         * @param count Number of instances to train.
         * @throws IndexOutOfBoundsException If the end index minus
         * one and the start index are not in the range of the
         * character slice.
         */
        public void train(char[] cs, int start, int end, int count);
        
    }

    /**
     * A <code>LanguageModel.Tokenized</code> provides a means of
     * estimating the probability of a sequence of tokens.  These may
     * be returned in either linear or log form.
     *
     * @author  Bob Carpenter
     * @version 2.2
     * @since   LingPipe2.2
     */
    public interface Tokenized extends LanguageModel {

    /**
     * Returns the log (base 2) probability of the specified
     * token slice in the underlying token n-gram distribution.  This
     * includes the estimation of the actual token for unknown
     * tokens.
     *
     * @param tokens Underlying array of tokens.
     * @param start Index of first token in slice.
     * @param end Index of one past the last token in the slice.
     * @return The log (base 2) probability of the token slice.
     */
    public double tokenLog2Probability(String[] tokens, 
                       int start, int end);

    /**
     * Returns the probability of the specified token slice in the
     * token n-gram distribution.  This estimate includes the
     * estimates of the actual token for unknown tokens.
     *
     * @param tokens Underlying array of tokens.
     * @param start Index of first token in slice.
     * @param end Index of one past the last token in the slice.
     * @return The probability of the token slice.
     */
    public double tokenProbability(String[] tokens,
                       int start, int end);
    }
}
