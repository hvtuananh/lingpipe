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

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

public class IndoEuropeanTokenizerFactoryTest extends IndoEuropean {

    @Test
    public void testBeginEndsPeriods() {
        String in
            = "Mr. John Jones ran to Washington.";
        //     0123456789012345678901234567890123456789
        //     0         1         2         3
        Tokenizer tokenizer
            = IndoEuropeanTokenizerFactory
            .INSTANCE
            .tokenizer(in.toCharArray(),0,in.length());
        String token = tokenizer.nextToken();
        assertEquals("Mr",token);
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals(".",token);
        assertEquals(2,tokenizer.lastTokenStartPosition());
        assertEquals(3,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals("John",token);
        assertEquals(4,tokenizer.lastTokenStartPosition());
        assertEquals(8,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals("Jones",token);
        assertEquals(9,tokenizer.lastTokenStartPosition());
        assertEquals(14,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals("ran",token);
        assertEquals(15,tokenizer.lastTokenStartPosition());
        assertEquals(18,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals("to",token);
        assertEquals(19,tokenizer.lastTokenStartPosition());
        assertEquals(21,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals("Washington",token);
        assertEquals(22,tokenizer.lastTokenStartPosition());
        assertEquals(32,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertEquals(".",token);
        assertEquals(32,tokenizer.lastTokenStartPosition());
        assertEquals(33,tokenizer.lastTokenEndPosition());

        token = tokenizer.nextToken();
        assertNull(token);
    }

    @Test
    public void testBeginEnds() {
        String s = "foo bar 1.1 baz.";
        //          0123456789012345
        //          0         1
        char[] cs = "foo bar 1.1 baz.".toCharArray();
        Tokenizer tokenizer = IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(cs,3,cs.length-3);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals(-1,tokenizer.lastTokenEndPosition());
        String token = tokenizer.nextToken();
        assertEquals("bar",token);
        assertEquals(4-3,tokenizer.lastTokenStartPosition());
        assertEquals(7-3,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(4-3,tokenizer.lastTokenStartPosition());
        assertEquals(7-3,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextToken();
        assertEquals("1.1",token);
        assertEquals(8-3,tokenizer.lastTokenStartPosition());
        assertEquals(11-3,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(8-3,tokenizer.lastTokenStartPosition());
        assertEquals(11-3,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextToken();
        assertEquals("baz",token);
        assertEquals(12-3,tokenizer.lastTokenStartPosition());
        assertEquals(15-3,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextToken();
        assertEquals(".",token);
        assertEquals(15-3,tokenizer.lastTokenStartPosition());
        assertEquals(16-3,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertNull(tokenizer.nextToken());
        assertEquals(15-3,tokenizer.lastTokenStartPosition());
        assertEquals(16-3,tokenizer.lastTokenEndPosition());
    }


    @Override
    protected void assertTokenize(String input,
                        String[] whitespaces, String[] tokens,
                        int[] starts) {

        TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;

        assertArrayEquals(tokens,
                          factory.tokenizer(input.toCharArray(),
                                            0,input.length()).tokenize());

        Tokenizer tokenizer
            = factory.tokenizer(input.toCharArray(),0,input.length());
        for (int i = 0; i < starts.length; ++i) {
            String whitespace = tokenizer.nextWhitespace();
            String token = tokenizer.nextToken();
            assertEquals("Whitespace mismatch",whitespace,whitespaces[i]);
            assertEquals("Token mismatch",token,tokens[i]);
            assertEquals("Last token start position mismatch",
                         tokenizer.lastTokenStartPosition(),starts[i]);
        }
        assertEquals("Final whitespace mismatch",
                     whitespaces[whitespaces.length-1],
                     tokenizer.nextWhitespace());
        assertNull("Should return final null",
                   tokenizer.nextToken());

    }

    @Test
    public void testSerialization() throws ClassNotFoundException, IOException {
        IndoEuropeanTokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory compiledFactory
            = (TokenizerFactory) AbstractExternalizable.serializeDeserialize(factory);
        assertTrue(IndoEuropeanTokenizerFactory.INSTANCE
                   == compiledFactory);
    }

}
