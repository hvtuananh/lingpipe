package com.aliasi.test.unit.corpus;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.ListCorpus;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ListCorpusTest {

    @Test
    public void testOne() throws IOException {
        ListCorpus<String> corpus = new ListCorpus<String>();

        List<String> trains = Arrays.asList("a","b","c");
        List<String> tests = Arrays.asList("c","d");

        for (String s : trains) corpus.addTrain(s);
        for (String s : tests) corpus.addTest(s);

        Collector trainCs = new Collector();
        Collector testCs = new Collector();
        
        corpus.visitTrain(trainCs);
        corpus.visitTest(testCs);
        assertEquals(trains,trainCs);
        assertEquals(tests,testCs);

        @SuppressWarnings("unchecked")
        Corpus<ObjectHandler<String>> corpusDeser
            = (Corpus<ObjectHandler<String>>)
            AbstractExternalizable.serializeDeserialize(corpus);
        
        
        Collector trainCs2 = new Collector();
        Collector testCs2 = new Collector();
        
        corpusDeser.visitTrain(trainCs2);
        corpusDeser.visitTest(testCs2);
        assertEquals(trains,trainCs2);
        assertEquals(tests,testCs2);
    }

    static class Collector extends ArrayList<String> implements ObjectHandler<String> {
        public void handle(String s) {
            add(s);
        }
    }

}