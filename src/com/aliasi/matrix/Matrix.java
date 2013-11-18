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
 * A <code>Matrix</code> represents a 2-dimensional matrix.
 * Matrices provide a fixed number of rows and columns, with values
 * for each row and column.
 *
 * <P>An optional operation {@link #setValue(int,int,double)} allows
 * values to be set.
 *
 * <P>Two matrices should be equal if they have the same number of
 * rows and columns and every value is equal.  Their hash codes should
 * be defined as specified in the documentation for {@link
 * #hashCode()}.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public interface Matrix {

    /**
     * Returns the number of rows in the matrix.
     *
     * @return The number of rows in the matrix.
     */
    public int numRows();

    /**
     * Returns the number of columns in the matrix.
     *
     * @return The number of columns in the matrix.
     */
    public int numColumns();

    /**
     * Returns the value in the matrix at the specified row and
     * column.
     *
     * @param row The specified row.
     * @param column The specified column.
     * @return The value in the matrix at the specified row and
     * column.
     * @throws IndexOutOfBoundsException If the row or column index
     * are out of bounds for this matrix.
     */
    public double value(int row, int column);

    /**
     * Sets the value for the specified row and column to be
     * the specified value.
     *
     * <P>This operation is optional.  Implementations may
     * throw unsupported operation exceptions.
     *
     * @param row Specified row.
     * @param column Specified column.
     * @param value New value for specified row and column.
     * @throws UnsupportedOperationException If this operation is not
     * supported.
     * @throws IndexOutOfBoundsException If the row or column index
     * are out of bounds for this matrix.
     */
    public void setValue(int row, int column, double value);


    /**
     * Returns the vector of values in the specified row.  The
     * dimensionality of the returned vector will be the number of
     * columns in this matrix.
     *
     * <P>Changes to the returned vector will affect this matrix and
     * vice-versa.  To circumvent this dependency, clone the result.
     *
     * @param row Index of row whose vector is returned.
     * @return Row vector for specified row.
     * @throws IndexOutOfBoundsException If the row index is
     * out of bounds for this matrix.
     */
    public Vector rowVector(int row);

    /**
     * Returns the vector of values in the specified column.
     * The dimensionality of the returned vector will be
     * the number of rows in this matrix.
     *
     * <P>Changes to the returned vector will affect this matrix and
     * vice-versa.  To circumvent this dependency, clone the result.
     *
     * @param column Index of column whose vector is returned.
     * @return Column vector for specified column.
     * @throws IndexOutOfBoundsException If the column index is
     * out of bounds for this matrix.
     */
    public Vector columnVector(int column);

    /**
     * Returns <code>true</code> if the specified object is a matrix
     * with the same number of rows and columns and the same value in
     * every cell as this matrix.
     *
     * @param that Object to test for equality with this matrix.
     * @return <code>true</code> if the specified object is a matrix
     * with the same values and number of rows and columns as this
     * matrix.
     */
    public boolean equals(Object that);

    /**
     * Return the hash code for the matrix. The hash code of a
     * matrix is computed value-wise as if it were a
     * <code>java.util.List</code> of <code>java.lang.Double</code> values
     * ordered row by row:
     *
     * <pre>
     *   int hashCode = 1;
     *   for (int i = 0; i < numRows(); ++i) {
     *     for (int j = 0; j < numColumns(); ++j) {
     *       int v = Double.doubleToLongBits(value(i,j));
     *       int valHash = (int) (v^(v>>>32));
     *       hashCode = 31*hashCode + valHash;
     *     }
     *   }
     * </pre>
     *
     * Note that this definition is consistent with {@link #equals(Object)}.
     *
     * @return The hash code for this matrix.
     */
    public int hashCode();

}
