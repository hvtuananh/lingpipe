package com.aliasi.test.unit.dca;

import com.aliasi.dca.DiscreteChooser;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.Vector;
import com.aliasi.matrix.DenseVector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.util.AbstractExternalizable;

import static com.aliasi.test.unit.Asserts.assertEqualsArray;
import static com.aliasi.test.unit.Asserts.succeed;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;

import java.util.Random;

public class DiscreteChooserTest {

    @Test
    public void testSim() throws IOException {
        int numSamples = 1000;
        double[] simCoeffs 
            = new double[] { 0.0, 3.0, -2.0, 1.0 };
        int numDims = simCoeffs.length;
        Vector simCoeffVector
            = new DenseVector(simCoeffs);
        DiscreteChooser simChooser = new DiscreteChooser(simCoeffVector);

        Random random = new Random(42);
        // y = -1 + 3 * x[1] + -2 * x[2] + 1*x[3] 
        Vector[][] alternativess = new Vector[numSamples][];
        int[] choices = new int[numSamples];
        for (int i = 0; i < numSamples; ++i) {
            int numChoices = 1 + random.nextInt(8);
            alternativess[i] = new Vector[numChoices];
            for (int k = 0; k < numChoices; ++k) {
                double[] xs = new double[numDims];
                xs[0] = 1.0; // intercept
                for (int d = 1; d < numDims; ++d) {
                    xs[d] = 2.0 * random.nextGaussian();
                }
                alternativess[i][k] = new DenseVector(xs);
            }
            double[] choiceProbs = simChooser.choiceProbs(alternativess[i]);
            double choiceProb = random.nextDouble();
            // System.out.println("i=" + i + " choiceProb=" + choiceProb);
            double cumProb = 0.0;
            for (int k = 0; k < numChoices; ++k) {
                cumProb += choiceProbs[k];
                if (choiceProb < cumProb || k == (numChoices - 1)) {
                    choices[i] = k;
                    break;
                }
            }
            // System.out.println("\nSample " + i);
            // for (int k = 0; k < numChoices; ++k) {
            // System.out.println((choices[i] == k ? "* " : "  ") + k
            // + " p=" + choiceProbs[k]
            // + " xs=" + alternativess[i][k]);
            // }
        }

        double priorVariance = 5.0;
        boolean nonInformativeIntercept = true;
        RegressionPrior prior 
            = RegressionPrior.gaussian(priorVariance,nonInformativeIntercept);
        int priorBlockSize = 100;
        
        double initialLearningRate = 0.1;
        double decayBase = 0.99;
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(initialLearningRate,decayBase);

        double minImprovement = 0.00001;
        int minEpochs = 5;
        int maxEpochs = 500;
        
        // Reporter reporter = Reporters.stdOut().setLevel(LogLevel.DEBUG);
        Reporter reporter = null; // silent

        DiscreteChooser chooser
            = DiscreteChooser.estimate(alternativess,
                                       choices,
                                       prior,
                                       priorBlockSize,
                                       annealingSchedule,
                                       minImprovement,
                                       minEpochs,
                                       maxEpochs,
                                       reporter);

        Vector coeffVector = chooser.coefficients();
        // System.out.println("\nACTUAL coeffs=" + simCoeffVector);
        // System.out.println("FIT coeffs=" + coeffVector);
        for (int d = 0; d < coeffVector.numDimensions(); ++d)
            assertEquals(simCoeffVector.value(d), coeffVector.value(d), 0.1); // low tolerance

        @SuppressWarnings("unchecked")
        DiscreteChooser deserChooser
            = (DiscreteChooser) AbstractExternalizable.serializeDeserialize(chooser);
        Vector deserCoeffVector = deserChooser.coefficients();
        for (int d = 0; d < coeffVector.numDimensions(); ++d)
            assertEquals(coeffVector.value(d), deserCoeffVector.value(d), 0.00001); 
        
    }

    @Test
    public void testChoice() throws IOException {
        assertChoice(new double[] { }, // output
                     new double[] { 0.2, 0.8 });

        assertChoice(new double[] { }, // output
                     new double[] { 0.2, 0.8 }, // coeffs
                     new double[] { -1, 1 });

        assertChoice(new double[] { }, // output
                     new double[] { 0.2, -1.2, 0.8 }, // coeffs
                     new double[] { -1, 1, 1 },
                     new double[] { 2, 1, -1 },
                     new double[] { -1, -1, -21 },
                     new double[] { -1, 2, 1 },
                     new double[] { 1, -2, -1 });
    }

    void assertChoice(double[] expectedBases,
                      double[] coeffs,
                      double[]... inputs) throws IOException {

        Vector coeffVector = new DenseVector(coeffs);
        DiscreteChooser chooser = new DiscreteChooser(coeffVector);

        assertChoice(coeffVector,chooser,
                     expectedBases,coeffs,inputs);
        @SuppressWarnings("unchecked")
        DiscreteChooser serDeserChooser
            = (DiscreteChooser)
            AbstractExternalizable.serializeDeserialize(chooser);
        assertChoice(coeffVector,serDeserChooser,
                     expectedBases,coeffs,inputs);
    }

    void assertChoice(Vector coeffVector,
                      DiscreteChooser chooser,
                      double[] expectedBases,
                      double[] coeffs,
                      double[][] inputs) {

        Vector[] inputVecs = new Vector[inputs.length];
        for (int i = 0; i < inputs.length; ++i)
            inputVecs[i] = new DenseVector(inputs[i]);

        if (inputVecs.length == 0) {
            try {
                chooser.choose(inputVecs);
                fail();
            } catch (IllegalArgumentException e) {
                succeed();
            }

            try {
                chooser.choiceProbs(inputVecs);
                fail();
            } catch (IllegalArgumentException e) {
                succeed();
            }

            try {
                chooser.choiceLogProbs(inputVecs);
                fail();
            } catch (IllegalArgumentException e) {
                succeed();
            }

            return;
        }

        int choice = chooser.choose(inputVecs);
        double[] choiceProbs = chooser.choiceProbs(inputVecs);
        double[] choiceLogProbs = chooser.choiceLogProbs(inputVecs);

        double[] bases = new double[inputs.length];
        for (int i = 0; i < bases.length; ++i)
            bases[i] = inputVecs[i].dotProduct(coeffVector);
        double[] expBases = new double[inputs.length];
        for (int i = 0; i < expBases.length; ++i)
            expBases[i] = Math.exp(bases[i]);
        double Z = 0.0;
        for (int i = 0; i < expBases.length; ++i)
            Z += expBases[i];
        double[] expProbs = new double[inputs.length];
        for (int i = 0; i < expProbs.length; ++i)
            expProbs[i] = expBases[i] / Z;
        double[] expLogProbs = new double[inputs.length];
        for (int i = 0; i < expLogProbs.length; ++i)
            expLogProbs[i] = Math.log(expProbs[i]);
        int expChoice = 0;
        for (int i = 1; i < expBases.length; ++i)
            if (expBases[i] > expBases[expChoice])
                expChoice = i;

        assertEquals(expChoice,choice);
        assertEqualsArray(expProbs,choiceProbs,0.001);
        assertEqualsArray(expLogProbs,choiceLogProbs,0.001);

        assertEquals(com.aliasi.util.Math.sum(choiceProbs), 1.0, 0.001);

    }

}