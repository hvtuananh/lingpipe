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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>SimpleElementHandler</code> is a default handler that
 * supplies utilities for simple elements and attributes.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe1.0
 */
public class SimpleElementHandler extends DefaultHandler {

    /**
     * Converts the string to a character array in order to
     * call {@link #characters(char[],int,int)} to handle the
     * characters.
     *
     * @param s String whose characters are to be handled.
     *
     * @throws SAXException if the handler throws a SAX exception.
     */
    public void characters(String s) throws SAXException {
        characters(this,s);
    }

    /**
     * Handls the specified character array by delegation to
     * {@link #characters(char[],int,int)}.
     *
     * @param cs Character array to be handled.
     *
     * @throws SAXException if the handler throws a SAX exception.
     */
    public void characters(char[] cs) throws SAXException {
        characters(this,cs);
    }

    /**
     * Starts an element with a <code>null</code> namespace
     * and no attributes.
     *
     * @param name Name of element to start.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     *
     * @see #startSimpleElement(String,Attributes)
     */
    public void startSimpleElement(String name) throws SAXException {
        startSimpleElement(this,name);
    }

    /**
     * Starts an element with a <code>null</code> namespace and a
     * single attribute and value.
     *
     * @param name Name of element to start.
     * @param att Name of single attribute.
     * @param value The attribute's value.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     *
     * @see #startSimpleElement(String,Attributes)
     */
    public void startSimpleElement(String name, String att, String value)
        throws SAXException {

        startSimpleElement(this,name,att,value);
    }

    /**
     * Starts an element with a <code>null</code> namespace and a
     * pair of attributes and values.
     *
     * @param name Name of element to start.
     * @param att1 Name of the first attribute.
     * @param val1 Value of the first attribute.
     * @param att2 Name of the second attribute.
     * @param val2 Value of the second attribute.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void startSimpleElement(String name, String att1, String val1,
                                   String att2, String val2)
        throws SAXException {

        Attributes atts = createAttributes(att1,val1,att2,val2);

        startSimpleElement(name,atts);
    }


    /**
     * Starts an element with a <code>null</code> namespace and the
     * specified local name, which is also provided as the qualified
     * name.
     *
     * @param localName Local name of element to start.
     * @param atts Attributes for this element.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void startSimpleElement(String localName, Attributes atts)
        throws SAXException {

        startSimpleElement(this,localName,atts);
    }

    /**
     * End an element with a <code>null</code> namespace, using
     * the local name for both local and qualified names.
     *
     * @param localName Local name of element to end.
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public void endSimpleElement(String localName) throws SAXException {
        endSimpleElement(this,localName);
    }

    /**
     * Starts and then ends a simple element of the specified
     * local name.
     *
     * @param localName Name of element.
     * @throws SAXException If the contained handler throws a SAX
     * exception.
     */
    public void startEndSimpleElement(String localName) 
        throws SAXException {

        startSimpleElement(localName);
        endSimpleElement(localName);
    }

    /**
     * Starts and then ends a simple element of the specified
     * local name with the specified attributes.
     *
     * @param localName Name of element.
     * @param atts Attributes for the element.
     * @throws SAXException If the contained handler throws a SAX
     * exception.
     */
    public void startEndSimpleElement(String localName, Attributes atts) 
        throws SAXException {
    
        startSimpleElement(localName,atts);
        endSimpleElement(localName);
    }    

    /**
     * Add a local name and value to an attributes implementation
     * as type <code>"CDATA"</code>.  Sets both local and qualified
     * name on attribute.
     *
     * @param atts Attributes to which the specified attribute and
     * value are added.
     * @param localName Local name of attribute to add.
     * @param value Value of attribute to add.
     */
    public static void addSimpleAttribute(AttributesImpl atts,
                                          String localName,
                                          String value) {
        atts.addAttribute(null,localName,
                          localName,CDATA_ATTS_TYPE,value);
    }


    /**
     * An implementation of <code>Attributes</code> with no
     * attributes.
     */
    public static final Attributes EMPTY_ATTS = new AttributesImpl();

    /**
     * Create an attributes list with the specified attribute
     * and value.
     *
     * @param attribute Name of attribute.
     * @param value Value of  attribute.
     * @return Resulting attribute-value list.
     */
    public static final Attributes createAttributes(String attribute,
                                                    String value) {
        AttributesImpl atts = new AttributesImpl();
        addSimpleAttribute(atts,attribute,value);
        return atts;
    }

    /**
     * Create an attributes list with the specified pair of
     * attributes.
     *
     * @param attribute1 Name of first attribute.
     * @param value1 Value of first attribute.
     * @param attribute2 Name of second attribute.
     * @param value2 Value of second attribute.
     * @return Resulting attribute-value list.
     */
    public static final Attributes createAttributes(String attribute1,
                                                    String value1,
                                                    String attribute2,
                                                    String value2) {
        AttributesImpl atts = new AttributesImpl();
        addSimpleAttribute(atts,attribute1,value1);
        addSimpleAttribute(atts,attribute2,value2);
        return atts;
    }

    /**
     * Create an attributes list with the specified tripe of
     * attributes and values.
     *
     * @param attribute1 Name of first attribute.
     * @param value1 Value of first attribute.
     * @param attribute2 Name of second attribute.
     * @param value2 Value of second attribute.
     * @param attribute3 Name of third attribute.
     * @param value3 Value of third attribute.
     * @return Resulting attribute-value list.
     */
    public static final Attributes createAttributes(String attribute1,
                                                    String value1,
                                                    String attribute2,
                                                    String value2,
                                                    String attribute3,
                                                    String value3) {
        AttributesImpl atts = new AttributesImpl();
        addSimpleAttribute(atts,attribute1,value1);
        addSimpleAttribute(atts,attribute2,value2);
        addSimpleAttribute(atts,attribute3,value3);
        return atts;
    }


    /**
     * Create an attributes list with the specified four attributes and values.
     *
     * @param attribute1 Name of first attribute.
     * @param value1 Value of first attribute.
     * @param attribute2 Name of second attribute.
     * @param value2 Value of second attribute.
     * @param attribute3 Name of third attribute.
     * @param value3 Value of third attribute.
     * @param attribute4 Name of fourth attribute.
     * @param value4 Value of fourth attribute.
     * @return Resulting attribute-value list.
     */
    public static final Attributes createAttributes(String attribute1,
                                                    String value1,
                                                    String attribute2,
                                                    String value2,
                                                    String attribute3,
                                                    String value3,
                                                    String attribute4,
                                                    String value4) {
        AttributesImpl atts = new AttributesImpl();
        addSimpleAttribute(atts,attribute1,value1);
        addSimpleAttribute(atts,attribute2,value2);
        addSimpleAttribute(atts,attribute3,value3);
        addSimpleAttribute(atts,attribute4,value4);
        return atts;
    }


    /**
     * Create an attributes list with the specified five attributes
     * and values.
     *
     * @param attribute1 Name of first attribute.
     * @param value1 Value of first attribute.
     * @param attribute2 Name of second attribute.
     * @param value2 Value of second attribute.
     * @param attribute3 Name of third attribute.
     * @param value3 Value of third attribute.
     * @param attribute4 Name of fourth attribute.
     * @param value4 Value of fourth attribute.
     * @param attribute5 Name of fifth attribute.
     * @param value5 Value of fifth attribute.
     * @return Resulting attribute-value list.
     */
    public static final Attributes createAttributes(String attribute1,
                                                    String value1,
                                                    String attribute2,
                                                    String value2,
                                                    String attribute3,
                                                    String value3,
                                                    String attribute4,
                                                    String value4,
                                                    String attribute5,
                                                    String value5) {
        AttributesImpl atts = new AttributesImpl();
        addSimpleAttribute(atts,attribute1,value1);
        addSimpleAttribute(atts,attribute2,value2);
        addSimpleAttribute(atts,attribute3,value3);
        addSimpleAttribute(atts,attribute4,value4);
        addSimpleAttribute(atts,attribute5,value5);
        return atts;
    }


    /**
     * Create an attributes list with the specified six attributes
     * and values.
     *
     * @param attribute1 Name of first attribute.
     * @param value1 Value of first attribute.
     * @param attribute2 Name of second attribute.
     * @param value2 Value of second attribute.
     * @param attribute3 Name of third attribute.
     * @param value3 Value of third attribute.
     * @param attribute4 Name of fourth attribute.
     * @param value4 Value of fourth attribute.
     * @param attribute5 Name of fifth attribute.
     * @param value5 Value of fifth attribute.
     * @param attribute6 Name of sixth attribute.
     * @param value6 Value of sixth attribute.
     * @return Resulting attribute-value list.
     */
    public static final Attributes createAttributes(String attribute1,
                                                    String value1,
                                                    String attribute2,
                                                    String value2,
                                                    String attribute3,
                                                    String value3,
                                                    String attribute4,
                                                    String value4,
                                                    String attribute5,
                                                    String value5,
                                                    String attribute6,
                                                    String value6) {
        AttributesImpl atts = new AttributesImpl();
        addSimpleAttribute(atts,attribute1,value1);
        addSimpleAttribute(atts,attribute2,value2);
        addSimpleAttribute(atts,attribute3,value3);
        addSimpleAttribute(atts,attribute4,value4);
        addSimpleAttribute(atts,attribute5,value5);
        addSimpleAttribute(atts,attribute6,value6);
        return atts;
    }



    /**
     * Converts the string to a character array in order to
     * call {@link #characters(char[],int,int)} to handle the
     * characters.
     *
     * @param handler Handler for resulting event.
     * @param s String whose characters are to be handled.
     *
     * @throws SAXException if the handler throws a SAX exception.
     */
    public static void characters(DefaultHandler handler, String s)
        throws SAXException {

        handler.characters(s.toCharArray(),0,s.length());
    }

    /**
     * Handls the specified character array by delegation to
     * {@link #characters(char[],int,int)}.
     *
     * @param handler Handler for resulting event.
     * @param cs Character array to be handled.
     *
     * @throws SAXException if the handler throws a SAX exception.
     */
    public static void characters(DefaultHandler handler, char[] cs)
        throws SAXException {

        handler.characters(cs,0,cs.length);
    }

    /**
     * Starts an element with a <code>null</code> namespace
     * and no attributes.
     *
     * @param handler Handler for resulting event.
     * @param name Name of element to start.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public static void startSimpleElement(DefaultHandler handler, String name)
        throws SAXException {

        startSimpleElement(handler,name,EMPTY_ATTS);
    }

    /**
     * Starts an element with a <code>null</code> namespace and a
     * single attribute and value.
     *
     * @param handler Handler for resulting event.
     * @param name Name of element to start.
     * @param att Name of single attribute.
     * @param value The attribute's value.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     *
     * @see #startSimpleElement(String,Attributes)
     */
    public static void startSimpleElement(DefaultHandler handler,
                                          String name, String att, String value)
        throws SAXException {

        startSimpleElement(handler,name,createAttributes(att,value));
    }

    /**
     * Starts an element with a <code>null</code> namespace and the
     * specified local name, which is also provided as the qualified
     * name.
     *
     * @param handler Handler for resulting event.
     * @param localName Local name of element to start.
     * @param atts Attributes for this element.
     *
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public static void startSimpleElement(DefaultHandler handler,
                                          String localName, Attributes atts)
        throws SAXException {

        handler.startElement(null,localName,localName,atts);
    }

    /**
     * End an element for the specified handler with a
     * <code>null</code> namespace, using the local name for both
     * local and qualified names.
     *
     * @param handler Handler for resulting event.
     * @param localName Local name of element to end.
     * @throws SAXException if the contained hanlder throws a SAX
     * exception.
     */
    public static void endSimpleElement(DefaultHandler handler,
                                        String localName)
        throws SAXException {

        handler.endElement(null,localName,localName);
    }


    /**
     * A default handler which performs no operation for any method
     * calls.
     */
    public static final DefaultHandler NO_OP_DEFAULT_HANDLER
        = new DefaultHandler();

    /**
     * The type of character data attributes.
     */
    protected static final String CDATA_ATTS_TYPE = "CDATA";

}
