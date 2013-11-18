package com.aliasi.test.unit.features;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.features.ChunkerFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import static com.aliasi.test.unit.features.MockFeatureExtractor.assertFeats;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.io.NotSerializableException;

import java.util.Map;

public class ChunkerFeatureExtractorTest {

    @Test
    public void testNonPhrasal() throws IOException {

        ChunkerFeatureExtractor extractor
            = new ChunkerFeatureExtractor(new MockChunker(),
                                          false);

        @SuppressWarnings("unchecked")
        FeatureExtractor<CharSequence> extractorDeser
            = (FeatureExtractor<CharSequence>)
            AbstractExternalizable.serializeDeserialize(extractor);

        Map<String,? extends Number> features
            = extractor.features("1");
        assertTrue(features.isEmpty());

        features = extractorDeser.features("1");
        assertTrue(features.isEmpty());

        features = extractor.features("2");
        assertEquals(2,features.size());
        assertEquals(1.0,features.get("LOC"));
        assertEquals(2.0,features.get("PER"));

        features = extractorDeser.features("2");
        assertEquals(2,features.size());
        assertEquals(1.0,features.get("LOC"));
        assertEquals(2.0,features.get("PER"));
   
        
    }

    @Test
    public void testPhrasal() throws IOException {

        ChunkerFeatureExtractor extractor
            = new ChunkerFeatureExtractor(new MockChunker(),
                                          true);

        @SuppressWarnings("unchecked")
        FeatureExtractor<CharSequence> extractorDeser
            = (FeatureExtractor<CharSequence>)
            AbstractExternalizable.serializeDeserialize(extractor);

        Map<String,? extends Number> features
            = extractor.features("1");
        assertTrue(features.isEmpty());

        features = extractorDeser.features("1");
        assertTrue(features.isEmpty());

        features = extractor.features("2");
        assertEquals(2,features.size());
        assertEquals(1.0,features.get("LOC_Washington"));
        assertEquals(2.0,features.get("PER_John"));

        features = extractorDeser.features("2");
        assertEquals(2,features.size());
        assertEquals(1.0,features.get("LOC_Washington"));
        assertEquals(2.0,features.get("PER_John"));
    }

    static class MockChunker implements Chunker, Serializable {
        public Chunking chunk(char[] cs, int start, int end) {
            return chunk(new String(cs,start,end-start));
        }
        public Chunking chunk(CharSequence in) {
            if (in.equals("1"))
                return chunking("John ran");
            else if (in.equals("2"))
                return chunking("John met John in Washington.",
                              // 01234567890123456789012345678
                                ChunkFactory.createChunk(0,4,"PER"),
                                ChunkFactory.createChunk(9,13,"PER"),
                                ChunkFactory.createChunk(17,27,"LOC"));
            else
                return null;
        }
        Chunking chunking(String text, Chunk... chunks) {
            ChunkingImpl chunking = new ChunkingImpl(text);
            for (Chunk chunk : chunks) 
                chunking.add(chunk);
            return chunking;
        }
        
    }

}
