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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import com.aliasi.xml.SAXWriter;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;



public class SAXWriterTest  {


    @Test
    public void testCharsets() throws IOException, SAXException {
        assertReadWrite("<?xml version=\"1.0\" encoding=\"UTF-8\"?><a/>","UTF-8");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ASCII\"?><a/>","ASCII");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a/>","ISO-8859-1");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"UTF-16\"?><a/>","UTF-16");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a/>","ISO-8859-1");
    }

    @Test
    public void testIgnorableWhitespace() throws IOException, SAXException {
        assertReadWrite("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <a/> ",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><a/>",
                        "UTF-8");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <a></a> ",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><a/>",
                        "UTF-8");
    }

    @Test
    public void testIllegalChars() throws IOException, SAXException {
        assertReadWrite("<?xml version=\"1.0\" encoding=\"UTF-16\"?><a>\u2297</a>","UTF-16");
        // encodes \u2297, using &#2297;instead
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>\u2297</a>",
                        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>&#x2297;</a>",
                        "ISO-8859-1");

    }

    @Test
    public void testNamespaces() throws IOException, SAXException {
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><foo:a xmlns:foo=\"http://bar\">Hello World.</foo:a>",
                        "ISO-8859-1");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a xmlns=\"http://bar\">Hello World.</a>",
                        "ISO-8859-1");
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a xmlns=\"http://bar\">Hello World.</a>",
                        "ISO-8859-1");
        // need better test that doesn't depend on ordering
        // assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a xmlns:foo=\"http://baz\" xmlns=\"http://bar\"><foo:x>Hello World.</foo:x></a>",
        // "ISO-8859-1");
    }

    @Test
    public void testXHTML() throws IOException, SAXException {
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><a name=\"foo\"/><br/></html>",
                        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><a name=\"foo\"></a><br /></html>",
                        "ISO-8859-1",
                        true);
        assertReadWrite("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><a name=\"foo\"/><br/></html>",
                        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><a name=\"foo\"></a><br /></html>",
                        "ISO-8859-1",
                        true);
    }

    private void assertReadWrite(String sourceString, String charset) 
        throws IOException, SAXException {

        assertReadWrite(sourceString,charset,false);
    }
    
    private void assertReadWrite(String sourceString, String charset, boolean xhtmlMode)
        throws IOException, SAXException {

        assertReadWrite(sourceString,sourceString,charset,xhtmlMode);

    }

    private void assertReadWrite(String sourceString, String targetString, 
                                 String charset) 
        throws IOException, SAXException {
    
        assertReadWrite(sourceString,targetString,charset,false);
    }


    private void assertReadWrite(String sourceString, String targetString, 
                                 String charset, boolean xhtmlMode)
        throws IOException, SAXException {


        CharArrayReader reader 
            = new CharArrayReader(sourceString.toCharArray());
        InputSource in = new InputSource(reader);
        in.setEncoding(charset);

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        SAXWriter writer = new SAXWriter(bytesOut,charset,xhtmlMode);
    
        xmlReader.setContentHandler(writer);
        xmlReader.setDTDHandler(writer);
        xmlReader.parse(in);
        byte[] bytes = bytesOut.toByteArray();
        String s = new String(bytes,0,bytes.length,charset);
        assertEquals("\n" + targetString + " \n!=\n" + s + "\n", targetString ,s);
    }


}
