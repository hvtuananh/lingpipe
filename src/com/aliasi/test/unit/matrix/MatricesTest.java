package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DenseMatrix;
import com.aliasi.matrix.Matrices;
import com.aliasi.matrix.Matrix;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


public class MatricesTest  {

    @Test
    public void testHasZeroDiagonal() {
        Matrix m = new DenseMatrix(new double[][] { { 0, 2 },
                                                    { 1, 0 } });
        assertTrue(Matrices.hasZeroDiagonal(m));

        Matrix m2 = new DenseMatrix(new double[][] { { 0, 2, 3},
                                                     { 1, 0, 4 } });
        assertFalse(Matrices.hasZeroDiagonal(m2));
        
        Matrix m3 = new DenseMatrix(new double[][] { { 0, 2, 3 },
                                                     { 1, 0, 4 },
                                                     { 2, 0, 1 } });
        assertFalse(Matrices.hasZeroDiagonal(m3));
    }

    @Test
    public void testNonNegative() {
        Matrix m = new DenseMatrix(new double[][] { { 1.0, 2.0 },
                                                    { 3.0, 5.0 } });
        assertTrue(Matrices.isNonNegative(m));
        m.setValue(0,0,-1.0);
        assertFalse(Matrices.isNonNegative(m));
        m.setValue(0,0,1.0);
        assertTrue(Matrices.isNonNegative(m));
        m.setValue(1,0,Double.NaN);
        assertFalse(Matrices.isNonNegative(m));
        m.setValue(1,0,1.0);
        assertTrue(Matrices.isNonNegative(m));
        m.setValue(0,1,Double.NEGATIVE_INFINITY);
        assertFalse(Matrices.isNonNegative(m));
    }

    @Test
    public void testSymmetric() {
        Matrix m = new DenseMatrix(new double[2][1]);
        assertFalse(Matrices.isSymmetric(m));

        m = new DenseMatrix(new double[2][3]);
        assertFalse(Matrices.isSymmetric(m));

        m = new DenseMatrix(new double[3][3]);
        assertTrue(Matrices.isSymmetric(m));

        m.setValue(0,0,3);
        assertTrue(Matrices.isSymmetric(m));
        m.setValue(0,1,3);
        assertFalse(Matrices.isSymmetric(m));
        m.setValue(1,0,3);
        assertTrue(Matrices.isSymmetric(m));

        Matrix m2 = new DenseMatrix(new double[][] { { 0, 2, 1 },
                                                     { 2, 0, 1 },
                                                     { 1, 1, 3 } });
        assertTrue(Matrices.isSymmetric(m2));
    }




}
