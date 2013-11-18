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
import com.aliasi.util.ObjectToDoubleMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Map;

/**
 * A {@code PrefixedFeatureExtractor} applies a specified prefix to all
 * of the feature names from a base feature extractor.  This class is
 * convenient when combining feature extractors in situations where
 * there might be name-space conflicts.
 *
 * <h3>Thread Safety</h3>
 *
 * A prefixed feature extractor is thread safe if its underlying
 * feature extractor is thread safe.
 * 
 * <h3>Serialization</h3>
 *
 * A prefixed feature extractor may be serialized if its underlying
 * feature extractor is serializable.  Deserialization produces an
 * instance of this class.
 * 
 * @author Bob Carpenter
 * @version 3.9.2
 * @since Lingpipe3.9.2
 * @param <E> Type of objects whose features are extracted
 */
public class PrefixedFeatureExtractor<E> 
    implements FeatureExtractor<E>, 
               Serializable {

    static final long serialVersionUID = -4693775065158617229L;


    private final FeatureExtractor<E> mBaseExtractor;
    private final String mPrefix;

    /**
     * Construct a feature extractor that adds the specified prefix
     * to feature names extracted by the specified extractor.
     *
     * @param prefix Prefix for feature names.
     * @param extractor Base feature extractor.
     */
    public PrefixedFeatureExtractor(String prefix,
                                    FeatureExtractor<E> extractor) {
        mPrefix = prefix;
        mBaseExtractor = extractor;
    }


    /**
     * Return the feature map derived by prefixing the specified prefix
     * to the feature names extracted by the base feature extractor.
     *
     * @param in Object whose features are extracted.
     * @return Mapping from prefixed features to values.
     */
    public Map<String,? extends Number> features(E in) {
        ObjectToDoubleMap<String> prefixedFeatureMap
            = new ObjectToDoubleMap<String>();
        Map<String,? extends Number> featureMap = mBaseExtractor.features(in);
        for (Map.Entry<String,? extends Number> entry : featureMap.entrySet())
            prefixedFeatureMap.set(mPrefix + entry.getKey(),
                                   entry.getValue().doubleValue());
        return prefixedFeatureMap;
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 6332957246411784407L;
        final PrefixedFeatureExtractor<F> mPrefixedFeatureExtractor;
        public Serializer(PrefixedFeatureExtractor<F> prefixedFeatureExtractor) {
            mPrefixedFeatureExtractor = prefixedFeatureExtractor;
        }
        public Serializer() {
            this(null);
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(mPrefixedFeatureExtractor.mPrefix);
            out.writeObject(mPrefixedFeatureExtractor.mBaseExtractor);
        }
        @Override
        public Object read(ObjectInput in) 
            throws ClassNotFoundException, IOException {
            
            String prefix = in.readUTF();
            @SuppressWarnings("unchecked")
            FeatureExtractor<F> extractor 
                = (FeatureExtractor<F>) in.readObject();
            return new PrefixedFeatureExtractor<F>(prefix,extractor);
        }
    }

}