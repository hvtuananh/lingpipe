package com.aliasi.test.unit.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


import com.aliasi.util.MinMaxHeap;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.util.Arrays;
import java.util.Random;


public class MinMaxHeapTest  {

    static final Random RANDOM = new Random();
    
    @Test
    public void testCreate() {
        MinMaxHeap heap = new MinMaxHeap(10);
        assertNotNull(heap);
    }        

    @Test(expected=IllegalArgumentException.class)
    public void testSizeZero() {
        new MinMaxHeap(0);
    }

    @Test
    public void testOne() {
        MinMaxHeap heap = new MinMaxHeap(1);
        assertEquals(0,heap.size());
        
        DoubleS d1 = new DoubleS(1);
        heap.add(d1);
        assertEquals(1,heap.size());
        assertEquals(d1,heap.peekMax());

        DoubleS d2 = new DoubleS(2);
        heap.add(d2);
        assertEquals(1,heap.size());
        assertEquals(d2,heap.peekMax());
        assertEquals(d2,heap.peekMin());

        DoubleS d0 = new DoubleS(0);
        heap.add(d0);
        assertEquals(1,heap.size());
        assertEquals(d2,heap.peekMin());

        assertEquals(d2,heap.popMin());
        assertEquals(0,heap.size());
    }

    @Test
    public void testTwo() {
        MinMaxHeap heap = new MinMaxHeap(2);
        assertEquals(0,heap.size());
        
        DoubleS d1 = new DoubleS(1);
        heap.add(d1);
        assertEquals(1,heap.size());
        assertEquals(d1,heap.peekMax());
        assertEquals(d1,heap.peekMin());

        DoubleS d2 = new DoubleS(2);
        heap.add(d2);
        assertEquals(2,heap.size());
        assertEquals(d2,heap.peekMax());
        assertEquals(d1,heap.peekMin());

        DoubleS d0 = new DoubleS(0);
        heap.add(d0);
        assertEquals(2,heap.size());
        assertEquals(d2,heap.peekMax());
        assertEquals(d1,heap.peekMin());

        DoubleS d4 = new DoubleS(4);
        heap.add(d4);
        assertEquals(2,heap.size());
        assertEquals(d4,heap.peekMax());
        assertEquals(d2,heap.peekMin());

        DoubleS d3 = new DoubleS(3);
        heap.add(d3);
        assertEquals(2,heap.size());
        assertEquals(d4,heap.peekMax());
        assertEquals(d3,heap.peekMin());

    }

    @Test
    public void testThree() {
        MinMaxHeap heap = new MinMaxHeap(3);
        assertEquals(0,heap.size());
        
        DoubleS d1 = new DoubleS(1);
        heap.add(d1);
        assertEquals(1,heap.size());
        assertEquals(d1,heap.peekMax());
        assertEquals(d1,heap.peekMin());
        // 1

        DoubleS d2 = new DoubleS(2);
        heap.add(d2);
        assertEquals(2,heap.size());
        assertEquals(d2,heap.peekMax());
        assertEquals(d1,heap.peekMin());
        // 1, 2

        DoubleS d3 = new DoubleS(3);
        heap.add(d3);
        assertEquals(3,heap.size());
        assertEquals(d3,heap.peekMax());
        assertEquals(d1,heap.peekMin());
        // 1, 2, 3

        DoubleS d0 = new DoubleS(0);
        heap.add(d0);
        assertEquals(3,heap.size());
        assertEquals(d3,heap.peekMax());
        assertEquals(d1,heap.peekMin());
        // 1, 2, 3

        DoubleS d2_5 = new DoubleS(2.5);
        heap.add(d2_5);
        assertEquals(3,heap.size());
        assertEquals(d3,heap.peekMax());
        assertEquals(d2,heap.peekMin());
        // 2, 2.5, 3

        DoubleS d5 = new DoubleS(5);
        heap.add(d5);
        assertEquals(3,heap.size());
        assertEquals(d5,heap.peekMax());
        assertEquals(d2_5,heap.peekMin());
        // 2.5, 5, 3

        DoubleS d4 = new DoubleS(4);
        heap.add(d4);
        assertEquals(3,heap.size());
        assertEquals(d5,heap.peekMax());
        assertEquals(d3,heap.peekMin());

    }



    @Test
    public void testAdd() {
        for (int numTests = 0; numTests < 10; ++numTests) {
            for (int size = 1; size < 150; ++size) {
                // int size = RANDOM.nextInt(500) + 1;
                MinMaxHeap heap = new MinMaxHeap(size);
                assertEquals(0,heap.size());
                Scored[] scoreds = sample(RANDOM.nextInt(1000)+size);
                for (int i = 0; i < scoreds.length; ++i)
                    heap.add(scoreds[i]);
        
                assertEquals(size,heap.size());
        
                Arrays.sort(scoreds,ScoredObject.reverseComparator());
                for (int i = 0; ; ++i) {
                    Scored next = heap.popMax();
                    if (next == null) {
                        assertEquals(size,i);
                        break;
                    }
                    assertEquals(scoreds[i],next);
                }
                assertEquals(0,heap.size());
            }
        }
    }

    public Scored[] sample(int size) {
        Scored[] result = new Scored[size];
        for (int i = 0; i < size; ++i)
            result[i] = new DoubleS(RANDOM.nextDouble());
        return result;
    }
    
    static class DoubleS implements Scored {
        final double mX;
        public DoubleS(double x) {
            mX = x;
        }
        public double score() {
            return mX;
        }
        @Override
        public String toString() {
            return Double.toString(mX);
        }
    }

}
