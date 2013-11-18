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

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

/**
 * A <code>ScoredPrecisionRecallEvaluation</code> provides an
 * evaluation based on the precision-recall operating points and
 * sensitivity-specificity operating points.  The unscored
 * precision-recall evaluation class is {@link
 * PrecisionRecallEvaluation}.
 *
 * <h3>Construction and Population</h3>
 *
 * <p>There is a single no-arg constructor {@link
 * #ScoredPrecisionRecallEvaluation()}.  
 *
 * <p>The method {@link #addCase(boolean,double)} is used to populate
 * the evaluation, with the first argument representing whether the
 * response was correct and the second the score that was assigned.
 *
 * <h4>Missing Cases</h4>
 *
 * <p>If there are positive reference cases that are not added through
 * {@code addCase()}, the total number of such cases should be added
 * using the method {@link #addMisses(int)}.  This method effectively
 * increments the number of reference positive cases used to compute
 * recall values.
 *
 * <p>If there are negative reference cases that are not dealt with
 * through {@code addCase()}, the method {@link
 * #addNegativeMisses(int)} should be called with the total number of
 * such cases as an argument.  This method increments the number of 
 * reference engative cases used to compute specificity values.
 *
 * <h3>Example</h3>
 *
 * <P>By way of example, consider the following table of cases, all of
 * which involve positive responses.  The cases are in rank order, but
 * may be added in any order.  
 *
 * <blockquote>
 * <table border="1" cellpadding="5">
 * <tr><td><i>Rank</i></td>
 *     <td><i>Score</i></td>
 *     <td><i>Correct</i></td>
 *     <td><i>TP</i></td>
 *     <td><i>TN</i></td>
 *     <td><i>FP</i></td>
 *     <td><i>FN</i></td>
 *     <td><i>Rec</i></td>
 *     <td><i>Prec</i></td>
 *     <td><i>Spec</i></td>
 *     <td><i>F Meas</i></td></tr>
 * <tr><td>(-1)</td><td>n/a</td><td>n/a</td>
 *     <td>0</td> <td>6</td> <td>0</td> <td>5</td>
 *     <td>0.00</td>
 *     <td bgcolor="yellow">1.00</td>
 *     <td bgcolor="orange">1.00</td>
 *     <td>0.00</td></tr>
 * <tr><td colspan="11"> </td></tr>
 * <tr><td>0</td><td>-1.21</td><td>no</td>
 *     <td>0</td> <td>5</td> <td>1</td> <td>5</td>
 *     <td>0.00</td>
 *     <td>0.00</td>
 *     <td>0.83</td>
 *     <td>0.00</td></tr>
 * <tr bgcolor="#CCCCFF"><td>1</td><td>-1.27</td><td>yes</td>
 *     <td>1</td> <td>5</td> <td>1</td> <td>4</td>
 *     <td>0.20</td>
 *     <td>0.50</td>
 *     <td bgcolor="orange">0.83</td>
 *     <td>0.29</td></tr>
 * <tr><td>2</td><td>-1.39</td><td>no</td>
 *     <td>1</td> <td>4</td> <td>2</td> <td>4</td>
 *     <td>0.20</td>
 *     <td>0.33</td>
 *     <td>0.67</td>
 *     <td>0.25</td></tr>
 * <tr bgcolor="#CCCCFF"><td>3</td><td>-1.47</td><td>yes</td>
 *     <td>2</td> <td>4</td> <td>2</td> <td>3</td>
 *     <td>0.40</td>
 *     <td>0.50</td>
 *     <td>0.67</td>
 *     <td>0.44</td></tr>
 * <tr bgcolor="#CCCCFF"><td>4</td><td>-1.60</td><td>yes</td>
 *     <td>3</td> <td>4</td> <td>2</td> <td>2</td>
 *     <td>0.60</td>
 *     <td bgcolor="yellow">0.60</td>
 *     <td bgcolor="orange">0.67</td>
 *     <td bgcolor="pink">0.60</td></tr>
 * <tr><td>5</td><td>-1.65</td><td>no</td>
 *     <td>3</td> <td>3</td> <td>3</td> <td>2</td>
 *     <td>0.60</td>
 *     <td>0.50</td>
 *     <td>0.50</td>
 *     <td>0.55</td></tr>
 * <tr><td>6</td><td>-1.79</td><td>no</td>
 *     <td>3</td> <td>2</td> <td>4</td> <td>2</td>
 *     <td>0.60</td>
 *     <td>0.43</td>
 *     <td>0.33</td>
 *     <td>0.50</td></tr>
 * <tr><td>7</td><td>-1.80</td><td>no</td>
 *     <td>3</td> <td>1</td> <td>5</td> <td>2</td>
 *     <td>0.60</td>
 *     <td>0.38</td>
 *     <td>0.17</td>
 *     <td>0.47</td></tr>
 * <tr bgcolor="#CCCCFF"><td>8</td><td>-2.01</td><td>yes</td>
 *     <td>4</td> <td>1</td> <td>5</td> <td>1</td>
 *     <td>0.80</td>
 *     <td bgcolor="yellow">0.44</td>
 *     <td bgcolor="orange">0.17</td>
 *     <td>0.53</td></tr>
 * <tr><td>9</td><td>-3.70</td><td>no</td>
 *     <td>4</td> <td>0</td> <td>6</td> <td>1</td>
 *     <td>0.80</td>
 *     <td>0.40</td>
 *     <td>0.00</td>
 *     <td>0.53</td></tr>
 * <tr><td colspan="11"> </td></tr>
 * <tr><td>?</td><td>n/a</td><td>yes</td>
 *     <td>5</td> <td>0</td> <td>6</td> <td>0</td>
 *     <td>1.00</td>
 *     <td bgcolor="yellow">0.00</td>
 *     <td bgcolor="orange">0.00</td>
 *     <td>0.00</td></tr>
 * </table>
 * </blockquote>
 *
 * The first line, which is separated, indicates the values before any
 * results have been returned.  There's no score corresponding to this
 * operating point, and given that it doesn't correspond to a result,
 * correctness is not applicable.  It has zero recall, one
 * specificity, and one precision (letting zero divided by zero be one
 * here).
 *
 * <p>The next lines, listed as ranks 0 to 9, correspond to calls to
 * {@code addCase()} with the specified score and correctness.  For
 * each of these lines, we list the corresponding number of true
 * positives (TP), true negatives (TN), false positives (FP), and
 * false negatives (FN).  These are followed by recall, precision and
 * specificity (aka rejection recall).  See the class documentation
 * for {@link PrecisionRecallEvaluation} for definitions of these
 * values in terms of the TP, TN, FP, and FN counts.
 *
 * <p>There are five positive reference cases (blue backgrounds) and
 * six negative reference cases (clear backgrounds) in this diagram.
 * The yellow precision values and orange specificity values are used
 * for interpolated curves.
 *
 * <h3>Precision-Recall Curves</h3>
 * 
 * <P>The pairs of precision/recall values form the basis for the
 * precision-recall curve returned by {@link #prCurve(boolean)}, with
 * the argument indicating whether to perform precision interpolation.
 * For the above graph, the uninterpolated precision-recall curve is:
 *
 * <blockquote><pre>
 * <b>prCurve</b>(false) = {
 *     { 0.00, 1.00 },
 *     { 0.20, 0.50 },
 *     { 0.20, 0.33 },
 *     { 0.40, 0.50 },
 *     { 0.60, 0.60 },
 *     { 0.60, 0.50 },
 *     { 0.60, 0.43 },
 *     { 0.60, 0.38 },
 *     { 0.60, 0.38 },
 *     { 0.80, 0.44 },
 *     { 0.80, 0.40 },
 *     { 1.00, 0.00 }
 * }</pre></blockquote>
 * 
 * Typically, a form of interpolation is performed that sets the
 * precision for a given recall value to the maximum of the precision
 * at the curent or greater recall value.  This pushes the yellow
 * precision values up the graph.  At the same time, we only return
 * values that correspond to jumps in recall, corresponding to ranks
 * at which true positives were returned.  For the example above,
 * the result is
 *
 * <blockquote><pre>
 * <b>prCurve</b>(true) = {
 *     { 0.00, 1.00 },
 *     { 0.20, 0.60 },
 *     { 0.40, 0.60 },
 *     { 0.60, 0.60 },
 *     { 0.80, 0.44 },
 *     { 1.00, 0.00 }
 * }</pre></blockquote>
 *
 * For convenience, the evaluation always adds the two limit points,
 * one with precision 0 and recall 1, and one with precision 1 and
 * recall 0.  These operating points are always achievable, the first
 * by returning every possible answer, and the second by returning no
 * answers.  
 *
 * <h3>ROC Curves</h3>
 *
 * Another popular graph for visualizing classification results is the
 * receiver operating characteristic (ROC) curve, which plots
 * sensitivity (a.k.a. recall) versus one minus specificity
 * (a.k.a. one minus rejection recall).  Because specificity is
 * accuracy on negative cases and sensitivity accuracy on positive
 * cases, these graphs are fairly easy to interpret.  The
 * precision-recall curve, on the othe hand, does not consider true
 * negative (TN) counts.
 * 
 * <p>The ROC curve is returned by the method {@link
 * #rocCurve(boolean)} with the boolean parameter again indicating
 * whether to perform precision interpolation.  For the above graph,
 * the result is:
 *
 * <blockquote><pre>
 * <b>rocCurve</b>(false) = {
 *     { 1 - 1.00, 0.00 },
 *     { 1 - 0.83, 0.00 },
 *     { 1 - 0.83, 0.20 },
 *     { 1 - 0.67, 0.20 },
 *     { 1 - 0.67, 0.40 },
 *     { 1 - 0.67, 0.60 },
 *     { 1 - 0.50, 0.60 },
 *     { 1 - 0.33, 0.60 },
 *     { 1 - 0.17, 0.60 },
 *     { 1 - 0.17, 0.80 },
 *     { 1 - 0.00, 0.80 },
 *     { 1 - 0.00, 1.00 }
 * }</pre></blockquote>
 *
 * Interpolation works exactly the same way as for the precision-recall
 * curves, but based on specificity rather than precsion.
 *
 * <blockquote><pre>
 * <b>rocCurve</b>(true) = {
 *     { 1 - 1.00, 0.00 },
 *     { 1 - 0.83, 0.20 },
 *     { 1 - 0.67, 0.60 },
 *     { 1 - 0.50, 0.60 },
 *     { 1 - 0.33, 0.60 },
 *     { 1 - 0.17, 0.80 },
 *     { 1 - 0.00, 1.00 }
 * }</pre></blockquote>
 *
 *
 * <h3>Precision at <i>N</i></h3>
 *
 * <p>In some information extraction or retrieval tasks, a system
 * might only return a fixed number of examples to a user.  To
 * evaluate the result of such truncated result sets, it is common to
 * report the precision after <i>N</i> returned results.  The counting
 * starts from one rather than zero for returned results, but we fill in
 * a limiting value of 1.0 for precision at 0.  In our running
 * example, we have
 *
 * <pre>
 *      <b>precisionAt</b>(0) = 1.0
 *      <b>precisionAt</b>(1) = 0.0
 *      <b>precisionAt</b>(5) = 0.6
 *      <b>precisionAt</b>(10) = 0.4
 *      <b>precisionAt</b>(20) = 0.2
 *      <b>precisionAt</b>(100) = 0.04</pre>
 *
 * The return value for a rank greater than the number of cases added
 * will be calculated assuming all other results are errors.
 * 
 * <h3>Reciprocal Rank</h3>
 *
 * For information extraction tasks, one result is often enough to
 * satisfy an information need.  A popular measure to characterize
 * this situation is reciprocal rank.  The reciprocal rank is defined
 * to be <code>1/<i>M</i></code>, where <code><i>M</i></code> is the
 * rank (counting from 1) of the first true positive return.  In our
 * running example, the first result is a false positive and the
 * second a true positive, so reciprocal rank is
 *
 * <pre>
 *      <b>reciprocalRank()</b>() = 0.5</pre>
 *
 * Note that this measure emphasizes differences in early ranks
 * much more than later ones.  For instance, the reciprocal rank
 * for a system returning a correct result first is 1/1, but
 * for one returning it second, it's 1/2, and for one returning
 * the first true positive at rank 10, it's 1/10.  The difference
 * between rank 1 and 2 is greater than that between 2 and 10. 
 *
 * <h3>R Precision</h3>
 *
 * The R precision is defined as the precision for the first R
 * results, where R is the number of reference positive cases.  If
 * there are not enough results, the value returned is calculated by
 * assuming all the non-added results are errors.  
 *
 * <p>For the running example, R precision is
 *
 * <blockquote><pre>
 * <b>rPrecision</b>() = 0.6</pre></blockquote>
 *
 * R precision will always be at a point where precision equals
 * recall.  It is also known as the precision-recall break-even point
 * (BEP), and for convenience, there is a method of that name,
 *
 * <blockquote><pre>
 * <b>prBreakevenPoint</b>() = rPrecision() = 0.6</pre></blockquote>
 *
 * <h3>Maximum F Measure</h3>
 *
 * Another commonly reported statistic that may be calculated from the
 * precisino-recall curve is the maximum F measure (see {@link
 * PrecisionRecallEvaluation#fMeasure(double,double,double)} for a
 * definition of F measure).  The result is the maximum F measure
 * value achieved at any position on the curve.  For our example, this
 * arises at
 *
 * <blockquote><pre>
 * maximumFMeasure() = 0.6</pre></blockquote>
 *
 * <p>In general, the maximum F measure may occur at a point
 * other than the precision-recall break-even point.
 *
 * <h3>Averaged Results</h3>
 *
 * If there is more than one classifier or information extractor
 * being evaluated, it is common to report averages of several
 * of the statistics reported by this class.  LingPipe does not compute
 * these values, but they are easy to calculate by accumulating
 * results for individual ranked precision-recall evaluations.
 *
 * <p>The average across multiple evaluations of average precision is
 * somewhat misleadingly called mean average precision (MAP) [it should
 * be average average precision, because averages are over finite
 * samples and means are properties of distributions].
 *
 * <p>The eleven-point precision-recall curves, reciprocal rank, and R
 * precision are also popular targets for reporting averaged results.
 *
 * <h3>References</h3>
 *
 * For texts on ROC and PR evaluations, see the following.
 *
 * <ul> 
 * 
 * <li>Wikipedia. <a
 * href="http://en.wikipedia.org/wiki/Receiver_operating_characteristic">Receiver
 * Operating Characteristic</a>.
 *
 * <li>Lasko, Thomas A., Jui G. Bhagwat, Kelly H. Zou, and Lucila Ohno-Machado.
 * 2005. The use of receiver operating
 * characteristic curves in biomedical informatics.  <i>Journal of
 * Biomedical Informatics</i> <b>38</b>:404–-415.</li>
 *
 * <li> Manning, Christopher D., Prabhakar Raghavan, and Hinrich
 * Sch&#252;tze. 2008. <i> Introduction to Information Retrieval</i>. Cambridge
 * University Press.  Chapter 8, Evaluation in information retrieval.</li>
 * </ul>
 * 
 * @author Bob Carpenter
 * @author Mike Ross
 * @author Breck Baldwin
 * @version 4.0.1
 * @since   LingPipe2.1
 */
public class ScoredPrecisionRecallEvaluation {

    private final List<Case> mCases = new ArrayList<Case>();
    private int mNegativeRef = 0;
    private int mPositiveRef = 0;

    /**
     * Construct a scored precision-recall evaluation.
     */
    public ScoredPrecisionRecallEvaluation() {
        /* do nothing */
    }

    /**
     * Add a case with the specified correctness and response score.
     * Only positive response cases are considered, and the correct
     * flag is set to <code>true</code> if the reference was also
     * positive.  The score is just the response score.
     *
     * <P><b>Warning:</b> The scores should be sensibly comparable
     * across cases.
     *
     * @param correct <code>true</code> if this case was correct.
     * @param score Score of response.
     */
    public void addCase(boolean correct, double score) {
        mCases.add(new Case(correct,score));
        if (correct) ++mPositiveRef;
        else ++mNegativeRef;
    }

    /**
     * Incrments the positive reference count without adding a
     * case from the classifier. This method is used for
     * precision-recall evaluations where the set of returned items
     * does not enumerate all positive references.  These misses are
     * used in calcuating statistics such as precision-recall curves.
     *
     * @param count Number of outright misses to add to
     * this evaluation.
     * @throws IllegalArgumentException if the count is not positive.
     */
    public void addMisses(int count) {
        if (count < 0) {
            String msg = "Miss count must be non-negative."
                + " Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        mPositiveRef += count;
    }

    /**
     * Incrments the negative reference count without adding a case
     * from the classifier. This method is used for ROC evaluations
     * where the set of returned items does not enumerate all negative
     * references.  
     *
     * @param count Number of outright misses to add to
     * this evaluation.
     * @throws IllegalArgumentException if the count is not positive.
     */
    public void addNegativeMisses(int count) {
        if (count < 0) {
            String msg = "Miss count must be non-negative."
                + " Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        mNegativeRef += count;
    }

    /**
     * Returns the total number of positive and negative reference
     * cases for this evaluation.  The return value is the sum of
     * {@link #numPositiveRef()} and {@code #numNegativeRef()}.
     *
     * @return The number of cases for this evaluation.
     */
    public int numCases() {
        return mPositiveRef + mNegativeRef;
    }

    /**
     * Returns the number of positive reference cases.  This count
     * includes the number of cases added with flag {@code true} plus
     * the number of misses added.
     * 
     * @return Number of positive reference cases.
     */
    public int numPositiveRef() {
        return mPositiveRef;
    }

    /**
     * Return the number of negative reference cases.  The count
     * includes the number of cases added with flag {@code false} plus
     * the number of negative misses added.
     *
     * @return Number of negative reference cases.
     */
    public int numNegativeRef() {
        return mNegativeRef;
    }

    /**
     * Return the R precision.  See the class documentation above for
     * a definition.  The R-precision operating point has identical
     * precision and recall by definition.
     *
     * @return The R precision.
     */
    public double rPrecision() {
        if (mPositiveRef == 0)
            return 1.0;
        double[][] rps = prCurve(false);
        return (mPositiveRef < rps.length)
            ? rps[mPositiveRef-1][1]
            : rps[rps.length-1][1] * (rps.length-1) / mPositiveRef;
    }

    /**
     * Returns the interpolated precision at eleven recall points
     * evenly spaced between 0 and 1.  The recall points are { 0.0,
     * 0.1, 0.2, ..., 1.0 }.
     *
     * @return Eleven-point interpolated precision.
     */
    public double[] elevenPtInterpPrecision() {
        double[] xs = new double[11];
        double[][] rps = prCurve(true);
        double sum = 0.0;
        for (int i = 0; i <= 10; ++i) 
            xs[i] = precisionAtRecall(0.1 * i, rps);
        return xs;
    }

    // could fold this into 11-pt for efficiency
    static double precisionAtRecall(double recall, double[][] rps) {
        for (int i = 0; i < rps.length; ++i) {
            // avoid close near misses from divisions
            if (rps[i][0] + 0.0000000000001 >= recall) {
                return rps[i][1];
            }
        }
        return 0.0;
    }

    /**
     * Returns the average of precisions at the true positive
     * results.  If an item is missed (i.e., it was added
     * by {@code #addMisses(int)}, the precision is considered
     * to be zero.  (See class documentation for more information.)
     * 
     * @return Average precision at each true positive.
     */
    public double averagePrecision() {
        double recall = 0.0;
        double[][] rps = prCurve(false);
        double sum = 0.0;
        for (double[] rp : rps) {
            if (rp[0] > recall) {
                sum += rp[1];
                recall = rp[0];
            }
        }
        return sum / mPositiveRef;
    }

    /**
     * Returns the precision-recall curve, interpolating if
     * the specified flag is true.  See the class documentation
     * above for a definition of the curve.
     *
     * <p><i>Warning:</i> Despite the name, the values
     * returned are in the arrays with recall at index 0
     * and precision at index 1.
     *
     * @param interpolate Set to <code>true</code> for precision
     * interpolation.
     * @return The precision-recall curve.
     */
    public double[][] prCurve(boolean interpolate) {
        PrecisionRecallEvaluation eval
            = new PrecisionRecallEvaluation();
        List<double[]> prList = new ArrayList<double[]>();
        prList.add(new double[] { 0.0, 1.0 });
        for (Case cse : sortedCases()) {
            boolean correct = cse.mCorrect;
            eval.addCase(correct,true);
            double r = div(eval.truePositive(),mPositiveRef);
            double p = eval.precision();
            if (r == 0.0 && p == 0.0) 
                continue;
            prList.add(new double[] { r, p });
        }
        prList.add(new double[] { 1.0, 0.0 });
        return interpolate(prList,interpolate);
    }

    /**
     * Returns the array of recall/precision/score operating points
     * according to the scores of the cases.  Other than adding
     * scores, this method works just like {@link #prCurve(boolean)}.
     * Index 0 is recall, 1 is precision and 2 is the score.
     * <p>.
     *
     * @param interpolate Set to <code>true</code> if the precisions
     * are interpolated through pruning dominated points.
     * @return The precision-recall-score curve for the specified category.
     */
    public double[][] prScoreCurve(boolean interpolate) {
        PrecisionRecallEvaluation eval
            = new PrecisionRecallEvaluation();
        List<double[]> prList = new ArrayList<double[]>();

        for (Case cse : sortedCases()) {
            boolean correct = cse.mCorrect;
            eval.addCase(correct,true);
            double r = div(eval.truePositive(),mPositiveRef);
            double p = eval.precision();
            double s = cse.score();
            prList.add(new double[] { r, p, s });
        }
        return interpolate(prList,interpolate);
    }

    /**
     * Returns the receiver operating characteristic (ROC) curve for
     * the cases ordered by score, interpolating if the specified flag
     * is {@code true}.  See the class documentation above for
     * a definition and example of the returned curve.
     *
     * @param interpolate Interpolate specificity values.
     * @return The receiver operating characteristic curve.
     */
    public double[][] rocCurve(boolean interpolate) {
        PrecisionRecallEvaluation eval = new PrecisionRecallEvaluation();
        List<double[]> ssList = new ArrayList<double[]>();
        int trueNegs = mNegativeRef;
        ssList.add(new double[] { 0.0, 0.0 });
        for (Case cse : sortedCases()) {
            boolean correct = cse.mCorrect;
            eval.addCase(correct,true);
            if (!correct) --trueNegs;
            double r = div(eval.truePositive(), mPositiveRef);
            double rr = div(trueNegs,mNegativeRef);
            ssList.add(new double[] { 1-rr, r });
        }
        ssList.add(new double[] { 1.0, 1.0 });
        if (interpolate)
            ssList = interpolateRoc(ssList);
        return ssList.toArray(EMPTY_DOUBLE_2D_ARRAY);
    }
    
    static List<double[]> interpolateRoc(List<double[]> ssList) {
        List<double[]> result = new ArrayList<double[]>();
        for (int i = 0; (i+1) < ssList.size(); ++i)
            if (ssList.get(i)[0] != ssList.get(i+1)[0])
                result.add(ssList.get(i));
        result.add(ssList.get(ssList.size()-1));
        return result;
    }


    /**
     * Returns the maximum F<sub><sub>1</sub></sub>-measure for an
     * operating point on the PR curve.  See the class documentation
     * above for an example and further explanation.
     *
     * @return Maximum f-measure for the specified category.
     */
    public double maximumFMeasure() {
        return maximumFMeasure(1.0);
    }

    /**
     * Returns the maximum F<sub><sub>&beta;</sub></sub>-measure for
     * an operating point on the precision-recall curve for a
     * specified precision weight <code>&beta; &gt; 0</code>.
     *
     * @return Maximum f-measure for the specified category.
     */
    public double maximumFMeasure(double beta) {
        double maxF = 0.0;
        double[][] pr = prCurve(false);
        for (int i = 0; i < pr.length; ++i) {
            double f = PrecisionRecallEvaluation.fMeasure(beta,pr[i][0],pr[i][1]);
            maxF = Math.max(maxF,f);
        }
        return maxF;
    }

    /**
     * Returns the precision score achieved by returning the top
     * scoring documents up to (but not including) the specified rank.
     * The precision-recall curve is not interpolated for this
     * computation.  For rank 0, the result <code>Double.NaN</code> is
     * returned.
     *
     * @return The precision at the specified rank.
     */
    public double precisionAt(int rank) {
        if (rank < 0) {
            String msg = "Rank must be positive."
                + " Found rank=" + rank;
            throw new IllegalArgumentException(msg);
        }
        if (rank == 0) return 1.0;
        int correctCount = 0;
        Iterator<Case> it = sortedCases().iterator();
        for (int i = 0; i < rank && i < mCases.size(); ++i)
            if (it.next().mCorrect)
                ++correctCount;
        return ((double) correctCount) / (double) rank;
    }

    /*
     * Returns the breakeven point (BEP) for precision and recall
     * based on the the uninterpolated precision-recall curve.
     *
     * <p><b>Note:</b> This method and {@link #rPrecision()} return
     * the same values.
     *
     * @return The break-even point for precision and recall.
     */
    public double prBreakevenPoint() {
        return rPrecision();
    }
    
    /**
     * Returns the reciprocal rank for this evaluation.  The reciprocal
     * rank is defined as the reciprocal <code>1/N</code> of the
     * rank <code>N</code> at which the first true positive is found.
     * This method counts ranks from 1 rather than 0.
     *
     * The return result will be between 1.0 for the first-best result
     * being correct and 0.0, for none of the results being correct.
     *
     * @return The reciprocal rank.
     */
    public double reciprocalRank() {
        Iterator<Case> it = sortedCases().iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Case cse = it.next();
            boolean correct = cse.mCorrect;
            if (correct)
                return 1.0 / (double) (i + 1);
        }
        return 0.0;
    }

    /**
     * Returns the area under the curve (AUC) for the recall-precision
     * curve with interpolation as specified.  See the class documentation
     * for more information.
     *
     * <p><b>Warning:</b> This method uses the parallelogram method
     * for interpolation rather than the usual interpolation method
     * used to calculate AUC for precision-recall in information
     * retrieval evaluations.  The usual AUC calculation for PR curves
     *
     * @param interpolate Set to <code>true</code> to interpolate
     * the precision values.
     * @return The area under the specified precision-recall curve.
     */
    public double areaUnderPrCurve(boolean interpolate) {
        return areaUnder(prCurve(interpolate));
    }

    /**
     * Returns the area under the receiver operating characteristic
     * (ROC) curve.  See the class documentation for more information.
     *
     * @param interpolate Set to <code>true</code> to interpolate
     * the rejection recall values.
     * @return The area under the ROC curve.
     */
    public double areaUnderRocCurve(boolean interpolate) {
        return areaUnder(rocCurve(interpolate));
    }

    /**
     * Returns a string-based representation of this scored precision
     * recall evaluation.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Area Under PR Curve (interpolated)="
                  + areaUnderPrCurve(true));
        sb.append("\n  Area Under PR Curve (uninterpolated)="
                  + areaUnderPrCurve(false));
        sb.append("\n  Area Under ROC Curve (interpolated)="
                  + areaUnderRocCurve(true));
        sb.append("\n  Area Under ROC Curve (uninterpolated)="
                  + areaUnderRocCurve(false));
        sb.append("\n  Average Precision=" + averagePrecision());
        sb.append("\n  Maximum F(1) Measure=" + maximumFMeasure());
        sb.append("\n  BEP (Precision-Recall break even point)="
                  + prBreakevenPoint());
        sb.append("\n  Reciprocal Rank=" + reciprocalRank());
        int[] ranks = new int[] { 5, 10, 25, 100, 500 };
        for (int i = 0; i < ranks.length && mCases.size() < ranks[i]; ++i)
            sb.append("\n  Precision at " + ranks[i]
                      + "=" + precisionAt(ranks[i]));
        return sb.toString();
    }


    /**
     * Prints a precision-recall curve with F-measures.  The curve is formatted
     * as in {@link #prCurve(boolean)}: an array of length-2 arrays of doubles.
     * In each length-2 array, the recall value is at index 0, and the precision
     * is at index 1.  The printed curve prints 3 columns in the following order:
     * precision, recall, F-measure.
     *
     * @param prCurve A precision-recall curve.
     * @param pw The output PrintWriter.
     */
    public static void printPrecisionRecallCurve(double[][]prCurve, PrintWriter pw) {
        pw.printf("%8s %8s %8s\n","PRECI.","RECALL","F");
        for (double[] pr : prCurve) {
            pw.printf("%8.6f %8.6f %8.6f\n", pr[1], pr[0],
                      PrecisionRecallEvaluation.fMeasure(1.0,pr[0],pr[1]));
        }
        pw.flush();
    }



    /**
     * Prints a precision-recall curve with score.  The curve is formatted
     * as in {@link #prScoreCurve(boolean)}: an array of length-3 arrays of doubles.
     * In each length-3 array, the recall value is at index 0, and the precision
     * is at index 1 and score at 2.  The printed curve prints 3 columns in the following order:
     * precision, recall, score.
     *
     * @param prScoreCurve A precision-recall score curve.
     * @param pw The output PrintWriter.
     */
    public static void printScorePrecisionRecallCurve(double[][]prScoreCurve, PrintWriter pw) {
        pw.printf("%8s %8s %8s\n","PRECI.","RECALL","SCORE");
        for (double[] pr : prScoreCurve) {
            pw.printf("%8.6f %8.6f %8.6f\n", pr[1], pr[0],pr[2]);
        }
        pw.flush();
    }


    private List<Case> sortedCases() {
        Collections.sort(mCases,ScoredObject.reverseComparator());
        return mCases;
    }

    // this is used just to cast args to doubles
    static double div(double x, double y) {
        return x/y;
    }

    // first arg is recall, in in strictly increasing order
    private static double[][] interpolate(List<double[]> prList,
                                          boolean interpolate) {
        if (!interpolate)
            return prList.<double[]>toArray(EMPTY_DOUBLE_2D_ARRAY);
        Collections.reverse(prList);
        LinkedList<double[]> resultList = new LinkedList<double[]>();
        double minP = 0.0;
        for (double[] rp : prList) {
            double p = rp[1];
            if (p > minP)
                minP = p;
            else
                rp[1] = minP;
            resultList.addFirst(rp);
        }
        List<double[]> trimmedResultList = new LinkedList<double[]>();
        double[] rp = new double[] { 0.0, 1.0 };
        for (double[] rp2 : resultList) {
            if (rp2[0] == rp[0]) continue;
            trimmedResultList.add(rp);
            rp = rp2;
        }
        trimmedResultList.add(rp);
        return trimmedResultList.<double[]>toArray(EMPTY_DOUBLE_2D_ARRAY);
    }

    static final double[][] EMPTY_DOUBLE_2D_ARRAY = new double[0][];

    private static double areaUnder(double[][] f) {
        double area = 0.0;
        for (int i = 1; i < f.length; ++i)
            area += area(f[i-1][0],f[i-1][1],f[i][0],f[i][1]);
        return area;
    }

    // x2 >= x1
    private static double area(double x1, double y1, double x2, double y2) {
        return (y1 + y2) * (x2-x1) / 2.0;
    }

    static class Case implements Scored {
        private final boolean mCorrect;
        private final double mScore;
        Case(boolean correct, double score) {
            mCorrect = correct;
            mScore = score;
        }
        public double score() {
            return mScore;
        }
        @Override
            public String toString() {
            return mCorrect + " : " + mScore;
        }
    }

}
