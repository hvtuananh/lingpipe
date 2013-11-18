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

package com.aliasi.coref.matchers;

import com.aliasi.coref.Killer;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

/**
 * Implements a killing function that defeats a match of a mention
 * against a mention chain with an incompatible gender.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public class GenderKiller implements Killer {
    
    /**
     * Construct a new gender killer.
     */
    public GenderKiller() { 
        /* do nothing */
    }

    /**
     * Returns <code>true</code> if the specified mention and
     * mention chain have incompatible genders.  Genders are
     * incompatible if they are both non-null, but not equals.
     *
     * @param mention Mention to test.
     * @param chain Mention chain to test.
     * @return <code>true</code> if the specified mention and
     * chain have incompatible genders.
     */
    public boolean kill(Mention mention, MentionChain chain) {
        return mention.gender() != null
            && chain.gender() != null
            && !mention.gender().equals(chain.gender());
    }

}
