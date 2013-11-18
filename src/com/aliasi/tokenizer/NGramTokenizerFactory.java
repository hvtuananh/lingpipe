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
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.Serializable;

/**
 * An <code>NGramTokenizerFactory</code> creates n-gram tokenizers
 * of a specified minimum and maximun length.
 *
 * <p>An <code>NGramTokenizer</code> is a tokenizer that returns the
 * character n-grams from a specified sequence between a minimum
 * and maximum length.  Whitespace takes the default behavior from
 * {@link Tokenizer#nextWhitespace()}, returning a string consisting of
 * a single space character.
 *
 * <p>For example, the result of
 * <blockquote>
 *   <code>
 *     new NGramTokenizer("abcd".toCharArray(),0,4,2,3).tokenize()
 *   </code>
 * </blockquote>
 * is the string array:
 * <blockquote>
 *   <code>
 *     { "ab", "bc", "cd", "abc", "bcd" }
 *   </code>
 * </blockquote>
 *
 * <h3>Thread Safety</h3>
 *
 * N-gram tokenizer factories are completely thread safe.
 *
 * <h3>Serialization</h3>
 *
 * <p>N-gram tokenizer factories are serializable.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe1.0
 */
public class NGramTokenizerFactory
    implements TokenizerFactory, Serializable {


    static final long serialVersionUID = -3208689473309929010L;

    private final int mMinNGram;
    private final int mMaxNGram;

    /**
     * Create an n-gram tokenizer factory with the specified minimum
     * and maximum n-gram lengths.
     *
     * @param minNGram Minimum n-gram length.
     * @param maxNGram Maximum n-gram length.
     * @throws IllegalArgumentException If the minimum is greater than
     * the maximum or if the maximum is less than one.
     */
    public NGramTokenizerFactory(int minNGram, int maxNGram) {
        if (maxNGram < 1) {
            String msg = "Require max >= 1."
                + " Found maxNGram=" + maxNGram;
            throw new IllegalArgumentException(msg);
        }
        if (minNGram > maxNGram) {
            String msg = "Require min <= max."
                + " Found min=" + minNGram
                + " max=" + maxNGram;
            throw new IllegalArgumentException(msg);
        }
        mMinNGram = minNGram;
        mMaxNGram = maxNGram;
    }


    /**
     * Returns the minimum n-gram length returned by this tokenizer
     * factory.
     *
     * @return The minimum n-gram length.
     */
    public int minNGram() {
        return mMinNGram;
    }

    /**
     * Returns the maximum n-gram length returned by this tokenizer
     * factory.
     *
     * @return The maximum n-gram length.
     */
    public int maxNGram() {
        return mMaxNGram;
    }


    /**
     * Returns an n-gram tokenizer for the specified characters
     * with the minimum and maximum n-gram lengths as specified
     * in the constructor.
     *
     * @param cs Underlying character array.
     * @param start Index of first character in array to tokenize.
     * @param length Number of characters to tokenize.
     */
    public Tokenizer tokenizer(char[] cs, int start, int length) {
        return new NGramTokenizer(cs,start,length,mMinNGram,mMaxNGram);
    }

    /**
     * Returns a description of this n-gram tokenizer factory,
     * including minimum and maximum token lengths.
     *
     * @return A description of this n-gram tokenizer factory.
     */
    @Override 
    public String toString() {
        return getClass().getName()
            + "\n min=" + mMinNGram
            + "\n max=" + mMaxNGram;
    }

    Object writeReplace() {
        return new Externalizer(this);
    }

    private static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = 7342984199917470310L;
        final NGramTokenizerFactory mFactory;
        public Externalizer() {
            this(null);
        }
        public Externalizer(NGramTokenizerFactory factory) {
            mFactory = factory;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mFactory.mMinNGram);
            objOut.writeInt(mFactory.mMaxNGram);
        }
        @Override
        public Object read(ObjectInput objIn) throws IOException {
            int minNGram = objIn.readInt();
            int maxNGram = objIn.readInt();
            return new NGramTokenizerFactory(minNGram,maxNGram);
        }
    }


}
