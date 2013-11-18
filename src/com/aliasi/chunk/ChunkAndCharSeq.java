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
import com.aliasi.util.Strings;

/**
 * The <code>ChunkAndCharSeq</code> is an immutable composite of a
 * chunk and a character sequence.  This class also implements the
 * {@link Scored} interface based on chunk score (see {@link
 * #score()}), so that elements may be sorted by score.
 *
 * <P>Equality for the composite is based on object equality.  This
 * means that two chunk and character sequences may have the
 * same span over the same character sequence with the same type
 * and same score and still be different. 
 *
 * <P>Instances of this class are returned in the sets produced by a
 * {@link ChunkingEvaluation} and in a {@link
 * com.aliasi.sentences.SentenceEvaluation}.
 * 
 * @author  Mitzi Morris
 * @version 2.1.1
 * @since   LingPipe2.1
 */
public class ChunkAndCharSeq implements Scored {

    private final Chunk mChunk;
    private final CharSequence mSeq;
    private final int mHashCode;

    /**
     * Construct a composite of the specified chunk and sequence.
     *
     * @param chunk The underlying chunk.
     * @param cSeq The character sequence for the chunk.
     * @throws IllegalArgumentException If the chunk bounds exceed the
     * sequence length.
     */
    public ChunkAndCharSeq(Chunk chunk, CharSequence cSeq) {
	if (chunk.end() > cSeq.length()) {
	    String msg = "Character sequence not long enough for chunk."
		+ " Chunk end=" + chunk.end()
		+ " Character sequence length=" + cSeq.length();
	    throw new IllegalArgumentException(msg);
	}
	mChunk = chunk;
	mSeq = cSeq;
	mHashCode = chunk.hashCode() + 31*Strings.hashCode(cSeq);
    }

    /**
     * Returns the cached hash code for this chunk and character
     * sequence.
     *
     * @return The hash code for this chunk and character sequence.
     */
    @Override
    public int hashCode() {
	return mHashCode;
    }

    /**
     * Returns <code>true</code> if the specified object is a chunk
     * and character sequence structurally equivalent to this one.
     * For equality, the character sequences must be equivalent and
     * the chunks must be equivalent.
     *
     * @param that Object to test for equality.
     * @return <code>true</code> if equivalent to this object.
     */
    @Override
    public boolean equals(Object that) {
	if (!(that instanceof ChunkAndCharSeq)) return false;
	ChunkAndCharSeq thatChunk = (ChunkAndCharSeq) that;
	if (thatChunk.hashCode() != hashCode()) return false; // cached
	return mChunk.equals(thatChunk.mChunk)
	    && mSeq.equals(thatChunk.mSeq);
    }

    /**
     * Return the characters spanned by this chunk.  Note that this is
     * the subsequence of the character sequence,
     * <code>sequence()</code>, running from this chunk's start index,
     * <code>chunk().start()</code>, up to but not including the chunk's
     * end index, <code>chunk().end()</code>.
     *
     * @return The characters spanned by this chunk.
     */
    public String span() {
	return mSeq.subSequence(mChunk.start(),mChunk.end()).toString();
    }


    /**
     * Return a span of characters centered around the start of this
     * chunk plus or minus the context length.  If there isn't
     * sufficient context, the returned string will be padded so that
     * the chunk start is at position contextLen. Note that this is the
     * subsequence of the character sequence, <code>sequence()</code>,
     * running from this chunk's start index - contextLen,
     * <code>chunk().start()</code>, up to not including start index +
     * contextLen.
     *
     * @param contextLength The number of characters before and after the
     * chunk start to retrieve.
     * @return The characters centered around the start of this chunk.
     * @throws IllegalArgumentException If the contextLength < 1.
     */
    public CharSequence spanStartContext(int contextLength) {
	if (contextLength < 1) {
	    String msg = "Context length must be greater than 0.";
	    throw new IllegalArgumentException(msg);
	}
	int start = mChunk.start() - contextLength;
	if (start < 0) start = 0;
	int end = mChunk.start() + contextLength;
	if (end > mSeq.length()) end = mSeq.length();
	int len = end - start;
	if (len < contextLength*2) {
	    StringBuilder padded = new StringBuilder();
	    for (int i = contextLength*2; i > len; i--) {
		padded.append(" ");
	    }
	    padded.append(mSeq.subSequence(start,end).toString());
	    return padded.subSequence(0,padded.length());
	}
	return mSeq.subSequence(start,end).toString();
    }

    /**
     * Return a span of characters centered around the end of this
     * chunk plus or minus the context length. If there isn't
     * sufficient context, the returned string will be padded so that
     * the chunk end is at position contextLength. Note that this is the
     * subsequence of the character sequence, <code>sequence()</code>,
     * running from this chunk's end index - contextLength,
     * <code>chunk().end()</code>, up to not including end index +
     * contextLength.
     *
     * @param contextLength The number of characters before and after the
     * chunk end to retrieve.
     * @return The characters centered around the end of this chunk.
     * @throws IllegalArgumentException If the contextLength < 1.
     */
    public CharSequence spanEndContext(int contextLength) {
	if (contextLength < 1) {
	    String msg = "Context length must be greater than 0.";
	    throw new IllegalArgumentException(msg);
	}
	int start = mChunk.end() - contextLength;
	if (start < 0) start = 0;
	int end = mChunk.end() + contextLength;
	if (end > mSeq.length()) end = mSeq.length();
	int len = end - start;
	if (len < contextLength*2) {
	    StringBuilder padded = new StringBuilder();
	    padded.append(mSeq.subSequence(start,end).toString());
	    for (int i = contextLength*2; i > len; i--) {
		padded.append(" ");
	    }
	    return padded.subSequence(0,padded.length());
	}
	return mSeq.subSequence(start,end).toString();
    }

    /**
     * Return the underlying character sequence for this chunk.
     *
     * @return The underlying character sequence for this chunk.
     */
    public String charSequence() {
	return mSeq.toString();
    }

    /**
     * Return the chunk underlying this compositie chunk and character
     * sequence.
     */
    public Chunk chunk() {
	return mChunk;
    }

    /**
     * Return the underlying chunk's score.  
     *
     * @return The underlying chunk's score.
     */
    public double score() {
	return chunk().score();
    }

    /**
     * Returns a string-based representation of this chunk and
     * character sequence.  
     *
     * <P><i>Implementation Note:</i> This representation is of the
     * form <code>&quot;start-end/span:type&quot;</code>.
     */
    @Override
    public String toString() {
	return chunk().start() + "-" + chunk().end() + "/"
	    + span() + ":" + chunk().type();
    }

}


