package com.aliasi.test.unit.hmm;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tag.Tagging;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;
import static com.aliasi.test.unit.Asserts.succeed;

import java.io.IOException;

import java.util.Arrays;


public class HmmCharLmEstimatorTest  {

    public void handle(HmmCharLmEstimator estimator,
                       String[] toks, String[] whitespaces, String[] tags) {
        Tagging<String> tagging
            = new Tagging<String>(Arrays.asList(toks),
                                  Arrays.asList(tags));
        estimator.handle(tagging);
    }



    @Test
    public void testCons() {
        new HmmCharLmEstimator();
        succeed();
    }

    @Test
    public void testCons2() {
        assertNotNull(new HmmCharLmEstimator(5,256,4.0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExc1() { 
        new HmmCharLmEstimator(0,256,4.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExc2() { 
        new HmmCharLmEstimator(-1,256,4.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExc3() { 
        new HmmCharLmEstimator(2,0,4.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExc4() { 
        new HmmCharLmEstimator(2,-1,4.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExc5() { 
        new HmmCharLmEstimator(2,1000000,4.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExc6() { 
        new HmmCharLmEstimator(2,256,-1);
    }
    

    @Test
    public void testStart() throws IOException, ClassNotFoundException {
        HmmCharLmEstimator est = new HmmCharLmEstimator(5,256,4.0);
        est.trainStart("a");
        est.trainStart("a");
        est.trainStart("b");
        est.trainStart("a");

        assertEquals(0.75,est.startProb("a"),0.001);
        assertEquals(0.25,est.startProb("b"), 0.001);
        assertEquals(0.0,est.startProb("c"), 0.001);

        int idA = est.stateSymbolTable().symbolToID("a");
        int idB = est.stateSymbolTable().symbolToID("b");

        assertEquals(est.startProb("a"),est.startProb(idA),0.0001);
        assertEquals(est.startProb("b"),est.startProb(idB),0.0001);
        assertEquals(est.startLog2Prob("a"),est.startLog2Prob(idA),0.0001);
        assertEquals(est.startLog2Prob("b"),est.startLog2Prob(idB),0.0001);

        HiddenMarkovModel est2 
            = (HiddenMarkovModel) AbstractExternalizable.compile(est);
        assertEquals(0.75,est2.startProb("a"),0.001);
        assertEquals(0.25,est2.startProb("b"), 0.001);
        assertEquals(0.0,est2.startProb("c"), 0.001);
    
        assertEquals(est2.startProb("a"),est2.startProb(idA),0.0001);
        assertEquals(est2.startProb("b"),est2.startProb(idB),0.0001);
        assertEquals(est2.startLog2Prob("a"),est2.startLog2Prob(idA),0.0001);
        assertEquals(est2.startLog2Prob("b"),est2.startLog2Prob(idB),0.0001);
    
        est = new HmmCharLmEstimator();
        String[] tags1 = new String[] { "a", "b", "c" };
        String[] tags2 = new String[] { "a", "b", "d" };
        String[] tags3 = new String[] { "a", "a", "c" };
        String[] tags4 = new String[] { "b", "a", "a" };
        String[] toks = new String[] { "1", "2", "3" };
        handle(est,toks,null,tags1);
        handle(est,toks,null,tags2);
        handle(est,toks,null,tags3);
        handle(est,toks,null,tags4);

        assertEquals(0.75,est.startProb("a"),0.001);
        assertEquals(0.25,est.startProb("b"), 0.001);
        assertEquals(0.0,est.startProb("c"), 0.001);

        assertEquals(com.aliasi.util.Math.log2(est.startProb("a")),
                     est.startLog2Prob("a"),0.0001);
        assertEquals(com.aliasi.util.Math.log2(est.startProb("b")),
                     est.startLog2Prob("b"),0.0001);

        est2 = (HiddenMarkovModel) AbstractExternalizable.compile(est);
        assertEquals(0.75,est2.startProb("a"),0.001);
        assertEquals(0.25,est2.startProb("b"), 0.001);
        assertEquals(0.0,est2.startProb("c"), 0.001);

        assertEquals(com.aliasi.util.Math.log2(est2.startProb("a")),
                     est2.startLog2Prob("a"),0.0001);
        assertEquals(com.aliasi.util.Math.log2(est2.startProb("b")),
                     est2.startLog2Prob("b"),0.0001);

    }

    @Test
    public void testEnd() throws IOException, ClassNotFoundException {
        HmmCharLmEstimator est = new HmmCharLmEstimator(5,256,4.0);    
        est.trainEnd("a");
        est.trainEnd("a");
        est.trainEnd("b");
        est.trainEnd("a");

        assertEquals(0.75,est.endProb("a"),0.001);
        assertEquals(0.25,est.endProb("b"), 0.001);
        assertEquals(0.0,est.endProb("c"), 0.001);

        int idA = est.stateSymbolTable().symbolToID("a");
        int idB = est.stateSymbolTable().symbolToID("b");

        assertEquals(est.endProb("a"),est.endProb(idA),0.0001);
        assertEquals(est.endProb("b"),est.endProb(idB),0.0001);
        assertEquals(est.endLog2Prob("a"),est.endLog2Prob(idA),0.0001);
        assertEquals(est.endLog2Prob("b"),est.endLog2Prob(idB),0.0001);

        HiddenMarkovModel est2 
            = (HiddenMarkovModel) AbstractExternalizable.compile(est);
        assertEquals(0.75,est2.endProb("a"),0.001);
        assertEquals(0.25,est2.endProb("b"), 0.001);
        assertEquals(0.0,est2.endProb("c"), 0.001);

        assertEquals(est2.endProb("a"),est2.endProb(idA),0.0001);
        assertEquals(est2.endProb("b"),est2.endProb(idB),0.0001);
        assertEquals(est2.endLog2Prob("a"),est2.endLog2Prob(idA),0.0001);
        assertEquals(est2.endLog2Prob("b"),est2.endLog2Prob(idB),0.0001);

        assertEquals(com.aliasi.util.Math.log2(est.endProb("a")),
                     est.endLog2Prob("a"),0.0001);
        assertEquals(com.aliasi.util.Math.log2(est.endProb("b")),
                     est.endLog2Prob("b"),0.0001);

        est = new HmmCharLmEstimator();
        String[] tags1 = new String[] { "a", "b", "c" };
        String[] tags2 = new String[] { "a", "b", "d" };
        String[] tags3 = new String[] { "a", "a", "c" };
        String[] tags4 = new String[] { "b", "a", "a" };
        String[] toks = new String[] { "1", "2", "3" };
        handle(est,toks,null,tags1);
        handle(est,toks,null,tags2);
        handle(est,toks,null,tags3);
        handle(est,toks,null,tags4);

        assertEquals(0.25,est.endProb("a"),0.001);
        assertEquals(0.0,est.endProb("b"), 0.001);
        assertEquals(0.50,est.endProb("c"), 0.001);
        assertEquals(0.25,est.endProb("d"), 0.001);
        assertEquals(0.0,est.endProb("e"), 0.001);

        est2 = (HiddenMarkovModel) AbstractExternalizable.compile(est);
        assertEquals(0.25,est2.endProb("a"),0.001);
        assertEquals(0.0,est2.endProb("b"), 0.001);
        assertEquals(0.50,est2.endProb("c"), 0.001);
        assertEquals(0.25,est2.endProb("d"), 0.001);
        assertEquals(0.0,est2.endProb("e"), 0.001);

        assertEquals(com.aliasi.util.Math.log2(est2.endProb("a")),
                     est2.endLog2Prob("a"),0.0001);
        assertEquals(com.aliasi.util.Math.log2(est2.endProb("b")),
                     est2.endLog2Prob("b"),0.0001);
    }

    @Test
    public void testTransitions() throws IOException, ClassNotFoundException {
        HmmCharLmEstimator est = new HmmCharLmEstimator(5,256,4.0);    
        String[] tags = new String[] { "a", "b", "c", "a", "d", "b" };
        String[] toks = new String[] { "1", "2", "3", "4", "5", "6" };
        handle(est,toks,null,tags);
    
        assertEquals(0.5,est.transitProb("a","b"),0.0001);
        assertEquals(0.5,est.transitProb("a","d"),0.0001);
        assertEquals(0.0,est.transitProb("a","e"),0.0001);
        assertEquals(0.5,est.transitProb("b","c"),0.0001);

        HiddenMarkovModel est2 
            = (HiddenMarkovModel) AbstractExternalizable.compile(est);

        assertEquals(0.5,est2.transitProb("a","b"),0.0001);
        assertEquals(0.5,est2.transitProb("a","d"),0.0001);
        assertEquals(0.0,est2.transitProb("a","e"),0.0001);
        assertEquals(0.5,est2.transitProb("b","c"),0.0001);
    }

    @Test
    public void testTransitions2() throws IOException, ClassNotFoundException {
        HmmCharLmEstimator est = new HmmCharLmEstimator(5,256,4.0);    
        String[] toks = new String[] { "a", "b", "c", "d", "e", "f", "g" };
        String[] tags = new String[] { "A", "B", "A", "A", "B", "B", "A" };
        handle(est,toks,null,tags);
    
        assertEquals(0.25,est.transitProb("A","A"),0.0001);
        assertEquals(0.5,est.transitProb("A","B"),0.0001);
    
        assertEquals(0.3333,est.transitProb("B","B"),0.0001);
        assertEquals(0.6666,est.transitProb("B","A"),0.0001);

        double[][] transitions = new double[2][2];
    
        transitions[0][0] = est.transitProb(0,0);
        transitions[0][1] = est.transitProb(0,1);
        transitions[1][0] = est.transitProb(1,0);
        transitions[1][1] = est.transitProb(1,1);
    
        int idA = est.stateSymbolTable().symbolToID("A");
        int idB = est.stateSymbolTable().symbolToID("B");
    
        assertEquals(est.transitProb("A","B"),
                     est.transitProb(idA,idB),0.0001);
        assertEquals(est.transitProb("B","A"),
                     est.transitProb(idB,idA),0.0001);

        assertEquals(est.transitLog2Prob("A","B"),
                     est.transitLog2Prob(idA,idB),0.0001);
        assertEquals(est.transitLog2Prob("B","A"),
                     est.transitLog2Prob(idB,idA),0.0001);

        HiddenMarkovModel est2 
            = (HiddenMarkovModel) AbstractExternalizable.compile(est);

        assertEquals(0.25,est2.transitProb("A","A"),0.0001);
        assertEquals(0.5,est2.transitProb("A","B"),0.0001);
    
        assertEquals(0.3333,est2.transitProb("B","B"),0.0001);
        assertEquals(0.6666,est2.transitProb("B","A"),0.0001);

        assertEquals(est.transitProb("A","B"),
                     est2.transitProb(idA,idB),0.0001);
        assertEquals(est.transitProb("B","A"),
                     est2.transitProb(idB,idA),0.0001);

        assertEquals(est2.transitLog2Prob("A","B"),
                     est2.transitLog2Prob(idA,idB),0.0001);
        assertEquals(est2.transitLog2Prob("B","A"),
                     est2.transitLog2Prob(idB,idA),0.0001);

        assertEquals(est.transitLog2Prob("A","B"),
                     est2.transitLog2Prob("A","B"),0.0001);

        assertEquals(est.transitLog2Prob("A","B"),
                     com.aliasi.util.Math.log2(est.transitProb("A","B")),
                     0.0001);

        assertEquals(est2.transitLog2Prob("A","B"),
                     com.aliasi.util.Math.log2(est2.transitProb("A","B")),
                     0.0001);

    }

    @Test
    public void testEmissions() throws IOException, ClassNotFoundException {
        HmmCharLmEstimator est = new HmmCharLmEstimator(5,256,0.01);    
        String[] toks = new String[] { "John", "likes", "Mary" };
        String[] tags = new String[] { "N", "V", "N" };
        handle(est,toks,null,tags);
        assertTrue(est.emitProb("N","John") > est.emitProb("N","xxxxx"));
        assertTrue(est.emitProb("N","John") > est.emitProb("N","Jon"));
        assertTrue(est.emitProb("N","John") == est.emitProb("N","Mary"));
        assertTrue(est.emitProb("V","liked") == est.emitProb("V","liker"));

        assertEquals(com.aliasi.util.Math.log2(est.emitProb("V","liked")),
                     est.emitLog2Prob("V","liked"), 0.0001);

        int idN = est.stateSymbolTable().symbolToID("N");
        int idV = est.stateSymbolTable().symbolToID("V");

        assertEquals(est.emitLog2Prob(idV,"liked"),
                     est.emitLog2Prob("V","liked"), 0.0001);
             
        assertEquals(est.emitProb("N","John"),
                     est.emitProb(idN,"John"),0.0001);
        assertEquals(est.emitProb("V","foo"),
                     est.emitProb(idV,"foo"),0.0001);

        double V_lakes = est.emitProb("V","lakes");
        double V_likes = est.emitProb("V","likes");
        double V_laks = est.emitProb("V","laks");
        double V_like = est.emitProb("V","like");
        assertTrue(V_likes > V_lakes);
        assertTrue(V_likes > V_laks);
        assertTrue(V_likes > V_like);

        HiddenMarkovModel est2 
            = (HiddenMarkovModel) AbstractExternalizable.compile(est);
        assertTrue(est2.emitProb("N","John") > est2.emitProb("N","xxxxx"));
        assertTrue(est2.emitProb("N","John") > est2.emitProb("N","Jon"));
        assertTrue(est2.emitProb("N","John") == est2.emitProb("N","Mary"));
        assertTrue(est2.emitProb("V","liked") == est2.emitProb("V","liker"));

        V_lakes = est2.emitProb("V","lakes");
        V_likes = est2.emitProb("V","likes");
        V_laks = est2.emitProb("V","laks");
        V_like = est2.emitProb("V","like");
        assertTrue(V_likes > V_lakes);
        assertTrue(V_likes > V_laks);
        assertTrue(V_likes > V_like);

        assertEquals(com.aliasi.util.Math.log2(est2.emitProb("V","liked")),
                     est2.emitLog2Prob("V","liked"), 0.0001);

        /*
          assertEquals(est2.emitProb("N","John"),
          est2.emitProb(idN,"John"),0.0001);
          assertEquals(est2.emitProb("V","foo"),
          est2.emitProb(idV,"foo"),0.0001);
        */
    }
}
