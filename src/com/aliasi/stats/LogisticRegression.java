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
package com.aliasi.stats;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Matrices;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * A <code>LogisticRegression</code> instance is a multi-class vector
 * classifier model generating conditional probability estimates of
 * categories.  This class also provides static factory methods for
 * estimating multinomial regression models using stochastic gradient
 * descent (SGD) to find maximum likelihood or maximum a posteriori
 * (MAP) estimates with Laplace, Gaussian, Cauchy priors on
 * coefficients.
 *
 * <p>The classification package contains a class {@link
 * com.aliasi.classify.LogisticRegressionClassifier} which adapts this
 * class's models and estimators to act as generic classifiers given
 * an instance of {@link com.aliasi.util.FeatureExtractor}.
 *
 *
 * <table border="0" style="font-size:80%; width:80%; margin:0 1em 0 2em">
 * <tr><td>
 * <b>Also Known As (AKA)</b>
 *
 * <p>Multinomial logistic regression is also known as polytomous,
 * polychotomous, or multi-class logistic regression, or just
 * multilogit regression.
 *
 * <p>Binary logistic regression is an instance of a generalized
 * linear model (GLM) with the logit link function.  The logit
 * function is the inverse of the logistic function, and the logistic
 * function is sometimes called the sigmoid function or the s-curve.
 *
 * <p>Logistic regression estimation obeys the maximum entropy
 * principle, and thus logistic regression is sometimes called
 * &quot;maximum entropy modeling&quot;, and the resulting classifier
 * the &quot;maximum entropy classifier&quot;.
 *
 * <p>The generalization of binomial logistic regression to
 * multinomial logistic regression is sometimes called a softmax or
 * exponential model.
 *
 * <p>Maximum a priori (MAP) estimation with Gaussian priors is often
 * referred to as &quot;ridge regression&quot;; with Laplace priors
 * MAP estimation is known as the &quot;lasso&quot;.  MAP estimation
 * with Gaussian, Laplace or Cauchy priors is known as parameter
 * shrinkage.  Gaussian and Laplace are forms of regularized
 * regression, with the Gaussian version being regularized with the
 * L<sub>2</sub> norm (Euclidean distance, called the Frobenius norm
 * for matrices of parameters) and the Laplace version being
 * regularized with the L<sub>1</sub> norm (taxicab distance or
 * Manhattan metric); other Minkowski metrics may be used for
 * shrinkage.
 *
 * <p>Binary logistic regression is equivalent to a one-layer,
 * single-output neural network with a logistic activation function
 * trained under log loss.  This is sometimes called classification
 * with a single neuron.</td></tr></table>
 *
 * <h4>Model Parameters</h4>
 *
 * A logistic regression model is a discriminitive classifier for
 * vectors of fixed dimensionality.  The dimensions are often referred
 * to as &quot;features&quot;. The method {@link
 * #numInputDimensions()} returns the number of dimensions (features)
 * in the model.  Because the model is well-behaved under sparse
 * vectors, the dimensionality may be returned as {@link
 * Integer#MAX_VALUE}, a common default choice for sparse vectors.

 *
 * <p>A logistic regression model also fixes the number of output
 * categories.  The method {@link #numOutcomes()} returns the number
 * of categories.  These outcome categories will be represented as
 * integers from <code>0</code> to <code>numOutcomes()-1</code>
 * inclusive.
 *
 * <p>A model is parameterized by a real-valued vector for every
 * category other than the last, each of which must be of the same
 * dimensionality as the model's input feature dimensionality.  The
 * constructor {@link #LogisticRegression(Vector[])} takes an array of
 * {@link Vector} objects, which may be dense or sparse, but must all
 * be of the same dimensionality.
 *
 *
 * <h4>Likelihood</h4>
 *
 * <p>The likelihood of a given output category <code>k &lt;
 * numOutcomes()</code> given an input vector <code>x</code> of
 * dimensionality <code>numFeatures()</code> is given by:
 *
 * <blockquote><pre>
 * p(c | x, &beta;) = exp(&beta;<sub>k</sub> * x)  / Z(x)   if c &lt; numOutcomes()-1
 *
 *               1 / Z(x)              if c = numOutcomes()-1</pre></blockquote>
 *
 * where <code>&beta;<sub>k</sub> * x</code> is vector dot (or inner)
 * product:
 *
 * <blockquote><pre>
 * &beta;<sub>k</sub> * x = <big><big><big>&Sigma;</big></big></big><sub>i &lt; numDimensions()</sub> &beta;<sub>k,i</sub> * x<sub>i</sub></pre></blockquote>
 *
 * and where the normalizing denominator, called the partition function,
 * is:
 *
 * <blockquote><pre>
 * Z(x) = 1 + <big><big><big>&Sigma;</big></big></big><sub><sub>k &lt; numOutcomes()-1</sub></sub> exp(&beta;<sub>k</sub> * x)</pre></blockquote>
 *
 *
 * <h4>Error and Gradient</h4>
 *
 * This class computes maximum a posteriori parameter values given a
 * sequence of training pairs <code>(x,c)</code> and a prior, which
 * must be an instance of {@link RegressionPrior}.  The error function
 * is just the negative log likelihood and log prior:
 *
 * <blockquote><pre>
 * Err(D,&beta;) = -<big>(</big> log<sub>2</sub> p(&beta;|&sigma;<sup>2</sup>) + <big><big><big>&Sigma;</big></big></big><sub>{(x,c') in D}</sub> log<sub>2</sub> p(c'|x,&beta;)<big>)</big></pre></blockquote>
 *
 * where <code>p(&beta;|&sigma;<sub>2</sub>)</code> is the likelihood of the parameters
 * <code>&beta;</code> in the prior, and <code>p(c|x,&beta;)</code> is
 * the probability of category <code>c</code> given input <code>x</code>
 * and parameters <code>&beta;</code>.
 *
 * <p>The maximum a posteriori estimate is such that the gradient
 * (vector of partial derivatives of parameters) is zero.  If the data
 * is not linearly separable, a maximum likelihood solution must
 * exist.  If the data is not linearly separable and none of the data
 * dimensions is colinear, the solution will be unique.  If there is
 * an informative Cauchy, Gaussian or Laplace prior, there will be a
 * unique MAP solution even in the face of linear separability or
 * colinear dimensions.  Proofs of solution exists require showing the
 * matrix of second partial derivatives of the error with respect to
 * pairs of parameters, is positive semi-definite; if it is positive
 * definite, the error is strictly concave and the MAP solution is
 * unique.
 *
 * <p>The gradient
 * for parameter vector <code>&beta;<sub>c</sub></code> for
 * outcome <code>c &lt; k-1</code> is:
 *
 * <blockquote><pre>
 * grad(Err(D,&beta;<sub>c</sub>))
 * = &part;Err(D,&beta;) / &part;&beta;<sub>c</sub>
 * = &part;(- log p(&beta;|&sigma;<sup>2</sup>)) / &part;&beta;<sub>c</sub>
 *   + &part;( - <big><big>&Sigma;</big></big><sub>{(x,c') in D}</sub> log p(c' | x, &beta;))</pre></blockquote>
 *
 * where the gradient of the priors are described in the
 * class documentation for {@link RegressionPrior}, and the
 * gradient of the likelihood function is:.
 *
 * <blockquote><pre>
 * &part;(-<big><big>&Sigma;</big></big><sub>{(x,c') in D}</sub> log p(c' | x, &beta;)) / &part;&beta;<sub>c</sub>
 * =  - <big><big>&Sigma;</big></big><sub>{(x,c') in D}</sub> &part; log p(c' | x, &beta;))  /&part;&beta;<sub>c</sub>
 * =  - <big><big>&Sigma;</big></big><sub>{(x,c') in D}</sub> x * (p(c' | x, &beta;) - I(c = c'))</pre></blockquote>
 *
 * where the indicator function <code>I(c=c')</code> is equal to 1 if
 * <code>c=c'</code> and equal to 0 otherwise.
 *
 *
 * <h4>Intercept Term</h4>
 *
 * It is conventional to assume that inputs have their first dimension
 * reserved for the constant <code>1</code>, which makes the
 * parameters <code>&beta;<sub>c,0</sub></code> intercepts.  The priors
 * allow the intercept to be given an uninformative prior even if the
 * other dimensions have informative priors.
 *
 * <h4>Feature Normalization</h4>
 *
 * It is also common to convert inputs to z-scores in logistic
 * regression.  The z-score is computed given the mean and deviation
 * of each dimension.  The problem with centering (subtracting the mean
 * from each value) is that it destroys sparsity.  We recommend not
 * centering and using an intercept term with an uninformative prior.
 *
 * <p>Variance normalization can be achieved by setting the variance
 * prior parameter independently for each dimension.
 *
 *
 * <h4>Non-Linear and Interaction Features</h4>
 *
 * It is common in logistic regression to include derived features
 * which represent non-linear combinations of other input features.
 * Typically, this is done through multiplication.  For instance, if
 * the output is a quadratic function of an input dimension
 * <code>i</code>, then in addition to the raw value
 * <code>x<sub>i</sub></code>, anotehr feature <code>j</code> may be
 * introduced with value <code>x<sub>i</sub><sup>2</sup></code>.
 *
 * <p>Similarly, interaction terms are often added for features
 * <code>x<sub>i</sub></code> and <code>x<sub>j</sub></code>,
 * with a new feature <code>x<sub>k</sub></code> being defined
 * with value <code>x<sub>i</sub> <code>x<sub>j</sub></code>.
 *
 * <p>The resulting model is linear in the derived features, but
 * will no longer be linear in the original features.
 *
 *
 * <h4>Stochastic Gradient Descent</h4>
 *
 * <p>This class estimates logistic regression models using stochastic
 * gradient descent (SGD).  The SGD method runs through the data one
 * or more times, considering one training case at a time, adjusting
 * the parameters along some multiple of the contribution to the gradient
 * of the error for that case.
 *
 * <p>With informative priors, the search space
 * is strictly concave, and there will be a unique solution.  In cases
 * of linear dependence between dimensions or in separable data,
 * maximum likelihood estimation may diverge.
 *
 * <p>The basic algorithm is:
 *
 * <blockquote><pre>
 * &beta; = 0;
 * for (epoch = 0; epoch &lt; maxEpochs; ++epoch)
 *     for training case (x,c') in D
 *         for category c &lt; numOutcomes-1
 *             &beta;<sub>c</sub> -= learningRate(epoch) * grad(Err(x,c,c',&beta;,&sigma;<sup>2</sup>))
 *     if (epoch &gt; minEpochs &amp;&amp; converged)
 *         return &beta;</pre></blockquote>
 *
 * where we discuss the learning rate and convergence conditions
 * in the next section.  The gradient of the error is described
 * above, and the gradient contribution of the prior and its
 * parameters <code>&sigma;</code> are described in the class
 * documentation for {@link RegressionPrior}.  Note that the error
 * gradient must be divided by the number of training cases to
 * get the incremental contribution of the prior gradient.
 *
 * The actual algorithm uses a lazy form of updating the contribution
 * of the gradient of the prior.  The result is an algorithm that
 * handles sparse input data touching only the non-zero dimensions of
 * inputs during parameter updates.
 *
 * <h4>Learning Parameters</h4>
 *
 * In addition to the model parameters (including priors) and training
 * data (input vectors and reference categories), the regression
 * estimation method also requires four parameters that control
 * search.  The simplest search parameters are the minimum and maximum
 * epoch parameters, which control the number of epochs used for
 * optimzation.
 *
 * <p>The argument <code>minImprovement</code> determines how much
 * improvement in training data and model log likelihood under the
 * current model is necessary to go onto the next epoch.  This is
 * measured relatively, with the algorithm stopping when the current
 * epoch's error <code>err</code> is relatively close to the previous
 * epoch's error, <code>errLast</code>:
 *
 * <blockquote><pre>
 * abs(err - errLast)/(abs(err) + abs(errLast)) &lt; minImprovement</pre></blockquote>
 *
 * Setting this to a low value will lead to slow, but accurate
 * coefficient estimates.
 *
 * <p>Finally, the search parameters include an instance of
 * {@link AnnealingSchedule} which impelements the <code>learningRate(epoch)</code>
 * method.  See that method for concrete implementations, including
 * a standard inverse epoch annealing and exponential decay annealing.
 *
 * <h4>Blocked Updates</h4>
 *
 * The implementation of stochastic gradient descent used in this
 * class for fitting a logistic regression model calculates the
 * likelihoods for an entire block of examples at once without
 * changing the model parameters.  The parameters are then updated for
 * the entire block at once.  
 *
 * <p>The last block may be smaller than the others, but it will
 * be treated the same way.  First its classifications are computed,
 * then the gradient updates are made, then the prior updates.
 *
 * <p>Larger block sizes tend to lead to more robust fitting, but
 * may be slower to converge in terms of number of epochs.
 *
 * <p>In fitting models with priors, large block sizes will cause
 * each epoch to run faster because the dense operation of adjusting
 * for priors is performed less frequently.
 *
 * <p>If the block size is set to the corpus size, gradient descent
 * reduces to conjugate gradient descent, although step sizes will
 * still be calculated with the learning rate, not by a line search
 * along the gradient direction.
 *
 *
 * <h4>Serialization and Compilation</h4>
 *
 * For convenience, this class implements both the {@link Serializable}
 * and {@link Compilable} interfaces.  Serializing or compiling
 * a logistic regression model has the same effect.  The model
 * read back in from its serialized state will be an instance of
 * this class, {@link LogisticRegression}.
 *
 * <h4>References</h4>
 *
 * Logistic regression is discussed in most machine learning and
 * statistics textbooks.  These three machine learning textbooks all
 * introduce some form of stochastic gradient descent and logistic
 * regression (often not together, and often under different names as
 * listed in the AKA section above):
 *
 * <ul>
 *
 * <li>MacKay, David. 2003. <a href="http://www.inference.phy.cam.ac.uk/mackay/itprnn/book.html"><i>Information Theory, Inference, and Learning Algorithms</i></a> (includes free download links).
 * Cambridge University Press.
 *
 * <li>Hastie, Trevor, Robert Tibshirani, and Jerome Friedman. 2001.
 * <i><a href="http://www-stat.stanford.edu/~tibs/ElemStatLearn/">Elements of Statistical Learning</a></i>.
 * Springer.</li>
 *
 * <li>Bishop, Christopher M. 2006. <a href="http://research.microsoft.com/~cmbishop/PRML/">Pattern Recognition and Machine Learning</a>.
 * Springer.</li>
 *
 * </ul>
 *
 * An introduction to traditional statistical modeling with logistic
 * regression may be found in:
 *
 * <ul>
 * <li>Gelman, Andrew and Jennnifer Hill.  2007.  <a href="http://www.stat.columbia.edu/~gelman/arm/">Data Analysis Using Regression and Multilevel/Hierarchical Models</a>. Cambridge University Press.
 * </ul>
 *
 * A discussion of text classification using regression that evaluates
 * with respect to support vector machines (SVMs) and considers
 * informative Laplace and Gaussian priors varying by dimension (which
 * this class supports), see:
 *
 * <ul>
 * <li>Genkin, Alexander, David D. Lewis, and David Madigan. 2004.
 * <a href="http://www.stat.columbia.edu/~gelman/stuff_for_blog/madigan.pdf">Large-Scale Bayesian Logistic Regression for Text Categorization</a>.
 * Rutgers University Technical Report.
 * (<a href="http://stat.rutgers.edu/~madigan/PAPERS/techno-06-09-18.pdf">alternate download</a>).
 * </li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @author Mike Ross
 * @version 4.0.1
 * @since   LingPipe3.5
 */
public class LogisticRegression
    implements Compilable, Serializable {

    static final long serialVersionUID = -8585743596322227589L;

    private final Vector[] mWeightVectors;

    /**
     * Construct a multinomial logistic regression model with
     * the specified weight vectors.  With <code>k-1</code>
     * weight vectors, the result is a multinomial classifier
     * with <code>k</code> outcomes.
     *
     * <p>The weight vectors are stored rather than copied, so
     * changes to them will affect this class.
     *
     *<p>See the class definition above for more information on
     *logistic regression.
     *
     * @param weightVectors Weight vectors definining this regression
     * model.
     * @throws IllegalArgumentException If the array of weight vectors
     * does not have at least one element or if there are two weight
     * vectors with different numbers of dimensions.
     */
    public LogisticRegression(Vector[] weightVectors) {
        if (weightVectors.length < 1) {
            String msg = "Require at least one weight vector.";
            throw new IllegalArgumentException(msg);
        }
        int numDimensions = weightVectors[0].numDimensions();
        for (int k = 1; k < weightVectors.length; ++k) {
            if (numDimensions != weightVectors[k].numDimensions()) {
                String msg = "All weight vectors must be same dimensionality."
                    + " Found weightVectors[0].numDimensions()=" + numDimensions
                    + " weightVectors[" + k + "]=" + weightVectors[k].numDimensions();
                throw new IllegalArgumentException(msg);
            }
        }
        mWeightVectors = weightVectors;
    }

    /**
     * Construct a binomial logistic regression model with the
     * specified parameter vector.  See the class definition above
     * for more information on logistic regression.
     *
     * <p>The weight vector is stored rather than copied, so 
     * changes to it will affect this class.
     *
     * @param weightVector The weights of features defining this
     * model.
     */
    public LogisticRegression(Vector weightVector) {
        mWeightVectors = new Vector[] { weightVector };
    }

    /**
     * Returns the dimensionality of inputs for this logistic
     * regression model.
     *
     * @return The number of dimensions for this model.
     */
    public int numInputDimensions() {
        return mWeightVectors[0].numDimensions();
    }

    /**
     * Returns the number of outcomes for this logistic regression
     * model.
     *
     * @return The number of outcomes for this model.
     */
    public int numOutcomes() {
        return mWeightVectors.length + 1;
    }

    /**
     * Returns an array of views of the weight vectors used for this
     * regression model.  The returned weight vectors are immutable
     * views of the underlying vectors used by this model, so will
     * change if the vectors making up this model change.
     *
     * @return An array of views of the weight vectors for this model.
     *
     */
    public Vector[] weightVectors() {
        Vector[] immutables = new Vector[mWeightVectors.length];
        for (int i = 0; i < immutables.length; ++i)
            immutables[i] = Matrices.unmodifiableVector(mWeightVectors[i]);
        return immutables;
    }

    /**
     * Returns an array of conditional probabilities indexed by
     * outcomes for the specified input vector.  The resulting array
     * has a value for index <code>i</code> that is equal to the
     * probability of the outcome <code>i</code> for the specified
     * input.  The sum of the returned values will be 1.0 (modulo
     * arithmetic precision).
     *
     * <p>See the class definition above for more information on
     * how the conditional probabilities are computed.
     *
     * @param x The input vector.
     * @return The array of conditional probabilities of
     * outcomes.
     * @throws IllegalArgumentException If the specified vector is not
     * the same dimensionality as this logistic regression instance.
     */
    public double[] classify(Vector x) {
        // standard softmax without overflow with k-1 prods, kth = 0
        // ugly re-use of ysHat in softmax computation
        double[] ysHat = new double[numOutcomes()];
        classify(x,ysHat);
        return ysHat;
    }   

    /**
     * Fills the specified array with the conditional probabilities
     * indexed by outcomes for the specified input vector.
     *
     * <p>The resulting array has a value for index <code>i</code>
     * that is equal to the probability of the outcome <code>i</code>
     * for the specified input.  The sum of the returned values will
     * be 1.0 (modulo arithmetic precision).  
     *
     * <p>See the class definition above for more information on
     * how the conditional probabilities are computed.
     *
     * @param x The input vector.
     * @param ysHat Array into which conditional probabilities are written.
     * @throws IllegalArgumentException If the specified vector is not
     * the same dimensionality as this logistic regression instance.
     */
    public void classify(Vector x, double[] ysHat) {
        if (numInputDimensions() != x.numDimensions()) {
            String msg = "Vector and classifer must be of same dimensionality."
                + " Regression model this.numInputDimensions()=" + numInputDimensions()
                + " Vector x.numDimensions()=" + x.numDimensions();
            throw new IllegalArgumentException(msg);
        }
        int numOutcomesMinus1 = ysHat.length - 1;
        ysHat[numOutcomesMinus1] = 0.0;
        double max = 0.0;
        for (int k = 0; k < numOutcomesMinus1; ++k) {
            ysHat[k] = x.dotProduct(mWeightVectors[k]);
            if (ysHat[k] > max)
                max = ysHat[k];
        }
        double z = 0.0;
        for (int k = 0; k < ysHat.length; ++k) {
            ysHat[k] = Math.exp(ysHat[k] - max);
            z += ysHat[k];
        }
        for (int k = 0; k < ysHat.length; ++k)
            ysHat[k] /= z;
    }

    /**
     * Compiles this model to the specified object output.  The
     * compiled model, when read back in, will remain an instance of
     * this class, {@link LogisticRegression}.
     *
     * <p>Compilation does the same thing as serialization.
     *
     * @param out Object output to which this model is compiled.
     * @throws IOException If there is an underlying I/O error during
     * serialization.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer(this));
    }

    Object writeReplace() {
        return new Externalizer(this);
    }


    /**
     * Estimate a logistic regression model from the specified input
     * data using the specified Gaussian prior, initial learning rate
     * and annealing rate, the minimum improvement per epoch, the
     * minimum and maximum number of estimation epochs, and a
     * reporter.  The block size defaults to the number of examples
     * divided by 50 (or 1 if the division results in 0).
     *
     * <p>See the class documentation above for more information on
     * logistic regression and the stochastic gradient descent algorithm
     * used to implement this method.
     *
     * <p><b>Reporting:</b> Reports at the debug level provide
     * epoch-by-epoch feedback.  Reports at the info level indicate
     * inputs and major milestones in the algorithm.  Reports at the
     * fatal levels are for thrown exceptions.
     *
     * @param xs Input vectors indexed by training case.
     * @param cs Output categories indexed by training case.
     * @param prior The prior to be used for regression.
     * @param annealingSchedule Class to compute learning rate for each epoch.
     * @param minImprovement The minimum relative improvement in
     * log likelihood for the corpus to continue to another epoch.
     * @param minEpochs Minimum number of epochs.
     * @param maxEpochs Maximum number of epochs.
     * @param reporter Reporter to which progress reports are written, or
     * {@code null} if no progress reports are needed.
     * @throws IllegalArgumentException If the set of input vectors
     * does not contain at least one instance, if the number of output
     * categories isn't the same as the input categories, if two input
     * vectors have different dimensions, or if the prior has a
     * different number of dimensions than the instances.
     */
    public static LogisticRegression
        estimate(Vector[] xs,
                 int[] cs,
                 RegressionPrior prior,

                 AnnealingSchedule annealingSchedule,

                 Reporter reporter,

                 double minImprovement,
                 int minEpochs,
                 int maxEpochs) {

        LogisticRegression hotStart = null;
        ObjectHandler<LogisticRegression> handler = null;
        int rollingAverageSize = 10;
        int blockSize = Math.max(1,cs.length / 50);
        return estimate(xs,cs,
                        prior,
                        blockSize,
                        hotStart,annealingSchedule,minImprovement,rollingAverageSize,minEpochs,maxEpochs,
                        handler,
                        reporter);
    }


    /**
     * Estimate a logistic regression model from the specified input
     * data using the specified Gaussian prior, initial learning rate
     * and annealing rate, the minimum improvement per epoch, the
     * minimum and maximum number of estimation epochs, and a
     * reporter.
     *
     * <p>See the class documentation above for more information on
     * logistic regression and the stochastic gradient descent algorithm
     * used to implement this method.
     *
     * <p><b>Reporting:</b> Reports at the debug level provide
     * epoch-by-epoch feedback.  Reports at the info level indicate
     * inputs and major milestones in the algorithm.  Reports at the
     * fatal levels are for thrown exceptions.
     *
     * @param xs Input vectors indexed by training case.
     * @param cs Output categories indexed by training case.
     * @param prior The prior to be used for regression.
     * @param blockSize Number of examples whose gradient is
     * computed before updating coefficients.
     * @param hotStart Logistic regression from which to retrieve
     * initial weights or null to use zero vectors.
     * @param annealingSchedule Class to compute learning rate for each epoch.
     * @param minImprovement The minimum relative improvement in
     * log likelihood for the corpus to continue to another epoch.
     * @param minEpochs Minimum number of epochs.
     * @param maxEpochs Maximum number of epochs.
     * @param handler Handler for intermediate regression results.
     * @param reporter Reporter to which progress reports are written, or
     * {@code null} if no progress reports are needed.
     * @throws IllegalArgumentException If the set of input vectors
     * does not contain at least one instance, if the number of output
     * categories isn't the same as the input categories, if two input
     * vectors have different dimensions, or if the prior has a
     * different number of dimensions than the instances.
     */
    public static LogisticRegression
        estimate(Vector[] xs,
                 int[] cs,
                 RegressionPrior prior,

                 int blockSize,
                 LogisticRegression hotStart,
                 AnnealingSchedule annealingSchedule,
                 double minImprovement,
                 int rollingAverageSize,
                 int minEpochs,
                 int maxEpochs,

                 ObjectHandler<LogisticRegression> handler,

                 Reporter reporter) {


        if (reporter == null)
            reporter = Reporters.silent();

        reporter.info("Logistic Regression Estimation");

        boolean monitoringConvergence = !Double.isNaN(minImprovement);
        reporter.info("Monitoring convergence=" + monitoringConvergence);
        if (minImprovement < 0.0) {
            String msg = "Min improvement should be Double.NaN to turn off convergence or >= 0.0 otherwise."
                + " Found minImprovement=" + minImprovement;
            throw new IllegalArgumentException(msg);
        }

        if (xs.length < 1) {
            String msg = "Require at least one training instance.";
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);
        }

        if (xs.length != cs.length) {
            String msg = "Require same number of training instances as outcomes."
                + " Found xs.length=" + xs.length
                + " cs.length=" + cs.length;
            reporter.fatal(msg);
            throw new IllegalArgumentException(msg);

        }

        int numTrainingInstances = xs.length;
        int numOutcomesMinus1 = com.aliasi.util.Math.max(cs);
        int numOutcomes = numOutcomesMinus1 + 1;
        int numDimensions = xs[0].numDimensions();

        prior.verifyNumberOfDimensions(numDimensions);

        for (int i = 1; i < xs.length; ++i) {
            if (xs[i].numDimensions() != numDimensions) {
                String msg = "Number of dimensions must match for all input vectors."
                    + " Found xs[0].numDimensions()=" + numDimensions
                    + " xs[" + i + "].numDimensions()=" + xs[i].numDimensions();
                reporter.fatal(msg);
                throw new IllegalArgumentException(msg);
            }
        }


        DenseVector[] weightVectors = new DenseVector[numOutcomesMinus1];
        if (hotStart == null) {
            for (int k = 0; k < numOutcomesMinus1; ++k)
                weightVectors[k] = new DenseVector(numDimensions);  // values all 0.0
        } else {
            Vector[] hotStartWeightVectors = hotStart.weightVectors();
            for (int k = 0; k < weightVectors.length; ++k)
                weightVectors[k] = new DenseVector(hotStartWeightVectors[k]); // deep copy
        }
        LogisticRegression regression = new LogisticRegression(weightVectors);

        boolean hasPrior = (prior != null) && !prior.isUniform();

        reporter.info("Number of dimensions=" + numDimensions);
        reporter.info("Number of Outcomes=" + numOutcomes);
        reporter.info("Number of Parameters=" + (numOutcomes-1)*(long)numDimensions);
        reporter.info("Number of Training Instances=" + cs.length);
        reporter.info("Prior=" + prior);
        reporter.info("Annealing Schedule=" + annealingSchedule);
        reporter.info("Minimum Epochs=" + minEpochs);
        reporter.info("Maximum Epochs=" + maxEpochs);
        reporter.info("Minimum Improvement Per Period=" + minImprovement);
        reporter.info("Has Informative Prior=" + hasPrior);

        double lastLog2LikelihoodAndPrior = -(Double.MAX_VALUE / 2.0);

        double[] rollingAbsDiffs = new double[rollingAverageSize];
        Arrays.fill(rollingAbsDiffs,Double.POSITIVE_INFINITY);
        int rollingAveragePosition = 0;
        double bestLog2LikelihoodAndPrior = Double.NEGATIVE_INFINITY;
        int partialBlockSize = numTrainingInstances % blockSize;
        int numFullBlocks = numTrainingInstances / blockSize;
        double[][] blockCondProbs = new double[blockSize][numOutcomes];

        for (int epoch = 0; epoch < maxEpochs; ++epoch) {
            // copy allows backout by annealing schedule
            DenseVector[] weightVectorCopies 
                = annealingSchedule.allowsRejection()
                ? copy(weightVectors)
                : weightVectors;

            double learningRate = annealingSchedule.learningRate(epoch);

            for (int b = 0; b < numFullBlocks; ++b) {

                adjustBlock(b*blockSize,(b+1)*blockSize,
                            xs,cs,weightVectors,learningRate,prior,
                            blockCondProbs,regression);
                if (reporter.isDebugEnabled())
                    reporter.debug("          epoch " + epoch + " " 
                                   + (int) ((100.0 * (b+1) * blockSize) / numTrainingInstances)
                                   + "% complete");            }
            if (partialBlockSize > 0)
                adjustBlock(numFullBlocks*blockSize,numTrainingInstances,
                            xs,cs,weightVectors,learningRate,prior,
                            blockCondProbs,regression);

            if (handler != null) {
                reporter.debug("handling regression for epoch");
                handler.handle(regression);
            }


            if (!monitoringConvergence) {
                reporter.info("Unmonitored Epoch=" + epoch);
                continue;
            }


            // from here only if monitoring convergence; can't reject unless monitoring!
            
            reporter.debug("computing log likelihood");
            // recompute all estimates; very expensive, but required to monitor convergence
            double log2Likelihood = log2Likelihood(xs,cs,regression);
            double log2Prior = prior.log2Prior(weightVectors);
            double log2LikelihoodAndPrior = log2Likelihood + prior.log2Prior(weightVectors);
            if (log2LikelihoodAndPrior > bestLog2LikelihoodAndPrior)
                bestLog2LikelihoodAndPrior = log2LikelihoodAndPrior;
            
            if (reporter.isInfoEnabled()) {
                Formatter formatter = null;
                try {
                    formatter = new Formatter(Locale.ENGLISH);
                    formatter.format("epoch=%5d lr=%11.9f ll=%11.4f lp=%11.4f llp=%11.4f llp*=%11.4f",
                                     epoch, learningRate,
                                     log2Likelihood,
                                     log2Prior,
                                     log2LikelihoodAndPrior,
                                     bestLog2LikelihoodAndPrior);
                    reporter.info(formatter.toString());
                } catch (IllegalFormatException e) {
                    reporter.warn("Illegal format in Logistic Regression");
                } finally {
                    if (formatter != null)
                        formatter.close();
                }
            }

            boolean acceptUpdate = annealingSchedule.receivedError(epoch,learningRate,-log2LikelihoodAndPrior);
            if (!acceptUpdate) {
                reporter.info("Annealing rejected update at learningRate=" + learningRate + " error=" + (-log2LikelihoodAndPrior));
                weightVectors = weightVectorCopies;
                regression = new LogisticRegression(weightVectors);
            } else {
                double relativeAbsDiff
                    = com.aliasi.util.Math
                    .relativeAbsoluteDifference(lastLog2LikelihoodAndPrior,log2LikelihoodAndPrior);
                    

                rollingAbsDiffs[rollingAveragePosition] = relativeAbsDiff;
                if (++rollingAveragePosition == rollingAbsDiffs.length)
                    rollingAveragePosition = 0;

                double rollingAvgAbsDiff = Statistics.mean(rollingAbsDiffs);
                
                reporter.debug("relativeAbsDiff=" + relativeAbsDiff
                               + " rollingAvg=" + rollingAvgAbsDiff);

                lastLog2LikelihoodAndPrior = log2LikelihoodAndPrior;
                
                if (rollingAvgAbsDiff < minImprovement) {
                    reporter.info("Converged with Rolling Average Absolute Difference="
                                  + rollingAvgAbsDiff);
                    break; // goes to "return regression;"
                }
            }
        }
        return regression;
    }

    /**
     * Returns the log (base 2) likelihood of the specified inputs
     * with the specified categories using the specified regression
     * model.
     *
     * @param inputs Input vectors.
     * @param cats Categories for input vectors.
     * @param regression Model to use for computing likelihood.
     * @throws IllegalArgumentException If the inputs and categories
     * are not the same length.
     */
    public static double log2Likelihood(Vector[] inputs, int[] cats, LogisticRegression regression) {
        if (inputs.length != cats.length) {
            String msg = "Inputs and categories must be same length."
                + " Found inputs.length=" + inputs.length
                + " cats.length=" + cats.length;
            throw new IllegalArgumentException(msg);
        }
        int numTrainingInstances = inputs.length;
        double log2Likelihood = 0.0;
        double[] conditionalProbs = new double[regression.numOutcomes()];
        for (int j = 0; j < numTrainingInstances; ++j) {
            regression.classify(inputs[j],conditionalProbs);
            log2Likelihood += com.aliasi.util.Math.log2(conditionalProbs[cats[j]]);
        }
        return log2Likelihood;
    }



    private static void adjustBlock(int start, int end, 
                                    Vector[] xs, int[] cs,
                                    DenseVector[] weightVectors,
                                    double learningRate,
                                    RegressionPrior prior,
                                    double[][] conditionalProbs,
                                    LogisticRegression regression) {
        for (int j = start; j < end; ++j)
            regression.classify(xs[j],conditionalProbs[j-start]);
        for (int j = start; j < end; ++j)
            for (int k = 0; k < weightVectors.length; ++k)
                adjustWeightsWithConditionalProbs(weightVectors[k],
                                                  conditionalProbs[j-start][k],
                                                  learningRate,
                                                  xs[j],k,cs[j]);
        if (prior != null && !prior.isUniform())
            adjustWeightsWithPrior(weightVectors,prior,
                                   (learningRate*(end-start))/xs.length);
    }

    private static void adjustWeightsWithPrior(DenseVector[] weightVectors,
                                               RegressionPrior prior,
                                               double learningRate) {
        for (int k = 0; k < weightVectors.length; ++k) {
            DenseVector weightVectorsK = weightVectors[k];
            int numDimensions = weightVectorsK.numDimensions();
            for (int i = 0; i < numDimensions; ++i) {
                double weight_k_i = weightVectorsK.value(i);
                double priorMode = prior.mode(i);
                if (weight_k_i == priorMode)
                    continue;
                double priorGradient = prior.gradient(weight_k_i,i);
                double delta = priorGradient * learningRate;
                if (delta == 0.0) 
                    continue;
                double adjWeight_k_i = weight_k_i - delta;
                // arithmetic if non-zero mode
                double mode = prior.mode(i);
                if (weight_k_i > mode) {
                    if (adjWeight_k_i < mode)
                        adjWeight_k_i = mode;
                } else if (adjWeight_k_i > mode) {
                    adjWeight_k_i = mode;
                }
                weightVectorsK.setValue(i,adjWeight_k_i);
            }
        }
    }

    private static void adjustWeightsWithConditionalProbs(DenseVector weightVectorsK, double conditionalProb,
                                                          double learningRate, Vector xsJ, int k, int csJ) {
        double conditionalProbMinusTruth
            = (k == csJ)
            ? (conditionalProb - 1.0)
            : conditionalProb;
        if (conditionalProbMinusTruth == 0.0) return; // could we prune this if close to 0?
        weightVectorsK.increment(-learningRate * conditionalProbMinusTruth,xsJ);
    }



    private static DenseVector[] copy(DenseVector[] xs) {
        DenseVector[] result = new DenseVector[xs.length];
        for (int k = 0; k < xs.length; ++k)
            result[k] = new DenseVector(xs[k]);
        return result;
    }


    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -2256261505231943102L;
        final LogisticRegression mRegression;
        public Externalizer() {
            this(null);
        }
        public Externalizer(LogisticRegression regression) {
            mRegression = regression;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            int numOutcomes = mRegression.mWeightVectors.length + 1;
            out.writeInt(numOutcomes);
            int numDimensions = mRegression.mWeightVectors[0].numDimensions();
            out.writeInt(numDimensions);
            for (int c = 0; c < (numOutcomes - 1); ++c) {
                Vector vC = mRegression.mWeightVectors[c];
                for (int i = 0; i < numDimensions; ++i)
                    out.writeDouble(vC.value(i));
            }
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            int numOutcomes = in.readInt();
            int numDimensions = in.readInt();
            Vector[] weightVectors = new Vector[numOutcomes-1];
            for (int c = 0; c < weightVectors.length; ++c) {
                Vector weightVectorsC = new DenseVector(numDimensions);
                weightVectors[c] = weightVectorsC;
                for (int i = 0; i < numDimensions; ++i)
                    weightVectorsC.setValue(i,in.readDouble());
            }
            return new LogisticRegression(weightVectors);
        }
    }



}
