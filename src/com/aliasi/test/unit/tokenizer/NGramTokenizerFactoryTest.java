package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import static org.junit.Assert.assertArrayEquals;


public class NGramTokenizerFactoryTest  {

    @Test
    public void testStartEnd() {
        String s = "abc defg b";
        //          0123456789
        //            01234
        char[] cs = s.toCharArray();
        Tokenizer tokenizer
            = new NGramTokenizerFactory(1,2).tokenizer(cs,2,5);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals(-1,tokenizer.lastTokenEndPosition());
        String token = tokenizer.nextToken();
        assertEquals("c",token);
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals(1,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals(1,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextToken();
        assertEquals(" ",token);
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());
        assertEquals("d",tokenizer.nextToken());
        assertEquals("e",tokenizer.nextToken());
        assertEquals(3,tokenizer.lastTokenStartPosition());
        assertEquals(4,tokenizer.lastTokenEndPosition());
        assertEquals("f",tokenizer.nextToken());
        assertEquals(4,tokenizer.lastTokenStartPosition());
        assertEquals(5,tokenizer.lastTokenEndPosition());

        assertEquals("c ",tokenizer.nextToken());
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());
        assertEquals(" d",tokenizer.nextToken());
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(3,tokenizer.lastTokenEndPosition());
        assertEquals("de",tokenizer.nextToken());
        assertEquals(2,tokenizer.lastTokenStartPosition());
        assertEquals(4,tokenizer.lastTokenEndPosition());
        assertEquals("ef",tokenizer.nextToken());
        assertEquals(3,tokenizer.lastTokenStartPosition());
        assertEquals(5,tokenizer.lastTokenEndPosition());
        assertNull(tokenizer.nextToken());
        assertEquals(3,tokenizer.lastTokenStartPosition());
        assertEquals(5,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(3,tokenizer.lastTokenStartPosition());
        assertEquals(5,tokenizer.lastTokenEndPosition());
    }

    @Test
    public void test1() {
        char[] cs = "abcd".toCharArray();
        assertArrayEquals(new String[] { "ab", "bc", "cd", "abc", "bcd" },
                          new NGramTokenizerFactory(2,3)
                          .tokenizer(cs,0,cs.length)
                          .tokenize());
        assertArrayEquals(new String[] { "a", "b", "c", "d" },
                          new NGramTokenizerFactory(1,1)
                          .tokenizer(cs,0,cs.length)
                          .tokenize());
    }

    @Test(expected=IllegalArgumentException.class)
    public void test1Exc() {
            new NGramTokenizerFactory(3,2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test1ExcB() {
        new NGramTokenizerFactory(-2,0);
    }

    @Test
    public void test2() {
        char[] cs = "he".toCharArray();
        assertArrayEquals(new String[] { "he" },
                          new NGramTokenizerFactory(2,4)
                          .tokenizer(cs,0,cs.length)
                          .tokenize());
    }

    @Test
    public void test3() {
        char[] cs = "abcd".toCharArray();
        Tokenizer tokenizer = new NGramTokenizerFactory(2,3).tokenizer(cs,0,cs.length);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals("ab",tokenizer.nextToken());
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals("bc",tokenizer.nextToken());
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals("cd",tokenizer.nextToken());
        assertEquals(2,tokenizer.lastTokenStartPosition());
        assertEquals("abc",tokenizer.nextToken());
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals("bcd",tokenizer.nextToken());
        assertEquals(1,tokenizer.lastTokenStartPosition());
    }

}
