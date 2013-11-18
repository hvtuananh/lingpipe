
package com.aliasi.test.unit.chunk;



import com.aliasi.chunk.*;
import com.aliasi.tokenizer.*;

import com.aliasi.util.AbstractExternalizable;

import static com.aliasi.test.unit.chunk.CharLmHmmChunkerTest.assertChunking;

import org.junit.Test;



public class TrainTokenShapeChunkerTest {


    @Test
    public void testOne() throws Exception {
        IndoEuropeanTokenizerFactory factory 
            = IndoEuropeanTokenizerFactory.INSTANCE;
        IndoEuropeanTokenCategorizer categorizer
            = IndoEuropeanTokenCategorizer.CATEGORIZER;
        TrainTokenShapeChunker trainer
            = new TrainTokenShapeChunker(categorizer,factory);
        String text1 = "John J. Smith lives in Washington.";
        //              0123456789012345678901234567890123
        //              0         1         2         3
        ChunkingImpl chunking1 = new ChunkingImpl(text1);
        Chunk chunk11 = ChunkFactory.createChunk(0,13,"PER");
        Chunk chunk12 = ChunkFactory.createChunk(23,33,"LOC");
        chunking1.add(chunk11);
        chunking1.add(chunk12);
    
        for (int i = 0; i < 5; ++i)
            trainer.handle(chunking1);

        Chunker chunker 
            = (Chunker) AbstractExternalizable.compile(trainer);
    
        assertChunking(chunker,chunking1);

        String text2 = "blah blah blah" + text1 + "blah blah.";
        char[] cs = text2.toCharArray();
        int start = "blah blah blah".length();
        Chunking resultChunking2 
            = chunker.chunk(cs,start,start + text1.length());
        assertChunking(chunker,resultChunking2);

    }

}
