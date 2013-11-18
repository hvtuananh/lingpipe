package com.aliasi.test.unit.stats;

import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.stats.MultivariateEstimator;
import com.aliasi.stats.MultivariateConstant;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.*;

public class MultivariateEstimatorTest  {

    @Test
    public void testDecrement() {
    MultivariateEstimator me = new MultivariateEstimator();
    me.train("a",2);
    me.train("b",3);
    me.train("c",2);
    me.train("c",2);
    assertEquals(4,me.getCount("c"));
    assertEquals(4.0/9.0,me.probability(me.outcome("c")),
             0.001);

    me.resetCount("c");
    assertEquals(0,me.getCount("c"));
    assertEquals(3.0/5.0,
             me.probability(me.outcome("b")),
             0.0001);
    }

    @Test
    public void testOne() throws ClassNotFoundException, IOException {
    MultivariateEstimator me = new MultivariateEstimator();
    for (int i = 0; i < 10; ++i)
        me.train(Integer.toString(i),1);
    assertDistro(me);

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
    me.compileTo(objOut);
    byte[] bytes = bytesOut.toByteArray();
    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
    ObjectInputStream objIn = new ObjectInputStream(bytesIn);
    MultivariateConstant mvc
        = (MultivariateConstant) objIn.readObject();
    assertDistro(mvc);
    }

    public void assertDistro(MultivariateDistribution distro) {
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
