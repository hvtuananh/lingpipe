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

package com.aliasi.tokenizer;

import com.aliasi.util.Strings;

/**
 * Returns a category for tokens made up out of a single character.
 * Possible categories are <code>LETTER</code>, <code>DIGIT</code>,
 * <code>PUNCTUATION</code>, <code>OTHER</code>, and
 * <code>UNKNOWN</code>.  The latter class is for those tokens that
 * are not single characters.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe1.0
 */ 
public class CharacterTokenCategorizer implements TokenCategorizer {

    /**
     * Construct an instance of a character token categorizer.
     *
     */
    CharacterTokenCategorizer() {
        /* do nothing */
    }

    /**
     * Returns the category of the specified token.  The result will
     * be <code>UNKNOWN</code> for tokens that are not a single
     * character long.  A token that is a single digit will return
     * <code>DIGIT</code>, a single letter <code>LETTER</code>, and
     * punctuation <code>PUNCTUATION</code>.  All other single-letter
     * tokens will return <code>OTHER</code>.
     *
     * @param token Token to categorize.
     * @return Category of specified token.
     */
    public String categorize(String token) {
        if (token.length() != 1) return UNKNOWN_CAT;
        char c = token.charAt(0);
        if (Character.isDigit(c)) return DIGIT_CAT;
        if (Character.isLetter(c)) return LETTER_CAT;
        if (Strings.isPunctuation(c)) return PUNCTUATION_CAT;
        return OTHER_CAT;
    }

    /**
     * Returns a copy of the array of categories used by this categorizer.
     *
     * @return The array of categories used by this categorizer.
     */
    public String[] categories() {
        return CATEGORY_ARRAY.clone();
    }

    /**
     * Returns the name of this class.
     *
     * @return The name of this class.
     */
    @Override public String toString() {
    return getClass().getName();
    }

    /** 
     * The unknown category for tokens not one character long. 
     */
    public final static String UNKNOWN_CAT = "UNKNOWN";

    /** 
     * The digit category. 
     */
    public final static String DIGIT_CAT = "DIGIT";

    /** 
     * The letter category. 
     */
    public final static String LETTER_CAT = "LETTER";

    /** 
     * The punctuation category. 
     */
    public final static String PUNCTUATION_CAT = "PUNCTUATION";

    /** The other category for non-digits, non-letters and non-punctuation
     * tokens of a single character long. 
     */
    public final static String OTHER_CAT = "OTHER";


    /**
     * Singleton instance of a character token categorizer.  This
     * instance is thread safe and should be used for all instances
     * required.
     */
    static final CharacterTokenCategorizer INSTANCE 
        = new CharacterTokenCategorizer();

    private final static String[] CATEGORY_ARRAY = new String[] {
        UNKNOWN_CAT, DIGIT_CAT, LETTER_CAT, PUNCTUATION_CAT, OTHER_CAT
    };

}
