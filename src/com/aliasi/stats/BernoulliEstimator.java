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

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

/**
 * A <code>BernoulliEstimator</code> provides a maximum likelihood
 * estimate of a Bernoulli distribution.  Training samples are
 * provided through the method {@link #train(boolean,int)} specifying
 * success or failure and the number of samples.  An unbiased
 * estimator for a Bernoulli distribution's probability of success is
 * simply the percentage of successes.
 *
 * @author Bob Carpenter
 * @version 2.4
 * @since   LingPipe2.0
 */
public class BernoulliEstimator 
    extends BernoulliDistribution 
    implements Compilable {

    long mTotalCount;
    long mSuccessCount;

    /**
     * Construct a Bernoulli estimator with zero counts.
     */
    public BernoulliEstimator() { 
        /* do nothing */
    }

    /**
     * Train this estimator with the specified number of samples
     * for success or failure as specified.
     *
     * @param success A flag for whether the training samples
     * are for success or failure.
     * @param numSamples Number of samples to train.
     */
    public void train(boolean success, int numSamples) {
        mTotalCount += numSamples;
        if (success) mSuccessCount += numSamples;
    }

    /**
     * Trains the estimator with one sample that is specified as
     * a success or failure.
     *
     * @param success Flag for whether the sample was a success
     * or a failure.
     */
    public void train(boolean success) {
        train(success,1);
    }

    /**
     * Returns the maximum likelihood estimate of success from
     * the training samples provided.
     *
     * @return The maximum likelihood estimate of success from
     * the training samples provided.
     */
    @Override
    public double successProbability() {
        return ((double) mSuccessCount) / (double) mTotalCount;
    }

    /**
     * Returns the number of training samples provided for this
     * estimator.
     *
     * @return The number of training samples provided for this
     * estimator.
     */
    public long numTrainingSamples() {
        return mTotalCount;
    }

    /**
     * Returns the number of training samples for the specified
     * outcome, success or failure.
     *
     * @param success Flag indicating whether outcome is success
     * or failure.
     * @return Count of training samples with specified success.
     */
    public long numTrainingSamples(boolean success) {
        return success ? mSuccessCount : (mTotalCount - mSuccessCount);
    }

    /**
     * Compiles this Bernoulli estimator to the specified object
     * output.  The corresponding read will produce an instance of
     * {@link BernoulliConstant} with the same success probability as
     * the estimate derived from this estimator.
     *
     * @param objOut Object output to which this Bernoulli distribution
     * is written.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = -3979523774865702910L;
        final BernoulliEstimator mDistro;
        public Externalizer() { mDistro = null; }
        public Externalizer(BernoulliEstimator distro) {
            mDistro = distro;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeDouble(mDistro.successProbability());
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            double successProb = in.readDouble();
            return new BernoulliConstant(successProb);
        }
    }

    
}
