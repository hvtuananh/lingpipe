package com.aliasi.test.unit.stats;

import com.aliasi.stats.RegressionPrior;

import com.aliasi.util.AbstractExternalizable;

import static com.aliasi.test.unit.Asserts.succeed;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


import java.io.IOException;

public class RegressionPriorTest  {

    @Test
    public void testMeans() {
        RegressionPrior prior1 = RegressionPrior.gaussian(1.0,true);
        assertEquals(0.0,prior1.mode(0));
        assertEquals(0.0,prior1.mode(1));
        
        RegressionPrior prior2 
            = RegressionPrior.shiftMeans(new double[] { 1.0, 2.0, -3.0 },
                                         prior1);
        
        assertEquals(1.0,prior2.mode(0));
        assertEquals(2.0,prior2.mode(1));
        assertEquals(-3.0,prior2.mode(2));
        
        assertEquals(0.0,prior2.gradient(1.0,0),0.0001);
        assertEquals(0.0,prior2.gradient(2.0,1),0.0001);
        assertEquals(0.0,prior2.gradient(-3.0,2),0.0001);

        RegressionPrior prior3
            = RegressionPrior.shiftMeans(new double[] { 2.0, 1.0, 3.0 },
                                         prior2);
        assertEquals(3.0,prior3.mode(0));
        assertEquals(3.0,prior3.mode(1));
        assertEquals(0.0,prior3.mode(2));

        assertEquals(0.0,prior3.gradient(3.0,0),0.0001);
        assertEquals(0.0,prior3.gradient(3.0,1),0.0001);
        assertEquals(0.0,prior3.gradient(0.0,2),0.0001);
    }

    @Test
    public void testElasticNet() {
        RegressionPrior prior
            = RegressionPrior.elasticNet(0.3,2.0,true);

        RegressionPrior laplacePrior = RegressionPrior.laplace(1.0/Math.sqrt(2.0),true);
        RegressionPrior gaussianPrior = RegressionPrior.gaussian(Math.sqrt(2)/2.0,true);
        for (int i = -5; i < 5; ++i) {
            assertEquals(0.3 * laplacePrior.log2Prior(i,2)
                         + 0.7 * gaussianPrior.log2Prior(i,2),
                         prior.log2Prior(i,2),
                         0.0001);
            assertEquals(0.3 * laplacePrior.log2Prior(i,0)
                         + 0.7 * gaussianPrior.log2Prior(i,0),
                         prior.log2Prior(i,0),
                         0.0001);

            assertEquals(0.3 * laplacePrior.gradient(i,1)
                         + 0.7 * gaussianPrior.gradient(i,1),
                         prior.gradient(i,1),
                         0.0001);

            assertEquals(0.3 * laplacePrior.gradient(i,0)
                         + 0.7 * gaussianPrior.gradient(i,0),
                         prior.gradient(i,0),
                         0.0001);


        }
        RegressionPrior priorNonInt
            = RegressionPrior.elasticNet(0.3,2.0,false);
        for (int i = -5; i < 5; ++i) {
            assertEquals(0.3 * laplacePrior.log2Prior(i,2)
                         + 0.7 * gaussianPrior.log2Prior(i,2),
                         prior.log2Prior(i,2));
            assertEquals(0.0, prior.log2Prior(i,0), 0.0001);

            assertEquals(0.3 * laplacePrior.gradient(i,1)
                         + 0.7 * gaussianPrior.gradient(i,1),
                         prior.gradient(i,1),
                         0.0001);
            assertEquals(0.0, prior.gradient(5.0,0), 0.0001);
        }
        
    }

    @Test
    public void testMeanOffsets() {
        RegressionPrior basePrior = RegressionPrior.gaussian(1.0,false);
        RegressionPrior prior
            = RegressionPrior.shiftMeans(new double[] { 1.0, -2.0, 3.0 },
                                         basePrior);
        assertEquals(basePrior.log2Prior(0.0,0),
                     prior.log2Prior(1.0,0));
        assertEquals(basePrior.log2Prior(1.0,0),
                     prior.log2Prior(2.0,0));
        assertEquals(basePrior.log2Prior(-1.0,0),
                     prior.log2Prior(0.0,0));

        assertEquals(basePrior.gradient(0.0,0),
                     prior.gradient(1.0,0));
        assertEquals(basePrior.gradient(1.0,0),
                     prior.gradient(2.0,0));
        assertEquals(basePrior.gradient(-2.0,0),
                     prior.gradient(-1.0,0));

        assertEquals(basePrior.log2Prior(3.0,1),
                     prior.log2Prior(1.0,1));

        assertEquals(basePrior.log2Prior(7.0,2),
                     prior.log2Prior(10.0,2));

        assertEquals(basePrior.gradient(7.0,2),
                     prior.gradient(10.0,2));
    }


    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx1() {
        RegressionPrior.elasticNet(-1.0,2.0,true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx2() {
        RegressionPrior.elasticNet(Double.NaN,2.0,true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx3() {
        RegressionPrior.elasticNet(Double.POSITIVE_INFINITY,2.0,true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx4() {
        RegressionPrior.elasticNet(0.5,-1,true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx5() {
        RegressionPrior.elasticNet(0.5,Double.NaN,true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx6() {
        RegressionPrior.elasticNet(0.5,Double.POSITIVE_INFINITY,true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testElasticNetEx7() {
        RegressionPrior.elasticNet(0.5,0.0,true);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        double[] priorVariances = new double[] { 1.0, 2.0, 3.0 };
        double priorVariance = 1.0;

        assertSerialization(RegressionPrior.shiftMeans(new double[] { 1.0, -2.0, 3.0 },
                                                       RegressionPrior.gaussian(priorVariance,false)),
                            3);

        // doesn't have dimensionality built in, so only -1 test
        assertSerialization(RegressionPrior.elasticNet(0.95,2.0,false),-1);

        assertSerialization(RegressionPrior.cauchy(priorVariances),3);
        assertSerialization(RegressionPrior.cauchy(priorVariance,true),-1);
        assertSerialization(RegressionPrior.cauchy(priorVariance,false),-1);

        assertSerialization(RegressionPrior.gaussian(priorVariances),3);
        assertSerialization(RegressionPrior.gaussian(priorVariance,true),-1);
        assertSerialization(RegressionPrior.gaussian(priorVariance,false),-1);

        assertSerialization(RegressionPrior.laplace(priorVariances),3);
        assertSerialization(RegressionPrior.laplace(priorVariance,true),-1);
        assertSerialization(RegressionPrior.laplace(priorVariance,false),-1);

        assertSerialization(RegressionPrior.noninformative(),-1);

    }

    // dimensionality == -1 to not test
    void assertSerialization(RegressionPrior prior, int dimensionality)
        throws IOException, ClassNotFoundException {

        RegressionPrior prior2 = (RegressionPrior) AbstractExternalizable.serializeDeserialize(prior);
        for (int i = 0; i < dimensionality || dimensionality == -1 && i < 10; ++i) {
            assertEquals(prior.log2Prior(2.0,i),
                         prior2.log2Prior(2.0,i),
                         0.00001);
            assertEquals(prior.log2Prior(-1.0,i),
                         prior2.log2Prior(-1.0,i),
                         0.00001);
            assertEquals(prior.gradient(5.0,i),
                         prior2.gradient(5.0,i),
                         0.00001);
            assertEquals(prior.gradient(-2.0,i),
                         prior2.gradient(-2.0,i),
                         0.00001);
        }
        if (dimensionality > 0) {
            try {
                prior.gradient(2.0,dimensionality+1);
                fail();
            } catch (ArrayIndexOutOfBoundsException e) {
                succeed();
            }
        }
    }
}
