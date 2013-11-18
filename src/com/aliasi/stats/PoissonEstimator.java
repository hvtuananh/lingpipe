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

import java.io.IOException;
import java.io.ObjectOutput;

/**
 * A <code>PoissonEstimator</code> implements the maximum likelihood
 * Poisson distribution given training events.  The training events
 * are simply in the form of long integer outcomes.  The rate
 * parameter for the unbiased maximum likelihood estimator is given by
 * the mean of the training samples.  
 * likelihood unbiased estimator
 * 
 * <P>If there have been no training events, or if all training events
 * have 0 values, an illegal state exception is thrown by
 * <code>lambda()</code> and <code>log2Prob()</code>. 
 *
 * <P>The method {@link #compileTo(ObjectOutput)} writes a compiled
 * version of this distribution to the specified output.  Reading it
 * back in will produce a constant extension of {@link
 * PoissonDistribution}.  Poisson estimators are also serializable and
 * the estimator read back in will have the same state as the one
 * written out.
 *
 * @author  Bob Carpenter
 * @version 2.4
 * @since   LingPipe2.0
 */
public class PoissonEstimator extends PoissonDistribution {

    private double mSum = 0l;
    private double mNumSamples = 0l;

    /**
     * Construct a Poisson estimator.
     */
    public PoissonEstimator() { 
        /* nothing to init */
    }

    /**
     * Construct a Poisson estimator with a prior set by the specified
     * number of samples and mean value.  The combined effect will be
     * as if the specified number of samples had be trained resulting
     * the specified mean.  Further training instances add to the 
     *
     * @param priorNumSamples The initial number of samples given by
     * the prior.
     * @param priorMean The initial mean.
     * @throws IllegalArgumentException If either number is not
     * positive and finite.
     */
    public PoissonEstimator(double priorNumSamples,
                            double priorMean) {
        if (priorMean <= 0.0 
            || Double.isNaN(priorMean) 
            || Double.isInfinite(priorMean)) {
            String msg = "Prior mean must be finite and positive."
                + " Found priorMean=" + priorMean;
            throw new IllegalArgumentException(msg);
        }
        if (priorNumSamples <= 0.0
            || Double.isNaN(priorNumSamples)
            || Double.isInfinite(priorNumSamples)) {
            String msg = "Prior number of samples must be finite and positive."
                + " Found priorNumSamples=" + priorNumSamples;
            throw new IllegalArgumentException(msg);
        }
        mSum = priorMean * priorNumSamples;
        mNumSamples = priorNumSamples;
    }

    /**
     * Add the specified sample to the collection of training data.
     * The sample must be a number greater than or equal to zero.  If
     * adding the sample to the running sum would cause overflow, it
     * is not added and an illegal state exception is thrown instead.
     * If overflow is a problem, samples and the resulting rates may
     * be scaled down.
     *
     * @param sample Sample to add to the training data.
     * @throws IllegalArgumentException If the sample is less than
     * @throws IllegalStateException If the sample would overflow the
     * running sum of samples.
     */
    public void train(long sample) {
        train(sample,1);
    }

    /**
     * Add the specified sample to the collection of training data
     * with the specified weight.  The sample must be a number greater
     * than or equal to zero.  Training weights must be greater than
     * zero and not infinite. 
     * 
     * <p>If adding the sample to the running sum
     * would cause overflow, it is not added and an illegal state
     * exception is thrown instead.  If overflow is a problem, samples
     * and the resulting rates may be scaled down.
     *
     * @param sample Sample to add to the training data.
     * @throws IllegalArgumentException If the sample is less than
     * @throws IllegalStateException If the sample would overflow the
     * running sum of samples.
     */
    public void train(long sample, double weight) {
        if (sample < 0) {
            String msg = "Poisson distributions only have positive outcomes."
                + " Found training sample=" + sample;
            throw new IllegalArgumentException(msg);
        }
        if (weight < 0 || Double.isNaN(weight) || Double.isInfinite(weight)) {
            String msg = "Training weights must be finite and positive."
                + " Found weight=" + weight;
            throw new IllegalArgumentException(msg);
        }
        if (Long.MAX_VALUE - mSum < sample) {
            String msg = "Adding last sample overflows the event sum."
                + " Sum so far=" + mSum
                + " Number of training samples=" + mNumSamples;
            throw new IllegalStateException(msg);
        }
        mSum += sample*weight;
        mNumSamples += weight;
    }

    /**
     * Returns the mean for this estimator.  This is simply the
     * mean of the training samples.
     *
     * @return Rate parameter for this distribution.
     * @throws IllegalStateException If there have been no training
     * instances or all training instances had value 0, an illegal
     * state exception is thrown.
     */
    @Override
    public double mean() {
        if (mSum <= 0.0) {
            String msg = (mNumSamples == 0)
                ? "No samples provided."
                : "Only zero samples provided.";
            throw new IllegalStateException(msg);
        }
        return mSum/mNumSamples;
    }

    /**
     * Writes a constant Poisson distribution with the same mean
     * as the current value of this Poisson distribution's mean.
     *
     * @param objOut Object output to which a compiled version of this
     * distribution is written.
     * @throws IllegalStateException If there have been no training
     * instances or all training instances had value 0, an illegal
     * state exception is thrown.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        PoissonConstant dist = new PoissonConstant(mean());
        dist.compileTo(objOut);
    }

}
