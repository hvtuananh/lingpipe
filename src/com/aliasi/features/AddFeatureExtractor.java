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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An {@code AddFeatureExtractor} returns feature vectors that result
 * from summing the feature vectors returned by a collection of
 * contained feature extractors.
 *
 * @author Bob Carpenter
 * @version 3.9.2
 * @since Lingpipe3.8
 * @param <E> Type of objects whose features are extracted
 */
public class AddFeatureExtractor<E> 
    implements FeatureExtractor<E>, Serializable {

    static final long serialVersionUID = -79320527848334652L;

    private final List<FeatureExtractor<? super E>> mExtractors;

    /**
     * Construct an additive feature extractor from the specified
     * collection of extractors.  Each extractor in the collection must be
     * capable of parsing objects of type {@code E}.
     * 
     * <p>The collection will be copied locally, so that subsequent
     * changes to the extractor collection supplied to the constructor
     * will not affect the returned feature extractor.
     * 
     * @param extractors Collection of feature extractors.
     */
    public AddFeatureExtractor(Collection<? extends FeatureExtractor<? super E>> extractors) {
        mExtractors = new ArrayList<FeatureExtractor<? super E>>(extractors);
    }

    /**
     * Construct an additive feature extractor from the specified pair
     * of extractors.
     *
     * @param extractor1 First feature extractor.
     * @param extractor2 Second feature extractor.
     */
    public AddFeatureExtractor(FeatureExtractor<? super E> extractor1,
                               FeatureExtractor<? super E> extractor2) {
        mExtractors = new ArrayList<FeatureExtractor<? super E>>(2);
        mExtractors.add(extractor1);
        mExtractors.add(extractor2);
    }

    /**
     * Construct an additive feature extractor from the specified
     * extractors.
     * 
     * @param extractors Variable length list (or a single array) of extractors.
     */
    public AddFeatureExtractor(FeatureExtractor<? super E>... extractors) {
        this(Arrays.asList(extractors));
    }


    public Map<String,? extends Number> features(E in) {
        ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
        for (FeatureExtractor<? super E> extractor : mExtractors)
            for (Map.Entry<String,? extends Number> featMap : extractor.features(in).entrySet())
                result.increment(featMap.getKey(),featMap.getValue().doubleValue());
        return result;
    }
    
    /**
     * Returns an unmodifiable view of the list of base feature
     * extractors for this additive feature extractor.
     *
     * @return The base feature extractors.
     */
    public List<FeatureExtractor<? super E>> baseFeatureExtractors() {
        return Collections.unmodifiableList(mExtractors);
    }


    Object writeReplace() {
        return new Externalizer<E>(this);
    }

    static class Externalizer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -3717190107802122376L;
        final AddFeatureExtractor<F> mAddFeatureExtractor;

        public Externalizer(AddFeatureExtractor<F> addFeatureExtractor) {
            mAddFeatureExtractor = addFeatureExtractor;
        }

        public Externalizer() {
            this(null);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mAddFeatureExtractor.mExtractors.size());
            for (FeatureExtractor<? super F> extractor : mAddFeatureExtractor.mExtractors)
                out.writeObject(extractor);
        }

        @Override
        public Object read(ObjectInput in) 
            throws ClassNotFoundException, IOException {

            int size = in.readInt();
            List<FeatureExtractor<? super F>> extractors = new ArrayList<FeatureExtractor<? super F>>();
            for (int i = 0; i < size; ++i) {
                @SuppressWarnings("unchecked")
                // know this is right type if written
                    FeatureExtractor<? super F> extractor = (FeatureExtractor<? super F>) in
                    .readObject();
                extractors.add(extractor);
            }
            return new AddFeatureExtractor<F>(extractors); // copies again
        }
    }
}

