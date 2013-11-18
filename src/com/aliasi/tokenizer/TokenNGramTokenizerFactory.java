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
 * A {@code TokenNGramTokenizerFactory} wraps a base tokenizer to
 * produce token <i>n</i>-gram tokens of a specified size.  
 * 
 * <p>For example, suppose we have a regex tokenizer factory that
 * generates tokens based on contiguous letter characters.  We can
 * use it to build a token <i>n</i>-gram tokenizer factory that
 * generates token bigrams and trigrams made up of the tokens
 * from the base tokenizer.
 *
 * <blockquote><pre>
 * TokenizerFactory tf 
 *    = new RegExTokenizerFactory("\\S+");
 * TokenizerFactory ntf 
 *    = new TokenNGramTokenizerFactory(2,3,tf);</pre></blockquote>
 *
 * The sequences of tokens produced by <code>tf</code> for some
 * inputs are as follows.
 *
 * <blockquote><table cellpadding="5" border="1">
 * <tr><th>String</th><th>Tokens</th></tr>
 * <tr><td><code>"a"</code></td>
 *     <td><code></code></td></tr>
 * <tr><td><code>"a b"</code></td>
 *     <td><code>"a b"</code></td></tr>
 * <tr><td><code>"a b c"</code></td>
 *     <td><code>"a b", "b c", "a b c"</code></td></tr>
 * <tr><td><code>"a b c d"</code></td>
 *     <td><code>"a b", "b c", "c d", "a b c", "b c d"</code></td></tr>
 * </table></blockquote>
 *
 * The start and end positions are calculated based on
 * the positions for the base tokens provided by the base
 * tokenizer.
 *
 * <h3>Thread Safety</h3>
 *
 * A token n-gram tokenizer factory is thread safe if its
 * embedded tokenizer factory is thread safe.
 *
 * <h3>Serializability</h3>
 *
 * A token n-gram tokenizer factory is serializable if its
 * embedded tokenizer factory is serializable.  The reconstituted
 * object will be of this same class with the same parameters.
 *
 * @author Bob Carpenter
 * @author Breck Baldwin
 * @version 4.0.1
 * @since LingPipe4.0.1
 * 
 */
public class TokenNGramTokenizerFactory
    implements TokenizerFactory, Serializable {

    static final long serialVersionUID = -2133429510163306074L;

    private final int mMin;
    private final int mMax;
    private final TokenizerFactory mTokenizerFactory;

    /**
     * Construct a token n-gram tokenizer factory using the
     * specified base factory that produces n-grams within the
     * specified minimum and maximum length bounds.
     *
     * @param factory Base tokenizer factory.
     * @param min Minimum n-gram length (inclusive).
     * @param max Maximum n-gram length (inclusive).
     * @throws IllegalArgumentException If the minimum is less than 1 or
     * the maximum is less than the minimum.
     */
    public TokenNGramTokenizerFactory(TokenizerFactory factory,
                                      int min, int max) {
        mTokenizerFactory = factory;
        mMin = min;
        mMax = max;
        if (min < 1) {
            String msg = "Minimum must be > 0."
                + " Found min=" + min;
            throw new IllegalArgumentException(msg);
        }
        if (min > max) {
            String msg = "Require min <= max."
                + " Found min=" + min
                + " max=" + max;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Return the minimum n-gram length.
     *
     * @return Minimum n-gram length.
     */
    public int minNGram() {
        return mMin;
    }

    /**
     * Return the maximum n-gram length.
     *
     * @return Maximum n-gram length.
     */
    public int maxNGram() {
        return mMax;
    }

    /**
     * Return the base tokenizer factory used to generate
     * the underlying tokens from which <i>n</i>-grams are
     * generated.
     *
     * @return Underlying tokenizer factory.
     */
    public TokenizerFactory baseTokenizerFactory() {
        return mTokenizerFactory;
    }


    public Tokenizer tokenizer(char[] cs, int start, int len) {
        Tokenization tokenization = new Tokenization(cs,start,len,mTokenizerFactory);
        return new TokenNGramTokenizer(tokenization,mMin,mMax);
    }

    @Override
    public String toString() {
        return this.getClass().getName()
            + "\n  min ngram=" + mMin
            + "\n  max ngram=" + mMax
            + "\n  base factory=\n    " + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 4080546796365073045L;
        final TokenNGramTokenizerFactory mFactory;
        public Serializer() { 
            this(null); 
        }
        public Serializer(TokenNGramTokenizerFactory factory) {
            mFactory = factory;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mFactory.mMin);
            out.writeInt(mFactory.mMax);
            out.writeObject(mFactory.mTokenizerFactory);
        }
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            int min = in.readInt();
            int max = in.readInt();
            @SuppressWarnings("unchecked")
            TokenizerFactory factory = (TokenizerFactory) in.readObject();
            return new TokenNGramTokenizerFactory(factory,min,max);
        }
    }

    static class TokenNGramTokenizer extends Tokenizer {
        private final int mMax;
        private final Tokenization mTokenization;
        private int mNGram;
        private int mPos = 0;
        private int mLastTokenStartPosition = -1;
        private int mLastTokenEndPosition = -1;
        public TokenNGramTokenizer(Tokenization tokenization,
                                   int min, int max) {
            mTokenization = tokenization;
            mMax = max;
            mNGram = min;
            
        }
        public String nextToken() {
            if (mNGram > mMax)
                return null;
            int endPos = mPos + mNGram - 1;
            if (endPos >= mTokenization.numTokens()) {
                ++mNGram;
                mPos = 0;
                return nextToken();
            }
            mLastTokenStartPosition = mTokenization.tokenStart(mPos);
            mLastTokenEndPosition = mTokenization.tokenEnd(endPos);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mNGram; ++i) {
                if (i > 0) sb.append(' ');
                sb.append(mTokenization.token(mPos + i));
            }           
            ++mPos;
            return sb.toString();
        }
        public int lastTokenStartPosition() {
            return mLastTokenStartPosition;
        }
        public int lastTokenEndPosition() {
            return mLastTokenEndPosition;
        }
    }
    

}
