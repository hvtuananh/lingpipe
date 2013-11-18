package com.aliasi.test.unit.stats;

import com.aliasi.stats.AbstractDiscreteDistribution;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class AbstractDiscreteDistributionTest  {

    @Test
    public void testOne() {
    Distro distro = new Distro();

    assertEquals(1l,distro.minOutcome());
    assertEquals(10l,distro.maxOutcome());

    assertEquals(.50,distro.cumulativeProbabilityLess(5l),0.001);
    assertEquals(.00,distro.cumulativeProbabilityLess(0l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityLess(10l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityLess(20l),0.001);

    assertEquals(.50,distro.cumulativeProbabilityGreater(6l),0.001);
    assertEquals(.00,distro.cumulativeProbabilityGreater(11l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityGreater(0l),0.001);
    assertEquals(1.00,distro.cumulativeProbabilityGreater(-20l),0.001);

    assertEquals(.50,distro.cumulativeProbability(1l,5l),0.001);
    assertEquals(.50,distro.cumulativeProbability(-3l,5l),0.001);
    assertEquals(.50,distro.cumulativeProbability(-3l,5l),0.001);
    assertEquals(.00,distro.cumulativeProbability(-3l,-4l),0.001);
    assertEquals(1.00,distro.cumulativeProbability(-3l,15l),0.001);
    assertEquals(1.00,distro.cumulativeProbability(1l,10l),0.001);

    assertEquals(.10,distro.probability(1l),0.0001);
    assertEquals(.10,distro.probability(5l),0.0001);
    assertEquals(.10,distro.probability(10l),0.0001);
    assertEquals(.00,distro.probability(17l),0.0001);

    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(1l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(5l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.10),
             distro.log2Probability(10l),0.0001);
    assertEquals(com.aliasi.util.Math.log2(.00),
             distro.log2Probability(17l),0.0001);

    double mean = (10.0*11.0/2.0)/10.0;
    double variance = 0.0;
    for (int i = 1; i <= 10; ++i) {
        double diff = mean - (double)i;
        variance += diff*diff;
    }
    variance /= 10.0;
    assertEquals(mean,distro.mean(),0.0001);
    assertEquals(variance,distro.variance(),0.0001);

    double entropy = 0.0;
    for (int i = 1; i <= 10; ++i)
        entropy += -distro.probability(i) * distro.log2Probability(i);
    assertEquals(entropy,distro.entropy(),0.0001);
    }

    public static class Distro extends AbstractDiscreteDistribution {
    @Override
    public double probability(long outcome) {
        if (outcome < 1l || outcome > 10l) return 0.0;
        return 1.0/10.0;
    }
    @Override
    public long minOutcome() {
        return 1l;
    }
    @Override
    public long maxOutcome() {
        return 10l;
    }
    }

}

