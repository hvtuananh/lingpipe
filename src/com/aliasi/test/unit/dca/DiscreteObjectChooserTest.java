package com.aliasi.test.unit.dca;

import com.aliasi.dca.DiscreteChooser;
import com.aliasi.dca.DiscreteObjectChooser;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.Vector;
import com.aliasi.matrix.DenseVector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;

import static com.aliasi.test.unit.Asserts.assertEqualsArray;
import static com.aliasi.test.unit.Asserts.succeed;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.Serializable;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscreteObjectChooserTest {

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
            double cumProb = 0.0;
            for (int k = 0; k < numChoices; ++k) {
                cumProb += choiceProbs[k];
                if (choiceProb < cumProb || k == (numChoices - 1)) {
                    choices[i] = k;
                    break;
                }
            }
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

        Map<Integer,Vector> vectorMap
            = new HashMap<Integer,Vector>();
        List<List<Integer>> alternativeObjectss
            = new ArrayList<List<Integer>>(alternativess.length);
        int count = 0;
        for (int i = 0; i < alternativess.length; ++i) {
            List<Integer> alternativeObjects = new ArrayList<Integer>(alternativess[i].length);
            alternativeObjectss.add(alternativeObjects);
            for (int j = 0; j < alternativess[i].length; ++j) {
                Integer obj = count++;
                vectorMap.put(obj,alternativess[i][j]);
                alternativeObjects.add(obj);
            }
        }
        
        FeatureExtractor<Integer> featureExtractor
            = new MapFeatureExtractor(vectorMap);

        int minFeatureCount = 5;
        
        DiscreteObjectChooser<Integer> objectChooser
            = DiscreteObjectChooser.estimate(featureExtractor,
                                             alternativeObjectss,
                                             choices,
                                             minFeatureCount,
                                             prior,
                                             priorBlockSize,
                                             annealingSchedule,
                                             minImprovement,
                                             minEpochs,
                                             maxEpochs,
                                             reporter);

        DiscreteChooser chooser = objectChooser.chooser();
        SymbolTable featureSymbolTable = objectChooser.featureSymbolTable();

        Vector coeffVector = chooser.coefficients();
        for (int d = 0; d < coeffVector.numDimensions(); ++d)
            assertEquals(simCoeffVector.value(d), 
                         coeffVector.value(featureSymbolTable.symbolToID(Integer.toString(d))),
                         0.1); 

        @SuppressWarnings("unchecked")
        DiscreteObjectChooser deserChooser
            = (DiscreteObjectChooser) AbstractExternalizable.serializeDeserialize(objectChooser);

        Vector deserCoeffVector = deserChooser.chooser().coefficients();
        SymbolTable deserSymTab = deserChooser.featureSymbolTable();
        for (int d = 0; d < deserCoeffVector.numDimensions(); ++d)
            assertEquals(simCoeffVector.value(d), 
                         deserCoeffVector.value(deserSymTab.symbolToID(Integer.toString(d))),
                         0.1); 
    }

    static class MapFeatureExtractor 
        implements FeatureExtractor<Integer>, Serializable {
        final Map<Integer,Vector> mMap;
        MapFeatureExtractor(Map<Integer,Vector> map) {
            mMap = map;
        }
        public Map<String,Double> features(Integer i) {
            Vector v = mMap.get(i);
            Map<String,Double> result = new HashMap<String,Double>(5);
            for (int d = 0; d < v.numDimensions(); ++d)
                result.put(Integer.toString(d),
                           v.value(d));
            return result;
        }
    }

}