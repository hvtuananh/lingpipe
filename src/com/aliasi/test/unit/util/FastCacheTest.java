package com.aliasi.test.unit.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static com.aliasi.test.unit.Asserts.succeed;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FastCache;

import java.io.IOException;

import java.util.Map;
import java.util.Random;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class FastCacheTest  {

    @Test
    public void testSerialize() throws IOException {
        FastCache<String,String> fc = new FastCache<String,String>(100,0.5);
        fc.put("a","b");
        fc.put("c","d");
        assertEquals(2,fc.size());
        assertEquals("b",fc.get("a"));
        FastCache<String,String> fc2
            = (FastCache<String,String>) 
            AbstractExternalizable.serializeDeserialize(fc);
        assertEquals(2,fc2.size());
        assertEquals("b",fc.get("a"));
        assertFalse(fc.containsKey("d"));
    }


    @Test 
    public void testClear() {
        FastCache<String,String> fc = new FastCache<String,String>(100,0.5);
        fc.put("a","b");
        fc.put("c","d");
        assertEquals(2,fc.size());
        assertEquals("b",fc.get("a"));
        fc.clear();
        assertEquals(0,fc.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastCacheException1() {
        new FastCache<String,String>(0,0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastCacheException2() {
        new FastCache<String,String>(-10,0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastCacheException3() {
        new FastCache<String,String>(5,-0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastCacheException4() {
        new FastCache<String,String>(5,Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastCacheException5() {
        new FastCache<String,String>(5,Double.POSITIVE_INFINITY);
    }
    

    @Test
    public void testOne() {
        int numIts = 100000;
        FastCache cache = new FastCache(5000000,1.0);
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
        FastCache cache = new FastCache(5000000,1.0);
        for (int i = 0; i < numMegabytes; ++i)
            cache.put(Integer.valueOf(i),
                      new int[megabyte]);
        succeed();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxExc1() {
            new FastCache(0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstruxExc2() {
        new FastCache(1,0.0);
    }

    @Test
    public void testPrune() {
        FastCache cache = new FastCache(150,0.5);
        int max = 10000;
        for (int i = 0; i < max; ++i)
            cache.put(Integer.valueOf(i),Integer.valueOf(i/2));
        assertTrue(cache.size() < 75);
    }

    // cache size forces misses and flushes
    // thread pool size forces some threads to finish before other start
    // tests result always correct
    @Test
    public void testMulti() throws InterruptedException {
        int numThreads = 128;
        int numEntries = 32;
        Map<Integer,Integer> cache = new java.util.concurrent.ConcurrentHashMap<Integer,Integer>();
        // Map<Integer,Integer> cache = new FastCache<Integer,Integer>(numEntries/2, 0.75);
        CacheTest[] cacheTests = new CacheTest[numThreads];
        for (int i = 0; i < numThreads; ++i)
            cacheTests[i] = new CacheTest(cache,numEntries);
        ExecutorService executor
            = new ScheduledThreadPoolExecutor((numThreads*3)/4);
        for (CacheTest testCache : cacheTests)
            executor.execute(testCache);
        executor.shutdown();
        executor.awaitTermination(30,TimeUnit.SECONDS);
        for (int j = 0; j < numEntries; ++j) {
            Integer val = (Integer) cache.get(Integer.valueOf(j));
            if (val == null) continue;
            assertEquals(val, Integer.valueOf(j/2));
        }
        int sumHits = 0;
        for (int i = 0; i < numThreads; ++i)
            sumHits += cacheTests[i].mHits;
        int misses = numThreads * numEntries - sumHits;
        // System.out.println("hits=" + sumHits
        // + " misses=" + misses
        // + " numEntries=" + numEntries);
        // for (int i = 0; i < numThreads; ++i)
        // sSystem.out.println("  cache size=" + cacheTests[i].mCache.size());
    }


    static class CacheTest implements Runnable {
        final Map<Integer,Integer> mCache;
        final int mNumEntries;
        int mHits = 0;
        CacheTest(Map<Integer,Integer> cache, int numEntries) {
            mCache = cache;
            mNumEntries = numEntries;
        }
        public void run() {
            Random random = new Random();
            for (int j = 0; j < mNumEntries; ++j) {
                try {
                    Thread.sleep(random.nextInt(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (mCache.get(j) != null) {
                    synchronized (this) {
                        ++mHits;
                    }
                } else {
                    mCache.put(j, j/2);
                }
            }
        }
    }



}
