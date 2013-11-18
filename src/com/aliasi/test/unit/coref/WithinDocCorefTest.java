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

package com.aliasi.test.unit.coref;

import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.Mention;
import com.aliasi.coref.WithinDocCoref;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class WithinDocCorefTest  {

    @Test
    public void testOne() {
        String[] phrases = new String[] {
            "Mr. John Smith",
            "John Smith",
            "Johanna Smith",
            "he",
            "IBM"
        };
        String[] entityTypes = new String[] {
            "PERSON",
            "PERSON",
            "PERSON",
            "MALE_PRONOUN",
            "ORGANIZATION"
        };
        int[] offsets = new int[] {
            1, 2, 3, 3, 3
        };
        int[] expectedIds = new int[] {
            0, 0, 1, 1, 2
        };
        assertCoref(phrases,entityTypes,offsets,expectedIds);
    }

    @Test
    public void testTwo() {
        String[] phrases = new String[] {
            "Mr. John Smith",
            "John Smith",
            "Johanna Smith",
            "he",
            "IBM"
        };
        String[] entityTypes = new String[] {
            "PERSON",
            "PERSON",
            "PERSON",
            "MALE_PRONOUN",
            "ORGANIZATION"
        };
        int[] offsets = new int[] {
            1, 2, 3, 3, 3
        };
        int[] expectedIds = new int[] {
            0, 0, 1, 1, 2
        };
        assertCoref(phrases,entityTypes,offsets,expectedIds);
    }

    public void assertCoref(String[] phrases, String[] entityTypes,
                            int[] offsets, int[] refIds) {
        assertEquals(phrases.length,entityTypes.length);
        assertEquals(phrases.length,offsets.length);
        assertEquals(refIds.length,offsets.length);
        EnglishMentionFactory factory = new EnglishMentionFactory();
        WithinDocCoref coref = new WithinDocCoref(factory);
        for (int i = 0; i < phrases.length; ++i) {
            Mention mention = factory.create(phrases[i],
                                             entityTypes[i]);
            assertEquals(refIds[i], coref.resolveMention(mention,offsets[i]));
        }
    }

}
