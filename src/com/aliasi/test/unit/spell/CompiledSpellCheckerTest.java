package com.aliasi.test.unit.spell;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.CompiledNGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import com.aliasi.util.AbstractExternalizable;

import java.util.HashSet;

import java.io.IOException;

public class CompiledSpellCheckerTest  {




    @Test
    public void testShortTokenMidQuery() throws 
        ClassNotFoundException, IOException {
        NGramProcessLM lm = new NGramProcessLM(5);
        for ( int i = 1; i < 10000; ++i) {
            lm.train(" FINANCE ACT SCHEDULE ");
        }

        CompiledNGramProcessLM clm 
            = (CompiledNGramProcessLM) AbstractExternalizable.compile(lm);
        FixedWeightEditDistance editDistance
            = new FixedWeightEditDistance(0,-1,-1,-1,-1);
        HashSet tokenSet = new HashSet();
        tokenSet.add("FINANCE");
        tokenSet.add("ACT");
        tokenSet.add("SCHEDULE");
        CompiledSpellChecker sc 
            = new CompiledSpellChecker(clm,editDistance,tokenSet);
        sc.setFirstCharEditCost(-1);
        sc.setSecondCharEditCost(-1);
        sc.setNBest(32);
        sc.setKnownTokenEditCost(-1);
        sc.setNumConsecutiveInsertionsAllowed(1);
        sc.setAllowDelete(true);
        sc.setAllowInsert(true);
        sc.setAllowMatch(true);
        sc.setAllowSubstitute(true);
        sc.setAllowTranspose(true);
        sc.setMinimumTokenLengthToCorrect(2);
        assertCorrection(sc,"FINANCE ACT SCEDULE","FINANCE ACT SCHEDULE");
        sc.setMinimumTokenLengthToCorrect(3);
        assertCorrection(sc,"FINANCE ACT SCEDULE","FINANCE ACT SCHEDULE");
    }


    @Test
    public void testShortToken() throws 
        ClassNotFoundException, IOException {

        NGramProcessLM lm = new NGramProcessLM(5);
        String training1 = " ab "; 
        for ( int i = 1; i < 1000; ++i) {
            lm.train(training1);
        }

        CompiledNGramProcessLM clm 
            = (CompiledNGramProcessLM) AbstractExternalizable.compile(lm);
        FixedWeightEditDistance editDistance
            = new FixedWeightEditDistance(0,-1,-1,-1,-1);

        HashSet tokenSet = new HashSet();
        tokenSet.add("ab");
        CompiledSpellChecker sc 
            = new CompiledSpellChecker(clm,editDistance,tokenSet);
        sc.setMinimumTokenLengthToCorrect(2);
        assertCorrection(sc,"ac","ac");
    }


    @Test
    public void testTwo() throws 
        ClassNotFoundException, IOException {

        NGramProcessLM lm = new NGramProcessLM(5);
        String training1 = " Smith ";
        for ( int i = 1; i < 10000; ++i) {
            lm.train(training1);
        }

        CompiledNGramProcessLM clm 
            = (CompiledNGramProcessLM) AbstractExternalizable.compile(lm);
        FixedWeightEditDistance editDistance
            = new FixedWeightEditDistance(0,-2,-2,-2,-2);

        HashSet tokenSet = new HashSet();
        tokenSet.add("Smith");

        CompiledSpellChecker sc 
            = new CompiledSpellChecker(clm,editDistance,tokenSet);

        assertCorrection(sc,"Smythe","Smith");
        assertCorrection(sc,"mith","Smith");
        assertCorrection(sc,"Tmith","Smith");
        assertCorrection(sc,"mSith","Smith");
        assertCorrection(sc,"Stith","Smith");
        assertCorrection(sc,"Skth","Smith");
    
    
        assertCorrection(sc,"mith Smith","Smith Smith");
        assertCorrection(sc,"Smith mith","Smith Smith");

        assertCorrection(sc,"SmithSmith","Smith Smith");
        assertCorrection(sc,"Smi th","Smith");

        HashSet doNotEditTokens = new HashSet();
        doNotEditTokens.add("mith");
        sc.setDoNotEditTokens(doNotEditTokens);
        assertCorrection(sc,"mith","mith");
        assertCorrection(sc,"Smith mith","Smith mith");

        sc.setMinimumTokenLengthToCorrect(3);
        assertCorrection(sc,"Sm th","Sm th");

        sc.setMinimumTokenLengthToCorrect(1);
        assertCorrection(sc,"Sm th","Smith");
    
        doNotEditTokens.add("Sm");
        doNotEditTokens.add("th");
        sc.setDoNotEditTokens(doNotEditTokens);
        assertCorrection(sc,"Sm th","Sm th");
    
        sc.setDoNotEditTokens(java.util.Collections.EMPTY_SET);

        sc.setFirstCharEditCost(-1000);
        assertCorrection(sc,"mith","mith");
        assertCorrection(sc,"Tmith","Tmith");
        // assertCorrection(sc,"mSith","mith");

        // no tokens
        CompiledSpellChecker sc2
            = new CompiledSpellChecker(clm,editDistance,new HashSet());

        assertCorrection(sc2,"Smth","Smth");
    }

    void assertCorrection(CompiledSpellChecker sc,
                          String query, String expectedCorrection) {
        String correction = sc.didYouMean(query);

    
        // displayPs("Query",query,sc);
        // displayPs("Correction",correction,sc);
        // displayPs("Expected",expectedCorrection,sc);

        assertEquals(expectedCorrection,correction);
    }

    void displayPs(String msg, String query, CompiledSpellChecker sc) {
        CompiledNGramProcessLM lm = sc.languageModel();
        System.out.println(msg + " " 
                           + " log2 P(" + query + ")="
                           + lm.log2Estimate(" " + query + " "));
    }

}
