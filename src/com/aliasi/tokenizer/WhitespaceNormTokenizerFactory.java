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
 * A {@code WhitespaceNormTokenizerFactory} filters the tokenizers produced
 * by a base tokenizer factory to convert non-empty whitespaces to a single
 * space and leave empty (zero-length) whitespaces alone.
 *
 * <h3>Thread Safety</h3>
 *
 * A whitespace normalizing tokenizer factory is thread
 * safe if its base tokenizer factory is thread safe.
 *
 * <h3>Serialization</h3>
 *
 * A whitespace normalizing tokenizer factory is serializable if its
 * base tokenizer factory is serializable.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class WhitespaceNormTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = -5560361157129856065L;

    /**
     * Construct a whitespace normalizing tokenizer factory from the
     * specified base factory.
     *
     * @param factory Base tokenizer factory.
     */
    public WhitespaceNormTokenizerFactory(TokenizerFactory factory) {
        super(factory);
    }

    /**
     * Return the normalized form of the specified whitespace.
     *
     * @param whitespace Input whitespace.
     * @return Normalized whitespace.
     */
    @Override
    public String modifyWhitespace(String whitespace) {
        return whitespace.length() > 0 ? " " : "";
    }

    @Override
        public String toString() {
        return getClass().toString()
            + "\n  base factory=" 
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }


    static class Serializer
        extends AbstractSerializer<WhitespaceNormTokenizerFactory> {
        static final long serialVersionUID = -9192398875622789296L;
        public Serializer() {
            this(null);
        }
        public Serializer(WhitespaceNormTokenizerFactory factory) {
            super(factory);
        }
        public Object read(ObjectInput in,
                           TokenizerFactory baseFactory) {
            return new WhitespaceNormTokenizerFactory(baseFactory);
        }
    }


}


