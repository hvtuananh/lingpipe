import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.Classification;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.LogisticRegressionClassifier;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TextClassificationDemo {

    static final File TRAINING_DIR
        = new File("../../data/fourNewsGroups/4news-train");

    static final File TESTING_DIR
        =  new File("../../data/fourNewsGroups/4news-test");

    static final String[] CATEGORIES
        = { "soc.religion.christian",
            "talk.religion.misc",
            "alt.atheism",
            "misc.forsale" };


    public static void main(String[] args) throws Exception {
        if (!TRAINING_DIR.isDirectory()) {
            System.out.println("Could not find data directory=" + TRAINING_DIR);
            System.out.println("Have you unpacked 4 newsgroups from $LINGPIPE/demos/data?");
            return;
        }

        PrintWriter progressWriter = new PrintWriter(System.out,true);

        progressWriter.println("Reading data.");
        int numFolds = 10;
        XValidatingObjectCorpus<Classified<CharSequence>> corpus
            = new XValidatingObjectCorpus<Classified<CharSequence>>(numFolds);
        for (String category : CATEGORIES) {
            Classification c = new Classification(category);

            for (File trainingFile : new File(TRAINING_DIR,category).listFiles()) {
                String text = Files.readFromFile(trainingFile,"ISO-8859-1");
                Classified<CharSequence> classified
                    = new Classified<CharSequence>(text,c);
                corpus.handle(classified);
            }
            for (File trainingFile : new File(TESTING_DIR,category).listFiles()) {
                String text = Files.readFromFile(trainingFile,"ISO-8859-1");
                Classified<CharSequence> classified
                    = new Classified<CharSequence>(text,c);
                corpus.handle(classified);
            }
        }
        progressWriter.println("Num instances=" + corpus.size() + ".");
        progressWriter.println("Permuting corpus.");
        corpus.permuteCorpus(new Random(7117)); // destroys runs of categories


        progressWriter.println("\nEVALUATING FOLDS\n");

        TokenizerFactory tokenizerFactory
            // = new com.aliasi.tokenizer.NGramTokenizerFactory(3,5);
            = new RegExTokenizerFactory("\\p{L}+|\\d+"); // letter+ | digit+
        FeatureExtractor<CharSequence> featureExtractor
            = new TokenFeatureExtractor(tokenizerFactory);
        int minFeatureCount = 2;
        boolean addInterceptFeature = true;
        boolean noninformativeIntercept = true;
        double priorVariance = 10.0;
        // RegressionPrior prior = RegressionPrior.elasticNet(0.10, 1.0, noninformativeIntercept);
        RegressionPrior prior = RegressionPrior.gaussian(1.0,noninformativeIntercept);
        
        // = RegressionPrior.noninformative();
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(0.00025,0.999);  // exp(0.00025,0.999) works OK
        double minImprovement = 0.000000001;
        int minEpochs = 100;
        int maxEpochs = 20000;

        int blockSize = corpus.size(); // reduces to conjugate gradient
        LogisticRegressionClassifier<CharSequence> hotStart = null;
        int rollingAvgSize = 10;
        ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler
            = null;

        for (int fold = 0; fold < numFolds; ++fold) {
            corpus.setFold(fold);

            Reporter reporter = Reporters.writer(progressWriter);
            LogisticRegressionClassifier<CharSequence> classifier
                = LogisticRegressionClassifier.<CharSequence>train(corpus,
                                                                   featureExtractor,
                                                                   minFeatureCount,
                                                                   addInterceptFeature,
                                                                   prior,
                                                                   blockSize,
                                                                   hotStart,
                                                                   annealingSchedule,
                                                                   minImprovement,
                                                                   rollingAvgSize,
                                                                   minEpochs,
                                                                   maxEpochs,
                                                                   classifierHandler,
                                                                   reporter);

            progressWriter.println("\nCLASSIFIER & FEATURES\n");
            progressWriter.println(classifier);

            progressWriter.println("\nEVALUATION\n");
            boolean storeInputs = false;
            ConditionalClassifierEvaluator<CharSequence> evaluator 
                = new ConditionalClassifierEvaluator<CharSequence>(classifier,CATEGORIES,storeInputs);
            corpus.visitTest(evaluator);
            progressWriter.printf("FOLD=%5d  ACC=%4.2f  +/-%4.2f\n", 
                                  fold,
                                  evaluator.confusionMatrix().totalAccuracy(),
                                  evaluator.confusionMatrix().confidence95());
        }
    }

    // 108.6   .001/.999
    // -107.9336 .002/.9975
}
