package com.aliasi.test.unit.suffixarray;

import com.aliasi.suffixarray.TokenSuffixArray;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

import java.util.List;

public class TokenSuffixArrayTest {

    @Test
    public void testAbracadabra() {

        // abracadabra  a->XYZ, b-> UV, r -> 1234, c->tru, d->F900

        String cs = "aXYZ bUV ruzw aXYZ ctru aXYZ dF900 aXYZ bUV ruzw aXYZ ";
        //           012345678901234567890123456789012345678901234567890123456
        //           0         1         2         3         4         5
        //
        // TOKS      0    1   2    3    4    5    6     7    8   9    10

        TokenSuffixArray tsa = new TokenSuffixArray(new Tokenization(cs,
                                                                     IndoEuropeanTokenizerFactory.INSTANCE),
                                                    Integer.MAX_VALUE);
        assertNotNull(tsa);

        assertEquals(cs,tsa.tokenization().text());

        int[] expected_sa = new int[] {
            10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2
        };
        assertEquals(expected_sa.length, tsa.suffixArrayLength());
        for (int i = 0; i < expected_sa.length; ++i)
            assertEquals(expected_sa[i],tsa.suffixArray(i));

        List<int[]> prefixMatches = tsa.prefixMatches(3);

           
        assertArrayEquals(new int[] { 1, 3 }, prefixMatches.get(0));
        assertArrayEquals(new int[] { 5, 7 }, prefixMatches.get(1));
        assertEquals(2,prefixMatches.size());

        assertEquals("aXYZ bUV", tsa.substring(1,2));
        assertEquals("aXYZ bUV", tsa.substring(2,2));
        assertEquals("bUV ruzw aXYZ", tsa.substring(5,3));
        assertEquals("bUV ruzw aXYZ", tsa.substring(6,3));
    }

    @Test
    public void testEmpty() {
        String cs = "";
        TokenSuffixArray tsa = new TokenSuffixArray(new Tokenization(cs,
                                                                     IndoEuropeanTokenizerFactory.INSTANCE));
        assertEquals(cs,tsa.tokenization().text());
        assertEquals(0,tsa.suffixArrayLength());
        assertEquals(0,tsa.prefixMatches(3).size());
    }

    @Test
    public void testLengthBound() {
        String cs = "aa bb aa bb aa bb cc cc cc";
        Tokenization tokenization 
            = new Tokenization(cs,
                               IndoEuropeanTokenizerFactory.INSTANCE);
        TokenSuffixArray tsa
            = new TokenSuffixArray(tokenization,1);

        List<int[]> prefixMatches = tsa.prefixMatches(1);
        assertEquals(3,prefixMatches.size());
        for (int[] match : prefixMatches) {
            assertEquals(3,match[1]-match[0]);
            for (int j = match[0] + 1; j < match[1]; ++j)
                assertEquals(tokenization.token(tsa.suffixArray(j-1)),
                             tokenization.token(tsa.suffixArray(j)));
        }

    }

    @Test
    public void testBoundaryToken() {
        String cs = "aa bb X cc aa bb cc X";
        // TOKS      0  1  2  3 4  5  6  7
        // SA = 2, 7, 0, 4, 1, 5, 6, 3
        Tokenization tokenization
            = new Tokenization(cs,IndoEuropeanTokenizerFactory.INSTANCE);
        TokenSuffixArray tsa
            = new TokenSuffixArray(tokenization,Integer.MAX_VALUE,"X");
        List<int[]> prefixMatches = tsa.prefixMatches(2);
        assertEquals(1,prefixMatches.size());
        int[] match = prefixMatches.get(0);
        assertEquals("aa bb", tsa.substring(match[0],2));
        assertEquals("aa bb", tsa.substring(match[0]+1,2));
        
    }



}