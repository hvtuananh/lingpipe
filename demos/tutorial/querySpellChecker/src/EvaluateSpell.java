import com.aliasi.lm.CompiledNGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.SpellChecker;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class EvaluateSpell extends AbstractCommand {

    Set<String> mTokens = new HashSet<String>();

    ObjectToCounterMap<String> mTokenCounter;

    private String[] mDataLine;
    private String[] mOriginalQ;
    private String[] mCorrectQ;
    private String[] mSystemQ;
    private String[] mThrownOutQ;

    private CompiledSpellChecker mSpellChecker;

    public EvaluateSpell(String args[]) {
        super(args);
    }

    public void run() {
        try {
            runWithExceptions();
        }  catch(Exception e) {
            System.out.println("error scoring, got error "+e);
            e.printStackTrace();
        }
    }

    void runWithExceptions() throws IOException, ClassNotFoundException {
        String str = null;
        try {
            str = Files.readFromFile(getArgumentFile(REFERENCE_FILE_PARAM),"ISO-8859-1");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Could not find reference file=" + REFERENCE_FILE_PARAM);
            System.out.println("Have you unpacked the demo data?");
            System.out.println("If not, run:");
            System.out.println("  > cd $LINGPIPE");
            System.out.println("  > ant jars");
            return;
        }
        String lines[] = str.split("\n");

        loadData(lines);

        try {
            @SuppressWarnings("unchecked") // ok by construction
            CompiledSpellChecker spellChecker                
                = (CompiledSpellChecker)
                AbstractExternalizable.readObject(getArgumentFile(MODEL_FILE_PARAM));
            mSpellChecker = spellChecker;
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Model file not found. File=" + MODEL_FILE_PARAM);
            System.out.println("Have you run the training program?");
            System.out.println("If not, run:");
            System.out.println("  > ant train");
            return;
        }
        mSpellChecker.setTokenizerFactory(com.aliasi.tokenizer.IndoEuropeanTokenizerFactory.INSTANCE);
        if (hasArgument(TOKEN_COUNTER_FILE_PARAM)) {
            @SuppressWarnings("unchecked") // ok by construction
            ObjectToCounterMap<String> tokenCounter
                = (ObjectToCounterMap<String>)
                AbstractExternalizable.readObject(getArgumentFile(TOKEN_COUNTER_FILE_PARAM));
            mTokenCounter = tokenCounter;
            if (hasArgument(THRESHOLD_TOKENS_FLAG))
                mTokenCounter.prune(getArgumentInt(THRESHOLD_TOKENS_FLAG));
            mSpellChecker.setTokenSet(mTokenCounter.keySet());
        }

        if (hasFlag(DELETE_OFF_PARAM))
            mSpellChecker.setAllowDelete(false);
        if (hasFlag(SUBSTITUTE_OFF_PARAM))
            mSpellChecker.setAllowSubstitute(false);

        mSpellChecker.setMinimumTokenLengthToCorrect(getArgumentInt(MIN_TOKEN_LENGTH_PARAM));
        mSpellChecker.setNumConsecutiveInsertionsAllowed(getArgumentInt(CONSEC_INSERTS_PARAM));
        mSpellChecker.setKnownTokenEditCost(getArgumentDouble(KNOWN_TOKEN_PARAM));
        mSpellChecker.setFirstCharEditCost(getArgumentDouble(FIRST_CHAR_EDIT_PARAM));
        mSpellChecker.setSecondCharEditCost(getArgumentDouble(SECOND_CHAR_EDIT_PARAM));
        mSpellChecker.setNBest(getArgumentInt(BEAM_PARAM));

        FixedWeightEditDistance edDist =
            new FixedWeightEditDistance(getArgumentDouble(MATCH_PARAM),
                                        getArgumentDouble(DELETE_PARAM),
                                        getArgumentDouble(INSERT_PARAM),
                                        getArgumentDouble(SUBSTITUTE_PARAM),
                                        getArgumentDouble(TRANSPOSE_PARAM));
        mSpellChecker.setEditDistance(edDist);

        System.out.println(mSpellChecker.parametersToString());

        evaluate();
    }

    private void loadData(String[] lines) {
        List<String> dataLineSet = new  ArrayList<String>();
        List<String> originalQ = new ArrayList<String>();
        List<String> correctQ = new ArrayList<String>();

        for (int i = 0; i < lines.length; ++i) {
            if (!lines[i].startsWith("D:")) {
                if (lines[i].length() > 0)
                    throw new IllegalArgumentException("Illegal line=" + i + " line=" + lines[i]);
                continue;
            }
            dataLineSet.add(lines[i].substring(2));
            ++i;
            if (!lines[i].startsWith("O:"))
                throw new IllegalArgumentException("Illegal line=" + i + " line=" + lines[i]);
            originalQ.add(lines[i].substring(2));
            ++i;
            if (!lines[i].startsWith("C:"))
                throw new IllegalArgumentException("Illegal line=" + i + " line=" + lines[i]);
            correctQ.add(lines[i].substring(2));
        }

        mDataLine = dataLineSet.<String>toArray(new String[dataLineSet.size()]);
        mOriginalQ = originalQ.<String>toArray(new String[originalQ.size()]);
        mCorrectQ = correctQ.<String>toArray(new String[correctQ.size()]);
    }


    private double evaluate() {
        int userErrorSystemCorrect = 0;
        int userErrorSystemWrongCorrection = 0;
        int userErrorSystemNoSuggestion = 0;
        int userCorrectSystemCorrect = 0;
        int userCorrectSystemWrongCorrection = 0;
        int outOfVocabTokenCount = 0;
        int outOfVocabTokenCountSystemWrong = 0;

        for (int i = 0; i < mOriginalQ.length; ++i) {
            String rawData = mDataLine[i];
            String original = mOriginalQ[i];
            String correct = mCorrectQ[i];
            String system = mSpellChecker.didYouMean(original);

            if (outOfVocabToken(correct)) {
                ++outOfVocabTokenCount;
            }
            if (!original.equals(correct)) {
                if (correct.equals(system)) {
                    ++userErrorSystemCorrect;
                    report("ec",rawData,original,correct,system);
                    System.out.println("------- user error, spell check correctly");
                }
                if (!correct.equals(system) && !system.equals(original)) {
                    ++userErrorSystemWrongCorrection;
                    if (outOfVocabToken(correct)) {
                        ++outOfVocabTokenCountSystemWrong;
                    }
                    report("ee",rawData,original,correct,system);
                    System.out.println("------- user error, spell check wrong suggestion");
                }

                if (!correct.equals(system) && system.equals(original)) {
                    ++userErrorSystemNoSuggestion;
                    if (outOfVocabToken(correct)) {
                        ++outOfVocabTokenCountSystemWrong;
                    }
                    report("e_",rawData,original,correct,system);
                    System.out.println("------- user error, spell check no suggestion");
                }
            }
            if (original.equals(correct)) {
                if (correct.equals(system)) {
                    ++userCorrectSystemCorrect;
                    report("cc",rawData,original,correct,system);
                    System.out.println("------- user correct, spell check correct");
                }

                if (!system.equals(correct)) {
                    ++userCorrectSystemWrongCorrection;
                    if (outOfVocabToken(correct)) {
                        ++outOfVocabTokenCountSystemWrong;
                    }
                    report("ee",rawData,original,correct,system);
                    System.out.println("------- user correct, spell check incorrect");
                }
            }

        }


        double lookGood
            =  ((double) userErrorSystemCorrect)
            / (double) (userCorrectSystemWrongCorrection
                        + userErrorSystemWrongCorrection
                        + userErrorSystemCorrect);

        double savedQueries
            =  userErrorSystemCorrect
            / (double) (userErrorSystemCorrect
                        + userErrorSystemWrongCorrection
                        + userErrorSystemNoSuggestion);

        double score
            = userErrorSystemNoSuggestion * -.2
            + userErrorSystemWrongCorrection * -1
            + userErrorSystemCorrect * 1
            + userCorrectSystemWrongCorrection * -1.5;


        System.out.println("\n\n-------------+++++++++++++-------\n");
        System.out.println("user error: sys correct "+userErrorSystemCorrect+
                           ", sys incorrect "+userErrorSystemWrongCorrection+
                           ", sys no sugg "+userErrorSystemNoSuggestion);

        System.out.println("user correct: sys no suggestion "+userCorrectSystemCorrect+
                           ", sys incorrect "+userCorrectSystemWrongCorrection);

        if (mTokenCounter != null) {
            System.out.println("Out of vocab query count: "+outOfVocabTokenCount);
            System.out.println("Out of vocab query wrong: "+outOfVocabTokenCountSystemWrong);
        }

        System.out.println("Score "+ score);

        return score;
    }


    void report(String msg, String rawData, String original, String correct, String system) {
        System.out.println("\n" + msg + ":"
                           + "\nD:"
                           + rawData
                           + "\nO:"
                           + addCountsProbs(original)
                           + "\nC:"
                           + addCountsProbs(correct)
                           + "\nS:"
                           + addCountsProbs(system));
    }

    private String addCountsProbs(String string) {
        CompiledNGramProcessLM lm = mSpellChecker.languageModel();
        return mTokenCounter != null
            ? (addCounts(string) + " :" + lm.log2Estimate(string))
            : (string + " : " + lm.log2Estimate(string) + " ");
    }

    private String addCounts(String str) {
        String[] toks = str.split(" ");
        StringBuilder sb = new StringBuilder(" ");
        for (int i = 0; i < toks.length; ++i) {
            sb.append(toks[i] + " (");
            sb.append(mTokenCounter.containsKey(toks[i])
                      ? mTokenCounter.get(toks[i])
                      : "0");
            sb.append(") ");
        }
        return sb.toString();
    }


    private boolean outOfVocabToken(String str) {
        if (mTokenCounter == null)
            return false;
        for (String tok : str.split(" "))
            if (!mTokenCounter.containsKey(tok))
                return true;
        return false;
    }


    private static final String REFERENCE_FILE_PARAM = "refFile";
    private static final String MODEL_FILE_PARAM = "modelFile";
    private static final String TOKEN_COUNTER_FILE_PARAM = "tokenCounterFile";
    private static final String THRESHOLD_TOKENS_FLAG = "thresholdTokens";

    private static final String CONSEC_INSERTS_PARAM = "numConsecutiveInserts";
    private static final String KNOWN_TOKEN_PARAM = "knownTokenEditCost";
    private static final String FIRST_CHAR_EDIT_PARAM = "firstCharEditCost";
    private static final String SECOND_CHAR_EDIT_PARAM = "secondCharEditCost";
    private static final String MATCH_PARAM = "match";
    private static final String INSERT_PARAM = "insert";
    private static final String DELETE_PARAM = "delete";
    private static final String SUBSTITUTE_PARAM = "substitute";
    private static final String TRANSPOSE_PARAM = "transpose";
    private static final String DELETE_OFF_PARAM = "deleteOff";
    private static final String SUBSTITUTE_OFF_PARAM = "substituteOff";
    private static final String MIN_TOKEN_LENGTH_PARAM = "minTokLengthToEdit";

    private static final String BEAM_PARAM = "beam";


    public static void main(String args[]){
        EvaluateSpell cmd = new EvaluateSpell(args);
        cmd.run();
    }


}



