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

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A <code>IndoEuropeanTokenCategorizer</code> is a generic token
 * categorizer for Indo-European languages that is based on character
 * &quot;shape&quot;.
 *
 * <p>
 * The token categories returned by {@link #categorize(String)} are
 * as follows.  To find the category for a given token, the first
 * category that matches in the following list is chosen.
 * <br/><br/>
 * <table cellpadding="5" border="1">
 * <tr><td width="30%"><b>Category</b></td>
 *     <td width="70%"><b>Description</b></td></tr>
 * <tr><td><code>NULL-TOK</code></td>
 *     <td>Zero-length string</td></tr>
 * <tr><td><code>1-DIG</code></td>
 *     <td>A single digit.</td></tr>
 * <tr><td><code>2-DIG</code></td>
 *     <td>A two-digit string.</td></tr>
 * <tr><td><code>3-DIG</code></td>
 *     <td>A three digit string.</td></tr>
 * <tr><td><code>4-DIG</code></td>
 *     <td>A four digit string.</td></tr>
 * <tr><td><code>5+-DIG</code></td>
 *     <td>String of all digits five or more digits long.</td></tr>
 * <tr><td><code>DIG-LET</code></td>
 *     <td>Contains digits and letters.</td></tr>
 * <tr><td><code>DIG--</code></td>
 *     <td>Contains digits and hyphens</td></tr>
 * <tr><td><code>DIG-/</code></td>
 *     <td>Contains digits and slashes.</td></tr>
 * <tr><td><code>DIG-,</code></td>
 *     <td>Contains digits and commas.</td></tr>
 * <tr><td><code>DIG-.</code></td>
 *     <td>Contains digits and periods.</td></tr>
 * <tr><td><code>1-LET-UP</code></td>
 *     <td>A single uppercase letter.</td></tr>
 * <tr><td><code>1-LET-LOW</code></td>
 *     <td>One lowercase letter</td></tr>
 * <tr><td><code>LET-UP</code></td>
 *     <td>Uppercase letters only.</td></tr>
 * <tr><td><code>LET-LOW</code></td>
 *     <td>Lowercase letters only.</td></tr>
 * <tr><td><code>LET-CAP</code></td>
 *     <td>Uppercase letter followed by one or more
 lowercase letters.</td></tr>
 * <tr><td><code>LET-MIX</code></td>
 *     <td>Letters only, containing both uppercase and lettercase.</td></tr>
 * <tr><td><code>PUNC-</code></td>
 *     <td>A sequence of punctuation characters.</td></tr>
 * <tr><td><code>OTHER</code></td>
 *     <td>Anything else.</td></tr>
 * </table>
 * </p>
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe1.0
 */
public final class IndoEuropeanTokenCategorizer 
    implements Compilable, TokenCategorizer {


    IndoEuropeanTokenCategorizer() { 
        /* do nothing */
    }

    /**
     * Returns the type of a token, based on its structure or other
     * information.  The returned type is a string that is used
     * as a proxy for the token.  Estimates are stored for tokens
     * and for their classes.  The class based estimates are interpolated
     * with the word-based estimates once the most specific matching
     * context is found.
     *
     * @param token Token whose class is returned.
     * @return String representing the class of a token.
     */
    public String categorize(String token) {
        char[] chars = token.toCharArray();
        if (chars.length == 0) return NULL_CLASS;
        if (Strings.allDigits(chars,0,chars.length)) {
            if (chars.length == 1) return ONE_DIGIT_CLASS;
            if (chars.length == 2) return TWO_DIGIT_CLASS;
            if (chars.length == 3) return THREE_DIGIT_CLASS;
            if (chars.length == 4) return FOUR_DIGIT_CLASS;
            return FIVE_PLUS_DIGITS_CLASS;
        }
        if (Strings.containsDigits(chars)) {
            if (Strings.containsLetter(chars)) return DIGITS_LETTERS_CLASS;
            if (token.indexOf('-') >= 0) return DIGITS_DASH_CLASS;
            if (token.indexOf('/') >= 0) return DIGITS_SLASH_CLASS;
            if (token.indexOf(',') >= 0) return DIGITS_COMMA_CLASS;
            if (token.indexOf('.') >= 0) return DIGITS_PERIOD_CLASS;
            return MISC_DIGITS_CLASS;
        }
        if (Strings.allPunctuation(chars)) return PUNCTUATION_CLASS;
        if (Character.isUpperCase(chars[0])
            && chars.length == 1) return ONE_UPPERCASE_CLASS;
        if (Character.isLowerCase(chars[0])
            && chars.length == 1) return ONE_LOWERCASE_CLASS;
        if (Strings.allUpperCase(chars)) return UPPERCASE_CLASS;
        if (Strings.allLowerCase(chars)) return LOWERCASE_CLASS;
        if (Strings.capitalized(chars)) return CAPITALIZED_CLASS;
        if (Strings.allLetters(chars)) return MIXEDCASE_CLASS;
        return OTHER_CLASS;
    }

    /**
     * Returns a copy of the array of strings representing all the
     * categories produced by this categorizer.
     *
     * @return Copy of the categories for this categorizer.
     */
    public String[] categories() {
        return CATEGORY_ARRAY.clone();
    }

    // Strings used to represent the classes of tokens.
    // All contain a hyphen ('-') to prevent
    // conflicts with other tokens and tags

    private static final String NULL_CLASS = "NULL-TOK";
    private static final String ONE_DIGIT_CLASS = "1-DIG";
    private static final String TWO_DIGIT_CLASS = "2-DIG";
    private static final String THREE_DIGIT_CLASS = "3-DIG";
    private static final String FOUR_DIGIT_CLASS = "4-DIG";
    private static final String FIVE_PLUS_DIGITS_CLASS = "5+-DIG";
    private static final String DIGITS_LETTERS_CLASS = "DIG-LET";
    private static final String MISC_DIGITS_CLASS = "DIG-MSC";

    private static final String DIGITS_DASH_CLASS = "DIG--";
    private static final String DIGITS_SLASH_CLASS = "DIG-/";
    private static final String DIGITS_COMMA_CLASS = "DIG-,";
    private static final String DIGITS_PERIOD_CLASS = "DIG-.";
    private static final String UPPERCASE_CLASS = "LET-UP";
    private static final String LOWERCASE_CLASS = "LET-LOW";
    private static final String CAPITALIZED_CLASS = "LET-CAP";
    private static final String MIXEDCASE_CLASS = "LET-MIX";
    private static final String ONE_UPPERCASE_CLASS = "1-LET-UP";
    private static final String ONE_LOWERCASE_CLASS = "1-LET-LOW";
    private static final String PUNCTUATION_CLASS = "PUNC-";

    private static final String OTHER_CLASS = "OTHER";

    /**
     * Array of category symbols returned by {@link #categorize(String)}.
     */
    private static final String[] CATEGORY_ARRAY = new String[] {
        NULL_CLASS,
        ONE_DIGIT_CLASS,
        TWO_DIGIT_CLASS,
        THREE_DIGIT_CLASS,
        FOUR_DIGIT_CLASS,
        FIVE_PLUS_DIGITS_CLASS,
        DIGITS_LETTERS_CLASS,
        MISC_DIGITS_CLASS,
        DIGITS_DASH_CLASS,
        DIGITS_SLASH_CLASS,
        DIGITS_COMMA_CLASS,
        DIGITS_PERIOD_CLASS,
        UPPERCASE_CLASS,
        LOWERCASE_CLASS,
        CAPITALIZED_CLASS,
        MIXEDCASE_CLASS,
        OTHER_CLASS,
        ONE_UPPERCASE_CLASS,
        ONE_LOWERCASE_CLASS,
        PUNCTUATION_CLASS
    };

    /**
     * This is a constant Indo-European token categorizer.  Because
     * the categorizer is thread safe, this can be used in lieu of
     * creating a new instance with the zero-argument constructor.
     */
    public static final IndoEuropeanTokenCategorizer CATEGORIZER 
        = new IndoEuropeanTokenCategorizer();

    /**
     * Compiles this token categorizer to the specified object output.
     * The categorizer read back in is reference identical to the
     * static constant {@link #CATEGORIZER}.
     *
     * @param objOut Object output to which this categorizer is
     * written.
     * @throws IOException If there is an underlying I/O exception
     * during the write.a
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer());
    }


    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = -7153532326881222261L;
        public Externalizer() { 
            /* do nothing */
        }
        @Override
        public void writeExternal(ObjectOutput objOut) { 
            /* do nothing */
        }
        @Override
        public Object read(ObjectInput objIn) { return CATEGORIZER; }
    }

}
