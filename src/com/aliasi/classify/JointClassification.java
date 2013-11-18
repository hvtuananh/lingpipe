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

package com.aliasi.classify;

import com.aliasi.util.Pair;

// import com.aliasi.util.Math;  (conflicts with java.Math)

/**
 * A <code>JointClassification</code> is a conditional classification
 * derived from a joint probability assignment to each category and
 * the object being classified.  The conditional probabilities are
 * computed from the joint probabilities, but an additional score may
 * be provided for ordering.  These scores must be ordered in the same
 * way as the joint probabilities.  For example, the language model
 * classifiers implement the score as an entropy rate to allow
 * between-document comparisons.
 *
 * <P>In addition to the score and conditional probability methods,
 * this interface adds a method to retrieve joint log (base 2)
 * probability by rank, {@link #jointLog2Probability(int)}.
 *
 * <P>The conditional probability estimate of the category given the
 * input is derived from the joint probability of category and input:
 *
 * <blockquote><code>
 * P(category|input) = P(category,input) / P(input)
 * </code></blockquote>
 *
 * where the joint probability <code>P(category,input)</code> is
 * determined by the joint probability estimate and the input
 * probability <code>P(input)</code> is estimated by marginalization:
 *
 * <blockquote><code>
 *  P(input)
 *  = <big><big>&Sigma;</big></big><sub><sub>category</sub></sub>
 *    P(category,input)
 * </code>
 * </code></blockquote>
 *
 * <P><b>Warning:</b> The result of marginalization is the same as
 * that of {@link com.aliasi.stats.Statistics#normalize(double[])}
 * applied to the joint probabilities.  The same warning carries over
 * here: if the largest joint probability is more than
 * <code>2<sup><sup>52</sup></sup></code> times larger than the next
 * largest, the largest will round off to one and all others will
 * round off to zero due to underflow.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public class JointClassification extends ConditionalClassification {

    private final double[] mLogJointProbs;

    /**
     * Construct a joint classification with the specified parallel
     * arrays of categories and log (base 2) joint probabilities of
     * category and input object.  The scores are taken to be the log
     * joint probabilities.  Joint log probabilities must be in
     * descending numerical order and all log probabilities all must
     * be zero or negative.  If a probability is zero, the
     * corresponding log probability should be
     * <code>Double.NEGATIVE_INFINITY</code>, which is a legal input
     * to this constructor.
     *
     * @param categories Array of categories.
     * @param log2JointProbs Log (base 2) joint probabilities of
     * categories, in descending numerical order.
     * @throws IllegalArgumentException If any of the log joint
     * probabilities is not zero or negative, or if they are not
     * in descending order.
     */
    public JointClassification(String[] categories,
                               double[] log2JointProbs) {
        this(categories,log2JointProbs,log2JointProbs);
    }

    JointClassification(String[] categories,
                        double[] condProbs,
                        double[] log2JointProbs,
                        double tolerance) {
        super(categories,
              condProbs,
              tolerance);
        mLogJointProbs = log2JointProbs;
    }



    /**
     * Construct a joint classification with the specified parallel
     * arrays of categories and log (base 2) joint probabilities of
     * category and input object.  The scores and joint probabilities
     * must be in descending numerical order.  Log probabilities all
     * must be zero or negative.  If a probability is zero, the
     * corresponding log probability should be
     * <code>Double.NEGATIVE_INFINITY</code>, which is a legal input
     * to this constructor.
     *
     * @param categories Array of categories.
     * @param scores Scores of categories, in descending numerical
     * order.
     * @param log2JointProbs Log (base 2) joint probabilities of
     * categories, in descending numerical order.
     * @throws IllegalArgumentException If any of the log joint
     * probabilities is not zero or negative, or if they are not
     * in descending order.
     */
    public JointClassification(String[] categories,
                               double[] scores,
                               double[] log2JointProbs) {
        super(categories,
              scores,
              logJointToConditional(log2JointProbs),
              Double.POSITIVE_INFINITY);  // unlimited tolerance
        mLogJointProbs = log2JointProbs;
    }

    /**
     * Returns the log (base 2) probability of the category at
     * the specified rank.  Note that this is the same value as
     * is returned by {@link #score(int)}.
     *
     * @param rank Rank of result.
     * @return Log (base 2) estimate of the joint probability of the
     * category of the specified rank and the input object.
     */
    public double jointLog2Probability(int rank) {
        return rank >= mLogJointProbs.length
            ? Double.NEGATIVE_INFINITY
            : mLogJointProbs[rank];
    }

    /**
     * Returns the cross-entropy rate of the category and text at the
     * specified rank.  As with all ranked classifications, scores are
     * in non-ascending order.
     *
     * <p>The cross-entropy rate of the category and text is defined
     * differently than the cross-entropy of the text.   For the
     * combination, we divide the log (base 2) probability of the
     * text plus the log (base 2) probability of the category
     * by the length of the text plus 1.  This non-standard definition
     * ensures that the cross-entropy ordering remains the same as
     * the joint probability ordering.
     *
     * @return The cross-entropy rate of the category at the specified
     * rank.
     */
    @Override
    public double score(int rank) {
        return super.score(rank); // is there a way to remove this & comment?
    }

    /**
     * Returns a string-based representation of this joint probability
     * ranked classification.
     *
     * @return A string-based representation of this classification.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank  Category  Score  P(Category|Input)   log2 P(Category,Input)\n");
        for (int i = 0; i < size(); ++i)
            sb.append(i + "=" + category(i) + " " + score(i)
                      + " " + conditionalProbability(i)
                      + " " + jointLog2Probability(i) + '\n');
        return sb.toString();
    }

    /**
     * Return a joint classification given the categories and log
     * probabilities.
     *
     * <p>The log probabilities must be finite and non-positive.
     * A collection of joint probabilities should not exceed 1.0, but
     * there is no such check; the result is just normalized.
     *
     * @param categories Array of categories.
     * @param logProbabilities Parallel array of log probabilities.
     * @return Joint classification corresponding to categories
     * and probabilities.
     * @throws IllegalArgumentException If any of the log probabilities
     * is infinite, not a number, or positive, or if the arrays are not
     * of the same length.
     */
    public static JointClassification
        create(String[] categories,
               double[] logProbabilities) {

        verifyLengths(categories,logProbabilities);
        verifyLogProbs(logProbabilities);
        Pair<String[],double[]> catsProbs = sort(categories,logProbabilities);
        return new JointClassification(catsProbs.a(),
                                       catsProbs.b());
    }



}
