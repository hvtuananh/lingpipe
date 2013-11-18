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

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tag.Tagging;

import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectOutput;

import java.util.Arrays;

/**
 * An <code>HmmEstimator</code> may be used to train a hidden Markov
 * model (HMM).  Training events are supplied through the {@link
 * com.aliasi.corpus.ObjectHandler} interface method {@link
 * #handle(Tagging)}.  The estimator implements an
 * HMM, so is suitable for use in a tag-a-little, learn-a-little
 * environment or elswhere when an adaptive HMM is required.
 * At any point, the estimator may be compiled to an object output
 * stream using {@link #compileTo(ObjectOutput)}.  
 * 
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.1
 */
public abstract class AbstractHmmEstimator 
    extends AbstractHmm
    implements ObjectHandler<Tagging<String>>, 
               Compilable {

    private long mNumTrainingTokens = 0;
    private long mNumTrainingTaggings = 0;

    /**
     * Construct an HMM estimator with the specified tag symbol table.
     *
     * @param table Symbol table for tags.
     */
    public AbstractHmmEstimator(SymbolTable table) {
        super(table);
    }

    /**
     * Train the start state estimator with the specified start state.
     * This increases the likelihood that the specified state will be
     * the state of the first token.
     *
     * @param state State being trained.
     */
    public abstract void trainStart(String state);

    /**
     * Train the end state estimator with the specified end state.
     * This increases the likelihood that the specified state will be
     * the state of the last token.
     *
     * @param state State being trained.
     */
    public abstract void trainEnd(String state);

    /**
     * Trains the transition estimator from the specified transition
     * from the specified source state to the specified target state.
     *
     * @param sourceState State from which the transition is made.
     * @param targetState State to which the transition is made.
     */
    public abstract void 
        trainTransit(String sourceState, String targetState);

    /**
     * Train the emission estimator with the specified training
     * instance consisting of a state and emission.  This method may
     * be used for dictionary-based training for a particular state.
     *
     * @param state State being trained.
     * @param emission Emission from state being trained.
     */
    public abstract void trainEmit(String state, CharSequence emission);

    /**
     * Compiles a copy of this estimated HMM to the specified object
     * output.  Reading in the resulting bytes with an object input
     * will produce an instance of {@link HiddenMarkovModel}, but will
     * most likely not be an instance of the same class as the object
     * being compiled.
     *
     * @param objOut Object output to which this estimator is
     * compiled.
     * @throws IOException If there is an I/O exception compiling this
     * object.
     */
    public abstract void compileTo(ObjectOutput objOut) throws IOException;


    /**
     * Return the number of taggings handled.  This is simply the
     * number of times {@link #handle(Tagging)} has been called.
     *
     * @return The number of taggings handled for training.
     */
    public long numTrainingCases() {
        return mNumTrainingTaggings;
    }

    /**
     * Returns the number of tokens handled for training.  This is the
     * sum of the length of token arrays in all calls to the {@link
     * #handle(Tagging)} method.
     *
     * @return The number of tokens handled for training.
     */
    public long numTrainingTokens() {
        return mNumTrainingTokens;
    }

    /**
     * Train the estimator with the specified tokens, whitespaces and
     * states.  The whitespaces are ignored.
     *
     * <P>For a specified tagging made up of parallel sequences
     * of tags and tokens, this method calls:
     * <UL>
     * <LI> {@link #trainTransit(String,String)}
     * on each tag pair, 
     * <LI>{@link #trainEmit(String,CharSequence)} on
     * each tag/token pair,
     * <LI>  {@link #trainStart(String)} on the first tag, and
     * <LI>  {@link #trainEnd(String)} on the last tag.
     * </UL>
     *
     * @param tagging Tagging from which tokens and tags are derived.
     */
    public void handle(Tagging<String> tagging) {
        ++mNumTrainingTaggings;
        mNumTrainingTokens += tagging.size();
        if (tagging.size() < 1) return;
        trainStart(tagging.tag(0));
        for (int i = 0; i < tagging.size(); ++i) {
            trainEmit(tagging.tag(i), tagging.token(i));
            if (i > 0) trainTransit(tagging.tag(i-1),
                                    tagging.tag(i));
        }
        trainEnd(tagging.tag(tagging.size()-1));
    }

}
