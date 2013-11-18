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

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Iterators;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 */
abstract class AbstractTagChunkCodec implements TagChunkCodec {

    final TokenizerFactory mTokenizerFactory;
    final boolean mEnforceConsistency;
    
    public AbstractTagChunkCodec() {
        this(null,false);
    }

    public AbstractTagChunkCodec(TokenizerFactory tokenizerFactory,
                                 boolean enforceConsistency) {
        mTokenizerFactory = tokenizerFactory;
        mEnforceConsistency = enforceConsistency;
    }

    /**
     * Returns {@code true} if this codec enforces consistency
     * of the chunkings relative to the tokenizer factory.  Consistency
     * requires each chunk to start on the first character of a token
     * and requires each chunk to end on the last character of
     * a token (as usual, ends are one past the last character).
     *
     * @return {@code true} if this codec enforces consistency of
     * chunkings relative to tokenization.
     */
    public boolean enforceConsistency() {
        return mEnforceConsistency;
    }

    /**
     * Return the tokenizer factory for this codec.  The tokenizer
     * factory may be null if this was only constructed as a decoder.
     *
     * @return The underlying tokenizer factory.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns {@code true} if the specified chunking may be
     * consistently encoded as a tagging.  A chunking is encodable if
     * none of the chunks overlap, and if all chunks begin on the
     * first character of a token and end on the character one past
     * the end of the last character in a token.  
     *
     * <p>Subclasses may enforce further conditions as defined in
     * their class documentation.
     *
     * @param chunking Chunking to test.
     * @return {@code true} if the chunking is consistently encodable.
     * @throws UnsupportedOperationException If the tokenizer is null so that
     * this is only a decoder.
     */
    public boolean isEncodable(Chunking chunking) {
        return isEncodable(chunking,null);
    }

    /**
     * Returns {@code true} if the specified tagging may be
     * consistently decoded into a chunking.  A tagging is decodable
     * if its tokens are the tokens produced by the tokenizer for this
     * coded and if the tags form a legal sequence.
     *
     * @param tagging Tagging to test for decodability.
     * @throws UnsupportedOperationException If the tokenizer is null so that
     * this is only a decoder.
     */
    public boolean isDecodable(StringTagging tagging) {
        return isDecodable(tagging,null);
    }

    boolean isEncodable(Chunking chunking, StringBuilder sb) {
        if (mTokenizerFactory == null) {
            String msg = "Tokenizer factory must be non-null to support encodability test.";
            throw new UnsupportedOperationException(msg);
        }
        Set<Chunk> chunkSet = chunking.chunkSet();
        if (chunkSet.size() == 0) return true;
        Chunk[] chunks = chunkSet.toArray(new Chunk[chunkSet.size()]);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
        int lastEnd = chunks[0].end();
        for (int i = 1; i < chunks.length; ++i) {
            if (chunks[i].start() < lastEnd) {
                if (sb != null) {
                    sb.append("Chunks must not overlap."
                              + " chunk=" + chunks[i-1]
                              + " chunk=" + chunks[i]);
                }
                return false;
            }
            lastEnd = chunks[i].end();
        }
        char[] cs = Strings.toCharArray(chunking.charSequence());
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        int chunkPos = 0;
        boolean chunkStarted = false;
        String token;
        while (chunkPos < chunks.length && (token = tokenizer.nextToken()) != null) {
            int tokenStart = tokenizer.lastTokenStartPosition();
            if (tokenStart == chunks[chunkPos].start())
                chunkStarted = true;
            int tokenEnd = tokenizer.lastTokenEndPosition();
            if (tokenEnd == chunks[chunkPos].end()) {
                if (!chunkStarted) {
                    if (sb != null)
                        sb.append("Chunks must start on token boundaries."
                                  + " Illegal chunk=" + chunks[chunkPos]);
                    return false;
                }
                ++chunkPos;
                chunkStarted = false;
            }
        }
        if (chunkPos < chunks.length) {
            if (sb != null)
                sb.append("Chunk beyond last token."
                          + " chunk=" + chunks[chunkPos]);
            return false;
        }
        return true;
    }

    boolean isDecodable(StringTagging tagging, StringBuilder sb) {
        if (mTokenizerFactory == null) {
            String msg = "Tokenizer factory must be non-null to support decodability test.";
            throw new UnsupportedOperationException(msg);
        }
        if (!legalTags(tagging.tags().toArray(Strings.EMPTY_STRING_ARRAY))) {
            sb.append("Illegal tags=" + tagging.tags());
            return false;
        }
        char[] cs = Strings.toCharArray(tagging.characters());
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        for (int n = 0; n < tagging.size(); ++n) {
            String nextToken = tokenizer.nextToken();
            if (nextToken == null) {
                if (sb != null)
                    sb.append("More tags than tokens.");
                return false;
            }
            if (tagging.tokenStart(n) != tokenizer.lastTokenStartPosition()) {
                if (sb != null)
                    sb.append("Tokens must start/end in tagging to match tokenizer."
                              + " Found token " + n +
                              " from tokenizer=" + nextToken
                              + " tokenizer.lastTokenStartPosition()="
                              + tokenizer.lastTokenStartPosition()
                              + " tagging.tokenStart(" + n + ")="
                              + tagging.tokenStart(n));
                return false;
            }
            if (tagging.tokenEnd(n) != tokenizer.lastTokenEndPosition()) {
                if (sb != null)
                    sb.append("Tokens must start/end in tagging to match tokenizer."
                              + " Found token " + n
                              + " from tokenizer=" + nextToken
                              + " tokenizer.lastTokenEndPosition()="
                              + tokenizer.lastTokenEndPosition()
                              + " tagging.tokenEnd(" + n + ")="
                              + tagging.tokenEnd(n));
                return false;
            }
        }
        String excessToken = tokenizer.nextToken();
        if (excessToken != null) {
            if (sb != null)
                sb.append("Extra token from tokenizer beyond tagging."
                          + " token=" + excessToken
                          + " startPosition=" + tokenizer.lastTokenStartPosition()
                          + " endPosition=" + tokenizer.lastTokenEndPosition());
        }
        return true;
    }

    void enforceConsistency(StringTagging tagging) {
        if (!mEnforceConsistency) return;
        StringBuilder sb = new StringBuilder();
        if (isDecodable(tagging,sb)) return;
        throw new IllegalArgumentException(sb.toString());
    }

    void enforceConsistency(Chunking chunking) {
        if (!mEnforceConsistency) return;
        StringBuilder sb = new StringBuilder();
        if (isEncodable(chunking,sb)) return;
        throw new IllegalArgumentException(sb.toString());
    }


}