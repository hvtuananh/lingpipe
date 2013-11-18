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

package com.aliasi.coref;

import java.util.Set;

/**
 * An instance of <code>Mention</code> represents a single mention of
 * a given phrase in context.  It provides information about the
 * entity type of the mention, and various properties of its phrase.
 * Mentions are created by a {@link MentionFactory}.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public interface Mention {

    /**
     * Returns the original phrase underlying this mention.
     *
     * @return Original phrase underlying this mention.
     */
    public String phrase();

    /**
     * Returns the type of entity recognized for this mention.
     *
     * @return Type of entity recognized for this mention.
     */
    public String entityType();

    /**
     * Returns the set of honorifics which occur in this mention.
     *
     * @return Set of honorifics which occur in this mention.
     */
    public Set<String> honorifics();

    /**
     * Returns a normalized version of the phrase.
     *
     * @return Normalized version of the phrase.
     */
    public String normalPhrase();

    /**
     * Returns an array of normalized tokens for this phrase.
     *
     * @return Array of normalized tokens for this phrase.
     */
    public String[] normalTokens();

    /**
     * Returns <code>true</code> if this mention is a pronoun.
     *
     * @return <code>true</code> if this mention is a pronoun.
     */
    public boolean isPronominal();

    /**
     * Returns a string representing the gender of this mention.
     *
     * @return A string representing the gender of this mention.
     */
    public String gender();

}
