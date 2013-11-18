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

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An <code>XHtmlWriter</code> provides constants and convenience
 * methods for generating strict XHTML 1.0 output.  This class includes
 * the Strict DTD specification:
 *
 * <blockquote>
 * <code>
 * &lt;!DOCTYPE html
 *     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 *      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"&gt;
 * </code>
 * </blockquote>
 *
 * The XML version and decoding declaration will be included as in
 * the parent class {@link SAXWriter}, as in:
 *
 * <blockquote>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * </code>
 * </blockquote>
 * 
 * <p>The parent class <code>SAXWriter</code> is set to
 * use XHTML mode (see {@link SAXWriter#SAXWriter(OutputStream,String,boolean)})
 * output.
 * 
 * <p>It is up to the application to supply an appropriate document
 * structure and to validate any output.  For instance, the top-level
 * element must be <code>html</code>, and it must specify the
 * appropriate namespace and language, as in:
 *
 * <blockquote>
 * <code>
 * &lt;html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en"&gt;
 * </code>
 * </blockquote>
 * 
 * <p>A constant is provided for each legal XHTML element tag and each
 * XHTML attribute.  There are six convenience methods supplied to
 * start each element.  The first five methods allow from zero to four
 * pairs of attributes and values to be supplied as strings.  The
 * sixth method accepts an arbitrary SAX {@link Attributes} object to
 * supply the attributes.  Elements must be closed using the
 * underlying SAXWriter's end methods, such as {@link
 * #endSimpleElement(String)}.
 * 
 * <p>Like for SAXWriter, the underlying output stream must be closed
 * by the calling application after the {@link #endDocument()} method
 * is called.
 *
 * <p><b>Warning:</b> No well-formedness checks are performed on the
 * output.  In particular, elements may not be balanced leading to
 * invalid XML, or elements may not provide attributes matching the
 * XHTML 1.0 specification.  We suggest validating HTML using the W3C
 * validator.
 * 
 * <ul>
 * <li><a href="http://www.w3.org/TR/xhtml1/">Official XHTML 1.0 Specification</a> (from W3C)
 * <li><a href="http://validator.w3.org/">W3C XHTML Validator</a></li>
 * <li><a href="http://www.w3schools.com/xhtml/default.asp">W3 Schools XHTML Tutorial</a></li> (W3 Schools is not affiliated with the W3C -- they're
 * a <a href="http://www.w3schools.com/about/about_refsnes.asp">small Norwegian outfit</a>)</li>

 * </ul>
 *
 * @author  Bob Carpenter
 * @version 2.3.1
 * @since   LingPipe2.3.1
 */
public class XHtmlWriter extends SAXWriter {

    public XHtmlWriter(OutputStream out, String charsetName) 
        throws UnsupportedEncodingException {

        super(out,charsetName,XHTML_MODE);
        setDTDString(XHTML_1_0_STRICT_DTD);

    }

    public XHtmlWriter() {
        super(XHTML_MODE);
        setDTDString(XHTML_1_0_STRICT_DTD);
    }

    /**
     * Constant for element <code>a</code>.
     */
    public static final String A = "a";
    /**
     * Start an <code>a</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void a() throws SAXException {
        a(EMPTY_ATTS);
    }
    /**
     * Start an <code>a</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void a(Attributes atts) throws SAXException {
        startSimpleElement(A,atts);
    }
    /**
     * Start an <code>a</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void a(String att, String val) throws SAXException {
        startSimpleElement(A,att,val);
    }
    /**
     * Start an <code>a</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void a(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(A,att1,val1,att2,val2);
    }
    /**
     * Start an <code>a</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void a(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(A,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>a</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void a(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(A,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>abbr</code>.
     */
    public static final String ABBR = "abbr";
    /**
     * Start an <code>abbr</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void abbr() throws SAXException {
        abbr(EMPTY_ATTS);
    }
    /**
     * Start an <code>abbr</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void abbr(Attributes atts) throws SAXException {
        startSimpleElement(ABBR,atts);
    }
    /**
     * Start an <code>abbr</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void abbr(String att, String val) throws SAXException {
        startSimpleElement(ABBR,att,val);
    }
    /**
     * Start an <code>abbr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void abbr(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(ABBR,att1,val1,att2,val2);
    }
    /**
     * Start an <code>abbr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void abbr(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(ABBR,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>abbr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void abbr(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(ABBR,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>acronym</code>.
     */
    public static final String ACRONYM = "acronym";
    /**
     * Start an <code>acronym</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void acronym() throws SAXException {
        acronym(EMPTY_ATTS);
    }
    /**
     * Start an <code>acronym</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void acronym(Attributes atts) throws SAXException {
        startSimpleElement(ACRONYM,atts);
    }
    /**
     * Start an <code>acronym</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void acronym(String att, String val) throws SAXException {
        startSimpleElement(ACRONYM,att,val);
    }
    /**
     * Start an <code>acronym</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void acronym(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(ACRONYM,att1,val1,att2,val2);
    }
    /**
     * Start an <code>acronym</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void acronym(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(ACRONYM,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>acronym</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void acronym(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(ACRONYM,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>address</code>.
     */
    public static final String ADDRESS = "address";
    /**
     * Start an <code>address</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void address() throws SAXException {
        address(EMPTY_ATTS);
    }
    /**
     * Start an <code>address</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void address(Attributes atts) throws SAXException {
        startSimpleElement(ADDRESS,atts);
    }
    /**
     * Start an <code>address</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void address(String att, String val) throws SAXException {
        startSimpleElement(ADDRESS,att,val);
    }
    /**
     * Start an <code>address</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void address(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(ADDRESS,att1,val1,att2,val2);
    }
    /**
     * Start an <code>address</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void address(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(ADDRESS,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>address</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void address(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(ADDRESS,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>applet</code>.
     */
    public static final String APPLET = "applet";
    /**
     * Start an <code>applet</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void applet() throws SAXException {
        applet(EMPTY_ATTS);
    }
    /**
     * Start an <code>applet</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void applet(Attributes atts) throws SAXException {
        startSimpleElement(APPLET,atts);
    }
    /**
     * Start an <code>applet</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void applet(String att, String val) throws SAXException {
        startSimpleElement(APPLET,att,val);
    }
    /**
     * Start an <code>applet</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void applet(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(APPLET,att1,val1,att2,val2);
    }
    /**
     * Start an <code>applet</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void applet(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(APPLET,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>applet</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void applet(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(APPLET,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>area</code>.
     */
    public static final String AREA = "area";
    /**
     * Start an <code>area</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void area() throws SAXException {
        area(EMPTY_ATTS);
    }
    /**
     * Start an <code>area</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void area(Attributes atts) throws SAXException {
        startSimpleElement(AREA,atts);
    }
    /**
     * Start an <code>area</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void area(String att, String val) throws SAXException {
        startSimpleElement(AREA,att,val);
    }
    /**
     * Start an <code>area</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void area(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(AREA,att1,val1,att2,val2);
    }
    /**
     * Start an <code>area</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void area(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(AREA,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>area</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void area(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(AREA,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>b</code>.
     */
    public static final String B = "b";
    /**
     * Start an <code>b</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void b() throws SAXException {
        b(EMPTY_ATTS);
    }
    /**
     * Start an <code>b</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void b(Attributes atts) throws SAXException {
        startSimpleElement(B,atts);
    }
    /**
     * Start an <code>b</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void b(String att, String val) throws SAXException {
        startSimpleElement(B,att,val);
    }
    /**
     * Start an <code>b</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void b(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(B,att1,val1,att2,val2);
    }
    /**
     * Start an <code>b</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void b(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(B,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>b</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void b(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(B,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>base</code>.
     */
    public static final String BASE = "base";
    /**
     * Start an <code>base</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void base() throws SAXException {
        base(EMPTY_ATTS);
    }
    /**
     * Start an <code>base</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void base(Attributes atts) throws SAXException {
        startSimpleElement(BASE,atts);
    }
    /**
     * Start an <code>base</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void base(String att, String val) throws SAXException {
        startSimpleElement(BASE,att,val);
    }
    /**
     * Start an <code>base</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void base(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BASE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>base</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void base(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BASE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>base</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void base(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BASE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>basefont</code>.
     */
    public static final String BASEFONT = "basefont";
    /**
     * Start an <code>basefont</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void basefont() throws SAXException {
        basefont(EMPTY_ATTS);
    }
    /**
     * Start an <code>basefont</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void basefont(Attributes atts) throws SAXException {
        startSimpleElement(BASEFONT,atts);
    }
    /**
     * Start an <code>basefont</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void basefont(String att, String val) throws SAXException {
        startSimpleElement(BASEFONT,att,val);
    }
    /**
     * Start an <code>basefont</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void basefont(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BASEFONT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>basefont</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void basefont(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BASEFONT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>basefont</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void basefont(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BASEFONT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>bdo</code>.
     */
    public static final String BDO = "bdo";
    /**
     * Start an <code>bdo</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void bdo() throws SAXException {
        bdo(EMPTY_ATTS);
    }
    /**
     * Start an <code>bdo</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void bdo(Attributes atts) throws SAXException {
        startSimpleElement(BDO,atts);
    }
    /**
     * Start an <code>bdo</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void bdo(String att, String val) throws SAXException {
        startSimpleElement(BDO,att,val);
    }
    /**
     * Start an <code>bdo</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void bdo(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BDO,att1,val1,att2,val2);
    }
    /**
     * Start an <code>bdo</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void bdo(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BDO,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>bdo</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void bdo(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BDO,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>big</code>.
     */
    public static final String BIG = "big";
    /**
     * Start an <code>big</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void big() throws SAXException {
        big(EMPTY_ATTS);
    }
    /**
     * Start an <code>big</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void big(Attributes atts) throws SAXException {
        startSimpleElement(BIG,atts);
    }
    /**
     * Start an <code>big</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void big(String att, String val) throws SAXException {
        startSimpleElement(BIG,att,val);
    }
    /**
     * Start an <code>big</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void big(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BIG,att1,val1,att2,val2);
    }
    /**
     * Start an <code>big</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void big(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BIG,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>big</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void big(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BIG,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>blockquote</code>.
     */
    public static final String BLOCKQUOTE = "blockquote";
    /**
     * Start an <code>blockquote</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void blockquote() throws SAXException {
        blockquote(EMPTY_ATTS);
    }
    /**
     * Start an <code>blockquote</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void blockquote(Attributes atts) throws SAXException {
        startSimpleElement(BLOCKQUOTE,atts);
    }
    /**
     * Start an <code>blockquote</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void blockquote(String att, String val) throws SAXException {
        startSimpleElement(BLOCKQUOTE,att,val);
    }
    /**
     * Start an <code>blockquote</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void blockquote(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BLOCKQUOTE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>blockquote</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void blockquote(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BLOCKQUOTE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>blockquote</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void blockquote(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BLOCKQUOTE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>body</code>.
     */
    public static final String BODY = "body";
    /**
     * Start an <code>body</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void body() throws SAXException {
        body(EMPTY_ATTS);
    }
    /**
     * Start an <code>body</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void body(Attributes atts) throws SAXException {
        startSimpleElement(BODY,atts);
    }
    /**
     * Start an <code>body</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void body(String att, String val) throws SAXException {
        startSimpleElement(BODY,att,val);
    }
    /**
     * Start an <code>body</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void body(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BODY,att1,val1,att2,val2);
    }
    /**
     * Start an <code>body</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void body(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BODY,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>body</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void body(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BODY,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>br</code>.
     */
    public static final String BR = "br";
    /**
     * Start an <code>br</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void br() throws SAXException {
        br(EMPTY_ATTS);
    }
    /**
     * Start an <code>br</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void br(Attributes atts) throws SAXException {
        startSimpleElement(BR,atts);
    }
    /**
     * Start an <code>br</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void br(String att, String val) throws SAXException {
        startSimpleElement(BR,att,val);
    }
    /**
     * Start an <code>br</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void br(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BR,att1,val1,att2,val2);
    }
    /**
     * Start an <code>br</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void br(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BR,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>br</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void br(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BR,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>button</code>.
     */
    public static final String BUTTON = "button";
    /**
     * Start an <code>button</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void button() throws SAXException {
        button(EMPTY_ATTS);
    }
    /**
     * Start an <code>button</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void button(Attributes atts) throws SAXException {
        startSimpleElement(BUTTON,atts);
    }
    /**
     * Start an <code>button</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void button(String att, String val) throws SAXException {
        startSimpleElement(BUTTON,att,val);
    }
    /**
     * Start an <code>button</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void button(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(BUTTON,att1,val1,att2,val2);
    }
    /**
     * Start an <code>button</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void button(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(BUTTON,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>button</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void button(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(BUTTON,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>caption</code>.
     */
    public static final String CAPTION = "caption";
    /**
     * Start an <code>caption</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void caption() throws SAXException {
        caption(EMPTY_ATTS);
    }
    /**
     * Start an <code>caption</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void caption(Attributes atts) throws SAXException {
        startSimpleElement(CAPTION,atts);
    }
    /**
     * Start an <code>caption</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void caption(String att, String val) throws SAXException {
        startSimpleElement(CAPTION,att,val);
    }
    /**
     * Start an <code>caption</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void caption(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(CAPTION,att1,val1,att2,val2);
    }
    /**
     * Start an <code>caption</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void caption(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(CAPTION,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>caption</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void caption(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(CAPTION,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>center</code>.
     */
    public static final String CENTER = "center";
    /**
     * Start an <code>center</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void center() throws SAXException {
        center(EMPTY_ATTS);
    }
    /**
     * Start an <code>center</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void center(Attributes atts) throws SAXException {
        startSimpleElement(CENTER,atts);
    }
    /**
     * Start an <code>center</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void center(String att, String val) throws SAXException {
        startSimpleElement(CENTER,att,val);
    }
    /**
     * Start an <code>center</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void center(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(CENTER,att1,val1,att2,val2);
    }
    /**
     * Start an <code>center</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void center(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(CENTER,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>center</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void center(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(CENTER,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>cite</code>.
     */
    public static final String CITE = "cite";
    /**
     * Start an <code>cite</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void cite() throws SAXException {
        cite(EMPTY_ATTS);
    }
    /**
     * Start an <code>cite</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void cite(Attributes atts) throws SAXException {
        startSimpleElement(CITE,atts);
    }
    /**
     * Start an <code>cite</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void cite(String att, String val) throws SAXException {
        startSimpleElement(CITE,att,val);
    }
    /**
     * Start an <code>cite</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void cite(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(CITE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>cite</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void cite(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(CITE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>cite</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void cite(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(CITE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>code</code>.
     */
    public static final String CODE = "code";
    /**
     * Start an <code>code</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void code() throws SAXException {
        code(EMPTY_ATTS);
    }
    /**
     * Start an <code>code</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void code(Attributes atts) throws SAXException {
        startSimpleElement(CODE,atts);
    }
    /**
     * Start an <code>code</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void code(String att, String val) throws SAXException {
        startSimpleElement(CODE,att,val);
    }
    /**
     * Start an <code>code</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void code(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(CODE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>code</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void code(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(CODE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>code</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void code(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(CODE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>col</code>.
     */
    public static final String COL = "col";
    /**
     * Start an <code>col</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void col() throws SAXException {
        col(EMPTY_ATTS);
    }
    /**
     * Start an <code>col</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void col(Attributes atts) throws SAXException {
        startSimpleElement(COL,atts);
    }
    /**
     * Start an <code>col</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void col(String att, String val) throws SAXException {
        startSimpleElement(COL,att,val);
    }
    /**
     * Start an <code>col</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void col(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(COL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>col</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void col(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(COL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>col</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void col(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(COL,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>colgroup</code>.
     */
    public static final String COLGROUP = "colgroup";
    /**
     * Start an <code>colgroup</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void colgroup() throws SAXException {
        colgroup(EMPTY_ATTS);
    }
    /**
     * Start an <code>colgroup</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void colgroup(Attributes atts) throws SAXException {
        startSimpleElement(COLGROUP,atts);
    }
    /**
     * Start an <code>colgroup</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void colgroup(String att, String val) throws SAXException {
        startSimpleElement(COLGROUP,att,val);
    }
    /**
     * Start an <code>colgroup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void colgroup(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(COLGROUP,att1,val1,att2,val2);
    }
    /**
     * Start an <code>colgroup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void colgroup(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(COLGROUP,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>colgroup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void colgroup(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(COLGROUP,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>dd</code>.
     */
    public static final String DD = "dd";
    /**
     * Start an <code>dd</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dd() throws SAXException {
        dd(EMPTY_ATTS);
    }
    /**
     * Start an <code>dd</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dd(Attributes atts) throws SAXException {
        startSimpleElement(DD,atts);
    }
    /**
     * Start an <code>dd</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dd(String att, String val) throws SAXException {
        startSimpleElement(DD,att,val);
    }
    /**
     * Start an <code>dd</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dd(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DD,att1,val1,att2,val2);
    }
    /**
     * Start an <code>dd</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dd(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DD,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>dd</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dd(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DD,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>del</code>.
     */
    public static final String DEL = "del";
    /**
     * Start an <code>del</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void del() throws SAXException {
        del(EMPTY_ATTS);
    }
    /**
     * Start an <code>del</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void del(Attributes atts) throws SAXException {
        startSimpleElement(DEL,atts);
    }
    /**
     * Start an <code>del</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void del(String att, String val) throws SAXException {
        startSimpleElement(DEL,att,val);
    }
    /**
     * Start an <code>del</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void del(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DEL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>del</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void del(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DEL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>del</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void del(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DEL,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>dfn</code>.
     */
    public static final String DFN = "dfn";
    /**
     * Start an <code>dfn</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dfn() throws SAXException {
        dfn(EMPTY_ATTS);
    }
    /**
     * Start an <code>dfn</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dfn(Attributes atts) throws SAXException {
        startSimpleElement(DFN,atts);
    }
    /**
     * Start an <code>dfn</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dfn(String att, String val) throws SAXException {
        startSimpleElement(DFN,att,val);
    }
    /**
     * Start an <code>dfn</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dfn(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DFN,att1,val1,att2,val2);
    }
    /**
     * Start an <code>dfn</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dfn(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DFN,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>dfn</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dfn(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DFN,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>dir</code>.
     */
    public static final String DIR = "dir";
    /**
     * Start an <code>dir</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dir() throws SAXException {
        dir(EMPTY_ATTS);
    }
    /**
     * Start an <code>dir</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dir(Attributes atts) throws SAXException {
        startSimpleElement(DIR,atts);
    }
    /**
     * Start an <code>dir</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dir(String att, String val) throws SAXException {
        startSimpleElement(DIR,att,val);
    }
    /**
     * Start an <code>dir</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dir(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DIR,att1,val1,att2,val2);
    }
    /**
     * Start an <code>dir</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dir(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DIR,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>dir</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dir(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DIR,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>div</code>.
     */
    public static final String DIV = "div";
    /**
     * Start an <code>div</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void div() throws SAXException {
        div(EMPTY_ATTS);
    }
    /**
     * Start an <code>div</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void div(Attributes atts) throws SAXException {
        startSimpleElement(DIV,atts);
    }
    /**
     * Start an <code>div</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void div(String att, String val) throws SAXException {
        startSimpleElement(DIV,att,val);
    }
    /**
     * Start an <code>div</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void div(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DIV,att1,val1,att2,val2);
    }
    /**
     * Start an <code>div</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void div(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DIV,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>div</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void div(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DIV,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>dl</code>.
     */
    public static final String DL = "dl";
    /**
     * Start an <code>dl</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dl() throws SAXException {
        dl(EMPTY_ATTS);
    }
    /**
     * Start an <code>dl</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dl(Attributes atts) throws SAXException {
        startSimpleElement(DL,atts);
    }
    /**
     * Start an <code>dl</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dl(String att, String val) throws SAXException {
        startSimpleElement(DL,att,val);
    }
    /**
     * Start an <code>dl</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dl(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>dl</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dl(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>dl</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dl(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DL,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>dt</code>.
     */
    public static final String DT = "dt";
    /**
     * Start an <code>dt</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dt() throws SAXException {
        dt(EMPTY_ATTS);
    }
    /**
     * Start an <code>dt</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dt(Attributes atts) throws SAXException {
        startSimpleElement(DT,atts);
    }
    /**
     * Start an <code>dt</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dt(String att, String val) throws SAXException {
        startSimpleElement(DT,att,val);
    }
    /**
     * Start an <code>dt</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dt(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(DT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>dt</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dt(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(DT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>dt</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void dt(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(DT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>em</code>.
     */
    public static final String EM = "em";
    /**
     * Start an <code>em</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void em() throws SAXException {
        em(EMPTY_ATTS);
    }
    /**
     * Start an <code>em</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void em(Attributes atts) throws SAXException {
        startSimpleElement(EM,atts);
    }
    /**
     * Start an <code>em</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void em(String att, String val) throws SAXException {
        startSimpleElement(EM,att,val);
    }
    /**
     * Start an <code>em</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void em(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(EM,att1,val1,att2,val2);
    }
    /**
     * Start an <code>em</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void em(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(EM,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>em</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void em(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(EM,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>fieldset</code>.
     */
    public static final String FIELDSET = "fieldset";
    /**
     * Start an <code>fieldset</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void fieldset() throws SAXException {
        fieldset(EMPTY_ATTS);
    }
    /**
     * Start an <code>fieldset</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void fieldset(Attributes atts) throws SAXException {
        startSimpleElement(FIELDSET,atts);
    }
    /**
     * Start an <code>fieldset</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void fieldset(String att, String val) throws SAXException {
        startSimpleElement(FIELDSET,att,val);
    }
    /**
     * Start an <code>fieldset</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void fieldset(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(FIELDSET,att1,val1,att2,val2);
    }
    /**
     * Start an <code>fieldset</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void fieldset(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(FIELDSET,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>fieldset</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void fieldset(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(FIELDSET,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>font</code>.
     */
    public static final String FONT = "font";
    /**
     * Start an <code>font</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void font() throws SAXException {
        font(EMPTY_ATTS);
    }
    /**
     * Start an <code>font</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void font(Attributes atts) throws SAXException {
        startSimpleElement(FONT,atts);
    }
    /**
     * Start an <code>font</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void font(String att, String val) throws SAXException {
        startSimpleElement(FONT,att,val);
    }
    /**
     * Start an <code>font</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void font(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(FONT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>font</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void font(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(FONT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>font</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void font(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(FONT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>form</code>.
     */
    public static final String FORM = "form";
    /**
     * Start an <code>form</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void form() throws SAXException {
        form(EMPTY_ATTS);
    }
    /**
     * Start an <code>form</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void form(Attributes atts) throws SAXException {
        startSimpleElement(FORM,atts);
    }
    /**
     * Start an <code>form</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void form(String att, String val) throws SAXException {
        startSimpleElement(FORM,att,val);
    }
    /**
     * Start an <code>form</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void form(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(FORM,att1,val1,att2,val2);
    }
    /**
     * Start an <code>form</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void form(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(FORM,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>form</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void form(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(FORM,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>frame</code>.
     */
    public static final String FRAME = "frame";
    /**
     * Start an <code>frame</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frame() throws SAXException {
        frame(EMPTY_ATTS);
    }
    /**
     * Start an <code>frame</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frame(Attributes atts) throws SAXException {
        startSimpleElement(FRAME,atts);
    }
    /**
     * Start an <code>frame</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frame(String att, String val) throws SAXException {
        startSimpleElement(FRAME,att,val);
    }
    /**
     * Start an <code>frame</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frame(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(FRAME,att1,val1,att2,val2);
    }
    /**
     * Start an <code>frame</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frame(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(FRAME,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>frame</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frame(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(FRAME,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>frameset</code>.
     */
    public static final String FRAMESET = "frameset";
    /**
     * Start an <code>frameset</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frameset() throws SAXException {
        frameset(EMPTY_ATTS);
    }
    /**
     * Start an <code>frameset</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frameset(Attributes atts) throws SAXException {
        startSimpleElement(FRAMESET,atts);
    }
    /**
     * Start an <code>frameset</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frameset(String att, String val) throws SAXException {
        startSimpleElement(FRAMESET,att,val);
    }
    /**
     * Start an <code>frameset</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frameset(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(FRAMESET,att1,val1,att2,val2);
    }
    /**
     * Start an <code>frameset</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frameset(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(FRAMESET,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>frameset</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void frameset(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(FRAMESET,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>head</code>.
     */
    public static final String HEAD = "head";
    /**
     * Start an <code>head</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void head() throws SAXException {
        head(EMPTY_ATTS);
    }
    /**
     * Start an <code>head</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void head(Attributes atts) throws SAXException {
        startSimpleElement(HEAD,atts);
    }
    /**
     * Start an <code>head</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void head(String att, String val) throws SAXException {
        startSimpleElement(HEAD,att,val);
    }
    /**
     * Start an <code>head</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void head(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(HEAD,att1,val1,att2,val2);
    }
    /**
     * Start an <code>head</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void head(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(HEAD,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>head</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void head(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(HEAD,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>h1</code>.
     */
    public static final String H1 = "h1";
    /**
     * Start an <code>h1</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h1() throws SAXException {
        h1(EMPTY_ATTS);
    }
    /**
     * Start an <code>h1</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h1(Attributes atts) throws SAXException {
        startSimpleElement(H1,atts);
    }
    /**
     * Start an <code>h1</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h1(String att, String val) throws SAXException {
        startSimpleElement(H1,att,val);
    }
    /**
     * Start an <code>h1</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h1(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(H1,att1,val1,att2,val2);
    }
    /**
     * Start an <code>h1</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h1(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(H1,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>h1</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h1(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(H1,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>h2</code>.
     */
    public static final String H2 = "h2";
    /**
     * Start an <code>h2</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h2() throws SAXException {
        h2(EMPTY_ATTS);
    }
    /**
     * Start an <code>h2</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h2(Attributes atts) throws SAXException {
        startSimpleElement(H2,atts);
    }
    /**
     * Start an <code>h2</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h2(String att, String val) throws SAXException {
        startSimpleElement(H2,att,val);
    }
    /**
     * Start an <code>h2</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h2(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(H2,att1,val1,att2,val2);
    }
    /**
     * Start an <code>h2</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h2(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(H2,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>h2</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h2(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(H2,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>h3</code>.
     */
    public static final String H3 = "h3";
    /**
     * Start an <code>h3</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h3() throws SAXException {
        h3(EMPTY_ATTS);
    }
    /**
     * Start an <code>h3</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h3(Attributes atts) throws SAXException {
        startSimpleElement(H3,atts);
    }
    /**
     * Start an <code>h3</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h3(String att, String val) throws SAXException {
        startSimpleElement(H3,att,val);
    }
    /**
     * Start an <code>h3</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h3(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(H3,att1,val1,att2,val2);
    }
    /**
     * Start an <code>h3</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h3(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(H3,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>h3</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h3(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(H3,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>h4</code>.
     */
    public static final String H4 = "h4";
    /**
     * Start an <code>h4</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h4() throws SAXException {
        h4(EMPTY_ATTS);
    }
    /**
     * Start an <code>h4</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h4(Attributes atts) throws SAXException {
        startSimpleElement(H4,atts);
    }
    /**
     * Start an <code>h4</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h4(String att, String val) throws SAXException {
        startSimpleElement(H4,att,val);
    }
    /**
     * Start an <code>h4</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h4(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(H4,att1,val1,att2,val2);
    }
    /**
     * Start an <code>h4</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h4(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(H4,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>h4</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h4(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(H4,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>h5</code>.
     */
    public static final String H5 = "h5";
    /**
     * Start an <code>h5</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h5() throws SAXException {
        h5(EMPTY_ATTS);
    }
    /**
     * Start an <code>h5</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h5(Attributes atts) throws SAXException {
        startSimpleElement(H5,atts);
    }
    /**
     * Start an <code>h5</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h5(String att, String val) throws SAXException {
        startSimpleElement(H5,att,val);
    }
    /**
     * Start an <code>h5</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h5(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(H5,att1,val1,att2,val2);
    }
    /**
     * Start an <code>h5</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h5(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(H5,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>h5</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h5(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(H5,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>h6</code>.
     */
    public static final String H6 = "h6";
    /**
     * Start an <code>h6</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h6() throws SAXException {
        h6(EMPTY_ATTS);
    }
    /**
     * Start an <code>h6</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h6(Attributes atts) throws SAXException {
        startSimpleElement(H6,atts);
    }
    /**
     * Start an <code>h6</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h6(String att, String val) throws SAXException {
        startSimpleElement(H6,att,val);
    }
    /**
     * Start an <code>h6</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h6(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(H6,att1,val1,att2,val2);
    }
    /**
     * Start an <code>h6</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h6(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(H6,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>h6</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void h6(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(H6,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>hr</code>.
     */
    public static final String HR = "hr";
    /**
     * Start an <code>hr</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void hr() throws SAXException {
        hr(EMPTY_ATTS);
    }
    /**
     * Start an <code>hr</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void hr(Attributes atts) throws SAXException {
        startSimpleElement(HR,atts);
    }
    /**
     * Start an <code>hr</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void hr(String att, String val) throws SAXException {
        startSimpleElement(HR,att,val);
    }
    /**
     * Start an <code>hr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void hr(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(HR,att1,val1,att2,val2);
    }
    /**
     * Start an <code>hr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void hr(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(HR,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>hr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void hr(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(HR,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>html</code>.
     */
    public static final String HTML = "html";
    /**
     * Start an <code>html</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void html() throws SAXException {
        html(EMPTY_ATTS);
    }
    /**
     * Start an <code>html</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void html(Attributes atts) throws SAXException {
        startSimpleElement(HTML,atts);
    }
    /**
     * Start an <code>html</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void html(String att, String val) throws SAXException {
        startSimpleElement(HTML,att,val);
    }
    /**
     * Start an <code>html</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void html(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(HTML,att1,val1,att2,val2);
    }
    /**
     * Start an <code>html</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void html(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(HTML,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>html</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void html(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(HTML,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>i</code>.
     */
    public static final String I = "i";
    /**
     * Start an <code>i</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void i() throws SAXException {
        i(EMPTY_ATTS);
    }
    /**
     * Start an <code>i</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void i(Attributes atts) throws SAXException {
        startSimpleElement(I,atts);
    }
    /**
     * Start an <code>i</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void i(String att, String val) throws SAXException {
        startSimpleElement(I,att,val);
    }
    /**
     * Start an <code>i</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void i(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(I,att1,val1,att2,val2);
    }
    /**
     * Start an <code>i</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void i(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(I,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>i</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void i(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(I,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>iframe</code>.
     */
    public static final String IFRAME = "iframe";
    /**
     * Start an <code>iframe</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void iframe() throws SAXException {
        iframe(EMPTY_ATTS);
    }
    /**
     * Start an <code>iframe</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void iframe(Attributes atts) throws SAXException {
        startSimpleElement(IFRAME,atts);
    }
    /**
     * Start an <code>iframe</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void iframe(String att, String val) throws SAXException {
        startSimpleElement(IFRAME,att,val);
    }
    /**
     * Start an <code>iframe</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void iframe(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(IFRAME,att1,val1,att2,val2);
    }
    /**
     * Start an <code>iframe</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void iframe(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(IFRAME,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>iframe</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void iframe(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(IFRAME,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>img</code>.
     */
    public static final String IMG = "img";
    /**
     * Start an <code>img</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void img() throws SAXException {
        img(EMPTY_ATTS);
    }
    /**
     * Start an <code>img</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void img(Attributes atts) throws SAXException {
        startSimpleElement(IMG,atts);
    }
    /**
     * Start an <code>img</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void img(String att, String val) throws SAXException {
        startSimpleElement(IMG,att,val);
    }
    /**
     * Start an <code>img</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void img(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(IMG,att1,val1,att2,val2);
    }
    /**
     * Start an <code>img</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void img(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(IMG,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>img</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void img(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(IMG,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>input</code>.
     */
    public static final String INPUT = "input";
    /**
     * Start an <code>input</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void input() throws SAXException {
        input(EMPTY_ATTS);
    }
    /**
     * Start an <code>input</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void input(Attributes atts) throws SAXException {
        startSimpleElement(INPUT,atts);
    }
    /**
     * Start an <code>input</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void input(String att, String val) throws SAXException {
        startSimpleElement(INPUT,att,val);
    }
    /**
     * Start an <code>input</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void input(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(INPUT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>input</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void input(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(INPUT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>input</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void input(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(INPUT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>ins</code>.
     */
    public static final String INS = "ins";
    /**
     * Start an <code>ins</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ins() throws SAXException {
        ins(EMPTY_ATTS);
    }
    /**
     * Start an <code>ins</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ins(Attributes atts) throws SAXException {
        startSimpleElement(INS,atts);
    }
    /**
     * Start an <code>ins</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ins(String att, String val) throws SAXException {
        startSimpleElement(INS,att,val);
    }
    /**
     * Start an <code>ins</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ins(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(INS,att1,val1,att2,val2);
    }
    /**
     * Start an <code>ins</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ins(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(INS,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>ins</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ins(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(INS,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>kbd</code>.
     */
    public static final String KBD = "kbd";
    /**
     * Start an <code>kbd</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void kbd() throws SAXException {
        kbd(EMPTY_ATTS);
    }
    /**
     * Start an <code>kbd</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void kbd(Attributes atts) throws SAXException {
        startSimpleElement(KBD,atts);
    }
    /**
     * Start an <code>kbd</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void kbd(String att, String val) throws SAXException {
        startSimpleElement(KBD,att,val);
    }
    /**
     * Start an <code>kbd</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void kbd(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(KBD,att1,val1,att2,val2);
    }
    /**
     * Start an <code>kbd</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void kbd(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(KBD,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>kbd</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void kbd(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(KBD,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>label</code>.
     */
    public static final String LABEL = "label";
    /**
     * Start an <code>label</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void label() throws SAXException {
        label(EMPTY_ATTS);
    }
    /**
     * Start an <code>label</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void label(Attributes atts) throws SAXException {
        startSimpleElement(LABEL,atts);
    }
    /**
     * Start an <code>label</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void label(String att, String val) throws SAXException {
        startSimpleElement(LABEL,att,val);
    }
    /**
     * Start an <code>label</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void label(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(LABEL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>label</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void label(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(LABEL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>label</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void label(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(LABEL,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>legend</code>.
     */
    public static final String LEGEND = "legend";
    /**
     * Start an <code>legend</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void legend() throws SAXException {
        legend(EMPTY_ATTS);
    }
    /**
     * Start an <code>legend</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void legend(Attributes atts) throws SAXException {
        startSimpleElement(LEGEND,atts);
    }
    /**
     * Start an <code>legend</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void legend(String att, String val) throws SAXException {
        startSimpleElement(LEGEND,att,val);
    }
    /**
     * Start an <code>legend</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void legend(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(LEGEND,att1,val1,att2,val2);
    }
    /**
     * Start an <code>legend</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void legend(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(LEGEND,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>legend</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void legend(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(LEGEND,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>li</code>.
     */
    public static final String LI = "li";
    /**
     * Start an <code>li</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void li() throws SAXException {
        li(EMPTY_ATTS);
    }
    /**
     * Start an <code>li</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void li(Attributes atts) throws SAXException {
        startSimpleElement(LI,atts);
    }
    /**
     * Start an <code>li</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void li(String att, String val) throws SAXException {
        startSimpleElement(LI,att,val);
    }
    /**
     * Start an <code>li</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void li(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(LI,att1,val1,att2,val2);
    }
    /**
     * Start an <code>li</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void li(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(LI,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>li</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void li(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(LI,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>link</code>.
     */
    public static final String LINK = "link";
    /**
     * Start an <code>link</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void link() throws SAXException {
        link(EMPTY_ATTS);
    }
    /**
     * Start an <code>link</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void link(Attributes atts) throws SAXException {
        startSimpleElement(LINK,atts);
    }
    /**
     * Start an <code>link</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void link(String att, String val) throws SAXException {
        startSimpleElement(LINK,att,val);
    }
    /**
     * Start an <code>link</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void link(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(LINK,att1,val1,att2,val2);
    }
    /**
     * Start an <code>link</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void link(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(LINK,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>link</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void link(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(LINK,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>map</code>.
     */
    public static final String MAP = "map";
    /**
     * Start an <code>map</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void map() throws SAXException {
        map(EMPTY_ATTS);
    }
    /**
     * Start an <code>map</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void map(Attributes atts) throws SAXException {
        startSimpleElement(MAP,atts);
    }
    /**
     * Start an <code>map</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void map(String att, String val) throws SAXException {
        startSimpleElement(MAP,att,val);
    }
    /**
     * Start an <code>map</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void map(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(MAP,att1,val1,att2,val2);
    }
    /**
     * Start an <code>map</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void map(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(MAP,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>map</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void map(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(MAP,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>menu</code>.
     */
    public static final String MENU = "menu";
    /**
     * Start an <code>menu</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void menu() throws SAXException {
        menu(EMPTY_ATTS);
    }
    /**
     * Start an <code>menu</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void menu(Attributes atts) throws SAXException {
        startSimpleElement(MENU,atts);
    }
    /**
     * Start an <code>menu</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void menu(String att, String val) throws SAXException {
        startSimpleElement(MENU,att,val);
    }
    /**
     * Start an <code>menu</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void menu(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(MENU,att1,val1,att2,val2);
    }
    /**
     * Start an <code>menu</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void menu(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(MENU,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>menu</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void menu(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(MENU,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>meta</code>.
     */
    public static final String META = "meta";
    /**
     * Start an <code>meta</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void meta() throws SAXException {
        meta(EMPTY_ATTS);
    }
    /**
     * Start an <code>meta</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void meta(Attributes atts) throws SAXException {
        startSimpleElement(META,atts);
    }
    /**
     * Start an <code>meta</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void meta(String att, String val) throws SAXException {
        startSimpleElement(META,att,val);
    }
    /**
     * Start an <code>meta</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void meta(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(META,att1,val1,att2,val2);
    }
    /**
     * Start an <code>meta</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void meta(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(META,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>meta</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void meta(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(META,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>noframes</code>.
     */
    public static final String NOFRAMES = "noframes";
    /**
     * Start an <code>noframes</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noframes() throws SAXException {
        noframes(EMPTY_ATTS);
    }
    /**
     * Start an <code>noframes</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noframes(Attributes atts) throws SAXException {
        startSimpleElement(NOFRAMES,atts);
    }
    /**
     * Start an <code>noframes</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noframes(String att, String val) throws SAXException {
        startSimpleElement(NOFRAMES,att,val);
    }
    /**
     * Start an <code>noframes</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noframes(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(NOFRAMES,att1,val1,att2,val2);
    }
    /**
     * Start an <code>noframes</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noframes(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(NOFRAMES,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>noframes</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noframes(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(NOFRAMES,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>noscript</code>.
     */
    public static final String NOSCRIPT = "noscript";
    /**
     * Start an <code>noscript</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noscript() throws SAXException {
        noscript(EMPTY_ATTS);
    }
    /**
     * Start an <code>noscript</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noscript(Attributes atts) throws SAXException {
        startSimpleElement(NOSCRIPT,atts);
    }
    /**
     * Start an <code>noscript</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noscript(String att, String val) throws SAXException {
        startSimpleElement(NOSCRIPT,att,val);
    }
    /**
     * Start an <code>noscript</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noscript(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(NOSCRIPT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>noscript</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noscript(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(NOSCRIPT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>noscript</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void noscript(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(NOSCRIPT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>object</code>.
     */
    public static final String OBJECT = "object";
    /**
     * Start an <code>object</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void object() throws SAXException {
        object(EMPTY_ATTS);
    }
    /**
     * Start an <code>object</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void object(Attributes atts) throws SAXException {
        startSimpleElement(OBJECT,atts);
    }
    /**
     * Start an <code>object</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void object(String att, String val) throws SAXException {
        startSimpleElement(OBJECT,att,val);
    }
    /**
     * Start an <code>object</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void object(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(OBJECT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>object</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void object(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(OBJECT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>object</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void object(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(OBJECT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>ol</code>.
     */
    public static final String OL = "ol";
    /**
     * Start an <code>ol</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ol() throws SAXException {
        ol(EMPTY_ATTS);
    }
    /**
     * Start an <code>ol</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ol(Attributes atts) throws SAXException {
        startSimpleElement(OL,atts);
    }
    /**
     * Start an <code>ol</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ol(String att, String val) throws SAXException {
        startSimpleElement(OL,att,val);
    }
    /**
     * Start an <code>ol</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ol(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(OL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>ol</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ol(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(OL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>ol</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ol(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(OL,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>optgroup</code>.
     */
    public static final String OPTGROUP = "optgroup";
    /**
     * Start an <code>optgroup</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void optgroup() throws SAXException {
        optgroup(EMPTY_ATTS);
    }
    /**
     * Start an <code>optgroup</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void optgroup(Attributes atts) throws SAXException {
        startSimpleElement(OPTGROUP,atts);
    }
    /**
     * Start an <code>optgroup</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void optgroup(String att, String val) throws SAXException {
        startSimpleElement(OPTGROUP,att,val);
    }
    /**
     * Start an <code>optgroup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void optgroup(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(OPTGROUP,att1,val1,att2,val2);
    }
    /**
     * Start an <code>optgroup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void optgroup(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(OPTGROUP,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>optgroup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void optgroup(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(OPTGROUP,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>option</code>.
     */
    public static final String OPTION = "option";
    /**
     * Start an <code>option</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void option() throws SAXException {
        option(EMPTY_ATTS);
    }
    /**
     * Start an <code>option</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void option(Attributes atts) throws SAXException {
        startSimpleElement(OPTION,atts);
    }
    /**
     * Start an <code>option</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void option(String att, String val) throws SAXException {
        startSimpleElement(OPTION,att,val);
    }
    /**
     * Start an <code>option</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void option(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(OPTION,att1,val1,att2,val2);
    }
    /**
     * Start an <code>option</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void option(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(OPTION,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>option</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void option(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(OPTION,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>p</code>.
     */
    public static final String P = "p";
    /**
     * Start an <code>p</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void p() throws SAXException {
        p(EMPTY_ATTS);
    }
    /**
     * Start an <code>p</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void p(Attributes atts) throws SAXException {
        startSimpleElement(P,atts);
    }
    /**
     * Start an <code>p</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void p(String att, String val) throws SAXException {
        startSimpleElement(P,att,val);
    }
    /**
     * Start an <code>p</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void p(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(P,att1,val1,att2,val2);
    }
    /**
     * Start an <code>p</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void p(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(P,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>p</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void p(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(P,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>param</code>.
     */
    public static final String PARAM = "param";
    /**
     * Start an <code>param</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void param() throws SAXException {
        param(EMPTY_ATTS);
    }
    /**
     * Start an <code>param</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void param(Attributes atts) throws SAXException {
        startSimpleElement(PARAM,atts);
    }
    /**
     * Start an <code>param</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void param(String att, String val) throws SAXException {
        startSimpleElement(PARAM,att,val);
    }
    /**
     * Start an <code>param</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void param(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(PARAM,att1,val1,att2,val2);
    }
    /**
     * Start an <code>param</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void param(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(PARAM,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>param</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void param(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(PARAM,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>pre</code>.
     */
    public static final String PRE = "pre";
    /**
     * Start an <code>pre</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void pre() throws SAXException {
        pre(EMPTY_ATTS);
    }
    /**
     * Start an <code>pre</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void pre(Attributes atts) throws SAXException {
        startSimpleElement(PRE,atts);
    }
    /**
     * Start an <code>pre</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void pre(String att, String val) throws SAXException {
        startSimpleElement(PRE,att,val);
    }
    /**
     * Start an <code>pre</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void pre(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(PRE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>pre</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void pre(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(PRE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>pre</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void pre(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(PRE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>q</code>.
     */
    public static final String Q = "q";
    /**
     * Start an <code>q</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void q() throws SAXException {
        q(EMPTY_ATTS);
    }
    /**
     * Start an <code>q</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void q(Attributes atts) throws SAXException {
        startSimpleElement(Q,atts);
    }
    /**
     * Start an <code>q</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void q(String att, String val) throws SAXException {
        startSimpleElement(Q,att,val);
    }
    /**
     * Start an <code>q</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void q(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(Q,att1,val1,att2,val2);
    }
    /**
     * Start an <code>q</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void q(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(Q,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>q</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void q(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(Q,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>s</code>.
     */
    public static final String S = "s";
    /**
     * Start an <code>s</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void s() throws SAXException {
        s(EMPTY_ATTS);
    }
    /**
     * Start an <code>s</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void s(Attributes atts) throws SAXException {
        startSimpleElement(S,atts);
    }
    /**
     * Start an <code>s</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void s(String att, String val) throws SAXException {
        startSimpleElement(S,att,val);
    }
    /**
     * Start an <code>s</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void s(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(S,att1,val1,att2,val2);
    }
    /**
     * Start an <code>s</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void s(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(S,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>s</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void s(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(S,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>samp</code>.
     */
    public static final String SAMP = "samp";
    /**
     * Start an <code>samp</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void samp() throws SAXException {
        samp(EMPTY_ATTS);
    }
    /**
     * Start an <code>samp</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void samp(Attributes atts) throws SAXException {
        startSimpleElement(SAMP,atts);
    }
    /**
     * Start an <code>samp</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void samp(String att, String val) throws SAXException {
        startSimpleElement(SAMP,att,val);
    }
    /**
     * Start an <code>samp</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void samp(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SAMP,att1,val1,att2,val2);
    }
    /**
     * Start an <code>samp</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void samp(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SAMP,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>samp</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void samp(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SAMP,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>script</code>.
     */
    public static final String SCRIPT = "script";
    /**
     * Start an <code>script</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void script() throws SAXException {
        script(EMPTY_ATTS);
    }
    /**
     * Start an <code>script</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void script(Attributes atts) throws SAXException {
        startSimpleElement(SCRIPT,atts);
    }
    /**
     * Start an <code>script</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void script(String att, String val) throws SAXException {
        startSimpleElement(SCRIPT,att,val);
    }
    /**
     * Start an <code>script</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void script(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SCRIPT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>script</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void script(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SCRIPT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>script</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void script(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SCRIPT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>select</code>.
     */
    public static final String SELECT = "select";
    /**
     * Start an <code>select</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void select() throws SAXException {
        select(EMPTY_ATTS);
    }
    /**
     * Start an <code>select</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void select(Attributes atts) throws SAXException {
        startSimpleElement(SELECT,atts);
    }
    /**
     * Start an <code>select</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void select(String att, String val) throws SAXException {
        startSimpleElement(SELECT,att,val);
    }
    /**
     * Start an <code>select</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void select(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SELECT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>select</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void select(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SELECT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>select</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void select(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SELECT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>small</code>.
     */
    public static final String SMALL = "small";
    /**
     * Start an <code>small</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void small() throws SAXException {
        small(EMPTY_ATTS);
    }
    /**
     * Start an <code>small</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void small(Attributes atts) throws SAXException {
        startSimpleElement(SMALL,atts);
    }
    /**
     * Start an <code>small</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void small(String att, String val) throws SAXException {
        startSimpleElement(SMALL,att,val);
    }
    /**
     * Start an <code>small</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void small(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SMALL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>small</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void small(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SMALL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>small</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void small(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SMALL,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>span</code>.
     */
    public static final String SPAN = "span";
    /**
     * Start an <code>span</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void span() throws SAXException {
        span(EMPTY_ATTS);
    }
    /**
     * Start an <code>span</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void span(Attributes atts) throws SAXException {
        startSimpleElement(SPAN,atts);
    }
    /**
     * Start an <code>span</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void span(String att, String val) throws SAXException {
        startSimpleElement(SPAN,att,val);
    }
    /**
     * Start an <code>span</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void span(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SPAN,att1,val1,att2,val2);
    }
    /**
     * Start an <code>span</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void span(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SPAN,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>span</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void span(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SPAN,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>strike</code>.
     */
    public static final String STRIKE = "strike";
    /**
     * Start an <code>strike</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strike() throws SAXException {
        strike(EMPTY_ATTS);
    }
    /**
     * Start an <code>strike</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strike(Attributes atts) throws SAXException {
        startSimpleElement(STRIKE,atts);
    }
    /**
     * Start an <code>strike</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strike(String att, String val) throws SAXException {
        startSimpleElement(STRIKE,att,val);
    }
    /**
     * Start an <code>strike</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strike(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(STRIKE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>strike</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strike(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(STRIKE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>strike</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strike(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(STRIKE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>strong</code>.
     */
    public static final String STRONG = "strong";
    /**
     * Start an <code>strong</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strong() throws SAXException {
        strong(EMPTY_ATTS);
    }
    /**
     * Start an <code>strong</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strong(Attributes atts) throws SAXException {
        startSimpleElement(STRONG,atts);
    }
    /**
     * Start an <code>strong</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strong(String att, String val) throws SAXException {
        startSimpleElement(STRONG,att,val);
    }
    /**
     * Start an <code>strong</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strong(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(STRONG,att1,val1,att2,val2);
    }
    /**
     * Start an <code>strong</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strong(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(STRONG,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>strong</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void strong(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(STRONG,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>style</code>.
     */
    public static final String STYLE = "style";
    /**
     * Start an <code>style</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void style() throws SAXException {
        style(EMPTY_ATTS);
    }
    /**
     * Start an <code>style</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void style(Attributes atts) throws SAXException {
        startSimpleElement(STYLE,atts);
    }
    /**
     * Start an <code>style</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void style(String att, String val) throws SAXException {
        startSimpleElement(STYLE,att,val);
    }
    /**
     * Start an <code>style</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void style(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(STYLE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>style</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void style(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(STYLE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>style</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void style(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(STYLE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>sub</code>.
     */
    public static final String SUB = "sub";
    /**
     * Start an <code>sub</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sub() throws SAXException {
        sub(EMPTY_ATTS);
    }
    /**
     * Start an <code>sub</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sub(Attributes atts) throws SAXException {
        startSimpleElement(SUB,atts);
    }
    /**
     * Start an <code>sub</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sub(String att, String val) throws SAXException {
        startSimpleElement(SUB,att,val);
    }
    /**
     * Start an <code>sub</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sub(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SUB,att1,val1,att2,val2);
    }
    /**
     * Start an <code>sub</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sub(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SUB,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>sub</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sub(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SUB,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>sup</code>.
     */
    public static final String SUP = "sup";
    /**
     * Start an <code>sup</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sup() throws SAXException {
        sup(EMPTY_ATTS);
    }
    /**
     * Start an <code>sup</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sup(Attributes atts) throws SAXException {
        startSimpleElement(SUP,atts);
    }
    /**
     * Start an <code>sup</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sup(String att, String val) throws SAXException {
        startSimpleElement(SUP,att,val);
    }
    /**
     * Start an <code>sup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sup(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(SUP,att1,val1,att2,val2);
    }
    /**
     * Start an <code>sup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sup(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(SUP,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>sup</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void sup(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(SUP,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>table</code>.
     */
    public static final String TABLE = "table";
    /**
     * Start an <code>table</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void table() throws SAXException {
        table(EMPTY_ATTS);
    }
    /**
     * Start an <code>table</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void table(Attributes atts) throws SAXException {
        startSimpleElement(TABLE,atts);
    }
    /**
     * Start an <code>table</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void table(String att, String val) throws SAXException {
        startSimpleElement(TABLE,att,val);
    }
    /**
     * Start an <code>table</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void table(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TABLE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>table</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void table(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TABLE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>table</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void table(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TABLE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>tbody</code>.
     */
    public static final String TBODY = "tbody";
    /**
     * Start an <code>tbody</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tbody() throws SAXException {
        tbody(EMPTY_ATTS);
    }
    /**
     * Start an <code>tbody</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tbody(Attributes atts) throws SAXException {
        startSimpleElement(TBODY,atts);
    }
    /**
     * Start an <code>tbody</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tbody(String att, String val) throws SAXException {
        startSimpleElement(TBODY,att,val);
    }
    /**
     * Start an <code>tbody</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tbody(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TBODY,att1,val1,att2,val2);
    }
    /**
     * Start an <code>tbody</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tbody(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TBODY,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>tbody</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tbody(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TBODY,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>td</code>.
     */
    public static final String TD = "td";
    /**
     * Start an <code>td</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void td() throws SAXException {
        td(EMPTY_ATTS);
    }
    /**
     * Start an <code>td</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void td(Attributes atts) throws SAXException {
        startSimpleElement(TD,atts);
    }
    /**
     * Start an <code>td</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void td(String att, String val) throws SAXException {
        startSimpleElement(TD,att,val);
    }
    /**
     * Start an <code>td</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void td(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TD,att1,val1,att2,val2);
    }
    /**
     * Start an <code>td</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void td(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TD,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>td</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void td(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TD,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>textarea</code>.
     */
    public static final String TEXTAREA = "textarea";
    /**
     * Start an <code>textarea</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void textarea() throws SAXException {
        textarea(EMPTY_ATTS);
    }
    /**
     * Start an <code>textarea</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void textarea(Attributes atts) throws SAXException {
        startSimpleElement(TEXTAREA,atts);
    }
    /**
     * Start an <code>textarea</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void textarea(String att, String val) throws SAXException {
        startSimpleElement(TEXTAREA,att,val);
    }
    /**
     * Start an <code>textarea</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void textarea(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TEXTAREA,att1,val1,att2,val2);
    }
    /**
     * Start an <code>textarea</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void textarea(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TEXTAREA,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>textarea</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void textarea(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TEXTAREA,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>tfoot</code>.
     */
    public static final String TFOOT = "tfoot";
    /**
     * Start an <code>tfoot</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tfoot() throws SAXException {
        tfoot(EMPTY_ATTS);
    }
    /**
     * Start an <code>tfoot</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tfoot(Attributes atts) throws SAXException {
        startSimpleElement(TFOOT,atts);
    }
    /**
     * Start an <code>tfoot</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tfoot(String att, String val) throws SAXException {
        startSimpleElement(TFOOT,att,val);
    }
    /**
     * Start an <code>tfoot</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tfoot(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TFOOT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>tfoot</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tfoot(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TFOOT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>tfoot</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tfoot(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TFOOT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>th</code>.
     */
    public static final String TH = "th";
    /**
     * Start an <code>th</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void th() throws SAXException {
        th(EMPTY_ATTS);
    }
    /**
     * Start an <code>th</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void th(Attributes atts) throws SAXException {
        startSimpleElement(TH,atts);
    }
    /**
     * Start an <code>th</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void th(String att, String val) throws SAXException {
        startSimpleElement(TH,att,val);
    }
    /**
     * Start an <code>th</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void th(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TH,att1,val1,att2,val2);
    }
    /**
     * Start an <code>th</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void th(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TH,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>th</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void th(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TH,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>thead</code>.
     */
    public static final String THEAD = "thead";
    /**
     * Start an <code>thead</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void thead() throws SAXException {
        thead(EMPTY_ATTS);
    }
    /**
     * Start an <code>thead</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void thead(Attributes atts) throws SAXException {
        startSimpleElement(THEAD,atts);
    }
    /**
     * Start an <code>thead</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void thead(String att, String val) throws SAXException {
        startSimpleElement(THEAD,att,val);
    }
    /**
     * Start an <code>thead</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void thead(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(THEAD,att1,val1,att2,val2);
    }
    /**
     * Start an <code>thead</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void thead(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(THEAD,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>thead</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void thead(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(THEAD,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>title</code>.
     */
    public static final String TITLE = "title";
    /**
     * Start an <code>title</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void title() throws SAXException {
        title(EMPTY_ATTS);
    }
    /**
     * Start an <code>title</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void title(Attributes atts) throws SAXException {
        startSimpleElement(TITLE,atts);
    }
    /**
     * Start an <code>title</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void title(String att, String val) throws SAXException {
        startSimpleElement(TITLE,att,val);
    }
    /**
     * Start an <code>title</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void title(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TITLE,att1,val1,att2,val2);
    }
    /**
     * Start an <code>title</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void title(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TITLE,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>title</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void title(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TITLE,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>tr</code>.
     */
    public static final String TR = "tr";
    /**
     * Start an <code>tr</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tr() throws SAXException {
        tr(EMPTY_ATTS);
    }
    /**
     * Start an <code>tr</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tr(Attributes atts) throws SAXException {
        startSimpleElement(TR,atts);
    }
    /**
     * Start an <code>tr</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tr(String att, String val) throws SAXException {
        startSimpleElement(TR,att,val);
    }
    /**
     * Start an <code>tr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tr(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TR,att1,val1,att2,val2);
    }
    /**
     * Start an <code>tr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tr(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TR,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>tr</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tr(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TR,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>tt</code>.
     */
    public static final String TT = "tt";
    /**
     * Start an <code>tt</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tt() throws SAXException {
        tt(EMPTY_ATTS);
    }
    /**
     * Start an <code>tt</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tt(Attributes atts) throws SAXException {
        startSimpleElement(TT,atts);
    }
    /**
     * Start an <code>tt</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tt(String att, String val) throws SAXException {
        startSimpleElement(TT,att,val);
    }
    /**
     * Start an <code>tt</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tt(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(TT,att1,val1,att2,val2);
    }
    /**
     * Start an <code>tt</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tt(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(TT,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>tt</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void tt(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(TT,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>u</code>.
     */
    public static final String U = "u";
    /**
     * Start an <code>u</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void u() throws SAXException {
        u(EMPTY_ATTS);
    }
    /**
     * Start an <code>u</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void u(Attributes atts) throws SAXException {
        startSimpleElement(U,atts);
    }
    /**
     * Start an <code>u</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void u(String att, String val) throws SAXException {
        startSimpleElement(U,att,val);
    }
    /**
     * Start an <code>u</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void u(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(U,att1,val1,att2,val2);
    }
    /**
     * Start an <code>u</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void u(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(U,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>u</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void u(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(U,att1,val1,att2,val2,att3,val3,att4,val4);
    }

    /**
     * Constant for element <code>ul</code>.
     */
    public static final String UL = "ul";
    /**
     * Start an <code>ul</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ul() throws SAXException {
        ul(EMPTY_ATTS);
    }
    /**
     * Start an <code>ul</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ul(Attributes atts) throws SAXException {
        startSimpleElement(UL,atts);
    }
    /**
     * Start an <code>ul</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ul(String att, String val) throws SAXException {
        startSimpleElement(UL,att,val);
    }
    /**
     * Start an <code>ul</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ul(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(UL,att1,val1,att2,val2);
    }
    /**
     * Start an <code>ul</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ul(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(UL,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>ul</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void ul(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(UL,att1,val1,att2,val2,att3,val3,att4,val4);
    }


    /**
     * Constant for element <code>var</code>.
     */
    public static final String VAR = "var";
    /**
     * Start an <code>var</code> element with no attributes.
     *
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void var() throws SAXException {
        var(EMPTY_ATTS);
    }
    /**
     * Start an <code>var</code> element with the specified
     * attributes.
     *
     * @param atts Attributes for element.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void var(Attributes atts) throws SAXException {
        startSimpleElement(VAR,atts);
    }
    /**
     * Start an <code>var</code> element with the specified
     * attribute and value.
     *
     * @param att Qualified name of attribute.
     * @param val Value of attribute.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void var(String att, String val) throws SAXException {
        startSimpleElement(VAR,att,val);
    }
    /**
     * Start an <code>var</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void var(String att1, String val1,
                    String att2, String val2) throws SAXException {
        startSimpleElement(VAR,att1,val1,att2,val2);
    }
    /**
     * Start an <code>var</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void var(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3) throws SAXException {
        startSimpleElement(VAR,att1,val1,att2,val2,att3,val3);
    }
    /**
     * Start an <code>var</code> element with the specified
     * attributes and values.
     *
     * @param att1 Qualified name of attribute one.
     * @param val1 Value of attribute one.
     * @param att2 Qualified name of attribute two.
     * @param val2 Value of attribute two.
     * @param att3 Qualified name of attribute three.
     * @param val3 Value of attribute three.
     * @param att4 Qualified name of attribute four.
     * @param val4 Value of attribute four.
     * @throws SAXException If there is an underlying SAX exception.
     */
    public void var(String att1, String val1,
                    String att2, String val2,
                    String att3, String val3,
                    String att4, String val4) throws SAXException {
        startSimpleElement(VAR,att1,val1,att2,val2,att3,val3,att4,val4);
    }


    /**
     * A constant for the <code>abbr</code> attribute.
     */
    public static final String ABBR_ATT = "abbr";

    /**
     * A constant for the <code>accept</code> attribute.
     */
    public static final String ACCEPT = "accept";

    /**
     * A constant for the <code>accept-charset</code> attribute.
     */
    public static final String ACCEPT_CHARSET = "accept-charset";

    /**
     * A constant for the <code>accesskey</code> attribute.
     */
    public static final String ACCESSKEY = "accesskey";

    /**
     * A constant for the <code>action</code> attribute.
     */
    public static final String ACTION = "action";

    /**
     * A constant for the <code>align</code> attribute.
     */
    public static final String ALIGN = "align";

    /**
     * A constant for the <code>alt</code> attribute.
     */
    public static final String ALT = "alt";

    /**
     * A constant for the <code>archive</code> attribute.
     */
    public static final String ARCHIVE = "archive";

    /**
     * A constant for the <code>axis</code> attribute.
     */
    public static final String AXIS = "axis";

    /**
     * A constant for the <code>border</code> attribute.
     */
    public static final String BORDER = "border";

    /**
     * A constant for the <code>cellspacing</code> attribute.
     */
    public static final String CELLSPACING = "cellspacing";

    /**
     * A constant for the <code>cellpadding</code> attribute.
     */
    public static final String CELLPADDING = "cellpadding";

    /**
     * A constant for the <code>char</code> attribute.
     */
    public static final String CHAR = "char";

    /**
     * A constant for the <code>charoff</code> attribute.
     */
    public static final String CHAROFF = "charoff";

    /**
     * A constant for the <code>charset</code> attribute.
     */
    public static final String CHARSET = "charset";

    /**
     * A constant for the <code>checked</code> attribute.
     */
    public static final String CHECKED = "checked";

    /**
     * A constant for the <code>cite</code> attribute.
     */
    public static final String CITE_ATT = "cite";

    /**
     * A constant for the <code>class</code> attribute.
     */
    public static final String CLASS = "class";

    /**
     * A constant for the <code>classid</code> attribute.
     */
    public static final String CLASSID = "classid";

    /**
     * A constant for the <code>codebase</code> attribute.
     */
    public static final String CODEBASE = "codebase";

    /**
     * A constant for the <code>codetype</code> attribute.
     */
    public static final String CODETYPE = "codetype";

    /**
     * A constant for the <code>cols</code> attribute.
     */
    public static final String COLS = "cols";

    /**
     * A constant for the <code>colspan</code> attribute.
     */
    public static final String COLSPAN = "colspan";

    /**
     * A constant for the <code>content</code> attribute.
     */
    public static final String CONTENT = "content";

    /**
     * A constant for the <code>coords</code> attribute.
     */
    public static final String COORDS = "coords";

    /**
     * A constant for the <code>data</code> attribute.
     */
    public static final String DATA = "data";

    /**
     * A constant for the <code>datetime</code> attribute.
     */
    public static final String DATETIME = "datetime";

    /**
     * A constant for the <code>declare</code> attribute.
     */
    public static final String DECLARE = "declare";

    /**
     * A constant for the <code>defer</code> attribute.
     */
    public static final String DEFER = "defer";

    /**
     * A constant for the <code>dir</code> attribute.
     */
    public static final String DIR_ATT = "dir";

    /**
     * A constant for the <code>disabled</code> attribute.
     */
    public static final String DISABLED = "disabled";

    /**
     * A constant for the <code>enctype</code> attribute.
     */
    public static final String ENCTYPE = "enctype";

    /**
     * A constant for the <code>for</code> attribute.
     */
    public static final String FOR = "for";

    /**
     * A constant for the <code>frame</code> attribute.
     */
    public static final String FRAME_ATT = "frame";

    /**
     * A constant for the <code>headers</code> attribute.
     */
    public static final String HEADERS = "headers";

    /**
     * A constant for the <code>height</code> attribute.
     */
    public static final String HEIGHT = "height";

    /**
     * A constant for the <code>href</code> attribute.
     */
    public static final String HREF = "href";

    /**
     * A constant for the <code>hreflang</code> attribute.
     */
    public static final String HREFLANG = "hreflang";

    /**
     * A constant for the <code>http-equiv</code> attribute.
     */
    public static final String HTTP_EQUIV = "http-equiv";

    /**
     * A constant for the <code>id</code> attribute.
     */
    public static final String ID = "id";

    /**
     * A constant for the <code>ismap</code> attribute.
     */
    public static final String ISMAP = "ismap";

    /**
     * A constant for the <code>label</code> attribute.
     */
    public static final String LABEL_ATT = "label";

    /**
     * A constant for the <code>lang</code> attribute.
     */
    public static final String LANG = "lang";

    /**
     * A constant for the <code>longdesc</code> attribute.
     */
    public static final String LONGDESC = "longdesc";

    /**
     * A constant for the <code>maxlength</code> attribute.
     */
    public static final String MAXLENGTH = "maxlength";

    /**
     * A constant for the <code>media</code> attribute.
     */
    public static final String MEDIA = "media";

    /**
     * A constant for the <code>method</code> attribute.
     */
    public static final String METHOD = "method";

    /**
     * A constant for the <code>multiple</code> attribute.
     */
    public static final String MULTIPLE = "multiple";

    /**
     * A constant for the <code>name</code> attribute.
     */
    public static final String NAME = "name";

    /**
     * A constant for the <code>nohref</code> attribute.
     */
    public static final String NOHREF = "nohref";

    /**
     * A constant for the <code>onblur</code> attribute.
     */
    public static final String ONBLUR = "onblur";

    /**
     * A constant for the <code>onchange</code> attribute.
     */
    public static final String ONCHANGE = "onchange";

    /**
     * A constant for the <code>onclick</code> attribute.
     */
    public static final String ONCLICK = "onclick";

    /**
     * A constant for the <code>ondblclick</code> attribute.
     */
    public static final String ONDBLCLICK = "ondblclick";

    /**
     * A constant for the <code>onfocus</code> attribute.
     */
    public static final String ONFOCUS = "onfocus";

    /**
     * A constant for the <code>onkeydown</code> attribute.
     */
    public static final String ONKEYDOWN = "onkeydown";

    /**
     * A constant for the <code>onkeypress</code> attribute.
     */
    public static final String ONKEYPRESS = "onkeypress";

    /**
     * A constant for the <code>onkeyup</code> attribute.
     */
    public static final String ONKEYUP = "onkeyup";

    /**
     * A constant for the <code>onload</code> attribute.
     */
    public static final String ONLOAD = "onload";

    /**
     * A constant for the <code>onmousedown</code> attribute.
     */
    public static final String ONMOUSEDOWN = "onmousedown";

    /**
     * A constant for the <code>onmousemove</code> attribute.
     */
    public static final String ONMOUSEMOVE = "onmousemove";

    /**
     * A constant for the <code>onmouseout</code> attribute.
     */
    public static final String ONMOUSEOUT = "onmouseout";

    /**
     * A constant for the <code>onmouseover</code> attribute.
     */
    public static final String ONMOUSEOVER = "onmouseover";

    /**
     * A constant for the <code>onmouseup</code> attribute.
     */
    public static final String ONMOUSEUP = "onmouseup";

    /**
     * A constant for the <code>onreset</code> attribute.
     */
    public static final String ONRESET = "onreset";

    /**
     * A constant for the <code>onselect</code> attribute.
     */
    public static final String ONSELECT = "onselect";

    /**
     * A constant for the <code>onsubmit</code> attribute.
     */
    public static final String ONSUBMIT = "onsubmit";

    /**
     * A constant for the <code>onunload</code> attribute.
     */
    public static final String ONUNLOAD = "onunload";

    /**
     * A constant for the <code>profile</code> attribute.
     */
    public static final String PROFILE = "profile";

    /**
     * A constant for the <code>readonly</code> attribute.
     */
    public static final String READONLY = "readonly";

    /**
     * A constant for the <code>rel</code> attribute.
     */
    public static final String REL = "rel";

    /**
     * A constant for the <code>rev</code> attribute.
     */
    public static final String REV = "rev";

    /**
     * A constant for the <code>rows</code> attribute.
     */
    public static final String ROWS = "rows";

    /**
     * A constant for the <code>rowspan</code> attribute.
     */
    public static final String ROWSPAN = "rowspan";

    /**
     * A constant for the <code>rules</code> attribute.
     */
    public static final String RULES = "rules";

    /**
     * A constant for the <code>scheme</code> attribute.
     */
    public static final String SCHEME = "scheme";

    /**
     * A constant for the <code>scope</code> attribute.
     */
    public static final String SCOPE = "scope";

    /**
     * A constant for the <code>selected</code> attribute.
     */
    public static final String SELECTED = "selected";

    /**
     * A constant for the <code>shape</code> attribute.
     */
    public static final String SHAPE = "shape";

    /**
     * A constant for the <code>size</code> attribute.
     */
    public static final String SIZE = "size";

    /**
     * A constant for the <code>span</code> attribute.
     */
    public static final String SPAN_ATT = "span";

    /**
     * A constant for the <code>src</code> attribute.
     */
    public static final String SRC = "src";

    /**
     * A constant for the <code>standby</code> attribute.
     */
    public static final String STANDBY = "standby";

    /**
     * A constant for the <code>style</code> attribute.
     */
    public static final String STYLE_ATT = "style";

    /**
     * A constant for the <code>summary</code> attribute.
     */
    public static final String SUMMARY = "summary";

    /**
     * A constant for the <code>tabindex</code> attribute.
     */
    public static final String TABINDEX = "tabindex";

    /**
     * A constant for the <code>title</code> attribute.
     */
    public static final String TITLE_ATT = "title";

    /**
     * A constant for the <code>type</code> attribute.
     */
    public static final String TYPE = "type";

    /**
     * A constant for the <code>usemap</code> attribute.
     */
    public static final String USEMAP = "usemap";

    /**
     * A constant for the <code>valign</code> attribute.
     */
    public static final String VALIGN = "valign";

    /**
     * A constant for the <code>value</code> attribute.
     */
    public static final String VALUE = "value";

    /**
     * A constant for the <code>valuetype</code> attribute.
     */
    public static final String VALUETYPE = "valuetype";

    /**
     * A constant for the <code>width</code> attribute.
     */
    public static final String WIDTH = "width";

    /**
     * A constant for the <code>xml:lang</code> attribute.
     */
    public static final String XML_LANG = "xml:lang";

    /**
     * A constant for the <code>xml:space</code> attribute.
     */
    public static final String XML_SPACE = "xml:space";

    /**
     * A constant for the <code>xmlns</code> attribute.
     */
    public static final String XMLNS = "xmlns";


    private static final boolean XHTML_MODE = true;

    private static final String XHTML_1_0_STRICT_DTD 
        = "<!DOCTYPE html"
        + " PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
        + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";


    private void startSimpleElement(String qName, String att1, String val1,
                                    String att2, String val2,
                                    String att3, String val3) 
        throws SAXException {

        Attributes atts = createAttributes(att1,val1,att2,val2,att3,val3);
        startSimpleElement(qName,atts);
    }

    private void startSimpleElement(String qName, String att1, String val1,
                                    String att2, String val2,
                                    String att3, String val3,
                                    String att4, String val4)
        throws SAXException {

        Attributes atts = createAttributes(att1,val1,att2,val2,att3,val3,
                                           att4,val4);
        startSimpleElement(qName,atts);
    }
    
}
