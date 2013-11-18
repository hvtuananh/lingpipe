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

package com.aliasi.tokenizer;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * A {@code TokenChunker} provides an implementationg of the {@code
 * Chunker} interface based on an underlying tokenizer factory.  
 * 
 * <p>The chunkings produced will have one chunk per token produced
 * by the underlying tokenizer factory, with start and end positions
 * as determined by the tokenizer's start and end position methods.
 * The type of the chunk will be the actual string yield of the token,
 * which in the case of modifying tokenizers like stemmers, will not
 * necessarily be the same as the underlying text span.
 *
 * <h3>Serialization</h3>
 *
 * The token chunker will be serializable if the underlying
 * tokenizer factory is serializable.  If it is not, serialization
 * will throw an {@code java.io.NotSerializableException}.  The
 * object read back in will be an instance of {@code TokenChunker}
 * constructed with the reconstituted tokenizer factory.
 *
 * @author Bob Carpenter
 * @version 3.8.1
 * @since Lingpipe3.9
 */
public class TokenChunker 
    implements Chunker, Serializable {

    static final long serialVersionUID = -6559339721653291504L;

    private final TokenizerFactory mTokenizerFactory;

    /**
     * Construct a chunker from the specified tokenizer
     * factory.
     *
     * @param tokenizerFactory Tokenizer factory for this chunker.
     */
    public TokenChunker(TokenizerFactory tokenizerFactory) {
        mTokenizerFactory = tokenizerFactory;
    }

    /**
     * Return the tokenizer factory for this token chunker.
     *
     * @return The tokenizer factory for this chunker.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Return the chunking produced by tokenizing the specified
     * character sequence.
     *
     * @param cSeq Character sequence to chunk.
     * @return The chunking corresponding to tokens produced by
     * the tokenizer factory.
     */
    public Chunking chunk(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return chunk(cs,0,cs.length);
    }

    /**
     * Return the chunking produced by tokenizing the specified
     * character array slice.
     *
     * @param cs Underlying characters for slice.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     * @return The chunking corresponding to tokens produced by
     * the tokenizer factory.
     */
    public Chunking chunk(char[] cs, int start, int end) {
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end-start);
        ChunkingImpl chunking = new ChunkingImpl(cs,start,end);
        String token;
        while ((token = tokenizer.nextToken()) != null) {
            int chunkStart = tokenizer.lastTokenStartPosition();
            int chunkEnd = tokenizer.lastTokenEndPosition();
            Chunk chunk = ChunkFactory.createChunk(chunkStart,chunkEnd,token);
            chunking.add(chunk);
        }
        return chunking;
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 5541846439684999440L;
        private final TokenChunker mChunker;
        public Serializer(TokenChunker chunker) {
            mChunker = chunker;
        }
        public Serializer() {
            this(null);
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mChunker.tokenizerFactory());
        }
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {

            TokenizerFactory tokenizerFactory 
                = (TokenizerFactory) in.readObject();
            return new TokenChunker(tokenizerFactory);
        }
    }

}