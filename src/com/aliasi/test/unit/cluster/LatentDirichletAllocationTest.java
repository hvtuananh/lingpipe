package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import java.util.Random;

public class LatentDirichletAllocationTest  {


    @Test
    public void testSerializer() throws IOException, ClassNotFoundException {
        double docTopicPrior = 0.1;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.70, .25, .05 },  // topic 0 = rivers
            { 0.05, .50, .45},  // topic 1 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);
        LatentDirichletAllocation ldaDeser
            = (LatentDirichletAllocation)
            AbstractExternalizable.serializeDeserialize(lda);
        
        assertEquals(lda.documentTopicPrior(), ldaDeser.documentTopicPrior(), 0.00001);
        assertEquals(lda.numTopics(), ldaDeser.numTopics());
        assertEquals(lda.numWords(), ldaDeser.numWords());
        for (int topic = 0; topic < lda.numTopics(); ++topic)
            for (int word = 0; word < lda.numWords(); ++word)
                assertEquals(lda.wordProbability(topic,word),
                             ldaDeser.wordProbability(topic,word),
                             0.00001);
    }

    @Test
    public void testTopicSampler() {
        double docTopicPrior = 0.1;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.70, .25, .05 },  // topic 0 = rivers
            { 0.05, .50, .45},  // topic 1 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);

        int[] doc1 = new int[] { 0, 0, 1, 0, 1, 0 };
        int[] doc2 = new int[] { 2, 1, 1, 2, 2, 2, 1, 2, 2, 2 };
        int[] doc3 = new int[] { 2, 2, 2, 0, 0, 2, 2, 1, 1 };

        assertTopicSamples(doc1,lda);
        assertTopicSamples(doc2,lda);
        assertTopicSamples(doc3,lda);
    }

    void assertTopicSamples(int[] doc, LatentDirichletAllocation lda) {
        short[][] samples
            = lda.sampleTopics(doc,20,500,500,new Random());

        for (int i = 0; i < samples.length; ++i) {
            short[] sample = samples[i];
            for (int tok = 0; tok < sample.length; ++tok) {
                // System.out.printf(" %3d",sample[tok]);
            }
            // System.out.println();
        }
    }


    @Test
    public void testGetters() {
        double docTopicPrior = 1.0;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.7, .25, .05 },  // topic 1 = rivers
            { 0.01, .30, .69},  // topic 2 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);

        assertEquals(1.0,lda.documentTopicPrior(),0.0001);
        assertEquals(3, lda.numWords());
        assertEquals(2, lda.numTopics());
        for (int topic = 0; topic < lda.numTopics(); ++topic) {
            double[] topicWordProbsOut = lda.wordProbabilities(topic);
            for (int word = 0; word < lda.numWords(); ++word) {
                assertEquals(topicWordProbs[topic][word], topicWordProbsOut[word],
                             0.0001);
                assertEquals(topicWordProbs[topic][word],
                             lda.wordProbability(topic,word),
                             0.0001);
            }
        }
    }

    @Test
    public void testExs() {
        double docTopicPrior = 1.0;
        double[][] topicWordProbs
            = new double[][] {
            // words = { river, bank, loan }
            { 0.7, .25, .05 },  // topic 1 = rivers
            { 0.01, .30, .69},  // topic 2 = finance
        };

        LatentDirichletAllocation lda
            = new LatentDirichletAllocation(docTopicPrior,
                                            topicWordProbs);

    }

    @Test(expected=IllegalArgumentException.class)
    public void testExs1() {
        double[][] topicWordProbs
            = new double[][] { { 0.7, .25, .05 },  { 0.01, .30, .69} };
        new LatentDirichletAllocation(0,topicWordProbs);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testExs2() {
        double[][] topicWordProbs
            = new double[][] { { 0.7, .25, .05 },  { 0.01, .30, .69} };
        new LatentDirichletAllocation(-1.0,topicWordProbs);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExs3() {
        double[][] topicWordProbs
            = new double[][] { { 0.7, .25, .05 },  { 0.01, .30, .69} };
        new LatentDirichletAllocation(Double.NaN,topicWordProbs);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExs4() {
        double[][] topicWordProbs
            = new double[][] { { 0.7, .25, .05 },  { 0.01, .30, .69} };
        new LatentDirichletAllocation(Double.POSITIVE_INFINITY,topicWordProbs);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testExs5() {
        new LatentDirichletAllocation(1.0, new double[0][2]);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExs6() {
        new LatentDirichletAllocation(1.0, new double[][] { { 0.0, 1.0 }, { -.2, 0.5 } });
    }


    @Test(expected=IllegalArgumentException.class)
    public void testExs7() {
        new LatentDirichletAllocation(1.0, new double[][] { { 0.0, 1.0 }, { 0.5, 1.5 }, { 0.5, 0.5} });
    }


}
