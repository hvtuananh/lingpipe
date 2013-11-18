package com.aliasi.test.unit.util;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BinaryMap;

import static com.aliasi.test.unit.Asserts.succeed;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BinaryMapTest  {

    static final Integer TEST_ONE = new Integer(1); // not same as ONE

    @Test
    public void testSerializable() throws IOException {
        BinaryMap map = new BinaryMap<String>();
        map.put("foo",TEST_ONE);
        @SuppressWarnings("unchecked")
        Map<String,Integer> map2 
            = (Map<String,Integer>) AbstractExternalizable.serializeDeserialize(map);
        assertEquals(map,map2);
    }

    @Test
    public void testEmpty() {
        Map<String,Integer> map = new BinaryMap<String>();
        assertTrue(map.isEmpty());
        assertEquals(0,map.size());
        assertFalse(map.containsKey("foo"));
        assertFalse(map.containsValue(TEST_ONE));
        map.clear();
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey("foo"));
        assertFalse(map.containsValue(TEST_ONE));
        assertTrue(map.entrySet().isEmpty());

        assertNull(map.get("foo"));
        assertNull(map.remove("foo"));

        Map<String,Integer> map2 = new HashMap<String,Integer>();
        assertEquals(map,map2);
        assertEquals(map2,map);
        assertEquals(map.hashCode(), map2.hashCode());
        assertEquals(map2.entrySet(), map.entrySet());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testUnsuppSetKey() {
        Map<String,Integer> map = new BinaryMap<String>();
        map.put("foo",TEST_ONE);
        Iterator<Map.Entry<String,Integer>> it 
            = map.entrySet().iterator();
        Map.Entry<String,Integer> entry = it.next();
        entry.setValue(new Integer(0));
    }

    @Test
    public void testSingleton() {
        Map<String,Integer> map = new BinaryMap<String>();
        map.put("foo",new Integer(1));
        assertFalse(map.isEmpty());
        assertEquals(1,map.size());

        assertTrue(map.containsKey("foo"));
        assertFalse(map.containsKey("bar"));
        
        assertTrue(map.containsValue(TEST_ONE));
        assertFalse(map.containsValue(new Integer(0)));

        assertFalse(map.entrySet().isEmpty());

        assertNull(map.get("bar"));
        assertEquals(TEST_ONE,map.get("foo"));

        Map<String,Integer> map2 = new HashMap<String,Integer>();
        map2.put("foo",new Integer(1));
        assertEquals(map,map2);
        assertEquals(map2,map);
        assertEquals(map.hashCode(), map2.hashCode());
        assertEquals(map2.entrySet(), map.entrySet());
        assertEquals(new HashSet<Integer>(map2.values()),
                     new HashSet<Integer>(map.values()));
        assertEquals(map2.keySet(), map.keySet());

        assertNull(map.remove("bar"));
        assertEquals(TEST_ONE,map.remove("foo"));
        assertTrue(map.isEmpty());
        assertEquals(0,map.size());
    }



    @Test
    public void testPair() {
        Map<String,Integer> map = new BinaryMap<String>();
        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));
        assertFalse(map.isEmpty());
        assertEquals(2,map.size());

        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertFalse(map.containsKey("baz"));
        
        assertTrue(map.containsValue(TEST_ONE));
        assertFalse(map.containsValue(new Integer(0)));

        assertFalse(map.entrySet().isEmpty());

        assertNull(map.get("baz"));
        assertEquals(TEST_ONE,map.get("foo"));
        assertEquals(TEST_ONE,map.get("bar"));

        Map<String,Integer> map2 = new HashMap<String,Integer>();
        map2.put("foo",new Integer(1));
        map2.put("bar",new Integer(1));
        assertEquals(map,map2);
        assertEquals(map2,map);
        assertEquals(map.hashCode(), map2.hashCode());
        assertEquals(map2.entrySet(), map.entrySet());
        assertEquals(new HashSet<Integer>(map2.values()),
                     new HashSet<Integer>(map.values()));
        assertEquals(map2.keySet(), map.keySet());

        assertNull(map.remove("baz"));
        assertEquals(TEST_ONE,map.remove("foo"));
        assertEquals(1,map.size());
        assertNull(map.remove("baz2"));
        assertEquals(TEST_ONE,map.remove("bar"));
        assertTrue(map.isEmpty());
        assertEquals(0,map.size());
    }


    @Test
    public void testMutableEntrySet() {
        Map<String,Integer> map = new BinaryMap<String>();
        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));
        Set<Map.Entry<String,Integer>> entrySet = map.entrySet();
        assertTrue(entrySet.contains(new SNEntry("foo")));
        assertTrue(entrySet.contains(new SNEntry("bar")));
        assertFalse(entrySet.contains(new SNEntry("baz")));
        
        assertTrue(entrySet.remove(new SNEntry("foo")));
        assertEquals(1,map.size());
        assertFalse(entrySet.remove(new SNEntry("baz")));
        assertFalse(entrySet.remove(new SNEntry("bar",new Integer(4))));
        assertEquals(1,map.size());
        assertEquals(1,entrySet.size());

        map.put("baz",new Integer(1));
        assertEquals(2,map.size());
        assertEquals(2,entrySet.size());
        
        try {
            entrySet.add(new SNEntry("biz"));
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }

        entrySet.clear();
        assertTrue(map.isEmpty());
        assertTrue(entrySet.isEmpty());

        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));
        entrySet.remove(new SNEntry("foo"));
        assertEquals(1,map.size());
        assertEquals(1,entrySet.size());
        
        assertEquals(TEST_ONE,map.get("bar"));
        assertNull(map.get("foo"));
    }

    @Test
    public void testMutableKeySet() {
        Map<String,Integer> map = new BinaryMap<String>();
        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));

        Set<String> keySet = map.keySet();
        assertTrue(keySet.contains("foo"));
        assertTrue(keySet.contains("bar"));
        assertFalse(keySet.contains("baz"));
        
        assertTrue(keySet.remove("foo"));
        assertEquals(1,map.size());
        assertFalse(keySet.remove("baz"));
        assertEquals(1,map.size());
        assertEquals(1,keySet.size());

        map.put("baz",new Integer(1));
        assertEquals(2,map.size());
        assertEquals(2,keySet.size());
        
        keySet.add("bing");
        assertEquals(3,map.size());
        assertEquals(3,keySet.size());

        keySet.clear();
        assertTrue(map.isEmpty());
        assertTrue(keySet.isEmpty());

        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));
        keySet.remove("foo");
        assertEquals(1,map.size());
        assertEquals(1,keySet.size());
        
        assertEquals(TEST_ONE,map.get("bar"));
        assertNull(map.get("foo"));

    }


    @Test
    public void testMutableValues() {
        Map<String,Integer> map = new BinaryMap<String>();
        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));

        Collection<Integer> values = map.values();
        assertTrue(values.contains(TEST_ONE));
        assertFalse(values.contains(new Integer(4)));
        assertEquals(1,values.size());
        assertFalse(values.isEmpty());
        
        assertFalse(values.remove(new Integer(54)));
        assertTrue(values.remove(TEST_ONE));
        assertTrue(map.isEmpty());
        assertEquals(0,map.size());
        assertEquals(0,values.size());

        map.put("foo",new Integer(1));
        map.put("baz",new Integer(1));
        assertEquals(2,map.size());
        assertEquals(1,values.size());
        
        try {
            values.add(new Integer(3));
            fail();
        } catch (UnsupportedOperationException e) {
            succeed();
        }

        values.clear();
        assertTrue(map.isEmpty());
        assertTrue(values.isEmpty());

        map.put("foo",new Integer(1));
        map.put("bar",new Integer(1));
        assertEquals(1,values.size());
        
        assertFalse(values.removeAll(Arrays.asList(new Integer(2), new Integer(3))));
        assertEquals(1,values.size());
        assertEquals(2,map.size());

        assertTrue(values.removeAll(Arrays.asList(new Integer(2), new Integer(3), new Integer(1))));
        assertTrue(values.isEmpty());
        assertTrue(map.isEmpty());

        Map<String,Integer> map2 = new HashMap<String,Integer>();
        map2.put("foo",new Integer(1));
        map2.put("bar",new Integer(1));
        assertFalse(map2.isEmpty());
        map2.values().removeAll(Arrays.asList(TEST_ONE));
        assertTrue(map2.isEmpty());
        
    }
    

    static class SNEntry implements Map.Entry<String,Integer> {
        final String mS;
        final Integer mN;
        public SNEntry(String s) {
            this(s,1);
        }
        public SNEntry(String s, int n) {
            mS = s;
            mN = new Integer(n);
        }
        public String getKey() {
            return mS;
        }
        public Integer getValue() {
            return mN;
        }
        public boolean equals(Object that) {
            if (!(that instanceof Map.Entry<?,?>))
                return false;
            Map.Entry<?,?> e1 = this;
            @SuppressWarnings("unchecked")
            Map.Entry<?,?> e2 = (Map.Entry<?,?>) that;
            return (e1.getKey()==null 
                    ? e2.getKey()==null 
                    : ( e1.getKey().equals(e2.getKey())))
                && (e1.getValue()==null 
                    ? e2.getValue()==null 
                    : e1.getValue().equals(e2.getValue()));
        }
        public Integer setValue(Integer n) {
            throw new UnsupportedOperationException();
        }
        public int hashCode() {
            Map.Entry<?,?> e = this;
            return (e.getKey()==null   ? 0 : e.getKey().hashCode())
                ^ (e.getValue()==null ? 0 : e.getValue().hashCode());
        }
    }

}

