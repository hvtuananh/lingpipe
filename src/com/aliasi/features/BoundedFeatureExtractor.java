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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code BoundedFeatureExtractor} provides a lower-bound and
 * upper-bound on feature values between which all values from a
 * contained base extractor are bounded.  Values greater than the
 * upper bound are replaced with the upper bound, and values less than
 * the lower bound are replaced with the lower bound.  Values may be
 * unbounded below by using {@link Double#NEGATIVE_INFINITY} as the
 * lower bound, and may be unbounded above by using {@link
 * Double#POSITIVE_INFINITY} the upper bound.
 * 
 * @author Mike Ross
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 * @param <E> The type of objects whose features are extracted.
 */
public class BoundedFeatureExtractor<E> 
    extends ModifiedFeatureExtractor<E> 
    implements Serializable {

    static final long serialVersionUID = -5628628360712035433L;

    private final double mMinValue;
    private final double mMaxValue;
    private final Number mMinValueNumber;
    private final Number mMaxValueNumber;

    /**
     * Construct a bounded feature extractor that bounds the feature
     * values produced by the specified extractor to be within the
     * specified minimum and maximum values.
     *
     * @param extractor Base feature extractor.
     * @param minValue Minimum value of a feature
     * @param maxValue Maximum value of a feature
     * @throws IllegalArgumentException If {@code minVal > maxVal}
     */
    public BoundedFeatureExtractor(FeatureExtractor<? super E> extractor, 
                                double minValue,
                                double maxValue) {
        super(extractor);
        if (minValue > maxValue) {
            String msg = "Require minValue <= maxValue."
                + " Found  minValue=" + minValue 
                + " maxValue=" + maxValue;
            throw new IllegalArgumentException(msg);
        }
        mMinValue = minValue;
        mMaxValue = maxValue;
        mMinValueNumber = Double.valueOf(minValue);
        mMaxValueNumber = Double.valueOf(maxValue);
    }

    /**
     * Return the bounded value corresponding to the specified value.
     * The feature name is ignored.
     *
     * @param feature Name of feature, which is ignored.
     * @return The bounded value.
     */
    @Override
    public Number filter(String feature, Number value) {
        double v = value.doubleValue();
        if (v < mMinValue)
            return mMinValueNumber;
        if (v > mMaxValue)
            return mMaxValueNumber;
        return value;
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 6365515337527915147L;
        private final BoundedFeatureExtractor<F> mBFExtractor;
        public Serializer() {
            this(null);
        }
        public Serializer(BoundedFeatureExtractor<F> extractor) {
            mBFExtractor = extractor;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeDouble(mBFExtractor.mMinValue);
            out.writeDouble(mBFExtractor.mMaxValue);
            out.writeObject(mBFExtractor.baseExtractor());
        }

        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            double minValue = in.readDouble();
            double maxValue = in.readDouble();
            @SuppressWarnings("unchecked")
            // required for deserialization
            FeatureExtractor<? super F> extractor = (FeatureExtractor<? super F>) in
                .readObject();
            return new BoundedFeatureExtractor<F>(extractor,minValue,maxValue);
        }
    }
}
