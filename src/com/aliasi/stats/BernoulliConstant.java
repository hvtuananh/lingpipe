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
 * A <code>BernoulliConstant</code> implements a Bernoulli
 * distribution with a constant probability of success.
 * 
 * @author Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class BernoulliConstant extends BernoulliDistribution {

    private final double mSuccessProbability;

    /**
     * Construct a constant Bernoulli distribution with
     * the specified success probability.
     *
     * @param successProbability Probability of success for this
     * Bernoulli distribution.
     * @throws IllegalArgumentException If the specified probability
     * is not between 0.0 and 1.0 inclusive.
     */
    public BernoulliConstant(double successProbability) {
    validateProbability(successProbability);
    mSuccessProbability = successProbability;
    }

    /**
     * Returns the constant success probability for this distribution.
     *
     * @return The constant success probability for this distribution.
     */
    @Override
    public double successProbability() {
    return mSuccessProbability;
    }

}

