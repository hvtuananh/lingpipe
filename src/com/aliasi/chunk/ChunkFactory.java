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

/**
 * The <code>ChunkFactory</code> provides static factory methods for
 * creating chunks from components.  Each chunk must specify the index
 * of its first character and the index of one past its last
 * character.  A chunk may optionally specify a string-based type and
 * score.  If a type or score is not specified, the default values
 * {@link #DEFAULT_CHUNK_TYPE} and {@link #DEFAULT_CHUNK_SCORE},
 * respecitively.
 *
 * @author  Bob Carpenter
 * @version 2.1
 * @since   LingPipe2.1
 */
public class ChunkFactory {

    // don't allow instances
    private ChunkFactory() { 
        /* do nothing */
    }

    /**
     * The default chunk type, <code>&quot;CHUNK&quot;</code>.
     */
    public static final String DEFAULT_CHUNK_TYPE = "CHUNK";

    /**
     * The default chunk score, {@link Double#NEGATIVE_INFINITY}.  This
     * value, unlike {@link Double#NaN}, is less than other double
     * values and equal (<code>==</code>) to itself.
     */
    public static final double DEFAULT_CHUNK_SCORE 
        = Double.NEGATIVE_INFINITY;
    
    /**
     * Return a chunk with the specified start and end positions, with
     * default type and score.  The default type is given by {@link
     * #DEFAULT_CHUNK_TYPE} and default score by {@link
     * #DEFAULT_CHUNK_SCORE}.  
     *
     * @param start Index of the first character in the chunk.
     * @param end Index of one past the last character in the chunk.
     * @return The created chunk.
     * @throws IllegalArgumentException If start is less than zero or
     * end is less than start.
     */
    public static Chunk createChunk(int start, int end) {
        validateSpan(start,end);
        return new StartEndChunk(start,end);
    }

    /**
     * Return a chunk with the specified start and end positions, specified
     * type and default score.  The default score is given by {@link
     * #DEFAULT_CHUNK_SCORE}.  
     *
     * @param start Index of the first character in the chunk.
     * @param end Index of one past the last character in the chunk.
     * @param type Type of the chunk created.
     * @return The created chunk.
     * @throws IllegalArgumentException If start is less than zero or
     * end is less than start.
     */
    public static Chunk createChunk(int start, int end, String type) {
        validateSpan(start,end);
        return new StartEndTypeChunk(start,end,type);
    }

    /**
     * Return a chunk with the specified start and end positions,
     * specified score and default type.  The default type is given by
     * {@link #DEFAULT_CHUNK_TYPE}.
     *
     * @param start Index of the first character in the chunk.
     * @param end Index of one past the last character in the chunk.
     * @param score Score of created chunk.
     * @return The created chunk.
     * @throws IllegalArgumentException If start is less than zero or
     * end is less than start.
     */
    public static Chunk createChunk(int start, int end, double score) {
        validateSpan(start,end);
        return new StartEndScoreChunk(start,end,score);
    }

    /**
     * Return a chunk with the specified start and end positions, type
     * and score.  
     *
     * @param start Index of the first character in the chunk.
     * @param end Index of one past the last character in the chunk.
     * @param type Type of the chunk created.
     * @param score Score of created chunk.
     * @return The created chunk.
     * @throws IllegalArgumentException If start is less than zero or
     * end is less than start.
     */
    public static Chunk createChunk(int start, int end, 
                                    String type, double score) {
        validateSpan(start,end);
        return new StartEndTypeScoreChunk(start,end,type,score);
    }

    private static void validateSpan(int start, int end) {
        if (start < 0) {
            String msg = "Start must be >= 0."
                + " Found start=" + start;
            throw new IllegalArgumentException(msg);
        }
        if (start > end) {
            String msg = "Start must be > end."
                + " Found start=" + start
                + " end=" + end;
            throw new IllegalArgumentException(msg);
        }
    }

    private static abstract class AbstractChunk implements Chunk {
        private final int mStart;
        private final int mEnd;
        AbstractChunk(int start, int end) {
            mStart = start;
            mEnd = end;
        }
        public final int start() {
            return mStart;
        }
        public final int end() {
            return mEnd;
        }
        public String type() {
            return DEFAULT_CHUNK_TYPE;
        }
        public double score() {
            return DEFAULT_CHUNK_SCORE;
        }
        @Override
        public boolean equals(Object that) {
            if (!(that instanceof Chunk)) return false;
            Chunk thatChunk = (Chunk) that;
            return start() == thatChunk.start()
                && end() == thatChunk.end()
                && score() == thatChunk.score()
                && type().equals(thatChunk.type());
        }
        @Override
        public int hashCode() {
            int h1 = start();
            int h2 = end();
            int h3 = type().hashCode();
            return h1 + 31*(h2 + 31*h3); // ignores score
        }
        @Override
        public String toString() {
            return start() + "-" + end() + ":" + type() + "@" + score();
        }
    }

    private static final class StartEndChunk extends AbstractChunk {
        StartEndChunk(int start, int end) {
            super(start,end);
        }
    }

    private static final class StartEndTypeChunk extends AbstractChunk {
        private final String mType;
        StartEndTypeChunk(int start, int end, String type) {
            super(start,end);
            mType = type;
        }
        @Override
        public String type() {
            return mType;
        }
    }

    private static final class StartEndScoreChunk extends AbstractChunk {
        private final double mScore;
        StartEndScoreChunk(int start, int end, double score) {
            super(start,end);
            mScore = score;
        }
        @Override
        public double score() {
            return mScore;
        }
    }

    private static final class StartEndTypeScoreChunk extends AbstractChunk {
        private final String mType;
        private final double mScore;
        StartEndTypeScoreChunk(int start, int end, String type,
                               double score) {
            super(start,end);
            mType = type;
            mScore = score;
        }
        @Override
        public String type() {
            return mType;
        }
        @Override
        public double score() {
            return mScore;
        }
    }

}
