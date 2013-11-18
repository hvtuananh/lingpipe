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

import com.aliasi.lm.CompiledNGramProcessLM;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Compilable;
import com.aliasi.util.Iterators;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.SmallSet;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The <code>CompiledSpellChecker</code> class implements a first-best
 * spell checker based on models of what users are likely to mean and
 * what errors they are likely to make in expressing their meaning.
 * This class is based on a character language model which represents
 * likely user intentions, and a weighted edit distance, which
 * represents how noise is introduced into the signal via typos,
 * brainos, or other sources such as case-normalization, diacritic
 * removal, bad character encodings, etc.

 * <P>The usual way of creating a compiled checker is through an
 * instance of {@link TrainSpellChecker}.  The result of compiling the
 * spell checker training class and reading it back in is a compiled
 * spell checker.  Only the basic models, weighted edit distance, and
 * token set are supplied through compilation; all
 * other parameters described below need to be set after an instance
 * is read in from its compiled form.  The token set may be null
 * at construction time and may be set later.
 *
 * <P>This class adopts the noisy-channel model approach to decoding
 * likely user intentions given received signals.  Spelling correction
 * simply returns the most likely intended message given the message
 * actually received.  In symbols:
 *
 * <blockquote><code>
 * didYouMean(received)
 * <br>= ArgMax<sub><sub>intended</sub></sub>
 *       P(intended | received)
 * <br>= ArgMax<sub><sub>intended</sub></sub>
 *       P(intended,received) / P(received)
 * <br>= ArgMax<sub><sub>intended</sub></sub>
 *       P(intended,received)
 * <br>= ArgMax<sub><sub>intended</sub></sub>
 *       P(intended) * P(received|intended)
 * </code></blockquote>
 *
 * The estimator <code>P(intended)</code>, called the <i>source
 * model</i>, estimates which signals are likely to be sent along the
 * channel.  For instance, the source might be a model of user's
 * intent in entering information on a web page.  The estimator
 * <code>P(received|intended)</code>, called the <i>channel model</i>,
 * estimates how intended messages are likely to be garbled.
 *
 * <P>For this class, the source language model must be a compiled
 * n-gram character language model.  Compiled models are required for
 * the efficiency of their suffix-tree encodings in evaluating
 * sequences of characters.  Optimizing held-out sample cross-entropy
 * is not necessarily the best approach to building these language
 * models, because they are being used here in a discriminitive
 * fashion, much as in language-model-based classification, tagging
 * or chunking.
 *
 * <P>For this class, the channel model must be a weighted edit
 * distance.  For traditional spelling correction, this is a model of
 * typos and brainos.  There are two static constant weighted edit
 * distances supplied in this class which are useful for other
 * decoding tasks.  The {@link #CASE_RESTORING} distance may be used
 * to restore case in single-case text.  The {@link #TOKENIZING} model
 * may be used to tokenize untokenized text, and is used in our <a
 * href="http://alias-i.com/lingpipe/demos/tutorial/chineseTokens/read-me.html">Chinese
 * tokenization demo</a>.
 *
 * <P>All input is normalized for whitespace, which consists of
 * removing initial and final whitespaces and reducing all other space
 * sequences to a single space character.  A single space character is
 * used as the initial context for the source language model.  A
 * single final uneditable space character is estimated at the end of
 * the language model, thus adapting the process language model to be
 * used as a bounded sequence language model just as in the language
 * model package itself.
 *
 * <P>This class optionally restricts corrections to sequences of
 * valid tokens.  The valid tokens are supplied as a set either during
 * construction time or later.  If the set of valid tokens is
 * <code>null</code>, then the output is not token sensitive, and
 * results may include tokens that are not in the training data.
 * Token-matching is case sensitive.
 *
 * <P>If a set of valid tokens is supplied, then a tokenizer factory
 * should also be supplied to carry out tokenization normalization on
 * input.  This tokenizer factory will be used to separate input
 * tokens with single spaces.  This tokenization may also be done
 * externally and normalized text passed into the
 * <code>didYouMean</code> method; this approach makes sense if the
 * tokenization is happening elsewhere already.
 *
 * <P>There are a number of tuning parameters for this class.  The
 * coarsest form of tuning simply sets whether or not particular edits
 * may be performed.  For instance, {@link #setAllowDelete(boolean)}
 * is used to turn deletion on or off.  Although edits with negative
 * infinity scores will never be used, it is more efficient to simply
 * disallow them if they are all infinite.  This is used in the
 * Chinese tokenizer, for instance, to only allow insertions and
 * matches.
 *
 * <P>There are three scoring parameters that determine how expensive
 * input characters are to edit.  The first of these is {@link
 * #setKnownTokenEditCost(double)}, which provides a penalty to be
 * added to the cost of editing characters that fall within known
 * tokens.  This value is only used for token-sensitive correctors.
 * Setting this to a low value makes it less likely to suggest an edit
 * on a known token.  The default value is -2.0, which on a log (base
 * 2) scale makes editing characters in known tokens 1/4 as likely
 * as editing characters in unknown tokens.
 *
 * <P>The next two scoring parameters provide penalties for editing
 * the first or second character in a token, whether it is known or
 * not.  In most cases, users make more mistakes later in words than
 * in the first few characters.  These values are controlled
 * independently through values provided at construction time or by
 * using the methods {@link #setFirstCharEditCost(double)} and {@link
 * #setSecondCharEditCost(double)}.  The default values for these are
 * -2.0 and -1.0 respectively.
 *
 * <P>The final tuning parameter is controlled with {@link
 * #setNumConsecutiveInsertionsAllowed(int)}, which determines how
 * many characters may be inserted in a row.  The default value is 1,
 * and setting this to 2 or higher may seriously slow down correction,
 * especially if it not token sensitive.
 *
 * <P>Search is further controlled by an n-best parameter, which
 * specifies the number of ongoing hypotheses considered after
 * inspecting each character.  This value is settable either in the
 * constructor or for models compiled from a trainer, by using the
 * method {@link #setNBest(int)}.  This lower this value, the faster
 * the resulting spelling correction.  But the danger is that with
 * low values, there may be search errors, where the correct
 * hypothesis is pruned because it did not look promising enough
 * early on.  In general, this value should be set as low as
 * possible without causing search errors.
 *
 * <P>This class requires external concurrent-read/synchronous-write
 * (CRSW) synchronization. All of the methods begining with
 * <code>set</code> must be executed exclusively in order to guarantee
 * consistent results; all other methods may be executed concurrently.
 * The {@link #didYouMean(String)} method for spelling correction may
 * be called concurrently with the same blocking and thread safety
 * constraints as the underlying language model and edit distance,
 * both of which are called repeatedly by this method.  If both the
 * language model and edit distance are thread safe and non-blocking,
 * as in all of LingPipe's implementations, then
 * <code>didYouMean</code> will also be concurrently executable and
 * non-blocking.
 *
 * <h4>Blocking Corrections</h4>
 *
 * <p>There are two ways to block tokens from being edited.  The first
 * is by setting a minimum length of edited tokens.  Standard language
 * models trained on texts tend to overestimate the likelihood of
 * queries that contain well-known short words or phrases like
 * <code>of</code> or <code>a</code>.  The method {@link
 * #setMinimumTokenLengthToCorrect(int)} sets a minimum token length
 * that is corrected.  The default value is <code>0</code>.
 *
 * <p>The second way to block corrections is to provide a set of
 * tokens that are never corrected.  One way to construct such a set
 * during training is by taking large-count tokens from the counter
 * returned by {@link TrainSpellChecker#tokenCounter()}.

 * <p>Note that these methods are heuristics that move the spelling
 * corrector in the same direction as two existing parameters.  First,
 * there is the pair of methods {@link #setFirstCharEditCost(double)}
 * and {@link #setSecondCharEditCost(double)} which make it less
 * likely to edit the first two characters (which are all of the
 * characters in a two-character token).  Second, there is a flexible
 * penalty for editing known tokens that may be set with {@link
 * #setKnownTokenEditCost(double)}.
 *
 * <p>Blocking corrections has a positive effect on speed, because
 * it eliminates any search over the tokens that are excluded from
 * correction.
 *
 * <h4>N-best Output</h4>
 *
 * It is possible to retrieve a list of possible spelling corrections,
 * ordered by plausibility.  The method {@link
 * #didYouMeanNBest(String)} returns an iterator over corrections in
 * decreasing order of likelihood.  Note that the same exact string
 * may be proposed more than once as a correction because of
 * alternative edits leading to the same result.  For instance,
 * &quot;a&quot; may be turned into &quot;b&quot; by substitution in one
 * step, or by deletion and insertion (or insertion then deletion)
 * in two steps.  These alternatives typically have different scores
 * and only the highest-scoring one is maintained at any given stage
 * of the algorithm by the first-best analyzer.
 *
 * <p>The n-best analyzer needs a much wider n-best list in order
 * to return sensible results, especially for very long inputs.  The
 * specified n-best size for the spell checker should, in fact, be
 * substantially larger than the desired number of n-best results.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public class CompiledSpellChecker implements SpellChecker {

    CompiledNGramProcessLM mLM;
    WeightedEditDistance mEditDistance;
    Set<String> mTokenSet;
    Set<String> mDoNotEditTokens = SmallSet.<String>create();
    int mNBestSize;

    boolean mAllowInsert = true;
    boolean mAllowDelete = true;
    boolean mAllowMatch = true;
    boolean mAllowSubstitute = true;
    boolean mAllowTranspose = true;
    int mMinimumTokenLengthToCorrect = 0;
    int mNumConsecutiveInsertionsAllowed = 1;

    double mKnownTokenEditCost;
    double mFirstCharEditCost;
    double mSecondCharEditCost;

    TokenizerFactory mTokenizerFactory;

    TokenTrieNode mTokenPrefixTrie;

    /**
     * Construct a compiled spell checker based on the specified
     * language model and similarity edit distance, set of valid
     * output tokens, maximum n-best size per character, and the
     * specified edit penalities for editing known tokens or the first
     * or second characters of tokens.   The set of do-not-edit tokens
     * is initiall empty; set it using {@link #setDoNotEditTokens(Set)}.
     *
     * <p>The weighted edit distance is required to be a similarity
     * measure for compatibility with the order of log likelihoods in
     * the source (language) model.  See {@link WeightedEditDistance}
     * for more information about similarity versus dissimilarity
     * distance measures.
     *
     * <P>If the set of tokens is <code>null</code>, the constructed
     * spelling checker will not be token-sensitive.  That is, it
     * will allow edits to strings which are not tokens in the token set.
     *
     * @param lm Source language model.
     * @param editDistance Channel edit distance model.
     * @param factory Tokenizer factory for tokenizing inputs.
     * @param tokenSet Set of valid tokens for outputs or
     * <code>null</code> if output is not token sensitive.
     * @param nBestSize Size of n-best list for spell checking.
     * hypothesis is pruned.
     * @param knownTokenEditCost Penalty for editing known tokens per edit.
     * @param firstCharEditCost Penalty for editing while scanning the
     * first character in a token.
     * @param secondCharEditCost Penalty for editing while scanning
     * the second character in a token.
     */
    public CompiledSpellChecker(CompiledNGramProcessLM lm,
                                WeightedEditDistance editDistance,
                                TokenizerFactory factory,
                                Set<String> tokenSet,
                                int nBestSize,
                                double knownTokenEditCost,
                                double firstCharEditCost,
                                double secondCharEditCost) {
        mLM = lm;
        mEditDistance = editDistance;
        mTokenizerFactory = factory;
        mNBestSize = nBestSize;
        setTokenSet(tokenSet);
        mKnownTokenEditCost = knownTokenEditCost;
        mFirstCharEditCost = firstCharEditCost;
        mSecondCharEditCost = secondCharEditCost;
    }

    /**
     * Construct a compiled spell checker based on the specified
     * language model and edit distance, tokenizer factory, the
     * set of valid output tokens, and maximum n-best size, with
     * default known token and first and second character edit costs.
     * The set of do-not-edit tokens
     * is initiall empty; set it using {@link #setDoNotEditTokens(Set)}.
     *
     * @param lm Source language model.
     * @param editDistance Channel edit distance model.
     * @param factory Tokenizer factory for tokenizing inputs.
     * @param tokenSet Set of valid tokens for outputs or
     * <code>null</code> if output is not token sensitive.
     * @param nBestSize Size of n-best list for spell checking.
     * hypothesis is pruned.
     * @throws IllegalArgumentException If the edit distance is not a
     * similarity measure.
     */
    public CompiledSpellChecker(CompiledNGramProcessLM lm,
                                WeightedEditDistance editDistance,
                                TokenizerFactory factory,
                                Set<String> tokenSet,
                                int nBestSize) {
        this(lm,editDistance,factory,tokenSet,nBestSize,
             DEFAULT_KNOWN_TOKEN_EDIT_COST,
             DEFAULT_FIRST_CHAR_EDIT_COST,
             DEFAULT_SECOND_CHAR_EDIT_COST);
    }

    /**
     * Construct a compiled spell checker based on the specified
     * language model and edit distance, a null tokenizer factory, the
     * set of valid output tokens, and maximum n-best size, with
     * default known token and first and second character edit costs.
     * The set of do-not-edit tokens
     * is initiall empty; set it using {@link #setDoNotEditTokens(Set)}.
     *
     * @param lm Source language model.
     * @param editDistance Channel edit distance model.
     * @param tokenSet Set of valid tokens for outputs or
     * <code>null</code> if output is not token sensitive.
     * @param nBestSize Size of n-best list for spell checking.
     * hypothesis is pruned.
     */
    public CompiledSpellChecker(CompiledNGramProcessLM lm,
                                WeightedEditDistance editDistance,
                                Set<String> tokenSet,
                                int nBestSize) {
        this(lm,editDistance,null,tokenSet,nBestSize);
    }

    /**
     * Construct a compiled spell checker based on the specified
     * language model and edit distance, with a null tokenizer
     * factory, the specified set of valid output tokens, with default
     * value for n-best size, known token edit cost and first and
     * second character edit costs.
     * The set of do-not-edit tokens
     * is initiall empty; set it using {@link #setDoNotEditTokens(Set)}.
     *
     * @param lm Source language model.
     * @param editDistance Channel edit distance model.
     * @param tokenSet Set of valid tokens for outputs or
     * <code>null</code> if output is not token sensitive.
     */
    public CompiledSpellChecker(CompiledNGramProcessLM lm,
                                WeightedEditDistance editDistance,
                                Set<String> tokenSet) {
        this(lm,editDistance,tokenSet,DEFAULT_N_BEST_SIZE);
    }


    /**
     * Returns the compiled language model for this spell checker.
     * Compiled language models are themselves immutable, and the
     * language model for a spell checker may not be changed, but
     * the result returned by this method may be used to construct
     * a new compiled spell checker.
     *
     * @return The language model for this spell checker.
     */
    public CompiledNGramProcessLM languageModel() {
        return mLM;
    }

    /**
     * Returns the weighted edit distance for this compiled spell
     * checker.
     *
     * @return The edit distance for this spell checker.
     */
    public WeightedEditDistance editDistance() {
        return mEditDistance;
    }

    /**
     * Returns the tokenizer factory for this spell checker.
     *
     * @return The tokenizer factory for this spell checker.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns an unmodifiable view the set of tokens for this spell
     * checker.  In order to change the token set, construct a new
     * set and use {@link #setTokenSet(Set)}.
     *
     * @return The set of tokens for this spell checker.
     */
    public Set<String> tokenSet() {
        return Collections.<String>unmodifiableSet(mTokenSet);
    }

    /**
     * Returns an unmodifiable view of the set of tokens that will
     * never be edited in this compiled spell checker.  To change the
     * value of this set, use {@link #setDoNotEditTokens(Set)}.
     *
     * @return The set of tokens that will not be edited.
     */
    public Set<String> doNotEditTokens() {
        return Collections.<String>unmodifiableSet(mDoNotEditTokens);
    }

    /**
     * Updates the set of do-not-edit tokens to be the specified
     * value.  If one of these tokens shows up in the input, it will
     * also show up in any correction supplied.
     *
     * @param tokens Set of tokens not to edit.
     */
    public void setDoNotEditTokens(Set<String> tokens) {
        mDoNotEditTokens = tokens;
    }


    /**
     * Returns the n-best size for this spell checker. See the class
     * documentation above and the documentation for the method {@link
     * #setNBest(int)} for more information.
     *
     * @return The n-best size for this spell checker.
     */
    public int nBestSize() {
        return mNBestSize;
    }

    /**
     * Returns the cost penalty for editing a character in a known
     * token.  This penalty is added to each edit within a known
     * token.
     *
     * @return Known token edit penalty.
     */
    public double knownTokenEditCost() {
        return mKnownTokenEditCost;
    }

    /**
     * Returns the cost penalty for editing the first character in a
     * token.  This penalty is added to each edit while scanning the
     * first character of a token in the input.
     *
     * <P>As a special case, transposition only pays a single
     * penalty based on the penalty of the first character in
     * the transposition.
     *
     * @return First character edit penalty.
     */
    public double firstCharEditCost() {
        return mFirstCharEditCost;
    }

    /**
     * Returns the cost penalty for editing the second character
     * in a token.  This penalty is added for each edit while
     * scanning the second character in an input.
     *
     * @return Second character edit penalty.
     */
    public double secondCharEditCost() {
        return mSecondCharEditCost;
    }

    /**
     * Set the known token edit cost to the specified value.
     *
     * @param cost New value for known token edit cost.
     */
    public void setKnownTokenEditCost(double cost) {
        mKnownTokenEditCost = cost;
    }

    /**
     * Set the first character edit cost to the specified value.
     *
     * @param cost New value for the first character edit cost.
     */
    public void setFirstCharEditCost(double cost) {
        mFirstCharEditCost = cost;
    }

    /**
     * Set the second character edit cost to the specified value.
     *
     * @param cost New value for the second character edit cost.
     */
    public void setSecondCharEditCost(double cost) {
        mSecondCharEditCost = cost;
    }

    /**
     * Returns the number of consecutive insertions allowed.
     * This will be zero if insertions are not allowed.
     */
    public int numConsecutiveInsertionsAllowed() {
        return mNumConsecutiveInsertionsAllowed;
    }

    /**
     * Returns <code>true</code> if this spell checker allows
     * insertions.
     *
     * @return <code>true</code> if this spell checker allows
     * insertions.
     */
    public boolean allowInsert() {
        return mAllowInsert;
    }

    /**
     * Returns <code>true</code> if this spell checker allows
     * deletions.
     *
     * @return <code>true</code> if this spell checker allows
     * deletions.
     */
    public boolean allowDelete() {
        return mAllowDelete;
    }

    /**
     * Returns <code>true</code> if this spell checker allows
     * matches.
     *
     * @return <code>true</code> if this spell checker allows
     * matches.
     */
    public boolean allowMatch() {
        return mAllowMatch;
    }

    /**
     * Returns <code>true</code> if this spell checker allows
     * substitutions.
     *
     * @return <code>true</code> if this spell checker allows
     * substitutions.
     */
    public boolean allowSubstitute() {
        return mAllowSubstitute;
    }

    /**
     * Returns <code>true</code> if this spell checker allows
     * transpositions.
     *
     * @return <code>true</code> if this spell checker allows
     * transpositions.
     */
    public boolean allowTranspose() {
        return mAllowTranspose;
    }


    /**
     * Sets the edit distance for this spell checker to the
     * specified value.
     *
     * @param editDistance Edit distance to use for spell checking.
     */
    public void setEditDistance(WeightedEditDistance editDistance) {
        mEditDistance = editDistance;
    }


    /**
     * Sets a minimum character length for tokens to be eligible for
     * editing.
     *
     * @param tokenCharLength Edit distance to use for spell checking.
     * @throws IllegalArgumentException If the character length
     * specified is less than <code>0</code>.
     */
    public void setMinimumTokenLengthToCorrect(int tokenCharLength) {
        if (tokenCharLength < 0) {
            String msg = "Minimum token length to correct must be >= 0."
                + " Found tokenCharLength=" + tokenCharLength;
            throw new IllegalArgumentException(msg);
        }
        mMinimumTokenLengthToCorrect = tokenCharLength;
    }

    /**
     * Returns the minimum length of token that will be corrected.
     * This value is initially <code>0</code>, but may be set
     * using {@link #setMinimumTokenLengthToCorrect(int)}.
     *
     * @return The minimum token length to correct.
     */
    public int minimumTokenLengthToCorrect() {
        return mMinimumTokenLengthToCorrect;
    }



    /**
     * Sets the language model for this spell checker to the
     * specified value.
     *
     * @param lm New language model for this spell checker.
     */
    public void setLanguageModel(CompiledNGramProcessLM lm) {
        mLM = lm;
    }

    /**
     * Sets the tokenizer factory for input processing to the
     * specified value.  If the value is <code>null</code>, no
     * tokenization is performed on the input.
     *
     * @param factory Tokenizer factory for this spell checker.
     */
    public void setTokenizerFactory(TokenizerFactory factory) {
        mTokenizerFactory = factory;
    }

    /**
     * Sets the set of tokens that can be produced by editing.
     * If the specified set is <code>null</code>, editing will
     * not be token sensitive.
     *
     * <p>If the token set is null, nothing will happen.
     *
     * <P><i>Warning:</i> Spelling correction without tokenization may
     * be slow, especially with a large n-best size.
     *
     * @param tokenSet The new set of tokens or <code>null</code> if
     * not tokenizing.
     */
    public final void setTokenSet(Set<String> tokenSet) {
        if (tokenSet == null) return;
        int maxLen = 0;
        for (String token : tokenSet)
            maxLen = java.lang.Math.max(maxLen,token.length());
        mTokenSet = tokenSet;
        mTokenPrefixTrie = tokenSet == null ? null : prefixTrie(tokenSet);
    }

    /**
     * Sets The n-best size to the specified value.  The n-best
     * size controls the number of hypotheses maintained going forward
     * for each character in the input.  A higher value indicates
     * a broader and slower search for corrections.
     *
     * @param size Size of the n-best lists at each character.
     * @throws IllegalArgumentException If the size is less than one.
     */
    public void setNBest(int size) {
        if (size < 1) {
            String msg = "N-best size must be greather than 0."
                + " Found size=" + size;
            throw new IllegalArgumentException(msg);
        }
        mNBestSize = size;
    }


    /**
     * Sets this spell checker to allow insertions if the specified
     * value is <code>true</code> and to disallow them if it is
     * <code>false</code>.  If the value is <code>false</code>, then
     * the number of consecutive insertions allowed is also set
     * to zero.
     *
     * @param allowInsert New insertion mode.
     */
    public void setAllowInsert(boolean allowInsert) {
        mAllowInsert = allowInsert;
        if (!allowInsert) setNumConsecutiveInsertionsAllowed(0);
    }

    /**
     * Sets this spell checker to allow deletions if the specified
     * value is <code>true</code> and to disallow them if it is
     * <code>false</code>.
     *
     * @param allowDelete New deletion mode.
     */
    public void setAllowDelete(boolean allowDelete) {
        mAllowDelete = allowDelete;
    }

    /**
     * Sets this spell checker to allow matches if the specified
     * value is <code>true</code> and to disallow them if it is
     * <code>false</code>.
     *
     * @param allowMatch New match mode.
     */
    public void setAllowMatch(boolean allowMatch) {
        mAllowMatch = allowMatch;
    }

    /**
     * Sets this spell checker to allow substitutions if the specified
     * value is <code>true</code> and to disallow them if it is
     * <code>false</code>.
     *
     * @param allowSubstitute New substitution mode.
     */
    public void setAllowSubstitute(boolean allowSubstitute) {
        mAllowSubstitute = allowSubstitute;
    }

    /**
     * Sets this spell checker to allow transpositions if the specified
     * value is <code>true</code> and to disallow them if it is
     * <code>false</code>.
     *
     * @param allowTranspose New transposition mode.
     */
    public void setAllowTranspose(boolean allowTranspose) {
        mAllowTranspose = allowTranspose;
    }

    /**
     * Set the number of consecutive insertions allowed to the
     * specified value.  The value must not be negative.  If the
     * number of insertions allowed is specified to be greater than
     * zero, then the allow insertion model will be set to
     * <code>true</code>.
     *
     * @param numAllowed Number of insertions allowed in a row.
     * @throws IllegalArgumentException If the number specified is
     * less than zero.
     */
    public void setNumConsecutiveInsertionsAllowed(int numAllowed) {
        if (numAllowed < 0) {
            String msg = "Num insertions allowed must be >= 0."
                + " Found numAllowed=" + numAllowed;
            throw new IllegalArgumentException(msg);
        }
        if (numAllowed > 0) setAllowInsert(true);
        mNumConsecutiveInsertionsAllowed = numAllowed;
    }

    /**
     * Returns a first-best hypothesis of the intended message given a
     * received message.  This method returns <code>null</code> if the
     * received message is itself the best hypothesis.  The exact
     * definition of hypothesis ranking is provided in the class
     * documentation above.
     *
     * @param receivedMsg The message received over the noisy channel.
     * @return The first-best hypothesis of the intended source
     * message.
     */
    public String didYouMean(String receivedMsg) {
        String msg = normalizeQuery(receivedMsg);
        if (msg.length() == 0) return msg;
        DpSpellQueue queue = new DpSpellQueue();
        DpSpellQueue finalQueue = new DpSpellQueue();
        computeBestPaths(msg,queue,finalQueue);
        if (finalQueue.isEmpty())
            return msg;
        State bestState = finalQueue.poll();
        //System.out.println("Winner is: "+bestState);
        return bestState.output().trim();
    }

    void computeBestPaths(String msg,
                          StateQueue queue, StateQueue finalQueue) {
        double[] editPenalties = editPenalties(msg);
        State initialState = new State(0.0,false,mTokenPrefixTrie,
                                       null,
                                       mLM.nextContext(0,' '));
        addToQueue(queue,initialState,editPenalties[0]);
        DpSpellQueue nextQ = new DpSpellQueue();
        DpSpellQueue nextQ2 = new DpSpellQueue();
        for (int i = 0; i < msg.length(); ++i) {
            char c = msg.charAt(i);
            char nextC = ((i+1) < msg.length()) ? msg.charAt(i+1) : 0;
            for (State state : queue) {
                if ((i+1) < msg.length())
                    extend2(c,nextC,state,nextQ,nextQ2,editPenalties[i]);
                else
                    extend1(c,state,nextQ,editPenalties[i]);
            }
            queue = nextQ;
            nextQ = nextQ2;
            nextQ2 = new DpSpellQueue();
        }
        extendToFinalSpace(queue,finalQueue);
    }

    /**
     * Returns an iterator over the n-best spelling corrections for
     * the specified input string.  The iterator produces instances
     * of {@link ScoredObject}, the object of which is the corrected
     * string and the score of which is the joint score of edit (channel)
     * costs and language model (source) cost of the output.
     *
     * <p>Unlike for HMMs and chunking, this n-best list is not exact
     * due to pruning during spelling correction.  The maximum number
     * of returned results is determined by the n-best paramemter, as
     * set through {@link #setNBest(int)}.  The larger the n-best list,
     * the higher-quality the results, even earlier on the list.
     *
     * <p>N-best spelling correction is not an exact computation
     * due to heuristic pruning during decoding.  Thus setting the
     * n-best list to a larger result may result in better n-best
     * results, even for earlier results on the list.  For instance,
     * the result of the first five corrections is not necessarily the
     * same with a 5-element, 10-element or 1000-element n-best size
     * (as specified by {@link #setNBest(int)}.
     *
     * <p>A rough confidence measure may be determined by comparing
     * the scores, which are log (base 2) edit (channel) plus log
     * (base 2) language model (source) scores.  A very crude measure
     * is to compare the score of the first result to the score of
     * the second result; if there is a large gap, confidence is high.
     * A tighter measure is to convert the log probabilities back to
     * linear, add them all up, and then divide.  For instance, if
     * there were results:
     *
     * <blockquote><table border="1" cellpadding="5">
     * <tr><th algin="left">Rank</th>
     *     <th algin="left">String</th>
     *     <th algin="left">Log (2) Prob</th>
     *     <th algin="left">Prob</th>
     *     <th align="left">Conf</th></tr>
     * <tr><td>0</td><td>foo</td><td>-2</td> <td>0.250</td><td>0.571</tr>
     * <tr><td>0</td><td>for</td><td>-3</td> <td>0.125</td>0.285</tr>
     * <tr><td>0</td><td>food</td><td>-4</td><td>0.062</td>0.143</tr>
     * <tr><td>0</td><td>of</td><td>-10</td> <td>0.001</td>0.002</tr>
     * </table></blockquote>
     *
     * Here there are four results, with log probabilities -2, -3,
     * -4 and -10, which have the corresponding linear probabilities.
     * The sum of these probabilities is 0.438.  Hence the confidence
     * in the top-ranked answer is 0.250/0.438=0.571.
     *
     * <p><b>Warning:</b> Spell checking with n-best output is
     * currently implemented with a very naive algorithm and is
     * thus very slow compared to first-best spelling correction.
     * The reason for this is that there the dynamic programming
     * is turned off for n-best spelling correction, hence a lot
     * redundant computation is done.
     *
     * @param receivedMsg Input message.
     * @return Iterator over n-best spelling suggestions.
     */
    public Iterator<ScoredObject<String>> didYouMeanNBest(String receivedMsg) {
        String msg = normalizeQuery(receivedMsg);
        if (msg.length() == 0)
            return Iterators.<ScoredObject<String>>singleton(new ScoredObject<String>("",0));
        StateQueue queue = new NBestSpellQueue();
        StateQueue finalQueue = new NBestSpellQueue();
        computeBestPaths(msg,queue,finalQueue);
        BoundedPriorityQueue<ScoredObject<String>> resultQueue
            = new BoundedPriorityQueue<ScoredObject<String>>(ScoredObject.comparator(),
                                                             mNBestSize);
        for (State state : finalQueue) {
            resultQueue.offer(new ScoredObject<String>(state.output().trim(),
                                                       state.score()));
        }
        return resultQueue.iterator();
    }

    private boolean isShortToken(String token) {
        return token.length() <= mMinimumTokenLengthToCorrect;
    }

    private double[] editPenalties(String msg) {
        double[] penalties = new double[msg.length()];
        Arrays.fill(penalties,0.0);

        if (mTokenSet == null) return penalties;

        int charPosition = 0;
        for (int i = 0; i < penalties.length; ++i) {
            char c = msg.charAt(i);
            if ((mTokenSet != null)
                && ((i == 0) || (msg.charAt(i-1) == ' '))) {
                int endIndex = msg.indexOf(' ', i);
                if (endIndex == -1)
                    endIndex = msg.length();
                String token = msg.substring(i,endIndex);
                if (mDoNotEditTokens.contains(token)
                    || isShortToken(token)) {
                    // penalize space before
                    if (i > 0) {
                        penalties[i-1] = Double.NEGATIVE_INFINITY;
                    }
                    // penalize chars within
                    for (int j = i; j < endIndex; ++j) {
                        penalties[j] = Double.NEGATIVE_INFINITY;
                    }
                    // penalize space after (may get double penalized)
                    if (endIndex < penalties.length) {
                        penalties[endIndex] = Double.NEGATIVE_INFINITY;
                    }
                } else if (mTokenSet.contains(token)) {
                    if (i > 0) {
                        penalties[i-1] += mKnownTokenEditCost;
                    }
                    // penalize chars within
                    for (int j = i; j < endIndex; ++j) {
                        penalties[j] += mKnownTokenEditCost;
                    }
                    // penalize space after (may get double penalized)
                    if (endIndex < penalties.length) {
                        penalties[endIndex] += mKnownTokenEditCost;
                    }
                }
            }
            if (c == ' ') {
                charPosition = 0;
                // this'll also affect first by making it non-first
                penalties[i] += mFirstCharEditCost;
            } else if (charPosition == 0) {
                penalties[i] += mFirstCharEditCost;
                ++charPosition;
            } else if (charPosition == 1) {
                penalties[i] += mSecondCharEditCost;
                ++charPosition;
            }
        }
        // for (int i = 0; i < penalties.length; ++i)
        //   System.out.println(" " + msg.charAt(i) + "=" + penalties[i]);
        //
        return penalties;
    }

    /**
     * Returns a string-based representation of the parameters of
     * this compiled spell checker.
     *
     * @return A string representing the parameters of this spell
     * checker.
     */
    public String parametersToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("SEARCH");
        sb.append("\n  N-best size=" + mNBestSize);

        sb.append("\n\nTOKEN SENSITIVITY");
        sb.append("\n  Token sensitive=" + (mTokenSet != null));
        if (mTokenSet != null) {
            sb.append("\n  # Known Tokens=" + mTokenSet.size());
        }

        sb.append("\n\nEDITS ALLOWED");
        sb.append("\n  Allow insert=" + mAllowInsert);
        sb.append("\n  Allow delete=" + mAllowDelete);
        sb.append("\n  Allow match=" + mAllowMatch);
        sb.append("\n  Allow substitute=" + mAllowSubstitute);
        sb.append("\n  Allow transpose=" + mAllowTranspose);
        sb.append("\n  Num consecutive insertions allowed="
                  + mNumConsecutiveInsertionsAllowed);
        sb.append("\n  Minimum Length Token Edit="
                  + mMinimumTokenLengthToCorrect);
        sb.append("\n  # of do-not-Edit Tokens="
                  + mDoNotEditTokens.size());

        sb.append("\n\nEDIT COSTS");
        sb.append("\n  Edit Distance=" + mEditDistance);
        sb.append("\n  Known Token Edit Cost=" + mKnownTokenEditCost);
        sb.append("\n  First Char Edit Cost=" + mFirstCharEditCost);
        sb.append("\n  Second Char Edit Cost=" + mSecondCharEditCost);

        sb.append("\n\nEDIT DISTANCE\n");
        sb.append(mEditDistance);

        sb.append("\n\nTOKENIZER FACTORY\n");
        sb.append(mTokenizerFactory);

        return sb.toString();
    }

    String normalizeQuery(String query) {
        StringBuilder sb = new StringBuilder();
        if (mTokenizerFactory == null) {
            Strings.normalizeWhitespace(query,sb);
        } else {
            char[] cs = query.toCharArray();
            Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
            String nextToken;
            for (int i = 0; (nextToken = tokenizer.nextToken()) != null; ++i) {
                if (i > 0) sb.append(' ');
                sb.append(nextToken);
            }
        }
        return sb.toString();
    }

    // set this locally if tokenset == null
    char[] observedCharacters() {
        return mLM.observedCharacters();
    }

    void extendToFinalSpace(StateQueue queue,
                            StateQueue finalQueue) {
        for (State state : queue) {
            if (state.mTokenEdited && !state.tokenComplete()) {
                continue; // delete if final edited to non-token

            }
            double nextScore = state.mScore
                + mLM.log2Estimate(state.mContextIndex,' ');
            if (nextScore == Double.NEGATIVE_INFINITY) {
                continue;
            }
            State nextState = new State(nextScore,false,
                                        null,
                                        state,
                                        -1);
            finalQueue.addState(nextState);
        }
    }

    void extend2(char c1, char c2, State state,
                 DpSpellQueue nextQ, DpSpellQueue nextQ2,
                 double positionalEditPenalty) {
        extend1(c1,state,nextQ,positionalEditPenalty);
        if (positionalEditPenalty == Double.NEGATIVE_INFINITY) return;
        if (allowTranspose())
            transpose(c1,c2,state,nextQ2,positionalEditPenalty);
    }

    void extend1(char c, State state, DpSpellQueue nextQ,
                 double positionalEditPenalty) {
        if (allowMatch())
            match(c,state,nextQ,positionalEditPenalty);
        if (positionalEditPenalty == Double.NEGATIVE_INFINITY) return;
        if (allowSubstitute())
            substitute(c,state,nextQ,positionalEditPenalty);
        if (allowDelete())
            delete(c,state,nextQ,positionalEditPenalty);
    }


    void addToQueue(StateQueue queue, State state,
                    double positionalEditPenalty) {
        addToQueue(queue,state,0,positionalEditPenalty);
    }

    void addToQueue(StateQueue queue, State state, int numInserts,
                    double positionalEditPenalty) {
        if (!queue.addState(state)) return;
        if (numInserts >= mNumConsecutiveInsertionsAllowed) return;
        if (positionalEditPenalty == Double.NEGATIVE_INFINITY) return;
        insert(state,queue,numInserts,positionalEditPenalty);
    }

    TokenTrieNode daughter(TokenTrieNode node,
                           char c) {
        return node == null ? null : node.daughter(c);
    }

    void match(char c, State state, DpSpellQueue nextQ,
               double positionalEditPenalty) {
        if (state.mTokenEdited) {
            if (c == ' ') {
                if (!state.tokenComplete()) {
                    return;
                }
            } else if (!state.continuedBy(c)) {
                return;
            }
        }
        double score = state.mScore
            + mLM.log2Estimate(state.mContextIndex,c)
            + mEditDistance.matchWeight(c);
        if (score == Double.NEGATIVE_INFINITY) return;
        TokenTrieNode tokenTrieNode =
            (c == ' ') ? mTokenPrefixTrie : daughter(state.mTokenTrieNode,c);
        addToQueue(nextQ,
                   new State1(score,
                              (c != ' ') && state.mTokenEdited,
                              tokenTrieNode,
                              state,c,
                              mLM.nextContext(state.mContextIndex,c)),
                   positionalEditPenalty);
    }

    void delete(char c, State state, DpSpellQueue nextQ,
                double positionalEditPenalty) {
        double deleteWeight = mEditDistance.deleteWeight(c);
        if (deleteWeight == Double.NEGATIVE_INFINITY) return;
        double score = state.mScore + deleteWeight + positionalEditPenalty;
        addToQueue(nextQ,
                   new State(score, true,
                             state.mTokenTrieNode,
                             state,
                             state.mContextIndex),
                   positionalEditPenalty);
    }

    void insert(State state, StateQueue nextQ, int numInserts,
                double positionalEditPenalty) {
        if (state.tokenComplete()) {
            double score = state.mScore
                + mLM.log2Estimate(state.mContextIndex,' ')
                + mEditDistance.insertWeight(' ')
                + positionalEditPenalty;
            if (score != Double.NEGATIVE_INFINITY)
                addToQueue(nextQ,
                           new State1(score,true,
                                      mTokenPrefixTrie,
                                      state,' ',
                                      mLM.nextContext(state.mContextIndex,
                                                      ' ')),
                           numInserts+1,positionalEditPenalty);
        }
        char[] followers = state.getContinuations();
        if (followers == null) return;
        for (int i = 0; i < followers.length; ++i) {
            char c = followers[i];
            double insertWeight = mEditDistance.insertWeight(c);
            if (insertWeight == Double.NEGATIVE_INFINITY) continue;
            double score = state.mScore
                + mLM.log2Estimate(state.mContextIndex,c)
                + insertWeight
                + positionalEditPenalty;
            if (score == Double.NEGATIVE_INFINITY) continue;
            addToQueue(nextQ,
                       new State1(score,true,
                                  state.followingNode(i),
                                  state,c,
                                  mLM.nextContext(state.mContextIndex,c)),
                       numInserts+1, positionalEditPenalty);
        }
    }

    void substitute(char c, State state, StateQueue nextQ,
                    double positionalEditPenalty) {
        if (state.tokenComplete() && c != ' ') {
            double score = state.mScore
                + mLM.log2Estimate(state.mContextIndex,' ')
                + mEditDistance.substituteWeight(c,' ')
                + positionalEditPenalty;
            if (score != Double.NEGATIVE_INFINITY)
                addToQueue(nextQ,
                           new State1(score,true,
                                      mTokenPrefixTrie,
                                      state,' ',
                                      mLM.nextContext(state.mContextIndex,
                                                      ' ')),
                           positionalEditPenalty);
        }
        char[] followers = state.getContinuations();
        if (followers == null) return;
        for (int i = 0; i < followers.length; ++i) {
            char c2 = followers[i];
            if (c == c2) continue; // don't match
            double substWeight
                = mEditDistance.substituteWeight(c,c2);
            if (substWeight == Double.NEGATIVE_INFINITY) continue;
            double score = state.mScore
                + mLM.log2Estimate(state.mContextIndex,c2)
                + substWeight
                + positionalEditPenalty;
            if (score == Double.NEGATIVE_INFINITY) continue;
            addToQueue(nextQ,
                       new State1(score,true,
                                  state.followingNode(i),
                                  state,c2,
                                  mLM.nextContext(state.mContextIndex,
                                                  c2)),
                       positionalEditPenalty);
        }
    }

    void transpose(char c1, char c2, State state, StateQueue nextQ,
                   double positionalEditPenalty) {
        double transposeWeight = mEditDistance.transposeWeight(c1,c2);
        if (transposeWeight == Double.NEGATIVE_INFINITY) return;
        if (c2 == ' ' && !state.tokenComplete()) return;
        TokenTrieNode midNode
            = (c2 == ' ')
            ? mTokenPrefixTrie
            : daughter(state.mTokenTrieNode,c2);
        if (c1 == ' ' && midNode != null && !midNode.mIsToken) return;
        int nextContextIndex = mLM.nextContext(state.mContextIndex,c2);
        int nextContextIndex2 = mLM.nextContext(nextContextIndex,c1);
        double score = state.mScore
            + mLM.log2Estimate(state.mContextIndex,c2)
            + mLM.log2Estimate(nextContextIndex,c1)
            + mEditDistance.transposeWeight(c1,c2)
            + positionalEditPenalty;
        if (score == Double.NEGATIVE_INFINITY) return;
        TokenTrieNode nextNode
            = (c1 == ' ')
            ? mTokenPrefixTrie
            : daughter(midNode,c1);
        addToQueue(nextQ,
                   new State2(score,true,nextNode,
                              state,c2,c1,
                              nextContextIndex2),
                   positionalEditPenalty);
    }


    /**
     * A weighted edit distance ordered by similarity that treats case
     * variants as zero cost and all other edits as infinite cost.
     * The inifite cost is {@link Double#NEGATIVE_INFINITY}.  See
     * {@link WeightedEditDistance} for more information on
     * similarity-based distances.
     *
     * <P>If this model is used for spelling correction, the result is
     * a system that simply chooses the most likely case for output
     * characters given an input character and does not change anything
     * else.
     *
     * <P>Case here is based on the methods
     * {@link Character#isUpperCase(char)}, {@link Character#isLowerCase(char)}
     * and equality is tested by converting the upper case character to
     * lower case using {@link Character#toLowerCase(char)}.
     *
     * <P>This edit distance is compilable and the result of writing it
     * and reading it is referentially equal to this instance.
     */
    public static WeightedEditDistance CASE_RESTORING = new CaseRestoring();

    /**
     * A weighted edit distance ordered by similarity that allows free
     * space insertion.  The cost of inserting a space is zero, the
     * cost of matching is zero, and all other costs are infinite.
     * See {@link WeightedEditDistance} for more information on
     * similarity-based distances.
     *
     * <P>If this model is used for spelling correction, the result is
     * as system that will retokenize input with no spaces.  For
     * instance, if the source model is trained with chinese tokens
     * separated by spaces and the input is a sequence of chinese
     * characters not separated by spaces, the output is a space-separated
     * tokenization.  If the source model is valid pronunciations
     * separated by spaces and the input is pronunciations not separated
     * by spaces, the result is a tokenization.
     *
     * <P>This edit distance is compilable and the result of writing it
     * and reading it is referentially equal to this instance.
     */
    public static WeightedEditDistance TOKENIZING = new Tokenizing();

    static final int DEFAULT_N_BEST_SIZE = 64;
    static final double DEFAULT_KNOWN_TOKEN_EDIT_COST = -2.0;
    static final double DEFAULT_FIRST_CHAR_EDIT_COST = -1.5;
    static final double DEFAULT_SECOND_CHAR_EDIT_COST = -1.0;


    private static Map<String,char[]> prefixToContinuations(Set<String> tokens) {
        Map<String,char[]> prefixToContinuations
            = new HashMap<String,char[]>();
        for (String token : tokens) {
            for (int i = 0; i < token.length(); ++i) {
                String prefix = token.substring(0,i);
                char nextChar = token.charAt(i);
                char[] currentCs = prefixToContinuations.get(prefix);
                if (currentCs == null) {
                    prefixToContinuations.put(prefix,new char[] { nextChar });
                } else {
                    char[] nextCs = com.aliasi.util.Arrays.add(nextChar,currentCs);
                    if (nextCs.length > currentCs.length)
                        prefixToContinuations.put(prefix,nextCs);
                }
            }
        }
        return prefixToContinuations;
    }

    private TokenTrieNode prefixTrie(Set<String> tokens) {
        Map<String,char[]> prefixMap = prefixToContinuations(tokens);
        return completeTrieNode("",tokens,prefixMap);
    }

    private static TokenTrieNode completeTrieNode(String prefix,
                                                  Set<String> tokens,
                                                  Map<String,char[]> prefixMap) {
        // System.out.println("printing prefix map");
        // for (Map.Entry<String,char[]> entry : prefixMap.entrySet())
        // System.out.println("|" + entry.getKey() + "|==>|" + new String(entry.getValue()) + "|");
        char[] contChars = prefixMap.get(prefix);
        if (contChars == null)
            contChars = Strings.EMPTY_CHAR_ARRAY;
        else
            Arrays.sort(contChars);
        // System.out.println("prefix=|" + prefix + "| cont chars=|" + new String(contChars) + '|');
        TokenTrieNode[] contNodes = new TokenTrieNode[contChars.length];
        for (int i = 0; i < contNodes.length; ++i)
            contNodes[i]
                = completeTrieNode(prefix+contChars[i],tokens,prefixMap);
        return new TokenTrieNode(tokens.contains(prefix),
                                 contChars, contNodes);
    }

    private static final class TokenTrieNode {
        final boolean mIsToken;
        final char[] mFollowingChars;
        final TokenTrieNode[] mFollowingNodes;
        TokenTrieNode(boolean isToken, char[] followingChars,
                      TokenTrieNode[] followingNodes) {
            mIsToken = isToken;
            mFollowingChars = followingChars;
            mFollowingNodes = followingNodes;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString("",sb,0);
            return sb.toString();
        }
        void toString(String prefix, StringBuilder sb, int indent) {
            if (mIsToken) sb.append(" [token=" + prefix + "]");
            sb.append('\n');
            for (int i = 0; i < mFollowingChars.length; ++i) {
                for (int k = 0; k < indent; ++k) sb.append("  ");
                sb.append(mFollowingChars[i]);
                mFollowingNodes[i].toString(prefix+mFollowingChars[i],
                                            sb,indent+1);
            }
        }
        TokenTrieNode daughter(char c) {
            int index = Arrays.binarySearch(mFollowingChars,c);
            return index < 0
                ? null
                : mFollowingNodes[index];
        }
    }

    private final class State1 extends State {
        final char mChar1;
        State1(double score, boolean tokenEdited,
               TokenTrieNode tokenTrieNode, State previousState,
               char char1,
               int contextIndex) {

            super(score,tokenEdited,tokenTrieNode,previousState,
                  contextIndex);
            mChar1 = char1;
        }
        @Override
        void outputLocal(StringBuilder sb) {
            sb.append(mChar1);
        }
    }

    private final class State2 extends State {
        final char mChar1;
        final char mChar2;
        State2(double score, boolean tokenEdited,
               TokenTrieNode tokenTrieNode, State previousState,
               char char1, char char2,
               int contextIndex) {

            super(score,tokenEdited,tokenTrieNode,previousState,
                  contextIndex);
            mChar1 = char1;
            mChar2 = char2;
        }
        @Override
        void outputLocal(StringBuilder sb) {
            sb.append(mChar2);
            sb.append(mChar1);
        }
    }


    private class State implements Scored {
        final TokenTrieNode mTokenTrieNode; // null if not tokenizing
        final double mScore;
        final boolean mTokenEdited;
        final State mPreviousState;
        final int mContextIndex;
        State(double score, boolean tokenEdited,
              TokenTrieNode tokenTrieNode, State previousState,
              int contextIndex) {
            mScore = score;
            mTokenEdited = tokenEdited;
            mTokenTrieNode = tokenTrieNode;
            mPreviousState = previousState;
            mContextIndex = contextIndex;
        }
        public double score() {
            return mScore;
        }
        TokenTrieNode followingNode(int i) {
            return mTokenTrieNode == null
                ? null
                : mTokenTrieNode.mFollowingNodes[i];
        }
        @Override
        public String toString() {
            return output() + "/" + mTokenEdited + "/" + mScore;
        }
        boolean tokenComplete() {
            boolean result = (mTokenSet == null)
                || ((mTokenTrieNode != null) && mTokenTrieNode.mIsToken);
            return result;
        }
        boolean continuedBy(char c) {
            if (mTokenTrieNode == null) return true;
            char[] continuations = getContinuations();
            return (continuations != null)
                && Arrays.binarySearch(continuations,c) >= 0;
        }
        char[] getContinuations() {
            return mTokenTrieNode == null
                ? observedCharacters()
                : mTokenTrieNode.mFollowingChars;
        }
        void outputLocal(StringBuilder ignoreMeSb) {
            /* do nothing - must impl because override */
        }
        String output() {
            StringBuilder sb = new StringBuilder();
            for (State s = this; s != null; s = s.mPreviousState)
                s.outputLocal(sb);
            // reverse
            int len = sb.length();
            char[] cs = new char[len];
            for (int i = 0; i < len; ++i)
                cs[i] = sb.charAt(len-i-1);
            return new String(cs);
        }
    }


    // easy to add a beam here and return false right away
    private final class DpSpellQueue extends StateQueue {
        private final Map<Integer,State> mStateToBest
            = new HashMap<Integer,State>();
        @Override
        public boolean addState(State state) {
            Integer dp = Integer.valueOf(state.mContextIndex);
            State bestState = mStateToBest.get(dp);
            if (bestState == null) {
                mStateToBest.put(dp,state);
                return offer(state);
            }
            if (bestState.mScore >= state.mScore)
                return false;
            remove(bestState);
            mStateToBest.put(dp,state);
            return offer(state);
        }
    }

    private final class NBestSpellQueue extends StateQueue {

        @Override
        public boolean addState(State state) {
            return offer(state);
        }
    }

    private abstract class StateQueue extends BoundedPriorityQueue<State> {
        StateQueue() {
            super(ScoredObject.comparator(),mNBestSize);
        }
        abstract boolean addState(State state);
    }


    private static final class CaseRestoring
        extends FixedWeightEditDistance
        implements Compilable {

        static final long serialVersionUID = -4504141535738468405L;

        public CaseRestoring() {
            super(0.0,
                  Double.NEGATIVE_INFINITY,
                  Double.NEGATIVE_INFINITY,
                  Double.NEGATIVE_INFINITY,
                  Double.NEGATIVE_INFINITY);
        }
        @Override
        public double substituteWeight(char cDeleted, char cInserted) {
            return (Character.toLowerCase(cDeleted)
                    == Character.toLowerCase(cInserted))
                ? 0.0
                : Double.NEGATIVE_INFINITY;
        }
        @Override
        public void compileTo(ObjectOutput objOut) throws IOException {
            objOut.writeObject(new Externalizable());
        }
        private static class Externalizable extends AbstractExternalizable {
            private static final long serialVersionUID = 2825384056772387737L;
            public Externalizable() {
                /* do nothing */
            }
            @Override
            public void writeExternal(ObjectOutput objOut) {
                /* do nothing */
            }
            @Override
            public Object read(ObjectInput objIn) {
                return CASE_RESTORING;
            }
        }
    }

    private static final class Tokenizing
        extends FixedWeightEditDistance
        implements Compilable {

        static final long serialVersionUID = 514970533483080541L;

        public Tokenizing() {
            super(0.0,
                  Double.NEGATIVE_INFINITY,
                  Double.NEGATIVE_INFINITY,
                  Double.NEGATIVE_INFINITY,
                  Double.NEGATIVE_INFINITY);
        }
        @Override
        public double insertWeight(char cInserted) {
            return cInserted == ' ' ? 0.0 : Double.NEGATIVE_INFINITY;
        }
        @Override
        public void compileTo(ObjectOutput objOut) throws IOException {
            objOut.writeObject(new Externalizable());
        }
        private static class Externalizable extends AbstractExternalizable {
            private static final long serialVersionUID = -3015819851142009998L;
            public Externalizable() {
                /* do nothing */
            }
            @Override
            public void writeExternal(ObjectOutput objOut) {
                /* do nothing */
            }
            @Override
            public Object read(ObjectInput objIn) {
                return TOKENIZING;
            }
        }
    }


}
