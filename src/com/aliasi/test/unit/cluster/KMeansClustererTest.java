package com.aliasi.test.unit.cluster;

import com.aliasi.cluster.Clusterer;
import com.aliasi.cluster.KMeansClusterer;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.Distance;
import com.aliasi.util.FeatureExtractor;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;



public class KMeansClustererTest  {

    static FeatureExtractor FEATURE_EXTRACTOR
        = new TokenFeatureExtractor(IndoEuropeanTokenizerFactory.INSTANCE);

    static Random RANDOM = new Random(42);

    static final String AA = "A A";
    static final String AAA = "A A A";
    static final String BBB = "B B B";
    static final String CCC = "C C C";
    static final String AAB = "A A B";
    static final String BBA = "B B A";

    static Set<double[]> randomDenseElts(int numElts, int numDims, Random random) {
        Set<double[]> inputSet = new HashSet<double[]>(numElts*2);
        for (int i = 0; i < numElts; ++i)
            inputSet.add(randomDenseElt(numDims,random));
        return inputSet;
    }

    static double[] randomDenseElt(int numDims, Random random) {
        double[] xs = new double[numDims];
        for (int i = 0; i < xs.length; ++i)
            xs[i] =  10.0 * random.nextDouble() - 5.0;
        return xs;
    }

    static FeatureExtractor<double[]> ID_PARSER
        = new FeatureExtractor<double[]>() {
        public Map<String,? extends Number> features(double[] xs) {
            Map<String,Double> result = new HashMap<String,Double>();
            for (int i = 0; i < xs.length; ++i)
                result.put(Integer.toString(i), Double.valueOf(xs[i]));
            return result;
        }
    };

    // @Test // need to scale down for release and remove main!
    public void testKMeansPlusPlus() {
        int K = 500; // 1000; // 
        int n = 89037; // 167194; // 
        int d = 50;

        int maxEpochs = 200;
        double minImprovement = 0.0001;
        KMeansClusterer clustererKmpp
            = new KMeansClusterer(ID_PARSER,K,maxEpochs,true,minImprovement);

        KMeansClusterer clusterer
            = new KMeansClusterer(ID_PARSER,K,maxEpochs,false,minImprovement);

        Random random = new Random(); // (747L);

        Set<double[]> eltSet = randomDenseElts(n,d,random);

        Set<Set<double[]>> clustering
            = clusterer.cluster(eltSet,random,Reporters.silent());
    }


    public static void main(String[] args) {
        new KMeansClustererTest().testKMeansPlusPlus();
    }






    @Test(expected=IllegalArgumentException.class)
    public void testZeroExc1() {
        int numClusters = 0;
        int maxIterations = 10;
        new KMeansClusterer(FEATURE_EXTRACTOR,
                            numClusters,
                            maxIterations, false, 0.0);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testZeroExc2() {
        int numClusters = 10;
        int maxIterations = -1;
        new KMeansClusterer(FEATURE_EXTRACTOR,
                            numClusters,
                            maxIterations,
                            false, 0.0);
    }

    @Test
    public void testOne() {
        int numClusters = 1;
        int maxIterations = 10;
        KMeansClusterer<String> clusterer
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations,
                                  false,0.0);
        KMeansClusterer<String> clustererPP
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations,
                                  false,0.0);

        assertCluster(clusterer,clustererPP,
                      new String[] { },
                      new String[][] { });

        assertCluster(clusterer,clustererPP,
                      new String[] { AAA },
                      new String[][] { { AAA } });

        assertCluster(clusterer,clustererPP,
                      new String[] { AAA, BBB },
                      new String[][] { { AAA, BBB } });

        assertCluster(clusterer,clustererPP,
                      new String[] { AAA, BBB, CCC },
                      new String[][] { { AAA, BBB, CCC } });
    }

    @Test
    public void testTwo() {
        int numClusters = 2;
        int maxIterations = 10;
        KMeansClusterer<String> clusterer
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations,
                                  false,0.0);
        KMeansClusterer<String> clustererPP
            = new KMeansClusterer(FEATURE_EXTRACTOR,
                                  numClusters,
                                  maxIterations,
                                  true,0.0);

        assertCluster(clusterer,clustererPP,
                      new String[] { },
                      new String[][] { });

        assertCluster(clusterer,clustererPP,
                      new String[] { AAA },
                      new String[][] { { AAA } });

        assertCluster(clusterer,clustererPP,
                      new String[] { AAA, BBB },
                      new String[][] { { AAA }, { BBB } });

        // all inits produce same result in 2 iterations
        // { AAA, AAB }, { BBB }
        // { AAA, BBB }, { AAB }
        // { AAB, BBB }, { AAA }
        assertCluster(clusterer,clustererPP,
                      new String[] { AA, AAB, BBB },
                      new String[][] { { AA, AAB }, { BBB } });


    }

    @Test
    public void testRandomRecluster() {
        KMeansClusterer<String> clusterer
            = new KMeansClusterer(FEATURE_EXTRACTOR,100,20,false,0.0);
        for (int t = 0; t < 5; ++t) {
            Random random = new Random();
            long seed = random.nextLong();
            random = new Random(seed);
            int numClusters = 2 + random.nextInt(1);
            Set<String> inputSet = new HashSet<String>();
            Set<Set<String>> initialClustering
                = new HashSet<Set<String>>();
            for (int i = 0; i < numClusters; ++i)
                initialClustering.add(randomInputs(1+random.nextInt(3),random,inputSet));
            Set<String> unclusteredElements 
                = randomInputs(0 + random.nextInt(5),random,inputSet);
            Set<Set<String>> clustering
                = clusterer.recluster(initialClustering,
                                      unclusteredElements,
                                      Reporters.silent());
            assertCovers(clustering,inputSet);
        }
    }

    Set<String> randomInputs(int numInputs, Random random) {
        return randomInputs(numInputs,random,new HashSet<String>());
    }

    Set<String> randomInputs(int numInputs, Random random, Set<String> accum) {
        Set<String> inputSet = new HashSet<String>(numInputs*2);
        for (int n = 0; n < numInputs; ++n) {
            StringBuilder sb = new StringBuilder();
                int length = 5; // random.nextInt(20) + 5;
                int numChars = 2; // random.nextInt(16);
                for (int i = 0; i < length; ++i) {
                    if (i > 0) sb.append(' ');
                    char c = (char) ( ((int)'a') + random.nextInt(numChars));
                    sb.append(c);
                }
                if (!accum.add(sb.toString())) {
                    --n; 
                    continue;
                }
                inputSet.add(sb.toString());
            }
        return inputSet;
    }

    @Test
    public void testRandom() {
        for (int t = 0; t < 100; ++t) {
            Random random = new Random();
            long seed = random.nextLong();
            // seed = -3241003427224084927L;
            random = new Random(seed);
            int numInputs = random.nextInt(10) + 5;
            Set<String> inputSet = randomInputs(numInputs,random);

            int maxIterations = random.nextInt(50) + 50;
            int numClusters = 3; // random.nextInt(5) + 2;
            KMeansClusterer<String> clusterer
                = new KMeansClusterer(FEATURE_EXTRACTOR,
                                      numClusters,
                                      maxIterations,
                                      false,0.0);

            KMeansClusterer<String> clustererPlusPlus
                = new KMeansClusterer(FEATURE_EXTRACTOR,
                                      numClusters,
                                      maxIterations,
                                      true,0.0);

            int expectedNumClusters = Math.min(inputSet.size(),numClusters);

            Set<Set<String>> clustering =
                clusterer.cluster(inputSet,random,null);
            assertCovers(clustering,inputSet);
            // assertEquals("cluster() seed=" + seed + " t=" + t + " input set=" + inputSet
            // + " clustering=" + clustering,
            // expectedNumClusters,clustering.size());

            Set<Set<String>> clustering2 =
                clustererPlusPlus.cluster(inputSet,random,null);

            assertCovers(clustering2,inputSet);
            // assertEquals("seed=" + seed, expectedNumClusters,clustering.size());
        }
    }

    void assertCovers(Set<Set<String>> clustering, Set<String> elts) {
        // System.out.println("clustering=" + clustering);
        Set<String> clusteringElts = new HashSet<String>();
        for (Set<String> cluster : clustering) {
            assertTrue(cluster.size() > 0);
            clusteringElts.addAll(cluster);
        }
        assertEquals(elts,clusteringElts);
    }

    void assertCluster(KMeansClusterer clusterer, KMeansClusterer clustererPP,
                       String[] inputs,
                       String[][] expectedClusters) {

        Set<Set<String>> expectedClustering = toClustering(expectedClusters);

        // lots of random inits, etc.
        for (int i = 0; i < 1000; ++i) {
            Set<String> inputSet = toSet(inputs);

            Set<Set<String>> clustering = clusterer.cluster(inputSet,RANDOM,null);
            assertEquals(expectedClustering,clustering);

            Set<Set<String>> clusteringPP = clustererPP.cluster(inputSet,RANDOM,null);
            assertEquals(expectedClustering,clusteringPP);
        }
    }

    static Set<Set<String>> toClustering(String[][] clusters) {
        Set<Set<String>> clustering
            = new HashSet<Set<String>>();
        for (int i = 0; i < clusters.length; ++i)
            clustering.add(toSet(clusters[i]));
        return clustering;
    }


    static Set<String> toSet(String[] xs) {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < xs.length; ++i)
            result.add(xs[i]);
        return result;
    }

}
