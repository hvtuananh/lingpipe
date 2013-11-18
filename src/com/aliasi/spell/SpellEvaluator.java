/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 * 
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.spell;

import com.aliasi.classify.ConfusionMatrix;

import com.aliasi.lm.LanguageModel;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * The <code>SpellEvaluator</code> provides an evaluation harness for
 * spell checkers.  As with the other evaluator classes, it is
 * constructed with the spell checker that will be evaluated.  Test
 * cases are presented to the evaluator using the {@link
 * #addCase(String,String)} method.  The {@link #getLastCaseReport()}
 * method returns a string-based representation of the performance of
 * the most recently provided test case.  The method {@link
 * #toString()} provides a general report of results.
 *
 * <p>The method {@link #normalize(String)} may be used to normalize
 * both input text and system outputs before comparing them.  This
 * may be used to do an evaluation that is case or space or
 * punctuation insensitive, for example.
 *
 * <p>The basic output of the spell checker evaluation classifies
 * test cases into one of five categories:
 * 
 * <blockquote>
 * <table border="1" cellpadding="5">
 * <tr><td><i>User Input</i></td><td><i>System Suggestion</i></td><td>Status</td><td><i>Method</i></td></tr>
 * <tr><td>Correct</td><td>No Suggestion</td><td>TN</td>
 * <td><small>{@link #userCorrectSystemNoSuggestion()}</small></td></tr>

 * <tr><td>Correct</td><td>Wrong Suggestion</td><td>FP</td>
 * <td><small>{@link #userCorrectSystemWrongSuggestion()}</small></td></tr>

 * <tr><td>Error</td><td>Correct Suggestion</td><td>TP</td>
 * <td><small>{@link #userErrorSystemCorrect()}</small></td></tr>

 * <tr><td>Error</td><td>No Suggestion</td><td>FN</td>
 * <td><small>{@link #userErrorSystemNoSuggestion()}</small></td></tr>

 * <tr><td>Error</td><td>Wrong Suggestion</td><td>FN,FP</td>
 * <td><small>{@link #userErrorSystemWrongSuggestion()}</small></td></tr>
 * </table></blockquote>
 *
 * <p>The status indicates whether the case counts as a true positive
 * (TP), false positive (FP), true negative (TN), or false negative
 * (FN).  Note that if the user's input contains an error and the
 * system provides the wrong suggestion, the result counts as both a
 * false negative (failure to correct) and a false positive (erroneous
 * correction).  Because of the case of user
 * input error and wrong system correction, the confusion matrix count
 * is not quite one-to-one in size with the input size.  A confusion
 * matrix may be retrieved (populated with the above counts) through
 * the method {@link #confusionMatrix()}.
 *
 * <p>The methods for extracting the cases are listed in the final column
 * for each of the five result types.
 *
 * @author  Breck Baldwin
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.4.1
 */
public class SpellEvaluator {

    private final SpellChecker mSpellChecker;

    private final List<String> mTextList 
        = new ArrayList<String>();
    private final List<String> mCorrectTextList 
        = new ArrayList<String>();
    private final List<String> mSuggestionList
        = new ArrayList<String>();

    private String mLastCaseReport = "No cases added yet.";

    private int mUserCorrectSystemWrongSuggestion = 0;
    private int mUserCorrectSystemNoSuggestion = 0;
    private int mUserErrorSystemNoSuggestion = 0;
    private int mUserErrorSystemCorrect = 0;
    private int mUserErrorSystemWrongSuggestion = 0;

    private final ObjectToCounterMap<String> mTokenCounter;

    /** 
     * Construct a spelling evaluator for the specified spell checker.
     *
     * @param checker Spell checker to evaluate.
     */
    public SpellEvaluator(SpellChecker checker) {
        this(checker,null);
    }

    /** 
     * Construct a spelling evaluator for the specified spell checker
     * with the specified token counts.  The token counts will be
     * used to report counts of tokens in the corpus along with
     * per-line outputs.  If the token counter is <code>null</code>,
     * no token reports are provided.  In order for the token counts
     * to be used, the spell checker must be an instance of
     * {@link CompiledSpellChecker}.
     *
     * @param checker Spell checker to evaluate.
     * @param tokenCounter Counter for tokens in the speller.
     */
    public SpellEvaluator(SpellChecker checker,
                          ObjectToCounterMap<String> tokenCounter) {
        mSpellChecker = checker;
        mTokenCounter = tokenCounter;
    }

    /**
     * Adds a training case to the spelling evaluator in the form
     * of input text and its corrected form.  
     *
     * @param text Text to spell check.
     * @param correctText Correct form of input text.
     */
    public void addCase(String text, String correctText) {
        String normalizedText = normalize(text);
        String normalizedCorrectText = normalize(correctText);
        String suggestion = mSpellChecker.didYouMean(text);
        String normalizedSuggestion = (suggestion == null)
            ? normalizedText
            : normalize(suggestion);

        mTextList.add(normalizedText);
        mCorrectTextList.add(normalizedCorrectText);
        mSuggestionList .add(normalizedSuggestion);

        String resultDescription = null;
        if (normalizedText.equals(normalizedCorrectText)) {
            resultDescription = "user correct, ";
            if (normalizedText.equals(normalizedSuggestion)) {
                resultDescription += "spell check wrong suggestion (FP)";
                ++mUserCorrectSystemWrongSuggestion;
            } else {
                resultDescription += "spell check no suggestion (TN)";
                ++mUserCorrectSystemNoSuggestion;
            }
        } else {
            resultDescription = "user incorrect, ";
            if (normalizedText.equals(normalizedSuggestion)) {
                resultDescription += "spell check no suggestion (FN)";
                ++mUserErrorSystemNoSuggestion;
            } else if (normalizedCorrectText.equals(normalizedSuggestion)) {
                resultDescription += "spell check correct (TP)";
                ++mUserErrorSystemCorrect;
            } else {
                resultDescription += "spell check wrong suggestion (FP,FN)";
                ++mUserErrorSystemWrongSuggestion;
            }
        }
        
        StringBuilder sb = new StringBuilder();
        report(sb,"input",normalizedText);
        sb.append("\n");
        report(sb,"correct",normalizedCorrectText);
        sb.append("\n");
        report(sb,"suggest",normalizedSuggestion);
        sb.append("\n");
        
        mLastCaseReport = sb.toString();
    }


    void report(StringBuilder sb, String msg, String text) {
        sb.append(msg + "=|" + text + "|");
        if (!(mSpellChecker instanceof CompiledSpellChecker))
            return;
        CompiledSpellChecker checker = (CompiledSpellChecker) mSpellChecker;
        LanguageModel lm = checker.languageModel();
        double estimate 
            = lm.log2Estimate(" " + text + " ")
            - lm.log2Estimate(" ");

        sb.append(" log2 p=" + lpFormat(estimate));

        TokenizerFactory tf = checker.tokenizerFactory();
        char[] cs = text.toCharArray();
        Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);
        String[] tokens = tokenizer.tokenize();
        Set<String> tokenSet = checker.tokenSet();
        for (int i = 0; i < tokens.length; ++i) {
            sb.append(" ");
            sb.append(tokens[i]);
            sb.append("[");
            if (mTokenCounter == null)
                sb.append(tokenSet.contains(tokens[i]) ? "+" : "-");
            else
                sb.append(mTokenCounter.getCount(tokens[i]));
            sb.append("]");
        }
    }
    
    static final DecimalFormat LP_FORMAT = new DecimalFormat("#0.0");

    static String lpFormat(double x) {
        return LP_FORMAT.format(x);
    }
    

    /**
     * Return a string-based representation of the current status
     * of this evaluation.
     *
     * @return String-based representation of the evaluation.
     */
    @Override
    public String toString() {
        int userErrors = mUserErrorSystemWrongSuggestion
            + mUserErrorSystemCorrect
            + mUserErrorSystemNoSuggestion;
        int userCorrect = mUserCorrectSystemWrongSuggestion
            + mUserCorrectSystemNoSuggestion;
        int total = userErrors + userCorrect;

        StringBuilder sb = new StringBuilder();
        sb.append("EVALUATION\n");
        addReport(sb,"User Error", 
                  userErrors,total);
        addReport(sb,"     System Correct",
                  mUserErrorSystemCorrect,userErrors);
        addReport(sb,"     System Error",
                  mUserErrorSystemWrongSuggestion,userErrors);
        addReport(sb,"     System No Suggestion",
                  mUserErrorSystemNoSuggestion,userErrors);
        
        addReport(sb,"User Correct",
                  userCorrect,total);
        addReport(sb,"     System Error",
                  mUserCorrectSystemWrongSuggestion, userCorrect);
        addReport(sb,"     System No Suggestion",
                  mUserCorrectSystemNoSuggestion, userCorrect);

        sb.append("SPELL CHECKER toString()\n");
        sb.append(mSpellChecker);

        return sb.toString();
    }

    static void addReport(StringBuilder sb, String msg,
                          int correct, int total) {
        sb.append(msg);
        sb.append(": ");
        sb.append(correct);
        sb.append(" [");
        double percentage = (total > 0) ? (100.0 * correct)/total : 0;
        sb.append(PERCENT_FORMAT.format(percentage));
        sb.append("%]\n");
    }

    static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.0");

    /**
     * Returns an array of cases for which the user was correct
     * and the system made no suggestions.  The entries in the
     * array are of the form <code>{text,correct,suggestion}</code>.
     *
     * @return The user correct, system no suggestion cases.
     */
    public String[][] userCorrectSystemNoSuggestion() {
        return extract(true,true,true);
    }

    /**
     * Returns an array of cases for which the user was correct and
     * the system made an erroneous suggestion.  The entries in the
     * array are of the form <code>{text,correct,suggestion}</code>.
     *
     * @return The user correct, system wrong suggestion cases.
     */
    public String[][] userCorrectSystemWrongSuggestion() {
        return extract(true,false,false);
    }

    /**
     * Returns an array of cases for which the user made an error and
     * system returned the appropriate correction. The entries in the
     * array are of the form <code>{text,correct,suggestion}</code>.
     *
     * @return The user error, system correct cases.
     */
    public String[][] userErrorSystemCorrect() {
        return extract(false,true,false);
    }


    /**
     * Returns an array of cases for which the user made an error and
     * system returned the appropriate correction. The entries in the
     * array are of the form <code>{text,correct,suggestion}</code>.
     *
     * @return The user error, system correct cases.
     */
    public String[][] userErrorSystemWrongSuggestion() {
        return extract(false,false,false);
    }

    /**
     * Returns an array of cases for which the user made an
     * error and the systme made no suggestion. The entries in the
     * array are of the form <code>{text,correct,suggestion}</code>.
     *
     * @return The user error, system no suggestion cases.
     */
    public String[][] userErrorSystemNoSuggestion() {
        return extract(false,false,true);
    }

    
    String[][] extract(boolean textEqualsCorrect,
                       boolean correctEqualsSuggestion,
                       boolean textEqualsSuggestion) {
        List<String[]> result = new ArrayList<String[]>();
        for (int i = 0; i < mSuggestionList.size(); ++i) {
            String text = mTextList.get(i).toString();
            String correct = mCorrectTextList.get(i).toString();
            String suggestion = mSuggestionList.get(i).toString();
            if (text.equals(correct) == textEqualsCorrect
                && correct.equals(suggestion) == correctEqualsSuggestion
                && text.equals(suggestion) == textEqualsSuggestion) 

                result.add(new String[] { text, correct, suggestion });
        }
        return result.<String[]>toArray(Strings.EMPTY_STRING_2D_ARRAY);
    }

            
    /**
     * Returns a string-based representation of the last test case.
     *
     * @return A string-based representation of the last test case.
     */
    public String getLastCaseReport() {
        return mLastCaseReport;
    }

    /**
     * Returns the confusion matrix for the current state of this
     * evaluation.  The class documentation (see above) describes the
     * calculation of true positives, false positives, false
     * negatives, and true negatives.  The categories used are
     * <code>&quot;correct&quot;</code> and
     * <code>&quot;misspelled&quot;</code>.
     *
     * <p>The confusion matrix does not track this evaluator, so once
     * a confusion matrix is constructed and returned, it will not
     * reflect additional cases added to this evaluator.
     *
     * @return The confusion matrix for the current state of this evaluation.
     */
    public ConfusionMatrix confusionMatrix() {
        int tn = mUserCorrectSystemNoSuggestion;
        int tp = mUserErrorSystemCorrect;
        int fn = mUserErrorSystemNoSuggestion + mUserErrorSystemWrongSuggestion;
        int fp = mUserCorrectSystemWrongSuggestion;
        return new ConfusionMatrix(new String[] { "correct", 
                                                  "misspelled" },
                                   new int[][] { { tp, fp},
                                                 { fn, tn } });
    }

    /**
     * Return the normalized form of a query or system output.  This
     * method will be applied to the input text before sending it to
     * the spell checker and will be applied to the system suggestion
     * before comparing it to the correct text.  All cases are saved
     * in their normalized forms.
     *
     * <p>The default implementation in this class does nothing,
     * simply returning the input text.  Subclasses may override
     * this normalizer.
     *
     * @param text Text to normalize.
     * @return The normalized form of the text.
     */
    public String normalize(String text) {
        return text;
    }

}
