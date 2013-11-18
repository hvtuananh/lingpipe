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

import com.aliasi.util.SmallSet;
import org.junit.Test;
import org.junit.Before;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static com.aliasi.test.unit.Asserts.assertFullEquals;

import java.util.HashSet;
import java.util.Set;


public class SmallSetTest  {

    private SmallSet setEmpty;
    private SmallSet setEmptyC;
    private SmallSet set1;
    private SmallSet set1C;
    private SmallSet set2;
    private SmallSet set3;
    private SmallSet set12;
    private SmallSet set123;

    public void testEmtpySetMulti() {
        SmallSet<String> stringSet = SmallSet.create();
        SmallSet<Integer> intSet = SmallSet.create();
        assertEquals(stringSet,intSet); // whacky, but OK with generics
        assertEquals(new HashSet<String>(), new HashSet<Integer>());

        SmallSet<String> s2 = stringSet.union(SmallSet.create("abc"));
        Set<String> expS2 = new HashSet<String>();
        expS2.add("abc");
        assertEquals(expS2,s2);

        SmallSet<Integer> s3 = intSet.union(SmallSet.create(Integer.valueOf(5)));
        Set<Integer> expS3 = new HashSet<Integer>();
        expS3.add(Integer.valueOf(5));
        assertEquals(expS3,s3);
       
    }

    @Before
    public void setUp() {
        setEmpty = SmallSet.create();
        setEmptyC = SmallSet.create();
        set1 = SmallSet.create(Integer.valueOf(1));
        set1C = SmallSet.create(Integer.valueOf(1));
        set2 = SmallSet.create(Integer.valueOf(2));
        set3 = SmallSet.create(Integer.valueOf(3));
        set12 = SmallSet.create(Integer.valueOf(1), Integer.valueOf(2));
        set123 = SmallSet.create(new Integer[] {
            Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) });
        // just test creation on these
        SmallSet.create(new Integer[] {
            Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4) });
        SmallSet.create(new Integer[] {
            Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5) });
    }

    @Test
    public void testEmptySet() {
        assertEquals(setEmpty.size(),0);
        assertFullEquals(setEmpty,setEmptyC);
        assertFalse(setEmpty.contains(Integer.valueOf(0)));
        testUnion(setEmpty,setEmpty,setEmpty);
    }

    @Test
    public void testSingleton() {
        assertEquals(set1.size(),1);
        assertFalse(set1.contains(Integer.valueOf(0)));
        assertTrue(set1.contains(Integer.valueOf(1)));
        assertFullEquals(set1,set1C);
        testUnion(set1,setEmpty,set1);
        assertEquals(set1,SmallSet.create(Integer.valueOf(1),Integer.valueOf(1)));
    }

    @Test
    public void testPair() {
        assertEquals(set12.size(),2);
        assertFalse(set12.contains(Integer.valueOf(0)));
        assertTrue(set12.contains(Integer.valueOf(1)));
        assertTrue(set12.contains(Integer.valueOf(2)));
        testUnion(set1,set2,set12);
        testUnion(set1,set12,set12);
        testUnion(setEmpty,set12,set12);
    }

    @Test
    public void testTriple() {
        assertEquals(set123.size(),3);
        assertTrue(set123.contains(Integer.valueOf(1)));
        assertTrue(set123.contains(Integer.valueOf(2)));
        assertTrue(set123.contains(Integer.valueOf(3)));
        assertFalse(set123.contains(Integer.valueOf(0)));
        testUnion(set12,set3,set123);
        testUnion(set123,setEmpty,set123);
        testUnion(set123,set1,set123);
        testUnion(set123,set12,set123);
        testUnion(set123,set123,set123);
    }

    public void testUnion(SmallSet set1, SmallSet set2, SmallSet result) {
        assertFullEquals(set1.union(set2),result);
        assertFullEquals(set2.union(set1),result);
    }

}
