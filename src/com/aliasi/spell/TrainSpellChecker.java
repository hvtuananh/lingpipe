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

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.lm.CompiledNGramProcessLM;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

/**
 * A <code>TrainSpellChecker</code> instance provides a mechanism for
 * collecting training data for a compiled spell checker.  Training
 * instances are nothing more than character sequences which represent
 * likely user queries.
 *
 * <h3>Data Normalization</h3>
 *
 * <P>In training the source language model, all training data is
 * whitespace normalized with an initial whitespace, final whitespace,
 * and all internal whitespace sequences converted to a single space
 * character.
 *
 * <h3>Token Sensitivity</h3>
 *
 * <P>A tokenization factory may be optionally specified for training
 * token-sensitive spell checkers.  With tokenization, input is
 * further normalized to insert a single whitespace between all
 * tokens not already separated by a space in the input.  The tokens
 * are then output during compilation and read back into the compiled
 * spell checker.  The set of tokens output may be pruned to remove
 * any below a given count threshold.  The resulting set of tokens
 * is used to constrain the set of alternative spellings suggested
 * during spelling correction to include only tokens in the observed
 * token set.
 *
 * <h3>Direct Training</h3>
 *
 * <P>As an alternative to using the spell checker trainer, a language
 * model may be trained directly and supplied in compiled form along
 * with a weighted edit distance to the public constructors for
 * compiled spell checkers.  It's critical that the normalization happens
 * the same way as for the spell checker trainer.
 *
 * <h3>Weighted Edit Distance</h3>
 *
 * <P>In constructing a spell checker trainer, a compilable weighted
 * edit distance must be specified.  This edit distance model will be
 * compiled along with the language model and token set and used as
 * the channel model in the compiled spell checker.  The
 *
 * <h3>Compilation</h3>
 *
 * <P>After training, a model is written out through the
 * <code>Compilable</code> interface using {@link
 * #compileTo(ObjectOutput)}.  When this model is read back in, it
 * will be an instance of {@link CompiledSpellChecker}.  The compiled
 * spell checkers allow many runtime parameters to be tuned; see the
 * class documentation for full details.
 *
 * <p><b>Warning:</b> Unlike for serialization, the tokenizer factory
 * is <b>not</b> serialized along with the model during compilation.
 * After the compiled spell checker is read back in, use {@link
 * CompiledSpellChecker#setTokenizerFactory(TokenizerFactory)} to set
 * up the tokenizer factory in the compiled model.
 *
 * <h3>Serialization</h3>
 *
 * A spell checker trainer may be serialized in the usual way:
 *
 * <blockquote><pre>
 * TrainSpellChecker trainer = ...;
 * ObjectOutput out = ...;
 * out.writeObject(trainer);</pre></blockquote>
 *
 * And then read back in by reversing this operation:
 *
 * <blockquote><pre>
 * ObjectInput in = ...;
 * TrainSpellChecker trainer
 *   = (TrainSpellChecker) in.readObject();</pre></blockquote>
 *
 * <p>The resulting round trip produces a trainer that is functionally
 * identical to the original one.   Serialization is useufl for
 * storing models for which more training data will be available
 * later.
 *
 * <p><b>Warning:</b> The object input and output used for
 * serialization must extend {@link java.io.InputStream} and {@link
 * java.io.OutputStream}.  The only implementations of {@link ObjectInput} and
 * {@link ObjectOutput} as of the 1.6 JDK do extend the streams, so
 * this will only be a problem with customized object input or output
 * objects.  If you need this method to work with custom input and
 * output objects that do not extend the corresponding streams, drop
 * us a line and we can perhaps refactor the output methods to remove
 * this restriction.  [Note: This warning was inherited from {@link
 * NGramProcessLM}.]
 *
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.0
 */
public class TrainSpellChecker 
    implements ObjectHandler<CharSequence>,
               Compilable,
               Serializable  {

    static final long serialVersionUID = -3599682964675009111L;

    private final WeightedEditDistance mEditDistance;
    private final NGramProcessLM mLM;
    private final TokenizerFactory mTokenizerFactory;
    private final ObjectToCounterMap<String> mTokenCounter;

    private long mNumTrainingChars = 0L;

    private TrainSpellChecker(long numTrainingChars,
                              WeightedEditDistance editDistance,
                              NGramProcessLM lm,
                              TokenizerFactory tokenizerFactory,
                              ObjectToCounterMap<String> tokenCounter) {
        mNumTrainingChars = numTrainingChars;
        mEditDistance = editDistance;
        mLM = lm;
        mTokenizerFactory = tokenizerFactory;
        mTokenCounter = tokenCounter;
    }

    /**
     * Construct a non-tokenizing spell checker trainer from the
     * specified language model and edit distance.  See {@link
     * SpellChecker} for more information on the language model and
     * edit distance models in the compiled spell checker.
     *
     * @param lm Compilable language model.
     * @param editDistance Compilable weighted edit distance.
     * @throws IllegalArgumentException If the edit distance is not
     * compilable.
     */
    public TrainSpellChecker(NGramProcessLM lm,
                             WeightedEditDistance editDistance) {
        this(lm,editDistance,null);
    }

    /**
     * Construct a spell checker trainer from the specified n-gram
     * process language model, tokenizer factory and edit distance.
     * The language model must be an instance of the character-level
     * n-gram process language model class.  The edit distance must be
     * compilable.  The tokenizer factory may be <code>null</code>, in
     * which case tokens are not saved as part of training and the
     * compiled spell checker is not token sensitive.  If the
     * tokenizer factory is specified, it must be compilable.
     *
     * @param lm Compilable language model.
     * @param editDistance Compilable weighted edit distance.
     * @param tokenizerFactory Optional tokenizer factory.
     * @throws IllegalArgumentException If the edit distance is not
     * compilable or if the tokenizer factory is non-null and not compilable.
     */
    public TrainSpellChecker(NGramProcessLM lm,
                             WeightedEditDistance editDistance,
                             TokenizerFactory tokenizerFactory) {
        mLM = lm;
        mTokenizerFactory = tokenizerFactory;
        mEditDistance = editDistance;
        mTokenCounter = new ObjectToCounterMap<String>();
    }


    /**
     * Returns the n-gram process language model (source model)
     * underlying this spell checker trainer.
     *
     * <p>The returned value is a reference to the language model
     * held by the trainer, so any changes to it will affect this
     * spell checker.
     *
     * @return The n-gram process LM for this trainer.
     */
    public NGramProcessLM languageModel() {
        return mLM;
    }

    /**
     * Returns the weighted edit distance (channel model) underlying this spell checker
     * trainer.
     *
     * <p>The returned value is a reference to the langauge model
     * held by the trainer, so any changes to it will affect this
     * spell checker.
     *
     * @return The edit distance for this trainer.
     */
    public WeightedEditDistance editDistance() {
        return mEditDistance;
    }

    /**
     * Returns the counter for the tokens in the training set.  This
     * may be used to print out the tokens with their counts for later
     * perusal.  The value returned is the actual counter, so any
     * changes made to it will be reflected in this spell checker.
     * Pruning the token counts may have eliminated tokens in the
     * training data from the counter.
     *
     * @return The counter for the tokens in the training set.
     */
    public ObjectToCounterMap<String> tokenCounter() {
        return mTokenCounter;
    }

    /**
     * Train the spelling checker on the specified character sequence
     * as if it had appeared with a frequency given by the specified
     * count.
     *
     * <p>See the method {@link #handle(CharSequence)} for information
     * on the normalization carried out on the input character
     * sequence.
     *
     * <p>Although calling this method is equivalent to calling {@link
     * #handle(CharSequence)} the specified count number of times, this
     * mehod is much more efficient because it does not require
     * iteration.
     *
     * <p>This method may be used to boost the training for a specified
     * input, or just to combine inputs into single method calls.
     *
     * @param cSeq Character sequence for training.
     * @param count Frequency of sequence to train.
     * @throws IllegalArgumentException If the specified count is negative.
     */
    public void train(CharSequence cSeq, int count) {
        if (count < 0) {
            String msg = "Training counts must be non-negative."
                + " Found count=" + count;
            throw new IllegalArgumentException(msg);
        }
        if (count == 0) return;
        mLM.train(normalizeQuery(cSeq),count);
        mNumTrainingChars += count * cSeq.length();
    }

    /**
     * Returns the total length in characters of all text used to
     * train the spell checker.
     *
     * @return The number of training characters seen.
     */
    public long numTrainingChars() {
        return mNumTrainingChars;
    }

    /**
     * Train the spell checker on the specified character sequence.
     * The sequence is normalized by normalizing all whitespace
     * sequences to a single space character and inserting an initial
     * and final whitespace.  If a tokenization factory is specified,
     * a single space character is insterted between any tokens
     * not already separated by a white space.
     *
     * @param cSeq Characters for training.
     */
    public void handle(CharSequence cSeq) {
        mLM.train(normalizeQuery(cSeq));
        mNumTrainingChars += cSeq.length();
    }

    /**
     * Prunes the set of collected tokens of all tokens with count
     * less than the specified minimum.  If there was no tokenization
     * factory specified for this spell checker, this method will
     * have no effect.
     *
     * @param minCount Minimum count of preserved token.
     */
    public void pruneTokens(int minCount) {
        mTokenCounter.prune(minCount);
    }

    /**
     * Prunes the underlying character language model to remove
     * substring counts of less than the specified minimum.
     *
     * @param minCount Minimum count of preserved substrings.
     */
    public void pruneLM(int minCount) {
        mLM.substringCounter().prune(minCount);
    }

    /**
     * Writes a compiled spell checker to the specified object output.
     * The class of the spell checker read back in is {@link
     * CompiledSpellChecker}.
     *
     * @param objOut Object output to which this spell checker is
     * written.
     * @throws IOException If there is an I/O error while writing.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }


    private Object writeReplace() {
        return new Serializer(this);
    }

    StringBuilder normalizeQuery(CharSequence cSeq) {
        StringBuilder sb = new StringBuilder();
        sb.append(' ');
        if (mTokenizerFactory == null) {
            Strings.normalizeWhitespace(cSeq,sb);
            sb.append(' ');
        } else {
            char[] cs = Strings.toCharArray(cSeq);
            Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
            String nextToken;
            while ((nextToken = tokenizer.nextToken()) != null) {
                mTokenCounter.increment(nextToken);
                sb.append(nextToken);
                sb.append(' ');
            }
        }
        return sb;
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 4907338741905144267L;
        private final TrainSpellChecker mTrainer;
        public Externalizer() {
            this(null);
        }
        public Externalizer(TrainSpellChecker trainer) {
            mTrainer = trainer;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            mTrainer.mLM.compileTo(objOut);
            boolean tokenizing = mTrainer.mTokenizerFactory != null;
            objOut.writeBoolean(tokenizing);
            if (tokenizing) {
                Set<String> keySet = mTrainer.mTokenCounter.keySet();
                objOut.writeObject(new HashSet<String>(keySet));
            }
            AbstractExternalizable.compileOrSerialize(mTrainer.mEditDistance,objOut);
        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {

            CompiledNGramProcessLM lm
                = (CompiledNGramProcessLM) objIn.readObject();
            boolean tokenizing = objIn.readBoolean();
            // System.out.println("reading token set");
            Set<String> tokenSet = null;
            if (tokenizing) {
                // required for read cast; needs temp for suppression
                @SuppressWarnings("unchecked")
                Set<String> tempTokenSet =  (Set<String>) objIn.readObject();
                tokenSet = tempTokenSet;
            }
            // System.out.println("     finished");
            WeightedEditDistance editDistance
                = (WeightedEditDistance) objIn.readObject();
            return new CompiledSpellChecker(lm,editDistance,tokenSet);
        }
    }

    static class Serializer extends AbstractExternalizable {

        static final long serialVersionUID = -8575906929649837646L;

        private TrainSpellChecker mTrainer;
        public Serializer() {
            this(null);
        }
        public Serializer(TrainSpellChecker trainer) {
            mTrainer = trainer;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeLong(mTrainer.mNumTrainingChars);
            objOut.writeObject(mTrainer.mLM);
            boolean tokenizing = mTrainer.mTokenizerFactory != null;
            objOut.writeBoolean(tokenizing);
            if (tokenizing) {
                AbstractExternalizable.serializeOrCompile(mTrainer.mTokenizerFactory,objOut);
                objOut.writeObject(mTrainer.mTokenCounter);
            }
            AbstractExternalizable.serializeOrCompile(mTrainer.mEditDistance,objOut);
        }
        @Override
        public Object read(ObjectInput objIn)
            throws ClassNotFoundException, IOException {

            long numTrainingChars = objIn.readLong();
            NGramProcessLM lm = (NGramProcessLM) objIn.readObject();
            boolean tokenizing = objIn.readBoolean();
            TokenizerFactory tokenizerFactory = null;
            ObjectToCounterMap<String> tokenCounter = null;
            if (tokenizing) {
                tokenizerFactory = (TokenizerFactory) objIn.readObject();
                // required for readObject; temp required for suppress
                @SuppressWarnings("unchecked")
                ObjectToCounterMap<String> tempTokenCounter = (ObjectToCounterMap<String>) objIn.readObject();
                tokenCounter = tempTokenCounter;
            }
            WeightedEditDistance editDistance
                = (WeightedEditDistance) objIn.readObject();
            return new TrainSpellChecker(numTrainingChars,
                                         editDistance,
                                         lm,
                                         tokenizerFactory,
                                         tokenCounter);
        }

    }

}
