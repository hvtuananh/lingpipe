package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.AbstractExternalizable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NGramProcessLMTest  {

    @Test
    public void testExs() {
        NGramProcessLM lm = new NGramProcessLM(3,128);
        try {
            lm.log2ConditionalEstimate("");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testOne() throws ClassNotFoundException, IOException {
        NGramProcessLM model
            = new NGramProcessLM(3,
                                 alphabetSize,
                                 lambdaFactor);
        model.train(ABRACADABRA,0,ABRACADABRA.length);
        assertModel(model);
    }

    @Test
    public void testA()
        throws ClassNotFoundException, IOException {

        NGramProcessLM model
            = new NGramProcessLM(4,128,4.0);
        model.train("a");
        double expectedLambda = 1.0/(1.0+4.0*1.0);
        assertEquals(com.aliasi.util.Math.log2(expectedLambda*1.0
                                               + (1.0-expectedLambda)*1.0/128.0),
                     model.log2ConditionalEstimate("a"),
                     0.005);
        model.train("a");
        expectedLambda = 2.0/(2.0 + 4.0*1.0);
        assertEquals(com.aliasi.util.Math.log2(expectedLambda*1.0
                                               + (1.0-expectedLambda)*1.0/128.0),
                     model.log2ConditionalEstimate("a"),
                     0.005);
    }


    @Test
    public void testA_AB() {
        NGramProcessLM model
            = new NGramProcessLM(4,128,4.0);
        model.train("a");

        model.train("ab");

        double expectedLambda = 3.0/(3.0 + 4.0*2.0);
        double expectedCondA
            = expectedLambda*2.0/3.0
            + (1.0-expectedLambda)*1.0/128.0;
        assertEquals(com.aliasi.util.Math.log2(expectedCondA),
                     model.log2ConditionalEstimate("a"),
                     0.0005);
        double expectedCondB
            = expectedLambda*1.0/3.0
            + (1.0-expectedLambda)*1.0/128.0;
        assertEquals(com.aliasi.util.Math.log2(expectedCondB),
                     model.log2ConditionalEstimate("b"),
                     0.0005);

        double expectedLambdaA = 1.0/(1.0+4.0*1.0);
        double expectedCondAB = expectedLambdaA * 1.0
            + (1.0-expectedLambdaA) * expectedCondB;
        assertEquals(com.aliasi.util.Math.log2(expectedCondAB),
                     model.log2ConditionalEstimate("ab"),
                     0.0005);

    }

    static double lambdaFactor = 4.0;
    static int alphabetSize = 255;

    static char[] ABRACADABRA = "abracadabra".toCharArray();
    static double count = 0.0;

    static char[] A = "a".toCharArray();
    static double numOutcomesNull = 5;
    static double aCount = 5.0;
    static double numEventsNull = 11.0;
    static double mlEstimateA = ((double)aCount)/(double)numEventsNull;
    static double uniformEstimate = 1.0/(double)alphabetSize;
    static double lambdaNull = numEventsNull / (numEventsNull + lambdaFactor * numOutcomesNull);
    static double estimateA = lambdaNull * mlEstimateA + (1.0-lambdaNull) * uniformEstimate;
    static char[] B = "b".toCharArray();
    static double bCount = 2;
    static double mlEstimateB = bCount/numEventsNull;
    static double estimateB = lambdaNull * mlEstimateB + (1.0-lambdaNull) * uniformEstimate;
    static char[] AB = "ab".toCharArray();
    static double aContextCount = 4.0; // 5.0; // should be 4.0!!!
    static double abCount = 2.0;
    static double numOutcomesA = 3.0;
    static double lambdaA = aContextCount / (aContextCount + lambdaFactor * numOutcomesA);
    static double mlEstimateAB = abCount/aContextCount;
    static double estimateAB = lambdaA * mlEstimateAB + (1.0-lambdaA) * estimateB;

    static char[] DAB = "dab".toCharArray();
    static double daContextCount = 1.0;
    static double dabCount = 1.0;
    static double numOutcomesDA = 1.0;
    static double lambdaDA = daContextCount / (daContextCount + lambdaFactor * numOutcomesDA);
    static double mlEstimateDAB = 1.0;
    static double estimateDAB = lambdaDA * mlEstimateDAB
        + (1.0 - lambdaDA) * estimateAB;
    static char[] ZAB = "zab".toCharArray();
    static char[] XDAB = "xdab".toCharArray();


    public void assertModel(NGramProcessLM model) throws IOException, ClassNotFoundException {
        // test dynamic model
        assertConditionalLM(model);

        // test compiled version
        try {
            LanguageModel.Conditional compiledModel
                = (LanguageModel.Conditional) AbstractExternalizable.compile(model);
            assertConditionalLM(compiledModel);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail(e.toString());
        }

        // test read/write version
        try {
            LanguageModel.Conditional serializedModel
                = readWrite(model);
            assertConditionalLM(serializedModel);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail(e.toString());
        }

        // test serialized version
        try {
            LanguageModel.Conditional serializedModel
                = (LanguageModel.Conditional) AbstractExternalizable.serializeDeserialize(model);
            assertConditionalLM(serializedModel);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail(e.toString());
        }
    }


    public static NGramProcessLM readWrite(NGramProcessLM lm)
        throws IOException {

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        lm.writeTo(bytesOut);

        ByteArrayInputStream bytesIn
            = new ByteArrayInputStream(bytesOut.toByteArray());
        return NGramProcessLM.readFrom(bytesIn);
    }

    public void assertConditionalLM(LanguageModel.Conditional model)
        throws IOException {

        assertEquals(com.aliasi.util.Math.log2(estimateA),
                     model.log2ConditionalEstimate(A,0,1),
                     0.0005);
        assertEquals(com.aliasi.util.Math.log2(estimateA),
                     model.log2Estimate(A,0,1),
                     0.0005);

        assertEquals(com.aliasi.util.Math.log2(estimateB),
                     model.log2ConditionalEstimate(B,0,1),
                     0.0005);

        assertEquals("AB",
                     com.aliasi.util.Math.log2(estimateAB),
                     model.log2ConditionalEstimate(AB,0,2),
                     0.0005);

        assertEquals(model.log2ConditionalEstimate(ZAB,0,3),
                     model.log2ConditionalEstimate(AB,0,2),
                     0.0005);

        assertEquals("DAB",
                     com.aliasi.util.Math.log2(estimateDAB),
                     model.log2ConditionalEstimate(DAB,0,3),
                     0.00005);

        assertEquals(com.aliasi.util.Math.log2(estimateDAB),
                     model.log2ConditionalEstimate(XDAB,0,4),
                     0.0005);

        assertEquals(model.log2ConditionalEstimate(A,0,1),
                     model.log2Estimate(A,0,1),
                     0.0005);
        assertEquals(model.log2ConditionalEstimate(AB,0,1)
                     + model.log2ConditionalEstimate(AB,0,2),
                     model.log2Estimate(AB,0,2),
                     0.0005);
        assertEquals(model.log2ConditionalEstimate(DAB,0,1)
                     + model.log2ConditionalEstimate(DAB,0,2)
                     + model.log2ConditionalEstimate(DAB,0,3),
                     model.log2Estimate(DAB,0,3),
                     0.0005);
        assertEquals(model.log2ConditionalEstimate(DAB,1,2)
                     + model.log2ConditionalEstimate(DAB,1,3),
                     model.log2Estimate(DAB,1,3),
                     0.0005);


    }


}
