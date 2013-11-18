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

package com.aliasi.xml;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/** A <code>RemoveElementsFilter</code> filters out specified elements
 * from a stream of SAX events.  The elements to remove are specified
 * by qualified name with the method {@link #removeElement(String)}.
 * The content of the elements is not removed.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class RemoveElementsFilter extends SAXFilterHandler {

    /**
     * Set of elements to remove.
     */
    private final Set<String> mElementsToRemove = new HashSet<String>();

    /**
     * Construct a filter to remove elements.  Elements to be removed
     * should be defined using {@link #removeElement(String)}.  The
     * handler to receive events should be set with {@link
     * #setHandler(DefaultHandler)}.
     */
    public RemoveElementsFilter() { 
        /* nothing to do */
    }

    /**
     * Construct a filter to remove elements and pass events to
     * the specified handler.  Elements to be removed
     * should be defined using {@link #removeElement(String)}.
     */
    public RemoveElementsFilter(DefaultHandler handler) {
        super(handler);
    }

    /**
     * Add the specified qualified element name to the
     * set of elements to remove.
     *
     * @param qName Qualified name of element to remove.
     */
    public void removeElement(String qName) {
        mElementsToRemove.add(qName);
    }

    /**
     * Removes specified elements and passes others through to
     * the contained handler.
     *
     * @param namespaceURI The URI identifying the name space, or
     * <code>null</code> if there isn't one.
     * @param localName Local name of element.
     * @param qName Qualified name of element, which is prefixed with
     * the name space URI and a colon if it is non-null, and is equal
     * to local name if there is no name space specified.
     * @param atts Attributes for this element.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {
        if (!mElementsToRemove.contains(qName))
            mHandler.startElement(namespaceURI,localName,qName,atts);
    }

    /**
     * Removes specified elements, passing others to the contained
     * handler.
     *
     * @param namespaceURI The URI identifying the name space, or
     * <code>null</code> if there isn't one.
     * @param localName Local name of element.
     * @param qName Qualified name of element, which is prefixed with
     * the name space URI and a colon if it is non-null, and is equal
     * to local name if there is no name space specified.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void endElement(String namespaceURI, String localName,
                           String qName)
        throws SAXException {

        if (!mElementsToRemove.contains(qName))
            mHandler.endElement(namespaceURI,localName,qName);
    }

}
