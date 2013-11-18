package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;


import static com.aliasi.test.unit.Asserts.assertNotSerializable;
import static com.aliasi.test.unit.tokenizer.TokenizerTest.assertFactory;

public class PorterStemmerTokenizerFactoryTest {

    @Test
    public void testStartEnd() {
        PorterStemmerTokenizerFactory factory
            = new PorterStemmerTokenizerFactory(IndoEuropeanTokenizerFactory
                                                .INSTANCE);
        String s = "going running hastens  unfriendly things. Then";
        //          01234567890123456789012
        //               01234567890123456789012345678901234567890
        //               0         1         2         3         4
        char[] cs = s.toCharArray();
        Tokenizer tokenizer = factory.tokenizer(cs,5,cs.length-5-4);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals(-1,tokenizer.lastTokenEndPosition());
        tokenizer.nextToken();
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(8,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(8,tokenizer.lastTokenEndPosition());

        tokenizer.nextToken();
        assertEquals(9,tokenizer.lastTokenStartPosition());
        assertEquals(16,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(9,tokenizer.lastTokenStartPosition());
        assertEquals(16,tokenizer.lastTokenEndPosition());

        tokenizer.nextToken();
        assertEquals(18,tokenizer.lastTokenStartPosition());
        assertEquals(28,tokenizer.lastTokenEndPosition());

        tokenizer.nextToken();
        assertEquals(29,tokenizer.lastTokenStartPosition());
        assertEquals(35,tokenizer.lastTokenEndPosition());

        tokenizer.nextToken();
        assertEquals(35,tokenizer.lastTokenStartPosition());
        assertEquals(36,tokenizer.lastTokenEndPosition());

    }

    @Test
    public void testFactory() {
        TokenizerFactory ieFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory factory
            = new PorterStemmerTokenizerFactory(ieFactory);
        assertFactory(factory,
                      "");
        assertFactory(factory,
                      "a",
                      "a");
        assertFactory(factory,
                      "A",
                      "A");
        assertFactory(factory,
                      "The starling is flying towards home smiling happily",
                      PorterStemmerTokenizerFactory.stem("The"),
                      PorterStemmerTokenizerFactory.stem("starling"),
                      PorterStemmerTokenizerFactory.stem("is"),
                      PorterStemmerTokenizerFactory.stem("flying"),
                      PorterStemmerTokenizerFactory.stem("towards"),
                      PorterStemmerTokenizerFactory.stem("home"),
                      PorterStemmerTokenizerFactory.stem("smiling"),
                      PorterStemmerTokenizerFactory.stem("happily"));
    }

    @Test
    public void testNotSerializable() {
        PorterStemmerTokenizerFactory unserializable
            = new PorterStemmerTokenizerFactory(TokenizerTest
                                                .UNSERIALIZABLE_FACTORY);
        assertNotSerializable(unserializable);
    }

    // no tests of stemming itself as we're using Porter's port.
    // we don't  have definitions of what it's supposed to do.

}
