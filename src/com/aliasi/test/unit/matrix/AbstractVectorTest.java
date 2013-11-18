package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.AbstractVector;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;

public class AbstractVectorTest  {

    @Test
    public void testOne() {
        Vector v1 = new Vector1();
        Vector v2 = new Vector2();
        assertFalse(v1.equals(v2));
        assertEquals(v1,v1);

        Vector v3 = v1.add(v2);
        assertEquals(3,v3.numDimensions());
        assertEquals(8.0,v3.value(1),0.00001);

        double l1 = Math.sqrt(1 + 4 + 9);
        double l2 = Math.sqrt(25 + 36 + 49);
        assertEquals(l1,v1.length());

        double dot = 5 + 12 + 21;
        assertEquals(dot,v1.dotProduct(v2));

        double cos = dot / (l1 * l2);
        assertEquals(cos,v1.cosine(v2),0.00001);

        try {
            v1.setValue(1,3);
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }

        try {
            v1.value(7);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }

        int[] expectedNonZeroDims = new int[] { 0, 1, 2 };
        int[] nonZeroDims = v1.nonZeroDimensions();
        assertEquals(expectedNonZeroDims.length, nonZeroDims.length);
        for (int i = 0; i < nonZeroDims.length; ++i)
            assertEquals(expectedNonZeroDims[i], nonZeroDims[i]);

        try {
            v1.increment(1.0, new DenseVector(new double[] { -1, 0, 0 }));
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }

        assertEquals(1.0,v1.value(0),0.00001);
    }


    @Test
    public void testMut() {
        Vector v = new MutVect();

        assertEquals(0,v.nonZeroDimensions().length);

        v.increment(3.0,new Vector1());
        assertEquals(3,v.nonZeroDimensions().length);

        assertEquals(3.0,v.value(0),0.0001);
        assertEquals(6.0,v.value(1),0.0001);
        assertEquals(9.0,v.value(2),0.0001);

        Vector v2 = new MutVect();
        v2.increment(1.0, new DenseVector(new double[] { 0, 1, 0 }));
        assertEquals(1,v2.nonZeroDimensions().length);
        assertEquals(1,v2.nonZeroDimensions()[0]);
    }

    public static class MutVect extends AbstractVector {
        double[] xs = new double[] { 0, 0, 0 };
        @Override
        public int numDimensions() {
            return 3;
        }
        @Override
        public double value(int i) {
            return xs[i];
        }
        @Override
        public void setValue(int i, double v) {
            xs[i] = v;
        }
    }


    public static class Vector1 extends AbstractVector {
        @Override
        public int numDimensions() {
            return 3;
        }
        @Override
        public double value(int d) {
            if (d == 0) return 1.0;
            if (d == 1) return 2.0;
            if (d == 2) return 3.0;
            throw new IndexOutOfBoundsException("boo");
        }
    }

    public static class Vector2 extends AbstractVector {
        @Override
        public int numDimensions() {
            return 3;
        }
        @Override
        public double value(int d) {
            if (d == 0) return 5.0;
            if (d == 1) return 6.0;
            if (d == 2) return 7.0;
            throw new IndexOutOfBoundsException("boo");
        }
    }



}
