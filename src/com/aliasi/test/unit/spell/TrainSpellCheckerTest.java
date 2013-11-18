package com.aliasi.test.unit.spell;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static com.aliasi.test.unit.Asserts.succeed;

import java.io.IOException;


public class TrainSpellCheckerTest  {

    @Test
    public void testNullTF() throws IOException, ClassNotFoundException {
        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance distance = new FixedWeightEditDistance(1,1,1,1,1);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,distance,null);
        lm.handle("A dog ran down the street.");
        Object o = AbstractExternalizable.compile(trainer);
        assertNotNull(o);
    }

    @Test
    public void testEx() {
        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance distance = new FixedWeightEditDistance(1,1,1,1,1);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,distance,IndoEuropeanTokenizerFactory.INSTANCE);
        try {
            trainer.train("tra la la",-1);
            fail();
        } catch (IllegalArgumentException e) {
            succeed();
        }

    }

    @Test
    public void testSerialize() throws Exception {
        NGramProcessLM lm = new NGramProcessLM(5);
        WeightedEditDistance distance = new FixedWeightEditDistance(1,1,1,1,1);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,distance,IndoEuropeanTokenizerFactory.INSTANCE);
        trainer.handle("tra la la");
        trainer.handle("fa do");
        trainer.handle("do do");
        TrainSpellChecker trainer2
            = (TrainSpellChecker) AbstractExternalizable.serializeDeserialize(trainer);

        assertEquals(trainer.numTrainingChars(), trainer2.numTrainingChars());
        assertEquals(trainer.tokenCounter().keySet(), trainer2.tokenCounter().keySet());
        for (String key : trainer.tokenCounter().keySet())
            assertEquals(trainer.tokenCounter().getCount(key),
                         trainer2.tokenCounter().getCount(key));

        WeightedEditDistance distance2 = trainer2.editDistance();
        assertEquals(distance.deleteWeight('a'), distance2.deleteWeight('a'));
        assertEquals(distance.transposeWeight('e','1'),
                     distance2.transposeWeight('e','1'));
        assertEquals(distance.substituteWeight('F','&'),
                     distance2.substituteWeight('F','&'));
        assertEquals(distance.matchWeight('-'),
                     distance2.matchWeight('-'));

        NGramProcessLM lm2 = trainer2.languageModel();
        assertEquals(lm.log2Estimate("foo bar"),
                     lm2.log2Estimate("foo bar"),
                     0.0001);
    }

}
