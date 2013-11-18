package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static com.aliasi.test.unit.Asserts.assertFullEquals;


import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChunkingImplTest {

    @Test(expected=IllegalArgumentException.class)
    public void testMergeClustersExc() {
        Chunking chunking1 = new ChunkingImpl("abc");
        Chunking chunking2 = new ChunkingImpl("cde");
        ChunkingImpl.merge(chunking1,chunking2);
    }

    @Test
    public void testMerge() {
        assertMerge("abc",
                    Arrays.<Chunk>asList(),
                    Arrays.<Chunk>asList(),
                    Arrays.<Chunk>asList());

        assertMerge("abc",
                    Arrays.asList(ChunkFactory.createChunk(0,1)),
                    Arrays.<Chunk>asList(),
                    Arrays.asList(ChunkFactory.createChunk(0,1)));

        assertMerge("abc",
                    Arrays.<Chunk>asList(),
                    Arrays.asList(ChunkFactory.createChunk(0,1)),
                    Arrays.asList(ChunkFactory.createChunk(0,1)));

        assertMerge("ab de fg",
                    Arrays.asList(ChunkFactory.createChunk(0,5),
                                  ChunkFactory.createChunk(3,5),
                                  ChunkFactory.createChunk(0,2)),
                    Arrays.<Chunk>asList(),
                    Arrays.asList(ChunkFactory.createChunk(0,2),
                                  ChunkFactory.createChunk(3,5)));

        assertMerge("ab de fg",
                    Arrays.<Chunk>asList(),
                    Arrays.asList(ChunkFactory.createChunk(0,5),
                                  ChunkFactory.createChunk(3,5),
                                  ChunkFactory.createChunk(0,2)),
                    Arrays.asList(ChunkFactory.createChunk(0,2),
                                  ChunkFactory.createChunk(3,5)));

        assertMerge("ab cd ef gh",
                    Arrays.asList(ChunkFactory.createChunk(0,2,"A"),
                                  ChunkFactory.createChunk(6,8,"B")),
                    Arrays.asList(ChunkFactory.createChunk(0,2,"C"),
                                  ChunkFactory.createChunk(3,5,"D"),
                                  ChunkFactory.createChunk(3,8,"E")),
                    Arrays.asList(ChunkFactory.createChunk(0,2,"A"),
                                  ChunkFactory.createChunk(3,5,"D"),
                                  ChunkFactory.createChunk(6,8,"B")));

        assertMerge("ab cd ef gh",
                    Arrays.asList(ChunkFactory.createChunk(0,2,"A"),
                                  ChunkFactory.createChunk(0,5,"B")),
                    Arrays.asList(ChunkFactory.createChunk(3,5,"D")),
                    Arrays.asList(ChunkFactory.createChunk(0,2,"A"),
                                  ChunkFactory.createChunk(3,5,"D")));
    }

    void assertMerge(CharSequence cs,
                     List<Chunk> chunks1,
                     List<Chunk> chunks2,
                     List<Chunk> chunksExpected) {
        ChunkingImpl chunking1 = new ChunkingImpl(cs);
        chunking1.addAll(chunks1);
        ChunkingImpl chunking2 = new ChunkingImpl(cs);
        chunking2.addAll(chunks2);
        ChunkingImpl chunkingExpected = new ChunkingImpl(cs);
        chunkingExpected.addAll(chunksExpected);
        assertEquals(chunkingExpected,
                     ChunkingImpl.merge(chunking1,chunking2));
    }


    @Test
    public void testOverlap() {
        assertOverlap(false,0,1,1,2);
        assertOverlap(false,10,11,2,9);

        assertOverlap(true,0,1,0,2);
        assertOverlap(true,1,4,2,3);
        assertOverlap(true,1,5,2,7);
        assertOverlap(true,1,5,2,5);
    }

    void assertOverlap(boolean overlap, int start1, int end1, int start2, int end2) {
        assertOverlapOneWay(overlap,start1,end1,start2,end2);
        assertOverlapOneWay(overlap,start2,end2,start1,end1);
    }

    void assertOverlapOneWay(boolean overlap, int start1, int end1, int start2, int end2) {
        assertEquals(overlap,
                     ChunkingImpl.overlap(ChunkFactory.createChunk(start1,end1),
                                          ChunkFactory.createChunk(start2,end2)));
    }

    @Test
    public void testHashCode() {
        ChunkingImpl c1 = new ChunkingImpl("foo bar");

        assertEquals(Strings.hashCode(c1.charSequence())
                     + 31 * c1.chunkSet().hashCode(),
                     c1.hashCode());

        c1.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertEquals(Strings.hashCode(c1.charSequence())
                     + 31 * c1.chunkSet().hashCode(),
                     c1.hashCode());

        c1.add(ChunkFactory.createChunk(4,7,"BAR"));
        assertEquals(Strings.hashCode(c1.charSequence())
                     + 31 * c1.chunkSet().hashCode(),
                     c1.hashCode());
    }


    @Test
    public void testEquals() {
        StringBuilder sb = new StringBuilder("foo bar");
        ChunkingImpl c1 = new ChunkingImpl(sb);
        ChunkingImpl c2 = new ChunkingImpl(sb.toString());
        assertFullEquals(c1,c2);

        c1.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertFalse(c1.equals(c2));

        c2.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertFullEquals(c1,c2);

        c1.add(ChunkFactory.createChunk(0,3,"FOO"));
        assertFullEquals(c1,c2);

        c2.add(ChunkFactory.createChunk(4,7,"BAR"));
        assertFalse(c1.equals(c2));

        c1.add(ChunkFactory.createChunk(4,7,"BAR"));
        assertFullEquals(c1,c2);
    }

    @Test
    public void testSeq() {
        String seq = "span of text";
        Chunking c1 = new ChunkingImpl(seq);
        Chunking c2 = new ChunkingImpl(seq.toCharArray(),0,seq.length());
        assertEquals(seq,c1.charSequence());
        assertEquals(seq,c2.charSequence());
        assertEquals(c1.charSequence(),c2.charSequence());
    }

    @Test
    public void testSet() {
        String seq = "012345";
        ChunkingImpl chunking = new ChunkingImpl(seq);
        Set set1 = new LinkedHashSet();
        assertEquals(set1,chunking.chunkSet());
    }

    @Test
    public void testAdd() {
        String seq = "012345";
        ChunkingImpl chunking = new ChunkingImpl(seq);
        Chunk c1 = ChunkFactory.createChunk(0,1,"foo");
        chunking.add(c1);
        LinkedHashSet set1 = new LinkedHashSet();
        set1.add(c1);
        LinkedHashSet set2 = new LinkedHashSet(chunking.chunkSet());
        assertEquals(set1,set2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrow() {
        String seq = "012345";
        ChunkingImpl chunking = new ChunkingImpl(seq);
        Chunk c1 = ChunkFactory.createChunk(0,101,"foo");
        chunking.add(c1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrow2() {
        String seq = "012345";
        ChunkingImpl chunking = new ChunkingImpl(seq);
        Chunk c1 = ChunkFactory.createChunk(100,101,"foo");
        chunking.add(c1);
    }


}
