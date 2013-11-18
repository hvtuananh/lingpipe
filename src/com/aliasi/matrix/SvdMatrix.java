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

package com.aliasi.matrix;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Random;

/**
 * An <code>SvdMatrix</code> provides a means of storing a matrix that
 * has been factored via a singular-value decomposition (SVD).  This
 * class also provides a static method for computing regularized
 * singular-value decompositions of partial matrices.
 *
 * <h3>Singular Value Decomposition</h3>
 *
 * <p>Singular value decomposition (SVD) factors an
 * <code>m&times;n</code> matrix <code>A</code> into a product of
 * three matrices:
 *
 * <blockquote><pre>
 * A = U * S * V<sup><sup>T</sup></sup></pre></blockquote>
 *
 * where <code>U</code> is an <code>m&times;k</code> matrix,
 * <code>V</code> is an <code>n&times;k</code> matrix, and
 * <code>S</code> is a <code>k&times;k</code> matrix, where
 * <code>k</code> is the rank of the matrix <code>A</code>.  The
 * multiplication (<code>*</code>) is matrix multiplication and
 * the superscripted <code>T</code> indicates matrix transposition.
 *
 * <p>The <code>m</code>-dimensional vectors making up the columns of
 * <code>U</code> are called left singular vectors, whereas
 * the <code>n</code>-dimesnional vectors making up the rows of
 * <code>V</code> are called right singular vectors.  The values on
 * the diagonal of <code>S</code> are called the singular values.
 * The combination of the <code>q</code>-th left singular vector,
 * right singular vector, and singular value is known as a factor.
 *
 * <p>The singular value matrix <code>S</code> is a diagonal matrix
 * with positive, strictly non-increasing values, so that
 * <code>S[i][i] &gt;= S[i+1][i+1]</code>, for <code>i &lt; k</code>.
 * The set of left and set of right singular vectors are orthonormal.
 * Normality means that each singular vector is of unit length (length
 * <code>1</code>).  Orthogonality means that any pair of left singular
 * vectors is orthogonal and any pair of right singular vectors are
 * orthogonal (meaning their dot product is <code>0</code>).
 *
 * <p>Matrices have unique singular-value decompositions
 * up to the re-ordering of columns with equal singular values and
 * up to cancelling sign changes in the singular vectors.
 *
 * <h3>Construction and Value Computation</h3>
 *
 * An <code>SvdMatrix</code> may be constructed out of the singular
 * vectors and singular values, or out of the vectors with singular
 * values scaled in.
 *
 * <p>Given that <code>S</code> is diagonal, the value of a particular
 * entry in <code>A</code> works out to:
 *
 * <blockquote><pre>
 * A[i][j] = <big>&Sigma;</big><sub>k</sub></code> U[i][k] * S[k][k] * V[j][k]</pre></blockquote>
 *
 * To save time in the application and space in the class,
 * we factor <code>S</code> into <code>U</code> and <code>V</code>
 * to produce a new pair of matrices <code>U'</code> and <code>V'</code>
 * defined by:
 *
 * <blockquote><pre>
 * U' = U * sqrt(S)
 * V'<sup>T</sup> = sqrt(S) * V<sup>T</sup></pre></blockquote>
 *
 * with the square-root performed component-wise:
 *
 * <blockquote><pre>
 * sqrt(S)[i][j] = sqrt(S[i][j])</pre></blockquote>
 *
 * By the associativity of matrix multiplication, we have:
 *
 * <blockquote><pre>
 * U * S * V<sup>T</sup>
 * = U * sqrt(S) * sqrt(S) * V
 * = U' * V'<sup>T</sup></pre></blockquote>
 *
 * <p>Thus the class implementation is able to store <code>U'</code>
 * and <code>V'</code>, thus reducing the amount computation involved
 * in returning a value (using column vectors as the default vector
 * orientation):
 *
 * <blocqkuote><pre>
 * A[i][j] = U'[i]<sup>T</sup> * V'[j]</pre></blockquote>
 *
 *
 * <h3>Square Error and the Frobenius Norm</h3>
 *
 * Suppose <code>A</code> and <code>B</code> are
 * <code>m&times;n</code> matrices.  The square error between them is
 * defined by the so-called Frobenius norm, which extends the standard
 * vector L<sub>2</sub> or Euclidean norm to matrices:
 *
 * <blockquote><pre>
 * squareError(A,B)

 * = frobeniusNorm(A-B)

 * = <big>&Sigma;</big><sub>i &lt; m</sub> <big>&Sigma;</big><sub>j &lt; n</sub> (A[i][j] - B[i][j])<sup>2</sup></pre></blockquote>
 *
 * The square error is sometimes referred to as the residual sum
 * of squares (RSS), because <code>A[i][j] - B[i][j]</code>
 * is the residual (difference between actual and predicted value).
 *
 * <h3>Lower-order Approximations</h3>
 *
 * Consider factoring a matrix <code>A</code> of dimension
 * <code>m&times;n</code> into the product of two matrices
 * <code>X * Y<sup>T</sup></code>, where <code>X</code> is of dimension
 * <code>m&times;k</code> and <code>Y</code> is of dimension
 * <code>n&times;k</code>.  We may then measure the square
 * error <code>squareError(A,X * Y)</code> to determine how well
 * the factorization matches <code>A</code>.  We know that if
 * we take <code>U', V'</code> from the singular value
 * decomposition that:
 *
 * <blockquote><pre>
 * squareError(A, U' * V'<sup>T</sup>) = 0.0</pre></blockquote>
 *
 * Here <code>U'</code> and <code>V'</code> have a number of columns
 * (called factors) equal to the rank of the matrix <code>A</code>.
 * The singular value decomposition is such that the first
 * <code>k</code> columns of <code>U'</code> and <code>V'</code>
 * provide the best order <code>q</code> approximation of
 * <code>A</code> of any <code>X</code> and <code>Y</code>
 * of dimensions <code>m&times;q</code> and <code>n&times;q</code>
 * In symbols:
 *
 * <blockquote><pre>
 * U'<sub>q</sub>, V'<sub>q</sub> = argmin <sub>X is m&times;q, Y is n&times;q</sub> squareError(A, X * Y<sup>T</sup>)</pre></blockquote>
 *
 * where <code>U'<sub>q</sub></code> is the restriction of <code>U'</code>
 * to its first <code>q</code> columns.
 *
 * <p>Often errors are reported as means, where the mean square error
 * (MSE) is defined by:
 *
 * <blockquote><pre>
 * meanSquareError(A,B) = squareError(A,B)/(m&times;n)</pre></blockquote>
 *
 * <p>To adjust to a linear scale, the square root of mean square
 * error (RMSE) is often used:
 *
 * <blockquote><pre>
 * rootMeanSquareError(A,B) = sqrt(meanSquareError(A,B))</pre></blockquote>
 *
 *
 * <h3>Partial Matrices</h3>
 *
 * A partial matrix is one in which some of the values are unknown.
 * This is in contrast with a sparse matrix, in which most of the
 * values are zero.  A variant of singular value decomposition may be
 * used to impute the unknown values by minimizing square error with
 * respect to the known values only.  Unknown values are then simply
 * derived from the factorization <code>U' * V'<sup>T</sup></code>.
 * Typically, the approximation is of lower order than the rank of
 * the matrix.
 *
 * <h3>Regularized SVD via Shrinkage</h3>
 *
 * Linear regression techniques such as SVD often overfit their
 * training data in order to derive exact answers.  This problem
 * is mitigated somewhat by choosing low-order approximations to
 * the full-rank SVD.  Another option is to penalize large values
 * in the singular vectors, thus favoring smaller values.  The
 * most common way to do this because of its practicality is via
 * parameter shrinkage.
 *
 * <p>Shrinkage is a general technique in least squares fitting that
 * adds a penalty term proportional to the square of the size of
 * the parameters.  Thus the square error objective function is
 * replaced with a regularized version:
 *
 * <blockquote><pre>
 * regularizedError(A, U' * V')

 * = squareError(A, U' * V')

 * + parameterCost(U') + parameterCost(V')</pre></blockquote>
 *
 * where the parameter costs for a vector <code>X</code> of
 * dimensionality <code>q</code> is the sum of the squared
 * parameters:
 *
 * <blockquote><pre>
 * parameterCost(X)

 * = &lambda; * <big>&Sigma;</big><sub>i &lt; q</sub> X[i]<sup>2</sup>

 * = &lambda; * length(X)<sup>2</sup></pre></blockquote>
 *
 * Note that the hyperparameter <code>&lambda;</code> scales the
 * parameter cost term in the overall error.
 *
 * <p>Setting the regularization parameter to zero sets the parameter
 * costs to zero, resulting in an unregularized SVD computation.

 * <p>In Bayesian terms, regularization is equivalent to a normal
 * prior on parameter values centered on zero with variance controlled
 * by <code>&lambda;</code>.  The resulting minimizer of regularized
 * error is the maximum a posteriori (MAP) solution.  The Bayesian
 * approach also allows covariances to be estimated (including simple
 * parameter variance estimates), but these are not implemented in
 * this class.
 *
 *
 * <h3>Regularized Stochastic Gradient Descent</h3>
 *
 * Singular value decomposition may be computed &quot;exactly&quot;
 * (modulo arithmetic precision and convergence) using an algorithm
 * whose time complexity is <code>O(m<sup>3</sup> +
 * n<sup>3</sup>)</code> for an <code>m&times;n</code> matrix (equal
 * to <code>O(max(m,n)<sup>3</sup>)</code>.  Arithmetic precision is
 * especially vexing at singular values near zero and with highly
 * correlated rows or columns in the input matrix.
 *
 * <p>For large partial matrices, we use a form of stochastic gradient
 * descent which computes a single row and column singular vector (a
 * single factor, that is) at a time.  Each factor is estimated by
 * iteratively visiting the data points and adjusting the unnormalized
 * singular vectors making up the current factor.  Each adjustment is
 * a least-squares fitting step, where we simultaneously adjust the
 * left singular vectors given the right singular vectors and
 * vice-versa.
 *
 * <p>The least-squares adjustments take the following form.  For each
 * value, we compute the current error (using the order <code>k</code>
 * approximation and the current order <code>k+1</code> values) and
 * then move the vectors to reduce error.  We use <code>U'</code> and
 * <code>V'</code> for the incremental values of the singular vectors
 * scaled by the singular values:
 *
 * <blockquote><pre>
 * for (k = 0; k &lt; maxOrder; ++k)
 *     for (i &lt; m) U'[i][k] = random.nextGaussian()*initValue;
 *     for (j &lt; n) V'[j][k] = random.nextGaussian()*initValue;
 *     for (epoch = 0; epoch &lt; maxEpochs &amp;&amp; not converged; ++epoch)
 *         for (i,j such that M[i][j] defined)
 *             error = M[i][j] - U'<sub>k</sub>[i] * V'<sub>k</sub>[j] <small>// * is vector dot product</small>
 *             uTemp = U'[i][k]
 *             vTemp = V'[j][k]
 *             U'[i][k] += learningRate[epoch] * ( error * vTemp - regularization * uTemp )
 *             V'[j][k] += learningRate[epoch] * ( error * uTemp - regularization * vTemp )</pre></blockquote>
 *
 * where <code>initValue</code>, <code>maxEpochs</code>,
 * <code>learningRate</code> (see below), and
 * <code>regularization</code> are algorithm hyperparameters.  Note
 * that the initial values of the singular vectors are set randomly to
 * the result of a draw from a Gaussian (normal) distribution with
 * mean 0.0 and variance 1.0.
 *
 * <p>Because we use the entire set of
 * factors in the error computation, the current factor is guaranteed
 * to have singular vectors orthogonal to the singular vectors already
 * computed.
 *
 * <p>Note that in the actual implementation, the contribution to the
 * error of the first <code>k-1</code> factors is cached to reduce
 * time in the inner loop.  This requires a double-length floating
 * point value for each defined entry in the matrix.
 *
 * <h4>Gradient Interpretation</h4>
 *
 * <p>Like most linear learners, this algorithm merely moves the
 * parameter vectors <code>U'[i]</code> and <code>U'[j]</code> in the
 * direction of the gradient of the error.  The gradient is the
 * multivariate derivative of the objective function being minimized.
 * Because our object is squared error, the gradient is just its
 * derivative, which is just (two times) the (linear) error itself.  We
 * roll the factor of 2 into the learning rate to derive the update in
 * the algorithm pseudo-code above.
 *
 * <p>The term <code>(error * vTemp)</code> is the component of the
 * error gradient due to the current value of <code>V'[i][k]</code>
 * and the term <code>(regularization * uTemp)</code> is the component of the
 * gradient to the size of the parameter <code>U'[i][k]</code>.  The
 * updates thus move the parameter vectors in the direction of
 * the gradient.
 *
 * <h4>Convergence Conditions</h4>
 *
 * <p>The convergence conditions for a given factor require either
 * hitting the maximum number of allowable epochs, or finding the
 * improvement from one epoch to the next is below some relative
 * threshold:
 *
 * <blockquote><pre>
 * regError<sup>(epoch)</sup> = regError(M,U'<sub>k</sub><sup>(epoch)</sup> * V'<sub>k</sub><sup>(epoch)</sup><sup>T</sup>)

 * relativeImprovement<sup>(epoch+1)</sup> = relativeDiff(regError<sup>(epoch+1)</sup>, regError<sup>(epoch)</sup>)

 * relativeDiff(x,y) = abs(x-y)/(abs(x) + abs(y))</pre></blockquote>
 *
 * When the relative difference in square error is less than
 * a hyperparameter threshold <code>minImprovement</code>, the
 * epoch terminates and the algorithm moves on to the next
 * factor <code>k+1</code>.
 *
 * <p>Note that a complete matrix is a degenerate kind of partial
 * matrix.  The gradient descent computation still works in this
 * situation, but is not as efficient or as accurate as an
 * algebraic SVD solver for small matrices.
 *
 * <h3>Annealing Schedule</h3>
 *
 * Learning rates that are too high are unstable, whereas learning rates
 * that are too low never reach their targets.  To get around this
 * problem, the learning rate, <code>learningRate[epoch]</code>, is
 * lowered as the number of epochs increase.  This lowering of the
 * learning rate has a thermodynamic interpretation in terms of free
 * energy, hence the name &quot;annealing&quot;.  Larger moves are
 * made in earlier epochs, then the temperature is gradually lowered
 * so that the learner may settle into a stable fixed point.  The
 * function <code>learningRate[epoch]</code> is called the annealing
 * schedule.
 *
 * <p>There are theoretical requirements on the annealing schedule
 * that guarantee convergence (up to arithmetic precision and no upper
 * bound on the number of epochs):
 *
 * <blockquote><pre>
 * <big>&Sigma;</big><sub>epoch</sub> learningRate[epoch] = infinity

 * <big>&Sigma;</big><sub>epoch</sub> learningRate[epoch]<sup>2</sup> &lt; infinity</pre></blockquote>
 *
 * The schedule we use is the one commonly chosen to meet the
 * above requirements:
 *
 * <blockquote><pre>
 * learningRate[epoch] = initialLearningRate / (1 + epoch/annealingRate)</pre></blockquote>
 *
 * where <code>initialLearningRate</code> is an initial learning rate and
 * <code>annealingFactor</code> determines how quickly it shrinks.
 * The learning rate moves from its initial size
 * (<code>initialLearningRate</code>) to one half (<code>1/2</code>) of its
 * original size after <code>annealingRate</code> epochs, and
 * moves from its initial size to one tenth (<code>1/10</code>) of
 * its initial size after <code>9 * annealingRate</code> epochs,
 * and one hundredth of its initial size after
 * <code>99 * annealingRate</code> epochs..
 *
 * <h3>Parameter Choices</h3>
 *
 * <p>The previous discussion has introduced a daunting list of
 * parameters required for gradient descent for singular value
 * decomposition.  Unfortunately, the stochastic gradient descent
 * solver requires a fair amount of tuning to recover a low mean
 * square error factorization.  The optimal settings will also depend
 * on the input matrix; for example, very sparse partial matrices are
 * much easier to fit than dense matrices.
 *
 * <h4>Maximum Order</h4>
 *
 * <p>Determining the order of the decomposition is mostly a matter
 * of determining how many orders are needed for the amount of
 * smoothing required.  Low order reconstructions are useful for
 * most applications.  One way to determine maximum order is using
 * held out data for an application.  Another is to look for
 * a point where the singular values become (relatively) insignificant.
 *
 *
 * <h4>Maximum Epochs and Early Stopping</h4>
 *
 * <p>For low mean square error factorizations, many epochs may be
 * necessary.  Lowering the maximum number of epochs leads to what is
 * known as early stopping.  Sometimes, early stopping leads to
 * more accurate held-out predictions, but it will always raise
 * the factorization error (training data predictions).  In general,
 * the maximum number of epochs needs to be set empirically.
 * Initially, try fewer epochs and gradually raise the number of
 * epochs until the desired accuracy is achieved.
 *
 * <h4>Minimum Improvement</h4>
 *
 * By setting the minimum improvement to 0.0, the algorithm is forced
 * to use the maximum number of epochs.  By setting it above this
 * level, a form of early stopping is achieved when the benefit of
 * another epoch of refitting falls below a given improvement
 * threshold.  This value may also be set on an application basis
 * using held out data, or it may be fit to achieve a given level of
 * mean square error on the training (input) data.  The minimum
 * improvement needs to be set fairly low (less than 0.000001) to
 * achieve reasonably precise factorizations.  Note that minimum
 * improvement is defined relatively, as noted above.
 *
 * <h4>Initial Parameter Values</h4>
 *
 * <p>Initial values of the singular vectors are not particularly
 * sensitive, because we are using multiplicative updates.  A good
 * rule of thumb for starting values is the the inverse square root of
 * the maximum order:
 *
 * <blockquote><pre>
 * initVal ~ 1 / sqrt(maxOrder)</pre></blockquote>
 *
 * <h4>Learning Rate, Maximum Epochs and Annealing</h4>
 *
 * <p>A good starting point for the learning rate is 0.01.  The
 * annealing parameter should be set so that the learning rate is cut
 * by at least a factor of 10 in the final rounds.  This calls for a
 * value that's roughly one tenth of the maximum number of epochs.  If
 * the initial learning rate is set higher, then the annealing
 * schedule should be more agressive so that it spends a fair amount
 * of time in the 0.001 to 0.0001 learning rate range.
 *
 *
 * <h3>References</h3>
 *
 * <p>There are thorough Wikipedia articles on singular value decomposition
 * and gradient descent, although the SVD entry focuses entirely on
 * complete (non-partial) matrices:
 *
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Singular_value_decomposition">Wikipedia: Singular Value Decomposition</a></li>
 * <li><a href="http://en.wikipedia.org/wiki/Gradient_descent">Wikipedia: Gradient Descent</a></li>
 * </ul>
 *
 * Both of the standard machine learning textbooks have good
 * theoretical explanations of least squares, regularization,
 * gradient descent, and singular value decomposition, but not
 * all together:
 *
 * <ul>
 * <li>Chris Bishop.  2007.  <i>Pattern Recognition and Machine Learning.</i>  Springer.</li>
 * <li>Trevor Hastie, Robert Tibshirani, and Jerome Friedman.  2001.
 * <i>The Elements of Statistical Learning</i>.  Springer.</i>
 * </ul>
 *
 * <p>The following is a particularly clear explanation of many of
 * the issues involved in the context of neural networks:
 *
 * <ul>
 * <li>Genevieve Orr, Nici Schraudolph, and Fred Cummins.  1999.
<a href="http://www.willamette.edu/~gorr/classes/cs449/intro.html">CS-449: Neural Networks</a>.  Willamette University course notes.</li>
 * </ul>

 * <p>Our partial SVD solver is based on C code from Timely
 * Development (see license below).  Timely based their code on Simon
 * Funk's blog entry.  Simon Funk's approach was itself based on his
 * and Genevieve Gorrell's 2005 EACL paper.
 *
 * <ul>
 * <li><a href="http://www.timelydevelopment.com/Demos/NetflixPrize.htm">Timely Development's Netflix Prize Page</a>.
 * <li>Simon Funk (pseudonym for Brandyn Webb). 2007.
 * <a href="http://sifter.org/~simon/journal/20061211.html">Gradient Descent SVD Algorithm</a>.  <i>The Evolution of Cybernetics</i> blog.
 * <li>Genevieve Gorrell.  2006.
 * <a href="http://acl.ldc.upenn.edu/E/E06/E06-1013.pdf">Generalized Hebbian Algorithm for Incremental Singular Value
Decomposition in Natural Language Processing</a>.  EACL 2006.</li>
* <li>Genevieve Gorrell.  2006.
* <a href="http://www.dcs.shef.ac.uk/~genevieve/gorrell_thesis.pdf">Generalized Hebbian Algorithm for Dimensionality Reduction in Natural Language Processing</a>. Ph.D. Thesis.
* Link&#246;ping University.  Sweden.</li>
 * </ul>
 *
 * <h3>Acknowledgements</h3>
 *
 * <p>The singular value decomposition code is rather loosely based on
 * a C program developed by <a
 * href="http://www.timelydevelopment.com/">Timely Development</a>.
 * Here is the copyright notice for the original code:
 *
 * <blockquote><pre style="font-size:80%">
 * // SVD Sample Code
 * //
 * // Copyright (C) 2007 Timely Development (www.timelydevelopment.com)
 * //
 * // Special thanks to Simon Funk and others from the Netflix Prize contest
 * // for providing pseudo-code and tuning hints.
 * //
 * // Feel free to use this code as you wish as long as you include
 * // these notices and attribution.
 * //
 * // Also, if you have alternative types of algorithms for accomplishing
 * // the same goal and would like to contribute, please share them as well :)
 * //
 * // STANDARD DISCLAIMER:
 * //
 * // - THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY
 * // - OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
 * // - LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR
 * // - FITNESS FOR A PARTICULAR PURPOSE.
 * </pre></blockquote>
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.2
 */
public class SvdMatrix extends AbstractMatrix {

    private final double[][] mRowVectors;
    private final double[][] mColumnVectors;
    private final int mOrder;

    /**
     * Construct a factored matrix using the specified row and column
     * vectors at the specified order.  Each vector in the arrays of
     * row and column vectors must be of the same dimension as the
     * order.
     *
     * <p>See the class documentation above for more information
     * on singular value decomposition.
     *
     * @param rowVectors Row vectors, indexed by row.
     * @param columnVectors Column vectors, indexed by column.
     * @param order Dimensionality of the row and column vectors.
     */
    public SvdMatrix(double[][] rowVectors,
                     double[][] columnVectors,
                     int order) {
        verifyDimensions("row",order,rowVectors);
        verifyDimensions("column",order,columnVectors);
        mRowVectors = rowVectors;
        mColumnVectors = columnVectors;
        mOrder = order;
    }

    /**
     * Construct an SVD matrix using the specified singular vectors
     * and singular values.  The order of the factorization is equal to
     * the length of the singular values.  Each singular vector must
     * be the same dimensionality as the array of singular values.
     *
     * <p>See the class documentation above for more information
     * on singular value decomposition.
     *
     * @param rowSingularVectors Row singular vectors, indexed by row.
     * @param columnSingularVectors Column singular vectors, indexed by column.
     * @param singularValues Array of singular values, in decreasing order.
     */
    public SvdMatrix(double[][] rowSingularVectors,
                     double[][] columnSingularVectors,
                     double[] singularValues) {
        mOrder = singularValues.length;
        verifyDimensions("row",mOrder,rowSingularVectors);
        verifyDimensions("column",mOrder,columnSingularVectors);

        mRowVectors = new double[rowSingularVectors.length][mOrder];
        mColumnVectors = new double[columnSingularVectors.length][mOrder];
        double[] sqrtSingularValues = new double[singularValues.length];
        for (int i = 0; i < sqrtSingularValues.length; ++i)
            sqrtSingularValues[i] = Math.sqrt(singularValues[i]);
        scale(mRowVectors,rowSingularVectors,sqrtSingularValues);
        scale(mColumnVectors,columnSingularVectors,sqrtSingularValues);
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return The number of rows in this matrix.
     */
    @Override
    public int numRows() {
        return mRowVectors.length;
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return The number of columns in this matrix.
     */
    @Override
    public int numColumns() {
        return mColumnVectors.length;
    }

    /**
     * Returns the order of this factorization.  The order
     * is the number of dimensions in the singular vectors
     * and the singular values.
     *
     * @return The order of this decomposition.
     */
    public int order() {
        return mRowVectors[0].length;
    }

    /**
     * Returns the value of this matrix at the specified row and column.
     *
     * @param row Row index.
     * @param column Column index.
     * @return The value of this matrix at the specified row and
     * column.
     */
    @Override
    public double value(int row, int column) {
        double[] rowVec = mRowVectors[row];
        double[] colVec = mColumnVectors[column];
        double result = 0.0;
        for (int i = 0; i < rowVec.length; ++i)
            result += rowVec[i] * colVec[i];
        return result;
    }

    /**
     * Returns the array of singular values for this decomposition.
     *
     * @return The singular values for this decomposition.
     */
    public double[] singularValues() {
        double[] singularValues = new double[mOrder];
        for (int i = 0; i < singularValues.length; ++i)
            singularValues[i] = singularValue(i);
        return singularValues;
    }

    /**
     * Returns the singular value for the specified order.
     *
     * @param order Dimension of the singular value.
     * @return The singular value at the specified order.
     */
    public double singularValue(int order) {
        if (order >= mOrder) {
            String msg = "Maximum order=" + (mOrder-1)
                + " found order=" + order;
            throw new IllegalArgumentException(msg);
        }
        return columnLength(mRowVectors,order)
            * columnLength(mColumnVectors,order);
    }

    /**
     * Returns a matrix in which the left singular vectors make up the
     * columns.  The first index is for the row of the original matrix
     * and the second index is for the order of the singular vector.
     * Thus the returned matrix is <code>m&times;k</code>, if the
     * original input was an <code>m&times;n</code> matrix and SVD was
     * performed at order <code>k</code>.

     * @return The left singular vectors of this matrix.
     */
    public double[][] leftSingularVectors() {
        return normalizeColumns(mRowVectors);
    }


    /**
     * Returns a matrix in which the right singular vectors make up
     * the columns.  The first index is for the column of the original
     * matrix and the second index is for the order of the singular
     * vector.  Thus the returned matrix is <code>n&times;k</code>, if
     * the original input was an <code>m&times;n</code> matrix and SVD
     * was performed at order <code>k</code>.
     *
     * @return The right singular vectors.
     */
    public double[][] rightSingularVectors() {
        return normalizeColumns(mColumnVectors);
    }


    /**
     * Returns the signular value decomposition of the specified
     * complete matrix of values.
     *
     * <p>For a full description of the arguments and values, see
     * the method documentation for
     * {@link #partialSvd(int[][],double[][],int,double,double,double,double,Reporter,double,int,int)}
     * and the class documentation above.
     *
     * <p>The two-dimensional array of values must be an
     * <code>m &times; n</code> matrix.  In particular, each row
     * must be of the same length.  If this is not the case, an illegal
     * argument exception will be raised.
     *
     * <p>This is now a utility method that calls {@link
     * #svd(double[][],int,double,double,double,double,Reporter,double,int,int)}
     * with a reporter wrapping the specified print writer at the
     * debug level, or a silent print writer.
     *
     * @param values Array of values.
     * @param maxOrder Maximum order of the decomposition.
     * @param featureInit Initial value for singular vectors.
     * @param initialLearningRate Incremental multiplier of error
     * determining how fast learning occurs.
     * @param annealingRate Rate at which annealing occurs; higher values
     * provide more gradual annealing.
     * @param regularization A regularization constant to damp learning.
     * @param reporter Reporter to which to send progress and error
     * reports.
     * @param minImprovement Minimum relative improvement in mean
     * square error required to finish an epoch.
     * @param minEpochs Minimum number of epochs for training.
     * @param maxEpochs Maximum number of epochs for training.
     * training, or {@code null} if no output is desired.
     * @return Singular value decomposition for the specified partial matrix
     * at the specified order.
     * @throws IllegalArgumentException Under conditions listed in the
     * method documentation above.
     */
    public static SvdMatrix svd(double[][] values,
                                int maxOrder,

                                double featureInit,
                                double initialLearningRate,
                                double annealingRate,
                                double regularization,

                                Reporter reporter,

                                double minImprovement,
                                int minEpochs,
                                int maxEpochs) {

        if (reporter == null)
            reporter = Reporters.silent();

        int m = values.length;
        int n = values[0].length;
        reporter.info("Calculating SVD");
        reporter.info("#Rows=" + m + " #Cols=" + n);

        // check all rows same length
        for (int i = 1; i < m; ++i) {
            if (values[i].length != n) {
                String msg = "All rows must be of same length."
                    + " Found row[0].length=" + n
                    + " row[" + i + "]=" + values[i].length;
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // shared column Ids rows
        int[] sharedRow = new int[n];
        for (int j = 0; j < n; ++j)
            sharedRow[j] = j;
        int[][] columnIds = new int[m][];
        for (int j = 0; j < m; ++j)
            columnIds[j] = sharedRow;

        return partialSvd(columnIds,
                          values,
                          maxOrder,
                          featureInit,
                          initialLearningRate,
                          annealingRate,
                          regularization,
                          reporter,
                          minImprovement,
                          minEpochs,
                          maxEpochs);
    }




    /**
     * Return the singular value decomposition of the specified
     * partial matrix, using the specified search parameters.
     *
     * <p>The writer parameter may be set to allow incremental progress
     * reports to that writer during training.  These report on RMSE
     * per epoch.
     *
     * <p>See the class documentation above for a description of the
     * algorithm.
     *
     * <p>There are a number of constraints on the input, any
     * violation of which will raise an illegal argument exception.
     * The conditions are:
     * <ul>
     *
     *
     * <li>The maximum order must be greater than zero.</li>
     *
     * <li>The minimum relative improvement in mean square error must be non-negative
     * and finite.</li>
     *
     * <li>The minimum number of epochs must be greater than zero
     * and less than or equal to the maximum number of epochs.</li>
     *
     * <li>The feature initialization value must be non-zero and finite.</li>
     *
     * <li>The learning rate must be positive and finite.</li>
     *
     * <li>The regularization parameter must be non-negative and finite.</li>
     *
     * <li>The column identitifer and value arrays must be the same
     * length.</li>
     *
     * <li>The elements of the column identifier array and the
     * value array must all be of the same length.</li>
     *
     * <li>All column identifiers must be non-negative.</li>
     *
     * <li>Each row of the column identifier matrix must contain
     * columns in strictly ascending order.
     *
     * </ul>
     *
     * @param columnIds Identifiers of column index for given row and entry.
     * @param values Values at row and column index for given entry.
     * @param maxOrder Maximum order of the decomposition.
     * @param featureInit Initial value for singular vectors.
     * @param initialLearningRate Incremental multiplier of error determining how
     * fast learning occurs.
     * @param annealingRate Rate at which annealing occurs; higher values
     * provide more gradual annealing.
     * @param regularization A regularization constant to damp learning.
     * @param reporter Reporter to which progress reports are written, or
     * {@code null} if no reporting is required.
     * @param minImprovement Minimum relative improvement in mean square error required
     * to finish an epoch.
     * @param minEpochs Minimum number of epochs for training.
     * @param maxEpochs Maximum number of epochs for training.
     * @return Singular value decomposition for the specified partial matrix
     * at the specified order.
     * @throws IllegalArgumentException Under conditions listed in the
     * method documentation above.
     */
    public static SvdMatrix partialSvd(int[][] columnIds,
                                       double[][] values,

                                       int maxOrder,

                                       double featureInit,
                                       double initialLearningRate,
                                       double annealingRate,
                                       double regularization,

                                       Reporter reporter,

                                       double minImprovement,
                                       int minEpochs,
                                       int maxEpochs) {
        return partialSvd(columnIds,values,
                          maxOrder,
                          featureInit,initialLearningRate,annealingRate,regularization,
                          new Random(),
                          reporter,
                          minImprovement,minEpochs,maxEpochs);
    }



    /**
     * This method is identical to the other singular-value decomposition
     * method, only with a specified randomizer.
     *
     * @param columnIds Identifiers of column index for given row and entry.
     * @param values Values at row and column index for given entry.
     * @param maxOrder Maximum order of the decomposition.
     * @param featureInit Initial value for singular vectors.
     * @param initialLearningRate Incremental multiplier of error determining how
     * fast learning occurs.
     * @param annealingRate Rate at which annealing occurs; higher values
     * provide more gradual annealing.
     * @param random Randomizer to use for initialization.
     * @param reporter Reporter to which progress reports are written, or {@code null}
     * for no reporting.
     * @param regularization A regularization constant to damp learning.
     * @param minImprovement Minimum relative improvement in mean square error required
     * to finish an epoch.
     * @param minEpochs Minimum number of epochs for training.
     * @param maxEpochs Maximum number of epochs for training.
     * @return Singular value decomposition for the specified partial matrix
     * at the specified order.
     * @throws IllegalArgumentException Under conditions listed in the
     * method documentation above.
     */
        static SvdMatrix partialSvd(int[][] columnIds,
                                       double[][] values,

                                       int maxOrder,

                                       double featureInit,
                                       double initialLearningRate,
                                       double annealingRate,
                                       double regularization,

                                       Random random,

                                       Reporter reporter,

                                       double minImprovement,
                                       int minEpochs,
                                       int maxEpochs) {

        if (reporter == null)
            reporter = Reporters.silent();
        reporter.info("Start");
        if (maxOrder < 1) {
            String msg = "Max order must be >= 1."
                + " Found maxOrder=" + maxOrder;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }
        if (minImprovement < 0 || notFinite(minImprovement)) {
            String msg = "Min improvement must be finite and non-negative."
                + " Found minImprovement=" + minImprovement;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }
        if (minEpochs <= 0 || maxEpochs < minEpochs) {
            String msg = "Min epochs must be non-negative and less than or equal to max epochs."
                + " found minEpochs=" + minEpochs
                + " maxEpochs=" + maxEpochs;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }
        if (notFinite(featureInit) || featureInit == 0.0) {
            String msg = "Feature inits must be finite and non-zero."
                + " Found featureInit=" + featureInit;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }
        if (notFinite(initialLearningRate) || initialLearningRate < 0) {
            String msg = "Initial learning rate must be finite and non-negative."
                + " Found initialLearningRate=" + initialLearningRate;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }
        if (notFinite(regularization) || regularization < 0) {
            String msg = "Regularization must be finite and non-negative."
                + " Found regularization=" + regularization;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }
        for (int row = 0; row < columnIds.length; ++row) {
            if (columnIds == null) {
                String msg = "ColumnIds must not be null.";
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
            if (values == null) {
                String msg = "Values must not be null";
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
            if (columnIds[row] == null) {
                String msg = "All column Ids must be non-null."
                    + " Found null in row=" + row;
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
            if (values[row] == null) {
                String msg = "All values must be non-null."
                    + " Found null row=" + row;
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
            if (columnIds[row].length != values[row].length) {
                String msg = "column Ids and values must be same length."
                    + " For row=" + row
                    + " Found columnIds[row].length=" + columnIds[row].length
                    + " Found values[row].length=" + values[row].length;
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
            for (int i = 0; i < columnIds[row].length; ++i) {
                if (columnIds[row][i] < 0) {
                    String msg = "Column ids must be non-negative."
                        + " Found columnIds[" + row + "][" + i + "]=" + columnIds[row][i];
                    reporter.fatal(msg);
                    throw new IllegalArgumentException(msg);
                }
                if (i > 0 && columnIds[row][i-1] >= columnIds[row][i]) {
                    String msg = "All column Ids must be same length."
                        + " At row=" + row
                        + " Mismatch at rows " + i + " and " + (i-1);
                    reporter.fatal(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        if (annealingRate < 0 || notFinite(annealingRate)) {
            String msg = "Annealing rate must be finite and non-negative."
                + " Found rate=" + annealingRate;
            reporter.fatal(msg);
            throw new IllegalArgumentException("14");
        }

        int numRows = columnIds.length;

        int numEntries = 0;
        for (double[] xs : values)
            numEntries += xs.length;

        int maxColumnIndex = 0;
        for (int[] xs : columnIds)
            for (int i = 0; i < xs.length; ++i)
                if (xs[i] > maxColumnIndex)
                    maxColumnIndex = xs[i];
        int numColumns = maxColumnIndex + 1;

        maxOrder = Math.min(maxOrder,Math.min(numRows,numColumns));

        double[][] cache = new double[values.length][];
        for (int row = 0; row < numRows; ++row) {
            cache[row] = new double[values[row].length];
            Arrays.fill(cache[row],0.0F);
        }

        List<double[]> rowVectorList = new ArrayList<double[]>(maxOrder);
        List<double[]> columnVectorList = new ArrayList<double[]>(maxOrder);
        for (int order = 0; order < maxOrder; ++order) {
            reporter.info("  Factor=" + order);
            double[] rowVector = initArray(numRows,featureInit,random);
            double[] columnVector = initArray(numColumns,featureInit,random);
            double rmseLast = Double.POSITIVE_INFINITY;
            for (int epoch = 0; epoch < maxEpochs; ++epoch) {
                double learningRateForEpoch = initialLearningRate / (1.0 + epoch / annealingRate);
                double sumOfSquareErrors = 0.0;
                for (int row = 0; row < numRows; ++row) {
                    int[] columnIdsForRow = columnIds[row];
                    double[] valuesForRow = values[row];
                    double[] cacheForRow = cache[row];
                    for (int i = 0; i < columnIdsForRow.length; ++i) {
                        int column = columnIdsForRow[i];
                        double prediction = predict(row,column,
                                                    rowVector,columnVector,
                                                    cacheForRow[i]);
                        double error = valuesForRow[i] - prediction;

                        sumOfSquareErrors += error * error;

                        double rowCurrent = rowVector[row];
                        double columnCurrent = columnVector[column];

                        rowVector[row]
                            += learningRateForEpoch
                            * (error * columnCurrent - regularization * rowCurrent);
                        columnVector[column]
                            += learningRateForEpoch
                            * (error * rowCurrent - regularization * columnCurrent);
                    }
                }
                double rmse = Math.sqrt(sumOfSquareErrors/numEntries);
                reporter.info("    epoch=" + epoch + " rmse=" + rmse);
                if ((epoch >= minEpochs) && (relativeDifference(rmse,rmseLast) < minImprovement)) {
                    reporter.info("Converged in epoch=" + epoch
                                  + " rmse=" + rmse
                                  + " relDiff=" + relativeDifference(rmse,rmseLast));
                    break;
                }
                rmseLast = rmse;
            }
            reporter.info("Order=" + order + " RMSE=" + rmseLast);
            rowVectorList.add(rowVector);
            columnVectorList.add(columnVector);

            for (int row = 0; row < cache.length; ++row) {
                double[] cacheRow = cache[row];
                for (int i = 0; i < cacheRow.length; ++i) {
                    cacheRow[i]
                        = predict(row,columnIds[row][i],
                                  rowVector,columnVector,
                                  cacheRow[i]);
                }
            }
        }
        double[][] rowVectors = rowVectorList.toArray(EMPTY_DOUBLE_2D_ARRAY);
        double[][] columnVectors = columnVectorList.toArray(EMPTY_DOUBLE_2D_ARRAY);

        return new SvdMatrix(transpose(rowVectors),
                             transpose(columnVectors),
                             maxOrder);
    }



    static double relativeDifference(double x, double y) {
        return Math.abs(x - y) / (Math.abs(x) + Math.abs(y));
    }

    static double[][] transpose(double[][] xs) {
        double[][] ys = new double[xs[0].length][xs.length];
        for (int i = 0; i < xs.length; ++i)
            for (int j = 0; j < xs[i].length; ++j)
                ys[j][i] = xs[i][j];
        return ys;
    }

    static double predict(int row, int column,
                          double[] rowVector, double[] columnVector,
                          double cache) {
        return cache + rowVector[row] * columnVector[column];
    }

    static double[] initArray(int size, double val, Random random) {
        double[] xs = new double[size];

        // random init
        for (int i = 0; i < xs.length; ++i)
            xs[i] = random.nextGaussian() * val;

        return xs;
    }

    static boolean notFinite(double x) {
        return Double.isNaN(x) || Double.isInfinite(x);
    }

    static double columnLength(double[][] xs, int col) {
        double sumOfSquares = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sumOfSquares += xs[i][col] * xs[i][col];  // subopt array mem order
        return Math.sqrt(sumOfSquares);
    }


    static void scale(double[][] vecs, double[][] singularVecs,
                      double[] singularVals) {
        for (int i = 0; i < vecs.length; ++i)
            for (int k = 0; k < vecs[i].length; ++k)
                vecs[i][k] = singularVecs[i][k] * singularVals[k];
    }

    static void verifyDimensions(String prefix, int order, double[][] vectors) {
        for (int i = 0; i < vectors.length; ++i) {
            if (vectors[i].length != order) {
                String msg = "All vectors must have length equal to order."
                    + " order=" + order
                    + " " + prefix + "Vectors[" + i + "].length="
                    + vectors[i].length;
                throw new IllegalArgumentException(msg);
            }
        }
    }

    // normalize columns to unit length;
    static double[][] normalizeColumns(double[][] xs) {
        int numDims = xs.length;
        int order = xs[0].length;
        double[][] result = new double[numDims][order];
        for (int j = 0; j < order; ++j) {
            double sumOfSquares = 0.0;
            for (int i = 0; i < numDims; ++i) {
                double valIJ = xs[i][j];
                result[i][j] = valIJ;
                sumOfSquares += valIJ * valIJ;
            }
            double length = Math.sqrt(sumOfSquares);
            for (int i = 0; i < numDims; ++i)
                result[i][j] /= length;
        }
        return result;
    }

    static final double[][] EMPTY_DOUBLE_2D_ARRAY = new double[0][];


}
