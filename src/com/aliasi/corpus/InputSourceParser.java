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

import com.aliasi.util.Strings;

import org.xml.sax.InputSource;

import java.io.CharArrayReader;
import java.io.IOException;

/**
 * An <code>InputSourceParser</code> is an abstract parser based
 * on an abstract method for parsing from an input source.  All
 * parsing methods will eventually call {@link #parse(InputSource)}.
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 * @param <H> the type of handler to which this parser sends events
 */
public abstract class InputSourceParser<H extends Handler> 
    extends Parser<H> {

    /**
     * Construct an input source parser with no specified handler.
     */
    protected InputSourceParser() { 
        this(null);
    }

    /**
     * Construct an input source parser with the specified handler.
     *
     * @param handler Handler for input events.
     */
    protected InputSourceParser(H handler) {
        super(handler);
    }

    /**
     * Parse the specified character slice.
     *
     * <P>This method is implemented by parsing an input source
     * created from a character array reader based on the specified
     * character slice.
     *
     * @param cs Underlying charactes for slice.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     * @throws IOException If there is an I/O error parsing from the string.
     * @throws IllegalArgumentException if the specified indices are out
     * of bounds of the specified character array.
     */
    @Override
    public void parseString(char[] cs, int start, int end) 
        throws IOException {

        Strings.checkArgsStartEnd(cs,start,end);
        CharArrayReader reader = new CharArrayReader(cs,start,end-start);
        InputSource in = new InputSource(reader);
        parse(in);
    }

}
