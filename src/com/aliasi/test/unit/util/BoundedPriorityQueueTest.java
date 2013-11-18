package com.aliasi.test.unit.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;


import com.aliasi.util.BoundedPriorityQueue;

import java.util.Comparator;
import java.util.Iterator;

public class BoundedPriorityQueueTest  {

    @Test
    public void testRemove() {
        BoundedPriorityQueue queue
            = new BoundedPriorityQueue(new IntComparator(),4);
        queue.offer(Integer.valueOf(1));
        queue.offer(Integer.valueOf(55));
        queue.offer(Integer.valueOf(233));
        assertEquals(3,queue.size());
        assertTrue(queue.remove(Integer.valueOf(55)));
        assertFalse(queue.remove(Integer.valueOf(10001)));
        assertEquals(2,queue.size());
        assertTrue(queue.contains(Integer.valueOf(1)));
        assertTrue(queue.contains(Integer.valueOf(233)));
        assertFalse(queue.contains(Integer.valueOf(55)));
    }

    @Test
    public void testClear() {
        BoundedPriorityQueue queue
            = new BoundedPriorityQueue(new IntComparator(),4);

        assertEquals(0,queue.size());
        queue.clear();
        assertEquals(0,queue.size());

        queue.offer(Integer.valueOf(42));
        assertEquals(1,queue.size());

        queue.offer(Integer.valueOf(42));
        assertEquals(1,queue.size());

        queue.offer(Integer.valueOf(43));
        assertEquals(2,queue.size());

        queue.clear();
        assertEquals(0,queue.size());
    }

    @Test
    public void testOne() {
        BoundedPriorityQueue queue
            = new BoundedPriorityQueue(new IntComparator(),4);
        assertEquals(0,queue.size());
        Iterator it = queue.iterator();
        assertFalse(it.hasNext());
        assertNull(queue.peek());
        assertNull(queue.peekLast());
        assertNull(queue.poll());
        assertTrue(queue.isEmpty());


        assertTrue(queue.offer(Integer.valueOf(1)));
        assertTrue(queue.offer(Integer.valueOf(3)));
        assertEquals(2,queue.size());
        it = queue.iterator();
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(1), it.next());
        assertFalse(it.hasNext());

        assertEquals(Integer.valueOf(3),queue.peek());
        assertEquals(Integer.valueOf(1),queue.peekLast());

        assertTrue(queue.offer(Integer.valueOf(50)));
        assertTrue(queue.offer(Integer.valueOf(20)));
        assertTrue(queue.offer(Integer.valueOf(7)));
        assertFalse(queue.offer(Integer.valueOf(0)));
        assertFalse(queue.offer(Integer.valueOf(4))); // not bigger than smallest = 3 by ordering
        assertFalse(queue.offer(Integer.valueOf(50)));

        assertEquals(4,queue.size());
        it = queue.iterator();
        assertEquals(Integer.valueOf(50), it.next());
        assertEquals(Integer.valueOf(20), it.next());
        assertEquals(Integer.valueOf(7), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());

        assertTrue(queue.offer(Integer.valueOf(8)));

        assertEquals(Integer.valueOf(50),queue.poll());
        assertEquals(Integer.valueOf(20),queue.peek());

    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAddUnsupported() {
        BoundedPriorityQueue<Object> queue
            = new BoundedPriorityQueue<Object>(new IntComparator(),4);
        queue.add(Integer.valueOf(2));
        
    }



    // why is this asymmetric?
    static class IntComparator implements Comparator {
        public int compare(Object obj1, Object obj2) {
            Integer int1 = (Integer) obj1;
            Integer int2 = (Integer) obj2;
            return int1.intValue()/2 - int2.intValue();
        }
    }

}
