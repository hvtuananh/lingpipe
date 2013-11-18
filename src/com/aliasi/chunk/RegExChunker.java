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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>RegExChunker</code> finds chunks that matches regular
 * expressions.  Specifically, a matcher is created and its {@link
 * Matcher#find()} method is used to iterate over matching text
 * segments and convert them to chunks.
 *
 * <p>The behavior of the find method is largely determined by the
 * specific instance of {@link Pattern}) on which the chunker is
 * based.  For more information, see Sun's <a
 * href="http://java.sun.com/docs/books/tutorial/extra/regex/quant.html">RegEx
 * Tutorial</a>.
 *
 * <p>All found chunks will receive a type and score that is specified
 * at construction time.
 *
 * <p><b>Warning:</b> Java uses the same regular expression matching
 * as <a href="http://www.perl.com/">Perl</a>.  Perl uses a greedy
 * strategy for quantifiers, taking something like <code>.*</code> to
 * match as many characters as possible.  In constrast, disjunction
 * uses a first-match strategy.  For example, the regular expression
 * <code>ab|abc</code> will not produce the same chunker as
 * <code>abc|ab</code>; for input <code>abcde</code>, the former will
 * return <code>ab</code> as a chunk, whereas the latter will return
 * <code>abc</code>.  This first-best matching through disjunctions
 * takes precedence over any quantifiers applied to the strings.  
 *
 * <h3>Compilation and Serialization</h3>
 *
 * <p>For convenience, this class implements both the <code>util.Compilable</code>
 * and <code>java.io.Serializable</code> interfaces.  These both store the
 * same thing, namely the string underlying the regex pattern, the chunk type
 * and the score.  The reconstituted object will also be an instance of this
 * class.
 * 
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.3
 */
public class RegExChunker implements Chunker, Compilable, Serializable {

    static final long serialVersionUID = -8997320544817071938L;

    private final Pattern mPattern;
    private final String mChunkType;
    private final double mChunkScore;

    /**
     * Construct a chunker based on the specified regular expression,
     * producing the specified chunk type and score.  The regular
     * expression is compiled using the default method {@link
     * Pattern#compile(String)}.
     *
     * @param regex Regular expression for chunks.
     * @param chunkType Type for all found chunks.
     * @param chunkScore Score for all found chunks.
     */
    public RegExChunker(String regex, String chunkType, double chunkScore) {
	this(Pattern.compile(regex),chunkType,chunkScore);
    }

    /**
     * Construct a chunker based on the specified regular expression
     * pattern, producing the specified chunk type and score.
     *
     * @param pattern Regular expression patternfor chunks.
     * @param chunkType Type for all found chunks.
     * @param chunkScore Score for all found chunks.
     */
    public RegExChunker(Pattern pattern, String chunkType, double chunkScore) {
	mPattern = pattern;
	mChunkType = chunkType;
	mChunkScore = chunkScore;
    }

    /**
     * Return the chunking of the specified character sequence.  Chunkings
     * are defined by the behavior of {@link Matcher#find()} as applied
     * to the regular expression pattern underlying this chunker.
     *
     * @param cSeq Character sequence to chunk.
     * @return A chunking of the character sequence.
     */
    public Chunking chunk(CharSequence cSeq) {
	ChunkingImpl result = new ChunkingImpl(cSeq);
	Matcher matcher = mPattern.matcher(cSeq);
	while (matcher.find()) {
	    int start = matcher.start();
	    int end = matcher.end();
	    Chunk chunk 
		= ChunkFactory.createChunk(start,end,mChunkType,mChunkScore);
	    result.add(chunk);
	}
	return result;
    }


    /**
     * Compiles this regular-expression chunker to the specified
     * object output.  When read back in, the object will be an
     * instance of this class.
     *
     * @param out Object output to which this chunker is compiled.
     * @throws IOException If there is an underlying I/O error during
     * the write.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer(this));
    }

    private Object writeReplace() {
        return new Externalizer(this);
    }

    /**
     * Return the chunking of the specified character slice.
     *
     * @param cs Underlying character sequence.
     * @param start Index of first character in slice.
     * @param end Index of one past the last character in the slice.
     * @return The chunking over the specified character slice.
     */
    public Chunking chunk(char[] cs, int start, int end) {
	return chunk(new String(cs,start,end-start));
    }

    static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = -3419191413174871277L;
        private final RegExChunker mChunker;
        public Externalizer() {
            this(null);
        }
        public Externalizer(RegExChunker chunker) {
            mChunker = chunker;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(mChunker.mPattern.pattern());
            out.writeUTF(mChunker.mChunkType);
            out.writeDouble(mChunker.mChunkScore);
        }
        @Override
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            String pattern = in.readUTF();
            String chunkType = in.readUTF();
            double score = in.readDouble();
            return new RegExChunker(pattern,chunkType,score);
        }
    }

}
