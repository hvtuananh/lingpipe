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

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A filter that applies an operation to text content in specified
 * elements.  Elements for which text should be filtered are specified
 * using {@link #filterElement(String)}.  The operation to apply to
 * character content for these elements is defined by an
 * implementation in a subclass of {@link
 * #filteredCharacters(char[],int,int)}.  Unfiltered characters
 * and all other SAX events are delegated to the contained handler.
 * To process text in contiguous chunks, wrap instances of
 * <code>TextContentFilter</code> as contained handler in an instance
 * of {@link GroupCharactersFilter}.
 *
 * <P>The elements that are filtered are specified by qualified
 * element name.  If there are no namespace qualifications, the name
 * will be unqualified.  For instance, the following document contains
 * two <code>bar</code> elements with the same URI:
 *
 * <pre>
 *   &lt;foo&gt;
 *     &lt;a:bar xmlns:a=&quot;http://one&quot;&gt;xyz&lt;/a:bar&gt;
 *     &lt;b:bar xmlns:b=&quot;http://one&quot;&gt;uvw&lt;/b:bar&gt;
 *   &lt;/foo&gt;
 * </pre>
 *
 * In order to filter the content of element <code>bar</code>, both
 * prefixes need to be specified: <code>a:bar</code> and
 * <code>b:bar</code> and other equivalent versions will not be recognized.
 * There is no way to properly handle a document such as the 
 * following, where the two qualified element names are the same,
 * but the elements are different:
 *
 * <pre>
 *   &lt;foo&gt;
 *     &lt;a:bar xmlns:a=&quot;http://one&quot;&gt;xyz&lt;/a:bar&gt;
 *     &lt;a:bar xmlns:a=&quot;http://two&quot;&gt;uvw&lt;/a:bar&gt;
 *   &lt;/foo&gt;
 * </pre>
 *
 * For this document, specifying <code>a:bar</code> will pick up the
 * <code>bar</code> element from both the <code>http://one</code>
 * and <code>http://two</code> namespaces.
 *
 * <P>Because this filter requires qualified names, the XML parser
 * must set the following SAX2 feature to <code>true</code>:
 *
 * <blockquote>
 *   http://xml.org/sax/features/namespace-prefixes
 * </blockquote>
 *
 * See the <a
 * href="http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html#package_description">SAX2
 * Feature Specification</a> for information on this and other features.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public abstract class TextContentFilter extends ElementStackFilter {

    /**
     * Set of elements to filter.
     */
    private final Set<String> mFilteredElements = new HashSet<String>();

    /**
     * Construct a text content filter which passes events
     * to the specified handler.
     *
     * @param handler Contained handler to which events are passed.
     */
    public TextContentFilter(DefaultHandler handler) {
        super(handler);
    }

    /**
     * Construct a text content filter without a specified contained
     * handler.  Set the contained handler using {@link
     * #setHandler(DefaultHandler)}.
     */
    public TextContentFilter() {
        super();
    }

    /**
     * Filter the text content of elements with the specified
     * qualified name.
     *
     * @param qName Qualified name of elements to filter.
     */
    public void filterElement(String qName) {
        mFilteredElements.add(qName);
    }

    /**
     * Handle character content, delegating unfiltered characters
     * to the contained handler, and delegating filtered characters
     * to {@link #filteredCharacters(char[],int,int)}.
     *
     * @param cs Array of characters to filter.
     * @param start First character to filter.
     * @param length Number of characters to filter.
     * @throws SAXException If there is an exception from the
     * contained handler or from the filtered characters method.
     */
    @Override
    public void characters(char[] cs, int start, int length)
        throws SAXException {

        if (mFilteredElements.contains(currentElement())) {
            filteredCharacters(cs,start,length);
        } else {
            super.characters(cs,start,length);
        }
    }


    /**
     * Handle filtered character content.  This method will be called
     * on all text content of elements specified using {@link
     * #filterElement(String)}.  It is important that this method
     * not invoke {@link #characters(char[],int,int)}, either directly
     * through a <code>super</code> call from a subclass; instead,
     * access the embedded handler {@link #mHandler} directly.
     *
     * @param cs Array of characters to filter.
     * @param start First character to filter.
     * @param length Number of characters to filter.
     * @throws SAXException If there is an exception handling the
     * characters.
     */
    public abstract void filteredCharacters(char[] cs,
                                            int start, int length)
        throws SAXException;

}
