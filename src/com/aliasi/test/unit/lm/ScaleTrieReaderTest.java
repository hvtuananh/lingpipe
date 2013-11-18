package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;


import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import com.aliasi.lm.BitTrieReader;
import com.aliasi.lm.BitTrieWriter;
import com.aliasi.lm.ScaleTrieReader;
import com.aliasi.lm.TrieCharSeqCounter;

import java.io.*;

public class ScaleTrieReaderTest  {


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
    
    // System.out.println("Trie=\n" + c);

    String[] tests = new String[] {
        "abc", "bcd", "cd", "ab", "bc", "e", "de", "a", "b", "c"
    };

    assertScaling(c,0.1,nGram,tests);
    assertScaling(c,0.3,nGram,tests);
    assertScaling(c,0.6,nGram,tests);

    try {
        assertScaling(c,-1.0,nGram,tests);
        fail();
    } catch (IllegalArgumentException e) {
        succeed();
    }
    }

    void assertScaling(TrieCharSeqCounter counter, double scale,
               int maxNGram, String[] tests) 
    throws IOException {

    TrieCharSeqCounter scaled = scale(counter,scale,maxNGram);
    for (int i = 0; i < tests.length; ++i) {
        assertScaled(counter,scaled,scale,tests[i]);
    }
    }

    void assertScaled(TrieCharSeqCounter counter, TrieCharSeqCounter scaled,
              double scale, String s) {
    long count = counter.count(s);
    long expected = Math.round(scale * (double) count);
    long found = scaled.count(s);
    assertEquals(s + " scale=" + scale
             + " count=" + count
             + " expected=" + expected
             + " found=" + found,
             expected,found);
    }

    static TrieCharSeqCounter scale(TrieCharSeqCounter counter, double scale,
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
    ScaleTrieReader scaledReader = new ScaleTrieReader(reader,scale);
    return TrieCharSeqCounter.readCounter(scaledReader,maxNGram);
    }
}
