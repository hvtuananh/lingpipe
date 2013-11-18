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

import com.aliasi.xml.SAXFilterHandler;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import com.aliasi.test.unit.MockObjectHelper;

import com.aliasi.util.Tuple;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class SimpleElementHandlerTest  {

    @Test
    public void testSimpleElement() throws SAXException {

        FilterHandlerTester filter = new FilterHandlerTester();
        MockDefaultHandler mockHandler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        filter.setHandler(mockHandler);

        Attributes atts = SAXFilterHandler.createAttributes("att","val");
        filter.startSimpleElement("foo",atts);
        helper.add("startElement",null,"foo","foo",
                   MockDefaultHandler.toString(atts));

        filter.startSimpleElement("elt");
        helper.add("startElement",null,"elt","elt",
                   MockDefaultHandler.EMPTY_ATTS_STRING);


        filter.endSimpleElement("elt");
        helper.add("endElement",null,"elt","elt");

        assertEquals(helper.getCalls(),mockHandler.getCalls());
    }

    @Test
    public void testSingleElementAtts() throws SAXException {
        FilterHandlerTester filter = new FilterHandlerTester();
        MockDefaultHandler mockHandler = new MockDefaultHandler();
        filter.setHandler(mockHandler);

        filter.startSimpleElement("bar","att","val");

        ArrayList calls = mockHandler.getCalls();
        assertEquals(((Tuple) calls.get(0)).get(4),
                     "att=val");
    }

    @Test
    public void testCreateAtts() {
        Attributes atts = SAXFilterHandler.createAttributes("foo","bar");
        assertEquals(1,atts.getLength());
        assertEquals("bar",atts.getValue("foo"));
        assertEquals("CDATA",atts.getType(0));
        assertEquals("foo",atts.getQName(0));
        assertEquals("foo",atts.getLocalName(0));
        assertEquals(null,atts.getURI(0));
        assertEquals("bar",atts.getValue(0));

        Attributes atts2 = SAXFilterHandler.createAttributes("foo1","bar1",
                                                             "foo2","bar2");
        assertEquals(2,atts2.getLength());
        assertEquals("bar1",atts2.getValue("foo1"));
        assertEquals("bar2",atts2.getValue("foo2"));

        AttributesImpl impl = new AttributesImpl();
        assertEquals(0,impl.getLength());
        SAXFilterHandler.addSimpleAttribute(impl,"ping","pong");
        SAXFilterHandler.addSimpleAttribute(impl,"base","ball");
        assertEquals(2,impl.getLength());
        assertEquals("pong",impl.getValue("ping"));
        assertEquals("ball",impl.getValue("base"));
    }

    static class FilterHandlerTester extends SAXFilterHandler {

        public DefaultHandler noOpHandler() {
            return NO_OP_DEFAULT_HANDLER;
        }

    }



}
