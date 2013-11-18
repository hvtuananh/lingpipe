package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


import com.aliasi.lm.CompiledNGramBoundaryLM;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.LanguageModel;

import com.aliasi.util.AbstractExternalizable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class NGramBoundaryLMTest  {

    @Test(expected=IllegalArgumentException.class)
    public void testEx() {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,128,4.0,'a');
        char[] bad = "bad".toCharArray();
        lm.train(bad, 0, 3);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testEx2() {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,128,4.0,'a');
        lm.train("bad");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEx3() {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,128,4.0,'a');
        char[] bad = "bad".toCharArray();
        lm.log2ConditionalEstimate(bad,0,3);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testEx4() {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,128,4.0,'a');
        lm.log2ConditionalEstimate("bad");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEx5() {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,128,4.0,'a');
        lm.log2ConditionalEstimate("");
    }

    @Test
    public void test() throws ClassNotFoundException, IOException {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,128,4.0,'a');
        char[] bad = "bad".toCharArray();
        LanguageModel.Conditional lmSD = writeRead(lm);

        assertTrue((lmSD instanceof LanguageModel.Sequence));

        try {
            lmSD.log2ConditionalEstimate(bad,0,3);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            lmSD.log2ConditionalEstimate("bad");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            lmSD.log2ConditionalEstimate("");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

    }

    @Test
    public void testNullEsts() throws ClassNotFoundException, IOException {
        NGramBoundaryLM lm = new NGramBoundaryLM(3,127,4.0,'a');
        assertEquals(-7.0,lm.log2Estimate(""),0.005);  // end at 1/128
        assertEquals(-14.0,lm.log2Estimate("b"),0.005);

        LanguageModel.Sequence lmSD = writeRead(lm);
        assertEquals(-7.0,lmSD.log2Estimate(""),0.005);  // end at 1/128
        assertEquals(-14.0,lmSD.log2Estimate("b"),0.005);


    }

    // 10
    // abc 1
    // bcd 1
    // cd? 2
    // d? 2
    // ? 2
    //   ab 1
    //   cd 1
    @Test
    public void testSimpleEsts() throws ClassNotFoundException, IOException {
        double lambdaFactor = 4.0;
        NGramBoundaryLM lm = new NGramBoundaryLM(3,127,lambdaFactor,'\uFFFF');
        double uniform = 1.0/128.0;
        lm.train("abcd");
        lm.train("cd");
        double numOutcomesBound = 2.0;
        double extCountBound = 2.0;
        double lambdaBound
            = extCountBound / (extCountBound + lambdaFactor * numOutcomesBound);

        double numOutcomesEmpty = 5.0;
        double extCountEmpty = 8.0;
        double lambdaEmpty
            = extCountEmpty / (extCountEmpty + lambdaFactor * numOutcomesEmpty);

        double mlBound = 2.0/8.0;
        double pBound = lambdaEmpty * mlBound
            + (1.0 - lambdaEmpty) * uniform;

        double pTotal = (1.0 - lambdaBound) * pBound;
        assertEquals(com.aliasi.util.Math.log2(pTotal),
                     lm.log2Estimate(""),
                     0.005);


        LanguageModel lmSD = writeRead(lm);
        assertEquals(com.aliasi.util.Math.log2(pTotal),
                     lmSD.log2Estimate(""),
                     0.005);
    }

    public static NGramBoundaryLM serDeser(NGramBoundaryLM model) 
        throws ClassNotFoundException, IOException {
        return (NGramBoundaryLM) AbstractExternalizable.serializeDeserialize(model);
    }

    public static CompiledNGramBoundaryLM writeRead(NGramBoundaryLM model) 
        throws ClassNotFoundException, IOException {
        return (CompiledNGramBoundaryLM)
            AbstractExternalizable.compile(model);
    }

}
