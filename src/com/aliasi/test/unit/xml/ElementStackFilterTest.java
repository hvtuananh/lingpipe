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
import static junit.framework.Assert.assertTrue;


import com.aliasi.xml.ElementStackFilter;
import com.aliasi.xml.SimpleElementHandler;

import java.util.EmptyStackException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ElementStackFilterTest  {

    @Test
    public void test1() throws SAXException {
        MockDefaultHandler handler = new MockDefaultHandler();

        ElementStackFilter filter
            = new ElementStackFilter(handler);

        boolean threw = false;
        try {
            filter.currentElement();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            filter.currentAttributes();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);

        filter.startDocument();

        threw = false;
        try {
            filter.currentElement();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            filter.currentAttributes();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);

        filter.startElement(null,"foo","foo",
                            SimpleElementHandler.EMPTY_ATTS);
        assertEquals("foo",filter.currentElement());

        filter.characters("foobar");

        assertEquals("foo",filter.currentElement());
        assertEqualsAtts(SimpleElementHandler.EMPTY_ATTS,
                         filter.currentAttributes());

        filter.startElement(null,"bar","biz:baz",

                            SimpleElementHandler.EMPTY_ATTS);

        assertEquals("biz:baz",filter.currentElement());


        filter.endElement(null,"bar","biz:baz");
        filter.endElement(null,"foo","foo");
        filter.endDocument();

        threw = false;
        try {
            filter.currentElement();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            filter.currentAttributes();
        } catch (EmptyStackException e) {
            threw = true;
        }
        assertTrue(threw);

    }


    void assertEqualsAtts(Attributes atts1, Attributes atts2) {
        assertEquals(atts1.getLength(), atts2.getLength());
        for (int i = 0; i < atts1.getLength(); ++i) {
            // account for order variation by testing as map
            String att1QName = atts1.getQName(i);
            assertEquals(atts1.getValue(att1QName),
                         atts2.getValue(att1QName));
            String att2QName = atts2.getQName(i);
            assertEquals(atts1.getValue(att2QName),
                         atts2.getValue(att2QName));
        }
    }

}
