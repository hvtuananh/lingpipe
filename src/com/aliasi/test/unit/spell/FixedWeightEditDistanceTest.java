package com.aliasi.test.unit.spell;

import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;


public class FixedWeightEditDistanceTest  {

    @Test
    public void testOne() throws IOException, ClassNotFoundException {
        FixedWeightEditDistance ed
            = new FixedWeightEditDistance(0,-3,-4,-5,-6);
        assertEquals(0.0,ed.matchWeight('a'),0.005);
        assertEquals(-3.0,ed.deleteWeight('b'),0.005);
        assertEquals(-4.0,ed.insertWeight('c'),0.005);
        assertEquals(-5.0,ed.substituteWeight('d','e'),0.005);
        assertEquals(-6.0,ed.transposeWeight('f','g'),0.005);

        WeightedEditDistance ed2
            = (WeightedEditDistance) AbstractExternalizable.compile(ed);
        assertEquals(0.0,ed2.matchWeight('a'),0.005);
        assertEquals(-3.0,ed2.deleteWeight('b'),0.005);
        assertEquals(-4.0,ed2.insertWeight('c'),0.005);
        assertEquals(-5.0,ed2.substituteWeight('d','e'),0.005);
        assertEquals(-6.0,ed2.transposeWeight('f','g'),0.005);


        WeightedEditDistance ed3
            = (WeightedEditDistance) AbstractExternalizable.serializeDeserialize(ed);
        assertEquals(0.0,ed3.matchWeight('a'),0.005);
        assertEquals(-3.0,ed3.deleteWeight('b'),0.005);
        assertEquals(-4.0,ed3.insertWeight('c'),0.005);
        assertEquals(-5.0,ed3.substituteWeight('d','e'),0.005);
        assertEquals(-6.0,ed3.transposeWeight('f','g'),0.005);
    }


}
