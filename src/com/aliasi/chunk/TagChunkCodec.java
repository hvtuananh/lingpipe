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

import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;

import java.util.Iterator;
import java.util.Set;

/**
 * A {@code TagChunkCodec} provides a means of coding chunkings as
 * taggings and decoding (string) taggings back to chunkings.  
 *
 * <p>Each codec contains a method {@link #tagSet(Set)} to return the
 * complete set of tags used in the coding given a set of chunk types.
 * Codecs also use a variable argument method {@link
 * #legalTags(String[])} to determine if a sequence of tags is legal.
 * For a known set of chunk types, the followers of a tag can be
 * constructed by iterating over the set of tags returned by {@code
 * tagSet()} and check if they're legal using {@code legalTags()}.
 *
 * <p>To validate whether a chunking may be successfully encoded as a
 * tagging and then decoded to the original chunking, use the method
 * {@link #isEncodable(Chunking)}.  To validate whether a string
 * tagging may be successfully decoded to a chunking and then
 * reencoded to the original string tagging, use {@link
 * #isDecodable(StringTagging)}.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 */
public interface TagChunkCodec {

    /**
     * Return the tagging that partially encodes the specified
     * chunking.  This method does not return the underlying character
     * sequence or token positions -- that functionality is available
     * from the method {@link #toStringTagging(Chunking)}.
     *
     * <p>This method will typically be more efficient than {@code
     * toStringTagging()}, but implementations may just return the
     * same value, because {@code StringTagging} extends {@code
     * Tagging<String>}.
     *
     * <p>This method may be implemented by delegating to
     * call to {@link #toStringTagging(Chunking)}, but a direct
     * implementation is often more efficient.  
     *
     * @param chunking Chunking to encode.
     * @return Tagging that encodes the chunking.
     */
    public Tagging<String> toTagging(Chunking chunking);

    /**
     * Return the string tagging that fully encodes the specified
     * chunking.
     *
     * @param chunking Chunking to encode.
     * @return Tagging that encodes the chunking.
     */
    public StringTagging toStringTagging(Chunking chunking);
    
    /**
     * Return the result of decoding the specified tagging into
     * a chunking.
     *
     * @param tagging Tagging to decode.
     * @return Chunking resulting from tagging.
     * @throws IllegalArgumentException If the tag sequence is
     * illegal.
     */
    public Chunking toChunking(StringTagging tagging);

    /**
     * Returns the complete set of tags used by this codec
     * for the specified set of chunk types.  
     *
     * <p>Modifying the returned set will not affect the
     * codec.
     *
     * @param chunkTypes Set of types for chunks.
     * @return Set of all tags used to encode chunks of
     * types in the specified set.
     */
    public Set<String> tagSet(Set<String> chunkTypes);

    /**
     * Returns {@code true} if the specified sequence of tags is a
     * complete legal tag sequence.  The companion method {@link
     * #legalTagSubSequence(String[])} tests if a substring of tags is
     * legal.
     *
     * @param tags Variable length array of tags.
     * @return {@code true} if the specified sequence of tags is
     * a complete legal tag sequence.
     */
    public boolean legalTags(String... tags);


    /** 
     * Returns {@code true} if the specified sequence of tags
     * is a legal subsequence of tags.  See the companion
     * method {@link #legalTags(String[])} to test if a complete
     * sequence is legal.
     * 
     * <p>A sequence of tags is a legal subsequence if a legal
     * sequence may be created by adding more tags to the front and/or
     * end of the specified sequence.
     *
     * <p>Providing an empty sequence of tags always returns {@code
     * true}.  The result for a single input tag determines if the tag
     * itself is legal.  For longer sequences, the tags must all be
     * legal and their order must be legal.
     *
     * @param tags Sequence of tags to test.
     * @return {@code true} if the sequence of tags is legal as a
     * subsequence of some larger sequence.
     */
    public boolean legalTagSubSequence(String... tags);

    

    /**
     * Returns {@code true} if the specified chunking may be encoded
     * as a tagging then decoded back to the original chunking accurately.
     *
     * @param chunking Chunking to test.
     * @return {@code true} if encoding then decoding produces the
     * specified chunking.
     */
    public boolean isEncodable(Chunking chunking);

    /**
     * Returns {@code true} if the specified tagging may be decoded
     * as a chunking then encoded back to the original tagging accurately.
     *
     * @param tagging Tagging to test.
     * @return {@code true} if decoding then encoding produces the
     * specified tagging.
     */
    public  boolean isDecodable(StringTagging tagging);

    
    /**
     * Returns an iterator over chunks extracted in order of highest
     * probability up to the specified maximum number of results.
     *
     * @param lattice Lattice from which chunks are extracted.
     * @param maxResults Maximum number of chunks to return.
     * @return Iterator over the chunks in the lattice in order
     * from highest to lowest probability.
     * 
     */
    public Iterator<Chunk> nBestChunks(TagLattice<String> lattice, 
                                       int[] tokenStarts, int[] tokenEnds,
                                       int maxResults);
    
}