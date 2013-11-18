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

package com.aliasi.dca;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Matrices;
import com.aliasi.matrix.Vector;

import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * The {@code DiscreteChooser} class implements multinomial
 * conditional logit discrete choice analysis with variables varying
 * over alternatives.
 *
 * <h3>Multinomial Logit Discrete Choice Analysis</h3>
 *
 * This form of discrete choice analysis considers an arbitrary
 * positive number of choices represented as vectors and computes the
 * probability of each choice as a vector product on the multilogit
 * scale.
 *
 * <p>The model is characterized by a coefficient vector
 * <code>&beta;</code>, which is supplied at construction time.
 * Choices are represented as vectors, and there may be any number of
 * them.
 *
 * <p>Suppose the model is presented with
 * <code>N</code> choices represented as vectors
 * <code>&alpha;[0],...,&alpha;[N-1]</code>.  The probability
 * of choice <code>n</code> for {@code 0 <= n < N} is
 *
 * <blockquote><pre>
 * p(n|&alpha;,&beta;) = exp(&alpha;[n] * &beta;) / Z</pre></blockquote>
 *
 * where the asterisk (<code>*</code>) represents vector dot products, and
 * the normalizing constant is defined by summation in the usual way, by
 *
 * <blockquote><pre>
 * Z = <big><big>&Sigma;</big></big><sub><sub>0 &lt;= n &lt; N</sub></sub> exp(&alpha;[n] * &beta;)</pre></blockquote>
 *
 * <p>This model is related to logistic regression in that it is
 * a log-linear model.
 *
 * <h3>No Intercepts</h3>
 *
 * Constant features in the training data will be ignored
 * statistically.  This is because the sum of its gradients will
 * always be 0.  For instance, consider an update in the training
 * data.  If there are four alternatives with probabilities 0.8, 0.1,
 * 0.05, and 0.05 and the first alternative with probability of 0.8 is
 * correct, then the error will be -0.2 for the correct choice and
 * (0.1 + 0.05 + 0.05) = 0.2 for all of the incorrect choices, leading
 * to a total gradient of 0.0.
 *
 * <p>Thus intercepts should not be added to choice vectors and a
 * regression prior with an uninformative intercept should not be
 * used for estimation.
 * 
 * <h3>Independence of Irrelevant Alternatives</h3>
 *
 * The ratio of probabilities between two choices does not change
 * based on the other choices presented.  In econometrics, this is
 * known as the <i>independence of irrelevant alternatives</i> (IIA).
 * It is easy to verify for discrete choosers by computing the ratio
 * between the probability of choice <code>n</code> and choice
 * <code>m</code>,
 *
 * <blockquote><pre>
 * p(n|&alpha;,&beta;)/p(m|&alpha;, &beta;)
 *
 *     = (exp(&alpha;[n] * &beta;) / Z) / (exp(&alpha;[m] * &beta;) / Z)
 *
 *     = exp(&alpha;[n] * &beta;) / exp(&alpha;[m] * &beta;).</pre></blockquote>
 *
 * The value does depends on only <code>&alpha;[n]</code> and
 * <code>&alpha;[m]</code> (and the model's coefficient vector
 * <code>&beta;</code>).
 *
 * <p>This is fine when the choices are independent, but problematic
 * when there are dependencies between the choices.  A standard
 * example of is given a choice between items A and B may be modeled
 * properly.  But consider a B' that is very much like B and added to
 * the mix.  For instance, consider choosing between a California
 * cabernet and a Bordeaux.  Suppose you have a 2/3 probability of
 * choosing the Bordeaux and a 1/3 probability of choosing the
 * California cabernet.  Now consider adding a second Califoronia
 * cabernet that's very similar to the first one (as measured by the
 * model, of course).  Then the probabilities will be roughly 1/2 for
 * choosing the Bordeaux, and 1/4 for each of the California
 * cabernets.  With similar choices, the probability of each should go
 * down.  If they were identical (perfectly correlated), the right
 * answer would seem to be a 2/3 probability of choosing the Bordeaux
 * and 1/6 probability for choosing each of the Califoronia cabernets.
 *
 * <h3>One or More Choosers</h3>
 *
 * If the coefficients all correspond to features of the choice, the
 * model is implicitly representing the decision function of a single
 * chooser.
 *
 * <p>If some of the coefficients are features of a chooser, the model
 * may be used to represent the decision function of multiple
 * choosers.  In this case, all fetaure tying is up to the
 * implementing class.  Typically, there will be interaction features
 * included between the chooser and the choice.  Returning to the wine
 * example, different choosers might put different weights on the
 * degree of new oak used, acid levels, or complexity.  In these
 * cases, overall preferences may be represented by chooser-independent
 * variables, and then chooser-dependent preferences would be
 * interpreted relatively.
 *
 * <h3>References</h3>
 *
 * <ul>
 * <li>Wikipedia: <a href="http://en.wikipedia.org/wiki/Discrete_choice">Discrete Choice Analysis</a></li>
 * </ul>
 *
 * @author  Bob Carpenter
 * @version 3.9.2
 * @since   LingPipe3.9.1
 */
public class DiscreteChooser
    implements Serializable {

    static final long serialVersionUID = 9199242060691577692L;

    private final Vector mCoefficients;

    /**
     * Construct a discrete chooser with the specified
     * coefficient vector.
     *
     * @param coefficients Coefficient vector.
     */
    public DiscreteChooser(Vector coefficients) {
        mCoefficients = coefficients;
    }

    /**
     * Returns the most likely choice among the choices in the
     * specified array of vectors.
     *
     * @param choices Array of alternative choices represented
     * as vectors.
     * @return The most likely choice.
     * @throws IllegalArgumentException If there is not at least one
     * choice.
     */
    public int choose(Vector[] choices) {
        verifyNonEmpty(choices);
        if (choices.length == 1)
            return 0;
        int maxIndex = 0;
        double maxScore = linearBasis(choices[0]);
        for (int i = 1; i < choices.length; ++i) {
            double score = linearBasis(choices[i]);
            if (score > maxScore) {
                maxScore = score;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Return an array of choice probabilities corresponding to the
     * input array of choices.  The array of results will be parallel
     * to the array of input vectors.
     *
     * @param choices Array of alternative choices represented
     * as vectors.
     * @return Parallel array of probabilities for each choice.
     * @throws IllegalArgumentException If there is not at least one
     * choice.
     */
    public double[] choiceProbs(Vector[] choices) {
        verifyNonEmpty(choices);
        double[] scores = choiceLogProbs(choices);
        // scores are log probs
        for (int i = 0; i < scores.length; ++i)
            scores[i] = Math.exp(scores[i]);
        // scores are linear probs
        return scores;
    }

    /**
     * Return an array of (natural) log choice probabilities
     * corresponding to the input array of choices.  The array of
     * results will be parallel to the array of input vectors.
     *
     * @param choices Array of alternative choices represented
     * as vectors.
     * @return Parallel array of choice (natural) log probabilities.
     * @throws IllegalArgumentException If there is not at least one
     * choice.
     */
    public double[] choiceLogProbs(Vector[] choices) {
        verifyNonEmpty(choices);
        double[] scores = new double[choices.length];
        for (int i = 0; i < choices.length; ++i)
            scores[i] = mCoefficients.dotProduct(choices[i]);
        // scores are unnormalized log prob ratios
        double Z = com.aliasi.util.Math.logSumOfExponentials(scores);
        for (int i = 0; i < choices.length; ++i)
            scores[i] -= Z;
        // scores are log probs
        return scores;
    }

    /**
     * Return an unmodifiable view of the coefficients
     * underlying this discrete chooser.
     *
     * @return The coefficient vector for this chooser.
     */
    public Vector coefficients() {
        return Matrices.unmodifiableVector(mCoefficients);
    }

    /**
     * Return a string-based representation of the coefficient
     * vector underlying this discrete chooser.
     *
     * @return String representation of this chooser.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiscreteChoose(");
        int[] nzDims = mCoefficients.nonZeroDimensions();
        for (int i = 0; i < nzDims.length; ++i) {
            int d = nzDims[i];
            if (i > 0)
                sb.append(",");
            sb.append(Integer.toString(d));
            sb.append('=');
            sb.append(Double.toString(mCoefficients.value(d)));
        }
        sb.append(")");
        return sb.toString();
    }

    double linearBasis(Vector v) {
        return v.dotProduct(mCoefficients);
    }

    Object writeReplace() {
        return new Externalizer(this);
    }

    /**
     * Returns a discrete choice model estimated from the specified
     * training data, prior, and learning parameters.
     *
     * <p>Training is carried out using stochastic gradient descent.
     * Priors are only applied every block size number of examples, and
     * at the end of each epoch to catch up.
     *
     * <p>The reporter receives {@code LogLevel.INFO} reports on
     * parameters and {@code LogLevel.DEBUG} reports on a per-epoch
     * basis of learning rate, log likelihood, log prior, and totals.
     *
     * @param alternativess An array of vectors for each training instance.
     * @param choices The index of the vector chosen for each training instance.
     * @param prior The prior to apply to coefficients.
     * @param priorBlockSize Period with which the prior is applied.
     * @param annealingSchedule Learning rates per epoch.
     * @param minImprovement Minimum improvement in the rolling average
     * of log likelihood plus prior to compute another epoch.
     * @param minEpochs Minimum number of epochs to compute.
     * @param maxEpochs Maximum number of epochs.
     * @param reporter Reporter to which progress reports are sent.
     
     */
    public static DiscreteChooser
        estimate(Vector[][] alternativess,
                 int[] choices,
                 RegressionPrior prior,
                 int priorBlockSize,
                 AnnealingSchedule annealingSchedule,
                 double minImprovement,
                 int minEpochs,
                 int maxEpochs,
                 Reporter reporter) {
        if (reporter == null)
            reporter = Reporters.silent();

        int numTrainingInstances = alternativess.length;

        reporter.info("estimate()");
        reporter.info("# training cases=" + numTrainingInstances);
        reporter.info("regression prior=" + prior);
        reporter.info("annealing schedule=" + annealingSchedule);
        reporter.info("min improvement=" + minImprovement);
        reporter.info("min epochs=" + minEpochs);
        reporter.info("max epochs=" + maxEpochs);

        if (alternativess.length == 0) {
            String msg = "Require at least 1 training instance."
                + "   Found alternativess.length=0";
            throw new IllegalArgumentException(msg);
        }
        if (alternativess.length != choices.length) {
            String msg = "Alternatives and choices must be the same length."
                + " Found alternativess.length=" + alternativess.length
                + " choices.length=" + choices.length;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < alternativess.length; ++i) {
            if (alternativess[i].length < 1) {
                String msg = "Require at least one alternative."
                    + " Found alternativess[" + i + "].length=0";
                throw new IllegalArgumentException(msg);
            }
        }
        for (int i = 0; i < alternativess.length; ++i) {
            if (choices[i] < 0) {
                String msg = "Choices must be non-negative."
                    + " Found choices[" + i + "]=" + choices[i];
                throw new IllegalArgumentException(msg);
            }
            if (choices[i] > alternativess[i].length) {
                String msg = "Choices must be less than alts length."
                    + " Found choices[" + i + "]=" + choices[i]
                    + " alternativess[" + i + "].length=" + alternativess.length
                    + ".";
                throw new IllegalArgumentException(msg);
            }
        }
        int numDimensions = alternativess[0][0].numDimensions();
        for (int i = 0; i < alternativess.length; ++i) {
            for (int j = 0; j < alternativess[i].length; ++j) {
                if (numDimensions != alternativess[i][j].numDimensions()) {
                    String msg = "All alternatives must be same length."
                        + " alternativess[0][0].length=" + numDimensions
                        + " alternativess[" + i + "][" + j + "]="
                        + alternativess[i][j] + ".";
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        Vector coefficientVector
            = new DenseVector(numDimensions);
        DiscreteChooser chooser
            = new DiscreteChooser(coefficientVector);

        double lastLlp = Double.NaN;
        double rollingAverageRelativeDiff = 1.0; // arbitrary starting point
        double bestLlp = Double.NEGATIVE_INFINITY;
        for (int epoch = 0; epoch < maxEpochs; ++epoch) {
            double learningRate = annealingSchedule.learningRate(epoch);
            for (int j = 0; j < numTrainingInstances; ++j) {
                Vector[] alternatives = alternativess[j];
                int choice = choices[j];
                double[] probs = chooser.choiceProbs(alternatives);
                for (int k = 0; k < alternatives.length; ++k) {
                    double condProbMinusTruth =
                        choice == k
                        ? (probs[k] - 1.0)
                        : probs[k];
                    if (condProbMinusTruth == 0.0)
                        continue;
                    coefficientVector.increment(- learningRate * condProbMinusTruth,
                                                alternatives[k]);
                }
                if ((j % priorBlockSize) == 0) {
                    updatePrior(prior,coefficientVector,(learningRate*priorBlockSize)/numTrainingInstances);
                }
            }
            updatePrior(prior,coefficientVector,(learningRate*(numTrainingInstances % priorBlockSize))/numTrainingInstances);
            
            double ll
                = logLikelihood(chooser,alternativess,choices);
            double lp
                = com.aliasi.util.Math.logBase2ToNaturalLog(prior.log2Prior(coefficientVector));
            double llp = ll + lp;
            if (llp > bestLlp) {
                bestLlp = llp;
            }
            if (epoch > 0) {
                double relativeDiff
                    = com.aliasi.util.Math.relativeAbsoluteDifference(lastLlp,llp);
                rollingAverageRelativeDiff = (9.0 * rollingAverageRelativeDiff + relativeDiff)/10.0;
            }
            lastLlp = llp;
            if (reporter.isDebugEnabled()) {
                Formatter formatter = null;
                try {
                    formatter = new Formatter(Locale.ENGLISH);
                    formatter.format("epoch=%5d lr=%11.9f ll=%11.4f lp=%11.4f llp=%11.4f llp*=%11.4f",
                                     epoch, learningRate,
                                     ll,
                                     lp,
                                     llp,
                                     bestLlp);
                    reporter.debug(formatter.toString());
                } catch (IllegalFormatException e) {
                    reporter.warn("Illegal format in discrete chooser");
                } finally {
                    if (formatter != null)
                        formatter.close();
                }
            }

            if (rollingAverageRelativeDiff < minImprovement) {
                reporter.info("Converged with rollingAverageRelativeDiff="
                              + rollingAverageRelativeDiff);
                break; // goes to "return regression;"
            }
        }
        return chooser;
    }

    static void updatePrior(RegressionPrior prior,
                            Vector coefficientVector,
                            double learningRate) {
        if (prior.isUniform()) 
            return;
        int numDimensions = coefficientVector.numDimensions();
        for (int d = 0; d < numDimensions; ++d) {
            double priorMode = prior.mode(d);
            double oldVal = coefficientVector.value(d);
            if (oldVal == priorMode)
                continue;
            double priorGradient = prior.gradient(oldVal,d);
            double delta = learningRate * priorGradient;
            if (oldVal == 0.0) continue;
            double newVal = oldVal > 0.0
                ? Math.max(0.0, oldVal - delta)
                : Math.min(0.0, oldVal - delta);
            coefficientVector.setValue(d,newVal);
        }
    }

    static double logLikelihood(DiscreteChooser chooser,
                                Vector[][] alternativess,
                                int[] choices) {
        double ll = 0.0;
        for (int i = 0; i < alternativess.length; ++i)
            ll += logLikelihood(chooser, alternativess[i], choices[i]);
        return ll;
    }

    static double logLikelihood(DiscreteChooser chooser,
                                Vector[] alternatives,
                                int choice) {
        double[] logProbs = chooser.choiceLogProbs(alternatives);
        return logProbs[choice];
    }

    static void verifyNonEmpty(Vector[] choices) {
        if (choices.length > 0) return;
        String msg = "Require at least one choice."
            + " Found choices.length=0.";
        throw new IllegalArgumentException(msg);
    }


    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -8567713287299117186L;
        private final DiscreteChooser mChooser;
        public Externalizer() {
            this(null);
        }
        public Externalizer(DiscreteChooser chooser) {
            mChooser = chooser;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mChooser.mCoefficients);
        }
        public Object read(ObjectInput in)
            throws IOException, ClassNotFoundException {

            @SuppressWarnings("unchecked") // nec for object read
            Vector v = (Vector) in.readObject();
            return new DiscreteChooser(v);
        }
    }

}