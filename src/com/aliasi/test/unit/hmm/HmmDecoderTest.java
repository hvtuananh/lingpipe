package com.aliasi.test.unit.hmm;

import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.Tagging;

import com.aliasi.util.FastCache;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Iterator;

public class HmmDecoderTest  {

    static void handle(HmmCharLmEstimator estimator,
                String[] tokens, String[] whitespaces, String[] tags) {
        Tagging<String> tagging
            = new Tagging<String>(Arrays.asList(tokens),
                                  Arrays.asList(tags));
        estimator.handle(tagging);
    }


    @Test
    public void testCons() {
        HmmCharLmEstimator est = new HmmCharLmEstimator();


        String[] toks1 = new String[] { "John", "ran", "." };
        String[] tags1 = new String[] { "PN", "IV", "." };
        handle(est,toks1,null,tags1);

        String[] toks2 = new String[] { "Mary", "ran", "." };
        String[] tags2 = new String[] { "PN", "IV", "." };
        handle(est,toks2,null,tags2);

        String[] toks3 = new String[] { "Fred", "ran", "." };
        String[] tags3 = new String[] { "PN", "IV", "." };
        handle(est,toks3,null,tags3);

        String[] toks4 = new String[] { "John", "likes", "Mary", "." };
        String[] tags4 = new String[] { "PN", "TV", "PN", "." };
        handle(est,toks4,null,tags4);

        HmmDecoder decoder = new HmmDecoder(est);
        HmmDecoder decoderCached = new HmmDecoder(est,
                                                  new FastCache(1000),
                                                  new FastCache(1000));
    
        assertArrayEquals(tags4,
                          firstBest(decoder,toks4));
        for (int i = 0; i < 5; ++i) {
            assertArrayEquals(tags4,
                              firstBest(decoderCached,toks4));
        }

        String[] empty = new String[0];
        assertArrayEquals(empty, firstBest(decoderCached,empty));

        for (int i = 0; i < 5; ++i)
            assertArrayEquals(empty, firstBest(decoderCached,empty));
        
    }

    static String[] firstBest(HmmDecoder decoder, String[] tokens) {
        Tagging<String> tagging = decoder.tag(Arrays.asList(tokens));
        return tagging.tags().toArray(Strings.EMPTY_STRING_ARRAY);
    }

    // RE-ADD TESTS AFTER NEW PKG STRUCTURE
    
    @Test
    public void testLattice() {
        HmmCharLmEstimator est = new HmmCharLmEstimator();


        String[] toks1 = new String[] { "John", "ran", "." };
        String[] tags1 = new String[] { "PN", "IV", "." };
        handle(est,toks1,null,tags1);

        String[] toks2 = new String[] { "Mary", "ran", "." };
        String[] tags2 = new String[] { "PN", "IV", "." };
        handle(est,toks2,null,tags2);

        String[] toks3 = new String[] { "Fred", "ran", "." };
        String[] tags3 = new String[] { "PN", "IV", "." };
        handle(est,toks3,null,tags3);

        String[] toks4 = new String[] { "John", "likes", "Mary", "." };
        String[] tags4 = new String[] { "PN", "TV", "PN", "." };
        handle(est,toks4,null,tags4);

        HmmDecoder decoder = new HmmDecoder(est);
        HmmDecoder decoderCached = new HmmDecoder(est,new FastCache(1000),
                                                  new FastCache(1000));

        TagLattice<String> lattice = lattice(decoder,toks4);
        lattice(decoderCached,toks4);
    
        // RE-ADD WITH NEW PACKAGE

        // String[] decodedTags = lattice.bestForwardBackward();
        // assertArrayEquals(tags4,decodedTags);

        for (int i = 0; i < 5; ++i) {
            // String[] decodedTagsCached = lattice.bestForwardBackward();
            // assertArrayEquals(tags4,decodedTagsCached);
        }

        String[] empty = new String[0];
        lattice = lattice(decoder,empty);
        // assertArrayEquals(empty, lattice.bestForwardBackward());

        for (int i = 0; i < 5; ++i) {
            lattice = lattice(decoderCached,empty);
            // assertArrayEquals(empty, lattice.bestForwardBackward());
        }
              
    }

    static TagLattice<String> lattice(HmmDecoder decoder, String[] toks) {
        return decoder.tagMarginal(Arrays.asList(toks));
    }

    static Iterator nBest(HmmDecoder decoder, String[] tokens) {
        return nBest(decoder,tokens,Integer.MAX_VALUE);
    }

    static Iterator nBest(HmmDecoder decoder, String[] tokens, int maxResults) {
        return new IteratorWrapper(decoder.tagNBest(Arrays.asList(tokens),maxResults));
    }

    static class IteratorWrapper implements Iterator {
        Iterator<ScoredTagging<String>> mIt;
        public IteratorWrapper(Iterator<ScoredTagging<String>> it) {
            mIt = it;
        }
        public Object next() {
            ScoredTagging<String> st = mIt.next();
            return new ScoredObject<String[]>(st.tags().toArray(Strings.EMPTY_STRING_ARRAY),
                                              st.score());
        }
        public boolean hasNext() {
            return mIt.hasNext();
        }
        public void remove() {
            mIt.remove();
        }
    }

    

    @Test
    public void testNBest() {
        HmmCharLmEstimator est = new HmmCharLmEstimator();


        String[] toks1 = new String[] { "John", "ran", "." };
        String[] tags1 = new String[] { "PN", "IV", "." };
        handle(est,toks1,null,tags1);

        String[] toks2 = new String[] { "Mary", "ran", "." };
        String[] tags2 = new String[] { "PN", "IV", "." };
        handle(est,toks2,null,tags2);

        String[] toks3 = new String[] { "Fred", "ran", "." };
        String[] tags3 = new String[] { "PN", "IV", "." };
        handle(est,toks3,null,tags3);

        String[] toks4 = new String[] { "John", "likes", "Mary", "." };
        String[] tags4 = new String[] { "PN", "TV", "PN", "." };
        handle(est,toks4,null,tags4);

        HmmDecoder decoder = new HmmDecoder(est);
        HmmDecoder decoderCached = new HmmDecoder(est,
                                                  new FastCache(1000),
                                                  new FastCache(1000));


        Iterator nBest = nBest(decoder,toks4);
        ScoredObject best = (ScoredObject) nBest.next();
        String[] decodedTags = (String[]) best.getObject();
        assertArrayEquals(tags4,decodedTags);

        Iterator nBestC = nBest(decoderCached,toks4);
        ScoredObject bestC = (ScoredObject) nBestC.next();
        String[] decodedTagsC = (String[]) bestC.getObject();
        assertArrayEquals(tags4,decodedTagsC);

        String[] tags5 = new String[] { "A", "B", "C", "." };
        handle(est,toks4,null,tags5);

        nBest = nBest(decoder,toks4);
        double lastScore = Double.POSITIVE_INFINITY;
        for (int i = 0; nBest.hasNext(); ++i) {
            ScoredObject nextBest = (ScoredObject) nBest.next();
            double score = nextBest.score();
            assertTrue(score < lastScore);
            lastScore = score;
            String[] nextTags = (String[]) nextBest.getObject();
            assertEquals(4,nextTags.length);
        }

        HmmDecoder decoderCached2 = new HmmDecoder(est,
                                                   new FastCache(1000),
                                                   new FastCache(1000));
        for (int k = 0; k < 5; ++k) {
            Iterator nBestC2 = nBest(decoderCached2,toks4);
            double lastScoreC2 = Double.POSITIVE_INFINITY;
            for (int i = 0; nBestC2.hasNext(); ++i) {
                ScoredObject nextBestC2 = (ScoredObject) nBestC2.next();
                double scoreC2 = nextBestC2.score();
                assertTrue(scoreC2 < lastScoreC2);
                lastScoreC2 = scoreC2;
                String[] nextTagsC2 = (String[]) nextBestC2.getObject();
                assertEquals(4,nextTagsC2.length);
            }
        }

        ScoredObject firstBest = (ScoredObject) nBest(decoder,toks4).next();
        String[] yield = (String[]) firstBest.getObject();
        assertArrayEquals(firstBest(decoder,toks4),yield);

        for (int i = 0; i < 5; ++i) {
            ScoredObject firstBestC2 
                = (ScoredObject) nBest(decoderCached2,toks4).next();
            String[] yieldC2 = (String[]) firstBestC2.getObject();
            assertArrayEquals(firstBest(decoderCached2,toks4),yieldC2);
        }

        String[] empty = new String[0];
        Iterator nBest2 = nBest(decoder,empty);
        nBest2.next();
        assertFalse(nBest2.hasNext());

        for (int i = 0; i < 5; ++i) {
            Iterator nBestC2 = nBest(decoderCached2,empty);
            nBestC2.next();
            assertFalse(nBestC2.hasNext());
        }    
    }

    @Test
    public void testNBestFull() {
        HmmCharLmEstimator est = new HmmCharLmEstimator();


        String[] toks1 = new String[] { "a", "b" };
        String[] tags1 = new String[] { "X", "Y" };
        handle(est,toks1,null,tags1);

        String[] toks2 = new String[] { "b", "a" };
        handle(est,toks2,null,tags1);

        String[] toks3 = new String[] { "a", "b" };
        String[] tags3 = new String[] { "Y", "X" };
        handle(est,toks3,null,tags3);

        String[] toks4 = new String[] { "b", "a" };
        String[] tags4 = new String[] { "Y", "X" };
        handle(est,toks4,null,tags4);

        String[] toks5 = new String[] { "a", "b" };
        String[] tags5 = new String[] { "X", "X" };
        handle(est,toks5,null,tags5);

        String[] toks6 = new String[] { "b", "a" };
        String[] tags6 = new String[] { "X", "X" };
        handle(est,toks6,null,tags6);

        String[] toks7 = new String[] { "a", "b" };
        String[] tags7 = new String[] { "Y", "Y" };
        handle(est,toks7,null,tags7);

        String[] toks8 = new String[] { "b", "a" };
        String[] tags8 = new String[] { "Y", "Y" };
        handle(est,toks8,null,tags8);

        HmmDecoder decoder = new HmmDecoder(est);
        assertNBestCount(decoder,new String[] { }, 1);
        assertNBestCount(decoder,new String[] { "a" }, 2);
        assertNBestCount(decoder,new String[] { "a", "a" }, 4);
        assertNBestCount(decoder,new String[] { "a", "a", "a" }, 8);
        assertNBestCount(decoder,new String[] { "a", "a", "a", "a" }, 16);

        HmmDecoder decoderCached = new HmmDecoder(est,
                                                  new FastCache(1000),
                                                  new FastCache(1000));
        for (int i = 0; i < 5; ++i) {
            assertNBestCount(decoderCached,new String[] { }, 1);
            assertNBestCount(decoderCached,new String[] { "a" }, 2);
            assertNBestCount(decoderCached,new String[] { "a", "a" }, 4);
            assertNBestCount(decoderCached,new String[] { "a", "a", "a" }, 8);
            assertNBestCount(decoderCached,
                             new String[] { "a", "a", "a", "a" }, 16);
        }
    }

    void assertNBestCount(HmmDecoder decoder, String[] toks, int expCount) {
        Iterator nBest = nBest(decoder,toks);
        int count = 0;
        while (nBest.hasNext()) {
            ++count;
            nBest.next();
        }
        assertEquals(expCount,count);
    }
}
