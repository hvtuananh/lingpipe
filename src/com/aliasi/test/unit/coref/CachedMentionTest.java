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

import com.aliasi.coref.CachedMention;
import com.aliasi.coref.Mention;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;


import java.util.HashSet;
import java.util.Set;

public class CachedMentionTest  {

    @Test
    public void testOne() {
        String phrase = "Foo bar baz";
        String entityType = "theType";
        Set honorifics = new HashSet();
        honorifics.add("baz");
        String gender = "it";
        boolean isPronominal = false;
        String[] normalTokens = new String[] { "foo", "bar" };
        Mention m1 = new CachedMention(phrase,entityType,
                                       honorifics,normalTokens,
                                       gender,isPronominal);
        Mention m2 = new CachedMention(phrase,entityType,
                                       honorifics,normalTokens,
                                       gender,isPronominal);
        assertFalse(m1.equals(m2));
        // assertFullEquals(m1,m2);
        assertEquals(phrase,m1.phrase());
        assertEquals(entityType,m1.entityType());
        assertEquals(honorifics,m1.honorifics());
        assertEquals("foo bar",m1.normalPhrase());
        assertEquals(gender,m1.gender());
        assertFalse(m1.isPronominal());
        assertArrayEquals(normalTokens,m1.normalTokens());
    }

}
