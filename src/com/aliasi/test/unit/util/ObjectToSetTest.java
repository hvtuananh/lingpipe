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

import com.aliasi.util.ObjectToSet;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ObjectToSetTest  {

    @Test
    public void testRemove2() {
        ObjectToSet<String,Long> ots = new ObjectToSet<String,Long>();
        ots.addMember("a",1L);
        ots.addMember("a",2L);
        assertNotNull(ots.get("a"));
        ots.addMember("b",3L);
        assertNotNull(ots.get("b"));
        assertNull(ots.get("c"));
        ots.removeMember("a",1L);
        assertNotNull(ots.get("a"));
        ots.removeMember("a",2L);
        assertNull(ots.get("a"));
    }


    @Test
    public void testConstructor() {
        ObjectToSet ots = new ObjectToSet();
        assertNotNull(ots);
        assertEquals(0,ots.size());
    }

    @Test
    public void testRemove() {
        ObjectToSet<String,String> ots = new ObjectToSet<String,String>();
        ots.addMember("a","b");
        HashSet s1 = new HashSet();
        s1.add("b");
        assertEquals(s1,ots.get("a"));

        boolean removed = ots.removeMember("a","b");
        assertTrue(removed);
        assertEquals(0,ots.getSet("a").size());
    }

    @Test
    public void testOne() {
        ObjectToSet<String,String> ots = new ObjectToSet<String,String>();

        Set<String> foos = ots.getSet("foo");
        assertEquals(null,ots.get("foo"));
        assertEquals(Collections.EMPTY_SET,ots.getSet("foo"));

        ots.addMember("foo","bar");
        foos = ots.getSet("foo");
        assertEquals(ots.get("foo"),foos);
        assertEquals(1,foos.size());
        assertTrue(foos.contains("bar"));
        assertFalse(foos.contains("fiz"));

        ots.addMember("foo","baz");
        foos = ots.get("foo");
        assertEquals(2,foos.size());
        assertTrue(foos.contains("bar"));
        assertTrue(foos.contains("baz"));
        assertFalse(foos.contains("fiz"));

        ots.addMember("boo","bar");
        foos = ots.get("foo");
        assertEquals(2,foos.size());
        assertTrue(foos.contains("bar"));
        assertTrue(foos.contains("baz"));
        assertFalse(foos.contains("fiz"));
        Set boos = ots.get("boo");
        assertEquals(1,boos.size());
        assertTrue(boos.contains("bar"));
        assertFalse(boos.contains("fiz"));
        ots.addMembers("boo",Collections.EMPTY_SET);
        boos = ots.get("boo");
        assertEquals(1,boos.size());
        assertTrue(boos.contains("bar"));

        HashSet members = new HashSet();
        members.add("1");
        members.add("2");
        ots.addMembers("boo",members);
        boos = ots.get("boo");
        assertEquals(3,boos.size());
        assertTrue(boos.contains("1"));
        assertTrue(boos.contains("bar"));
        assertFalse(boos.contains("fiz"));

        HashSet allMembers = new HashSet();
        allMembers.add("bar");
        allMembers.add("baz");
        allMembers.add("1");
        allMembers.add("2");
        assertEquals(allMembers,ots.memberValues());

    }

    @Test
    public void testMemberIterator() {
    ObjectToSet ots = new ObjectToSet();
    HashSet expected = new HashSet();

    assertMemberIterator(ots,expected,0);

    ots.addMember("a","1");
    expected.add("1");
    assertMemberIterator(ots,expected,1);

    ots.addMember("a","2");
    expected.add("2");
    assertMemberIterator(ots,expected,2);

    ots.addMember("a","3");
    expected.add("3");
    assertMemberIterator(ots,expected,3);

    ots.addMember("b","3");
    assertMemberIterator(ots,expected,4);

    ots.addMember("b","2");
    assertMemberIterator(ots,expected,5);

    ots.addMember("c","4");
    expected.add("4");
    assertMemberIterator(ots,expected,6);
    }

    void assertMemberIterator(ObjectToSet ots, Set expected, int expectedCount) {
    HashSet found = new HashSet();
    Iterator it = ots.memberIterator();
    int count = 0;
    while (it.hasNext()) {
        ++count;
        found.add(it.next());
    }
    assertEquals(expectedCount,count);
    assertEquals(expected,found);
    }

}
