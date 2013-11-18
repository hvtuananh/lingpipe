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

package com.aliasi.test.unit.tokenizer;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenChunker;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import java.util.Arrays;

public class TokenChunkerTest {

    @Test
    public void testChunker() throws IOException {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenChunker chunker = new TokenChunker(tf);
        String s = "";
        assertChunking(chunker,"",new String[0],new int[0], new int[0]);
        assertChunking(chunker,"     ",new String[0],new int[0], new int[0]);
        assertChunking(chunker,"John ran.",
                       //       012345678
                       new String[] { "John", "ran", "." },
                       new int[] { 0, 5, 8 },
                       new int[] { 4, 8, 9 });
        assertChunking(chunker," John    ran.",
                       //       0123456789012
                       new String[] { "John", "ran", "." },
                       new int[] { 1, 9, 12 },
                       new int[] { 5, 12, 13 });
    }

    @Test
    public void testCompoundChunkers() throws IOException {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        tf = new LowerCaseTokenizerFactory(tf);
        tf = new EnglishStopTokenizerFactory(tf);
        tf = new PorterStemmerTokenizerFactory(tf);
        TokenChunker chunker = new TokenChunker(tf);
        assertChunking(chunker,"",new String[0],new int[0], new int[0]);
        assertChunking(chunker," John  is running ",
                       //       012345678901234567
                       new String[] { PorterStemmerTokenizerFactory.stem("john"),
                                      PorterStemmerTokenizerFactory.stem("running") },
                       new int[] { 1, 10 },
                       new int[] { 5, 17 });
                       
    }

    void assertChunking(TokenChunker chunker,
                        String in, String[] types, int[] starts, int[] ends) throws IOException {

        assertChunkingResult(chunker,in,types,starts,ends);
        Chunker chunker2 = (Chunker) AbstractExternalizable.serializeDeserialize(chunker);
    }

    void assertChunkingResult(Chunker chunker,
                              String in, String[] types, 
                              int[] starts, int[] ends) throws IOException {

        Chunking chunking = chunker.chunk(in);
        Chunk[] chunks = chunking.chunkSet().toArray(new Chunk[0]);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
        assertEquals(types.length,starts.length);
        assertEquals(starts.length,ends.length);
        assertEquals(chunks.length,types.length);
        for (int i = 0; i < chunks.length; ++i) {
            assertEquals(types[i],chunks[i].type());
            assertEquals(starts[i],chunks[i].start());
            assertEquals(ends[i],chunks[i].end());
        }
    }

}


