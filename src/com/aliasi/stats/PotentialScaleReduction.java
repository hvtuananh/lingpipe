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

/**
 * The {@code PotentialScaleReduction} class provides an online
 * computationa of Rhat, the potential scale reduction statistic for
 * measuring mixing and convergence of multiple Markov chain Monte
 * Carlo (MCMC) samplers.
 *
 * <p>At construction time, the number of estimators is specified.
 * There must be at least two estimators in order to compute Rhat.
 * Samples from the Markov chains are provided to this class via the
 * {@link #update(int,double)} method.  
 *
 * <h3>Normality Assumptiosn</h3>
 *
 * These estimates make nomality assumptions for the samples which are
 * not justified at samller sample sizes for all ditribution shapes.
 * It may help to transform samples on a [0,1] scale using the inverse
 * logistic (logit) transform, and samples representing ratios in
 * [0,infinity) using a log transform.
 * 
 * <h3>Definition of Rhat</h3>
 *
 * The idea is to compare the cross-chain variances to the within-chain
 * variances, with values near 1.0 indicating good mixing, and values
 * greater than 1 indicating the potential for better mixing.  
 *
 * Suppose we have {@code M} Markov chains with {@code N} samples
 * each, with a sample being a floating point value {@code y[m,n]}.
 * As usual, {@code y[m,]} is the sequence of samples from the single
 * chain {@code m} and {@code y[,n]} are the {@code n}-th samples from
 * each chain.  Unbiased mean and variance estimates are defined for
 * sequences in the usual way (see {@link OnlineNormalEstimator} for
 * definitions).  Using vector notation, {@code mean'(y[,])} is the
 * average value of all samples whereas {@code mean'(y[m,])} is the
 * average of samples from chain {@code m}; similarly {@code
 * var'(y[,])} is the variance over all samples and {@code
 * var'(y[m,])} the variance of samples in chain {@code m}.
 *
 * <p>The definition of the Rhat is:
 *
 * <blockquote><pre>
 * Rhat = sqrt(varHatPlus/W)</pre></blockquote>
 *
 * where {@code varHatPlus} is a weighted average of the within-chain
 * ({@code W}) and between-chain ({@code B}) variances.
 *
 * <blockquote><pre>
 * varHatPlus = (N-1)/N * W + 1/N * B.</pre></blockquote>
 *
 * The between-chain variance is defined by
 *
 * <blockquote><pre>
 * B = N * var'(mean'(y[m,]))
 *
 *   = N/(M-1) * <big><big>&Sigma;</big></big><sub>m</sub> (mean'(y[m,]) - mean'(y[,]))<sup><sup><big>2</big></sup></sup>.</pre></blockquote>
 *
 * The within-chain variance is the average of the unbiased within-chain variance estimates:
 *
 * <blockquote><pre>
 * W = mean'(var'(y[m,]))
 *
 *   = 1/M <big><big>&Sigma;</big></big><sub>m</sub> var'(y[m,]).</pre></blockquote>
 *
 * This is the usual definition for chains in which there are the same
 * number of samples.  For the implementation here, we take {@code N}
 * to be the minimum of the numbers of samples in the chains.  The
 * within-chain statistics {@code mean'(y[m,])} and {@code
 * var'(y[m,])} are computed using all of the samples for chain {@code
 * m}.  But the cross-chain statistics are not normalized, so {@code
 * mean'(y[,])} is computed here as {@code mean'(mean'(y[m,]))}.
 *
 * <h3>Per-Chain and Global Statistics</h3>
 *
 * The estimators for the within-chain means and variances, {@code mean'(y[m,])}
 * and {@code var'(y[m,])}, are available through the estimator returned by
 * {@link #estimator(int)}.  
 *
 * <p>An estimator for the complete set of samples mean and variance,
 * {@code mean'(y[,])} and {@code var'(y[,])}, are available through
 * {@link #globalEstimator()}.  Note that these are truly global
 * estimates, not the estimates used in asynchronous Rhat calculations
 * as defined in he previous section.
 * 
 * <h3>Thread Safety</h3>
 *
 * Updates are write operations that need to be read-write
 * synchronized with the estimator methods.
 *
 * <h3>References</h3>
 *
 * The method was introduced by Gelman and Rubin in 1992, summarized in
 * their book, and implemented in the R coda package.
 *
 * <ul>
 * <li> Gelman and Rubin. 1992. <a href="http://www.stat.columbia.edu/~gelman/research/published/itsim.pdf">Inference from iterative simulation using multiple sequences</a> (with discussion).  <i>Statistical Science</i> <b>7</b>.
 * <li> Gelman, Carlin, Stern and Rubin. 2004. <i>Bayesian Data Analysis</i>.  Second Edition. Chapman &amp; Hall/CRC.  Section 11.6.
 * <li>Plummer, Best, Cowles and Vines. 2009. <a href="http://cran.r-project.org/web/packages/coda/coda.pdf">Coda Package Documentation</a> [pdf]. Version 0.13-4.  On <i>CRAN</i>.
 * </ul>
 * 
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   Lingpipe3.9.1
 */
public class PotentialScaleReduction {

    private final OnlineNormalEstimator mGlobalEstimator;

    private final OnlineNormalEstimator[] mChainEstimators;

    /**
     * Construct a potential scale reduction with the specified number
     * of Markov chains for input.
     *
     * @param numChains Number of Markov chains.
     * @throws IllegalArgumentException If the number of chains is less than 2.
     */
    public PotentialScaleReduction(int numChains) {
        if (numChains < 2) {
            String msg = "Need at least two chains."
                + " Found numChains=" + numChains;
            throw new IllegalStateException(msg);
        }
        mChainEstimators = new OnlineNormalEstimator[numChains];
        for (int m = 0; m < numChains; ++m)
            mChainEstimators[m] = new OnlineNormalEstimator();
        mGlobalEstimator = new OnlineNormalEstimator();
    }

    /**
     * Construct a potential scale reduction for the specified matrix
     * Of estimates for each chain.  The matrix entries {@code yss[m][n]} 
     * are for the {@code n}-th sample from chain {@code m}.  The chains
     * may have different numbers of samples.
     *
     * @param yss Matrix of estimates by chain and sample.
     * @throws IllegalStateException If the number of chains (length
     * of {@code yss}) is less than 2.
     */
    public PotentialScaleReduction(double[][] yss) {
        this(yss.length);
        for (int m = 0; m < yss.length; ++m) 
            for (int n = 0; n < yss[m].length; ++n)
                update(m,yss[m][n]);
    }

    /**
     * Returns the number of chains for this estimator.
     *
     * @return Number of chains for this estimator.
     */
    public int numChains() {
        return mChainEstimators.length;
    }

    /**
     * Returns the estimator for the specified chain.
     *
     * @param chain Index of chain.
     * @return Estimator for the chain.
     */
    public OnlineNormalEstimator estimator(int chain) {
        return mChainEstimators[chain];
    }
    

    /**
     * Returns the estimator that pools the estimates across the
     * chains.  
     *
     * @return Overall estimator for samples.
     */
    public OnlineNormalEstimator globalEstimator() {
        return mGlobalEstimator;
    }

    /**
     * Provide a sample of the specified value from the specified chain.
     *
     * @param chain Chain from which sample was drawn.
     * @param y Value of sample.
     * @throws IndexOutOfBoundsException If the chain is less than zero or
     * greater than or equal to the number of chains.
     */
    public void update(int chain, double y) {
        mChainEstimators[chain].handle(y);
        mGlobalEstimator.handle(y);
    }
    
    /**
     * Returns the Rhat statistic as defined in the class
     * documentation.  
     *
     * @return The Rhat statistic.
     */
    public double rHat() {
        long minSamples = Long.MAX_VALUE;
        for (OnlineNormalEstimator estimator : mChainEstimators)
            if (minSamples > estimator.numSamples())
                minSamples = estimator.numSamples();
        
        double w = 0.0;
        for (OnlineNormalEstimator estimator : mChainEstimators)
            w += estimator.varianceUnbiased();
        w /= numChains();
        
        double crossChainMean = 0.0;
        for (OnlineNormalEstimator estimator : mChainEstimators)
            crossChainMean += estimator.mean();
        crossChainMean /= numChains();

        double b = 0.0;
        for (OnlineNormalEstimator estimator : mChainEstimators) 
            b += square(estimator.mean() - crossChainMean);

        b /= (numChains() - 1.0);
        
        double varPlus = ((minSamples - 1) * w)/minSamples + b;
        
        return Math.sqrt(varPlus / w);
    }

    static double square(double x) {
        return x * x;
    }
    

}
