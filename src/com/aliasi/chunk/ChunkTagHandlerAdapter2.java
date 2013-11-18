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

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import java.util.Arrays;

/**
 * CUT AND PASTE from corpus.
 * 
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe3.9.1
 */
class ChunkTagHandlerAdapter2 {

    private ObjectHandler<Chunking> mChunkHandler;

    /**
     * Construct a tag handler with no specified chunk handler.  The
     * handler may be set after construction using {@link
     * #setChunkHandler(ObjectHandler)}, and must be called at least
     * once before handling input taggings if this constructor is
     * used.
     */
    public ChunkTagHandlerAdapter2() {
        /* do nothing */
    }
    
    /**
     * Construct a tag handler adapter with the specified chunk
     * handler.  The handler may be set after construction using
     * {@link #setChunkHandler(ObjectHandler)}.
     *
     * @param handler Chunk handler.
     */
    public ChunkTagHandlerAdapter2(ObjectHandler<Chunking> handler) {
        mChunkHandler = handler;
    }

    /**
     * Sets the contained chunk handler to the specified value.  Calls
     * to handle taggings with {@link
     * #handle(String[],String[],String[])} result in the
     * correspdoning chunking being created and passed to the
     * specified chunk handler.
     *
     * @param handler Chunk handler.
     */
    public void setChunkHandler(ObjectHandler<Chunking> handler) {
        mChunkHandler = handler;
    }

    /**
     * Handle the specified arrays of tokens, whitespaces and tags.  This
     * adpater first converts the three parallel arrays to a chunking
     * and then passes the chunking to the contained chunk handler.  If
     * the arrays are not validly BIO-coded, an exception is raised.
     *
     * <P>The token and tag arrays must be the same length.  The
     * whitespaces array may be <code>null</code>, in which case each
     * space is treated as a single whitespace character
     * (<code>'&nbsp;'</code>).  If the whitespaces array is not null,
     * it must be one element longer than the tokens array.
     *
     * <P>If the chunk handler has not been set in the construct or 
     * by {@link #setChunkHandler(ObjectHandler)}, this call will raise
     * a null pointer exception.
     *
     * @param tokens Array of tokens.
     * @param whitespaces Array of whitespaces.
     * @param tags Array of tags.
     * @throws IllegalArgumentException If the tokens, whitespaces and
     * tags are not aligned.
     */
    public void handle(String[] tokens, String[] whitespaces, String[] tags) {
        if (tokens.length != tags.length) {
            String msg = "Tags and tokens must be same length."
                + " Found tokens.length=" + tokens.length
                + " tags.length=" + tags.length;
            throw new IllegalArgumentException(msg);
        }
        if ((whitespaces != null) 
            && (whitespaces.length != 1 + tokens.length) ) {
            String msg = "Whitespaces must be one longer than tokens."
                + " Found tokens.length=" + tokens.length
                + " whitespaces.length=" + whitespaces.length;
            throw new IllegalArgumentException(msg);
        }
        Chunking chunking = toChunkingBIO(tokens,whitespaces,tags);
        mChunkHandler.handle(chunking);
    }

    /**
     * The tag assigned to tokens that are not in a chunk, namely
     * <code>&quot;O&quot;</code> (the letter <code>O</code>).
     */
    public static String OUT_TAG = "O";

    /**
     * The prefix to which a tag is appended to produce a begin tag,
     * namely <code>&quot;B-&quot;</code>.
     */
    public static String BEGIN_TAG_PREFIX = "B-";

    /**
     * The prefix to which a tag is appended to produce a continuation,
     * or &quot;in&quot; tag, namely <code>&quot;I-&quot;</code>.
     */
    public static String IN_TAG_PREFIX = "I-";

    /**
     * Returns the base tag for the specified begin or continuation
     * tag.  If the tag is a begin tag or a continuation tag, its
     * suffix, representing its type, is returned.
     * In all other cases, an
     * exception is raised.
     *
     * @param tag Tag to convert to base form.
     * @return Base form of tag.
     */
    public static String toBaseTag(String tag) {
        if (isBeginTag(tag) || isInTag(tag)) return tag.substring(2);
        String msg = "Tag is neither begin not continuation tag."
            + " Tag=" + tag;
        throw new IllegalArgumentException(msg);
    }

    /**
     * Returns <code>true</code> if the specified tag is the first
     * token in a chunk.  The first token is labeled with a begin tag,
     * consisting of the begin-tag prefix {@link #BEGIN_TAG_PREFIX}
     * appended to a type.
     *
     * @param tag Tag to test.
     * @return <code>true</code> if the tag is for the first token in a
     * chunk.
     */
    public static boolean isBeginTag(String tag) {
        return tag.startsWith(BEGIN_TAG_PREFIX);
    }

    /**
     * Returns <code>true</code> if the specified tag is for the first
     * token in a chunk.  The first token is labeled with a begin tag,
     * consisting of the begin-tag prefix {@link #BEGIN_TAG_PREFIX}
     * appended to a type.
     *
     * @param tag Tag to test.
     * @return <code>true</code> if the tag is for the first token in a
     * chunk.
     */
    public static boolean isOutTag(String tag) {
        return tag.equals(OUT_TAG);
    }

    /**
     * Returns <code>true</code> if the specified tag is for the
     * continuation of a chunk.  A continuation token is labeled with
     * a continuation or &quot;in&quot; tag, consisting of the
     * in-tag prefix {@link #IN_TAG_PREFIX} appended to a type.
     *
     * @param tag Tag to test.
     * @return <code>true</code> if the tag is for the continuation of
     * a chunk.
     */
    public static boolean isInTag(String tag) {
        return tag.startsWith(IN_TAG_PREFIX);
    }

    /**
     * Returns the continuation tag for the specified type.
     * The return value is the {@link #IN_TAG_PREFIX} constant
     * prepended to the specified type.
     *
     * @param type Type to convert to continuation tag.
     * @return Continuation tag for specified type.
     */
    public static String toInTag(String type) {
        return IN_TAG_PREFIX + type;
    }

    /**
     * Returns the begin tag for the specified type.  The return
     * value is the {@link #BEGIN_TAG_PREFIX} constant
     * prepended to the specified type.
     *
     * @param type Type to convert to begin tag.
     * @return The begin tag for the specified type.
     */
    public static String toBeginTag(String type) {
        return BEGIN_TAG_PREFIX + type;
    }

    /**
     * Converts the BIO-coded tokens, whitespaces and tags into a
     * chunking.  This algorithm is not actually sensitive to other
     * categories than the BI marked ones.
     *
     * @param tokens Tokens for tagging.
     * @param whitespaces Whitespaces for tagging.
     * @param tags BIO-coded tags.
     * @return The chunking derived from the BIO-coded tagging.
     */
    public static Chunking toChunkingBIO(String[] tokens, 
                                         String[] whitespaces,
                                         String[] tags) {
        StringBuilder sb = new StringBuilder();
        if (whitespaces == null) {
            whitespaces = new String[tokens.length+1];
            Arrays.fill(whitespaces," ");
            whitespaces[0] = "";
            whitespaces[whitespaces.length-1] = "";
        }
        for (int i = 0; i < tokens.length; ++i) {
            sb.append(whitespaces[i]);
            sb.append(tokens[i]);
        }
        sb.append(whitespaces[whitespaces.length-1]);
        ChunkingImpl chunking = new ChunkingImpl(sb);
    
        int pos = 0;
        for (int i = 0; i < tokens.length; ) {
            pos += whitespaces[i].length();
            if (!isBeginTag(tags[i])) {
                pos += tokens[i].length();
                ++i;
                continue;
            }
            int start = pos;
            String type = toBaseTag(tags[i]);
            while (true) {
                pos += tokens[i].length();
                ++i;
                if (i >= tokens.length 
                    || !isInTag(tags[i])) {
                    chunking.add(ChunkFactory.createChunk(start,pos,type));
                    break;
                }
                pos += whitespaces[i].length();
            }
        }        
        return chunking;
    }




}
