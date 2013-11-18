
package com.aliasi.test.unit.util;

import com.aliasi.util.ObjectToCounterMap;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;


import java.util.Arrays;
import java.util.List;

public class ObjectToCounterMapTest  {

    @Test
    public void testConstructor() {
        ObjectToCounterMap map = new ObjectToCounterMap();
        assertNotNull(map);
    }

    @Test
    public void testIncrement() {
        ObjectToCounterMap map = new ObjectToCounterMap();
    assertEquals(0,map.getCount("a"));
        map.increment("a");
        assertEquals(1,map.getCount("a"));
        map.increment("a");
        assertEquals(2,map.getCount("a"));
        map.increment("a",5);
        assertEquals(7,map.getCount("a"));
        assertTrue(map.containsKey("a"));
        map.increment("a",-7);
        assertFalse(map.containsKey("a"));
    }

    @Test
    public void testSet() {
        ObjectToCounterMap map = new ObjectToCounterMap();
        map.set("a",3);
        assertEquals(3,map.getCount("a"));
        assertTrue(map.containsKey("a"));
        map.set("a",0);
        assertEquals(0,map.getCount("a"));
        assertFalse(map.containsKey("a"));
        map.set("a",3);
        map.set("a",4);
        assertEquals(4,map.getCount("a"));
        map.set("b",17);
        assertEquals(17,map.getCount("b"));
    }

    @Test
    public void testKeysOrderedByCount() {
        ObjectToCounterMap map = new ObjectToCounterMap();
        map.set("e",1);
        map.set("c",3);
        map.set("d",2);
        map.set("a",5);
        List keysOrderedByCount = map.keysOrderedByCountList();
        assertEquals(Arrays.asList(new Object[] { "a", "c", "d", "e" }),
                          keysOrderedByCount);
    }

    @Test
    public void testCountComparator() {
        // two incomparables
        ObjectToCounterMap map = new ObjectToCounterMap();
        Object o1 = new Object();
        Object o2 = new Object();
        map.set(o1,2);
        map.set(o2,2);
        assertEquals(0,map.countComparator().compare(o1,o2));
        map.set(o1,3);
        assertEquals(-1,map.countComparator().compare(o1,o2));
        map.set(o1,1);
        assertEquals(1,map.countComparator().compare(o1,o2));

        // two comparaombles
        String s1 = "a";
        String s2 = "b";
        map.set(s1,2);
        map.set(s2,2);
        assertEquals(-1,map.countComparator().compare(s1,s2));
        map.set(s1,3);
        assertEquals(-1,map.countComparator().compare(s1,s2));
        map.set(s1,1);
        assertEquals(1,map.countComparator().compare(s1,s2));
        map.set(o1,1);
        map.set(s1,1);
        assertEquals(0,map.countComparator().compare(o1,s1));
    }

    @Test
    public void testPrune() {
    ObjectToCounterMap map = new ObjectToCounterMap();
    Object o1 = Integer.valueOf(1);
    Object o2 = Integer.valueOf(2);
    Object o3 = Integer.valueOf(3);
    map.set(o1,1);
    map.set(o2,1);
    map.set(o3,3);
    assertEquals(3,map.size());
    assertEquals(3,map.getCount(o3));
    assertEquals(1,map.getCount(o1));
    assertEquals(0,map.getCount(Integer.valueOf(10)));
    map.prune(1);
    assertEquals(3,map.size());
    map.prune(2);
    assertEquals(1,map.size());
    assertEquals(3,map.getCount(o3));
    assertEquals(0,map.getCount(o1));
    }

}
