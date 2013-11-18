package com.aliasi.test.unit.lm;

import com.aliasi.lm.CompiledTokenizedLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;
import com.aliasi.lm.TrieIntSeqCounter;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;
import static com.aliasi.test.unit.Asserts.succeed;


import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.ScoredObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;

public class TokenizedLMTest  {

    private static final int MAX_NGRAM = 3;
    private static final double LAMBDA_FACTOR = 4.0;

    @Test
    public void testTrainSequence() {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizedLM lm = new TokenizedLM(tf,3);
        SymbolTable st = lm.symbolTable();
        TrieIntSeqCounter counter = lm.sequenceCounter();

        // automatically get the BOUNDARY_TOKEN incremented to start
        assertEquals(1,counter.count(new int[0],0,0));

        String ab = "a b";
        String ac = "a c";
        String abc = "a b c";
        lm.trainSequence(ab,2);
        lm.trainSequence(ac,3);
        lm.trainSequence(abc,4);
        
        int a = st.symbolToID("a");
        int b = st.symbolToID("b");
        int c = st.symbolToID("c");
        
        assertEquals(2,counter.count(new int[] { a, b },0,2));
        assertEquals(3,counter.count(new int[] { a, c },0,2));
        assertEquals(4,counter.count(new int[] { a, b, c},0,3));
        assertEquals(5,counter.extensionCount(new int[] { a },0,1));

        lm.trainSequence("a a a c c c",111);
        assertEquals(111,counter.count(new int[] { c, c, c},0,3));
        assertEquals(111,counter.extensionCount(new int[] { c, c},0,2));

        lm.trainSequence("",999);
        assertEquals(1000,counter.count(new int[0],0,0));
    }

    @Test
    public void testZeroGram() {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        try { 
            new TokenizedLM(tf,
                            0,
                            new UniformBoundaryLM(16),
                            new UniformBoundaryLM(16),
                            LAMBDA_FACTOR);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    @Test
    public void testUnigram() {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizedLM lm 
            = new TokenizedLM(tf,
                              1,
                              new UniformBoundaryLM(16),
                              new UniformBoundaryLM(16),
                              LAMBDA_FACTOR);
        lm.train("John Smith");
    }

    @Test
    public void testBiggerGram() {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizedLM lm 
            = new TokenizedLM(tf,
                              4,
                              new UniformBoundaryLM(16),
                              new UniformBoundaryLM(16),
                              LAMBDA_FACTOR);
        lm.train("John Smith");
    }


    @Test
    public void testChiSquaredIndependence() {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizedLM lm 
            = new TokenizedLM(tf,
                              3,
                              new UniformBoundaryLM(16),
                              new UniformBoundaryLM(16),
                              LAMBDA_FACTOR);
        lm.train("a b c a b d a b e a b");
        SymbolTable table = lm.symbolTable();
        assertEquals(5,table.numSymbols());
        int aI = table.symbolToID("a");
        int bI = table.symbolToID("b");
        int cI = table.symbolToID("c");
        int dI = table.symbolToID("d");
        int eI = table.symbolToID("e");
        assertTrue(aI >= 0);
        assertTrue(bI >= 0);
        assertTrue(cI >= 0);
        assertTrue(dI >= 0);
        assertTrue(eI >= 0);
    
        assertTrue(lm.chiSquaredIndependence(new int[] { aI, bI })
                   > lm.chiSquaredIndependence(new int[] { bI, cI }));

        assertTrue(lm.chiSquaredIndependence(new int[] { cI, aI })
                   > lm.chiSquaredIndependence(new int[] { cI, eI }));

    }

    @Test
    public void testConstantSubModels() throws ClassNotFoundException, IOException {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizedLM lm
            = new TokenizedLM(tf,
                              MAX_NGRAM,
                              new UniformBoundaryLM(127), // unknown tok
                              new UniformBoundaryLM(15), // whitespace
                              LAMBDA_FACTOR);

        // INITIAL SYMBOL TRIE
        // 2
        // (EOS) 1

        // P("")
        // P(EOS|EOS) + Pws("")
        // P(EOS|EOS) = P(EOS) = lambda() * PML(EOS)
        double lambda_ = 1.0/(1.0 + 4.0*1.0);
        double pml_EOS = 1.0;
        double p_EOS = lambda_ * pml_EOS;
        double pws_ = 1.0/16.0;
        assertEstimate(com.aliasi.util.Math.log2(p_EOS * pws_),
                       lm,"");

        // "a"
        // P(UNK|EOS) * P(EOS|EOS,UNK) * Ptok("a") * Pws("") * Pws("")
        // P(UNK|EOS) = P(UNK) = (1 - lambda())
        // P(EOS|EOS,UNK) = P(EOS|EOS) = P(EOS)
        double p_UNK = 1.0 - lambda_;
        double ptok_a = (1.0/128.0) * (1.0/128.0);
        assertEstimate(com.aliasi.util.Math.log2(p_UNK * p_EOS 
                                                 * ptok_a 
                                                 * pws_ * pws_),
                       lm,"a");

        // P("a b")
        // = P(UNK|EOS) P(UNK|EOS,UNK) P(EOS|UNK,UNK) 
        //   Pws("") Pws("") Pws(" ") 
        //   Ptok(a) Ptok(b)
        // P(UNK|EOS) = P(UNK)
        // P(UNK|EOS,UNK) = P(UNK|UNK) = P(UNK)
        // P(EOS|UNK) = P(EOS)
        double ptok_b = ptok_a;
        double pws_s = pws_ * 1.0/16.0;

        assertEstimate(com.aliasi.util.Math.log2(p_UNK * p_UNK * p_EOS
                                                 * pws_ * pws_ * pws_s
                                                 * ptok_a * ptok_b),
                       lm,"a b");

        double ptok_c = ptok_b;
        assertEstimate(com.aliasi.util.Math.log2(p_UNK * p_UNK * p_UNK * p_EOS
                                                 * pws_ * pws_ * pws_s * pws_s
                                                 * ptok_a * ptok_b * ptok_c),
                       lm,"a b c");

        double ptok_d = ptok_b;
        assertEstimate(com.aliasi.util.Math.log2(p_UNK * p_UNK * p_UNK * p_UNK * p_EOS
                                                 * pws_ * pws_ * pws_s * pws_s * pws_s
                                                 * ptok_a * ptok_b * ptok_c * ptok_d),
                       lm,"a b c d");


        // ============ train on "a" ===================
        // go through same estimates, see above
    
        lm.train("a");

        // EOS 2
        //     a 1
        //       EOS 1
        // a 1
        //   EOS 1

        // P("")
        // P(EOS|EOS) + Pws("")
        // P(EOS|EOS) = lambda(EOS) * PML(EOS|EOS)
        //            + (1-lambda(EOS)) * P(EOS)
        //            = (1-lambda(EOS)) *(lambda() * PML(EOS))
        lambda_ = 3.0/(3.0 + 4.0*2.0);
        pml_EOS = 2.0/3.0;
        p_EOS = lambda_ * pml_EOS;
        double lambda_EOS = 1.0 / (1.0 + 4.0 * 1.0);
        double p_EOS_giv_EOS = (1.0 - lambda_EOS) * p_EOS;

        assertEstimate(com.aliasi.util.Math.log2(p_EOS_giv_EOS * pws_),
                       lm,"");

        // "a"
        // P(a|EOS) + P(EOS|EOS,a) + Pws("") + Pws("")

        // P(a|EOS) = lambda(EOS) Pml(a|EOS) + (1-lambda(EOS)) P(a)
    
        //    P(a) = lambda() * Pml(a)

        // P(EOS|EOS,a) = lambda(EOS,a) Pml(EOS|EOS,a) 
        //              + (1-lambda(EOS,a)) P(EOS|a)
        // 
        //     P(EOS|a) = lambda(a) Pml(EOS|a) 
        //              + (1-lambda(a)) P(EOS)
        //         P(EOS) = lambda() * Pml(EOS)

        lambda_EOS = 1.0 / (1.0 + 4.0 * 1.0);
        double pml_A_giv_EOS = 1.0;
        lambda_ = 3.0 / (3.0 + 4.0 * 2.0);
        double pml_A = 1.0 / 3.0;
        double p_A = lambda_ * pml_A;
        double p_A_giv_EOS = lambda_EOS * pml_A_giv_EOS
            + (1.0 - lambda_EOS) * p_A;
    
        double lambda_EOS_A = 1.0 / (1.0 + 4.0 * 1.0);
        double pml_EOS_giv_EOS_A = 1.0;
        double lambda_A = 1.0 / (1.0 + 4.0 * 1.0);
        double pml_EOS_giv_A = 1.0;
        pml_EOS = 2.0 / 3.0;
        p_EOS = lambda_ * pml_EOS;
        double p_EOS_giv_A = lambda_A * pml_EOS_giv_A 
            + (1.0 - lambda_A) * p_EOS;
        double p_EOS_giv_EOS_A = lambda_EOS_A * pml_EOS_giv_EOS_A
            + (1.0 - lambda_EOS_A) * p_EOS_giv_A;
        
        assertEstimate(com.aliasi.util.Math.log2(p_A_giv_EOS
                                                 * p_EOS_giv_EOS_A
                                                 * pws_ * pws_),
                       lm,"a");
    }



    @Test
    public void testTwo() throws ClassNotFoundException, IOException {
        TokenizedLM lm
            = new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,
                              MAX_NGRAM,
                              new UniformBoundaryLM(127), // unknown tok
                              new UniformBoundaryLM(15), // whitespace
                              LAMBDA_FACTOR);

        assertEqEstimate(lm,"a");
        assertEqEstimate(lm,"a b");
        assertEqEstimate(lm,"a a b");
        assertEqEstimate(lm,"a b a");

        lm.train("a");
        assertEqEstimate(lm,"a");
        assertEqEstimate(lm,"a b");
        assertEqEstimate(lm,"a a b");
        assertEqEstimate(lm,"a b a");

        lm.train("a b c");
        assertEqEstimate(lm,"a");
        assertEqEstimate(lm,"a b");
        assertEqEstimate(lm,"a b e");

        lm.train("x y");
        assertEqEstimate(lm,"x y a b e x y");
        assertEqEstimate(lm,"");
        assertEqEstimate(lm,"x");
    }

    @Test
    public void testCollocs() {
        TokenizedLM lm
            = new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,4);
        lm.train("a b c d");
        lm.train("a b e f");
        lm.train("c f e");

        SortedSet<ScoredObject<String[]>> collocSet
            = lm.collocationSet(2,1,2);
        assertEquals(2,collocSet.size());
        Iterator<ScoredObject<String[]>> it = collocSet.iterator();
        assertArrayEquals(new String[] { "a", "b" },
                          it.next().getObject());
        assertArrayEquals(new String[] { "c", "d" },
                          it.next().getObject());


        lm = new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,4);
        lm.train("a b c d");
        lm.train("a b c e");
        lm.train("d e f");
        lm.train("f d e");
        lm.train("e f d");
        collocSet = lm.collocationSet(3,1,2);
        assertEquals(2,collocSet.size());
        assertArrayEquals(new String[] { "a", "b", "c" },
                          collocSet.iterator().next().getObject());

        try {
            lm.collocationSet(1,1,3);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }
    }

    static ScoredObject[] newTerms(TokenizedLM lm, int ngram, int minCount, int maxReturn, TokenizedLM lm2) {
        SortedSet<ScoredObject<String[]>> termSet = lm.newTermSet(ngram,minCount,maxReturn,lm2);
        ScoredObject[] result = new ScoredObject[termSet.size()];
        Iterator<ScoredObject<String[]>> it = termSet.iterator();
        for (int i = 0; i < result.length; ++i)
            result[i] = it.next();
        return result;
    }

    static ScoredObject[] oldTerms(TokenizedLM lm, int ngram, int minCount, int maxReturn, TokenizedLM lm2) {
        SortedSet<ScoredObject<String[]>> termSet = lm.oldTermSet(ngram,minCount,maxReturn,lm2);
        ScoredObject[] result = new ScoredObject[termSet.size()];
        Iterator<ScoredObject<String[]>> it = termSet.iterator();
        for (int i = 0; i < result.length; ++i)
            result[i] = it.next();
        return result;
    }


    static ScoredObject[] frequentTerms(TokenizedLM lm, int ngram, int maxReturn) {
        SortedSet<ScoredObject<String[]>> termSet = lm.frequentTermSet(ngram,maxReturn);
        ScoredObject[] result = new ScoredObject[termSet.size()];
        Iterator<ScoredObject<String[]>> it = termSet.iterator();
        for (int i = 0; i < result.length; ++i)
            result[i] = it.next();
        return result;
    }


    static ScoredObject[] infrequentTerms(TokenizedLM lm, int ngram, int maxReturn) {
        SortedSet<ScoredObject<String[]>> termSet = lm.infrequentTermSet(ngram,maxReturn);
        ScoredObject[] result = new ScoredObject[termSet.size()];
        Iterator<ScoredObject<String[]>> it = termSet.iterator();
        for (int i = 0; i < result.length; ++i)
            result[i] = it.next();
        return result;
    }



    @Test
    public void testNewAndOldTerms() {
        TokenizedLM lm1
            = new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,3);
        TokenizedLM lm2
            = new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,3);
        // need several instances to overcome boundaries and unknowns
        lm1.train("b c d");
        lm1.train("b c d");
        lm1.train("b c d");
        lm1.train("b c f");

        lm2.train("b c x");
        lm2.train("b c x");
        lm2.train("b c x");
        lm2.train("b c y");
    
        ScoredObject[] newTerms1 = newTerms(lm1,2,1,3,lm2);
        assertArrayEquals(new String[] { "c", "d" },
                          (String[]) newTerms1[0].getObject());

        ScoredObject[] newTerms2 = newTerms(lm2,2,1,2,lm1);
        assertArrayEquals(new String[] { "c", "x" },
                          (String[]) newTerms2[0].getObject());

        ScoredObject[] oldTerms1 = oldTerms(lm1,2,1,3,lm2);
        assertArrayEquals(new String[] { "c", "f" },
                          (String[]) oldTerms1[0].getObject());

        ScoredObject[] oldTerms2 = oldTerms(lm2,2,1,3,lm1);
        assertArrayEquals(new String[] { "c", "y" },
                          (String[]) oldTerms2[0].getObject());

        ScoredObject[] fTerms1 = frequentTerms(lm1,2,10);
        assertArrayEquals(new String[] { "b", "c" },
                          (String[]) fTerms1[0].getObject());
        assertArrayEquals(new String[] { "c", "d" },
                          (String[]) fTerms1[1].getObject());
        assertArrayEquals(new String[] { "c", "f" },
                          (String[]) fTerms1[2].getObject());

        ScoredObject[] fTerms2 = infrequentTerms(lm1,2,10);
        assertArrayEquals(new String[] { "b", "c" },
                          (String[]) fTerms2[2].getObject());
        assertArrayEquals(new String[] { "c", "d" },
                          (String[]) fTerms2[1].getObject());
        assertArrayEquals(new String[] { "c", "f" },
                          (String[]) fTerms2[0].getObject());
    }

    private void assertEstimate(double estimate,
                                TokenizedLM lm,
                                CharSequence cSeq)
        throws ClassNotFoundException, IOException {

        assertEquals(estimate,lm.log2Estimate(cSeq),0.005);
        assertEqEstimate(lm,cSeq.toString());
    }

    public void assertEqEstimate(TokenizedLM lm, CharSequence cSeq)
        throws ClassNotFoundException, IOException {

        assertEquals(lm.log2Estimate(cSeq),
                     writeRead(lm).log2Estimate(cSeq),
                     0.005);
    }

    private static CompiledTokenizedLM writeRead(TokenizedLM lm) {
        try { 
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
            lm.compileTo(objOut);
            ByteArrayInputStream bytesIn 
                = new ByteArrayInputStream(bytesOut.toByteArray());
            ObjectInputStream objIn = new ObjectInputStream(bytesIn);
            return (CompiledTokenizedLM) objIn.readObject();
        } catch (IOException e) {
            fail(e.toString());
        } catch (ClassNotFoundException e) {
            fail(e.toString());
        }
        return null; // bogus unreachable; compiler doesn't know fail()
    }


    
    @Test
    public void testMultipleIncrements() {
        Random random = new Random();
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizedLM lm1 = new TokenizedLM(tf,3);
        TokenizedLM lm2 = new TokenizedLM(tf,3);
        for (int i = 0; i < 100; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 5; ++k) {
                sb.append((char)random.nextInt(16));
                sb.append(' ');
            }
            int trainingCount = random.nextInt(10); // train 0 to 10 times
            incrementAssertSynched(lm1,lm2,sb,trainingCount);
        }
    }

    void incrementAssertSynched(TokenizedLM lm1,
                                TokenizedLM lm2,
                                CharSequence cs,
                                int count) {
        for (int i = 0; i < count; ++i)
            lm1.train(cs);
        lm2.train(cs,count);
        assertSynched(lm1,lm2);
    }

    void assertSynched(TokenizedLM lm1, TokenizedLM lm2) {
        for (int i = 0; i < 100; ++i)
            for (int k = 0; k < 5; ++k) 
                assertSynched(lm1,lm2,k);
    }

    void assertSynched(TokenizedLM lm1, TokenizedLM lm2, int k) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < k; ++i) {
            sb.append((char)random.nextInt(16));
            sb.append(' ');
        }
        assertEquals(lm1.log2Estimate(sb),lm2.log2Estimate(sb),0.0001);
    }


}
