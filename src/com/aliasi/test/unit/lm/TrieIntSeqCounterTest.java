package com.aliasi.test.unit.lm;

import com.aliasi.lm.TrieIntSeqCounter;

import com.aliasi.util.ObjectToCounterMap;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;


import java.util.List;
import java.util.Random;

public class TrieIntSeqCounterTest  {

    @Test
    public void testZero() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(3);

        int[] is_12 = new int[] { 1, 2 };
        int[] is_13 = new int[] { 1, 3 };

        counter.incrementSequence(is_12,0,2,12);
        assertEquals(12,counter.count(is_12,0,2));
        assertEquals(12,counter.extensionCount(is_12,0,1));

        counter.incrementSequence(is_13,0,2,10);
        assertEquals(10,counter.count(is_13,0,2));
        assertEquals(22,counter.extensionCount(is_13,0,1));

        counter.incrementSequence(is_13,0,2,44);
        assertEquals(54,counter.count(is_13,0,2));
        assertEquals(66,counter.extensionCount(is_13,0,1));

        counter.incrementSequence(is_13,0,0,111);
        assertEquals(111,counter.count(is_13,0,0));

        counter.incrementSequence(is_13,0,1,444);
        assertEquals(444,counter.count(is_13,0,1));
        assertEquals(444,counter.extensionCount(is_13,0,0));

        counter.incrementSequence(is_13,0,0,111);
        assertEquals(222,counter.count(is_13,0,0));

        counter.incrementSequence(is_13,0,1,444);
        assertEquals(888,counter.count(is_13,0,1));
        assertEquals(888,counter.extensionCount(is_13,0,0));

        int[] is_2 = new int[] { 2 };
        int[] is_3 = new int[] { 3 };
        int[] is_4 = new int[] { 4 };
        counter.incrementSequence(is_2,0,1,10);
        counter.incrementSequence(is_3,0,1,100);
        counter.incrementSequence(is_4,0,1,1000);

        assertEquals(10,counter.count(is_2,0,1));
        assertEquals(100,counter.count(is_3,0,1));
        assertEquals(1000,counter.count(is_4,0,1));
        assertEquals(1998,counter.extensionCount(is_4,0,0));
    }


    @Test
    public void testSequence() {
        int[] is_1 = new int[] { 1 };
        TrieIntSeqCounter counter = new TrieIntSeqCounter(3);
        counter.incrementSequence(is_1, 0, 1, 3);
        assertEquals(3,counter.extensionCount(is_1, 0, 0));
        assertEquals(3,counter.count(is_1, 0, 1));
        assertEquals(0,counter.extensionCount(is_1,0,1));

        int[] is_123 = new int[] { 1, 2, 3 };
        counter.incrementSequence(is_123, 0, 3, 5);
        assertEquals(3,counter.extensionCount(is_1, 0, 0));
        assertEquals(3,counter.count(is_1, 0, 1));
        assertEquals(0,counter.extensionCount(is_1,0,1));
        assertEquals(5,counter.count(is_123,0,3));
        assertEquals(5,counter.extensionCount(is_123,0,2));

        int[] is_12345 = new int[] { 1, 2, 3, 4, 5 };
        int[] is_345 = new int[] { 3, 4, 5 };
        int[] is_34 = new int[] { 3, 4 };
        counter.incrementSequence(is_12345,0,5,109);
        assertEquals(0,counter.count(is_12345,0,5));
        assertEquals(109,counter.extensionCount(is_34,0,2));
        assertEquals(109,counter.count(is_345,0,3));
        assertEquals(109,counter.count(is_12345,2,5));

        int[] is_134 = new int[] { 1, 3, 4 };
        int[] is_135 = new int[] { 1, 3, 5 };
        int[] is_136 = new int[] { 1, 3, 6 };
        int[] is_137 = new int[] { 1, 3, 7 };
        counter.incrementSequence(is_134,0,3,12);
        assertEquals(12,counter.count(is_134,0,3));
        counter.incrementSequence(is_134,0,3,10);
        assertEquals(22,counter.count(is_134,0,3));

        counter.incrementSequence(is_135,0,3,44);
        assertEquals(22,counter.count(is_134,0,3));
        assertEquals(44,counter.count(is_135,0,3));

        counter.incrementSequence(is_136,0,3,55);
        assertEquals(22,counter.count(is_134,0,3));
        assertEquals(44,counter.count(is_135,0,3));
        assertEquals(55,counter.count(is_136,0,3));

        counter.incrementSequence(is_137,0,3,81);
        assertEquals(22,counter.count(is_134,0,3));
        assertEquals(44,counter.count(is_135,0,3));
        assertEquals(55,counter.count(is_136,0,3));
        assertEquals(81,counter.count(is_137,0,3));

        counter.incrementSequence(is_136,0,3,1000);
        assertEquals(22,counter.count(is_134,0,3));
        assertEquals(44,counter.count(is_135,0,3));
        assertEquals(1055,counter.count(is_136,0,3));
        assertEquals(81,counter.count(is_137,0,3));
    }

    @Test
    public void testSize() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        assertEquals(1,counter.trieSize());

        counter.incrementSubsequences(new int[] { 1 }, 0, 1);
        assertEquals(2,counter.trieSize());

        counter.incrementSubsequences(new int[] { 2 }, 0, 1);
        assertEquals(3,counter.trieSize());

        counter.incrementSubsequences(new int[] { 3 }, 0, 1);
        assertEquals(4,counter.trieSize());

        counter.incrementSubsequences(new int[] { 4 }, 0, 1);
        assertEquals(5,counter.trieSize());

        counter.incrementSubsequences(new int[] { 1, 2 }, 0, 2);
        assertEquals(6,counter.trieSize());

        counter.incrementSubsequences(new int[] { 1, 3 }, 0, 2);
        assertEquals(7,counter.trieSize());

        counter.incrementSubsequences(new int[] { 1, 4 }, 0, 2);
        assertEquals(8,counter.trieSize());

        counter.incrementSubsequences(new int[] { 2, 3}, 0, 2);
        assertEquals(9,counter.trieSize());

        counter.incrementSubsequences(new int[] { 1, 2, 3}, 0, 3);
        assertEquals(10,counter.trieSize());

        counter.incrementSubsequences(new int[] { 1, 2, 3, 4}, 0, 4);
        assertEquals(13,counter.trieSize()); // +3,4; +2,3,4; +1,2,3,4
    }

    @Test
    public void testScaling1() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        counter.incrementSubsequences(new int[] { 1, 2 }, 0, 2);
        counter.incrementSubsequences(new int[] { 1, 3 }, 0, 2);
        counter.incrementSubsequences(new int[] { 1, 3 }, 0, 2);
        counter.rescale(0.5F);
        assertEquals(3,counter.count(new int[] { }, 0, 0));
        assertEquals(1,counter.count(new int[] { 1 }, 0, 1));
        assertEquals(0,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(1,counter.count(new int[] { 1, 3 }, 0, 2));
    }

    @Test
    public void testScaling2() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        counter.incrementSubsequences(new int[] { 1, 2 }, 0, 2);
        counter.incrementSubsequences(new int[] { 1, 3 }, 0, 2);
        counter.incrementSubsequences(new int[] { 1, 3 }, 0, 2);
        counter.incrementSubsequences(new int[] { 1, 4 }, 0, 2);
        counter.rescale(0.5);
        assertEquals(4,counter.count(new int[] { }, 0, 0));
        assertEquals(2,counter.count(new int[] { 1 }, 0, 1));
        assertEquals(0,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(1,counter.count(new int[] { 1, 3 }, 0, 2));
        assertEquals(0,counter.count(new int[] { 1, 4 }, 0, 2));
    }

    @Test
    public void testScaling3() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 5 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 6 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 7, 8, 9 }, 0, 4);
        counter.rescale(0.5);
        assertEquals(12,counter.count(new int[] { }, 0, 0));
        assertEquals(3,counter.count(new int[] { 1 }, 0, 1));
        assertEquals(2,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(2,counter.count(new int[] { 1, 2, }, 0, 2));
        assertEquals(0,counter.count(new int[] { 1, 7, }, 0, 2));
        assertEquals(2,counter.count(new int[] { 1, 2, 3}, 0, 3));
        assertEquals(1,counter.count(new int[] { 1, 2, 3, 4}, 0, 4));
    }

    @Test
    public void testPruning1() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3 }, 0, 3);
        counter.prune(3);
        assertEquals(0,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
    }

    @Test
    public void testPruning2() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3, 4 }, 0, 4);
        counter.incrementSubsequences(new int[] { 1, 2, 3 }, 0, 3);
        counter.incrementSubsequences(new int[] { 1, 2 }, 0, 2);
        counter.incrementSubsequences(new int[] { 1 }, 0, 1);

        counter.incrementSubsequences(new int[] { 2, 3 }, 0, 2);
        counter.incrementSubsequences(new int[] { 2, 4 }, 0, 2);
        counter.incrementSubsequences(new int[] { 2, 4 }, 0, 2);
        counter.incrementSubsequences(new int[] { 2, 5 }, 0, 2);
        counter.incrementSubsequences(new int[] { 2, 5 }, 0, 2);
        counter.incrementSubsequences(new int[] { 2, 5 }, 0, 2);

        counter.incrementSubsequences(new int[] { 2 }, 0, 1);

        counter.incrementSubsequences(new int[] { 3, 5 }, 0, 2);
        counter.incrementSubsequences(new int[] { 3, 6 }, 0, 2);
        counter.incrementSubsequences(new int[] { 3, 6 }, 0, 2);

        counter.incrementSubsequences(new int[] { 4, 5 }, 0, 2);
        counter.incrementSubsequences(new int[] { 4, 5 }, 0, 2);
        counter.incrementSubsequences(new int[] { 4, 5 }, 0, 2);

        counter.incrementSubsequences(new int[] { 7 }, 0, 1);

        assertEquals(1,counter.count(new int[] { 7 }, 0, 1));
        assertEquals(2,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
        assertEquals(3,counter.count(new int[] { 1, 2, 3 }, 0, 3));
        assertEquals(4,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(4,counter.count(new int[] { 2, 3 }, 0, 2));
        assertEquals(11,counter.count(new int[] { 2 }, 0, 1));

        counter.prune(1);

        assertEquals(1,counter.count(new int[] { 7 }, 0, 1));
        assertEquals(2,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
        assertEquals(3,counter.count(new int[] { 1, 2, 3 }, 0, 3));
        assertEquals(4,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(4,counter.count(new int[] { 2, 3 }, 0, 2));
        assertEquals(11,counter.count(new int[] { 2 }, 0, 1));

        counter.prune(2);

        assertEquals(0,counter.count(new int[] { 7 }, 0, 1));
        assertEquals(2,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
        assertEquals(3,counter.count(new int[] { 1, 2, 3 }, 0, 3));
        assertEquals(4,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(4,counter.count(new int[] { 2, 3 }, 0, 2));
        assertEquals(11,counter.count(new int[] { 2 }, 0, 1));

        counter.prune(3);

        assertEquals(0,counter.count(new int[] { 7 }, 0, 1));
        assertEquals(0,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
        assertEquals(3,counter.count(new int[] { 1, 2, 3 }, 0, 3));
        assertEquals(4,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(4,counter.count(new int[] { 2, 3 }, 0, 2));
        assertEquals(11,counter.count(new int[] { 2 }, 0, 1));

        counter.prune(4);

        assertEquals(0,counter.count(new int[] { 7 }, 0, 1));
        assertEquals(0,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
        assertEquals(0,counter.count(new int[] { 1, 2, 3 }, 0, 3));
        assertEquals(4,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(4,counter.count(new int[] { 2, 3 }, 0, 2));
        assertEquals(11,counter.count(new int[] { 2 }, 0, 1));

        counter.prune(5);

        assertEquals(0,counter.count(new int[] { 7 }, 0, 1));
        assertEquals(0,counter.count(new int[] { 1, 2, 3, 4 }, 0, 4));
        assertEquals(0,counter.count(new int[] { 1, 2, 3 }, 0, 3));
        assertEquals(0,counter.count(new int[] { 1, 2 }, 0, 2));
        assertEquals(0,counter.count(new int[] { 2, 3 }, 0, 2));
        assertEquals(11,counter.count(new int[] { 2 }, 0, 1));

        counter.prune(100);
        assertEquals(0,counter.count(new int[] { 2 }, 0, 1));

    }

    @Test
    public void testOutcomeHistograms() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(4);
        counter.incrementSubsequences(new int[] { 1, 2, 3 }, 0, 3);
        counter.incrementSubsequences(new int[] { 1, 2, 4 }, 0, 3);
        counter.incrementSubsequences(new int[] { 1, 2, 4 }, 0, 3);
        counter.incrementSubsequences(new int[] { 1, 2, 5 }, 0, 3);
        counter.incrementSubsequences(new int[] { 1, 3, 6 }, 0, 3);
        counter.incrementSubsequences(new int[] { 7, 1, 2 }, 0, 3);
        counter.incrementSubsequences(new int[] { 7, 1, 3 }, 0, 3);
        counter.incrementSubsequences(new int[] { 1, 3 }, 0, 2);
        counter.incrementSubsequences(new int[] { 7, 1 }, 0, 2);
        ObjectToCounterMap trigramCounter = counter.nGramCounts(3,2);
        List keysOrderedByCount = trigramCounter.keysOrderedByCountList();
        assertEquals(1,keysOrderedByCount.size());
        assertArrayEquals(new int[] { 1, 2, 4 },
                          (int[]) keysOrderedByCount.get(0));


        trigramCounter = counter.nGramCounts(2,2);
        keysOrderedByCount = trigramCounter.keysOrderedByCountList();
        assertEquals(4 ,keysOrderedByCount.size()); // 1,2=5; 7,1=3; 1,3=3; 2,4=2
        assertArrayEquals(new int[] { 1, 2 },
                          (int[]) keysOrderedByCount.get(0));
        assertEquals(5,trigramCounter.getCount((int[]) keysOrderedByCount.get(0)));
        assertArrayEquals(new int[] { 2, 4 },
                          (int[]) keysOrderedByCount.get(3));

        try {
            counter.nGramCounts(0,4);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testExs() {
        try {
            new TrieIntSeqCounter(-1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        TrieIntSeqCounter counter = new TrieIntSeqCounter(3);
        try {
            counter.incrementSubsequences(new int[4], -1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.incrementSubsequences(new int[4], 2, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.incrementSubsequences(new int[4], 2, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.incrementSubsequences(new int[4], 5, 7);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.count(new int[3], 5, 7);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.extensionCount(new int[3], 5, 7);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

        try {
            counter.numExtensions(new int[3], 5, 7);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }

    }

    @Test
    public void testMaxLength() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(3);
        assertEquals(3,counter.maxLength());
    }

    public int A = 'a';
    public int B = 'b';
    public int C = 'c';
    public int D = 'd';
    public int R = 'r';

    public int[] ABRACADABRA = new int[] { A, B, R, A, C, A, D, A, B, R, A };

    @Test
    public void testCount() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(3);
        counter.incrementSubsequences(new int[] { }, 0, 0);
        assertEquals(0,counter.count(new int[] { }, 0, 0));
        assertEquals(0,counter.count(new int[] { 1 }, 0, 1));

        counter.incrementSubsequences(new int[] { 1 }, 0, 1);
        assertEquals(1,counter.count(new int[] { 1 }, 0, 1));
        assertEquals(0,counter.count(new int[] { 17 }, 0, 1));

        counter.incrementSubsequences(new int[] { 1 }, 0, 1);
        assertEquals(2,counter.count(new int[] { 1 }, 0, 1));
    }

    @Test
    public void testAbracadbra() {
        TrieIntSeqCounter counter = new TrieIntSeqCounter(3);
        counter.incrementSubsequences(ABRACADABRA,0,ABRACADABRA.length);

        assertEquals(11,counter.count(new int[] { }, 0, 0));
        assertEquals(5,counter.count(new int[] { A }, 0, 1));
        assertEquals(2,counter.count(new int[] { B }, 0, 1));
        assertEquals(1,counter.count(new int[] { C }, 0, 1));
        assertEquals(5,counter.count(new int[] { B, A }, 1, 2));
        assertEquals(2,counter.count(new int[] { A, B }, 0, 2));
        assertEquals(2,counter.count(new int[] { A, B, R }, 0, 3));
        assertEquals(1,counter.count(new int[] { A, D, A }, 0, 3));
        assertEquals(0,counter.count(new int[] { A, D, A, A }, 0, 4));
        assertEquals(0,counter.count(new int[] { A, B, R, A }, 0, 4));

        assertEquals(11,counter.extensionCount(new int[] { }, 0, 0));
        assertEquals(4,counter.extensionCount(new int[] { A }, 0, 1));
        assertEquals(4,counter.extensionCount(new int[] { B, A }, 1, 2));
        assertEquals(4,counter.extensionCount(new int[] { A, B }, 0, 1));
        assertEquals(0,counter.extensionCount(new int[] { A, B, R }, 0, 3));
        assertEquals(1,counter.extensionCount(new int[] { D, A }, 0, 2));

        assertEquals(5,counter.numExtensions(new int[] { }, 0, 0));
        assertEquals(3,counter.numExtensions(new int[] { A }, 0, 1));
        assertEquals(3,counter.numExtensions(new int[] { B, A }, 1, 2));
        assertEquals(3,counter.numExtensions(new int[] { A, B }, 0, 1));
        assertEquals(0,counter.numExtensions(new int[] { A, B, R }, 0, 3));
        assertEquals(1,counter.numExtensions(new int[] { D, A }, 0, 2));

        counter.incrementSubsequences(new int[] { A, D, B }, 0, 3);
        assertEquals(2,counter.extensionCount(new int[] { A, D, B }, 0, 2));
        assertEquals(2,counter.numExtensions(new int[] { A, D, B }, 0, 2));


        counter.incrementSubsequences(new int[] { A, D, B }, 0, 3);
        assertEquals(3,counter.extensionCount(new int[] { A, D, B }, 0, 2));

        counter.incrementSubsequences(new int[] { A, D, A}, 0, 3);
        assertEquals(4,counter.extensionCount(new int[] { A, D }, 0, 2));
        assertEquals(2,counter.numExtensions(new int[] { A, D }, 0, 2));

        assertArrayEquals(new int[] { A, B, C, D, R },
                          counter.observedIntegers());

        assertArrayEquals(new int[] { R },
                          counter.integersFollowing(new int[] { B }, 0, 1));
        assertArrayEquals(new int[] { A, B, C, D, R },
                          counter.integersFollowing(new int[0], 0, 0));

        assertArrayEquals(new int[] { B, C, D },
                          counter.integersFollowing(new int[] { A }, 0, 1));


    }


    @Test
    public void testMultipleIncrements() {
        Random random = new Random();
        TrieIntSeqCounter counter1 = new TrieIntSeqCounter(3);
        TrieIntSeqCounter counter2 = new TrieIntSeqCounter(3);
        int[] xs = new int[5];
        for (int i = 0; i < 100; ++i) {
            for (int k = 0; k < xs.length; ++k)
                xs[k] = random.nextInt(16);
            int trainingCount = random.nextInt(10); // train 0 to 10 times
            incrementAssertSynched(counter1,counter2,xs,trainingCount);
        }
    }

    void incrementAssertSynched(TrieIntSeqCounter counter1,
                                TrieIntSeqCounter counter2,
                                int[] seq,
                                int count) {
        for (int i = 0; i < count; ++i)
            counter1.incrementSubsequences(seq,0,seq.length);
        counter2.incrementSubsequences(seq,0,seq.length,count);
        assertSynched(counter1,counter2);
        assertSynched(counter2,counter1);
    }

    void assertSynched(TrieIntSeqCounter counter1, TrieIntSeqCounter counter2) {
        int[] seq = new int[counter1.maxLength()];
        for (int n = 0; n < counter1.maxLength(); ++n) {
            int[] followers = counter1.integersFollowing(seq,0,n);
            for (int k = 0; k < followers.length; ++k) {
                seq[n] = followers[k];
                assertCount(counter1,counter2,seq,n+1);
            }
        }
    }

    void assertCount(TrieIntSeqCounter counter1, TrieIntSeqCounter counter2,
                     int[] seq, int length) {
        assertEquals(counter1.count(seq,0,length),
                     counter2.count(seq,0,length));
        assertEquals(counter1.extensionCount(seq,0,length),
                     counter2.extensionCount(seq,0,length));
    }


}
