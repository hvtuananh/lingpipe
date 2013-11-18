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
 * An <code>AbstractMatrix</code> implements most of a matrix's
 * functionality in terms of methods for accessing numbers of rows and
 * columns and values.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public abstract class AbstractMatrix implements Matrix {

    /**
     * Construct an abstract matrix.
     */
    protected AbstractMatrix() {
        /* do nothing */
    }

    public abstract int numRows();

    public abstract int numColumns();

    public abstract double value(int row, int column);


    /**
     * Throw an unsupported operation exception.  This
     * method should be overriden by value mutable subclasses.
     *
     * @param row Ignored.
     * @param column Ignored.
     * @param value Ignored.
     * @throws UnsupportedOperationException If not overridden by a
     * subclass implementation.
     * @throws IndexOutOfBoundsException If the row or column indexes
     * are out of bounds.
     */
    public void setValue(int row, int column, double value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the specified row of this matrix.  The vector returned
     * is backed by this matrix, and changes to its values or labels
     * affect this matrix.  The returned vector may be cloned to
     * produce an equivalent vector that is not linked to this matrix.
     *
     * <P>No check is made that the row is within current bounds of the
     * matrix, but attempts to access values for a row matrix that
     * is out of bounds will throw a runtime exception if the underlying
     * matrix's <code>value(int,int)</code> method throws an exception..
     *
     * @param row Row whose vector is returned.
     * @return Vector for the specified row.
     * @throws IndexOutOfBoundsException If the row index is out of bounds.
     */
    public Vector rowVector(int row) {
        return new MatrixBackedVector.Row(this,row);
    }

    /**
     * Returns the specified column of this matrix.  The vector
     * returned is backed by this matrix, and changes to its values or
     * labels affect this matrix.  The returned vector may be cloned
     * to produce an equivalent vector that is not linked to this
     * matrix.
     *
     * <P>No check is made that the column is within current bounds of
     * the matrix, but attempts to access values for a column matrix
     * that is out of bounds will throw a runtime exception if the
     * underlying matrix's <code>value(int,int)</code> method throws
     * an exception.
     *
     * @param column Column whose vector is returned.
     * @return Vector for the specified column.
     */
    public Vector columnVector(int column) {
        return new MatrixBackedVector.Column(this,column);
    }

    /**
     * Return <code>true</code> if the specified object is a matrix of
     * the same dimensionality with the same values.  Note that labels
     * are ignored in establishing matrix identity.  This definition
     * is consistent witht he definition of {@link #hashCode()}.
     *
     * @param that Object to test for equality with this matrix.
     * @return <code>true</code> if the specified object is a matrix
     * with the same dimensionality and values as this matrix.
     *
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Matrix)) return false;
        Matrix thatMatrix = (Matrix) that;
        if (numRows() != thatMatrix.numRows()) return false;
        if (numColumns() != thatMatrix.numColumns()) return false;
        for (int i = numRows(); --i >= 0; )
            if (!rowVector(i).equals(thatMatrix.rowVector(i)))
                return false;
        return true;
    }

    /**
     * Returns a hash code as specified in the <code>Matrix</code>
     * interface's documentation.
     *
     * @return Hash code for this matrix.
     */
    @Override
    public int hashCode() {
        int code = 1;
        int numRows = numRows();
        int numColumns = numColumns();
        for (int i = 0; i < numRows; ++i)
            for (int j = 0; j < numColumns; ++j) {
                long v = Double.doubleToLongBits(value(i,j));
                int valHash = (int)(v^(v>>>32));
                code = 31 * code + valHash;
            }
        return code;
    }






}
