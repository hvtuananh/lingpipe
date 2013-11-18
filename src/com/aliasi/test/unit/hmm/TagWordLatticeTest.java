package com.aliasi.test.unit.hmm;

// import com.aliasi.hmm.TagWordLattice;

import com.aliasi.symbol.MapSymbolTable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;



public class TagWordLatticeTest  {

    @Test
    public void test1() {
        assertEquals(1,1); // need test as proxy for now
    }

    // RE-ADD TEST WHEN PACKAGE STRUCTURE CHANGES

    /*
    @Test
    public void testEmpty() {
        MapSymbolTable syms = new MapSymbolTable();
        syms.getOrAddSymbol("FOO");
        syms.getOrAddSymbol("BAR");
        String[] toks = new String[0];
        double[] ends = new double[0];
        double[] starts = new double[0];
        double[][][] transits = new double[0][0][0];
        TagWordLattice lattice
            = new TagWordLattice(toks,syms,starts,ends,transits);
    
        assertEquals(0,lattice.bestForwardBackward().length);
        assertEquals(0.0,lattice.log2Total(),0.0001);
        assertEquals(syms,lattice.tagSymbolTable());
        assertArrayEquals(toks,lattice.tokens());
        assertEquals(1.0,lattice.total(),0.0001);
    
    }

    @Test
        public void testOne() {
        MapSymbolTable syms = new MapSymbolTable();
        syms.getOrAddSymbol("FOO");
        syms.getOrAddSymbol("BAR");
        String[] toks = new String[] { "a", "b" };


        double[] ends = new double[] { 0.4, 0.8 };
        double[] starts = new double[] { 0.2, 0.1 };
        double[][][] transits 
            = new double[][][] { { { -1, -1 }, {-1, -1 } },
                                 { { 0.2, 0.1 }, { 0.4, 0.8 } } };
        TagWordLattice lattice 
            = new TagWordLattice(toks,syms,starts,ends,transits);

        assertEquals(0.2,lattice.start(0),0.001);
        assertEquals(0.1,lattice.start(1),0.001);
    
        assertEquals(0.1,lattice.transition(1,0,1),0.0001);
        assertEquals(0.4,lattice.transition(1,1,0),0.0001);

        assertEquals(0.4,lattice.end(0),0.0001);
        assertEquals(0.8,lattice.end(1),0.0001);
    
        assertEquals(0.2,lattice.forward(0,0),0.0001);
        assertEquals(0.1,lattice.forward(0,1),0.0001);
        assertEquals(0.08,lattice.forward(1,0),0.0001);
        assertEquals(0.10,lattice.forward(1,1),0.0001);

        assertEquals(0.16,lattice.backward(0,0),0.0001);
        assertEquals(0.80,lattice.backward(0,1),0.0001);
        assertEquals(0.4,lattice.backward(1,0),0.0001);
        assertEquals(0.8,lattice.backward(1,1),0.0001);

        assertEquals(0.4*0.08 + 0.8 * 0.1,lattice.total(),0.0001);
    }
    */

}
