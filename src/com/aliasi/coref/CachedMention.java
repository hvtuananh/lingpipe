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

import com.aliasi.util.Arrays;
import com.aliasi.util.Strings;

import java.util.Set;

/**
 * A <code>CachedMention</code> stores all of the retun values
 * specified by the {@link Mention} interface.
 *
 * <p>Cached mentions are individuated by reference.
 *
 * @author  Bob Carpenter
 * @version 3.1.2
 * @since   LingPipe1.0
 */
public class CachedMention implements Mention {

    /**
     * The phrase underlying this mention.
     */
    private final String mPhrase;

    /**
     * The entity type underlying this mention.
     */
    private final String mEntityType;

    /**
     * The honorifics underlying this mention.
     */
    private final Set<String> mHonorifics;

    /**
     * The normal phrase underlying this mention.
     */
    private final String mNormalPhrase;

    /**
     * The normal tokens underlying this mention.
     */
    private final String[] mNormalTokens;

    /**
     * The gender of this mention.
     */
    private final String mGender;

    /**
     * <code>true</code> if this mention is a pronoun.
     */
    private final boolean mIsPronominal;

    /**
     * Construct a cached mention from the specified return values.
     * The normal phrase is defined to be the normal tokens concatenated
     * with a single whitespace separator.
     *
     * @param phrase Underlying phrase for the mention.
     * @param entityType The type of the mention.
     * @param honorifics The honorifics for the mention.
     * @param normalTokens The sequence of normal tokens for the mention.
     * @param gender The gender of the mention constructed.
     * @param isPronominal <code>true</code> if this mention is a
     * pronoun.
     */
    public CachedMention(String phrase, String entityType,
                         Set<String> honorifics, String[] normalTokens,
                         String gender, boolean isPronominal) {
        mPhrase = phrase;
        mEntityType = entityType;
        mHonorifics = honorifics;
        mNormalTokens = normalTokens;
        mNormalPhrase = Strings.concatenate(normalTokens);
        if (mNormalPhrase == null) throw new IllegalArgumentException("yikes");
        mGender = gender;
        mIsPronominal = isPronominal;
    }

    /**
     * Returns the original phrase underlying this mention.
     *
     * @return Original phrase underlying this mention.
     */
    public String phrase() {
        return mPhrase;
    }

    /**
     * Returns the type of entity recognized for this mention.
     *
     * @return Type of entity recognized for this mention.
     */
    public String entityType() {
        return mEntityType;
    }

    /**
     * Returns the set of honorifics which occur in this mention.
     *
     * @return Set of honorifics which occur in this mention.
     */
    public Set<String> honorifics() {
        return mHonorifics;
    }

    /**
     * Returns a normalized version of the phrase, defined
     * to be the concatenation of the normal tokens by a single
     * whitespace.
     *
     * @return Normalized version of the phrase.
     */
    public String normalPhrase() {
        return mNormalPhrase;
    }

    /**
     * Returns an array of normalized tokens for this phrase.
     *
     * @return Array of normalized tokens for this phrase.
     */
    public String[] normalTokens() {
        return mNormalTokens;
    }

    /**
     * Returns <code>true</code> if this mention is a pronoun.
     *
     * @return <code>true</code> if this mention is a pronoun.
     */
    public boolean isPronominal() {
        return mIsPronominal;
    }

    /**
     * Returns a string representing the gender of this mention.
     *
     * @return A string representing the gender of this mention.
     */
    public String gender() {
        return mGender;
    }


    /**
     * Returns a string-based representation of this mention.
     *
     * @return String representation of this mention.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("phrase=");
        sb.append(phrase());
        sb.append("; type=");
        sb.append(entityType());
        sb.append("; honorifics=");
        sb.append(honorifics());
        sb.append("; isPronominal=");
        sb.append(isPronominal());
        sb.append("; gender=");
        sb.append(gender());
        sb.append("; normalPhrase=");
        sb.append(normalPhrase());
        sb.append("; normalTokens=");
        sb.append(Arrays.arrayToString(normalTokens()));
        return sb.toString();
    }


}
