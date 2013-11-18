package com.aliasi.test.unit.dict;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.TrieDictionary;
import com.aliasi.dict.ApproxDictionaryChunker;

import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.Serializable;

public class ApproxDictionaryChunkerTest  {

    @Test
    public void testGeneChunker() {
        TrieDictionary trie = new TrieDictionary();
        trie.addEntry(new DictionaryEntry("SERPINA3","GENE"));
        TokenizerFactory tokenizer = IndoEuropeanTokenizerFactory.INSTANCE;

        ApproxDictionaryChunker fuzzyDictMatcher =
            new ApproxDictionaryChunker(trie,
                                        tokenizer,
                                        ApproxDictionaryChunker.TT_DISTANCE,
                                        500);
        String test1 = "serpin";
        Chunking chunking = fuzzyDictMatcher.chunk(test1);

        ChunkingImpl chunkingExpected = new ChunkingImpl(test1);
        chunkingExpected.add(ChunkFactory.createChunk(0,6,"GENE",260));

        assertEquals(chunkingExpected,chunking);
        // System.out.println("Found Chunks=" + chunking.chunkSet());
    }


    @Test
    public void testDissimilarity() {
        TrieDictionary dict = new TrieDictionary();

        DictionaryEntry entry1
            = new DictionaryEntry("ab","X");
        DictionaryEntry entry2
            = new DictionaryEntry("ab","Y");
        // new DictionaryEntry("ab cde","X");
        DictionaryEntry entry3
            = new DictionaryEntry("cd","Z");

        dict.addEntry(entry1);
        dict.addEntry(entry2);
        dict.addEntry(entry3);

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        double matchWeight = 0;
        double deleteWeight = -1;
        double insertWeight = -1;
        double substituteWeight = -1;
        double transposeWeight = Double.NEGATIVE_INFINITY;
        WeightedEditDistance editDistance
            = new FixedWeightEditDistance(matchWeight,deleteWeight,
                                          insertWeight,substituteWeight,
                                          transposeWeight);
        ApproxDictionaryChunker chunker
            = new ApproxDictionaryChunker(dict,tokenizerFactory,
                                          editDistance,2);

        String test1 = "ab";
        ChunkingImpl chunking = new ChunkingImpl(test1);
        chunking.add(ChunkFactory.createChunk(0,2,"X",0.0));
        chunking.add(ChunkFactory.createChunk(0,2,"Y",0.0));
        chunking.add(ChunkFactory.createChunk(0,2,"Z",2.0));
        test(test1,chunker,chunking);

        String test2 = " ab xyzwf ";
        ChunkingImpl chunking2 = new ChunkingImpl(test2);
        chunking2.add(ChunkFactory.createChunk(1,3,"X",0.0));
        chunking2.add(ChunkFactory.createChunk(1,3,"Y",0.0));
        chunking2.add(ChunkFactory.createChunk(1,3,"Z",2.0));
        test(test2,chunker,chunking2);

        String test3 = " a c ";
        ChunkingImpl chunking3 = new ChunkingImpl(test3);
        chunking3.add(ChunkFactory.createChunk(1,2,"X",1.0));
        chunking3.add(ChunkFactory.createChunk(1,2,"Y",1.0));
        chunking3.add(ChunkFactory.createChunk(1,2,"Z",2.0));
        chunking3.add(ChunkFactory.createChunk(3,4,"X",2.0));
        chunking3.add(ChunkFactory.createChunk(3,4,"Y",2.0));
        chunking3.add(ChunkFactory.createChunk(3,4,"Z",1.0));
        chunking3.add(ChunkFactory.createChunk(1,4,"X",2.0));
        chunking3.add(ChunkFactory.createChunk(1,4,"Y",2.0));
        test(test3,chunker,chunking3);

    }

    @Test
    public void testSimilarity() {
        TrieDictionary dict = new TrieDictionary();

        DictionaryEntry entry1
            = new DictionaryEntry("ab","X");
        DictionaryEntry entry2
            = new DictionaryEntry("ab","Y");
        // new DictionaryEntry("ab cde","X");
        DictionaryEntry entry3
            = new DictionaryEntry("cd","Z");

        dict.addEntry(entry1);
        dict.addEntry(entry2);
        dict.addEntry(entry3);

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        double matchWeight = 0;
        double deleteWeight = -1;
        double insertWeight = -1;
        double substituteWeight = -1;
        double transposeWeight = Double.NEGATIVE_INFINITY;
        WeightedEditDistance editDistance
            = new FixedWeightEditDistance(matchWeight,deleteWeight,
                                          insertWeight,substituteWeight,
                                          transposeWeight);
        ApproxDictionaryChunker chunker
            = new ApproxDictionaryChunker(dict,tokenizerFactory,
                                          editDistance,2);

        String test1 = "ab";
        ChunkingImpl chunking = new ChunkingImpl(test1);
        chunking.add(ChunkFactory.createChunk(0,2,"X",0.0));
        chunking.add(ChunkFactory.createChunk(0,2,"Y",0.0));
        chunking.add(ChunkFactory.createChunk(0,2,"Z",2.0));
        test(test1,chunker,chunking);

        String test2 = " ab xyzwf ";
        ChunkingImpl chunking2 = new ChunkingImpl(test2);
        chunking2.add(ChunkFactory.createChunk(1,3,"X",0.0));
        chunking2.add(ChunkFactory.createChunk(1,3,"Y",0.0));
        chunking2.add(ChunkFactory.createChunk(1,3,"Z",2.0));
        test(test2,chunker,chunking2);

        String test3 = " a c ";
        ChunkingImpl chunking3 = new ChunkingImpl(test3);
        chunking3.add(ChunkFactory.createChunk(1,2,"X",1.0));
        chunking3.add(ChunkFactory.createChunk(1,2,"Y",1.0));
        chunking3.add(ChunkFactory.createChunk(1,2,"Z",2.0));
        chunking3.add(ChunkFactory.createChunk(3,4,"X",2.0));
        chunking3.add(ChunkFactory.createChunk(3,4,"Y",2.0));
        chunking3.add(ChunkFactory.createChunk(3,4,"Z",1.0));
        chunking3.add(ChunkFactory.createChunk(1,4,"X",2.0));
        chunking3.add(ChunkFactory.createChunk(1,4,"Y",2.0));
        test(test3,chunker,chunking3);

    }

    /* NO TRANSPOSE YET
       @Test
       public void testThree() {
       TrieDictionary dict = new TrieDictionary();

       DictionaryEntry entry1
       = new DictionaryEntry("abcde","X");
       DictionaryEntry entry2
       = new DictionaryEntry("fgh","Y");

       dict.addEntry(entry1);
       dict.addEntry(entry2);

       TokenizerFactory tokenizerFactory
       = IndoEuropeanTokenizerFactory.INSTANCE;
       double matchWeight = 0;
       double deleteWeight = 1.1;
       double insertWeight = 1.01;
       double substituteWeight = 1.001;
       double transposeWeight = 1.0001;
       WeightedEditDistance editDistance
       = new FixedWeightEditDistance(matchWeight,deleteWeight,
       insertWeight,substituteWeight,
       transposeWeight);
       ApproxDictionaryChunker chunker
       = new ApproxDictionaryChunker(dict,tokenizerFactory,
       editDistance,1.5);

       String test1 = " abdce ";
       ChunkingImpl chunking = new ChunkingImpl(test1);
       chunking.add(ChunkFactory.createChunk(1,6,"X",1.0001));
       test(test1,chunker,chunking);
       }
    */

    void test(String test, Chunker chunker, Chunking expectedChunking) {
        char[] cs = test.toCharArray();
        Chunking chunking = chunker.chunk(cs,0,cs.length);
        // System.out.println("Found Chunks=" + chunking.chunkSet());
        // System.out.println("    Expected=" + expectedChunking.chunkSet());
        assertEquals(expectedChunking,chunking);

        @SuppressWarnings("unchecked")
            Chunker chunker2 = null;
        try {
            chunker2 = (Chunker) AbstractExternalizable.serializeDeserialize((Serializable) chunker);
        } catch (IOException e) {
            fail();
        }
        Chunking chunking2 = chunker2.chunk(cs,0,cs.length);
        assertEquals(expectedChunking,chunking2);
    }



}
