package com.aliasi.test.unit.classify;

import static com.aliasi.test.unit.Asserts.assertEqualsArray;
import static com.aliasi.test.unit.Asserts.assertEqualsArray2D;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.junit.Test;

import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

public class ScoredPrecisionRecallEvaluationTest  {

    static ScoredPrecisionRecallEvaluation testCase() {
        ScoredPrecisionRecallEvaluation eval 
            = new ScoredPrecisionRecallEvaluation();
        eval.addCase(false,-1.21);
        eval.addCase(true,-1.27);
        eval.addCase(false,-1.39);
        eval.addCase(true,-1.47);
        eval.addCase(true,-1.60);
        eval.addCase(false,-1.65);
        eval.addCase(false,-1.79);
        eval.addCase(false,-1.80);
        eval.addCase(true,-2.01);
        eval.addCase(false,-3.70);
        eval.addMisses(1);
        return eval;
    }

    @Test
    public void rPrecisionTest() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        double rPrecision = eval.rPrecision();
        double expRPrecision = 0.6;
        assertEquals(expRPrecision,rPrecision,0.0001);
    }

    @Test
    public void elevenPtInterpPrecTest() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        double[] precs = eval.elevenPtInterpPrecision();
        double[] expectedPrecs = new double[] {
            1.0, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.444, 0.444, 0.0, 0.0
        };
        assertEqualsArray(expectedPrecs,precs,0.01);
    }
    
    @Test
    public void averagePrecisionTest() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        double[][] rps = eval.prCurve(false);
        double expectedAvgPrecision
        = (0.5 + 0.5 + 0.6 + 0.4444 + 0.0)/5;
        double avgPrecision = eval.averagePrecision();
        assertEquals(expectedAvgPrecision,avgPrecision,0.001);
    }

    @Test
    public void rocCurveTest() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();

        double[][] expectedRocCurve = new double[][] {
            { 1 - 1.000, 0.000 },
            { 1 - 0.833, 0.000 },
            { 1 - 0.833, 0.200 },
            { 1 - 0.667, 0.200 },
            { 1 - 0.667, 0.400 },
            { 1 - 0.667, 0.600 },
            { 1 - 0.500, 0.600 },
            { 1 - 0.333, 0.600 },
            { 1 - 0.167, 0.600 },
            { 1 - 0.167, 0.800 },
            { 1 - 0.000, 0.800 },
            { 1 - 0.000, 1.000 }
        };
        double[][] rocCurve = eval.rocCurve(false);
        assertEqualsArray2D(expectedRocCurve,rocCurve,0.01);

        double[][] expectedInterpolatedRocCurve = new double[][] {
            { 1 - 1.000, 0.000 },
            { 1 - 0.833, 0.200 },
            { 1 - 0.667, 0.600 },
            { 1 - 0.500, 0.600 },
            { 1 - 0.333, 0.600 },
            { 1 - 0.167, 0.800 },
            { 1 - 0.000, 1.000 }
        };
        double[][] interpolatedRocCurve = eval.rocCurve(true);
        assertEqualsArray2D(expectedInterpolatedRocCurve,interpolatedRocCurve,0.01);

        // .167 * .1 + (.333 - .167)* .3 + (.833 - .333) * .7 + (1 - .833)*.9
        assertEquals(0.55, eval.areaUnderRocCurve(true),0.001);
    }

    

    static void print(String msg, double[][] rocCurve) {
        System.out.println("\n" + msg);
        for (double[] rs : rocCurve) {
            System.out.printf("sens=%5.3f spec=%5.3f\n",rs[0],rs[1]);
        }
    }

    @Test
    public void prCurveTest() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();

        double[][] expectedRps = new double[][] {
            { 0.00, 1.00 },
            { 0.20, 0.50 },
            { 0.20, 0.33 },
            { 0.40, 0.50 },
            { 0.60, 0.60 },
            { 0.60, 0.50 },
            { 0.60, 0.43 },
            { 0.60, 0.38 },
            { 0.80, 0.44 },
            { 0.80, 0.40 },
            { 1.00, 0.00 }
        };

        double[][] rps = eval.prCurve(false);
        assertEqualsArray2D(expectedRps,rps,0.01);

        double[][] expectedInterpolatedPrs = new double[][] {
            { 0.00, 1.00 },
            { 0.20, 0.60 },
            { 0.40, 0.60 },
            { 0.60, 0.60 },
            { 0.80, 0.44 },
            { 1.00, 0.00 }
        };

        double[][] interpolatedRps = eval.prCurve(true);
        assertEqualsArray2D(expectedInterpolatedPrs,interpolatedRps,0.01);
    }

    @Test
    public void testMaxF() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        double maxF = eval.maximumFMeasure();
        double expectedMaxF = 0.6;
        assertEquals(expectedMaxF,maxF,0.001);
    }

    @Test
    public void testPrecisionAtN() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        assertEquals(1.0, eval.precisionAt(0), 0.001);
        assertEquals(0.0, eval.precisionAt(1), 0.001);
        assertEquals(0.6, eval.precisionAt(5), 0.001);
        assertEquals(0.4, eval.precisionAt(10), 0.001);
        assertEquals(0.2, eval.precisionAt(20), 0.001);
        assertEquals(0.04, eval.precisionAt(100), 0.001);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPrecisionAtNExc() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        eval.precisionAt(-1);
    }

    @Test
    public void testReciprocalRank() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        assertEquals(0.5, eval.reciprocalRank(),0.001);
    }

    @Test
    public void testRPrecision() {
        ScoredPrecisionRecallEvaluation eval 
            = testCase();
        assertEquals(0.6, eval.rPrecision(), 0.001);
    }

    /*
    @Test
    public void testListScored() {
        ScoredPrecisionRecallEvaluation eval 
            = new ScoredPrecisionRecallEvaluation();
        eval.addCase(true,-1);
        eval.addCase(true,-1);
        eval.addCase(false,-2);
        eval.addCase(true,-3);
        eval.addCase(true,-4);
        assertEquals(5,eval.numCases());
        double[][] expectedPrCurve = new double[][] {
            { 1.0/4.0, 1.0/1.0, -1.0 },
            { 2.0/4.0, 2.0/2.0, -1.0 },
            // { 2.0/4.0, 2.0/3.0, -2.0 }, // not added, because it's not a FP step
            { 3.0/4.0, 3.0/4.0, -3.0 },
            { 4.0/4.0, 4.0/5.0, -4.0 }
        };
        double[][] prCurve = eval.prScoreCurve(false);
        assertEquals("length",expectedPrCurve.length,prCurve.length);
        for (int i = 0; i < prCurve.length; ++i) {
            for (int j = 0; j < 3; ++j) {
                assertEquals(i + "," + j, expectedPrCurve[i][j],prCurve[i][j],0.0001); 
            }
        }

        double[][] expectedPrCurveI = new double[][] {
            // { 1.0/4.0, 1.0/1.0, -1.0 }, // interpolated out
            { 2.0/4.0, 2.0/2.0, -1.0 },
            // { 2.0/4.0, 2.0/3.0, -2.0 }, // not added, because it's not a FP step
            // { 3.0/4.0, 3.0/4.0, -3.0 }, // interpolated out
            { 4.0/4.0, 4.0/5.0, -4.0 }
        };
        double[][] prCurveI = eval.prScoreCurve(true);
        assertEquals("length",expectedPrCurveI.length,prCurveI.length);
        for (int i = 0; i < prCurveI.length; ++i) {
            for (int j = 0; j < 3; ++j) {
                assertEquals(i + "," + j, expectedPrCurveI[i][j],prCurveI[i][j],0.0001); 
            }
        }
    }


    @Test
    public void testOne() {
        ScoredPrecisionRecallEvaluation eval 
            = new ScoredPrecisionRecallEvaluation();
        eval.addCase(false,-1.21);
        eval.addCase(true,-1.27);
        eval.addCase(false,-1.39);
        eval.addCase(true,-1.47);
        eval.addCase(true,-1.60);
        eval.addCase(false,-1.65);
        eval.addCase(false,-1.79);
        eval.addCase(false,-1.80);
        eval.addCase(true,-2.01);
        eval.addCase(false,-3.70);

        double[][] prCurve = eval.prCurve(false);
        assertEquals(4,prCurve.length);
        assertEqualsArray(new double[] { 0.25, 0.50 },
                          prCurve[0], 0.01);
        assertEqualsArray(new double[] { 0.50, 0.50 },
                          prCurve[1], 0.01);
        assertEqualsArray(new double[] { 0.75, 0.60 },
                          prCurve[2], 0.01);
        assertEqualsArray(new double[] { 1.00, 0.44 },
                          prCurve[3], 0.01);

        assertEquals(0.5,eval.reciprocalRank(),0.0005);
    
        assertEquals(0.0,eval.precisionAt(1),0.0005);
        assertEquals(0.5,eval.precisionAt(2),0.0005);
        assertEquals(0.6,eval.precisionAt(5),0.0005);
        assertTrue(Double.isNaN(eval.precisionAt(20)));

        double[][] interpolatedPrCurve = eval.prCurve(true);
        assertEquals(2,interpolatedPrCurve.length);
        assertEqualsArray(new double[] { 0.75, 0.60 },
                          interpolatedPrCurve[0], 0.01);
        assertEqualsArray(new double[] { 1.00, 0.44 },
                          interpolatedPrCurve[1], 0.01);
        
        CharArrayWriter caw = new CharArrayWriter();
        eval.printPrecisionRecallCurve(interpolatedPrCurve, new PrintWriter(caw));
        String[] lines = caw.toString().split("\\n");
        String[] fieldsOnLine1 = lines[1].split(" ");
        assertEquals(lines.length,3,0.01);
        assertEquals(fieldsOnLine1.length,3,0.01);
        assertEquals(Double.parseDouble(fieldsOnLine1[0]),0.6,0.01);
        assertEquals(Double.parseDouble(fieldsOnLine1[1]),0.75,0.01);
        assertEquals(Double.parseDouble(fieldsOnLine1[2]),0.66666,0.01);
        
        assertEquals(0.51,eval.areaUnderPrCurve(false),0.01);
        assertEquals(0.56,eval.areaUnderPrCurve(true),0.01);

        assertEquals(0.51,eval.averagePrecision(),0.01);
        assertEquals(0.67,eval.maximumFMeasure(),0.01);
        assertEquals(0.60,eval.prBreakevenPoint(),0.01);


        double[][] rocCurve = eval.rocCurve(false);
        assertEquals(4,rocCurve.length);
        assertEqualsArray(new double[] { 0.25, 0.83 },
                          rocCurve[0], 0.01);
        assertEqualsArray(new double[] { 0.50, 0.67 },
                          rocCurve[1], 0.01);
        assertEqualsArray(new double[] { 0.75, 0.67 },
                          rocCurve[2], 0.01);
        assertEqualsArray(new double[] { 1.00, 0.17 },
                          rocCurve[3], 0.01);

        double[][] interpolatedRocCurve = eval.rocCurve(true);
        assertEquals(3,interpolatedRocCurve.length);
        assertEqualsArray(new double[] { 0.25, 0.83 },
                          interpolatedRocCurve[0], 0.01);
        assertEqualsArray(new double[] { 0.75, 0.67 },
                          interpolatedRocCurve[1], 0.01);
        assertEqualsArray(new double[] { 1.00, 0.17 },
                          interpolatedRocCurve[2], 0.01);

        assertEquals(0.58,eval.areaUnderRocCurve(false),0.01);
        assertEquals(0.58,eval.areaUnderRocCurve(true),0.01);
    }

    */
}
