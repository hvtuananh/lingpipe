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

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>TokenShapeChunker</code> uses a named-entity
 * <code>TokenShapeDecoder</code> and tokenizer factory to implement
 * entity detection through the <code>chunk.Chunker</code> interface.
 * A named-entity chunker is constructed from a tokenizer factory and
 * decoder.  The tokenizer factory creates the tokens that are sent to
 * the decoder.  The chunks have types derived from the named-entity
 * types found.
 *
 * <P>The tokens and whitespaces returned by the tokenizer are
 * concatenated to form the underlying text slice of the chunks
 * returned by the chunker.  Thus a tokenizer like the stop list
 * tokenizer or Porter stemmer tokenizer will create a character slice
 * that does not match the input.  A whitespace-normalizing tokenizer
 * filter can be used, for example, to produce normalized text for
 * the basis of the chunks.
 * 
 * @author  Mitzi Morris
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.1
 */
public class TokenShapeChunker implements Chunker {

    private final TokenizerFactory mTokenizerFactory;
    private final TokenShapeDecoder mDecoder;

    /**
     * Construct a named-entity chunker from the specified tokenizer
     * factory and decoder.
     *
     * @param tf Tokenizer factory for tokenization.
     * @param decoder Decoder for named-entity detection.
     */
    TokenShapeChunker(TokenizerFactory tf, TokenShapeDecoder decoder) {
	mTokenizerFactory = tf;
	mDecoder = decoder;
    }

    /**
     * Return the set of named-entity chunks derived from the
     * uderlying decoder over the tokenization of the specified
     * character sequence.  
     *
     * <P>For more information on return results, see {@link
     * #chunk(char[],int,int)}.
     *
     * @param cSeq Character sequence to chunk.
     * @return The named-entity chunking of the specified character
     * sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
	char[] cs = Strings.toCharArray(cSeq);
	return chunk(cs,0,cs.length);
    }

    /**
     * Return the set of named-entity chunks derived from the
     * underlying decoder over the tokenization of the specified
     * character slice.  Iterating over the returned set is
     * guaranteed to return the sentence chunks in their original
     * textual order.  As noted in the class documentation, a
     * tokenizer factory may cause the underlying character slice for
     * the chunks to differ from the slice provided as an argument.
     *
     * @param cs Characters underlying slice.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     */
    public Chunking chunk(char[] cs, int start, int end) {
	List<String> tokenList = new ArrayList<String>();
	List<String> whiteList = new ArrayList<String>();

	Tokenizer tokenizer 
	    = mTokenizerFactory.tokenizer(cs,start,end-start);
	tokenizer.tokenize(tokenList,whiteList);

	ChunkingImpl chunking = new ChunkingImpl(cs,start,end);

	if (tokenList.size() == 0) return chunking;

	String[] tokens = tokenList.toArray(Strings.EMPTY_STRING_ARRAY);
	String[] whites = whiteList.toArray(Strings.EMPTY_STRING_ARRAY);

	int[] tokenStarts = new int[tokens.length];
	int[] tokenEnds = new int[tokens.length];
    
	int pos = whites[0].length();
	for (int i = 0; i < tokens.length; ++i) {
	    tokenStarts[i] = pos;
	    pos += tokens[i].length();
	    tokenEnds[i] = pos;
	    pos += whites[i+1].length();
	}

        String[] tags = mDecoder.decodeTags(tokens);
	if (tags.length < 1) return chunking;

	int neStartIdx = -1;
	int neEndIdx = -1;
	String neTag = Tags.OUT_TAG;

	for (int i = 0; i < tags.length; ++i) {
	    if (!tags[i].equals(neTag)) {
		if (!Tags.isOutTag(neTag)) {
		    Chunk chunk
			= ChunkFactory.createChunk(neStartIdx,
						   neEndIdx,
						   Tags.baseTag(neTag));
		    chunking.add(chunk);
		}
		neTag = Tags.toInnerTag(tags[i]);
		neStartIdx = tokenStarts[i];
	    }
	    neEndIdx = tokenEnds[i];
	}
	// check final tag
	if (!Tags.isOutTag(neTag)) {
	    Chunk chunk
		= ChunkFactory.createChunk(neStartIdx,neEndIdx,
					   Tags.baseTag(neTag));
	    chunking.add(chunk);
	}
	return chunking;
    }

    /**
     * Sets the log (base 2) beam width for the decoder.  The beam
     * is synchronous by token, with any hypothesis whose log (base 2)
     * probability is more than the beam width's worse than the best
     * hypothesis is removed from further consideration.
     *
     * @param beamWidth Width of beam.
     * @throws IllegalArgumentException If the beam width is not
     * positive.
     */
    public void setLog2Beam(double beamWidth) {
	if (beamWidth <= 0.0 || Double.isNaN(beamWidth)) {
	    String msg = "Require beam width to be positive."
		+ " Found beamWidth=" + beamWidth;
	    throw new IllegalArgumentException(msg);
	}
	mDecoder.setLog2Beam(beamWidth);
    }
}


