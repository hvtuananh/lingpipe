
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.tokenizer.*;
import com.aliasi.stats.Statistics;
import com.aliasi.symbol.*;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.io.*;
import java.util.*;
import java.util.zip.*;


public class LdaTopicSimilarity {

    static class LdaRunnable implements Runnable {
        LatentDirichletAllocation mLda;
        final int[][] mDocTokens;
        final ObjectHandler<LatentDirichletAllocation.GibbsSample> mHandler;
        final Random mRandom;

        LdaRunnable(int[][] docTokens,
                    ObjectHandler<LatentDirichletAllocation.GibbsSample> handler,
                    Random random) {
            mDocTokens = docTokens;
            mHandler = handler;
            mRandom = random;
        }

        public void run() {
            mLda = sample(mDocTokens,mHandler,mRandom);
        }
    }

    public static void main(String[] args) throws Exception {
        int minTokenCount = 5;
        File corpusFile = new File(args[0]);
        CharSequence[] articleTexts = LdaWormbase.readCorpus(corpusFile);
        SymbolTable symbolTable = new MapSymbolTable();
        TokenizerFactory tokenizerFactory = LdaWormbase.WORMBASE_TOKENIZER_FACTORY;
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(articleTexts,tokenizerFactory,symbolTable,minTokenCount);


        LdaRunnable runnable1 = new LdaRunnable(docTokens,
                                                new LdaReportingHandler(symbolTable),
                                                new Random());

        LdaRunnable runnable2 = new LdaRunnable(docTokens,
                                                new LdaReportingHandler(symbolTable),
                                                new Random());

        Thread thread1 = new Thread(runnable1);
        Thread thread2 = new Thread(runnable2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LatentDirichletAllocation lda0 = runnable1.mLda;

        LatentDirichletAllocation lda1 = runnable2.mLda;


        System.out.println("\nComputing Greedy Aligned Symmetrized KL Divergences");
        double[] scores = similarity(lda0,lda1);
        for (int i = 0; i < scores.length; ++i)
            System.out.printf("%4d %15.3f\n",i,scores[i]);
    }

    static double[] similarity(LatentDirichletAllocation lda0,
                               LatentDirichletAllocation lda1) {

        int numTopics = lda0.numTopics();

        int numPairs = numTopics * (numTopics - 1);
        @SuppressWarnings({"unchecked","rawtypes"}) // ok given use w. erasure
        ScoredObject<int[]>[] pairs
            = (ScoredObject<int[]>[]) new ScoredObject[numPairs];
        int pos = 0;
        for (int i = 0; i < numTopics; ++i) {
            for (int j = 0; j < numTopics; ++j) {
                if (i == j) continue;
                double divergence
                    = Statistics
                    .symmetrizedKlDivergence(lda0.wordProbabilities(i),
                                             lda1.wordProbabilities(j));
                pairs[pos++] = new ScoredObject<int[]>(new int[] { i , j }, divergence);
            }
        }
        Arrays.sort(pairs,ScoredObject.comparator());
        boolean[] taken0 = new boolean[numTopics];
        Arrays.fill(taken0,false);
        boolean[] taken1 = new boolean[numTopics];
        Arrays.fill(taken1,false);
        double[] scores = new double[numTopics];
        int scorePos = 0;
        for (pos = 0; pos < numPairs && scorePos < numTopics; ++pos) {
            int[] pair = pairs[pos].getObject();
            if (!taken0[pair[0]] && !taken1[pair[1]]) {
                taken0[pair[0]] = true;
                taken1[pair[1]] = true;
                scores[scorePos++] = pairs[pos].score();
            }
        }
        return scores;
    }

    static LatentDirichletAllocation
        sample(int[][] docTokens,
               ObjectHandler<LatentDirichletAllocation.GibbsSample> handler,
               Random random) {

        short numTopics = 50;
        double topicPrior = 0.1;
        double wordPrior = 0.01;
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 2000;

        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(docTokens,

                          numTopics,
                          topicPrior,
                          wordPrior,

                          burninEpochs,
                          sampleLag,
                          numSamples,

                          random,
                          handler);

        return sample.lda();
    }
}
