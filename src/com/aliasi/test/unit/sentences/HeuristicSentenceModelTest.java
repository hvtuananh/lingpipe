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

package com.aliasi.test.unit.sentences;

import com.aliasi.sentences.HeuristicSentenceModel;
import com.aliasi.sentences.SentenceModel;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;


import java.util.Arrays;
import java.util.HashSet;


public class HeuristicSentenceModelTest  {

    @Test
    public void testBoundaries() {
        assertBoundaries(new String[] { },
                         new String[] { "" },
                         new int[] { });
        assertBoundaries(new String[] { "a" },
                         new String[] { "", ""},
                         new int[] { 0 });
        assertBoundaries(new String[] { ".", "a" },
                         new String[] { "", "", ""},
                         new int[] { 1 });
        assertBoundaries(new String[] { "a", "." },
                         new String[] { "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { ".", "a", "." },
                         new String[] { "", "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { "X", "a" },
                         new String[] { "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { "a", "1" },
                         new String[] { "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { "X", "a", "." },
                         new String[] { "", "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { ".", "a", "1" },
                         new String[] { "", "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { "a", "zoo" },
                         new String[] { "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { "a", "Zoo" },
                         new String[] { "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { ".", "a", "zoo" },
                         new String[] { "", "", "", ""},
                         new int[] { });
        assertBoundaries(new String[] { ".", "a", "Zoo" },
                         new String[] { "", "", "", ""},
                         new int[] { });
    }

    private void assertBoundaries(String[] tokens, String[] whitespaces,
                                  int[] boundaries) {

        HashSet stops = new HashSet();
        stops.add("A");
        stops.add("B");
        stops.add("a");
        stops.add("b");
        HashSet badPrevious = new HashSet();
        badPrevious.add("X");
        badPrevious.add("Y");
        HashSet badFollowing = new HashSet();
        badFollowing.add("1");
        badFollowing.add("2");
        SentenceModel model
            = new HeuristicSentenceModel(stops,badPrevious,badFollowing);

        assertBoundaries(model,tokens,whitespaces,0,tokens.length,
                         boundaries);

        String[] tokens2 = new String[tokens.length+10];
        String[] whitespaces2 = new String[whitespaces.length+10];
        Arrays.fill(tokens2,"boo");
        Arrays.fill(whitespaces2," ");
        for (int i = 0; i < tokens.length; ++i) {
            tokens2[i+5] = tokens[i];
            whitespaces2[i+5] = whitespaces[i];
        }
        int[] boundaries2 = new int[boundaries.length];
        for (int i = 0; i < boundaries.length; ++i)
            boundaries2[i] = boundaries[i]+5;

        assertBoundaries(model,tokens2,whitespaces2,5,tokens.length,
                         boundaries2);

    }

    private void assertBoundaries(SentenceModel model,
                                  String[] tokens, String[] whitespaces,
                                  int start, int length,
                                  int[] boundaries) {

        // full API call
        HashSet boundariesSet = new HashSet();
            model.boundaryIndices(tokens,whitespaces,start,length,
                                  boundariesSet);
        assertEquals(boundaries.length,boundariesSet.size());
        for (int i = 0; i < boundaries.length; ++i)
            assertTrue(boundariesSet.contains(Integer.valueOf(boundaries[i])));

        // simple API call
        if (start == 0 && length == tokens.length) {
            int[] boundariesDerived
                = model.boundaryIndices(tokens,whitespaces);
            assertArrayEquals(boundaries,boundariesDerived);
        }
    }

}
