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
 * The <code>PoissonDistribution</code> abstract class is used for
 * calculating Poisson distributions.  Poisson distributions are
 * limits of Poisson processes, and are used to model rates of
 * occurrences of events within a fixed period (of time, space, etc.).
 * Poisson distributions are good models of lengths of texts or the
 * rate of occurrence of words in text, as well as many other natural
 * phenomena.
 *
 * <P>The Poisson distribution is a parametric discrete distribution
 * with a single parameter <code>&lambda; &gt; 0</code> which is the
 * average rate of occurrence of events in a period. The resulting
 * distribution provides a likelihood for each non-negative number of
 * outcomes.  Specifically, the Poisson distribution with rate
 * parameter &lambda; is defined for <code><i>k</i> &gt; 0</code> by:
 *
 * <blockquote><code>
 *   Poisson<sub><sub><sub>&lambda;</sub></sub></sub>(<i>k</i>)
 *   = e<sup><sup>-&lambda;</sup></sup> &lambda;<sup><sup><i>k</i></sup></sup> / <i>k</i>!
 * </code></blockquote>
 *
 * Note that this definition produces a properly normalized
 * probability distribution over natural numbers; if <code>&lambda;
 * &gt; 0</code>, then:
 *
 * <blockquote><code>
 *   <big><big>&Sigma;</big></big><sub><sub><i>k</i> >= 0</sub></sub>
 *   Poisson<sub><sub><sub>&lambda;</sub></sub></sub>(<i>k</i>)
 *   = 1.0
 * </code></blockquote>
 *
 * The expected value of a Poisson distribution is equal to the rate parameter:
 *
 * <blockquote><code>
 * E(Poisson<sub><sub><sub>&lambda;</sub></sub></sub>) = &lambda;
 * </code></blockquote>
 *
 * The variance is also equal to the rate parameter:
 *
 * <blockquote><code>
 * Var(Poisson<sub><sub><sub>&lambda;</sub></sub></sub>)
 * =<sub><sub><i>def</i></sub></sub>
 * E([Poisson<sub><sub><sub>&lambda;</sub></sub></sub> - E(Poisson<sub><sub><sub>&lambda;</sub></sub></sub>)]<sup><sup>2</sup></sup>)
 * = &lambda;
 * </blockquote></code>
 *
 * <P>Concrete subclasses need only implement the abstract {@link
 * #mean()} method; the method {@link #log2Probability(long)} computes the
 * log (base 2) of the Poisson probability estimate for a given number
 * of outcomes in terms of the value of the rate parameter
 * <code>lambda()</code>.  Logarithms are used to prevent over- and
 * underflow in calculations.
 *
 *
 * <P>For more information, see: 
 * <UL> 
 * <LI> Eric  W. Weisstein. 
 * <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson Distribution</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource. 
 * </UL>

 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public abstract class PoissonDistribution extends AbstractDiscreteDistribution {

    /**
     * Construct an abstract Poisson distribution.
     */
    protected PoissonDistribution() { 
        /* do nothing */
    }

    /**
     * Returns the mean of this Poisson distribution, which is equal
     * to the rate parameter &lambda;.  Concrete implementations are
     * responsible for ensuring that the mean is positive and finite.
     *
     * @return The mean of this distribution.
     */
    @Override
    public abstract double mean();

    /**
     * Returns the variance of this Poisson distribution, which is
     * equal to the mean.
     *
     * @return The variance of this distribution.
     */
    @Override
    public double variance() {
        return mean();
    }

    /**
     * Returns the minimum outcome with non-zero probability,
     * <code>0</code>.
     *
     * @return Zero.
     */
    @Override
    public long minOutcome() {
        return 0l;
    }

    /**
     * Returns the log (base 2) probability estimate in this Poisson
     * distribution for the specified outcome.  This method will throw
     * an illegal state exception if the mean implementation returns a
     * non-positive number.  If the outcome is negative, the result
     * will be negative-infinity.
     *
     * @param outcome The outcome being estimated.  
     * @return The log (base 2) probability of finding the specified
     * number of outcomes given this distribution's rate parameter.
     * @throws IllegalStateException if the mean is not a positive
     * finite value.
     */
    @Override
    public final double log2Probability(long outcome) {
        return log2Poisson(mean(),outcome);
    }

    /**
     * Returns the probability estimate in this Poisson distribution
     * for the specified outcome.  Note that if the outcome is
     * negative, the result will be zero.
     *
     * @param outcome The outcome whose probability is returned.
     * @return The log (base 2) probability of finding the specified
     * number of outcomes given this distribution's rate parameter.
     * @throws IllegalStateException If the mean is not a positive
     * finite value.
     */
    @Override
    public final double probability(long outcome) {
        return java.lang.Math.pow(2.0,log2Probability(outcome));
    }
    
    private static double log2Poisson(double lambda, long k) {
        if (lambda <= 0.0 || Double.isInfinite(lambda)) {
            String msg = "Mean must be a positive non-infiite value."
                + " Found mean=" + lambda;
            throw new IllegalStateException(msg);
        }
        if (k < 0l) return Double.NEGATIVE_INFINITY;
        return -lambda * com.aliasi.util.Math.LOG2_E
            + (((double)k) * com.aliasi.util.Math.log2(lambda))
            - com.aliasi.util.Math.log2Factorial(k);
    }

}
