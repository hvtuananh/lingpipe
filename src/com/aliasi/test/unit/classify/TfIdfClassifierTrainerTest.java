package com.aliasi.test.unit.classify;

import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifier;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class TfIdfClassifierTrainerTest  {


    @Test
    public void testTf() {
        TokenFeatureExtractor featureExtractor
            = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

        TfIdfClassifierTrainer<CharSequence> trainer
            = new TfIdfClassifierTrainer<CharSequence>(featureExtractor);
        
        handle(trainer,"a b b", "cat1");
        handle(trainer,"b c c c d", "cat2");
        handle(trainer,"c c c c c", "cat3");
        handle(trainer,"a d", "cat1");
        
        assertEquals(Math.sqrt(2.0), trainer.tf("a","cat1"));
        assertEquals(0.0, trainer.tf("c","cat1"));
        assertEquals(Math.sqrt(5.0), trainer.tf("c","cat3"));

        assertEquals(0.0, trainer.tf("foo","cat1"));
    }

    @Test
    public void testIdf() {
        TokenFeatureExtractor featureExtractor
            = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

        TfIdfClassifierTrainer<CharSequence> trainer
            = new TfIdfClassifierTrainer<CharSequence>(featureExtractor);
        
        handle(trainer,"a b b", "cat1");
        handle(trainer,"b c c c d", "cat2");
        handle(trainer,"b c c c c c", "cat3");

        assertEquals(Math.log(3.0/1.0), trainer.idf("a"));
        assertEquals(Math.log(3.0/2.0), trainer.idf("c"));
        assertEquals(Math.log(3.0/3.0), trainer.idf("b"));
        assertEquals(0.0, trainer.idf("foo"));
    }

    @Test
    public void testTfIdf() {
        TokenFeatureExtractor featureExtractor
            = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

        TfIdfClassifierTrainer<CharSequence> trainer
            = new TfIdfClassifierTrainer<CharSequence>(featureExtractor);
        
        handle(trainer,"a b b", "cat1");
        handle(trainer,"b c c c d", "cat2");
        handle(trainer,"b c c c c c", "cat3");

        assertEquals(0.0, trainer.tfIdf("foo","bar"));
        assertEquals(0.0, trainer.tfIdf("a","bar"));
        assertEquals(0.0, trainer.tfIdf("foo","cat1"));

        assertEquals(Math.sqrt(1.0), trainer.tf("a","cat1"));
        assertEquals(Math.log(3.0/1.0), trainer.idf("a"));

        assertEquals(Math.sqrt(1.0) * Math.log(3.0/1.0), trainer.tfIdf("a","cat1"));
        assertEquals(Math.sqrt(2.0) * Math.log(3.0/3.0), trainer.tfIdf("b","cat1"));
        assertEquals(0.0, trainer.tfIdf("c","cat1"));
        assertEquals(0.0, trainer.tfIdf("d","cat1"));

        assertEquals(Math.sqrt(0.0) * Math.log(3.0/1.0), trainer.tfIdf("a","cat2"));
        assertEquals(Math.sqrt(2.0) * Math.log(3.0/3.0), trainer.tfIdf("b","cat2"));
        assertEquals(Math.sqrt(3.0) * Math.log(3.0/2.0), trainer.tfIdf("c","cat2"));
        assertEquals(Math.sqrt(1.0) * Math.log(3.0/1.0), trainer.tfIdf("d","cat2"));

        assertEquals(Math.sqrt(0.0) * Math.log(3.0/1.0), trainer.tfIdf("a","cat3"));
        assertEquals(Math.sqrt(1.0) * Math.log(3.0/3.0), trainer.tfIdf("b","cat3"));
        assertEquals(Math.sqrt(5.0) * Math.log(3.0/2.0), trainer.tfIdf("c","cat3"));
        assertEquals(0.0, trainer.tfIdf("d","cat3"));
    }


    static void handle(TfIdfClassifierTrainer classifier,
                       String input, 
                       Classification c) {
        classifier.handle(new Classified<CharSequence>(input,c));
    }

    static void handle(TfIdfClassifierTrainer classifier,
                       String input, 
                       String cat) {
        handle(classifier,input, new Classification(cat));
    }


    @Test
    public void testOne() throws Exception {
        TokenFeatureExtractor featureExtractor
            = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

        TfIdfClassifierTrainer<CharSequence> trainer
            = new TfIdfClassifierTrainer<CharSequence>(featureExtractor);

        handle(trainer,"a b b", new Classification("cat1"));
        handle(trainer,"b c c c d", new Classification("cat2"));
        handle(trainer,"c c c c c", new Classification("cat3"));

        double cat1_a = Math.sqrt(1.0) * java.lang.Math.log(3.0/1.0);
        double cat1_b = Math.sqrt(2.0) * java.lang.Math.log(3.0/2.0);
        double len1 = Math.sqrt(cat1_a * cat1_a + cat1_b * cat1_b);
        double cat1_a_n = cat1_a/len1;
        double cat1_b_n = cat1_b/len1;


        double cat2_b = Math.sqrt(1.0) * java.lang.Math.log(3.0/2.0);
        double cat2_c = Math.sqrt(3.0) * java.lang.Math.log(3.0/2.0);
        double cat2_d = Math.sqrt(1.0) * java.lang.Math.log(3.0);
        double len2 = Math.sqrt(cat2_b*cat2_b + cat2_c*cat2_c + cat2_d*cat2_d);
        double cat2_b_n = cat2_b/len2;
        double cat2_c_n = cat2_c/len2;
        double cat2_d_n = cat2_d/len2;

        double cat3_c = Math.sqrt(5.0) * java.lang.Math.log(3.0/2.0);
        double len3 = Math.sqrt(cat3_c * cat3_c);
        double cat3_c_n = cat3_c/len3; // = 1.0 :-)

        ScoredClassifier<CharSequence> classifier
            = (ScoredClassifier<CharSequence>)
            AbstractExternalizable.compile(trainer);

        ScoredClassification classification
            = classifier.classify("a b b");

        assertEquals("cat1",classification.bestCategory());

        assertEquals("cat1",classification.category(0));
        assertEquals(1.0,classification.score(0),0.001);
        
        assertEquals("cat2",classification.category(1));
        assertEquals(cat1_b_n * cat2_b_n,classification.score(1),0.05); // off by 0.01

        assertEquals("cat3",classification.category(2));
        assertEquals(0.0,classification.score(2),0.001);

        TfIdfClassifierTrainer<CharSequence> trainer2
            = (TfIdfClassifierTrainer<CharSequence>)
            AbstractExternalizable.serializeDeserialize(trainer);

        ScoredClassifier<CharSequence> classifier2
            = (ScoredClassifier<CharSequence>)
            AbstractExternalizable.compile(trainer2);

        assertEquals("cat1",classification.bestCategory());

        assertEquals("cat1",classification.category(0));
        assertEquals(1.0,classification.score(0),0.001);

        assertEquals("cat2",classification.category(1));
        assertEquals(cat1_b_n * cat2_b_n,classification.score(1),0.05); // off by 0.01

        assertEquals("cat3",classification.category(2));
        assertEquals(0.0,classification.score(2),0.001);


    }

}
