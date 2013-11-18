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

import java.util.regex.Pattern;

/**
 * A {@code RegExFilteredTokenizerFactory} modifies the tokens
 * returned by a base tokenizer factory's tokizer by removing
 * those that do not match a regular expression pattern.
 *
 * <h3>Thread Safety</h3>
 *
 * A regular expression filtered tokenizer factory is thread safe if
 * its base tokenizer factory is thread safe.  The pattern for
 * this filter is used to create a {@link java.util.regex.Matcher}
 * for each token.  If the matcher matches, that is, if
 * {@link java.util.regex.Matcher#matches()} returns {@code true},
 * then the token is kept; otherwise, the token is removed.
 *
 * <h3>Serialization</h3>
 *
 * A regular expression filtered tokenizer factory is serializable if its
 * base tokenizer factory is serializable.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class RegExFilteredTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = -288230238413152431L;

    private final Pattern mPattern;

    /**
     * Construct a regular-expression filtered tokenizer factory from
     * the specified base factory and regular expression pattern that
     * accepted tokens must match.
     *
     * @param factory Base tokenizer factory.
     * @param pattern Pattern to match against tokens.
     */
    public RegExFilteredTokenizerFactory(TokenizerFactory factory,
                                         Pattern pattern) {
        super(factory);
        mPattern = pattern;
    }

    /**
     * Returns the pattern for this regex-filtered tokenizer.
     *
     * @return The pattern for this regex-filtered tokenizer.
     */
    public Pattern getPattern() {
        return mPattern;
    }



    /**
     * Returns the specified token if it matches this
     * filter's pattern and {@code null} otherwise.
     *
     * @param token Input token.
     * @return The input token if it matches, and {@code null}
     * otherwise.
     */
    @Override
    public String modifyToken(String token) {
        return mPattern.matcher(token).matches()
            ? token
            : null;
    }

    @Override
    public String toString() {
        return this.getClass().getName()
            + "\n  pattern=" + mPattern
            + "\n  base factory=\n    " 
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer
        extends AbstractSerializer<RegExFilteredTokenizerFactory> {
        static final long serialVersionUID = -9179825153562519026L;
        public Serializer() {
            this(null);
        }
        public Serializer(RegExFilteredTokenizerFactory factory) {
            super(factory);
        }
        @Override
        public void writeExternalRest(ObjectOutput out) throws IOException {
            out.writeObject(factory().mPattern);
        }
        public Object read(ObjectInput in, TokenizerFactory baseFactory)
            throws IOException, ClassNotFoundException {
            Pattern pattern = (Pattern) in.readObject();
            return new RegExFilteredTokenizerFactory(baseFactory,pattern);
        }
    }



}
