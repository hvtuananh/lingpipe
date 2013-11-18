import com.aliasi.dca.DiscreteChooser;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.Vector;
import com.aliasi.matrix.DenseVector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import java.util.Random;

public class Dca {

    public static void main(String[] args) {
        System.out.println("DCA Demo");

        double[] simCoeffs 
            = new double[] { 3.0, -2.0, 1.0 };
        Vector simCoeffVector
            = new DenseVector(simCoeffs);
        DiscreteChooser simChooser = new DiscreteChooser(simCoeffVector);

        int numDims = simCoeffs.length;
        int numSamples = 1000;

        Random random = new Random(42);
        Vector[][] alternativess = new Vector[numSamples][];
        int[] choices = new int[numSamples];
        for (int i = 0; i < numSamples; ++i) {
            int numChoices = 1 + random.nextInt(8);
            alternativess[i] = new Vector[numChoices];
            for (int k = 0; k < numChoices; ++k) {
                double[] xs = new double[numDims];
                for (int d = 0; d < numDims; ++d) {
                    xs[d] = 2.0 * random.nextGaussian();
                }
                alternativess[i][k] = new DenseVector(xs);
            }
            double[] choiceProbs = simChooser.choiceProbs(alternativess[i]);
            double choiceProb = random.nextDouble();
            double cumProb = 0.0;
            for (int k = 0; k < numChoices; ++k) {
                cumProb += choiceProbs[k];
                if (choiceProb < cumProb || k == (numChoices - 1)) {
                    choices[i] = k;
                    break;
                }
            }
            System.out.println("\nSample " + i + " random choice prob=" + choiceProb);
            for (int k = 0; k < numChoices; ++k) {
                System.out.println((choices[i] == k ? "* " : "  ") + k
                                   + " p=" + choiceProbs[k]
                                   + " xs=" + alternativess[i][k]);
            }
        }

        double priorVariance = 4.0;
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
        
        Reporter reporter = Reporters.stdOut().setLevel(LogLevel.DEBUG);

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
        System.out.println("\nACTUAL coeffs=" + simCoeffVector);
        System.out.println("FIT coeffs=" + coeffVector);
    }


}        
