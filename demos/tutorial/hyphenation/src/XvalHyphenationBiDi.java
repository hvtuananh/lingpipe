import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;



import java.util.Arrays;


public class XvalHyphenationBiDi {

    static boolean VERBOSE = false;

    static int NUM_FOLDS = 10;

    static int MAX_NGRAM = 8;
    static int NUM_CHARS = 64;
    static double INTERPOLATION_RATIO = 4.0;
    static int HYPHENATION_N_BEST = 256;


    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if (args.length > 1)
            VERBOSE = Boolean.parseBoolean(args[1]);

        System.out.println("ACCURACY ARGS: NGRAM=" + MAX_NGRAM
                           + " NUM_CHARS=" + NUM_CHARS
                           + " INTERPOLATION_RATIO=" + INTERPOLATION_RATIO
                           + " HYPHENATION_N_BEST=" + HYPHENATION_N_BEST);

        File hyphenationDataFile = new File(args[0]);

        String[] hyphenatedWords = XvalHyphenation.parseCorpus(hyphenationDataFile);
        int numTagDecisions = XvalHyphenation.describeData(hyphenatedWords);

        double[] accuracies = new double[NUM_FOLDS];
        PrecisionRecallEvaluation prEval = new PrecisionRecallEvaluation();
        System.out.println();
        for (int fold = 0; fold < NUM_FOLDS; ++fold)
            evaluateFold(hyphenatedWords,fold,NUM_FOLDS,accuracies,prEval);

        XvalHyphenation.report(prEval,numTagDecisions,accuracies);
    }

    static void evaluateFold(String[] hyphenatedWords, 
                             int fold, int numFolds, double[] accuracies,
                             PrecisionRecallEvaluation prEval) 
        throws ClassNotFoundException, IOException {

        if (VERBOSE)
            System.out.println("EVALUATING FOLD=" + fold + "/" + numFolds);


        CompiledSpellChecker hyphenator 
            = XvalHyphenation.compileHyphenator(hyphenatedWords,fold,numFolds,
                                                MAX_NGRAM,NUM_CHARS,INTERPOLATION_RATIO);
        CompiledSpellChecker hyphenatorReversed 
            = XvalHyphenation.compileHyphenator(reverse(hyphenatedWords),fold,numFolds,
                                                MAX_NGRAM,NUM_CHARS,INTERPOLATION_RATIO);


        int numCases = 0;
        int numCorrect = 0;
        if (VERBOSE)
            System.out.printf("%25s %25s %7s\n","REFERENCE","RESPONSE","CORRECT");
        for (int i = 0; i < hyphenatedWords.length; ++i) {
            if (i % numFolds == fold) {
                String hyphenatedWord = hyphenatedWords[i];
                String unhyphenatedWord = hyphenatedWord.replaceAll(" ","");
                String rehyphenatedWord = hyphenate(unhyphenatedWord,
                                                    hyphenator,
                                                    hyphenatorReversed);
                boolean correct = hyphenatedWord.equals(rehyphenatedWord);
                if (VERBOSE) {
                    if (!correct) {
                        System.out.printf("%25s %25s %7b\n",
                                          hyphenatedWord,rehyphenatedWord,correct);
                    }
                }
                XvalHyphenation.updatePrEval(prEval,hyphenatedWord,rehyphenatedWord);
                ++numCases;
                if (correct)
                    ++numCorrect;
            }
        }
        if (VERBOSE)
            System.out.println("=================================");
        double accuracy = numCorrect / (double) numCases;
        System.out.printf("FOLD =%2d ACCURACY=%5.3f\n",fold,accuracy);
        accuracies[fold] = accuracy;
    }


    static String hyphenate(String word,
                            CompiledSpellChecker hyphenator,
                            CompiledSpellChecker hyphenatorReversed) {

        String reversed = reverse(word);
        ObjectToDoubleMap<String> fwdNBest = nBest(hyphenator,word,false);
        ObjectToDoubleMap<String> revNBest = nBest(hyphenatorReversed,word,true);
        ObjectToDoubleMap<String> nBest = new ObjectToDoubleMap<String>();
        for (String hyphenation : fwdNBest.keySet()) {
            double fwdScore = fwdNBest.getValue(hyphenation);
            double revScore = revNBest.getValue(hyphenation);
            nBest.set(hyphenation, 0.5 * fwdScore + 0.5 * revScore);
        }

        return nBest.keysOrderedByValueList().get(0);
    }

    static ObjectToDoubleMap<String> nBest(CompiledSpellChecker hyphenator, String word, boolean reverseIO) {
        Iterator<ScoredObject<String>> nBest = hyphenator.didYouMeanNBest(reverseIO ? reverse(word) : word);
        ObjectToDoubleMap<String> nBestMap = new ObjectToDoubleMap<String>();
        while (nBest.hasNext()) {
            ScoredObject<String> scoredHyphenation = nBest.next();
            String hyphenation = XvalHyphenation.tamp(scoredHyphenation.getObject());
            Double score = java.lang.Math.pow(2.0,scoredHyphenation.score());
            String orderedHyphenation = reverseIO ? reverse(hyphenation) : hyphenation;
            Double currentScore = nBestMap.get(orderedHyphenation);
            if (currentScore != null) {
                // happens when two inputs tamp to same val
                score = currentScore + score;
            }
            nBestMap.set(orderedHyphenation,score);
        }   
        double totalProb = 0.0;
        for (String key : nBestMap.keySet()) {
            totalProb += nBestMap.get(key);
        }
        ObjectToDoubleMap<String> conditionalNBestMap = new ObjectToDoubleMap<String>();
        for (String key : nBestMap.keySet()) {
            double conditionalProb = nBestMap.get(key) / totalProb;
            conditionalNBestMap.set(key,conditionalProb);
        }
        return conditionalNBestMap;
    }

   static String[] reverse(String[] hyphenatedWords) {
        String[] result = new String[hyphenatedWords.length];
        for (int i = 0; i < hyphenatedWords.length; ++i)
            result[i] = reverse(hyphenatedWords[i]);
        return result;
    }

    public static String reverse(String in) {
        return new StringBuilder(in).reverse().toString();
    }


}