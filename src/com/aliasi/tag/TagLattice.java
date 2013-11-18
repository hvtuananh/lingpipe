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

import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.Strings;

import java.util.List;

/**
 * The abstract base class {@code TagLattice} provides an interface
 * for the output of a marginal tagger, based on forward, backward,
 * and transition log potentials.  The allowance of general potentials
 * makes this interface suitable for the output of either hidden
 * Markov models (HMM) or linear chain conditional random fields
 * (CRF).
 *
 * <h3>Marginal Tag Probabilities</h3>
 *
 * <p>The forward, backward, and normalizing terms are used to define
 * the marginal probability of a single tag at a specified position
 * given the inputs:
 *
 * <blockquote><pre>
 * p(tag[n]=k | toks[0],...,toks[N-1])
 * &nbsp;&nbsp;&nbsp;= fwd(n,k) * bk(n,k) / Z</pre></blockquote>
 *
 * To avoid problems with overflow, natural log values are
 * used.  On the log scale,
 *
 * <blockquote><pre>
 * log p(tag[n]=k | toks[0],...,toks[N-1])
 * &nbsp;&nbsp;&nbsp;= log fwd(n,k) + log bk(n,k) - log Z</pre></blockquote>
 *
 * <b>Warning:</b> No checks are made to ensure that values
 * supplied to the constructor form a coherent probability
 * estimate.
 *
 * <h3>Marginal Tag Sequence Probabilities</h3>
 *
 * <p>This allows us to compute the probability of a sequence of tags,
 * for instance for a phrase, by:
 *
 * <blockquote><pre>
 * p(tag[n]=k0, tag[n+1]=k1, ..., tag[n+m]=km | toks)
 * &nbsp;&nbsp;&nbsp;= fwd(n,k0) * bk(n+m,km) * <big><big>&Pi;</big></big><sub>i &lt; m</sub> trans(n+i,tags[n+i],tags[n+i+1]) / Z</pre></blockquote>
 * On the log scale, this becomes:
 *
 * <blockquote><pre>
 * log p(tag[n]=k0, tag[n+1]=k1, ..., tag[n+m]=km | toks)
 * &nbsp;&nbsp;&nbsp;= log fwd(n,k0) + log bk(n+m,km) * <big><big>&Sigma;</big></big><sub>i &lt; m</sub> log trans(n+i,tags[n+i],tags[n+i+1]) - log Z</pre></blockquote>


 * The log of this value returned by {@link #logProbability(int,int[])}, where
 * the first argument is {@code n} and the second argument
 * {@code {k0,k1,...,km}}.
 *
 *
 * <h3>Linear Probabilities</h3>
 *
 * Linear probabilities (or potentials) are computed by exponentiating
 * the log probabilities returned by the various methods.
 *
 *
 * <h3>Transition Potentials</h3>
 *
 * <p>The transitions are defined by:
 *
 * <blockquote><pre>
 * trans(n,k1,k2) = &phi;(tag[n]=k2|tag[n-1]=k1)</pre></blockquote>
 *
 * where <code>&phi;</code> is the transition potential from the
 * tag k1 at position n-1 to tag k2 at position n.
 * The log of this value is returned by {@link #logTransition(int,int,int)},
 * where the first argument is {@code n} and the second two {@code k1}
 * and {@code k2}.
 *
 * <h3>Forward Potentials</h3>
 *
 * <p>The forward values are defined by:
 *
 * <blockquote><pre>
 * log fwd(0,k) = init(k)</pre></blockquote>
 *
 * where {@code init(k)} is the start potential, defined on an
 * application-specific basis.  The recursive step defines the forward
 * values for subsequent positions 0 &lt; n &lt; N& and tags k
 *
 * <blockquote><pre>
 * fwd(n,k) = <big><big>&Sigma;</big></big><sub>k'</sub> fwd(n-1,k') * trans(n-1,k,k')</pre></blockquote>
 *
 * This is typically computed for all n and k in n*k time using the
 * forward algorithm.
 *
 * <p>The log of this value is returned by {@link #logForward(int,int)}
 * where the first argument is {@code n} and the second {@code k}.
 *
 * <h3>Backward Potentials</h3>
 *
 * <p>The backward potentials are similar, but condition on rather
 * than predict the label.  This simplifies the basis of the
 * recursion to
 *
 * <blockquote><pre>
 * bk(N-1,k) = 1.0</pre></blockquote>
 *
 * where N is the length of the input (number of tokens).  The
 * recursive step for 0 &lt;= n &lt; N-1 is
 *
 * <blockquote><pre>
 * bk(n,k) = <big><big>&Sigma;</big></big><sub>k'</sub> trans(n,k,k') * bk(n+1,k')</pre></blockquote>
 *
 * The log of this value is returned by {@link #logBackward(int,int)}
 * where the first argument is {@code n} and the second {@code k}.
 *
 * <h3>Normalizer</h3>
 *
 * <p>The normalizer is the sum over all paths, and acts to
 * normalize sequences to probabilities.  It may be computed
 * by:
 *
 * <blockquote><pre>
 * Z = <big><big>&Sigma;</big></big><sub>k</sub> fwd(N-1,k)</pre></blockquote>
 *
 * The log of this value is returned by {@link #logZ()}.
 *
 *
 * <h3>Probabilistic Lattices</h3>
 *
 * <p>In some settings, such as hidden Markov models (HMMs) the
 * forward, backward, transition and normalizer may be interpreted as
 * probabilities.
 *
 * <blockquote><pre>
 * trans(n,k1,k2) = p(tag[n]=k2|tag[n-1]=k1)</pre></blockquote>

 * <blockquote><pre>
 * fwd(n,k) = p(label[n]=k, toks[0], ..., toks[n])</pre></blockquote>
 * </pre></blockquote>
 *
 * <blockquote><pre>
 * bk(n,k) = p(toks[n+1],...,toks[N-1] | label[n]=k)</pre></blockquote>
 *
 * <blockquote><pre>
 * Z = p(toks[0],...,toks[N-1])</pre></blockquote>
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the lattice.
 */
public abstract class TagLattice<E> {

    /**
     * Construct an empty tag lattice.
     */
    public TagLattice() {
        /* no op */
    }

    /**
     * Return an unmodifiable view of the underlying tokens for this
     * tag lattice.  
     *
     * @return The tokens for this lattice.
     */
    public abstract List<E> tokenList();

    /**
     * Returns an unmodifiable view of the list of tags
     * used in this lattice, indexed by identifier.
     *
     * @return The symbol table for tags.
     */
    public abstract List<String> tagList();

    /**
     * Return the tag with the specified symbol identifier.
     *
     * @param id Identifer for tag.
     * @return Tag with specified ID.
     * @throws IndexOutOfBoundsException If the specified identifier is
     * not in range for the list of tags.
     */
    public abstract String tag(int id);

    /**
     * Return the number of tags in this tag lattice.
     *
     * @return Number of tags for this tag lattice.
     */
    public abstract int numTags();

    /**
     * Return the token at the specified position in the input.
     *
     * @param n Input position.
     * @return Token at position.
     * @throws IndexOutOfBoundsException If the specified index is
     * not in range for the list of tokens.
     */
    public abstract E token(int n);

    /**
     * Returns the length of this tag lattice as measured
     * by number of tokens.
     *
     * @return Number of tokens in this lattice.
     */
    public abstract int numTokens();

    /**
     * Returns a symbol table which converts tags to identifiers and
     * vice-versa.  
     *
     * <p>A new symbol table is constructed for each call, so it
     * should be saved and reused if possible.  Changing the returned
     * symbol table will not affect this lattice.  
     *
     * @return Symbol table for tags in this lattice.
     */
    public abstract SymbolTable tagSymbolTable();

    /**
     * Convenience method returning the log of the conditional
     * probability that the specified token has the specified tag,
     * given the complete list of input tokens.
     *
     * <p>This method returns results defined by
     *
     * <blockquote><pre>
     * logProbability(n,tag)
     *     == logProbability(n,new int[] { tag })</pre></blockquote>
     *
     * @param n Position of input token.
     * @param tag Identifier of tag.
     * @return The log probability the token has the tag.
     * @throws ArrayIndexOutOfBoundsException If the token or tag
     * identifiers are not in range.
     */
    public abstract double logProbability(int n, int tag);


    /**
     * Convenience method returning the log of the conditional
     * probability that the specified two tokens have the specified
     * tag given the complete list of input tokens.
     *
     * <p>This method returns results defined by
     *
     * <blockquote><pre>
     * logProbability(nTo,tagFrom,tagTo) 
     *     == logProbability(n-1,new int[] { tagFrom, tagTo })</pre></blockquote>
     *
     * @param nTo Position of second token.
     * @param tagFrom First Tag from which transition is made.
     * @param tagTo Second Tag to which transition is made.
     * @return Log probability of the tags at the specified position.
     */
    public abstract double logProbability(int nTo, int tagFrom, int tagTo);

    /**
     * Return the log conditional probability that the tokens starting
     * with the specified token position have the specified tags given
     * the complete sequence of input tokens.
     *
     * @param nFrom Starting position of sequence.
     * @param tags Tag identifiers for sequence.
     * @return Log probability that sequence starting at the specified
     * position has the specified tags.
     * @throws IllegalArgumentException If the token is out of range or
     * the token plus the length of the tag sequence is out of range of
     * tokens, or if any of the tags is not a known identifier.
     */
    public abstract double logProbability(int nFrom, int[] tags);

    /**
     * Return the log of the forward probability of the specified tag
     * at the specified position.  The forward probability is the sum
     * of the joint probabilities of all sequences from the initial token
     * to the specified token ending with the specified tag.
     *
     * @param token Token position.
     * @param tag Tag identifier.
     * @return Log forward probability specified token has specified
     * tag.
     * @throws ArrayIndexOutOfBoundsException If the token or tag index are
     * out of bounds for this lattice's tokens or tags.
     */
    public abstract double logForward(int token, int tag);


    /**
     * Returns the log of the backward probability to the specified
     * token and tag.  The backward probability is the sum of the
     * joint probabilities of all sequences starting from the
     * specified token and specified tag and going to the end of
     * the list of tokens.
     *
     * @param token Input token position.
     * @param tag Tag identifier.
     * @throws ArrayIndexOutOfBoundsException If the token or tag index are
     * out of bounds for this lattice's tokens or tags.
     */
    public abstract double logBackward(int token, int tag);


    /**
     * Returns the log of the transition probability from the specified
     * input token position with the specified previous tag to the
     * specified target tag.  
     *
     * @param tokenFrom Token position from which the transition is
     * made.
     * @param tagFrom Identifier for the previous tag from which the
     * transition is made.
     * @param tagTo Tag identifier for the target tag to which the
     * the transition is made.
     * @return Log probability of the transition.
     * @throws ArrayIndexOutOfBoundsException If the token index or
     * either of the tag indexes are out of bounds for this lattice's
     * tokens or tags.
     */
    public abstract double logTransition(int tokenFrom, int tagFrom, int tagTo);

    /**
     * Return the log of the normalizing constant for the lattice.
     * Its value is the log of the marginal probability of the input
     * tokens.  By the additive law of probability, this is equivalent
     * to the sum of the probabilities of all possible analyses for
     * the input sequence
     *
     * @return The normalizing constant.
     */
    public abstract double logZ();

    /**
     * Returns the classification of the token at the specified position
     * in this tag lattice.  Tag probabilities are conditional probabilities
     * of a tag given the entire sequence of input tokens.  The tags
     * are represented in their string form.
     *
     * @param tokenIndex Position of token to classify.
     * @return Classification of token in terms of tags.
     */
    public ConditionalClassification tokenClassification(int tokenIndex) {
        String[] tags = tagList().toArray(Strings.EMPTY_STRING_ARRAY);
        double[] logProbs = new double[tags.length];
        for (int tagId = 0; tagId < tags.length; ++tagId) {
            double logProbTagId = logProbability(tokenIndex,tagId);
            logProbs[tagId] 
                = (Double.isNaN(logProbTagId) || Double.isInfinite(logProbTagId))
                ? -500.0 // effectively zero
                : ( (logProbTagId > 0)
                    ? 0.0
                    : logProbTagId);
        }
        ConditionalClassification classification
            = ConditionalClassification.createLogProbs(tags,logProbs);
        return classification;
    }

}