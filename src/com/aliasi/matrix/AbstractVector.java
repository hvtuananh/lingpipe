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
 * An <code>AbstractVector</code> implements most of a vector's
 * functionality in terms of methods for dimensionality and values.
 *
 * @author Bob Carpenter
 * @version 3.5.1
 * @since   LingPipe2.0
 */
public abstract class AbstractVector implements Vector {

    /**
     * Construct an abstract vector.
     */
    protected AbstractVector() {
        /* empty constructor */
    }

    /**
     * Returns an array with the non-zero dimensions of this vector.
     *
     * The implementation in this class is makes two passes over the
     * input data, first collecting the count of non-zero dimensions,
     * and then filling in the values.
     *
     * @return The non-zero dimensions of this vector.
     */
    public int[] nonZeroDimensions() {
        int count = 0;
        for (int i = 0; i < numDimensions(); ++i)
            if (value(i) != 0)
                ++count;
        int[] result = new int[count];
        int pos = 0;
        for (int i = 0; i < numDimensions(); ++i)
            if (value(i) != 0)
                result[pos++] = i;
        return result;
    }

    /**
     * Adds the specified vector multiplied by the specified scalar to
     * this vector.
     *
     * @param scale The scalar multiplier for the added vector.
     * @param v Vector to scale and add to this vector.
     * @throws IllegalArgumentException If the specified vector is not
     * of the same dimensionality as this vector.
     */
    public void increment(double scale, Vector v) {
        if (v.numDimensions() != numDimensions()) {
            String msg = "Specified vector not same dimensionality."
                + " Found this.numDimensions()=" + numDimensions()
                + " v.numDimensions()=" + v.numDimensions();
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < numDimensions(); ++i)
            setValue(i, value(i) + scale * v.value(i));
    }

    /**
     * Returns the number of dimensions of this vector.
     * Concrete subclasses must implement at least this
     * method and {@link #value(int)}.
     *
     * @return The number of dimensions of this vector.
     */
    public abstract int numDimensions();

    /**
     * The value of this vector for the specified dimension.
     * Concrete subclasses must implement at least this
     * method and {@link #numDimensions()}.
     *
     * @param dimension Dimension whose value is returned.
     * @return Value of this vector for the specified dimension.
     * @throws IndexOutOfBoundsException If the dimension is less than
     * zero or greater than or equal to the number of dimensions.
     */
    public abstract double value(int dimension);

    /**
     * Throws an unsupported operation exception.  Subclasses
     * with mutable values should override this method.
     *
     * @param dimension Ignored.
     * @param value Ignored.
     * @throws UnsupportedOperationException Always.
     */
    public void setValue(int dimension, double value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the length of this vector.
     *
     * <P>the implementation iterates over the dimensions once
     * accessing each value.
     *
     * @return The length of this vector.
     */
    public double length() {
        double length = 0.0;
        for (int i = numDimensions(); --i >= 0; ) {
            double val = value(i);
            length += val*val;
        }
        return Math.sqrt(length);
    }

    /**
     * Returns the result of adding the specified vector to this
     * vector.
     *
     * <p><i>Implementation Note:</i> The result is a dense vector and
     * this method iterates over the dimensions adding.  Subclasses
     * may override this with a more specific implementation and then
     * fall back on this implementation for the general case.
     *
     * @param v Vector to add to this vector.
     * @throws IllegalArgumentException If the specified vector does
     * not have the same dimensionality as this vector.
     */
    public Vector add(Vector v) {
        int numDimensions = numDimensions();
        if (v.numDimensions() != numDimensions) {
            String msg = "Arrays must have same dimensions to add."
                + " found this.numDimensions()=" + numDimensions
                + " v.numDimensions()=" + v.numDimensions();
            throw new IllegalArgumentException(msg);
        }
        double[] result = new double[numDimensions];
        for (int i = 0; i < numDimensions; ++i)
            result[i] = value(i) + v.value(i);
        return new DenseVector(result);
    }

    /**
     * Returns the dot product (inner product) of this vector with the
     * specified vector.
     *
     * <P><i>Implementation Note:</i> This method iterates over
     * the dimensions, accessing values for this vector and the
     * specified vector for each dimension.
     *
     * @param v The specified vector.
     * @return The dot product of this vector with the specified
     * vector.
     * @throws IllegalArgumentException If the specified vector
     * is not of the same dimensionality as this vector.
     */
    public double dotProduct(Vector v) {
        verifyMatchingDimensions(v);
        double product = 0.0;
        for (int i = numDimensions(); --i >= 0; )
            product += value(i) * v.value(i);
        return product;
    }

    /**
     * Returns the cosine of this vector and the specified vector.
     *
     * <P><i>Implementation Note</i>: This method iterates over the
     * dimensions once and accesses the value of each vector once per
     * dimension.
     *
     * @param v The specified vector.
     * @return The cosine of this vector with the specified
     * vector.
     * @throws IllegalArgumentException If the specified vector
     * is not of the same dimensionality as this vector.
     */
    public double cosine(Vector v) {
        verifyMatchingDimensions(v);
        double product = 0.0;
        double length1 = 0.0;
        double length2 = 0.0;
        for (int i = numDimensions(); --i >= 0; ) {
            double val1 = value(i);
            double val2 = v.value(i);
            product += val1 * val2;
            length1 += val1 * val1;
            length2 += val2 * val2;
        }
        double cosine =  product / Math.sqrt(length1 * length2);
        return (cosine < -1.0)
            ? -1.0
            : ( (cosine > 1.0)
                ? 1.0
                : cosine );
    }

    /**
     * Returns <code>true</code> if the specified object is a vector
     * with the same dimensionality and values as this vector.  Note
     * that labels are not considered for equality.  This
     * implementation is consistent with hash codes.
     *
     * @param that Specified object.
     * @return <code>true</code> if the specified object is a vector
     * with the same dimensionality and values as this vector.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Vector)) return false;
        Vector thatVector = (Vector) that;
        if (numDimensions() != thatVector.numDimensions())
            return false;
        for (int i = numDimensions(); --i >= 0; )
            if (value(i) != thatVector.value(i))
                return false;
        return true;
    }

    /**
     * Returns the hash code for this vector according to the
     * specification.  This hash code is compatible with equality.
     *
     * <i>Implementation Note:</i> The implementation iterates over
     * the dimensions accessing the value of each dimension once.
     *
     * @return The hash code for this vector.
     */
    @Override
    public int hashCode() {
        int code = 1;
        int numDimensions = numDimensions();
        for (int i = 0; i < numDimensions; ++i) {
            if (value(i) == 0.0) continue;
            long v = Double.doubleToLongBits(value(i));
            int valHash = (int)(v^(v>>>32));
            code = 31 * code + valHash;
        }
        return code;
    }

    void verifyMatchingDimensions(Vector v) {
        if (numDimensions() != v.numDimensions()) {
            String msg = "Vectors must be same dimensionality."
                + " This vector's dimensionality=" + numDimensions()
                + " Specified vector's dimensionality=" + v.numDimensions();
            throw new IllegalArgumentException(msg);
        }
    }

}
