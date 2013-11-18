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
 * A <code>DiscreteDistribution</code> provides a probability
 * distribution over long integer outcomes.  Mathematically, such a
 * distribution defines a discrete-valued random variable.
 *
 * <P>Discrete probability distributions return values between
 * <code>0.0</code> and <code>1.0</code> inclusive for outcomes.  The
 * sum of the probabilities over all integers should be
 * <code>1.0</code>, but it may be less than <code>1.0</code> for the
 * sum of all integers representable as longs (64 bits).  Discrete
 * distributions are also required to return log (base 2)
 * probabilities to support probabilities very close to 0.0 or 1.0.
 *
 * <P>Cumulative probabilities may be calculated over discrete
 * distributions.  A cumulative probabilty is a sum of probabilities
 * within a given range.  
 *
 * <P>Discrete distributions optionally implement methods to return
 * their mean, variance and entropy.  Discrete distributions are
 * required to indicate the minimum and maximum outcome with non-zero
 * probability.  This allows cumulative probabilities, means,
 * variances and entropies to be computed by iterating over values in
 * range.  If there are no minimum or maximum values, these methods
 * should return the minimum and maximum long values respectively.
 *
 * <P>For more information, see: 
 * <UL> 
 * <LI> Eric  W. Weisstein. 
 * <a href="http://mathworld.wolfram.com/DiscreteDistribution.html">Discrete Distribution</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource. 
 * </UL>
 * 
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public interface DiscreteDistribution {

    /**
     * Returns the probability of the specified outcome.
     *
     * @param outcome The discrete outcome.
     * @return The probability of the outcome in this distribution.
     */
    public double probability(long outcome);

    /**
     * Returns the log (base 2) probability of the specified outcome.
     *
     * @param outcome The discrete outcome.
     * @return The log (base 2) probability of the outcome in this
     * distribution.
     */
    public double log2Probability(long outcome);

    /**
     * Returns the probability an outcome will be less than or
     * equal to the specified outcome.  Implemented by calling
     * the cumulative probability method with the minimum long
     * value as lower bound and specified outcome as upper bound.
     *
     * @param upperBound Upper bound of the outcome.
     * @return The cumulative probability of numbers less than
     * or equal to the upper bound.
     */
    public double cumulativeProbabilityLess(long upperBound);

    /**
     * Returns the probability an outcome will be greater than or
     * equal to the specified outcome.  This method is implemented by
     * calling the two-argument cumulative probability method with the
     * maximum long value as upper bound and specified outcome as
     * lower bound.
     * 
     * @param lowerBound Lower bound of outcomes considered.
     * @return The cumulative probability of numbers greater than
     * or equal to the lower bound.
     */
    public double cumulativeProbabilityGreater(long lowerBound);

    /**
     * Returns the probability that an outcome will fall in the range
     * between the specified lower and upper bounds inclusive.
     *
     * @param lowerBound Lower bound of outcomes considered.
     * @param upperBound Upper bound of the outcome.
     * @return Probability that an outcome will be between the
     * specified minium and maximum inclusive.
     */
    public double cumulativeProbability(long lowerBound, 
                    long upperBound);

    /**
     * Returns the minimum outcome with non-zero probability.
     * Distributions with no minimum outcome should return {@link
     * Long#MIN_VALUE}.
     *
     * @return The minimum outcome with non-zero probability.
     */
    public long minOutcome();
    
    /**
     * Returns the maximum outcome with non-zero
     * probability. Distributions with no maximum should return {@link
     * Long#MAX_VALUE}.
     *
     * @return The minimum outcome with non-zero probability.
     */
    public long maxOutcome();

    /**
     * Returns the mean of this distribution.  Optional operation.
     *
     * @return The mean of this distribution.
     * @throws UnsupportedOperationException If this operation is not
     * supported.
     */
    public double mean();

    /**
     * Returns the variance of this distribution.  Optional operation.
     *
     * @return The variance of this distribution.
     * @throws UnsupportedOperationException If this operation is not
     * supported.
     */
    public double variance();

    /**
     * Returns the entropy of this distribution.  Optional operation.
     *
     * @return The entropy of this distribution.
     * @throws UnsupportedOperationException If this operation is not
     * supported.
     */
    public double entropy();

}
