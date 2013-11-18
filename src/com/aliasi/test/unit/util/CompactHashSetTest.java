package com.aliasi.test.unit.util;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.CompactHashSet;

import static com.aliasi.test.unit.Asserts.succeed;
import static com.aliasi.test.unit.Asserts.assertFullEquals;
import static com.aliasi.test.unit.Asserts.assertFullSerialization;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class CompactHashSetTest  {


    @Test
    public void testConstruct() {
        new CompactHashSet<String>(1);
        new CompactHashSet<String>(2);
        new CompactHashSet<String>(3);
        new CompactHashSet<String>(4);
        new CompactHashSet<String>(912);
        new CompactHashSet<String>(15485863);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxNeg() {
        new CompactHashSet<String>(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstrux0() {
        new CompactHashSet<String>(0);
    }

    @Test(expected=OutOfMemoryError.class)
    public void testConstruxTooBig() {
        // if you find a JVM that'll handle this big an array
        // let us know via e-mail to bugs@alias-i.com
        new CompactHashSet<String>(Integer.MAX_VALUE);
    }

    @Test
    public void testZeroSize() {
        Set<String> set = new CompactHashSet<String>(1);
        assertSetEquals(new HashSet<String>(), set);
    }

    @Test
    public void test1Element() {
        Set<String> xSet = hashSet("foo");
        Set<String> ySet = new CompactHashSet<String>(15);
        ySet.add("foo");
        assertSetEquals(xSet,ySet);

        Set<String> ySet2 = new CompactHashSet<String>(1);
        ySet2.add("foo");
        assertSetEquals(xSet,ySet2);
    }

    @Test
    public void test2Elements15() {
        Set<String> xSet = hashSet("foo","bar");
        Set<String> ySet = new CompactHashSet<String>(15);
        ySet.addAll(xSet);
        assertSetEquals(xSet,ySet);
    }

    @Test
    public void testSimp() {
        Set<String> ySet = new CompactHashSet<String>(1);
        assertTrue(ySet.add("foo"));
        assertTrue(ySet.add("bar"));
    }

    @Test
    public void test2Elements1() {
        Set<String> xSet = hashSet("bing","badkdki3lkawelkfj");
        Set<String> ySet = new CompactHashSet<String>(1);
        assertTrue(ySet.add("bing"));
	assertEquals(1,ySet.size());
        assertFalse(ySet.add("bing"));
	assertEquals(1,ySet.size());
        assertTrue(ySet.add("badkdki3lkawelkfj"));
	assertEquals(2,ySet.size());
        assertEquals(xSet,ySet);
    }

    @Test
    public void test5Elements1() {
        assertAdds(1,"abcd","12345","ZCXKDKD","s","yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
    }

    @Test
    public void testIteratorRemoves() {
        assertRemoves(1,"a");
    }

    static void assertAdds(int initialSize, String... elts) {
        Random random = new Random(42);
        String s1 = Arrays.asList(elts).toString();
        com.aliasi.util.Arrays.permute(elts,random);
        Set<String> xSet = new HashSet<String>();
        Set<String> ySet = new CompactHashSet<String>(initialSize);
        for (String elt : elts) {
            assertTrue(xSet.add(elt));
            assertFalse(xSet.add(elt));
            assertTrue(ySet.add(elt));
            assertFalse(ySet.add(elt));
            assertFullEquals("init=" + initialSize + " ",xSet,ySet);
        }
        com.aliasi.util.Arrays.permute(elts,random);
        String s2 = Arrays.asList(elts).toString();
        for (String elt : elts) {
            assertTrue(xSet.remove(elt));
            assertFalse(xSet.remove(elt));
            assertTrue(ySet.remove(elt));
            assertFalse(ySet.remove(elt));
            for (String x : xSet)
                assertTrue("\nx=" + x + "ySet=" + ySet + " s1=" + s1 + " s2=" + s2,ySet.contains(x));
            assertEquals(ySet,xSet);
            assertFullEquals("init=" + initialSize + "\n" + "xSet=" + xSet + "\nySet=" + ySet + "\ns1=" + s1 + "\ns2=" + s2 + "\n",
                             xSet,ySet);
        }
    }

    static void assertRemoves(int initialSize, String... elts) {
        Set<String> xSet = hashSet(elts);
        Set<String> ySet = new CompactHashSet<String>(10);
        ySet.addAll(xSet);
        Iterator<String> it = ySet.iterator();
        for (int i = 0; i < elts.length; ++i) {
            assertEquals(xSet,ySet);
            assertIllegalRemoveState(it);
            String s = it.next();
            it.remove();
            xSet.remove(s);
        }
        assertIllegalRemoveState(it);
        assertSetEquals(xSet,ySet);
    }

    static void assertIllegalRemoveState(Iterator<String> it) {
        try {
            it.remove();
            fail();
        } catch (IllegalStateException e) {
            succeed();
        }
    }


    static Set<String> hashSet(String... xs) {
        Set<String> set = new HashSet<String>();
        for (String x : xs)
            set.add(x);
        return set;
    }

    // xs will be expected; ys will be the small set
    static void assertSetEquals(Set<String> xSet, Set<String> ySet) {
        assertFullEquals(xSet,ySet);
        assertFullSerialization(ySet);

        assertEquals(xSet.size(), ySet.size());
        assertEquals(xSet.isEmpty(), ySet.isEmpty());

        Iterator<String> xIt = xSet.iterator();
        Iterator<String> yIt = ySet.iterator();
        Set<String> xItSet = new HashSet<String>();
        Set<String> yItSet = new HashSet<String>();
        while (xIt.hasNext()) {
            xItSet.add(xIt.next());
            yItSet.add(yIt.next());
        }
        assertFalse(yIt.hasNext());
        assertEquals(xItSet,yItSet);

        String[] xsS = xSet.toArray(new String[0]);
        String[] ysS = ySet.toArray(new String[ySet.size()]);
        Arrays.sort(xsS);
        Arrays.sort(ysS);
        assertArrayEquals(xsS,ysS);

        Object[] xs = xSet.toArray();
        Object[] ys = ySet.toArray();
        Arrays.sort(xs);
        Arrays.sort(ys);
        assertArrayEquals(xs,ys);

        for (String x : xSet)
            assertTrue(ySet.contains(x));
        
        for (String y : ySet)
            assertTrue(xSet.contains(y));


    }
    

}

