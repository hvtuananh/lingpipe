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
 * A <code>MultinomialDistribution</code> results from drawing a fixed
 * number of samples from a multivariate distribution.  Thus the
 * probability distribution {@link #log2Probability(int[])} is over an
 * array of counts for the dimensions of the underlying multivariate
 * distribution.  This class also contains a static method {@link
 * #log2MultinomialCoefficient(int[])}to compute multinomial coefficients.
 *
 * <P>The method {@link #chiSquared(int[])} returns the chi-squared
 * statistic for a sample of outcome counts represented by an array of
 * integers.  The number of degrees of freedom is one less than the
 * number of dimensions.
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
 * import org.apache.commons.math.distribution.ChiSquaredDistribution;
 * import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
 *
 *
 *   /**
 *    * Returns the p-value for the chi-squared statistic on the specified
 *    * sample counts.
 *   ...
 *   double pValue(int[] sampleCounts) throws MathException {
 *       ChiSquaredDistribution chiSq
 *           = new ChiSquaredDistributionImpl(numDimensions()-1);
 *       double c = chiSquared(sampleCounts);
 *       return chiSq.cumulativeProbability(c);
 *   }
 * </pre></blockquote>

 * <P>For more information, see:
 * <UL>
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/MultinomialDistribution.html">Multinomial Distribution</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/MultinomialCoefficient.html">Multinomial Coefficient</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/Chi-SquaredDistribution.html">Chi-Squared Distribution</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/P-Value.html">P-Value</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * <LI> Eric  W. Weisstein.
 * <a href="http://mathworld.wolfram.com/HypothesisTesting.html">Hypothesis Testing</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource.
 * </UL>
 * @author Bob Carpenter
 * @version 3.2.0
 * @since   LingPipe2.0
 */
public class MultinomialDistribution {

    private final MultivariateDistribution mBasisDistribution;
    // private final ChiSquaredDistribution mChiSquaredDistribution;

    /**
     * Construct a multinomial distribution based on the specified
     * multivariate distribution.  Note that the multivariate
     * distribution is simply stored in this class and changes to it
     * will result in changes to the multinomial distribution.
     *
     * @param distribution Underlying multivariate distribution
     * defining the constructed multinomial.
     */
    public MultinomialDistribution(MultivariateDistribution distribution) {
        mBasisDistribution = distribution;
        // mChiSquaredDistribution
        // = new ChiSquaredDistributionImpl(distribution.numDimensions()-1);
    }

    /**
     * Returns the log (base 2) probability of the distribution of
     * outcomes specified in the argument.  The argument values
     * represent the number of outcomes and must be 0 or more.  All
     * outcomes with values of more than zero must have a non-zero
     * probability.  Note that the probability returned is normalized
     * for all sets of the same number of samples.
     *
     * <P>The definition of the probability value for multinomials is:
     *
     * <blockquote><code>
     * P(sampleCounts)
     * <br> &nbsp;
     * = multinomialCoefficient(sampleCounts)
     * <br> &nbsp;
     * * <big><big>&Pi;</big></big><sub><sub>i</sub></sub>
     *   P(i)<sup><sup>sampleCounts[i]</sup></sup>
     * </code></blockquote>
     *
     * where the multinomial coefficient is as defined in the method documentation
     * for {@link #log2MultinomialCoefficient(int[])}.  Taking logarithms yields:
     *
     * <blockquote><code>
     * log<sub>2</sub> P(sampleCounts)
     * <br> &nbsp; =
     * log<sub>2</sub> multinomialCoefficient(sampleCounts)
     * <br> &nbsp; +
     * <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   sampleCounts[i] * log<sub>2</sub> P(i)
     * </code></blockquote>
     *
     * Note that if the multivariate probability is zero for an
     * outcome with a non-zero count, the result will be {@link
     * Double#NEGATIVE_INFINITY}.
     *
     * @param sampleCounts Array of counts for outcomes.
     * @return The log (base 2) probability of the specified outcome
     * counts.
     * @throws IllegalArgumentException If the number of outcome
     * counts is not the same as the number of dimensions of this multinomial.
     */
    public double log2Probability(int[] sampleCounts) {
        checkNumSamples(sampleCounts);
        double sum = log2MultinomialCoefficient(sampleCounts);
        for (int i = 0; i < sampleCounts.length; ++i)
            sum += (double) sampleCounts[i]
                * mBasisDistribution.log2Probability(i);
        return sum;
    }

    /**
     * Returns the chi-squared statistic for rejecting the null
     * hypothesis that the specified samples were generated by this
     * distribution.  The number of degrees of freedom is the number
     * of outcomes minus one.  The lower the return value, the more
     * likely the sample was derived from this distribution.
     *
     * <P>The definition for the chi-square value is the sum
     * of square differences between sample counts and expected
     * counts, normalized by expected count:
     *
     * <blockquote><code>
     * &chi;<sup>2</sup>(sampleCounts)
     * = <big><big>&Sigma;</big></big><sub><sub>i</sub></sub>
     *   (sampleCounts[i] - expectedCount(i))<sup><sup>2</sup></sup>
     *   / expectedCount(i)
     * </code></blockquote>
     *
     * where the expected counts are computed based on the underlying
     * multivariate distribution and the total sample count:
     *
     * <blockquote><code>
     *   expectedCount(i)
     *    = probability(i) * totalCount
     * </code></blockquote>
     *
     * where <code>totalCount</code> is the sum of all of the sample
     * counts.
     *
     * <P>Note that the chi-squared test is a large sample test.  For
     * accurate results, each expected count should be at least five; in
     * symbols, <code>expectedCount(i) >= 5</code> for all <code>i</code>.
     *
     * @param sampleCounts Array of sample counts.
     * @return The chi-square estimate of the confidence that the
     * specified samples were generated by this distribution.
     * @throws IllegalArgumentException If the number of outcome
     * counts is not the same as the number of dimensions of this
     * multinomial.
     */
    public double chiSquared(int[] sampleCounts) {
        checkNumSamples(sampleCounts);
        int totalCount = com.aliasi.util.Math.sum(sampleCounts);
        double sum = 0.0;
        for (int i = 0; i < sampleCounts.length; ++i)
            sum += normSquareDiff(sampleCounts[i],
                                  mBasisDistribution.probability(i),
                                  totalCount);
        return sum;
    }

    /**
     * Returns the number of dimensions in this multinomial.  This is
     * equal to the number of dimensions of the underlying
     * multivariate distribution.
     *
     * @return The number of dimensions of this multinomial
     * distribution.
     */
    public int numDimensions() {
        return mBasisDistribution.numDimensions();
    }

    /**
     * Returns the multivariate distribution that forms the basis of
     * this multinomial distribution.  Note that changes to the basis
     * distribution affect this multinomial distribution.
     *
     * @return The basis distribution underlying this multinomial.
     */
    public MultivariateDistribution basisDistribution() {
        return mBasisDistribution;
    }

    void checkNumSamples(int[] samples) {
        if (samples.length != numDimensions()) {
            String msg = "Require same number of samples as dimensions."
                + " Number of dimensions=" + numDimensions()
                + "  Found #samples=" + samples.length;
            throw new IllegalArgumentException(msg);
        }
    }


    /**
     * Returns the log (base 2) multinomial coefficient for the
     * specified counts.  The multinomial coefficient counts the
     * number of ways the set of outcomes represented by the array of
     * individual outcome counts can be linearly ordered.  The result
     * is:
     *
     * <blockquote><code>
     * multinomialCoefficient(sampleCounts)
     * <br> &nbsp;
     * = totalCount! / ( <big><big>&Pi;</big></big><sub><sub>i</sub></sub> sampleCounts[i]! )
     * </code></blockquote>
     *
     * Taking logarithms produces:
     *
     * <blockquote><code>
     * log<sub>2</sub> multinomialCoefficient(sampleCounts)
     * <br> &nbsp;
     * = log<sub>2</sub> totalCount!
     * - <big><big>&Sigma;</big></big><sub><sub>i</sub></sub> log<sub>2</sub> sampleCounts[i]!
     * </code></blockquote>
     *
     * The multinomial coefficient is often written using a notation
     * similar to that used for the factorial as
     * <code>(sampleCounts[0],...,sampleCounts[n-1])!</code>.
     *
     * @param sampleCounts Array of outcome counts.
     * @return Number of ways outcomes can be linearly ordered.
     */
    public static double log2MultinomialCoefficient(int[] sampleCounts) {
        checkNonNegative(sampleCounts);
        long totalCount = com.aliasi.util.Math.sum(sampleCounts);
        double coeff = com.aliasi.util.Math.log2Factorial(totalCount);
        for (int i = 0; i < sampleCounts.length; ++i)
            coeff -= com.aliasi.util.Math.log2Factorial(sampleCounts[i]);
        return coeff;
    }

    static double normSquareDiff(double count, double probability,
                                 double totalCount) {
        double expectedCount = totalCount * probability;
        double diff = (count - expectedCount);
        return diff * diff / expectedCount;
    }

    static void checkNonNegative(int[] sampleCounts) {
        for (int i = 0; i < sampleCounts.length; ++i) {
            if (sampleCounts[i] < 0) {
                String msg = "Sample Counts must be non-negative."
                    + " Found sampleCounts[" + i + "]=" + sampleCounts[i];
                throw new IllegalArgumentException(msg);
            }
        }

    }

}
