package com.aliasi.test.unit.tag;

import com.aliasi.tag.Tagging;

import java.util.Arrays;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import static com.aliasi.test.unit.Asserts.succeed;

public class TaggingTest {

    @Test(expected=IllegalArgumentException.class)
    public void testConsExc1() {
        new Tagging<Integer>(Arrays.<Integer>asList(1,2,3),
                             Arrays.asList("one","two"));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testLengthExc1() {
        Tagging<Integer> tagging
            = new Tagging<Integer>(Arrays.<Integer>asList(1,2,3),
                                   Arrays.<String>asList("a","b","c"));
        tagging.token(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testLengthExc2() {
        Tagging<Integer> tagging
            = new Tagging<Integer>(Arrays.<Integer>asList(1,2,3),
                                   Arrays.<String>asList("a","b","c"));
        tagging.token(5);
    }

    
    @Test
    public void testCons0() {
        Tagging<Integer> tagging
            = new Tagging<Integer>(Arrays.<Integer>asList(),
                                   Arrays.<String>asList());
        assertEquals(0,tagging.size());
        assertEquals(Arrays.<Integer>asList(), tagging.tags());
        assertEquals(Arrays.<String>asList(), tagging.tokens());
        assertNotNull(tagging.toString());
    }

    @Test
    public void testCons2() {
        Tagging<Integer> tagging
            = new Tagging<Integer>(Arrays.<Integer>asList(1,2,3),
                                   Arrays.<String>asList("a","b","c"));
        assertEquals(3,tagging.size());
        assertEquals(Arrays.<Integer>asList(1,2,3), tagging.tokens());
        assertEquals(Arrays.<String>asList("a","b","c"), tagging.tags());
        assertEquals(Integer.valueOf(2),tagging.token(1));
        assertEquals("c",tagging.tag(2));
        assertNotNull(tagging.toString());
    }

    
    


   


}


