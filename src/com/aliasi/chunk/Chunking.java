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

import java.util.Set;

/**
 * The <code>Chunking</code> interface specifies a set of chunks
 * over a shared underlying character sequence.  Each chunk in
 * the chunk set should provide a valid slice of that character
 * sequence.
 *
 * <P>A simple mutable implementation of the <code>Chunking</code>
 * interface is provided by the {@link ChunkingImpl} class.
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 */
public interface Chunking {

    /**
     * Returns the chunks for this chunking.
     *
     * @return The chunks for this chunking.
     */
    public Set<Chunk> chunkSet();
    
    /**
     * Returns the character sequence underlying this chunking.
     *
     * @return The character sequence underlying this set of chunks.
     */
    public CharSequence charSequence();

    /**
     * Returns <code>true</code> if the specified object is a chunking
     * equal to this one.  Equality for chunking is defined by
     * character sequence yield equality and chunk set equality.
     * Character sequences are tested for equality with {@link
     * com.aliasi.util.Strings#equalCharSequence(CharSequence,CharSequence)}
     * and chunks are compared as sets with elements tested for
     * equality using {@link Chunk#equals(Object)}.
     *
     * There is a utility implementation of this definition provided
     * for chunkings in {@link ChunkingImpl#equal(Chunking,Chunking)}.
     *
     * @param that Object to compare.
     * @return <code>true</code> if the specified object is a chunking
     * equal to this one.  
     */
    public boolean equals(Object that);


    /**
     * Returns the hash code for this chunking.  Hash codes for
     * chunkings are defined by:
     *
     * <blockquote><pre>
     * hashCode() 
     *   = Strings.hashCode(charSequence())
     *     + 31 * chunkSet().hashCode()
     * </pre></blockquote>
     *
     * There is a utility implementation of this definition provided
     * for chunkings in {@link ChunkingImpl#hashCode(Chunking)}.
     *
     * @return The hash code for this chunking.
     */
    public int hashCode();


}
