package com.aliasi.test.unit.matrix;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.assertFullEquals;


import java.util.Arrays;
import java.util.List;

import java.io.IOException;

public class DenseVectorTest  {

    @Test
    public void testSerialize() throws IOException {
        assertSerializeDeserialize(new DenseVector(new double[] { 1 }));
        assertSerializeDeserialize(new DenseVector(new double[] { 1, 2, 3 }));
        assertSerializeDeserialize(new DenseVector(17));
        assertSerializeDeserialize(new DenseVector(new DenseVector(17)));
    }

    void assertSerializeDeserialize(DenseVector v) throws IOException {
        DenseVector v2
            = (DenseVector) AbstractExternalizable.serializeDeserialize(v);
        assertEquals(v.numDimensions(), v2.numDimensions());
        for (int i = 0; i < v.numDimensions(); ++i)
            assertEquals(v.value(i),v2.value(i),0.000001);
    }

    @Test
    public void testIncrementZeros() {
        // dense.dense
        Vector v1 = new DenseVector(new double[] { 1, 2, 3 });
        Vector v2 = new DenseVector(new double[] { 5, 6, 7 });

        int[] nonZeroDims = v1.nonZeroDimensions();
        assertEquals(3,nonZeroDims.length);
        assertEquals(0,nonZeroDims[0]);
        assertEquals(1,nonZeroDims[1]);
        assertEquals(2,nonZeroDims[2]);

        v2.increment(2.0,v1);
        assertEquals(7.0,v2.value(0),0.00001);
        assertEquals(10.0,v2.value(1),0.00001);
        assertEquals(13.0,v2.value(2),0.00001);

        // dense.sparse
        Vector v3 = new DenseVector(new double[] { 1, 2, 3});
        Vector v4 = new SparseFloatVector(new int[] { 0, 2 },
                                          new float[] { 5.0f, 6.0f },
                                          3);
        v3.increment(2.0,v4);
        assertEquals(11.0,v3.value(0),0.0001);
        assertEquals(2.0,v3.value(1),0.0001);
        assertEquals(15.0,v3.value(2),0.0001);


    }


    @Test
    public void testVectorOps() {
        Vector v1 = new DenseVector(new double[] { 1, 1, 0});
        Vector v2 = new DenseVector(new double[] { 1, 0, 1});
        assertEquals(Math.sqrt(2),v1.length(),0.0001);
        assertEquals(Math.sqrt(2),v2.length(),0.0001);
        assertEquals(1.0,v1.dotProduct(v2),0.0001);
        assertEquals(1.0,v2.dotProduct(v1),0.0001);
        assertEquals(1.0/2.0,v1.cosine(v2),0.0001);
        assertEquals(1.0/2.0,v2.cosine(v1),0.0001);

        Vector v3 = new DenseVector(new double[] { 3, 5 });
        try {
            v1.dotProduct(v3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            v3.dotProduct(v1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            v1.cosine(v3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            v3.cosine(v1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testSized() {
        Vector v = new DenseVector(2);
        assertEquals(2,v.numDimensions());
        assertEquals(0.0,v.value(0),0.001);

        v.setValue(0,5.0);
        assertEquals(5.0,v.value(0),0.001);

        v.setValue(1,2.0);
        assertEquals(2.0,v.value(1),0.0001);

    }

    @Test
    public void testHashCode() {
        Vector v = new DenseVector(new double[] { 1, 2, 3 });
        List list = Arrays.asList(new Double[] {
            Double.valueOf(1),
            Double.valueOf(2),
            Double.valueOf(3) });
        assertEquals(list.hashCode(),v.hashCode());
    }

    @Test
    public void testEquals() {
        Vector v1 = new DenseVector(new double[] { 1, 3, 7, 12 });
        Vector v2 = new DenseVector(4);
        v2.setValue(0,1);
        v2.setValue(1,3);
        v2.setValue(2,7);
        v2.setValue(3,12);
        assertFullEquals(v1,v2);
    }

    @Test
    public void testAllocated() {
        Vector v = new DenseVector(new double[] { 1, 2, 3});
        assertEquals(3,v.numDimensions());
        assertEquals(2.0,v.value(1),0.0001);

        Vector v2 = new DenseVector(new double[] { 1, 2, 3});
        assertEquals(3,v2.numDimensions());
        assertEquals(2.0,v2.value(1),0.0001);

        Vector v3 = new DenseVector(2);
        assertEquals(2,v3.numDimensions());
        assertEquals(0.0,v3.value(1),0.0001);
    }


    @Test
    public void testConstructorExs() {
        try {
            new DenseVector(new double[0]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new DenseVector(0);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }


    }



}
