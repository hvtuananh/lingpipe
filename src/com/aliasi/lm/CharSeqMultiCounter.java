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
 * A <code>CharSeqMultiCounter</code> combines the counts from a pair
 * of character sequence counters.  The returned values are the values
 * resulting from combining the counts in both counters.
 *
 * <P>Multi-counters are particularly useful in situations where a
 * large or constant background counter must be updated several
 * different ways simultaneously.  For instance, a general 5-gram
 * counter of a language trained over a lot of data might be combined
 * with an 8-gram topic-specific model for use in a classifier.
 *
 * <P>More than two counters may be combined by combining them two at
 * a time.  The best strategy is to combine them two at a time into a
 * balanced tree of counters, as done by the constructor {@link
 * #CharSeqMultiCounter(CharSeqCounter[])}.  For instance, with
 * <code>CharSeqCounter</code> instances <code>c1</code>,
 * <code>c2</code>, <code>c3</code>, and <code>c4</code>, the balanced
 * construction of <code>c1234</code> in:
 *
 * <pre>
 * CharSeqCounter c12 = new CharSeqMultiCounter(c1,c2);
 * CharSeqCounter c34 = new CharSeqMultiCounter(c3,c4);
 * CharSeqCounter c1234 = new CharSeqMultiCounter(c12,c34);
 * </pre>
 *
 * is more efficient for many operations than the linear
 * construction in:
 *
 * <pre>
 * CharSeqCounter c12 = new CharSeqMultiCounter(c1,c2);
 * CharSeqCounter c123 = new CharSeqMultiCounter(c12,c3);
 * CharSeqCounter c1234 = new CharSeqMultiCounter(c123,c4);
 * </pre>
 *
 * <P><i>Implementation Note:</i> The methods {@link
 * #numCharactersFollowing(char[],int,int)}, {@link
 * #charactersFollowing(char[],int,int)}, and {@link
 * #observedCharacters()} all call the contained counters' {@link
 * CharSeqCounter#charactersFollowing(char[],int,int)} methods and
 * then merge or count results.  All other methods only perform
 * arithmetic on the result of the corresponding method call son the
 * contained counters.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class CharSeqMultiCounter implements CharSeqCounter {

    private final CharSeqCounter mCounter1;
    private final CharSeqCounter mCounter2;

    /**
     * Construct a character sequence counter from the specified array
     * of counters.  This will piecewise construct multi-counters from
     * the component counters in a balanced way.
     *
     * @param counters Array of counters to back multicounter.
     * @throws IllegalArgumentException If the list of counters is
     * less than two elements long.
     */
    public CharSeqMultiCounter(CharSeqCounter[] counters) {
        this(counter(counters,0,counters.length/2),
             counter(counters,counters.length/2,counters.length));
    }

    /**
     * Construct a multi-counter from the specified pair of counters.
     *
     * @param counter1 First counter in multi-counter.
     * @param counter2 Second counter in multi-counter.
     */
    public CharSeqMultiCounter(CharSeqCounter counter1,
                               CharSeqCounter counter2) {
        mCounter1 = counter1;
        mCounter2 = counter2;
    }

    public long count(char[] cs, int start, int end) {
        return mCounter1.count(cs,start,end)
            + mCounter2.count(cs,start,end);
    }

    public long extensionCount(char[] cs, int start, int end) {
        return mCounter1.extensionCount(cs,start,end)
            + mCounter2.extensionCount(cs,start,end);
    }

    public int numCharactersFollowing(char[] cs, int start, int end) {
        char[] cs1 = mCounter1.charactersFollowing(cs,start,end);
        char[] cs2 = mCounter2.charactersFollowing(cs,start,end);
        return unionSize(cs1,cs2);
    }

    public char[] charactersFollowing(char[] cs, int start, int end) {
        char[] cs1 = mCounter1.charactersFollowing(cs,start,end);
        char[] cs2 = mCounter2.charactersFollowing(cs,start,end);
        return orderedUnion(cs1,cs2);
    }

    public char[] observedCharacters() {
        return charactersFollowing(new char[0],0,0);
    }

    private int unionSize(char[] cs1, char[] cs2) {
        int count = 0;
        int i1 = 0;
        int i2 = 0;
        while (i1 < cs1.length) {
            while (i2 < cs2.length && cs2[i2] < cs1[i1]) {
                ++i2;
                ++count;
            }
            if (i2 < cs2.length && cs2[i2] == cs1[i1])
                ++i2;
            ++count;
            ++i1;
        }
        return count + cs2.length - i2;
    }

    private char[] orderedUnion(char[] cs1, char[] cs2) {
        char[] cs = new char[unionSize(cs1,cs2)];
        int i = 0;
        int i1 = 0;
        int i2 = 0;
        while (i1 < cs1.length) {
            while (i2 < cs2.length && cs2[i2] < cs1[i1])
                cs[i++] = cs2[i2++];
            if (i2 < cs2.length && cs1[i1] == cs2[i2])
                ++i2;
            cs[i++] = cs1[i1++];
        }
        System.arraycopy(cs2,i2,cs,i,cs2.length-i2);
        return cs;
    }

    private static CharSeqCounter counter(CharSeqCounter[] counters,
                                          int start, int end) {
        if (end <= start) {
            String msg = "Too few counters provided.";
            throw new IllegalArgumentException(msg);
        }
        if (end - start == 1)
            return counters[start];
        int mid = start + (end - start) / 2;
        return new CharSeqMultiCounter(counter(counters,start,mid),
                                       counter(counters,mid,end));
    }


}
