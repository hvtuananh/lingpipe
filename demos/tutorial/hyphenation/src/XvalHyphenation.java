import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;



import java.util.Arrays;


public class XvalHyphenation {

    static boolean VERBOSE = false;

    static int NUM_FOLDS = 10;

    static int MAX_NGRAM = 8;
    static int NUM_CHARS = 64;
    static double INTERPOLATION_RATIO = 4.0;
    static int HYPHENATION_N_BEST = 1024;


    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if (args.length > 1) 
            VERBOSE = Boolean.parseBoolean(args[1]);

        File hyphenationDataFile = new File(args[0]);
                
        String[] hyphenatedWords = parseCorpus(hyphenationDataFile);
        int numTagDecisions = describeData(hyphenatedWords);

        System.out.println("ACCURACY ARGS: NGRAM=" + MAX_NGRAM
                           + " NUM_CHARS=" + NUM_CHARS
                           + " INTERPOLATION_RATIO=" + INTERPOLATION_RATIO
                           + " HYPHENATION_N_BEST=" + HYPHENATION_N_BEST
                           + " VERBOSE=" + VERBOSE);

        double[] accuracies = new double[NUM_FOLDS];
        PrecisionRecallEvaluation prEval = new PrecisionRecallEvaluation();
        System.out.println();
        for (int fold = 0; fold < NUM_FOLDS; ++fold)
            evaluateFold(hyphenatedWords,fold,NUM_FOLDS,accuracies,prEval);
                
        System.out.printf("NGRAM= %2d INTERP=%4.1f ACC=%5.3f P=%5.3f R=%5.3f F=%5.3f\n",
                          MAX_NGRAM,
                          INTERPOLATION_RATIO,
                          Statistics.mean(accuracies),
                          prEval.precision(),
                          prEval.recall(),
                          prEval.fMeasure());
                
        report(prEval,numTagDecisions,accuracies);
    }



    public static void report(PrecisionRecallEvaluation prEval, int numTagDecisions,
                         double[] accuracies) {

        System.out.println("\nOVERALL ACCURACIES ACROSS FOLDS");
        System.out.printf("\nPER HYPHENATION: PREC=%5.3f RECALL=%5.3f F(1)=%5.3f\n",
                           prEval.precision(),
                           prEval.recall(),
                           prEval.fMeasure());

        double numTagErrors = prEval.falsePositive() + prEval.falseNegative();
        double perTagAccuracy = (numTagDecisions - numTagErrors) / (double) numTagDecisions;
        
        System.out.printf("\nPER TAGGING DECISION: ACC=%5.4f #DECISIONS=%7d\n",
                           perTagAccuracy,numTagDecisions);

        System.out.printf("\nWHOLE WORD: ACCURACY=%5.3f DEV=%5.3f\n",
                          Statistics.mean(accuracies),
                          Statistics.standardDeviation(accuracies));

    }





    static CompiledSpellChecker compileHyphenator(String[] hyphenatedWords, 
                                                  int fold, int numFolds,
                                                  int maxNGram, int numChars, double interpolationRatio)
        throws IOException, ClassNotFoundException {

        if (VERBOSE)
            System.out.println("     Training Hyphenator");
        NGramProcessLM lm 
            = new NGramProcessLM(maxNGram,numChars,interpolationRatio);
        WeightedEditDistance distance
            = CompiledSpellChecker.TOKENIZING;
        TrainSpellChecker trainer 
            = new TrainSpellChecker(lm,distance,null);
        for (int i = 0; i < hyphenatedWords.length; ++i) {
            if (i % numFolds != fold) {
                trainer.handle(hyphenatedWords[i]);
            }
        }

        if (VERBOSE)
            System.out.println("     Compiling Hyphenator");

        CompiledSpellChecker hyphenator
            = (CompiledSpellChecker) AbstractExternalizable.compile(trainer);
        hyphenator.setAllowInsert(true);
        hyphenator.setAllowMatch(true);
        hyphenator.setAllowDelete(false);
        hyphenator.setAllowSubstitute(false);
        hyphenator.setAllowTranspose(false);
        hyphenator.setNumConsecutiveInsertionsAllowed(1);
        hyphenator.setFirstCharEditCost(0);
        hyphenator.setSecondCharEditCost(0);
        hyphenator.setNBest(HYPHENATION_N_BEST);
        

        return hyphenator;
    }


    static void evaluateFold(String[] hyphenatedWords, int fold, int numFolds, double[] accuracies,
                             PrecisionRecallEvaluation prEval) 
        throws ClassNotFoundException, IOException {

        if (VERBOSE)
            System.out.println("EVALUATING FOLD=" + fold + "/" + numFolds);

        CompiledSpellChecker hyphenator = compileHyphenator(hyphenatedWords,fold,numFolds,
                                                            MAX_NGRAM,NUM_CHARS,INTERPOLATION_RATIO);

        int numCases = 0;
        int numCorrect = 0;
        if (VERBOSE)
            System.out.printf("%25s %25s %7s\n","REFERENCE","RESPONSE","CORRECT");
        for (int i = 0; i < hyphenatedWords.length; ++i) {
            if (i % numFolds == fold) {
                String hyphenatedWord = hyphenatedWords[i];
                String unhyphenatedWord = hyphenatedWord.replaceAll(" ","");
                String rehyphenatedWord = tamp(hyphenator.didYouMean(unhyphenatedWord));
                
                boolean correct = hyphenatedWord.equals(rehyphenatedWord);
                if (VERBOSE) {
                    if (!correct) {
                        System.out.printf("%25s %25s %7s\n",hyphenatedWord,rehyphenatedWord,correct?"true":"false");
                    }
                }
                ++numCases;
                if (correct)
                    ++numCorrect;
                updatePrEval(prEval,hyphenatedWord,rehyphenatedWord);
            }
        }
        if (VERBOSE)
            System.out.println("=================================");
        double accuracy = numCorrect / (double) numCases;
        if (VERBOSE)
            System.out.printf("FOLD =%2d ACCURACY=%5.3f\n",fold,accuracy);
        accuracies[fold] = accuracy;
    }

    static String tamp(String hyphenation) {
        return "@" + hyphenation.replaceAll("@","").trim() + "@";
    }

    static void updatePrEval(PrecisionRecallEvaluation prEval, String reference, String response) {
        Set<Integer> referenceBoundarySet = getBoundarySet(reference);
        Set<Integer> responseBoundarySet = getBoundarySet(response);
        Set<Integer> universalSet = new HashSet<Integer>();
        universalSet.addAll(referenceBoundarySet);
        universalSet.addAll(responseBoundarySet);
        for (Integer i : universalSet) {
            boolean ref = referenceBoundarySet.contains(i);
            boolean resp = responseBoundarySet.contains(i);
            prEval.addCase(ref,resp);
        }
    }


   static Set<Integer> getBoundarySet(String hyphenatedWord) {
        Set<Integer> boundarySet = new HashSet<Integer>();
        int pos = 0;
        for (int i = 0; i < hyphenatedWord.length(); ++i) {
            if (hyphenatedWord.charAt(i) == ' ')
                boundarySet.add(pos);
            else
                ++pos;
        }
        return boundarySet;
    }


    public static String[] parseCorpus(File hyphenationDataFile) throws IOException {
        System.out.println("\nReading data from file=" + hyphenationDataFile.getCanonicalPath());
        String hyphenationData = Files.readFromFile(hyphenationDataFile,Strings.UTF8);
        String[] hyphenatedWordCandidates = hyphenationData.split("\n");
        List<String> hyphenatedWordList = new ArrayList<String>();
        for (String candidate : hyphenatedWordCandidates) {
            // very conservative rejection policy -- no false positives; could check vowel/cons alternations >=4
            if (candidate.length() <= 9 || candidate.indexOf(' ') >= 0) {
                hyphenatedWordList.add(candidate);
            } else {
                if (VERBOSE)
                    System.out.println("Rejecting word=" + candidate);
            }
        }
        String[] hyphenatedWords = new String[hyphenatedWordList.size()];
        for (int i = 0; i < hyphenatedWords.length; ++i)
            hyphenatedWords[i] = "@" + hyphenatedWordList.get(i) + "@";
        com.aliasi.util.Arrays.<String>permute(hyphenatedWords, new Random(42));
        return hyphenatedWords;
    }

    public static int describeData(String[] hyphenatedWords) {
        System.out.println("     #words=" + hyphenatedWords.length);

        int numHyphens = 0;
        for (String hyphenatedWord : hyphenatedWords) {
            numHyphens += getBoundarySet(hyphenatedWord).size();
        }
        System.out.println("     #hyphens=" + numHyphens);

        int numTagDecisions = numTagDecisions(hyphenatedWords);
        System.out.println("     #hyphen insertion points=" + numTagDecisions);
        return numTagDecisions;
    }


    static int numTagDecisions(String[] hyphenations) {
        int count = 0;
        for (String hyphenation : hyphenations)
            count += countChars(hyphenation) - 1;
        return count;
    }

     static int countChars(String hyphenation) {
        int count = 0;
        for (int i = 0; i < hyphenation.length(); ++i)
            if (Character.isLetter(hyphenation.charAt(i)))
                ++count;
        return count;
    }

    static String strip(String hyphenation) {
        return hyphenation.replaceAll("@","").replaceAll("@","").trim();
    }




}