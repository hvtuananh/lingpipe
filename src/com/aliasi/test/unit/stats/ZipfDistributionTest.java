package com.aliasi.test.unit.stats;

import com.aliasi.stats.ZipfDistribution;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;



public class ZipfDistributionTest  {

    @Test
    public void testZero() {
        assertTrue(true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExs() {
        new ZipfDistribution(-1);
    }

    @Test
    public void testDistro() {

        ZipfDistribution dist = new ZipfDistribution(3);
        assertEquals(3,dist.numOutcomes());
        double sum = 1.0 + 1.0/2.0 + 1.0/3.0;

        double p1 = 1.0/sum;
        double p2 = 1.0/2.0/sum;
        double p3 = 1.0/3.0/sum;

        assertEquals(p1,dist.probability(1),0.0005);
        assertEquals(p2,dist.probability(2),0.0005);
        assertEquals(p3,dist.probability(3),0.0005);
        assertEquals(0.0,dist.probability(0),0.0005);
        assertEquals(0.0,dist.probability(-1),0.0005);
        assertEquals(0.0,dist.probability(20),0.0005);
    
        double[] probs = ZipfDistribution.zipfDistribution(3);
        assertEquals(p1,probs[0],0.0005);
        assertEquals(p2,probs[1],0.0005);
        assertEquals(p3,probs[2],0.0005);

        assertEquals(-( p1*com.aliasi.util.Math.log2(p1)
                        + p2*com.aliasi.util.Math.log2(p2)
                        + p3*com.aliasi.util.Math.log2(p3) ),
                     dist.entropy(),
                     0.0005);

    }

    @Test
    public void testDistro2() {
        ZipfDistribution distro = new ZipfDistribution(100);
        double sum = 0.0;
        for (int i = 0; i < 100; ++i)
            sum += distro.probability(i);
        assertEquals(1.0,sum,0.005);
    
        assertEquals(2.0,distro.probability(1)/distro.probability(2),0.001);
    }

}

