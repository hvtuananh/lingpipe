/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.util;

import com.aliasi.util.Math;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


public class MathTest  {

    // digamma test values.
    // generated from R
    static double[][] DIGAMMA_TESTS = new double[][] {
        { -10, Double.NaN },
        { -9.8, -1.99149533102274 },
        { -9.6, 1.29217785271165 },
        { -9.4, 3.31372445948209 },
        { -9.2, 6.59659923473126 },
        { -9, Double.NaN },
        { -8.8, -2.09353614734927 },
        { -8.6, 1.18801118604499 },
        { -8.4, 3.20734148075868 },
        { -8.2, 6.48790358255735 },
        { -8, Double.NaN },
        { -7.8, -2.20717251098560 },
        { -7.6, 1.07173211627754 },
        { -7.4, 3.08829386171106 },
        { -7.2, 6.36595236304515 },
        { -7, Double.NaN },
        { -6.8, -2.33537763919073 },
        { -6.6, 0.940153168909122 },
        { -6.4, 2.95315872657593 },
        { -6.2, 6.22706347415626 },
        { -6, Double.NaN },
        { -5.8, -2.48243646272015 },
        { -5.6, 0.78863801739397 },
        { -5.4, 2.79690872657593 },
        { -5.2, 6.06577315157562 },
        { -5, Double.NaN },
        { -4.8, -2.65485025582359 },
        { -4.6, 0.610066588822547 },
        { -4.4, 2.61172354139075 },
        { -4.2, 5.87346545926791 },
        { -4, Double.NaN },
        { -3.8, -2.86318358915693 },
        { -3.6, 0.392675284474721 },
        { -3.4, 2.38445081411802 },
        { -3.2, 5.63537022117267 },
        { -3, Double.NaN },
        { -2.8, -3.12634148389377 },
        { -2.6, 0.114897506696942 },
        { -2.4, 2.09033316705920 },
        { -2.2, 5.32287022117267 },
        { -2, Double.NaN },
        { -1.8, -3.4834843410366 },
        { -1.6, -0.269717877918446 },
        { -1.4, 1.67366650039252 },
        { -1.2, 4.86832476662722 },
        { -1, Double.NaN },
        { -0.8, -4.03903989659216 },
        { -0.6, -0.894717877918445 },
        { -0.4, 0.959380786106823 },
        { -0.2, 4.03499143329388 },
        { 0, Double.NaN },
        { 0.2, -5.28903989659216 },
        { 0.4, -2.56138454458511 },
        { 0.6, -1.54061921389319 },
        { 0.8, -0.965008566706137 },
        { 1, -0.577215664901533 },
        { 1.2, -0.289039896592187 },
        { 1.4, -0.0613845445851157 },
        { 1.6, 0.126047452773477 },
        { 1.8, 0.284991433293862 },
        { 2, 0.422784335098467 },
        { 2.2, 0.544293436741145 },
        { 2.4, 0.652901169700598 },
        { 2.6, 0.751047452773477 },
        { 2.8, 0.840546988849417 },
        { 3, 0.922784335098467 },
        { 3.2, 0.9988388912866 },
        { 3.4, 1.06956783636727 },
        { 3.6, 1.13566283738886 },
        { 3.8, 1.19768984599227 },
        { 4, 1.2561176684318 },
        { 4.2, 1.3113388912866 },
        { 4.4, 1.36368548342609 },
        { 4.6, 1.41344061516664 },
        { 4.8, 1.46084774072912 },
        { 5, 1.5061176684318 },
        { 5.2, 1.54943412938184 },
        { 5.4, 1.59095821069882 },
        { 5.6, 1.63083191951446 },
        { 5.8, 1.66918107406245 },
        { 6, 1.7061176684318 },
        { 6.2, 1.74174182168953 },
        { 6.4, 1.776143395884 },
        { 6.6, 1.80940334808589 },
        { 6.8, 1.8415948671659 },
        { 7, 1.87278433509847 },
        { 7.2, 1.90303214427017 },
        { 7.4, 1.932393395884 },
        { 7.6, 1.96091849960104 },
        { 7.8, 1.98865369069531 },
        { 8, 2.01564147795561 },
        { 8.2, 2.04192103315906 },
        { 8.4, 2.06752853101914 },
        { 8.6, 2.09249744696947 },
        { 8.8, 2.11685881890044 },
        { 9, 2.14064147795561 },
        { 9.2, 2.16387225267126 },
        { 9.4, 2.18657615006676 },
        { 9.6, 2.20877651673691 },
        { 9.8, 2.2304951825368 },
        { 10,   2.25175258906672 }
    };

    @Test
    public void testDigamma() {
        for (double[] xFx : DIGAMMA_TESTS) {
            double x = xFx[0];
            double digamma_x = xFx[1];
            double val = com.aliasi.util.Math.digamma(x); // ,200000);
            if (Double.isNaN(digamma_x)) {
                assertTrue(Double.isNaN(val));
            } else {
                assertEquals(digamma_x,
                             val,
                             0.0000000000001);
            }
        }
    }

    // computed these using the R statistical package lgamma function
    // and double-checked on http://www.mhtl.uwaterloo.ca/old/onlinetools/func_calc/func_calc.html?flag=1&x=0.3&acc=6
    static double[][] GAMMA_INPUT_NATLOGOUT_TESTS = new double[][] {
        // from Waterloo's calculator
        { 0.1, 2.25271 },
        { 0.2, 1.52406 },
        { 0.3, 1.09580 },
        { 0.4, 0.796678 },

        // from R, doublechecked on Waterloo
        { 0.5, 0.57236494 },
        { 0.6, 0.39823386 },
        { 0.7, 0.26086725 },
        { 0.8, 0.15205968 },
        { 0.9, 0.06637624 },
        { 1.0, 0.00000000 },
        { 1.1, -0.04987244 },
        { 1.2, -0.08537409 },
        { 1.3, -0.10817481 },
        { 1.4, -0.11961291 },
        { 1.5,  -0.12078224 }
    };

    @Test
    public void testGamma() {
        for (int i = 2; i < 100; ++i)
            assertEquals(com.aliasi.util.Math.log2Factorial(i-1),
                         com.aliasi.util.Math.log2Gamma(i),
                         0.0001);

        for (double[] pair : GAMMA_INPUT_NATLOGOUT_TESTS) {
            double z = pair[0];
            double logEGammaZ = pair[1];
            double gammaZ = java.lang.Math.exp(logEGammaZ);
            double log2GammaZ = com.aliasi.util.Math.log2(gammaZ);
            double foundLog2GammaZ = com.aliasi.util.Math.log2Gamma(z);
            assertEquals(log2GammaZ,com.aliasi.util.Math.log2Gamma(z),0.0001);
        }
    }

    @Test
    public void testNextPrime() {
        assertEquals(2,Math.nextPrime(-7));
        assertEquals(2,Math.nextPrime(-2));
        assertEquals(2,Math.nextPrime(-1));
        assertEquals(2,Math.nextPrime(0));
        assertEquals(2,Math.nextPrime(1));
        assertEquals(3,Math.nextPrime(2));
        assertEquals(5,Math.nextPrime(3));
        assertEquals(7,Math.nextPrime(5));
        assertEquals(11,Math.nextPrime(7));
        assertEquals(103,Math.nextPrime(101));
    }

    @Test
    public void testIsPrime() {
        assertFalse(Math.isPrime(-7));
        assertFalse(Math.isPrime(-2));
        assertFalse(Math.isPrime(-1));
        assertFalse(Math.isPrime(0));
        assertFalse(Math.isPrime(1));
        assertTrue(Math.isPrime(2));
        assertTrue(Math.isPrime(3));
        assertFalse(Math.isPrime(4));
        assertTrue(Math.isPrime(5));
        assertFalse(Math.isPrime(6));
        assertFalse(Math.isPrime(25));
        assertFalse(Math.isPrime(100));
        assertTrue(Math.isPrime(101));
        assertFalse(Math.isPrime(999));
        assertFalse(Math.isPrime(1024));
    }

    @Test
    public void testLN_2() {
        assertEquals(Math.LN_2,
                     java.lang.Math.log(2.0),
                     0.0005);
    }

    @Test
    public void testNaturalToBase2() {
        assertEquals(1.0,
                     Math.naturalLogToBase2Log(java.lang.Math.log(2.0)),
                     0.0005);
        assertEquals(3.0,
                     Math.naturalLogToBase2Log(java.lang.Math.log(8.0)),
                     0.0005);
        assertEquals(-3.0,
                     Math.naturalLogToBase2Log(java.lang.Math.log(1.0/8.0)),
                     0.0005);
        assertEquals(0.0,
                     Math.naturalLogToBase2Log(java.lang.Math.log(1.0)),
                     0.0005);
    }

    @Test
    public void testLogBase2ToNaturalLog() {
        assertEquals(java.lang.Math.log(8.0),
                     Math.logBase2ToNaturalLog(3.0),
                     0.0001);
        assertEquals(java.lang.Math.log(0.5),
                     Math.logBase2ToNaturalLog(-1),
                     0.0001);
        assertEquals(0.0,
                     Math.logBase2ToNaturalLog(0.0),
                     0.0001);
    }

    @Test
    public void testByteAsUnsigned() {
        assertEquals(0,Math.byteAsUnsigned((byte)0));
        assertEquals(12,Math.byteAsUnsigned((byte)12));
        assertEquals(128,Math.byteAsUnsigned((byte)Byte.MIN_VALUE));
        assertEquals(200,Math.byteAsUnsigned((byte)-56));
        assertEquals(255,Math.byteAsUnsigned((byte)-1));
    }

    @Test
    public void testLnFactorial() {
        assertEquals(log2FactorialFull(0),Math.log2Factorial(0),0.0005);
        assertEquals(log2FactorialFull(10),Math.log2Factorial(10),0.0005);
        assertEquals(log2FactorialFull(100),Math.log2Factorial(100),0.0005);
        assertEquals(log2FactorialFull(101),Math.log2Factorial(101),0.0005);
        assertEquals(log2FactorialFull(1000),Math.log2Factorial(1000),0.0005);
        assertEquals(log2FactorialFull(1001),Math.log2Factorial(1001),0.0005);
    }

    private static double log2FactorialFull(int n) {
        if (n == 0) return 0;
        return com.aliasi.util.Math.log2(n) + log2FactorialFull(n-1);
    }

    @Test
    public void testInfiniteMaximum() {
        double max = Math.maximum(Double.NEGATIVE_INFINITY,1.0,2.0);
        assertEquals(2.0,max,0.0001);
    }

    @Test
    public void testLogSumOfExponentials() {
        assertLogSumOfExponentials(1.0);
        assertLogSumOfExponentials(1.0, 2.0);
        assertLogSumOfExponentials(-10.0, 20.0, 13.0, 0.0);

        assertLogSumOfExponentials(Double.NEGATIVE_INFINITY,1.0,2.0);
        assertLogSumOfExponentials(Double.NEGATIVE_INFINITY,1.0);
        assertLogSumOfExponentials(Double.NEGATIVE_INFINITY);

        // test overflow
        assertEquals(10000.0, com.aliasi.util.Math.logSumOfExponentials(new double[] { 10000.0 }), 0.0001);
    }

    @Test
    public void testMax() {
        assertTrue(Double.isNaN(com.aliasi.util.Math.max(new double[] { })));
        assertTrue(Double.isNaN(com.aliasi.util.Math.max(new double[] {
                                                             Double.NaN
                                                         })));
        assertTrue(Double.isNaN(com.aliasi.util.Math.max(new double[] {
                                                             Double.NaN,
                                                             1.0
                                                         })));
        assertTrue(Double.isNaN(com.aliasi.util.Math.max(new double[] {
                                                             1.0,
                                                             Double.NaN
                                                         })));
        assertTrue(Double.isNaN(com.aliasi.util.Math.max(new double[] {
                                                             1.0,
                                                             2.0,
                                                             Double.NaN
                                                         })));
        assertEquals(1.0,com.aliasi.util.Math.max(new double[] { 1.0 }));
        assertEquals(Double.POSITIVE_INFINITY,
                     com.aliasi.util.Math.max(new double[] {
                                                  Double.POSITIVE_INFINITY,
                                                  Double.NEGATIVE_INFINITY
                                              }));

        assertEquals(2.0,com.aliasi.util.Math.max(new double[] { 1.0, 2.0 }));
        assertEquals(2.0,com.aliasi.util.Math.max(new double[] { 2.0, 1.0 }));
        assertEquals(2.0,com.aliasi.util.Math.max(new double[] { 2.0, 1.0, -1.0 }));
    }

    static void assertLogSumOfExponentials(double... xs) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += java.lang.Math.exp(xs[i]);
        double expected = java.lang.Math.log(sum);
        assertEquals(expected,com.aliasi.util.Math.logSumOfExponentials(xs),0.0001);
    }




}
