package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;


import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import com.aliasi.lm.BitTrieReader;
import com.aliasi.lm.BitTrieWriter;
import com.aliasi.lm.PruneTrieReader;
import com.aliasi.lm.TrieCharSeqCounter;

import java.io.*;

public class PruneTrieReaderTest  {


    @Test
    public void testOne() 
    throws IOException {

    int nGram = 3;

        TrieCharSeqCounter c = new TrieCharSeqCounter(nGram);
    c.incrementSubstrings("abc");
    c.incrementSubstrings("bcd");
    c.incrementSubstrings("cde");
    c.incrementSubstrings("cde");
    c.incrementSubstrings("e");
    c.incrementSubstrings("e");
    c.incrementSubstrings("e");
    c.incrementSubstrings("e");
    c.incrementSubstrings("e");
    c.incrementSubstrings("e");
    
    String[] tests = new String[] {
        "abc", "bcd", "cd", "ab", "bc", "e", "de", "a", "b", "c"
    };

    assertPruning(c,1,nGram,tests);
    assertPruning(c,3,nGram,tests);
    assertPruning(c,5,nGram,tests);

    try {
        assertPruning(c,-1,nGram,tests);
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }
    }

    void assertPruning(TrieCharSeqCounter counter, int min, 
               int maxNGram, String[] tests) 
    throws IOException {

    TrieCharSeqCounter pruned = prune(counter,min,maxNGram);
    for (int i = 0; i < tests.length; ++i) {
        assertPruned(counter,pruned,min,tests[i]);
    }
    }

    void assertPruned(TrieCharSeqCounter counter, TrieCharSeqCounter pruned,
              int min, String s) {
    long count = counter.count(s);
    long expected = count >= min ? count : 0L;
    if (expected < min) expected = 0;
    long found = pruned.count(s);
    assertEquals(s + " min=" + min
             + " count=" + count
             + " expected=" + expected
             + " found=" + found,
             expected,found);
    }

    static TrieCharSeqCounter prune(TrieCharSeqCounter counter, int min,
                    int maxNGram) 
    throws IOException {
    
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    BitOutput bitsOut = new BitOutput(bytesOut);
    BitTrieWriter writer = new BitTrieWriter(bitsOut);
    TrieCharSeqCounter.writeCounter(counter,writer,128);
    bitsOut.flush();
    byte[] bytes = bytesOut.toByteArray();

    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
    BitInput bitsIn = new BitInput(bytesIn);
    BitTrieReader reader = new BitTrieReader(bitsIn);
    PruneTrieReader prunedReader = new PruneTrieReader(reader,min);
    return TrieCharSeqCounter.readCounter(prunedReader,maxNGram);
    }
}
