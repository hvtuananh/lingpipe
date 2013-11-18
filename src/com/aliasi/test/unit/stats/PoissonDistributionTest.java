package com.aliasi.test.unit.stats;

import com.aliasi.stats.PoissonDistribution;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


public class PoissonDistributionTest  {
    
    @Test
    public void testOne() {
        PoissonDistribution dist = new PoissonTest(2.0);
    assertPoissonTwo(dist);
    }

    @Test
    public void testExceptions() {
        PoissonDistribution dist = new PoissonTest(2.0);
    assertTrue(Double.NEGATIVE_INFINITY == dist.log2Probability(-1));
    assertEquals(0.0,dist.probability(-1),0.005);
    
    dist = new PoissonTest(-1.0);
    try {
        dist.log2Probability(5);
        fail();
    } catch (IllegalStateException e) {
        assertTrue(true);
    }
    
    }


    static void assertSumOne(PoissonDistribution dist) {
        double sum = 0.0;
        for (int i = 0; i < 100; ++i) {
            double logProbI = dist.log2Probability(i);
            double probI = java.lang.Math.pow(2.0,logProbI);
            sum += probI;
        }
        assertEquals(1.0,sum,0.005);
    }

    static void assertPoissonTwo(PoissonDistribution dist) {
        assertSumOne(dist);
    assertEquals(0l,dist.minOutcome());
    assertEquals(Long.MAX_VALUE,dist.maxOutcome());
    assertEquals(2.0,dist.mean(),0.0001);
    assertEquals(2.0,dist.variance(),0.0001);
    }


    static class PoissonTest extends PoissonDistribution {
        private final double mLambda;
        public PoissonTest(double lambda) {
            mLambda = lambda;
        }
        @Override
        public double mean() {
            return mLambda;
        }
    }

}
