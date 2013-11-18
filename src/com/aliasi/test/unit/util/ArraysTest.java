/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.util;

import com.aliasi.util.Arrays;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;


import java.util.HashSet;
import java.util.Set;

public class ArraysTest  {

    @Test
    public void testAdd() {
        testAdd("",'a',"a");
        testAdd("b",'a',"ab");
        testAdd("b",'b',"b");
        testAdd("b",'c',"bc");
        testAdd("dg",'a',"adg");
        testAdd("dg",'d',"dg");
        testAdd("dg",'e',"deg");
        testAdd("dg",'g',"dg");
        testAdd("dg",'h',"dgh");
        testAdd("dgj",'d',"dgj");
        testAdd("dgj",'g',"dgj");
        testAdd("dgj",'j',"dgj");
        testAdd("dgj",'a',"adgj");
        testAdd("dgj",'e',"degj");
        testAdd("dgj",'h',"dghj");
        testAdd("dgj",'k',"dgjk");
    }

    void testAdd(String csIn, char c, String csOut) {
        char[] cs = csIn.toCharArray();
        char[] cs2 = Arrays.add(c,cs);
        assertEquals(csOut,new String(cs2));
    }

    @Test
    public void testPermute() {
        Integer[] xs = new Integer[0];
        Arrays.<Integer>permute(xs);

        xs = new Integer[1];
        xs[0] = Integer.valueOf(5);
        Arrays.<Integer>permute(xs);
        assertEquals(Integer.valueOf(5), xs[0]);

        xs = new Integer[2];
        xs[0] = Integer.valueOf(0);
        xs[1] = Integer.valueOf(1);
        Arrays.<Integer>permute(xs);
        assertTrue(( xs[0].equals(Integer.valueOf(0))
                     && xs[1].equals(Integer.valueOf(1)) )
                   ||
                   ( xs[0].equals(Integer.valueOf(1))
                     && xs[1].equals(Integer.valueOf(0))) );

        xs = new Integer[100];
        for (int i = 0; i < 100; ++i)
            xs[i] = Integer.valueOf(i);
        Arrays.<Integer>permute(xs);
        Set<Integer> resultSet = new HashSet<Integer>(200);
        for (int i = 0; i < xs.length; ++i) {
            int val = xs[i].intValue();
            assertTrue(0 <= val && val < 100);
            resultSet.add(xs[i]);
        }
        assertEquals(100,resultSet.size());

    }


    @Test
    public void testReallocate() {
        int[] xs = new int[] { 1, 2, 3 };
        assertReallocate(xs,5);
        assertReallocate(xs,3);
        assertReallocate(xs,1);
        assertReallocate(xs,0);

        int[] zs = new int[] { };
        assertReallocate(zs,0);
        assertReallocate(zs,3);

    }

    void assertReallocate(int[] xs, int len) {
        int[] ys = Arrays.reallocate(xs,len);
        assertEquals(len,ys.length);
        for (int i = 0; i < xs.length && i < len; ++i)
            assertEquals(xs[i],ys[i]);
        for (int i = xs.length; i < ys.length; ++i)
            assertEquals(0,ys[i]);
    }

    @Test
    public void testEquals() {
        String[] xs1 = new String[] { "a", "b", "c" };
        String[] xs2 = new String[] { "a", "b", "c" };
        String[] xs3 = new String[] { "a", "b" };
        assertTrue(Arrays.equals(xs1,xs2));
        assertFalse(Arrays.equals(xs2,xs3));
        assertTrue(Arrays.equals(new Object[0], new Object[0]));
    }


    @Test
    public void testMemberObject() {
        assertFalse(Arrays.member("a",null));
        assertFalse(Arrays.member("a",new Object[] { "b", null }));
        assertFalse(Arrays.member("a",new Object[] { }));
        assertTrue(Arrays.member("a",new Object[] { "a" }));
        assertTrue(Arrays.member("a",new Object[] { null, "a" }));
    }

    @Test
    public void testMemberChar() {
        assertFalse(Arrays.member('a',null));
        assertFalse(Arrays.member('a',new char[] { }));
        assertFalse(Arrays.member('a',new char[] { 'b', 'c' }));
        assertTrue(Arrays.member('a',new char[] { 'a' }));
        assertTrue(Arrays.member('a',new char[] { 'b', 'a' }));
    }

    @Test
    public void testArrayToString() {
        assertEquals("[]",Arrays.arrayToString(new Object[] { }));
        assertEquals("[a]",Arrays.arrayToString(new Object[] { "a" }));
        assertEquals("[a,b]",Arrays.arrayToString(new Object[] { "a", "b" }));
    }

    @Test
    public void testArrayToStringBuilder() {
        StringBuilder sb = new StringBuilder();
        Arrays.arrayToStringBuilder(sb,new Object[] { });
        assertEquals("[]",sb.toString());
        sb = new StringBuilder();
        Arrays.arrayToStringBuilder(sb,new Object[] { "a" });
        assertEquals("[a]",sb.toString());
        sb = new StringBuilder();
        Arrays.arrayToStringBuilder(sb,new Object[] { "a", "b" });
        assertEquals("[a,b]",sb.toString());
    }

    @Test
    public void testConcatenate() {
        assertArrayEquals(new String[] { },
                          Arrays.concatenate(new String[] { },
                                             new String[] { }));
        assertArrayEquals(new String[] { "a" },
                          Arrays.concatenate(new String[] { "a" },
                                             new String[] { }));
        assertArrayEquals(new String[] { "b" },
                          Arrays.concatenate(new String[] { },
                                             new String[] { "b" }));
        assertArrayEquals(new String[] { "a","b","c","d" },
                          Arrays.concatenate(new String[] { "a", "b" },
                                             new String[] { "c", "d" }));


    }

}
