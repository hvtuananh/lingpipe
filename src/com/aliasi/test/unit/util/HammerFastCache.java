package com.aliasi.test.unit.util;

import com.aliasi.util.FastCache;
import com.aliasi.util.Strings;

import java.util.Map;
import java.util.Random;

public class HammerFastCache {

    static final Random RANDOM = new Random();

    static final long NUM_TESTS = 100000000000L;
    static final int DIMENSIONS = 40;

    static double sNextVal = 0.0;

    static long sElapsedSum = 0;

    static long sStartTime;

    public static void main(String[] args) {
        sStartTime = System.currentTimeMillis();
        System.out.println("\nFastCache");
        hammer(new FastCache<String,double[]>(100000));

        // System.out.println("\nHardFastCache");
        // hammer(new com.aliasi.util.HardFastCache<String,double[]>(100000));
        
    }

    static void hammer(Map<String,double[]> cache) {
        long start = System.currentTimeMillis();
        for (long i = 1; i <= NUM_TESTS; ++i) {
            String s = nextString();
            double[] a = nextArray();
            cache.put(s,a);
            long current = System.currentTimeMillis();
            long elapsed = current - start;
            start = current;
            sElapsedSum += elapsed;
            double avg = sElapsedSum / (double) i;
            if (elapsed > 100 || (i % 10000000) == 0) 
                System.out.println(i + " t=" + elapsed + "ms" + " avg=" + avg + "ms"
                                   + " total time=" + Strings.msToString(current - sStartTime));
            // if ((i % 100000) == 0)
            // System.out.println(i + " avg=" + avg + "ms");
        }
    }

    static String nextString() {
        char[] cs = new char[RANDOM.nextBoolean()
                             ? 1
                             : ( RANDOM.nextBoolean()
                                 ? 2
                                 : ( RANDOM.nextBoolean()
                                     ? 3
                                     : ( RANDOM.nextBoolean()
                                         ? 4
                                         : RANDOM.nextBoolean()
                                         ? 5
                                         : 6 )))];

        for (int i = 0; i < cs.length; ++i)
            cs[i] = (char) RANDOM.nextInt(128);
        return new String(cs);
    }

    static double[] nextArray() {
        double[] result = new double[DIMENSIONS];
        for (int i = 0; i < result.length; ++i) {
            result[i] = sNextVal;
            sNextVal += 0.0001;
        }
        return result;
    }

}