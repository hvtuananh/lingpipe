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

import com.aliasi.tag.Tagger;
import com.aliasi.tag.NBestTagger;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.MarginalTagger;
import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.Tagging;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Iterators;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * An <code>HmmDecoder</code> provides implementations of first-best,
 * n-best and marginal taggers for hidden Markov models (HMMs).  A
 * decoder is constructed from a hidden Markov model.
 *
 * <h3>First-best Output</h3>
 * 
 * HMM decoders implement the interface {@link Tagger}, which
 * specifies a first-best tagging method {@link #tag(List)}.
 * This method provides the likely (first best) path of HMM states
 * (tags) given a sequence of string emissions (outputs).  First-best
 * decoding is implemented using Viterbi's dynamic programming
 * algorithm.
 *
 * <h3>N-best Output</h3>
 *
 * <P>HMM decoders also implement the interface {@code NBestTagger},
 * which specifies the method {@link #tagNBest(List,int)} and {@link
 * #tagNBestConditional(List,int)}.  These methods both return an
 * iterator over scored taggings.  N-best decoding is implemented
 * using the Viterbi algorithm in a forward pass and the A<sup>*</sup>
 * algorithm in the backward pass using the Viterbi estimates as exact
 * completion estimates.  The variant conditional method further
 * normalizes outputs to posterior conditional probabilities, and is
 * a bit more expensive to compute.
 *
 * <h3>Confidence and Lattice Output</h3>
 *
 * <p>HMM decoders also implement the {@link MarginalTagger}
 * interface, which specifies a method {@link #tagMarginal(List)} for
 * providing marginal probability estimates for categories for a token
 * given the input string.  Marginal decoding is implemented using the
 * standard forward-backward algorithm.  The lattice is an instance of
 * {@link TagLattice}, which itself implements {@link TagLattice};
 * see that class's documentation for information on how to retrieve
 * cumulative (total) probabilities for input token sequences and
 * posterior conditional probabilities (confidence scores) per token.
 *
 * <h3>Caching</h3> 
 *
 * <P>The decoder is able to cache emission probabilities, the
 * computation of which is often the bottleneck in decoding.  Caches
 * may be provided in the constructor for both ordinary (linear)
 * probabilities and log probabilities of emissions.  A cache is
 * simply an instance of {@link Map} from strings to arrays of doubles.
 *
 * <p>The first-best and n-best decoders only uses log probabilities,
 * whereas the n-best normalized and lattice decoders use linear
 * probabilities.  Only the probabilities computed are cached, so if a
 * program only does first-best processing, only linear estimates are
 * cached.
 * 
 * <P>Any implementation of <code>Map</code> may be used as a cache,
 * but particular attention must be paid to thread safety and
 * scalability.  A fully synchronized cache can be created with:
 * 
 * <blockquote><pre>
 * Map&lt;String,double[]&gt; cache 
 *     = java.util.Collections.synchronizedMap(new HashMap&lt;String,double[]&gt;());</pre></blockquote>
 *
 * LingPipe's map implementation {@link com.aliasi.util.FastCache} is
 * designed specifically to be used as a cache in settings such as
 * these.
 *
 * <P>It is often (e.g. on English newsire) easy to get high token
 * coverage (e.g. 97%) with a rather modestly sized cache (e.g. 100K
 * tokens).  Other corpora and languages may vary and we encourage
 * experimentation with efficiency versus memory for caching.  Note
 * that run times will speed up as more and more estimates are returned
 * from the cache rather than being computed directly.
 *
 * <h3>Synchrnonization and Thread Safety</h3>
 *
 * <p>This class does not perform any underlying sychronization.  If
 * the hidden Markov model is not thread safe, then it must be
 * synchronized.  Similarly for the caches.  Note that {@link
 * com.aliasi.util.FastCache}, while not synchronized, is thread safe.
 * Similarly, the compilation of an HMM trained with {@link
 * HmmCharLmEstimator} is thread safe, in fact allowing safe
 * concurrent access because it is immutable.
 *
 * <h3>Beam and Pruning</h3>
 *
 * <p>For first-best and n-best decoding, a beam may be used to prune
 * unlikely hypotheses.  This beam is set during construction or
 * through the method {@link #setLog2EmissionBeam(double)} (setting
 * and access must be concurrent read/exclusive write synchronized
 * from the caller).  The beam works token by token.  As each token is
 * considered, any tag whose emission log (base 2) likelihood is more
 * than the beam less than the bes5t emission estimate is eliminated
 * from further consideration.
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.1
 */
public class HmmDecoder 
    implements Tagger<String>,
               NBestTagger<String>,
               MarginalTagger<String> {
    
    private final HiddenMarkovModel mHmm;

    private Map<String,double[]> mEmissionCache;
    private Map<String,double[]> mEmissionLog2Cache;

    private double mLog2EmissionBeam;
    private double mLog2Beam;

    /**
     * Construct an HMM decoder from the specified HMM.  No caching is
     * applied to estimates, and the beams are set to positive infinity,
     * turning off pruning.  This constructor is appropriate for
     * dynamic models with changing probability estimates.
     *
     * @param hmm Model to use as basis of decoding.
     */
    public HmmDecoder(HiddenMarkovModel hmm) {
        this(hmm,null,null);
    }

    /**
     * Construct an HMM decoder from the specified HMM using the
     * specified caches for linear and log probabilities.  The beams
     * are set to positive infinity, turning off pruning.  Either or
     * both of the caches may be <code>null</code>, in which case the
     * corresponding values will not be cached.
     *
     * @param hmm Model to use for decoding.
     * @param emissionCache Map to use for emission caching.
     * @param emissionLog2Cache Map to use for log emission caching.
     */
    public HmmDecoder(HiddenMarkovModel hmm, 
                      Map<String,double[]> emissionCache,
                      Map<String,double[]> emissionLog2Cache) {
        this(hmm,emissionCache,emissionLog2Cache,
             Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Construct an HMM decoder from the specified HMM using the
     * specified caches for linear and log probabilities, with the
     * specified beam width for emission estimates.  Either or both of
     * the caches may be <code>null</code>, in which case the
     * corresponding values will not be cached.
     *
     * @param hmm Model to use for decoding.
     * @param emissionCache Map to use for emission caching.
     * @param emissionLog2Cache Map to use for log emission caching.
     * @param log2Beam The log (base 2) beam for pruning full hypotheses.
     * @param log2EmissionBeam The log (base 2) beam for pruning emission hypotheses.
     * @throws IllegalArgumentException If either beam is not a
     * non-negative number.
     */
    public HmmDecoder(HiddenMarkovModel hmm,
                      Map<String,double[]> emissionCache,
                      Map<String,double[]> emissionLog2Cache,
                      double log2Beam,
                      double log2EmissionBeam) {
        mHmm = hmm;
        mEmissionCache = emissionCache;
        mEmissionLog2Cache = emissionLog2Cache;
        setLog2Beam(log2Beam);
        setLog2EmissionBeam(log2EmissionBeam);
    }

    /**
     * Returns the hidden Markov model underlying this decoder.
     * The returned value is the actual HMM used by this decoder,
     * so changes to it will affect this decoder.
     *
     * @return The HMM for this decoder.
     */
    public HiddenMarkovModel getHmm() {
        return mHmm;
    }

    /**
     * Returns the mapping used to cache emission probabilities, or
     * <code>null</code> if not caching.  This is the actual mapping,
     * so changes to it will affect this decoder.
     *
     * @return The emission probability cache.
     */
    public Map<String,double[]> emissionCache() {
        return mEmissionCache;
    }

    /**
     * Returns the mapping used to cache log (base 2) emission
     * probabilities, or <code>null</code> if not caching.  This is
     * the actual mapping, so changes to it will affect this decoder.
     *
     * @return The emission probability cache.
     */
    public Map<String,double[]> emissionLog2Cache() {
        return mEmissionLog2Cache;
    }


    /**
     * Sets the emission cache to the specified value.
     *
     * <p><i>Warning:</i> This method should not be executed
     * concurrently with any calls to decoding, as it may produce an
     * inconsistent result.  The typical application will be to set a
     * cache before using a decoder.
     *
     * @param cache Cache for linear emission estimates.
     */
    public void setEmissionCache(Map<String,double[]> cache) {
        mEmissionCache = cache;
    }

    /**
     * Sets the log (base 2) emission beam width.  Any tag with
     * a log (base 2) emission probability more than the beam width
     * less than the best hypothesis is discarded.  Setting the beam
     * width to zero results in pruning to category with the best emission.
     * Setting the beam width to positive infinity effectively turns
     * off the beam.
     *
     * @param log2EmissionBeam Width of beam.
     * @throws IllegalArgumentException If the beam width is negative.
     */
    public void setLog2EmissionBeam(double log2EmissionBeam) {
        if (log2EmissionBeam <= 0 
            || Double.isNaN(log2EmissionBeam)) {
            String msg = "Beam width must be a positive number."
                + " Found log2EmissionBeam=" + log2EmissionBeam;
            throw new IllegalArgumentException(msg);
        }
        mLog2EmissionBeam = log2EmissionBeam;
    }

    /**
     * Sets the value of the log2 beam to the specified value.  
     * This beam controls pruning based on full Viterbi values
     * for a given lattice slice.  See the class documentation above
     * for more details.
     *
     * @param log2Beam The log (base 2) Viterbi beam.
     * @throws IllegalArgumentException If the beam width is negative.
     */
    public void setLog2Beam(double log2Beam) {
        if (log2Beam <= 0 
            || Double.isNaN(log2Beam)) {
            String msg = "Beam width must be a positive number."
                + " Found log2EmissionBeam=" + log2Beam;
            throw new IllegalArgumentException(msg);
        }
        mLog2Beam = log2Beam;

    }

    /**
     * Sets the log emission cache to the specified value.
     *
     * <p><i>Warning:</i> This method should not be executed
     * concurrently with any calls to decoding, as it may produce an
     * inconsistent result.  The typical application will be to set a
     * cache before using a decoder.
     *
     * @param cache Cache for linear emission estimates.
     */
    public void setEmissionLog2Cache(Map<String,double[]> cache) {
        mEmissionLog2Cache = cache;
    }

    double[] cachedEmitProbs(String emission) {
        double[] emitProbs = mEmissionCache.get(emission);
        if (emitProbs != null) {
            return emitProbs;
        }
        emitProbs = computeEmitProbs(emission);
        mEmissionCache.put(emission,emitProbs);
        return emitProbs;
    }
    
    double[] computeEmitProbs(String emission) {
        int numTags = mHmm.stateSymbolTable().numSymbols();
        double[] emitProbs = new double[numTags];
        for (int i = 0; i < numTags; ++i)
            emitProbs[i] = mHmm.emitProb(i,emission);
        return emitProbs;
    }

    double[] emitProbs(String emission) {
        return (mEmissionCache == null)
            ? computeEmitProbs(emission)
            : cachedEmitProbs(emission);
    }

    double[] cachedEmitLog2Probs(String emission) {
        double[] emitLog2Probs = mEmissionLog2Cache.get(emission);
        if (emitLog2Probs != null) {
            return emitLog2Probs;
        }
        emitLog2Probs = computeEmitLog2Probs(emission);
        mEmissionLog2Cache.put(emission,emitLog2Probs);
        return emitLog2Probs;
    }

    double[] computeEmitLog2Probs(String emission) {
        int numTags = mHmm.stateSymbolTable().numSymbols();
        double[] emitLog2Probs = new double[numTags];
        for (int i = 0; i < numTags; ++i)
            emitLog2Probs[i] = mHmm.emitLog2Prob(i,emission);
        additiveBeamPrune(emitLog2Probs,mLog2EmissionBeam);
        return emitLog2Probs;
    }

    static void additiveBeamPrune(double[] emitLog2Probs, double beam) {
        if (beam == Double.POSITIVE_INFINITY) return; // no pruning
        double best = emitLog2Probs[0];
        for (int i = 1; i < emitLog2Probs.length; ++i)
            if (emitLog2Probs[i] > best) 
                best = emitLog2Probs[i];
        for (int i = 1; i < emitLog2Probs.length; ++i)
            if (emitLog2Probs[i] + beam < best)
                emitLog2Probs[i] = Double.NEGATIVE_INFINITY;
    }

    double[] emitLog2Probs(String emission) {
        return (mEmissionLog2Cache == null)
            ? computeEmitLog2Probs(emission)
            : cachedEmitLog2Probs(emission);
    }


    /**
     * Return a complete tag-word lattice for the specified array of
     * string emissions.  Lattices provide forward and backward
     * values.
     *
     * <P><i>Implementation Note:</i> This method is implemented by
     * the standard forward-backward dynamic programming algorithm.
     * The estimates
     * <code>P<sub><sub>&alpha;</sub></sub>(n,state)</code> are of
     * the probability of a derivation starting at the beginning and
     * arriving at the specified state after the specified number of
     * tokens.
     * 
     * <blockquote><code>
     * P<sub><sub>&alpha;</sub></sub>(0,state) 
     *   = P<sub><sub>start</sub></sub>(state)
     *     * P<sub><sub>emit</sub></sub>(emissions[0],state)
     * </code></blockquote>
     * <blockquote><code>
     * P<sub><sub>&alpha;</sub></sub>(n,state)
     * <br> &nbsp; &nbsp; 
     * = <big><big>&Sigma;</big></big><sub><sub>sourceState</sub></sub>
     *     P<sub><sub>&alpha;</sub></sub>(n-1,sourceState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>transit</sub></sub>(state|sourceState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>emit</sub></sub>(emissions[n]|state)
     * </code></blockquote>
     *
     * Note that the forward probabilities up to a token position
     * include the emission probability for that token.
     *
     * <P>The backward values are defined dually as the probability of
     * a derivation ending at a specified state being continued to
     * the end of the analysis.  
     * 
     * <blockquote><code>
     * P<sub><sub>&beta;</sub></sub>(|emissions|-1,state) 
     *   = P<sub><sub>end</sub></sub>(state)
     * </code></blockquote>
     *
     * <blockquote><code>
     * P<sub><sub>&beta;</sub></sub>(n,state)
     * <br> &nbsp; &nbsp; 
     * = <big><big>&Sigma;</big></big><sub><sub>targetState</sub></sub>
     *     P<sub><sub>&beta;</sub></sub>(n+1,targetState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>transit</sub></sub>(targetState|state)
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>emit</sub></sub>(emissions[n+1]|targetState)
     * </code></blockquote>
     *
     * Note that token emission probabilities for a given state are not
     * included in the backward score; they are computed with target
     * states in a way that matches the forward algorithm's
     * definition.  This asymmetry is so that the forward-backward
     * estimates <code>P<sub><sub>&gamma;</sub></sub>(n,state)</code>
     * correspond to the probability of a derivation being in a given
     * state for a given position given the input:
     *
     * <blockquote><code>
     * P<sub><sub>&gamma;</sub></sub>(n,state)
     * = P<sub><sub>&alpha;</sub></sub>(n,state)
     * * P<sub><sub>&beta;</sub></sub>(n,state)
     * </code></blockquote>
     *
     * These values include the token emission probabilities,
     * but may be normalized in the usual Bayesian fashion by
     * dividing by the marginal <code>P(emissions)</code>:
     *
     * <blockquote><code>
     * P(n,state|emissions) 
     * = P<sub><sub>&gamma;</sub></sub>(n,state) / P(emissions)</code>
     * </code></blockquote>
     *
     * where the marginal is just the sum of all forward-backward
     * values for any token position <code>i</code>:
     * 
     * <blockquote><code>
     * P(emissions)
     * = <big><big>&Sigma;</big></big><sub><sub>state</sub></sub>
     *     P<sub><sub>&gamma;</sub></sub>(i,state)
     * </code></blockquote>
     *
     * Most of the computation is carried out by the (private)
     * implementation of the tag lattice, which requires the following
     * start, end and transition arrays:
     *
     * <blockquote><code>
     * start(state) = P<sub><sub>start</sub></sub>(0,state)
     *                * P<sub><sub>emit</sub></sub>(emissions[0],state)
     * </code></blockquote>
     * 
     * <blockquote><code>
     * end(state) = P<sub><sub>end</sub></sub>(state)
     * </code></blockquote>
     * 
     * <blockquote><code>
     * transition(i,sourceState,targetState)
     * <br> &nbsp; &nbsp; 
     * = P<sub><sub>transit</sub></sub>(targetState|sourceState)
     * <br> &nbsp; &nbsp; &nbsp; 
     *   * P<sub><sub>emit</sub></sub>(emissions[i]|targetState)
     * </code></blockquote>
     * 
     * @param emissions Array of strings emitted.
     * @return Full tag-word lattice for specified emissions.
     */
    TagWordLattice lattice(String[] emissions) {
        int numTokens = emissions.length;
        int numTags = mHmm.stateSymbolTable().numSymbols();
        if (numTokens == 0) 
            return new TagWordLattice(emissions,mHmm.stateSymbolTable(),
                                      new double[numTags],
                                      new double[numTags],
                                      new double[0][numTags][numTags]);

        double[] starts = new double[numTags];
        double[] emitProbs = emitProbs(emissions[0]);
        for (int tagId = 0; tagId < numTags; ++tagId)
            starts[tagId]
                = mHmm.startProb(tagId) * emitProbs[tagId];
    
        double[][][] transitions = new double[numTokens][][];
        for (int i = 1; i < numTokens; ++i) {
            double[][] transitionsI = new double[numTags][];
            transitions[i] = transitionsI;
            double[] emitProbs2 = emitProbs(emissions[i]);
            for (int prevTagId = 0; prevTagId < numTags; ++prevTagId) {
                double[] transitionsIPrevTag = new double[numTags];
                transitions[i][prevTagId] = transitionsIPrevTag;
                for (int tagId = 0; tagId < numTags; ++tagId) {
                    double transitEstimate = mHmm.transitProb(prevTagId,tagId);
                    transitionsIPrevTag[tagId] 
                        = transitEstimate * emitProbs2[tagId];
                }
            }
        }

        double[] ends = new double[numTags];
        for (int tagId = 0; tagId < numTags; ++tagId)
            ends[tagId] = mHmm.endProb(tagId);

        return new TagWordLattice(emissions,mHmm.stateSymbolTable(),
                                  starts,ends,transitions);
    }
            
    /**
     * Returns an array consisting of the states with the highest
     * likelihood to emit the specifed array of strings.  
     *
     * <P><i>Implementation Note:</i> This method is implemented with
     * the Viterbi algorithm.  The Viterbi algorithm uses dynamic
     * programming (memoization) to compute the maximum probability of 
     * arriving in a state <code>state</code> after consuming
     * inputs <code>emissions[0],...,emissions[n-1]</code>:
     * 
     * <blockquote><code>
     * P<sub><sub>best</sub></sub>(0,state) 
     * <br> &nbsp; &nbsp;
     *   = P<sub><sub>start</sub></sub>(state)
     * <br> &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>emit</sub></sub>(emissions[0],state)
     * </code></blockquote>
     *
     * <blockquote><code>
     * P<sub><sub>best</sub></sub>(n,state)
     * <br> &nbsp; &nbsp;
     * = MAX<sub><sub>prevState</sub></sub>
     *     P<sub><sub>best</sub></sub>(n-1,prevState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>transit</sub></sub>(state|prevState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
     *     * P<sub><sub>emit</sub></sub>(emissions[n]|state)
     * </code></blockquote>
     *
     * <blockquote><code>
     * P<sub><sub>best</sub></sub>(last,state) 
     *    *= P<sub><sub>end</sub></sub>(state)
     * </code></blockquote>
     *
     * Note that the initial condition uses the start probability
     * rather than the transition times the previous best probability.
     * The notation in the last line is meant to indicate that
     * the last index has the probability of a state being the
     * last state multiplied in.  As usual, we use logarithms
     * and additions rather than multiplication.
     * 
     * <P>As usual, the algorithm employs an array of backpointers
     * from a state at a given input to the last state along
     * the best path.  This is computed by simply recording the
     * state maximizing the above equation:
     *
     * <blockquote><code>
     * backPtr(0,state) = null
     * </code></blockquote>
     *
     * <blockquote><code>
     * backPtr(n,state) 
     * <br> &nbsp; &nbsp;
     * = ARGMAX<sub><sub>prevState</sub></sub>
     *       P<sub><sub>best</sub></sub>(n-1,prevState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; 
     *      &nbsp; &nbsp;
     *       * P<sub><sub>transit</sub></sub>(state|prevState) 
     * <br> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; 
     *      &nbsp; &nbsp;
     *       * P<sub><sub>emit</sub></sub>(emissions[n]|state)
     * </code></blockquote>
     *
     * By tracing the array of backpointers from the best final
     * state, the best path can be recovered.
     *
     * @param emissions Array of strings emitted.
     * @return Array of states most likely to have emitted the
     * specified strings.
     */
    String[] firstBest(String[] emissions) {
        if (emissions.length == 0) 
            return Strings.EMPTY_STRING_ARRAY;
        return new Viterbi(emissions).bestStates();
    }

    /**
     * Returns a best-first iterator of {@link ScoredObject} instances
     * consisting of arrays of tags and log (base 2) joint likelihoods
     * of the tags and emissions with respect to the underlying HMM.
     * Only analyses with non-zero probability estimates are returned.
     *
     * <P><i>Implementation Note:</i> This method is implemented by
     * doing a Viterbi search to provide exact A<sup>*</sup> bounds
     * for a backwards n-best pass using the A<sup>*</sup> algorithm.
     * Thus it will be slower than just computing the first best
     * result using {@link #firstBest(String[])}.  The iterator stores
     * the entire Viterbi lattice as well as a priority queue of
     * partial states ordered by the A<sup>*</sup> condition.
     * 
     * @param emissions String outputs whose tag sequences are returned.
     * @return Iterator over scored tag sequences in decreasing order
     * of likelihood.
     */
    Iterator<ScoredObject<String[]>> nBest(String[] emissions) {
        if (emissions.length == 0) {
            ScoredObject<String[]> result 
                = new ScoredObject<String[]>(Strings.EMPTY_STRING_ARRAY,0.0);
            return Iterators.<ScoredObject<String[]>>singleton(result);
        }
        Viterbi viterbiLattice = new Viterbi(emissions);
        return new NBestIterator(viterbiLattice,Integer.MAX_VALUE);
    }

    /**
     * Returns a best-first iterator of {@link ScoredObject} instances
     * consisting of arrays of tags and log (base 2) joint likelihoods
     * of the tags and emissions with respect to the underlying HMM up
     * to the specified maximum number of results.
     *
     * <P><i>Implementation Note:</i> This method is implemented by
     * doing a Viterbi search to provide exact A<sup>*</sup> bounds
     * for a backwards n-best pass using the A<sup>*</sup> algorithm.
     * Thus it will be slower than just computing the first best
     * result using {@link #firstBest(String[])}.  The iterator stores
     * the entire Viterbi lattice as well as a priority queue of
     * partial states ordered by the A<sup>*</sup> condition.
     * 
     * @param emissions String outputs whose tag sequences are returned.
     * @return Iterator over scored tag sequences in decreasing order
     * of likelihood.
     */
    Iterator<ScoredObject<String[]>> nBest(String[] emissions, int maxN) {
        if (emissions.length == 0) {
            ScoredObject<String[]> result 
                = new ScoredObject<String[]>(Strings.EMPTY_STRING_ARRAY,0.0);
            return Iterators.<ScoredObject<String[]>>singleton(result);
        }
        Viterbi viterbiLattice = new Viterbi(emissions);
        return new NBestIterator(viterbiLattice,maxN);
    }

    /**
     * Returns a best-first iterator of scored objects consisting of
     * arrays of tags and log (base 2) conditional likelihoods of the
     * tags given the specified emissions with respect to the
     * underlying HMM.  Only analyses with non-zero probability
     * estimates are returned.  For this method, the sum of all
     * iterated estimates should be 1.0, plus or minus rounding
     * errors.
     *
     * <P>Conditional estimates of tags given emissions are derived
     * from dividing the joint estimates by the marginal likelihood
     * of the emissions as computed by summing over all joint estimates.
     * 
     * <P><i>Implementation Note:</i> The total log likelihood is
     * returned by applying {@link TagLattice#logZ()} to the
     * result of decoding the input with {@link #lattice(String[])}.
     * The joint estimates are iterated using the iterator returned by
     * {@link #nBest(String[])} and then modified by subtracting the
     * emission marginal log likelihood from the joint emission/tags
     * log likelihood.  This method adds the cost of the full lattice
     * computation to the joint n-best method.  The space for the full
     * lattice is used transiently when this method is called and
     * may be garbage-collected even before the first element is returned
     * by the iterator.  
     * 
     * @param emissions String outputs whose tag sequences are returned.
     * @return Iterator over scored tag sequences in decreasing order
     * of likelihood.
     */
    Iterator<ScoredObject<String[]>> nBestConditional(String[] emissions) {
        Iterator<ScoredObject<String[]>> nBestIterator = nBest(emissions);
        double jointLog2Prob = lattice(emissions).log2Total();
        return new JointIterator(nBestIterator,jointLog2Prob);
    }


    public Tagging<String> tag(List<String> tokens) {
        String[] tokenArray = tokens.toArray(Strings.EMPTY_STRING_ARRAY);
        String[] tags = firstBest(tokenArray);
        return new Tagging<String>(Arrays.asList(tokenArray),
                                   Arrays.asList(tags));
    }

    public Iterator<ScoredTagging<String>> tagNBest(List<String> tokens, int maxResults) {
        String[] tokenArray = tokens.toArray(Strings.EMPTY_STRING_ARRAY);
        Iterator<ScoredObject<String[]>> it = nBest(tokenArray,maxResults);
        return new TaggingIteratorAdapter(tokens,it,maxResults);
    }

    public Iterator<ScoredTagging<String>> tagNBestConditional(List<String> tokens, int maxResults) {
        String[] tokenArray = tokens.toArray(Strings.EMPTY_STRING_ARRAY);
        Iterator<ScoredObject<String[]>> it = nBestConditional(tokenArray);
        return new TaggingIteratorAdapter(tokens,it,maxResults);
    }

    public TagLattice<String> tagMarginal(List<String> tokens) {
        String[] tokenArray = tokens.toArray(Strings.EMPTY_STRING_ARRAY);
        return lattice(tokenArray);
    }

    static class TaggingIteratorAdapter implements Iterator<ScoredTagging<String>> {
        private final Iterator<ScoredObject<String[]>> mIt;
        private final List<String> mTokens;
        private final int mMaxResults;
        private int mResults = 0;
        TaggingIteratorAdapter(List<String> tokens, 
                               Iterator<ScoredObject<String[]>> it,
                               int maxResults) {
            mTokens = tokens;
            mIt = it;
            mMaxResults = maxResults;
        }
        public ScoredTagging<String> next() {
            ScoredObject<String[]> so = mIt.next();
            double score = so.score();
            String[] tags = so.getObject();
            List<String> tagList = Arrays.asList(tags);
            ++mResults;
            return new ScoredTagging<String>(mTokens,tagList,score);
        }
        public boolean hasNext() {
            return (mResults < mMaxResults) && mIt.hasNext();
        }
        public void remove() {
            mIt.remove();
        }
    }

    void unprunedSources(double[] sources, int[] survivors, double beam) {
        double best = sources[0];
        for (int i = 0; i < sources.length; ++i)
            if (sources[i] > best) 
                best = sources[i];
        int next = 0;
        for (int i = 0; i < sources.length; ++i)
            if (sources[i] + beam >= best)
                survivors[next++] = i;
        survivors[next] = -1;
    }

    private class Viterbi {
        private final String[] mEmissions;
        private final double[][] mLattice;
        private final int[][] mBackPts;
        Viterbi(String[] emissions) {
            mEmissions = emissions;
            HiddenMarkovModel hmm = mHmm;
            int numStates = hmm.stateSymbolTable().numSymbols();
            int numEmits = emissions.length;
            double[][] lattice = new double[numEmits][numStates];
            mLattice = lattice;
            int[][] backPts = new int[numEmits][numStates];
            mBackPts = backPts;
            if (emissions.length == 0) {
                return;
            }
            double[] emitLog2Probs = emitLog2Probs(emissions[0]);
            
            for (int stateId = 0; stateId < numStates; ++stateId) {
                lattice[0][stateId] 
                    = emitLog2Probs[stateId]
                    + hmm.startLog2Prob(stateId);
            }
        
            int[] unprunedSources = new int[numStates+1];
            for (int i = 1; i < numEmits; ++i) {
                double[] lastSlice = lattice[i-1];
                unprunedSources(lastSlice,unprunedSources,mLog2Beam);
                double[] emitLog2Probs2 = emitLog2Probs(emissions[i]);
                for (int targetId = 0; targetId < numStates; ++targetId) {
                    if (Double.NEGATIVE_INFINITY != emitLog2Probs2[targetId]) {
                        double best = Double.NEGATIVE_INFINITY;
                        int bk = 0; // default tag
                        for (int next = 0; unprunedSources[next] != -1; ++next) {
                            int sourceId = unprunedSources[next];
                            double est = lastSlice[sourceId]
                                + hmm.transitLog2Prob(sourceId,targetId);
                            if (est > best) {
                                best = est;
                                bk = sourceId;
                            }
                        }
                        lattice[i][targetId] 
                            = best + emitLog2Probs2[targetId];
                        backPts[i][targetId] = bk;
                    } else {
                        lattice[i][targetId] = Double.NEGATIVE_INFINITY;
                        backPts[i][targetId] = 0; // default tag
                    }
                }
            }
            // handles finals even if only one emission
            double[] lastColumn = lattice[numEmits-1];
            for (int i = 0; i < numStates; ++i)
                lastColumn[i] += hmm.endLog2Prob(i);
        }
            
        String[] bestStates() {
            HiddenMarkovModel hmm = mHmm;
            int numStates = hmm.stateSymbolTable().numSymbols();
            int numEmits = mEmissions.length;
            if (numEmits == 0) return Strings.EMPTY_STRING_ARRAY;
            int[][] backPts = mBackPts;
            double[][] lattice = mLattice;
        
            int[] bestStateIds = new int[numEmits];
            int bestStateId = 0;
            double[] lastCol = lattice[numEmits-1];
            for (int i = 1; i < numStates; ++i)
                if (lastCol[i] > lastCol[bestStateId])
                    bestStateId = i;
            bestStateIds[numEmits-1] = bestStateId;
            for (int i = numEmits; --i > 0; )
                bestStateIds[i-1] = backPts[i][bestStateIds[i]];
            String[] bestStates = new String[numEmits];
            SymbolTable st = hmm.stateSymbolTable();
            for (int i = 0; i < bestStates.length; ++i)
                bestStates[i] = st.idToSymbol(bestStateIds[i]);
            return bestStates;
        }
    }

    private class NBestIterator extends Iterators.Buffered<ScoredObject<String[]>> {
        private final Viterbi mViterbi;
        private final BoundedPriorityQueue<State> mPQ;
        NBestIterator(Viterbi vit, int maxSize) {
            mViterbi = vit;
            mPQ = new BoundedPriorityQueue<State>(ScoredObject.comparator(),
                                                  maxSize);
            String[] emissions = vit.mEmissions;
            int numStates = mHmm.stateSymbolTable().numSymbols();
            int numEmits = emissions.length;
            int lastEmitIndex = numEmits-1;
            for (int tagId = 0; tagId < numStates; ++tagId) {
                double contScore = vit.mLattice[lastEmitIndex][tagId];
                if (contScore > Double.NEGATIVE_INFINITY) {
                    double score = 0.0;
                    mPQ.offer(new State(lastEmitIndex,score,contScore,
                                        tagId,null));
                }
            }
        }
        @Override
        public ScoredObject<String[]> bufferNext() {
            int numTags = mHmm.stateSymbolTable().numSymbols();
            int numEmissions = mViterbi.mEmissions.length;
            int lastEmitIndex = numEmissions-1;
            while (!mPQ.isEmpty()) {
                State st = mPQ.poll();
                int emitIndex = st.emissionIndex();
                if (emitIndex == 0) {
                    mPQ.setMaxSize(mPQ.maxSize()-1);
                    return st.result(numEmissions);
                }
                String emission = mViterbi.mEmissions[emitIndex];
                int emitTagId = st.mTagId;
                double score = st.mScore;
                if (emitIndex == lastEmitIndex)
                    score += mHmm.endLog2Prob(emitTagId);
                int emitIndexMinus1 = emitIndex-1;
                // don't compile because only need one tagId
                double emitLog2Prob = mHmm.emitLog2Prob(emitTagId,emission);
                for (int tagId = 0; tagId < numTags; ++tagId) {
                    double nextScore = score
                        + mHmm.transitLog2Prob(tagId,emitTagId)
                        + emitLog2Prob;
                    double contScore 
                        = mViterbi.mLattice[emitIndexMinus1][tagId];
                    if (nextScore > Double.NEGATIVE_INFINITY
                        && contScore > Double.NEGATIVE_INFINITY)
                        mPQ.offer(new State(emitIndexMinus1,
                                            nextScore,
                                            contScore,
                                            tagId,st));
                }
            }
            return null;
        }
    }

    private final class State implements Scored {
        private final double mScore;
        private final double mContScore;
        private final int mTagId;
        private final State mPreviousState;
        private final int mEmissionIndex; // used outside
        State(int emissionIndex, double score, double contScore,
              int tagId, State previousState) {
            mEmissionIndex = emissionIndex;
            mScore = score;
            mContScore = contScore;
            mTagId = tagId;
            mPreviousState = previousState;
        }
        public int emissionIndex() {
            return mEmissionIndex;
        }
        public double score() {
            return mScore + mContScore;
        }
        ScoredObject<String[]> result(int numTags) {
            return new ScoredObject<String[]>(tags(numTags),score());
        }
        String[] tags(int numTags) {
            SymbolTable symTable = mHmm.stateSymbolTable();
            String[] tags = new String[numTags];
            State state = this; 
            for (int i = 0; i < numTags; ++i) {
                tags[i] = symTable.idToSymbol(state.mTagId);
                state = state.mPreviousState;
            }
            return tags;
        }
    }

    private static final class JointIterator extends Iterators.Modifier<ScoredObject<String[]>> {
        final double mLog2TotalProb;
        JointIterator(Iterator<ScoredObject<String[]>> nBestIterator, double log2TotalProb) {
            super(nBestIterator);
            mLog2TotalProb = log2TotalProb;
        }
        @Override
        public ScoredObject<String[]> modify(ScoredObject<String[]> jointObj) {
            String[] tags = jointObj.getObject();
            double log2JointProb = jointObj.score();
            double log2CondProb = log2JointProb - mLog2TotalProb;
            return new ScoredObject<String[]>(tags,log2CondProb);
        }
    }
}
    
