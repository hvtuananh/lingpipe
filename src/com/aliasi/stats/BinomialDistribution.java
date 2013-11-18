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
 * A <code>BinomialDistribution</code> is a discrete distribution over
 * the number of successes given a fixed number of Bernoulli trials.
 * A binomial distribution is constructed from a specified Bernoulli
 * distribution which determines the success probability.  The minimum
 * outcome is <code>0</code> and the maximum outcome is the number of
 * trials.  This class also defines a constant method {@link
 * #log2BinomialCoefficient(long,long)} for computing binomial
 * coefficients.
 *
 * <P>The method {@link #z(int)} returns the z-score statistic for a
 * specified number of outcomes.
 *
 * <h3>Computing P-Values</h3>
 *
 * <p>As of LingPipe 3.2.0, the dependency on <a
 * href="http://commons.apache.org/math/">Jakarta Commons Math</a> was
 * removed.  As a result, we removed the two methods that computed
 * p-values.  Here's their implementation in case you need the
 * functionality (you may need to increas the text size):
 *
 * <blockquote><pre style="border: 1px solid #DDD; padding: 1em; font-size:70%; font-family: 'lucida console', 'courier new'">
 * import org.apache.commons.math.MathException;
 * import org.apache.commons.math.distribution.NormalDistribution;
 * import org.apache.commons.math.distribution.NormalDistributionImpl;
 *
 * static final NormalDistribution Z_DISTRIBUTION
 *       = new NormalDistributionImpl();
 *
 * /**
 *  * Returns the two-sided p-value computed from the z-score for
 *  * this distribution for the specified number of successes.
 *  ...
 *  double pValue(int numSuccesses) throws MathException {
 *     return pValue(bernoulliDistribution().successProbability(),
 *                   numSuccesses,
 *                   numTrials());
 * }
 *
 * /**
 *  * Returns the one-sided p-value computed from the z-score for
 *  * this distribution for the specified number of successes.
 *  ...
 *  double pValueLess(int numSuccesses) throws MathException {
 *      return pValueLess(bernoulliDistribution().successProbability(),
 *                        numSuccesses,
 *                        mNumTrials());
 *  }
 *
 * /**
 *  * Returns the two-sided p-value for the z-score statistic on the
 *  * specified number of successes out of the specified number of
 *  * trials for the specified success probability.
 *  ...
 *  static double pValue(double successProbability,
 *                       int numSuccesses,
 *                       int numTrials) throws MathException {
 *
 *      double z = z(successProbability,numSuccesses,numTrials);
 *      return 2.0 * Z_DISTRIBUTION.cumulativeProbability(Math.min(-z,z));
 *   }

 *  /**
 *   * Returns the one-sided (lower) p-value for the z-score statistic
 *   * on the specified number of successes out of the specified
 *   * number of trials for the specified success probability.
 *   ...
 *   static double pValueLess(double successProbability,
 *                            int numSuccesses,
 *                            int numTrials) throws MathException {
 *       double z = z(successProbability,numSuccesses,numTrials);
 *       return 1.0 - Z_DISTRIBUTION.cumulativeProbability(z);
 *   }</pre></blockquote>
 *
 * <P>For more information, see:
 * <UL>
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/BinomialDistribution.html">Binomial Distribution</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/BinomialCoefficient.html">Binomial Coefficient</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/z-Score.html">z-Score</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/P-Value.html">P-Value</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/HypothesisTesting.html">Hypothesis Testing</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 3.2.0
 * @since   LingPipe2.0
 */
public class BinomialDistribution extends AbstractDiscreteDistribution {

    private final BernoulliDistribution mBernoulliDistribution;
    private final int mNumTrials;

    /**
     * Construct a binomial distribution that samples from the
     * specified Bernoulli distribution the specified number of times.
     * The resulting distribution is over the number of successes,
     * with a range between zero and the number of trials.
     *
     * <P>The Bernoulli distribution is stored and any change to it
     * will affect the constructed binomial distribution.
     *
     * @param distribution Underlying Bernoulli distribution.
     */
    public BinomialDistribution(BernoulliDistribution distribution,
                                int numTrials) {
        if (numTrials < 0) {
            String msg = "Number of trials must be non-negative."
                + " Found num trials=" + numTrials;
            throw new IllegalArgumentException(msg);
        }
        mBernoulliDistribution = distribution;
        mNumTrials = numTrials;
    }

    /**
     * Returns the underlying Bernoulli (two outcome) distribution
     * underlying this binomial distribution.
     *
     * @return The base distribution.
     */
    public BernoulliDistribution bernoulliDistribution() {
        return mBernoulliDistribution;
    }


    /**
     * Returns zero, the minimum outcome for a binomial distribution.
     *
     * @return Zero, the minimum outcome for a binomial distribution.
     */
    @Override
    public long minOutcome() {
        return 0l;
    }

    /**
     * Returns the maximum non-zero probability outcome, which is the
     * number of trials for this distribution.
     *
     * @return The maximum non-zero probability outcome.
     */
    @Override
    public long maxOutcome() {
        return mNumTrials;
    }

    /**
     * Returns the number of trials for this binomial distribution.
     * This is the same as the result of {@link #maxOutcome()}.
     *
     * @return The number of trials.
     */
    public long numTrials() {
        return mNumTrials;
    }

    /**
     * Returns the probability of the specified outcome.  The
     * probability is determined by the likelihood of the specified
     * number of successes out of the number of trials for this
     * distribution.
     *
     * <P>The probability for a specified number of outcomes is:
     *
     * <blockquote><code>
     * P(numSuccesses)
     * <br> &nbsp;
     * = binomialCoefficient(numTrials,numSuccesses)
     * <br> &nbsp;
     * * P(success)<sup><sup>n</sup></sup>
     * <br> &nbsp;
     * * (1 - P(success))<sup><sup>numTrials - numSuccesses</sup></sup>
     * </code></blockquote>
     *
     * where <code>numTrials</code> is the number of trials for this
     * binomial distribution and <code>P(success)</code> is the
     * success probability of the Bernoulli distribution underlying
     * this binomial distribution.
     *
     * @param outcome Number of successes.
     * @return Probability of specified number of successes.
     */
    @Override
    public double probability(long outcome) {
        return java.lang.Math.pow(2.0,log2Probability(outcome));
    }

    /**
     * Returns the log (base 2) probability of the specified outcome.
     * The probability is determined by the likelihood of the
     * specified number of successes out of the number of trials for
     * this distribution. See the documentation for the method {@link
     * #probability(long)} for an exact definition.

     *
     * @param outcome Number of successes.
     * @return Probability of specified number of successes.
     */
    @Override
    public double log2Probability(long outcome) {
        if (outcome < 0 || outcome > maxOutcome())
            return Double.NEGATIVE_INFINITY;
        return  log2BinomialCoefficient(mNumTrials,outcome)
            + ( ((double) outcome)
                * mBernoulliDistribution.log2Probability(1l) )
            + ( ((double) (mNumTrials - outcome))
                * mBernoulliDistribution.log2Probability(0l) );
    }


    /**
     * Returns the z-score for the specified number of successes given
     * this distribution's success probability and number of trials.
     * Z-scores may take on any value from negative to positive
     * infinity.  A z-score is the number of standard deviations above
     * or below the expected number of successes for this
     * distribution.  Thus the greater the absolute value of the
     * z-score, the less likely the number of successes was drawn from
     * this distribution.  The lower a negative z-score, the more
     * likely it was drawn from a distribution with a lower success
     * probability and the higher a positive z-score, the more likely
     * it was drawn from a distribution with a higher success
     * probability.
     *
     * <P>The formula for z-scores is provided in the documentation
     * for the static method {@link #z(double,int,int)}.
     *
     * @param numSuccesses Number of successes in sample.
     * @return Z score value.
     * @throws IllegalArgumentException If the number of successes is less
     * than 0 or more than the number of trials for this distribution.
     */
    public double z(int numSuccesses) {
        return z(mBernoulliDistribution.successProbability(),
                 numSuccesses,
                 mNumTrials);
    }


    /**
     * Returns the variance of this binomial distribution.  The
     * variance of a binomial distribution is:
     *
     * <blockquote>
     *   variance = numTrials * P(success) * (1 - P(success))
     * </blockquote>
     *
     * @return The variance of this binomial distribution.
     */
    @Override
    public double variance() {
        double successProb = mBernoulliDistribution.successProbability();
        return successProb * (1.0 - successProb) * (double) mNumTrials;
    }

    /**
     * Returns the z score for the specified number of successes out
     * of the specified number of trials given the specified success
     * probability.  The z-score is the number of standard deviations
     * above or below the median number of outcomes the given number
     * of successes lies given the success probability and number of
     * trials.
     *
     * <P>The z-score for binomial distributions is defined by:
     *
     * <blockquote><code>
     *  z = (numSuccesses - expectedSuccesses)
     * <br> &nbsp;
     * / (numTrials * P(success) * (1-P(success)))<sup><sup>1/2</sup></sup>
     * </code></blockquote>
     *
     * where
     *
     * <blockquote><code>
     * expectedSuccesses = P(success) * numTrials
     * </code></blockquote>
     *
     * Thus numerator is the difference between observed and expected
     * values for the number of successes and the denominator is the
     * standard deviation for the Bernoulli trial iterated over the
     * specified number of trials.
     *
     * @param successProbability Probability of success.
     * @param numSuccesses Number of successes.
     * @param numTrials Number of trials.
     * @throws IllegalArgumentException If the success probability is
     * not between 0 and 1 or if the number of successes is less than
     * zero or greater than the number of trials.
     */
    public static double z(double successProbability,
                           int numSuccesses,
                           int numTrials) {
        if (successProbability < 0.0
            || successProbability > 1.0
            || Double.isNaN(successProbability)) {
            String msg = "Require probability between 0 and 1 for success."
                + " Found success probability=" + successProbability;
            throw new IllegalArgumentException(msg);
        }
        if (numSuccesses < 0 || numSuccesses > numTrials) {
            String msg = "Require 0 <= num successes <= num trials"
                + " Found num successes= " + numSuccesses
                + " num successes=" + numTrials;
            throw new IllegalArgumentException(msg);
        }
        double numTrialsD = numTrials;
        double numSuccessesD = numSuccesses;
        double expectedSuccesses = successProbability * numTrialsD;
        return (numSuccessesD - expectedSuccesses)
            / Math.sqrt(numTrialsD
                        * successProbability
                        * (1.0 - successProbability));
    }


    /**
     * Returns the log (base 2) of the binomial coefficient of the
     * specified arguments.  The binomial coefficient is equal to the
     * number of ways to choose a subset of size <code>m</code> from a
     * set of <code>n</code> objects, which is pronounced "n choose
     * m", and is given by:
     *
     * <blockquote><code>
     *   binomialCoefficient(n,m) = n! / ( m! * (n-m)!)
     *   <br>
     *   log<sub>2</sub> choose(n,m)
     *    = log<sub>2</sub> n - log<sub>2</sub> m
     *      - log<sub>2</sub> (n-m)
     * </code></blockquote>
     *
     * @return The log (base 2) of the binomial coefficient of the
     * specified arguments.
     */
    public static double log2BinomialCoefficient(long n, long m) {
        if (n < m) {
            String msg = "Require n > m for binomial coefficient."
                + " Found n= " + n
                + " m = " + m;
            throw new IllegalArgumentException(msg);
        }
        return com.aliasi.util.Math.log2Factorial(n)
            - com.aliasi.util.Math.log2Factorial(m)
            - com.aliasi.util.Math.log2Factorial(n-m);
    }




}
