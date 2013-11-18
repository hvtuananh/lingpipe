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
 * A <code>ProximityMatrix</code> provides a pseudo-metric of proximities
 * between points.  As a matrix, proximity matrices are symmetric, contain
 * only non-negative values and have <code>0.0</code> values on the diagonal.
 * 
 * <P><i>Implementation Note:</i> Proximity matrices are implemented as a
 * wrapper around a triangular matrix of primitive double values
 * without the diagonal values.
 *
 * @author Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.0
 */
public class ProximityMatrix extends AbstractMatrix {

    private final double[][] mValues;
    private final int mSize;
    
    ProximityMatrix(double[][] values, boolean ignoreMe) {
	mValues = values;
	mSize = values.length;
    }

    /**
     * Construct a square proximity matrix of the specified
     * dimensionality, with all values initially zero.
     *
     * @param numDimensions Number of dimensions.
     */
    public ProximityMatrix(int numDimensions) {
	this(zeroValues(numDimensions),true);
    }

    /**
     * Returns the number of columns for this matrix.  The number
     * of rows is the same as the number of columns.
     *
     * @return The number of columns for this matrix.  
     */
    @Override
    public int numColumns() {
	return mSize;
    }

    /**
     * Returns the number of rows for this matrix.  The number
     * of rows is the same as the number of columns.
     *
     * @return The number of rows for this matrix.  
     */
    @Override
    public int numRows() {
	return mSize;
    }

    /**
     * Sets the value for the specified row and column to the
     * specified non-negative value as well as swapping row for column
     * for symmetry.  Thus <code>setValue(i,j,x)</code> sets the value
     * for row <code>i</code> and column <code>j</code> to
     * <code>x</code> as well as setting the value for row
     * <code>j</code> and column <code>i</code> to <code>x</code>.
     *
     * @param row Matrix row.
     * @param column Matrix column.
     * @param value Value to set for row and column and reverse.
     * @throws IllegalArgumentException If the row and column are the
     * same or if the value is negative or not a number. 
     * @throws IndexOutOfBoundsException If the row or column are less
     * than 0 or greater than the dimensionality of this proximity
     * matrix.
     */
    @Override
    public void setValue(int row, int column, double value) {
	if (row == column) {
	    if (value == 0.0) return;
	    String msg = "Cannot set non-zero diagonal on a proximity matrix."
		+ " Found row=" + row
		+ " column=" + column
		+ " value=" + value;
	    throw new IllegalArgumentException(msg);
	}
	if (value < 0.0 || Double.isNaN(value)) {
	    String msg = "Proximity matrix values must be >= 0.0"
		+ " Found=" + value;
	    throw new IllegalArgumentException(msg);
	}
	if (row < column) {
	    mValues[column-1][row] = value;
        } else {
	    mValues[row-1][column] = value;
        }
    }

    /**
     * Returns the value in this proximity matrix for the specified
     * row and column.
     *
     * @param row Specified row.
     * @param column Specified column.
     * @return Value for the specified row and column.
     * @throws IndexOutOfBoundsException If the row or column are
     * negative or greater than or equal to the dimensionality of this
     * matrix.
     */
    @Override
    public double value(int row, int column) {
	if (row == column) {
	    if (row > mSize) {
		String msg = "Index out of bounds row=column=" + row;
		throw new IndexOutOfBoundsException(msg);
	    } 
	    return 0.0;
	}
        return (row < column) 
            ? mValues[column-1][row]
	    : mValues[row-1][column];
    }


    private static double[][] zeroValues(int numDimensions) {
	if (numDimensions < 1) {
	    String msg = "Require positive number of dimensions."
		+ " Found numDimensions=" + numDimensions;
	    throw new IllegalArgumentException(msg);
	}
	double[][] values = new double[numDimensions-1][];
	for (int i = 0; i < values.length; ++i) {
	    values[i] = new double[i + 1];
	    for (int j = 0; j < values[i].length; ++j)
		values[i][j] = 0.0;
	}
	return values;
    }

}
