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

import com.aliasi.util.Streams;

import java.io.IOException;

import org.xml.sax.InputSource;

/**
 * A <code>StringParser</code> is an abstract parser based on an
 * abstract method for parsing from a character slice.  All parsing
 * methods will eventually call {@link #parseString(char[],int,int)}.
 *
 * <p>Input sources are converted to character sequences using
 * {@link Streams#toCharArray(InputSource)}.
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 * @param <H> the type of handler to which this parser sends events
 */
public abstract class StringParser<H extends Handler> 
    extends Parser<H> {

    /**
     * Construct a string parser with no handler specified.
     */
    protected StringParser() { 
        this(null);
    }

    /**
     * Construct a string parser with the specified handler.
     *
     * @param handler Handler for input events.
     */
    protected StringParser(H handler) {
        super(handler);
    }

    /**
     * Parse the specified input source.
     *
     * <P>The implementation here converts the input source to a
     * character slice using {@link Streams#toCharArray(InputSource)}
     * and passes the result to {@link #parseString(char[],int,int)}.
     *
     * @param in Input source from which to read.
     * @throws IOException If there is an I/O error reading from the
     * input source.
     */
    @Override
    public void parse(InputSource in) throws IOException {
        char[] cs = Streams.toCharArray(in);
        parseString(cs,0,cs.length);
    }

}
