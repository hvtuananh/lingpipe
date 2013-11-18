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
 * A <code>BernoulliDistribution</code> is a multivariate distribution
 * with two outcomes, 0 (labeled "failure") and 1 (labeled "success").
 * A Bernoulli distribution is the basis of the binomial distribution.
 *
 * <P>For more information, see: 
 *
 * <UL> 
 * <LI> Eric W. Weisstein.  <a
 * href="http://mathworld.wolfram.com/BernoulliDistribution.html">Bernoulli
 * Distribution</a>.  From <i>MathWorld</i>--A Wolfram Web Resource.
 * </UL>
 *
 * @author Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public abstract class BernoulliDistribution 
    extends MultivariateDistribution {

    /**
     * Construct a Bernoulli distribution.
     */
    public BernoulliDistribution() { 
        /* do nothing */
    }

    /**
     * Returns one, the maximum outcome for a Bernoulli distribution.
     *
     * @return The long integer one.
     */
    @Override
    public long maxOutcome() {
        return 1l;
    }


    /**
     * Returns two, the number of dimensions for a Bernoulli
     * distribution.
     *
     * @return Two, the number of dimensions for a Bernoulli
     * distribution.
     */
    @Override
    public int numDimensions() {
        return 2;
    }

    /**
     * Returns the variance of this Bernoulli distribution.  Applying
     * the general definition of variance to a Bernoulli distribution
     * yields:
     *
     * <code><blockquote>
     *  variance = P(success) * (1 - P(success))
     * </blockquote></code>
     *
     * @return The variance of this distribution.
     */
    @Override
    public double variance() {
        double successProb = successProbability();
        return successProb * (1.0 - successProb);
    }

    /**
     * Returns the probability of the specified outcome.  This method
     * is implemented to return one minus the success probability for
     * outcome zero, the success probabilty for outcome one, and zero for
     * all other outcomes.
     */
    @Override
    public double probability(long outcome) {
        if (outcome == 0l)
            return 1.0 - successProbability();
        else if (outcome == 1l)
            return successProbability();
        else return 0.0;
    }

    /**
     * Returns the failure label for outcome zero, and the success
     * label for outcome one.  The label for zero is {@link
     * #FAILURE_LABEL} and the label for one is {@link #SUCCESS_LABEL}
     * respectively.
     *
     * @param outcome Outcome whose label is returned.
     * @return The label for the outcome.
     * @throws IllegalArgumentException If the outcome is out of
     * range.
     */
    @Override
    public String label(long outcome) {
        if (outcome == 0l) return FAILURE_LABEL;
        if (outcome == 1l) return SUCCESS_LABEL;
        String msg = "Only outcomes 0 and 1 have labels."
            + " Found outcome=" + outcome;
        throw new IllegalArgumentException(msg);
    }

    /**
     * Returns the success probability for this distribution.  This
     * method must be defined by concrete subclasses, and will be
     * used to define {@link #probability(long)}.
     *
     * @return The probability of success for this distribution.
     */
    public abstract double successProbability();

    /**
     * The label for dimension zero: <code>&quot;failure&quot;</code>.
     */
    public static final String FAILURE_LABEL = "failure";

    /**
     * The label for dimension one: <code>&quot;success&quot;</code>.
     */
    public static final String SUCCESS_LABEL = "success";

}
