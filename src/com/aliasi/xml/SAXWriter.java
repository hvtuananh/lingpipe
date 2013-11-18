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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A <code>SAXWriter</code> handles SAX events and writes a
 * character-based representation to a specified output stream in the
 * specified character encoding.   Characters that can't be encoded
 * in the specified encoding will be written as question marks, rather
 * than being escpaed or throwing exceptions, which is the default behavior
 * of Java; this allows the character-handling methods to be faster than
 * if they had to be inspected for escapes.
 *
 * <p>An XML 1.0 declaration with an
 * explicit encoding specification is inserted as the first line of
 * the file, where <code><i>CharSet</i></code> is the name of the
 * specified charset:
 *
 * <blockquote>
 *   <code>&lt;?xml version="1.0" encoding="<i>CharSet</i>"?&gt;</code>
 * </blockquote>
 *
 * A DTD may be specified with the method {@link
 * #setDTDString(String)}, which must be called before the start
 * document handler is called.
 *
 * <p>
 * Comments, processing section, and non-ignorable whitespace
 * are left in place.  Ignorable whitespace is removed.
 * The order of attributes is alphabetized.
 *
 * <P>
 * Characters in <code>PCDATA</code> content are rendered as
 * entities if they are one of the illegal characters, or if
 * they are unicode code points that are not directly encodable
 * in the current character set.
 *
 * <blockquote>
 * <table border="1" cellpadding='5'>
 * <tr>
 *   <td><i>Character</i></td>
 *   <td><i>Name</i></td>
 *   <td><i>Entity Escape</i></td>
 * </tr>
 * <tr><td>&amp;</td><td>Ampersand</td>
 *     <td><code>&amp;amp;</code></td></tr>
 * <tr><td>&lt;</td><td>Less than</td>
 *     <td><code>&amp;lt;</code></td></tr>
 * <tr><td>&gt;</td><td>Greater than</td>
 *     <td><code>&amp;gt;</code></td></tr>
 * <tr><td>&quot;</td><td>Double quote</td>
 *     <td><code>&amp;quot;</code></td></tr>
 * <tr><td>U+<i>wxyz</i></td>
 *     <td>Unencodable Hex Unicode <i>wxyz</i></td>
 *     <td><code>&amp;#x<i>wxyz</i>;</code></td></tr>
 * </table>
 * </blockquote>
 *
 * Note that unmatched unicode surrogate pairs should not be presented
 * through {@link #characters(char[],int,int)}.  Specifically, every
 * low surrogate must be followed by a high surrogate, and every high
 * surrogate must be preceded by a low surrogate.  A low surrogate is
 * a unicode character in the range <code>U+D800</code> to
 * <code>U+DBFF</code> inclusive.  A high surrogate is a character in
 * the range <code>U+DC00</code> to </code>U+DFFF</code> inclusive.
 * The code points for sentinels, <code>U+FFFF</code>, and byte-order
 * marking, <code>U+FFFE</code>, should also not be encoded. An
 * attempt to encode an unmatched surrogate or sentinel/indicator will
 * not raise an exception on output; the characters will simply be
 * output.  The resulting XML bytes will not be converted back to
 * their original form by a unicode-compliant byte-to-character
 * converter.  Default settings of {@link java.io.InputStreamReader} will
 * simply perform a substitution.
 *
 * <P>The SAXWriter does not test document well-formedness.  Nor does
 * it test well-formedness with respect to a document-type definition
 * (DTD).  For instance, an entity <code>foo</code> can be ended by an
 * entity <code>bar</code> and <code>&lt;foo&gt;&lt/bar&gt;</code>
 * will be output.  As with other handlers, to throw exceptions in the
 * face of ill-formed documents, compose a well-formedness filter with
 * a SAXWriter.
 *
 * <p>If <code>xmlReader</code> is an {@link org.xml.sax.XMLReader} and
 * <code>contentHandler</code> is a {@link org.xml.sax.ContentHandler}, then
 *
 * <pre>
 *   xmlReader.setContentHandler(contentHandler);
 *   xmlReader.parse(in);
 * </pre>
 *
 * calls the same methods (modulo order of attributes and I/O
 * exceptions) on the <code>contentHandler</code> as the following
 * sequences of methods which write an intermediate XML file:
 *
 * <pre>
 *  FileOutputStream out = new FileOutputStream(fileName);
 *  xmlReader.setContentHandler(new SAXWriter(out,"UTF8"));
 *  xmlReader.parse(new InputSource(in));
 *  out.close();
 *  xmlReader.setContentHandler(contentHandler);
 *  xmlReader.parse(fileName);
 * </pre>
 *
 * <P>The <code>SAXWriter</code> handles namespace declarations
 * according to the SAX 2 specification.  It does this by storing the
 * URI and namespace prefix received through the {@link
 * #startPrefixMapping(String,String)} event and prints it in the
 * usual way as part of the attribute declaration of the next element.
 * For proper behavior, the SAXWriter must receive start element
 * events that are consistent with the following feature settings:
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td><i>Feature</i></td>
 *     <td><i>Value</i></td>
 *     <td><i>Description</i></td></tr>
 * <tr><td><code>http://xml.org/sax/features/namespaces</code></td>
 *     <td><code>true or false</code></td>
 *     <td>Provides URI arguments to elements and attributes.</td></tr>
 * <tr><td><code>http://xml.org/sax/features/namespace-prefixes</code></td>
 *     <td><code>true</code></td>
 *     <td>Provides qualified name arguments to elements and attributes.</td></tr>
 * </table>
 * </blockquote>
 *
 * For more information on these and other features, see:
 *
 * <UL>
 * <LI> <a href="http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html#package_description">Standard SAX2 Features</a>
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class SAXWriter extends SimpleElementHandler {

    /**
     * Encoder for the current charset.  Used to test if
     * a character is encodable or needs to be escaped.
     */
    private CharsetEncoder mCharsetEncoder;

    /**
     * Printer to which characters are written.
     */
    private PrintWriter mPrinter;

    /**
     * Buffered writer to which printer writers characters.
     */
    private BufferedWriter mBufWriter;

    /**
     * Output stream writer to which buffered writer writers
     * characters for conversion to bytes.  Wrapping by buffer as per
     * recommendation in {@link java.io.OutputStreamWriter} class
     * documentation.
     */
    private OutputStreamWriter mWriter;

    /**
     * Character set in which characters are encoded.
     */
    private String mCharsetName;

    /**
     * The string to write for a DTD declaration in the XML file, or
     * <code>null</code> if none.
     */
    private String mDtdString = null;

    /**
     * Set to true if an element has been started, but
     * not yet closed with a final right angle bracket.
     */
    private boolean mStartedElement;

    private String mStartedNamespaceURI;
    private String mStartedLocalName;
    private String mStartedQName;
    private boolean mStartedHasAtts;

    private final Map<String,String> mPrefixMap = new HashMap<String,String>();

    private boolean mXhtmlMode;

    /**
     * Construct a SAX writer that writes to the specified output
     * stream using the specified character set.  See {@link
     * #setOutputStream(OutputStream,String)} for details on the
     * management of the output stream and character set.
     *
     * <p>By default, the SAXWriter is not in XHTML mode.
     * See {@link #SAXWriter(OutputStream,String,boolean)} for
     * more information.
     *
     * @param out Output stream to which bytes are written.
     * @param charsetName Name of character encoding used to write output.
     * @throws UnsupportedEncodingException If the character set is
     * not supported.
     */
    public SAXWriter(OutputStream out, String charsetName)
        throws UnsupportedEncodingException {

        this(out,charsetName,false);
    }


    /**
     * Construct a SAX writer that writes to the specified output
     * stream using the specified character set and specified XHTML
     * compliance.  See {@link #setOutputStream(OutputStream,String)}
     * for details on the management of the output stream and
     * character set.
     *
     * <p>Compliance with the XHTML compliance goes beyond well-formed
     * XML documents.  Although each XHTML document must be
     * well-formed XML, not all well-formed XML documents are XHTML
     * compliant.  XHTML imposes two additional requirements on the
     * expression of elements.  The first requires a space before
     * elements ended inline.  Thus although the element
     * <code>&lt;br/&gt;</code> is perfectly well-formed XML, in XHTML
     * it must be written as <code>&lt;br /&gt;</code>.  The second
     * requirement is that there be a distinct end tag for elements
     * with attributes.  This forbids valid XML such as <code>&lt;a
     * name=&quot;foo&quot;/&gt;</code>, requiring the alternative
     * form <code>&lt;a name=&quot;foo&quot;&gt;&lt;/a&gt;</code> for
     * XHTML compliance.
     *
     * @param out Output stream to which bytes are written.
     * @param charsetName Name of character encoding used to write output.
     * @param xhtmlMode Set to <code>true</code> to render
     * XHTML-compliant output.
     * @throws UnsupportedEncodingException If the character set is
     * not supported.
     */
    public SAXWriter(OutputStream out, String charsetName,
                     boolean xhtmlMode) throws UnsupportedEncodingException {
        this(xhtmlMode);
        setOutputStream(out,charsetName);
    }



    /**
     * Construct a SAX writer that does not have an output stream or
     * character set specified.  These must be set through {@link
     * #setOutputStream(OutputStream,String)} or an illegal state
     * exception will be thrown by any output method.  By default, the
     * XHTML mode is turned off.  See {@link
     * #SAXWriter(OutputStream,String,boolean)} for more information
     * on XHTML compliance.
     */
    public SAXWriter() {
        this(false);
    }

    /**
     * Construct a SAX writer with the specified XHTML compliance
     * mode, but without an output stream or character set specified.
     * These must be set through {@link
     * #setOutputStream(OutputStream,String)} or an illegal state
     * exception will be thrown by any output method.  By default, the
     * XHTML mode is turned off.  See {@link
     * #SAXWriter(OutputStream,String,boolean)} for more information
     * on XHTML compliance.
     *
     * @param xhtmlMode Set to <code>true</code> to render
     * XHTML-compliant output.
     */
    public SAXWriter(boolean xhtmlMode) {
        mXhtmlMode = xhtmlMode;
    }

    /**
     * Sets the DTD to be written by this writer to the specified
     * value.  There is no error checking on its well-formedness, and
     * it is not wrapped in any way other than being printed on its
     * own line; this allows arbitrary DTDs to be written.
     *
     * @param dtdString String to write after the XML declaration as
     * the DTD declaration.
     */
    public void setDTDString(String dtdString) {
        mDtdString = dtdString;
    }


    /**
     * Sets the output stream to which the XML is written, and the
     * character set which is used to encode characters.  Before
     * writing a document, the output stream and character set must be
     * set by the constructor or by this method.  The output stream is
     * not closed after an XML document is written, but all output to
     * the stream will be produced and does not need to be otherwise
     * flushed.
     *
     * @param out Output stream to which encoded characters are written.
     * @param charsetName Character set to use for encoding characters.
     * @throws UnsupportedEncodingException If the character set is
     * not supported by the Java runtime.
     */
    public final void setOutputStream(OutputStream out, String charsetName)
        throws UnsupportedEncodingException {

        Charset charset = Charset.forName(charsetName);
        mCharsetEncoder = charset.newEncoder();
        mWriter = new OutputStreamWriter(out,mCharsetEncoder);
        mBufWriter = new BufferedWriter(mWriter);
        mPrinter = new PrintWriter(mBufWriter); // no auto-flush
        mCharsetName = charsetName;
    }


    // ContentHandler

    /**
     * Prints the XML declaration, and DTD declaration if any.
     */
    @Override
    public void startDocument() {
        printXMLDeclaration();
        mStartedElement = false;
    }

    /**
     * Flushes the underlying character writers output to the
     * output stream, trapping all exceptions.
     */
    @Override
    public void endDocument() {
        if (mStartedElement) {
            endElement(mStartedNamespaceURI,
                       mStartedLocalName,
                       mStartedQName);
        }
        mPrinter.flush();
        try {
            mBufWriter.flush();
        } catch (IOException e) {
            // ignore exception
        }
        try {
            mWriter.flush();
        } catch (IOException e) {
            // ignore exception
        }
    }

    /**
     * Handles the declaration of a namespace mapping from a specified
     * URI to its identifying prefix.  The mapping is buffered and
     * then flushed and printed as an attribute during the next
     * start-element call.
     *
     * @param prefix The namespace prefix being declared..
     * @param uri The namespace URI mapped to prefix.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) {
        mPrefixMap.put(prefix,uri);
    }

    /**
     * Prints the start element, using the qualified name, and sorting
     * the attributes using the underlying string ordering.  Namespace
     * URI and local names are ignored, and qualified name must not be
     * <code>null</code>.
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     * @param atts The attributes for this element.
     */
    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) {
        if (mStartedElement) {
            mPrinter.print(">");
            mStartedElement=false;
        }
        mPrinter.print('<');
        mPrinter.print(qName);
        printAttributes(atts);
        mStartedElement = true;
        mStartedNamespaceURI = namespaceURI;
        mStartedLocalName = localName;
        mStartedQName = qName;
        mStartedHasAtts = atts.getLength() > 0;
        // close is picked up later in implicit continuation
        // mPrinter.print('>');
    }

    /**
     * Prints the end element, using the qualified name.  Namespace
     * URI and local name parameters are ignored, and the qualified
     * name must not be <code>null</code>
     *
     * @param namespaceURI The URI of the namespace for this element.
     * @param localName The local name (without prefix) for this
     * element.
     * @param qName The qualified name (with prefix, if any) for this
     * element.
     */
    @Override
    public void endElement(String namespaceURI, String localName,
                           String qName) {

        if (mStartedElement && !mXhtmlMode) {
            mStartedElement = false;
            mPrinter.print("/>");
        } else if (mStartedElement && !mStartedHasAtts) {
            mStartedElement = false;
            mPrinter.print(" />");
        } else {
            if (mStartedElement) {
                mPrinter.print(">");
                mStartedElement = false;
            }
            mPrinter.print('<');
            mPrinter.print('/');
            mPrinter.print(qName);
            mPrinter.print('>');
        }
    }

    /**
     * Prints the characters in the specified range.
     *
     * @param ch Character array from which to draw characters.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (mStartedElement) {
            mPrinter.print('>');
            mStartedElement = false;
        }
        printCharacters(ch,start,length);
    }

    /**
     * Does not print ignorable whitespace.
     *
     * @param ch Character array from which to draw characters.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
        /* ignore ignorable whitespace */
    }

    /**
     * Print a representation of the proecssing instruction.  This
     * will be <code>&langle;?<i>Target</i>&rangle;</code> if there is
     * no data, or * <code>&lt;?<i>Target</i>
     * <i>Data</i>&gt;</code> if there is data.
     *
     * @param target Target of the instruction.
     * @param data Value of the instruction, or <code>null</code>.
     */
    @Override
    public void processingInstruction(String target, String data) {
        if (mStartedElement) {
            mPrinter.print('>');
            mStartedElement = false;
        }
        mPrinter.print("<?");
        mPrinter.print(target);
        if (data != null && data.length() > 0) {
            mPrinter.print(' ');
            mPrinter.print(data);
        }
        mPrinter.print("?>");
    }

    /**
     * Convenience method to write a slice of character data as a
     * comment.  This method delegates a new string created from the
     * specified slice to {@link #comment(String)}; see that method's
     * documentation for more information.
     *
     * <p>Exceptions match those thrown by {@link
     * String#String(char[],int,int)}.
     *
     * @param cs Underlying characters.
     * @param start First character in sequence.
     * @param length Number of characters in sequence.
     * @throws IndexOutOfBoundsException if <code>start</code> and
     * <code>length</code> are out of bounds.
     */
    public void comment(char[] cs, int start, int length) {
        comment(new String(cs,start,length));
    }

    /**
     * Write the specified string as a comment.  The string is first
     * sanitized by breaking any double hyphens
     * (<code>&quot;--&quot;</code>) with a space (producing
     * <code>&quot;-&nbsp;-&quot;</code>).  If the comment starts with
     * a hyphen (<code>-</code>), a space is inserted before the
     * comment (causing it to start with <code>&nbsp;-</code>).  If
     * the comment ends with a hyphen (<code>-</code>), a space is
     * appended (causing it to end <code>-&nbsp;</code>).
     *
     * <p>Comments are written between comment delimeters for the
     * begin (<code>&lt;--</code>) and end (<code>--&gt;</code>) of a
     * comment.  No extra space is inserted after the opening hyphen
     * or before the closing hyphen, and no extra line-breaks, etc.
     * are inserted.  The method {@link #characters(char[],int,int)}
     * may be used for inserting additional formatting, but beware
     * that this adds whitespace to the current element's content
     * which is only ignored if there is a DTD specifiying that no
     * text content is allowed in the current element.
     *
     * @param comment Comment to write.
     */
    public void comment(String comment) {
        mPrinter.print(START_COMMENT);
        String noDoubleHyphenComment = comment.replaceAll("--","- -");
        if (noDoubleHyphenComment.startsWith("-"))
            mPrinter.print(" ");
        mPrinter.print(noDoubleHyphenComment);
        if (noDoubleHyphenComment.endsWith("-"))
            mPrinter.print(" ");
        mPrinter.print(END_COMMENT);
    }


    /**
     * Returns the name of the character set being used by
     * this writer.
     *
     * @return The character set for this writer.
     */
    public String charsetName() {
        return mCharsetName;
    }

    // prints atts and outstanding namespace decls
    private void printAttributes(Attributes atts) {
        if (mPrefixMap.size() > 0) {
            for (Map.Entry<String,String> entry : mPrefixMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                printAttribute(key.length() == 0
                               ? "xmlns"
                               : "xmlns:" + key,
                               value);
            }
            mPrefixMap.clear();
        }
        Set<String> orderedAtts = new TreeSet<String>();
        for (int i = 0; i < atts.getLength(); ++i)
            orderedAtts.add(atts.getQName(i));
        for (String attQName : orderedAtts) {
            printAttribute(attQName,atts.getValue(attQName));
        }
    }

    /**
     * Prints an attribute and value, with value properly
     * escaped. Prints leading space before attribute and value pair.
     *
     * @param att Attribute name.
     * @param val Attribute value.
     */
    private void printAttribute(String att, String val) {
        mPrinter.print(' ');
        mPrinter.print(att);
        mPrinter.print('=');
        mPrinter.print('"');
        printCharacters(val);
        mPrinter.print('"');
    }

    /**
     * Print the specified string, with appropriate escapes.
     *
     * @param s Print the characters in the specified string.
     */
    private void printCharacters(String s) {
        printCharacters(s.toCharArray(),0,s.length());
    }

    /**
     * Print the specified range of characters, with escapes.
     *
     * @param ch Array of characters from which to draw.
     * @param start Index of first character to print.
     * @param length Number of characters to print.
     */
    private void printCharacters(char[] ch, int start, int length) {
        for (int i = start; i < start+length; ++i)
            printCharacter(ch[i]);
    }

    /**
     * Print the specified character, rendering it as an entity if
     * necessary (see class doc).
     *
     * @param c Character to print.
     */
    private void printCharacter(char c) {
        // note: does not catch illegal conjugate pairs
        if (!mCharsetEncoder.canEncode(c)) {
            printEntity("#x" + Integer.toHexString((int)c));
            return;
        }
        switch (c) {
        case '<':  { printEntity("lt"); break; }
        case '>':  { printEntity("gt"); break; }
        case '&':  { printEntity("amp"); break; }
        case '"':  { printEntity("quot"); break; }
        default:   { mPrinter.print(c); }
        }
    }

    /**
     * Print the specified entity.
     *
     * @param entity Name of entity to print.
     */
    private void printEntity(String entity) {
        mPrinter.print('&');
        mPrinter.print(entity);
        mPrinter.print(';');
    }

    /**
     * Prints the XML declaration, including the character set
     * declaration and the DTD if any is defined.  The declaration
     * printed is:
     * <blockquote>
     *   <code>&lt;?xml version="1.0" encoding="<i>CharSet</i>"?&gt;</code>.
     * </blockquote>
     * where <code><i>CharSet</i></code> is the string representation
     * of the character set being used.  No spaces are included after
     *  the XML declaration or the DTD declaration, if any.
     */
    private void printXMLDeclaration() {
        mPrinter.print("<?xml");
        printAttribute("version","1.0");
        printAttribute("encoding",mCharsetName);
        mPrinter.print("?>");
        if (mDtdString != null) {
            mPrinter.print(mDtdString);
        }
    }

    private static String START_COMMENT = "<!--";
    private static String END_COMMENT = "-->";

}
