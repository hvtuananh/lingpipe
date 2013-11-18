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

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.ObjectToCounterMap;

import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * An <code>NBestTaggerEvaluator</code> provides an evaluation
 * framework for n-best taggers.  
 *
 * <p>Test cases may be added directly using the {@link
 * #addCase(Tagging,Iterator)} method, which accepts a reference
 * gold-standard tagging and a system response consisting of an
 * iterator such as the result produced by an n-best tagger.
 *
 * <p>A specific tagger may be supplied to the constructor or set
 * using the {@link #setTagger(NBestTagger)} method.  Test cases may
 * be supplied to the tagger through the object handler method {@link
 * #handle(Tagging)}, which accepts a gold-standard reference tagging.
 * The tagger is then used to produce the system response which is
 * then added as a test case.
 *
 * <p>The main n-best evaluation is the histogram returned
 * by {@link #nBestHistogram()}, which provides counts for
 * the number of times the correct reference result was
 * found in the response at a particular rank. 
 *
 * <p>The method {@link #recallAtN()} returns an array of
 * recall values indexed by the rank of results.  For instance,
 * {@code recallAtN()[0]} is the percentage of cases for which
 * the first-best result was correct, {@code recallAtN()[1]} is
 * the percentage of cases where the first-best result was 
 * returned as the first or second result (ranks 0 or 1).
 *
 * <p>A string-based representation of the last case that was
 * evaluated is available through {@link #lastCaseToString(int)}.
 * The report is based on a set of known tokens, which is up to
 * the evaluation client to provide; null values evaluate without
 * previously known tokens being used.
 *
 * <h3>Thread Safety</h3>
 *
 * <p>An n-best tagger evaluator must be read-write synchronized.
 * The write methods are {@code handle()}, {@code addCase()},
 * and {@code setTagger()}.
 * 
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public class NBestTaggerEvaluator<E> 
    implements ObjectHandler<Tagging<E>> {

    private final int mMaxNBest;
    private final int mMaxNBestToString;
    private NBestTagger<E> mTagger;
    private final ObjectToCounterMap<Integer> mNBestHistogram
        = new ObjectToCounterMap<Integer>();
    private int mNumCases = 0;
    private long mNumTokens = 0L;
    private int mLastCaseRank;
    private Tagging<E> mLastCase;


    /**
     * Construct an n-best tagger evaluator using the specified
     * tagger, restricting the response taggings to the maximum
     * number of outputs specified, and writing the specified
     * number of outputs to the last case string.
     *
     * @param tagger Tagger to evaluate, or {@code null} if cases
     * are added directly or the tagger is set later.
     * @param maxNBest Maximum number of n-best results in the
     * system response to evaluate.
     * @param maxNBestToString Maximum number of n-best results
     * to write to the string output for the last case.

     */
    public NBestTaggerEvaluator(NBestTagger<E> tagger,
                                int maxNBest,
                                int maxNBestToString) {
        mTagger = tagger;
        mMaxNBest = maxNBest;
        mMaxNBestToString = maxNBestToString;
    }
                                                
    /**
     * Returns the maximum number of results examined in the response
     * for each test case.
     *
     * @return Maximum n-best explored in responses.
     */
    public int maxNBest() {
        return mMaxNBest;
    }

    /**
     * Set the tagger to the specified value.
     *
     * @param tagger Tagger to use for evaluation.
     */
    public void setTagger(NBestTagger<E> tagger) {
        mTagger = tagger;
    }


    /**
     * Return the n-best tagger used for this evaluator.
     *
     * @return The tagger being evaluated.
     */
    public NBestTagger<E> tagger() {
        return mTagger;
    }

    /**
     * Add the specified reference tagging as a test case,
     * with a response tagging computed by the contained n-best
     * tagger.
     *
     * @param referenceTagging Reference tagging to evaluate.
     */
    public void handle(Tagging<E> referenceTagging) {
        mLastCase = referenceTagging;
        Iterator<ScoredTagging<E>> it
            = mTagger.tagNBest(referenceTagging.tokens(),mMaxNBest);
        addCase(referenceTagging,it);
    }

    /**
     * Add a test case consisting of the specified reference tagging
     * and iterator over responses. 
     *
     * @param referenceTagging Reference gold-standard tagging.
     * @param responseTaggingIterator System response as an iterator
     * over taggings.
     */
    public void addCase(Tagging<E> referenceTagging,
                        Iterator<ScoredTagging<E>> responseTaggingIterator) {
        ++mNumCases;
        mNumTokens += referenceTagging.size();
        mLastCase = referenceTagging;
        List<String> expectedTags = referenceTagging.tags();
        for (int i = 0; i < mMaxNBest && responseTaggingIterator.hasNext(); ++i) {
            Tagging<E> tagging = responseTaggingIterator.next();
            if (expectedTags.equals(tagging.tags())) {
                mNBestHistogram.increment(i);
                mLastCaseRank = i;
                return;
            }
        }
        mLastCaseRank = -1;
        mNBestHistogram.increment(-1);
    }

    /**
     * Return the histogram of results mapping ranks to the number
     * of test cases where the correct result was at that rank.
     *
     * @return Histogram of n-best result ranks.
     */
    public ObjectToCounterMap<Integer> nBestHistogram() {
        return mNBestHistogram;
    }

    /**
     * Return an array of recall values indexed by rank.  The
     * recall at rank n is defined as the percentage of test
     * cases in which the correct result was found at rank
     * n or better in the result.
     *
     * @return Array of recall at N values.
     */
    public double[] recallAtN() {
        double[] result = new double[mMaxNBest];
        double maxNBest = mMaxNBest;
        int sum = 0;
        for (int i = 0; i < mMaxNBest; ++i) {
            sum += mNBestHistogram.getCount(i);
            result[i] = sum / maxNBest;
        }
        return result;
    }

    /**
     * Return the number of test cases in this evaluation.
     *
     * @return Number of test cases.
     */
    public int numCases() {
        return mNumCases;
    }

    /**
     * Return the total number of tokens in all test
     * cases for this evaluator.
     *
     * @return Number of tokens tested.
     */
    public long numTokens() {
        return mNumTokens;
    }

    /**
     * Return a string-based representation of the last case
     * evaluated.  The n-best results will be printed up to the number
     * of results specified in the argument, limited by the maximum
     * n-best evaluated in the constructor.
     *
     * @param maxNBestReport Maximum number of n-best results to report.
     * @return String-based representation of the last case.
     */
    public String lastCaseToString(int maxNBestReport) {
        int max = Math.min(maxNBestReport,mMaxNBest);
        if (numCases() == 0)
            return "No cases seen yet.";
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb,Locale.US);
        sb.append("Last case n-best reference rank=" + mLastCaseRank + "\n");
        sb.append("Last case " + max + "-best:\n");
        sb.append("Correct,Rank,LogJointProb,Tags\n");
        List<E> tokenList = mLastCase.tokens();
        Iterator<ScoredTagging<E>> nBestIt = mTagger.tagNBest(tokenList,max);
        for (int n = 0; n < max && nBestIt.hasNext(); ++n) {
            sb.append(n == mLastCaseRank ? " *** " : "     ");
            ScoredTagging<E> scoredTagging = nBestIt.next();
            double score = scoredTagging.score();
            sb.append(n + "   " + format(score) + "  ");
            for (int i = 0; i < tokenList.size(); ++i)
                sb.append(scoredTagging.token(i) + "_" + TaggerEvaluator.pad(scoredTagging.tag(i),5));
            sb.append("\n");
        }        
        return sb.toString();
    }

    static String format(double x) {
	return String.format("%9.3f",x);
    }


}