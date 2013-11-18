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
 * A {@code PorterStemmerTokenizerFactory} applies Porter's stemmer
 * to the tokenizers produced by a base tokenizer factory.
 *
 * <p>Porter's stemmer computes an approximation of converting words
 * to their morphological base form.  This class provides a single
 * top-level static method, {@link #stem(String)}, which returns a
 * stemmed form of an input string.
 *
 * <h4>Serialization</h4>
 *
 * A Porter stemming tokenizer factory is serializable if its
 * base tokenizer factory is serializable.
 *
 * <h4>Thread Safety</h4>
 *
 * A Porter stemming tokenizer factory is thread safe if its
 * base tokenizer factory is thread safe.
 *
 * <h4>Implementation</h4>
 *
 * <p>The underlying
 * stemming code is Martin Porter's own public domain Java port of his
 * original C implementation of stemming.  More information can be found at:
 *
 * <p>
 * <blockquote>
 *   <a href="http://www.tartarus.org/~martin/PorterStemmer">Porter
 *     Stemmer Home Page</a>
 * </blockquote>
 * </p>
 *
 * <h4>References</h4>
 *
 * <p>
 * The original paper describing Porter's stemmer is:
 *
 * <blockquote>
 *   Porter, Martin. 1980. An algorithm for suffix stripping. <i>Program</i>.
 *    <b>14</b>:3. 130--137.
 * </blockquote>
 * </p>
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class PorterStemmerTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = 1257970981781551262L;

    /**
     * Construct a tokenizer factory that applies Porter stemming
     * to the tokenizers produced by the specified base factory.
     *
     * @param factory Base tokenizer factory.
     */
    public PorterStemmerTokenizerFactory(TokenizerFactory factory) {
        super(factory);
    }

    /**
     * Returns the Porter stemmed version of the specified
     * token.
     *
     * @param token Token to stem.
     * @return Stemmed version of token.
     */
    public String modifyToken(String token) {
        return stem(token);
    }

    /**
     * Return the stem of the specified input string using the Porter
     * stemmer.
     *
     * @param in String to stem.
     * @return Stem of the specified string.
     */
    static public String stem(String in) {
        String result = PorterStemmer.stem(in);
        return result;
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    public String toString() {
        return getClass().toString()
            + "\n  base factory=\n    "
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    static class Serializer
        extends AbstractSerializer<PorterStemmerTokenizerFactory> {
        static final long serialVersionUID = -4758505014396491716L;
        public Serializer() {
            this(null);
        }
        public Serializer(PorterStemmerTokenizerFactory factory) {
            super(factory);
        }
        public Object read(ObjectInput in,
                           TokenizerFactory baseFactory) {
            return new PorterStemmerTokenizerFactory(baseFactory);
        }
    }



}
