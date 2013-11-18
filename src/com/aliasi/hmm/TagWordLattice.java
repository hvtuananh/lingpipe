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

package com.aliasi.hmm;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tag.TagLattice;

import com.aliasi.util.ScoredObject;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PACKAGE PRIVATIZED in 4.0
 * 
 * A <code>TagWordLattice</code> encodes a lattice resulting from
 * decoding a hidden Markov model (HMM).  The lattice encodes the
 * tokens used as input and the tag symbol table, as well as matrices
 * for transition, forward and backward scores.
 *
 * <P>The lattice probabilities are factored into start, transition,
 * and end probabilities.  In general, the start, transition and
 * forward probabilities include the emission probabilities of their
 * destination tag.  The backward probabilities include all emissions
 * up to, but not including, the indexed node.
 *
 * @author  Bob Carpenter
 * @version 4.0
 * @since   LingPipe2.1
 */
class TagWordLattice extends TagLattice<String> {

    final double[][][] mTransitions;
    final double[][] mForwards;
    final double[] mForwardExps;
    final double[][] mBacks;
    final double[] mBackExps;
    final double[] mStarts;
    final double[] mEnds;
    final String[] mTokens;
    final SymbolTable mTagSymbolTable;
    double mTotal = Double.NaN;
    double mLog2Total = Double.NaN;

    /**
     * Construct a tag-word lattice for the specified token inputs and
     * the specified tag symbol table with the specified estimates.
     * This constructor also allocates the forward and backward arrays
     * which are the size of the number of tokens times the number of
     * tags.
     *
     * <P>There are a number of consistency conditions on the input:
     *
     * <UL>
     *
     * <LI> The tag symbol table should not be empty.
     *
     * <LI> Start and end probability arrays must be the same length
     * as the number of tags in the symbol table.  Start tags include
     * the start tag estimate and the emission estimate for the first
     * token.  End tags only include the end transition probabilities.
     *
     * <LI> Transit probability array should be dimension of number of
     * tokens times number of tags times number of tags.  They should
     * contain estimates of transition from the first tag to the
     * second tag and emitting the token.
     *
     * <LI> Values of the transit probabilities for the first token are
     * ignored because they are computed by the start probs without
     * previous states.
     *
     * </UL>
     *
     * @param tokens Array of input tokens.
     * @param tagSymbolTable Symbol table for tags.
     * @param startProbs Array of start probabilities.
     * @param endProbs Array of end probabilities.
     * @param transitProbs Array of transition probabilities.
     * @throws IllegalArgumentException If any of the probabilities are
     * not between 0.0 and 1.0 inclusive.
     */
    public TagWordLattice(String[] tokens,
                          SymbolTable tagSymbolTable,
                          double[] startProbs,
                          double[] endProbs,
                          double[][][] transitProbs) {
        for (int i = 0; i < startProbs.length; ++i) {
            if (startProbs[i] < 0.0 || startProbs[i] > 1.0) {
                String msg = "startProbs[" + i + "]=" + startProbs[i];
                throw new IllegalArgumentException(msg);
            }
        }
        for (int i = 0; i < endProbs.length; ++i) {
            if (endProbs[i] < 0.0 || endProbs[i] > 1.0) {
                String msg = "endProbs[" + i + "]=" + endProbs[i];
                throw new IllegalArgumentException(msg);
            }
        }
        for (int i = 1; i < transitProbs.length; ++i) {
            for (int j = 0; j < transitProbs[i].length; ++j) {
                for (int k = 0; k < transitProbs[i][j].length; ++k) {
                    if (transitProbs[i][j][k] < 0.0 || transitProbs[i][j][k] > 1.0) {
                        String msg = "transitProbs[" + i + "][" + j + "][" + k + "]="
                            + transitProbs[i][j][k];
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }

        int numTags = tagSymbolTable.numSymbols();
        int numTokens = tokens.length;
        mStarts = startProbs;
        mEnds = endProbs;
        mTransitions = transitProbs;
        mTokens = tokens;
        mTagSymbolTable = tagSymbolTable;
        mForwards = new double[numTokens][numTags];
        mForwardExps = new double[numTokens];
        // Arrays.fill(mForwardExps,0.0);
        mBacks = new double[numTokens][numTags];
        mBackExps = new double[numTokens];
        // Arrays.fill(mBackExps,0.0);
        computeAll();
    }

    /**
     * Returns the array of tokens underlying this tag-word lattice.
     *
     * @return The array of tokens for this lattice.
     */
    public String[] tokens() {
        return mTokens;
    }

    /**
     * Return the symbol table for tags in this tag-word lattice.
     *
     * @return The symbol table for the lattice.
     */
    public SymbolTable tagSymbolTable() {
        return mTagSymbolTable;
    }

    /**
     * Returns a list of tag-score pairs for the specified token
     * index as scored objects in order of descending score.  The
     * scores are log (base 2) conditional probabilities of the tag
     * being assigned to the specified token given the token sequence.
     *
     * @param tokenIndex Token index whose tags are returned.
     * @return Scored tags for the specified index.
     */
    public List<ScoredObject<String>> log2ConditionalTagList(int tokenIndex) {
        double log2Total = log2Total();
        SymbolTable st = mTagSymbolTable;
        int numTags = st.numSymbols();
        List<ScoredObject<String>> scoredTagList
            = new ArrayList<ScoredObject<String>>();
        for (int tagId = 0; tagId < numTags; ++tagId) {
            String tag = st.idToSymbol(tagId);
            double log2P = log2ForwardBackward(tokenIndex,tagId);
            double condLog2P = log2P - log2Total;
            if (condLog2P > 0.0)
                condLog2P = 0.0;
            else if (Double.isNaN(condLog2P) || Double.isInfinite(condLog2P))
                condLog2P = com.aliasi.util.Math.log2(Double.MIN_VALUE);
            scoredTagList.add(new ScoredObject<String>(tag,condLog2P));
        }
        Collections.sort(scoredTagList,ScoredObject.reverseComparator());
        return scoredTagList;
    }

    /**
     * Returns the array of tag-score pairs for the specified token
     * index as scored objects in order of descending score.  The
     * scores are log (base 2) conditional probabilities of the tag
     * being assigned to the specified token given the token sequence.
     *
     * @param tokenIndex Token index whose tags are returned.
     * @return Array of scored tags for the specified index.
     */
    public ScoredObject<String>[] log2ConditionalTags(int tokenIndex) {
        double log2Total = log2Total();
        SymbolTable st = mTagSymbolTable;
        int numTags = st.numSymbols();
        @SuppressWarnings({"unchecked","rawtypes"}) // required for array alloc
        ScoredObject<String>[] scoredTags
            = (ScoredObject<String>[]) new ScoredObject[numTags];
        for (int tagId = 0; tagId < numTags; ++tagId) {
            String tag = st.idToSymbol(tagId);
            double log2P = log2ForwardBackward(tokenIndex,tagId);
            double condLog2P = log2P - log2Total;
            if (condLog2P > 0.0)
                condLog2P = 0.0;
            else if (Double.isNaN(condLog2P) || Double.isInfinite(condLog2P))
                condLog2P = com.aliasi.util.Math.log2(Double.MIN_VALUE);
            scoredTags[tagId] = new ScoredObject<String>(tag,condLog2P);
        }
        Arrays.sort(scoredTags,ScoredObject.reverseComparator());
        return scoredTags;
    }

    /**
     * Returns the array of tags with the best forward-backward
     * probabilities for each token position.
     *
     * <P><i>Note:</i> This is the independent optimization of each
     * position and is not guaranteed to yield the sequence of states
     * that has the highest probability.
     *
     * @return Array of tags with the best forward-backward scores.
     */
    public String[] bestForwardBackward() {
        String[] bestTags = new String[mTokens.length];
        int numTags = mTagSymbolTable.numSymbols();
        for (int i = 0; i < bestTags.length; ++i) {
            int bestTagId = 0;
            double bestFB = forwardBackward(i,0);
            for (int tagId = 1; tagId < numTags; ++tagId) {
                double fb = forwardBackward(i,tagId);
                if (fb > bestFB) {
                    bestFB = fb;
                    bestTagId = tagId;
                }
            }
            bestTags[i] = mTagSymbolTable.idToSymbol(bestTagId);
        }
        return bestTags;
    }

    /**
     * Return the probability of the lattice starting with the tag
     * with the specified identifier and emitting the first input token.
     *
     * @param tagId Identifier for the tag in the symbol table.
     * @return Start probability.
     * @throws IndexOutOfBoundsException If the tagId is out of bounds.
     */
    public double start(int tagId) {
        return mStarts[tagId];
    }

    /**
     * Return the log (base 2) probability of the lattice starting
     * with the tag with the specified identifier and emitting the
     * first input token.  See {@link #start(int)} for more
     * information.
     *
     * @param tagId Identifier for the tag in the symbol table.
     * @return Log start probability.
     * @throws IndexOutOfBoundsException If the tagId is out of bounds.
     */
    public double log2Start(int tagId) {
        return com.aliasi.util.Math.log2(start(tagId));
    }


    /**
     * Return the probability of the lattice ending with the specified
     * tag.  Note that this does not include the probability of
     * emitting the final token.
     *
     * @param tagId Identifier for the tag in the symbol table.
     * @return End probability.
     * @throws IndexOutOfBoundsException If the tag identifier is out
     * of bounds.
     */
    public double end(int tagId) {
        return mEnds[tagId];
    }

    /**
     * Return the log (base 2) probability of the lattice ending with
     * the specified tag.  See {@link #end(int)} for more information.
     *
     * @param tagId Identifier for the tag in the symbol table.
     * @return Log end probability.
     * @throws IndexOutOfBoundsException If the tag identifier is out
     * of bounds.
     */
    public double log2End(int tagId) {
        return com.aliasi.util.Math.log2(end(tagId));
    }

    /**
     * Returns the transtion probability for the specified token index
     * and source and target tag identifiers.  This transition
     * probability includes the transition from the source tag
     * to the target tag times the probability of the target tag
     * emitting the token at the specified index.
     *
     * <P>Note that the token index cannot be zero here, as it is the
     * index of the target of a transition.
     *
     * @param tokenIndex Index of token.
     * @param sourceTagId Identifier for source tag in symbol table.
     * @param targetTagId Identifier for target tag in symbol table.
     * @return Transition score from source tag to target tag arriving
     * at the specified token index.
     * @throws IndexOutOfBoundsException If the token index or either
     * tag identifier is out of bounds.
     */
    public double transition(int tokenIndex,
                             int sourceTagId, int targetTagId) {
        if (tokenIndex == 0) {
            String msg = "Token index must be > 0.";
            throw new IndexOutOfBoundsException(msg);
        }
        return mTransitions[tokenIndex][sourceTagId][targetTagId];
    }

    /**
     * Returns the log (base 2) transtion probability for the
     * specified token index and source and target tag identifiers.
     * See {@link #transition(int,int,int)} for more information.
     *
     * @param tokenIndex Index of token.
     * @param sourceTagId Identifier for source tag in symbol table.
     * @param targetTagId Identifier for target tag in symbol table.
     * @return Log transition probability from source tag to target
     * tag arriving at the specified token index.
     * @throws IndexOutOfBoundsException If the token index or either
     * tag identifier is out of bounds.
     */
    public double log2Transitions(int tokenIndex,
                                  int sourceTagId, int targetTagId) {
        return com.aliasi.util.Math.log2(transition(tokenIndex,
                                                    sourceTagId,targetTagId));
    }

    /**
     * Returns the forward probability up to the token of the
     * specified index and for the tag of the specified identifier.
     * The forward estimate includes the start probabilities and the
     * emissions up to and including the token at the specified index.
     *
     * @param tokenIndex Index of token.
     * @param tagId Identifier of tag in symbol table.
     * @return Forward probability for the token and tag.
     * @throws IndexOutOfBoundsException If the token index or the
     * tag identifier is out of bounds.
     */
    public double forward(int tokenIndex, int tagId) {
        return mForwards[tokenIndex][tagId]
            * java.lang.Math.pow(2.0,mForwardExps[tokenIndex]);
    }

    /**
     * Returns the log (base 2) of the forward probabilty up to the
     * token of the specified index and for the tag of the specified
     * identifier.  See {@link #forward(int,int)} for more
     * information.
     *
     * @param tokenIndex Index of token.
     * @param tagId Identifier of tag in symbol table.
     * @return Log forward probability for the token index and tag.
     * @throws IndexOutOfBoundsException If the token index or the
     * tag identifier is out of bounds.
     */
    public double log2Forward(int tokenIndex, int tagId) {
        return com.aliasi.util.Math.log2(mForwards[tokenIndex][tagId])
            + mForwardExps[tokenIndex];
    }


    /**
     * Returns the backward probability up to the token of the
     * specified index and for the tag of the specified identifier.
     * This includes the stop probability and emissions up to but
     * not including the specified token index.
     *
     * @param tokenIndex Index of token.
     * @param tagId Identifier of tag in symbol table.
     * @return Backward probability for the token and tag.
     * @throws IndexOutOfBoundsException If the token index or the
     * tag identifier is out of bounds.
     */
    public double backward(int tokenIndex, int tagId) {
        return mBacks[tokenIndex][tagId]
            * java.lang.Math.pow(2.0,mBackExps[tokenIndex]);

    }

    /**
     * Returns the log (base 2) backward probability up to the token
     * of the specified index and for the tag of the specified
     * identifier.  See {@link #backward(int,int)} for more information.
     *
     * @param tokenIndex Index of token.
     * @param tagId Identifier of tag in symbol table.
     * @return Log backward probability for the token and tag.
     * @throws IndexOutOfBoundsException If the token index or the
     * tag identifier is out of bounds.
     */
    public double log2Backward(int tokenIndex, int tagId) {
        return com.aliasi.util.Math.log2(mBacks[tokenIndex][tagId])
            + mBackExps[tokenIndex];
    }

    /**
     * Returns the product of the forward and backward probabilities
     * for the token with the specified index and tag with the
     * specified identifier.  Dividing this result by the total
     * probability as given by {@link #total()} results in the
     * normalized state probability between 0.0 and 1.0.  Furthermore,
     * the sum of all forward-backward probabilities at any given token
     * index is equal to the total lattice probability.
     *
     * @param tokenIndex Index of token.
     * @param tagId Identifier of tag in symbol table.
     * @return Forward-backward probability for the token and tag.
     * @throws IndexOutOfBoundsException If the token index or the
     * tag identifier is out of bounds.
     */
    public double forwardBackward(int tokenIndex, int tagId) {
        return forward(tokenIndex,tagId) * backward(tokenIndex,tagId);
    }

    /**
     * Returns the product of the forward and backward probabilities
     * for the token with the specified index and tag with the
     * specified identifier.  Dividing this result by the total
     * probability as given by {@link #total()} results in the
     * normalized state probability between 0.0 and 1.0.  Furthermore,
     * the sum of all forward-backward probabilities at any given token
     * index is equal to the total lattice probability.
     *
     * @param tokenIndex Index of token.
     * @param tagId Identifier of tag in symbol table.
     * @return Forward-backward probability for the token and tag.
     * @throws IndexOutOfBoundsException If the token index or the
     * tag identifier is out of bounds.
     */
    public double log2ForwardBackward(int tokenIndex, int tagId) {
        return log2Forward(tokenIndex,tagId)
            + log2Backward(tokenIndex,tagId);
    }


    /**
     * Returns the total probability for all paths in the lattice.
     * This probability is the marginal probability of the input
     * tokens. This probability will be equal to the sum of the
     * forward-backward probabilities at any token index.  If there
     * are no tokens in the lattice, the total probability is 1.0, and
     * the log probability is 0.0.

     * <P>The conditional probability of a state at a given token
     * position given the entire input is equal to the
     * forward-backward probability divided by the total
     * probability. The forward-backward probability is the joint
     * probability of the input tokens and state, whereas the total
     * probability is the probability of the input tokens.
     *
     * <P><i>Warning:</i> This value is likely to underflow for long
     * inputs; in this case use {@link #log2Total()} instead.  If
     * there are no tokens in the lattice, the total probability is
     * 1.0, and the log probability is 0.0.
     *
     * @return Total probability for the lattice.
     */
    public double total() {
        return mTotal;
    }

    /**
     * Returns the log (base 2) total probability for all paths in the
     * lattice.  See {@link #total()} for more information.
     *
     * @return Log total probability for the lattice.
     */
    public double log2Total() {
        return mLog2Total;
    }


    public double logForward(int token, int tag) {
        return com.aliasi.util.Math.logBase2ToNaturalLog(log2Forward(token,tag));
    }

    public double logBackward(int token, int tag) {
        return com.aliasi.util.Math.logBase2ToNaturalLog(log2Backward(token,tag));
    }


    public double logZ() {
        return com.aliasi.util.Math.logBase2ToNaturalLog(log2Total());
    }

    public double logTransition(int tokenFrom, int tagFrom, int tagTo) {
        return com.aliasi.util.Math.logBase2ToNaturalLog(log2Transitions(tokenFrom+1,tagFrom,tagTo));
    }

    public double logProbability(int tokenIndex, int tagId) {
        return com.aliasi.util.Math.logBase2ToNaturalLog(log2ForwardBackward(tokenIndex,tagId));
    }

    public double logProbability(int tokenTo, int tagFrom, int tagTo) {
        return logProbability(tokenTo-1,new int[] { tagFrom, tagTo }); // could unfold
    }

    public double logProbability(int tokenFrom, int[] tags) {
        int startTag = tags[0];
        int endTag = tags[tags.length-1];
        int tokenTo = tokenFrom + tags.length - 1;
        double logProb  = logForward(tokenFrom,startTag)
            + logBackward(tokenTo,endTag)
            - logZ();
        for (int n = 1; n < tags.length; ++n)
            logProb += logTransition(tokenFrom+n-1,tags[n-1],tags[n]);
        return logProb;
    }

    public int numTokens() {
        return mTokens.length;
    }

    public List<String> tokenList() {
        return Arrays.asList(mTokens);
    }

    public String token(int n) {
        return mTokens[n];
    }

    public int numTags() {
        return mTagSymbolTable.numSymbols();
    }

    public String tag(int n) {
        return mTagSymbolTable.idToSymbol(n);
    }

    public List<String> tagList() {
        List<String> result = new ArrayList<String>(numTags());
        for (int i = 0; i < numTags(); ++i)
            result.add(tag(i));
        return result;
    }

    final void computeAll() {
        computeForward();
        computeBackward();
        computeTotal();
    }

    private void computeTotal() {
        if (mForwards.length == 0) {
            mTotal = 1.0;
            mLog2Total = 0.0;
            return;
        }
        double total = 0.0;
        int numSymbols = tagSymbolTable().numSymbols();
        for (int tagId = 0; tagId < numSymbols; ++tagId)
            total += mForwards[0][tagId] * mBacks[0][tagId];
        double exp = mForwardExps[0] + mBackExps[0];
        mLog2Total = com.aliasi.util.Math.log2(total)
            + exp;
        mTotal = total * java.lang.Math.pow(2.0,exp);
    }

    private void computeForward() {
        if (mForwards.length == 0) return;
        int numSymbols = tagSymbolTable().numSymbols();
        double[] forwards = mForwards[0];
        for (int tagId = 0; tagId < numSymbols; ++tagId) {
            if (mStarts[tagId] < 0.0) {
                mStarts[tagId] = 0.0;
            }
            forwards[tagId] = mStarts[tagId];  // could assign array
        }
        mForwardExps[0] = log2ScaleExp(forwards);
        int numToks = mTokens.length;
        for (int tokenId = 1; tokenId < numToks; ++tokenId) {
            forwards = mForwards[tokenId-1];
            double[][] transits = mTransitions[tokenId];
            for (int tagId = 0; tagId < numSymbols; ++tagId) {
                double f = 0.0;
                for (int prevTagId = 0; prevTagId < numSymbols; ++prevTagId) {
                    f += forwards[prevTagId]
                        * transits[prevTagId][tagId];
                }
                mForwards[tokenId][tagId] = f;
            }
            mForwardExps[tokenId]
                = log2ScaleExp(mForwards[tokenId]) + mForwardExps[tokenId-1];
        }
    }

    private void computeBackward() {
        if (mBacks.length == 0) return;
        int numSymbols = tagSymbolTable().numSymbols();
        int lastTok = mTokens.length - 1;
        double[] backs = mBacks[lastTok];
        for (int tagId = 0; tagId < numSymbols; ++tagId)
            backs[tagId] = mEnds[tagId]; // could assign array
        mBackExps[lastTok] = log2ScaleExp(backs);
        for (int tokenId = lastTok; --tokenId >= 0; ) {
            backs = mBacks[tokenId+1];
            double[][] transits = mTransitions[tokenId+1];
            for (int tagId = 0; tagId < numSymbols; ++tagId) {
                double b = 0.0;
                for (int nextTagId = 0; nextTagId < numSymbols; ++nextTagId) {
                    b += backs[nextTagId]
                        * transits[tagId][nextTagId];
                }
                mBacks[tokenId][tagId] = b;
            }
            mBackExps[tokenId] = log2ScaleExp(mBacks[tokenId])
                + mBackExps[tokenId+1];
        }
    }

    

    // xs are linear probabilities here
    static double log2ScaleExp(double[] xs) {
        if (xs.length == 0) return 0.0;
        double max = xs[0];
        for (int i = 1; i < xs.length; ++i)
            if (max < xs[i]) max = xs[i];
        if (max < 0.0 || max > 1.0) {
            String msg = "Max must be >= 0 and <= 1."
                + " Found max=" + max;
            throw new IllegalArgumentException(msg);
        }
        if (max == 0.0) return 0.0;
        // can't we just do this analytically?
        double exp = 0.0;
        double mult = 1.0;
        while (max < 0.5) {
            exp -= 1.0;
            mult *= 2.0;
            max *= 2.0;
        }
        for (int j = 0; j < xs.length; ++j)
            xs[j] = xs[j] * mult;
        if (exp > 0) {
            String msg = "Exponent must be <= 0."
                + " Found exp=" + exp;
            throw new IllegalArgumentException(msg);
        }
        return exp;
    }


}
