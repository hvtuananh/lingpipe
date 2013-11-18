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

import com.aliasi.tag.StringTagging;
import com.aliasi.tag.Tagging;

/**
 * The {@code TagChunkCodecAdapters} class contains static utility methods for
 * adapting tagging handlers to chunking handlers and vice-versa using tag-chunk
 * codecs.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe3.9.1
 */
public class TagChunkCodecAdapters {

    private TagChunkCodecAdapters() { 
        /* no instances */ 
    }

    /**
     * Return the chunking handler that converts chunkings to taggings
     * using the specified codec.
     *
     * @param codec Tag chunk coder/decoder for converting chunkings
     * to string taggings.
     * @param handler Handler to receive string taggings corresponding
     * to chunkings.
     * @return Chunking handler adapted from string tagging handler.
     */
    public static ObjectHandler<Chunking> stringTaggingToChunking(TagChunkCodec codec,
                                                                  ObjectHandler<StringTagging> handler) {
        return new StringTaggingHandlerAdapter(codec,handler);
    }

    /**
     * Return the chunking handler that converts chunkings to simple
     * taggings using the specified codec.
     *
     * @param codec Tag chunk coder/decoder for converting chunkings
     * to taggings.
     * @param handler Handler to receive string taggings corresponding
     * to chunkings.
     * @return Chunking handler adapted from string tagging handler.
     */
    public static ObjectHandler<Chunking>
        taggingToChunking(TagChunkCodec codec,
                          ObjectHandler<Tagging<String>> handler) {
        return new TaggingHandlerAdapter(codec,handler);
    }


    /**
     * Return the string tagging handler that converts string taggings
     * to chunkings.
     *
     * @param codec Tag-chunk coder/decoder for converting string
     * taggings to chunkings.
     * @param handler Handler to receive chunkings.
     * @return String tagging handler adapted from the chunking
     * handler.
     */
    public static ObjectHandler<StringTagging>
        chunkingToStringTagging(TagChunkCodec codec,
                                ObjectHandler<Chunking> handler) {
        return new ChunkingHandlerAdapter(codec,handler);
    }

    /**
     * Returns the tagging handler that converts taggings to chunkings
     * using the specified codec.  The conversion goes by means of
     * a string tagging created from the tagging by concatenating all
     * of the tokens and separating them with a single space character.
     *
     * @param codec Tag-chunk coder/decoder for converting string
     * taggings to chunkings.
     * @param handler Handler to receive chunkings.
     * @return Tagging handler adapted from the chunking
     * handler.
     */
    public static ObjectHandler<Tagging<String>>
        chunkingToTagging(TagChunkCodec codec,
                          ObjectHandler<Chunking> handler) {
        return new ChunkingHandlerAdapterPad(codec,handler);
    }


    static StringTagging pad(Tagging<String> tagging) {
        StringBuilder sb = new StringBuilder();
        int[] tokenStarts = new int[tagging.size()];
        int[] tokenEnds = new int[tagging.size()];
        for (int n = 0; n < tagging.size(); ++n) {
            if (n > 0) sb.append(' ');
            tokenStarts[n] = sb.length();
            sb.append(tagging.token(n));
            tokenEnds[n] = sb.length();
        }
        return new StringTagging(tagging.tokens(),
                                 tagging.tags(),
                                 sb,
                                 tokenStarts,
                                 tokenEnds);
    }
                                                                      
    static class StringTaggingHandlerAdapter implements ObjectHandler<Chunking> {
        private final TagChunkCodec mCodec;
        private final ObjectHandler<StringTagging> mHandler;
        public StringTaggingHandlerAdapter(TagChunkCodec codec,
                                           ObjectHandler<StringTagging> handler) {
            mCodec = codec;
            mHandler = handler;
        }
        public void handle(Chunking chunking) {
            StringTagging tagging = mCodec.toStringTagging(chunking);
            mHandler.handle(tagging);
        }
    }

    static class TaggingHandlerAdapter implements ObjectHandler<Chunking> {
        private final TagChunkCodec mCodec;
        private final ObjectHandler<Tagging<String>> mHandler;
        public TaggingHandlerAdapter(TagChunkCodec codec,
                                     ObjectHandler<Tagging<String>> handler) {
            mCodec = codec;
            mHandler = handler;
        }
        public void handle(Chunking chunking) {
            Tagging<String> tagging = mCodec.toTagging(chunking);
            mHandler.handle(tagging);
        }
    }

    static class ChunkingHandlerAdapter implements ObjectHandler<StringTagging> {
        private final TagChunkCodec mCodec;
        private final ObjectHandler<Chunking> mHandler;
        public ChunkingHandlerAdapter(TagChunkCodec codec,
                                      ObjectHandler<Chunking> handler) {
            mCodec = codec;
            mHandler = handler;
        }
        public void handle(StringTagging tagging) {
            Chunking chunking = mCodec.toChunking(tagging);
            mHandler.handle(chunking);
        }
    }

    static class ChunkingHandlerAdapterPad implements ObjectHandler<Tagging<String>> {
        private final TagChunkCodec mCodec;
        private final ObjectHandler<Chunking> mHandler;
        public ChunkingHandlerAdapterPad(TagChunkCodec codec,
                                         ObjectHandler<Chunking> handler) {
            mCodec = codec;
            mHandler = handler;
        }
        public void handle(Tagging<String> tagging) {
            StringTagging stringTagging = pad(tagging);
            Chunking chunking = mCodec.toChunking(stringTagging);
            mHandler.handle(chunking);
        }
    }

}