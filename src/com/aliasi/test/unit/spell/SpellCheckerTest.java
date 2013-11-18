package com.aliasi.test.unit.spell;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;

import java.io.IOException;

import java.util.Iterator;

public class SpellCheckerTest  {

    @Test
    public void testCaser() throws ClassNotFoundException, IOException {
        NGramProcessLM lm = new NGramProcessLM(6,256,6);

        WeightedEditDistance caseRestoring
            = CompiledSpellChecker.CASE_RESTORING;

        assertEquals(0.0,caseRestoring.substituteWeight('a','A'),0.0005);
        assertEquals(0.0,caseRestoring.substituteWeight('A','a'),0.0005);
        assertEquals(0.0,caseRestoring.matchWeight('a'),0.0005);
        assertTrue(caseRestoring.substituteWeight('a','B')
                   == Double.NEGATIVE_INFINITY);
        assertTrue(caseRestoring.transposeWeight('a','b')
                   == Double.NEGATIVE_INFINITY);
        assertTrue(caseRestoring.insertWeight('a')
                   == Double.NEGATIVE_INFINITY);
        assertTrue(caseRestoring.deleteWeight('a')
                   == Double.NEGATIVE_INFINITY);

        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,
                                    CompiledSpellChecker.CASE_RESTORING,
                                    IndoEuropeanTokenizerFactory.INSTANCE);


        for (int i = 0; i < 1000; ++i)
            trainer.handle("abc DEF gHiJk lm");

        CompiledSpellChecker speller
            = (CompiledSpellChecker) AbstractExternalizable.compile(trainer);

        assertSpell(speller,"abc def","abc DEF");
        assertSpell(speller,"DEF ghijk","DEF gHiJk");
        assertSpell(speller,"def ghijk","DEF gHiJk");
    }


    @Test
    public void testTrain() throws ClassNotFoundException, IOException {

        NGramProcessLM lm = new NGramProcessLM(6,256,6);

        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,
                                    new FixedWeightEditDistance(0.0,
                                                                -3.0,
                                                                -2.0,
                                                                -1.0,
                                                                -1.0),
                                    IndoEuropeanTokenizerFactory.INSTANCE);


        for (int i = 0; i < 1000; ++i) {
            // trainer.handle("abracadabra ");
            trainer.handle("abracadabra abracadabra abracadabra");
        }

        CompiledSpellChecker speller
            = (CompiledSpellChecker)
            AbstractExternalizable.compile(trainer);

        assertSpell(speller,"abracadabra","abracadabra"); // match (0)

        assertSpell(speller,"ibracadabra","abracadabra"); // initial subst (1)
        assertSpell(speller,"abricadabra","abracadabra"); // internal subst (1)
        assertSpell(speller,"abracadabri","abracadabra"); // final subst (1)

        assertSpell(speller,"iabracadabra","abracadabra"); // initial delete (1)
        assertSpell(speller,"abraicadabra","abracadabra"); // internal delete (1)
        assertSpell(speller,"abracadabrai","abracadabra"); //  final delete (1)

        assertSpell(speller,"bracadabra","abracadabra"); // initial insert (1)
        assertSpell(speller,"abracdabra","abracadabra"); // internal insert (1)
        assertSpell(speller,"abracadabr","abracadabra"); // final insert (1)

        assertSpell(speller,"baracadabra","abracadabra"); // initial transpose (1)
        assertSpell(speller,"abraacdabra","abracadabra"); // internal transpose (1)
        assertSpell(speller,"abracadabar","abracadabra"); // final transpose (1)

        assertSpell(speller,"abra cadabra","abracadabra"); // merge (delete 1)

        assertSpell(speller,"abracadabraabracadabra","abracadabra abracadabra");
    }

    @Test
    public void testTokenizer() throws ClassNotFoundException, IOException {
        NGramProcessLM lm = new NGramProcessLM(6,256,6);

        WeightedEditDistance tokenizingDistance = CompiledSpellChecker.TOKENIZING;
        assertEquals(0.0,tokenizingDistance.insertWeight(' '),0.0005);
        assertEquals(0.0,tokenizingDistance.matchWeight(' '),0.0005);
        assertTrue(Double.NEGATIVE_INFINITY == tokenizingDistance.insertWeight('a'));
        assertTrue(Double.NEGATIVE_INFINITY == tokenizingDistance.deleteWeight('a'));
        assertTrue(Double.NEGATIVE_INFINITY
                   == tokenizingDistance.transposeWeight('a','b'));
        assertTrue(Double.NEGATIVE_INFINITY
                   == tokenizingDistance.substituteWeight('a','b'));


        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,tokenizingDistance,
                                    IndoEuropeanTokenizerFactory.INSTANCE);


        for (int i = 0; i < 20; ++i)
            trainer.handle("abc def ghijk lm");

        CompiledSpellChecker speller
            = (CompiledSpellChecker)
            AbstractExternalizable.compile(trainer);

        assertSpell(speller,"abcdef","abc def");
        assertSpell(speller,"ghijklm","ghijk lm");
        assertSpell(speller,"abclm","abc lm");

        assertSpell(speller,"ghief","ghief");

        assertSpell(speller,"boo abcdef","boo abc def");
    }

    @Test
    public void testNBest() throws ClassNotFoundException, IOException {

        NGramProcessLM lm = new NGramProcessLM(6,256,6);

        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,
                                    new FixedWeightEditDistance(0.0,
                                                                -3.0,
                                                                -2.0,
                                                                -1.0,
                                                                -1.0),
                                    IndoEuropeanTokenizerFactory.INSTANCE);


        for (int i = 0; i < 1000; ++i) {
            trainer.handle("abc");
            trainer.handle("abd");
            trainer.handle("abe");
            trainer.handle("abf");
        }
        for (int i = 0; i < 10; ++i)
            trainer.handle("abc");

        CompiledSpellChecker speller
            = (CompiledSpellChecker)
            AbstractExternalizable.compile(trainer);

        assertSpell(speller,"abx","abc");
    }


    void assertSpell(CompiledSpellChecker sc,
                     String in, String expected) {
        sc.setNBest(32);
        String found = sc.didYouMean(in);
        assertEquals("\n  FAILED TEST\n     In=/" + in
                     + "/\n     Expected=/" + expected
                     + "/\n     Found=/" + found + "/\n",
                     expected,found);
        Iterator nBestIt = sc.didYouMeanNBest(in);
        assertTrue(nBestIt.hasNext());
        ScoredObject so = (ScoredObject) nBestIt.next();
        assertEquals(so.getObject().toString(),expected);
    }


}
