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
 * An <code>AnnealingSchedule</code> instance implements a method to
 * return the learning rate for a specified epoch.  It also has a
 * method to receive callback reports on the resulting error rate for
 * an epoch so that implementations may adapt the learning rate to
 * received error.
 *
 * <p>This class provides an abstract base class implementation along
 * with static factory methods to create the two most popular annealing
 * schedules, constant, exponential decay and inverse scaling.
 *
 *
 * <h4>Constant Learning Rate</h4>
 *
 * <p>The contant learning rate schedule always returns the same learning
 * rate, which is fixed at construction time.

 * <h4>Exponential Decay</h4>
 *
 * <p>The exponential decay annealing schedule sets the learning
 * rate as follows based on a specfieid exponential base:
 *
 * <blockquote><pre>
 * learningRate(epoch) = initialLearningRate * java.lang.Math.pow(base,epoch)</pre></blockquote>
 *
 * Under this schedule, the learning rate undergoes exponential decay
 * starting at the initial learning rate and decaying exponentially at
 * a rate determined by the base of the exponent.
 *
 * <p>The exponential learning rate can find solutions quickly in
 * fairly well-behaved spaces, but may stop short of the minimum
 * error solution due to too much decay in later epochs.
 *
 * <h4>Inverse Scaling</h4>
 *
 * <p>The inverse learning rate scaling sets the learning rate as:
 *
 * <blockquote><pre>
 * learningRate(epoch) = initialLearningRate / (1 + epoch/annealingRate)</pre></blockquote>
 *
 * The inverse scaling annealing schedule lowers the rate more quickly
 * than the exponential rates initially and then more slowly for later
 * epochs.
 *
 * <p>This is a popular learning rate because it is guaranteed to
 * converge in the limit.  It can be slower to converge once it
 * gets near a solution than exponential decay.
 *
 *
 * <h4>Rejecting Updates</h4>
 *
 * The return value of the <code>receivedError()</code> method is a
 * boolean flag indicating whether to accept the updates to the
 * underlying vectors or not.  This allows sampling-based annealing
 * schedules to be implemented that evaluate several learning
 * rates and accept just the one with the most error reduction.
 *
 * <p>The method {@link #allowsRejection()} should be overridden
 * to return {@code false} if the annealing schedule never rejects
 * updates; this will save a coefficient vector copy per epoch
 * in logistic regression.
 *
 * <h4>Convergence Guarantees in the Limit</h4>
 *
 * <p>The inverse scale metric is popular because it is theoretically
 * guaranteed to converge in the limit.
 *
 * <p>An annealing rate will converge in the limit within arbitrary
 * precision of a solution if the learning rate satisfies:
 *
 * <blockquote><pre>
 * <big><big><big>&Sigma;</big></big></big><sub>epoch</sub> learningRate(epoch) = &#8734;</pre></blockquote>
 *
 * and:
 *
 * <blockquote><pre>
 * <big><big><big>&Sigma;</big></big></big><sub>epoch</sub> learningRate(epoch)<sup>2</sup> &lt; &#8734;</pre></blockquote>
 * <h4>References</h4>
 * <ul>
 * <li>Wikipedia: <a href="http://en.wikipedia.org/wiki/Simulated_annealing">Simulated Annealing</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe3.5
 */
public abstract class AnnealingSchedule {

    /**
     * Do-nothing constructor to be used by concrete implementations.
     */
    public AnnealingSchedule() { /* empty construx */ }


    /**
     * Return {@code true} if this annealing schedule allows
     * updates to be rejected.  The implementation in this
     * class always returns {@code true} for backward compatibility
     * reasons.  Implementations that never reject updates
     * should override this method to return {@code false}.
     *
     * @return {@code true} if this schedule allows update rejections.
     */
    public boolean allowsRejection() {
        return true;
    }


    /**
     * Return the learning rate for the specified epoch.
     *
     * @param epoch Epoch being evaluated.
     * @return Learning rate for that epoch.
     */
    abstract public double learningRate(int epoch);

    /**
     * Receive a report from an optimizer about the effect of the
     * specified learning rate in the specified epoch and return
     * <code>true</code> if the update producing the error should
     * be accepted or rejected.
     *
     * <p>This abstract class's implementation of this method is to do
     * nothing.  Concrete subclasses which adapt learning rates based
     * on empirical error reports from the optimizer must override
     * this method.
     *
     * @param epoch Training epoch.
     * @param rate Training rate.
     * @param error Training error.
     */
    public boolean receivedError(int epoch, double rate, double error) {
            return true;
    }

    /**
     * Return the inverse annealing schedule with the specified
     * initial learning rate and annealing rate.  The inverse annealing
     * schedule is not adaptive.
     * adaptation.
     *
     * @param initialLearningRate Initial learning rate for epoch zero.
     * @param annealingRate Rate at which initial learning rate anneals.
     * @return The inverse annealing schedule for the specified initial learning
     * rate and annealing rate.
     * @throws IllegalArgumentException If the initial learning rate or
     * the annealing rates are not finite and positive.
     */
    public static AnnealingSchedule inverse(double initialLearningRate,
                                            double annealingRate) {
        return new InverseAnnealingSchedule(initialLearningRate,annealingRate);
    }


    /**
     * Return the exponential annealing schedule with the specified
     * initial learning rate and exponent.  The exponential annealing
     * schedule is not adaptive.
     *
     * @param initialLearningRate Initial learning rate for epoch 0.
     * @param base Base of the exponential decay.
     * @return The exponential annealing schedule for the specified initial
     * learning rate and exponent.
     * @throws IllegalArgumentException If the initial learning rate is
     * not finite and positive, or if the exponent is not between 0.0 (exclusive)
     * and 1.0 (inclusive).
     */
    public static AnnealingSchedule exponential(double initialLearningRate,
                                                double base) {
        return new ExponentialAnnealingSchedule(initialLearningRate,base);
    }

    /**
     * Return the annealing schedule for the specified constant learning
     * rate.  The constant annealing schedule is not adaptive.
     *
     * @param learningRate The constant rate returned by this
     * annealing schedule.
     * @return The annealing schedule returning the specified constant
     * learning rate.
     * @throws IllegalArgumentException If the learning rate is not
     * finite and positive.
     */
    public static AnnealingSchedule constant(double learningRate) {
        return new ConstantAnnealingSchedule(learningRate);
    }



    static void verifyFinitePositive(String varName, double val) {
        if (Double.isNaN(val)
            || Double.isInfinite(val)
            || val <= 0.0) {
            String msg = varName + " must be finite and positive."
                + " Found " + varName + "=" + val;
            throw new IllegalArgumentException(msg);
        }
    }

    static abstract class AnnealingScheduleImpl extends AnnealingSchedule {
        final double mInitialLearningRate;
        AnnealingScheduleImpl(double initialLearningRate) {
            verifyFinitePositive("initial learning rate",initialLearningRate);
            mInitialLearningRate = initialLearningRate;
        }
        @Override
        public boolean allowsRejection() {
            return false;
        }
    }

    static class ConstantAnnealingSchedule extends AnnealingScheduleImpl {
        ConstantAnnealingSchedule(double learningRate) {
            super(learningRate);
        }
        @Override
        public double learningRate(int epoch) {
            return mInitialLearningRate;
        }
        @Override
        public String toString() {
            return "AnnealingSchedule.constant(" + mInitialLearningRate + ")";
        }
    }

    static class InverseAnnealingSchedule extends AnnealingScheduleImpl {
        private final double mAnnealingRate;

        InverseAnnealingSchedule(double initialLearningRate,
                                 double annealingRate) {
            super(initialLearningRate);
            verifyFinitePositive("annealing rate",annealingRate);
            mAnnealingRate = annealingRate;
        }
        @Override
        public double learningRate(int epoch) {
            return mInitialLearningRate / (1.0 + epoch/mAnnealingRate);
        }
        @Override
        public String toString() {
            return "Inverse(initialLearningRate=" + mInitialLearningRate
                + ", annealingRate=" + mAnnealingRate + ")";
        }
    }


    static class ExponentialAnnealingSchedule extends AnnealingScheduleImpl {
        private final double mExponentBase;
        ExponentialAnnealingSchedule(double initialLearningRate,
                                     double base) {
            super(initialLearningRate);
            if (Double.isNaN(base)
                || Double.isInfinite(base)
                || base <= 0.0
                || base > 1.0) {
                String msg = "Base must be between 0.0 (exclusive) and 1.0 (inclusive)"
                    + " Found base=" + base;
                throw new IllegalArgumentException(msg);
            }
            mExponentBase = base;
        }
        @Override
        public double learningRate(int epoch) {
            return mInitialLearningRate * java.lang.Math.pow(mExponentBase,epoch);
        }
        @Override
        public String toString() {
            return "Exponential(initialLearningRate=" + mInitialLearningRate
                + ", base=" + mExponentBase + ")";
        }
    }




}
