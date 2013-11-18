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

import java.util.Arrays;

/**
 * A <code>DenseMatrix</code> is a matrix implementation suitable for
 * matrices with primarily non-zero values.  The dimensionality of a
 * dense matrix is set at construction time and immutable afterwards.
 * Values may be specified at construction time or given default
 * values; they are mutable and may be reset after construction.
 *
 * <P><i>Implementation Note:</i> A dense matrix represents the values
 * with a two-dimensional array of primitive double values.
 *
 * @author Bob Carpenter
 * @version 2.4
 * @since   LingPipe2.0
 */
public class DenseMatrix extends AbstractMatrix {

    private final double[][] mValues;
    private final int mNumRows;
    private final int mNumColumns;

    private static final boolean IGNORE = true;

    /**
     * Construct a dense matrix with the specified positive number of
     * rows and columns.  All values are initially <code>0.0</code>.
     *
     * @param numRows Number of rows for this matrix.
     * @param numColumns Number of columns for this matrix.
     * @throws IllegalArgumentException If either the number of rows or
     * columns is not positive.
     */
    public DenseMatrix(int numRows, int numColumns) {
        this(zeroValues(numRows,numColumns),IGNORE);
    }

    /**
     * Construct a dense matrix with the specified values.  Row
     * dimensionality is determined by the dimensionality of the
     * specified array of values.  Column dimensionality is specified
     * as the maximum length of a row of specified values.  Shorter
     * rows in the specified values are filled with <code>0.0</code>.
     * All labels are initialized to <code>null</code>.
     *
     * @param values Two-dimensional array of values on which to base
     * this matrix.
     * @throws IllegalArgumentException If the either dimension of
     * the values array is 0.
     */
    public DenseMatrix(double[][] values) {
	this(copyValues(values),IGNORE);
    }

    // package protected with dummy arg to distinguish
    DenseMatrix(double[][] values,
                boolean ignoreMe) {
        mValues = values;
	mNumRows = values.length;
	if (mNumRows < 1) {
	    String msg = "Require positive number of rows."
		+ " Found number of rows=" + mNumRows;
	    throw new IllegalArgumentException(msg);
	}
	mNumColumns = values[0].length; 
	if (mNumColumns < 1) {
	    String msg = "Require positive number of columns."
		+ " Found number of columns=" + mNumColumns;
	    throw new IllegalArgumentException(msg);
	}
    }

    @Override
    public final int numRows() {
        return mNumRows;
    }

    @Override
    public final int numColumns() {
        return mNumColumns;
    }

    @Override
    public double value(int row, int column) {
        return mValues[row][column];
    }

    @Override
    public void setValue(int row, int column, double value) {
        mValues[row][column] = value;
    }


    private static int numColumns(double[][] values) {
	int numColumns = 0;
	for (int i = 0; i < values.length; ++i)
	    numColumns = Math.max(numColumns,values[i].length);
	return numColumns;
    }

    private static double[][] copyValues(double[][] values) {
	int numRows = values.length;
	int numColumns = numColumns(values);
	double[][] result = new double[numRows][numColumns];
        for (int i = 0; i < numRows; ++i) {
            for (int j = 0; j < values[i].length; ++j)
                result[i][j] = values[i][j];
            for (int j = values[i].length; j < numColumns; ++j)
                result[i][j] = 0.0;
        }
	return result;
    }

    private static double[][] zeroValues(int numRows, int numColumns) {
	double[][] result = new double[numRows][numColumns];
        for (int i = 0; i < result.length; ++i)
            Arrays.fill(result[i],0.0);
	return result;
    }

}
