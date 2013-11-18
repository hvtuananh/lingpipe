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
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.nio.CharBuffer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <code>RegExTokenizerFactory</code> creates a tokenizer factory
 * out of a regular expression.  The regular expression is presented
 * as an instance of {@link Pattern} and matching is carried out with
 * the {@link java.util.regex} package.  The pattern provided when the
 * factory is constructed is used to create instances of {@link
 * Matcher} for use in tokenizers.  The method {@link
 * Matcher#find(int)} is called to find the next token in an input
 * sequence.
 *
 * <P>For instance, consider a regular expression which takes a token
 * to be a sequence of alphabetic characters, a sequence of numeric
 * characters, or a single non-alphanumeric character:
 *
 * <pre>
 *      [a-zA-Z]+|[0-9]+|\S</pre>
 *
 * This can be used to construct a tokenizer factory:
 *
 * <pre>
 *     String regex = "[a-zA-Z]+|[0-9]+|\\S";
 *     TokenizerFactory tf = new RegExTokenizerFactory(regex);
 *     char[] cs = "abc de 123. ".toCharArray();
 *     Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);</pre>
 *
 * Note the escaping of the backslash character (<code>\</code>) in
 * the Java string <code>regex</code> with a backslash
 * (<code>\</code>), resulting in <code>\\</code>.  For the regular
 * expression there are no spaces within any of the disjuncts because
 * the matched tokens should not contain whitespaces.  Finally note
 * the use of Kleene plus (<code>+</code>) rather than Kleene star
 * (<code>*</code>) to ensure that tokens are at least a single
 * character long.  In fact, the constructor will throw an exception
 * if the pattern matches the empty string.
 *
 * <P>The tokenizer above will return the following tokens,
 * whitespaces and character offsets:
 *
 * <pre>
 *     whitespaces: "", " ", " ", "", " "
 *          tokens: "abc", "de", "123", "."
 *    token starts: 0, 4, 7, 10</pre>
 *
 * <h3>Thread Safety</h3>
 *
 * <p>A regular-expression-based tokenizer factory is
 * completely thread safe.
 *
 * <h3>Serialization</h3>
 *
 * <p>A regular-expression-based tokenizer factory may
 * be serialized.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe2.1
 */
public class RegExTokenizerFactory
    implements Serializable, TokenizerFactory {

    static final long serialVersionUID = -1668745791323535436L;

    private final Pattern mPattern;

    /**
     * Construct a regular expression tokenizer factory
     * using the specified regular expression for matching.
     *
     * @param regex The regular expression.
     * @throws PatternSyntaxException If the expression's syntax is
     * invalid.
     */
    public RegExTokenizerFactory(String regex) {
        this(Pattern.compile(regex));
    }

    /**
     * Construct a regular expression tokenizer factory using the
     * specified regular expression for matching according to the
     * specified flags.  The value of the lag should be a bitwise
     * disjunction (single vertical bar "<code>|</code>") of the
     * following flags: {@link Pattern#CASE_INSENSITIVE}, {@link
     * Pattern#MULTILINE}, {@link Pattern#DOTALL}, {@link
     * Pattern#UNICODE_CASE} and {@link Pattern#CANON_EQ}.
     * See {@link Pattern#compile(String,int)} for more information.
     *
     * @param regex The regular expression.
     * @param flags The match flags.
     * @throws PatternSyntaxException If the expression's syntax is
     * invalid.
     * @throws IllegalArgumentException If bit values other than those
     * corresponding to defined match flags are set in the flags.
     */
    public RegExTokenizerFactory(String regex, int flags) {
        this(Pattern.compile(regex,flags));
    }

    /**
     * Construct a regular expression tokenizer factory with
     * the specified pattern for matching.
     *
     * @param pattern Pattern to use for matching.
     */
    public RegExTokenizerFactory(Pattern pattern) {
        mPattern = pattern;
    }

    /**
     * Returns the regular expression pattern backing this
     * tokenizer factory.
     *
     * @return The pattern for this factory.
     */
    public Pattern pattern() {
        return mPattern;
    }

    public Tokenizer tokenizer(char[] cs, int start, int length) {
        return new RegExTokenizer(mPattern,cs,start,length);
    }

    Object writeReplace() {
        return new Externalizer(this);
    }

    /**
     * Return a description of this regex-based tokenizer
     * factory including its pattern's regular expression
     * and flags.
     *
     * @return A description of this regex-based tokenizer
     * factory.
     */
    @Override
    public String toString() {
        return getClass().toString()
            + "\n  pattern=" + pattern().pattern()
            + "\n  flags=" + pattern().flags();
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 7772106464245966975L;
        final RegExTokenizerFactory mFactory;
        public Externalizer() { this(null); }
        public Externalizer(RegExTokenizerFactory factory) {
            mFactory = factory;
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {
            return new RegExTokenizerFactory((Pattern) in.readObject());
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeObject(mFactory.mPattern);
        }
    }

    static class RegExTokenizer extends Tokenizer {
        final Matcher mMatcher;
        final char[] mCs;
        final int mStart;
        final int mLength;
        int mWhiteStart = 0;
        int mTokenStart = 0;
        int mTokenEnd = -1;
        boolean mHasNext = false;
        int mLastTokenStartPosition = -1;
        int mLastTokenEndPosition = -1;
        
        RegExTokenizer(Pattern pattern, char[] cs, int start, int length) {
            mMatcher = pattern.matcher(CharBuffer.wrap(cs,start,length));
            mCs = cs;
            mStart = start;
            mLength = length;
        }
        @Override
        public String nextToken() {
            return hasNextToken() ? getNextToken() : null;
        }
        boolean hasNextToken() {
            if (mHasNext) return true;
            if (!mMatcher.find(mWhiteStart)) return false;
            mHasNext = true;
            mTokenStart = mMatcher.start(0);
            mTokenEnd = mMatcher.end(0);
            return true;
        }
        String getNextToken() {
            String token
                = new String(mCs,mStart+mTokenStart,mTokenEnd-mTokenStart);
            mWhiteStart = mTokenEnd;
            mHasNext = false;
            mLastTokenStartPosition = mTokenStart;
            mLastTokenEndPosition = mTokenEnd;
            return token;
        }
        @Override
        public String nextWhitespace() {
            return new String(mCs,mStart+mWhiteStart,
                              (hasNextToken() ? mTokenStart : mLength)
                              - mWhiteStart);
        }
        @Override
        public int lastTokenStartPosition() {
            return mLastTokenStartPosition;
        }
        @Override
        public int lastTokenEndPosition() {
            return mLastTokenEndPosition;
        }            
    }

}
