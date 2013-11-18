package com.aliasi.test.unit.spell;

import com.aliasi.spell.EditDistance;

import com.aliasi.util.Distance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class EditDistanceTest  {

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

    private static void assertDistance(int expectedDistance,
                       CharSequence cs1,
                       CharSequence cs2,
                       boolean allowTransposition) {
    assertEquals(expectedDistance,
             EditDistance.editDistance(cs1,cs2,allowTransposition));

    Distance<CharSequence> editDistance = new EditDistance(allowTransposition);
    assertEquals((double) expectedDistance,
             editDistance.distance(cs1,cs2),
             0.0001);

    if (allowTransposition) 
        assertEquals((double) expectedDistance,
             EditDistance.TRANSPOSING.distance(cs1,cs2),
             0.0001);
    else
        assertEquals((double) expectedDistance,
             EditDistance.NON_TRANSPOSING.distance(cs1,cs2),
             0.0001);

    }
                       

}
