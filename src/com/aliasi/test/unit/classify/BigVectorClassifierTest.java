package com.aliasi.test.unit.classify;

import com.aliasi.classify.BigVectorClassifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.matrix.Vector;
import com.aliasi.matrix.SparseFloatVector;
import com.aliasi.util.AbstractExternalizable;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import java.util.HashSet;
import java.util.Set;


public class BigVectorClassifierTest {

    @Test
    public void testOne() throws IOException, ClassNotFoundException {
        Vector[] termVectors = new Vector[] {
            new SparseFloatVector(new int[] { 0, 2, 3},
                                  new float[] { -1.0f, 3.0f, 2.0f },
                                  Integer.MAX_VALUE),
            new SparseFloatVector(new int[] { 1, 2, 3 },
                                  new float[] { 1.0f, -1.0f, 2.0f },
                                  Integer.MAX_VALUE),
            new SparseFloatVector(new int[] { 0, 1 },
                                  new float[] { -1.0f, 1.0f },
                                  Integer.MAX_VALUE),
        };
        int maxResults = 4;
        BigVectorClassifier classifier
            = new BigVectorClassifier(termVectors,maxResults);
        assertNotNull(classifier);

        Vector x1
            = new SparseFloatVector(new int[] { 0, 1 },
                                    new float[] { 2.0f, 3.0f },
                                    Integer.MAX_VALUE);

        ScoredClassification c1 = classifier.classify(x1);
        assertAsExpected(c1);

        BigVectorClassifier classifier2
            = (BigVectorClassifier)
            AbstractExternalizable.serializeDeserialize(classifier);

        ScoredClassification c2 = classifier2.classify(x1);
        assertAsExpected(c2);
    }

    void assertAsExpected(ScoredClassification c) {
        assertNotNull(c);
        // System.out.println("c=" + c);
        assertEquals(4,c.size());
        assertEquals("3",c.category(0));
        assertEquals(10.0,c.score(0),0.0001);
        assertEquals(3.0,c.score(1),0.0001);
        assertEquals(3.0,c.score(2),0.0001);
        Set<String> expectedCats = new HashSet<String>();
        expectedCats.add("1");
        expectedCats.add("2");
        Set<String> foundCats = new HashSet<String>();
        foundCats.add(c.category(1));
        foundCats.add(c.category(2));
        assertEquals(expectedCats,foundCats);
        assertEquals("0",c.category(3));
        assertEquals(-2.0,c.score(3));


    }

}

