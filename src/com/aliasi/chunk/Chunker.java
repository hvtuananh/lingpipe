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

package com.aliasi.chunk;

/**
 * The <code>Chunker</code> interface specifies methods for returning
 * a chunking given a character sequence or character slice.
 *
 * <P>Note that a <code>Chunker</code> may be used to implement a
 * {@link com.aliasi.corpus.Parser} that extends {@link
 * com.aliasi.corpus.StringParser} by implementing its {@link
 * com.aliasi.corpus.StringParser#parseString(char[],int,int)} method using
 * the chunker method {@link #chunk(char[],int,int)}.  
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public interface Chunker {

    /**
     * Return the chunking of the specified character sequence.
     *
     * @param cSeq Character sequence to chunk.
     * @return A chunking of the character sequence.
     */
    public Chunking chunk(CharSequence cSeq);

    /**
     * Return the chunking of the specified character slice.
     *
     * @param cs Underlying character sequence.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     * @return The chunking over the specified character slice.
     */
    public Chunking chunk(char[] cs, int start, int end);

}
