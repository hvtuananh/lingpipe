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
import com.aliasi.coref.Killer;
import com.aliasi.coref.Matcher;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChainImpl;
import com.aliasi.coref.matchers.*;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;


import java.util.HashSet;
import java.util.Set;

public class MentionChainImplTest  {

    @Test
    public void testOne() {
        String phrase = "Mr. John Smith";
        String entityType = "PERSON";
        Set honorifics = new HashSet();
        honorifics.add("mr");
        String gender = null;
        boolean isPronominal = false;
        String[] normalTokens = new String[] { "john", "smith" };
        Mention m1 = new CachedMention(phrase,entityType,
                                       honorifics,normalTokens,
                                       gender,isPronominal);
        MentionChainImpl mc = new MentionChainImpl(m1,7,0);
        assertNotNull(mc);

        Matcher[] matchers = mc.matchers();
        Class[] matcherClasses = new Class[] {
            ExactPhraseMatch.class,
            EntityTypeMatch.class,
            EntityTypeMatch.class,
            SequenceSubstringMatch.class,
            SynonymMatch.class
        };
        assertArrayClass(matchers,matcherClasses);
        Killer[] killers = mc.killers();
        Class[] killerClasses = new Class[] {
            GenderKiller.class,
            HonorificConflictKiller.class
        };
        assertArrayClass(killers,killerClasses);

        HashSet expectedMentions = new HashSet();
        expectedMentions.add(m1);
        assertEquals(expectedMentions,mc.mentions());
        assertEquals(honorifics,mc.honorifics());
        assertNull(mc.gender());
        assertEquals(7,mc.maxSentenceOffset());
        assertEquals("PERSON",mc.entityType());
        assertEquals(0,mc.identifier());


        String phrase2 = "Dr. John Joseph Smith";
        String entityType2 = "PERSON";
        Set honorifics2 = new HashSet();
        honorifics2.add("dr");
        String gender2 = null;
        boolean isPronominal2 = false;
        String[] normalTokens2 = new String[] { "john", "joseph", "smith" };
        Mention m2 = new CachedMention(phrase2,entityType2,
                                       honorifics2,normalTokens2,
                                       gender2,isPronominal2);
        mc.add(m2,9);
        expectedMentions.add(m2);
        assertEquals(expectedMentions,mc.mentions());
        assertArrayClass(mc.matchers(),matcherClasses);
        assertArrayClass(mc.killers(),killerClasses);
        HashSet expectedHonorifics = new HashSet();
        expectedHonorifics.add("mr");
        expectedHonorifics.add("dr");
        assertEquals(expectedHonorifics,mc.honorifics());
        assertNull(mc.gender());
        assertEquals(9,mc.maxSentenceOffset());
        assertEquals("PERSON",mc.entityType());
        assertEquals(0,mc.identifier());

        String phrase3 = "He";
        String entityType3 = "MALE_PRONOUN";
        Set honorifics3 = new HashSet();
        String gender3 = "male";
        boolean isPronominal3 = true;
        String[] normalTokens3 = new String[] { "he" };
        Mention m3 = new CachedMention(phrase3,entityType3,
                                       honorifics3,normalTokens3,
                                       gender3,isPronominal3);
        mc.add(m3,9);
        expectedMentions.add(m3);
        assertEquals(expectedMentions,mc.mentions());
        Class[] matcherClasses2 = new Class[] {
            ExactPhraseMatch.class,
            EntityTypeMatch.class,
            SequenceSubstringMatch.class,
            SynonymMatch.class
        };
        assertArrayClass(mc.matchers(),matcherClasses2);
        assertArrayClass(mc.killers(),killerClasses);
        assertEquals(expectedHonorifics,mc.honorifics());
        assertEquals("male",mc.gender());
        assertEquals(9,mc.maxSentenceOffset());
        assertEquals("PERSON",mc.entityType());
        assertEquals(0,mc.identifier());

        assertEquals(1,new MentionChainImpl(m2,17,1).identifier());
        assertEquals(2,new MentionChainImpl(m2,19,2).identifier());

        String phrase4 = "He";
        String entityType4 = "MALE_PRONOUN";
        Set honorifics4 = new HashSet();
        String gender4 = "male";
        boolean isPronominal4 = true;
        String[] normalTokens4 = new String[] { "he" };
        Mention m4 = new CachedMention(phrase4,entityType4,
                                       honorifics4,normalTokens4,
                                       gender4,isPronominal4);
        assertFalse(mc.killed(m4));
        assertEquals(1,mc.matchScore(m4)); // male pronoun match

        String phrase5 = "Dr. John Joseph Smith";
        String entityType5 = "PERSON";
        Set honorifics5 = new HashSet();
        honorifics5.add("dr");
        String gender5 = null;
        boolean isPronominal5 = false;
        String[] normalTokens5 = new String[] { "john", "joseph", "smith" };
        Mention m5 = new CachedMention(phrase5,entityType5,
                                       honorifics5,normalTokens5,
                                       gender5,isPronominal5);
        assertEquals(1,mc.matchScore(m5)); // exact match

        String phrase6 = "she";
        String entityType6 = "FEMALE_PRONOUN";
        Set honorifics6 = new HashSet();
        String gender6 = null;
        boolean isPronominal6 = true;
        String[] normalTokens6 = new String[] { "she" };
        Mention m6 = new CachedMention(phrase6,entityType6,
                                       honorifics6,normalTokens6,
                                       gender6,isPronominal6);

        assertEquals(Matcher.NO_MATCH_SCORE,mc.matchScore(m6)); // no match

        String phrase7 = "Mrs. Johanna Smith";
        String entityType7 = "PERSON";
        Set honorifics7 = new HashSet();
        honorifics7.add("mrs");
        String gender7 = null;
        boolean isPronominal7 = false;
        String[] normalTokens7 = new String[] { "johanna", "smith" };
        Mention m7 = new CachedMention(phrase7,entityType7,
                                       honorifics7,normalTokens7,
                                       gender7,isPronominal7);
        assertTrue(mc.killed(m7)); // honorific conflict

        String phrase8 = "Johanna Smith";
        String entityType8 = "PERSON";
        Set honorifics8 = new HashSet();
        String gender8 = "female";
        boolean isPronominal8 = false;
        String[] normalTokens8 = new String[] { "johanna", "smith" };
        Mention m8 = new CachedMention(phrase8,entityType8,
                                       honorifics8,normalTokens8,
                                       gender8,isPronominal8);
        assertTrue(mc.killed(m8)); // gender conflict

    }

    private void assertArrayClass(Object[] xs, Class[] cs) {
        assertEquals("Expected same length.",xs.length,cs.length);
        for (int i = 0; i < xs.length; ++i)
            assertEquals("Expected class=" + cs[i]
                         + " Found class=" + xs[i].getClass(),
                         xs[i].getClass(),cs[i]);
    }


}
