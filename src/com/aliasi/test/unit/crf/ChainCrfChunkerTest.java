package com.aliasi.test.unit.crf;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.symbol.SymbolTable;
import com.aliasi.symbol.SymbolTableCompiler;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ForwardBackwardTagLattice;

import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import static org.junit.Assert.assertArrayEquals;

import static com.aliasi.test.unit.Asserts.succeed;

public class ChainCrfChunkerTest {


    @Test
    public void test1() throws IOException {
        boolean addIntercept = false;
        int minFeatureCount = 1;
        boolean cacheFeatureVectors = false;
        double minImprovement = 0.00000001;
        int minEpochs = 10;
        int maxEpochs = 10000;
        int priorBlockSize = 3;
        Reporter reporter
            = Reporters.stdOut();
        reporter.setLevel(LogLevel.WARN);

        ChainCrfChunker chunker
            = ChainCrfChunker.estimate(TrainCorpus1.INSTANCE,
                                       TAG_CHUNK_CODEC,
                                       IndoEuropeanTokenizerFactory.INSTANCE,
                                       ChainCrfTest.FEATURE_EXTRACTOR,
                                       addIntercept,
                                       minFeatureCount,
                                       cacheFeatureVectors,
                                       RegressionPrior.noninformative(), // laplace(20,addIntercept),
                                       priorBlockSize,
                                       AnnealingSchedule.exponential(0.005,0.9999),
                                       minImprovement,
                                       minEpochs,
                                       maxEpochs,
                                       reporter);

        assertNotNull(chunker);

        ChainCrf<String> crf = chunker.crf();

        String test = "John likes New York City.";
        Chunking chunking = chunker.chunk(test);
        assertEquals(chunking("John likes New York City.",
                            // 0123456789012345678901234
                              chunk(0,4,"PER"),
                              chunk(11,24,"LOC")),
                     chunking);
    }

    static final TagChunkCodec TAG_CHUNK_CODEC
        = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                               true);

    static Chunking chunking(String s, Chunk... chunks) {
        ChunkingImpl chunking = new ChunkingImpl(s);
        for (Chunk chunk : chunks)
            chunking.add(chunk);
        return chunking;
    }
    static Chunk chunk(int start, int end, String type) {
        return ChunkFactory.createChunk(start,end,type);
    }

    static class TrainCorpus1 extends Corpus<ObjectHandler<Chunking>> {
        static final Corpus<ObjectHandler<Chunking>> INSTANCE
            = new TrainCorpus1();
        static final Chunking[] TRAIN_CHUNKINGS
            = new Chunking[] {
            chunking(""),
            chunking("The"),
            chunking("John ran.",
                     chunk(0,4,"PER")),
            chunking("Mary ran.",
                     chunk(0,4,"PER")),
            chunking("The kid ran."),
            chunking("John likes Mary.",
                   // 0123456789012345
                     chunk(0,4,"PER"),
                     chunk(11,15,"PER")),
            chunking("Tim lives in Washington",
                   // 012345678901234567890123456789
                     chunk(0,3,"PER"),
                     chunk(13,23,"LOC")),
            chunking("Mary Smith is in New York City",
                   // 0123456789012345678901234567890
                     chunk(0,10,"PER"),
                     chunk(17,30,"LOC")),
            chunking("New York City is fun",
                   // 012345678901234567890123456789
                     chunk(0,13,"LOC")),
            chunking("Chicago is not like Washington",
                   // 0123456789012345678901234567890
                     chunk(0,7,"LOC"),
                     chunk(20,30,"LOC"))
        };
        public void visitTrain(ObjectHandler<Chunking> handler) {
            for (Chunking chunking : TRAIN_CHUNKINGS)
                handler.handle(chunking);
        }
        public void visitTest(ObjectHandler<Chunking> handler) {
            /* no op */
        }
    }



}