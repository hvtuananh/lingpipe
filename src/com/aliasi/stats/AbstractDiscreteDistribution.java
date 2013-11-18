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
 * An <code>AbstractDiscreteDistribution</code> provides a default
 * abstract implementation of discrete distributions.  Concrete
 * subclasses need only implement the {@link #probability(long)}
 * method, which returns the probability for each outcome.
 *
 * <P>The method {@link #minOutcome()} and {@link #maxOutcome()} bound
 * the range of non-zero probabilities.  They default to {@link
 * Long#MIN_VALUE} and {@link Long#MAX_VALUE} respectively. Concrete
 * subclasses should implement the tightest possible bounds for these
 * methods, because cumulative probabilities, means, variances and
 * entropies are implemented by looping between the minimum and
 * maximum values and evaluating the probability at each point.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public abstract class AbstractDiscreteDistribution
    implements DiscreteDistribution {

    /**
     * Construct an abstract discrete distribution.
     */
    public AbstractDiscreteDistribution() {
        /* do nothing */
    }

    /**
     * Returns the probability of the specified outcome in this
     * distribution.  This abstract method is the only one that needs
     * to be implemented by subclasses, though most will also override
     * the minimum and maximum outcome methods.
     *
     * @param outcome Outcome whose probability is returned.
     * @return Probability of specified outcome.
     */
    public abstract double probability(long outcome);


    /**
     * Returns the cumulative probability of all outcomes less
     * than or equal to the specified upper bound.  This method
     * is implemented by looping over all values within the specified
     * range and within the minimum and maximum outcome bounds.
     *
     * @param upperBound Upper bound of outcome.
     * @return The cumulative probability of all outcomes less
     * than or equal to the specified upper bound.
     */
    public double cumulativeProbabilityLess(long upperBound) {
        return cumulativeProbability(Long.MIN_VALUE,upperBound);
    }

    /**
     * Returns the cumulative probability of all outcomes greater than
     * or equal to the specified lower bound.  This method is
     * implemented by looping over all values within the specified
     * range and within the minimum and maximum outcome bounds.
     *
     * @param lowerBound Lower bound on outcomes.
     * @return The cumulative probability of all outcomes less
     * than or equal to the specified upper bound.
     */
    public double cumulativeProbabilityGreater(long lowerBound) {
        return cumulativeProbability(lowerBound,Long.MAX_VALUE);
    }

    /**
     * Returns the cumulative probability of all outcomes between the
     * specified bounds, inclusive.  This method is implemented by
     * looping over all outcomes within range of the specified bounds
     * and within the minimum and maximum outcomes for this distribution.
     *
     * @param lowerBound Lower bound of outcome set.
     * @param upperBound Upper bound of outcome set.
     * @return The cumulative probability of all outcomes between
     * the bounds, inclusive.
     */
    public double cumulativeProbability(long lowerBound, long upperBound) {
        double sum = 0.0;
        long start = Math.max(lowerBound,minOutcome());
        long last = Math.min(upperBound,maxOutcome());
        for (long i = start; i <= last; ++i)
            sum += probability(i);
        return sum;
    }

    /**
     * Returns the log (base 2) probability of the specified outcome.
     * Implemented by taking the log of the probability estimate.
     *
     * @param outcome Outcome whose log probability is returned.
     * @return Log (base 2) probability of the specified outcome.
     */
    public double log2Probability(long outcome) {
        return com.aliasi.util.Math.log2(probability(outcome));
    }

    /**
     * Returns the minimum outcome with non-zero probability for this
     * distribution.  Implemented to return the constant {@link
     * Long#MIN_VALUE}.  If possible, concrete subclasses should
     * override this method with a tighter bound.
     *
     * @return The minimum outcome for this distribution.
     */
    public long minOutcome() {
        return Long.MIN_VALUE;
    }

    /**
     * Returns the maximum outcome with non-zero probability for this
     * distribution.  Implemented to return the constant {@link
     * Long#MAX_VALUE}.  If possible, concrete subclasses should
     * override this method with a tighter bound.
     *
     * @return The maximum outcome for this distribution.
     */
    public long maxOutcome() {
        return Long.MAX_VALUE;
    }

    /**
     * Returns the mean of this distribution.  This is implemented as
     * a weighted sum of probabilities over the outcomes within the
     * minimum and maximum for this distribution.
     *
     * @return Mean of this distribution.
     */
    public double mean() {
        double mean = 0.0;
        long maxOutcome = maxOutcome();
        for (long i = minOutcome(); i <= maxOutcome; ++i)
            mean += ((double)i)*probability(i);
        return mean;
    }

    /**
     * Returns the variance of this distribution.  This is implemented
     * by first computing the mean and then looping over outcomes
     * between the minimum and maximum and summing the squared
     * differences between outcomes and the mean, weighted by outcome
     * probability.
     *
     * @return Variance of this distribution.
     */
    public double variance() {
        double mean = mean();
        double variance = 0.0;
        long maxOutcome = maxOutcome();
        for (long i = minOutcome(); i <= maxOutcome; ++i) {
            double diff = (mean - (double)i);
            variance += probability(i) * diff * diff;
        }
        return variance;
    }

    /**
     * Returns the entropy of this distribution in bits (log 2).
     * Recall that entropy in bits (base 2) is defined by:
     *
     * <blockquote><code>
     *  H(P) = - <big><big>&Sigma;</big></big><sub><sub>x</sub></sub>
     *         P(x) * log<sub><sub>2</sub></sub> P(x)
     * </code></blockquote>
     *
     * This method is implemented by iterating over the outcomes
     * between the minimum and maximum and summing their negative
     * probability weighted log probabilities.
     *
     * @return The entropy of this distribution.
     */
    public double entropy() {
        double sum = 0.0;
        long maxOutcome = maxOutcome();
        for (long i = minOutcome(); i <= maxOutcome; ++i) {
            sum += probability(i) * log2Probability(i);
        }
        return -sum;
    }


    void checkOutcome(long outcome) {
        if (outcomeOutOfRange(outcome)) {
            String msg = "Outcome must be in range.  Minimum = 0."
                + " Maximum=" + maxOutcome()
                + " Found outcome=" + outcome;
            throw new IllegalArgumentException(msg);
        }
    }

    boolean outcomeOutOfRange(long outcome) {
        return outcome < 0l
            || outcome > maxOutcome();
    }

    void validateProbability(double p) {
        if (p >= 0.0 && p <= 1.0) return;
        String msg = "Probabilities must be between 0 and 1 inclusive."
            + " Found probability=" + p;
        throw new IllegalArgumentException(msg);
    }


}
