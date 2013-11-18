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
 * A {@code ConditionalClassifierEvaluator} provides an evaluation
 * harness for conditional probability-based n-best classifiers.  It
 * extends the scored classifier evaluator with conditional probability
 * specific evaluation metrics.
 *
 * <h4>Thread Safety</h4>
 *
 * This class must be read-write synchronized externally for use
 * in multiple threads.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 * @param <E> The type of objects being classified by the evaluated classifier.
 */
public class ConditionalClassifierEvaluator<E> extends ScoredClassifierEvaluator<E> {

    private final List<ScoreOutcome>[] mConditionalOutcomeLists;
    boolean mDefectiveConditioning = false;

    /**
     * Construct a scored classifier evaluator with the specified
     * classifier, categories and flag indicating whether or not to
     * store inputs.
     *
     * @param classifier Classifier to evaluate.
     * @param categories Complete list of categories.
     * @param storeInputs Set to {@code true} to store input objects.
     */
    public ConditionalClassifierEvaluator(ConditionalClassifier<E> classifier,
                                          String[] categories,
                                          boolean storeInputs) {
        super(classifier,categories,storeInputs);
        @SuppressWarnings({"unchecked","rawtypes"})
        List<ScoreOutcome>[] conditionalOutcomeLists = new ArrayList[numCategories()];
        mConditionalOutcomeLists = conditionalOutcomeLists;
        for (int i = 0; i < mConditionalOutcomeLists.length; ++i)
            mConditionalOutcomeLists[i] = new ArrayList<ScoreOutcome>();
    }

    /**
     * Set the classifier being evaluated to the specified value.  This
     * method is useful to evaluate multiple classifiers with the same
     * evaluator, for instance for use in cross-validation.
     *
     * @param classifier New classifier for this evaluation.
     * @throws IllegalArgumentException If called from an evaluator with
     * a runtime type other than {@code ConditionalClassifierEvaluator}.
     */
    public void setClassifier(ConditionalClassifier<E> classifier) {
        setClassifier(classifier,ConditionalClassifierEvaluator.class);
    }

    /**
     * Return the classifier being evaluated.
     *
     * @return The classifier for this evaluator.
     */
    @Override
    public ConditionalClassifier<E> classifier() {
        @SuppressWarnings("unchecked")
        ConditionalClassifier<E> result
            = (ConditionalClassifier<E>) super.classifier();
        return result;
    }

    @Override
    public void handle(Classified<E> classified) {
        // CUT AND PASTE FROM SUPERCLASS, and INTO SUBCLASS
        E input = classified.getObject();
        Classification refClassification = classified.getClassification();
        String refCategory = refClassification.bestCategory();
        validateCategory(refCategory);
        ConditionalClassification classification = classifier().classify(input);
        addClassification(refCategory,classification,input);
        addRanking(refCategory,classification);
        addScoring(refCategory,classification);

        // new stuff
        addConditioning(refCategory,classification);
    }

    /**
     * Returns the average conditional probability of the specified response
     * category for test cases with the specified reference category.  If
     * there are no cases matching the reference category, the result
     * is <code>Double.NaN</code>.  If the conditional classifiers'
     * results are properly normalized, the sum of the averages over
     * all categories will be 1.0.
     *
     * <P>Better classifiers return high values when the reference and
     * response categories are the same and lower values when they are
     * different.  The log value would be extremely volatile given the
     * extremely low and high conditional estimates of the language
     * model classifiers.
     *
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average conditional probability of response category in
     * cases for specified reference category.
     * @throws IllegalArgumentException If the either category is unknown.
     */
    public double averageConditionalProbability(String refCategory,
                                                String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                ConditionalClassification c
                    = (ConditionalClassification) mClassifications.get(i);
                for (int rank = 0; rank < c.size(); ++rank) {
                    if (c.category(rank).equals(responseCategory)) {
                        sum += c.conditionalProbability(rank);
                        ++count;
                        break;
                    }
                }
            }
        }
        return sum / (double) count;
    }

    /**
     * Returns the average over all test cases of the conditional
     * probability of the response that matches the reference
     * category.  Better classifiers return higher values for this
     * average.
     *
     * <P>As a normalized value, the average conditional probability
     * always has a sensible interpretation across training instances.
     *
     * @return The average conditional probability of the reference
     * category in the response.
     */
    public double averageConditionalProbabilityReference() {
        double sum = 0.0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCategory = mReferenceCategories.get(i).toString();
            ConditionalClassification c
                = (ConditionalClassification) mClassifications.get(i);
            for (int rank = 0; rank < c.size(); ++rank) {
                if (c.category(rank).equals(refCategory)) {
                    sum += c.conditionalProbability(rank);
                    break;
                }
            }
        }
        return sum / (double) mReferenceCategories.size();
    }

    /**
     * Returns a scored precision-recall evaluation of the
     * classifcation of the specified reference category versus all
     * other categories using the conditional probability scores.
     * This method may only be called for evaluations that have
     * scores.
     *
     * @param refCategory Reference category.
     * @return The conditional one-versus-all precision-recall evaluatuion.
     * @throws IllegalArgumentException If the specified category
     * is unknown.
     */
    public ScoredPrecisionRecallEvaluation
        conditionalOneVersusAll(String refCategory) {

        validateCategory(refCategory);
        return scoredOneVersusAll(mConditionalOutcomeLists,
                                  categoryToIndex(refCategory));
    }

    void addConditioning(String refCategory,
                                 ConditionalClassification scoring) {
        if (scoring.size() < numCategories())
            mDefectiveConditioning = true;
        for (int rank = 0; rank < numCategories() && rank < scoring.size(); ++rank) {
            double score = scoring.conditionalProbability(rank);
            String category = scoring.category(rank);
            int categoryIndex = categoryToIndex(category);
            boolean match = category.equals(refCategory);
            ScoreOutcome outcome = new ScoreOutcome(score,match,rank==0);
            mConditionalOutcomeLists[categoryIndex].add(outcome);
        }
    }

    @Override
    void baseToString(StringBuilder sb) {
        super.baseToString(sb);
        sb.append("Average Conditional Probability Reference="
                  + averageConditionalProbabilityReference() + "\n");
    }

    @Override
    void oneVsAllToString(StringBuilder sb, String category, int i) {
        super.oneVsAllToString(sb,category,i);
        sb.append("Conditional One Versus All\n");
        sb.append(conditionalOneVersusAll(category).toString() + "\n");
        sb.append("Average Conditional Probability Histogram=\n");
        appendCategoryLine(sb);
        for (int j = 0; j < numCategories(); ++j) {
            if (j > 0) sb.append(',');
            sb.append(averageConditionalProbability(category,
                                                    categories()[j]));
        }
        sb.append("\n");
    }

}