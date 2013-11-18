import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;

import com.aliasi.corpus.Corpus;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

public class Conll2003EnglishNeEval {

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        File conllMungedDataDir = new File(args[0]);
        int minFeatureCount = Integer.parseInt(args[1]);
        boolean addIntercept = Boolean.parseBoolean(args[2]);
        boolean cacheFeatures = Boolean.parseBoolean(args[3]);
        double priorVariance = Double.parseDouble(args[4]);
        int priorBlockSize = Integer.parseInt(args[5]);
        double initialLearningRate = Double.parseDouble(args[6]);
        double learningRateDecay = Double.parseDouble(args[7]);
        double minImprovement = Double.parseDouble(args[8]);
        int maxEpochs = Integer.parseInt(args[9]);

        Conll2003EnglishNeCorpus corpus
            = new Conll2003EnglishNeCorpus(conllMungedDataDir);

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        boolean enforceConsistency = true;
        TagChunkCodec tagChunkCodec
            = new BioTagChunkCodec(tokenizerFactory,
                                   enforceConsistency);

        ChainCrfFeatureExtractor<String> featureExtractor
            = new ChunkerFeatureExtractor();

        boolean uninformativeIntercept = addIntercept;
        RegressionPrior prior
            = RegressionPrior.laplace(priorVariance,
                                      uninformativeIntercept);

        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(initialLearningRate,
                                            learningRateDecay);

        Reporter reporter
            = Reporters.stdOut().setLevel(LogLevel.DEBUG);

        int minEpochs = 1;
        
        ChainCrfChunker crfChunker
            = ChainCrfChunker.estimate(corpus,
                                       tagChunkCodec,
                                       tokenizerFactory,
                                       featureExtractor,
                                       addIntercept,
                                       minFeatureCount,
                                       cacheFeatures,
                                       prior,
                                       priorBlockSize,
                                       annealingSchedule,
                                       minImprovement,
                                       minEpochs,
                                       maxEpochs,
                                       reporter);

        System.out.println("compiling");
        @SuppressWarnings("unchecked") // required for serialized compile
            ChainCrfChunker compiledCrfChunker
            = (ChainCrfChunker)
            AbstractExternalizable.serializeDeserialize(crfChunker);
        System.out.println("     compiled");

        System.out.println("\nEvaluating");
        ChunkerEvaluator evaluator
            = new ChunkerEvaluator(compiledCrfChunker);

        corpus.visitTest(evaluator);
        System.out.println("\nEvaluation");
        System.out.println(evaluator);
        
    }
    
}
