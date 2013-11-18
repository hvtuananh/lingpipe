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

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.SmallSet;
import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The <code>AbstractMentionFactory</code> class implements the
 * mention factory interface using linguistically-motivated abstract
 * methods.  These methods control gender, honorific status,
 * pronominal status, as well as providing a way to normalize tokens
 * for comparison.  Furthermore, this class provides a way of generating
 * a new mention chain composed of a single mention.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public abstract class AbstractMentionFactory implements MentionFactory {

    /**
     * Identifier to assign to next mention chain created.
     */
    private int mNextChainIdentifier = 0;

    /**
     * Tokenizer factory for this mention factory.
     */
    private final TokenizerFactory mTokenizerFactory;

    /**
     * Construct an abstract mention factory with the specified
     * tokenizer factory.
     *
     * @param tokenizerFactory Tokenizer factory for this mention
     * factory.
     */
    public AbstractMentionFactory(TokenizerFactory tokenizerFactory) {
        mTokenizerFactory = tokenizerFactory;
    }

    /**
     * Return a mention based on the specified phrase and entity type.
     *
     * @param phrase Phrase underlying the mention created.
     * @param entityType Type of the mention created.
     * @return A mention based on the specified phrase and entity type.
     */
    public Mention create(String phrase, String entityType) {
        List<String> tokens = new ArrayList<String>();
        Set<String> honorifics = extractTokens(phrase,tokens);
        return new CachedMention(phrase,entityType,
                                 honorifics,
                                 tokens.toArray(Strings.EMPTY_STRING_ARRAY),
                                 gender(entityType),isPronominal(entityType));

    }


    /**
     * Returns a new mention chain based on the specified mention.
     *
     * @param mention Mention to promote to a mention chain.
     * @return Mention chain constructed from the specified mention.
     */
    public MentionChain promote(Mention mention, int offset) {
        return new MentionChainImpl(mention,offset,nextChainIdentifier());
    }

    private int nextChainIdentifier() {
        synchronized (this) {
            return mNextChainIdentifier++;
        }
    }

    /**
     * Returns <code>true</code> if the specified token is an
     * honorific.
     *
     * @param token Token to test.
     * @return <code>true</code> if the specified token is an
     * honorific.
     */
    protected abstract boolean isHonorific(String token);

    /**
     * Returns a string rerpesenting the gender entities
     * with the specified entity type.
     *
     * @param entityType Entity type from which to extract gender.
     * @return Gender corresponding to the specified entity type.
     */
    protected abstract String gender(String entityType);

    /**
     * Returns <code>true</code> if the specified entity type
     * is a pronominal type.
     *
     * @param entityType Type of entity to test.
     * @return <code>true</code> if the specified entity type is
     * pronominal.
     */
    protected abstract boolean isPronominal(String entityType);

    /**
     * Returns a normalized version of the specified token.
     *
     * @param token Token to normalize.
     * @return Normalized version of token.
     */
    protected abstract String normalizeToken(String token);


    /**
     * Parses the phrase into tokens, adding the honorifics to
     * the returned set and the non-honorifics to the array list.
     *
     * @param phrase String to tokenize.
     * @param tokens List of tokens to which tokens are added.
     * @return Set of honorifics extracted from the phrase.
     */
    private Set<String> extractTokens(String phrase, List<String> tokens) {
        Set<String> honorifics = SmallSet.<String>create();
        char[] cs = phrase.toCharArray();
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        for (String token : tokenizer) {
            String normalToken = normalizeToken(token);
            if (normalToken == null) continue;
            else if (isHonorific(normalToken))
                honorifics = SmallSet.<String>create(normalToken,honorifics);
            else
                tokens.add(normalToken);
        }
        return honorifics;
    }

}
