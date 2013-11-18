package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;


import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import com.aliasi.lm.BitTrieReader;
import com.aliasi.lm.BitTrieWriter;
import com.aliasi.lm.TrieCharSeqCounter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class TrieCharSeqCounterTest  {

    @Test
    public void testDecrementSubstrings() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(5);
    char[] cs = "abcdef".toCharArray();
    counter.incrementSubstrings(cs,0,cs.length);
    counter.decrementSubstrings(cs,0,3);
    assertEquals(0,counter.count("abc".toCharArray(),0,3));
    assertEquals(1,counter.count("abcd".toCharArray(),0,4));
    }

    @Test
    public void testDecrementSubstrings2() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(5);
    char[] cs = "abcdef".toCharArray();
    counter.incrementSubstrings(cs,0,cs.length);
    counter.incrementSubstrings(cs,0,cs.length);
    counter.decrementSubstrings(cs,0,3);
    assertEquals(1,counter.count("abc".toCharArray(),0,3));
    assertEquals(2,counter.count("abcd".toCharArray(),0,4));
    }

    @Test
    public void testDecrementSubstrings3() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(5);
    char[] cs = "abcdef".toCharArray();
    counter.incrementSubstrings(cs,0,cs.length);
    char[] cs2 = "abxyz".toCharArray();
    counter.incrementSubstrings(cs2,0,cs2.length);
    counter.decrementSubstrings(cs,0,3);
    assertEquals(1,counter.count("ab".toCharArray(),0,2));
    assertEquals(0,counter.count("abc".toCharArray(),0,3));
    assertEquals(1,counter.count("abx".toCharArray(),0,3));
    assertEquals(1,counter.count("abcd".toCharArray(),0,4));
    assertEquals(1,counter.count("abxy".toCharArray(),0,4));
    }

    @Test
    public void testDecrementSubstrings4() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(5);
    char[] cs = "abcdef".toCharArray();
    counter.incrementSubstrings(cs,0,cs.length);
    char[] cs2 = "abxyz".toCharArray();
    counter.incrementSubstrings(cs2,0,cs2.length);
    char[] cs3 = "abmnl".toCharArray();
    counter.incrementSubstrings(cs3,0,cs3.length);
    counter.decrementSubstrings(cs,0,3);
    assertEquals(2,counter.count("ab".toCharArray(),0,2));
    assertEquals(0,counter.count("abc".toCharArray(),0,3));
    assertEquals(1,counter.count("abx".toCharArray(),0,3));
    assertEquals(1,counter.count("abm".toCharArray(),0,3));
    assertEquals(1,counter.count("abcd".toCharArray(),0,4));
    assertEquals(1,counter.count("abxy".toCharArray(),0,4));
    assertEquals(1,counter.count("bmnl".toCharArray(),0,4));
    }

    @Test
    public void testDecrementSubstrings5() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(5);
    char[] cs = "abcdef".toCharArray();
    counter.incrementSubstrings(cs,0,cs.length);
    char[] cs2 = "abxyz".toCharArray();
    counter.incrementSubstrings(cs2,0,cs2.length);
    char[] cs3 = "abmnl".toCharArray();
    counter.incrementSubstrings(cs3,0,cs3.length);
    char[] cs4 = "ab123".toCharArray();
    counter.incrementSubstrings(cs4,0,cs4.length);
    counter.decrementSubstrings(cs,0,3);
    assertEquals(3,counter.count("ab".toCharArray(),0,2));
    assertEquals(0,counter.count("abc".toCharArray(),0,3));
    assertEquals(1,counter.count("abx".toCharArray(),0,3));
    assertEquals(1,counter.count("abm".toCharArray(),0,3));
    assertEquals(1,counter.count("abcd".toCharArray(),0,4));
    assertEquals(1,counter.count("abxy".toCharArray(),0,4));
    assertEquals(1,counter.count("bmnl".toCharArray(),0,4));
    assertEquals(1,counter.count("123".toCharArray(),0,3));
    }

    @Test
    public void testUniqueTotals() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(3);
        char[] cs = "abcde".toCharArray();
        counter.incrementSubstrings(cs,0,cs.length);
        long[][] uniqueTotals = counter.uniqueTotalNGramCount();

        assertEquals(1,counter.uniqueSequenceCount(0));
        assertEquals(5,counter.totalSequenceCount(0));
        assertEquals(1,uniqueTotals[0][0]);
        assertEquals(5,uniqueTotals[0][1]);

        assertEquals(5,counter.uniqueSequenceCount(1));
        assertEquals(5,counter.totalSequenceCount(1));
        assertEquals(5,uniqueTotals[1][0]);
        assertEquals(5,uniqueTotals[1][1]);

        assertEquals(4,counter.uniqueSequenceCount(2));
        assertEquals(4,counter.totalSequenceCount(2));
        assertEquals(4,uniqueTotals[2][0]);
        assertEquals(4,uniqueTotals[2][1]);

        assertEquals(3,counter.uniqueSequenceCount(3));
        assertEquals(3,counter.totalSequenceCount(3));
        assertEquals(3,uniqueTotals[3][0]);
        assertEquals(3,uniqueTotals[3][1]);
    }



    @Test
    public void testExs() {
        try {
            new TrieCharSeqCounter(-1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        TrieCharSeqCounter counter = new TrieCharSeqCounter(5);
        try {
            counter.count(new char[4], -1, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.count(new char[4], 3, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.count(new char[4], 2, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.extensionCount(new char[4], 2, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.charactersFollowing(new char[4], 2, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.numCharactersFollowing(new char[4], 2, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }



    }

    // COUNTS FOR: abracadabra
    // 11
    // a 5
    //   bra 2
    //   cad 1
    //   dab 1
    // b 2
    //   r 2
    //     a 2
    //       c 1
    // cada 1
    // dabr 1
    // r 2
    //   a 2
    //     ca 1
    // INCLUDES: Root, OneDtr, ThreeDtr, PAT1, PAT3, PAT4
    // MISSES: TwoDtr, ArrayDtr, PAT2
    @Test
    public void testAbracadabra() {
        String abracadabra = "abracadabra";
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings(abracadabra);
        assertEquals(5,counter.uniqueSequenceCount(1));
        assertEquals(7,counter.uniqueSequenceCount(2));
        assertEquals(7,counter.uniqueSequenceCount(3));
        assertEquals(7,counter.uniqueSequenceCount(4));

        assertEquals(11,counter.totalSequenceCount(1));
        assertEquals(10,counter.totalSequenceCount(2));
        assertEquals(9,counter.totalSequenceCount(3));
        assertEquals(8,counter.totalSequenceCount(4));

        assertArrayEquals(new int[] { 5, 2, 2, 1, 1 },
                          counter.nGramFrequencies(1));
    }


    @Test
    public void testPruneCount() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        assertEquals(0,counter.count("a"));
        counter.incrementSubstrings("a");
        assertEquals(1,counter.count("a"));
        assertEquals(1,counter.count(""));
        counter.prune(2);
        assertEquals(0,counter.count("a"));
        assertEquals(0,counter.count(""));

        counter.incrementSubstrings("a");
        assertEquals(1,counter.count("a"));
        assertEquals(1,counter.count(""));

        counter.incrementSubstrings("ab");
        assertEquals(3,counter.extensionCount("")); // a, ab, b
        assertEquals(2,counter.count("a"));
        assertEquals(1,counter.count("ab"));

        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        // assertEquals(7,counter.extensionCount(""));
        assertEquals(4,counter.count("a"));
        assertEquals(1,counter.count("ab"));
        assertEquals(2,counter.count("ac"));

        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(4,counter.count("a"));
        assertEquals(0,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
    }

    @Test
    public void testPruneCount2() {
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("abc");
        counter.incrementSubstrings("ab");
        // one dtr node losing extension
        counter.prune(2);
        assertEquals(0,counter.count("abc"));
        assertEquals(2,counter.count("ab"));
        assertEquals(2,counter.count("a"));
        // one dtr node losing         counter = new NGramProcessLM(4,128,4.0,1000000,100,0,Math.sqrt(2.0));self
        counter.prune(3);
        assertEquals(0,counter.count("ab"));
    }

    @Test
    public void testPruneCount3() {
        // one dtr node prune
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("abc");
        counter.incrementSubstrings("ab");
        counter.prune(2);
        assertEquals(0,counter.count("abc"));
        assertEquals(2,counter.count("ab"));
        assertEquals(2,counter.count("a"));
    }


    @Test
    public void testPruneCount4() {
        // two dtr node prune
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        // two dtr node, losing first dtr
        counter.prune(2);
        assertEquals(0,counter.count("ac"));
        assertEquals(2,counter.count("ab"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
    }


    @Test
    public void testPruneCount5() {
        // three dtr node prune
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        // lose {3}
        counter.prune(2);
        assertEquals(2,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
        assertEquals(0,counter.count("ad"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        // lose {2}
        counter.prune(2);
        assertEquals(2,counter.count("ab"));
        assertEquals(0,counter.count("ac"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        // lose {1}
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(2,counter.count("ac"));


        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        // lose {1,2}
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(0,counter.count("ac"));
        assertEquals(2,counter.count("ad"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        // lose {1,3}
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
        assertEquals(0,counter.count("ad"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        // lose {2,3}
        counter.prune(2);
        assertEquals(2,counter.count("ab"));
        assertEquals(0,counter.count("ac"));
        assertEquals(0,counter.count("ad"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        // lose {1,2,3}
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(0,counter.count("ac"));
        assertEquals(0,counter.count("ad"));
    }

    @Test
    public void testPruneCount6() {
        // array dtr
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ae");
        counter.incrementSubstrings("ae");
        // lose first in array
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
        assertEquals(2,counter.count("ad"));
        assertEquals(2,counter.count("ae"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ae");
        counter.incrementSubstrings("ae");
        // lose second
        counter.prune(2);
        assertEquals(2,counter.count("ab"));
        assertEquals(0,counter.count("ac"));
        assertEquals(2,counter.count("ad"));
        assertEquals(2,counter.count("ae"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ae");
        // lose last
        counter.prune(2);
        assertEquals(2,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
        assertEquals(2,counter.count("ad"));
        assertEquals(0,counter.count("ae"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ae");
        // lose first and last
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(2,counter.count("ac"));
        assertEquals(2,counter.count("ad"));
        assertEquals(0,counter.count("ae"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("ac");
        counter.incrementSubstrings("ad");
        counter.incrementSubstrings("ae");
        counter.incrementSubstrings("ae");
        // lose two middle
        counter.prune(2);
        assertEquals(2,counter.count("ab"));
        assertEquals(0,counter.count("ac"));
        assertEquals(0,counter.count("ad"));
        assertEquals(2,counter.count("ae"));
    }

    @Test
    public void testPruneCount7() {
        // PAT Dtr
        TrieCharSeqCounter counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("ab");
        counter.incrementSubstrings("cd");
        counter.incrementSubstrings("cd");
        // whole 1pat
        counter.prune(2);
        assertEquals(0,counter.count("ab"));
        assertEquals(0,counter.count("a"));
        assertEquals(2,counter.count("cd"));
        assertEquals(2,counter.count("c"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("abc");
        counter.incrementSubstrings("def");
        counter.incrementSubstrings("def");
        // whole 2pat
        counter.prune(2);
        assertEquals(0,counter.count("abc"));
        assertEquals(0,counter.count("a"));
        assertEquals(2,counter.count("def"));
        assertEquals(2,counter.count("de"));

        counter = new TrieCharSeqCounter(4);
        counter.incrementSubstrings("abcd");
        counter.incrementSubstrings("efgh");
        counter.incrementSubstrings("efgh");
        // whole 3pat
        counter.prune(2);
        assertEquals(0,counter.count("abcd"));
        assertEquals(0,counter.count("a"));
        assertEquals(2,counter.count("efgh"));
        assertEquals(2,counter.count("efg"));
        assertEquals(2,counter.count("ef"));
        assertEquals(2,counter.count("e"));

        counter = new TrieCharSeqCounter(6);
        counter.incrementSubstrings("abcde");
        counter.incrementSubstrings("fghij");
        counter.incrementSubstrings("fghij");
        // whole 4pat
        counter.prune(2);
        assertEquals(0,counter.count("abcde"));
        assertEquals(0,counter.count("a"));
        assertEquals(2,counter.count("fghij"));
        assertEquals(2,counter.count("fghi"));
        assertEquals(2,counter.count("fgh"));
        assertEquals(2,counter.count("fg"));
        assertEquals(2,counter.count("f"));

        counter = new TrieCharSeqCounter(7);
        counter.incrementSubstrings("abcdef");
        counter.incrementSubstrings("ghijkl");
        counter.incrementSubstrings("ghijkl");
        // array pat
        counter.prune(2);
        assertEquals(0,counter.count("abcdef"));
        assertEquals(0,counter.count("a"));
        assertEquals(2,counter.count("ghijkl"));
        assertEquals(2,counter.count("ghijk"));
        assertEquals(2,counter.count("ghij"));
        assertEquals(2,counter.count("ghi"));
        assertEquals(2,counter.count("gh"));
        assertEquals(2,counter.count("g"));

    }

    @Test
    public void testReadWrite() throws IOException {
        TrieCharSeqCounter c1 = new TrieCharSeqCounter(3);
    c1.incrementSubstrings("abcd");



    assertEqualCounts(c1,new String[] { "a", "b", "c", "x",
                        "ab", "bc", "xy", "ax", "xa",
                        "abc", "bxa" },
              3);

    c1.incrementSubstrings("aef");
    c1.incrementSubstrings("bef");
    c1.incrementSubstrings("cde");

    assertEqualCounts(c1,new String[] { "a", "b", "c", "x",
                        "ab", "bc", "xy", "ax", "xa",
                        "abc", "bxa" },
              3);


    c1.incrementSubstrings("abracadabra");
    assertEqualCounts(c1,new String[] { "abr", "br", "cad", "db" },
              3);

    }

    public void assertEqualCounts(TrieCharSeqCounter c,
                  String[] ss,
                  int maxNGram) throws IOException {
    assertCopy(c);

    // top level tests
    TrieCharSeqCounter cId = writeRead(c);
    assertEquals(c.uniqueSequenceCount(),cId.uniqueSequenceCount());
    assertEquals(c.totalSequenceCount(),cId.totalSequenceCount());



        
    TrieCharSeqCounter c2 = writeRead(c);

    for (int i = 0; i < ss.length; ++i) {
        String s = ss[i];
        if (s.length() <= maxNGram) {
        assertEquals(s,c.count(s), c2.count(s));
        if (s.length() < maxNGram) 
            assertEquals("ngram=" + maxNGram
                 + " extensionCount(" + s + ")",
                 c.extensionCount(s), c2.extensionCount(s));
        } else {
        assertEquals(0,c2.count(s));
        assertEquals(0,c2.extensionCount(s));
        }
    }
    }


    public TrieCharSeqCounter writeRead(TrieCharSeqCounter counter)
    throws IOException {

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    counter.writeTo(bytesOut);

    byte[] bytes = bytesOut.toByteArray();
    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
    TrieCharSeqCounter counter2 
        = TrieCharSeqCounter.readFrom(bytesIn);

    return counter2;

    }

    public void assertCopy(TrieCharSeqCounter counter) throws IOException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    BitOutput bitsOut = new BitOutput(bytesOut);
    BitTrieWriter writer = new BitTrieWriter(bitsOut);
    TrieCharSeqCounter.writeCounter(counter,writer,128);
    bitsOut.flush();
    byte[] bytes = bytesOut.toByteArray();

    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
    BitInput bitsIn = new BitInput(bytesIn);
    BitTrieReader reader = new BitTrieReader(bitsIn);
    ByteArrayOutputStream bytesOut2 = new ByteArrayOutputStream();
    BitOutput bitsOut2 = new BitOutput(bytesOut2);
    BitTrieWriter writer2 = new BitTrieWriter(bitsOut2);
    BitTrieWriter.copy(reader,writer2);
    bitsOut2.flush();
    byte[] bytes2 = bytesOut2.toByteArray();

    // System.out.println("\nRound trip it");
    ByteArrayInputStream bytesIn3 = new ByteArrayInputStream(bytes);
    BitInput bitsIn3 = new BitInput(bytesIn3);
    BitTrieReader reader3 = new BitTrieReader(bitsIn3);
    ByteArrayOutputStream bytesOut3 = new ByteArrayOutputStream();
    BitOutput bitsOut3 = new BitOutput(bytesOut3);
    BitTrieWriter writer3 = new BitTrieWriter(bitsOut3);
    BitTrieWriter.copy(reader3,writer3);
    bitsOut3.flush();
    byte[] bytes3 = bytesOut3.toByteArray();

    // System.out.println("bytes.length=" + bytes.length);
    // System.out.println("bytes2.length=" + bytes2.length);
    // System.out.println("bytes3.length=" + bytes3.length);
    assertEqualsBytes(bytes,bytes3);
    assertEqualsBytes(bytes,bytes2);
    }

    void assertEqualsBytes(byte[] bytes, byte[] bytes2) {
    assertEquals("length",bytes.length,bytes2.length);
    for (int i = 0; i < bytes2.length; ++i)
        assertEquals(bytes[i],bytes2[i]);
    }

}
