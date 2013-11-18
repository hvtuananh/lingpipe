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
import com.aliasi.coref.Matcher;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChainImpl;
import com.aliasi.coref.matchers.*;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.util.HashSet;
import java.util.Set;

public class SequenceSubstringMatchTest  {


    @Test
    public void testOne() {
        String phrase = "A B C D E";
        String entityType = "PERSON";
        Set honorifics = new HashSet();
        honorifics.add("mr");
        String gender = null;
        boolean isPronominal = false;
        String[] normalTokens = new String[] { "A", "B", "C", "D", "E" };
        Mention m1 = new CachedMention(phrase,entityType,
                                       honorifics,normalTokens,
                                       gender,isPronominal);


        MentionChainImpl mc = new MentionChainImpl(m1,7,0);

        String phrase2 = "A B C D";
        String[] normalTokens2 = new String[] { "A", "B", "C", "D" };
        String entityType2 = "ORGANIZATION";
        Mention m2 = new CachedMention(phrase2,entityType2,
                                       honorifics,normalTokens2,
                                       gender,isPronominal);


        Matcher epm = new SequenceSubstringMatch(3);
        assertEquals(3,epm.match(m2,mc));


        String phrase3 = "D";
        String[] normalTokens3 = new String[] { "D" };
        String entityType3 = "ORGANIZATION";
        Mention m3 = new CachedMention(phrase3,entityType3,
                                       honorifics,normalTokens3,
                                       gender,isPronominal);


        assertEquals(Matcher.NO_MATCH_SCORE,epm.match(m3,mc));

        mc.add(m3,17);

        assertEquals(3,epm.match(m3,mc));



    }
}
