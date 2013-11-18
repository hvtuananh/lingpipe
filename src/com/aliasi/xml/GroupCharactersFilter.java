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

import com.aliasi.util.Strings;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides a SAX filter that groups sequential calls to {@link
 * #characters(char[],int,int)} into a single call with all of the
 * content concatenated.  A flag may be supplied at construction time
 * that will cause all character content that is all whitespace, as
 * defined by {@link Strings#allWhitespace(char[],int,int)} to be
 * ignored.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public class GroupCharactersFilter extends SAXFilterHandler {

    /**
     * True if character content that is all whitespace
     * should be ignored.
     */
    private final boolean mRemoveWhitespace;

    /**
     * Buffer to accumulate characters.
     */
    private StringBuilder mCharAccumulator;

    /**
     * Construct a character grouping filter that delegates events to
     * the specified handler.  The handler may be reset with {@link
     * #setHandler(DefaultHandler)}.
     *
     * @param handler Handler to which events are delegated.
     */
    public GroupCharactersFilter(DefaultHandler handler) {
        this(handler,false);
    }

    /**
     * Construct a character grouping filter without a specified
     * handler to which to delegate events.  Set the handler
     * with {@link #setHandler(DefaultHandler)}.
     */
    public GroupCharactersFilter(DefaultHandler handler,
                                 boolean removeWhitespace) {
        super(handler);
        mRemoveWhitespace = removeWhitespace;
    }

    /**
     * Start the document, delegating the call to the contained
     * handler.
     *
     * @throws SAXException If there is an exception raised by the
     * contained handler.
     */
    @Override
    public void startDocument() throws SAXException {
        mCharAccumulator = new StringBuilder();
        super.startDocument();
    }

    /**
     * End the document, delegating the call to the contained
     * handler, after calling {@link #characters(char[],int,int)} on
     * any accumulated characters.
     *
     * @throws SAXException If there is an exception raised by the
     * contained handler.
     */
    @Override
    public void endDocument() throws SAXException {
        checkCharacters();
        super.endDocument();
    }

    /**
     * Start the specified element, delegating the SAX event to the
     * contained handler, after calling {@link
     * #characters(char[],int,int)} to handle any accumulated
     * characters.
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
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {

        checkCharacters();
        super.startElement(namespaceURI,localName,qName,atts);
    }

    /**
     * Ends the specified element, delegating the SAX event to the
     * contained handler, after calling {@link
     * #characters(char[],int,int)} to handle any accumulated
     * characters.
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
    public void endElement(String namespaceURI, String localName,
                           String qName) throws SAXException {
        checkCharacters();
        super.endElement(namespaceURI,localName,qName);
    }

    /**
     * Adds the characters to the character accumulator.
     *
     * @param ch Array of characters to add to accumulator.
     * @param start First character to accumulate.
     * @param length Number of characters to accumulate.
     */
    @Override
    public final void characters(char[] ch, int start, int length)
        throws SAXException {

        mCharAccumulator.append(ch,start,length);
    }

    /**
     * Checks before starting an element or ending the document
     * to see if there are accumulated characters that need to
     * be handled.  If the characters are all whitespace and the
     * ignore whitespace flag is set, they are ignored.
     *
     * @throws SAXException If there is an exception raised by
     * the contained handler when handling the characters.
     */
    private void checkCharacters() throws SAXException {
        if (mCharAccumulator.length() == 0) return;
        if (mRemoveWhitespace && Strings.allWhitespace(mCharAccumulator)) {
            mCharAccumulator = new StringBuilder();
            return;
        }
        super.characters(mCharAccumulator.toString().toCharArray(),
                         0,mCharAccumulator.length());
        mCharAccumulator = new StringBuilder();
    }



}
