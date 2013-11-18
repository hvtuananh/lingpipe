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

package com.aliasi.sentences;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>SentenceChunker</code> class uses a
 * <code>SentenceModel</code> to implement sentence detection through
 * the <code>chunk.Chunker</code> interface.  A sentence chunker is
 * constructed from a tokenizer factory and a sentence model.  The
 * tokenizer factory creates tokens that it sends to the sentence
 * model.  The types of the chunks produced are given by the constant
 * {@link #SENTENCE_CHUNK_TYPE}.
 *
 * <h3>Thread Safety</h3>
 *
 * A sentence chunker is thread safe if its tokenizer factory
 * and sentence model are thread safe.  Typical LingPipe sentence
 * models and tokenizer factories are thread safe for reads.
 * 
 * <h3>Serialization</h3>
 *
 * A sentence chunker is serializer if both its tokenizer
 * factory and sentence model are serializable.  The deserialized
 * object will be an instance of {@code SentenceChunker} constructed
 * from the deserialized tokenizer factory and sentence model.
 * 
 * @author  Mitzi Morris
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe2.1
 */
public class SentenceChunker 
    implements Chunker,
               Serializable {

    static final long serialVersionUID = 2296001471469838378L;

    private final TokenizerFactory mTokenizerFactory;
    private final SentenceModel mSentenceModel;

    /**
     * Construct a sentence chunker from the specified tokenizer
     * factory and sentence model.
     *
     * @param tf Tokenizer factory for chunker.
     * @param sm Sentence model for chunker.
     */
    public SentenceChunker(TokenizerFactory tf, SentenceModel sm) {
	mTokenizerFactory = tf;
	mSentenceModel = sm;
    }

    /**
     * Returns the tokenizer factory for this chunker.
     *
     * @return The tokenizer factory for this chunker.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns the sentence model for this chunker.
     *
     * @return The sentence model for this chunker.
     */
    public SentenceModel sentenceModel() {
        return mSentenceModel;
    }

    /**
     * Return the chunking derived from the underlying sentence model
     * over the tokenization of the specified character slice.
     * Iterating over the returned set is guaranteed to return the
     * sentence chunks in their original textual order.  
     *
     * <P><i>Warning:</i> As described in the class documentation
     * above, a tokenizer factory that produces tokenizers that do not
     * reproduce the original sequence may cause the underlying
     * character slice for the chunks to differ from the slice
     * provided as an argument.
     *
     * @param cSeq Character sequence underlying the slice.
     * @return The sentence chunking of the specified character
     * sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
	char[] cs = Strings.toCharArray(cSeq);
	return chunk(cs,0,cs.length);
    }
    
    /**
     * Return the chunking derived from the underlying sentence model
     * over the tokenization of the specified character slice.  See
     * {@link #chunk(CharSequence)} for more information.
     *
     * @param cs Underlying character sequence.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     * @return The sentence chunking of the specified character slice.
     */
    public Chunking chunk(char[] cs, int start, int end) {
        Tokenization toks
            = new Tokenization(cs,start,end-start,mTokenizerFactory);
        String[] tokens = toks.tokens();
        String[] whitespaces = toks.whitespaces();

	ChunkingImpl chunking = new ChunkingImpl(cs,start,end);

	if (tokens.length == 0) return chunking;
        
	int[] sentenceBoundaries 
            = mSentenceModel.boundaryIndices(tokens,whitespaces);
	if (sentenceBoundaries.length < 1) return chunking;

        int startToken = 0;
	for (int i = 0; i < sentenceBoundaries.length; ++i) {
	    int endToken = sentenceBoundaries[i];
            Chunk chunk 
		= ChunkFactory.createChunk(toks.tokenStart(startToken),
                                           toks.tokenEnd(endToken),
					   SENTENCE_CHUNK_TYPE);
	    chunking.add(chunk);
            startToken = endToken+1;
	}
	return chunking;
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    /**
     * The type assigned to sentence chunks, namely
     * <code>&quot;S&quot;</code>.
     */
    public static final String SENTENCE_CHUNK_TYPE = "S";


    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = -8566130480755448404L;

        final SentenceChunker mChunker;

        public Serializer() {
            this(null);
        }
        public Serializer(SentenceChunker chunker) {
            mChunker = chunker;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mChunker.mTokenizerFactory);
            out.writeObject(mChunker.mSentenceModel);
        }
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {

            @SuppressWarnings("unchecked")
            TokenizerFactory tokenizerFactory
                = (TokenizerFactory) in.readObject();
            @SuppressWarnings("unchecked")
            SentenceModel sentenceModel
                = (SentenceModel) in.readObject();
            return new SentenceChunker(tokenizerFactory,
                                       sentenceModel);
        }
    }

}


