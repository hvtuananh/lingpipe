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

/**
 * A {@code RankedClassifierEvaluator} provides an evaluation harness for
 * ranked classifiers.  It extends the base classifier evaluator with
 * ranking-specific evaluation metrics.
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
public class RankedClassifierEvaluator<E> extends BaseClassifierEvaluator<E> {

    boolean mDefectiveRanking = false;
    private final int[][] mRankCounts;

    /**
     * Construct a ranked classifier evaluator with the specified
     * classifier, categories and flag indicating whether or not to store
     * inputs.
     *
     * @param classifier Classifier to evaluate.
     * @param categories Complete list of categories.
     * @param storeInputs Set to {@code true} to store input objects.
     */
    public RankedClassifierEvaluator(RankedClassifier<E> classifier,
                                     String[] categories,
                                     boolean storeInputs) {
        super(classifier,categories,storeInputs);
        int len = categories.length;
        mRankCounts = new int[len][len];
    }

    /**
     * Set the classifier being evaluated to the specified value.  This
     * method is useful to evaluate multiple classifiers with the same
     * evaluator, for instance for use in cross-validation.
     *
     * @param classifier New classifier for this evaluation.
     * @throws IllegalArgumentException If called from an evaluator with
     * a runtime type other than {@code RankedClassifierEvaluator}.
     */
    public void setClassifier(RankedClassifier<E> classifier) {
        if (!this.getClass().equals(RankedClassifierEvaluator.class)) {
            String msg = "Require appropriate classifier type."
                + " Evaluator class=" + this.getClass()
                + " Found classifier.class=" + classifier.getClass();
            throw new IllegalArgumentException(msg);
        }
        mClassifier = classifier;
    }

    /**
     * Return the classifier being evaluated.
     *
     * @return The classifier for this evaluator.
     */
    @Override
    public RankedClassifier<E> classifier() {
        @SuppressWarnings("unchecked")
        RankedClassifier<E> result
            = (RankedClassifier<E>) super.classifier();
        return result;
    }

    @Override
    public void handle(Classified<E> classified) {
        // CUT AND PASTE FROM SUPERCLASS, and INTO SUBCLASS
        E input = classified.getObject();
        Classification refClassification = classified.getClassification();
        String refCategory = refClassification.bestCategory();
        validateCategory(refCategory);
        RankedClassification classification = classifier().classify(input);
        addClassification(refCategory,classification,input);

        // new stuff
        addRanking(refCategory,classification);
    }

    void addRanking(String refCategory, RankedClassification ranking) {
        int refCategoryIndex = categoryToIndex(refCategory);
        if (ranking.size() < numCategories())
            mDefectiveRanking = true;
        for (int rank = 0; rank < numCategories() && rank < ranking.size(); ++rank) {
            String category = ranking.category(rank);
            if (category.equals(refCategory)) {
                ++ mRankCounts[refCategoryIndex][rank];
                return;
            }
        }
        // assume the reference has last rank
        ++mRankCounts[refCategoryIndex][mCategories.length-1];
    }
    

    /**
     * Returns the number of times that the reference category's
     * rank was the specified rank.
     *
     * <P>For example, in the set of training samples and results
     * described in the method documentation for {@link
     * #averageRank(String,String)}, sample rank counts are as
     * follows:
     *
     * <blockquote><code>
     * rankCount(&quot;a&quot;,0) = 3
     * <br>rankCount(&quot;a&quot;,1) = 1
     * <br>rankCount(&quot;a&quot;,2) = 0
     * <br> &nbsp;
     * <br>rankCount(&quot;b&quot;,0) = 1
     * <br>rankCount(&quot;b&quot;,1) = 0
     * <br>rankCount(&quot;b&quot;,2) = 1
     * <br> &nbsp;
     * <br>rankCount(&quot;c&quot;,0) = 1
     * <br>rankCount(&quot;c&quot;,1) = 0
     * <br>rankCount(&quot;c&quot;,2) = 0
     * </code></blockquote>
     *
     * These results are typically presented as a bar graph histogram
     * per category.
     *
     * @param referenceCategory Reference category.
     * @param rank Rank of count.
     * @return Number of times the reference category's ranking was
     * the specified rank.
     * @throws IllegalArgumentException If the category is unknown.
     */
    public int rankCount(String referenceCategory, int rank) {
        validateCategory(referenceCategory);
        int i = categoryToIndex(referenceCategory);
        return mRankCounts[i][rank];
    }
    
    /**
     * Returns the average over all test samples of the rank of
     * the the response that matches the reference category.
     *
     * <P>Using the example classifications shown in the method
     * documentation of {@link #averageRank(String,String)}:
     *
     * <blockquote><code>
     * averageRankReference()
     * <br> = (0 + 0 + 0 + 1 + 0 + 2 + 0)/7 ~ 0.43
     * </code></blockquote>
     *
     * @return The average rank of the reference category in
     * all classification results.
     */
    public double averageRankReference() {
        double sum = 0.0;
        int count = 0;
        for (int i = 0; i < numCategories(); ++i) {
            for (int rank = 0; rank < numCategories(); ++rank) {
                int rankCount = mRankCounts[i][rank];
                if (rankCount == 0) continue; // just efficiency
                count += rankCount;
                sum += rank * rankCount;
            }
        }
        return sum / (double) count;
    }


    /**
     * Returns the average over all test samples of the reciprocal of
     * one plus the rank of the reference category in the response.
     * This represents counting from one, so if the first-best answer
     * is correct, the reciprocal rank is 1/1; if the second is
     * correct, 1/2; if the third, 1/3; and so on.  These individual
     * recirpocals are then averaged over cases.
     *
     * <P>Using the example classifications shown in the method
     * documentation of {@link #averageRank(String,String)}:
     *
     * <blockquote><code>
     * averageRankReference()
     * <br> = (1/1 + 1/1 + 1/1 + 1/2 + 1/1 + 1/3 + 1/1)/7 ~ 0.83
     * </code></blockquote>
     *
     * @return The mean reciprocal rank of the reference category in
     * the result ranking.
     */
    public double meanReciprocalRank() {
        double sum = 0.0;
        int numCases = 0;
        for (int i = 0; i < numCategories(); ++i) {
            for (int rank = 0; rank < numCategories(); ++rank) {
                int rankCount = mRankCounts[i][rank];
                if (rankCount == 0) continue;  // just for efficiency
                numCases += rankCount;
                sum += ((double) rankCount) / (1.0 + (double) rank);
            }
        }
        return sum / (double) numCases;
    }

    /**
     * Returns the average rank of the specified response category for
     * test cases with the specified reference category.  If there are
     * no cases matching the reference category, the result is
     * <code>Double.NaN</code>.
     *
     * <P>Better classifiers return lower values when the reference
     * and response categories are the same and higher values
     * when they are different.
     *
     * <P>For example, suppose there are three categories,
     * <code>a</code>, <code>b</code> and <code>c</code>.  Consider
     * the following seven test cases, with the specified ranked
     * results:
     *
     * <blockquote>
     * <table border='1' cellpadding='5'>
     * <tr><td><i>Test Case</i></td>
     *     <td><i>Reference</i></td>
     *     <td><i>Rank 0</i></td>
     *     <td><i>Rank 1</i></td>
     *     <td><i>Rank 2</i></td></tr>
     * <tr><td>0</td><td>a</td><td>a</td><td>b</td><td>c</td></tr>
     * <tr><td>1</td><td>a</td><td>a</td><td>c</td><td>b</td></tr>
     * <tr><td>2</td><td>a</td><td>a</td><td>b</td><td>c</td></tr>
     * <tr><td>3</td><td>a</td><td>b</td><td>a</td><td>c</td></tr>
     * <tr><td>4</td><td>b</td><td>b</td><td>a</td><td>c</td></tr>
     * <tr><td>5</td><td>b</td><td>a</td><td>c</td><td>b</td></tr>
     * <tr><td>6</td><td>c</td><td>c</td><td>b</td><td>a</td></tr>
     * </table>
     * </blockquote>
     *
     * for which:
     *
     * <blockquote><code>
     * averageRank(&quot;a&quot;,&quot;a&quot;) = (0 + 0 + 0 + 1)/4 = 0.25
     * <br>
     * averageRank(&quot;a&quot;,&quot;b&quot;) = (1 + 2 + 1 + 0)/4 = 1.00
     * <br>
     * averageRank(&quot;a&quot;,&quot;c&quot;) = (2 + 1 + 2 + 2)/4 = 1.75
     * <br>&nbsp;<br>
     * averageRank(&quot;b&quot;,&quot;a&quot;) = (1 + 0)/2 = 0.50
     * <br>
     * averageRank(&quot;b&quot;,&quot;b&quot;) = (0 + 2)/2 = 1.0
     * <br>
     * averageRank(&quot;b&quot;,&quot;c&quot;) = (2 + 1)/2 = 1.5
     * <br>&nbsp;<br>
     * averageRank(&quot;c&quot;,&quot;a&quot;) = (2)/1 = 2.0
     * <br>
     * averageRank(&quot;c&quot;,&quot;b&quot;) = (1)/1 = 1.0
     * <br>
     * averageRank(&quot;c&quot;,&quot;c&quot;) = (0)/1 = 0.0
     * </code></blockquote>
     *
     * <p>If every ranked result is complete in assigning every
     * category to a rank, the sum of the average ranks will be one
     * less than the number of cases with the specified reference
     * value.  If categories are missing from ranked results, the
     * sums may possible be larger than one minus the number of test
     * cases.
     *
     * <p>Note that the confusion matrix is computed using only the
     * reference and first column of this matrix of results.
     *
     * @param refCategory Reference category.
     * @param responseCategory Response category.
     * @return Average rank of response category in test cases for
     * specified reference category.
     * @throws IllegalArgumentException If either category is unknown.
     */
    public double averageRank(String refCategory,
                              String responseCategory) {
        validateCategory(refCategory);
        validateCategory(responseCategory);
        double sum = 0.0;
        int count = 0;
        // iterate over all paired classifications and lists
        for (int i = 0; i < mReferenceCategories.size(); ++i) {
            if (mReferenceCategories.get(i).equals(refCategory)) {
                RankedClassification rankedClassification
                    = (RankedClassification) mClassifications.get(i);
                int rank = getRank(rankedClassification,responseCategory);
                sum += rank;
                ++count;
            }
        }
        return sum / (double) count;
    }

    int categoryToIndex(String category) {
        int result = confusionMatrix().getIndex(category);
        if (result < 0) {
            String msg = "Unknown category=" + category;
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    int getRank(RankedClassification classification, String responseCategory) {
        for (int rank = 0; rank < classification.size(); ++rank)
            if (classification.category(rank).equals(responseCategory))
                return rank;
        // default to putting it in last rank
        return mCategories.length-1;
    }

    @Override
    void baseToString(StringBuilder sb) {
        super.baseToString(sb);
        sb.append("Average Reference Rank="
                  + averageRankReference() + "\n");
    }

    @Override
    void oneVsAllToString(StringBuilder sb, String category, int i) {
        super.oneVsAllToString(sb,category,i);
        sb.append("Rank Histogram=\n");
        appendCategoryLine(sb);
        for (int rank = 0; rank < numCategories(); ++rank) {
            if (rank > 0) sb.append(',');
            sb.append(mRankCounts[i][rank]);
        }
        sb.append("\n");
        
        sb.append("Average Rank Histogram=\n");
        appendCategoryLine(sb);
        for (int j = 0; j < numCategories(); ++j) {
            if (j > 0) sb.append(',');
            sb.append(averageRank(category,categories()[j]));
        }
        sb.append("\n");
    }

    void appendCategoryLine(StringBuilder sb) {
        sb.append("  ");
        for (int i = 0; i < numCategories(); ++i) {
            if (i > 0) sb.append(',');
            sb.append(categories()[i]);
        }
        sb.append("\n  ");
    }
}