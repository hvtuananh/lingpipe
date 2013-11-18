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

import com.aliasi.coref.BooleanMatcherAdapter;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

/**
 * Implements a matching function that returns the score
 * specified in the constructor if the mention has
 * the specified entity type.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public class EntityTypeMatch extends BooleanMatcherAdapter {

    /**
     * The entity type which is matched.
     */
    private final String mEntityType;

    /**
     * Construct an instance of an entity type matcher
     * that returns the specified score for a match against
     * a mention of the specified type.
     *
     * @param score Score to return if there is a match.
     * @param entityType Entity type of mention resulting in a match.
     */
    public EntityTypeMatch(int score, String entityType) {
        super(score);
    mEntityType = entityType;
    }

    /**
     * Returns <code>true</code> if the mention has the type
     * that was specified in the constructor.
     *
     * @param mention Mention to match.
     * @param chain Mention chain to match.
     * @return <code>true</code> if the mention has the specified type.
     */
    @Override
    public boolean matchBoolean(Mention mention, MentionChain chain) {
        return mention.entityType().equals(mEntityType);
    }

}
