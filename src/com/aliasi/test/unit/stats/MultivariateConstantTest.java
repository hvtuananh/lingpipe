package com.aliasi.test.unit.stats;

import com.aliasi.stats.MultivariateConstant;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


import java.util.Arrays;

public class MultivariateConstantTest  {

    @Test
    public void testOne() {

    long[] counts = new long[10];
    Arrays.fill(counts,5l);

    String[] labels = new String[10];
    for (int i = 0; i < labels.length; ++i)
        labels[i] = Long.toString((long)i);

    double[] ratios = new double[10];
    Arrays.fill(ratios,0.72);

    assertDistro(new MultivariateConstant(10));
    assertDistro(new MultivariateConstant(counts));
    assertDistro(new MultivariateConstant(counts,labels));
    assertDistro(new MultivariateConstant(labels));
    assertDistro(new MultivariateConstant(ratios));
    assertDistro(new MultivariateConstant(ratios,labels));
    }

    @Test
    public void testExs() {
    try {
        new MultivariateConstant(-1);
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new long[] {3,-1});
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new long[0]);
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new long[] {-1, 2}, new String[] { "foo", "bar" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new long[] {-1, 2}, new String[] { "foo" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }


    try {
        new MultivariateConstant(new double[0]);
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new double[] {-1, 2}, new String[] { "foo", "bar" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new double[] {-1, 2}, new String[] { "foo" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }


    try {
        new MultivariateConstant(new String[0]);
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new String[] { "foo", "bar", "baz", "bar" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    try {
        new MultivariateConstant(new long[] { 1, 2, 3, 4 }, 
                     new String[] { "foo", "bar", "baz", "bar" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }


    try {
        new MultivariateConstant(new double[] { 1, 2, 3, 4 }, 
                     new String[] { "foo", "bar", "baz", "bar" });
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }
    }

    @Test
    public void testLabelsExplicit() {
    MultivariateConstant mvc = new MultivariateConstant(new String[] { "foo", "bar" });
    assertEquals("foo",mvc.label(0l));
    assertEquals("bar",mvc.label(1l));
    try {
        mvc.label(2l);
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }

    assertEquals(0l,mvc.outcome("foo"));
    assertEquals(1l,mvc.outcome("bar"));
    try {
        mvc.outcome("baz");
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }
    }
    
    @Test
    public void testLabelsImplicit() {
    MultivariateConstant mvc = new MultivariateConstant(4);
    assertEquals("1",mvc.label(1l));
    assertEquals("3",mvc.label(3l));
    try {
        mvc.label(4l);
        fail();
    } catch (IllegalArgumentException e) {
        assertTrue(true);
    }
    
    assertEquals(0l,mvc.outcome("0"));
    assertEquals(2l,mvc.outcome("2"));
    assertEquals(3l,mvc.outcome("3"));

    assertEquals(-1l,mvc.outcome("foo"));
    assertEquals(-1l,mvc.outcome("4"));
    }


    public void assertDistro(MultivariateConstant distro) {
    assertEquals(0l,distro.minOutcome());
    assertEquals(9l,distro.maxOutcome());
    assertEquals(10,distro.numDimensions());

    assertEquals(.50,distro.cumulativeProbabilityLess(4l),0.001);
    assertEquals(.00,distro.cumulativeProbabilityLess(-1l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityLess(9l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityLess(20l),0.001);

    assertEquals(.50,distro.cumulativeProbabilityGreater(5l),0.001);
    assertEquals(.00,distro.cumulativeProbabilityGreater(10l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityGreater(0l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityGreater(-20l),0.001);

    assertEquals(.50,distro.cumulativeProbability(1l,5l),0.001);
    assertEquals(.50,distro.cumulativeProbability(-3l,4l),0.001);
    assertEquals(.50,distro.cumulativeProbability(-3l,4l),0.001);
    assertEquals(.00,distro.cumulativeProbability(-3l,-4l),0.001);
    assertEquals(1.00,distro.cumulativeProbability(-3l,15l),0.001);
    assertEquals(1.00,distro.cumulativeProbability(0l,9l),0.001);

    assertEquals(.10,distro.probability(0l),0.0001);
    assertEquals(.10,distro.probability(5l),0.0001);
    assertEquals(.10,distro.probability(9l),0.0001);
    assertEquals(.00,distro.probability(17l),0.0001);

    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(0l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(5l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(9l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.00),
             distro.log2Probability(17l),0.0001);

    double mean = (10.0*9.0/2.0)/10.0;
    double variance = 0.0;
    for (int i = 0; i < 10; ++i) {
        double diff = mean - (double)i;
        variance += diff*diff;
    }
    variance /= 10.0;
    assertEquals(mean,distro.mean(),0.0001);
    assertEquals(variance,distro.variance(),0.0001);

    double entropy = 0.0;
    for (int i = 0; i <= 9; ++i)
        entropy += -distro.probability(i) * distro.log2Probability(i);
    assertEquals(entropy,distro.entropy(),0.0001);
    }

}

