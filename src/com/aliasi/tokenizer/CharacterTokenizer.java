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

/**
 * A character tokenizer treats each character as a token, excluding
 * whitespace.  A sequence of whitespace is treated as a single token.
 *
 * <p><b>Warning:</b> This tokenizer operates at the level of Java
 * {@code char} values, which are UTF-16 encodings.  Such encodings
 * may be conjugate pairs making up half of a Unicode code point.
 * To get a proper Unicode-sensitive character tokenizer, you need
 * to use a regex tokenizer defined defined by the regex 
 * {@code [^\\p{Z}]}.  The {@code Z} is the Unicode character
 * class for spaces, and the carat performs negation, so this
 * finds all Unicode characters that are not spaces.
 *
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   LingPipe1.0
 */
class CharacterTokenizer extends Tokenizer {

    private final char[] mChars;
    private final int mLastPosition;
    private int mPosition;
    private final int mStartPosition;
    
    private int mLastTokenStartPosition = -1;
    private int mLastTokenEndPosition = -1;

    /**
     * Construct a character-based tokenizer based on the specified
     * character slice.
     *
     * @param ch Underlying character array.
     * @param offset Index of first character in the slice.
     * @param length Number of characters in the slice.
     * @throws IllegalArgumentException If the offset or length are
     * negative, or if the offset plus length are greater than the length
     * of the character array.
     */
    public CharacterTokenizer(char[] ch, int offset, int length) {
        if (offset < 0) {
            String msg = "Offset must be greater than 0."
                + " Found offset=" + offset;
            throw new IllegalArgumentException(msg);
        }
        if (length < 0) {
            String msg = "Length must be greater than 0."
                + " Found length=" + length;
            throw new IllegalArgumentException(msg);
        }
        if (offset + length > ch.length) {
            String msg = "Offset Plus length must be less than or equal array length."
                + " Found ch.length=" + ch.length
                + " offset=" + offset
                + " length=" + length
                + " (offset+length)=" + (offset+length);
            throw new IllegalArgumentException(msg);
        }
        mChars = ch;
        mPosition = offset;
        mStartPosition = offset;
        mLastPosition = offset+length;
    }

    @Override public int lastTokenStartPosition() {
        return mLastTokenStartPosition;
    }

    @Override public int lastTokenEndPosition() {
        return mLastTokenEndPosition;
    }


    @Override public String nextWhitespace() {
    StringBuilder sb = new StringBuilder();
        while (hasMoreCharacters()
               && Character.isWhitespace(currentChar())) {
            sb.append(currentChar());
            ++mPosition;
        }
        return sb.toString();
    }

    @Override public String nextToken() {
        skipWhitespace();
        if (!hasMoreCharacters()) return null;
        mLastTokenStartPosition = mPosition - mStartPosition;
        mLastTokenEndPosition = mLastTokenStartPosition + 1;
        return new String(new char[] { mChars[mPosition++] });
    }


    /**
     * Returns this class's name.  This does not include the
     * characters or current position.
     *
     * @return A string representation of this character tokenizer.
     */
    @Override public String toString() {
    return getClass().getName();
    }


    private void skipWhitespace() {
        while (hasMoreCharacters()
               && Character.isWhitespace(currentChar()))
            ++mPosition;
    }

    private boolean hasMoreCharacters() {
        return mPosition < mLastPosition;
    }

    private char currentChar() {
        return mChars[mPosition];
    }

}
