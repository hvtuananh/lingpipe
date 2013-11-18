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
 * A <code>CharacterTokenizerFactory</code> considers each
 * non-whitespace character in the input to be a distinct token.  This
 * factory is useful for handling languages such as Chinese, which
 * includes thousands of characters and presents a difficult tokenization
 * problem for standard tokenizers.
 *
 * <h3>Thread Safety</h3>
 *
 * Character tokenizer factories are completely thread safe.
 *
 * <h3>Singleton</h3>
 *
 * <p>Because the tokenizer factory is thread safe and immutable, the
 * recommended usage is through the static singleton instance {@link
 * #INSTANCE}.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * Character tokenizer factories may be serialized.  The
 * deserialized version will be equal to the singleton {@link #INSTANCE}.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   LingPipe1.0
 */
public class CharacterTokenizerFactory
    implements Serializable, TokenizerFactory {


    static final long serialVersionUID = -7533920958722689657L;

    /**
     * Construct a character tokenizer factory.
     *
     * <p><i>Implementation Note:</i> All character tokenizer
     * factories behave the same way, and they are thread safe, so the
     * constant {@link #INSTANCE} may be used anywhere a freshly
     * constructed character tokenizer factory is used, without loss
     * of performance.
     *
     */
    CharacterTokenizerFactory() {
        /* do nothing */
    }

    /**
     * Returns a character tokenizer for the specified character
     * array slice.
     *
     * @param ch Characters to tokenize.
     * @param start Index of first character to tokenize.
     * @param length Number of characters to tokenize.
     */
    public Tokenizer tokenizer(char[] ch, int start, int length) {
        return new CharacterTokenizer(ch,start,length);
    }

    /**
     * Returns a string representation of this tokenizer factory,
     * which is just its name.
     *
     * @return The string representation of this tokenizer factory.
     */
    @Override 
    public String toString() {
        return getClass().getName();
    }

    Object writeReplace() {
        return new Externalizer();
    }


    /**
     * An instance of a character tokenizer factory, which may be used
     * wherever a character tokenizer factory is needed.  This
     * instance is returned by compilation.
     */
    public static final TokenizerFactory INSTANCE
        = new CharacterTokenizerFactory();


    private static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 1313238312180578595L;
        public Externalizer() {
            /* do nothing */
        }
        @Override
        public void writeExternal(ObjectOutput objOut) {
            /* do nothing */
        }
        @Override
        public Object read(ObjectInput objIn) { 
            return INSTANCE; 
        }
    }



}
