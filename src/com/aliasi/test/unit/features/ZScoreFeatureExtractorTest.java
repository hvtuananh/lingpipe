package com.aliasi.test.unit.features;

import static com.aliasi.test.unit.features.MockFeatureExtractor.assertFeats;

import java.io.IOException;
import java.io.NotSerializableException;

import org.junit.Test;

import com.aliasi.classify.Classified;
import com.aliasi.classify.Classification;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.features.ZScoreFeatureExtractor;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;

public class ZScoreFeatureExtractorTest {
    
    @Test
    public void testZeros() throws IOException {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        mfe.put(1,new String[] { "A", "B" }, new double[] { 1.0, 2.0 });
        mfe.put(2,new String[] { "B", "C" }, new double[] { 1.0, 2.0 });
        mfe.put(3,new String[] { "C" , "A" }, new double[] { 1.0, 2.0 });
        MyCorpus corpus = new MyCorpus(1,2,3); // don't visit 5

        ZScoreFeatureExtractor<Integer> zsfe
            = new ZScoreFeatureExtractor<Integer>(corpus,mfe);
        
        double[] xs = new double[] { 1.0, 2.0, 0.0};
        double mean = Statistics.mean(xs);
        double dev = Statistics.standardDeviation(xs);
        assertFeats(zsfe,1,
                    new String[] { "A", "B", "C" }, 
                    new double[] { (1.0 - mean)/dev, (2.0-mean)/dev, (0.0-mean)/dev });
        assertFeats(zsfe,2,
                    new String[] { "A", "B", "C" }, 
                    new double[] { (0.0 - mean)/dev, (1.0 - mean)/dev, (2.0-mean)/dev });
        assertFeats(zsfe,3,
                    new String[] { "A", "B", "C" }, 
                    new double[] { (2.0 - mean)/dev, (0.0-mean)/dev, (1.0-mean)/dev });
    }

    @Test
    public void testCache() throws IOException {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        mfe.put(1,new String[] { "A", "B", "C" }, new double[] { 3.0, 4.0, 3.0 });
        mfe.put(2,new String[] { "A", "B", "C" }, new double[] { 7.0, 4.0, 3.0 });
        mfe.put(3,new String[] { "A", "B", "C" }, new double[] { 7.0, -4.0, 3.0 });
        mfe.put(4,new String[] { "A", "B", "C" }, new double[] { 19.0, -4.0, 3.0 });
        mfe.put(5,new String[] { "A", "B", "C", "D" }, new double[] { 3.0, 4.0, 3.0, 42.0 });

        MyCorpus corpus = new MyCorpus(1,2,3,4); // don't visit 5

        ZScoreFeatureExtractor<Integer> zsfe
            = new ZScoreFeatureExtractor<Integer>(corpus,mfe);
        
        double[] as = new double[] { 3.0, 7.0, 7.0, 19.0 };
        double[] bs = new double[] { 4.0, 4.0, -4.0, -4.0 };
        double[] cs = new double[] { 3.0, 3.0, 3.0, 3.0 };
        double meanA = Statistics.mean(as);
        double meanB = Statistics.mean(bs);
        double meanC = Statistics.mean(cs);
        double devA = Statistics.standardDeviation(as);
        double devB = Statistics.standardDeviation(bs);
        double devC = Statistics.standardDeviation(cs);


        assertFeats(zsfe,1,
                    new String[] { "A", "B" }, 
                    new double[] { (3.0 - meanA)/devA, (4.0-meanB)/devB });
        assertFeats(zsfe,2,
                    new String[] { "A", "B" }, 
                    new double[] { (7.0 - meanA)/devA, (4.0-meanB)/devB });
        assertFeats(zsfe,3,
                    new String[] { "A", "B" }, 
                    new double[] { (7.0 - meanA)/devA, (-4.0-meanB)/devB });
        assertFeats(zsfe,4,
                    new String[] { "A", "B" }, 
                    new double[] { (19.0 - meanA)/devA, (-4.0-meanB)/devB });
        assertFeats(zsfe,5,
                    new String[] { "A", "B" }, 
                    new double[] { (3.0 - meanA)/devA, (4.0-meanB)/devB });
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        SerializableMockFeatureExtractor mfe = new SerializableMockFeatureExtractor();
        mfe.put(1,new String[] { "A", "B", "C" }, new double[] { 3.0, 4.0, 3.0 });
        mfe.put(2,new String[] { "A", "B", "C" }, new double[] { 7.0, 4.0, 3.0 });
        mfe.put(3,new String[] { "A", "B", "C" }, new double[] { 7.0, -4.0, 3.0 });
        mfe.put(4,new String[] { "A", "B", "C" }, new double[] { 19.0, -4.0, 3.0 });
        mfe.put(5,new String[] { "A", "B", "C", "D" }, new double[] { 3.0, 4.0, 3.0, 42.0 });

        MyCorpus corpus = new MyCorpus(1,2,3,4); // don't visit 5

        ZScoreFeatureExtractor<Integer> zsfe
            = new ZScoreFeatureExtractor<Integer>(corpus,mfe);
        
        FeatureExtractor<Integer> zsfe2
            = (FeatureExtractor<Integer>) AbstractExternalizable.serializeDeserialize(zsfe);
        
        double[] as = new double[] { 3.0, 7.0, 7.0, 19.0 };
        double[] bs = new double[] { 4.0, 4.0, -4.0, -4.0 };
        double[] cs = new double[] { 3.0, 3.0, 3.0, 3.0 };
        double meanA = Statistics.mean(as);
        double meanB = Statistics.mean(bs);
        double meanC = Statistics.mean(cs);
        double devA = Statistics.standardDeviation(as);
        double devB = Statistics.standardDeviation(bs);
        double devC = Statistics.standardDeviation(cs);

        assertFeats(zsfe2,1,
                    new String[] { "A", 
                                   "B" }, 
                    new double[] { (3.0 - meanA)/devA, 
                                   (4.0-meanB)/devB });
        
        
    }

    @Test(expected = NotSerializableException.class)
    public void testUnSerialize() throws IOException, ClassNotFoundException {
        MockFeatureExtractor mfe = new MockFeatureExtractor();
        MyCorpus corpus = new MyCorpus();
        ZScoreFeatureExtractor<Integer> zsFe
            = new ZScoreFeatureExtractor<Integer>(corpus,mfe);
        AbstractExternalizable.serializeDeserialize(zsFe);
    }


    static class MyCorpus extends Corpus<ObjectHandler<Classified<Integer>>> {
        final Integer[] mItems;
        MyCorpus(Integer... items) {
            mItems = items;
        }
        public void visitTrain(ObjectHandler<Classified<Integer>> handler) {
            for (Integer item : mItems) {
                Classification c = new Classification("foo");
                Classified<Integer> classified = new Classified<Integer>(item,c);
                handler.handle(classified);
            }
        }
    }



}
