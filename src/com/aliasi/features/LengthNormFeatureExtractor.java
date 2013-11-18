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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code LengthNormFeatureExtractor} converts feature vectors
 * produced by a contained extractor into unit length vectors.
 * 
 * <p> Length is defined as the (Euclidean or L2) distance from the
 * origin, which is given by:
 * 
 * <blockquote><pre>
 * length(map) = sqrt<big><big>(&Sigma</big></big><sub>{v in map.values()}</sub> v * v<big><big>)</big></big></pre></blockquote>
 * 
 * If the length is non-zero, a new map is created in which all values
 * are divided by the length, so that the returned value has unit
 * length (length = 1). If the length is zero, the feature vector
 * extracted by the contained feature extractor is returned as is.
 * 
 * <p><b>Serialization</b> 
 *
 * <p>A length-norm feature
 * extractor is serializable if its contained base extractor is
 * serializable.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 * @param <E> The type of object whose features are extracted
 */
public class LengthNormFeatureExtractor<E> 
    extends FeatureExtractorFilter<E> 
    implements Serializable {

    static final long serialVersionUID = -5628628360712035433L;

    /**
     * Construct a length-normalized feature extractor from the 
     * specified extractor.
     *
     * @param extractor Base feature extractor.
     */
    public LengthNormFeatureExtractor(FeatureExtractor<? super E> extractor) {
        super(extractor);
    }

    /**
     * Return a length-normalized feature version of the
     * feature vector returned by the contained vector.
     *
     * @return The length-normalized feature vector.
     */
    @Override
    public Map<String, ? extends Number> features(E in) {
        Map<String, ? extends Number> baseMap = baseExtractor().features(in);
        double sumOfSquares = 0.0;
        for (Number n : baseMap.values()) {
            double val = n.doubleValue();
            sumOfSquares += val * val;
        }
        if (sumOfSquares == 0.0)
            return baseMap;
        double length = java.lang.Math.sqrt(sumOfSquares);
        Map<String, Double> resultMap = new HashMap<String, Double>();
        for (Map.Entry<String, ? extends Number> entry : baseMap.entrySet())
            resultMap.put(entry.getKey(), entry.getValue().doubleValue()
                          / length);
        return resultMap;
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 6365515337527915147L;
        private final LengthNormFeatureExtractor<F> mFilter;
        public Serializer() {
            this(null);
        }
        public Serializer(LengthNormFeatureExtractor<F> filter) {
            mFilter = filter;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mFilter.mExtractor);
        }
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {

            @SuppressWarnings("unchecked")
            // required for deserialization
            FeatureExtractor<F> extractor 
                = (FeatureExtractor<F>) in.readObject();
            return new LengthNormFeatureExtractor<F>(extractor);
        }
    }
}
