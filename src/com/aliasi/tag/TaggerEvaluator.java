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

package com.aliasi.tag;

import com.aliasi.classify.Classification;
import com.aliasi.classify.BaseClassifierEvaluator;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A <code>TaggerEvaluator</code> provides evaluation for
 * first-best taggers implementing the {@code Tagger} interface.
 *
 * <p>The basis of evaluation is a gold-standard set of reference
 * taggings.  The evaluation is of taggings produced by a system (or
 * other means) known as response taggings.  Cases consisting of of
 * reference and response taggings may be added to the evaluation
 * using {@link #addCase(Tagging,Tagging)}.
 *
 * <p>The evaluator takes a tagger as an argument in its constructor.
 * If the tagger is not null, the {@link ObjectHandler} method {@link
 * #handle(Tagging)} may be used to supply reference taggings for
 * which a response will be created using the tagger and then added
 * as a test case.  The tagger may be reset using {@link #setTagger(Tagger)},
 * which is useful for producing a single evaluation of different
 * taggers, such as for cross-validation.
 * 
 * <p>The constructor also takes an argument determining whether
 * inputs should be stored or not.  If they are stored, then all input
 * tokens will be available in the token-level evaluation.  Tokens
 * must be stored in order to compute unknown token accuracy.
 *
 * <p>The overall case-level accuracy, measuring how many inputs
 * received a completely correct set of tags, is returned by {@link
 * #caseAccuracy()}.  
 *
 * <p>The method {@link #inputSizeHistogram()} returns a map from
 * integers to the number of reference taggings with that many
 * input tokens.
 *
 * <p>The {@link #lastCaseToString(Set)} may be used to return a
 * string-based representation of the last case added. This method
 * requires a set of the known tokens, or {@code null} if known tokens
 * are not being tracked.
 * 
 * <p>The primary results at the token level are returned as a
 * classifier evaluator by {@link #tokenEval()}.  The cases here
 * are individual tokens.  For instance, if there were 100 cases used
 * for training of 15 tokens each, the classifier evaluator will
 * consider 15*100 = 1500 cases, one for each token.  If the inputs
 * are stored, they will be passed on to this classifier evaluator and
 * available through the evaluator's methods.
 *
 * <p>Accuracy for tokens not in a specified set, typically the tokens
 * used in training, are available through {@link
 * #unknownTokenEval(Set)}.  
 *
 * <h3>Thread Safety</h3>
 *
 * This class is not thread safe, and access to it must be
 * synchronized using read/write locks.  The methods to
 * add cases, handle reference taggings, and set the tagger
 * are write methods; all other methods are reads.
 * 
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public class TaggerEvaluator<E> implements ObjectHandler<Tagging<E>> {

    private Tagger<E> mTagger;
    private final List<List<String>> mReferenceTagss
        = new ArrayList<List<String>>();
    private final List<List<String>> mResponseTagss
        = new ArrayList<List<String>>();
    private final List<List<E>> mTokenss;

    /**
     * Construct a tagger evaluator using the specified tagger that
     * stores inputs if the specified flag is {@code true}.
     *
     * @param tagger Tagger to use for generating responses, or {@code null}
     * if cases are added manually.
     * @param storeTokens Flag set to {@code true} if the input tokens
     * for cases are stored.
     */
    public TaggerEvaluator(Tagger<E> tagger, 
                           boolean storeTokens) {
        mTagger = tagger;
        mTokenss = storeTokens ? new ArrayList<List<E>>() : null;
    }

    /**
     * Return the tagger for this evaluator.
     *
     * @return The tagger for this evaluator.
     */
    public Tagger<E> tagger() {
        return mTagger;
    }

    /**
     * Set the tagger for this evaluator to the specified value.
     *
     * @param tagger Tagger to use to generate responses.
     */
    public void setTagger(Tagger<E> tagger) {
        mTagger = tagger;
    }

    /**
     * Returns {@code true} if this evaluator stores input tokens.
     *
     * @return {@code true} if this evaluator stores input tokens.
     */
    public boolean storeTokens() {
        return mTokenss != null;
    }

    /**
     * Add a case for the specified reference tagging using the
     * contained tagger to generate a response tagging.  
     *
     * @param referenceTagging Reference gold-standard tagging.
     * @throws NullPointerException If the underlying tagger is null.
     */
    public void handle(Tagging<E> referenceTagging) {
        List<E> tokens = referenceTagging.tokens();
        Tagging<E> responseTagging = mTagger.tag(tokens);
        addCase(referenceTagging,responseTagging);
    }

    /**
     * Add a test case to this evaluator consisting of the specified
     * reference and response taggings.
     *
     * @param referenceTagging Reference gold-standard tags.
     * @param responseTagging Response system tags.
     * @throws IllegalArgumentException If the token lengths are not
     * the same in the two taggings.
     */
    public void addCase(Tagging<E> referenceTagging,
                        Tagging<E> responseTagging) {
        if (!referenceTagging.tokens().equals(responseTagging.tokens())) {
            String msg = "Require taggings to have same tokens."
                + " Found referenceTagging.tokens() = " + referenceTagging.tokens()
                + " responseTagging.tokens()=" + responseTagging.tokens();
            throw new IllegalArgumentException(msg);
        }
        mReferenceTagss.add(referenceTagging.tags());
        mResponseTagss.add(responseTagging.tags());
        if (storeTokens())
            mTokenss.add(referenceTagging.tokens());
    }

    /**
     * Returns the number of cases for this evaluation.  Each
     * case consists of a reference and response tagging.
     *
     * @return The number of test cases.
     */
    public int numCases() { 
        return mReferenceTagss.size();
    }    

    /**
     * Returns the number of tokens tested in the complete set
     * of test cases.  
     *
     * @return The number of tokens evaluated.
     */
    public long numTokens() {
        long count = 0L;
        for (List<String> tags : mReferenceTagss)
            count += tags.size();
        return count;
    }

    /**
     * Return the list of tags seen so far by this tagger evaluator in
     * either references or responses.
     *
     * @return List of tags for this evaluator.
     */
    public List<String> tags() {
        Set<String> tagSet = new HashSet<String>();
        for (int i = 0; i < mReferenceTagss.size(); ++i) {
            tagSet.addAll(mReferenceTagss.get(i));
            tagSet.addAll(mResponseTagss.get(i));
        }
        return new ArrayList<String>(tagSet);
    }



    /**
     * Returns a mapping from integers to the number of test cases
     * with that many tokens.
     *
     * @return Histogram of input sizes.
     */
    public ObjectToCounterMap<Integer> inputSizeHistogram() {
        ObjectToCounterMap<Integer> hist = new ObjectToCounterMap<Integer>();
        for (List<String> tags : mReferenceTagss)
            hist.increment(tags.size());
        return hist;
    }


    /**
     * Return the accuracy at the entire case level.  This is
     * the percentage of test cases where the response tags
     * exactly matched the reference tags.
     *
     * @return Whole case accuracy.
     */
    public double caseAccuracy() {
        int correct = 0;
        for (int i = 0; i < mReferenceTagss.size(); ++i)
            if (mReferenceTagss.get(i).equals(mResponseTagss.get(i)))
                ++correct;
        return correct / (double) mReferenceTagss.size();
    }

    /**
     * Return the accuracy over known token set as an instance
     * of a classifier evaluator whose cases are individual
     * tokens not in the specified known token set.
     *
     * @param knownTokenSet Set of known tokens to exclude from
     * evaluation.
     * @return Evaluation over unknown tokens.
     * @throws UnsupportedOperationException If the inputs are not
     * being stored.
     */
    public BaseClassifierEvaluator<E> unknownTokenEval(Set<E> knownTokenSet) {
        if (!storeTokens()) {
            String msg = "Must store inputs to compute unknown token accuracy.";
            throw new UnsupportedOperationException(msg);
        }
        return eval(knownTokenSet);
    }

    /**
     * Returns the token-level evaluation for this tag evaluator.
     * If the input tokens were stored, they will be available in
     * the returned evaluator.
     *
     * @return Evaluation for this tagger.
     */
    public BaseClassifierEvaluator<E> tokenEval() {
        return eval(null);
    }

    /**
     * Return a string-based representation of the last case
     * to be evaluated based on the specified known token set.
     * If the known token set is {@code null}, known tokens are
     * not distinguished.
     *
     * @param knownTokenSet Set of known tokens.
     * @return String-based representation of last case evalaution.
     */
    public String lastCaseToString(Set<E> knownTokenSet) {
        if (mTokenss.isEmpty())
            return "No cases handled yet.";
        List<E> lastTokens = mTokenss.get(mTokenss.size()-1);
        List<String> refTags = mReferenceTagss.get(mReferenceTagss.size()-1);
        List<String> respTags = mResponseTagss.get(mResponseTagss.size()-1);
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb,Locale.US);
        sb.append("Known  Token     Reference | Response  ?correct\n");
        for (int tokenIndex = 0; tokenIndex < lastTokens.size(); ++tokenIndex) {
            sb.append(( knownTokenSet == null || knownTokenSet.contains(lastTokens.get(tokenIndex)))
                      ? "    "
                      : "  ? ");
            sb.append(pad(lastTokens.get(tokenIndex),20));
            sb.append(pad(refTags.get(tokenIndex),4));
            sb.append("  |  ");
            sb.append(pad(respTags.get(tokenIndex),6));
            if (refTags.get(tokenIndex).equals(respTags.get(tokenIndex)))
                sb.append("\n");
            else
                sb.append(" XX\n");
        }
        return sb.toString();
    }

    static String pad(Object obj, int length) {
        String in = obj.toString();
        if (in.length() > length) return in.substring(0,length-4) + "... ";
        if (in.length() == length) return in;
        StringBuilder sb = new StringBuilder(length);
        sb.append(in);
        while (sb.length() < length) sb.append(' ');
        return sb.toString();

    }

    BaseClassifierEvaluator<E> eval(Set<E> knownTokenSet) {
        String[] tags = tags().toArray(Strings.EMPTY_STRING_ARRAY);
        BaseClassifierEvaluator<E> evaluator
            = new BaseClassifierEvaluator<E>(null,tags,storeTokens());
        for (int i = 0; i < mReferenceTagss.size(); ++i) {
            List<String> referenceTags = mReferenceTagss.get(i);
            List<String> responseTags = mResponseTagss.get(i);
            List<E> tokens = mTokenss.get(i);
            for (int j = 0; j < tokens.size(); ++j) {
                String referenceTag = referenceTags.get(j);
                Classification responseClassification = new Classification(responseTags.get(j));
                if (knownTokenSet == null || !knownTokenSet.contains(tokens.get(j))) {
                    evaluator.addClassification(referenceTag,
                                                responseClassification,
                                                tokens.get(j));
                }
            }
        }
        return evaluator;
    }



}    
    
