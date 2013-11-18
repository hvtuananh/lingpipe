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
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;


/**
 * An {@code InteractionFeatureExtractor} produces interaction
 * features between two feature extractors.  The value of an
 * interaction feature is the product of the values of the individual
 * features.  The feature itself will be prefixed with a specific
 * string and the features for the interaction are separated by a
 * specific string.
 *
 * <p>If the two feature extractors are the same, then only
 * one feature extraction is performed on an input and redundant
 * features are removed. 
 *
 * <h3>Two Extractor Example</h3>
 *
 * If we have feature maps <code>{"a"->1.5, "b"->2.0, "c"->3.0}</code>
 * and <code>{"d"->1.0, "e"-> 0.25}</code>, with prefix <code>I:</code>
 * and separator <code>*</code>, then the interaction features are
 * <code>{"I:a*d"->1.5, "I:a*e"->0.375, "I:b*d"->2.0, "I:b*e"->0.5, "I:c*d"->3.0, "I:c*e"->0.75}</code>.
 * 
 * <h3>One Extractor Example</h3>
 *
 * If only one extractor is involved, symmetry is applied and the
 * features are not generated twice.  For instance, if we have
 * the single feature map <code>{"x"->-1.5, "y"->2.0, "z"->1.0}</code>,
 * the interaction feature map is
 * <code>{"I:x*x"->2.25, "I:x*y"->-3.0, "I:x*z"->-1.5, "I:y*y"->4.0, "I:y*z"->2.0, "I:z*z"->1.0}</code>.
 *
 * <p>In order to preserve uniqueness of the feature output in the
 * one-extractor case, we need to sort the features.  So we make sure
 * that the features are in sorted order in the combination (as
 * determined by the natural sort order defined for {@code String} by
 * its {@code compareTo(String)} method.  Specifically, we get
 * {@code "I:x*z"}, but not {@code "I:z*x"} in the case above.
 * This is not an issue for the two-extractor case, as you get all
 * pairs, with the first extractor's feature always coming first
 * in the resulting feature.
 *
 * <h3>Serialization</h3>
 *
 * An interaction feature extractor may be serialized if its
 * component extractors are serializable.
 *
 * <h3>Thread Safety</h3>
 *
 * An interaction feature structure is thread safe if the component
 * feature extractor(s) are thread safe.
 *
 * @author  Bob Carpenter
 * @version 3.9.2
 * @since   LingPipe3.9.2
 */
public class InteractionFeatureExtractor<E> 
    implements FeatureExtractor<E>,
               Serializable {
    
    static final long serialVersionUID = -8221138094563655817L;

    private final String mPrefix;
    private final String mSeparator;

    private final FeatureExtractor<E> mExtractor1;
    private final FeatureExtractor<E> mExtractor2;

    /**
     * Construct a feature extractor for interactions between
     * the features extracted by the specified extractors, creating
     * new features with the specified prefix and separator.
     *
     * @param prefix Prefix to prepend to interaction features.
     * @param separator Separator between features in interaction features.
     * @param extractor1 First feature extractor.
     * @param extractor2 Second feature extractor.
     */
    public InteractionFeatureExtractor(String prefix, 
                                       String separator,
                                       FeatureExtractor<E> extractor1,
                                       FeatureExtractor<E> extractor2) {
        mPrefix = prefix;
        mSeparator = separator;
        mExtractor1 = extractor1;
        mExtractor2 = extractor2;
    }

    /**
     * Construct a feature extractor for interactions between the
     * features extracted by the specified extractor, creating new
     * features with the specified prefix and separator.
     *
     * @param prefix Prefix to prepend to interaction features.
     * @param separator Separator between features in interaction features.
     * @param extractor Feature extractor.
     */
    public InteractionFeatureExtractor(String prefix, 
                                       String separator,
                                       FeatureExtractor<E> extractor) {
        this(prefix,separator,extractor,extractor);
    }

    /**
     * Return the interaction feature map for the specified input.
     *
     * @param in Input whose features are extracted.
     * @return The interaction feature map for the input.
     */
    public Map<String,Double> features(E in) {
        return mExtractor1 == mExtractor2
            ? features1(in)
            : features2(in);
    }

    Map<String,Double> features1(E in) {
        Map<String,? extends Number> featureMap
            = mExtractor1.features(in);
        String[] features 
            = featureMap.keySet().toArray(Strings.EMPTY_STRING_ARRAY);
        Arrays.sort(features); // ,Collections.<String>reverseOrder(Collections.<String>reverseOrder())); 
        double[] values = new double[features.length];
        for (int i = 0; i < values.length; ++i)
            values[i] = featureMap.get(features[i]).doubleValue();
        ObjectToDoubleMap<String> featureMapResult
            = new ObjectToDoubleMap<String>();
        for (int i = 0; i < features.length; ++i) {
            String initial = mPrefix + features[i] + mSeparator;
            for (int j = i; j < features.length; ++j) {
                String feature = initial + features[j];
                double value = values[i] * values[j];
                featureMapResult.set(feature,value);
            }
        }
        return featureMapResult;
    }

    Map<String,Double> features2(E in) {
        Map<String,? extends Number> features1 
            = mExtractor1.features(in);
        Map<String,? extends Number> features2
            = mExtractor2.features(in);
        ObjectToDoubleMap<String> features 
            = new ObjectToDoubleMap<String>();
        for (Map.Entry<String,? extends Number> entry1 : features1.entrySet()) {
            String initial = mPrefix + entry1.getKey() + mSeparator;
            double val1 = entry1.getValue().doubleValue();
            for (Map.Entry<String,? extends Number> entry2 : features2.entrySet()) {
                String feature = initial + entry2.getKey();
                double value = val1 * entry2.getValue().doubleValue();
                features.set(feature,value);
            }
        }
        return features;
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -2678228697747811841L;
        final InteractionFeatureExtractor<F> mExtractor;
        public Serializer() {
            this(null);
        }
        public Serializer(InteractionFeatureExtractor<F> extractor) {
            mExtractor = extractor;
        }
        @Override
        public void writeExternal(ObjectOutput out) 
            throws IOException {

            out.writeUTF(mExtractor.mPrefix);
            out.writeUTF(mExtractor.mSeparator);
            boolean same = mExtractor.mExtractor1 == mExtractor.mExtractor2;
            out.writeBoolean(same);
            out.writeObject(mExtractor.mExtractor1);
            if (!same)
                out.writeObject(mExtractor.mExtractor2);
        }
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            
            String prefix = in.readUTF();
            String separator = in.readUTF();
            boolean same = in.readBoolean();
            @SuppressWarnings("unchecked")
            FeatureExtractor<F> featureExtractor
                = (FeatureExtractor<F>)
                in.readObject();
            if (same) {
                return new InteractionFeatureExtractor<F>(prefix,separator,featureExtractor);
            }
            @SuppressWarnings("unchecked")
            FeatureExtractor<F> featureExtractor2
                = (FeatureExtractor<F>)
                in.readObject();
            return new InteractionFeatureExtractor<F>(prefix,separator,featureExtractor,featureExtractor2);
        }
    }

}

