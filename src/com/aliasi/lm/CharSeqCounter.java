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

package com.aliasi.lm;

/**
 * A <code>CharSeqCounter</code> counter provides counts for sequences
 * of characters.

 * <P>The method {@link #count(char[],int,int)} returns the count of
 * the specified character array slice.  The method {@link
 * #extensionCount(char[],int,int)} counts the number of
 * single-character extensions of the specified character array slice.
 * The maximum likelihood estimator can be computed directly from
 * these counts by:
 *
 * <blockquote><code>
 *  P<sub><sub>ML</sub></sub>(c<sub><sub>N</sub></sub>|c<sub><sub>0</sub></sub>,...c<sub><sub>N-1</sub></sub>)
 *  <br>
 *  &nbsp; = count({c<sub><sub>0</sub></sub>,...,c<sub><sub>N</sub></sub>},0,N+1)
 *    / extensionCount({c<sub><sub>0</sub></sub>,...,c<sub><sub>N-1</sub></sub>},0,N)
 * </code></blockquote>
 *
 * The reason the denominator is not a simple count of the context is
 * because of the way final suffix counts are incremented.  For
 * instance, consider counts of all substrings of
 * <code>&quot;abab&quot;</code>; the maximum likelihood estimate of
 * <code>P(a|b)</code> is
 * <code>count(ba)/extensionCount(b)=1/1</code>, not
 * <code>count(ba)/count(b)=1/2</code>.
 *
 * <P>The method {@link #observedCharacters()} returns an array of all
 * characters that appear in at least one substring.  The method
 * method {@link #charactersFollowing(char[],int,int)} returns the
 * number of characters observed following the specified character slice,
 * whereas {@link #numCharactersFollowing(char[],int,int)} returns the
 * number of characters observed following the specified character
 * slice.  These methods are useful for computing the Witten-Bell
 * estimator used in {@link NGramProcessLM}.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public interface CharSeqCounter {

    /**
     * Returns the count for the specified character sequence.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @return Count of character array slice in model.
     * @throws IndexOutOfBoundsException If the start and end minus
     * one indices are not in the range of the character array.
     */
    public long count(char[] cs, int start, int end);

    /**
     * Returns the sum of the counts of all character sequences one
     * character longer than the specified character slice.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end Index of one past last character in slice.
     * @return The sum of the counts of all character sequences one
     * character longer than the specified character slice.
     * @throws IndexOutOfBoundsException If the start and end minus
     * one indices are not in the range of the character array.
     */
    public long extensionCount(char[] cs, int start, int end);

    /**
     * Returns the number of characters that when appended to the end
     * of the specified character slice produce an extended slice with
     * a non-zero count.  In symbols:
     *
     * <blockquote><code>
     *   numCharactersFollowing(cSlice)
     *   <br> &nbsp; = | { c | count(cSlice.c) > 0 } |
     * </code></blockquote>
     *
     * where <code>count(cSlice.c)</code> represents the count
     * of the character slice <code>cSlice</code> suffixed with the
     * character <code>c</code>.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end One plus index of last character in slice.
     * @return The number of characters following the specified
     * character slice.
     * @throws IndexOutOfBoundsException If the start and end minus
     * one indices are not in the range of the character array.
     */
    public int numCharactersFollowing(char[] cs, int start, int end);

    /**
     * Returns the array of characters that have been observed
     * following the specified character slice in unicode order.  The
     * returned array will be in ascending unicode numerical order.
     * Note that unicode order is not necessarily the same as any
     * localized alpha-numeric sort order.
     *rie
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param end One plus index of last character in slice.
     * @return The number of characters following the specified
     * character slice.
     * @throws IndexOutOfBoundsException If the start and end minus
     * one indices are not in the range of the character array.
     */
    public char[] charactersFollowing(char[] cs, int start, int end);

    /**
     * Returns an array consisting of the characters with non-zero
     * count in unicode order.  The return value of this method will
     * be equal to the return value of <code>charactersFollowing(new
     * char[0],0,0)</code>.
     *
     * @return Array of characters with non-zero counts.
     */
    public char[] observedCharacters();

}
