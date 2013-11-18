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

import com.aliasi.util.Scored;

import java.util.Comparator;

/**
 * The <code>Chunk</code> interface specifies a slice of a character
 * sequence, a chunk type and a chunk score.  It is important to keep
 * in mind that a chunk only stores character offsets into a character
 * sequence, not the character sequence itself.  A chunk is almost
 * always associated with a {@link Chunking} consisting of a character
 * sequence and set of chunks over that sequence.

 * <P>Equality for chunks is defined by the equality of the chunk's
 * components (see the method documentation of {@link #equals(Object)}
 * for details).  Hash codes are defined to be consistent with
 * equality (see the method documentation of {@link #hashCode()} for
 * details).
 *
 * <P>Chunks may be constructed using static methods in the {@link
 * ChunkFactory} class or they may be implemented directly.
 * 
 * <P>The chunk interface extends the {@link Scored} interface, so
 * chunks may be ordered by the {@link
 * com.aliasi.util.ScoredObject#comparator()} and {@link
 * com.aliasi.util.ScoredObject#reverseComparator()} comparators.
 * Note that these comparators are not consistent with equality, but
 * may be used for sorting chunks in score order in arrays.
 *
 * <P>Chunks may be ordered by their offsets using {@link
 * #TEXT_ORDER_COMPARATOR}.  Ordering the chunks of a given chunking
 * using this comparator produces an ordering based on first
 * appearance (and length in the case of ties).  An alternative
 * ordering is {@link #LONGEST_MATCH_ORDER_COMPARATOR}.
 * 
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.1
 */
public interface Chunk extends Scored {

    /**
     * Returns the index of the first character in this chunk.
     * 
     * @return The index of the first character in this chunk.
     */
    public int start();

    /**
     * Returns the index of one past the last character in this chunk.
     * 
     * @return The index of one past the last character in this chunk.
     */
    public int end();

    /**
     * Returns the type of this chunk.
     *
     * @return The type of this chunk.
     */
    public String type();

    /**
     * Returns the score of this chunk. 
     *
     * @return The score of this chunk.
     */
    public double score();

    /**
     * Returns <code>true</code> if the specified object is a chunk
     * that is equal to this chunk.  Another chunk is equal to this
     * one if they have the same start, end, type and score.
     *
     * @param that Object to compare to this chunk.
     * @return <code>true</code> if the specified object is equal to
     * this chunk.
     */
    public boolean equals(Object that);

    /**
     * Returns this chunk's hash code.  A chunk's hash code depends
     * on its start, end and type, but not its score:
     *
     * <blockquote><code>
     * hashCode() = start() + 31 * (end() + 31 * type().hashCode())
     * </code></blockquote>
     *
     * @return The hash code for this chunk.
     */
    public int hashCode();

    /**
     * Compares two chunks based on their text position.  A chunk is
     * greater if it starts later than another chunk, or if it starts
     * at the same position and ends later.  This comparator is not
     * compatible with {@link #equals(Object)}, but may be used for
     * sorting using {@link java.util.Arrays#sort(Object[],Comparator)}.
     */
    public static final Comparator<Chunk> TEXT_ORDER_COMPARATOR
    = new Comparator<Chunk>() {
        public int compare(Chunk c1, Chunk c2) {
            if (c1.start() < c2.start()) return -1;
            if (c1.start() > c2.start()) return 1;
            if (c1.end() < c2.end()) return -1;
            if (c1.end() > c2.end()) return 1;
            return 0;
        }
        };


    /**
     * Compares two chunks based on their text position.  A chunk is
     * greater if it starts later than another chunk, or if it starts
     * at the same position and ends earlier.  A chunk is also greater
     * if it starts and ends at the same point and has a higher score.
     * If start, end and scores are the same, the types are compared
     * alphabetically.
     * 
     * <p>This comparator is not compatible with {@link
     * #equals(Object)}, but may be used for sorting using {@link
     * java.util.Arrays#sort(Object[],Comparator)}.
     */
    public static final Comparator<Chunk> LONGEST_MATCH_ORDER_COMPARATOR
    = new Comparator<Chunk>() {
        public int compare(Chunk c1, Chunk c2) {
            if (c1.start() < c2.start()) return -1;
            if (c1.start() > c2.start()) return 1;
            if (c1.end() < c2.end()) return 1;
            if (c1.end() > c2.end()) return -1;
            if (c1.score() > c2.score()) return -1;
            if (c1.score() < c2.score()) return 1;
            return c1.type().compareTo(c2.type());
        }
        };
}


