package com.aliasi.test.unit.chunk;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;

import static com.aliasi.test.unit.Asserts.assertLess;

import org.junit.Test;

public class ChunkTest {


    @Test
    public void testComparators() {
        Chunk[][] chunks = new Chunk[6][6];
        for (int i = 0; i < chunks.length; ++i)
            for (int j = i+1; j < chunks[i].length; ++j)
                chunks[i][j] = ChunkFactory.createChunk(i,j,"category");
        // starts later
        assertLess(Chunk.TEXT_ORDER_COMPARATOR,
                   chunks[1][3],chunks[2][3]);
        assertLess(Chunk.TEXT_ORDER_COMPARATOR,
                   chunks[1][3],chunks[3][4]);
        assertLess(Chunk.TEXT_ORDER_COMPARATOR,
                   chunks[1][3],chunks[4][5]);
        // starts same, ends later
        assertLess(Chunk.TEXT_ORDER_COMPARATOR,
                   chunks[1][3],chunks[1][4]);

        // starts later
        assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
                   chunks[1][3],chunks[2][3]);
        assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
                   chunks[1][3],chunks[3][4]);
        assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
                   chunks[1][3],chunks[4][5]);
        // starts same, ends earlier
        assertLess(Chunk.LONGEST_MATCH_ORDER_COMPARATOR,
                   chunks[1][4],chunks[1][3]);

    }


}
