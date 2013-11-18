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
 * The {@code IoTagChunkCodec} implements a chunk to tag
 * coder/decoder based on the IO encoding scheme and a
 * specified tokenizer factory.
 *
 * <h3>Degenerate Encoding</h3>
 *
 * <p>Although this is a compact encoding in number of tags, it is
 * degenerate in that it does not allow adjacent chunks of the same
 * type.  The {@link #isEncodable(Chunking)} method reflects this
 * behavior.  
 *
 * <p>If consistency is not being enforced, the two entities will
 * simply be run together as a single entity.
 *
 * <h3>IO Encoding</h3>
 *
 * The basis of the IO encoding of a chunking is to break the
 * chunking down into tokens.  All tokens that are part of a chunk
 * of type <code><i>X</i></code> are tagged as <code><i>X</i></code>
 * and all tokens that are not part of an entity are tagged as <code>O</code>.
 *
 * <p>For instance, consider the following input string:
 *
 * <blockquote><pre>
 * John Jones Mary and Mr. J. J. Jones ran to Washington.
 * 012345678901234567890123456789012345678901234567890123
 * 0         1         2         3         4         5</pre></blockquote>
 *
 * and chunking consisting of the string and chunks:
 *
 * <blockquote><pre>
 * (0,10):PER, (11,15):PER, (24,35):PER, (43,53):LOC</pre></blockquote>
 *
 * Recall that indexing is of the first character and one past the
 * last character.  Note that the two person names "John Jones" and
 * "Mary", are separate chunks of type PER (for persons), and
 * the location chunk for "Washington" ends before the period.
 *
 * <p>If we have a tokenizer that breaks on whitespace
 * and punctuation, we have tokens starting at + and
 * continuing through the - signs.
 *
 * <blockquote><pre>
 * John Jones Mary and Mr. J. J. Jones ran to Washington.
 * +--- +---- +--- +-- +-+ ++ ++ +---- +-- +- +---------+</pre></blockquote>
 *
 * In particular, note that the the four periods form their own
 * tokens, even though they are adjacent to characters in other
 * tokens.  Writing the tokens out in a column, we show the tags used
 * by the BIO encoding to the right:
 *
 * <blockquote>
 * <table border="1" cellpadding="5">
 * <tr><th>Token</th><th>Tag</th></tr>
 * <tr><td>John</td><td><code>PER</code></td></tr>
 * <tr><td>Jones</td><td><code>PER</code></td></tr>
 * <tr><td>Mary</td><td><code>PER</code></td></tr>
 * <tr><td>and</td><td><code>O</code></td></tr>
 * <tr><td>Mr</td><td><code>O</code></td></tr>
 * <tr><td>.</td><td><code>O</code></td></tr>
 * <tr><td>J</td><td><code>PER</code></td></tr>
 * <tr><td>.</td><td><code>PER</code></td></tr>
 * <tr><td>J</td><td><code>PER</code></td></tr>
 * <tr><td>.</td><td><code>PER</code></td></tr>
 * <tr><td>Jones</td><td><code>PER</code></td></tr>
 * <tr><td>ran</td><td><code>O</code></td></tr>
 * <tr><td>to</td><td><code>O</code></td></tr>
 * <tr><td>Washington</td><td><code>LOC</code></td></tr>
 * <tr><td>.</td><td><code>O</code></td></tr>
 * </table>
 * </blockquote>
 *
 * Note that chunks may be any number of tokens long.
 *
 * <h3>Set of Tags</h3>
 *
 * There is a single tag <code>O</code>, as well as tags
 * <code><i>X</i></code> for each chunk type.
 *
 * <h3>Legal Tag Sequences</h3>
 *
 * One nice property of the IO encoding is that all sequences of
 * tags are legal.  
 *
 * <h3>Enforcing Tokenization Consistency</h3>
 *
 * <p>If the consistency flag is set on the constructor, attempts
 * to encode chunkings or decode taggings that are inconsistent with
 * the tokenizer will throw illegal argument exceptions.
 *
 * <p>In order for a tokenizer to be consistent with a chunking,
 * the tokenization of the characterer sequence for the chunking
 * must be such that every chunk start and end occurs at
 * a token start or end.  The same rule applies for tagging, in
 * that the chunking produced has to obey the same rules.
 *
 * <p>For example, if a regular-expression based tokenizer that breaks
 * on whitespace were used for the above example, the character
 * sequence "Washington." is a token, including the final period.
 * This conflicts with the location-type entity, which ends with the
 * last character before the period.
 *
 * <h3>Serialization</h3>
 *
 * Instances of this class are serializable if their underlying
 * tokenizer factories are serializable.  Reading them back in
 * produces an instance of the same class with the same behavior.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 */
public class IoTagChunkCodec
    extends AbstractTagChunkCodec
    implements Serializable {

    static final long serialVersionUID = 3871326314223465927L;

    final BioTagChunkCodec mBioCodec;


    /**
     * Construct an IO-encoding based tag-chunk coder with a null
     * tokenizer factory that does not enforce cons.  A codec
     * constructed with this method only supports the conversion of a
     * string tagging to a chunking, not vice-versa.
     */
    public IoTagChunkCodec() {
        this(null,false);
    }

    /**
     * Construct an IO-encoding based tag-chunk coder/decoder based on
     * the specified tokenizer factory, enforcing consistency of
     * chunkings and taggings if the specified flag is set.
     *
     * @param tokenizerFactory Tokenizer factory for generating tokens.
     * @param enforceConsistency Set to {@code true} to ensure all
     * coded chunkings and decoded taggings are consistent for
     * round trips.
     */
    public IoTagChunkCodec(TokenizerFactory tokenizerFactory,
                            boolean enforceConsistency) {
        super(tokenizerFactory,enforceConsistency);
        mBioCodec = new BioTagChunkCodec(tokenizerFactory,enforceConsistency);
    }

    @Override
    boolean isEncodable(Chunking chunking, StringBuilder sb) {
        if (!mBioCodec.isEncodable(chunking,sb))
            return false;
        Tagging<String> tagging = mBioCodec.toTagging(chunking);
        String lastTag = BioTagChunkCodec.OUT_TAG;
        for (String tag : tagging.tags()) {
            if (startSameType(lastTag,tag)) {
                if (sb != null)
                    sb.append("Two consectuive chunks of type " 
                              + tag.substring(BioTagChunkCodec.PREFIX_LENGTH));
                return false;
            }
            lastTag = tag;
        }
        return true;
    }

    boolean startSameType(String lastTag, String tag) {
        return tag.startsWith(BioTagChunkCodec.BEGIN_TAG_PREFIX)
            && !BioTagChunkCodec.OUT_TAG.equals(lastTag)
            && lastTag.substring(BioTagChunkCodec.PREFIX_LENGTH).equals(tag.substring(BioTagChunkCodec.PREFIX_LENGTH));
    }

    public Set<String> tagSet(Set<String> chunkTypes) {
        Set<String> tagSet = new HashSet<String>();
        tagSet.addAll(chunkTypes);
        tagSet.add(BioTagChunkCodec.OUT_TAG);
        return tagSet;
    }

    public boolean legalTagSubSequence(String... tags) {
        return true;
    }

    public boolean legalTags(String... tags) {
        return true;
    }

    public Chunking toChunking(StringTagging tagging) {
        enforceConsistency(tagging);
        ChunkingImpl chunking = new ChunkingImpl(tagging.characters());
        for (int n = 0; n < tagging.size(); ++n) {
            String tag = tagging.tag(n);
            if (BioTagChunkCodec.OUT_TAG.equals(tag)) continue;
            String type = tag;
            int start = tagging.tokenStart(n);
            while ((n + 1) < tagging.size() && tagging.tag(n+1).equals(type))
                ++n;
            int end = tagging.tokenEnd(n);
            Chunk chunk = ChunkFactory.createChunk(start,end,type);
            chunking.add(chunk);
        }
        return chunking;
    }

    /**
     * @inheritDoc
     *
     * @throws UnsupportedOperationException If the tokenizer factory is null.
     */
    public StringTagging toStringTagging(Chunking chunking) {
        if (mTokenizerFactory == null) {
            String msg = "Tokenizer factory must be non-null to convert chunking to tagging.";
            throw new UnsupportedOperationException(msg);
        }
        enforceConsistency(chunking);
        List<String> tokenList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        List<Integer> tokenStartList = new ArrayList<Integer>();
        List<Integer> tokenEndList = new ArrayList<Integer>();
        mBioCodec.toTagging(chunking,tokenList,tagList,
                            tokenStartList,tokenEndList);
        transformTags(tagList);
        StringTagging tagging = new StringTagging(tokenList,
                                                  tagList,
                                                  chunking.charSequence(),
                                                  tokenStartList,
                                                  tokenEndList);
        return tagging;
    }

    /**
     * @inheritDoc
     *
     * @throws UnsupportedOperationException If the tokenizer factory is null.
     */
    public Tagging<String> toTagging(Chunking chunking) {
        if (mTokenizerFactory == null) {
            String msg = "Tokenizer factory must be non-null to convert chunking to tagging.";
            throw new UnsupportedOperationException(msg);
        }
        enforceConsistency(chunking);
        List<String> tokens = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        mBioCodec.toTagging(chunking,tokens,tags,null,null);
        transformTags(tags);
        return new Tagging<String>(tokens,tags);
    }

    public Iterator<Chunk> nBestChunks(TagLattice<String> lattice,
                                       int[] tokenStarts,
                                       int[] tokenEnds,
                                       int maxResults) {
        throw new UnsupportedOperationException("no n-best chunks yet for IO encodings");
    }

    /**
     * Return a string-based representation of this codec.
     *
     * @return A string-based representation of this codec.
     */
    public String toString() {
        return "IoTagChunkCodec";
    }

    Object writeReplace() {
        return new Serializer(this);
    }


    static void transformTags(List<String> tagList) {
        for (int i = 0; i < tagList.size(); ++i) {
            String tag = tagList.get(i);
            if (BioTagChunkCodec.OUT_TAG.equals(tag))
                continue;
            String transformedTag = tag.substring(BioTagChunkCodec.PREFIX_LENGTH);
            tagList.set(i,transformedTag);
        }
    }


    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = -3559983129637286794L;
        private final IoTagChunkCodec mCodec;
        public Serializer() {
            this(null);
        }
        public Serializer(IoTagChunkCodec codec) {
            mCodec = codec;
        }
        public void writeExternal(ObjectOutput out)
            throws IOException {

            out.writeBoolean(mCodec.mEnforceConsistency);
            out.writeObject(mCodec.mTokenizerFactory != null
                            ? mCodec.mTokenizerFactory
                            : Boolean.FALSE); // dummy object
        }
        public Object read(ObjectInput in)
            throws IOException, ClassNotFoundException {

            boolean enforceConsistency = in.readBoolean();
            Object tfObj = in.readObject();
            TokenizerFactory tf
                = (tfObj instanceof TokenizerFactory)
                ? (TokenizerFactory) tfObj
                : null;
            return new IoTagChunkCodec(tf,enforceConsistency);
        }
    }

}