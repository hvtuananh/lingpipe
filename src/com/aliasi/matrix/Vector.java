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

package com.aliasi.matrix;

/**
 * A <code>Vector</code> represents an n-dimensional value.  A vector
 * provides a fixed number of dimensions, with a value for each
 * dimension.
 *
 * <P>The optional operation {@link #setValue(int,double)} allows values
 * to be set.
 *
 * <P>Equality for vectors is defined so that to be equal, two vectors
 * must have the same dimensionality and all values must be equal.
 * The required hash code for this definition of equality is defined
 * in the documentation for {@link #hashCode()}.
 *
 * @author Bob Carpenter
 * @version 3.5
 * @since   LingPipe2.0
 */
public interface Vector {

    /**
     * Returns the number of dimensions of this vector.
     *
     * @return The number of dimensions of this vector.
     */
    public int numDimensions();


    /**
     * Returns the array of dimensions that have non-zero
     * values.
     *
     * <p>This method is only required to return all the non-zero
     * dimensions.  It may also return some dimensions that have zero
     * values.
     *
     * @return The dimensions with non-zero values.
     */
    public int[] nonZeroDimensions();


    /**
     * Adds the specified vector multiplied by the specified scalar to
     * this vector.
     *
     * @param scale The scalar multiplier for the added vector.
     * @param v Vector to scale and add to this vector.
     * @throws IllegalArgumentException If the specified vector is not
     * of the same dimensionality as this vector.
     */
    public void increment(double scale, Vector v);

    /**
     * Returns the value of this vector for the specified dimension.
     *
     * @param dimension The specified dimension.
     * @return The value of this vector for the specified dimension.
     */
    public double value(int dimension);

    /**
     * Sets the value of the specified dimension to the specified
     * value.
     *
     * <P>This operation is optional.  Implementations may
     * throw unsupported operation exceptions.
     *
     * @param dimension The specified dimension.
     * @param value The new value for the specified dimension.
     * @throws UnsupportedOperationException If this operation is not supported.
     */
    public void setValue(int dimension, double value);

    /**
     * Returns the dot product of this vector with the specified
     * vector.  The dot product is also known as the inner
     * product. The specified vector must have the same length as this
     * vector.  Dot products may be either positive or negative and
     * will be zero only if the vectors are orthogonal.
     *
     * <P>The dot product is defined as follows:
     *
     * <blockquote><code>
     *  v1 <sup><sup>.</sup></sup> v2
     *  = <big><big><big>&Sigma;</big></big></big><sub><sub>0 &lt;= n &lt; v1.numDimensions()</sub></sub> v1.value(n) * v2.value(n)
     * </code></blockquote>
     *
     * @param v The specified vector.
     * @return The dot product of this vector with the specified
     * vector.
     * @throws IllegalArgumentException If the specified vector
     * is not of the same dimensionality as this vector.
     */
    public double dotProduct(Vector v);

    /**
     * Returns the cosine product of this vector with the specified
     * vector.  The specified vector must have the same length as this
     * vector.  If all entries in the vector are defined and finite,
     * values fall in the range from -1.0 to 1.0 inclusive, with a
     * value of 1.0 or -1.0 if they are identical or opposite, and 0.0
     * if they are orthogonal.
     *
     * <P>The cosine of two vectors is defined as their dot product
     * divided by their lengths:
     *
     * <blockquote><code>
     *  cos(v1,v2) = v1.dotProduct(v2) / (v1.length() * v2.length())
     * </code></blockquote>
     *
     * <P>Applying {@link Math#acos(double)} to the result returns the
     * angle in radians, ranging from 0.0 through {@link Math#PI}.  This
     * value can be converted to degrees with {@link Math#toDegrees(double)}.
     * Thus <code>Math.acos(cosine(v))</code> is the angle in radians
     * between this vector and the vector v, and
     * <code>Math.toDegrees(Math.acos(cosine(v)))</code> is the same
     * angle in degrees.
     *
     * @param v The specified vector.
     * @return The cosine of this vector with the specified
     * vector.
     * @throws IllegalArgumentException If the specified vector
     * is not of the same dimensionality as this vector.
     */
    public double cosine(Vector v);

    /**
     * Returns the length of this vector.
     *
     * <P>The length of a vector is defined as the square root
     * of its dot product with itself:
     *
     * <blockquote><code>
     *  | v | = (v.dotProduct(v))<sup><sup>1/2</sup></sup>
     * </code></blockquote>
     *
     * @return The length of this vector.
     */
    public double length();


    /**
     * Returns a new vector that is the reuslt of adding this vector
     * to the specified vector.
     *
     * @param v The vector to add to this vector.
     * @return The result of adding the specified vector to this
     * vector.
     * @throws IllegalArgumentException If the specified vector is not
     * of the same dimensionality as this vector.
     */
    public Vector add(Vector v);


    /**
     * Return the hash code for this vector.  The hash code for
     * vectors is as if they were a one-by-n matrix, which in turn is
     * the same as if they were a <code>List</code> of
     * <code>Double</code> objects.  Hash codes are computed as
     * follows:
     *
     * <pre>
     *   int hashCode = 1;
     *   for (int i = 0; i < numRows(); ++i) {
     *     int v = Double.doubleToLongBits(value(i));
     *     int valHash = (int) (v^(v>>>32));
     *     hashCode = 31*hashCode + valHash;
     *   }
     * </pre>
     *
     * Note that this definition is consistent with {@link
     * #equals(Object)}.  Subclasses that implement this method should
     * return a result that would be the same as if it were computed
     * by the above procedure.
     *
     * @return The hash code for this vector.
     */
    public int hashCode();


    /**
     * Returns <code>true</code> if the specified object is a vector
     * that has the same dimensionality and values as this vector.
     * Note that this definition is consistent with the definition of
     * {@link #hashCode()}.
     *
     * @param that Object to test for equality with this vector.
     * @return <code>true</code> if the specified object is a vector
     * that has the same dimensionality and values as this vector.
     */
    public boolean equals(Object that);

}
