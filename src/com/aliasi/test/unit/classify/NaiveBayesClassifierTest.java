package com.aliasi.test.unit.classify;

import com.aliasi.classify.NaiveBayesClassifier;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class NaiveBayesClassifierTest  {

    @Test
    public void testOne() {
    NaiveBayesClassifier classifier
        = new NaiveBayesClassifier(new String[] { "a", "b", "c" },
                                   IndoEuropeanTokenizerFactory.INSTANCE);
    classifier.train("a","John Smith",1);
    classifier.train("a","John Smith",1);
    classifier.train("b","Fred Smith",1);
    classifier.train("b","Fred Smith",1);
    classifier.train("c","Fred Jones",1);
    classifier.train("c","Fred Jones",1);
    
    assertEquals("a",classifier.classify("John Smith").bestCategory());
    }

}
