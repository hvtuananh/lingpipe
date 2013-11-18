package com.aliasi.test.unit.stats;

import com.aliasi.stats.OnlineNormalEstimator;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;

import java.util.Random;

public class OnlineNormalEstimatorTest {

    public void testBadUnHandle1() {
        OnlineNormalEstimator estimator = new OnlineNormalEstimator();
        estimator.unHandle(2.0);
    }

    @Test(expected = IllegalStateException.class)
    public void testBadUnhandle2() {
        OnlineNormalEstimator estimator = new OnlineNormalEstimator();
        estimator.handle(2.0);
        estimator.unHandle(2.0);
        estimator.unHandle(2.0);
    }

    public void testUnhandle() {
        OnlineNormalEstimator estimator = new OnlineNormalEstimator();
        estimator.handle(1.0);  // 1
        assertEquals(1,estimator.numSamples());

        estimator.unHandle(1.0); //
        assertEquals(0,estimator.numSamples());

        estimator.handle(2.0); // 2
        assertEquals(1,estimator.numSamples());
        assertEquals(2.0,estimator.mean(),0.0001);
        assertEquals(0.0,estimator.variance(),0.0001);

        estimator.handle(1.0); // 2, 1
        assertEquals(2,estimator.numSamples());
        assertEquals(1.5,estimator.mean(),0.0001);
        assertEquals(0.25,estimator.variance(),0.0001);

        estimator.unHandle(2.0); // 1
        assertEquals(1,estimator.numSamples());
        assertEquals(1.0,estimator.mean(),0.0001);
        assertEquals(0.0,estimator.variance(),0.0001);

        estimator.handle(2.0); // 1, 2
        estimator.handle(3.0); // 1, 2, 3
        estimator.unHandle(2.0); // 1, 3
        assertEquals(2,estimator.numSamples());
        assertEquals(2.0,estimator.mean(),0.0001);
        assertEquals(1.0,estimator.variance(),0.0001);
    }


    @Test
    public void testNumSamples() {
        OnlineNormalEstimator estimator = estimator(new double[] { });
        assertEquals(0,estimator.numSamples());
        estimator.handle(5.0);
        assertEquals(1,estimator.numSamples());
        estimator.handle(6.0);
        assertEquals(2,estimator.numSamples());
    }

    @Test
    public void testMean() {
        for (int i = 0; i < 10; ++i) {
            double[] xs = randomArray(42L, 500);
            assertEquals(mean(xs),estimator(xs).mean(),0.0001);
        }
    }

    @Test
    public void testVariance() {
        for (int i = 0; i < 10; ++i) {
            double[] xs = randomArray(42L, 500);
            assertEquals(variance(xs),estimator(xs).variance(),0.0001);
            assertEquals(Math.sqrt(variance(xs)),estimator(xs).standardDeviation(),0.0001);
            assertEquals(500.0/499.0 * variance(xs),
                         estimator(xs).varianceUnbiased(),0.0001);
            assertEquals(Math.sqrt(500.0/499.0 * variance(xs)),
                         estimator(xs).standardDeviationUnbiased(),0.0001);
        }
    }



    static double[] randomArray(long seed, int length) {
        Random random = new Random(seed);
        double[] xs = new double[length];
        for (int i = 0; i < xs.length; ++i)
            xs[i] = random.nextDouble();
        return xs;
    }


    static OnlineNormalEstimator estimator(double[] xs) {
        OnlineNormalEstimator est = new OnlineNormalEstimator();
        for (int i = 0; i < xs.length; ++i)
            est.handle(xs[i]);
        return est;
    }

    static double mean(double[] xs) {
        return com.aliasi.util.Math.sum(xs) / (double) xs.length;
    }

    static double variance(double[] xs) {
        return sumSquareDiffs(xs,mean(xs))/xs.length;
    }

    static double sumSquareDiffs(double[] xs, double mean) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i) {
            double diff = xs[i] - mean;
            sum += diff * diff;
        }
        return sum;
    }



}
