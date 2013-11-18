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

import java.util.Locale;

/**
 * A {@code LowerCaseTokenizerFactory} filters the tokenizers produced
 * by a base tokenizer factory to produce lower case output.  A locale
 * must be specified in order to carry out the case conversion.
 *
 * <h3>Thread Safety</h3>
 *
 * A lowercasing tokenizer factory is thread safe if its
 * base tokenizer factory is thread safe.
 *
 * <h3>Serialization</h3>
 *
 * A lowercasing tokenizer factory is serializable if its
 * base tokenizer factory is serializable.

 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class LowerCaseTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = 4616272325206021322L;

    private final Locale mLocale;

    /**
     * Construct a lowercasing tokenizer factory from
     * the specified base factory using the locale
     * {@code Locale.English}
     *
     * @param factory Base tokenizer factory.
     */
    public LowerCaseTokenizerFactory(TokenizerFactory factory) {
        this(factory,Locale.ENGLISH);
    }

    /**
     * Construct a lowercasing tokenizer factory from the
     * specified base factory using the specified locale.
     *
     * @param factory Base tokenizer factory.
     * @param locale Locale to use for lowercasing.
     */
    public LowerCaseTokenizerFactory(TokenizerFactory factory,
                                     Locale locale) {
        super(factory);
        mLocale = locale;
    }

    /**
     * Return the locale for this factory.
     *
     * @return The locale for this factory.
     */
    public Locale locale() {
        return mLocale;
    }

    /**
     * Return the lowercased version of the specified
     * token using this factory's locale.
     *
     * @param token Token to modify.
     * @return Lowercased token.
     */
    @Override
    public String modifyToken(String token) {
        return token.toLowerCase(mLocale);
    }

    @Override
        public String toString() {
        return getClass().getName()
            + "\n  base factory=\n    " 
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }


    static class Serializer
        extends AbstractSerializer<LowerCaseTokenizerFactory> {
        static final long serialVersionUID = 994442977212325798L;
        public Serializer() {
            this(null);
        }
        public Serializer(LowerCaseTokenizerFactory factory) {
            super(factory);
        }
        public void writeExternalRest(ObjectOutput out) throws IOException {
            out.writeObject(factory().locale());
        }
        public Object read(ObjectInput in,
                           TokenizerFactory baseFactory)
            throws IOException, ClassNotFoundException {

            Locale locale = (Locale) in.readObject();
            return new LowerCaseTokenizerFactory(baseFactory,locale);
        }
    }


}


