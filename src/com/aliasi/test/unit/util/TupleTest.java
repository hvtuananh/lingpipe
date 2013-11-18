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

import com.aliasi.util.Tuple;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;
import static com.aliasi.test.unit.Asserts.assertFullEquals;
import static com.aliasi.test.unit.Asserts.assertNotEquals;


public class TupleTest  {

    @Test
    public void testEmpty() {
        Tuple zeroton = Tuple.create(new Object[] { });
        Tuple zeroton2 = Tuple.create(new Object[] { });
        Tuple singletonA = Tuple.create(new Object[] { "A" });
        Tuple singletonA2 = Tuple.create(new Object[] { "A" });
        Tuple singletonB =  Tuple.create(new Object[] { "B" });
        Tuple pairAB = Tuple.create(new Object[] { "A", "B" });
        Tuple pairAB2 = Tuple.create(new Object[] { "A", "B" });
        Tuple pairAC = Tuple.create(new Object[] { "A", "A" });
        Tuple tripleABC = Tuple.create(new Object[] { "A", "B", "C" });
        Tuple tripleABC2 = Tuple.create(new Object[] { "A", "B", "C" });
        Tuple tripleABD = Tuple.create(new Object[] { "A", "B", "D" });

        assertNotNull(zeroton);
        assertNotNull(singletonA);
        assertNotNull(pairAB);
        assertNotNull(tripleABC);

        assertFullEquals(zeroton,zeroton2);
        assertFullEquals(singletonA,singletonA2);
        assertFullEquals(singletonA,singletonA2);
        assertFullEquals(pairAB,pairAB2);
        assertFullEquals(tripleABC,tripleABC2);

        assertEquals(0,zeroton.size());
        assertEquals(1,singletonA.size());
        assertEquals(2,pairAB.size());
        assertEquals(3,tripleABC.size());

        assertEquals("A",singletonA.get(0));
        assertEquals("A",pairAB.get(0));
        assertEquals("B",pairAB.get(1));
        assertEquals("C",tripleABC.get(2));

        assertNotEquals(null,zeroton);
        assertNotEquals(null,singletonA);
        assertNotEquals(null,pairAB);
        assertNotEquals(null,tripleABC);

        assertNotEquals(zeroton,singletonA);
        assertNotEquals(zeroton,pairAB);
        assertNotEquals(zeroton,tripleABC);
        assertNotEquals(singletonA,pairAB);
        assertNotEquals(singletonA,tripleABC);
        assertNotEquals(pairAB,tripleABC);

        assertNotEquals(singletonA,singletonB);
        assertNotEquals(pairAB,pairAC);
        assertNotEquals(tripleABC,tripleABD);
    }


    @Test
    public void testArrayBounds() {
    boolean threw;

    Tuple zeroton = Tuple.create(new Object[] { });
        threw = false;
        try {
            zeroton.get(0);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            zeroton.get(-1);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);

        Tuple singleton = Tuple.create(new Object[] { "A" });
        threw = false;
        try {
            singleton.get(-1);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            singleton.get(15);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);

    Tuple pair = Tuple.create(new Object[] { "A", "B" });
        threw = false;
        try {
            pair.get(-1);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            pair.get(3);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);

    Tuple triple = Tuple.create(new Object[] { "A", "B", "C" });
        threw = false;
        try {
            triple.get(-1);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            triple.get(3);
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        assertTrue(threw);
    }
}
