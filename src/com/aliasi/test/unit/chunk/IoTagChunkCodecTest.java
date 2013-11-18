package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.IoTagChunkCodec;
import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.crf.ForwardBackwardTagLattice;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Scored;

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import static com.aliasi.test.unit.Asserts.succeed;

public class IoTagChunkCodecTest {

    @Test(expected=UnsupportedOperationException.class)
    public void testNBestChunks() {
        TagChunkCodec codec
            = new IoTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);
        TagLattice<String> lattice
            = new ForwardBackwardTagLattice<String>(Collections.<String>emptyList(),
                                                    Collections.<String>emptyList(),
                                                    new double[0][0],
                                                    new double[0][0],
                                                    new double[0][0][0],
                                                    0.0);
        Iterator<Chunk> it = codec.nBestChunks(lattice,new int[0],new int[0],100);
    }

    @Test
    public void testLegalTagSubSequence() {
        TagChunkCodec codec
            = new IoTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);

        assertTrue(codec.legalTagSubSequence("O"));
        assertTrue(codec.legalTagSubSequence("PER"));
        assertTrue(codec.legalTagSubSequence("LOC"));
        assertTrue(codec.legalTagSubSequence("O","PER"));
        assertTrue(codec.legalTagSubSequence("PER","O"));
        assertTrue(codec.legalTagSubSequence("PER","PER"));
        assertTrue(codec.legalTagSubSequence("PER","LOC"));
        assertTrue(codec.legalTagSubSequence("O","PER","PER", "PER", "ORG", "O"));
    }

    @Test
    public void testLegalTags() {
        TagChunkCodec codec
            = new IoTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);

        assertTrue(codec.legalTags("O"));
        assertTrue(codec.legalTags("PER"));
        assertTrue(codec.legalTags("LOC"));
        assertTrue(codec.legalTags("O","PER"));
        assertTrue(codec.legalTags("PER","O"));
        assertTrue(codec.legalTags("PER","PER"));
        assertTrue(codec.legalTags("PER","LOC"));
        assertTrue(codec.legalTags("O","PER","PER", "PER", "ORG", "O"));
    }



    @Test
    public void testBioCodecTagSet() {
        TagChunkCodec codec
            = new IoTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                   false);
        Set<String> chunkTypes
            = new HashSet<String>(Arrays.asList("PER","LOC"));
        Set<String> expectedTags =
            new HashSet<String>(Arrays.asList("O","PER","LOC"));
        assertEquals(expectedTags,codec.tagSet(chunkTypes));
    }


    @Test
    public void testEncodable()
        throws IOException, ClassNotFoundException {

        TagChunkCodec codec
            = new IoTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                  true); // don't need to check errors to test
        //John Jones Mary and Mr. J. J. Jones ran to Washington.
        //012345678901234567890123456789012345678901234567890123456789
        //0         1         2         3         4         5
        ChunkingImpl chunkingOk
            = new ChunkingImpl("John Jones Mary and Mr. J. J. Jones ran to Washington.");
        assertEncodable(codec,chunkingOk); // non-adjacent chunks
        Chunk chunk1 = ChunkFactory.createChunk(0,10,"PER");
        Chunk chunk2 = ChunkFactory.createChunk(11,15,"PER");
        Chunk chunk3 = ChunkFactory.createChunk(24,35,"PER");
        Chunk chunk4 = ChunkFactory.createChunk(43,53,"LOC");
        chunkingOk.add(chunk4);
        assertEncodable(codec,chunkingOk); 
        chunkingOk.add(chunk3);
        assertEncodable(codec,chunkingOk); 
        chunkingOk.add(chunk1);
        assertEncodable(codec,chunkingOk); 
        chunkingOk.add(chunk2);            // adjacent chunks
        assertNotEncodable(codec,chunkingOk); 

        ChunkingImpl chunkingBad
            = new ChunkingImpl("John ran");
        //                      012345678
        Chunk chunk2_1 = ChunkFactory.createChunk(0,4,"PER");
        Chunk chunk2_2 = ChunkFactory.createChunk(0,8,"LOC");
        chunkingBad.add(chunk2_1);
        chunkingBad.add(chunk2_2);
        assertNotEncodable(codec,chunkingBad);

        ChunkingImpl chunkingBad3
            = new ChunkingImpl("John ran");
        Chunk chunk3_1 = ChunkFactory.createChunk(0,5,"PER");
        chunkingBad3.add(chunk3_1);
        assertNotEncodable(codec,chunkingBad3);

        ChunkingImpl chunkingBad4
            = new ChunkingImpl("John ran");
        Chunk chunk4_1 = ChunkFactory.createChunk(1,4,"PER");
        chunkingBad4.add(chunk4_1);
        assertNotEncodable(codec,chunkingBad4);
        assertNotEncodable(codec,chunkingBad4);

        ChunkingImpl chunkingBad5
            = new ChunkingImpl("John ran");
        Chunk chunk5_1 = ChunkFactory.createChunk(5,5,"LOC");
        chunkingBad5.add(chunk5_1);
        assertNotEncodable(codec,chunkingBad5);

        ChunkingImpl chunkingOk2
            = new ChunkingImpl("John ran");
        assertTrue(codec.isEncodable(chunkingOk2));
        Chunk chunk6_1 = ChunkFactory.createChunk(0,8,"LOC");
        chunkingOk2.add(chunk6_1);
        assertEncodable(codec,chunkingOk2);

        ChunkingImpl chunkingOk3
            = new ChunkingImpl("Mr. John Jones ran to Washington.");
        //                      0123456789012345678901234567890123456789
        //                      0         1         2         3
        Chunk jj = ChunkFactory.createChunk(4,14,"PER");
        Chunk w = ChunkFactory.createChunk(22,32,"LOC");
        chunkingOk3.add(jj);
        chunkingOk3.add(w);
        assertEncodable(codec,chunkingOk3);
    }

    @Test
    public void testDecodable()
        throws IOException, ClassNotFoundException {

        TagChunkCodec codec
            = new IoTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE,
                                  true); // don't need to check errors to test

        StringTagging taggingOk
            = new StringTagging(Arrays.asList("John","ran","to","Washington","DC"),
                                Arrays.asList("PER","O","O","LOC","LOC"),
                                "John ran to Washington DC",
                                //1234567890123456789012345
                                new int[] { 0, 5, 9, 12, 23 },
                                new int[] { 4, 8, 11, 22, 25 });
        assertDecodable(codec,taggingOk);

        StringTagging taggingBad
            = new StringTagging(Arrays.asList("John","ny","ran","."),
                                Arrays.asList("PER","PER","O","O"),
                                "Johnny ran.",
                               //01234567890
                                new int[] { 0, 4, 7, 10 },
                                new int[] { 4, 6, 10, 11 });
        assertNotDecodable(codec,taggingBad);


    }


    void assertEncodable(TagChunkCodec codec,
                         Chunking chunking)
        throws IOException, ClassNotFoundException {

        assertEncodable2(codec,chunking);

        @SuppressWarnings("unchecked")
        TagChunkCodec codec2
            = (TagChunkCodec)
            AbstractExternalizable
            .serializeDeserialize((Serializable) codec);
        assertEncodable2(codec2,chunking);
    }

    void assertEncodable2(TagChunkCodec codec,
                          Chunking chunking) {
        assertTrue(codec.isEncodable(chunking));

        StringTagging tagging = codec.toStringTagging(chunking);
        assertTrue(codec.isDecodable(tagging));

        Chunking chunking2 = codec.toChunking(tagging);
        assertEquals(chunking,chunking2);

        Tagging tagging2 = codec.toStringTagging(chunking2);
        assertEquals(tagging,tagging2);
    }

    void assertNotEncodable(TagChunkCodec codec,
                            Chunking chunking) {
        assertFalse(codec.isEncodable(chunking));

        try {
            codec.toTagging(chunking);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    void assertDecodable(TagChunkCodec codec,
                         StringTagging tagging)
        throws IOException, ClassNotFoundException {

        assertDecodable2(codec,tagging);

        @SuppressWarnings("unchecked")
        TagChunkCodec codec2
            = (TagChunkCodec)
            AbstractExternalizable
            .serializeDeserialize((Serializable) codec);
        assertDecodable2(codec2,tagging);
    }

    void assertDecodable2(TagChunkCodec codec,
                          StringTagging tagging) {
        assertTrue(codec.isDecodable(tagging));

        Chunking chunking = codec.toChunking(tagging);
        assertTrue(codec.isEncodable(chunking));

        StringTagging tagging2 = codec.toStringTagging(chunking);
        assertEquals(tagging,tagging2);

        Chunking chunking2 = codec.toChunking(tagging2);
        assertEquals(chunking,chunking2);
    }

    void assertNotDecodable(TagChunkCodec codec,
                            StringTagging tagging) {
        assertFalse(codec.isDecodable(tagging));
        try {
            codec.toChunking(tagging);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }


}

