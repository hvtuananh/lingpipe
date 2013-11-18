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
 * An implementation of the matching interface provides
 * a score for matching a mention against a mention chain.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public interface Matcher {

    /**
     * Returns the match score between a specified mention and mention
     * chain.  Normal return values are positive integers between
     * <code>0</code> and {@link #MAX_SEMANTIC_SCORE} inclusive.  A
     * return value of {@link #NO_MATCH_SCORE}, which is guaranteed to
     * be negative, indicates that the mention cannot be matched to
     * the mention chain.
     *
     * @param mention Mention to match.
     * @param chain Mention chain to match.
     * @return Match score between mention and mention chain.
     */
    int match(Mention mention, MentionChain chain);

    /**
     * Value to return in cases where there can be no
     * match. Guaranteed to be negative and less than any normal match
     * score.
     */
    public static final int NO_MATCH_SCORE = -1;

    /**
     * Maximum value for the matching function, currently
     * <code>4</code>.
     */
    public static final int MAX_SEMANTIC_SCORE = 4;

    /**
     * Maximum distance score between a mention and a mention chain.
     * Currently set to <code>2</code>.
     */
    public static final int MAX_DISTANCE_SCORE = 2;

    /**
     * Maximum score; equal to <code>{@link #MAX_SEMANTIC_SCORE} +
     * {@link #MAX_DISTANCE_SCORE}</code>.
     */
    public static final int MAX_SCORE
        = MAX_SEMANTIC_SCORE + MAX_DISTANCE_SCORE;


}

