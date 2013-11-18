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

import com.aliasi.stats.Statistics;

import com.aliasi.util.Math;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * An instance of <code>ConfusionMatrix</code> represents a
 * quantitative comparison between two classifiers over a fixed set of
 * categories on a number of test cases.  For convenience, one
 * classifier is termed the &quot;reference&quot; and the other the
 * &quot;response&quot;.
 *
 * <P>Typically the reference will be determined by a human or other
 * so-called &quot;gold standard&quot;, whereas the response will be
 * the result of an automatic classification.  This is how confusion
 * matrices are created from test cases.  With this confusion matrix
 * implementation, two human classifiers or two automatic
 * classifications may also be compared.  For instance, human
 * classifiers that label corpora for training sets are often
 * evaluated for inter-annotator agreement; the usual form of
 * reporting for this is the kappa statistic, which is available in
 * three varieties from the confusion matrix.  A set of systems may
 * also be compared pairwise, such as those arising from a competitive
 * evaluation.
 *
 * <P>Confusion matrices may be initialized on construction; with no
 * matrix argument, they will be constructed with zero values in all
 * cells.  The values can then be incremented by category name with
 * category name with {@link #increment(String,String)} or by
 * category index with {@link #increment(int,int)}. There is also
 * a {@link #incrementByN(int,int,int)} which allows explicit control
 * over matrix values.
 *
 * <P>Consider the following confusion matrix, which reports on the
 * classification of 27 wines by grape variety.  The reference in
 * this case is the true variety and the response arises from the
 * blind evaluation of a human judge.
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td colspan='5'><b>Many-way Confusion Matrix</b></td></tr>
 * <tr><td colspan='2' rowspan='2'>&nbsp;</td>
 *     <td colspan='3' align='center'><b><i>Response</i></b></td></tr>
 * <tr>
 *     <td><i>Cabernet</i></td>
 *     <td><i>Syrah</i></td>
 *     <td><i>Pinot</i></td></tr>
 * <tr><td rowspan='3'><i><b>Refer-<br>ence</b></i></td><td><i>Cabernet</i></td>
 *     <td bgcolor='#CCCCFF'>9</td><td>3</td><td>0</td></tr>
 * <tr><td><i>Syrah</i></td>
 *     <td>3</td><td bgcolor='#CCCCFF'>5</td><td>1</td></tr>
 * <tr><td><i>Pinot</i></td>
 *     <td>1</td><td>1</td><td bgcolor='#CCCCFF'>4</td></tr>
 * </table>
 * </blockquote>
 *
 * Each row represents the results of classifying objects belonging to
 * the category designated by that row.  For instance, the first row
 * is the result of 12 cabernet classifications.  Reading across, 9 of
 * those cabernets were correctly classified as cabernets, 3 were
 * misclassified as syrahs, and none were misclassified as pinot noir.
 * In the next row are the results for 9 syrahs, 3 of which were
 * misclassified as cabernets and 1 of which was misclassified as a
 * pinot.  Similarly, the six pinots being classified are represented
 * on the third row.  In total, the classifier categorized 13 wines as
 * cabernets, 9 wines as syrahs, and 5 wines as pinots.  The sum of
 * all numbers in the graph is equal to the number of trials, in this
 * case 27.  Further note that the correct answers are the ones on the
 * diagonal of the matrix.  The individual entries are recoverable
 * using the method {@link #count(int,int)}.  The positive and
 * negative counts per category may be recovered from the result of
 * {@link #oneVsAll(int)}.
 *
 * <P>Collective results are either averaged per category (macro
 * average) or averaged per test case (micro average).  The results
 * reported here are for a single operating point of results.  Very
 * often in the research literature, results are returned for the best
 * possible post-hoc system settings, established either globally or
 * per category.
 *
 * <P>The multiple outcome classification can be decomposed into a
 * number of one-versus-all classification problems.  For each
 * category, a classifier that categorizes objects as either belonging
 * to that category or not.  From an <i>n</i>-way classifier, a
 * one-versus-all classifier can be constructed automatically by
 * treating an object to be classified as belonging to the category if
 * the category is the result of classifying it.  For the above
 * three-way confusion matrix, the following three one-versus-all
 * matrices are returned as instances of {@link
 * PrecisionRecallEvaluation} through the method {@link
 * #oneVsAll(int)}:
 *
 * <blockquote>
 * <table border='0' cellpadding='5'>
 * <tr>

 * <td>
 * <table border='1' cellpadding='3'>
 * <tr><td colspan='4'><b>Cab-vs-All</b></td></tr>
 * <tr><td colspan='2' rowspan='2' bordercolor='white'>&nbsp;</td>
 *     <td colspan='3' align='center'><b><i>Response</i></b></td></tr>
 * <tr>
 *     <td><i>Cab</i></td>
 *     <td><i>Other</i></td></tr>
 * <tr><td rowspan='3'><i><b>Refer<br>-ence</b></i></td><td><i>Cab</i></td>
 *     <td bgcolor='#CCCCFF'>9</td><td>3</td></tr>
 * <tr><td><i>Other</i></td>
 *     <td>4</td><td bgcolor='#CCCCFF'>11</td></tr>
 * </table>
 * </td>
 *
 * <td>
 * <table border='1' cellpadding='3'>
 * <tr><td colspan='4'><b>Syrah-vs-All</b></td></tr>
 * <tr><td colspan='2' rowspan='2' bordercolor='white'>&nbsp;</td>
 *     <td colspan='3' align='center'><b><i>Response</i></b></td></tr>
 * <tr>
 *     <td><i>Syrah</i></td>
 *     <td><i>Other</i></td></tr>
 * <tr><td rowspan='3'><i><b>Refer<br>-ence</b></i></td><td><i>Syrah</i></td>
 *     <td bgcolor='#CCCCFF'>5</td><td>4</td></tr>
 * <tr><td><i>Other</i></td>
 *     <td>4</td><td bgcolor='#CCCCFF'>14</td></tr>
 * </table>
 * </td>
 *
 * <td>
 * <table border='1' cellpadding='3'>
 * <tr><td colspan='4'><b>Pinot-vs-All</b></td></tr>
 * <tr><td colspan='2' rowspan='2' bordercolor='white'>&nbsp;</td>
 *     <td colspan='3' align='center'><b><i>Response</i></b></td></tr>
 * <tr>
 *     <td><i>Pinot</i></td>
 *     <td><i>Other</i></td></tr>
 * <tr><td rowspan='3'><i><b>Refer<br>-ence</b></i></td><td><i>Pinot</i></td>
 *     <td bgcolor='#CCCCFF'>4</td><td>2</td></tr>
 * <tr><td><i>Other</i></td>
 *     <td>1</td><td bgcolor='#CCCCFF'>20</td></tr>
 * </table>
 * </td>
 *
 * </tr>
 * </table>
 *
 * </blockquote>
 *
 * Note that each has the same true-positive number as in the
 * corresponding cell of the original confusion matrix.  Further note
 * that the sum of the cells in each derived matrix is the same as in
 * the original matrix.  Finally note that if the original
 * classification problem was two dimensional, the derived matrix will
 * be the same as the original matrix.  The results of the various
 * precision-recall evaluation methods for these matrices are shown
 * in the class documentation for {@link PrecisionRecallEvaluation}.
 *
 * <P>Macro-averaged results are just the average of the per-category
 * results.  These include precision, recall and f-measure.  Yule's Q
 * and Y statistics along with the per-category chi squared results
 * are also computed based on the one-versus all matrices.
 *
 * <P>Micro-averaged results are reported based on another derived
 * matrix: the sum of the scores in the one-versus-all matrices.  For
 * the above case, the result given as a {@link PrecisionRecallEvaluation}
 * by the method {@link #microAverage()} is:
 *
 * <blockquote>
 * <table border='1' cellpadding='3'>
 * <tr><td colspan='4'><b>Sum of One-vs-All Matrices</b></td></tr>
 * <tr><td colspan='2' rowspan='2' bordercolor='white'>&nbsp;</td>
 *     <td colspan='3' align='center'><b><i>Response</i></b></td></tr>
 * <tr>
 *     <td><i>True</i></td>
 *     <td><i>False</i></td></tr>
 * <tr><td rowspan='3'><i><b>Refer<br>-ence</b></i></td><td><i>True</i></td>
 *     <td bgcolor='#CCCCFF'>18</td><td>9</td></tr>
 * <tr><td><i>False</i></td>
 *     <td>9</td><td bgcolor='#CCCCFF'>45</td></tr>
 * </table>
 * </blockquote>
 *
 * Note that the true positive cell will be the sum of the
 * true-positive cells of the original matrix (9+5+4=18 in the running
 * example).  A little algebra shows that the false positive cell will
 * be equal to the sum of the off-diagonal elements in the original
 * confusion matrix (3+3+1+1+1=9); symmetry then shows that the false
 * negative value will be the same.  Finally, the true negative cell
 * will bring the total up to the number of categories times the sum
 * of the entries in the original matrix (here 27*3-18-9-9=45); it is
 * also equal to two times the number of true positives plus the
 * number of false negatives (here 2*18+9=45).  Thus for
 * one-versus-all confusion matrices derived from many-way confusion
 * matrices, the micro-averaged precision, recall and f-measure will
 * all be the same.
 *
 * <P>For the above confusion matrix and derived matrices, the
 * no-argument and category-indexed methods will return the values in
 * the following tables. The hot-linked method documentation defines
 * each statistic in detail.
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Method</i></td><td><i>Method()</i></td></tr>
 * <tr><td>{@link #categories()}</code></td>
 *     <td><code>{ &quot;Cabernet&quot;,
 *               &quot;Syrah&quot;,
 *               &quot;Pinot&quot; }</code></td></tr>
 * <tr><td>{@link #totalCount()}</td>
 *     <td>27</td></tr>
 * <tr><td>{@link #totalCorrect()}</td>
 *     <td>18</td></tr>
 * <tr><td>{@link #totalAccuracy()}</td>
 *     <td>0.6667</td></tr>
 * <tr><td>{@link #confidence95()}</td>
 *     <td>0.1778</td></tr>
 * <tr><td>{@link #confidence99()}</td>
 *     <td>0.2341</td></tr>
 * <tr><td>{@link #macroAvgPrecision()}</td>
 *     <td>0.6826</td></tr>
 * <tr><td>{@link #macroAvgRecall()}</td>
 *     <td>0.6574</td></tr>
 * <tr><td>{@link #macroAvgFMeasure()}</td>
 *     <td>0.6676</td></tr>
 * <tr><td>{@link #randomAccuracy()}</td>
 *     <td>0.3663</td></tr>
 * <tr><td>{@link #randomAccuracyUnbiased()}</td>
 *     <td>0.3663</td></tr>
 * <tr><td>{@link #kappa()}</td>
 *     <td>0.4740</td></tr>
 * <tr><td>{@link #kappaUnbiased()}</td>
 *     <td>0.4735</td></tr>
 * <tr><td>{@link #kappaNoPrevalence()}</td>
 *     <td>0.3333</td></tr>
 * <tr><td>{@link #referenceEntropy()}</td>
 *     <td>1.5305</td></tr>
 * <tr><td>{@link #responseEntropy()}</td>
 *     <td>1.4865</td></tr>
 * <tr><td>{@link #crossEntropy()}</td>
 *     <td>1.5376</td></tr>
 * <tr><td>{@link #jointEntropy()}</td>
 *     <td>2.6197</td></tr>
 * <tr><td>{@link #conditionalEntropy()}</td>
 *     <td>1.0892</td></tr>
 * <tr><td>{@link #mutualInformation()}</td>
 *     <td>0.3973</td></tr>
 * <tr><td>{@link #klDivergence()}</td>
 *     <td>0.007129</td></tr>
 * <tr><td>{@link #chiSquaredDegreesOfFreedom()}</td>
 *     <td>4</td></tr>
 * <tr><td>{@link #chiSquared()}</td>
 *     <td>15.5256</td></tr>
 * <tr><td>{@link #phiSquared()}</td>
 *     <td>0.5750</td></tr>
 * <tr><td>{@link #cramersV()}</td>
 *     <td>0.5362</td></tr>
 * <tr><td>{@link #lambdaA()}</td>
 *     <td>0.4000</td></tr>
 * <tr><td>{@link #lambdaB()}</td>
 *     <td>0.3571</td></tr>
 * </table>
 * </blockquote>
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Method</i></td>
 *     <td><i>0 (Cabernet)</i></td>
 *     <td><i>1 (Syrah)</i></td>
 *     <td><i>2 (Pinot)</i></td></tr>
 * <tr><td>{@link #conditionalEntropy(int)}</td>
 *     <td>0.8113</td><td>1.3516</td><td>1.2516</td></tr>
 * </table>
 * </blockquote>
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe2.0
 */
public class ConfusionMatrix {

    private final String[] mCategories;
    private final int[][] mMatrix;
    private final Map<String,Integer> mCategoryToIndex
        = new HashMap<String,Integer>();

    /**
     * Construct a confusion matrix with all zero values from the
     * specified array of categories.  
     *
     * <p>The categories are copied so that subsequent changes to
     * the array passed in will not affect the confusion matrix.
     *
     * @param categories Array of categories for classification.
     */
    public ConfusionMatrix(String[] categories) {
        mCategories = categories.clone();
        int len = categories.length;
        mMatrix = new int[len][len];
        for (int i = 0; i < len; ++i)
            for (int j = 0; j < len; ++j)
                mMatrix[i][j] = 0;
        for (int i = 0; i < len; ++i)
            mCategoryToIndex.put(categories[i],Integer.valueOf(i));

    }

    /**
     * Construct a confusion matrix with the specified set of
     * categories and values.  The values are arranged in
     * reference-category dominant ordering.
     *
     * <P>For example, the many-way confusion matrix shown in
     * the class documentation above would be initialized
     * as:
     *
     * <pre>
     * String[] categories = new String[]
     *     { "Cabernet", "Syrah", "Pinot" };
     * int[][] wineTastingScores = new int[][]
     *     { { 9, 3, 0 },
     *       { 3, 5, 1 },
     *       { 1, 1, 4 } };
     * ConfusionMatrix matrix
     *   = new ConfusionMatrix(categories,wineTastingScores);
     * </pre>
     *
     * @param categories Array of categories for classification.
     * @param matrix Matrix of initial values.
     * @throws IllegalArgumentException If the categories and matrix
     * do not agree in dimension or the matrix contains a negative
     * value.
     */
    public ConfusionMatrix(String[] categories, int[][] matrix) {
        mCategories = categories;
        mMatrix = matrix;
        if (categories.length != matrix.length) {
            String msg = "Categories and matrix must be of same length."
                + " Found categories length=" + categories.length
                + " and matrix length=" + matrix.length;
            throw new IllegalArgumentException(msg);
        }
        for (int j = 0; j < matrix.length; ++j) {
            if (categories.length != matrix[j].length) {
                String msg = "Categories and all matrix rows must be of same length."
                    + " Found categories length=" + categories.length
                    + " Found row " + j + " length=" + matrix[j].length;
                throw new IllegalArgumentException(msg);
            }
        }
        int len = matrix.length;
        for (int i = 0; i < len; ++i) {
            for (int j = 0; j < len; ++j) {
                if (matrix[i][j] < 0) {
                    String msg = "Matrix entries must be non-negative."
                        + " matrix[" + i + "][" + j + "]=" + matrix[i][j];
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }

    /**
     * Return a copy of the array of categories for this confusion
     * matrix.  The order of categories here is the same as that in
     * the matrix and consistent with that returned by
     * <code>getIndex()</code>.  For a category <code>c</code> in the
     * set of categories:

     * <blockquote><code>
     * categories()[getIndex(c)].equals(c)
     * </code></blockquote>

     * and for  an index <code>i</code> in range:
     *
     * <blockquote><code>
     * getIndex(categories()[i]) = i
     * </code></blockquote>
     *
     * @return The array of categories for this matrix.
     * @see #getIndex(String)
     */
    public String[] categories() {
        return mCategories.clone();
    }

    /**
     * Returns the number of categories for this confusion matrix.
     * The underlying two-dimensional matrix of counts for this
     * confusion matrix has dimensions equal to the number of
     * categories.  Note that <code>numCategories()</code> is
     * guaranteed to be the same as <code>categories().length</code>
     * and thus may be used to compute iteration bounds.

     * @return The number of categories for this confusion matrix.
     */
    public int numCategories() {
        return categories().length;
    }

    /**
     * Return the index of the specified category in the list of
     * categories, or <code>-1</code> if it is not a category for this
     * confusion matrix.  The index is the index in the array
     * returned by {@link #categories()}.
     *
     * @param category Category whose index is returned.
     * @return The index of the specified category in the list of
     * categories.
     * @see #categories()
     */
    public int getIndex(String category) {
        Integer index = mCategoryToIndex.get(category);
        if (index == null) return -1;
        return index.intValue();
    }

    /**
     * Return a copy of the matrix values.  All values will be
     * non-negative.
     *
     * @return The matrix values.
     */
    public int[][] matrix() {
        return mMatrix.clone();
    }


    /**
     * Add one to the cell in the matrix for the specified reference
     * and response category indices.
     *
     * @param referenceCategoryIndex Index of reference category.
     * @param responseCategoryIndex Index of response category.
     * @throws IllegalArgumentException If either index is out of range.
     */
    public void increment(int referenceCategoryIndex,
                          int responseCategoryIndex) {
        checkIndex("reference",referenceCategoryIndex);
        checkIndex("response",responseCategoryIndex);
        ++mMatrix[referenceCategoryIndex][responseCategoryIndex];
    }

    /**
     * Add n to the cell in the matrix for the specified reference
     * and response category indices.  The value may be negative, but
     * must be large enough so that adding it to the specified cell
     * does not produce a negative number.
     *
     * @param referenceCategoryIndex Index of reference category.
     * @param responseCategoryIndex Index of response category.
     * @param num Number of instances to increment by.
     * @throws IllegalArgumentException If either index is out of
     * range, or if the result of the increment results in a negative
     * value in a cell.
     */
    public void incrementByN(int referenceCategoryIndex,
                             int responseCategoryIndex,
                             int num ) {
        checkIndex("reference",referenceCategoryIndex);
        checkIndex("response",responseCategoryIndex);
        if (mMatrix[referenceCategoryIndex][responseCategoryIndex] + num < 0) {
            String msg = "Cannot decrement to less than 0 value."
                + " referenceCategoryIndex=" + referenceCategoryIndex
                + " responseCategoryIndex=" + responseCategoryIndex
                + " matrix[referenceCategoryIndex][responseCategoryIndex]=" 
                + mMatrix[referenceCategoryIndex][referenceCategoryIndex]
                + " increment=" + num;
            throw new IllegalArgumentException(msg);
        }
        mMatrix[referenceCategoryIndex][responseCategoryIndex] += num;
    }


    /**
     * Add one to the cell in the matrix for the specified reference
     * and response categories.
     *
     * @param referenceCategory Name of reference category.
     * @param responseCategory Name of response category.
     * @throws IllegalArgumentException If either category is
     * not a category for this confusion matrix.
     */
    public void increment(String referenceCategory,
                          String responseCategory) {
        increment(getIndex(referenceCategory),getIndex(responseCategory));
    }

    /**
     * Returns the value of the cell in the matrix for the specified
     * reference and response category indices.
     *
     * @param referenceCategoryIndex Index of reference category.
     * @param responseCategoryIndex Index of response category.
     * @return Value of specified cell in the matrix.
     * @throws IllegalArgumentException If either index is out of range.
     */
    public int count(int referenceCategoryIndex,
                     int responseCategoryIndex) {
        checkIndex("reference",referenceCategoryIndex);
        checkIndex("response",responseCategoryIndex);
        return mMatrix[referenceCategoryIndex][responseCategoryIndex];
    }

    /**
     * Returns the total number of classifications.  This is just
     * the sum of every cell in the matrix:
     *
     * <blockquote><code>
     *  totalCount()
     *  = <big><big><big>&Sigma</big></big></big><sub><sub>i</sub></sub>
     *    <big><big><big>&Sigma</big></big></big><sub><sub>j</sub></sub>
     *     count(i,j)
     * </code></blockquote>
     *
     * @return The sum of the counts of the entries in the matrix.
     */
    public int totalCount() {
        int total = 0;
        int len = numCategories();
        for (int i = 0; i < len; ++i)
            for (int j = 0; j < len; ++j)
                total += mMatrix[i][j];
        return total;
    }

    /**
     * Returns the total number of responses that matched the
     * reference.  This is the sum of counts on the diagonal of the
     * matrix:
     *
     * <blockquote><code>
     *  totalCorrect()
     *  = <big><big><big>&Sigma</big></big></big><sub><sub>i</sub></sub>
     *     count(i,i)
     * </code></blockquote>
     *
     * The value is the same as that of the
     * <code>microAverage().correctResponse()</code>>
     *
     * @return The sum of the correct results.
     */
    public int totalCorrect() {
        int total = 0;
        int len = numCategories();
        for (int i = 0; i < len; ++i)
            total += mMatrix[i][i];
        return total;
    }

    /**
     * Returns the percentage of response that are correct.
     * That is:
     *
     * <blockquote><code>
     *  totalAccuracy() = totalCorrect() / totalCount()
     * </code></blockquote>
     *
     * Note that the classification error is just one minus the
     * accuracy, because each answer is either true or false.
     *
     * @return The percentage of responses that match the reference.
     */
    public double totalAccuracy() {
        return ((double) totalCorrect()) / (double) totalCount();
    }

    /**
     * Returns half the width of the 95% confidence interval for this
     * confusion matrix.  Thus the confidence is 95% that the accuracy
     * is the total accuracy plus or minus the return value of this method.
     *
     * <P>Confidence is determined as described in {@link #confidence(double)}
     * with parameter <code>z=1.96</code>.
     *
     * @return Half of the width of the 95% confidence interval.
     */
    public double confidence95() {
        return confidence(1.96);
    }

    /**
     * Returns half the width of the 99% confidence interval for this
     * confusion matrix.  Thus the confidence is 99% that the accuracy
     * is the total accuracy plus or minus the return value of this method.
     *
     * <P>Confidence is determined as described in {@link #confidence(double)}
     * with parameter <code>z=2.58</code>.
     *
     * @return Half of the width of the 99% confidence interval.
     */
    public double confidence99() {
        return confidence(2.58);
    }

    /**
     * Returns the normal approximation of half of the binomial
     * confidence interval for this confusion matrix for the specified
     * z-score.

     * <P>A z score represents the number of standard deviations from
     * the mean, with the following correspondence of z score and
     * percentage confidence intervals:
     *
     * <blockquote><table border='1' cellpadding='5'>
     * <tr><td><i>Z</i></td> <td><i>Confidence +/- Z</i></td></tr>
     * <tr><td>1.65</td> <td>90%</td></tr>
     * <tr><td>1.96</td> <td>95%</td></tr>
     * <tr><td>2.58</td> <td>99%</td></tr>
     * <tr><td>3.30</td> <td>99.9%</td></tr>
     * </table></blockquote>
     *
     * Thus the z-score for a 95% confidence interval is 1.96 standard
     * deviations.  The confidence interval is just the accuracy plus or minus
     * the z score times the standard deviation.

     * To compute the normal approximation to the deviation of the
     * binomial distribution, assume
     * <code>p=totalAccuracy()</code> and <code>n=totalCount()</code>.
     * Then the confidence interval is defined in terms of the deviation of
     * <code>binomial(p,n)</code>, which is defined by first taking
     * the variance of the Bernoulli (one trial) distribution with
     * success rate <code>p</code>:
     *
     * <blockquote><pre>
     * variance(bernoulli(p)) = p * (1-p)
     * </code></blockquote>
     *
     * and then dividing by the number <code>n</code> of trials in the
     * binomial distribution to get the variance of the binomial
     * distribution:
     *
     * <blockquote><pre>
     * variance(binomial(p,n)) = p * (1-p) / n
     * </code></blockquote>
     *
     * and then taking the square root to get the deviation:
     *
     * <blockquote><pre>
     * dev(binomial(p,n)) = sqrt(p * (1-p) / n)
     * </code></blockquote>
     *
     * For instance, with <code>p=totalAccuracy()=.90</code>, and
     * <code>n=totalCount()=10000</code>:
     *
     * <blockquote><code>
     * dev(binomial(.9,10000)) = sqrt(0.9 * (1.0 - 0.9) / 10000) = 0.003
     * </code></blockquote>
     *
     * Thus to determine the 95% confidence interval, we take
     * <code>z&nbsp;=&nbsp;1.96</code> for a half-interval width of
     * <code>1.96&nbsp;*&nbsp;0.003&nbsp;=&nbsp;0.00588</code>.  The
     * resulting interval is just <code>0.90&nbsp;+/-&nbsp;0.00588</code>
     * or roughly <code>(.894,.906)</code>.
     *
     * @param z The z score, or number of standard deviations.
     * @return Half the width of the confidence interval for the specified
     * number of deviations.
     */
    public double confidence(double z) {
        double p = totalAccuracy();
        double n = totalCount();
        return z * java.lang.Math.sqrt(p * (1-p) / n);
    }


    /**
     * The entropy of the decision problem itself as defined by the
     * counts for the reference.  The entropy of a distribution is the
     * average negative log probability of outcomes.  For the
     * reference distribution, this is:
     *
     * <code></blockquote>
     * referenceEntropy()
     * <br>&nbsp; &nbsp; =
     * - <big><big><big>&Sigma;</big></big></big><sub><sub>i</sub></sub>
     *     referenceLikelihood(i)
     *     * log<sub><sub>2</sub></sub> referenceLikelihood(i)
     * <br><br>
     * referenceLikelihood(i) = oneVsAll(i).referenceLikelihood()
     * </code></blockquote>
     *
     * @return The entropy of the reference distribution.
     */
    public double referenceEntropy() {
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            double prob = oneVsAll(i).referenceLikelihood();
            sum += prob * Math.log2(prob);
        }
        return -sum;
    }

    /**
     * The entropy of the response distribution.  The entropy of a
     * distribution is the average negative log probability of
     * outcomes.  For the response distribution, this is:
     *
     * <blockquote><code>
     * responseEntropy()
     * <br>&nbsp; &nbsp; =
     * - <big><big><big>&Sigma;</big></big></big><sub><sub>i</sub></sub>
     *     responseLikelihood(i)
     *     * log<sub><sub>2</sub></sub> responseLikelihood(i)
     * <br><br>
     * responseLikelihood(i) = oneVsAll(i).responseLikelihood()
     * </code></blockquote>
     *
     * @return The entropy of the response distribution.
     */
    public double responseEntropy() {
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            double prob = oneVsAll(i).responseLikelihood();
            sum += prob * Math.log2(prob);
        }
        return -sum;
    }

    /**
     * The cross-entropy of the response distribution against the
     * reference distribution.  The cross-entropy is defined by the
     * negative log probabilities of the response distribution
     * weighted by the reference distribution:
     *
     * <blockquote><code>
     * crossEntropy()
     * <br>&nbsp; &nbsp; =
     * - <big><big><big>&Sigma;</big></big></big><sub><sub>i</sub></sub>
     *     referenceLikelihood(i)
     *     * log<sub><sub>2</sub></sub> responseLikelihood(i)
     * <br><br>
     * responseLikelihood(i) = oneVsAll(i).responseLikelihood()
     * <br>
     * referenceLikelihood(i) = oneVsAll(i).referenceLikelihood()
     * </code></blockquote>
     *
     * Note that <code>crossEntropy() >= referenceEntropy()</code>.
     * The entropy of a distribution is simply the cross-entropy of
     * the distribution with itself.
     *
     * <P>Low cross-entropy does not entail good classification,
     * though good classification entails low cross-entropy.
     *
     * @return The cross-entropy of the response distribution
     * against the reference distribution.
     */
    public double crossEntropy() {
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            PrecisionRecallEvaluation eval = oneVsAll(i);
            double referenceProb = eval.referenceLikelihood();
            double responseProb = eval.responseLikelihood();
            sum += referenceProb * Math.log2(responseProb);
        }
        return -sum;
    }

    /**
     * Returns the entropy of the joint reference and response
     * distribution as defined by the underlying matrix.  Joint
     * entropy is derfined by:
     *
     * <blockquote><code>
     * jointEntropy()
     * <br>
     * = - <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   <big><big>&Sigma;</big></big><sub><sub>j</sub></sub>
     *      P'(i,j) * log<sub><sub>2</sub></sub> P'(i,j)
     * </code></blockquote>
     *
     * <blockquote><code>
     *    P'(i,j) = count(i,j) / totalCount()
     * </code></blockquote>
     *
     * and where by convention:
     *
     * <blockquote><code>
     *   0 log<sub><sub>2</sub></sub> 0 =<sub><sub>def</sub></sub> 0
     * </code></blockquote>
     *
     * @return Joint entropy of this confusion matrix.
     */
    public double jointEntropy() {
        double totalCount = totalCount();
        double entropySum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            for (int j = 0; j < numCategories(); ++j) {
                double prob = ((double)count(i,j)) / totalCount;
                if (prob <= 0.0) continue;
                entropySum += prob * com.aliasi.util.Math.log2(prob);
            }
        }
        return -entropySum;
    }

    /**
     * Returns the entropy of the distribution of categories
     * in the response given that the reference category was
     * as specified.  The conditional entropy is defined by:
     *
     * <blockquote><code>
     *  conditionalEntropy(i)
     *  <br>
     *  = - <big><big>&Sigma;</big></big><sub><sub>j</sub></sub>
     *    P'(j|i) * log<sub><sub>2</sub></sub> P'(j|i)
     * <br><br>
     * P'(j|i) = count(j,i) / referenceCount(i)
     * </code></blockquote>
     *
     * where
     *
     * <blockquote><code>
     * </code></blockquote>
     *
     * @param refCategoryIndex Index of the reference category.
     * @return Conditional entropy of the category with the specified
     * index.
     */
    public double conditionalEntropy(int refCategoryIndex) {
        double entropySum = 0.0;
        long refCount = oneVsAll(refCategoryIndex).positiveReference();
        for (int j = 0; j < numCategories(); ++j) {
            double conditionalProb = ((double) count(refCategoryIndex,j))
                / refCount;
            if (conditionalProb <= 0.0) continue;
            entropySum += conditionalProb
                * com.aliasi.util.Math.log2(conditionalProb);
        }
        return -entropySum;
    }

    /**
     * Returns the conditional entropy of the response distribution
     * against the reference distribution.  The conditional entropy
     * is defined to be the sum of conditional entropies per category
     * weighted by the reference likelihood of the category.
     *
     * <blockquote><code>
     * conditionalEntropy()
     * <br>
     * = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   referenceLikelihood(i) * conditionalEntropy(i)
     * <br><br>
     * referenceLikelihood(i) = oneVsAll(i).referenceLikelihood()
     * </code></blockquote>
     *
     * <P>Note that this statistic is not symmetric in that if the
     * roles of reference and response are reversed, the answer may be
     * different.
     *
     * @return The conditional entropy of the response distribution
     * against the reference distribution
     */
    public double conditionalEntropy() {
        double entropySum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            double refProbI = oneVsAll(i).referenceLikelihood();
            entropySum += refProbI * conditionalEntropy(i);
        }
        return entropySum;
    }

    /**
     * Returns the value of the kappa statistic with chance agreement
     * determined by the reference distribution.  Kappa is defined
     * in terms of total accuracy and random accuracy:
     *
     * <blockquote><code>
     *   kappa() = (totalAccuracy() - randomAccuracy())
     *             / (1 - randomAccuracy())
     * </code></blockquote>
     *
     * The kappa statistic was introduced in:
     *
     * <blockquote>
     * Cohen, Jacob. 1960. A coefficient of agreement for nominal scales.
     * <i>Educational And Psychological Measurement</i> <b>20</b>:37-46.
     * </blockquote>
     *
     * @return Kappa statistic for this confusion matrix.
     */
    public double kappa() {
        return kappa(randomAccuracy());
    }

    /**
     * Returns the value of the kappa statistic adjusted for bias.
     * The unbiased kappa value is defined in terms of total accuracy
     * and a slightly different computation of expected likelihood that
     * averages the reference and response probabilities.  The exact
     * definition is:
     *
     * <blockquote>
     *  kappaUnbiased() = (totalAccuracy() - randomAccuracyUnbiased())
     *                    / (1 - randomAccuracyUnbiased())
     * </blockquote>
     *
     * The unbiased version of Kappa was introduced in:
     *
     * <blockquote>
     *  Siegel, Sidney and N. John Castellan, Jr. 1988.
     *  <i>Nonparametric Statistics for the Behavioral Sciences</i>.
     *  McGraw Hill.
     * </blockquote>
     *
     * @return The unbiased version of the kappa statistic.
     */
    public double kappaUnbiased() {
        return kappa(randomAccuracyUnbiased());
    }

    /**
     * Returns the value of the kappa statistic adjusted for
     * prevalence.  The definition is:
     *
     * <blockquote><code>
     *   kappaNoPrevalence() = 2 * totalAccuracy() - 1
     * </code></blockquote>
     *
     * The no prevalence version of kappa was introduced in:
     *
     * <blockquote>
     * Byrt, Ted, Janet Bishop and John B. Carlin. 1993.
     * Bias, prevalence, and kappa.
     * <i>Journal of Clinical Epidemiology</i> <b>46</b>(5):423-429.
     * </blockquote>
     *
     * These authors suggest reporting the three kappa statistics
     * defined in this class: kappa, kappa adjusted for prevalence,
     * and kappa adjusted for bias.
     *
     * @return The value of kappa adjusted for prevalence.
     */
    public double kappaNoPrevalence() {
        return 2.0 * totalAccuracy() - 1.0;
    }

    private double kappa(double PE) {
        double PA = totalAccuracy();
        return (PA - PE) / (1.0 - PE);
    }


    /**
     * The expected accuracy from a strategy of randomly guessing
     * categories according to reference and response distributions.
     * This is defined by:
     *
     * <blockquote><code>
     *  randomAccuracy()
     *   = <big><big><big>&Sigma;</big></big></big><sub><sub>i</sub></sub>
     *     referenceLikelihood(i) * resultLikelihood(i)
     * <br><br>
     * referenceLikelihood(i) = oneVsAll(i).referenceLikelihood()
     * <br>
     * responseLikelihood(i) = oneVsAll(i).responseLikelihood()
     * </code></blockquote>
     *
     * @return The random accuracy for this matrix.
     */
    public double randomAccuracy() {
        double randomAccuracy = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            PrecisionRecallEvaluation eval = oneVsAll(i);
            randomAccuracy
                += eval.referenceLikelihood() * eval.responseLikelihood();
        }
        return randomAccuracy;
    }

    /**
     * The expected accuracy from a strategy of randomly guessing
     * categories according to the average of the reference and
     * response distributions.  This is defined by:
     *
     * <blockquote><code>
     *  randomAccuracyUnbaised()
     *   = <big><big><big>&Sigma;</big></big></big><sub><sub>i</sub></sub>
     *     ((referenceLikelihood(i) + resultLikelihood(i))/2)<sup>2</sup>
     * <br><br>
     * referenceLikelihood(i) = oneVsAll(i).referenceLikelihood()
     * <br>
     * responseLikelihood(i) = oneVsAll(i).responseLikelihood()
     * </code></blockquote>
     *
     * @return The unbiased random accuracy for this matrix.
     */
    public double randomAccuracyUnbiased() {
        double randomAccuracy = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            PrecisionRecallEvaluation eval = oneVsAll(i);
            double avgLikelihood
                = (eval.referenceLikelihood() + eval.responseLikelihood())
                / 2.0;
            randomAccuracy += avgLikelihood * avgLikelihood;
        }
        return randomAccuracy;
    }

    /**
     * Return the number of degrees of freedom of this confusion
     * matrix for the &chi;<sup>2</sup> statistic.  In general, for an
     * <code>n&times;m</code> matrix, the number of degrees of
     * freedom is equal to <code>(n-1)*(m-1)</code>.  Because this
     * is a symmetric matrix of dimensions equal to the number of
     * categories, the result is defined to be:
     *
     * <blockquote><code>
     *   chiSquaredDegreesOfFreedom()
     *   = (numCategories() - 1)<sup>2</sup>
     * </code></blockquote>
     *
     * @return The number of degrees of freedom for this confusion
     * matrix.
     */
    public int chiSquaredDegreesOfFreedom() {
        int sqrt = numCategories() - 1;
        return sqrt * sqrt;
    }

    /**
     * Returns Pearson's C<sub><sub>2</sub></sub> independence test
     * statistic for this matrix.  The value is asymptotically
     * &chi;<sup>2</sup> distributed with a number of degrees of
     * freedom as specified by {@link #chiSquaredDegreesOfFreedom()}.
     *
     * <P>See {@link Statistics#chiSquaredIndependence(double[][])}
     * for definitions of the statistic over matrices.
     *
     * @return The &chi;<sup>2</sup> statistic for this matrix.
     */
    public double chiSquared() {
        int numCategories = numCategories();
        double[][] contingencyMatrix
            = new double[numCategories][numCategories];
        for (int i = 0; i < numCategories; ++i)
            for (int j = 0; j < numCategories; ++j)
                contingencyMatrix[i][j] = count(i,j);
        return Statistics.chiSquaredIndependence(contingencyMatrix);
    }


    /**
     * Returns the value of Pearson's &phi;<sup>2</sup> index of mean
     * square contingency for this matrix.  The value of
     * &phi;<sup>2</sup> may be defined in terms of &chi;<sup>2</sup>
     * by:
     *
     * <blockquote><code>
     * phiSquared() = chiSquared() / totalCount()
     * </code></blockquote>
     *
     * <P>As with our other statistics, this is the <i>sample</i>
     * value; the true contingency by the true random variables
     * defining the reference and response.
     *
     * @return The &phi;<sup>2</sup> statistic for this matrix.
     */
    public double phiSquared() {
        return chiSquared() / (double) totalCount();
    }

    /**
     * Returns the value of Cram&#233;r's V statistic for this matrix.
     * The square of Cram&#233;r's statistic may be defined in terms
     * of the &phi;<sup>2</sup> statistic by:
     *
     * <blockquote><code>
     *  cramersV() = (phiSquared() / (numCategories()-1))<sup><sup>(1/2)</sup></sup>
     * </code></blockquote>
     *
     * @return The value of Cram&#233;r's V statistic for this matrix.
     */
    public double cramersV() {
        double LMinusOne = numCategories() - 1;
        return java.lang.Math.sqrt(phiSquared() / LMinusOne);
    }


    /**
     * Returns the one-versus-all precision-recall evaluation for the
     * specified category index.  See the class definition above for
     * examples.
     *
     * @param categoryIndex Index of category.
     * @return The precision-recall evaluation for the category.
     */
    public PrecisionRecallEvaluation oneVsAll(int categoryIndex) {
        PrecisionRecallEvaluation eval = new PrecisionRecallEvaluation();
        for (int i = 0; i < numCategories(); ++i)
            for (int j = 0; j < numCategories(); ++j)
                eval.addCase(i==categoryIndex,j==categoryIndex,mMatrix[i][j]);
        return eval;
    }

    /**
     * Returns the micro-averaged precision-recall evaluation.  This
     * is just the sum of the precision-recall evaluatiosn provided
     * by {@link #oneVsAll(int)} over all category indices.  See the
     * class definition above for an example.
     *
     * @return The micro-averaged precision-recall evaluation.
     */
    public PrecisionRecallEvaluation microAverage() {
        long tp = 0;
        long fp = 0;
        long fn = 0;
        long tn = 0;
        for (int i = 0; i < numCategories(); ++i) {
            PrecisionRecallEvaluation eval = oneVsAll(i);
            tp += eval.truePositive();
            fp += eval.falsePositive();
            tn += eval.trueNegative();
            fn += eval.falseNegative();
        }
        return new PrecisionRecallEvaluation(tp,fn,fp,tn);
    }

    /**
     * Returns the average precision per category.  This
     * averaging treats each category of being equal in
     * weight.  Macro-averaged precision is defined by:
     *
     * <blockquote><code>
     * macroAvgPrecision()
     * <br>
     * = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   precision(i) / numCategories()
     * <br><br>
     * precision(i) = oneVsAll(i).precision()
     * </code></blockquote>
     *
     * @return The macro-averaged precision.
     */
    public double macroAvgPrecision() {
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i)
            sum += oneVsAll(i).precision();
        return sum / (double) numCategories();
    }

    /**
     * Returns the average precision per category.  This averaging
     * treats each category as being equal in weight. Macro-averaged
     * recall is defined by:
     *
     * <blockquote><code>
     * macroAvgRecall()
     * <br>
     * = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   recall(i) / numCategories()
     * <br><br>
     * recall(i) = oneVsAll(i).recall()
     * </code></blockquote>
     *
     * @return The macro-averaged recall.
     */
    public double macroAvgRecall() {
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i)
            sum += oneVsAll(i).recall();
        return sum / (double) numCategories();
    }

    /**
     * Returns the average F measure per category.  This averaging
     * treats each category as being equal in weight. Macro-averaged
     * F measure is defined by:
     *
     * <blockquote><code>
     * macroAvgFMeasure()
     * <br>
     * = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   fMeasure(i) / numCategories()
     * <br><br>
     * recall(i) = oneVsAll(i).fMeasure()
     * </code></blockquote>
     *
     * <P>Note that this is not necessarily the same value as results
     * from computing the F-measure from the the macro-averaged
     * precision and macro-averaged recall.
     *
     * @return The macro-averaged F measure.
     */
    public double macroAvgFMeasure() {
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i)
            sum += oneVsAll(i).fMeasure();
        return sum / (double) numCategories();
    }

    /**
     * Returns Goodman and Kruskal's &lambda;<sub><sub>A</sub></sub> index
     * of predictive association.  This is defined by:
     *
     * <blockquote><code>
     * lambdaA()
     * <br>
     * = <big><big>(&Sigma;</big></big><sub><sub>j</sub></sub>
     *   maxReferenceCount(j)<big><big>)</big></big> - maxReferenceCount()
     * <br> &nbsp; / (totalCount() - maxReferenceCount())
     * </code></blockquote>
     *
     * where <code>maxReferenceCount(j)</code> is the maximum count
     * in column <code>j</code> of the matrix:
     *
     * <blockquote><code>
     * maxReferenceCount(j) = MAX<sub><sub>i</sub></sub> count(i,j)
     * </code></blockquote>
     *
     * and where <code>maxReferenceCount()</code> is the maximum
     * reference count:
     *
     * <blockquote><code>
     * maxReferenceCount() = MAX<sub><sub>i</sub></sub> referenceCount(i)
     * </code></blockquote>
     *
     * <P>Note that like conditional probability and conditional
     * entropy, the &lambda;<sub><sub>A</sub></sub> statistic is
     * antisymmetric; the measure &lambda;<sub><sub>B</sub></sub>
     * simply reverses the rows and columns.  The probabilistic
     * interpretation of &lambda;<sub><sub>A</sub></sub> is like that
     * of &lambda;<sub><sub>B</sub></sub>, only reversing the role of
     * the reference and response.
     *
     * @return The &lambda;<sub><sub>B</sub></sub> statistic for this
     * matrix.
     */
    public double lambdaA() {
        double maxReferenceCount = 0.0;
        for (int j = 0; j < numCategories(); ++j) {
            double referenceCount = oneVsAll(j).positiveReference();
            if (referenceCount > maxReferenceCount)
                maxReferenceCount = referenceCount;
        }
        double maxCountSum = 0.0;
        for (int j = 0; j < numCategories(); ++j) {
            int maxCount = 0;
            for (int i = 0; i < numCategories(); ++i) {
                int count = count(i,j);
                if (count > maxCount)
                    maxCount = count;
            }
            maxCountSum += maxCount;
        }
        double totalCount = totalCount();
        return (maxCountSum - maxReferenceCount)
            / (totalCount - maxReferenceCount);
    }

    /**
     * Returns Goodman and Kruskal's &lambda;<sub><sub>B</sub></sub> index
     * of predictive association.  This is defined by:
     *
     * <blockquote><code>
     * lambdaB()
     * <br>
     * = <big><big>(&Sigma;</big></big><sub><sub>j</sub></sub>
     *   maxResponseCount(i)<big><big>)</big></big> - maxResponseCount()
     * <br> &nbsp; / (totalCount() - maxResponseCount())
     * </code></blockquote>
     *
     * where <code>maxResponseCount(i)</code> is the maximum count
     * in row <code>i</code> of the matrix:
     *
     * <blockquote><code>
     * maxResponseCount(i) = MAX<sub><sub>j</sub></sub> count(i,j)
     * </code></blockquote>
     *
     * and where <code>maxResponseCount()</code> is the maximum
     * response count:
     *
     * <blockquote><code>
     * maxResponseCount() = MAX<sub><sub>j</sub></sub> responseCount(j)
     * </code></blockquote>
     *
     * <P>The probabilistic interpration of
     * &lambda;<sub><sub>B</sub></sub> is the reduction in error
     * likelihood from knowing the specified reference category in
     * predicting the response category.  It will thus take on a value
     * between 0.0 and 1.0, with higher values being better.  Perfect
     * association yields a value of 1.0 and perfect independence a
     * value of 0.0.
     *
     * <P>Note that the &lambda;<sub><sub>B</sub></sub> statistic is
     * antisymmetric; the measure &lambda;<sub><sub>A</sub></sub>
     * simply reverses the rows and columns.
     *
     * @return The &lambda;<sub><sub>B</sub></sub> statistic for this
     * matrix.
     */
    public double lambdaB() {
        double maxResponseCount = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            double responseCount = oneVsAll(i).positiveResponse();
            if (responseCount > maxResponseCount)
                maxResponseCount = responseCount;
        }
        double maxCountSum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            int maxCount = 0;
            for (int j = 0; j < numCategories(); ++j) {
                int count = count(i,j);
                if (count > maxCount)
                    maxCount = count;
            }
            maxCountSum += maxCount;
        }
        double totalCount = totalCount();
        return (maxCountSum - maxResponseCount)
            / (totalCount - maxResponseCount);
    }

    /**
     * Returns the mutual information between the reference and
     * response distributions.  Mutual information is defined
     * Kullback-Lieblier divergence, between the product of the
     * individual distributions and the joint distribution.  Mutual
     * information is defined as:
     *
     * <blockquote><code>
     * mutualInformation()
     * <br>
     * = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   <big><big>&Sigma;</big></big><sub><sub>j</sub></sub>
     *      P(i,j)
     *      * log<sub><sub>2</sub></sub>
     *             ( P(i,j) / (P<sub><sub>reference</sub></sub>(i)
     *                         * P<sub><sub>response</sub></sub>(j)) )
     * <br><br>
     * P(i,j) = count(i,j) / totalCount()
     * <br>
     * P<sub><sub>reference</sub></sub>(i) = oneVsAll(i).referenceLikelihood()
     * <br>
     * P<sub><sub>response</sub></sub>(i) = oneVsAll(i).responseLikelihood()
     * <br>
     * </code></blockquote>
     *
     * A bit of algebra shows that mutual information is the reduction
     * in entropy of the response distribution from knowing the
     * reference distribution:
     *
     * <blockquote><code>
     *   mutualInformation() = responseEntropy() - conditionalEntropy()
     * </code></blockquote>
     *
     * In this way it is similar to the
     * &lambda;<sub><sub>B</sub></sub> measure.  

     * <p>Mutual information is symmetric.  We could also subtract
     * the conditional entropy of the reference given the response
     * from the reference entropy to get the same result.
     *
     * @return The mutual information between the reference and the
     * response distributions.
     */
    public double mutualInformation() {
        double totalCount = totalCount();
        double sum = 0.0;
        for (int i = 0; i < numCategories(); ++i) {
            double pI = oneVsAll(i).referenceLikelihood();
            if (pI <= 0.0) continue;
            for (int j = 0; j < numCategories(); ++j) {
                double pJ = oneVsAll(j).responseLikelihood();
                if (pJ <= 0.0) continue;
                double pIJ = ((double)count(i,j)) / totalCount;
                if (pIJ <= 0.0) continue;
                sum += pIJ * com.aliasi.util.Math.log2(pIJ/(pI * pJ));
            }
        }
        return sum;
    }

    /**
     * Returns the Kullback-Liebler (KL) divergence between the
     * reference and response distributions.  KL divergence is also
     * known as relative entropy.
     *
     * <blockquote><code>
     * klDivergence()
     * <br>
     * = <big><big>&Sigma;</big></big><sub><sub>k</sub></sub>
     *   P<sub><sub>reference</sub></sub>(k)
     *   * log<sub><sub>2</sub></sub> (P<sub><sub>reference</sub></sub>(k)
     *                                 / P<sub><sub>response</sub></sub>(k))
     * <br><br>
     * P<sub><sub>reference</sub></sub>(i) = oneVsAll(i).referenceLikelihood()
     * <br>
     * P<sub><sub>response</sub></sub>(i) = oneVsAll(i).responseLikelihood()
     * <br>
     * </code></blockquote>
     * </code></blockquote>
     *
     * Note that KL divergence is not symmetric in the reference and response
     * distributions.
     *
     * @return the Kullback-Liebler divergence between the reference and
     * response distributions.
     */
    public double klDivergence() {
        double sum = 0.0;
        for (int k = 0; k < numCategories(); ++k) {
            PrecisionRecallEvaluation eval = oneVsAll(k);
            double refProb = eval.referenceLikelihood();
            double responseProb = eval.responseLikelihood();
            sum += refProb
                * com.aliasi.util.Math.log2(refProb/responseProb);
        }
        return sum;
    }

    /**
     * Return a string-based representation of this confusion matrix.
     *
     * @return A string-based representation of this confusion matrix.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GLOBAL CONFUSION MATRIX STATISTICS\n");
        toStringGlobal(sb);
        for (int i = 0; i < numCategories(); ++i) {
            sb.append("CATEGORY " + i + "=" + categories()[i] + " VS. ALL\n");
            sb.append("  Conditional Entropy=" + conditionalEntropy(i));
            sb.append('\n');
            sb.append(oneVsAll(i).toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    void toStringGlobal(StringBuilder sb) {
        String[] categories = categories();
        sb.append("Categories=" + Arrays.asList(categories));
        sb.append('\n');
        sb.append("Total Count=" + totalCount());
        sb.append('\n');
        sb.append("Total Correct=" + totalCorrect());
        sb.append('\n');
        sb.append("Total Accuracy=" + totalAccuracy());
        sb.append('\n');
        sb.append("95% Confidence Interval=" + totalAccuracy()
                  + " +/- " + confidence95());
        sb.append('\n');
        sb.append("Confusion Matrix\n");
        sb.append("reference \\ response\n");
        sb.append(matrixToCSV());
        sb.append('\n');
        sb.append("Macro-averaged Precision=" + macroAvgPrecision());
        sb.append('\n');
        sb.append("Macro-averaged Recall=" + macroAvgRecall());
        sb.append('\n');
        sb.append("Macro-averaged F=" + macroAvgFMeasure());
        sb.append('\n');
        sb.append("Micro-averaged Results\n");
        sb.append("         the following symmetries are expected:\n");
        sb.append("           TP=TN, FN=FP\n");
        sb.append("           PosRef=PosResp=NegRef=NegResp\n");
        sb.append("           Acc=Prec=Rec=F\n");
        sb.append(microAverage().toString());
        sb.append('\n');
        sb.append("Random Accuracy=" + randomAccuracy());
        sb.append('\n');
        sb.append("Random Accuracy Unbiased=" + randomAccuracyUnbiased());
        sb.append('\n');
        sb.append("kappa=" + kappa());
        sb.append('\n');
        sb.append("kappa Unbiased=" + kappaUnbiased());
        sb.append('\n');
        sb.append("kappa No Prevalence =" + kappaNoPrevalence());
        sb.append('\n');
        sb.append("Reference Entropy=" + referenceEntropy());
        sb.append('\n');
        sb.append("Response Entropy=" + responseEntropy());
        sb.append('\n');
        sb.append("Cross Entropy=" + crossEntropy());
        sb.append('\n');
        sb.append("Joint Entropy=" + jointEntropy());
        sb.append('\n');
        sb.append("Conditional Entropy=" + conditionalEntropy());
        sb.append('\n');
        sb.append("Mutual Information=" + mutualInformation());
        sb.append('\n');
        sb.append("Kullback-Liebler Divergence=" + klDivergence());
        sb.append('\n');
        sb.append("chi Squared=" + chiSquared());
        sb.append('\n');
        sb.append("chi-Squared Degrees of Freedom="
                  + chiSquaredDegreesOfFreedom());
        sb.append('\n');
        sb.append("phi Squared=" + phiSquared());
        sb.append('\n');
        sb.append("Cramer's V=" + cramersV());
        sb.append('\n');
        sb.append("lambda A=" + lambdaA());
        sb.append('\n');
        sb.append("lambda B=" + lambdaB());
        sb.append('\n');
    }

    /**
     * NEEDS PROPER CSV ESCAPES
     */
    String matrixToCSV() {
        StringBuilder sb = new StringBuilder();
        // width = height = numcats+1
        // upper left corner empty
        // ROW 0
        sb.append("  ");
        for (int i = 0; i < numCategories(); ++i) {
            sb.append(',');
            sb.append(categories()[i]);
        }
        // ROW 1 to ROW numCategories()
        for (int i = 0; i < numCategories(); ++i) {
            sb.append("\n  ");
            sb.append(categories()[i]);
            for (int j = 0; j < numCategories(); ++j) {
                sb.append(',');
                sb.append(count(i,j));
            }
        }
        return sb.toString();
    }

    /**
     * NEEDS PROPER HTML ESCAPES
     */
    String matrixToHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<table border='1' cellpadding='5'>");
        sb.append('\n');
        sb.append("<tr>\n  <td colspan='2' rowspan='2'>&nbsp;</td>");
        sb.append("\n  <td colspan='" + numCategories() + "' align='center' bgcolor='darkgray'><b>Response</b></td></tr>");
        sb.append("<tr>");
        for (int i = 0; i < numCategories(); ++i) {
            sb.append("\n  <td align='right' bgcolor='lightgray'><i>" + categories()[i] + "</i></td>");
        }
        sb.append("</tr>\n");
        for (int i = 0; i < numCategories(); ++i) {
            sb.append("<tr>");
            if (i == 0) sb.append("\n  <td rowspan='" + numCategories() + "' bgcolor='darkgray'><b>Ref-<br>erence</b></td>");
            sb.append("\n  <td align='right' bgcolor='lightgray'><i>" + categories()[i] + "</i></td>");
            for (int j = 0; j < numCategories(); ++j) {
                if (i == j) {
                    sb.append("\n  <td align='right' bgcolor='lightgreen'>");
                } else if (count(i,j) == 0) {
                    sb.append("\n  <td align='right' bgcolor='yellow'>");
                } else {
                    sb.append("\n  <td align='right' bgcolor='red'>");
                }
                sb.append(count(i,j));
                sb.append("</td>");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        sb.append("</html>\n");
        return sb.toString();
    }

    private void checkIndex(String argMsg, int index) {
        if (index < 0) {
            String msg = "Index for " + argMsg + " must be > 0."
                + " Found index=" + index;
            throw new IllegalArgumentException(msg);
        }
        if (index >= numCategories()) {
            String msg = "Index for " + argMsg
                + " must be < numCategories()=" + numCategories();
            throw new IllegalArgumentException(msg);
        }
    }


}
