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

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.ext.Attributes2Impl;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides a SAX filter that maintains a stack of elements visited.
 * The qualified name of the current element and attributes are
 * available through {@link #currentElement()} and {@link
 * #currentAttributes()} respectively.
 *
 * <p>The implementation uses {@link Attributes2Impl} to copy the
 * attributes from each element.  This defends against the policy of
 * some SAX parsers to re-use elements.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class ElementStackFilter extends SAXFilterHandler {

    /**
     * The stack of qualified element names.
     */
    private final Stack<String> mElementStack = new Stack<String>();

    /**
     * The stack of attributes.
     */
    private final Stack<Attributes> mAttributesStack = new Stack<Attributes>();

    /**
     * Construct an element stack filter that delegates
     * events to the specified handler.  The handler may
     * be reset with {@link #setHandler(DefaultHandler)}.
     *
     * @param handler Handler to which events are delegated.
     */
    public ElementStackFilter(DefaultHandler handler) {
        super(handler);
    }

    /**
     * Construct an element stack filter without a specified
     * handler to which to delegate events.  Set the handler
     * with {@link #setHandler(DefaultHandler)}.
     */
    public ElementStackFilter() {
        super();
    }

    /**
     * Returns an unmodifiable view of the stack of qualified names of
     * elements.  The view will change as the underlying stack
     * changes.
     *
     * <P>The elements are indexed from the bottom of the stack
     * to the top.  So <code>getElementStack().size()-1</code> is
     * the index of the top of a non-empty stack, and <code>0</code>
     * is the index of the bottom of a non-empty stack.
     *
     * <P>See {@link #getAttributesStack()} for
     * a method to return the corresponding attributes for the elements.
     *
     * @return The stack of elements.
     */
    public List<String> getElementStack() {
        return Collections.<String>unmodifiableList(mElementStack);
    }

    /**
     * Returns an unmodifiable view of the stack of attributes
     * associated with the stack of elements.  The view will change as
     * the underlying stack changes.
     *
     * <P>The members of this stack are implementations of
     * the XML SAX {@link Attributes} interface.
     *
     * <P>The elements are indexed from the bottom of the stack
     * to the top.  So <code>getAttributesStack().size()-1</code> is
     * the index of the top of a non-empty stack, and <code>0</code>
     * is the index of the bottom of a non-empty stack.
     *
     * <P>See {@link #getElementStack()}
     * to get a parallel stack of qualified element names.
     *
     * @return The stack of attributes.
     */
    public List<Attributes> getAttributesStack() {
        return Collections.<Attributes>unmodifiableList(mAttributesStack);
    }

    /**
     * Start the document, clearing the element stack
     * and delegating the start document even to the contained
     * handler.
     *
     * @throws SAXException If there is an exception raised by the
     * contained handler.
     */
    @Override
    public void startDocument() throws SAXException {
        mElementStack.clear();
        super.startDocument();
    }

    /**
     * Start the specified element, adding its qualified name
     * and attributes to the stack, and delegating the SAX event
     * to the contained handler.
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     * @param atts The attributes for this element.
     * @throws SAXException If there is an exception raised by the
     * contained handler.
     */
    @Override
    public void startElement(String namespaceURI,
                             String localName,
                             String qName, Attributes atts)
        throws SAXException {

        mElementStack.push(qName);
        mAttributesStack.push(copy(atts));
        super.startElement(namespaceURI,localName,qName,atts);
    }

    /**
     * End the specified element, popping its qualified name
     * and attributes off the stack, and delegating the SAX event
     * to the contained handler.
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     * @throws SAXException If there is an exception raised by the
     * contained handler.
     */
    @Override
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
        throws SAXException {

        mElementStack.pop();
        mAttributesStack.pop();
        super.endElement(namespaceURI,localName,qName);
    }

    /**
     * Returns <code>true</code> if there are no elements in the stack.
     * Should only return <code>true</code> outside of the top-level
     * element.
     *
     * @return <code>true</code> if there are no elements in the stack.
     */
    public boolean noElement() {
        return mElementStack.isEmpty();
    }


    /**
     * Returns the qualified name of the current containing element's
     * qualified name.  Should only be called when there is an element.
     *
     * @return Qualified name of current element.
     * @throws EmptyStackException If there is no containing element.
     */
    public String currentElement() {
        return mElementStack.peek();
    }

    /**
     * Returns the attributes as specified for the current containing
     * element. Should only be called when there is an element.
     *
     * @return Qualified name of current element.
     * @throws EmptyStackException If there is no containing element.
     */
    public Attributes currentAttributes() {
        return mAttributesStack.peek();
    }

    static Attributes copy(Attributes atts) {
        return new Attributes2Impl(atts);
    }

}
