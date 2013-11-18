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
 * Provides a means of implementing a {@link Matcher} that returns a
 * constant value if a boolean condition is satisifed and the no-match
 * score otherwise.  The constructor provides the constant score for a
 * match, and the logic is provided through an implementation of
 * the method {@link #matchBoolean(Mention,MentionChain)} by a subclass.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public abstract class BooleanMatcherAdapter implements Matcher {

    /**
     * The score to return in the event of a match.
     */
    private final int mScore;

    /**
     * Construct a boolean matcher adapter that returns the
     * specified score.
     *
     * @param score Score to return if there is a match.
     */
    public BooleanMatcherAdapter(int score) {
        mScore = score;
    }

    /**
     * Returns <code>true</code> if the specified mention matches
     * the specified mention chain.  
     *
     * @param mention Mention to compare for possible coreference.
     * @param mentionChain Mention chain to compare for possible
     * coreference.
     * @return <code>true</code> if the mention matches the mention
     * chain according to this matching function.
     */
    abstract public boolean matchBoolean(Mention mention,
                                         MentionChain mentionChain);

    /**
     * Final implementation of the matching function required by
     * {@link com.aliasi.coref.Matcher}.  Returns the constant
     * specified in the constructor if {@link
     * #matchBoolean(Mention,MentionChain)} returns <code>true</code>
     * and returns {@link com.aliasi.coref.Matcher#NO_MATCH_SCORE}
     * otherwise.
     *
     * @param mention Mention to compare for possible coreference.
     * @param mentionChain Mention chain to compare for possible
     * coreference.
     * @return Constant supplied in constructor if there is a match
     * and the no-match score otherwise.
     */
    public final int match(Mention mention, MentionChain mentionChain) {
        return matchBoolean(mention,mentionChain)
            ? mScore
            : Matcher.NO_MATCH_SCORE;
    }


}
