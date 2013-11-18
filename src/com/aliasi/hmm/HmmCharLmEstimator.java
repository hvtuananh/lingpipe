package com.aliasi.hmm;

import com.aliasi.lm.NGramBoundaryLM;

import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Exceptions;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Tuple;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An <code>HmmCharLmEstimator</code> employs a maximum a posteriori
 * transition estimator and a bounded character language model
 * emission estimator.
 *
 * <h3>Emission Language Models</h3>
 *
 * <p>The emission language models are instances {@link
 * NGramBoundaryLM}.  As such, they explicitly model start-of-token
 * (prefix) and end-of-token (suffix) and basic token-shape features.
 * The language model parameters are the usual ones: n-gram length,
 * interpolation ratio (controls amount of smoothing), and number of
 * characters (controls final smoothing).
 *
 * <h3>Transition Estimates and Smoothing</h3>
 *
 * <p>The initial state and final state estimators are <a
 * href="http://en.wikipedia.org/wiki/Multinomial_distribution">multinomial
 * distributions</a>, as is the conditional estimator of the next
 * state given a previous state.  The default behavior is to use <a
 * href="http://en.wikipedia.org/wiki/Maximum_likelihood">maximum
 * likelihood</a> estimates with no smoothing for initial state, final
 * state, and transition likelihoods in the model.  That is, the
 * estimated likelihood of a state being an initial state is
 * proportional its training data frequency, with the actual
 * likelihood being the training data frequency divided by the total
 * training data frequency across tags.
 *
 * <p>With the constructor {@link
 * #HmmCharLmEstimator(int,int,double,boolean)}, a flag may be
 * specified to use smoothing for states.  The smoothing used is
 * add-one smoothing, also called Laplace smoothing.  For each state,
 * it adds one to the count for that state being an initial state and
 * for that state being a final state.  For each pair of states, it
 * adds one to the count of the transitions (including the self
 * transition, which is only counted once.)  This smoothing is
 * equivalent to putting an alpha=1 uniform <a
 * href="http://en.wikipedia.org/wiki/Dirichlet_distribution">Dirichlet
 * prior</a> on the initial state, final state, and conditional next
 * state estimators, with the resulting estimates being the maximum a
 * posteriori estimates.
 *
 * <h4>Training with Partial Data</h4>
 *
 * <p>In the real world, corpora are noisy or incomplete.  As of
 * version 3.4.0, this estimator accepts taggings with
 * <code>null</code> categories.  If a category is <code>null</code>,
 * its emission is not trained, nor are the transitions to it,
 * transitions from it, start states involving it, or end states
 * involving it.
 *
 * <p>The estimator will also accept inputs with null emissions.
 * In the case of a null emission or null category, the emission
 * model will not be trained for that particular token/category
 * pair.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.1
 */
public class HmmCharLmEstimator extends AbstractHmmEstimator {

    private final MapSymbolTable mStateMapSymbolTable;

    private final ObjectToCounterMap<String> mStateExtensionCounter
        = new ObjectToCounterMap<String>();

    private final ObjectToCounterMap<Tuple<String>> mStatePairCounter
        = new ObjectToCounterMap<Tuple<String>>();

    private final Map<String,NGramBoundaryLM> mStateToLm
        = new HashMap<String,NGramBoundaryLM>();
    private final double mCharLmInterpolation;
    private final int mCharLmMaxNGram;
    private final int mMaxCharacters;

    private int mNumStarts = 0;
    private final ObjectToCounterMap<String> mStartCounter
        = new ObjectToCounterMap<String>();

    private int mNumEnds = 0;
    private final ObjectToCounterMap<String> mEndCounter
        = new ObjectToCounterMap<String>();

    private final boolean mSmootheStates;
    final Set<String> mStateSet = new HashSet<String>();


    /**
     * Construct an HMM estimator with default parameter settings.
     * The defaults are <code>6</code> for the maximum character
     * n-gram and 6.0, {@link Character#MAX_VALUE}<code>-1</code> for
     * the maximum number of characters, <code>6.0</code> for the
     * character n-gram interpolation factor, and no state likelihood
     * smoothing.
     */
    public HmmCharLmEstimator() {
        this(6, Character.MAX_VALUE-1, 6.0);
    }

    /**
     * Construct an HMM estimator with the specified maximum character
     * n-gram size, maximum number of characters in the data, and
     * character n-gram interpolation parameter, with no state
     * smoothing.  For more information on these parameters, see
     * {@link NGramBoundaryLM#NGramBoundaryLM(int,int,double,char)}.
     *
     * @param charLmMaxNGram Maximum n-gram for emission character
     * language models.
     * @param maxCharacters Maximum number of unique characters in
     * the training and test data.
     * @param charLmInterpolation Interpolation parameter for character
     * language models.
     * @throws IllegalArgumentException If the max n-gram is less
     * than one, the max characters is less than 1 or greater than
     * {@link Character#MAX_VALUE}<code>-1</code>, or if the interpolation
     * parameter is negative or greater than 1.0.
     */
    public HmmCharLmEstimator(int charLmMaxNGram,
                              int maxCharacters,
                              double charLmInterpolation) {
        this(charLmMaxNGram,maxCharacters,charLmInterpolation,false);

    }

    /**
     * Construct an HMM estimator with the specified maximum character
     * n-gram size, maximum number of characters in the data,
     * character n-gram interpolation parameter, and state
     * smoothing.  For more information on these parameters, see
     * {@link NGramBoundaryLM#NGramBoundaryLM(int,int,double,char)}.
     * For information on state smoothing, see the class documentation
     * above.
     *
     * @param charLmMaxNGram Maximum n-gram for emission character
     * language models.
     * @param maxCharacters Maximum number of unique characters in
     * the training and test data.
     * @param charLmInterpolation Interpolation parameter for character
     * language models.
     * @param smootheStates Flag indicating if add one smoothing is
     * carried out for HMM states.
     * @throws IllegalArgumentException If the max n-gram is less
     * than one, the max characters is less than 1 or greater than
     * {@link Character#MAX_VALUE}<code>-1</code>, or if the interpolation
     * parameter is negative or greater than 1.0.
     */
    public HmmCharLmEstimator(int charLmMaxNGram,
                              int maxCharacters,
                              double charLmInterpolation,
                              boolean smootheStates) {
        super(new MapSymbolTable());
        mSmootheStates = smootheStates;
        if (charLmMaxNGram < 1) {
            String msg = "Max n-gram must be greater than 0."
                + " Found charLmMaxNGram=" + charLmMaxNGram;
            throw new IllegalArgumentException(msg);
        }
        if (maxCharacters < 1 || maxCharacters > (Character.MAX_VALUE-1)) {
            String msg = "Require between 1 and "
                + (Character.MAX_VALUE-1) + " max characters."
                + " Found maxCharacters=" + maxCharacters;
            throw new IllegalArgumentException(msg);
        }
        if (charLmInterpolation < 0.0) {
            String msg = "Char interpolation param must be between "
                + " 0.0 and 1.0 inclusive."
                + " Found charLmInterpolation=" + charLmInterpolation;
            throw new IllegalArgumentException(msg);
        }
        mStateMapSymbolTable = (MapSymbolTable) stateSymbolTable();
        mCharLmInterpolation = charLmInterpolation;
        mCharLmMaxNGram = charLmMaxNGram;
        mMaxCharacters = maxCharacters;
    }

    void addStateSmoothe(String state) {
        if (!mStateSet.add(state)) return;
        mStateMapSymbolTable.getOrAddSymbol(state);
        if (!mSmootheStates) return;
        trainStart(state);
        trainEnd(state);
        for (String state2 : mStateSet) {
            trainTransit(state,state2);
            if (state.equals(state2)) continue;
            trainTransit(state2,state);
        }

    }

    @Override
    public void trainStart(String state) {
        if (state == null) return; // allow nulls, but ignore
        addStateSmoothe(state);
        ++mNumStarts;
        mStartCounter.increment(state);
    }

    static void verifyNonNegativeCount(int count) {
        if (count >= 0) return;
        String msg = "Counts must be positve."
            + " Found count=" + count;
        throw new IllegalArgumentException(msg);
    }


    @Override
    public void trainEnd(String state) {
        if (state == null) return; // ignore null states
        addStateSmoothe(state);
        mStateExtensionCounter.increment(state);
        ++mNumEnds;
        mEndCounter.increment(state);
    }

    @Override
    public void trainEmit(String state, CharSequence emission) {
        if (state == null) return; // ignore null states
        if (emission == null) return; // ignore null emissions
        addStateSmoothe(state);
        emissionLm(state).train(emission);
    }

    @Override
    public void trainTransit(String sourceState, String targetState) {
        if (sourceState == null || targetState == null) return; // ignore null states
        addStateSmoothe(sourceState);
        addStateSmoothe(targetState);
        mStateExtensionCounter.increment(sourceState);
        mStatePairCounter.increment(Tuple.<String>create(sourceState,targetState));
    }


    @Override
    public double startProb(String state) {
        double count = mStartCounter.getCount(state);
        double total = mNumStarts;
        return count / total;
    }

    @Override
    public double endProb(String state) {
        double count = mEndCounter.getCount(state);
        double total = mNumEnds;
        return count / total;
    }

    /**
     * Returns the transition estimate from the specified source state
     * to the specified target state.  For this estimator, this is
     * just the maximum likelihood estimate.  If all transitions
     * should be allowed, then each pair of states should be presented
     * in both orders to {@link #trainTransit(String,String)}, in
     * order to produce add-one smoothing.  Typically, maximum
     * likelihood estimates of state transitions are fine for HMMs
     * trained with large sets of supervised data.
     *
     * @param source Originating state for the transition.
     * @param target Resulting state after the transition.
     * @return Maximum likelihood estimate of transition probability
     * given training data.
     */
    @Override
    public double transitProb(String source, String target) {
        double extCount = mStateExtensionCounter.getCount(source);
        double pairCount
            = mStatePairCounter.getCount(Tuple.create(source,target));
        return pairCount / extCount;
    }

    /**
     * Returns the estimate of the probability of the specified string
     * being emitted from the specified state.  For a character
     * language-model based HMM, this is just the language model
     * estimate of the string likelihood of the emission for the
     * particular state.
     *
     * @param state State of HMM.
     * @param emission String emitted by state.
     * @return Estimate of probability of state emitting string.
     */
    @Override
    public double emitProb(String state, CharSequence emission) {
        return java.lang.Math.pow(2.0,emitLog2Prob(state,emission));
    }

    @Override
    public double emitLog2Prob(String state, CharSequence emission) {
        return emissionLm(state).log2Estimate(emission);
    }

    /**
     * Returns the language model used for emission probabilities for
     * the specified state.  By grabbing the models directly in
     * this way, they may be pruned, etc., before being compiled
     *
     * @param state State of the HMM.
     * @return The language model for the specified state.
     */
    public NGramBoundaryLM emissionLm(String state) {
        NGramBoundaryLM lm = mStateToLm.get(state);
        if (lm == null) {
            lm = new NGramBoundaryLM(mCharLmMaxNGram,
                                     mMaxCharacters,
                                     mCharLmInterpolation,
                                     '\uFFFF');
            mStateToLm.put(state,lm);
        }
        return lm;
    }

    @Override
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 8463739963673120677L;
        final HmmCharLmEstimator mEstimator;
        public Externalizer() {
            this(null);
        }
        public Externalizer(HmmCharLmEstimator handler) {
            mEstimator = handler;
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            try {
                return new CompiledHmmCharLm(in);
            } catch (ClassNotFoundException e) {
                throw Exceptions.toIO("HmmCharLmEstimator.compileTo()",e);
            }
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            // state sym table
            objOut.writeObject(mEstimator.mStateMapSymbolTable);

            int numStates = mEstimator.mStateMapSymbolTable.numSymbols();

            // float matrix: #state x #state
            for (int i = 0; i < numStates; ++i)
                for (int j = 0; j < numStates; ++j)
                    objOut.writeDouble((float) mEstimator.transitProb(i,j));

            // LM^(#state)
            for (int i = 0; i < numStates; ++i) {
                String state = mEstimator.mStateMapSymbolTable.idToSymbol(i);
                mEstimator.emissionLm(state).compileTo(objOut);
            }

            // start prob vector
            for (int i = 0; i < numStates; ++i)
                objOut.writeDouble(mEstimator.startProb(i));

            // end prob vector
            for (int i = 0; i < numStates; ++i)
                objOut.writeDouble(mEstimator.endProb(i));
        }
    }

}
