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

import com.aliasi.util.ObjectToSet;

import java.util.Set;

/**
 * Implements a matching function that returns the score specified in
 * the constructor if the mention has a synonym in the mention set as
 * specified by the synonym dictionary.  Synonyms are defined over
 * normalized phrases of the mention and the phrases of the mentions
 * in the mention chains.  Pairs of synonymous phrases are added to
 * the matcher with the method {@link #addSynonym(String,String)}.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class SynonymMatch extends BooleanMatcherAdapter {

    /**
     * The underlying mapping from phrases to their set of synonyms.
     * It's symmetric in that if the value for
     * <code>mSynonymDictionary.getSet(x).contains(y)</code> should be
     * the same as that for then
     * <code/>mSynonymDictionary.getSet(y).contains(x)</code>.
     */
    private final ObjectToSet<String,String> mSynonymDictionary
        = new ObjectToSet<String,String>();

    /**
     * Construct an instance of synonym matching that returns the
     * specified score in the case of a synonym match.
     *
     * @param score Score to return in case of a match.
     */
    public SynonymMatch(int score) {
        super(score);
    }

    /**
     * Returns <code>true</code> if the mention's normal phrase has a
     * synonym that is the normal phrase of one of the chain's mentions.
     *
     * @param mention Mention to test.
     * @param chain Mention chain to test.
     * @return <code>true</code> if there is a sequence substring
     * match between the mention and chain.
     */
    @Override
    public boolean matchBoolean(Mention mention, MentionChain chain) {
        String phrase = mention.normalPhrase();
        if (!mSynonymDictionary.containsKey(phrase)) return false;
        Set<String> synonyms = mSynonymDictionary.getSet(phrase);
        for (String synonym : synonyms)
            for (Mention chainMention : chain.mentions())
                if (synonym.equals(chainMention.normalPhrase()))
                    return true;
        return false;
    }

    /**
     * Adds the two phrases as synonyms for one another.  The
     * operation is symmetric, so that they do not need to be added in
     * the reverse order.
     *
     * @param phrase1 First phrase in the synonym pair.
     * @param phrase2 Second phrase in the synonym pair.
     */
    public void addSynonym(String phrase1, String phrase2) {
        mSynonymDictionary.addMember(phrase1,phrase2);
        mSynonymDictionary.addMember(phrase2,phrase1);
    }

    /**
     * Ensure sthat the two phrases are no longer synonyms of each other.
     * The operation is symmetric, so they do not need to be removed
     * in the reverse order.
     *
     * @param phrase1 First phrase in the synonym pair.
     * @param phrase2 Second phrase in the synonym pair.
     */
    public void removeSynonym(String phrase1, String phrase2) {
        mSynonymDictionary.removeMember(phrase1,phrase2);
        mSynonymDictionary.removeMember(phrase2,phrase1);
    }

    /**
     * Removes all synonym pairs from this synonym matcher.
     */
    public void clearSynonyms() {
        mSynonymDictionary.clear();
    }
}
