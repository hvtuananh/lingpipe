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

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>TextAccumulatorHandler</code> simply accumulates all text
 * content events into a single string buffer.  The buffer is reset
 * with each start document event, but is never freed entirely from
 * memory.
 *
 * @author  Bob Carpenter
 * @version 3.6
 * @since   LingPipe2.0
 */
public class TextAccumulatorHandler extends DefaultHandler {

    private int mMinBufLength;
    private StringBuilder mBuf;

    /**
     * Construct a text accumulator handler.
     */
    public TextAccumulatorHandler() {
        this(128);
    }

    /**
     * Construct a text accumulator handler with the specified
     * initial buffer length.
     *
     * @param minBufLength Minimum buffer length to assign to string buffer.
     */
    public TextAccumulatorHandler(int minBufLength) {
        mMinBufLength = minBufLength;
        mBuf = new StringBuilder(mMinBufLength);
    }

    /**
     * Resets the underlying string buffer to the empty state by
     * assigning it to a new buffer of the specified minimum length.
     */
    public void reset() {
        mBuf = new StringBuilder(mMinBufLength);
    }

    /**
     * Resets the string buffer.
     */
    @Override
    public void startDocument() {
        reset();
    }

    /**
     * Adds the specified character slice to the string buffer.
     *
     * @param cs Characters underlying slice.
     * @param start Index of first character in slice.
     * @param length Number of characters in slice.
     */
    @Override
    public void characters(char[] cs, int start, int length) {
        if (mBuf == null) return;
        mBuf.append(cs,start,length);
    }

    /**
     * Returns the text thus far accumulated.
     *
     * @return The text thus far accumulated.
     */
    public String getText() {
        return mBuf.toString();
    }
    
    /**
     * A convenience implementation returning the same value as
     * <code>getText()</code>, namely the text thus far accumulated.
     *
     * @return The text thus far accumulated.
     */
    @Override
    public String toString() {
        return getText();
    }

}

