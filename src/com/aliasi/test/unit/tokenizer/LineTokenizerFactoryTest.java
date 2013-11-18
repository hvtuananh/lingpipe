package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.LineTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

public class LineTokenizerFactoryTest  {

    @Test
    public void testSingleton() throws IOException {
        LineTokenizerFactory fact = LineTokenizerFactory.INSTANCE;
        Object deserFact = AbstractExternalizable.serializeDeserialize(fact);
        assertTrue(fact == deserFact);
    }
    

    @Test
    public void testOne() {
        assertTokenizer("",
                        new String[] { },
                        new String[] { "" });

        assertTokenizer("abc",
                        new String[] { "abc" },
                        new String[] { "", "" });

        assertTokenizer("abc\n",
                        new String[] { "abc" },
                        new String[] { "", "\n" });

        assertTokenizer("  \n",
                        new String[] { "  " },
                        new String[] { "", "\n" });

        assertTokenizer("abc\n def ",
                        new String[] { "abc", " def " },
                        new String[] { "", "\n", "" });

        assertTokenizer("abc\r\ndef",
                        new String[] { "abc", "def" },
                        new String[] { "", "\r\n", "" });

        assertTokenizer("abc\rdef",
                        new String[] { "abc", "def" },
                        new String[] { "", "\r", "" });

        assertTokenizer("abc\u2029def",
                        new String[] { "abc", "def" },
                        new String[] { "", "\u2029", "" });


    }

    void assertTokenizer(String input,
                         String[] tokens,
                         String[] whitespaces) {
        LineTokenizerFactory tf = LineTokenizerFactory.INSTANCE;
        Tokenizer tokenizer = tf.tokenizer(input.toCharArray(),0,input.length());
        ArrayList<String> tokenList = new ArrayList<String>();
        ArrayList<String> whiteList = new ArrayList<String>();
        tokenizer.tokenize(tokenList,whiteList);
        assertEquals(Arrays.<String>asList(tokens),tokenList);
        if (whitespaces != null)
            assertEquals(Arrays.<String>asList(whitespaces),whiteList);
    }

}
