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

package com.aliasi.features;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A {@code KnownFeatureExtractor} restricts a base feature extractor
 * to features contained in a set provided at construction time.
 *
 * <h3>Serialization</h3>
 *
 * A known feature extractor is serializable if its base extractor
 * is serializable.  If the base extractor is not serializable,
 * attempts to serialize throw a {@code java.io.NotSerializableException}.
 *
 * @author Bob Carpenter
 * @version 3.8.1
 * @since Lingpipe3.9
 * @param <E> The type of objects whose features are extracted.
 */
public class KnownFeatureExtractor<E> 
    extends ModifiedFeatureExtractor<E> 
    implements Serializable {

    static final long serialVersionUID = 973305985402711781L;

    private final Set<String> mKnownFeatureSet;

    /**
     * Construct a known feature extractor based on the specified feature
     * set and base extractor.
     *
     * @param knownFeatureSet Set of known features.
     * @param baseExtractor Base feature extractor.
     */
    public KnownFeatureExtractor(FeatureExtractor<? super E> baseExtractor,
                                 Set<String> knownFeatureSet) {
        super(baseExtractor);
        mKnownFeatureSet = knownFeatureSet;
    }

    /**
     * Returns the value if the feature is in the known feature
     * set and {@code null} otherwise.
     *
     * @return The value if the feature is in the known feature
     * set and {@code null} otherwise.
     */
    @Override
    public Number filter(String feature, Number value) {
        return mKnownFeatureSet.contains(feature)
            ? value
            : null;
    }

    /**
     * Returns and unmodifiable view of the known feature set
     * for this known feature extractor.
     *
     * @return View of the known feature set.
     */
    public Set<String> knownFeatureSet() {
        return Collections.unmodifiableSet(mKnownFeatureSet);
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -8477676811264111403L;
        final KnownFeatureExtractor<F> mExtractor;
        public Serializer(KnownFeatureExtractor<F> extractor) {
            mExtractor = extractor;
        }
        public Serializer() {
            this(null);
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mExtractor.mKnownFeatureSet.size());
            for (String s : mExtractor.mKnownFeatureSet)
                out.writeUTF(s);
            out.writeObject(mExtractor.baseExtractor());
        }
        @Override
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            int size = in.readInt();
            Set<String> knownFeatureSet = new HashSet<String>((3 * size)/2);
            for (int i = 0; i < size; ++i)
                knownFeatureSet.add(in.readUTF());
            @SuppressWarnings("unchecked") 
            FeatureExtractor<F> baseExtractor
                = (FeatureExtractor<F>) in.readObject();
            return new KnownFeatureExtractor<F>(baseExtractor,knownFeatureSet);
        }
    }
    

}