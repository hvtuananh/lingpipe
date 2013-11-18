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
 * The <code>Matrices</code> class contains static utility methods
 * for various matrix properties and operations.
 *
 * @author  Bob Carpenter
 * @version 3.5
 * @since   LingPipe2.0
 */
public class Matrices {

    // don't allow instances
    private Matrices() {
        /* do nothing */
    }

    /**
     * Returns an unmodifiable view of the specified vector.
     * This provides a &quot;read-only&quot; version of the specified
     * vector.  The returned vector is backed by the specified vector,
     * so changes to the specified vector will be refelcted in
     * the returned vector.
     *
     * An unmodifiable vector throws an {@link UnsupportedOperationException}
     * if {@link Vector#setValue(int,double)} is called.
     *
     * @param v Vector to return an
     */
    public static Vector unmodifiableVector(Vector v) {
        return new UnmodifiableVector(v);
    }

    static class UnmodifiableVector extends VectorFilter {
        UnmodifiableVector(Vector v) {
            super(v);
        }
        @Override
        public void setValue(int dimension, double value) {
            String msg = "Cannot modify an unmodifiable vector.";
            throw new UnsupportedOperationException(msg);
        }
        @Override
        public String toString() {
            return mV.toString();
        }
    }

    static class VectorFilter implements Vector {
        protected final Vector mV;
        VectorFilter(Vector v) {
            mV = v;
        }
        public int[] nonZeroDimensions() {
            return mV.nonZeroDimensions();
        }
        public void increment(double scale, Vector v) {
            mV.increment(scale,v);
        }
        public Vector add(Vector v) {
            return mV.add(v);
        }
        public double cosine(Vector v) {
            return mV.cosine(v);
        }
        public double dotProduct(Vector v) {
            return mV.dotProduct(v);
        }
        @Override
        public boolean equals(Object that) {
            return mV.equals(that);
        }
        @Override
        public int hashCode() {
            return mV.hashCode();
        }
        public double length() {
            return mV.length();
        }
        public int numDimensions() {
            return mV.numDimensions();
        }
        public void setValue(int dimension, double value) {
            mV.setValue(dimension,value);
        }
        public double value(int dimension) {
            return mV.value(dimension);
        }
    }

    /**
     * Returns <code>true</code> if the specified matrix has
     * only zero values on its diagonal.  If the matrix is
     * not square or has non-zero values on the diagonal, the
     * return result is <code>false</code>.
     *
     * @param m Matrix to test.
     * @return <code>true</code> if the matrix is square and has
     * only zero values on its diagonal.
     */
    public static boolean hasZeroDiagonal(Matrix m) {
        int n = m.numRows();
        if (n != m.numColumns())
            return false;
        for (int i = 0; i < n; ++i)
            if (m.value(i,i) != 0.0)
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if the specified matrix is symmetric.
     * A matrix <code>m</code> is symmetric if it's equal to its
     * transpose, <code>m = m<sup><sup>T</sup></sup></code>.  Stated
     * directly, a matrix <code>m</code> is symmetric if it has the
     * same number of rows as columns:
     *
     * <blockquote><code>
     * m.numRows() == m.numColumns()
     * </code></blockquote>
     *
     * and meets the symmetry condition:
     *
     * <blockquote>
     * <code>m.value(i,j) == m.value(j,i)</code>
     * for <code>i,j &lt; m.numRows()</code>.
     * </blockquote>
     *
     * @param m Matrix to test.
     * @return <code>true</code> if the matrix is symmetric.
     */
    public static boolean isSymmetric(Matrix m) {
        int n = m.numRows();
        if (n != m.numColumns()) return false;
        for (int i = 0; i < n; ++i)
            for (int j = i+1; j < n; ++j)
                if (m.value(i,j) != m.value(j,i))
                    return false;
        return true;
    }


    /**
     * Returns <code>true</code> if the matrix contains only positive
     * numbers or zeros.  If it contains a finite negative number,
     * {@link Double#NaN}, or {@link Double#NEGATIVE_INFINITY}, the
     * result will be <code>false</code>.
     *
     * @param m Matrix to test.
     * @return <code>true</code> if the matrix contains only positive
     * entries or zeros.
     */
    public static boolean isNonNegative(Matrix m) {
        for (int i = 0; i < m.numRows(); ++i)
            for (int j = 0; j < m.numColumns(); ++j)
                if (m.value(i,j) < 0.0 || Double.isNaN(m.value(i,j)))
                    return false;
        return true;
    }

    /**
     * Returns the content of the specified vector as an array.
     *
     * @param v The vector.
     * @return The content of the vector as an array.
     */
    public static double[] toArray(Vector v) {
        double[] xs = new double[v.numDimensions()];
        for (int i = 0; i < xs.length; ++i)
            xs[i] = v.value(i);
        return xs;
    }

    static Vector add(Vector v1, Vector v2) {
        int numDimensions = v1.numDimensions();
        if (numDimensions != v2.numDimensions()) {
            String msg = "Can only add vectors of the same dimensionality."
                + " Found v1.numDimensions()=" + v1.numDimensions()
                + " v2.numDimensions()=" + v2.numDimensions();
            throw new IllegalArgumentException(msg);
        }
        double[] vals = new double[numDimensions];
        for (int i = 0; i < numDimensions; ++i)
            vals[i] = v1.value(i) + v2.value(i);
        return new DenseVector(vals);
    }

}


