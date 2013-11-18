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

import com.aliasi.util.Math;
import com.aliasi.util.Pair;
import com.aliasi.util.ScoredObject;

import java.util.Arrays;

/**
 * A <code>ConditionalClassification</code> is a scored classification
 * which estimates conditional probabilities of categories given an
 * input.  By default, the scores are the conditional probabilities;
 * if the scores are different than the conditional probabilities,
 * they must be in the same order.  Both score and conditional
 * probability are tracked independently by the evaluators.  The
 * method {@link #conditionalProbability(int)} returns the conditional
 * probability based on rank while the superclass method {@link
 * #score(int)} returns the score by rank.
 *
 * <P>The conditional probabilities must sum to one over the set of
 * categories:
 *
 * <blockquote><code>
 * <big><big>&Sigma;</big></big><sub><sub>rank&lt;size()</sub></sub>
 * score(rank) = 1.0
 * </code></blockquote>
 *
 * <P>The constructors check that this criterion is satisfied to
 * within a specified arithmetic tolerance.  The convenience method
 * {@link com.aliasi.stats.Statistics#normalize(double[])} may be used
 * to normalize an array of probability ratios so that they will be an
 * acceptable input to this constructor, but note the warning in that
 * method's documentation concerning arithmetic precision.
 *
 * @author Bob Carpenter
 * @version 3.9
 * @since   LingPipe2.0
 */
public class ConditionalClassification extends ScoredClassification {

    private final double[] mConditionalProbs;

    /**
     * Construct a conditional classification with the specified
     * categories and conditional probabilities which sum to one
     * within the default tolerance of <code>0.01</code>.  The
     * conditional probabilities are used as the scores.
     *
     * @param categories Categories assigned by classification.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @throws IllegalArgumentException If the category and
     * probability arrays are of different lengths, if the
     * probabilities or scores are not in descending order, if any
     * probability is less than zero or greater than one, or if their
     * sum is not 1.0 plus or minus 0.01.
     */
    public ConditionalClassification(String[] categories,
                                     double[] conditionalProbs) {
        this(categories,conditionalProbs,conditionalProbs,TOLERANCE);
    }

    /**
     * Construct a conditional classification with the specified
     * categories, scores and conditional probabilities which sum to
     * one within the default tolerance of <code>0.01</code>.  The
     * scores and conditional probs must be of the same length as the
     * categories and in descending numerical order.
     *
     * @param categories Categories assigned by classification.
     * @param scores Scores of the categories.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @throws IllegalArgumentException If the category and
     * probability arrays are of different lengths, if the
     * probabilities or scores are not in descending order, if any
     * probability is less than zero or greater than one, or if their
     * sum is not 1.0 plus or minus 0.01.
     */
    public ConditionalClassification(String[] categories,
                                     double[] scores,
                                     double[] conditionalProbs) {
        this(categories,scores,conditionalProbs,TOLERANCE);
    }

    /**
     * Construct a conditional classification with the specified
     * categories and conditional probabilities whose probabilities
     * sum to one within the specified tolerance.  By setting the
     * tolerance to <code>Double.POSITIVE_INFINITY</code>, there is
     * effectively no consistency requirement placed on the
     * conditional probabilities.
     *
     * @param categories Categories assigned by classification.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @param tolerance Tolerance within which the conditional probabilities
     * must sum to one.
     * @throws IllegalArgumentException If the category and
     * probability arrays are of different lengths, if the probabilities
     * are not in descending order, if any probability is less than
     * zero or greater than one, or if their sum is not 1.0 plus or
     * minus the tolerance, or if the tolerance is not a positive number.
     */
    public ConditionalClassification(String[] categories,
                                     double[] conditionalProbs,
                                     double tolerance) {
        this(categories,conditionalProbs,conditionalProbs,tolerance);
    }


    /**
     * Construct a conditional classification with the specified
     * categories and conditional probabilities whose probabilities
     * sum to one within the specified tolerance.  By setting the
     * tolerance to <code>Double.POSITIVE_INFINITY</code>, there is
     * effectively no consistency requirement placed on the
     * conditional probabilities.
     *
     * @param categories Categories assigned by classification.
     * @param scores Scores of the categories.
     * @param conditionalProbs Conditional probabilities of the
     * categories.
     * @param tolerance Tolerance within which the conditional probabilities
     * must sum to one.
     * @throws IllegalArgumentException If the category and
     * probability or score arrays are of different lengths, if the
     * probabilities or scores are not in descending order, if any
     * probability is less than zero or greater than one, or if their
     * sum is not 1.0 plus or minus the tolerance, or if the tolerance
     * is not a positive number.
     */
    public ConditionalClassification(String[] categories,
                                     double[] scores,
                                     double[] conditionalProbs,
                                     double tolerance) {
        super(categories,scores);
        mConditionalProbs = conditionalProbs;
        if (tolerance < 0.0 || Double.isNaN(tolerance)) {
            String msg = "Tolerance must be a positive number."
                + " Found tolerance=" + tolerance;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < conditionalProbs.length; ++i) {
            if (conditionalProbs[i] < 0.0 || conditionalProbs[i] > 1.0) {
                String msg = "Conditional probabilities must be "
                    + " between 0.0 and 1.0."
                    + " Found conditionalProbs[" + i + "]="
                    + conditionalProbs[i];
                throw new IllegalArgumentException(msg);
            }
        }
        double sum = Math.sum(conditionalProbs);
        if (sum < (1.0-tolerance)  || sum > (1.0+tolerance)) {
            String msg = "Conditional probabilities must sum to 1.0."
                + " Acceptable tolerance=" + tolerance
                + " Found sum=" + sum;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns the conditional probability estimate for the category
     * at the specified rank.  Note that this method returns the same
     * result as {@link #score(int)} if this classification was initialized
     * without explicit score values.
     *
     * @param rank Rank of category.
     * @return The conditional probability of the category at the
     * specified rank.
     * @throws IllegalArgumentException If the rank is out of range.
     */
    public double conditionalProbability(int rank) {
        if (rank < 0 || rank > (mConditionalProbs.length - 1)) {
            String msg = "Require rank in range 0.."
                + (mConditionalProbs.length-1)
                + " Found rank=" + rank;
            throw new IllegalArgumentException(msg);
        }
        return mConditionalProbs[rank];
    }

    /**
     * Returns the conditional probability estimate for the specified category.
     *
     * @param category category to look for
     * @return The conditional probability of the specified category
     * @throws IllegalArgumentException If there is no such category.
     */
    public double conditionalProbability(String category) {
        for(int rank=0;rank<this.size();rank++) {
            if (category(rank).equals(category)) {
                return conditionalProbability(rank);
            }
        }
        String msg = category + " is not a valid category for this classification.  Valid categories are:";
        for(int rank=0;rank<this.size();rank++) {
            msg+=" " + category(rank) + ",";
        }
        msg = msg.substring(0,msg.length()-1);
        throw new IllegalArgumentException(msg);
    }

    /**
     * Returns a string-based representation of this conditional
     * probability ranked classification.
     *
     * @return A string-based representation of this classification.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank  Category  Score  P(Category|Input)\n");
        for (int i = 0; i < size(); ++i)
            sb.append(i + "=" + category(i) + " " + score(i)
                      + " " + conditionalProbability(i) + '\n');
        return sb.toString();
    }

    private static final double TOLERANCE = 0.01;


    /**
     * Return a conditional classification given the categories and
     * log probabilities.  The log probabilities do not need to sum to
     * 1; e.g. they could come from joint estimates.  This method will
     * normalize them and convert to linear probabilities, attempting
     * to prevent underflow.  The log probabilities do not need to be
     * sorted into descending order of probability.
     *
     * @param categories Array of categories.
     * @param logProbabilities Parallel array of log probabilities.
     * @return Conditional classification corresponding to categories
     * and probabilities.
     * @throws IllegalArgumentException If any of the log probabilities
     * is infinite, not a number, or positive, or if the arrays are not
     * of the same length.
     */
    public static ConditionalClassification
        createLogProbs(String[] categories,
                       double[] logProbabilities) {
        verifyLengths(categories,logProbabilities);
        verifyLogProbs(logProbabilities);
        double[] linearProbs = logJointToConditional(logProbabilities);
        Pair<String[],double[]> catsProbs = sort(categories,linearProbs);
        return new ConditionalClassification(catsProbs.a(),
                                             catsProbs.b());
    }

    /**
     * Static factory method for conditional classifications based on
     * the specified categories and linear probability ratios.  The
     * probabilities do not need to be normalized as they will be
     * renormalized by this method.  The probability ratios do not
     * need to be sorted.
     *
     * <p>If all probability ratios are zero, the result will be
     * a uniform distribution of the same probability for each
     * entry.
     *
     * @param categories Categories for classification.
     * @param probabilityRatios Parallel array of linear probability
     * ratios for the specified categories.
     * @return The corresponding conditional classification.
     * @throws IllegalArgumentException If any of the probability ratios
     * are not non-negative and finite numbers.
     */
    public static ConditionalClassification
        createProbs(String[] categories,
                    double[] probabilityRatios) {
        
        for (int i = 0; i < probabilityRatios.length; ++i) {
            if (probabilityRatios[i] < 0.0 || Double.isInfinite(probabilityRatios[i]) || Double.isNaN(probabilityRatios[i])) {
                String msg = "Probability ratios must be non-negative and finite."
                    + " Found probabilityRatios[" + i + "]=" + probabilityRatios[i];
                throw new IllegalArgumentException(msg);
            }
        }
        if (com.aliasi.util.Math.sum(probabilityRatios) == 0.0) {
            double[] conditionalProbs = new double[probabilityRatios.length];
            Arrays.fill(conditionalProbs, 1.0/probabilityRatios.length);
            return new ConditionalClassification(categories,conditionalProbs);
        }
            
        double[] logProbs = new double[probabilityRatios.length];
        for (int i = 0; i < probabilityRatios.length; ++i)
            logProbs[i] = com.aliasi.util.Math.log2(probabilityRatios[i]);
        return createLogProbs(categories,logProbs);
    }

    static void verifyLogProbs(double[] logProbabilities) {
        for (double x : logProbabilities) {
            if (Double.isNaN(x) || (x > 0.0)) {
                String msg = "Log probs must be non-positive numbers."
                    + " Found x=" + x;
                throw new IllegalArgumentException(msg);
            }
        }
    }


    static void verifyLengths(String[] categories, double[] logProbabilities) {
        if (categories.length != logProbabilities.length) {
            String msg = "Arrays must be same length."
                + " Found categories.length=" + categories.length
                + " logProbabilities.length=" + logProbabilities.length;
            throw new IllegalArgumentException(msg);
        }
    }

    static Pair<String[],double[]> sort(String[] categories, double[] vals) {
        verifyLengths(categories,vals);

        @SuppressWarnings({"unchecked","rawtypes"})
        ScoredObject<String>[] scoredObjects
            = (ScoredObject<String>[]) new ScoredObject[categories.length];

        for (int i = 0; i < categories.length; ++i)
            scoredObjects[i] = new ScoredObject<String>(categories[i],vals[i]);

        String[] categoriesSorted = new String[scoredObjects.length];

        double[] valsSorted = new double[categories.length];

        Arrays.sort(scoredObjects,ScoredObject.reverseComparator());
        for (int i = 0; i < scoredObjects.length; ++i) {
            categoriesSorted[i] = scoredObjects[i].getObject();
            valsSorted[i] = scoredObjects[i].score();
        }

        return new Pair<String[],double[]>(categoriesSorted,valsSorted);
    }

    static double[] logJointToConditional(double[] logJointProbs) {
        for (int i = 0; i < logJointProbs.length; ++i) {
            if (logJointProbs[i] > 0.0 && logJointProbs[i] < 0.0000000001)
                logJointProbs[i] = 0.0;
            if (logJointProbs[i] > 0.0 || Double.isNaN(logJointProbs[i])) {
                StringBuilder sb = new StringBuilder();
                sb.append("Joint probs must be zero or negative."
                          + " Found log2JointProbs[" + i + "]=" + logJointProbs[i]);
                for (int k = 0; k < logJointProbs.length; ++k)
                    sb.append("\nlogJointProbs[" + k + "]=" + logJointProbs[k]);
                throw new IllegalArgumentException(sb.toString());
            }
        }
        double max = com.aliasi.util.Math.maximum(logJointProbs);
        double[] probRatios = new double[logJointProbs.length];
        for (int i = 0; i < logJointProbs.length; ++i) {
            probRatios[i] = java.lang.Math.pow(2.0,logJointProbs[i] - max);  // diff is <= 0.0
            if (probRatios[i] == Double.POSITIVE_INFINITY)
                probRatios[i] = Float.MAX_VALUE;
            else if (probRatios[i] == Double.NEGATIVE_INFINITY || Double.isNaN(probRatios[i]))
                probRatios[i] = 0.0;
        }
        return com.aliasi.stats.Statistics.normalize(probRatios);
    }





}
