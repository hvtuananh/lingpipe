package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DenseMatrix;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Matrix;
import com.aliasi.matrix.Vector;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.assertFullEquals;
import static com.aliasi.test.unit.Asserts.succeed;


import java.util.Arrays;
import java.util.List;

public class DenseMatrixTest  {


    @Test
    public void testRowVectors() {
        Matrix m = new DenseMatrix(new double[][] { { 1, 2, 3 },
                                                    { 4, 5, 6 } });
        Vector v = m.rowVector(0);
        assertEquals(3,v.numDimensions());
        assertEquals(2.0,v.value(1),0.0001);
        assertEquals(3.0,v.value(2),0.0001);

        v.setValue(1,42);
        assertEquals(42.0,m.value(0,1),0.0001);


        v.setValue(1,2);
        assertEquals(Math.sqrt(1 + 4 + 9),v.length(),0.0001);

        Vector v2 = m.rowVector(1);
        assertEquals(4.0 + 10.0 + 18.0,v.dotProduct(v2),0.0001);

        assertEquals(v.dotProduct(v2)
                     /(Math.sqrt(1 + 4 + 9) * Math.sqrt(16 + 25 + 36)),
                     v.cosine(v2),0.0001);


        new DenseVector(new double[] { 1, 2, 3, 4 });
        succeed();
    }

    @Test
    public void testColVectors() {
        Matrix m = new DenseMatrix(new double[][] { { 1, 2, 3 },
                                                    { 4, 5, 6 } });
        Vector v = m.columnVector(1);
        assertEquals(2,v.numDimensions());
        assertEquals(2.0,v.value(0),0.0001);
        assertEquals(5.0,v.value(1),0.0001);


        v.setValue(1,42);
        assertEquals(42.0,m.value(1,1),0.0001);
    }



    @Test
    public void testSized() {
        Matrix m = new DenseMatrix(2,3);
        assertEquals(2,m.numRows());
        assertEquals(3,m.numColumns());
        assertEquals(0.0,m.value(1,2),0.001);

        assertEquals(0.0,m.value(0,1),0.001);
        m.setValue(0,1,5.0);
        assertEquals(5.0,m.value(0,1),0.001);

    }

    @Test
    public void testHashCode() {
        Matrix m = new DenseMatrix(new double[][] { { 1.0, 2.0, 3.0 },
                                                  { 4.0, 5.0, 6.0 } });

        List list = Arrays.asList(new Double[] {
            Double.valueOf(1),
            Double.valueOf(2),
            Double.valueOf(3),
            Double.valueOf(4),
            Double.valueOf(5),
            Double.valueOf(6) });
        assertEquals(list.hashCode(),m.hashCode());
    }

    @Test
    public void testEquals() {
        Matrix m1
            = new DenseMatrix(new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 }});
        Matrix m2
            = new DenseMatrix(new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 }});
        assertFullEquals(m1,m2);
    }

    @Test
    public void testAllocated() {
        Matrix m
            = new DenseMatrix(new double[][] { { 1.0, 2.0, 3.0 },
                                               { 3.0, 4.0, 5.0 }});
        assertEquals(2,m.numRows());
        assertEquals(3,m.numColumns());
        assertEquals(1.0,m.value(0,0),0.0001);

        Matrix m2
            = new DenseMatrix(new double[][] { { 1.0, 2.0, 3.0 },
                                               { 3.0, 4.0, 5.0 }});
        assertEquals(2,m2.numRows());
        assertEquals(3,m2.numColumns());
        assertEquals(1.0,m2.value(0,0),0.0001);
    }

    @Test
    public void testConstructorExs() {
        // dimensions > 0
        try {
            new DenseMatrix(new double[0][0]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new DenseMatrix(new double[2][0]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new DenseMatrix(new double[0][2]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new DenseMatrix(0,2);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new DenseMatrix(3,0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }


    }


}
