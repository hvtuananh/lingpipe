package com.aliasi.test.unit.classify;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConditionalClassifier;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static com.aliasi.test.unit.Asserts.succeed;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

public class TradNaiveBayesClassifierTest {

    static final String[] CATS_2 = new String[] { "a", "b" };
    static final Set<String> CAT_SET_2 = listToSet(CATS_2);
    static final TokenizerFactory TOKENIZER_FACTORY
        = IndoEuropeanTokenizerFactory.INSTANCE;

    @Test
    public void testSetLengthNorm() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,10.0);
        assertEquals(10.0,classifier.lengthNorm(),0.0001);
        classifier.setLengthNorm(Double.NaN);
        assertTrue(Double.isNaN(classifier.lengthNorm()));

        classifier.setLengthNorm(20.0);
        assertEquals(20.0,classifier.lengthNorm(),0.0001);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLengthNormNegExc() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLengthNormNegExc2() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,10);
        classifier.setLengthNorm(Double.POSITIVE_INFINITY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLengthNormInfExc() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,Double.POSITIVE_INFINITY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLengthNormInfExc2() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,10.0);
        classifier.setLengthNorm(Double.POSITIVE_INFINITY);
    }




    @Test
    public void testTrain() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,Double.NaN);

        try {
            classifier.train("", new Classification("a"), -1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        classifier.train("john ran", new Classification("a"), 2);
        classifier.train("john ran", new Classification("a"), -1);
        classifier.train("john ran", new Classification("a"), -1);
        classifier.train("run, mary, run", new Classification("b"),3);
        classifier.train("john jumped", new Classification("a"),3);

        try {
            classifier.train("john ran", new Classification("a"), -1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        try {
            classifier.train("mary hopscotched", new Classification("a"), -1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

        classifier.train("john ran",new Classification("a"),3);
        classifier.train("mary ran", new Classification("b"),3);

        classifier.train("john ran",new Classification("a"),-2);
        classifier.train("john jumped", new Classification("a"),-2);
        classifier.train("mary ran", new Classification("b"),-2);
        classifier.train("run, mary, run", new Classification("b"),-2);

        classifier.setLengthNorm(10.0); // shouldn't actually matter here

        assertEquals((2.0 + 1.0)/(4.0 + 6.0 * 1.0), classifier.probToken("john","a"),0.001);
        assertEquals((1.0 + 1.0)/(7.0 + 6.0 * 1.0), classifier.probToken("ran","b"),0.001);
        assertEquals((0.0 + 1.0)/(4.0 + 6.0 * 1.0), classifier.probToken("run","a"),0.001);

        assertEquals(0.5, classifier.probCat("a"), 0.001);
        assertEquals(0.5, classifier.probCat("b"), 0.001);
    }

    @Test
    public void testLengthNormStore() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(new String[] { "a", "b" }),
                                           TOKENIZER_FACTORY,
                                           1,1,42.0);
        assertEquals(42.0,classifier.lengthNorm(),0.0001);

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        assertEquals(42.0,classifier2.lengthNorm(),0.0001);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCatsEmpty() {
        new TradNaiveBayesClassifier(listToSet(new String[] { }),
                                     TOKENIZER_FACTORY,
                                     1,1,1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCats1() {
        String[] cats = new String[] { "foo" };
        new TradNaiveBayesClassifier(listToSet(cats),
                                     TOKENIZER_FACTORY,
                                     1,1,1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegCatPrior() {
        new TradNaiveBayesClassifier(CAT_SET_2,
                                     TOKENIZER_FACTORY,
                                     -1,1,1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNaNCatPrior() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,
                                           TOKENIZER_FACTORY,
                                           Double.NaN,1,1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfCatPrior() {
        new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                     Double.POSITIVE_INFINITY,1,1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegWordPrior() {
        new TradNaiveBayesClassifier(CAT_SET_2,
                                     TOKENIZER_FACTORY,
                                     1,-1,1.0);
    }

    @Test
    public void testCatSet() {
        String[] cats = new String[] { "a", "c", "d", "b" };
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(listToSet(cats),TOKENIZER_FACTORY,
                                           1,1,1.0);
        assertEquals(new HashSet<String>(Arrays.asList(cats)),
                     classifier.categorySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLengthNormInf() {
        new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                     1,1,Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLengthNormZero() {
        new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                     1,1,0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLengthNormNeg() {
        new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                     1,1,-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownCatTrainException() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        classifier.handle(new Classified<CharSequence>("any old string",
                                                       new Classification("unknownCat")));
    }

    @Test
    public void knownTokenTest() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        classifier.handle(new Classified<CharSequence>("john ran",new Classification("a")));
        assertTrue(classifier.isKnownToken("john"));
        assertTrue(classifier.isKnownToken("ran"));
        assertFalse(classifier.isKnownToken("unknownTok"));
    }

    static void handle(TradNaiveBayesClassifier classifier,
                       String input, 
                       Classification c) {
        classifier.handle(new Classified<CharSequence>(input,c));
    }

    @Test
    public void log2CaseProbTest() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        classifier.handle(new Classified<CharSequence>("john ran",new Classification("a")));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));

        assertEquals(com.aliasi.util.Math.log2(classifier.probCat("a") * classifier.probToken("mary","a") * classifier.probToken("jumped","a")
                                               + classifier.probCat("b") * classifier.probToken("mary","b") * classifier.probToken("jumped","b")),
                     classifier.log2CaseProb("mary jumped"),
                     0.001);
        assertEquals(com.aliasi.util.Math.log2(classifier.probCat("a") + classifier.probCat("b")), // should be 1.0
                     classifier.log2CaseProb(""), // normalized by length, should also be 1.0
                     0.0001);
    }

    /*
    @Test
    public void log2ModelProbTest() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));

        assertEquals(com.aliasi.util.Math.log2(classifier.probCat("a") + classifier.probCat("b")), // should be 1.0
                     classifier.log2CaseProb(""), // normalized by length, should also be 1.0
                     0.0001);
    }
    */


    @Test
    public void probTokenTest() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));

        assertEquals((2.0 + 1.0)/(4.0 + 6.0 * 1.0), classifier.probToken("john","a"),0.001);
        assertEquals((1.0 + 1.0)/(7.0 + 6.0 * 1.0), classifier.probToken("ran","b"),0.001);
        assertEquals((0.0 + 1.0)/(4.0 + 6.0 * 1.0), classifier.probToken("run","a"),0.001);

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        assertEquals((2.0 + 1.0)/(4.0 + 6.0 * 1.0), classifier2.probToken("john","a"),0.001);
        assertEquals((1.0 + 1.0)/(7.0 + 6.0 * 1.0), classifier2.probToken("ran","b"),0.001);
        assertEquals((0.0 + 1.0)/(4.0 + 6.0 * 1.0), classifier2.probToken("run","a"),0.001);

        assertEquals(classifier.categorySet(), classifier2.categorySet());
        assertEquals(classifier.knownTokenSet(), classifier2.knownTokenSet());

        assertFalse(classifier2.isKnownToken("thisisoneidon'tknow"));
        for (String token : classifier.knownTokenSet()) {
            assertTrue(classifier.isKnownToken(token));
            assertTrue(classifier2.isKnownToken(token));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void probTokenTestUnknownToken() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        handle(classifier,"john ran", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));

        classifier.probToken("unknownTok","a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void probTokenTestUnknownTokenSerDeser() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        handle(classifier,"john ran", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        classifier2.probToken("unknownTok","a");
    }


    @Test(expected = IllegalArgumentException.class)
    public void probTokenTestUnknownCat() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        handle(classifier,"john ran", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));

        classifier.probToken("john","unknownCat");
    }



    @Test(expected = IllegalArgumentException.class)
    public void probTokenTestUnknownCatSerDeser() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        handle(classifier,"john ran", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        classifier2.probToken("john","unknownCat");
    }


    @Test
    public void testKnownTokenSet() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);

        Set<String> knownTokenSet = classifier.knownTokenSet();
        Set<String> expectedTokenSet = new HashSet<String>();
        assertEquals(expectedTokenSet,knownTokenSet);

        handle(classifier,"john ran", new Classification("a"));
        expectedTokenSet.add("john");
        expectedTokenSet.add("ran");
        assertEquals(expectedTokenSet,knownTokenSet);

        handle(classifier,"mary ran", new Classification("b"));
        expectedTokenSet.add("mary");
        assertEquals(expectedTokenSet,knownTokenSet);

        for (String token : knownTokenSet)
            assertTrue(classifier.isKnownToken(token));

    }

    @Test
    public void testProbCat() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"john ran and jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));

        assertEquals((3.0 + 1.0) / (5.0 + 2.0*1.0),classifier.probCat("a"),0.00001);
        assertEquals(1.0 - (3.0 + 1.0) / (5.0 + 2.0*1.0),classifier.probCat("b"),0.00001);

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        assertEquals((3.0 + 1.0) / (5.0 + 2.0*1.0),classifier2.probCat("a"),0.00001);
        assertEquals(1.0 - (3.0 + 1.0) / (5.0 + 2.0*1.0),classifier2.probCat("b"),0.00001);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testProbCatUnknownCatExc() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"john ran and jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));

        classifier.probCat("unknownCat");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testProbCatUnknownCatExcSerDeser() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,1);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"john ran and jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        classifier2.probCat("unknownCat");
    }


    @Test
    public void testClassification() {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));
        handle(classifier,"mary run", new Classification("b"));

        ConditionalClassification c = classifier.classify("");
        assertEquals(2,c.size());
        assertEquals("a",c.category(1));
        assertEquals("b",c.category(0));
        assertEquals(3.0/7.0,c.conditionalProbability(1),0.0001);
        assertEquals(4.0/7.0,c.conditionalProbability(0),0.0001);

        ConditionalClassification c2 = classifier.classify("backbends");
        assertEquals(2,c2.size());
        assertEquals("a",c2.category(1));
        assertEquals("b",c2.category(0));
        assertEquals(3.0/7.0,c2.conditionalProbability(1),0.0001);
        assertEquals(4.0/7.0,c2.conditionalProbability(0),0.0001);

        ConditionalClassification c3 = classifier.classify("john");
        assertEquals("a",c3.category(0));
        assertEquals("b",c3.category(1));
        double z3 = 9.0/70.0 + 4.0/105.0;
        assertEquals(9.0/70.0 / z3,c3.conditionalProbability(0),0.0001);
        assertEquals(4.0/105.0 / z3,c3.conditionalProbability(1),0.0001);

        ConditionalClassification c4 = classifier.classify("john smith was here");
        assertEquals("a",c4.category(0));
        assertEquals("b",c4.category(1));
        double z4 = 9.0/70.0 + 4.0/105.0;
        assertEquals(9.0/70.0 / z4,c4.conditionalProbability(0),0.0001);
        assertEquals(4.0/105.0 / z4,c4.conditionalProbability(1),0.0001);

        ConditionalClassification c5 = classifier.classify("john saw mary");
        assertEquals("a",c5.category(0));
        assertEquals("b",c5.category(1));
        double z5 = 9.0/700.0 + 16.0/1575.0;
        assertEquals(9.0/700.0/z5,c5.conditionalProbability(0),0.0001);
        assertEquals(16.0/1575.0/z5,c5.conditionalProbability(1),0.0001);
    }


    @Test
    public void testClassificationSerDeser() throws IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));
        handle(classifier,"mary run", new Classification("b"));

        TradNaiveBayesClassifier classifier2
            = (TradNaiveBayesClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        ConditionalClassification c = classifier2.classify("");
        assertEquals(2,c.size());
        assertEquals("a",c.category(1));
        assertEquals("b",c.category(0));
        assertEquals(3.0/7.0,c.conditionalProbability(1),0.0001);
        assertEquals(4.0/7.0,c.conditionalProbability(0),0.0001);

        ConditionalClassification c2 = classifier2.classify("backbends");
        assertEquals(2,c2.size());
        assertEquals("a",c2.category(1));
        assertEquals("b",c2.category(0));
        assertEquals(3.0/7.0,c2.conditionalProbability(1),0.0001);
        assertEquals(4.0/7.0,c2.conditionalProbability(0),0.0001);

        ConditionalClassification c3 = classifier2.classify("john");
        assertEquals("a",c3.category(0));
        assertEquals("b",c3.category(1));
        double z3 = 9.0/70.0 + 4.0/105.0;
        assertEquals(9.0/70.0 / z3,c3.conditionalProbability(0),0.0001);
        assertEquals(4.0/105.0 / z3,c3.conditionalProbability(1),0.0001);

        ConditionalClassification c4 = classifier2.classify("john smith was here");
        assertEquals("a",c4.category(0));
        assertEquals("b",c4.category(1));
        double z4 = 9.0/70.0 + 4.0/105.0;
        assertEquals(9.0/70.0 / z4,c4.conditionalProbability(0),0.0001);
        assertEquals(4.0/105.0 / z4,c4.conditionalProbability(1),0.0001);

        ConditionalClassification c5 = classifier2.classify("john saw mary");
        assertEquals("a",c5.category(0));
        assertEquals("b",c5.category(1));
        double z5 = 9.0/700.0 + 16.0/1575.0;
        assertEquals(9.0/700.0/z5,c5.conditionalProbability(0),0.0001);
        assertEquals(16.0/1575.0/z5,c5.conditionalProbability(1),0.0001);
    }


    @Test
    public void testClassificationCompile() throws ClassNotFoundException, IOException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));
        handle(classifier,"mary run", new Classification("b"));

        ConditionalClassifier<CharSequence> classifier2
            = (ConditionalClassifier<CharSequence>)
            AbstractExternalizable.compile(classifier);

        ConditionalClassification c = classifier2.classify("");
        assertEquals(2,c.size());
        assertEquals("a",c.category(1));
        assertEquals("b",c.category(0));
        assertEquals(3.0/7.0,c.conditionalProbability(1),0.0001);
        assertEquals(4.0/7.0,c.conditionalProbability(0),0.0001);

        ConditionalClassification c2 = classifier2.classify("backbends");
        assertEquals(2,c2.size());
        assertEquals("a",c2.category(1));
        assertEquals("b",c2.category(0));
        assertEquals(3.0/7.0,c2.conditionalProbability(1),0.0001);
        assertEquals(4.0/7.0,c2.conditionalProbability(0),0.0001);

        ConditionalClassification c3 = classifier2.classify("john");
        assertEquals("a",c3.category(0));
        assertEquals("b",c3.category(1));
        double z3 = 9.0/70.0 + 4.0/105.0;
        assertEquals(9.0/70.0 / z3,c3.conditionalProbability(0),0.0001);
        assertEquals(4.0/105.0 / z3,c3.conditionalProbability(1),0.0001);

        ConditionalClassification c4 = classifier2.classify("john smith was here");
        assertEquals("a",c4.category(0));
        assertEquals("b",c4.category(1));
        double z4 = 9.0/70.0 + 4.0/105.0;
        assertEquals(9.0/70.0 / z4,c4.conditionalProbability(0),0.0001);
        assertEquals(4.0/105.0 / z4,c4.conditionalProbability(1),0.0001);

        ConditionalClassification c5 = classifier2.classify("john saw mary");
        assertEquals("a",c5.category(0));
        assertEquals("b",c5.category(1));
        double z5 = 9.0/700.0 + 16.0/1575.0;
        assertEquals(9.0/700.0/z5,c5.conditionalProbability(0),0.0001);
        assertEquals(16.0/1575.0/z5,c5.conditionalProbability(1),0.0001);
    }

    @Test
    public void testLengthNorm() throws IOException, ClassNotFoundException {
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(CAT_SET_2,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);

        // train without norm
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));
        handle(classifier,"mary run", new Classification("b"));

        // eval with norm
        classifier.setLengthNorm(10.0);

        JointClassifier<CharSequence> classifierSer
            = (JointClassifier<CharSequence>)
            AbstractExternalizable.serializeDeserialize(classifier);

        
        ConditionalClassifier<CharSequence> classifierComp
            = (ConditionalClassifier<CharSequence>)
            AbstractExternalizable.compile(classifier);

        ConditionalClassification c1 = classifier.classify("");
        assertEquals("b",c1.category(0));
        assertEquals("a",c1.category(1));
        assertEquals(4.0/7.0,c1.conditionalProbability(0),0.0001);
        assertEquals(3.0/7.0,c1.conditionalProbability(1),0.0001);

        ConditionalClassification c1c = classifierComp.classify("unknownToken");
        assertEquals("b",c1c.category(0));
        assertEquals("a",c1c.category(1));
        assertEquals(4.0/7.0,c1c.conditionalProbability(0),0.0001);
        assertEquals(3.0/7.0,c1c.conditionalProbability(1),0.0001);

        ConditionalClassification c1s = classifierSer.classify("unknownToken unknownToken");
        assertEquals("b",c1s.category(0));
        assertEquals("a",c1s.category(1));
        assertEquals(4.0/7.0,c1s.conditionalProbability(0),0.0001);
        assertEquals(3.0/7.0,c1s.conditionalProbability(1),0.0001);

        double jointA = 3.0/7.0 * java.lang.Math.pow(3.0/10.0,10.0);
        double jointB = 4.0/7.0 * java.lang.Math.pow(1.0/15.0,10.0);

        double expA = jointA / (jointA + jointB);
        double expB = jointB / (jointA + jointB);

        ConditionalClassification c2 = classifier.classify("john");
        assertEquals(2,c2.size());
        assertEquals("a",c2.category(0));
        assertEquals("b",c2.category(1));
        assertEquals(expA,c2.conditionalProbability(0),0.0001);
        assertEquals(expB,c2.conditionalProbability(1),0.0001);

        ConditionalClassification c2s = classifierSer.classify("john");
        assertEquals(2,c2s.size());
        assertEquals("a",c2s.category(0));
        assertEquals("b",c2s.category(1));
        assertEquals(expA,c2s.conditionalProbability(0),0.0001);
        assertEquals(expB,c2s.conditionalProbability(1),0.0001);

        ConditionalClassification c2c = classifierComp.classify("john");
        assertEquals(2,c2c.size());
        assertEquals("a",c2c.category(0));
        assertEquals("b",c2c.category(1));
        assertEquals(expA,c2c.conditionalProbability(0),0.0001);
        assertEquals(expB,c2c.conditionalProbability(1),0.0001);

        jointA = 3.0/7.0 * java.lang.Math.pow(3.0/10.0 * 2.0/10.0,10.0/2.0);
        jointB = 4.0/7.0 * java.lang.Math.pow(1.0/15.0 * 2.0/10.0,10.0/2.0);

        expA = jointA / (jointA + jointB);
        expB = jointB / (jointA + jointB);

        ConditionalClassification c3 = classifier.classify("john");
        assertEquals(2,c3.size());
        assertEquals("a",c3.category(0));
        assertEquals("b",c3.category(1));
        assertEquals(expA,c3.conditionalProbability(0),0.001);
        assertEquals(expB,c3.conditionalProbability(1),0.001);

        ConditionalClassification c3s = classifierSer.classify("john");
        assertEquals(2,c3s.size());
        assertEquals("a",c3s.category(0));
        assertEquals("b",c3s.category(1));
        assertEquals(expA,c3s.conditionalProbability(0),0.001);
        assertEquals(expB,c3s.conditionalProbability(1),0.001);

        ConditionalClassification c3c = classifierComp.classify("john");
        assertEquals(2,c3c.size());
        assertEquals("a",c3c.category(0));
        assertEquals("b",c3c.category(1));
        assertEquals(expA,c3c.conditionalProbability(0),0.001);
        assertEquals(expB,c3c.conditionalProbability(1),0.001);
    }


    @Test
    public void testLengthNormTernary() throws IOException, ClassNotFoundException {
        Set<String> catSet3 = new HashSet<String>(Arrays.asList(new String[] { "a", "b", "c" }));
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(catSet3,TOKENIZER_FACTORY,
                                           1,1,Double.NaN);

        // train without length norm
        handle(classifier,"john ran",new Classification("a"));
        handle(classifier,"john jumped", new Classification("a"));
        handle(classifier,"mary ran", new Classification("b"));
        handle(classifier,"run, mary, run", new Classification("b"));
        handle(classifier,"mary run", new Classification("b"));
        handle(classifier,"bill ran", new Classification("c"));

        // set it for running
        classifier.setLengthNorm(10.0);

        JointClassifier<CharSequence> classifierSer
            = (JointClassifier<CharSequence>)
            AbstractExternalizable.serializeDeserialize(classifier);

        ConditionalClassifier<CharSequence> classifierComp
            = (ConditionalClassifier<CharSequence>)
            AbstractExternalizable.compile(classifier);

        double jointA = 3.0/9.0;
        double jointB = 4.0/9.0;
        double jointC = 2.0/9.0;

        double expA = jointA/(jointA+jointB+jointC);
        double expB = jointB/(jointA+jointB+jointC);
        double expC = jointC/(jointA+jointB+jointC);

        ConditionalClassification c = classifier.classify("");
        assertEquals(3,c.size());
        assertEquals("b",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("c",c.category(2));
        assertEquals(expB,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expC,c.conditionalProbability(2),0.001);

        c = classifierSer.classify("");
        assertEquals(3,c.size());
        assertEquals("b",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("c",c.category(2));
        assertEquals(expB,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expC,c.conditionalProbability(2),0.001);

        c = classifierComp.classify("");
        assertEquals(3,c.size());
        assertEquals("b",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("c",c.category(2));
        assertEquals(expB,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expC,c.conditionalProbability(2),0.001);

        c = classifier.classify("blah blah");
        assertEquals(3,c.size());
        assertEquals("b",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("c",c.category(2));
        assertEquals(expB,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expC,c.conditionalProbability(2),0.001);

        c = classifierSer.classify("blah blah blah");
        assertEquals(3,c.size());
        assertEquals("b",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("c",c.category(2));
        assertEquals(expB,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expC,c.conditionalProbability(2),0.001);

        c = classifierComp.classify("blah blah blah blah blah");
        assertEquals(3,c.size());
        assertEquals("b",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("c",c.category(2));
        assertEquals(expB,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expC,c.conditionalProbability(2),0.001);

        jointA = 3.0/9.0 * java.lang.Math.pow(2.0/11.0, 10.0/1.0);
        jointB = 4.0/9.0 * java.lang.Math.pow(2.0/16.0, 10.0/1.0);
        jointC = 2.0/9.0 * java.lang.Math.pow(2.0/9.0, 10.0/1.0);

        expA = jointA/(jointA+jointB+jointC);
        expB = jointB/(jointA+jointB+jointC);
        expC = jointC/(jointA+jointB+jointC);

        c = classifier.classify("ran");
        assertEquals(3,c.size());
        assertEquals("c",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("b",c.category(2));
        assertEquals(expC,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expB,c.conditionalProbability(2),0.001);

        c = classifierSer.classify("ran");
        assertEquals(3,c.size());
        assertEquals("c",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("b",c.category(2));
        assertEquals(expC,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expB,c.conditionalProbability(2),0.001);

        c = classifierComp.classify("ran");
        assertEquals(3,c.size());
        assertEquals("c",c.category(0));
        assertEquals("a",c.category(1));
        assertEquals("b",c.category(2));
        assertEquals(expC,c.conditionalProbability(0),0.001);
        assertEquals(expA,c.conditionalProbability(1),0.001);
        assertEquals(expB,c.conditionalProbability(2),0.001);


        jointA = 3.0/9.0 * java.lang.Math.pow((1.0/11.0) * (1.0/11.0), 10.0/2.0);
        jointB = 4.0/9.0 * java.lang.Math.pow((1.0/16.0) * (4.0/16.0), 10.0/2.0);
        jointC = 2.0/9.0 * java.lang.Math.pow((2.0/9.0) * (1.0/9.0), 10.0/2.0);

        expA = jointA/(jointA+jointB+jointC);
        expB = jointB/(jointA+jointB+jointC);
        expC = jointC/(jointA+jointB+jointC);

        c = classifier.classify("bill run");
        assertEquals(3,c.size());
        assertEquals("c",c.category(0));
        assertEquals("b",c.category(1));
        assertEquals("a",c.category(2));
        assertEquals(expC,c.conditionalProbability(0),0.001);
        assertEquals(expB,c.conditionalProbability(1),0.001);
        assertEquals(expA,c.conditionalProbability(2),0.001);

        c = classifierSer.classify("bill run");
        assertEquals(3,c.size());
        assertEquals("c",c.category(0));
        assertEquals("b",c.category(1));
        assertEquals("a",c.category(2));
        assertEquals(expC,c.conditionalProbability(0),0.001);
        assertEquals(expB,c.conditionalProbability(1),0.001);
        assertEquals(expA,c.conditionalProbability(2),0.001);

        c = classifierComp.classify("bill run");
        assertEquals(3,c.size());
        assertEquals("c",c.category(0));
        assertEquals("b",c.category(1));
        assertEquals("a",c.category(2));
        assertEquals(expC,c.conditionalProbability(0),0.001);
        assertEquals(expB,c.conditionalProbability(1),0.001);
        assertEquals(expA,c.conditionalProbability(2),0.001);
    }

    @Test
    public void testNormTrain() {
        Set<String> catSet2 = new HashSet<String>(Arrays.asList(new String[] { "A", "B" }));
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(catSet2,TOKENIZER_FACTORY,
                                           10.0, 0.5, 1.0);

        classifier.train("aa aa bb bb", new Classification("A"), 1.0);
        classifier.train("aa aa aa", new Classification("A"), 1.0);
        classifier.train("bb bb bb", new Classification("B"), 1.0);
        classifier.train("bb cc cc bb", new Classification("B"), 1.0);
        classifier.train("bb bb bb dd", new Classification("B"), 1.0);

        assertEquals((2.0 + 10.0)/(5.0 + 2.0*10.0), classifier.probCat("A"), 0.0001);
        assertEquals((3.0 + 10.0)/(5.0 + 2.0*10.0), classifier.probCat("B"), 0.0001);

        assertEquals(2.0/4.0, classifier.probToken("aa","A"), 0.0001);
        assertEquals(1.0/4.0, classifier.probToken("bb","A"), 0.0001);
        assertEquals(0.5/4.0, classifier.probToken("cc","A"), 0.0001);
        assertEquals(0.5/4.0, classifier.probToken("dd","A"), 0.0001);

        assertEquals(0.5/5.0, classifier.probToken("aa","B"), 0.0001);
        assertEquals(2.75/5.0, classifier.probToken("bb","B"), 0.0001);
        assertEquals(1.0/5.0, classifier.probToken("cc","B"), 0.0001);
        assertEquals(0.75/5.0, classifier.probToken("dd","B"), 0.0001);

    }

    @Test
    public void testTrainConditional() {
        Set<String> catSet2 = new HashSet<String>(Arrays.asList(new String[] { "A", "B" }));
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(catSet2,TOKENIZER_FACTORY,
                                           0.5, 0.5, Double.NaN);
        ConditionalClassification c1
            = new ConditionalClassification(new String[] { "A", "B" },
                                            new double[] { .75, .25 });
        classifier.trainConditional("aa", c1, 4.0, 0.0);

        ConditionalClassification c2
            = new ConditionalClassification(new String[] { "B", "A" },
                                            new double[] { 0.9, 0.1 });
        classifier.trainConditional("bb",c2, 4.0, 0.0);

        assertEquals((3.4+0.5)/(8.0 + 2.0*0.5),classifier.probCat("A"),0.0001);
        assertEquals((4.6+0.5)/(8.0 + 2.0*0.5),classifier.probCat("B"),0.0001);

        // A:aa:3
        // B:aa:1


        assertEquals(3.5/4.4,classifier.probToken("aa","A"),0.0001);
        assertEquals(0.9/4.4,classifier.probToken("bb","A"),0.0001);
        assertEquals(1.5/5.6,classifier.probToken("aa","B"),0.0001);
        assertEquals(4.1/5.6,classifier.probToken("bb","B"),0.0001);
    }


    static Set<String> listToSet(String[] xs) {
        return new HashSet<String>(Arrays.asList(xs));
    }




}
