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
 * An <code>IntSeqCounter</code> provides counts for sequences of
 * integers.  This interface parallels {@link CharSeqCounter}.
 *
 * <P>The method {@link #count(int[],int,int)} returns the basic count
 * for the specified integer sequence.  The method {@link
 * #extensionCount(int[],int,int)} is the sum of the counts for all
 * single integer extensions of the specified integer sequence.  As
 * described in the class documentation for {@link CharSeqCounter},
 * these two methods are enough to compute a maximum likelihood
 * estimator of conditional integer likelihoods.
 *
 * <P>The method {@link #numExtensions(int[],int,int)} returns the
 * number of single integer extensions of the specified integer slice.
 * This is useful for computing interpolated estimates with
 * Witten-Bell smoothing.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public interface IntSeqCounter {

    /**
     * Returns the count of the specified sequence of integers.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     */
    public int count(int[] is, int start, int end);

    /**
     * Returns the sum of the count of all sequences that extend
     * the specified sequence by one integer.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     */
    public long extensionCount(int[] is, int start, int end);

    /**
     * Returns the number of one integer extensions of the specified
     * with non-zero counts.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     */
    public int numExtensions(int[] is, int start, int end);

    /**
     * Returns an array of the integers that follow the specified
     * integer array slice.
     *
     * @param is Underlying array of integers.
     * @param start Index of first integer in the slice.
     * @param end Index of one past the last integer in the slice.
     * @throws IndexOutOfBoundsException If the start and end minus one
     * indices do not fall within the range of the integer array.
     */
    public int[] integersFollowing(int[] is, int start, int end);

    /**
     * Returns an array of all integers that have non-zero counts in
     * the model.
     *
     * @return Integers with non-zero counts in the model.
     */
    public int[] observedIntegers();

}
