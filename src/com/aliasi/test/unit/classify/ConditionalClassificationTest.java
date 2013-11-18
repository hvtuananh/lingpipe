package com.aliasi.test.unit.classify;

import com.aliasi.classify.ConditionalClassification;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ConditionalClassificationTest {

    @Test
    public void testLinearProbs() {
        String[] cats = new String[] { "foo" };
        double[] probs = new double[] { 0.25 };
        ConditionalClassification classification
            = ConditionalClassification
            .createProbs(cats,probs);
        assertEquals(1,classification.size());
        assertEquals("foo",classification.category(0));
        assertEquals(1.0,classification.conditionalProbability(0),0.0001);

        cats = new String[] { "foo", "bar" };
        probs = new double[] { 0.1, 0.3 };
        classification
            = ConditionalClassification
            .createProbs(cats,probs);
        assertEquals(2,classification.size());
        assertEquals("bar",classification.category(0));
        assertEquals("foo",classification.category(1));
        assertEquals(0.75,classification.conditionalProbability(0),0.0001);
        assertEquals(0.25,classification.conditionalProbability(1),0.0001);
    }



    @Test(expected=IllegalArgumentException.class)
    public void testExcDiffLengths() {
        ConditionalClassification.createLogProbs(new String[5],
                                                 new double[6]);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExcNaN() {
        ConditionalClassification.createLogProbs(new String[] { "a", "b" },
                                                 new double[] { Double.NaN, -5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExcInf() {
        ConditionalClassification.createLogProbs(new String[] { "a", "b" },
                                                 new double[] { -5, Double.POSITIVE_INFINITY });
    }


    @Test(expected=IllegalArgumentException.class)
    public void testExcPos() {
        ConditionalClassification.createLogProbs(new String[] { "a", "b", "c" },
                                                 new double[] { -5, 1, -3 });
    }

    @Test
    public void testSort() {
        ConditionalClassification classification
            = ConditionalClassification.createLogProbs(new String[] { "a", "c", "d", "b" },
                                                       new double[] { -1, -3, -3, -2 });
        String[] expectedCats = new String[] { "a", "b", "c", "d" };
        double[] expectedProbs = new double[] { 0.5, 0.25, 0.125, 0.125 };

        assertEquals(4,classification.size());
        for (int rank = 0; rank < 4; ++rank) {
            assertEquals(expectedProbs[rank], classification.conditionalProbability(rank), 0.0001);
            assertEquals(expectedCats[rank], classification.category(rank));
        }
    }

    @Test
    public void testSortScale() {
        ConditionalClassification classification
            = ConditionalClassification.createLogProbs(new String[] { "a", "c", "d", "b" },
                                                       new double[] { -101, -103, -103, -102 });
        String[] expectedCats = new String[] { "a", "b", "c", "d" };
        double[] expectedProbs = new double[] { 0.5, 0.25, 0.125, 0.125 };

        assertEquals(4,classification.size());
        for (int rank = 0; rank < 4; ++rank) {
            assertEquals(expectedProbs[rank], classification.conditionalProbability(rank), 0.0001);
            assertEquals(expectedCats[rank], classification.category(rank));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSize() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.25, 0.25 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSize2() {
        new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                      new double[] { 0.5, 0.25, 0.25 });
    }

    @Test
    public void testSize3() {
        assertNotNull(new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                                    new double[] { 0.5, 0.25, 0.25, 0.0 }));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTolerance() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.2 },
                                      0.001);
    }
        

    @Test(expected=IllegalArgumentException.class)
    public void testSizeB() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.25, 0.25 },
                                      0.001);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeB2() {
        new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                      new double[] { 0.5, 0.25, 0.25 },
                                      0.001);
    }

    @Test
    public void testSizeB3() {
        assertNotNull(new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                                    new double[] { 0.5, 0.25, 0.25, 0.0 },
                                                    0.001));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testToleranceB() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.2 },
                                      0.2);
    }

    @Test
    public void testToleranceB2() {
        assertNotNull(new ConditionalClassification(new String[] { "a", "b" },
                                                    new double[] { 0.1, 0.001 },
                                                    new double[] { 0.55, 0.45 }));

    }


    @Test(expected=IllegalArgumentException.class)
    public void testSizeC() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.2, 0.1 },
                                      new double[] { 0.5, 0.25, 0.25 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeC2() {
        new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                      new double[] { 15, 10, 8 },
                                      new double[] { 0.5, 0.25, 0.25 });
    }

    @Test
    public void testSizeC3() {
        assertNotNull(new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                                    new double[] { 0.05, 0.025, 0.01, 0.0 },
                                                    new double[] { 0.5, 0.25, 0.25, 0.0 }));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeC4() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.2, 0.1 },
                                      new double[] { 0.5, 0.2 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeC5() {
        new ConditionalClassification(new String[] { "a", "b", "c" },
                                      new double[] { 0.5, 0.2 },
                                      new double[] { 0.5, 0.25, 0.25 });
    }
   

    @Test(expected=IllegalArgumentException.class)
    public void testSizeCOrder() {
        new ConditionalClassification(new String[] { "a", "b", "c" },
                                      new double[] { 0.5, 0.2, 2 },
                                      new double[] { 0.5, 0.25, 0.25 });
    }



    @Test(expected=IllegalArgumentException.class)
    public void testSizeD() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.2, 0.1 },
                                      new double[] { 0.5, 0.25, 0.25 },
                                      0.1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeD2() {
        new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                      new double[] { 15, 10, 8 },
                                      new double[] { 0.5, 0.25, 0.25 },
                                      0.1);
    }

    @Test
    public void testSizeD3() {
        assertNotNull(new ConditionalClassification(new String[] { "a", "b", "c", "d" },
                                                    new double[] { 0.05, 0.025, 0.01, 0.0 },
                                                    new double[] { 0.5, 0.25, 0.25, 0.0 },
                                                    0.001));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeD4() {
        new ConditionalClassification(new String[] { "a", "b" },
                                      new double[] { 0.5, 0.2, 0.1 },
                                      new double[] { 0.5, 0.5 },
                                      0.1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSizeD5() {
        new ConditionalClassification(new String[] { "a", "b", "c" },
                                      new double[] { 0.5, 0.2 },
                                      new double[] { 0.5, 0.25, 0.25 },
                                      0.1);
    }
   

    @Test(expected=IllegalArgumentException.class)
    public void testSizeDOrder() {
        new ConditionalClassification(new String[] { "a", "b", "c" },
                                      new double[] { 0.5, 0.2, 2 },
                                      new double[] { 0.5, 0.25, 0.25 },
                                      0.5);
    }

    @Test
    public void testSizeDTolerance() {
        assertNotNull(new ConditionalClassification(new String[] { "a", "b", "c" },
                                                    new double[] { 0.5, 0.2, 0.0 },
                                                    new double[] { 0.5, 0.2, 0.1 },
                                                    0.3));
    }

    @Test
    public void testOrdering() {
        ConditionalClassification c = new ConditionalClassification(new String[] { "a", "b", "c" },
                                                                    new double[] { 0.5, 0.25, 0.25 });
        assertEquals("a",c.category(0));
        assertEquals("b",c.category(1));
        assertEquals("c",c.category(2));
        
        assertEquals(0.5,c.score(0));
        assertEquals(0.25,c.score(1));
        assertEquals(0.25,c.score(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeExc() {
        ConditionalClassification c = new ConditionalClassification(new String[] { "a", "b", "c" },
                                                                    new double[] { 0.5, 0.25, 0.25 });
        c.conditionalProbability(-1);
    }        

    @Test(expected = IllegalArgumentException.class)
    public void testRangeExc2() {
        ConditionalClassification c = new ConditionalClassification(new String[] { "a", "b", "c" },
                                                                    new double[] { 0.5, 0.25, 0.25 });
        c.conditionalProbability(5);
    }        

}
