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

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;


import java.io.IOException;

public class RegExTokenizerFactoryTest  {

    @Test
    public void testPositions() {
        TokenizerFactory factory
            = new RegExTokenizerFactory("[a-z]+");
        String s = "  John ran/walked to the store. ";
        //          01234567890123456789012345678901
        //          0         1         2         3
        char[] cs = s.toCharArray();
        Tokenizer tokenizer = factory.tokenizer(cs,7,31-7);
        assertEquals(-1,tokenizer.lastTokenStartPosition());
        assertEquals(-1,tokenizer.lastTokenEndPosition());
        String token = tokenizer.nextToken();
        assertEquals("ran",token);
        assertEquals(7-7,tokenizer.lastTokenStartPosition());
        assertEquals(10-7,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(7-7,tokenizer.lastTokenStartPosition());
        assertEquals(10-7,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextToken();
        assertEquals("walked",token);
        assertEquals(11-7,tokenizer.lastTokenStartPosition());
        assertEquals(17-7,tokenizer.lastTokenEndPosition());
        tokenizer.nextWhitespace();
        assertEquals(11-7,tokenizer.lastTokenStartPosition());
        assertEquals(17-7,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextToken();
        token = tokenizer.nextToken();
        token = tokenizer.nextToken();
        assertEquals(25-7,tokenizer.lastTokenStartPosition());
        assertEquals(30-7,tokenizer.lastTokenEndPosition());
        token = tokenizer.nextWhitespace();
        assertEquals(25-7,tokenizer.lastTokenStartPosition());
        assertEquals(30-7,tokenizer.lastTokenEndPosition());
        assertNull(tokenizer.nextToken());
        assertEquals(25-7,tokenizer.lastTokenStartPosition());
        assertEquals(30-7,tokenizer.lastTokenEndPosition());
    }

    @Test
    public void testOne() throws IOException, ClassNotFoundException {
    
        RegExTokenizerFactory factory 
            = new RegExTokenizerFactory("[a-zA-Z]+|[0-9]+|\\S");
        char[] cs = "abc de 123. ".toCharArray();
        String[] whites = new String[] { "", " ", " ", "", " " };
        String[] toks = new String[] { "abc", "de", "123", "." };
        int[] starts = new int[] { 0, 4, 7, 10 };

        Tokenizer tokenizer = factory.tokenizer(cs,0,cs.length);
        assertTrue(tokenizer != null);

        assertTokenize(new String(cs),whites,toks,starts,factory);
    
        TokenizerFactory factory2
            = (TokenizerFactory) AbstractExternalizable.serializeDeserialize(factory);

        assertTokenize(new String(cs),whites,toks,starts,factory2);
    }


    protected void assertTokenize(String input, 
                                  String[] whitespaces, String[] tokens, 
                                  int[] starts,
                                  TokenizerFactory factory) {

        assertArrayEquals(tokens,
                          factory.tokenizer(input.toCharArray(),
                                            0,input.length()).tokenize());

        Tokenizer tokenizer 
            = factory.tokenizer(input.toCharArray(),0,input.length());
        for (int i = 0; i < starts.length; ++i) {
            String whitespace = tokenizer.nextWhitespace();
            String token = tokenizer.nextToken();
            assertEquals("Whitespace mismatch",whitespaces[i],whitespace);
            assertEquals("Token mismatch",tokens[i],token);
            assertEquals("Last token start position mismatch",starts[i],
                         tokenizer.lastTokenStartPosition());
        }
        assertEquals("Final whitespace mismatch",
                     whitespaces[whitespaces.length-1],
                     tokenizer.nextWhitespace());
        assertNull("Should return final null",
                   tokenizer.nextToken());

    }

}
