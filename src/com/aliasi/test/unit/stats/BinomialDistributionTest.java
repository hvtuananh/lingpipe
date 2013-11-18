package com.aliasi.test.unit.stats;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;


import com.aliasi.stats.BernoulliConstant;
import com.aliasi.stats.BinomialDistribution;

public class BinomialDistributionTest  {

    @Test
    public void testProbability() {
        BernoulliConstant bernoulli = new BernoulliConstant(0.4);
        BinomialDistribution distro
            = new BinomialDistribution(bernoulli,15);

        double log2Success = com.aliasi.util.Math.log2(0.4);
        double log2Fail = com.aliasi.util.Math.log2(0.6);

        assertEquals(BinomialDistribution.log2BinomialCoefficient(15,3)
                     + 3.0 * log2Success
                     + 12.0 * log2Fail,
                     distro.log2Probability(3),
                     0.0001);

        double sum = 0.0;
        for (long i = 0; i <= 15; ++i)
            sum += distro.probability(i);
        assertEquals(1.0,sum,0.001);

    }

    @Test
    public void testZandP() {
        double expectedZ4 = (4.0 - 0.3 * 11.0)
            / Math.sqrt(11.0 * 0.3 * 0.7);

        assertEquals(expectedZ4,BinomialDistribution.z(0.3,4,11),0.0001);

        BernoulliConstant bernoulli = new BernoulliConstant(0.3);
        BinomialDistribution distro
            = new BinomialDistribution(bernoulli,11);
        assertEquals(expectedZ4,distro.z(4),0.0001);

        /*
        // expectedZ4 = 0.460566
        // pValue(.460566) in (.1772,.1808)
        double pValue = distro.pValue(4);

        // table gives one-sided between 0.1772 and 0.1808
        assertTrue(1.0 - 2.0*0.1772 > pValue);
        assertTrue(pValue > 1.0 - 2.0 * 0.1808);

        double pValue2 = BinomialDistribution.pValue(0.3,4,11);
        assertEquals(pValue,pValue2,0.0001);
        */


    }

    @Test
    public void testExceptions() {
        BernoulliConstant bernoulli = new BernoulliConstant(0.4);
        try {
            new BinomialDistribution(bernoulli,-1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            BinomialDistribution.z(-2.0,3,10);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            BinomialDistribution.z(Double.POSITIVE_INFINITY,3,11);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            BinomialDistribution.z(2.0,5,10);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            BinomialDistribution.z(0.5,-1,10);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            BinomialDistribution.z(0.5,2,1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }


    @Test
    public void testCoeff() {
        assertEquals(com.aliasi.util.Math.log2Factorial(5)
                     - com.aliasi.util.Math.log2Factorial(3)
                     - com.aliasi.util.Math.log2Factorial(2),
                     BinomialDistribution.log2BinomialCoefficient(5,2),
                     0.0001);
        assertEquals(0.0,
                     BinomialDistribution.log2BinomialCoefficient(3,3),
                     0.0001);
        assertEquals(0.0,
                     BinomialDistribution.log2BinomialCoefficient(3,0),
                     0.0001);
        assertEquals(0.0,
                     BinomialDistribution.log2BinomialCoefficient(0,0),
                     0.0001);

        try {
            BinomialDistribution.log2BinomialCoefficient(3,4);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }

}
