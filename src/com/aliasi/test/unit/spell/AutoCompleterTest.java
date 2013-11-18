package com.aliasi.test.unit.spell;

import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;

public class AutoCompleterTest {

    static final WeightedEditDistance EDIT_DISTANCE
        = new FixedWeightEditDistance(0.0, -10.0, -10.0, -10.0, Double.NEGATIVE_INFINITY);
    static final int MAX_QUEUE_SIZE = 1000;
    static final double MIN_SCORE = -40.0;

    @Test
    public void testOne() {
        Random random = new Random();
        long seed = random.nextLong();
        random = new Random(seed);
        String[] phrases = new String[] {
            "a",
            "abe",
            "able",
            "ace",
            "aces",
            "acing",
            "ad",
            "add",
            "be",
            "ben",
            "c"
        };
        Map<String,Double> phraseCounts = new HashMap<String,Double>();
        for (String phrase : phrases)
            phraseCounts.put(phrase,random.nextDouble());

        int maxResultsPerPrefix = 3;
        AutoCompleter completer
            = new AutoCompleter(phraseCounts,EDIT_DISTANCE,maxResultsPerPrefix,MAX_QUEUE_SIZE,MIN_SCORE);
        assertNotNull(completer);

    }

    @Test
    public void testBruteForce() {
        Random random = new Random();
        // long seed = random.nextLong();
        long seed = -3652569214004964184L;
        random = new Random(seed);
        int numPhrases = 5 + random.nextInt(500);
        Map<String,Float> phraseCounter = new HashMap<String,Float>();
        for (int i = 0; i < numPhrases; ++i) {
            float randomScore = random.nextFloat();
            while (true) {
                String phrase = randomPhrase(random,12); // dict 1-12
                if (phraseCounter.containsKey(phrase))
                    continue;
                phraseCounter.put(phrase,randomScore);
                break;
            }
        }
        String[] queries = new String[100];
        for (int i = 0; i < queries.length; ++i)
            queries[i] = randomPhrase(random,16);  // query 1-16 (to get bigger ones)

        assertBruteForce(phraseCounter, EDIT_DISTANCE,
                         4, 5000000, queries);
    }

    static String randomPhrase(Random random, int size) {
        char[] cs = new char[1 + random.nextInt(size)]; // 1 to size long
        for (int i = 0; i < cs.length; ++i)
            cs[i] = (char) (65 + random.nextInt(25));
        return new String(cs);
    }

    void assertBruteForce(Map<String,? extends Number> phraseCounter,
                          WeightedEditDistance editDistance,
                          int maxResults, int maxQueue,
                          String[] queries) {
        String[] phrases = new String[phraseCounter.size()];
        float[] counts = new float[phraseCounter.size()];
        int k = 0;
        for (Map.Entry<String,? extends Number> entry : phraseCounter.entrySet()) {
            phrases[k] = entry.getKey();
            counts[k] = entry.getValue().floatValue();
            ++k;
        }

        double totalCount = 0.0;
        for (int i = 0; i < counts.length; ++i)
            totalCount += counts[i];


        double[] logProbs = new double[counts.length];
        for (int i = 0; i < counts.length; ++i)
            logProbs[i] = com.aliasi.util.Math.log2(counts[i]/totalCount);

        AutoCompleter completer
            = new AutoCompleter(phraseCounter, editDistance,
                                maxResults, maxQueue, MIN_SCORE);

        for (String query : queries) {
            SortedSet<ScoredObject<String>> expectedResults = bruteForce(query,phrases,logProbs,editDistance,maxResults);
            SortedSet<ScoredObject<String>> results = completer.complete(query);

            assertEquals(expectedResults.size(), results.size());
            Iterator<ScoredObject<String>> expectedIt = expectedResults.iterator();
            Iterator<ScoredObject<String>> resultsIt = results.iterator();
            for (int i = 0; i < expectedResults.size(); ++i) {
                ScoredObject<String> expectedSo = expectedIt.next();
                ScoredObject<String> resultSo = resultsIt.next();
                assertEquals(expectedSo.getObject(), resultSo.getObject());
            }
        }
    }

    static SortedSet<ScoredObject<String>> bruteForce(String query,
                                                      String[] phrases,
                                                      double[] logProbs,
                                                      WeightedEditDistance editDistance,
                                                      int maxResults) {
        Map<String,Double> resultMap = new HashMap<String,Double>();

        for (int k = 0; k < phrases.length; ++k) {
            for (int i = 0; i <= phrases[k].length(); ++i) {
                String prefix = phrases[k].substring(0,i);
                double proximity = editDistance.proximity(query,prefix);
                double score = logProbs[k] + proximity;
                if (score >= MIN_SCORE
                    && (!resultMap.containsKey(phrases[k])
                        || resultMap.get(phrases[k]) < score))
                    resultMap.put(phrases[k],score);
            }
        }
        BoundedPriorityQueue<ScoredObject<String>> results
            = new BoundedPriorityQueue<ScoredObject<String>>(ScoredObject.comparator(),maxResults);
        for (Map.Entry<String,Double> entry : resultMap.entrySet())
            results.offer(new ScoredObject<String>(entry.getKey(), entry.getValue()));

        return results;
    }


}
