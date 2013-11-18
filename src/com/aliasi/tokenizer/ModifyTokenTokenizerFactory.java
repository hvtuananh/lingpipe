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

import java.io.Serializable;

/**
 * The abstract base class {@code ModifyTokenTokenizerFactory}
 * adapts token and whitespace modifiers to modify tokenizer
 * factories.
 *
 * <p>The method {@link #modifyToken(String)} may be used to
 * modify or remove tokens from tokenizer outputs.  The method
 * {@link #modifyWhitespace(String)} may be used to modify the
 * whitespace returned by a tokenizer.  Both methods are given
 * pass-through implementations in this class.
 *
 * <h3>Serialization</h3>
 *
 * Like its parent class, this class implements {@code Serializable}.
 * There are no serialization methods defined, so the default
 * serialization is used.  There is a single reference to the base
 * tokenizer factory in the parent class, so a subclass will be
 * serializable if all of its member objects are serializable and the
 * base tokenizer is serializable.
 *
 * <p>It is good practice for each subclass to take completecontrol
 * over serialization using a serialization proxy implemented 
 * on top of the {@link com.aliasi.util.AbstractExternalizable} base
 * class.
 * 
 * <h3>Thread Safety</h3>
 *
 * This tokenizer factory is thread safe if the modify
 * token and modify whitespace implementations are thread
 * safe.  The implementations provided here are thread
 * safe.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
abstract public class ModifyTokenTokenizerFactory
    extends ModifiedTokenizerFactory implements Serializable {

    static final long serialVersionUID = -5608290781322140944L;

    /**
     * Construct a token-modifying tokenizer factory with
     * the specified base factory.
     *
     * @param factory Base tokenizer factory.
     */
    public ModifyTokenTokenizerFactory(TokenizerFactory factory) {
        super(factory);
    }

    /**
     * Return a modified version of the specified tokenizer that
     * modifies tokens and whitespaces as specified by the
     * corresponding string modifier methods.
     */
    public final Tokenizer modify(Tokenizer tokenizer) {
        return new ModifiedTokenizer(tokenizer);
    }

    /**
     * Return a modified form of the specified token, or
     * {@code null} to remove it.
     *
     * <p>The base implementation in this class simply
     * returns the specified token.
     *
     * @param token Token to modify.
     * @return Modified token or {@code null} to remove it.
     */
    public String modifyToken(String token) {
        return token;
    }

    /**
     * Return the modified form of the specified whitespace.
     *
     * <p>The base implementation in this class simply
     * returns the specified whitespace.
     *
     * @param whitespace Whitespace to modify.
     * @return The modified whitespace.
     */
    public String modifyWhitespace(String whitespace) {
        return whitespace;
    }

    @Override
    public String toString() {
        return getClass().getName()
            + "\n  base factory=\n    " + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    class ModifiedTokenizer extends Tokenizer implements Serializable {

        static final long serialVersionUID = -5608290781382946944L;
        private final Tokenizer mTokenizer;
        ModifiedTokenizer(Tokenizer tokenizer) {
            mTokenizer = tokenizer;
        }
        public String nextToken() {
            while (true) {
                String token = mTokenizer.nextToken();
                if (token == null)
                    return null;
                String modifiedToken = modifyToken(token);
                if (modifiedToken != null)
                    return modifiedToken;
            }
        }
        public String nextWhitespace() {
            String whitespace = mTokenizer.nextWhitespace();
            return modifyWhitespace(whitespace);
        }
        public int lastTokenStartPosition() {
            return mTokenizer.lastTokenStartPosition();
        }
        public int lastTokenEndPosition() {
            return mTokenizer.lastTokenEndPosition();
        }
    }

}
