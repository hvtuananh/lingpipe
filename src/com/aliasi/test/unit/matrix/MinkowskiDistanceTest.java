package com.aliasi.test.unit.matrix;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.matrix.Vector;

import com.aliasi.matrix.EuclideanDistance;
import com.aliasi.matrix.MinkowskiDistance;
import com.aliasi.matrix.TaxicabDistance;

import com.aliasi.util.Distance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;


import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MinkowskiDistanceTest  {

    @Test
    public void testOne() throws IOException, ClassNotFoundException {
        Vector v1 = new DenseVector(new double[] { 1, 2 });
        Vector v2 = new DenseVector(new double[] { 3, 1 });

        Map<Integer,Double> map1 = new HashMap<Integer,Double>();
        map1.put(0,1.0);
        map1.put(1,2.0);
        Map<Integer,Double> map2 = new HashMap<Integer,Double>();
        map2.put(0,3.0);
        map2.put(1,1.0);
        SparseFloatVector sv1 = new SparseFloatVector(map1);
        SparseFloatVector sv2 = new SparseFloatVector(map2);

        MinkowskiDistance d1
            = new MinkowskiDistance(3);

        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d1.distance(v1,v2),
                     0.0001);
        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d1.distance(sv1,sv2),
                     0.0001);
        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d1.distance(v1,sv2),
                     0.0001);
        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d1.distance(sv1,v2),
                     0.0001);

        MinkowskiDistance d2
            = (MinkowskiDistance)
            AbstractExternalizable
            .serializeDeserialize(d1);

        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d2.distance(v1,v2),
                     0.0001);
        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d2.distance(v1,sv2),
                     0.0001);
        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d2.distance(sv1,v2),
                     0.0001);
        assertEquals(java.lang.Math.pow(2*2*2 + 1*1*1,1.0/3.0),
                     d2.distance(sv1,sv2),
                     0.0001);

        Map<Integer,Double> map3 = new HashMap<Integer,Double>();
        map3.put(1,-1.0);
        SparseFloatVector vA = new SparseFloatVector(map3,5);

        Map<Integer,Double> map4 = new HashMap<Integer,Double>();
        map4.put(2,1.0);
        SparseFloatVector vB = new SparseFloatVector(map4,5);

        assertEquals(java.lang.Math.pow(2,1.0/3.0),
                     d2.distance(vA,vB),
                     0.0001);
        assertEquals(java.lang.Math.pow(2,1.0/3.0),
                     d2.distance(vB,vA),
                     0.0001);

        Vector vC = new DenseVector(new double[] { 0, 1, 0, 0, 0 });
        Vector vD = new DenseVector(new double[] { 0, 0, 1, 0, 0 });

        assertEquals(java.lang.Math.pow(2,1.0/3.0),
                     d2.distance(vA,vD),
                     0.0001);
        assertEquals(java.lang.Math.pow(2,1.0/3.0),
                     d2.distance(vD,vA),
                     0.0001);
        assertEquals(java.lang.Math.pow(2,1.0/3.0),
                     d2.distance(vC,vB),
                     0.0001);
        assertEquals(java.lang.Math.pow(2,1.0/3.0),
                     d2.distance(vB,vC),
                     0.0001);
    }

    public void randomTest() {
        MinkowskiDistance L1 = new MinkowskiDistance(1);
        MinkowskiDistance L2 = new MinkowskiDistance(2);


        Random random = new Random();
        int numDimensions = random.nextInt(20) + 5;

        double[] xs = new double[numDimensions];
        for (int i = 0; i < xs.length; ++i)
            xs[i] = random.nextDouble();
        Vector v0 = new DenseVector(xs);

        Vector sv1 = randomSparseVector(random,numDimensions);
        Vector sv2 = randomSparseVector(random,numDimensions);

        assertSame(v0,sv1);
        assertSame(v0,sv2);
        assertSame(sv1,sv2);
    }

    void assertSame(Vector v1, Vector v2) {
        assertSame(v1,v2,new MinkowskiDistance(1),TaxicabDistance.DISTANCE);
        assertSame(v1,v2,new MinkowskiDistance(2),EuclideanDistance.DISTANCE);
    }

    void assertSame(Vector v1, Vector v2, Distance<Vector> d1, Distance<Vector> d2) {
        assertEquals(d1.distance(v1,v2),
                     d2.distance(v1,v2),
                     0.0001);
        assertEquals(d1.distance(v1,v2),
                     d2.distance(v2,v1),
                     0.0001);
        assertEquals(d1.distance(v2,v1),
                     d2.distance(v2,v1),
                     0.0001);
    }

    Vector randomSparseVector(Random random, int numDimensions) {
        Map<Integer,Double> map1 = new HashMap<Integer,Double>();
        int numEntries = random.nextInt(numDimensions+1);
        for (int i = 0; i < numEntries; ++i)
            map1.put(random.nextInt(numDimensions),
                     random.nextDouble());
        return new SparseFloatVector(map1,numDimensions);
    }


    @Test
    public void testExs() {
        Vector v1 = new DenseVector(new double[] { 0.0, 1.0 });
        Vector v2 = new DenseVector(new double[] { 2.0 });
        try {
            new MinkowskiDistance(3).distance(v1,v2);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

}
