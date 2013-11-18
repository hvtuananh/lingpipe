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

package com.aliasi.corpus;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;

/**
 * The <code>Parser</code> abstract class provides methods for parsing
 * content from an input source or character sequence and passing
 * extracted events to a content handler.  Concrete implementations will
 * typically make assumptions about the type of the handler.
 *
 * <P>Concrete subclasses must implement both {@link
 * #parse(InputSource)} and {@link #parseString(char[],int,int)}.  Two
 * subclasses of this class, {@link InputSourceParser} and {@link
 * StringParser} may be extended by implementing only one of the
 * above methods.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe2.1
 * @param <H> the type of handler which receives events from this parser
 */
public abstract class Parser<H extends Handler> {

    private H mHandler;

    /**
     * Construct a parser with a <code>null</code> handler.
     */
    public Parser() {
        this(null);
    }

    /**
     * Construct a parser with the specified handler.
     *
     * @param handler Current handler.
     */
    public Parser(H handler) {
        mHandler = handler;
    }

    /**
     * Sets the content handler to the specified value.  The current
     * handler is used for all content extracted by this parser.
     *
     * @param handler Handler to use for content extracted from parsed
     * content.
     */
    public void setHandler(H handler) {
        mHandler = handler;
    }

    /**
     * Returns the current content handler.  The current handler is
     * applied to all extracted content.
     *
     * @return Current content handler.
     */
    public H getHandler() {
        return mHandler;
    }

    /**
     * Parse the specified system identifier, usually a URL name,
     * passing extracted events to the handler.
     *
     * <P>The implementation provided by this abstract class
     * constructs an input source from the system identifier
     * and passes it to {@link #parse(InputSource)}.
     *
     * <p><b>Warning:</b> This method does not specify a character
     * encoding.  Behavior will depend on the implementation.
     * See {@link #parse(String,String)} to set the encoding.
     *
     * @param sysId System ID from which to read.
     * @throws IOException If there is an exception reading
     * from the specified source.
     */
    public void parse(String sysId) throws IOException {
        InputSource in = new InputSource(sysId);
        parse(in);
    }

    /**
     * Parse the specified system identifier, usually a URL name,
     * using the specified character encoding, passing extracted
     * events to the handler.
     *
     * <p><b>Warning:</b> The character encoding is set on the
     * constructed input source, but this may be ignored by the
     * parser.
     *
     * @param sysId System identifier for data, typically a URL name.
     * @param charEncoding Character encoding to use for the data.
     */
    public void parse(String sysId, String charEncoding) throws IOException {
        InputSource in = new InputSource(sysId);
        in.setEncoding(charEncoding);
        parse(in);
    }

    /**
     * Parse the specified file, passing extracted events to the
     * handler.
     *
     * <p>The file is converted to a URL name using
     * {@code File.toURI().toURL().toString()}.  It is then used
     * to construct an input source which is parsed using
     * the {@code #parse(InputSource)} method.
     *
     * <p><b>Warning:</b> This method does not specify a character
     * encoding on the constructed input source.  Behavior will depend
     * on the implementation.  See {@link #parse(File,String)} to set
     * the character encoding.
     *
     * @param file File to parse.
     * @throws IOException If there is an exception reading
     * from the specified file or it does not exist.
     */
    public void parse(File file) throws IOException {
        parse(file.toURI().toURL().toString()); 
    }

    /**
     * Parse the specified file using the specified character
     * encoding, passing events to the handler.
     *
     * <p>The file is converted to a URL name using
     * {@code File.toURI().toURL().toString()}.
     * 
     * <p><b>Warning:</b> The character encoding will be set on the
     * input source for parsing, but parser implementations may
     * choose to ignore it.
     *
     * @param file File from which to read.
     * @param charEncoding Characte encoding for text.
     * @throws IOException If there is an exception reading
     * from the specified source.
     */
    public void parse(File file, String charEncoding) 
        throws IOException {

        String urlName = file.toURI().toURL().toString();
        InputSource in = new InputSource(urlName);
        in.setEncoding(charEncoding);
        parse(in);
    }
    

    /**
     * Parse the specified character sequence.   Extracted content
     * is passed to the current handler.
     *
     * <P>The character sequence is converted to a character array
     * using {@link Strings#toCharArray(CharSequence)} and then
     * passed as a slice to to {@link #parseString(char[],int,int)}.
     *
     * @param cSeq Character sequence to parse.
     * @throws IOException If there is an exception reading the
     * characters.
     */
    public void parseString(CharSequence cSeq) throws IOException {
        char[] cs = Strings.toCharArray(cSeq);
        parseString(cs,0,cs.length);
    }

    /**
     * Parse the specified input source, passing extracted events to
     * the handler.  Concrete subclasses must implement this method.
     *
     * @param in Input source from which to read.
     * @throws IOException If there is an exception reading from the
     * specified stream.
     */
    abstract public void parse(InputSource in) throws IOException;

    /**
     * Parse the specified character slice as a string input.  Extracted
     * content is passed to the current handler.
     *
     * @param cs Characters underlying slice.
     * @param start Index of first character in slice.
     * @param end One past the index of the last character in slice.
     * @throws IOException If there is an exception reading the
     * characters.
     */
    abstract public void parseString(char[] cs, int start, int end)
        throws IOException;

}
