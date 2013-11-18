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

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A {@code TokenLengthTokenizerFactory} filters the tokenizers produced
 * by a base tokenizer to only return tokens between specified lower and
 * upper length limits.
 *
 * <h3>Thread Safety</h3>
 *
 * Token-length bounded tokenizer factories are thread safe if their
 * base tokenizers are thread safe.
 *
 * <h3>Serialization</h3>
 *
 * Token-length bounded tokenizer factories may be serialized if their
 * base tokenizers are serializable.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class TokenLengthTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = -431337104035802336L;

    private final int mShortestTokenLength;
    private final int mLongestTokenLength;

    /**
     * Construct a token-length filtered tokenizer factory from the
     * specified factory that removes tokens shorter than the shortest
     * or longer than the longest length.  To effectively remove
     * bounds, use {@link Integer#MIN_VALUE} and {@link
     * Integer#MAX_VALUE}.
     *
     * @param factory Base tokenizer factory.
     * @param shortestTokenLength Length of shortest acceptable token.
     * @param longestTokenLength Length of longest acceptable token.
     * @throws IllegalArgumentException If the shortest length is negative, or
     * the shortest length is greater than the longest length.
     */
    public TokenLengthTokenizerFactory(TokenizerFactory factory,
                                       int shortestTokenLength,
                                       int longestTokenLength) {
        super(factory);
        if (shortestTokenLength < 0) {
            String msg = "Shortest token length must be non-negative."
                + " Found shortestTokenLength=" + shortestTokenLength;
            throw new IllegalArgumentException(msg);
        }
        if (shortestTokenLength > longestTokenLength) {
            String msg = "Shortest token length must be <= longest."
                + " Found shortestTokenLength=" + shortestTokenLength
                + " longestTokenLength=" + longestTokenLength;
            throw new IllegalArgumentException(msg);
        }
        mShortestTokenLength = shortestTokenLength;
        mLongestTokenLength = longestTokenLength;
    }

    /**
     * Return a tokenizer that filters out any tokens produced by the specified
     * tokenizer that are shorter than the shortest or longer than the longest
     * acceptable lengths.
     *
     * @param token Input token.
     * @return The input token if it is an acceptable length and
     * {@code null} otherwise.
     */
    @Override
    public String modifyToken(String token) {
        return (token.length() < mShortestTokenLength
                || token.length() > mLongestTokenLength)
            ? null
            : token;
    }

    @Override
    public String toString() {
        return getClass().getName()
            + "\n  min length=" + mShortestTokenLength
            + "\n  max length=" + mLongestTokenLength
            + "\n  base factory=\n    " + 
            baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer
        extends AbstractSerializer<TokenLengthTokenizerFactory> {
        static final long serialVersionUID = -8886994304636616691L;
        public Serializer() {
            this(null);
        }
        public Serializer(TokenLengthTokenizerFactory factory) {
            super(factory);
        }
        @Override
        public void writeExternalRest(ObjectOutput out) throws IOException {
            out.writeInt(factory().mShortestTokenLength);
            out.writeInt(factory().mLongestTokenLength);
        }
        public Object read(ObjectInput in, TokenizerFactory baseFactory)
            throws IOException {
            int shortest = in.readInt();
            int longest = in.readInt();
            return new TokenLengthTokenizerFactory(baseFactory,shortest,longest);
        }
    }

}
