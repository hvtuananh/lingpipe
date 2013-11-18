package com.aliasi.test.unit.crf;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.crf.ForwardBackwardTagLattice;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;


public class ForwardBackwardTagLatticeTest {

    @Test
    public void testInputsSyms() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList(),
                                            Arrays.asList("NP", "IV"),
                                            new double[0][0],
                                            new double[0][0],
                                            new double[0][0][0], // 0 oK if others length 0
                                            0.0);
        assertEquals(0,lattice.numTokens());
        assertEquals(2,lattice.numTags());
        assertEquals("NP",lattice.tag(0));
        assertEquals("IV",lattice.tag(1));
        assertEquals(Arrays.asList(),lattice.tokenList());

        SymbolTable tagSymbolTable = lattice.tagSymbolTable();
        assertEquals(0,tagSymbolTable.symbolToID("NP"));
        assertEquals(1,tagSymbolTable.symbolToID("IV"));
        assertEquals(2,tagSymbolTable.numSymbols());
        assertEquals("NP",tagSymbolTable.idToSymbol(0));
        assertEquals("IV",tagSymbolTable.idToSymbol(1));
        List<String> tagList = Arrays.asList("NP","IV");
        assertEquals(tagList,lattice.tagList());
    }


    @Test
    public void testGetters() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                            new double[][] { { -1.0 }, { -2.0 } },
                                            new double[][] { { -3.0 }, { -4.0 } },
                                            new double[][][] { { { -5.0 } } },
                                            -6.0);
        assertEquals(2,lattice.numTokens());
        assertEquals(Arrays.asList("John","ran"), lattice.tokenList());
        assertEquals("John",lattice.token(0));
        assertEquals("ran",lattice.token(1));
        
        assertEquals(-1.0,lattice.logForward(0,0),0.0001);
        assertEquals(-2.0,lattice.logForward(1,0),0.0001);
        assertEquals(-3.0,lattice.logBackward(0,0),0.0001);
        assertEquals(-4.0,lattice.logBackward(1,0),0.0001);
        assertEquals(-5.0,lattice.logTransition(0,0,0),0.0001);
        assertEquals(-6.0,lattice.logZ(),0.00001);
    }

    @Test
    public void testProbs() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "VP"),
                                            new double[][] { { -1.0, -3.0 }, { -2.0, -0.5 } },
                                            new double[][] { { -2.5, -5.0 }, { -10.0, -3.0 } },
                                            new double[][][] { { {-1.7, -2.91 }, { -5.0, -13.2 } } },
                                            -2.0);
        assertEquals(-1.0 - 2.5 + 2.0,lattice.logProbability(0,0),0.0001);
        assertEquals(-3.0 - 5.0 + 2.0,lattice.logProbability(0,1),0.0001);

        assertEquals(-2.0 - 10.0 + 2.0,lattice.logProbability(1,0),0.0001);
        assertEquals(-0.5 - 3.0 + 2.0,lattice.logProbability(1,1),0.0001);

        assertEquals(-1.0 - 10.0 - 1.7 + 2.0,lattice.logProbability(0,new int[] { 0, 0 }),0.0001);
        assertEquals(-1.0 - 3.0 - 2.91 + 2.0,lattice.logProbability(0,new int[] { 0, 1 }),0.0001);

        assertEquals(-3.0 - 10.0 - 5.0 + 2.0,lattice.logProbability(0,new int[] { 1, 0 }),0.0001);
        assertEquals(-3.0 - 3.0 - 13.2 + 2.0,lattice.logProbability(0,new int[] { 1, 1 }),0.0001);

    }























    // EXCEPTIONS

    @Test
    public void testConsLenOK() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][2],
                                new double[2][2],
                                new double[1][2][2],
                                0.0);
        assertNotNull(lattice);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLen1() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[3][2], // consistent, but wrong lengths
                                new double[3][2],
                                new double[2][2][2],
                                0.0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testConsExLen2() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][3], // consistent, but wrong lengths
                                new double[2][3],
                                new double[1][3][3],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA1() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[3][2],
                                new double[2][2],
                                new double[1][2][2],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA2() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][3],
                                new double[2][2],
                                new double[1][2][2],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA3() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][2],
                                new double[3][2],
                                new double[1][2][2],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA4() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][2],
                                new double[2][3],
                                new double[1][2][2],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA5() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][2],
                                new double[2][2],
                                new double[3][2][2],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA6() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][2],
                                new double[2][2],
                                new double[1][3][2],
                                0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExLenA7() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP", "IV"),
                                new double[2][2],
                                new double[2][2],
                                new double[1][2][3],
                                0.0);
    }


    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange1() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logForward(2,0);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange2() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logForward(0,1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange3() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logBackward(2,0);
    }


    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange4() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logBackward(0,2);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange5() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logTransition(1,0,0);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange6() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                            Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logTransition(0,1,0);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange7() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logTransition(0,0,1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange8() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange9() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(2,0);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange10() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,new int[] { 0, 0, 0 }); // too long
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange11() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,new int[] { });  // too short
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange12() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,new int[] { 1 }); // out of range
    }


    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange13() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange14() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(2,0);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange15() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,new int[] { 0, 0, 0 }); // too long
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange16() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,new int[] { });  // too short
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testRange17() {
        ForwardBackwardTagLattice<String> lattice
            = new ForwardBackwardTagLattice(Arrays.asList("John","ran"),
                                Arrays.asList("NP"),
                                new double[][] { { -1.0 }, { -2.0 } },
                                new double[][] { { -3.0 }, { -4.0 } },
                                new double[][][] { { { -5.0 } } },
                                -6.0);
        lattice.logProbability(0,new int[] { 1 }); // out of range
    }









}
