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

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConditionalClassifierEvaluator;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Strings;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@code MarginalTaggerEvaluator} evaluates marginal taggers either
 * directly or by adding their outputs.
 *
 * <p>Test cases may be added directly using the method {@link
 * #addCase(Tagging,TagLattice)}, where the tagging is a reference
 * gold-standard tagging and the tag lattice the system response
 * tagging.  
 *
 * <p>Test cases may be added indirectly using the method {@link
 * #handle(Tagging)}.  With this use, the contained tagger in the
 * evaluator is used to generate the response tag lattice, which is
 * then added along with the reference tagging. 
 *
 * <p>The tagger may be set in the constructor, or may be set using
 * the method {@link #setTagger(MarginalTagger)}.  The tagger must
 * be non-null in order for a call to the handle method to succeed.
 *
 * <p>The token-by-token evaluation is available through the method
 * {@link #perTokenEval()}, which returns a classifier
 * evaluator.  The items for the classifier evaluator are the tokens,
 * and their classification is determined by the marginal probability
 * assignments for those tags by the tag lattice.  If the marginal
 * tagger evaluator stores tokens, they will be available as part of
 * the returned evaluator.
 * 
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public class MarginalTaggerEvaluator<E> 
    implements ObjectHandler<Tagging<E>> {


    private MarginalTagger<E> mTagger;
    private final Set<String> mTagSet;
    private final boolean mStoreTokens;
    private final ConditionalClassifierEvaluator<E> mEval;
    private Tagging<E> mLastReferenceTagging;

    /**
     * Construct an evaluator for the specified marginal tagger,
     * using the specifed tag set and flag indicating whether or
     * not to store input tokens.  
     * 
     * @param tagger The tagger to use or {@code null} if all cases
     * are added directly or the tagger's specified later.
     * @param tagSet Complete set of tags found in reference and
     * response taggings.
     * @param storeTokens Flag set to {@code true} to store input
     * tokens for return during classification.
     */
    public MarginalTaggerEvaluator(MarginalTagger<E> tagger,
                                   Set<String> tagSet,
                                   boolean storeTokens) {
        mTagger = tagger;
        mTagSet = new HashSet<String>(tagSet);
        mStoreTokens = storeTokens;
        String[] tags = tagSet.toArray(Strings.EMPTY_STRING_ARRAY);
        mEval = new ConditionalClassifierEvaluator<E>(null,tags,storeTokens);
    }


    /**
     * Returns the tagger for this evaluator.  The tagger may be null.
     *
     * @return Tagger for this evaluator.
     */
    public MarginalTagger<E> tagger() {
        return mTagger;
    }

    /**
     * Set the tagger for this tagger evaluated to the specified
     * tagger.
     *
     * @param tagger Tagger to be evaluated.
     */
    public void setTagger(MarginalTagger<E> tagger) {
        mTagger = tagger;
    }

    /**
     * Returns {@code true} if this evaluator is storing input
     * tokens.
     *
     * @return Whether or not this evaluator stores tokens.
     */
    public boolean storeTokens() {
        return mStoreTokens;
    }

    /**
     * Returns an unmodifiable view of the tags used by
     * this evaluator.
     *
     * @return Tags for this evaluator.
     */
    public Set<String> tagSet() {
        return Collections.unmodifiableSet(mTagSet);
    }
    
    /**
     * Add a case for evaluation consisting of the gold-standard
     * reference tagging and a lattice representing the output of
     * a marginal tagger.
     *
     * @param referenceTagging Reference gold-standard tagging.
     * @param responseLattice System response.
     * @throws IllegalArgumentException If the reference tagging
     * is not the same length as the response.
     */
    public void addCase(Tagging<E> referenceTagging,
                        TagLattice<E> responseLattice) {
        mLastReferenceTagging = referenceTagging;
        if (referenceTagging.size() != responseLattice.numTokens()) {
            String msg = "Reference and response must have the same number of tokens."
                + " Found referenceTagging.size()=" + referenceTagging.size()
                + " responseLattice.numTokens()=" + responseLattice.numTokens();
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < referenceTagging.size(); ++i) {
            if (!referenceTagging.token(i).equals(responseLattice.token(i))) {
                String msg = "Reference and response token lists must be the same."
                    + " referenceTagging.token(" + i + ")=|" + referenceTagging.token(i) + "|"
                    + " responseLattice.token(" + i + ")=|" + responseLattice.token(i) + "|";
                throw new IllegalArgumentException(msg);
            }
        }
        for (String tag : referenceTagging.tags()) {
            if (!mTagSet.contains(tag)) {
                String msg = "Unknown tag in reference tagging."
                    + " Unknown tag=" + tag
                    + " reference tagging=" + referenceTagging;
                throw new IllegalArgumentException(msg);
            }
        }
        List<String> responseTags = responseLattice.tagList();
        for (String tag : responseTags) {
            if (!mTagSet.contains(tag)) {
                String msg = "Unknown tag in output lattice."
                    + " Tag=" + tag
                    + " referenceTagging=" + referenceTagging;
                throw new IllegalArgumentException(msg);
            }
        }
        List<E> tokens = referenceTagging.tokens();
        for (int n = 0; n < tokens.size(); ++n) {
            E token = referenceTagging.token(n);
            List<String> tags = responseLattice.tagList();
            String referenceTag = referenceTagging.tag(n);
            ObjectToDoubleMap<String> tagToScore = new ObjectToDoubleMap<String>();
            for (int i = 0; i < tags.size(); ++i) {
                tagToScore.set(responseTags.get(i),
                               Math.exp(responseLattice.logProbability(n,i)));
            }
            List<String> responseTagsList = tagToScore.keysOrderedByValueList();
            String[] responseTagArray = responseTags.toArray(Strings.EMPTY_STRING_ARRAY);
            double[] responseProbs = new double[responseTags.size()];
            for (int i = 0; i < responseTagArray.length; ++i)
                responseProbs[i] = tagToScore.getValue(responseTagArray[i]);
            ConditionalClassification responseClassification
                = ConditionalClassification.createProbs(responseTagArray,responseProbs);
            mEval.addClassification(referenceTag,responseClassification,token);
        }
    }

    /**
     * Add a test case consisting of the specified reference tagging
     * along with the lattice output of the marginal tagger being
     * evaluated on the tokens of the reference tagging.
     *
     * @param referenceTagging Reference tagging to evaluate
     */
    public void handle(Tagging<E> referenceTagging) {
        List<E> tokens = referenceTagging.tokens();
        TagLattice<E> responseLattice = mTagger.tagMarginal(tokens);
        addCase(referenceTagging,responseLattice);
    }

    public String lastCaseToString(int maxTagsPerToken) {
        if (mLastReferenceTagging == null)
            return "No cases seen yet.";
        List<E> tokenList = mLastReferenceTagging.tokens();
        TagLattice<E> lattice = mTagger.tagMarginal(tokenList);
        StringBuilder sb = new StringBuilder();
        sb.append("Index Token  RefTag  (Prob:ResponseTag)*\n");
        for (int tokenIndex = 0; tokenIndex < tokenList.size(); ++tokenIndex) {
            ConditionalClassification tagScores = lattice.tokenClassification(tokenIndex);
            sb.append(TaggerEvaluator.pad(Integer.toString(tokenIndex),4));
            sb.append(TaggerEvaluator.pad(tokenList.get(tokenIndex),15));
            String refTag = mLastReferenceTagging.tag(tokenIndex);
            sb.append(TaggerEvaluator.pad(refTag,6));
            sb.append("  ");
            for (int i = 0; i < maxTagsPerToken; ++i) {
                double conditionalProb = tagScores.score(i);
                String tag = tagScores.category(i);
                sb.append(" " + NBestTaggerEvaluator.format(conditionalProb) 
                          + ":" + TaggerEvaluator.pad(tag,4));
                sb.append(tag.equals(refTag) ? "* " : "  ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the tagger evaluation on a per-token basis.
     *
     * @return Evaluation of the tagger as a classifier.
     */
    public ConditionalClassifierEvaluator<E> perTokenEval() {
        return mEval;
    }

}