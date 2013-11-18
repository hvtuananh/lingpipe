package com.aliasi.test.unit.suffixarray;

import com.aliasi.suffixarray.CharSuffixArray;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

import java.util.List;

public class CharSuffixArrayTest {

    @Test
    public void testAbracadabra() {
        String cs = "abracadabra";
        CharSuffixArray csa = new CharSuffixArray(cs);

        assertEquals(cs,csa.text());

        int[] expected_sa = new int[] {
            10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2
        };
        assertEquals(expected_sa.length,csa.suffixArrayLength());
        for (int i = 0; i < expected_sa.length; ++i)
            assertEquals(expected_sa[i], csa.suffixArray(i));

        List<int[]> prefixMatches = csa.prefixMatches(3);

        // for (int[] match : prefixMatches) 
        //     System.out.println("match=" + match[0] + " " + match[1] );
           
        assertArrayEquals(new int[] { 1, 3 }, prefixMatches.get(0));
        assertArrayEquals(new int[] { 5, 7 }, prefixMatches.get(1));
        assertEquals(2,prefixMatches.size());
    }

    @Test
    public void testEmpty() {
        String cs = "";
        CharSuffixArray csa = new CharSuffixArray(cs);
        assertEquals(cs,csa.text());
        assertEquals(0,csa.suffixArrayLength());
        assertEquals(0,csa.prefixMatches(3).size());
    }

    @Test
    public void testBound() {
        String cs = "abababccc";
        CharSuffixArray csa = new CharSuffixArray(cs,1);
        // can't compute expected array, as undefined,
        List<int[]> prefixMatches = csa.prefixMatches(1);
        assertEquals(3,prefixMatches.size());
        for (int[] match : prefixMatches) {
            assertEquals(3,match[1]-match[0]);
            for (int j = match[0] + 1; j < match[1]; ++j)
                assertEquals(cs.charAt(csa.suffixArray(j-1)),
                             cs.charAt(csa.suffixArray(j)));
        }
    }


}

