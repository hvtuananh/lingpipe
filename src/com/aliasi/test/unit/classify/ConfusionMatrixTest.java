package com.aliasi.test.unit.classify;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;


import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.PrecisionRecallEvaluation;

public class ConfusionMatrixTest  {

    private static String[] BINARY_CATS = new String[] { "0", "1" };
    private static String[] WINE_CATS = new String[] { "Cab", "Syr", "Pin" };

    @Test(expected=IllegalArgumentException.class)
    public void testExcIncrement() {
        ConfusionMatrix matrix = new ConfusionMatrix(WINE_CATS);
        matrix.incrementByN(0,0,-1);
    }

    @Test
    public void testInit() {
    ConfusionMatrix matrix = new ConfusionMatrix(BINARY_CATS);
    assertArrayEquals(BINARY_CATS,matrix.categories());
    int[][] initMatrix = matrix.matrix();
    assertEquals(2,initMatrix.length);
    assertEquals(2,initMatrix[0].length);
    for (int i = 0; i < 2; ++i)
        for (int j = 0; j < 2; ++j)
        assertEquals(0,initMatrix[i][j]);
    assertEquals(0,matrix.getIndex("0"));
    assertEquals(1,matrix.getIndex("1"));
    assertEquals(-1,matrix.getIndex("2"));
    assertEquals((2-1)*(2-1),matrix.chiSquaredDegreesOfFreedom());
    assertEquals(2,matrix.numCategories());
    }

    @Test
    public void testIncrement() {
    ConfusionMatrix matrix = new ConfusionMatrix(WINE_CATS);
    matrix.increment("Cab","Cab");
    assertEquals(1,matrix.count(0,0));
    matrix.increment(0,0);
    assertEquals(2,matrix.count(0,0));
    matrix.increment(1,2);
    matrix.increment("Syr","Pin");
    assertEquals(2,matrix.count(1,2));
    assertEquals(0,matrix.count(2,1));
    int[][] expectedMatrix = new int[][] { { 2, 0, 0 }, 
                           { 0, 0, 2 }, 
                           { 0, 0, 0 } };
    int[][] foundMatrix = matrix.matrix();
    assertEquals(expectedMatrix.length, foundMatrix.length);
    for (int i = 0; i < foundMatrix.length; ++i)
        assertArrayEquals(expectedMatrix[i],foundMatrix[i]);
    }

    @Test
    public void testStats() {
    int[][] counts = new int[][] {
        { 9, 3, 0 },
        { 3, 5, 1 },
        { 1, 1, 4 }
    };
    ConfusionMatrix matrix = new ConfusionMatrix(WINE_CATS,counts);
    assertEquals(9,matrix.count(0,0));
    assertEquals(3,matrix.count(0,1));
    assertEquals(1,matrix.count(2,0));

    // no arg methods
    assertEquals(27,matrix.totalCount());
    assertEquals(18,matrix.totalCorrect());
    assertEquals(0.6667,matrix.totalAccuracy(),0.005);
    assertEquals(0.1778,matrix.confidence95(),0.005);
    assertEquals(0.2341,matrix.confidence99(),0.005);
    assertEquals(0.3663,matrix.randomAccuracy(),0.005);
    assertEquals(0.3669,matrix.randomAccuracyUnbiased(),0.005);
    assertEquals(0.4740,matrix.kappa(),0.005);
    assertEquals(0.4735,matrix.kappaUnbiased(),0.005);
    assertEquals(0.3333,matrix.kappaNoPrevalence(),0.005);
    assertEquals(1.5305,matrix.referenceEntropy(),0.005);
    assertEquals(1.4865,matrix.responseEntropy(),0.005);
    assertEquals(1.5376,matrix.crossEntropy(),0.005);

    // cat indexed methods
    PrecisionRecallEvaluation ova0 = matrix.oneVsAll(0);
    PrecisionRecallEvaluation ova1 = matrix.oneVsAll(1);
    PrecisionRecallEvaluation ova2 = matrix.oneVsAll(2);
    

    assertEquals(12,ova0.positiveReference());
    assertEquals(9,ova1.positiveReference());
    assertEquals(6,ova2.positiveReference());
    assertEquals(0.4414,ova0.referenceLikelihood(),0.005);
    assertEquals(0.3333,ova1.referenceLikelihood(),0.005);
    assertEquals(0.2222,ova2.referenceLikelihood(),0.005);
    assertEquals(13,ova0.positiveResponse());
    assertEquals(9,ova1.positiveResponse());
    assertEquals(5,ova2.positiveResponse());
    assertEquals(0.4815,ova0.responseLikelihood(),0.005);
    assertEquals(0.3333,ova1.responseLikelihood(),0.005);
    assertEquals(0.1852,ova2.responseLikelihood(),0.005);
    assertEquals(0.6923,ova0.precision(),0.005);
    assertEquals(0.5555,ova1.precision(),0.005);
    assertEquals(0.8000,ova2.precision(),0.005);
    assertEquals(0.7500,ova0.recall(),0.005);
    assertEquals(0.5555,ova1.recall(),0.005);
    assertEquals(0.6666,ova2.recall(),0.005);
    assertEquals(0.7200,ova0.fMeasure(),0.005);
    assertEquals(0.5555,ova1.fMeasure(),0.005);
    assertEquals(0.7273,ova2.fMeasure(),0.005);

    assertEquals(0.7333,ova0.rejectionRecall(),0.005);
    assertEquals(0.7778,ova1.rejectionRecall(),0.0001);
    assertEquals(0.9524,ova2.rejectionRecall(),0.0001);

    assertEquals(0.7857,ova0.rejectionPrecision(),0.0001);
    assertEquals(0.7778,ova1.rejectionPrecision(),0.0001);
    assertEquals(0.9091,ova2.rejectionPrecision(),0.0001);

    assertEquals(0.5625,ova0.jaccardCoefficient(),0.0001);
    assertEquals(0.3846,ova1.jaccardCoefficient(),0.0001);
    assertEquals(0.5714,ova2.jaccardCoefficient(),0.0001);

    assertEquals(0.7407,ova0.accuracy(),0.0001);
    assertEquals(0.7037,ova1.accuracy(),0.0001);
    assertEquals(0.8889,ova2.accuracy(),0.0001);
    

    assertEquals((3-1)*(3-1),matrix.chiSquaredDegreesOfFreedom());
    assertEquals(3,matrix.numCategories());
    assertEquals(15.5256,matrix.chiSquared(),0.005);
    assertEquals(6.2382,ova0.chiSquared(),0.005);
    assertEquals(3.0000,ova1.chiSquared(),0.005);
    assertEquals(11.8519,ova2.chiSquared(),0.005);

    assertEquals(0.6826,matrix.macroAvgPrecision(),0.005);
    assertEquals(0.6574,matrix.macroAvgRecall(),0.005);
    assertEquals(0.6676,matrix.macroAvgFMeasure(),0.005);

    PrecisionRecallEvaluation microAvg = matrix.microAverage();
    assertEquals(0.6666,microAvg.precision(),0.005);
    assertEquals(0.6666,microAvg.recall(),0.005);
    assertEquals(0.6666,microAvg.fMeasure(),0.005);

    assertEquals(2.6197,matrix.jointEntropy(),0.005);

    assertEquals(0.8113,matrix.conditionalEntropy(0),0.005);
    assertEquals(1.3516,matrix.conditionalEntropy(1),0.005);
    assertEquals(1.2516,matrix.conditionalEntropy(2),0.005);
    assertEquals(1.0892,matrix.conditionalEntropy(),0.005);

    assertEquals(0.5750,matrix.phiSquared(),0.005);
    assertEquals(0.5362,matrix.cramersV(),0.005);

    assertEquals(0.7838,ova0.yulesQ(),0.005);
    assertEquals(0.6279,ova1.yulesQ(),0.005);
    assertEquals(0.9512,ova2.yulesQ(),0.005);

    assertEquals(0.4835, ova0.yulesY(),0.005);
    assertEquals(0.3531,ova1.yulesY(),0.005);
    assertEquals(0.7269,ova2.yulesY(),0.005);

    assertEquals(12.49, ova0.fowlkesMallows(),0.05);
    assertEquals(9.00,ova1.fowlkesMallows(),0.05);
    assertEquals(5.48,ova2.fowlkesMallows(),0.05);

    assertEquals(0.4000,matrix.lambdaA(),0.005);
    assertEquals(0.3571,matrix.lambdaB(),0.005);

    assertEquals(matrix.responseEntropy()-matrix.conditionalEntropy(),
             matrix.mutualInformation(),0.005);
    assertEquals(0.007129,
             matrix.klDivergence(),0.00005);

    }
    
}

