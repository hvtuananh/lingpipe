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

import com.aliasi.util.Scored;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code JointClassifierEvaluator} provides an evaluation harness
 * for joint probability-based n-best classifiers.  It extends the
 * conditional classifier evaluator with joint probability specific
 * evaluation metrics.
 *
 * <h4>Thread Safety</h4>
 *
 * This class must be read-write synchronized externally for use in
 * multiple threads.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 * @param <E> The type of objects being classified by the evaluated classifier.
 */
public class JointClassifierEvaluator<E> extends ConditionalClassifierEvaluator<E> {

    /**
     * Construct a scored classifier evaluator with the specified
     * classifier, categories and flag indicating whether or not to
     * store inputs.
     *
     * @param classifier Classifier to evaluate.
     * @param categories Complete list of categories.
     * @param storeInputs Set to {@code true} to store input objects.
     */
    public JointClassifierEvaluator(JointClassifier<E> classifier,
                                    String[] categories,
                                    boolean storeInputs) {
        super(classifier,categories,storeInputs);
    }

    /**
     * Set the classifier being evaluated to the specified value.  This
     * method is useful to evaluate multiple classifiers with the same
     * evaluator, for instance for use in cross-validation.
     *
     * @param classifier New classifier for this evaluation.
     * @throws IllegalArgumentException If called from an evaluator with
     * a runtime type other than {@code JointClassifierEvaluator}.
     */
    public void setClassifier(JointClassifier<E> classifier) {
        setClassifier(classifier,JointClassifierEvaluator.class);
    }

    /**
     * Return the classifier being evaluated.
     *
     * @return The classifier for this evaluator.
     */
    @Override
    public JointClassifier<E> classifier() {
        @SuppressWarnings("unchecked")
        JointClassifier<E> result
            = (JointClassifier<E>) super.classifier();
        return result;
    }

    /**
     * Returns the average log (base 2) joint probability of the
     * response category for cases of the specified reference
     * category.  If there are no cases matching the reference
     * category, the result is <code>Double.NaN</code>.
     *
     * <P>Better classifiers return high values when the reference
     * and response categories are the same and lower values
     * when they are different.  Unlike the conditional probability
     * values, joint probability averages are not particularly
     * useful because they are not normalized by input length.  For
     * the language model classifiers, the scores are normalized
     * by length, and provide a better cross-case view.
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average log (base 2) conditional probability of
     * response category in cases for specified reference category.
     * @throws IllegalArgumentException If the either category is unknown.
     * @throws ClassCastException if the classifications are not joint
     * classifications.
     */
    public double averageLog2JointProbability(String refCategory,
                                              String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                JointClassification c
                    = (JointClassification) mClassifications.get(i);
                for (int rank = 0; rank < c.size(); ++rank) {
                    if (c.category(rank).equals(responseCategory)) {
                        sum += c.jointLog2Probability(rank);
                        ++count;
                        break;
                    }
                }
            }
        }
        return sum / (double) count;
    }


    /**
     * Returns the average over all test cases of the joint log (base
     * 2) probability of the response that matches the reference
     * category.  Better classifiers return higher values for this
     * average.
     *
     * <P>Whether average scores make sense across training instances
     * depends on the classifier.  For the language-model based
     * classifiers, the normalized score values are more reasonable
     * averages.
     *
     * @return The average joint log probability of the reference
     * category in the response.
     */
    public double averageLog2JointProbabilityReference() {
        double sum = 0.0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCategory = mReferenceCategories.get(i).toString();
            JointClassification c
                = (JointClassification) mClassifications.get(i);
            for (int rank = 0; rank < c.size(); ++rank) {
                if (c.category(rank).equals(refCategory)) {
                    sum += c.jointLog2Probability(rank);
                    break;
                }
            }
        }
        return sum / (double) mReferenceCategories.size();
    }

    /**
     * Returns the joint log (base 2) probability of the entire
     * evaluation corpus.  This is defined independently of the
     * reference categories by summing over inputs <code>x</code>:
     *
     * <blockquote><code>
     * log2 p(corpus)
     * = <big><big>&Sigma;</big></big><sub><sub>x in corpus</sub></sub> log2 p(x)
     * </code></blockquote>
     *
     * where the probability <code>p(x)</code> for a single case with
     * input <code>x</code> is defined in the usual way by summing
     * over categories:
     *
     * <blockquote><code>
     * p(x) = <big><big>&Sigma;</big></big><sub><sub>c in cats</sub></sub> p(c,x)
     * </code></blockquote>
     *
     * @return The log probability of the set of inputs.
     * @throws ClassCastException if the classifications are not joint
     * classifications.
     */
    public double corpusLog2JointProbability() {
        double total = 0.0;
        for (int i = 0; i < mClassifications.size(); ++i) {
            JointClassification c
                = (JointClassification) mClassifications.get(i);
            double maxJointLog2P = Double.NEGATIVE_INFINITY;
            for (int rank = 0; rank < c.size(); ++rank) {
                double jointLog2P = c.jointLog2Probability(rank);
                if (jointLog2P > maxJointLog2P)
                    maxJointLog2P = jointLog2P;
            }
            double sum = 0.0;
            for (int rank = 0; rank < c.size(); ++rank)
                sum += Math.pow(2.0,c.jointLog2Probability(rank) - maxJointLog2P);
            total += maxJointLog2P + com.aliasi.util.Math.log2(sum);
        }
        return total;
    }

    @Override
    void baseToString(StringBuilder sb) {
        super.baseToString(sb);
        sb.append("Average Log2 Joint Probability Reference="
                  + averageLog2JointProbabilityReference() + "\n");
    }

    @Override
    void oneVsAllToString(StringBuilder sb, String category, int i) {
        super.oneVsAllToString(sb,category,i);
        sb.append("Average Joint Probability Histogram=\n");
        appendCategoryLine(sb);
        for (int j = 0; j < numCategories(); ++j) {
            if (j > 0) sb.append(',');
            sb.append(averageLog2JointProbability(category,
                                                  categories()[j]));
        }
        sb.append("\n");
    }


}