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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@code StopTokenizerFactory} modifies a base tokenizer factory
 * by removing tokens in a specified stop set.  When a token is
 * removed from the output of a tokenizer, so is the whitespace
 * immediately following it.
 *
 * <h4>Thread Safety</h4>
 *
 * A stopped tokenizer factory is thread safe if its base
 * tokenizer factory is thread safe.
 *
 * <h4>Serialization</h4>
 *
 * A stopped tokenizer factory is serializable if its base
 * tokenizer factory is serializable.
 *
 * @author  Bob Carpenter
 * @version 4.0.1
 * @since   Lingpipe3.8
 */
public class StopTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    static final long serialVersionUID = -69680626775848410L;

    private final Set<String> mStopSet;

    /**
     * Construct a tokenizer factory that removes tokens
     * in the specified stop set from tokenizers produced
     * by the specified base factory.
     *
     * @param factory Base tokenizer factory.
     * @param stopSet Set of stop tokens.
     */
    public StopTokenizerFactory(TokenizerFactory factory,
                                Set<String> stopSet) {
        super(factory);
        mStopSet = new HashSet<String>(stopSet);
    }

    /**
     * Returns an unmodifiable view of the stop set
     * underlying this stop tokenizer factory.
     *
     * @return The stop set for this factory.
     */
    public Set<String> stopSet() {
        return Collections.unmodifiableSet(mStopSet);
    }

    @Override
    public String modifyToken(String token) {
        return mStopSet.contains(token)
            ? null
            : token;
    }

    @Override
    public String toString() {
        return this.getClass().getName()
            + "\n  stop set=" + mStopSet
            + "\n  base factory=\n    " 
            + baseTokenizerFactory().toString().replace("\n","\n    ");
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer
        extends AbstractSerializer<StopTokenizerFactory> {
        static final long serialVersionUID = 1555788949118743254L;
        public Serializer() {
            this(null);
        }
        public Serializer(StopTokenizerFactory factory) {
            super(factory);
        }
        @Override
        public void writeExternalRest(ObjectOutput out) throws IOException {
            out.writeInt(factory().mStopSet.size());
            for (String stop : factory().mStopSet)
                out.writeUTF(stop);
        }
        public Object read(ObjectInput in,
                           TokenizerFactory baseFactory)
            throws IOException, ClassNotFoundException {

            int size = in.readInt();
            Set<String> stopSet = new HashSet<String>();
            for (int i = 0; i < size; ++i)
                stopSet.add(in.readUTF());
            return new StopTokenizerFactory(baseFactory,stopSet);
        }
    }



}
