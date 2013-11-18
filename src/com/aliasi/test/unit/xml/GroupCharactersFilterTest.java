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

package com.aliasi.test.unit.xml;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import com.aliasi.test.unit.MockObjectHelper;

import com.aliasi.xml.GroupCharactersFilter;
import com.aliasi.xml.SimpleElementHandler;

import org.xml.sax.SAXException;

public class GroupCharactersFilterTest  {

    @Test
    public void test1() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        GroupCharactersFilter filter
            = new GroupCharactersFilter(handler);
        assertNotNull(filter);

        filter.startDocument();
        helper.add("startDocument");
        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());

    }

    @Test
    public void test2() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        GroupCharactersFilter filter
            = new GroupCharactersFilter(handler);
        assertNotNull(filter);

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",
                   null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());

    }

    @Test
    public void test3() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        GroupCharactersFilter filter
            = new GroupCharactersFilter(handler);
        assertNotNull(filter);

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",
                   null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);


        filter.characters("abc");
        helper.add("characters","abc");

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());

    }

    @Test
    public void test4() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        GroupCharactersFilter filter
            = new GroupCharactersFilter(handler);
        assertNotNull(filter);

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",
                   null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.characters("abc");
        filter.characters("def");
        filter.characters("");


        helper.add("characters","abcdef");

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());

    }

    @Test
    public void test5() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        GroupCharactersFilter filter
            = new GroupCharactersFilter(handler,true);
        assertNotNull(filter);

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",
                   null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.characters("abc");
        filter.characters("def");
        filter.characters("");

        helper.add("characters","abcdef");

        filter.startElement(null,"bar","bar",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",null,"bar","bar",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.endElement(null,"bar","bar");
        helper.add("endElement",null,"bar","bar");

        filter.characters("xyz");
        helper.add("characters","xyz");

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());

    }


    @Test
    public void testIgnoreSpaces() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        GroupCharactersFilter filter
            = new GroupCharactersFilter(handler,true);
        assertNotNull(filter);

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",
                   null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.characters("   ");
        filter.characters("\n");
        filter.characters("");

        filter.startElement(null,"bar","bar",
                            SimpleElementHandler.EMPTY_ATTS);
        helper.add("startElement",null,"bar","bar",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.endElement(null,"bar","bar");
        helper.add("endElement",null,"bar","bar");

        filter.characters("xyz");
        helper.add("characters","xyz");

        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.characters("    ");

        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());

    }


}
