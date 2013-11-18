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

import com.aliasi.util.Pair;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static com.aliasi.test.unit.Asserts.assertFullEquals;


public class PairTest  {

    @Test
    public void test1() {
	Pair<String,String> p1 = new Pair("a","b");
	Pair<CharSequence,CharSequence> p2 = new Pair("a","b");
	assertFullEquals(p1,p2);


	Pair<Integer,Integer> p3 = new Pair(1,2);
	assertFalse(p3.equals(p1));
	assertFalse(p1.equals(p3));

	Pair<Integer,String> p4 = new Pair(1,"foo");
	assertEquals(Integer.valueOf(1),p4.a());
	assertEquals("foo",p4.b());
    }

    

}
