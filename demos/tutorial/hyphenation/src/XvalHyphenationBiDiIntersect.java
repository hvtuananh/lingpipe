
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


public class XvalHyphenationBiDiIntersect {

    static boolean VERBOSE = false;

    static boolean INTERSECT = true; // true=intersect, false=union

    static int NUM_FOLDS = 10;

    static int MAX_NGRAM = 8;
    static int NUM_CHARS = 64;
    static double INTERPOLATION_RATIO = 4.0;
    static int HYPHENATION_N_BEST = 256;

    // can't get much precision vs. recall tradeoff this naively
    static double BREAK_WEIGHT = 0.0;
    static double CONTINUE_WEIGHT = 0.0;


    public static void main(String[] args) throws ClassNotFoundException, IOException {
        INTERSECT = Boolean.parseBoolean(args[1]);

        if (args.length > 2)
            VERBOSE = Boolean.parseBoolean(args[2]);

        System.out.println("ACCURACY ARGS: NGRAM=" + MAX_NGRAM
                           + " NUM_CHARS=" + NUM_CHARS
                           + " INTERPOLATION_RATIO=" + INTERPOLATION_RATIO
                           + " HYPHENATION_N_BEST=" + HYPHENATION_N_BEST
                           + " VERBOSE=" + VERBOSE
                           + " MODE=" + (INTERSECT ? "INTERSECTING" : "UNIONING"));

        File hyphenatedDataFile = new File(args[0]);
        String[] hyphenatedWords = XvalHyphenation.parseCorpus(hyphenatedDataFile);
        int numTagDecisions = XvalHyphenation.describeData(hyphenatedWords);

        double[] accuracies = new double[NUM_FOLDS];
        PrecisionRecallEvaluation prEval = new PrecisionRecallEvaluation();
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
            = XvalHyphenation.compileHyphenator(XvalHyphenationBiDi.reverse(hyphenatedWords),fold,numFolds,
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

        String reversed = XvalHyphenationBiDi.reverse(word);
        String hyphenationFwd = XvalHyphenation.strip(hyphenator.didYouMean(word));
        String hyphenationRev = XvalHyphenation.strip(XvalHyphenationBiDi.reverse(hyphenatorReversed.didYouMean(XvalHyphenationBiDi.reverse(word))));
        return "@" + intersect(hyphenationFwd,hyphenationRev) + "@";
    }


    static String intersect(String hyphenationFwd, String hyphenationRev) {
        boolean matched = hyphenationFwd.equals(hyphenationRev);
        if (matched) 
            return hyphenationFwd;

        Set<Integer> hyphenSetFwd = XvalHyphenation.getBoundarySet(hyphenationFwd);
        Set<Integer> hyphenSetRev = XvalHyphenation.getBoundarySet(hyphenationRev);

        if (INTERSECT)
            hyphenSetFwd.retainAll(hyphenSetRev);
        else
            hyphenSetFwd.addAll(hyphenSetRev);
        int k = 0;
        int[] hyphenPositions = new int[hyphenSetFwd.size()];
        for (Integer hyphenPosition : hyphenSetFwd) {
            hyphenPositions[k++] = hyphenPosition;
        }
        Arrays.sort(hyphenPositions);
        String hyphenation = hyphenationFwd.replaceAll(" ",""); // strip hyphens
        for (int i = hyphenPositions.length; --i >= 0; ) {
            hyphenation = hyphenation.substring(0,hyphenPositions[i])
                + " " + hyphenation.substring(hyphenPositions[i]);
        }
        if (!matched) {
            if (VERBOSE) 
                System.out.println("fwd=" + hyphenationFwd + " rev=" + hyphenationRev + " int=" + hyphenation);
        }
        if (VERBOSE) {
            System.out.println();
            System.out.println("     hyph=" + hyphenation);
            System.out.println("     fwd=" + hyphenationFwd);
            System.out.println("     rev=" + hyphenationRev);
        }
        return hyphenation;
    }


}