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

import com.aliasi.matrix.Vector;

import com.aliasi.util.AbstractExternalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>RegressionPrior</code> instance represents a prior
 * distribution on parameters for linear or logistic regression.
 * It has methods to return the log probabilities of input
 * parameters and compute the gradient of the log probability
 * for estimation.
 *
 * <p>Instances of this class are used as parameters in the {@link
 * LogisticRegression} class to control the regularization or lack
 * thereof used by the stochastic gradient descent optimizers.  The
 * priors typically assume a zero mode (maximal value) for each
 * dimension, but allow variances (or scales) to vary by input
 * dimension.  The method {@link #shiftMeans(double[],RegressionPrior)}
 * may be used to shift the means (and hence modes) of priors.
 *
 * <p>The behavior of a prior under stochastic gradient fitting is
 * determined by its gradient, the partial derivatives with respect to
 * the dimensions of the error function for the prior (negative log
 * likelihood) with respect to a coefficient
 * <code>&beta;<sub>i</sub></code>.
 *
 * <blockquote><pre>
 * gradient(&beta;,i) = - &part; log p(&beta;) / &part; &beta;<sub>i</sub></pre></blockquote>
 *
 * <p>See the class documentation for {@link LogisticRegression}
 * for more information.
 *
 * <p>Priors also implement a log (base 2) probability density for the
 * prior for a given parameter in a given dimension.  The total log
 * prior probability is defined as the sum of the log probabilities
 * for the dimensions,
 *
 * <blockquote><pre>
 * log p(&beta;) = <big>&Sigma;</big><sub>i</sub> log p(&beta;<sub>i</sub>)</pre></blockquote>
 *
 * <p>Priors affect gradient descent fitting of regression through
 * their contribution to the gradient of the error function with
 * respect to the parameter vector.  The contribution of the prior to
 * the error function is the negative log probability of the parameter
 * vector(s) with respect to the prior distribution.  The gradient of
 * the error function is the collection of partial derivatives of the
 * error function with respect to the components of the parameter
 * vector.  The regression prior abstract base class is defined in
 * terms of a single method {@link #gradient(double,int)}, which
 * specifies the value of the gradient of the error function for a
 * specified dimension with a specified value in that dimension.
 *
 * <p>This class implements static factory methods to construct
 * noninformative, Gaussian and Laplace priors.  The Gaussian and
 * Laplace priors may specify a different variance for each dimension,
 * but assumes all the prior means (which are equivalent to the modes)
 * are zero.  The priors also assume the dimensions are independent so
 * that the full covariance matrix is assumed to be diagonal (that is,
 * there is zero covariance between different dimensions).
 *
 *
 * <h4>Noninformative Prior &amp; Maximum Likelihood Estimation</h4>
 *
 * <p>Using a noninformative prior for regression results in standard
 * maximum likelihood estimation.
 *
 * <p>The noninformative prior assumes an improper uniform
 * distribution over parameter vectors:
 *
 * <blockquote><pre>
 * p(&beta;<sub>i</sub>) = Uniform(&beta;<sub>i</sub>) = constant</pre></blockquote>
 *
 * and thus the log probabiilty is constant
 *
 * <blockquote><pre>
 * log p(&beta;<sub>i</sub>) = log constant</pre></blockquote>
 * 
 * and therefore contributes nothing to the gradient:
 *
 * <blockquote><pre>
 * gradient(&beta;,i) =  0.0</pre></blockquote>
 *
 * A noninformative prior is constructed using the static method
 * {@link #noninformative()}.
 *
 *
 * <h4>Gaussian Prior, L<sub>2</sub> Regularization &amp; Ridge Regression</h4>
 *
 * <p>The Gaussian prior assumes a Gaussian (also known as normal) density over
 * parameter vectors which results in L<sub>2</sub>-regularized
 * regression, also known as ridge regression.  Specifically, the
 * prior allows a variance to be specified per dimension, but
 * assumes dimensions are independent in that all off-diagonal
 * covariances are zero.  The Gaussian prior has a single mode that
 * is the same as its mean.
 *
 * <p>The Gaussian density with variance is defined by:
 *
 * <blockquote><pre>
 * p(&beta;<sub>i</sub>) = 1.0/sqrt(2 * &pi; &sigma;<sub>i</sub><sup>2</sup>) * exp(-&beta;<sub>i</sub><sup>2</sup>/(2 * &sigma;<sub>i</sub><sup>2</sup>))</pre></blockquote>
 *
 * which on a log scale is
 *
 * <blockquote><pre>
 * log p(&beta;<sub>i</sub>) = log (1.0/sqrt(2 * &pi; * &sigma;<sub>i</sub><sup>2</sup>)) + -&beta;<sub>i</sub><sup>2</sup>/(2 * &sigma;<sub>i</sub><sup>2</sup>)</pre></blockquote>
 * 
 * <p>The Gaussian prior leads to the following contribution to the
 * gradient for a dimension <code>i</code> with parameter
 * <code>&beta;<sub>i</sub></code> and variance
 * <code>&sigma;<sub>i</sub><sup>2</sup></code>:
 *
 * <blockquote><pre>
 * gradient(&beta;,i) = &beta;<sub>i</sub>/&sigma;<sub>i</sub><sup>2</sup></pre></blockquote>
 *
 * As usual, the lower the variance, the steeper the gradient, and the stronger
 * the effect on the (maximum) a posteriori estimate.
 *
 * <p>Gaussian priors are constructed using one of the static factory
 * methods, {@link #gaussian(double[])} or {@link
 * #gaussian(double,boolean)}.
 *
 * <h4>Laplace Prior, L<sub>1</sub> Regularization &amp; the Lasso</h4>
 *
 * <p>The Laplace prior assumes a Laplace density over parameter
 * vectors which results in L<sub>1</sub>-regularized regression, also
 * known as the lasso.  The Laplace prior is called a
 * double-exponential distribution because it is looks like an
 * exponential distribution for positive values and the reflection of
 * this exponential distribution around zero (or more generally,
 * around its mean parameter).  The Laplace prior has the mode in
 * the same location as the mean.
 *
 * <p>A Laplace prior allows a variance to be specified per dimension,
 * but like the Gaussian prior, assumes means are zero and that the
 * dimensions are independent in that all off-diagonal covariances are
 * zero.
 *
 * <p>The Laplace density is defined by:
 *
 * <blockquote><pre>
 * p(&beta;<sub>i</sub>) = (sqrt(2)/(2 * &sigma;<sub>i</sub>)) * exp(- sqrt(2) * abs(&beta;<sub>i</sub>) / &sigma;<sub>i</sub>)</pre></blockquote>
 *
 * which on the log scale is
 * 
 * <blockquote><pre>
 * log p(&beta;<sub>i</sub>) = log (sqrt(2)/(2 * &sigma;<sub>i</sub>)) - sqrt(2) * abs(&beta;<sub>i</sub>) / &sigma;<sub>i</sub></pre></blockquote>

</pre></blockquote>
 * <p>The Laplace prior leads to the following contribution to the
 * gradient for a dimension <code>i</code> with parameter
 * <code>beta<sub>i</sub></code>, mean zero and variance
 * <code>&sigma;<sub>i</sub><sup>2</sup></code>:
 *
 * <blockquote><pre>
 * gradient(&beta;,i) = sqrt(2) * signum(&beta;<sub>i</sub>) / &sigma;<sub>i</sub></pre></blockquote>
 *
 * where the derivative of the absolute value function is the <code>signum</code> function, as defined by {@link Math#signum(double)}.
 *
 * <blockquote><pre>
 * signum(x) = x > 0 ? 1 : (x < 0 ? -1 : 0)</pre></blockquote>
 *
 * <p>Laplace priors are constructed using one of the static factory
 * methods, {@link #laplace(double[])} or {@link
 * #laplace(double,boolean)}.
 *
 *
 * <h4>Cauchy Prior</h4>
 *
 * <p>The Cauchy prior assumes a Cauchy density (also known as a
 * Lorentz density) over priors.  The Cauchy density allows a scale to
 * be specified for each dimension.  The mean and variance are
 * undefined as their integrals diverge.  The Cauchy distribution is
 * symmetric and for regression priors, we assume a mode of zero for
 * the base distribution.  The Cauchy prior also has a single mode
 * at its mean.
 *
 * <p>The Cauchy density with scale of 1
 * is a Student-t density with one degree of freedom.   
 *
 * <p>The Cauchy density is defined by:
 *
 * <blockquote><pre>
 * p(&beta;<sub>i</sub>,i) = (1 / &pi;) * (&lambda;<sub>i</sub> / (&beta;<sub>i</sub><sup>2</sup> + &lambda;<sub>i</sub><sup>2</sup>))</pre></blockquote>
 * which on a log scale is
 *
 * <blockquote><pre>
 * log p(&beta;<sub>i</sub>,i) = log (1 / &pi;) + log (&lambda;<sub>i</sub>) - log (&beta;<sub>i</sub><sup>2</sup> + &lambda;<sub>i</sub><sup>2</sup>)</pre></blockquote>
 * 
 * <p>The Cauchy prior leads to the following contribution to the
 * gradient for dimension <code>i</code> with parameter <code>&beta;<sub>i</sub></code> and scale
 * <code>&lambda;<sub>i</sub><sup>2</sup></code>:
 *
 * <blockquote><pre>
 * gradient(&beta;<sub>i</sub>, i) = 2 &beta;<sub>i</sub> / (&beta;<sub>i</sub><sup>2</sup> + &lambda;<sub>i</sub><sup>2</sup>)</pre></blockquote>
 *
 * <p>Cauchy priors are constructed using one of the static factory
 * methods {@link #cauchy(double[])} or {@link #cauchy(double,boolean)}.
 * 

 * <h4>Log Interpolated Priors</h4>
 *
 * <p>For use in gradient-based algorithms, the gradients of two
 * different priors may be interpolated.  A special case is the
 * elastic net, discussed in he next section.  Given two priors
 * <code>p1</code> and <code>p2</code>, and an interpolation ratio
 * <code>&alpha;</code> between 0 and 1, the interpolated prior is
 * defined by 
 *
 * <blockquote><pre>
 * log p(&beta;<sub>i</sub>) = &alpha; * log p1(&beta;<sub>i</sub>) + (1 - &alpha;) * log p2(&beta;<sub>i</sub>) - Z</pre></blockquote>
 *
 * where <code>Z</code> is the normalization constant not depending on
 * <code>&beta;</code> that normalizes the density,
 *
 * <blockquote><pre>
 * p(&beta;,i) = exp(log p(&beta;<sub>i</sub>))
 *
 *        = exp(&alpha; * log p1(&beta;<sub>i</sub>)) * exp((1 - &alpha;) * log p2(&beta;<sub>i</sub>)) / exp(Z)
 *
 *        = p1(&beta;,i)<sup>&alpha;</sup> * p2(&beta;,1)<sup>(1 - &alpha;)</sup> / exp(Z)</pre></blockquote>
 * 
 * <p>The gradient, being a derivative, will be the weighted sum of the
 * underlying gradients <code>gradient1</code> and <code>gradient2</code>,
 *
 * <blockquote><pre>
 * gradient(&beta;,i) = &alpha; * gradient1(&beta;,i) + (1 - &alpha;) * gradient2(&beta;,i)</pre></blockquote>
 * 
 *
 * <h4>Elastic Net Prior</h4>
 *
 * The elastic net prior interpolates between a Laplace prior and a
 * Gaussian prior on the log scale uniformly for all dimensions.  
 *
 * There are two parameters, a scale parameter for the prior variances
 * and an interpolation parameter that determines the weight given to
 * the Laplace prior versus the Gaussian prior.  The elastic net prior
 * with Laplace weight <code>&alpha;</code> and scale
 * <code>&lambda;</code> is defined by
 *
 * <blockquote><pre>
 * log p(&beta;,i) = &alpha; * log Laplace(&beta;<sub>i</sub>|1/sqrt(&lambda;)) + (1 - &alpha;) Gaussian(&beta;<sub>i</sub>|sqrt(2)/&lambda;)</pre></blockquote>
 *
 * where <code>Laplace(&beta;<sub>i</sub>|1/sqrt(&lambda;))</code> is
 * the density of the (zero-mean) Laplace distribution with variance
 * <code>1/sqrt(&lambda;)</code>, and
 * <code>Gaussian(&beta;<sub>i</sub>|sqrt(2)/&lambda;)</code> is the
 * (zero-mean) Gaussian density function with variance
 * <code>sqrt(2)/&lambda;</code>.

 + (1 - &alpha;) Gaussian(&beta;<sub>i</sub>|sqrt(2)/&lambda;)</pre></blockquote>
 * 
 * <p>Thus the gradient is an interpolation of the gradients of the
 * Laplace with variance <code>&sigma;<sup>2</sup> = 1/sqrt(&lambda;)</code> and
 * Gaussian with variance <code>&sigma;<sup>2</sup> = sqrt(2)/&lambda;</code>,
 * leading to a simple gradient form,
 *
 * <blockquote><pre>
 * gradient(&beta;,i) = &alpha; * &lambda; * signum(&beta;<sub>i</sub>) + (1 - &alpha;) * &lambda; * &beta;<sub>i</sub></pre></blockquote>
 *
 * <p>The basic elastic net prior has zero means and modes in all'
 * dimensions, but may be shifted like other priors.
 *
 * <h4>Non-Zero Means and Modes</h4>
 *
 * <p>Priors with non-zero means or modes typically arise in
 * hierarchical or multilevel regression models or models in which
 * infomative priors are available on a dimension-by-dimension basis.
 * 
 * <p>Through the method {@link #shiftMeans(double[],RegressionPrior)}
 * it is possible to shift the means of a prior by the specified
 * amount.  This allows any prior to be used with non-zero means.
 * Probabilities are computed by shifting back.  Suppose
 * <code>p2</code> is the density and <code>gradient2</code> the
 * gradient of the specified prior and <code>shifts</code> the
 * specified array of floats specifying the mean shifts.
 * Probabilities and gradients are computed by shifting back,
 *
 * <blockquote><pre>
 * p(&beta;) = p2(&beta; - shifts)</pre></blockquote>
 *
 * and
 *
 * <blockquote><pre>
 * gradient(&beta;,i) = gradient2(&beta; - shifts,i)</pre></blockquote>
 * 
 * Dimension by dimension, the value is computed by subtracting
 * the shift from the value and plugging it into the underlying
 * prior.
 *
 * <p>For example, to specify a Gaussian prior with means
 * <code>mus</code> and variances <code>vars</code>, use
 *
 * <blockquote><pre>
 * double[] mus = ...
 * double[] vars = ...
 * RegressionPrior prior = shiftMeans(mus,gaussian(vars))</pre></blockquote>
 *
 *
 * <h4>Special Treatment of Intercept</h4>
 *
 * <p>By convention, input dimension zero (<code>0</code>) may be
 * reserved for the intercept and set to value 1.0 in all input
 * vectors.  For regularized regression, the regularization is
 * typically not applied to the intercept term.  To match this
 * convention, the factory methods allow a boolean parameter
 * indicating whether the intercept parameter has a
 * noninformative/uniform prior.  If the intercept flag indicates it
 * is noninformative, then dimension 0 will not have an infinite
 * prior variance or scale, and hence a zero gradient.  The result is
 * that the intercept will be fit by maximum likelihood.
 *
 * <h4>Serialization</h4>
 *
 * <p>All of the regression priors may be serialized.

 * <h4>References</h4>
 *
 * <p>For full details on the Gaussian, cauchy, and Laplace distributions,
 * see:
 *
 * <ul>
 * <li>Wikipedia: <a href="http://en.wikipedia.org/wiki/Normal_distribution">Normal (Gaussian) Distribution</a></li>
 * <li>Wikipedia: <a href="http://en.wikipedia.org/wiki/Laplace_distribution">Laplace (Double Exponential) Distribution</a></li>
 * <li>Wikipedia: <a href="http://en.wikipedia.org/wiki/Cauchy_distribution">Cauchy Distribution</a>
 * </ul>
 *
 * <p>For explanations of how the priors are used with regression
 * including logistic regression, see the following three textbooks:
 *
 * <ul>
 * <li>Hill, Jennifer and Andrew Gelman. 2006. <i>Data Analysis Using Regression and Multilevel/Hierarchical Models</i>.  Cambridge University Press.
 * <li>Hastie, Trevor, Tibshirani, Robert and Jerome Friedman. 2001.
 * <i><a href="http://www-stat.stanford.edu/~tibs/ElemStatLearn/">Elements of Statistical Learning</a></i>.
 * Springer.</li>
 *
 * <li>Bishop, Christopher M. 2006. <a href="http://research.microsoft.com/~cmbishop/PRML/">Pattern Recognition and Machine Learning</a>.
 * Springer.</li>
 * </ul>
 *
 * and for non-zero means and gradient calculations, the tech reports:
 *
 * <ul>
 * <li>Genkin, Alexander, David D. Lewis, and David Madigan. 2004.
 * <a href="http://www.stat.columbia.edu/~gelman/stuff_for_blog/madigan.pdf">Large-Scale Bayesian Logistic Regression for Text Categorization</a>.
 * Rutgers University Technical Report.
 * (<a href="http://stat.rutgers.edu/~madigan/PAPERS/techno-06-09-18.pdf">alternate download</a>).
 * <li>
 * Carpenter, Bob. 2008. <a href="http://lingpipe.files.wordpress.com/2008/04/lazysgdregression.pdf">Lazy Sparse Stochastic Gradient Descent for Regularized Multinomial Logistic Regression</a>. Technical Report. Alias-i.
 * </ul>
 *
 * For a decription and evaluation of the Cauchy prior, see
 * 
 * <ul>
 * <li>
 Gelman, Andrew, Aleks Jakulin, Yu-Sung Su, and Maria Grazia Pittau. 2007.
<a href="http://ssrn.com/abstract=1010421">A Default Prior Distribution for Logistic and Other Regression Models</a>.
* </li>
 * </ul>
 *
 * <p>For details of the elastic net prior, see
 *
 * <ul>
 * <li>
 * Zou and Hastie. 2005. <a href="http://www-stat.stanford.edu/~hastie/Papers/elasticnet.pdf">Regularization and variable selection via the elastic net</a>. <i>Journal of the Royal Statistical Society, Series B</i>.
 * <li>
 * Friedman, Hastie and Tibshirani. 2010. <a href="http://www-stat.stanford.edu/~hastie/Papers/glmnet.pdf">Regularization paths for generalized linear models via coordinate descent</a>.  <i>Journal of Statistical Software</i> <b>33</b>:1.
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.9.2
 * @since   LingPipe3.5
 */
public abstract class RegressionPrior implements Serializable {

    static final long serialVersionUID = 2955531646832969891L;

    // do not allow instances or subclasses
    private RegressionPrior() { 
        /* empty constructor */
    }

    /**
     * Returns {@code true} if this prior is the uniform distribution.
     * Uniform priors reduce to maximum likelihood calculations.
     *
     * @return {@code true} if this prior is the uniform distribution.
     */
    public boolean isUniform() {
        return false;
    }

    /**
     * Returns the mode of the prior.   The mode is used to clip
     * gradient steps of the prior so they do not pass through the
     * mode.
     *
     * @param dimension Dimension position in vector.
     * @return The mean of the prior for the specified dimension.
     */
    public double mode(int dimension) {
        return 0.0;
    }

    /**
     * Returns the contribution to the gradient of the error function
     * of the specified parameter value for the specified dimension.
     *
     * @param betaForDimension Parameter value for the specified dimension.
     * @param dimension The dimension.
     * @return The contribution to the gradient of the error function
     * of the parameter value and dimension.
     */
    public abstract double gradient(double betaForDimension, int dimension);

    /**
     * Returns the log (base 2) of the prior density evaluated at the
     * specified coefficient value for the specified dimension (up to
     * an additive constant).  The overall error function is the sum of
     * the negative log likelihood of the data under the model and the
     * negative log of the prior.
     *
     * @param betaForDimension Parameter value for the specified dimension.
     * @param dimension The dimension.
     * @return The prior probability of the specified parameter value
     * for the specified dimension.
     */
    public abstract double log2Prior(double betaForDimension, int dimension);


    /**
     * Returns the log (base 2) prior density for a specified
     * coefficient vector (up to an additive constant).
     *
     * @param beta Parameter vector.
     * @return The log (base 2) prior for the specified parameter
     * vector.
     * @throws IllegalArgumentException If the specified parameter
     * vector does not match the dimensionality of the prior (if
     * specified).
     */
    public double log2Prior(Vector beta) {
        int numDimensions = beta.numDimensions();
        verifyNumberOfDimensions(numDimensions);
        double log2Prior = 0.0;
        for (int i = 0; i < numDimensions; ++i)
            log2Prior += log2Prior(beta.value(i),i);
        return log2Prior;
    }

    /**
     * Returns the log (base 2) prior density for the specified
     * array of coefficient vectors (up to an additive constant).
     *
     * @param betas The parameter vectors.
     * @return The log (base 2) prior density for the specified
     * @throws IllegalArgumentException If any of the specified
     * parameter vectors does not match the dimensionality of the
     * prior (if specified).
     */
    public double log2Prior(Vector[] betas) {
        double log2Prior = 0.0;
        for (Vector beta : betas)
            log2Prior += log2Prior(beta);
        return log2Prior;
    }

    // package local on purpose
    void verifyNumberOfDimensions(int ignoreMeNumDimensions) {
        // do nothing on purpose
    }


    static final double SQRT_2 = Math.sqrt(2.0);

    private final static RegressionPrior NONINFORMATIVE_PRIOR
        = new NoninformativeRegressionPrior();


    /**
     * Returns the noninformative or uniform prior to use for
     * maximum likelihood regression fitting.
     *
     * @return The noninformative prior.
     */
    public static RegressionPrior noninformative() {
        return NONINFORMATIVE_PRIOR;
    }


    /**
     * Returns the Gaussian prior with the specified prior variance
     * and indication of whether the intercept is given a
     * noninformative prior.
     *
     * <p>If the noninformative-intercept flag is set to
     * <code>true</code>, the prior variance for dimension zero
     * (<code>0</code>) is set to {@link Double#POSITIVE_INFINITY}.
     *
     * <p>See the class documentation above for more inforamtion on
     * Gaussian priors.
     *
     * @param priorVariance Variance of the Gaussian prior for each
     * dimension.
     * @param noninformativeIntercept Flag indicating if intercept is
     * given a noninformative (uniform) prior.
     * @return The Gaussian prior with the specified parameters.
     * @throws IllegalArgumentException If the prior variance is not
     * a non-negative number.
     */
    public static RegressionPrior gaussian(double priorVariance,
                                           boolean noninformativeIntercept) {
        verifyPriorVariance(priorVariance);
        return new VariableGaussianRegressionPrior(priorVariance,noninformativeIntercept);
    }


    /**
     * Returns the Gaussian prior with the specified priors for
     * each dimension.  The number of dimensions is taken to be
     * the length of the variance array.
     *
     * <p>See the class documentation above for more inforamtion on
     * Gaussian priors.
     *
     * @param priorVariances Array of prior variances for dimensions.
     * @return The Gaussian prior with the specified variances.
     * @throws IllegalArgumentException If any of the variances are not
     * non-negative numbers.
     *
     */
    public static RegressionPrior gaussian(double[] priorVariances) {
        verifyPriorVariances(priorVariances);
        return new GaussianRegressionPrior(priorVariances);
    }



    /**
     * Returns the Laplace prior with the specified prior variance
     * and number of dimensions and indication of whether the
     * intecept dimension is given a noninformative prior.
     *
     * <p>If the noninformative-intercept flag is set to
     * <code>true</code>, the prior variance for dimension zero
     * (<code>0</code>) is set to {@link Double#POSITIVE_INFINITY}.
     *
     * <p>See the class documentation above for more inforamtion on
     * Laplace priors.
     *
     * @param priorVariance Variance of the Laplace prior for each
     * dimension.
     * @param noninformativeIntercept Flag indicating if intercept is
     * given a noninformative (uniform) prior.
     * @return The Laplace prior with the specified parameters.
     * @throws IllegalArgumentException If the variance is not a non-negative
     * number.
     */
    public static RegressionPrior laplace(double priorVariance,
                                          boolean noninformativeIntercept) {
        verifyPriorVariance(priorVariance);
        return new VariableLaplaceRegressionPrior(priorVariance,noninformativeIntercept);
    }


    /**
     * Returns the Laplace prior with the specified prior variances
     * for the dimensions.
     *
     * <p>See the class documentation above for more inforamtion on
     * Laplace priors.
     *
     * @param priorVariances Array of prior variances for dimensions.
     * @return The Laplace prior for the specified variances.
     * @throws IllegalArgumentException If any of the variances is not
     * a non-negative number.
     */
    public static RegressionPrior laplace(double[] priorVariances) {
        verifyPriorVariances(priorVariances);
        return new LaplaceRegressionPrior(priorVariances);
    }


    /**
     * Returns the Cauchy prior with the specified prior squared
     * scales for the dimensions.
     *
     * <p>See the class documentation above for more information
     * on Cauchy priors.
     *
     * @param priorSquaredScale The square of the prior scae parameter.
     * @param noninformativeIntercept Flag indicating if intercept is
     * given a noninformative (uniform) prior.
     * @return The Cauchy prior for the specified squared scale and
     * intercept flag.
     * @throws IllegalArgumentException If the scale is not a non-negative
     * number.
     */
    public static RegressionPrior cauchy(double priorSquaredScale,
                                         boolean noninformativeIntercept) {
        verifyPriorVariance(priorSquaredScale);
        return new VariableCauchyRegressionPrior(priorSquaredScale,noninformativeIntercept);
    }

    /**
     * Returns the Cauchy prior for the specified squared scales.
     *
     * <p>See the class documentation above for more information
     * on Cauchy priors.
     *
     * @param priorSquaredScales Prior squared scale parameters.
     * @return The Cauchy prior for the specified square scales.
     * @throws IllegalArgumentException If any of the prior squared
     * scales is not a non-negative number.
     */
    public static RegressionPrior cauchy(double[] priorSquaredScales) {
        verifyPriorVariances(priorSquaredScales);
        return new CauchyRegressionPrior(priorSquaredScales);
    }

    /**
     * Returns the prior that interpolates its log probability between 
     * the specified priors with the weight going to the first prior.
     *
     * <p>See the class documentaton above for more information on
     * Cauchy priors.
     *
     * @param alpha Weight of first prior.
     * @param prior1 First prior for interpolation.
     * @param prior2 Second prior for interpolation.
     * @return The interpolated prior.
     * @throws IllegalArgumentException If the interpolation ratio is
     * not a number between 0 and 1 inclusive.
     */
    public static RegressionPrior logInterpolated(double alpha,
                                                  RegressionPrior prior1,
                                                  RegressionPrior prior2) {
        if (Double.isNaN(alpha) || alpha < 0.0 || alpha > 1.0) {
            String msg = "Weight of first prior must be between 0 and 1 inclusive."
                + " Found alpha=" + alpha;
            throw new IllegalArgumentException(msg);
                
        }
        return new LogInterpolatedRegressionPrior(alpha,prior1,prior2);
    }

    /**
     * Returns the elastic net prior with the specified weight on 
     * the Laplace prior, the specified scale parameter for the elastic
     * net and a noninformative prior on the intercept (dimension 0) if
     * the specified flag is set.
     *
     * <p>See the class documentation above for more information on
     * elastic net priors.
     *
     * <p>This is a convenience method for 
     * 
     * <blockquote><pre>
     * logInterpolated(laplaceWeight,
     *                 laplace(1/sqrt(scale),noninformativeIntercept),
     *                 gaussian(sqrt(2)/scale,noninformativeIntercept))
     * </pre></blockquote>
     *
     * @param laplaceWeight Weight on the Laplace prior.
     * @param scale Scale parameter for the elastic net.
     * @param noninformativeIntercept A flag indicating whether or not
     * the intercept (dimension 0) should have a noninformative prior.
     * @return The elastic net prior with the specified paramters.
     * @throws IllegalArgumentException If the interpolation parameter
     * is not between 0 and 1 inclusive, and if the scale is not
     * positive and finite.
     */
    public static RegressionPrior elasticNet(double laplaceWeight,
                                             double scale,
                                             boolean noninformativeIntercept) {
        if (Double.isInfinite(scale) || !(scale > 0.0)) {
            String msg = "Scale parameter must be finite and positive."
                + " Found scale=" + scale;
            throw new IllegalArgumentException(msg);
        }
        return logInterpolated(laplaceWeight,
                               laplace(1.0/Math.sqrt(scale),noninformativeIntercept),
                               gaussian(SQRT_2/scale,noninformativeIntercept));
    }

    /**
     * Returns the prior that shifts the means of the specified prior
     * by the specified values.  
     *
     * <p>See the class documentation above for more information.
     *
     * @param shifts Mean shifts indexed by dimension.
     * @param prior Prior to apply to shifted values.
     * @return Prior that shifts values before delegating to the
     * specified prior.
     */
    public static RegressionPrior shiftMeans(double[] shifts,
                                             RegressionPrior prior) {
        return new ShiftMeans(shifts,prior);
    }
                                             


    static void verifyPriorVariance(double priorVariance) {
        if (priorVariance < 0
            || Double.isNaN(priorVariance)
            || priorVariance == Double.NEGATIVE_INFINITY) {

            String msg = "Prior variance must be a non-negative number."
                + " Found priorVariance=" + priorVariance;
            throw new IllegalArgumentException(msg);
        }
    }

    static void verifyPriorVariances(double[] priorVariances) {
        for (int i = 0; i < priorVariances.length; ++i) {

            if (priorVariances[i] < 0
                || Double.isNaN(priorVariances[i])
                || priorVariances[i] == Double.NEGATIVE_INFINITY) {

                String msg = "Prior variances must be non-negative numbers."
                    + " Found priorVariances[" + i + "]=" + priorVariances[i];
                throw new IllegalArgumentException(msg);
            }
        }
    }

    static class NoninformativeRegressionPrior
        extends RegressionPrior
        implements Serializable {

        static final long serialVersionUID = -582012445093979284L;

        @Override
        public double gradient(double beta, int dimension) {
            return 0.0;
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            return 0.0;  // log2(1) = 0
        }
        @Override
        public double log2Prior(Vector beta) {
            return 0.0;
        }
        @Override
        public double log2Prior(Vector[] betas) {
            return 0.0;
        }
        @Override
        public String toString() {
            return "NoninformativeRegressionPrior";
        }
        @Override
        public boolean isUniform() { 
            return true; 
        }

    }

    static abstract class ArrayRegressionPrior extends RegressionPrior {
        static final long serialVersionUID = -1887383164794837169L;
        final double[] mValues;
        ArrayRegressionPrior(double[] values) {
            mValues = values;
        }
        @Override
        void verifyNumberOfDimensions(int numDimensions) {
            if (mValues.length != numDimensions) {
                String msg = "Prior and instances must match in number of dimensions."
                    + " Found prior numDimensions=" + mValues.length
                    + " instance numDimensions=" + numDimensions;
                throw new IllegalArgumentException(msg);
            }
        }
        public String toString(String priorName, String paramName) {
            StringBuilder sb = new StringBuilder();
            sb.append(priorName + "\n");
            sb.append("     dimensionality=" + mValues.length);
            for (int i = 0; i < mValues.length; ++i)
                sb.append("     " + paramName + "[" + i + "]=" + mValues[i] + "\n");
            return sb.toString();
        }
    }

    static class GaussianRegressionPrior
        extends ArrayRegressionPrior
        implements Serializable {

        static final long serialVersionUID = 8257747607648390037L;

        GaussianRegressionPrior(double[] priorVariances) {
            super(priorVariances);
        }
        @Override
        public double gradient(double beta, int dimension) {
            return beta / mValues[dimension];
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            return -log2Sqrt2Pi
                - 0.5 * com.aliasi.util.Math.log2(mValues[dimension])
                - beta * beta / (2.0 * mValues[dimension]);
        }
        @Override
        public String toString() {
            return toString("GaussianRegressionPrior","Variance");
        }
        private Object writeReplace() {
            return new Serializer(this);
        }
        private static class Serializer extends AbstractExternalizable {

            static final long serialVersionUID = -1129377549371296060L;

            final GaussianRegressionPrior mPrior;
            public Serializer(GaussianRegressionPrior prior) {
                mPrior = prior;
            }
            public Serializer() {
                this(null);
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(mPrior.mValues.length);
                for (int i = 0; i < mPrior.mValues.length; ++i)
                    out.writeDouble(mPrior.mValues[i]);
            }
            @Override
            public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
                int numDimensions = in.readInt();
                double[] priorVariances = new double[numDimensions];
                for (int i = 0; i < numDimensions; ++i)
                    priorVariances[i] = in.readDouble();
                return new GaussianRegressionPrior(priorVariances);
            }
        }
    }

    static final double sqrt2 = Math.sqrt(2.0);
    static final double log2Sqrt2Over2 = com.aliasi.util.Math.log2(sqrt2/2.0);
    static final double log2Sqrt2Pi
        = com.aliasi.util.Math.log2(Math.sqrt(2.0 * Math.PI));
    static final double log21OverPi = -com.aliasi.util.Math.log2(Math.PI);

    static class LaplaceRegressionPrior
        extends ArrayRegressionPrior
        implements Serializable {

        static final long serialVersionUID = 9120480132502062861L;

        LaplaceRegressionPrior(double[] priorVariances) {
            super(priorVariances);
        }
        @Override
        public double gradient(double beta, int dimension) {
            if (beta == 0.0) return 0.0;
            if (beta > 0)
                return Math.sqrt(2.0/mValues[dimension]);
            return -Math.sqrt(2.0/mValues[dimension]);
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            return log2Sqrt2Over2
                - 0.5 * com.aliasi.util.Math.log2(mValues[dimension])
                - sqrt2 * Math.abs(beta) / Math.sqrt(mValues[dimension]);
        }
        @Override
        public String toString() {
            return toString("LaplaceRegressionPrior","Variance");
        }
        private Object writeReplace() {
            return new Serializer(this);
        }
        private static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = 7844951573062416091L;
            final LaplaceRegressionPrior mPrior;
            public Serializer(LaplaceRegressionPrior prior) {
                mPrior = prior;
            }
            public Serializer() {
                this(null);
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(mPrior.mValues.length);
                for (int i = 0; i < mPrior.mValues.length; ++i)
                    out.writeDouble(mPrior.mValues[i]);
            }
            @Override
            public Object read(ObjectInput in)
                throws IOException, ClassNotFoundException {

                int numDimensions = in.readInt();
                double[] priorVariances = new double[numDimensions];
                for (int i = 0; i < numDimensions; ++i)
                    priorVariances[i] = in.readDouble();
                return new LaplaceRegressionPrior(priorVariances);
            }
        }
    }

    static class CauchyRegressionPrior
        extends ArrayRegressionPrior
        implements Serializable {

        static final long serialVersionUID = 2351846943518745614L;

        CauchyRegressionPrior(double[] priorSquaredScales) {
            super(priorSquaredScales);
        }
        @Override
        public double gradient(double beta, int dimension) {
            return 2.0 * beta / (beta * beta + mValues[dimension]);
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            return log21OverPi
                + 0.5 * com.aliasi.util.Math.log2(mValues[dimension])
                - com.aliasi.util.Math.log2(beta * beta + mValues[dimension]
                                            * mValues[dimension]);
        }
        @Override
        public String toString() {
            return toString("CauchyRegressionPrior","Scale");
        }
        private Object writeReplace() {
            return new Serializer(this);
        }
        private static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = 5202676106810759907L;
            final CauchyRegressionPrior mPrior;
            public Serializer(CauchyRegressionPrior prior) {
                mPrior = prior;
            }
            public Serializer() {
                this(null);
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(mPrior.mValues.length);
                for (int i = 0; i < mPrior.mValues.length; ++i)
                    out.writeDouble(mPrior.mValues[i]);
            }
            @Override
            public Object read(ObjectInput in)
                throws IOException, ClassNotFoundException {

                int numDimensions = in.readInt();
                double[] priorScales = new double[numDimensions];
                for (int i = 0; i < numDimensions; ++i)
                    priorScales[i] = in.readDouble();
                return new CauchyRegressionPrior(priorScales);
            }
        }
    }


    static abstract class VariableRegressionPrior extends RegressionPrior {

        static final long serialVersionUID = -7527207309328127863L;

        final double mPriorVariance;
        final boolean mNoninformativeIntercept;
        VariableRegressionPrior(double priorVariance,
                                boolean noninformativeIntercept) {
            mPriorVariance = priorVariance;
            mNoninformativeIntercept = noninformativeIntercept;
        }
        public String toString(String priorName, String paramName) {
            return priorName + "(" + paramName + "=" + mPriorVariance
                + ", noninformativeIntercept=" + mNoninformativeIntercept + ")";
        }
    }

    static class VariableGaussianRegressionPrior
        extends VariableRegressionPrior
        implements Serializable {
        static final long serialVersionUID = -7527207309328127863L;
        VariableGaussianRegressionPrior(double priorVariance,
                                        boolean noninformativeIntercept) {
            super(priorVariance,noninformativeIntercept);
        }
        @Override
        public double gradient(double beta, int dimension) {
            return (dimension == 0 && mNoninformativeIntercept)
                ? 0.0
                : beta / mPriorVariance;
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            if (dimension == 0 && mNoninformativeIntercept)
                return 0.0;  // log(1)=0.0
            return -log2Sqrt2Pi
                - 0.5 * com.aliasi.util.Math.log2(mPriorVariance)
                - beta * beta / (2.0 * mPriorVariance);

        }
        @Override
        public String toString() {
            return toString("GaussianRegressionPrior","Variance");
        }
        private Object writeReplace() {
            return new Serializer(this);
        }
        private static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = 5979483825025936160L;
            final VariableGaussianRegressionPrior mPrior;
            public Serializer(VariableGaussianRegressionPrior prior) {
                mPrior = prior;
            }
            public Serializer() {
                this(null);
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeDouble(mPrior.mPriorVariance);
                out.writeBoolean(mPrior.mNoninformativeIntercept);
            }
            @Override
            public Object read(ObjectInput in)
                throws IOException, ClassNotFoundException {

                double priorVariance = in.readDouble();
                boolean noninformativeIntercept = in.readBoolean();
                return new VariableGaussianRegressionPrior(priorVariance,
                                                           noninformativeIntercept);
            }
        }
    }

    static class VariableLaplaceRegressionPrior
        extends VariableRegressionPrior
        implements Serializable {

        static final long serialVersionUID = -4286001162222250623L;
        final double mPositiveGradient;
        final double mNegativeGradient;
        final double mPriorIntercept;
        final double mPriorCoefficient;
        VariableLaplaceRegressionPrior(double priorVariance,
                                       boolean noninformativeIntercept) {
            super(priorVariance,noninformativeIntercept);
            mPositiveGradient = Math.sqrt(2.0/priorVariance);
            mNegativeGradient = -mPositiveGradient;
            mPriorIntercept
                = log2Sqrt2Over2 - 0.5
                * com.aliasi.util.Math.log2(priorVariance);
            mPriorCoefficient = -sqrt2 / Math.sqrt(priorVariance);
        }
        @Override
        public double gradient(double beta, int dimension) {
            return (dimension == 0 && mNoninformativeIntercept || beta == 0.0)
                ? 0.0
                : (beta > 0
                   ? mPositiveGradient
                   : mNegativeGradient );
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            if (dimension == 0 && mNoninformativeIntercept)
                return 0.0;
            return mPriorIntercept + mPriorCoefficient * Math.abs(beta);

            // return log2Sqrt2Over2
            // - 0.5 * com.aliasi.util.Math.log2(mPriorVariance)
            // - sqrt2 * Math.abs(beta) / Math.sqrt(mPriorVariance);
        }
        @Override
        public String toString() {
            return toString("LaplaceRegressionPrior","Variance");
        }
        private Object writeReplace() {
            return new Serializer(this);
        }
        private static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = 2321796089407881776L;
            final VariableLaplaceRegressionPrior mPrior;
            public Serializer(VariableLaplaceRegressionPrior prior) {
                mPrior = prior;
            }
            public Serializer() {
                this(null);
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeDouble(mPrior.mPriorVariance);
                out.writeBoolean(mPrior.mNoninformativeIntercept);
            }
            @Override
            public Object read(ObjectInput in)
                throws IOException, ClassNotFoundException {

                double priorVariance = in.readDouble();
                boolean noninformativeIntercept = in.readBoolean();
                return new VariableLaplaceRegressionPrior(priorVariance,
                                                          noninformativeIntercept);
            }
        }
    }

    static class VariableCauchyRegressionPrior
        extends VariableRegressionPrior {
        static final long serialVersionUID = 3368658136325392652L;
        VariableCauchyRegressionPrior(double priorVariance,
                                      boolean noninformativeIntercept) {
            super(priorVariance,noninformativeIntercept);
        }
        @Override
        public double gradient(double beta, int dimension) {
            return (dimension == 0 && mNoninformativeIntercept)
                ? 0
                : 2.0 * beta / (beta * beta + mPriorVariance);
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            if (dimension == 0 && mNoninformativeIntercept)
                return 0.0;
            return log21OverPi
                + 0.5 * com.aliasi.util.Math.log2(mPriorVariance)
                - com.aliasi.util.Math.log2(beta * beta + mPriorVariance);
        }
        @Override
        public String toString() {
            return toString("CauchyRegressionPrior","Scale");
        }
        public Object writeReplace() {
            return new Serializer(this);
        }
        private static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = -7209096281888148303L;
            final VariableCauchyRegressionPrior mPrior;
            public Serializer(VariableCauchyRegressionPrior prior) {
                mPrior = prior;
            }
            public Serializer() {
                this(null);
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeDouble(mPrior.mPriorVariance);
                out.writeBoolean(mPrior.mNoninformativeIntercept);
            }
            @Override
            public Object read(ObjectInput in)
                throws IOException, ClassNotFoundException {

                double priorScale = in.readDouble();
                boolean noninformativeIntercept = in.readBoolean();
                return new VariableCauchyRegressionPrior(priorScale,
                                                         noninformativeIntercept);
            }
        }
    }


    static class LogInterpolatedRegressionPrior extends RegressionPrior {
        static final long serialVersionUID = 1052451778773339516L;
        private final double mAlpha;
        private final RegressionPrior mPrior1;
        private final RegressionPrior mPrior2;
        LogInterpolatedRegressionPrior(double alpha,
                                       RegressionPrior prior1,
                                       RegressionPrior prior2) {
            mAlpha = alpha;
            mPrior1 = prior1;
            mPrior2 = prior2;
        }
        @Override
        public double gradient(double beta, int dimension) {
            return mAlpha * mPrior1.gradient(beta,dimension)
                + (1 - mAlpha) * mPrior2.gradient(beta,dimension);
        }
        @Override
        public double log2Prior(double beta, int dimension) {
            return mAlpha * mPrior1.log2Prior(beta,dimension)
                + (1 - mAlpha) * mPrior2.log2Prior(beta,dimension);
        }
        @Override
        public String toString() {
            return "LogInterpolatedRegressionPrior("
                + "alpha=" + mAlpha
                + ", prior1=" + mPrior1
                + ", prior2=" + mPrior2 + ")";
        }
        Object writeReplace() {
            return new Serializer(this);
        }
        static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = 1071183663202516816L;
            final LogInterpolatedRegressionPrior mPrior;
            public Serializer() {
                this(null);
            }
            public Serializer(LogInterpolatedRegressionPrior prior) {
                mPrior = prior;
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeDouble(mPrior.mAlpha);
                out.writeObject(mPrior.mPrior1);
                out.writeObject(mPrior.mPrior2);
            }
            @Override
            public Object read(ObjectInput in) 
                throws IOException, ClassNotFoundException {
                
                double alpha = in.readDouble();
                @SuppressWarnings("unchecked")
                RegressionPrior prior1 = (RegressionPrior) in.readObject();
                @SuppressWarnings("unchecked")
                RegressionPrior prior2 = (RegressionPrior) in.readObject();
                return new LogInterpolatedRegressionPrior(alpha,prior1,prior2);
            }
        }
    }


    static class ShiftMeans extends RegressionPrior {
        static final long serialVersionUID = 5159543505446681732L;
        private final double[] mMeans;
        private final RegressionPrior mPrior;
        ShiftMeans(double[] means,
                   RegressionPrior prior) {
            mPrior = prior;
            mMeans = means;
        }
        @Override
        public double mode(int i) {
            return mMeans[i] + mPrior.mode(i);
        }
        @Override
        public boolean isUniform() {
            return mPrior.isUniform();
        }
        @Override
        public double log2Prior(double betaI, int i) {
            return mPrior.log2Prior(betaI - mMeans[i],i);
        }
        @Override
        public double gradient(double betaI, int i) {
            return mPrior.gradient(betaI - mMeans[i],i);
        }
        @Override
        public String toString() {
            return "ShiftMeans(means=...,prior=" + mPrior + ")";
        }
        static class Serializer extends AbstractExternalizable {
            static final long serialVersionUID = -777157399350907424L;
            final ShiftMeans mPrior;
            public Serializer() {
                this(null);
            }
            public Serializer(ShiftMeans prior) {
                mPrior = prior;
            }
            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                writeDoubles(mPrior.mMeans,out);
                out.writeObject(mPrior.mPrior);
            }
            @Override
            public Object read(ObjectInput in) 
                throws IOException, ClassNotFoundException {
                
                double[] means = readDoubles(in);
                @SuppressWarnings("unchecked")
                RegressionPrior prior = (RegressionPrior) in.readObject();
                return new ShiftMeans(means,prior);
            }
        }
    }


}


