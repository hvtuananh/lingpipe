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
 * An {@code OnlineNormalEstimator} provides an object that estimates
 * means, variances, and standard deviations for a stream of numbers
 * presented one at a time.  Given a set of samples {@code
 * x[0],...,x[N-1]}, the mean is defined by:
 *
 * <blockquote><pre>
 * mean(x) = (1/N) * <big><big>&Sigma;</big></big><sub>i &lt; N</sub> x[i]</pre></blockquote>
 *
 * The variance is defined as the average squared difference from the mean:
 *
 * <blockquote><pre>
 * var(x) = (1/N) * <big><big>&Sigma;</big></big><sub>i &lt; N</sub> (x[i] - mean(x))<sup>2</sup></pre></blockquote>
 *
 * and the standard deviation is the square root of variance:
 *
 * <blockquote><pre>
 * dev(x) = sqrt(var(x))</pre></blockquote>
 *
 * <p>By convention, the mean and variance of a zero-length sequence
 * of numbers are both returned as 0.0.
 *
 * <p>The above functions provide the maximum likelihood estimates of
 * the mean, variance and standard deviation for a normal distribution
 * generating the values.  That is, the estimated parameters are the
 * parameters which assign the observed data sequence the highest probability.
 *
 * <p>Unfortunately, the maximum likelihood variance and deviation
 * estimates are biased in that they tend to underestimate variance in
 * general.  The unbiased estimates adjust counts downward by one, thus
 * adjusting variance and deviation upwards:
 *
 * <blockquote><pre>
 * varUnbiased(x) = (N / (N-1)) * var(x)
 * devUnbiased(x) = sqrt(varUnbiased(x))</pre></blockquote>
 *
 * Note that {@code var'(x) >= var(x)} and {@code dev'(x) >= dev(x)}.
 *
 *
 * <p><b>Welford's Algorithm</b>
 *
 * <p>This class use's Welford's algorithm for estimation.  This
 * algorithm is far more numerically stable than either using two
 * passes calculating sums, and sum of square differences, or using a
 * single pass accumulating the sufficient statistics, which are the
 * two moments, the sum, and sum of squares of all entries.  The
 * algorithm keeps member variables in the class, and performs the
 * following update when seeing a new variable {@code x}:
 *
 * <blockquote><pre>
 * long n = 0;
 * double mu = 0.0;
 * double sq = 0.0;
 *
 * void update(double x) {
 *     ++n;
 *     double muNew = mu + (x - mu)/n;
 *     sq += (x - mu) * (x - muNew)
 *     mu = muNew;
 * }
 * double mean() { return mu; }
 * double var() { return n > 1 ? sq/n : 0.0; }</pre></blockquote>
 *
 * <p><b>Welford's Algorithm with Deletes</b></p>
 *
 * LingPipe extends the Welford's algorithm to support deletes by
 * value.  Given current values of {@code n}, {@code mu}, {@code sq},
 * and any {@code x} added at some point, we can compute the previous
 * values of {@code n}, {@code mu}, {@code sq}.  The delete method is:
 *
 * <blockquote><pre>
 * void delete(double x) {
 *     if (n == 0) throw new IllegalStateException();
 *     if (n == 1) {
 *         n = 0; mu = 0.0; sq = 0.0;
 *         return;
 *     }
 *     muOld = (n * mu - x)/(n-1);
 *     sq -= (x - mu) * (x - muOld);
 *     mu = muOld;
 *     --n;
 * }</pre></blockquote>
 *
 * Because the data are exchangable for mean and variance
 * calculations (that is, permutations of the inputs produce
 * the same mean and variance), the order of removal does not
 * need to match the order of addition.
 *
 * <p><b>References</b></p>
 *
 * <ul>
 *
 * <li>Knuth, Donald E. (1998) <i>The Art of Computer Programming,
 * Volume 2: Seminumerical Algorithms, 3rd Edition.</i> Boston:
 * Addison-Wesley. Page 232.</li>
 *
 * <li>Welford, B. P. (1962) Note on a method for calculating
 * corrected sums of squares and products. <i>Technometrics</i>
 * <b>4</b>(3):419--420.</li>
 *
 * <li>Cook, John D. <a
 * href="http://www.johndcook.com/standard_deviation.html">Accurately
 * computing running variance</a>.</li>
 *
 *  </ul>
 *
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   Lingpipe3.8
 */
public class OnlineNormalEstimator {

    private long mN = 0L;
    private double mM = 0.0;
    private double mS = 0.0;

    /**
     * Construct an instance of an online normal estimator that has
     * seen no data.
     */
    public OnlineNormalEstimator() {
        /* intentionally blank */
    }

    /**
     * Add the specified value to the collection of samples for this
     * estimator.
     *
     * @param x Value to add.
     */
    public void handle(double x) {
        ++mN;
        double nextM = mM + (x - mM) / mN;
        mS += (x - mM) * (x - nextM);
        mM = nextM;
    }

    /**
     * Removes the specified value from the sample set.  See the class
     * documentation above for the algorithm.
     *
     * @param x Value to remove from sample.
     * @throws IllegalStateException If the current number of samples
     * is 0.
     */
    public void unHandle(double x) {
        if (mN == 0L) {
            String msg = "Cannot unhandle after 0 samples.";
            throw new IllegalStateException(msg);
        }
        if (mN == 1L) {
            mN = 0L;
            mM = 0.0;
            mS = 0.0;
            return;
        }
        double mOld = (mN * mM - x)/(mN - 1L);
        mS -= (x - mM) * (x - mOld);
        mM = mOld;
        --mN;
    }

    /**
     * Returns the number of samples seen by this estimator.
     *
     * @return The number of samples seen by this estimator.
     */
    public long numSamples() {
        return mN;
    }

    /**
     * Returns the mean of the samples.
     *
     * @return The mean of the samples.
     */
    public double mean() {
        return mM;
    }

    /**
     * Returns the maximum likelihood estimate of the variance of
     * the samples.
     *
     * @return Maximum likelihood variance estimate.
     */
    public double variance() {
        return mN > 1 ? mS/mN : 0.0;
    }

    /**
     * Returns the unbiased estimate of the variance of the samples.
     *
     * @return Unbiased variance estimate.
     */
    public double varianceUnbiased() {
        return mN > 1 ? mS/(mN-1) : 0.0;
    }

    /**
     * Returns the maximum likelihood estimate of the standard deviation of
     * the samples.
     *
     * @return Maximum likelihood standard deviation estimate.
     */
    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    /**
     * Returns the unbiased estimate of the standard deviation of the samples.
     *
     * @return Unbiased standard deviation estimate.
     */
    public double standardDeviationUnbiased() {
        return Math.sqrt(varianceUnbiased());
    }

    /**
     * Returns a string-based representation of the mean and
     * standard deviation and number of samples for this estimator.
     *
     * @return String-based representation of this estimator.
     */
    public String toString() {
        return "Norm("
            + "mean=" + mean()
            + ", stdDev=" + standardDeviation()
            + ")[numSamples=" + numSamples() + "]";
    }

}