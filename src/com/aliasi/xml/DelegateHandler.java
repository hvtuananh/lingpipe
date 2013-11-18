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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>DelegateHandler</code> may be used with a delegating
 * handler to more efficiently implement nested embeddings.  A
 * delegate handler contains a pointer back to its delegating handler
 * in order to coordinate further delegation from the delegate
 * handler.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public class DelegateHandler extends SimpleElementHandler {

    private final Map<String,DefaultHandler> mDelegateMap = new HashMap<String,DefaultHandler>();
    final DelegatingHandler mDelegatingHandler;

    /**
     * Construct a delegate handler that coordinates recursive
     * delegation with the specified delegating handler.
     */
    public DelegateHandler(DelegatingHandler handler) {
        mDelegatingHandler = handler;
    }

    /**
     * Calling this method causes subsequent events embedded within
     * the specified element to be delegated to the specified handler.
     * If the events will might be further delegated, this handler
     * should itself be a delegate handler.  If the specified handler
     * is a delegate handler, it must be tied to the same delegating
     * handler as this delegate handler.
     *
     * @param qName Qualified name of element.
     * @param handler Handler to accept delegated events.
     * @throws IllegalArgumentException If the handler is a delegate
     * handler that is tied to a different delegating handler.
     */
    public void setDelegate(String qName, DefaultHandler handler) {
        if (handler instanceof DelegateHandler
            && ( mDelegatingHandler
                 != ((DelegateHandler)handler).mDelegatingHandler)) {
            String msg = "Delegate handlers must wrap the same delegating handler.";
            throw new IllegalArgumentException(msg);
        }
        mDelegateMap.put(qName,handler);
    }

    DefaultHandler getDelegate(String qName) {
        return mDelegateMap.get(qName);
    }

    /**
     * This method is called when this handler has finished delegating
     * the specified element to the specified handler.  This
     * implementation is a simple no-operation adapter; subclasses
     * should override it if they need to take action on delegate
     * completion.  This is most typically used for arranging data
     * collected by delegates results into larger data structures.
     *
     * @param qName Qualified name of element.
     * @param handler Name of handler that handled the delegated
     * events.
     */
    public void finishDelegate(String qName, DefaultHandler handler) {
        /* do nothing by default */
    }

    /**
     * Starts the specified element with the specified attributes.
     * This method is used to coordinate delegation, and any subclasses
     * implementing it should first make a call to this implementation
     * through the <code>super</code> construct.
     *
     * @param namespaceURI The URI identifying the name space, or
     * <code>null</code> if there isn't one.
     * @param localName Local name of element.
     * @param qName Qualified name of element, which is prefixed with
     * the name space URI and a colon if it is non-null, and is equal
     * to local name if there is no name space specified.
     * @param atts Attributes for this element.
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {

        DefaultHandler handler = getDelegate(qName);
        if (handler == null) return;
        handler.startDocument();
        handler.startElement(namespaceURI,localName,qName,atts);
        int top = ++mDelegatingHandler.mStackTop;
        mDelegatingHandler.mQNameStack[top] = qName;
        mDelegatingHandler.mDelegateStack[top] = handler;
    }

}
