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
import com.aliasi.coref.MentionFactory;
import com.aliasi.coref.MentionChain;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;


import java.util.HashSet;

public class EnglishMentionFactoryTest  {

    @Test
    public void testOne() {
        MentionFactory factory = new EnglishMentionFactory();

        Mention fm1 = factory.create("Mr. John Smith", "PERSON");
        assertEquals("Mr. John Smith",fm1.phrase());
        assertEquals("PERSON",fm1.entityType());
        HashSet honorifics = new HashSet();
        honorifics.add("mr");
        assertEquals(honorifics,fm1.honorifics());
        assertFalse(fm1.isPronominal());
        assertArrayEquals(new String[] { "john", "smith" },
                          fm1.normalTokens());

        MentionChain fc1 = factory.promote(fm1,7);

        HashSet mentions = new HashSet();
        mentions.add(fm1);
        assertEquals(mentions,fc1.mentions());
        assertEquals("PERSON",fc1.entityType());
        assertEquals(honorifics,fc1.honorifics());

        Mention fm2 = factory.create("He", "MALE_PRONOUN");
        assertEquals("He",fm2.phrase());
        assertEquals("MALE_PRONOUN", fm2.entityType());
        HashSet honorifics2 = new HashSet();
        assertEquals(honorifics2,fm2.honorifics());
        assertTrue(fm2.isPronominal());
        assertArrayEquals(new String[] { "he" }, fm2.normalTokens());

        MentionChain fc2 = factory.promote(fm2,19);
        HashSet mentions2 = new HashSet();
        mentions2.add(fm2);
        assertEquals(mentions2,fc2.mentions());
        assertEquals("MALE_PRONOUN",fc2.entityType());
        assertEquals(honorifics2,fc2.honorifics());

    }

}
