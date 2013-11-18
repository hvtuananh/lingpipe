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

/**
 * A <code>MentionFactory</code> is responsible for creating
 * and merging mentions and mention chains.  It is able
 * to create a mention from an underlying phrase and entity
 * type.  It can create a mention chain by promoting a mention.
 * It is also responsible for handling the merging of a mention
 * into a mention chain.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public interface MentionFactory {

    /**
     * Return a mention based on the specified phrase and entity type.
     *
     * @param phrase Phrase underlying the mention created.
     * @param entityType Type of the mention created.
     * @return A mention based on the specified phrase and entity type.
     */
    public Mention create(String phrase, String entityType);

    /**
     * Returns a new mention chain based on the specified mention.
     *
     * @param mention Mention to promote to a mention chain.
     * @return Mention chain constructed from the specified mention.
     */
    public MentionChain promote(Mention mention, int offset);


}
