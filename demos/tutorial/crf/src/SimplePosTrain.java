import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfFeatureExtractor;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tag.Tagging;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;

import java.io.File;
import java.io.IOException;

public class SimplePosTrain {

    public static void main(String[] args) throws IOException {
        Corpus<ObjectHandler<Tagging<String>>> corpus
            = new TinyPosCorpus();

        ChainCrfFeatureExtractor<String> featureExtractor
            = new SimpleChainCrfFeatureExtractor();

        boolean addIntercept = true;

        int minFeatureCount = 1;

        boolean cacheFeatures = false;

        boolean allowUnseenTransitions = true;

        double priorVariance = 4.0;
        boolean uninformativeIntercept = true;
        RegressionPrior prior
            = RegressionPrior.gaussian(priorVariance,
                                       uninformativeIntercept);
        int priorBlockSize = 3;

        double initialLearningRate = 0.05;
        double learningRateDecay = 0.995;
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(initialLearningRate,
                                            learningRateDecay);

        double minImprovement = 0.00001;
        int minEpochs = 2;
        int maxEpochs = 2000;

        Reporter reporter
            = Reporters.stdOut().setLevel(LogLevel.DEBUG);

        System.out.println("\nEstimating");
        ChainCrf<String> crf
            = ChainCrf.estimate(corpus,
                                featureExtractor,
                                addIntercept,
                                minFeatureCount,
                                cacheFeatures,
                                allowUnseenTransitions,
                                prior,
                                priorBlockSize,
                                annealingSchedule,
                                minImprovement,
                                minEpochs,
                                maxEpochs,
                                reporter);

        File modelFile = new File(args[0]);
        System.out.println("\nCompiling to file=" + modelFile);
        AbstractExternalizable.serializeTo(crf,modelFile);
    }

}