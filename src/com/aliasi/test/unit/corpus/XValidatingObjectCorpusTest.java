package com.aliasi.test.unit.corpus;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class XValidatingObjectCorpusTest {
    
    @Test
    public void testView() throws IOException {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(2);
        corpus.handle("a");
        corpus.handle("b");
        corpus.handle("c");
        corpus.handle("d");
        assertTest(corpus,"a","b");
        assertTrain(corpus,"c","d");

        XValidatingObjectCorpus<String> view
            = corpus.itemView();
        view.setFold(1);
        assertTest(view,"c","d");
        assertTrain(view,"a","b");
    }

    void assertTest(Corpus<ObjectHandler<String>> corpus,
                    String... expectedVals) throws IOException {
        final List<String> vals = new ArrayList<String>();
        corpus.visitTest(new ObjectHandler<String>() {
                             public void handle(String s) {
                                 vals.add(s);
                             }
                         });
        assertEquals(Arrays.asList(expectedVals), vals);
    }

    void assertTrain(Corpus<ObjectHandler<String>> corpus,
                    String... expectedVals) throws IOException {
        final List<String> vals = new ArrayList<String>();
        corpus.visitTrain(new ObjectHandler<String>() {
                              public void handle(String s) {
                                  vals.add(s);
                              }
                          });
        assertEquals(Arrays.asList(expectedVals), vals);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testViewEx1() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(2);
        corpus.handle("a");
        corpus.handle("b"); // need two items for exception!

        XValidatingObjectCorpus<String> view1
            = corpus.itemView();
        view1.permuteCorpus(new Random());

        // List<String> xs = java.util.Collections.unmodifiableList(Arrays.asList("a","b"));
        // java.util.Collections.shuffle(xs, new Random());
        // System.out.println(xs);
    }


    @Test(expected=UnsupportedOperationException.class)
    public void testViewEx2() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(2);
        corpus.handle("a");

        XValidatingObjectCorpus<String> view1
            = corpus.itemView();
        view1.handle("b");
    }

    @Test
    public void testSerializable() throws IOException {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(2);
        corpus.handle("a");
        corpus.handle("b");
        corpus.handle("c");
        corpus.handle("d");
        corpus.permuteCorpus(new Random());

        @SuppressWarnings("unchecked") // req for serialization
        XValidatingObjectCorpus<String> corpus2
            = (XValidatingObjectCorpus<String>)
            AbstractExternalizable.serializeDeserialize(corpus);

        final List<String> xs1 = corpusToString(corpus);
        final List<String> xs2 = corpusToString(corpus2);
        assertEquals(xs1,xs2);
        
    }

    static List<String> corpusToString(XValidatingObjectCorpus<String> corpus) {
        final List<String> xs = new ArrayList<String>();
        corpus.visitCorpus(new ObjectHandler<String>() {
                               public void handle(String s) {
                                   xs.add(s);
                               }
                           });
        return xs;
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsEx() {
        new XValidatingObjectCorpus<String>(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNumFoldsExc() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus(12);
        corpus.setNumFolds(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetFoldExc1() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus(12);
        corpus.setFold(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetFoldExc2() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus(12);
        corpus.setFold(12);
    }

    @Test
    public void testPermute() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(3);
        corpus.handle("a");
        corpus.handle("b");
        corpus.handle("c");
        
        Random random = new Random();
        corpus.permuteCorpus(random);
        Collector collector = new Collector();
        corpus.visitCorpus(collector);
        assertEquals(new HashSet<String>(Arrays.asList("a","b","c")),
                     new HashSet<String>(collector.mItems));
    }

    @Test
    public void testAllZeroFolds() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(0);
        corpus.handle("a");
        corpus.handle("b");
        corpus.handle("c");
        
        Random random = new Random();
        corpus.permuteCorpus(random);
        Collector collector = new Collector();
        corpus.visitTrain(collector);
        assertEquals(new HashSet<String>(Arrays.asList("a","b","c")),
                     new HashSet<String>(collector.mItems));
        Collector collector2 = new Collector();
        corpus.visitTest(collector2);
        assertEquals(0,collector2.mItems.size());
    }

    @Test
    public void testFolds() {
        XValidatingObjectCorpus<String> corpus
            = new XValidatingObjectCorpus<String>(3);
        corpus.handle("a");
        corpus.handle("b");
        corpus.handle("c");
        corpus.handle("d");
        corpus.handle("e");
        corpus.handle("f");

        assertTrainTest(corpus,0,
                        Arrays.asList("c","d","e","f"),
                        Arrays.asList("a","b"));

        assertTrainTest(corpus,1,
                        Arrays.asList("a","b","e","f"),
                        Arrays.asList("c","d"));
        
        assertTrainTest(corpus,2,
                        Arrays.asList("a","b","c","d"),
                        Arrays.asList("e","f"));
    }

    void assertTrainTest(XValidatingObjectCorpus<String> corpus,
                         int fold,
                         List<String> trainCases,
                         List<String> testCases) {
        corpus.setFold(fold);

        Collector collector = new Collector();
        corpus.visitTrain(collector);
        assertEquals(trainCases,collector.mItems);

        Collector collector2 = new Collector();
        corpus.visitTest(collector2);
        assertEquals(testCases,collector2.mItems);
    }

    static class Collector implements ObjectHandler<String> {
        List<String> mItems = new ArrayList<String>();
        public void handle(String s) {
            mItems.add(s);
        }
    }

}


