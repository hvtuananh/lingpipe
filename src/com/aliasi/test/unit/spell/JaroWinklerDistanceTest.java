package com.aliasi.test.unit.spell;

import com.aliasi.spell.JaroWinklerDistance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;



public class JaroWinklerDistanceTest  {

    @Test
    public void testOnes() {
        testDistance("","",1.0,1.0);
        testDistance("a","a",1.0,1.0);
        testDistance("ab","ab",1.0,1.0);
        testDistance("abc","abc",1.0,1.0);
        testDistance("abcd","abcd",1.0,1.0);
        testDistance("abcde","abcde",1.0,1.0);
        testDistance("aa","aa",1.0,1.0);
        testDistance("aaa","aaa",1.0,1.0);
        testDistance("aaaa","aaaa",1.0,1.0);
        testDistance("aaaaa","aaaaa",1.0,1.0);

        testDistance("a","b",0.0,0.0);
        testDistance("","abc",0.0,0.0);
        testDistance("abcd","",0.0,0.0);
    }

    @Test
    public void testLastNames() {
        testDistance("shackleford","shackelford",0.970,0.982);
        testDistance("dunningham","cunnigham",0.896,0.896);
        testDistance("nichleson","nichulson",0.926,0.956);
        testDistance("jones","johnson",0.790,0.832);
        testDistance("massey","massie",0.889,0.933);
        testDistance("abroms","abrams",0.889,0.922);
        
        // this looks wrong from paper, which says it's 0.
        // testDistance("hardin","martinez",0.000,0.000);
        // should be:
        testDistance("hardin","martinez",0.722,0.722);

        // this also looks wrong from paper, which says its 0
        // testDistance("itman","smith",0.000,0.000);
        // should be:
        testDistance("itman","smith",0.467,0.467);

    }

    @Test
    public void testFirstNames() {
        testDistance("jeraldine","geraldine",0.926,0.926);
        testDistance("marhta","martha",0.944,0.961);
        testDistance("michelle","michael",0.869,0.921);
        testDistance("julies","julius",0.889,0.933);
        testDistance("tanya","tonya",0.867,0.880);
        testDistance("dwayne","duane",0.822,0.840);
        testDistance("sean","susan",0.783,0.805);
        testDistance("jon","john",0.917,0.933);
        // this looks wrong from paper, which says it's 0
        // testDistance("jon","jan",0.000,0.000);
        // should be:
        testDistance("jon","jan",0.778, 0.800);
    }

    void testDistance(String s1, String s2, 
                      double expectedJaro, double expectedWinkler) {
        assertEquals(expectedJaro,
                     JaroWinklerDistance.JARO_DISTANCE.proximity(s1,s2),
                     0.002);
        assertEquals(expectedWinkler,
                     JaroWinklerDistance.JARO_WINKLER_DISTANCE.proximity(s1,s2),
                     0.002);
        assertEquals(1.0 - expectedJaro,
                     JaroWinklerDistance.JARO_DISTANCE.distance(s1,s2),
                     0.002);
        assertEquals(1.0 - expectedWinkler,
                     JaroWinklerDistance.JARO_WINKLER_DISTANCE.distance(s1,s2),
                     0.002);
    }

}
