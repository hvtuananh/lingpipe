import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.Statistics;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.WhitespaceNormTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Factory;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.Random;

import java.util.regex.Pattern;

public class EmTwentyNewsgroups {

    static final long RANDOM_SEED = 45L;

    static final int NUM_REPLICATIONS = 10;
    static final int MAX_EPOCHS = 20;

    static final double MIN_IMPROVEMENT = 0.0001;

    static final double CATEGORY_PRIOR = 0.005; // balanced, doesn't matter
    static final double TOKEN_IN_CATEGORY_PRIOR = 0.001;  //  very sensitive to this
    static final double INITIAL_TOKEN_IN_CATEGORY_PRIOR = 0.1; // only used first run; want more uniform
    static final double DOC_LENGTH_NORM = 9.0;
    static final double COUNT_MULTIPLIER = 1.0;
    static final double MIN_COUNT = 0.0001;

    static final TokenizerFactory TOKENIZER_FACTORY = tokenizerFactory();

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        File corpusPath = new File(args[0]);

        System.out.println("CORPUS PATH=" + corpusPath);

        System.out.println("DOC LENGTH NORM=" + DOC_LENGTH_NORM);
        System.out.println("CATEGORY PRIOR=" + CATEGORY_PRIOR);
        System.out.println("TOKEN IN CATEGORY PRIOR=" + TOKEN_IN_CATEGORY_PRIOR);
        System.out.println("INITIAL TOKEN IN CATEGORY PRIOR=" + INITIAL_TOKEN_IN_CATEGORY_PRIOR);

        System.out.println("NUM REPS=" + NUM_REPLICATIONS);
        System.out.println("MAX EPOCHS=" + MAX_EPOCHS);
        System.out.println("RANDOM SEED=" + RANDOM_SEED);
        System.out.println();

        final TwentyNewsgroupsCorpus corpus = new TwentyNewsgroupsCorpus(corpusPath);
        Corpus<ObjectHandler<CharSequence>> unlabeledCorpus = corpus.unlabeledCorpus();
        System.out.println(corpus);
        System.out.println();

        Reporter reporter = Reporters.stream(System.out,"ISO-8859-1").setLevel(LogLevel.DEBUG);

        Random random = new Random(RANDOM_SEED);
        for (int numSupervisedItems : new Integer[] {  1, 2, 4, 8, 16, 32, 64, 128, 256, 512 }) {
            System.out.println("SUPERVISED DOCS/CAT=" + numSupervisedItems);
            corpus.setMaxSupervisedInstancesPerCategory(numSupervisedItems);

            double[] accs = new double[NUM_REPLICATIONS];
            double[] accsEm = new double[NUM_REPLICATIONS];
            for (int trial = 0; trial < NUM_REPLICATIONS; ++trial) {
                System.out.println("TRIAL=" + trial);
                corpus.permuteInstances(random);

                TradNaiveBayesClassifier initialClassifier
                    = new TradNaiveBayesClassifier(corpus.categorySet(),
                                                   TOKENIZER_FACTORY,
                                                   CATEGORY_PRIOR,
                                                   INITIAL_TOKEN_IN_CATEGORY_PRIOR,
                                                   DOC_LENGTH_NORM);

                Factory<TradNaiveBayesClassifier> classifierFactory 
                    = new Factory<TradNaiveBayesClassifier>() {
                        public TradNaiveBayesClassifier create() {
                            return new TradNaiveBayesClassifier(corpus.categorySet(),
                                                                TOKENIZER_FACTORY,
                                                                CATEGORY_PRIOR,
                                                                TOKEN_IN_CATEGORY_PRIOR,
                                                                DOC_LENGTH_NORM);
                        }};
                
                TradNaiveBayesClassifier emClassifier
                    = TradNaiveBayesClassifier.emTrain(initialClassifier,
                                                       classifierFactory,
                                                       corpus,
                                                       unlabeledCorpus,
                                                       MIN_COUNT,
                                                       MAX_EPOCHS,
                                                       MIN_IMPROVEMENT,
                                                       reporter);
                accs[trial] = eval(initialClassifier,corpus);
                accsEm[trial] = eval(emClassifier,corpus);
                System.out.printf("ACC=%5.3f   EM ACC=%5.3f\n\n",
                                  accs[trial], accsEm[trial]);
            }                

            System.out.println("     ---------------------");
            System.out.printf("#Sup=%4d  Supervised mean(acc)=%5.3f sd(acc)=%5.3f   EM mean(acc)=%5.3f sd(acc)=%5.3f     %10s\n\n",
                              numSupervisedItems,
                              Statistics.mean(accs),
                              Statistics.standardDeviation(accs),
                              Statistics.mean(accsEm),
                              Statistics.standardDeviation(accsEm),
                              Strings.msToString(System.currentTimeMillis() - startTime));
        }
        reporter.close();
    }


    static double eval(TradNaiveBayesClassifier classifier, 
                       Corpus<ObjectHandler<Classified<CharSequence>>> corpus)
        throws IOException, ClassNotFoundException {

        String[] categories = classifier.categorySet().toArray(new String[0]);
        Arrays.sort(categories);
        @SuppressWarnings("unchecked")
        JointClassifier<CharSequence> compiledClassifier
            = (JointClassifier<CharSequence>)
            AbstractExternalizable.compile(classifier);
        boolean storeInputs = false;
        JointClassifierEvaluator<CharSequence> evaluator
            = new JointClassifierEvaluator<CharSequence>(compiledClassifier,
                                                         categories,
                                                         storeInputs);
        corpus.visitTest(evaluator);
        return evaluator.confusionMatrix().totalAccuracy();
    }

    static TokenizerFactory tokenizerFactory() {
        TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;
        factory = new RegExFilteredTokenizerFactory(factory,Pattern.compile("\\p{Alpha}+"));
        factory = new LowerCaseTokenizerFactory(factory);
        factory = new EnglishStopTokenizerFactory(factory);
        return factory;
    }



}

