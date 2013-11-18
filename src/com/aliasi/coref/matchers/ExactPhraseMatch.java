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
 * specified in the constructor if there is a match
 * between the normal phrases of the mention and one of
 * the mentions in the mention chain.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public final class ExactPhraseMatch extends BooleanMatcherAdapter {

    /**
     * Construct an instance of an exact phrase matcher that
     * returns the specified score if there is a match.
     *
     * @param score Score to return if there is a match.
     */
    public ExactPhraseMatch(int score) {
        super(score);
    }

    /**
     * Returns <code>true</code> if the normal phrase of the mention
     * is equal to the normal phrase of a mention in the mention chain.
     *
     * @param mention Mention to match.
     * @param chain Mention chain to match.
     * @return <code>true</code> if the normal phrase of the mention
     * is equal to the normal phrase of a mention in the mention
     * chain.
     */
    @Override
    public boolean matchBoolean(Mention mention, MentionChain chain) {
        if (mention.isPronominal()) return false;
        String mentionPhrase = mention.normalPhrase();
        for (Mention chainMention : chain.mentions())
            if (mentionPhrase.equals(chainMention.normalPhrase()))
                return true;
        return false;
    }

}
