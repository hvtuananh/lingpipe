
package com.aliasi.test.unit.util;

import com.aliasi.util.ObjectToDoubleMap;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;


import com.aliasi.util.ScoredObject;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class ObjectToDoubleMapTest  {


    @Test
    public void testZeroSet() {
	ObjectToDoubleMap map = new ObjectToDoubleMap();
	assertEquals(map.size(),0);
        map.setAndKeepZeros("a",0.0);
	assertEquals(map.size(),1);
	map.set("a",0.0);
	assertEquals(map.size(),0);
    }


    @Test
    public void testIncrement() {
        ObjectToDoubleMap map = new ObjectToDoubleMap();
        assertEquals(0.0,map.getValue("a"),0.0001);
        map.increment("a",1.0);
        assertEquals(1.0,map.getValue("a"),0.001);
        map.increment("a",2.5);
        assertEquals(3.5,map.getValue("a"),0.001);
        map.increment("a",5.0);
        assertEquals(8.5,map.getValue("a"),0.001);
        assertTrue(map.containsKey("a"));
        map.increment("a",-7);
        assertEquals(1.5,map.getValue("a"),0.001);
        map.increment("b",1);
        assertEquals(1.0,map.getValue("b"),0.001);
        map.increment("b",-1.0);
        assertEquals(0.0,map.getValue("b"),0.001);
        assertFalse(map.containsKey("b")); // depends on 1.0 - 1.0 = 0.0
    }


    @Test
    public void testSet() {
        ObjectToDoubleMap map = new ObjectToDoubleMap();
        map.set("a",3);
        assertEquals(3.0,map.getValue("a"),0.001);
        assertTrue(map.containsKey("a"));
        map.set("a",0.0);
        assertEquals(0.0,map.getValue("a"),0.001);
        assertFalse(map.containsKey("a"));
        map.set("a",3.0);
        map.set("a",4.0);
        assertEquals(4.0,map.getValue("a"),0.001);
        map.set("b",17.0);
        assertEquals(17.0,map.getValue("b"),0.001);
    }

    @Test
    public void testKeysOrderedByCount() {
        ObjectToDoubleMap map = new ObjectToDoubleMap();
        List keysOrderedByCountZero = map.keysOrderedByValueList();
        assertEquals(new ArrayList(), keysOrderedByCountZero);

        map.set("e",1);
        map.set("c",3);
        map.set("d",2);
        map.set("a",5);
        List keysOrderedByCount = map.keysOrderedByValueList();
        assertEquals(Arrays.asList(new Object[] { "a", "c", "d", "e" }),
                     keysOrderedByCount);
    }

    @Test
    public void testScoredObjects() {
        ObjectToDoubleMap map = new ObjectToDoubleMap();
        List<ScoredObject> sos1 = map.scoredObjectsOrderedByValueList();
        assertEquals(new ArrayList<ScoredObject>(), sos1);

        map.set("e",1);
        map.set("c",3);
        map.set("d",2);
        map.set("a",5);

        ScoredObject[] sos2s = new ScoredObject[] {
            new ScoredObject("a",5),
            new ScoredObject("c",3),
            new ScoredObject("d",2),
            new ScoredObject("e",1)
        };

        List<ScoredObject> sos2 = Arrays.asList(sos2s);
        List<ScoredObject> keysOrderedByCount = map.scoredObjectsOrderedByValueList();
        assertEquals(sos2.size(), keysOrderedByCount.size());
        for (int i = 0; i < sos2.size(); ++i) {
            assertEquals(sos2.get(i).score(), keysOrderedByCount.get(i).score(),
                         0.0001);
            assertEquals(sos2.get(i).getObject(),
                         keysOrderedByCount.get(i).getObject());
        }
    }


    @Test
    public void testCountComparator() {
        // two incomparables
        ObjectToDoubleMap map = new ObjectToDoubleMap();
        Object o1 = new Object();
        Object o2 = new Object();
        map.set(o1,2);
        map.set(o2,2);
        assertEquals(0,map.valueComparator().compare(o1,o2));
        map.set(o1,3);
        assertEquals(-1,map.valueComparator().compare(o1,o2));
        map.set(o1,1);
        assertEquals(1,map.valueComparator().compare(o1,o2));

        // two comparators
        String s1 = "a";
        String s2 = "b";
        map.set(s1,2);
        map.set(s2,2);
        assertEquals(-1,map.valueComparator().compare(s1,s2));
        map.set(s1,3);
        assertEquals(-1,map.valueComparator().compare(s1,s2));
        map.set(s1,1);
        assertEquals(1,map.valueComparator().compare(s1,s2));
        map.set(o1,1);
        map.set(s1,1);
        assertEquals(0,map.valueComparator().compare(o1,s1));
    }


}
