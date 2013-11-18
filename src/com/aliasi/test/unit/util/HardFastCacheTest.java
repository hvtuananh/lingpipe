package com.aliasi.test.unit.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static com.aliasi.test.unit.Asserts.succeed;


import com.aliasi.util.HardFastCache;

public class HardFastCacheTest  {

    @Test
    public void testOne() {
        int numIts = 100000;
        HardFastCache cache = new HardFastCache(5000000,1.0);
        for (long i = 0; i < numIts; ++i)
            cache.put(Long.valueOf(i), new int[(int)(i % 10L)]);
        for (long i = 0; i < numIts; ++i)
            assertEquals((int)(i % 10L), 
                         ((int[]) cache.get(Long.valueOf(i))).length);
    }

    @Test
    public void testRecover() {
        // shouldn't blow out memory
        int megabyte = 1000000;
        int numMegabytes = 100;
        HardFastCache cache = new HardFastCache(5000000,1.0);
        for (int i = 0; i < numMegabytes; ++i)
            cache.put(Integer.valueOf(i),
                      new int[megabyte]);
        succeed();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxExc1() {
            new HardFastCache(0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxExc2() {
        new HardFastCache(1,0.0);
    }

    @Test
    public void testPrune() {
        HardFastCache cache = new HardFastCache(150,0.5);
        int max = 10000;
        for (int i = 0; i < max; ++i)
            cache.put(Integer.valueOf(i),Integer.valueOf(i/2));
        assertTrue(cache.size() < 75);
    }

    @Test
    public void testMulti() throws InterruptedException {
        HardFastCache cache = new HardFastCache(1000000,1.0);
        int numThreads = 2; // 16; // 128;
        int numEntries = 8; // 128; // 1024;
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            threads[i] = new Thread(new TestCache(cache,numEntries));
            threads[i].start();
        }
        for (int i = 0; i < numThreads; ++i) {
            threads[i].join();
        }
        for (int i = 0; i < numEntries; ++i) {
            Integer val = (Integer) cache.get(Integer.valueOf(i));
            if (val == null) continue;
            assertEquals(val, Integer.valueOf(i/2));
        }
    }

    private static class TestCache implements Runnable {
        final HardFastCache mCache;
        int mNum;
        TestCache(HardFastCache cache, int num) {
            mCache = cache;
            mNum = num;
        }
        public void run() {
            for (int i = 0; i < mNum; ++i)
                mCache.put(Integer.valueOf(i), Integer.valueOf(i/2));
        }
    }


}
