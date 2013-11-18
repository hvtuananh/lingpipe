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
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@code Tokenization} represents the result of tokenizing a
 * string.  Tokenizations are constructed from a character sequence
 * and a tokenizer factory.  A tokenization contains the underlying
 * text, tokens, and token start/end positions in the text.
 *
 * <h3>Equality and Hash Codes</h3>
 *
 * Two tokenizations are equal if they have the same text, tokens,
 * whitespaces, and start/end positions for the tokens.  
 *
 * <p>Hash codes are consistent with equality.  They only depend on
 * the text and number of tokens.
 *
 * <h3>Serialization</h3>
 *
 * A tokenization may be serialized.  Deserialization should
 * produce an identical tokenization.
 * 
 * <h3>Thread Safety</h3>
 *
 * After safely published, objects are completely thread safe.  
 * The text and tokenizer factory should not be modified concurrently
 * with construction.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 */
public class Tokenization implements Serializable {
    
    static final long serialVersionUID = 3806073293589459401L;

    private final String mText;
    private final List<String> mTokens;
    private final List<String> mWhitespaces;
    private final int[] mTokenStarts;
    private final int[] mTokenEnds;

    /**
     * Construct a tokenization from the specified text and tokenizer
     * factory.  The text is converted to a string so that subsequent
     * changes to the text will not affect this class.  (Note that
     * the text should not be changed concurrently with constructing
     * a tokenization.)
     *
     * @param cs Underlying character array.
     * @param start Index of first character in slice.
     * @param length Length of slice.
     * @param factory Tokenizer factory to use for tokenization.
     * @throws IndexOutOfBoundsException If the start and length
     * indices are outside of bounds of the array.
     */
    public Tokenization(char[] cs, int start, int length,
                        TokenizerFactory factory) {
        this(new String(cs,start,length),
             factory.tokenizer(cs,start,length));
    }

    /**
     * Construct a tokenization from the specified text and tokenizer
     * factory.  
     *
     * @param text Underlying text for tokenization.
     * @param factory Tokenizer factory to perform tokenization.
     */
    public Tokenization(String text, TokenizerFactory factory) {
        this(text,factory.tokenizer(text.toCharArray(),0,text.length()));
    }


    /**
     * Construct a tokenization from the specified components.  The
     * arrays and lists are copied so that modifications to them
     * will not affect the constructed object after construction.
     *
     * @param text Underlying text.
     * @param tokens List of tokens.
     * @param whitespaces List of whitespaces.
     * @param tokenStarts Offset of first character in tokens.
     * @param tokenEnds Offset of last character plus one in tokens.
     * @throws IllegalArgumentException If the number of whitespaces is not
     * equal to the number of tokens plus one, a tokens start occurs after
     * a token end, or a token start or end is out of bounds for the text.
     */
    public Tokenization(String text,
                        List<String> tokens,
                        List<String> whitespaces,
                        int[] tokenStarts,
                        int[] tokenEnds) {
        this(text,
             new ArrayList<String>(tokens),
             new ArrayList<String>(whitespaces),
             tokenStarts.clone(),
             tokenEnds.clone(),
             false);
        if (tokens.size() != whitespaces.size() - 1) {
            String msg = "Require one more whitespace than token."
                + " Found tokens.size()=" + tokens.size()
                + " whitespaces.size()=" + whitespaces.size();
            throw new IllegalArgumentException(msg);
        }
        if (tokenStarts.length != tokens.size()) {
            String msg = "Require token starts to be same length as tokens"
                + " Found tokenStarts.length=" + tokenStarts.length
                + " tokenEnds.length=" + tokenEnds.length;
            throw new IllegalArgumentException(msg);
        }
        if (tokenEnds.length != tokens.size()) {
            String msg = "Require token starts to be same length as tokens"
                + " Found tokenEnds.length=" + tokenEnds.length
                + " tokenEnds.length=" + tokenEnds.length;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < tokenStarts.length; ++i) {
            if (tokenStarts[i] < 0) {
                String msg = "Token starts must be non-negative."
                    + " Found tokenStarts[" + i + "]=" + tokenStarts[i];
                throw new IllegalArgumentException(msg);
            }
            if (tokenEnds[i] > text.length()) {
                String msg = "Token ends must be less than or equal to text length."
                    + " Found tokenEnds[" + i + "]=" + tokenEnds[i]
                    + " text.length()=" + text.length();
                throw new IllegalArgumentException(msg);
            }
            if (tokenStarts[i] > tokenEnds[i]) {
                String msg = "Token starts must be less than or equal to ends."
                    + " Found tokenStarts[" + i + "]=" + tokenStarts[i]
                    + " tokenEnds[" + i + "]=" + tokenEnds[i];
                throw new IllegalArgumentException(msg);
            }
        }
    }

    Tokenization(String text,
                 List<String> tokens,
                 List<String> whitespaces,
                 int[] tokenStarts,
                 int[] tokenEnds,
                 boolean ignore) { // dummy var to distinguish constructor
        mText = text;
        mTokens = tokens;
        mWhitespaces = whitespaces;
        mTokenStarts = tokenStarts;
        mTokenEnds = tokenEnds;
    }

    Tokenization(String text, Tokenizer tokenizer) {
        mText = text;
        List<String> tokens = new ArrayList<String>();
        List<String> whitespaces = new ArrayList<String>();
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        String token;
        whitespaces.add(tokenizer.nextWhitespace());
        while ((token = tokenizer.nextToken()) != null) {
            tokens.add(token);
            whitespaces.add(tokenizer.nextWhitespace());
            starts.add(tokenizer.lastTokenStartPosition());
            ends.add(tokenizer.lastTokenEndPosition());
        }
        mTokens = tokens;
        mWhitespaces = whitespaces;
        mTokenStarts = new int[starts.size()];
        mTokenEnds = new int[starts.size()];
        for (int i = 0; i < starts.size(); ++i) {
            mTokenStarts[i] = starts.get(i);
            mTokenEnds[i] = ends.get(i);
        }
    }

    /**
     * Return the underlying text for this tokenization.
     *
     * @return Text for tokenization.
     */
    public String text() {
        return mText;
    }

    /**
     * Return the number of tokens in this tokenization.
     *
     * @return The number of tokens.
     */
    public int numTokens() {
        return mTokens.size();
    }

    /**
     * Return the token at the specified input position.
     *
     * @param n Position of token.
     * @return Token at specified position.
     * @throws IndexOutOfBoundsException If the position is less than 0 or
     * greater than or equal to the number of tokens.
     */
    public String token(int n) {
        return mTokens.get(n);
    }

    /**
     * Return the whitespace before the token at the specified
     * input position, or the last whitespace if the specified
     * position is the number of tokens.
     *
     * @param n Position of token.
     * @return Whitespace before the token in the specified position.
     * @throws IndexOutOfBoundsException If the position is less than 0
     * or greater than the number of tokens.
     */
    public String whitespace(int n) {
        return mWhitespaces.get(n);
    }

    /**
     * Return the position of the first character in the specified
     * input position.
     *
     * @param n Position of token.
     * @return The index of the first character in the specified
     * token.
     * @throws IndexOutOfBoundsException If the position is less than 0 or
     * greater than or equal to the number of tokens.
     */
    public int tokenStart(int n) {
        return mTokenStarts[n];
    }

    /**
     * Return the position of one past the last character in the
     * specified input position.
     *
     * @param n Position of token.
     * @return The index of the last character plus one for the
     * specified token.
     * @throws IndexOutOfBoundsException If the position is less than 0 or
     * greater than or equal to the number of tokens.
     */
    public int tokenEnd(int n) {
        return mTokenEnds[n];
    }

    /**
     * Returns the array of tokens underlying this tokenization.  This
     * array's length is the number of tokens and it is indexed by
     * token position.   
     *
     * <p>The array is copied from the underlying list of tokens, so
     * modifying it will not affect this tokenization.
     *
     * @return Array of tokens for this tokenization.
     */
    public String[] tokens() {
        return mTokens.toArray(Strings.EMPTY_STRING_ARRAY);
    }

    /**
     * Return the array of whitespaces for this tokenization.
     * The array's length is one greater than the number of tokens, and it
     * is indexed by following token position.
     *
     * <p>The array is copied from the underlying list of tokens, so
     * modifying it will not affect this tokenization.
     *
     * @return Array of whitespaces for this tokenization.
     */
    public String[] whitespaces() {
        return mWhitespaces.toArray(Strings.EMPTY_STRING_ARRAY);
    }

    /**
     * Returns an unmodifiable view of the list of tokens 
     * for this tokenization.
     *
     * @return List of tokens for this tokenization.
     */
    public List<String> tokenList() {
        return Collections.unmodifiableList(mTokens);
    }

    /**
     * Returns an unmodifiable view of the list of whitespaces
     * for this tokenization.
     *
     * @return List of whitespaces for this tokenization.
     */
    public List<String> whitespaceList() {
        return Collections.unmodifiableList(mWhitespaces);
    }

    /**
     * Returns {@code true} if the specified object is a tokenization
     * that is equal to this one.  Equality is defined as having the
     * same text, tokens, whitespaces, and token start and end positions.
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) 
            return true;
        if (!(that instanceof Tokenization))
            return false;
        Tokenization thatT = (Tokenization) that;
        if (!text().equals(thatT.text()))
            return false;
        if (numTokens() != thatT.numTokens())
            return false;
        for (int n = 0; n < numTokens(); ++n) {
            if (!token(n).equals(thatT.token(n)))
                return false;
            if (!whitespace(n).equals(thatT.whitespace(n)))
                return false;
            if (tokenStart(n) != thatT.tokenStart(n))
                return false;
            if (tokenEnd(n) != thatT.tokenEnd(n))
                return false;
        }
        if (!whitespace(numTokens()).equals(thatT.whitespace(numTokens())))
            return false;
        return true;
    }

    /**
     * Returns the hash code for this tokenization. The hash code is
     * consistent with equality, but only considers the text and
     * number of tokens.
     *
     * @return The hash code for this tokenization.
     */
    @Override
    public int hashCode() {
        return 31 * mText.hashCode() + mTokens.size();
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 5248361056143805108L;
        Tokenization mToks;
        public Serializer() {
            this(null);
        }
        public Serializer(Tokenization toks) {
            mToks = toks;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(mToks.mText);
            out.writeInt(mToks.mTokens.size());
            for (String token : mToks.mTokens)
                out.writeUTF(token);
            for (String whitespace : mToks.mWhitespaces)
                out.writeUTF(whitespace);
            writeInts(mToks.mTokenStarts,out);
            writeInts(mToks.mTokenEnds,out);
        }
        public Object read(ObjectInput in) throws IOException {
            String text = in.readUTF();
            int len = in.readInt();
            List<String> tokens = new ArrayList<String>(len);
            for (int i = 0; i < len; ++i)
                tokens.add(in.readUTF());
            List<String> whitespaces = new ArrayList<String>(len+1);
            for (int i = 0; i <= len; ++i)
                whitespaces.add(in.readUTF());
            int[] tokenStarts = readInts(in);
            int[] tokenEnds = readInts(in);
            boolean ignoreMe = true;
            return new Tokenization(text,tokens,whitespaces,tokenStarts,tokenEnds,ignoreMe);
        }
    }

}