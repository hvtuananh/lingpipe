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
 * A <code>MentionChain</code> represents a set of mentions that have
 * been resolved as coreferent in that they refer to the same
 * underlying entity.  Mention chains are created with a {@link
 * MentionFactory} by promoting mentions to mention chains.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public interface MentionChain {

    /**
     * Returns the set of mentions underlying this mention chain.
     *
     * @return Set of mentions underlying this mention chain.
     */
    public Set<Mention> mentions();

    /**
     * Offset of the last sentence in which a mention belonging to
     * this mention chain appears.
     *
     * @return Offset of last sentence containing a mention in this
     * chain.
     */
    public int maxSentenceOffset();

    /**
     * Returns the entity type associated with this mention chain.
     *
     * @return Entity type associated with this mention chain.
     */
    public String entityType();

    /**
     * Adds the specified mention appearing at the specified
     * sentence offset to this mention chain.
     *
     * @param mention Mention to add to this chain.
     * @param sentenceOffset Offset of mention added.
     */
    public void add(Mention mention, int sentenceOffset);

    /**
     * The unique identifier for this mention chain (within
     * a document).
     */
    public int identifier();

    /**
     * Returns <code>true</code> if there is a killing function that
     * defeats the match of the mention against the mention chain.  A
     * <code>true</code> return means that the mention will not
     * be allowed to match against the antecedent no matter what other
     * matching functions return.
     *
     * @param mention Mention to test against antecedent.
     * @return <code>true</code> if there is a killing function that
     * defeats the match of the mention against the mention chain.
     */
    public boolean killed(Mention mention);

    /**
     * Returns the best match score of the mention versus the mention
     * chain according to the matching functions determined by the
     * antecedent.  A score of {@link Matcher#NO_MATCH_SCORE}
     * indicates that all matching functions failed.  Otherwise, the
     * score will fall in the range <code>0</code> to {@link
     * Matcher#MAX_SCORE}, with a lower score being better.
     *
     * @param mention Mention to score against antecedent.
     * @return Best matching score between mention and antecedent.
     */
    public int matchScore(Mention mention);

    /**
     * Returns the gender for this mention chain.
     *
     * @return Gender for this mention chain.
     */
    public String gender();

    /**
     * Honorific phrases attached to instances of this mention chain.
     *
     * @return Set of honorifics for this mention chain.
     */
    public Set<String> honorifics();

}


