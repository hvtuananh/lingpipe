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
import static junit.framework.Assert.assertNotNull;

import com.aliasi.test.unit.MockObjectHelper;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.LocatorImpl;

public class SAXFilterHandlerTest  {

    @Test
    public void testSetHandler() throws SAXException {
        SAXFilterHandler handler = new SAXFilterHandler();
        handler.setHandler(new MockDefaultHandler());
        handler.startDocument();
        handler.endDocument();
    }

    @Test
    public void test1() {
        MockDefaultHandler handler = new MockDefaultHandler();
        MockObjectHelper helper = new MockObjectHelper();
        assertEquals(helper.getCalls(),handler.getCalls());

        SAXFilterHandler filter = new SAXFilterHandler(handler);
        assertNotNull(filter);

        handler.startDocument();
        helper.add("startDocument");

        handler.startElement(null,"foo","foo",
                             SAXFilterHandler.EMPTY_ATTS);
        helper.add("startElement",null,"foo","foo",
                   MockDefaultHandler.EMPTY_ATTS_STRING);

        handler.processingInstruction("instrux","data");
        helper.add("processingInstruction","instrux","data");

        handler.processingInstruction("instrux",null);
        helper.add("processingInstruction","instrux",null);

        handler.startPrefixMapping("prefix","uri17");
        helper.add("startPrefixMapping","prefix","uri17");

        Attributes attsAB = SAXFilterHandler.createAttributes("A","B");
        handler.startElement("baz","boo","baz:boo",
                             attsAB);
        helper.add("startElement","baz","boo","baz:boo",
                   MockDefaultHandler.toString(attsAB));

        char[] chars = "This is some text.".toCharArray();
        handler.characters(chars,5,12);
        helper.add("characters",new String(chars,5,12));

        char[] whites = "     ".toCharArray();
        handler.ignorableWhitespace(whites,1,5);
        helper.add("ignorableWhitespace",whites,Integer.valueOf(1),Integer.valueOf(5));

        handler.resolveEntity("publicId","systemId");
        helper.add("resolveEntity","publicId","systemId");

        handler.resolveEntity(null,"systemId2");
        helper.add("resolveEntity",null,"systemId2");

        handler.endElement("baz","boo","baz:boo");
        helper.add("endElement","baz","boo","baz:boo");

        handler.endPrefixMapping("prefix");
        helper.add("endPrefixMapping","prefix");

        handler.endElement(null,"foo","foo");
        helper.add("endElement",null,"foo","foo");

        handler.skippedEntity("skipper");
        helper.add("skippedEntity","skipper");

        Locator locator = new LocatorImpl();
        handler.setDocumentLocator(locator);
        helper.add("setDocumentLocator",locator);

        SAXParseException exception = new SAXParseException("msg", locator);
        handler.fatalError(exception);
        helper.add("fatalError",exception);

        handler.error(exception);
        helper.add("error",exception);

        handler.warning(exception);
        helper.add("warning",exception);

        handler.endDocument();
        helper.add("endDocument");

        handler.notationDecl("foo","bar","baz");
        helper.add("notationDecl","foo","bar","baz");

        handler.notationDecl("foo",null,"baz");
        helper.add("notationDecl","foo",null,"baz");

        handler.unparsedEntityDecl("name","foo","bar","baz");
        helper.add("unparsedEntityDecl","name","foo","bar","baz");

        handler.unparsedEntityDecl("name",null,"bar","baz");
        helper.add("unparsedEntityDecl","name",null,"bar","baz");

        assertEquals(helper.getCalls(),handler.getCalls());

    }



}
