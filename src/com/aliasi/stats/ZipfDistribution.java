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
 * The <code>ZipfDistribution</code> class provides a finite
 * distribution parameterized by a positive integer number of outcomes
 * with outcome probability inversely proportional to the rank of
 * the outcome (ordered by probablity).  Many natural language
 * phenomena such as unigram word probabilities and named-entity
 * probabilities follow roughly a Zipf distribution.
 *
 * <P> The Zipf probability distribution
 * <code>Zipf<sub><sub>n</sub></sub></code> with <code>n</code>
 * outcomes is defined by assigning a probability to the
 * rank <code>r</code> outcome, for <code>1<=r<=n</code>, by:
 *
 * <blockquote><code>
 *   Zipf<sub><sub>n</sub></sub>(r) = (1/r)/Z<sub><sub>n</sub></sub>
 * </code></blockquote>
 *
 * where <code>Z<sub><sub>n</sub></sub></code> is the normalizing factor
 * for a Zipf distribution with <code>n</code> outcomes:
 *
 * <blockquote><code>
 * Z<sub><sub>n</sub></sub>
 * = <big><big>&Sigma;</big></big><sub><sub>1<=j<=n</sub></sub>
 *   1/j
 * </code></blockquote>
 *
 * <P>The Zipf distribution class provides a method for returning the
 * entropy of the Zipf distribution.  It also provides a static
 * method for returning a Zipf distribution's probabilities in
 * rank order.  This latter method is useful for comparing observed
 * distributions to that expected from a Zipf distribution.
 *
 * <P>For more information, see: 
 * <UL> 
 * <LI> Eric  W. Weisstein. 
 * <a href="http://mathworld.wolfram.com/ZipfsLaw.html">Zipf's Law</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource. 
 * <LI> Eric  W. Weisstein. 
 * <a href="http://mathworld.wolfram.com/StatisticalRank.html">Statistical Rank</a>.
 * From <i>MathWorld</i>--A Wolfram Web Resource. 
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class ZipfDistribution extends AbstractDiscreteDistribution {
    
    private final double[] mEstimates;

    /**
     * Construct a Constant Zipf distribution with the specified number of
     * outcomes.
     *
     * @param numOutcomes Number of outcomes for the distribution.
     * @throws IllegalArgumentException If the number of outcomes
     * specified is not positive.
     */
    public ZipfDistribution(int numOutcomes) {
    if (numOutcomes <= 0) {
        String msg = "Number of outcomes must be postive."
        + " Found numOutcomes=" + numOutcomes;
        throw new IllegalArgumentException(msg);
    }
    mEstimates = zipfDistribution(numOutcomes);
    }

    /**
     * Returns one, the minimum outcome in a Zipf distribution.
     *
     * @return One.
    */
    @Override
    public long minOutcome() {
    return 1l;
    }

    /**
     * Returns the maximum outcome, which is just the number of
     * outcomes.
     *
     * @return The maximum non-zero outcome.
     */
    @Override
    public long maxOutcome() {
    return mEstimates.length;
    }

    /**
     * Returns the number of non-zero outcomes for this Zipf
     * distribution.
     *
     * @return The number of non-zero outcomes for this distributioni.
     */
    public int numOutcomes() {
    return mEstimates.length;
    }

    /**
     * Returns the probability of the outcome at the specified rank.
     * This method returns <code>0.0</code> for non-positive ranks or
     * ranks greater than the number of ranks in this distribution.
     *
     * @param rank Rank of outcome.
     * @return The probability of the outcome at the specified rank.
     */
    @Override
    public double probability(long rank) {
    if (rank <= 0 || rank >  mEstimates.length) 
        return 0.0;
    return mEstimates[((int)rank)-1];
    }

    /**
     * Returns the array of probabilities indexed by rank for the Zipf
     * distribution with the specified number of outcomes.  See the
     * class documentation above for a definition of these
     * probabilities.  Note that the index of the outcome will be
     * one less than its rank; for example, the rank 1 outcome's
     * probability is at index 0, the rank 5 outcome's probabilty
     * at index 4.
     *
     * @param numOutcomes Number of outcomes.
     * @return The array of probabilities indexed by rank for
     * the Zipf distribution with the specified number of outcomes.
     */
    public static double[] zipfDistribution(int numOutcomes) {
    // ratio of prob for rank n = 1/n
    double[] ratio = new double[numOutcomes];
    for (int i = 1; i <= numOutcomes; ++i)
        ratio[i-1] = 1.0 / (double) i;
    // compute normalizing sum
    double sum = 0.0;
    for (int i = 0; i < ratio.length; ++i)
        sum += ratio[i];
    // normalize ratios
    for (int i = 0; i < ratio.length; ++i)
        ratio[i] /= sum;
    return ratio;
    }
        

}
