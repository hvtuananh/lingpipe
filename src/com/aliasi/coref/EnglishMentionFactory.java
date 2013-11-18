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

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.Strings;

import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

/**
 * A mention factory for English phrases.  Defines genders
 * and honorifics in lists, which are accessible to other
 * modules.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe1.0
 */
public class EnglishMentionFactory extends AbstractMentionFactory {

    /**
     * Constructs a new English mention factory.
     */
    public EnglishMentionFactory() {
        super(IndoEuropeanTokenizerFactory.INSTANCE);
    }

    @Override
    public boolean isHonorific(String normalizedToken) {
        return HONORIFICS.contains(normalizedToken);
    }

    /**
     * Returns <code>true</code> if the specified entity type
     * is a pronominal type.
     *
     * @param entityType Type of entity to test.
     * @return <code>true</code> if the specified entity type is
     * pronominal.
     */
    @Override
    public boolean isPronominal(String entityType) {
        return entityType.equals(Tags.MALE_PRONOUN_TAG)
            || entityType.equals(Tags.FEMALE_PRONOUN_TAG)
            || entityType.equals(Tags.NEUTER_PRONOUN_TAG);
    }

    /**
     * Returns a normalized version of the specified token.  Normalization
     * returns a lowercased version of tokens, or <code>null</code>
     * if the token is entirely punctuation.
     *
     * @param token Token to normalize.
     * @return Normalized version of token.
     */
    @Override
    public String normalizeToken(String token) {
        if (Strings.allPunctuation(token)) return null;
        return token.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns a string rerpesenting the gender entities
     * with the specified entity type.
     *
     * @param entityType Entity type from which to extract gender.
     * @return Gender corresponding to the specified entity type.
     */
    @Override
    public String gender(String entityType) {
        if (entityType.equals(Tags.MALE_PRONOUN_TAG))
            return MALE_GENDER;
        if (entityType.equals(Tags.FEMALE_PRONOUN_TAG))
            return FEMALE_GENDER;
        if (entityType.equals(Tags.PERSON_TAG))
            return null;
        return NEUTER_GENDER;
    }

    /**
     * The gender for males.
     */
    public static final String MALE_GENDER = "male";

    /**
     * The gender for females.
     */
    public static final String FEMALE_GENDER = "female";

    /**
     * The gender for neuters.
     */
    public static final String NEUTER_GENDER = "neuter";

    /**
     * The set of male honorifics.
     */
    public static final Set<String> MALE_HONORIFICS = new HashSet<String>();
    static {
        MALE_HONORIFICS.add("mr");
        MALE_HONORIFICS.add("mssr");
        MALE_HONORIFICS.add("mister");
    }

    /**
     * The set of female honorifics.
     */
    public static final Set<String> FEMALE_HONORIFICS = new HashSet<String>();
    static {
        FEMALE_HONORIFICS.add("ms");
        FEMALE_HONORIFICS.add("mrs");
        FEMALE_HONORIFICS.add("missus");
        FEMALE_HONORIFICS.add("miss");
    }

    /**
     * The complete set of male, female, and generic honorifics.
     */
    public static final Set<String> HONORIFICS = new HashSet<String>();
    static {
        HONORIFICS.addAll(MALE_HONORIFICS);
        HONORIFICS.addAll(FEMALE_HONORIFICS);
        HONORIFICS.add("dr");
        HONORIFICS.add("gen");
        HONORIFICS.add("adm");
        HONORIFICS.add("pres");
    }


}
