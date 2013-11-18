package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;


import java.util.ArrayList;
import java.util.Arrays;

public class ConstantTokenizerTest  {

    private final char[] EMPTY_CHARS = new char[0];

    @Test
    public void testConstants() {
        String[] toks = new String[] { "John", "Smith", "rocks", "." };
        String[] whites = new String[] { "", " ", " ", " ", "" };
        TokenizerFactory tf
            = new ConstantTokenizerFactory(toks,whites);
        assertArrayEquals(toks,tf.tokenizer(EMPTY_CHARS,0,0).tokenize());

        Tokenizer t = tf.tokenizer(EMPTY_CHARS,0,0);
        ArrayList tokList = new ArrayList();
        ArrayList whiteList = new ArrayList();
        t.tokenize(tokList,whiteList);
        assertEquals(Arrays.asList(toks),tokList);
        assertEquals(Arrays.asList(whites),whiteList);
        assertNull(t.nextToken());
        assertEquals("",t.nextWhitespace());
        assertEquals("",t.nextWhitespace());
        assertNull(t.nextToken());
    }
}
