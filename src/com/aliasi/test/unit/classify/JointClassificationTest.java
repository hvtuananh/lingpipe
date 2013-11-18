package com.aliasi.test.unit.classify;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import com.aliasi.classify.JointClassification;


public class JointClassificationTest  {

    @Test
    public void testOne() {
	String[] categories = new String[] { "a", "b", "c", "d", "e" };
	double[] lps1 = new double[] { -100, -101, -150, -200, -1000 };
	JointClassification jc = new JointClassification(categories,lps1);
	assertEquals(-100,jc.jointLog2Probability(0),0.001);
	assertEquals(-101,jc.jointLog2Probability(1),0.001);
	assertEquals(-150,jc.jointLog2Probability(2),0.001);
	assertEquals(-200,jc.jointLog2Probability(3),0.001);
	assertEquals(-1000,jc.jointLog2Probability(4),0.001);
	assertEquals(2.0/3.0,jc.conditionalProbability(0),0.001);
	assertEquals(1.0/3.0,jc.conditionalProbability(1),0.001);
	assertEquals(0.0,jc.conditionalProbability(2),0.001);
	assertEquals(-100,jc.score(0),0.001);
	assertEquals(-101,jc.score(1),0.001);
	assertEquals(-150,jc.score(2),0.001);
	assertEquals(-200,jc.score(3),0.001);
	assertEquals(-1000,jc.score(4),0.001);

	double[] lps2 = new double[] { -100, -200, -300, -400, -500 };
	JointClassification jc2 = new JointClassification(categories,lps2);
	assertEquals(-100,jc2.jointLog2Probability(0),0.001);
	assertEquals(-200,jc2.jointLog2Probability(1),0.001);
	assertEquals(1.0,jc2.conditionalProbability(0),0.001);
	assertEquals(0.0,jc2.conditionalProbability(1),0.001);
	assertEquals(-100,jc2.score(0),0.001);
	assertEquals(-200,jc2.score(1),0.001);


	double[] scores = new double[] { 10, 9, 8, 7, 6 };
	JointClassification jc3 = new JointClassification(categories,scores,lps1);
	assertEquals(-100,jc3.jointLog2Probability(0),0.001);
	assertEquals(-101,jc3.jointLog2Probability(1),0.001);
	assertEquals(-150,jc3.jointLog2Probability(2),0.001);
	assertEquals(-200,jc3.jointLog2Probability(3),0.001);
	assertEquals(-1000,jc3.jointLog2Probability(4),0.001);
	assertEquals(2.0/3.0,jc3.conditionalProbability(0),0.001);
	assertEquals(1.0/3.0,jc3.conditionalProbability(1),0.001);
	assertEquals(0.0,jc3.conditionalProbability(2),0.001);
	assertEquals(10.0,jc3.score(0),0.001);
	assertEquals(9.0,jc3.score(1),0.001);
	assertEquals(8.0,jc3.score(2),0.001);
	assertEquals(7.0,jc3.score(3),0.001);
	assertEquals(6.0,jc3.score(4),0.001);

	JointClassification jc4 = new JointClassification(categories,scores,lps2);
	assertEquals(-100,jc4.jointLog2Probability(0),0.001);
	assertEquals(-200,jc4.jointLog2Probability(1),0.001);
	assertEquals(1.0,jc4.conditionalProbability(0),0.001);
	assertEquals(0.0,jc4.conditionalProbability(1),0.001);
	assertEquals(10.0,jc4.score(0),0.001);
	assertEquals(9.0,jc4.score(1),0.001);
	assertEquals(8.0,jc4.score(2),0.001);
	assertEquals(7.0,jc4.score(3),0.001);
	assertEquals(6.0,jc4.score(4),0.001);

    }

    @Test
    public void testTwo() {
	String[] cats = new String[] { "a", "b", "c", "d", "e" };
	double[] scores = new double[] { -2.34, -2.54, -2.857, -4.152, -16 };
	double[] joints = new double[] { -2683, -2915, -3274, -4759, -18312 };

	JointClassification jc = new JointClassification(cats,scores,joints);
	assertEquals(-2.34,jc.score(0),0.001);
	assertEquals(-2.54,jc.score(1),0.001);
	assertEquals(-2.857,jc.score(2),0.001);
	assertEquals(-4.152,jc.score(3),0.001);
	assertEquals(-16.0,jc.score(4),0.001);
	assertEquals(-2683.0,jc.jointLog2Probability(0),0.001);
	assertEquals(-2915.0,jc.jointLog2Probability(1),0.001);
	assertEquals(-3274.0,jc.jointLog2Probability(2),0.001);
	assertEquals(-4759.0,jc.jointLog2Probability(3),0.001);
	assertEquals(-18312.0,jc.jointLog2Probability(4),0.001);
	assertEquals(1.0,jc.conditionalProbability(0),0.001);
	assertEquals(0.0,jc.conditionalProbability(1),0.001);
	assertEquals(0.0,jc.conditionalProbability(2),0.001);
	assertEquals(0.0,jc.conditionalProbability(3),0.001);
	assertEquals(0.0,jc.conditionalProbability(4),0.001);
    
    }

}
