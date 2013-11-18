package com.aliasi.test.unit.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;


import com.aliasi.util.ShortPriorityQueue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

// mostly copied from BoundedPriorityQueueTest
// different comparator and some slightly different
// results
public class ShortPriorityQueueTest  {

    @Test
    public void testRemove() {
        ShortPriorityQueue queue
            = new ShortPriorityQueue(new IntComparator(),4);
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
        ShortPriorityQueue queue
            = new ShortPriorityQueue(new IntComparator(),4);

        assertEquals(0,queue.size());
        queue.clear();
        assertEquals(0,queue.size());

        queue.offer(Integer.valueOf(42));
        assertEquals(1,queue.size());

        // diff than bounded priority queue
        queue.offer(Integer.valueOf(42));
        assertEquals(2,queue.size());

        queue.offer(Integer.valueOf(43));
        assertEquals(3,queue.size());

        queue.clear();
        assertEquals(0,queue.size());
    }

    @Test
    public void testOne() {
        ShortPriorityQueue queue
            = new ShortPriorityQueue(new IntComparator(),4);
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
        assertTrue(queue.offer(Integer.valueOf(4))); // not bigger than smallest = 3 by ordering
        assertTrue(queue.offer(Integer.valueOf(50)));

        assertEquals(4,queue.size());
        it = queue.iterator();
        assertEquals(Integer.valueOf(50), it.next());
        assertEquals(Integer.valueOf(50), it.next());
        assertEquals(Integer.valueOf(20), it.next());
        assertEquals(Integer.valueOf(7), it.next());
        assertFalse(it.hasNext());

        assertTrue(queue.offer(Integer.valueOf(8)));

        assertEquals(Integer.valueOf(50),queue.poll());
        assertEquals(Integer.valueOf(50),queue.peek());

    }

    @Test(expected=UnsupportedOperationException.class)
    public void testUnsupported() {
        ShortPriorityQueue queue
            = new ShortPriorityQueue(new IntComparator(),10);
        queue.add(Integer.valueOf(42));
    }

    @Test
    public void testRandom() {
        int[] xs = new int[1000];
        for (int i = 0; i < 100; ++i) {
            Random random = new Random();
            ShortPriorityQueue queue
                = new ShortPriorityQueue(new IntComparator(),10);
            for (int j = 0; j < xs.length; ++j) {
                int z = random.nextInt(10000);
                queue.offer(z);
                xs[j] = z;
            }
            Iterator<Integer> it = queue.iterator();
            Arrays.sort(xs);
            for (int k = 0; k < 10; ++k) {
                assertEquals(xs[1000-k-1],it.next().intValue());
            }
        }
    }

    static class IntComparator implements Comparator {
        public int compare(Object obj1, Object obj2) {
            Integer int1 = (Integer) obj1;
            Integer int2 = (Integer) obj2;
            return int1.compareTo(int2);
        }
    }

}
