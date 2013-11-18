package com.aliasi.test.unit.classify;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.LogisticRegressionClassifier;

import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.RegExTokenizerFactory;

import com.aliasi.util.FeatureExtractor;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.IOException;

import java.util.Random;

public class LogisticRegressionClassifierTest  {

    @Test
    public void test1() throws IOException {

        Random random = new Random();

        int numFolds = 10;
        XValidatingObjectCorpus<Classified<CharSequence>> corpus
            = new XValidatingObjectCorpus<Classified<CharSequence>>(numFolds);
        // four categories
        for (int j = 0; j < 4; ++j) {
            Classification c = new Classification("cat_" + ((char)('a' + j)));
            // 100 instances each
            for (int i = 0; i < 100; ++i) {
                StringBuilder input = generateExample(j);
                Classified<CharSequence> classified
                    = new Classified<CharSequence>(input,c);
                corpus.handle(classified);
            }
        }

        corpus.permuteCorpus(random);

        FeatureExtractor<CharSequence> featureExtractor
            = new  TokenFeatureExtractor(new RegExTokenizerFactory("\\S+"));
        
        boolean addIntercept = true;
        RegressionPrior prior = RegressionPrior.noninformative();
        int priorBlockSize = 4;
        double initLearningRate = 0.01;
        double annealingRate = 500;
        double minImprovement = 0.001;
        int minEpochs = 2;
        int maxEpochs = 10000;
        int minFeatureCount = 2;
        int rollingAverageSize = 5;
        
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.inverse(initLearningRate,annealingRate);


        LogisticRegressionClassifier<CharSequence> classifier
            = LogisticRegressionClassifier.train(corpus,
                                                 featureExtractor,
                                                 minFeatureCount,
                                                 addIntercept,
                                                 prior,
                                                 priorBlockSize,
                                                 null, // hot start initially off
                                                 annealingSchedule,
                                                 minImprovement,
                                                 rollingAverageSize,
                                                 minEpochs,
                                                 maxEpochs,
                                                 null, // no epoch handler
                                                 null ); // no writer feedback for test


        for (int j = 0; j < 4; ++j) {
            Classification c = new Classification("cat_" + ((char)('a' + j)));
            // 100 instances each
            for (int i = 0; i < 10; ++i) {
                StringBuilder sb = generateExample(j);
                assertEquals(c.bestCategory(),
                             classifier.classify(sb).bestCategory());
            }
        }

        // train and test hot start here

        priorBlockSize = 2;
        minImprovement = minImprovement/1000.0;
        LogisticRegressionClassifier<CharSequence> classifier2
            = LogisticRegressionClassifier.train(corpus,
                                                 featureExtractor,
                                                 minFeatureCount,
                                                 addIntercept,
                                                 prior,
                                                 priorBlockSize,
                                                 classifier,
                                                 annealingSchedule,
                                                 minImprovement,
                                                 rollingAverageSize,
                                                 minEpochs,
                                                 maxEpochs,
                                                 null,  // no handler
                                                 null); // no reporter
        

        
    }

    static StringBuilder generateExample(int j) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < 100; ++k) {
            if (k > 0) sb.append(' ');
            if (random.nextBoolean())
                sb.append(((char)('a' + j)));
            else
                sb.append(((char)('a' + random.nextInt(10))));
        }
        return sb;
    }


}
