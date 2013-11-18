package com.aliasi.test.unit.tag;

import com.aliasi.tag.Tagging;
import com.aliasi.tag.StringTagging;

import java.util.Arrays;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import static com.aliasi.test.unit.Asserts.succeed;

public class StringTaggingTest {

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc1() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two"),
                          "a b c",
                          new int[] { 0, 2, 4 },
                          new int[] { 2, 3, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc2() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { 0, 2 },
                          new int[] { 2, 3, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc3() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { 0, 2, 4 },
                          new int[] { 2, 3 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc4() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { -1, 2, 4 },
                          new int[] { 2, 3, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc5() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { 2, 1, 4 },
                          new int[] { 2, 3, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc6() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { 1, 3, 4 },
                          new int[] { 2, 2, 5 });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc7() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { 0, 2, 4 },
                          new int[] { 1, 3, 6 });
    }


    @Test(expected=IllegalArgumentException.class)
    public void testConsExc8() {
        new StringTagging(Arrays.asList("one","two","three"),
                          Arrays.asList("one","two","three"),
                          "a b c",
                          new int[] { 2, 1, 4 },
                          new int[] { 3, 2, 5 });
    }

    




    @Test
    public void testConsOk() {
        StringTagging tagging
            = new StringTagging(Arrays.asList("1","2","3"),
                                Arrays.asList("a","b","c"),
                                "A B C",
                                new int[] { 0, 2, 4 },
                                new int[] { 1, 3, 5 });
        assertEquals(3,tagging.size());
        assertEquals(Arrays.asList("1","2","3"),tagging.tokens());
        assertEquals(Arrays.asList("a","b","c"),tagging.tags());
        assertEquals("a",tagging.tag(0));
        assertEquals("b",tagging.tag(1));
        assertEquals("c",tagging.tag(2));
        assertEquals("1",tagging.token(0));
        assertEquals("2",tagging.token(1));
        assertEquals("3",tagging.token(2));
        assertEquals("A B C", tagging.characters());
        assertEquals("A",tagging.rawToken(0));
        assertEquals("B",tagging.rawToken(1));
        assertEquals("C",tagging.rawToken(2));
        
        assertEquals(0,tagging.tokenStart(0));
        assertEquals(2,tagging.tokenStart(1));
        assertEquals(4,tagging.tokenStart(2));
        assertEquals(1,tagging.tokenEnd(0));
        assertEquals(3,tagging.tokenEnd(1));
        assertEquals(5,tagging.tokenEnd(2));

        assertNotNull(tagging.toString());
    }

    @Test
    public void testBoundaryCons() {
        StringTagging tagging
            = new StringTagging(Arrays.asList("1","2","3"),
                                Arrays.asList("a","b","c"),
                                "",
                                new int[] { 0, 0, 0 },
                                new int[] { 0, 0, 0 });
        assertEquals(3,tagging.size());
        assertEquals(Arrays.asList("1","2","3"),tagging.tokens());
        assertEquals(Arrays.asList("a","b","c"),tagging.tags());
        assertEquals("a",tagging.tag(0));
        assertEquals("b",tagging.tag(1));
        assertEquals("c",tagging.tag(2));
        assertEquals("1",tagging.token(0));
        assertEquals("2",tagging.token(1));
        assertEquals("3",tagging.token(2));
        assertEquals("", tagging.characters());
        assertEquals("",tagging.rawToken(0));
        assertEquals("",tagging.rawToken(1));
        assertEquals("",tagging.rawToken(2));
        
        assertEquals(0,tagging.tokenStart(0));
        assertEquals(0,tagging.tokenStart(1));
        assertEquals(0,tagging.tokenStart(2));
        assertEquals(0,tagging.tokenEnd(0));
        assertEquals(0,tagging.tokenEnd(1));
        assertEquals(0,tagging.tokenEnd(2));
        assertNotNull(tagging.toString());
    }
    

}
