/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.tokenizer;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.io.Serializable;

public class TokenizerTest  {

    @Test
    public void testAbstractTokenizerStart() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        boolean threw = false;
        try {
            testTokenizer.lastTokenStartPosition();
        } catch (UnsupportedOperationException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void testAbstractTokenizerWhitespace() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        for (int i = 0; i < 20; ++i)
            assertEquals(Strings.SINGLE_SPACE_STRING,
                         testTokenizer.nextWhitespace());
        for (int i = 0; i < 20; ++i) testTokenizer.nextToken();
        assertEquals(Strings.SINGLE_SPACE_STRING,
                     testTokenizer.nextWhitespace());
    }

    @Test
    public void testAbstractTokenizerNext() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        for (int i = 1; i <= 10; ++i) {
            assertEquals(String.valueOf(i),
                         testTokenizer.nextToken());
        }
        assertNull(testTokenizer.nextToken());
    }

    @Test
    public void testAbstractTokenizerToArray() throws IOException {
        Tokenizer testTokenizer = new TestTokenizer();
        String[] answer = new String[10];
        for (int i = 0; i < 10; ++i)
            answer[i] = String.valueOf(i+1);
        assertArrayEquals(answer,
                          testTokenizer.tokenize());
    }

    @Test
    public void testIterability() {
        Tokenizer tokenizer = new TestTokenizer();
        int count = 0;
        for (String token : tokenizer) {
            ++count;
            assertEquals(String.valueOf(count),token);
        }
        assertEquals(10,count);
    }

    private static class TestTokenizer extends Tokenizer {
        private int count = 0;
        @Override
        public String nextToken() {
            return (count++ < 10)
                ? String.valueOf(count)
                : null;
        }
    }

    public static void assertFactory(TokenizerFactory factory,
                                     String input,
                                     String... tokens) {
        assertFactory(factory,input,tokens,null);
    }

    public static void assertFactory(TokenizerFactory factory,
                                     String input,
                                     String[] tokens,
                                     String[] whitespaces) {
        assertTokenization(factory,input,tokens,whitespaces);
        if (!(factory instanceof Serializable)) return;
        try {
            TokenizerFactory deserializedFactory
                = (TokenizerFactory) 
                AbstractExternalizable
                .serializeDeserialize((Serializable) factory);
            assertTokenization(deserializedFactory,input,tokens,whitespaces);
        } catch (IOException e) {
            fail(e.toString());
        }
    }


    public static void assertTokenization(TokenizerFactory factory,
                                          String input,
                                          String[] tokens,
                                          String[] whitespaces) {
        Tokenizer tokenizer = factory.tokenizer(input.toCharArray(),
                                                0,input.length());
        if (whitespaces != null)
            assertEquals(whitespaces[0],tokenizer.nextWhitespace());
        for (int i = 0; i < tokens.length; ++i) {
            assertEquals(tokens[i],tokenizer.nextToken());
            if (whitespaces != null)
                assertEquals(whitespaces[i+1],tokenizer.nextWhitespace());
        }
        assertNull(tokenizer.nextToken());
    }

    static final TokenizerFactory UNSERIALIZABLE_FACTORY
        = new TokenizerFactory() {
                public Tokenizer tokenizer(char[] cs, int start, int len) {
                    return null;
                }
                public Tokenizer transform(String s) {
                    return tokenizer(s.toCharArray(),0,s.length());
                }
            };


}
