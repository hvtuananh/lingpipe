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

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The <code>SAXFilterHandler</code> provides a base class for
 * filtering SAX handlers.  A SAX filter handler behaves like other
 * output stream filters such as {@link java.io.FilterOutputStream}.
 * A filter acts as a container for a handler, which may be supplied
 * at construction time or set.  The filter typically receives SAX
 * events and processes them in some way before passing them to the
 * contained handler.  This particular filter implements the content
 * handler component of {@link org.xml.sax.helpers.XMLFilterImpl}.
 *
 * <p>
 * The contained
 * handler may be changed with {@link #setHandler(DefaultHandler)},
 * thus allowing reuse of filters.  The implementation of all methods
 * in this class delegates the methods to the contained handler, so an
 * extending class need only implement the handler methods that it
 * would like to filter.  The contained handler is protected and may
 * be accessed directly by subclasses; its methods may be accessed
 * through <code>super</code>.
 *
 * <P> <b>Warning:</b> The Sun JDK 1.4.2 implementation of {@link
 * DefaultHandler} is defective in not declaring an {@link
 * java.io.IOException} to be thrown by {@link
 * DefaultHandler#resolveEntity(String,String)}.  See the method
 * documentation for {@link #resolveEntity(String,String)} for a
 * description of this class's implemented workaround.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public class SAXFilterHandler extends SimpleElementHandler {

    /**
     * The contained default handler to which events are delegated by
     * default.  Subclasses may also access its methods by means of
     * <code>super</code> invocations.
     */
    protected DefaultHandler mHandler;

    /**
     * Construct a filter handler with the specified contained
     * handler.
     *
     * @param handler The handler to be contained by the constructed
     * filter.
     */
    public SAXFilterHandler(DefaultHandler handler) {
        mHandler = handler;
    }

    /**
     * Construct a filter handler that contains the no-op handler
     * {@link #NO_OP_DEFAULT_HANDLER}.
     */
    public SAXFilterHandler() {
        this(NO_OP_DEFAULT_HANDLER);
    }

    /**
     * Sets the contained handler to the specified value.
     *
     * @link handler New contained handler.
     */
    public void setHandler(DefaultHandler handler) {
        mHandler = handler;
    }

    // ContentHandler Implementation

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void startDocument() throws SAXException {
        mHandler.startDocument();
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void endDocument() throws SAXException {
        mHandler.endDocument();
    }

    /**
     * Call delegated to {@link #mHandler}.
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

        mHandler.startElement(namespaceURI,localName,qName,atts);
    }

    /**
     * Call delegated to {@link #mHandler}.
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

        mHandler.endElement(namespaceURI,localName,qName);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param ch Character array containing characters to handle.
     * @param start Index of first character to handle.
     * @param length Number of characters to handle.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        mHandler.characters(ch,start,length);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param ch Character array containing characters to handle.
     * @param start Index of first character to handle.
     * @param length Number of characters to handle.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {

        mHandler.ignorableWhitespace(ch,start,length);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or
     * <code>null</code> if none is supplied.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void processingInstruction(String target, String data)
        throws SAXException {

        mHandler.processingInstruction(target,data);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param prefix The namespace prefix being declared.
     * @param uri The namespace URI mapped to the prefix.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

        mHandler.startPrefixMapping(prefix,uri);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param prefix The namespace prefix being declared.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        mHandler.endPrefixMapping(prefix);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param name The name of the skipped entity.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        mHandler.skippedEntity(name);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param locator A locator for all SAX document events.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        mHandler.setDocumentLocator(locator);
    }

    /**
     * Call delegated to {@link #mHandler}.  
     *
     * <P>Note that this method only throws SAX exceptions, whereas
     * the SAX specification allows it to also throw an I/O exception.
     * In this class's implementation, all I/O exceptions thrown by
     * the delegate are converted to SAX exceptions before being
     * re-thrown by this method.  This extreme measure is taken because
     * a bug in JDK 1.4.x failed to delcare an I/O exception on the
     * resolve entity method.  Although Sun's 1.5 JDK does declare the
     * exception, this method converts it to allow the same source
     * to compile in 1.4 and 1.5.  For more information on this bug,
     * see Sun's bug report:
     *
     * <blockquote>
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4749727"
     *   >resolveEntity in DefaultHandler does not throw IOException</a>
     * </blockquote>
     *
     * @param publicId The public identifier, or <code>null</code> if
     * none is available.
     * @param systemId The system identifier provided in the XML
     * document.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {

        try {
            return mHandler.resolveEntity(publicId,systemId);
        } catch (Throwable t) {
            io2SAXException(t);
            return null; // unreachable semantically given io2SAXException
        }
    }

    /**
     * Throw the specified throwable, and if it is an IOException,
     * wrap it in a SAXException first.  All runtime exceptions and
     * errors are thrown as is.  This method only exists to code
     * around the JDK 1.4.2 bug as documented in {@link
     * #resolveEntity(String,String)}.
     *
     * @param t Throwable to coerce.
     * @throws SAXException If the specified throwable is a SAX
     * exception or an IOException.
     */
    static void io2SAXException(Throwable t)
        throws SAXException {

        if (t instanceof SAXException)
            throw (SAXException) t;
        if (t instanceof IOException)
            throw new SAXException("Converting IO to SAX exception",
                                   (IOException) t);
        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
        if (t instanceof Error)
            throw (Error) t;
        throw new Error("Unexpected unchecked, non-error, non-runtime exception throwable",
                        t);
    }


    // ErrorHandler

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param exception The error information, encoded as an exception.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void error(SAXParseException exception) throws SAXException {
        mHandler.error(exception);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param exception The fatal error information, encoded as an
     * exception.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void fatalError(SAXParseException exception)
        throws SAXException {

        mHandler.fatalError(exception);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param exception The warning information, encoded as an exception.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        mHandler.warning(exception);
    }


    // DTD Handler

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param name The notation name.
     * @param publicId The notation public identifier, or
     * <code>null</code> if none is available.
     * @param systemId The notation system identifier.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void notationDecl(String name, String publicId,
                             String systemId)
        throws SAXException {

        mHandler.notationDecl(name,publicId,systemId);
    }

    /**
     * Call delegated to {@link #mHandler}.
     *
     * @param name The entity name.
     * @param publicId The entity public identifier, or
     * <code>null</code> if none is available.
     * @param systemId The entity system identifier.
     * @param notationName The name of the associated notation.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    @Override
    public void unparsedEntityDecl(String name, String publicId,
                                   String systemId, String notationName)
        throws SAXException {

        mHandler.unparsedEntityDecl(name,publicId,systemId,notationName);
    }

}
