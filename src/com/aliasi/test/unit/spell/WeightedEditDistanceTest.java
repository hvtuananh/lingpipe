package com.aliasi.test.unit.spell;

import com.aliasi.spell.WeightedEditDistance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class WeightedEditDistanceTest  {

    @Test
    public void testOne() {
        assertDistance(0,"","",true);
        assertDistance(0,"","",false);

        assertDistance(1,"a","",true);
        assertDistance(1,"","a",true);

        assertDistance(1,"a","",false);
        assertDistance(1,"","a",false);

        assertDistance(1,"a","ab",true);
        assertDistance(1,"ab","a",true);

        assertDistance(1,"a","ab",false);
        assertDistance(1,"ab","a",false);
    
        assertDistance(1,"ab","ba",true);

        assertDistance(2,"ab","ba",false);
    
        assertDistance(1,"abc","bac",true);
        assertDistance(1,"abc","acb",true);

        assertDistance(2,"abc","bac",false);
        assertDistance(2,"abc","acb",false);

        assertDistance(1,"abcd","bacd",true);
        assertDistance(1,"abcd","acbd",true);
        assertDistance(1,"abcd","abdc",true);

        assertDistance(2,"dabc","dbac",false);

        assertDistance(2,"pwnag","ownage",true);

        assertDistance(2,"pwnag","ownage",false);

        assertDistance(2,"abxy","bayx",true);

        assertDistance(3,"abxy","bayx",false);
    }

    private static void assertDistance(double expectedDistance,
                                       CharSequence cs1,
                                       CharSequence cs2,
                                       boolean allowTransposition) {

        WeightedEditDistance distanceMeasure
            = allowTransposition 
            ? TRANSPOSING_EDIT 
            : NON_TRANSPOSING_EDIT;
        assertDistance(expectedDistance,
                       cs1,cs2,distanceMeasure,false);
    }

    private static void assertDistance(double expectedDistance,
                                       CharSequence cs1,
                                       CharSequence cs2,
                                       WeightedEditDistance editDistance,
                                       boolean isSimilarity) {
        if (isSimilarity) 
            assertEquals(expectedDistance,
                         editDistance.proximity(cs1,cs2),
                         0.0001);
        else
            assertEquals(expectedDistance,
                         editDistance.distance(cs1,cs2),
                         0.0001);
    }

    private static class Transposing extends WeightedEditDistance {
        @Override
        public double matchWeight(char c) { return 0.0; }
        @Override
        public double deleteWeight(char c) { return  -1.0; }
        @Override
        public double insertWeight(char c) { return  -1.0; }
        @Override
        public double substituteWeight(char c1, char c2) { return  -1.0; }
        @Override
        public double transposeWeight(char c1, char c2) { return  -1.0; }
    }

    private static class NonTransposing extends WeightedEditDistance {
    
        @Override
        public double matchWeight(char c) { return 0.0; }
        @Override
        public double deleteWeight(char c) { return -1.0; }
        @Override
        public double insertWeight(char c) { return -1.0; }
        @Override
        public double substituteWeight(char c1, char c2) { return -1.0; }
        @Override
        public double transposeWeight(char c1, char c2) { 
            return Double.NEGATIVE_INFINITY; 
        }
    }


    private static WeightedEditDistance TRANSPOSING_EDIT = new Transposing();
    private static WeightedEditDistance NON_TRANSPOSING_EDIT = new NonTransposing();

}
