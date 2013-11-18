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

package com.aliasi.crf;

import com.aliasi.tag.TagLattice;

import com.aliasi.symbol.SymbolTable;
import com.aliasi.symbol.SymbolTableCompiler;

import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code ForwardBackwardTagLattice} provides an implementation of
 * a tag lattice based on forward, backward, transition and
 * normalizing values.  
 *
 * <p>The forward and backward values are available through {@link
 * #logBackward(int,int)} and {@link #logForward(int,int)}; see the
 * method doc for details on the indexing.  Transition values are
 * available through {@link #logTransition(int,int,int)}.  The
 * normalizing term is returned by {@link #logZ()}.
 *
 * <p>Typically, this class is used only as a return object, not as a
 * return type.  For instance, the marginal tagging interface for HMMs
 * and CRFs specifies the return type only as implementing {@code
 * TagLattice}.  The class is public here so that there is some
 * public way available for creating tag lattices from their component
 * pieces.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the lattice.
 */
public class ForwardBackwardTagLattice<E> extends TagLattice<E> {


    // mTokens.get(nTo:N)
    private final List<E> mTokens;
    private final List<String> mTags;

    // mLogForwards[nTo][kTo]
    private final double[][] mLogForwards;

    // mLogBackwards[nTo][kFrom:K]
    private final double[][] mLogBackwards;

    // mLogTransitions[nTo-1:(N-1)][kFrom:K][kTo:K]
    private final double[][][] mLogTransitions;

    // mLogZ = sum mLogForwards[nTo:(N-1)]
    private final double mLogZ;


    /**
     * Construct a log tag lattice from the specified list of tokens,
     * list of tags in order of symbol identifier along with log
     * forward, backward, transition and normalizing values on a log
     * probability scale.
     *
     * <p>The lists of tokens and tags are copied so that changes to
     * the supplied list will not affect the constructed lattice.
     *
     * <p>The probabilities returned are all relative the normalizing
     * term {@code logZ}, which might not even be negative.  True
     * probabilities are computed as described in the class
     * documentation above.
     *
     * <p>See the class documentation above for information on
     * how these arrays are used to compute probabilities.
     *
     * <p><b>Warning:</b> No check is made to ensure the log
     * probabilities are coherent, only that the arrays are the right size
     * and are filled with non-positive numbers.
     *
     * @param tokens Underlying tokens in order.
     * @param tags List of tags in symbol order.
     * @param logForwards Log forward probabilities.
     * @param logBackwards Log backward probabilities.
     * @param logTransitions Log transition probabilities.
     * @param logZ Log normalizing probability.
     * @throws IllegalArgumentException If the various arrays are not
     * properly sized, meaning for tokens of length N and tags of
     * length K, logForwards[N][K], logBackward[N][K],
     * logTransitions[N-1][K][K], where N is the size of the tokens
     * list and K the length of the tags array, or if any of the log
     * probabilities is not finite.
     */
    public ForwardBackwardTagLattice(List<E> tokens,
                                     List<String> tags,
                                     double[][] logForwards,
                                     double[][] logBackwards,
                                     double[][][] logTransitions,
                                     double logZ) {
        this(new ArrayList<E>(tokens),
             new ArrayList<String>(tags),
             logForwards,logBackwards,logTransitions,logZ,true);


        int N = tokens.size();
        int K = tags.size();
        if (logForwards.length != N) {
            String msg = "Log forwards must be length of input."
                + " tokens.size()=" + N
                + " logForwards.length=" + logForwards.length;
            throw new IllegalArgumentException(msg);
        }
        if (logBackwards.length != N) {
            String msg = "Log backwards must be length of input."
                + " tokens.size()=" + N
                + " logBackwards.length=" + logBackwards.length;
            throw new IllegalArgumentException(msg);
        }
        if (N > 0 && logTransitions.length != (N-1)) {
            String msg = "Log transitions length must be one shorter than input, or empty."
                + " Found tokens.size()=" + N
                + " logTransitions.length=" + logTransitions.length;
            throw new IllegalArgumentException(msg);
        }
        for (int n = 0; n < N; ++n) {
            if (logForwards[n].length != K) {
                String msg = "Each log forward must be length of tags."
                    + " Found tags.size()=" + K
                    + " logForwards[" + n + "]=" + logForwards[n];
                throw new IllegalArgumentException(msg);
            }
            if (logBackwards[n].length != K) {
                String msg = "Each log backward must be length of tags."
                    + " Found tags.size()=" + K
                    + " logBackwards[" + n + "]=" + logBackwards[n];
                throw new IllegalArgumentException(msg);
            }
        }
        for (int n = 1; n < N; ++n) {
            if (logTransitions[n-1].length != K) {
                String msg = "Each transition source must be length of tags."
                    + " Found tags.size()=" + tags.size()
                    + " logTransitions[" + (n-1) + "].length=" + logTransitions[n-1].length;
                throw new IllegalArgumentException(msg);
            }
            for (int k = 0; k < K; ++k) {
                if (logTransitions[n-1][k].length != K) {
                    String msg = "Each transition target must be length of tags."
                        + " Found tags.size()=" + tags.size()
                        + " logTransitions[" + (n-1) + "][" + k + "].length=" + logTransitions[n-1][k].length;
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }

    // use package internally to avoid construction check loops
    ForwardBackwardTagLattice(List<E> tokens,
                              List<String> tags,
                              double[][] logForwards,
                              double[][] logBackwards,
                              double[][][] logTransitions,
                              double logZ,
                              boolean ignore) {
        mTokens = tokens;
        mTags = tags;
        mLogForwards = logForwards;
        mLogBackwards = logBackwards;
        mLogTransitions = logTransitions;
        mLogZ = logZ;
    }


    public List<E> tokenList() {
        return Collections.unmodifiableList(mTokens);
    }

    public List<String> tagList() { 
        return Collections.unmodifiableList(mTags);
    }

    public String tag(int id) {
        return mTags.get(id);
    }

    public int numTags() {
        return mTags.size();
    }

    public E token(int n) {
        return mTokens.get(n);
    }

    public int numTokens() {
        return mTokens.size();
    }

    public SymbolTable tagSymbolTable() {
        return SymbolTableCompiler.asSymbolTable(mTags.toArray(Strings.EMPTY_STRING_ARRAY));
    }

    /**
     * Return the log of the conditional probability that the
     * specified token has the specified tag, given the complete list
     * of input tokens.
     *
     * @param token Position of input token.
     * @param tag Identifier of tag.
     * @return The log probability the token has the tag.
     * @throws ArrayIndexOutOfBoundsException If the token or tag
     * identifiers are not in range.
     */
    public double logProbability(int token, int tag) {
        return mLogForwards[token][tag]
            + mLogBackwards[token][tag]
            - mLogZ;
    }

    // convenience for logProbability(tokenTo-1,{tagFrom,tagTo})
    public double logProbability(int tokenTo, int tagFrom, int tagTo) {
        double logProb = mLogForwards[tokenTo-1][tagFrom]
            + mLogBackwards[tokenTo][tagTo]
            + mLogTransitions[tokenTo-1][tagFrom][tagTo]
            - mLogZ;
        return logProb;
    }

    /**
     * Return the log conditional probability that the tokens starting
     * with the specified token position have the specified tags given
     * the complete sequence of input tokens.
     *
     * @param tokenFrom Starting position of sequence.
     * @param tags Tag identifiers for sequence.
     * @return Log probability that sequence starting at the specified
     * position has the specified tags.
     * @throws IllegalArgumentException If the token is out of range or
     * the token plus the length of the tag sequence is out of range of
     * tokens, or if any of the tags is not a known identifier.
     */
    public double logProbability(int tokenFrom, int[] tags) {
        int startTag = tags[0];
        int endTag = tags[tags.length-1];
        int tokenTo = tokenFrom + tags.length - 1;
        double logProb  = mLogForwards[tokenFrom][startTag]
            + mLogBackwards[tokenTo][endTag]
            - mLogZ;
        for (int n = 1; n < tags.length; ++n)
            logProb += mLogTransitions[tokenFrom+n-1][tags[n-1]][tags[n]];
        return logProb;
    }

    public double logForward(int token, int tag) {
        return mLogForwards[token][tag];
    }

    public double logBackward(int token, int tag) {
        return mLogBackwards[token][tag];
    }

    public double logTransition(int tokenFrom, int tagFrom, int tagTo) {
        return mLogTransitions[tokenFrom][tagFrom][tagTo];
    }

    /**
     * Return the log of the normalizing constant for the lattice.
     * Its value is the log of the marginal probability of the input
     * tokens.  By the additive law of probability, this is equivalent
     * to the sum of the probabilities of all possible analyses for
     * the input sequence
     *
     * @return The normalizing constant.
     */
    public double logZ() {
        return mLogZ;
    }

    /**
     * Return a string-based representation of this tag lattice.
     * All of the information in the string is available programatically
     * through methods.
     *
     * @return String representation of this lattice.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mTokens.size(); ++i)
            sb.append("token[" + i + "]=" + mTokens.get(i) + "\n");
        sb.append("\n");
        for (int k = 0; k < mTags.size(); ++k)
            sb.append("tag[" + k + "]=" + mTags.get(k) + "\n");
        sb.append("\nlogZ=" + logZ() + "\n");
        sb.append("\nlogFwd[token][tag]\n");
        for (int i = 0; i < mTokens.size(); ++i)
            for (int k = 0; k < mTags.size(); ++k)
                sb.append("logFwd[" + i + "][" + k + "]=" + logForward(i,k) + "\n");
        sb.append("\nlogBk[token][tag]\n");
        for (int i = 0; i < mTokens.size(); ++i)
            for (int k = 0; k < mTags.size(); ++k)
                sb.append("logBk[" + i + "][" + k + "]=" + logBackward(i,k) + "\n");
        sb.append("\nlogTrans[tokenFrom][tagFrom][tagTo]\n");
        for (int i = 1; i < mTokens.size(); ++i)
            for (int kFrom = 0; kFrom < mTags.size(); ++kFrom)
                for (int kTo = 0; kTo < mTags.size(); ++kTo)
                    sb.append("logTrans[" + (i-1) + "][" + kFrom + "][" + kTo + "]=" + logTransition(i-1,kFrom,kTo) + "\n");
        
        return sb.toString();
    }


    static void verifyNonPos(String var, double x) {
        if (Double.isNaN(x) || x > 0.0) {
            String msg = var + " must be a non-positive number."
                + " Found " + var + "=" + x;
            throw new IllegalArgumentException(msg);
        }
    }



    static void verifyNonPos(String var, double[] xs) {
        for (int i = 0; i < xs.length; ++i) {
            if (Double.isNaN(xs[i]) || xs[i] > 0.0) {
                String msg = var + " must be a non-positive number."
                    + " Found " + var + "[" + i + "]=" + xs[i];
                throw new IllegalArgumentException(msg);
            }
        }
    }

    static void verifyNonPos(String var, double[][] xs) {
        for (int i = 0; i < xs.length; ++i) {
            for (int j = 0; j < xs[i].length; ++j) {
                if (Double.isNaN(xs[i][j]) || xs[i][j] > 0.0) {
                    String msg = var + " must be a non-positive number."
                        + " Found " + var + "[" + i + "][" + j + "]=" + xs[i][j];
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }

    static void verifyNonPos(String var, double[][][] xs) {
        for (int i = 0; i < xs.length; ++i) {
            for (int j = 0; j < xs[i].length; ++j) {
                for (int k = 0; k < xs[i][j].length; ++k) {
                    if (Double.isNaN(xs[i][j][k]) || xs[i][j][k] > 0.0) {
                        String msg = var + " must be finite and non-positive."
                            + " Found " + var + "[" + i + "][" + j + "][" + k + "]=" + xs[i][j][k];
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
    }

}
