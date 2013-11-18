package com.aliasi.test.unit.classify;

import com.aliasi.classify.BinaryLMClassifier;

import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class BinaryLMClassifierTest  {

    @Test
    public void testNeg() {
        NGramBoundaryLM lm = new NGramBoundaryLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,10);
        classifier.train("true","Hello",1);
        classifier.train("false","Goodbye",1);
    }


    @Test
    public void testOne() {
        NGramBoundaryLM lm = new NGramBoundaryLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,10);

        for (int i = 0; i < 100; ++i)
            classifier.train("true","Kilroy was here.",1);

        // System.out.println("classification=" + classifier.classify("John Smith"));
    
        assertEquals("false",
                     classifier.classify("John Smith").bestCategory());

        assertEquals("true",
                     classifier.classify("Kilroy").bestCategory());
    
    }

    @Test(expected = UnsupportedOperationException.class) 
    public void testUnsupExc() {
        NGramBoundaryLM lm = new NGramBoundaryLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,10);
        classifier.resetCategory("true", new NGramBoundaryLM(5), 15);
    }

    @Test
    public void testTwo() {
        NGramProcessLM lm = new NGramProcessLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,12);

        assertEquals("false",
                     classifier.classify("").bestCategory());
        assertEquals("false",
                     classifier.classify("abcdefghijklmnop").bestCategory());

    
    }

    @Test
    public void testThree() {
        NGramProcessLM lm = new NGramProcessLM(5);
        BinaryLMClassifier classifier
            = new BinaryLMClassifier(lm,18);

        assertEquals("true",
                     classifier.classify("abcdefghijklmnop").bestCategory());

    }

}


