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
 * An <code>NGramTokenizer</code> returns character subsequences
 * of specified lengths as tokens.  The constructor allows
 * the minimum and maximum length of n-gram to be set.
 *
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   LingPipe1.0
 */
class NGramTokenizer extends Tokenizer {

    /**
     * Character array from which n-grams are drawn.
     */
    private final char[] mChars;

    /**
     * First character to consider in character array.
     */
    private final int mOffset;

    /**
     * Number of characters to consider in character array.
     */
    private final int mLength;

    /**
     * Maximum length n-gram generated.
     */
    private final int mMaxNGram;

    /**
     * The size of the next n-gram to generate.
     */
    private int mCurrentSize;

    /**
     * The next starting point in the character array from which to
     * generate an n-gram.
     */
    private int mNextStart;

    private int mLastTokenStartPosition = -1;
    private int mLastTokenEndPosition = -1;


    /**
     * Construct an n-gram tokenizer based on the specified character
     * array slice, returning n-gram tokens between the specified
     * minimum and maximum lengths, inclusive.
     *
     * @param ch Character array from which to derive tokens.
     * @param offset First character to consider.
     * @param length Number of characters to consider.
     * @param minNGram Minimum length n-gram to return.
     * @param maxNGram Maximum length n-gram to return.
     * @throws IllegalArgumentException If <code>minNGram &gt; maxNGram</code>.
     */
    public NGramTokenizer(char[] ch, int offset, int length,
                          int minNGram, int maxNGram) {
        if (minNGram > maxNGram) {
            String msg = "Require min n-gram to be less than max n-gram"
                + "found min=" + minNGram
                + "found max=" + maxNGram;
            throw new IllegalArgumentException(msg);
        }
        mChars = ch;
        mOffset =offset;
        mLength = length;

        mMaxNGram = maxNGram;

        mCurrentSize = minNGram; // don't need to store min n-gram with this
        mNextStart = mOffset;
    }

    /**
     * Returns the start position in the underlying character slice
     * of the last token returned.  For an n-gram tokenizer, this
     * is the index of the first character in the n-gram.
     *
     * @return The index of the character starting the last token
     * returned.
     */
    @Override
    public int lastTokenStartPosition() {
        return mLastTokenStartPosition;
    }

    @Override
    public int lastTokenEndPosition() {
        return mLastTokenEndPosition;
    }

    /**
     * Returns the next n-gram token for this tokenizer, or <code>null</code>
     * if there are no more tokens.
     *
     * @return Next n-gram token for this tokenizer.
     */
    @Override
    public String nextToken() {
        while (mCurrentSize <= mMaxNGram
               && mNextStart + mCurrentSize > mOffset + mLength) {
            ++mCurrentSize;
            mNextStart = mOffset;
        }
        if (mCurrentSize > mMaxNGram) return null;
        mLastTokenStartPosition = mNextStart-mOffset;
        mLastTokenEndPosition = mLastTokenStartPosition + mCurrentSize;
        return new String(mChars,mNextStart++,mCurrentSize);
    }

}
