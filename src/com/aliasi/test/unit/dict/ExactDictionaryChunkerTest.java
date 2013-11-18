package com.aliasi.test.unit.dict;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class ExactDictionaryChunkerTest  {

    TokenizerFactory TOKENIZER_FACTORY
        = IndoEuropeanTokenizerFactory.INSTANCE;


    String regex = "[a-zA-Z]+|[0-9]+";
    TokenizerFactory  REGEX_TOKENIZER_FACTORY
        = new RegExTokenizerFactory(regex);


    @Test
    public void testComposedFactories() {
        TokenizerFactory tf = new RegExTokenizerFactory("([a-z]+)|([A-Z]+)|([0-9]+)");
        tf = new LowerCaseTokenizerFactory(tf);
        MapDictionary<String> mapDict = new MapDictionary<String>();
        mapDict.addEntry(new DictionaryEntry("p-53","entry1"));
        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(mapDict,tf,true,true); // all matches, case sensitive
        String test = "bar mP53wt.";
        Chunking chunking = chunker.chunk(test);
        Set<Chunk> chunkSet = chunking.chunkSet();
        assertEquals(1,chunkSet.size());
        Chunk chunk = chunkSet.iterator().next();
        assertEquals("entry1",chunk.type());
    }

    @Test
    public void testNulls() {
        assertNotNull(REGEX_TOKENIZER_FACTORY);
        String test1 = "P53 should match both as should p53.";
        assertNotNull(REGEX_TOKENIZER_FACTORY
                      .tokenizer(test1.toCharArray(),
                                 0,test1.length()).tokenize());
    }

    @Test
    public void testCaseSensitivity2() {
        TrieDictionary trie = new TrieDictionary();
        trie.addEntry(new DictionaryEntry("P53","human"));
        trie.addEntry(new DictionaryEntry("p53","mouse"));


        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(trie,REGEX_TOKENIZER_FACTORY,true,false);
        String test1 = "P53 should match both as should p53.";


        Chunking chunking = chunker.chunk(test1);

        Chunk human1 = ChunkFactory.createChunk(0,3,"mouse",1.0);
        Chunk mouse1 = ChunkFactory.createChunk(0,3,"human",1.0);

        Chunk human2 = ChunkFactory.createChunk(32,35,"mouse",1.0);
        Chunk mouse2 = ChunkFactory.createChunk(32,35,"human",1.0);

        assertChunking(chunker,test1,
                       new Chunk[] { human1, mouse1, human2, mouse2 });
    }



    @Test
    public void testTokenSensitivity() {
        TrieDictionary trie = new TrieDictionary();
        trie.addEntry(new DictionaryEntry("p-53","human"));
        trie.addEntry(new DictionaryEntry("p53","mouse"));
        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(trie,REGEX_TOKENIZER_FACTORY,true,true);
        String test1 = "p53 should match both as should p-53.";
        Chunking chunking = chunker.chunk(test1);

        Chunk human1 = ChunkFactory.createChunk(0,3,"mouse",1.0);
        Chunk mouse1 = ChunkFactory.createChunk(0,3,"human",1.0);

        Chunk human2 = ChunkFactory.createChunk(32,36,"mouse",1.0);
        Chunk mouse2 = ChunkFactory.createChunk(32,36,"human",1.0);

        assertChunking(chunker,test1,
                       new Chunk[] { human1, mouse1, human2, mouse2 });
    }


    @Test
    public void testEmptyDictionary() {
        MapDictionary dictionary = new MapDictionary();
        ExactDictionaryChunker caseInsensitiveChunker
            = new ExactDictionaryChunker(dictionary,
                                         TOKENIZER_FACTORY,
                                         true,   // find all
                                         false); // not case sensitive
        caseInsensitiveChunker.chunk("John ran");
    }


    @Test
    public void testCaseSensitivity() {
        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("50 Cent","PERSON",1.0));
        dictionary.addEntry(new DictionaryEntry("xyz120 DVD Player","DB_ID_1232",1.0));

        String text = "50 Cent is worth more than 50 cent.";
        //             012345678901234567890123456789012345
        //             0         1         2         3

        Chunk capChunk = ChunkFactory.createChunk(0,7,"PERSON",1.0);
        Chunk lowChunk = ChunkFactory.createChunk(27,34,"PERSON",1.0);

        ExactDictionaryChunker caseInsensitiveChunker
            = new ExactDictionaryChunker(dictionary,
                                         TOKENIZER_FACTORY,
                                         true,   // find all
                                         false); // not case sensitive

        assertChunking(caseInsensitiveChunker,text,
                       new Chunk[] { lowChunk, capChunk });

        ExactDictionaryChunker caseSensitiveChunker
            = new ExactDictionaryChunker(dictionary,
                                         TOKENIZER_FACTORY,
                                         true,   // find all
                                         true); // is case sensitive

        assertChunking(caseSensitiveChunker,text,
                       new Chunk[] { capChunk });


    }

    @Test
    public void testOverlapsCase() {
        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("john smith","PER",7.0));
        dictionary.addEntry(new DictionaryEntry("smith and barney","ORG",3.0));
        dictionary.addEntry(new DictionaryEntry("smith","LOC",2.0));
        dictionary.addEntry(new DictionaryEntry("smith","PER",5.0));

        Chunk chunk_0_10_PER = ChunkFactory.createChunk(0,10,"PER",7.0);
        Chunk chunk_5_10_PER = ChunkFactory.createChunk(5,10,"PER",5.0);
        Chunk chunk_5_10_LOC = ChunkFactory.createChunk(5,10,"LOC",2.0);
        Chunk chunk_5_21_ORG = ChunkFactory.createChunk(5,21,"ORG",3.0);


        Chunk[] allChunks = new Chunk[] {
            chunk_0_10_PER,
            chunk_5_10_PER,
            chunk_5_10_LOC,
            chunk_5_21_ORG
        };

        Chunk[] casedChunks = new Chunk[] {
            chunk_5_10_PER,
            chunk_5_10_LOC,
        };

        Chunk[] singleChunks = new Chunk[] {
            chunk_0_10_PER
        };

        Chunk[] singleCaseChunks = new Chunk[] {
            chunk_5_10_PER
        };


        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         true,true);
        assertChunking(chunker,"john smith and barney",allChunks);
        assertChunking(chunker,"JohN smith AND Barney",casedChunks);

        chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         false,true);
        assertChunking(chunker,"john smith and barney",singleChunks);
        assertChunking(chunker,"JohN smith AND Barney",singleCaseChunks);

        chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         true,false);
        assertChunking(chunker,"john smith and barney",allChunks);
        assertChunking(chunker,"JohN smith AND Barney",allChunks);

        chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         false,false);
        assertChunking(chunker,"john smith and barney",singleChunks);
        assertChunking(chunker,"JohN smith AND Barney",singleChunks);
    }

    @Test
    public void testBoundaries() {
        MapDictionary dictionary = new MapDictionary();
        dictionary.addEntry(new DictionaryEntry("john smith","PER",7.0));

        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(dictionary,TOKENIZER_FACTORY,
                                         true,true);

        Chunk[] noChunks = new Chunk[0];
        assertChunking(chunker,"john",noChunks);
        assertChunking(chunker,"smith john",noChunks);
        assertChunking(chunker,"john smith",
                       new Chunk[] { ChunkFactory.createChunk(0,10,"PER",7.0) });
        assertChunking(chunker,"john smith smith",
                       new Chunk[] { ChunkFactory.createChunk(0,10,"PER",7.0) });
        assertChunking(chunker,"john smith frank",
                       new Chunk[] { ChunkFactory.createChunk(0,10,"PER",7.0) });
        assertChunking(chunker,"then john smith",
                       new Chunk[] { ChunkFactory.createChunk(5,15,"PER",7.0) });
        assertChunking(chunker,"john john smith",
                       new Chunk[] { ChunkFactory.createChunk(5,15,"PER",7.0) });
        assertChunking(chunker,"john john smith smith",
                       new Chunk[] { ChunkFactory.createChunk(5,15,"PER",7.0) });
    }

    void assertChunking(ExactDictionaryChunker chunker, String in,
                        Chunk[] chunks) {
        Chunking chunking = chunker.chunk(in);

        ChunkingImpl chunkingExpected = new ChunkingImpl(in);
        for (int i = 0; i < chunks.length; ++i)
            chunkingExpected.add(chunks[i]);

        assertEquals(chunkingExpected,chunking);
    }



}
