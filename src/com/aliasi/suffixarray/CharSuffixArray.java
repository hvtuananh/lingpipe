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

package com.aliasi.suffixarray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A {@code CharSuffixArray} implements a suffix array of characters.
 *
 * <h3>What is a Suffix Array?</h3>
 *
 * <p>Given a string characters {@code cs}, the corresponding
 * suffix array is an array of {@code int} values of length equal to
 * {@code cs.length()}.  The suffix array contains each integer between 0
 * (inclusive) and the length of {@code cs} (exclusive).  The suffix
 * array is sorted so that an index {@code m} appears before {@code n}
 * only if the string running from index {@code m} to the end of
 * {@code cs} (i.e., {@code cs.substring(m,cs.length-1)} is less
 * than the string running from index {@code n} to the end of {@code cs},
 * using ordinary Java {@code String} comparison.
 *
 * <h3>Example</h3>
 *
 * The standard example is the suffix array for the array
 * of characters derived from the string {@code "abracadabra"}.
 * Here's the string itself, with its corresponding indexes:
 *
 * <blockquote><pre>
 * abracadabra
 * 012345678901
 * 0         1
 * </pre></blockquote>
 *
 * The suffixes and their starting indexes are
 *
 * <blockquote><table border="1" cellpadding="3">
 * <tr><th>Char Array Index</th><th>Suffix</th></tr>
 * <tr><td>0</td><td>abracadabra</td>
 * <tr><td>1</td><td>bracadabra</td>
 * <tr><td>2</td><td>racadabra</td>
 * <tr><td>3</td><td>acadabra</td>
 * <tr><td>4</td><td>cadabra</td>
 * <tr><td>5</td><td>adabra</td>
 * <tr><td>6</td><td>dabra</td>
 * <tr><td>7</td><td>abra</td>
 * <tr><td>8</td><td>bra</td>
 * <tr><td>9</td><td>ra</td>
 * <tr><td>10</td><td>a</td>
 * </table></blockquote>
 *
 * The suffix array sorts the char array indexes based on the sort
 * order of the corresponding suffixes as strings.  
 *
 * <blockquote><table border="1" cellpadding="3">
 * <tr><th>Suffix Index</th><th>Value</th><th>Suffix</th></tr>
 * <tr><td>0</td><td>10</td><td>a</td></tr>
 * <tr><td>1</td><td>7</td><td>abra</td></tr>
 * <tr><td>2</td><td>0</td><td>abracadabra</td></tr>
 * <tr><td>3</td><td>3</td><td>acadabra</td></tr>
 * <tr><td>4</td><td>5</td><td>adabra</td></tr>
 * <tr><td>5</td><td>8</td><td>bra</td></tr>
 * <tr><td>6</td><td>1</td><td>bracadabra</td></tr>
 * <tr><td>7</td><td>4</td><td>cadabra</td></tr>
 * <tr><td>8</td><td>6</td><td>dabra</td></tr>
 * <tr><td>9</td><td>9</td><td>ra</td></tr>
 * <tr><td>10</td><td>2</td><td>racadabra</td></tr>
 * </table></blockquote>
 *
 * Thus the suffix array itself for {@code "abracadabra"}
 * is the {@code int[]}-type array
 *
 * <blockquote><pre>
 * suffixArray("abracadbra")
 * = { 10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2 }
 * </blockquote>
 *
 *
 * <h3>Constructing a Suffix Array</h3>
 *
 * A suffix array is constructed from a character array and optionally
 * a maximum length at which to compare strings.  If the maximum length
 * is less than the length of the array, strings are truncated to be at
 * most this length before comparison.  This isn't a true suffix array,
 * but can be faster to create and will suffice for many applications.
 * The indexes will be sorted relative to the truncated strings, so they
 * will be in order up to the specified length.
 *
 * <h3>Using Suffix Arrays</h3>
 *
 * The primary application of suffix arrays is finding duplicate
 * substrings.  The key idea is that two substrings of a string can be
 * represented as the prefixes of two suffixes of the string.  For
 * instance, the example above, {@code "abracadabra"}, has two
 * instances of the substring {@code "br"}, corresponding to the
 * suffixes {@code "bracadabra"} starting at index 1 in the original
 * string and {@code "bra"} starting at index 8 in the original
 * string.  Note that these two suffixes are adjacent in the suffix
 * array, occupying indexes 5 and 6 (in reverse order, because suffix
 * {@code "bra"} sorts before {@code "bracadabra"} as a string.
 *
 * <p>The method {@code prefixMatches(int)} will return all spans in
 * the suffix array that match up to a specific number of characters.
 * For instance, to find all substrings that match of length 3 from
 * suffix array {@code sa}, the method call {@code
 * sa.prefixMatches(3)} returns a list containing all spans as integer
 * arrays of type {@code int[]} with spans being represented from
 * start position (inclusive) to end position (exclusive), which would
 * here contain elements <code>{1,3}, {5,7}</code> indicating first
 * that the suffixes at positions 1 and 2, namely {@code "abra"} and
 * {@code "abracadabra"} start with the same three characters and
 * second that the suffixes at positions 5 and 6, {@code "bra"} and
 * {@code "bracadabra"}, start with the same three characters.  Thus
 * we found all substrings of length 3 that occur more than once,
 * namely {@code "abr"} and {@code "bra"}, along with their positions.
 *
 * By using the suffix array itself, the positions in the underlying
 * string may be retrieved.  For instance, the suffixes at positions
 * 1 and 2 in the suffix array start at positions
 *
 * <h3>Thread Safety</h3>
 *
 * A suffix array is thread safe after construction.
 * 
 * @author Bob Carpenter
 * @version 4.1.0
 * @since 4.0.2
 */
public class CharSuffixArray {

    private final String mText;
    private final int[] mSuffixArray;
    private final int mMaxSuffixLength;

    /**
     * Construct a suffix array from the specified string, with no
     * bound on suffix length.
     *
     * @param text Underlying characters making up suffix array.
     */
    public CharSuffixArray(String text) {
        this(text,Integer.MAX_VALUE);
    }

    /**
     * Construct a suffix array from the specified string, bounding
     * comparisons for sorting by the specified maximum suffix length.
     *
     * <p>This constructor is appropriate if no operations will be
     * subsequently performed on suffixes greater than the maximum
     * specified length.
     *
     * @param text Underlying text for suffix array.
     * @param maxSuffixLength Maximum suffix length for comparison.
     */
    public CharSuffixArray(String text, int maxSuffixLength) {
        mText = text;
        mMaxSuffixLength = maxSuffixLength;
        Integer[] is = new Integer[text.length()];
        for (int i = 0; i < text.length(); ++i)
            is[i] = i;
        Arrays.sort(is,new IndexComparator());
        int[] suffixArray = new int[is.length];
        for (int i = 0; i < is.length; ++i)
            suffixArray[i] = is[i];
        mSuffixArray = suffixArray;
    }

    /**
     * Returns the underlying array of characters for this class.
     *
     * @return The text underlying this suffix array.
     */
    public String text() {
        return mText;
    }

    /**
     * Returns the maximum suffix length for this character suffix
     * array.
     *
     * @return Maximum length of suffixes.
     */
    public int maxSuffixLength() {
        return mMaxSuffixLength;
    }


    /**
     * Return the value of the suffix array at the specified index.
     *
     * @param idx Index into suffix array.
     * @return Index of suffix start position in the underlying
     * character array.
     */
    public int suffixArray(int idx) {
        return mSuffixArray[idx];
    }

    /**
     * Return the number of entries in this suffix array.
     *
     * @return Length of the suffix array.
     */
    public int suffixArrayLength() {
        return mText.length();
    }

    /**
     * Returns the string that starts at position {@code i} in
     * the character index and runs to the end of the character array
     * or up to the specified maximum length.
     *
     * @param csIndex Starting index in underlying array of characters.
     * @param maxLength Maximum length of returned string.
     * @return String starting at the specified index to the end of
     * the character array, truncated at max length.
     */
    public String suffix(int csIndex, int maxLength) {
        return mText.substring(csIndex,end(csIndex,mText.length(),maxLength));
    }

    // req not to overflow
    static int end(int csIndex, int textLength, int maxLength) {
        return (csIndex + (long) maxLength >=  textLength)
            ? textLength
            : csIndex + maxLength;
    }

    /**
     * Returns a list of maximal spans of suffix array indexes which
     * refer to suffixes that share a prefix of at least the specified
     * minimum match length.
     *
     * @param minMatchLength Minimum number of characters required to
     * match.
     * @return The list of pairs of start (inclusive) and end
     * (exclsuive) positions in the suffix array that match up
     * to the specified minimum number of characters.
     */
    public List<int[]> prefixMatches(int minMatchLength) {
        List<int[]> matches = new ArrayList<int[]>();
        for (int i = 0; i < mSuffixArray.length; ) {
            int j = suffixesMatchTo(i,minMatchLength);
            if (i + 1 != j) {
                matches.add(new int[] { i, j });
                i = j;
            } else {
                ++i;
            }
        }
        return matches;
    }

    private int suffixesMatchTo(int i, int minMatchLength) {
        int index1 = mSuffixArray[i];
        int j = i+1;
        for (; j < mSuffixArray.length; ++j) {
            int index2 = mSuffixArray[j];
            if (!matchChars(index1,index2,minMatchLength))
                break;
        }
        return j;
    }

    private boolean matchChars(int index1, int index2, int minMatches) {
        if (index1 + minMatches > mSuffixArray.length)
            return false; // not enough chars
        if (index2 + minMatches > mSuffixArray.length)
            return false; // not enough chars
        for (int k = 0; k < minMatches; ++k)
            if (mText.charAt(index1 + k) != mText.charAt(index2 + k))
                return false;
        return true;
    }

    /**
     * A special separator character, used to mark the
     * boundaries of documents within the character array.
     * Suffixes are considered virtually to only run up
     * to a separator.  
     *
     * The value of the separator char is {@code '\uFFFF'}.
     */
    public static char SEPARATOR = '\uFFFF';


    class IndexComparator implements Comparator<Integer> {
        public int compare(Integer i, Integer j) {
            String cs = mText;
            for (int m = i, n = j, k = 0; k < mMaxSuffixLength; ++m, ++n, ++k) {
                if (m == cs.length() || cs.charAt(m) == SEPARATOR) {
                    if (n == cs.length() || cs.charAt(n) == SEPARATOR)
                        return 0;
                    return -1;
                }
                if (n == cs.length() || cs.charAt(n) == SEPARATOR)
                    return 1;
                if (cs.charAt(m) < cs.charAt(n))
                    return -1;
                if (cs.charAt(m) > cs.charAt(n))
                    return 1;
            }
            return 0;
        }
    }

}