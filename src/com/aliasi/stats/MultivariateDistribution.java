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
 * A <code>MultivariateDistribution</code> implements a discrete
 * distribution over a finite set of outcomes numbered consecutively
 * from zero.  The total number of outcomes is given by the abstract
 * method {@link #numDimensions()}.  The minimum outcome is zero and
 * the maximum outcome is the number of dimensions minus one.
 * Concrete subclasses must also implement the method {@link
 * #probability(long)}.
 * 
 * <P>Outcomes in multivariate distributions are labeled by strings.
 * The method {@link #label(long)} returns the label for an outcome.
 * The inverse method {@link #outcome(String)} maps labels to
 * outcomes.  The default implementation in this class provides labels
 * defined by converting the long integer outcomes to strings.
 * Subclasses may override these methods (together) to implement a
 * more meaningful notion of label.
 *
 * <P>Note that the multivariate distribution forms the basis of the
 * mulitnomial distribution.  The Bernoulli distribution is a special
 * case of the multivariate distribution with two outcomes.
 *
 * <P>For more information, see: 
 * <UL> 
 * <LI> Eric  W. Weisstein. 
 * <a href="http://mathworld.wolfram.com/MultivariateDistribution.html">Multivariate Distribution</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource. 
 * </UL>
 * 
 * @author Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public abstract class MultivariateDistribution 
    extends AbstractDiscreteDistribution {

    /**
     * Construct a multivariate distribution.
     */
    public MultivariateDistribution() { 
        /* do nothing */
    }

    /**
     * Returns zero, the minimum outcome with non-zero probability for
     * a multivariate distribution.
     *
     * @return Zero.
     */
    @Override
    public long minOutcome() {
        return 0l;
    }

    /**
     * Returns the maximum outcome with non-zero probability for a
     * multivariate distribution.  This method returns the number of
     * dimensions as specified by {@link #numDimensions()} minus one.
     *
     * @return The maximum outcome with non-zero probability for this
     * distribution.
     */
    @Override
    public long maxOutcome() {
        return numDimensions()-1;
    }

    /**
     * Return the outcome for the specified label.  The default
     * implementation is to return the result of applying the method
     * {@link Long#parseLong(String)} to the specified label.  If the
     * label is not a number, <code>-1</code> is returned.
     * 
     * @param label Label whose outcome is returned.
     * @return The outcome for the specified label.
     */
    public long outcome(String label) {
        try {
            long outcome = Long.valueOf(label);
            if (outcomeOutOfRange(outcome)) 
                return -1l;
            return outcome;
        } catch (NumberFormatException e) {
            return -1l;
        }
    }

    /**
     * Return the label for the specified outcome.  The default
     * implementation in this class is to return the result of {@link
     * Long#toString(long)} applied to the outcome.
     *
     * @param outcome Outcome whose label is returned.
     * @return The label for the specified outcome.
     * @throws IllegalArgumentException If the outcome index is out of range.
     */
    public String label(long outcome) {
        checkOutcome(outcome);
        return Long.toString(outcome);
    }

    /**
     * Returns the probability of the outcome specified by label.  If
     * there is no known outcome with the specified label, this method
     * will return <code>0.0</code>.
     *
     * @param label Label of outcome.
     * @return The probability of the outcome specified by label.
     */
    public double probability(String label) {
        return probability(outcome(label));
    }

    /**
     * Returns the log (base 2) probability of the outcome specified
     * by label.  If there is no known outcome with the specified
     * label, this method will return
     * <code>Double.NEGATIVE_INFINITY</code>.
     *
     * @param label Label of outcome.
     * @return The log probability of the outcome specified by label.
     */
    public double log2Probability(String label) {
        return log2Probability(outcome(label));
    }

    
    /**
     * Returns the number of dimensions of this multivariate distribution.
     * Note that this must be a positive number.  
     * 
     * @return The number of dimensions for this distribution.
     */
    public abstract int numDimensions();

    /**
     * Return the probability of the specified outcome in
     * this multivariate distribution. 
     *
     * @param outcome Outcome whose probability is returned.
     * @return The probability of the specified outcome.
     */
    @Override
    public abstract double probability(long outcome);


}
