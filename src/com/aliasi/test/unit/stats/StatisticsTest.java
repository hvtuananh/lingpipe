package com.aliasi.test.unit.stats;

import com.aliasi.stats.Statistics;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;


import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class StatisticsTest  {

    @Test
    public void testDirDivergence() {
	Random random = new Random();
	for (int k = 0; k < 100; ++k) {
	    double[] xs = new double[random.nextInt(100)+2];
	    double[] ys = new double[xs.length];
	    for (int i = 0; i < xs.length; ++i)
		xs[i] = random.nextDouble() * 10.0;
	    for (int i = 0; i < xs.length; ++i)
		ys[i] = random.nextDouble() * 10.0;
	    assertEquals(0.0,Statistics.klDivergenceDirichlet(xs,xs), 0.0001);
	    assertTrue(0.0 <= Statistics.klDivergenceDirichlet(xs,ys));
	}
    }

    @Test
    public void testDivergenceExceptions() {
	double[] p = new double[] { 0.5, 0.2, 0.3 };
	double[] q = new double[] { 0.2, 0.8 };
	double[] r = new double[] { 0.1, -0.2, 0.7 };
	double[] s = new double[] { 0.1, 1.2, 0.7 };
	double[] t = new double[] { 0.1, Double.POSITIVE_INFINITY, 0.7 };
	double[] u = new double[] { Double.NEGATIVE_INFINITY, 0.7, 0.3 };
	double[] v = new double[] { 0.1, 0.2, Double.NaN };
	assertFailDivergence(p,q);
	assertFailDivergence(p,r);
	assertFailDivergence(p,s);
	assertFailDivergence(p,t);
	assertFailDivergence(p,u);
	assertFailDivergence(p,v);
    }

    void assertFailDivergence(double[] p, double[] q) {
	try {
	    Statistics.klDivergence(p,q);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
	try {
	    Statistics.klDivergence(q,p);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}

	try {
	    Statistics.symmetrizedKlDivergence(p,q);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
	try {
	    Statistics.symmetrizedKlDivergence(q,p);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}

	try {
	    Statistics.jsDivergence(p,q);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
	try {
	    Statistics.jsDivergence(q,p);
	    fail();
	} catch (IllegalArgumentException e) {
	    succeed();
	}
    }

    @Test
    public void testEqualDivergences() {
	double[] p = new double[] { 0.1, 0.4, 0.5 };
	double[] q = new double[] { 0.1, 0.4, 0.5 };
	assertEquals(0.0,Statistics.klDivergence(p,q),0.0001);
	assertEquals(0.0,Statistics.symmetrizedKlDivergence(p,q),0.0001);
	assertEquals(0.0,Statistics.jsDivergence(p,q),0.0001);

	double[] r = new double[0];
	assertEquals(0.0,Statistics.klDivergence(r,r),0.0001);
	assertEquals(0.0,Statistics.symmetrizedKlDivergence(r,r),0.0001);
	assertEquals(0.0,Statistics.jsDivergence(r,r),0.0001);

	double[] s = new double[] { 1.0 };
	assertEquals(0.0,Statistics.klDivergence(s,s),0.0001);
	assertEquals(0.0,Statistics.symmetrizedKlDivergence(s,s),0.0001);
	assertEquals(0.0,Statistics.jsDivergence(s,s),0.0001);
    }

    @Test
    public void testDivergences() {
	double[] p = new double[] { 0.4, 0.6 };
	double[] q = new double[] { 0.6, 0.4 };
	double expectedKl = 0.4 * com.aliasi.util.Math.log2(0.4/0.6)
	    + 0.6 * com.aliasi.util.Math.log2(0.6/0.4);
	assertEquals(expectedKl,Statistics.klDivergence(p,q),0.0001);
	
	double expectedSkl = expectedKl;
	assertEquals(expectedSkl,Statistics.symmetrizedKlDivergence(p,q),0.0001);
	assertEquals(expectedSkl,Statistics.symmetrizedKlDivergence(q,p),0.0001);

	double expectedJs = 0.4 * com.aliasi.util.Math.log2(0.4/0.5)
	    + 0.6 * com.aliasi.util.Math.log2(0.6/0.5);
	assertEquals(expectedJs,Statistics.jsDivergence(p,q),0.0001);
	assertEquals(expectedJs,Statistics.jsDivergence(q,p),0.0001);
    }


    @Test
    public void testPermutation() {
        int[] xs = Statistics.permutation(0);
        assertEquals(0,xs.length);

        xs = Statistics.permutation(1);
        assertEquals(1,xs.length);
        assertEquals(0,xs[0]);

        xs = Statistics.permutation(2);
        assertEquals(2,xs.length);
        assertTrue(xs[0] == 0 && xs[1] == 1
                   || xs[0] == 1 && xs[1] == 0);

        xs = Statistics.permutation(100);
        Set<Integer> xSet = new HashSet<Integer>(200);
        for (int i = 0; i < xs.length; ++i) {
            assertTrue(0 <= xs[i] && xs[i] < 100);
            xSet.add(Integer.valueOf(xs[i]));
        }
        assertEquals(100,xSet.size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions1() {
        double[] xs = { 1 };
        double[] ys = { 2 };
        Statistics.linearRegression(xs,ys);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions2() {
        double[] xs = { 1 };
        double[] ys = { 2 };
        Statistics.logisticRegression(xs,ys,2.0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions3() {
        double[] xs2 = { 1, 2 };
        double[] ys3 = { 1, 2, 3 };
        Statistics.linearRegression(xs2,ys3);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions4() {
        double[] xs2 = { 1, 2 };
        double[] ys3 = { 1, 2, 3 };
        Statistics.logisticRegression(xs2,ys3,1.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions5() {
        double[] xs4 = { 1, 1, 1, 1 };
        double[] ys4 = { 2, 2, 2, 2 };
        Statistics.linearRegression(xs4,ys4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions6() {
        double[] xs4 = { 1, 1, 1, 1 };
        double[] ys4 = { 2, 2, 2, 2 };
        Statistics.logisticRegression(xs4,ys4,2.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRegressionExceptions7() {
        double[] xs3 = { 5, 9, 7 };
        double[] ys3 = { 1, 2, 3 };
        Statistics.logisticRegression(xs3,ys3,-1);
    }

    // Case Study 11.25, p. 577 Larsen & Marx. 2001. An Introduction
    // to Mathematical Statistics and Its Applications (3rd
    // Ed). Prentice-Hall.
    //
    @Test
    public void testLogisticRegression() {
        double[] xs = { 480, 690, 900, 1100, 1320, 1530 };
        double[] ys = { 0.3, 4.6, 15.6, 33.4, 44.4, 45.7 };
        double[] betas = Statistics.logisticRegression(xs,ys,48);
        assertEquals(7.91, betas[0], 0.1);
        assertEquals(-0.0076, betas[1], .0001);
    }

    @Test
    public void testRegression() {
        double[] xs = { 1, 2 };
        double[] ys = { 3, 4 };
        double[] betas = Statistics.linearRegression(xs,ys);
        assertEquals(betas[0],2.0,0.001);
        assertEquals(betas[1],1.0,0.001);

    }

    // from Larsen & Marx, case study 11.2.1, p. 561
    @Test
    public void testRegression2() {
        double[] xs = { 2.745, 2.700, 2.690, 2.680, 2.675, 2.670,
                        2.665, 2.660, 2.655, 2.655, 2.650, 2.650,
                        2.645, 2.635, 2.630, 2.625, 2.625, 2.620,
                        2.615, 2.615, 2.615, 2.610, 2.590, 2.590,
                        2.565 };
        double[] ys = { 2.080, 2.045, 2.050, 2.005, 2.035, 2.035,
                        2.020, 2.005, 2.010, 2.000, 2.000, 2.005,
                        2.015, 1.990, 1.990, 1.995, 1.985, 1.970,
                        1.985, 1.990, 1.995, 1.990, 1.975, 1.995,
                        1.955 };
        double[] betas = Statistics.linearRegression(xs,ys);
        assertEquals(betas[0],0.308,0.01);
        assertEquals(betas[1],0.642,0.01);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCorrelationExc() {
        Statistics.correlation(new double[] { 1, 2 }, new double[] { 3, 4, 5 });
    }

    @Test
    public void testCorrelation() {
        double[] xs = new double[] { 61, 61, 62.5, 63, 66, 70, 73, 75.5 };
        double[] ys = new double[] { 61, 63, 65, 63, 67, 72, 74, 75.5 };
        assertEquals(0.983798,Statistics.correlation(xs,ys),0.0001);

        double[] zs1 = { 1, 2, 3 };
        double[] zs2 = { 2, 4, 6 };
        double[] zs3 = { -1, -2, -3};
        assertEquals(1.0,Statistics.correlation(zs1,zs2),0.0001);
        assertEquals(1.0,Statistics.correlation(zs1,zs3),0.0001);
        assertEquals(1.0,Statistics.correlation(zs2,zs3),0.0001);
    }

    @Test
    public void testChiSquareMatrix() {
        // Larsen & Marx. p. 551
        double[][] matrix
            = new double[][]
            { { 70, 65 },
              { 39, 28 },
              { 14,  3 },
              { 13,  2 } };
        assertEquals(11.3,
                     Statistics.chiSquaredIndependence(matrix),
                     0.1);  // textbook rounding

        // Larsen & Marx. p. 552
        matrix
            = new double[][]
            { { 24, 8, 13 },
              { 8, 13, 11 },
              { 10, 9, 64 } }; // Siskel & Ebert ratings down/sideways/up
        assertEquals(45.37,
                     Statistics.chiSquaredIndependence(matrix),
                     0.1); // textbook rounding
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSqExc1() {
        double[][] matrix = new double[][] { { 1, 2}, {3, 5, 6} };
        Statistics.chiSquaredIndependence(matrix);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSqExc2() {
        double[][] matrix = new double[][] { { 1, -2, 3}, {4, 5, 6} };
        Statistics.chiSquaredIndependence(matrix);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSqExc3() {
        double[][] matrix = new double[][] { { 1, 2, 3}, {4, Double.NaN, 6} };
        Statistics.chiSquaredIndependence(matrix);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSqExc4() {
        double[][] matrix = new double[][] { { 1, 2, 3}, {4, 5, Double.POSITIVE_INFINITY} };
        Statistics.chiSquaredIndependence(matrix);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNormalizeExc1() {
        Statistics.normalize(new double[] { -1 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNormalizeExc2() {
        Statistics.normalize(new double[] { 0, 2, -1, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNormalizeExc3() {
        Statistics.normalize(new double[] { 0, 2, Double.NaN, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNormalizeExc4() {
        Statistics.normalize(new double[] { 0, 2, Double.POSITIVE_INFINITY, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNormalizeExc5() {
        Statistics.normalize(new double[] { 0, 0, 0 });
    }


    @Test(expected=IllegalArgumentException.class)
    public void testNormalizeExc6() {
        Statistics.normalize(new double[] { });
    }

    @Test
    public void testNormalizeOK() {
        assertEquals(1.0,
                     com.aliasi.util.Math.sum(Statistics.normalize(new double[] { 0, 1, 2, 3 })),
                     0.0001);
        assertEquals(1.0,
                     Statistics.normalize(new double[] { 17 })[0],
                     0.0001);
    }

    @Test
    public void testChiSquare() {
        double both = 3;
        double oneOnly = 1;
        double twoOnly = 2;
        double neither = 4; // total = 10
        double pOne = .40;
        double pTwo = .50;

        double eBoth = 10.0 * pOne * pTwo;
        double eOne = 10.0 * pOne * (1.0 - pTwo);
        double eTwo = 10.0 * (1.0 - pOne) * pTwo;
        double eNeither = 10.0 * (1.0 - pOne) * (1.0 - pTwo);

        double diffBoth = both - eBoth;
        double diffOne = oneOnly - eOne;
        double diffTwo = twoOnly - eTwo;
        double diffNeither = neither - eNeither;

        double eChiSquare
            = diffBoth * diffBoth / eBoth
            + diffOne * diffOne / eOne
            + diffTwo * diffTwo / eTwo
            + diffNeither * diffNeither / eNeither;

        assertEquals(eChiSquare,
                     Statistics
                     .chiSquaredIndependence(both,oneOnly,twoOnly,neither),
                     0.0005);

        // answer derived from http://math.hws.edu/javamath/ryan/ChiSquare.html
        assertEquals(1.66666,
                     Statistics
                     .chiSquaredIndependence(both,oneOnly,twoOnly,neither),
                     0.0005);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSquareExcs1() {
        Statistics.chiSquaredIndependence(-1,2,3,4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSquareExcs2() {
        Statistics.chiSquaredIndependence(1,-2,3,4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSquareExcs3() {
            Statistics.chiSquaredIndependence(1,2,-3,4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testChiSquareExcs4() {
        Statistics.chiSquaredIndependence(1,2,3,-4);
    }


    @Test
    public void testMeanVarDev() {
        double[] xs = new double[0];
        assertTrue(Double.isNaN(Statistics.mean(xs)));
        assertTrue(Double.isNaN(Statistics.variance(xs)));
        assertTrue(Double.isNaN(Statistics.standardDeviation(xs)));

        xs = new double[] { 1.0 };
        assertEquals(1.0, Statistics.mean(xs), 0.0001);
        assertEquals(0.0, Statistics.variance(xs), 0.0001);
        assertEquals(0.0, Statistics.standardDeviation(xs), 0.0001);

        xs = new double[] { 1.0, 3.0 };
        assertEquals(2.0, Statistics.mean(xs), 0.0001);
        assertEquals(1.0, Statistics.variance(xs), 0.0001);
        assertEquals(1.0, Statistics.standardDeviation(xs), 0.0001);

        xs = new double[] { 1.0, 3.0, 1.0, 3.0 };
        assertEquals(2.0, Statistics.mean(xs), 0.0001);
        assertEquals(1.0, Statistics.variance(xs), 0.0001);
        assertEquals(1.0, Statistics.standardDeviation(xs), 0.0001);
    }

    @Test
    public void testSampling() {
        Random baseRandom = new Random();

        for (int k = 0; k < 100; ++k) {
            int numTopics = baseRandom.nextInt(300) + 1;
            double[] probRatios = new double[numTopics];
            for (int i = 0; i < numTopics; ++i) {
                probRatios[i] = (i == 0) ? 0.0 : probRatios[i-1];
                if (baseRandom.nextDouble() > 0.1)
                    probRatios[i] += baseRandom.nextDouble() * 100.0;
            }

            for (int j = 0; j < 100; ++j) {
                long seed = baseRandom.nextLong();
                double x = new Random(seed).nextDouble() * probRatios[numTopics-1];
                int sample = Statistics.sample(probRatios,new Random(seed));
                assertTrue(x <= probRatios[sample]);
                assertTrue(sample == 0
                           || x > probRatios[sample-1]);
            }
        }


    }

    @Test
    public void testDirichlet() {
        assertDirichlet(2,new double[] { 0.5, 0.5 }, 1.5);
        assertDirichlet(new double[] { 2.0, 2.0 },
                        new double[] { 0.5, 0.5 },
                        1.5);
        double log2Expected = com.aliasi.util.Math.log2Gamma(2 + 2)
            - 2 * com.aliasi.util.Math.log2Gamma(2)
            + com.aliasi.util.Math.log2(0.25)
            + com.aliasi.util.Math.log2(0.75);
        assertDirichlet(2,
                        new double[] { 0.25, 0.75 },
                        java.lang.Math.pow(2.0,log2Expected));


        double log2Expected2 = com.aliasi.util.Math.log2Gamma(3 + 4 + 5)
            - com.aliasi.util.Math.log2Gamma(3)
            - com.aliasi.util.Math.log2Gamma(4)
            - com.aliasi.util.Math.log2Gamma(5)
            + com.aliasi.util.Math.log2(java.lang.Math.pow(0.2,3-1))
            + com.aliasi.util.Math.log2(java.lang.Math.pow(0.3,4-1))
            + com.aliasi.util.Math.log2(java.lang.Math.pow(0.5,5-1));

        assertDirichlet(new double[] { 3, 4, 5 },
                        new double[] { 0.2, 0.3, 0.5 },
                        java.lang.Math.pow(2.0, log2Expected2));





    }

    void assertDirichlet(double alpha, double[] xs, double expectedP) {
        double expectedLog2P = com.aliasi.util.Math.log2(expectedP);
        assertEquals(expectedLog2P,
                     Statistics.dirichletLog2Prob(alpha,xs),
                     0.0001);
    }

    void assertDirichlet(double[] alphas, double[] xs, double expectedP) {
        double expectedLog2P = com.aliasi.util.Math.log2(expectedP);
        assertEquals(expectedLog2P,
                     Statistics.dirichletLog2Prob(alphas,xs),
                     0.0001);
    }

    @Test
    public void testDirichletExceptions() {
        assertDirichletFail(-1,new double[] { 0.5, 0.5 });
        assertDirichletFail(0.0,new double[] { 0.5, 0.5 });
        assertDirichletFail(Double.NaN,new double[] { 0.5, 0.5 });
        assertDirichletFail(Double.POSITIVE_INFINITY,new double[] { 0.5, 0.5 });

        assertDirichletFail(new double[] { 0.4, -1 }, new double[] { 0.25, 0.75 });
        assertDirichletFail(new double[] { 0.4, 0 }, new double[] { 0.25, 0.75 });
        assertDirichletFail(new double[] { Double.NaN, 0.4 }, new double[] { 0.25, 0.75 });
        assertDirichletFail(new double[] { 0.4, 0.4, Double.POSITIVE_INFINITY },
                            new double[] { 0.25, 0.5, 0.5 });

        assertDirichletFail(1, new double[] { -1, 0.5 });
        assertDirichletFail(new double[] { 1, 1 }, new double[] { -1, 0.5 });

        assertDirichletFail(1, new double[] { 0.25, 2 });
        assertDirichletFail(new double[] { 1, 1 }, new double[] { 0.5, 2 });

        assertDirichletFail(1, new double[] { 0.25, Double.NEGATIVE_INFINITY });
        assertDirichletFail(new double[] { 1, 1 }, new double[] { 0.5, Double.POSITIVE_INFINITY });
        assertDirichletFail(new double[] { 1, 1 }, new double[] { 0.5, Double.NaN });


        assertDirichletFail(new double[] { 1, 2, 3 }, new double[] { 0.5, 0.5 });
    }

    void assertDirichletFail(double alpha, double[] xs) {
        try {
            Statistics.dirichletLog2Prob(-1,new double[] { 0.5, 0.5 });
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    void assertDirichletFail(double[] alphas, double[] xs) {
        try {
            Statistics.dirichletLog2Prob(alphas,xs);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }

}
