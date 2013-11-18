package com.aliasi.test.unit.classify;

import com.aliasi.classify.PrecisionRecallEvaluation;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class PrecisionRecallEvaluationTest  {

    @Test
    public void testOne() {
    PrecisionRecallEvaluation eval =
        new PrecisionRecallEvaluation(9,3,4,11);
    assertEquals(12,eval.positiveReference());
    assertEquals(15,eval.negativeReference());
    assertEquals(13,eval.positiveResponse());
    assertEquals(14,eval.negativeResponse());
    assertEquals(27,eval.total());
    
    assertEquals(.7407,eval.accuracy(),0.001);
    assertEquals(0.7500,eval.recall(),0.001);
    assertEquals(0.6923,eval.precision(),0.001);
    assertEquals(0.7333,eval.rejectionRecall(),0.001);
    assertEquals(0.7858,eval.rejectionPrecision(),0.001);
    assertEquals(0.7200,eval.fMeasure(),0.001);
    assertEquals(12.49,eval.fowlkesMallows(),0.001);
    assertEquals(0.5625,eval.jaccardCoefficient(),0.001);
    assertEquals(0.7838,eval.yulesQ(),0.001);
    assertEquals(0.4835,eval.yulesY(),0.001);
    assertEquals(0.4444,eval.referenceLikelihood(),0.001);
    assertEquals(0.4815,eval.responseLikelihood(),0.001);
    assertEquals(0.5021,eval.randomAccuracy(),0.001);
    assertEquals(0.4792,eval.kappa(),0.001);
    assertEquals(0.5027,eval.randomAccuracyUnbiased(),0.001);
    assertEquals(0.4789,eval.kappaUnbiased(),0.001);
    assertEquals(0.4814,eval.kappaNoPrevalence(),0.001);
    assertEquals(6.2382,eval.chiSquared(),0.001);
    assertEquals(0.2310,eval.phiSquared(),0.001);
    assertEquals(0.0843,eval.accuracyDeviation(),0.001);
    }

    @Test
    public void testTwo() {
    PrecisionRecallEvaluation eval =
        new PrecisionRecallEvaluation(5,4,4,14);
    assertEquals(9,eval.positiveReference());
    assertEquals(18,eval.negativeReference());
    assertEquals(9,eval.positiveResponse());
    assertEquals(18,eval.negativeResponse());
    assertEquals(27,eval.total());
    
    assertEquals(.7037,eval.accuracy(),0.001);
    assertEquals(0.5555,eval.recall(),0.001);
    assertEquals(0.5555,eval.precision(),0.001);
    assertEquals(0.7778,eval.rejectionRecall(),0.001);
    assertEquals(0.7778,eval.rejectionPrecision(),0.001);
    assertEquals(0.5555,eval.fMeasure(),0.001);
    assertEquals(9.00,eval.fowlkesMallows(),0.001);
    assertEquals(0.3846,eval.jaccardCoefficient(),0.001);
    assertEquals(0.6279,eval.yulesQ(),0.001);
    assertEquals(0.3531,eval.yulesY(),0.001);
    assertEquals(0.3333,eval.referenceLikelihood(),0.001);
    assertEquals(0.3333,eval.responseLikelihood(),0.001);
    assertEquals(0.5556,eval.randomAccuracy(),0.001);
    assertEquals(0.3333,eval.kappa(),0.001);
    assertEquals(0.5556,eval.randomAccuracyUnbiased(),0.001);
    assertEquals(0.333,eval.kappaUnbiased(),0.001);
    assertEquals(0.4074,eval.kappaNoPrevalence(),0.001);
    assertEquals(3.000,eval.chiSquared(),0.001);
    assertEquals(0.1111,eval.phiSquared(),0.001);
    assertEquals(0.0879,eval.accuracyDeviation(),0.001);
    }


    @Test
    public void testThree() {
    PrecisionRecallEvaluation eval =
        new PrecisionRecallEvaluation(4,2,1,20);
    assertEquals(6,eval.positiveReference());
    assertEquals(21,eval.negativeReference());
    assertEquals(5,eval.positiveResponse());
    assertEquals(22,eval.negativeResponse());
    assertEquals(27,eval.total());
    
    assertEquals(.8889,eval.accuracy(),0.001);
    assertEquals(0.6666,eval.recall(),0.001);
    assertEquals(0.8000,eval.precision(),0.001);
    assertEquals(0.9524,eval.rejectionRecall(),0.001);
    assertEquals(0.9091,eval.rejectionPrecision(),0.001);
    assertEquals(0.7272,eval.fMeasure(),0.001);
    assertEquals(5.4772,eval.fowlkesMallows(),0.001);
    assertEquals(0.5714,eval.jaccardCoefficient(),0.001);
    assertEquals(0.9512,eval.yulesQ(),0.001);
    assertEquals(0.7269,eval.yulesY(),0.001);
    assertEquals(0.2222,eval.referenceLikelihood(),0.001);
    assertEquals(0.1852,eval.responseLikelihood(),0.001);
    assertEquals(0.6749,eval.randomAccuracy(),0.001);
    assertEquals(0.6583,eval.kappa(),0.001);
    assertEquals(0.6756,eval.randomAccuracyUnbiased(),0.001);
    assertEquals(0.6575,eval.kappaUnbiased(),0.001);
    assertEquals(0.7778,eval.kappaNoPrevalence(),0.001);
    assertEquals(11.8519,eval.chiSquared(),0.001);
    assertEquals(0.4390,eval.phiSquared(),0.001);
    assertEquals(0.0605,eval.accuracyDeviation(),0.001);
    }


}
