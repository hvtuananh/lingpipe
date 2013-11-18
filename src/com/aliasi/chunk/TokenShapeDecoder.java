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

package com.aliasi.chunk;

import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.util.Strings;

import java.util.Arrays;

/**
 * A <code>Decoder</code> produces the most likely sequence of tags
 * for a given sequence of tokens according to a specified compiled
 * estimator and token categorizer.  The tag sequence should be
 * produced with the same tokenizer as was used to produce training
 * data for the estimator, and the same token categorizer should be
 * provided.
 *
 * <p> The decoder works through an interface where it accepts either
 * an array of tokens, or an array of token symbol table IDs. The
 * symbol table IDs can be produced externally, using the estimator's
 * symbol table methods.  </p>
 *
 * <p> The algorithm works by the standard Viterbi method, keeping a
 * first-best analysis for each history.  It only allocates memory one
 * slice at a time, so the space used is much less than would be
 * required for the full lattice.  Typically, it will be linear if
 * there are not too many live paths of back-pointers.  The beam value
 * is set during the constructor.  </p>
 *
 * @author  Bob Carpenter
 * @version 2.1.1
 * @since   LingPipe1.0
 *
 * @see CompiledEstimator
 * @see com.aliasi.tokenizer.Tokenizer
 * @see TokenCategorizer
 */
final class TokenShapeDecoder {

    /**
     * The threshold used for pruning during decoding.  Any hypothesis
     * further than log of the pruning threshold worse than the best
     * hypothesis at the end of each word of analysis will be pruned
     * from consideration.  Setting this value low will prune more
     * agressively, but might lead to search errors.  Typically, this
     * value is set rather high (around <code>8.0</code>, so that
     * there are few or no search errors. In tuning for a final
     * release, it will then be adjusted downward to improve search
     * speed to the lowest point possible without introducing search
     * errors.
     */
    private double mLog2Beam;

    /**
     * The estimator which generates probability estimates of to be
     * used during decoding.
     */
    private final CompiledEstimator mEstimator;

    /**
     * Categorizer to use for unknown tokens.
     */
    private final TokenCategorizer mTokenCategorizer;

    /**
     * Construct a decoder by reading its model from the specified
     * file, and using the specified token categorizer.
     *
     * @param estimatorFile File from which to read the estimator.
     * @param categorizer Token categorizer.
     * @throws IOException If there is an exception reading the
     * estimator from the specified file.
     public TokenShapeDecoder(File estimatorFile, TokenCategorizer categorizer,
     double pruningThreshold) throws IOException {

     this(readEstimator(estimatorFile,categorizer),
     categorizer, pruningThreshold);
     }
    */

    /**
     * Construct a decoder that will generate hypotheses based
     * on the specified estimator and token categorizer.
     *
     * @param estimator Compiled estimator for this decoder.
     * @param categorizer Token categorizer for this decoder.
     */
    public TokenShapeDecoder(CompiledEstimator estimator,
			     TokenCategorizer categorizer,
			     double pruningThreshold) {
        mEstimator = estimator;
        mTokenCategorizer = categorizer;
        mLog2Beam = pruningThreshold;
    }

    void setLog2Beam(double beam) {
	mLog2Beam = beam;
    }

    /**
     * Produces an array of tags from an array of tokens, where
     * the array of tags represents the best hypothesis the
     * decoder could find using the specified estimator for the
     * input tokens.
     *
     * @param tokens Array of strings representing tokens to decode.
     * @return Array of strings representing tags.
     */
    public String[] decodeTags(String[] tokens) {
        if (tokens == null) return null;
        if (tokens.length == 0) return Strings.EMPTY_STRING_ARRAY;
        TagHistory th = decode(tokens);
        String[] result = new String[tokens.length];
	if (th == null) {
	    // last resort recover to all OUT tags
	    Arrays.fill(result,Tags.OUT_TAG);
	    return result;
	}
        th.toTagArray(mEstimator, result);
        return result;
    }


    /**
     * Returns a tag history, which is a linked list of identifiers
     * from a symbol table.
     *
     * @param tokens The tokens to decode.
     * @return Tag history representing first-best analysis.
     */
    private TagHistory decode(String[] tokens) {
        int numTags = mEstimator.numTags();
        TagHistory[] history = new TagHistory[numTags];
        double[] historyScore = new double[numTags];
        TagHistory[] nextHistory = new TagHistory[numTags];
        double[] nextHistoryScore = new double[numTags];

        int startTagID = mEstimator.tagToID(Tags.START_TAG);
        int startTokenID = mEstimator.tokenToID(Tags.START_TOKEN);

        int tokenID;
        int tokenMinus1ID = startTokenID;
        int tokenMinus2ID = startTokenID;

	int outTagID = mEstimator.tagToID(Tags.OUT_TAG);

        // Handle First Token
        String token = tokens[0];
        tokenID = mEstimator.tokenToID(token);

        // unknown word treated as category for outcomes and contexts
        if (tokenID < 0) {
            String tokenCategory = mTokenCategorizer.categorize(token);
            tokenID = mEstimator.tokenToID(tokenCategory);
	    /*
	      if (tokenID < 0) {
	      // error
	      // do not return -- will just get low backoff estimate
	      }
	    */
        }

        // create score for each result tag for first token
        for (int resultTagID = 0; resultTagID < numTags; ++resultTagID) {
            if (mEstimator.cannotFollow(resultTagID,startTagID)) {
                historyScore[resultTagID] = Double.NaN;
                continue;
            }
            historyScore[resultTagID]
                = mEstimator.estimate(resultTagID, tokenID,
                                      startTagID,
                                      tokenMinus1ID,
                                      tokenMinus2ID);
        
	    /*
	      if (Double.isNaN(historyScore[resultTagID])) {
	      historyScore[resultTagID] = java.lang.Math.log(.00001);
	      }
	    */

            history[resultTagID] = ( Double.isNaN(historyScore[resultTagID])
                                     ? (TagHistory)null
                                     : new TagHistory(resultTagID,null)  );
	    // could prune here
        }

        // Handle Remaining Tokens
        for (int i = 1; i < tokens.length; ++i) {

            token = tokens[i];
            tokenID = mEstimator.tokenToID(token);

            // unknown word treated as category for outcomes and contexts
            if (tokenID < 0) {
                String tokenCategory = mTokenCategorizer.categorize(token);
                tokenID
                    = mEstimator.tokenToID(tokenCategory);
		/*
		  if (tokenID < 0) {
		  // error - no backoff category
		  }
		*/
            }

            // create score for each result tag
            for (int resultTagID = 0; resultTagID < numTags; ++resultTagID) {
                int bestPreviousTagID = -1;
                double bestScore = Double.NaN;

                // consider extending each previous tag
                for (int previousTagID = 0; previousTagID < numTags; ++previousTagID) {
                    if (history[previousTagID] == null) continue;

                    if (mEstimator.cannotFollow(resultTagID,previousTagID))
                        continue;

                    // use internal for estimate
                    double estimate = mEstimator.estimate(resultTagID, tokenID,
                                                          previousTagID,
                                                          tokenMinus1ID,
                                                          tokenMinus2ID);
                    if (!Double.isNaN(estimate)
                        && (bestPreviousTagID == -1
                            || ( estimate + historyScore[previousTagID]
                                 > bestScore))) {
                        bestPreviousTagID = previousTagID;
                        bestScore = estimate + historyScore[previousTagID];
                    }
                }

                // choose best history to extend to produce this tag for
                // this token, or set null if there is none possible
                if (bestPreviousTagID == -1) {
                    nextHistory[resultTagID] = null;
                } else {
                    nextHistory[resultTagID]
                        = new TagHistory(resultTagID,
                                         history[bestPreviousTagID]);
                    nextHistoryScore[resultTagID] = bestScore;
                }
            }

            int[] startIds = mEstimator.startTagIDs();
            int[] interiorIds = mEstimator.interiorTagIDs();
            for (int m = 0; m < startIds.length; ++m) {
                if (nextHistory[startIds[m]] == null
                    || nextHistory[interiorIds[m]] == null) continue;
                if (nextHistoryScore[startIds[m]] >
                    nextHistoryScore[interiorIds[m]]) {

                    nextHistoryScore[interiorIds[m]] = Double.NaN;
                    nextHistory[interiorIds[m]] = null;
                } else {
                    nextHistoryScore[startIds[m]] = Double.NaN;
                    nextHistory[startIds[m]] = null;
                }

            }

            // compute score of best hypothesis up to this token
            double bestScore = Double.NaN;
	    TagHistory bestPreviousHistory = null;
            for (int resultTagID = 0; resultTagID < numTags; ++resultTagID) {
                if (nextHistory[resultTagID] == null) continue;
                if (Double.isNaN(bestScore)
                    || nextHistoryScore[resultTagID] > bestScore) {
                    bestScore = nextHistoryScore[resultTagID];
		    bestPreviousHistory = nextHistory[resultTagID];
                }
            }

            // prune all results that are too far below best hypothesis
	    double worstScoreToKeep = bestScore - mLog2Beam;
            for (int resultTagID = 0; resultTagID < numTags; ++resultTagID) {
		// no OUT pruning
		if (resultTagID == outTagID) {
		    if (nextHistory[outTagID] == null) {
			nextHistory[outTagID]  // fill if necessary
			    = new TagHistory(outTagID,bestPreviousHistory);
			if (Double.isNaN(nextHistoryScore[outTagID])
			    || Double.isInfinite(nextHistoryScore[outTagID])) {
			    nextHistoryScore[outTagID] = bestScore;
			}
		    }
		    continue; // no OUT pruning
		} 
                if (nextHistory[resultTagID] == null) continue;
                if (nextHistoryScore[resultTagID] < worstScoreToKeep)
                    nextHistory[resultTagID] = null;
            }

            // bail if there aren't any more histories to extend
            if (allNull(nextHistory)) {
                // couldn't extend past token, even w/o OUT pruning
		return null;
            }

            // update histories before handling next token
            tokenMinus2ID = tokenMinus1ID;
            tokenMinus1ID = tokenID;
            TagHistory[] tempHistory = history;
            double[] tempHistoryScore = historyScore;
            history = nextHistory;
            historyScore = nextHistoryScore;
            nextHistory = tempHistory;
            nextHistoryScore = tempHistoryScore;
        }
        // return best history for final token
        return extractBest(history,historyScore);
    }

    /**
     * Returns the best scoring tag history from the specified
     * parallel arrays of histories and scores.  Returns
     * <code>null</code> if there are no non-<code>null</code>
     * entries in the history array.
     */
    private TagHistory extractBest(TagHistory[] history,
                                   double[] historyScore) {
        int bestIndex = -1;
        for (int i = 0; i < history.length; ++i) {
            if (history[i] == null) continue;
            else if (bestIndex == -1) bestIndex = i;
            else if (historyScore[i] > historyScore[bestIndex]) bestIndex = i;
        }
        return bestIndex == -1 ? null : history[bestIndex];
    }

    /**
     * Returns <code>true</code> if every element of the specified array is
     * <code>null</code>.
     */
    private static boolean allNull(Object[] xs) {
        for (int i = 0; i < xs.length; ++i)
            if (xs[i] != null)
                return false;
        return true;
    }

    /**
     * A tag history stores a linked list of integers, each of which
     * is an identifier in a symbol table for a tag.
     *
     * @author  Bob Carpenter
     * @version 1.0
     * @since   LingPipe1.0
     */
    private static final class TagHistory {

        /**
         * The tag in the tag history.
         */
        private final int mTag;

        /**
         * The previous tag history, or <code>null</code> if this
         * is the first history.
         */
        private final TagHistory mPreviousHistory;

        /**
         * Construct a tag history with the specified identifier for
         * tags, and the specified previous history, which may be
         * <code>null</code>.
         *
         * @param tag Identifier for tag.
         * @param previousHistory Previous tag history (the
         * backpointer) or <code>null</code> if this is the first
         * history.
         * @param previousHistory
         */
        public TagHistory(int tag, TagHistory previousHistory) {
            mTag = tag;
            mPreviousHistory = previousHistory;
        }

        /**
         * Writes the tag history into the specified result
         * array.
         *
         * @param estimator Estimator used for symbol table.
         * @param result Array into which tags are written.
         */
        public void toTagArray(CompiledEstimator estimator,
                               String[] result) {
            TagHistory history = this;
            for (int i = result.length;
                 --i >= 0;
                 history = history.mPreviousHistory)

                result[i] = estimator.idToTag(history.mTag);
        }
    }


}
