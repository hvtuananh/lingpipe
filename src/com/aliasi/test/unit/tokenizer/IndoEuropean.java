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

import org.junit.Test;



public abstract class IndoEuropean  {

    private static final String INPUT0 = "";
    private static final String[] WHITESPACES0 = { "" };
    private static final String[] TOKENS0 = { };
    private static final int[] STARTS0 = { };


    private static final String INPUT1 = "Hello world";
    private static final String[] TOKENS1 = { "Hello", "world" };
    private static final String[] WHITESPACES1 = { "", " ", "" };
    private static final int[] STARTS1 = { 0, 6 };

    private static final String INPUT2 = "\"This is a test (or two),\" John said.";
    private static final String[] TOKENS2
        = { "\"", "This", "is", "a", "test", "(", "or", "two", ")",
            ",", "\"", "John", "said", "." };
    private static final String[] WHITESPACES2
        = { "", "", " ", " ", " ", " ", "", " ", "", "", "", " ", " ", "", "" };
    private static final int[] STARTS2
        = { 0,1,6,9,11,16,17,20,23,24,25,27,32,36 };

    private static final String INPUT3 = "  a";
    private static final String[] TOKENS3 = { "a" };
    private static final String[] WHITESPACES3 = { "  ", "" };
    private static final int[] STARTS3 = { 2 };

    private static final String INPUT4 = "a  \n  ";
    private static final String[] TOKENS4 = { "a" };
    private static final String[] WHITESPACES4 = { "", "  \n  " };
    private static final int[] STARTS4 = { 0 };

    private static final String INPUT5 = "1.01";
    private static final String[] TOKENS5 = { "1.01" };
    private static final String[] WHITESPACES5 = { "", "" };
    private static final int[] STARTS5 = { 0 };

    private static final String INPUT6 = "1,000,000";
    private static final String[] TOKENS6 = { "1,000,000" };
    private static final String[] WHITESPACES6 = { "", "" };
    private static final int[] STARTS6 = { 0 };

    private static final String INPUT7 = "and ...";
    private static final String[] TOKENS7 = { "and", "..." };
    private static final String[] WHITESPACES7 = { "", " ", "" };
    private static final int[] STARTS7 = { 0, 4 };

    private static final String INPUT8 = "and).";
    private static final String[] TOKENS8 = { "and", ")", "." };
    private static final String[] WHITESPACES8 = { "", "", "", "" };
    private static final int[] STARTS8 = { 0, 3, 4 };

    private static final String INPUT9 = "p-53.";
    private static final String[] TOKENS9 = { "p", "-", "53", "." };
    private static final String[] WHITESPACES9 = { "", "", "", "", "" };
    private static final int[] STARTS9 = { 0, 1, 2, 4 };

    @Test
    public void test() {
        assertTokenize(INPUT0,WHITESPACES0,TOKENS0,STARTS0);
        assertTokenize(INPUT1,WHITESPACES1,TOKENS1,STARTS1);
        assertTokenize(INPUT2,WHITESPACES2,TOKENS2,STARTS2);
        assertTokenize(INPUT3,WHITESPACES3,TOKENS3,STARTS3);
        assertTokenize(INPUT4,WHITESPACES4,TOKENS4,STARTS4);
        assertTokenize(INPUT5,WHITESPACES5,TOKENS5,STARTS5);
        assertTokenize(INPUT6,WHITESPACES6,TOKENS6,STARTS6);
        assertTokenize(INPUT7,WHITESPACES7,TOKENS7,STARTS7);
        assertTokenize(INPUT8,WHITESPACES8,TOKENS8,STARTS8);
        assertTokenize(INPUT9,WHITESPACES9,TOKENS9,STARTS9);
    }
    
    abstract protected void assertTokenize(String input, 
                       String[] whitespaces, 
                       String[] tokens, 
                       int[] starts);

}
