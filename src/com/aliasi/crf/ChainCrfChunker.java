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

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.chunk.NBestChunker;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@code ChainCrfChunker} implements chunking based on a chain CRF
 * over string sequences, a tokenizer factory, and a tag to chunk
 * coder/decoder.
 *
 * <p>The tokenizer factory is used to turn an input sequence into
 * a list of tokens.  The codec is used to convert taggings into
 * chunkings and vice-versa.
 *
 * <h3>Codec-Based Features</h3>
 *
 * <p>For chunking, feature extraction is over the same two implicit
 * data structures as for chain CRFs, nodes and edges.  For chunkers,
 * the labels are coded and decoded by an instance of {@link
 * TagChunkCodec}, such as the BIO-based codec.  In order to generate
 * token-based representations on which to hang tags, an instance of
 * {@link TokenizerFactory} is supplied in the chunker constructor.
 *
 * <h3>Training</h3>
 *
 * The static {@code estimate()} method is used to train a chain
 * CRF-based chunker.  The training data is provided as a corpus of
 * chunkings.  The tag-chunk codec and tokenizer factory are then used
 * to convert the chunkings to taggings, and the resulting tag corpus
 * passed off to the chain CRF estimator method.  Feature extractors
 * are the same as for a chain CRF, with one for nodes and one for
 * edges.  The tags passed in to these feature extractors will be
 * determiend by the tag-chunk codec.  The remaining inputs are
 * identical to those for chain CRFs; see the method documentation for
 * more information.
 *
 * <h3>Decoding</h3>
 *
 * A chain CRF chunker implements all three chunker interfaces in
 * order to return first-best chunkings, n-best chunkings (with or
 * without normalization of scores to conditional probabilities), and
 * to iterate over the n-best chunks in decreasing order of
 * probability.
 *
 * <h3>Serialization</h3>
 *
 * Chain CRF chunkers are serializable if their contained tokenizer
 * factories and codecs are serializable.  The chunker read back in
 * will be of this class, {@code ChainCrfChunker}, with components
 * derived from serialization and deserialization.
 *
 * <h3>Thread Safety</h3>
 *
 * The chain CRF chunker class is thread safe if the tokenizer
 * factory and tag/chunk coder/decoder are thread safe.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 */
public class ChainCrfChunker
    implements Chunker,
               ConfidenceChunker,
               NBestChunker,
               Serializable {

    static final long serialVersionUID = -2244399751558084581L;

    private final ChainCrf<String> mCrf;
    private final TokenizerFactory mTokenizerFactory;
    private final TagChunkCodec mCodec;

    /**
     * Construct a chunker based on the specified chain conditional
     * random field, tokenizer factory and tag-chunk coder/decoder.  If
     * the codec requires a tokenizer factory, it should be the same
     * one as supplied to this chunker constructor.
     *
     * @param crf Underlying conditional random field.
     * @param tokenizerFactory Tokenizer factory for converting chunkings
     * to token sequences.
     * @param codec Coder/decoder for converting taggings to chunkings
     * and vice-versa.
     */
    public ChainCrfChunker(ChainCrf<String> crf,
                           TokenizerFactory tokenizerFactory,
                           TagChunkCodec codec) {
        mCrf = crf;
        mTokenizerFactory = tokenizerFactory;
        mCodec = codec;
    }

    /**
     * Returns the underlying CRF for this chunker.
     *
     * @return CRF for this chunker.
     */
    public ChainCrf<String> crf() {
        return mCrf;
    }

    /**
     * Returns the tag/chunk coder/decoder for this chunker.
     *
     * @return The tag chunk codec for this chunker.
     */
    public TagChunkCodec codec() {
        return mCodec;
    }

    /**
     * Return the tokenizer factory for this chunker.
     *
     * @return The tokenizer factory for this chunker.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Return a string-based representation of this CRF chunker.
     *
     * @return String representation of this chunker.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TagChunkCodec=" + codec());
        sb.append("\n");
        sb.append("Tokenizer Factory=" + tokenizerFactory());
        sb.append("\n");
        sb.append("CRF=\n");
        sb.append(crf().toString());
        return sb.toString();
    }

    public Chunking chunk(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return chunk(cs,0,cs.length);
    }

    public Chunking chunk(char[] cs, int start, int end) {
        PreTagging preTagging = preTag(cs,start,end);
        List<String> tokens = preTagging.mTokens;
        Tagging<String> tagging = mCrf.tag(tokens);
        return toChunking(tagging,preTagging,cs,start,end,mCodec);
    }


    public Iterator<ScoredObject<Chunking>> nBest(char[] cs, int start, int end, int maxResults) {
        PreTagging preTagging = preTag(cs,start,end);
        List<String> tokens = preTagging.mTokens;
        Iterator<ScoredTagging<String>> it
            = mCrf.tagNBest(tokens,maxResults);
        return new IteratorWrapper(it,preTagging,cs,start,end,mCodec);
    }


    /**
     * Returns an iterator over n-best chunkings with scores
     * normalized to conditional probabilities of the output given the
     * input string slice.  The same chunkings will be returned in the
     * same order as for the unnormalized method, {@link
     * #nBest(char[],int,int,int)}.  Like that method, the maximum number
     * of results parameter should be set as low as practical, as it
     * cuts down on memory requirement for outputs that will never be
     * returned.
     *
     * <p>Conditional probability normalization requires an additional
     * forward-backward pass to derive the normalizing factor, but the
     * benefit is that results become comparable across input strings.
     *
     * @param cs Underlying characters.
     * @param start First character in slice.
     * @param end One past the last character in the slice.
     * @param maxResults Maximum number of results to return.
     */
    public Iterator<ScoredObject<Chunking>>
        nBestConditional(char[] cs, int start, int end, int maxResults) {

        PreTagging preTagging = preTag(cs,start,end);
        List<String> tokens = preTagging.mTokens;
        Iterator<ScoredTagging<String>> it
            = mCrf.tagNBestConditional(tokens,maxResults);
        return new IteratorWrapper(it,preTagging,cs,start,end,mCodec);
    }


    public Iterator<Chunk> nBestChunks(char[] cs, int start, int end, int maxNBest) {
        PreTagging preTagging = preTag(cs,start,end);
        List<String> tokens = preTagging.mTokens;
        TagLattice<String> lattice = mCrf.tagMarginal(tokens);
        return mCodec.nBestChunks(lattice,
                                  preTagging.mTokenStarts,
                                  preTagging.mTokenEnds,
                                  maxNBest);
    }

    PreTagging preTag(char[] cs, int start, int end) {
        List<Integer> tokenStarts = new ArrayList<Integer>();
        List<Integer> tokenEnds = new ArrayList<Integer>();
        List<String> tokens = new ArrayList<String>();
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        String token;
        while ((token = tokenizer.nextToken()) != null) {
            tokens.add(token);
            tokenStarts.add(tokenizer.lastTokenStartPosition());
            tokenEnds.add(tokenizer.lastTokenEndPosition());
        }
        return new PreTagging(tokens,
                              toArray(tokenStarts),
                              toArray(tokenEnds));
    }

    Object writeReplace() {
        return new Serializer(this);
    }


    static final boolean ALLOW_UNSEEN_TAG_TRANSITIONS = false;

    /**
     * Return the chain CRF-based chunker estimated from the specified
     * corpus, which is converted to a tagging corpus using the
     * specified coder/decoder and tokenizer factory, then passed to
     * the chain CRF estimate method along with the rest of the
     * arguments.
     *
     * <p>Estimation is based on regularized stochastic gradient
     * descent.  See {@link
     * ChainCrf#estimate(Corpus,ChainCrfFeatureExtractor,boolean,int,boolean,boolean,RegressionPrior,int,AnnealingSchedule,double,int,int,Reporter)}
     * for more information.
     *
     * @param chunkingCorpus Training corpus of chunkings.
     * @param codec Coder/decoder for translating chunkings to
     * taggings and vice-versa.
     * @param tokenizerFactory Tokenizer factory for converting inputs to
     * token sequences for the underlying chain CRF.
     * @param featureExtractor Feature extractor for the underlying chain CRF.
     * @param addInterceptFeature Set to {@code true} to automatically add an
     * intercept feature with constant value 1.0 in position 0.
     * @param minFeatureCount Minimum number of times a feature must
     * show up in the tagging corpus given the feature extractors to
     * be retained for training.
     * @param cacheFeatureVectors Flag indicating whether or not to cache
     * extracted features. 
     * @param prior Prior to use to regularize the underlying chain
     * CRF estimates.
     * @param priorBlockSize Number of instances to update by gradeint
     * for every prior update.
     * @param annealingSchedule Annealing schedule to determine
     * learning rates for stochastic gradient descent training.
     * @param minImprovement Minimum improvement in epoch to terminate training (computed
     * with a rolling average).
     * @param minEpochs Minimum number of epochs for which to train.
     * @param maxEpochs Maximum nubmer of epochs for which to train.
     * @param reporter Reporter to which reports of training are sent, or
     * {@code null} for silent operation.
     * @throws IOException If there is an underlying I/O exception reading
     * the corpus.
     */
    public static ChainCrfChunker estimate(Corpus<ObjectHandler<Chunking>> chunkingCorpus,
                                           TagChunkCodec codec,
                                           TokenizerFactory tokenizerFactory,
                                           ChainCrfFeatureExtractor<String> featureExtractor,
                                           boolean addInterceptFeature,
                                           int minFeatureCount,
                                           boolean cacheFeatureVectors,
                                           RegressionPrior prior,
                                           int priorBlockSize,
                                           AnnealingSchedule annealingSchedule,
                                           double minImprovement,
                                           int minEpochs,
                                           int maxEpochs,
                                           Reporter reporter) throws IOException {
        if (reporter == null)
            reporter = Reporters.silent();
        reporter.info("Training chain CRF chunker");
        reporter.info("Converting chunk corpus to tag corpus using codec.");
        Corpus<ObjectHandler<Tagging<String>>> taggingCorpus
            = new TagCorpus(chunkingCorpus,codec);
        ChainCrf<String> crf
            = ChainCrf.estimate(taggingCorpus,
                                featureExtractor,
                                addInterceptFeature,
                                minFeatureCount,
                                cacheFeatureVectors,
                                ALLOW_UNSEEN_TAG_TRANSITIONS,
                                prior,
                                priorBlockSize,
                                annealingSchedule,
                                minImprovement,
                                minEpochs,
                                maxEpochs,
                                reporter);
        return new ChainCrfChunker(crf,tokenizerFactory,codec);
    }

    static Chunking toChunking(Tagging<String> tagging, PreTagging preTagging,
                               char[] cs, int start, int end, TagChunkCodec codec) {

        String s = new String(cs,start,end-start);
        List<String> tokens = preTagging.mTokens;
        int[] tokenStarts = preTagging.mTokenStarts;
        int[] tokenEnds = preTagging.mTokenEnds;
        List<String> tags = tagging.tags();

        StringTagging stringTagging
            = new StringTagging(tokens,tags,s,tokenStarts,tokenEnds);
        return codec.toChunking(stringTagging);
    }

    static int[] toArray(List<Integer> xs) {
        int len = xs.size();
        int[] ys = new int[len];
        for (int i = 0; i < len; ++i)
            ys[i] = xs.get(i);
        return ys;
    }

    static class PreTagging {
        final List<String> mTokens;
        final int[] mTokenStarts;
        final int[] mTokenEnds;
        public PreTagging(List<String> tokens,
                          int[] tokenStarts,
                          int[] tokenEnds) {
            mTokens = tokens;
            mTokenStarts = tokenStarts;
            mTokenEnds = tokenEnds;
        }
    }

    static class ChunkingAdapter implements ObjectHandler<Chunking> {
        private final ObjectHandler<Tagging<String>> mTagHandler;
        private final TagChunkCodec mCodec;
        public ChunkingAdapter(ObjectHandler<Tagging<String>> tagHandler,
                               TagChunkCodec codec) {
            mTagHandler = tagHandler;
            mCodec = codec;
        }
        public void handle(Chunking chunking) {
            Tagging<String> tagging = mCodec.toTagging(chunking);
            mTagHandler.handle(tagging);
        }
    }

    static class TagCorpus extends Corpus<ObjectHandler<Tagging<String>>> {
        private final Corpus<ObjectHandler<Chunking>> mChunkingCorpus;
        private final TagChunkCodec mCodec;
        public TagCorpus(Corpus<ObjectHandler<Chunking>> chunkingCorpus,
                         TagChunkCodec codec) {
            mChunkingCorpus = chunkingCorpus;
            mCodec = codec;
        }
        public void visitTrain(ObjectHandler<Tagging<String>> handler)
            throws IOException {

            mChunkingCorpus.visitTrain(new ChunkingAdapter(handler,mCodec));
        }
        public void visitTest(ObjectHandler<Tagging<String>> handler)
            throws IOException {

            mChunkingCorpus.visitTest(new ChunkingAdapter(handler,mCodec));
        }
    }

    static class IteratorWrapper implements Iterator<ScoredObject<Chunking>> {
        private final Iterator<ScoredTagging<String>> mIt;
        private final PreTagging mPreTagging;
        private final char[] mCs;
        private final int mStart;
        private final int mEnd;
        private final TagChunkCodec mCodec;
        IteratorWrapper(Iterator<ScoredTagging<String>> it,
                        PreTagging preTagging,
                        char[] cs, int start, int end,
                        TagChunkCodec codec) {
            mIt = it;
            mPreTagging = preTagging;
            mCs = cs;
            mStart = start;
            mEnd = end;
            mCodec = codec;
        }
        public boolean hasNext() {
            return mIt.hasNext();
        }
        public void remove() {
            mIt.remove();
        }
        public ScoredObject<Chunking> next() {
            ScoredTagging<String> tagging = mIt.next();
            double score = tagging.score();
            Chunking chunking
                = toChunking(tagging,mPreTagging,mCs,mStart,mEnd,mCodec);
            return new ScoredObject<Chunking>(chunking,score);
        }
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 2460314741682974199L;
        private final ChainCrfChunker mChunker;
        public Serializer() {
            this(null);
        }
        public Serializer(ChainCrfChunker chunker) {
            mChunker = chunker;
        }
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            ChainCrf<String> crf = (ChainCrf<String>) in.readObject();
            @SuppressWarnings("unchecked")
            TokenizerFactory factory = (TokenizerFactory) in.readObject();
            @SuppressWarnings("unchecked")
                TagChunkCodec codec = (TagChunkCodec) in.readObject();
            return new ChainCrfChunker(crf,factory,codec);

        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mChunker.mCrf);
            out.writeObject(mChunker.mTokenizerFactory);
            out.writeObject(mChunker.mCodec);
        }
    }


}


