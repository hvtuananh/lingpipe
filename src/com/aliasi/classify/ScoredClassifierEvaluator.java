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
 * A {@code ScoredClassifierEvaluator} provides an evaluation harness for
 * score-based classifiers.  It extends the ranked classifier evaluator with
 * score-specific evaluation metrics.
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
public class ScoredClassifierEvaluator<E> extends RankedClassifierEvaluator<E> {

    private final List<ScoreOutcome>[] mScoreOutcomeLists;
    boolean mDefectiveScoring = false;


    /**
     * Construct a scored classifier evaluator with the specified
     * classifier, categories and flag indicating whether or not to
     * store inputs.
     *
     * @param classifier Classifier to evaluate.
     * @param categories Complete list of categories.
     * @param storeInputs Set to {@code true} to store input objects.
     */
    public ScoredClassifierEvaluator(ScoredClassifier<E> classifier,
                                     String[] categories,
                                     boolean storeInputs) {
        super(classifier,categories,storeInputs);
        @SuppressWarnings({"unchecked","rawtypes"})
        List<ScoreOutcome>[] scoreOutcomeLists = new ArrayList[numCategories()];
        mScoreOutcomeLists = scoreOutcomeLists;
        for (int i = 0; i < mScoreOutcomeLists.length; ++i)
            mScoreOutcomeLists[i] = new ArrayList<ScoreOutcome>();
    }

    /**
     * Set the classifier being evaluated to the specified value.  This
     * method is useful to evaluate multiple classifiers with the same
     * evaluator, for instance for use in cross-validation.
     *
     * @param classifier New classifier for this evaluation.
     * @throws IllegalArgumentException If called from an evaluator with
     * a runtime type other than {@code ScoredClassifierEvaluator}.
     */
    public void setClassifier(ScoredClassifier<E> classifier) {
        setClassifier(classifier,ScoredClassifierEvaluator.class);
    }

    /**
     * Return the classifier being evaluated.
     *
     * @return The classifier for this evaluator.
     */
    @Override
    public ScoredClassifier<E> classifier() {
        @SuppressWarnings("unchecked")
        ScoredClassifier<E> result
            = (ScoredClassifier<E>) super.classifier();
        return result;
    }

    @Override
    public void handle(Classified<E> classified) {
        // CUT AND PASTE FROM SUPERCLASS, and INTO SUBCLASS
        E input = classified.getObject();
        Classification refClassification = classified.getClassification();
        String refCategory = refClassification.bestCategory();
        validateCategory(refCategory);
        ScoredClassification classification = classifier().classify(input);
        addClassification(refCategory,classification,input);
        addRanking(refCategory,classification);

        // new stuff
        addScoring(refCategory,classification);
    }

    /**
     * Returns a scored precision-recall evaluation of the
     * classification of the specified reference category versus all
     * other categories using the classification scores.
     *
     * @param refCategory Reference category.
     * @return The scored one-versus-all precision-recall evaluatuion.
     * @throws IllegalArgumentException If the specified category
     * is unknown.
     */
    public ScoredPrecisionRecallEvaluation
        scoredOneVersusAll(String refCategory) {

        validateCategory(refCategory);
        return scoredOneVersusAll(mScoreOutcomeLists,
                                  categoryToIndex(refCategory));
    }

    /**
     * Returns the average score of the specified response category
     * for test cases with the specified reference category.  If there
     * are no cases matching the reference category, the result is
     * <code>Double.NaN</code>.
     *
     * <P>Better classifiers return high values when the reference
     * and response categories are the same and lower values
     * when they are different.  Depending on the classifier, the
     * scores may or may not be meaningful as an average.
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average score of response category in test cases for
     * specified reference category.
     * @throws IllegalArgumentException If the either category is unknown.
     */
    public double averageScore(String refCategory,
                               String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                ScoredClassification c
                    = (ScoredClassification) mClassifications.get(i);
                for (int rank = 0; rank < c.size(); ++rank) {
                    if (c.category(rank).equals(responseCategory)) {
                        sum += c.score(rank);
                        ++count;
                        break;
                    }
                }
            }
        }
        return sum / (double) count;
    }


    /**
     * Returns the average over all test cases of the score of the
     * response that matches the reference category.  Better
     * classifiers return higher values for this average.
     *
     * <P>Whether average scores make sense across training instances
     * depends on the classifier.
     *
     * @return The average score of the reference category in the
     * response.
     */
    public double averageScoreReference() {
        double sum = 0.0;
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            String refCategory = mReferenceCategories.get(i).toString();
            ScoredClassification c
                = (ScoredClassification) mClassifications.get(i);
            for (int rank = 0; rank < c.size(); ++rank) {
                if (c.category(rank).equals(refCategory)) {
                    sum += c.score(rank);
                    break;
                }
            }
        }
        return sum / (double) mReferenceCategories.size();
    }

    ScoredPrecisionRecallEvaluation scoredOneVersusAll(List<ScoreOutcome>[] outcomeLists, int categoryIndex) {
        ScoredPrecisionRecallEvaluation eval
            = new ScoredPrecisionRecallEvaluation();
        for (ScoreOutcome outcome : outcomeLists[categoryIndex])
            eval.addCase(outcome.mOutcome,outcome.mScore);
        return eval;
    }

    void addScoring(String refCategory, ScoredClassification scoring) {
        // will this rank < scoring.size() mess up eval?
        if (scoring.size() < numCategories())
            mDefectiveScoring = true;
        for (int rank = 0; rank < numCategories() && rank < scoring.size(); ++rank) {
            double score = scoring.score(rank);
            String category = scoring.category(rank);
            int categoryIndex = categoryToIndex(category);
            boolean match = category.equals(refCategory);
            ScoreOutcome outcome = new ScoreOutcome(score,match,rank==0);
            mScoreOutcomeLists[categoryIndex].add(outcome);
        }
    }

    @Override
    void baseToString(StringBuilder sb) {
        super.baseToString(sb);
        sb.append("Average Score Reference="
                  + averageScoreReference() + "\n");
    }

    @Override
    void oneVsAllToString(StringBuilder sb, String category, int i) {
        super.oneVsAllToString(sb,category,i);
        sb.append("Scored One Versus All\n");
        sb.append(scoredOneVersusAll(category) + "\n");
        sb.append("Average Score Histogram=\n");
        appendCategoryLine(sb);
        for (int j = 0; j < numCategories(); ++j) {
            if (j > 0) sb.append(',');
            sb.append(averageScore(category,categories()[j]));
        }
        sb.append("\n");
    }
    
    static class ScoreOutcome implements Scored {
        private final double mScore;
        private final boolean mOutcome;
        private final boolean mFirstBest;
        public ScoreOutcome(double score, boolean outcome, boolean firstBest) {
            mOutcome = outcome;
            mScore = score;
            mFirstBest = firstBest;
        }
        public double score() {
            return mScore;
        }
        @Override
        public String toString() {
            return "(" + mScore + ": " + mOutcome + "firstBest=" + mFirstBest + ")";
        }
    }


}