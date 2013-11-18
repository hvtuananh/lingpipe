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

package com.aliasi.test.unit.coref.matchers;

import com.aliasi.coref.CachedMention;
import com.aliasi.coref.Killer;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChainImpl;
import com.aliasi.coref.matchers.*;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


import java.util.HashSet;
import java.util.Set;

public class GenderKillerTest  {


    @Test
    public void testOne() {
        String phrase = "Mr. John Smith";
        String entityType = "PERSON";
        Set honorifics = new HashSet();
        honorifics.add("mr");
        String gender = "male";
        boolean isPronominal = false;
        String[] normalTokens = new String[] { "john", "smith" };
        Mention m1 = new CachedMention(phrase,entityType,
                                       honorifics,normalTokens,
                                       gender,isPronominal);


        MentionChainImpl mc = new MentionChainImpl(m1,7,0);

        String phrase2 = "John Smith";
        String entityType2 = "ORGANIZATION";
        String gender2 = "female";
        Mention m2 = new CachedMention(phrase2,entityType2,
                                       honorifics,normalTokens,
                                       gender2,isPronominal);


        Killer k = new GenderKiller();
        assertTrue(k.kill(m2,mc));

        String phrase3 = "Mr. Johan Smith";
        String entityType3 = "PERSON";
        String[] normalTokens3 = new String[] { "johan", "smith" };
        String gender3 = null;
        Mention m3 = new CachedMention(phrase3,entityType3,
                                       honorifics,normalTokens3,
                                       gender3,isPronominal);

        assertFalse(k.kill(m3,mc));

        String phrase4 = "Mr. Johan Smith";
        String entityType4 = "PERSON";
        String[] normalTokens4 = new String[] { "johan", "smith" };
        String gender4 = "male";
        Mention m4 = new CachedMention(phrase4,entityType4,
                                       honorifics,normalTokens4,
                                       gender4,isPronominal);
        assertFalse(k.kill(m4,mc));
    }
}
