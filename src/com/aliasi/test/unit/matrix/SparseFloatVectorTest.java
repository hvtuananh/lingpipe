package com.aliasi.test.unit.matrix;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.assertFullEquals;
import static com.aliasi.test.unit.Asserts.assertFullSerialization;
import static com.aliasi.test.unit.Asserts.succeed;


import java.util.HashMap;
import java.util.Map;

public class SparseFloatVectorTest  {

    @Test
    public void testAdd1() {
        Vector v1 = new SparseFloatVector(new int[] { 0, 2, 3 },
                                          new float[] { 0.0f, 4.0f, 9.0f },
                                          5);
        Vector v2 = new SparseFloatVector(new int[] { 1, 2, 4 },
                                          new float[] { -1f, -1f, -1f },
                                          5);
        Vector v3 = v1.add(v2);
        assertEquals(0.0f, (float)v3.value(0));
        assertEquals(-1.0f, (float)v3.value(1));
        assertEquals(3.0f, (float)v3.value(2));
        assertEquals(9.0f, (float)v3.value(3));
        assertEquals(-1.0f, (float)v3.value(4));

        Vector v4 = v2.add(v1);
        assertEquals(0.0f, (float)v4.value(0));
        assertEquals(-1.0f, (float)v4.value(1));
        assertEquals(3.0f, (float)v4.value(2));
        assertEquals(9.0f, (float)v4.value(3));
        assertEquals(-1.0f, (float)v4.value(4));
    }

    @Test
    public void testAdd2() {
        Vector v1 = new SparseFloatVector(new int[] { 0, 2 },
                                          new float[] { 0.0f, 1.0f },
                                          5);
        Vector v2 = new SparseFloatVector(new int[] { },
                                          new float[] { },
                                          5);
        Vector v3 = v1.add(v2);
        assertEquals(0.0f, (float)v3.value(0));
        assertEquals(0.0f, (float)v3.value(1));
        assertEquals(1.0f, (float)v3.value(2));
        assertEquals(0.0f, (float)v3.value(3));

        Vector v4 = v2.add(v1);
        assertEquals(0.0f, (float)v4.value(0));
        assertEquals(0.0f, (float)v4.value(1));
        assertEquals(1.0f, (float)v4.value(2));
        assertEquals(0.0f, (float)v4.value(3));
    }

    @Test
    public void testIncrementZeros() {
        Vector v1 = new SparseFloatVector(new int[] { 0, 2, 4 },
                                          new float[] { 5, 6, 7 },
                                          5);
        assertEquals(3,v1.nonZeroDimensions().length);
        assertEquals(5.0,v1.value(0),0.0001);
        assertEquals(0.0,v1.value(1),0.0001);
        assertEquals(6.0,v1.value(2),0.0001);
        assertEquals(0.0,v1.value(3),0.0001);
        assertEquals(7.0,v1.value(4),0.0001);
    }

    @Test
    public void testExplicitCons() {
        Vector v = new SparseFloatVector(new int[] { 1, 2, 5 },
                                         new float[] { 3, 6, 15 },
                                         10);
        assertEquals(10,v.numDimensions());
        assertEquals(0.0,v.value(0),0.0001);
        assertEquals(0.0,v.value(4),0.0001);
        assertEquals(3.0,v.value(1),0.0001);
        assertEquals(6.0,v.value(2),0.0001);
        assertEquals(15.0,v.value(5),0.0001);

        try {
            new SparseFloatVector(new int[] { 3, 2, 5 },
                                  new float[] { 1, 2, 3 },
                                  10);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new SparseFloatVector(new int[] { 1, 2, 5 },
                                  new float[] { 0, 0, 0 },
                                  5);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            new SparseFloatVector(new int[] { 1, 2 },
                                  new float[] { 0, 0, 0 },
                                  5);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }


    @Test
    public void testSerialization() {
        Map<Integer,Float> map = new HashMap<Integer,Float>();

        assertFullSerialization(new SparseFloatVector(map));

        map.put(0,1.0f);
        assertFullSerialization(new SparseFloatVector(map));

        map.put(17,2.0f);
        assertFullSerialization(new SparseFloatVector(map));

        map.put(Integer.MAX_VALUE-1,3.0f);
        assertFullSerialization(new SparseFloatVector(map));
    }

    @Test
    public void testZero() {
        Map<Integer,Float> map = new HashMap<Integer,Float>();
        Vector vec0 = new SparseFloatVector(map);
        assertEquals(0,vec0.numDimensions());
        assertEquals(0.0,vec0.length(),0.001);
        assertEquals(0.0,vec0.dotProduct(vec0));

        try {
            vec0.setValue(2,5.0);
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }

        try {
            vec0.value(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            succeed();
        }

        assertFullEquals(vec0,vec0);
    }

    @Test
    public void testOne() {
        Map<Integer,Float> map1 = new HashMap<Integer,Float>();
        map1.put(Integer.valueOf(3),new Float(5.0f));
        Vector vec1 = new SparseFloatVector(map1);

        assertFullEquals(vec1,vec1);
        assertEquals(4,vec1.numDimensions());
        assertEquals(5.0,vec1.length(),0.001);
        assertEquals(25.0,vec1.dotProduct(vec1));
        assertEquals(1.0,vec1.cosine(vec1));

        Map<Integer,Float> map2 = new HashMap<Integer,Float>();
        map2.put(Integer.valueOf(3),new Float(7.0f));
        map2.put(Integer.valueOf(1),new Float(9.0f));
        Vector vec2 = new SparseFloatVector(map2);
        assertFullEquals(vec2,vec2);
        assertFalse(vec1.equals(vec2));
        assertEquals(4,vec2.numDimensions());
        assertEquals(1.0,vec2.cosine(vec2));
        assertEquals(Math.sqrt(81.0 + 49.0), vec2.length(), 0.0001);
        assertEquals(35.0,vec1.dotProduct(vec2), 0.0001);
    }

    @Test
    public void testTwo() {
        Map<Integer,Float> map1 = new HashMap<Integer,Float>();
        map1.put(Integer.valueOf(0),new Float(3.0f));
        map1.put(Integer.valueOf(1),new Float(5.0f));
        map1.put(Integer.valueOf(5),new Float(7.0f));
        Vector vec1 = new SparseFloatVector(map1);


        Map<Integer,Float> map2 = new HashMap<Integer,Float>();
        map2.put(Integer.valueOf(0), new Float(11.0f));
        map2.put(Integer.valueOf(1), new Float(13.0f));
        map2.put(Integer.valueOf(3), new Float(17.0f));
        Vector vec2 = new SparseFloatVector(map2,6);

        double len1 = Math.sqrt(3.0*3.0 + 5.0*5.0 + 7.0*7.0);
        double len2 = Math.sqrt(11.0*11.0 + 13.0*13.0 + 17.0*17.0);
        double product = 3.0*11.0 + 5.0*13.0;
        double cos = product / (len1 * len2);
        assertEquals(product,vec1.dotProduct(vec2),0.0001);
        assertEquals(cos,vec1.cosine(vec2),0.0001);
    }

    @Test
    public void testMixed() {
        Map<Integer,Float> map1 = new HashMap<Integer,Float>();
        map1.put(Integer.valueOf(0),new Float(3.0f));
        map1.put(Integer.valueOf(1),new Float(5.0f));
        map1.put(Integer.valueOf(5),new Float(7.0f));
        Vector vec1 = new SparseFloatVector(map1);

        Vector vec3 = new DenseVector(new double[] { 3.0, 5.0, 0.0, 0.0, 0.0, 7.0 });
        assertFullEquals(vec1,vec3);
        // assertTrue(vec1.equals(vec3));
        // assertEquals(vec1.hashCode(), vec3.hashCode());
    }

    @Test
    public void testNumber() {
        Map<Integer,Number> map1 = new HashMap<Integer,Number>();
        map1.put(Integer.valueOf(0), Integer.valueOf(1));
        map1.put(Integer.valueOf(2), Double.valueOf(3.0));

        Vector vec = new SparseFloatVector(map1);
        assertEquals(1.0,vec.value(0),0.0001);
        assertEquals(0.0,vec.value(1),0.0001);
        assertEquals(3.0,vec.value(2),0.0001);
    }

}
