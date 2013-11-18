package com.aliasi.test.unit.stats;

import com.aliasi.stats.MultinomialDistribution;
import com.aliasi.stats.MultivariateConstant;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class MultinomialDistributionTest  {

    @Test
    public void testBenford() {
        // exercise in Larsen & Marx, p. 536
        MultivariateConstant benfordsDistro
            = new MultivariateConstant(new double[] {
                0.301, 0.176, 0.125, 0.097, 0.079, 0.067, 0.058, 0.051, 0.046
            });
        MultinomialDistribution distro
            = new MultinomialDistribution(benfordsDistro);

        int[] samples = new int[] { 111, 60, 46, 29, 26, 22, 21, 20, 20 };


        double chiSquared = distro.chiSquared(samples);

        assertEquals(2.49,chiSquared,0.05);

        /*
        double pValue = distro.pValue(samples);

        // 0.0378
        // bounds from Larsen & Marx p. 721, chi-squared=2.49, dof=8
        assertTrue(pValue > 0.025);
        assertTrue(pValue < 0.05);
        */
    }

    @Test
    public void testOne() {
        MultivariateConstant basis
            = new MultivariateConstant(new long[] { 1, 2, 3});
        assertEquals(3,basis.numDimensions());

        MultinomialDistribution distro
            = new MultinomialDistribution(basis);
        assertEquals(3,distro.numDimensions());
        assertEquals(basis,distro.basisDistribution());

        int[] outcomes = new int[] { 3, 2, 1 };
        double log2Coeff = MultinomialDistribution.log2MultinomialCoefficient(outcomes);
        double log2Prob = log2Coeff + 3.0 * com.aliasi.util.Math.log2(1.0/6.0)
            + 2.0 * com.aliasi.util.Math.log2(2.0/6.0)
            + 1.0 * com.aliasi.util.Math.log2(3.0/6.0);
        assertEquals(log2Prob,distro.log2Probability(outcomes),0.0001);

    }

    @Test
    public void testTwo() {
        MultivariateConstant basis = new MultivariateConstant(new long[] { 0, 1, 1 });
        MultinomialDistribution distro = new MultinomialDistribution(basis);
        assertEquals(Double.NEGATIVE_INFINITY,
                     distro.log2Probability(new int[] { 1, 1, 0 }),
                     0.0001);
    }


    @Test
    public void testChiSquared() {
        MultivariateConstant basis
            = new MultivariateConstant(new long[] { 1, 2, 3});
        MultinomialDistribution distro = new MultinomialDistribution(basis);
        int[] testSample = new int[] { 3, 2, 1 };
        double expected = (3.0 - 1.0)*(3.0-1.0)/1.0 + (2.0 - 2.0)*(2.0 - 2.0)/2.0 + (1.0-3.0)*(1.0-3.0)/3.0;
        assertEquals(expected,distro.chiSquared(testSample),0.0001);
    }


    @Test
    public void testCoeff() {
        int[] counts = new int[] { 0 };
        assertEquals(0.0,
                     MultinomialDistribution
                     .log2MultinomialCoefficient(counts), 0.0001);

        counts = new int[] { 1 };
        assertEquals(0.0,
                     MultinomialDistribution
                     .log2MultinomialCoefficient(counts), 0.0001);


        counts = new int[] { 1, 1 };
        assertEquals(com.aliasi.util.Math.log2(2),
                     MultinomialDistribution
                     .log2MultinomialCoefficient(counts), 0.0001);

        counts = new int[] { 2, 0 };
        assertEquals(com.aliasi.util.Math.log2(1),
                     MultinomialDistribution
                     .log2MultinomialCoefficient(counts), 0.0001);

        counts = new int[] { 5, 5, 5 };
        assertEquals(com.aliasi.util.Math.log2Factorial(15)
                     - 3.0 * com.aliasi.util.Math.log2Factorial(5),
                     MultinomialDistribution
                     .log2MultinomialCoefficient(counts), 0.0001);

    }


}
