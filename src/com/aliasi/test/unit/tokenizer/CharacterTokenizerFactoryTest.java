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

import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.io.IOException;
import java.io.Serializable;

public class CharacterTokenizerFactoryTest  {

    @Test
    public void testPositions() {
        char[] cs = new char[] { 'a', 'b', 'c', 'd', 'e' };
        Tokenizer tokenizer = CharacterTokenizerFactory.INSTANCE.tokenizer(cs,1,2);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals(-1,tokenizer.lastTokenEndPosition());
        String t = tokenizer.nextToken();
        assertEquals("b",t);
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals(1,tokenizer.lastTokenEndPosition());
        String t2 = tokenizer.nextToken();
        assertEquals("c",t2);
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());
        String t3 = tokenizer.nextToken();
        assertNull(t3);
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());

        cs = new char[] { 'a', 'b', ' ', 'c', ' ' };
        tokenizer = CharacterTokenizerFactory.INSTANCE.tokenizer(cs,0,cs.length);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals(-1,tokenizer.lastTokenEndPosition());
        t = tokenizer.nextToken();
        assertEquals("a",t);
        assertEquals(0,tokenizer.lastTokenStartPosition());
        assertEquals(1,tokenizer.lastTokenEndPosition());
        t2 = tokenizer.nextToken();
        assertEquals("b",t2);
        assertEquals(1,tokenizer.lastTokenStartPosition());
        assertEquals(2,tokenizer.lastTokenEndPosition());
        t3 = tokenizer.nextToken();
        assertEquals("c",t3);
        assertEquals(3,tokenizer.lastTokenStartPosition());
        assertEquals(4,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(3,tokenizer.lastTokenStartPosition()); // bad here
        assertEquals(4,tokenizer.lastTokenEndPosition());
        assertNull(tokenizer.nextToken());
        assertEquals(3,tokenizer.lastTokenStartPosition());
        assertEquals(4,tokenizer.lastTokenEndPosition());
    }

    @Test
    public void testTokenize() {
    assertTokenize("abc",
               new String[] { "a", "b", "c" });
    assertTokenize("",
               new String[] { });
    }

    void assertTokenize(String input, String[] tokens) {
        TokenizerFactory factory = CharacterTokenizerFactory.INSTANCE;
        Tokenizer tokenizer
            = factory.tokenizer(input.toCharArray(),0,input.length());
        for (int i = 0; i < tokens.length; ++i) {
            assertEquals("",tokenizer.nextWhitespace());
            assertEquals(tokens[i],tokenizer.nextToken());
        }
        assertEquals("",tokenizer.nextWhitespace());
        assertNull(tokenizer.nextToken());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        TokenizerFactory factory = CharacterTokenizerFactory.INSTANCE;
        TokenizerFactory compiledFactory
            = (TokenizerFactory) AbstractExternalizable.serializeDeserialize((Serializable)factory);
        assertEquals(CharacterTokenizerFactory.INSTANCE,
                     compiledFactory);
    }

}
