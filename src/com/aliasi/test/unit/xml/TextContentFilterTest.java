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

import com.aliasi.test.unit.MockObjectHelper;

import com.aliasi.xml.TextContentFilter;
import com.aliasi.xml.SimpleElementHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TextContentFilterTest  {

    @Test
    public void test1() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        TextContentFilter filter
            = new TestTextFilter(handler);
        filter.filterElement("a");

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
        TextContentFilter filter
            = new TestTextFilter(handler);
        filter.filterElement("foo");

        filter.startDocument();
        helper.add("startDocument");

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);

        helper.add("startElement",null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.characters("abc");
        helper.add("characters","ABC");

        filter.startElement(null,"bar","bar",
                            SimpleElementHandler.EMPTY_ATTS);

        helper.add("startElement",null,"bar","bar",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        filter.characters("xyz");
        helper.add("characters","xyz");

        filter.endElement(null,"bar","bar");
        helper.add("endElement",null,"bar","bar");


        filter.characters("mno");
        helper.add("characters","MNO");



        filter.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        filter.endDocument();
        helper.add("endDocument");
        assertEquals(helper.getCalls(),handler.getCalls());
    }


    private static class TestTextFilter extends TextContentFilter {

        public TestTextFilter() {
            /* do nothing */
        }

        public TestTextFilter(DefaultHandler handler) {
            super(handler);
        }

        @Override
        public void filteredCharacters(char[] cs, int start, int length)
            throws SAXException {

            char[] newChars = new String(cs,start,length).toUpperCase().toCharArray();
            mHandler.characters(newChars,0,newChars.length);
        }
    }


}
