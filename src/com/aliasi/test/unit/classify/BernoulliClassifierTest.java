package com.aliasi.test.unit.classify;

import com.aliasi.classify.BernoulliClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassification;

import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

public class BernoulliClassifierTest  {

    static final FeatureExtractor FEATURE_EXTRACTOR
        = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

    static void handle(BernoulliClassifier classifier,
                       String input, 
                       Classification c) {
        classifier.handle(new Classified<String>(input,c));
    }


    @Test
    public void testOne() {
        BernoulliClassifier classifier
            = new BernoulliClassifier(FEATURE_EXTRACTOR);

        handle(classifier,"a b",new Classification("cat1"));
        handle(classifier,"a",new Classification("cat1"));

        handle(classifier,"a b",new Classification("cat2"));
        handle(classifier,"b",new Classification("cat2"));

        JointClassification c = classifier.classify("a");

        assertEquals("cat1",c.bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);


        assertEquals("cat2",classifier.classify("b").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);


        assertEquals("cat2",classifier.classify("b b").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);

        assertEquals("cat2",classifier.classify("b foo").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);

        handle(classifier,"d",new Classification("cat1"));

        JointClassification c2 = classifier.classify("d d");
        assertEquals("cat1",c2.bestCategory());

        JointClassification c3 = classifier.classify("e");
        assertEquals("cat1",c3.bestCategory());


    }

    @Test
    public void testSer() throws IOException, ClassNotFoundException {
        BernoulliClassifier classifier
            = new BernoulliClassifier(FEATURE_EXTRACTOR);

        handle(classifier,"a b",new Classification("cat1"));
        handle(classifier,"a",new Classification("cat1"));

        handle(classifier,"a b",new Classification("cat2"));
        handle(classifier,"b",new Classification("cat2"));

        BernoulliClassifier classifier2
            = (BernoulliClassifier) AbstractExternalizable.serializeDeserialize(classifier);

        JointClassification c = classifier2.classify("a");

        assertEquals("cat1",c.bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);


        assertEquals("cat2",classifier.classify("b").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);


        assertEquals("cat2",classifier.classify("b b").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);

        assertEquals("cat2",classifier.classify("b foo").bestCategory());
        assertEquals(0.75,c.conditionalProbability(0),0.0001);
        assertEquals(0.25,c.conditionalProbability(1),0.0001);

    }

}

