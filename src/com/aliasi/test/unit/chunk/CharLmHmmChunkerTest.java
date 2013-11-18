package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.CharLmHmmChunker;

import com.aliasi.hmm.AbstractHmmEstimator;
import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;


public class CharLmHmmChunkerTest {

    @Test
    public void testDictionaryTraining() throws IOException, ClassNotFoundException {
        TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;
        AbstractHmmEstimator estimator
            = new HmmCharLmEstimator(5,128,5.0,false); // don't smooth HMM directly
        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,estimator,true); // do smooth chunker

        chunkerEstimator.trainDictionary("John Smith","PERSON");
        chunkerEstimator.trainDictionary("Washington, D.C.","LOCATION");
        chunkerEstimator.trainDictionary("Alias-i","ORGANIZATION");

        // prints out start/end/transitions
        // System.out.println("chunkerEstimator\n" + chunkerEstimator.toString());

        Chunking chunking
            = chunkerEstimator.chunk("John Smith");
        assertEquals(1,chunking.chunkSet().size());
        Chunk chunk = chunking.chunkSet().iterator().next();
        assertEquals(0,chunk.start());
        assertEquals(10,chunk.end());
        assertEquals("PERSON",chunk.type());
    }


    @Test
    public void testDefectiveTraining() throws IOException, ClassNotFoundException {
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        AbstractHmmEstimator estimator
            = new HmmCharLmEstimator(5,128,5.0);
        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,estimator);
        assertEquals(new ChunkingImpl("hello"),
                     chunkerEstimator.chunk("hello"));

        assertChunkingCompile(chunkerEstimator,
                              new ChunkingImpl(""));

        assertChunkingCompile(chunkerEstimator,
                              new ChunkingImpl("hello"));

        assertChunkingCompile(chunkerEstimator,
                              new ChunkingImpl("John Smith lives in Washington."));



    }

    @Test
    public void testDefectiveTags() throws IOException, ClassNotFoundException {
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        AbstractHmmEstimator estimator
            = new HmmCharLmEstimator(5,128,5.0);
        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,estimator);

        String text1 = "John Smith lives in Washington.";
        //              0123456789012345678901234567890
        //              0         1         2         3
        ChunkingImpl chunking1 = new ChunkingImpl(text1);
        Chunk chunk11 = ChunkFactory.createChunk(0,10,"PER");
        Chunk chunk12 = ChunkFactory.createChunk(20,30,"LOC");
        chunking1.add(chunk11);
        chunking1.add(chunk12);
        // no middle tag or whole tag given for PERSON
        // no begin, middle end tag for LOCATION

        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking1);

        assertChunkingCompile(chunkerEstimator,chunking1);
    }

    @Test
    public void testDefectiveTags2() throws IOException, ClassNotFoundException {
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        AbstractHmmEstimator estimator
            = new HmmCharLmEstimator(5,128,5.0);
        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,estimator);

        String text1 = "John";
        //              012345
        //              0
        ChunkingImpl chunking1 = new ChunkingImpl(text1);
        Chunk chunk11 = ChunkFactory.createChunk(0,4,"PER");
        chunking1.add(chunk11);
        // no middle tag or whole tag given for PERSON
        // no begin, middle end tag for LOCATION

        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking1);

        assertChunkingCompile(chunkerEstimator,chunking1);
    }


    @Test
    public void testChunkHandler() throws IOException, ClassNotFoundException {
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        AbstractHmmEstimator estimator
            = new HmmCharLmEstimator(5,128,5.0);

        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,estimator);

        String text1 = "John J. Smith lives in Washington.";
        //              0123456789012345678901234567890123
        //              0         1         2         3
        ChunkingImpl chunking1 = new ChunkingImpl(text1);
        Chunk chunk11 = ChunkFactory.createChunk(0,13,"PER");
        Chunk chunk12 = ChunkFactory.createChunk(23,33,"LOC");
        chunking1.add(chunk11);
        chunking1.add(chunk12);

        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking1);

        assertChunkingCompile(chunkerEstimator,chunking1);

        String text2 = "Washington is near John";
        //              01234567890123456789012
        //              0         1         2
        ChunkingImpl chunking2 = new ChunkingImpl(text2);
        Chunk chunk21 = ChunkFactory.createChunk(0,10,"LOC");
        Chunk chunk22 = ChunkFactory.createChunk(19,23,"PER");
        chunking2.add(chunk21);
        chunking2.add(chunk22);

        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking2);

        assertChunkingCompile(chunkerEstimator,chunking2);


        String text3 = "Washington D.C. is near Frank Jones.";
        //              012345678901234567890123456789012345
        //              0         1         2         3
        ChunkingImpl chunking3 = new ChunkingImpl(text3);
        Chunk chunk31 = ChunkFactory.createChunk(0,15,"LOC");
        Chunk chunk32 = ChunkFactory.createChunk(24,36,"PER");
        chunking3.add(chunk31);
        chunking3.add(chunk32);

        for (int i = 0; i < 10; ++i)
            chunkerEstimator.handle(chunking3);

        assertChunkingCompile(chunkerEstimator,chunking3);

    }

    public static <C extends Chunker & com.aliasi.util.Compilable> 
        void assertChunkingCompile(C chunkerEstimator,
                                   Chunking expectedChunking) throws IOException, ClassNotFoundException {
        
        assertChunking(chunkerEstimator,
                       expectedChunking);
        assertChunking((Chunker) AbstractExternalizable.compile(chunkerEstimator),
                       expectedChunking);
    }

    public static void assertChunking(Chunker chunker,
                                      Chunking expectedChunking) {

        CharSequence text = expectedChunking.charSequence();
        Chunking chunking = chunker.chunk(text);
        assertEqualsChunking(expectedChunking,chunking);
    }

    public static void assertEqualsChunking(Chunking expectedChunking, Chunking chunking) {
        assertEquals(expectedChunking.charSequence(),
                     chunking.charSequence());
        assertEquals(expectedChunking.chunkSet(),
                     chunking.chunkSet());
    }

}
